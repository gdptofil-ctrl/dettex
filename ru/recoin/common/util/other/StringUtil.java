/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.glfw.GLFW
 */
package ru.recoin.common.util.other;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.lwjgl.glfw.GLFW;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.core.client.UserProfile;

public final class StringUtil {
    private static final int ELLIPSIZE_CACHE_LIMIT = 512;
    private static final Map<EllipsizeKey, String> ELLIPSIZE_CACHE = new LinkedHashMap<EllipsizeKey, String>(512, 0.75f, true){

        @Override
        protected boolean removeEldestEntry(Map.Entry<EllipsizeKey, String> eldest) {
            return this.size() > 512;
        }
    };

    public static String randomString(int length) {
        return IntStream.range(0, length).mapToObj(operand -> String.valueOf((char)new Random().nextInt(97, 123))).collect(Collectors.joining());
    }

    public static String getBindName(int key) {
        String bind;
        if (key < 0) {
            return "N/A";
        }
        if (key == 0) {
            return "N/A";
        }
        String directName = StringUtil.getDirectKeyName(key);
        if (directName != null) {
            return directName;
        }
        try {
            bind = PlayerIntersectionUtil.getKeyType(key).method_1447(key).method_1441().replace("key.keyboard.", "").replace("key.mouse.", "M").replace(".", " ").toUpperCase();
        }
        catch (Exception e) {
            bind = "UNKNOWN";
        }
        if (bind.isEmpty() || bind.equals("UNKNOWN") || bind.equals("?") || bind.contains("UNKNOWN")) {
            String glfwName;
            String string = glfwName = key >= 8 ? GLFW.glfwGetKeyName((int)key, (int)0) : null;
            if (glfwName != null && !glfwName.isBlank()) {
                return glfwName.toUpperCase();
            }
            return key >= 8 ? "KEY " + key : "M" + key;
        }
        return switch (bind) {
            case "BACKSLASH" -> "\\";
            case "SLASH" -> "/";
            case "LEFT BRACKET" -> "LBR";
            case "RIGHT BRACKET" -> "RBR";
            case "SEMICOLON" -> ";";
            case "APOSTROPHE" -> "'";
            case "COMMA" -> ",";
            case "PERIOD" -> ".";
            case "MINUS" -> "-";
            case "EQUAL" -> "=";
            case "GRAVE ACCENT" -> "`";
            default -> bind;
        };
    }

    private static String getDirectKeyName(int key) {
        if (key >= 65 && key <= 90) {
            return String.valueOf((char)(65 + (key - 65)));
        }
        if (key >= 48 && key <= 57) {
            return String.valueOf((char)(48 + (key - 48)));
        }
        if (key >= 290 && key <= 314) {
            return "F" + (1 + key - 290);
        }
        return switch (key) {
            case 32 -> "SPACE";
            case 39 -> "'";
            case 44 -> ",";
            case 45 -> "-";
            case 46 -> ".";
            case 47 -> "/";
            case 59 -> ";";
            case 61 -> "=";
            case 91 -> "LBR";
            case 92 -> "\\";
            case 93 -> "RBR";
            case 96 -> "`";
            case 256 -> "ESC";
            case 257 -> "ENTER";
            case 258 -> "TAB";
            case 259 -> "BSP";
            case 260 -> "INS";
            case 261 -> "DEL";
            case 262 -> "RIGHT";
            case 263 -> "LEFT";
            case 264 -> "DOWN";
            case 265 -> "UP";
            case 266 -> "PGUP";
            case 267 -> "PGDN";
            case 268 -> "HOME";
            case 269 -> "END";
            case 280 -> "CAPS";
            case 281 -> "SCRL";
            case 282 -> "NUM";
            case 283 -> "PRT";
            case 284 -> "PAUSE";
            case 340 -> "LSHIFT";
            case 341 -> "LCTRL";
            case 342 -> "LALT";
            case 343 -> "LSUPER";
            case 344 -> "RSHIFT";
            case 345 -> "RCTRL";
            case 346 -> "RALT";
            case 347 -> "RSUPER";
            case 348 -> "MENU";
            case 320 -> "NUM0";
            case 321 -> "NUM1";
            case 322 -> "NUM2";
            case 323 -> "NUM3";
            case 324 -> "NUM4";
            case 325 -> "NUM5";
            case 326 -> "NUM6";
            case 327 -> "NUM7";
            case 328 -> "NUM8";
            case 329 -> "NUM9";
            case 330 -> "NUM.";
            case 331 -> "NUM/";
            case 332 -> "NUM*";
            case 333 -> "NUM-";
            case 334 -> "NUM+";
            case 335 -> "NUMENT";
            case 336 -> "NUM=";
            default -> null;
        };
    }

    public static String wrap(String input, int width, int size) {
        String[] words = input.split(" ");
        StringBuilder output = new StringBuilder();
        float lineWidth = 0.0f;
        for (String word : words) {
            float wordWidth = Fonts.getSize(size).getStringWidth(word);
            if (lineWidth + wordWidth > (float)width) {
                output.append("\n");
                lineWidth = 0.0f;
            } else if (lineWidth > 0.0f) {
                output.append(" ");
                lineWidth += Fonts.getSize(size).getStringWidth(" ");
            }
            output.append(word);
            lineWidth += wordWidth;
        }
        return output.toString();
    }

    public static int lineCount(String text) {
        if (text == null || text.isEmpty()) {
            return 1;
        }
        return text.split("\n", -1).length;
    }

    public static String ellipsize(String text, float width, int size) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        EllipsizeKey key = new EllipsizeKey(text, Float.floatToIntBits(width), size);
        String cached = ELLIPSIZE_CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        FontRenderer font = Fonts.getMenuSize(size);
        if (font.getStringWidth(text) <= width) {
            ELLIPSIZE_CACHE.put(key, text);
            return text;
        }
        String ellipsis = "...";
        int best = 0;
        int left = 1;
        int right = text.length();
        while (left <= right) {
            int mid = left + right >>> 1;
            if (font.getStringWidth(text.substring(0, mid) + ellipsis) <= width) {
                best = mid;
                left = mid + 1;
                continue;
            }
            right = mid - 1;
        }
        String result = best == 0 ? ellipsis : text.substring(0, best) + ellipsis;
        ELLIPSIZE_CACHE.put(key, result);
        return result;
    }

    public static String getUserRole() {
        return switch (UserProfile.getRole()) {
            case "DEVELOPER" -> "DEVELOPER";
            case "ADMIN" -> "ADMIN";
            default -> "USER";
        };
    }

    public static String getDuration(int time) {
        int mins = time / 60;
        int seconds = time % 60;
        return mins + ":" + (seconds < 10 ? "0" : "") + seconds;
    }

    private StringUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    private record EllipsizeKey(String text, int widthBits, int size) {
    }
}

