/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2561
 *  net.minecraft.class_2596
 *  net.minecraft.class_266
 *  net.minecraft.class_268
 *  net.minecraft.class_269
 *  net.minecraft.class_270
 *  net.minecraft.class_2761
 *  net.minecraft.class_3532
 *  net.minecraft.class_642
 *  net.minecraft.class_8646
 *  net.minecraft.class_9011
 *  org.apache.commons.lang3.StringUtils
 */
package ru.recoin.common.util.world;

import java.util.Locale;
import java.util.Objects;
import net.minecraft.class_2561;
import net.minecraft.class_2596;
import net.minecraft.class_266;
import net.minecraft.class_268;
import net.minecraft.class_269;
import net.minecraft.class_270;
import net.minecraft.class_2761;
import net.minecraft.class_3532;
import net.minecraft.class_642;
import net.minecraft.class_8646;
import net.minecraft.class_9011;
import org.apache.commons.lang3.StringUtils;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.other.StopWatch;
import ru.recoin.implement.events.packet.PacketEvent;

public final class ServerUtil
implements QuickImports {
    private static final StopWatch pvpWatch = new StopWatch();
    public static String server = "Vanilla";
    public static float TPS = 20.0f;
    public static long timestamp;
    public static class_642 lastServerInfo;
    public static class_642 lastAttemptedServerInfo;
    public static int anarchy;
    public static boolean pvpEnd;

    public static void tick() {
        if (mc.method_1558() != null) {
            lastServerInfo = mc.method_1558();
        } else if (mc.method_1562() != null && mc.method_1562().method_45734() != null) {
            lastServerInfo = mc.method_1562().method_45734();
        }
        anarchy = ServerUtil.getAnarchyMode();
        server = ServerUtil.getServer();
        pvpEnd = ServerUtil.inPvpEnd();
        if (ServerUtil.inPvp()) {
            pvpWatch.reset();
        }
    }

    public static void packet(PacketEvent e) {
        class_2596<?> class_25962 = e.getPacket();
        Objects.requireNonNull(class_25962);
        class_2596<?> class_25963 = class_25962;
        if (class_25963 instanceof class_2761 time) {
            long nanoTime = System.nanoTime();
            float maxTPS = 20.0f;
            float rawTPS = maxTPS * (1.0E9f / (float)(nanoTime - timestamp));
            TPS = class_3532.method_15363((float)rawTPS, (float)0.0f, (float)maxTPS);
            timestamp = nanoTime;
        }
    }

    private static String getServer() {
        if (PlayerIntersectionUtil.nullCheck() || mc.method_1562() == null || mc.method_1562().method_45734() == null || mc.method_1562().method_52790() == null) {
            return "Vanilla";
        }
        String serverIp = ServerUtil.mc.method_1562().method_45734().field_3761.toLowerCase();
        String brand = mc.method_1562().method_52790().toLowerCase();
        if (brand.contains("botfilter")) {
            return "FunTime";
        }
        if (brand.contains("\u00a76spooky\u00a7ccore") || serverIp.contains("spooky")) {
            return "SpookyTime";
        }
        if (serverIp.contains("funtime") || serverIp.contains("skytime") || serverIp.contains("space-times") || serverIp.contains("funsky")) {
            return "CopyTime";
        }
        if (brand.contains("holyworld") || brand.contains("vk.com/idwok")) {
            return "HolyWorld";
        }
        if (serverIp.contains("holyfake")) {
            return "HolyFake";
        }
        if (serverIp.contains("reallyworld")) {
            return "ReallyWorld";
        }
        if (serverIp.contains("dexland")) {
            return "DexLand";
        }
        if (serverIp.contains("mineblaze")) {
            return "MineBlaze";
        }
        return "Vanilla";
    }

    public static boolean isSpookyTime() {
        String addr;
        if ("SpookyTime".equalsIgnoreCase(server)) {
            return true;
        }
        if (lastServerInfo != null && ServerUtil.lastServerInfo.field_3761 != null && ((addr = ServerUtil.lastServerInfo.field_3761.toLowerCase(Locale.ROOT)).contains("spooky") || addr.contains("spookytime"))) {
            return true;
        }
        return mc.method_1562() != null && mc.method_1562().method_45734() != null && ((addr = ServerUtil.mc.method_1562().method_45734().field_3761.toLowerCase(Locale.ROOT)).contains("spooky") || addr.contains("spookytime"));
    }

    private static int getAnarchyMode() {
        if (ServerUtil.mc.field_1687 == null) {
            return -1;
        }
        class_269 scoreboard = ServerUtil.mc.field_1687.method_8428();
        if (scoreboard == null) {
            return -1;
        }
        class_266 objective = scoreboard.method_1189(class_8646.field_45157);
        switch (server) {
            case "FunTime": 
            case "SpookyTime": 
            case "CopyTime": {
                String allDigits;
                String digits;
                if (objective == null) break;
                String clean = objective.method_1114().getString().replaceAll("\u00a7.", "").trim();
                String[] parts = clean.split("-");
                if (parts.length > 1 && !(digits = parts[1].replaceAll("\\D", "")).isEmpty()) {
                    try {
                        return Integer.parseInt(digits);
                    }
                    catch (NumberFormatException numberFormatException) {
                        // empty catch block
                    }
                }
                if ((allDigits = clean.replaceAll("\\D", "")).isEmpty()) break;
                try {
                    return Integer.parseInt(allDigits);
                }
                catch (NumberFormatException numberFormatException) {
                    break;
                }
            }
            case "HolyFake":
            case "HolyWorld": {
                if (objective == null) break;
                for (class_9011 scoreboardEntry : scoreboard.method_1184(objective)) {
                    String string;
                    String text = class_268.method_1142((class_270)scoreboard.method_1164(scoreboardEntry.comp_2127()), (class_2561)scoreboardEntry.method_55387()).getString();
                    if (text.isEmpty() || (string = StringUtils.substringBetween((String)text, (String)"#", (String)" -\u0432\u2014\u2020-")) == null || string.isEmpty()) continue;
                    try {
                        return Integer.parseInt(string.replace(" (1.20)", "").trim());
                    }
                    catch (NumberFormatException numberFormatException) {
                    }
                }
                break;
            }
        }
        return -1;
    }

    public static boolean isPvp() {
        return ServerUtil.inPvp() || !pvpWatch.finished(300.0);
    }

    public static boolean inPvp() {
        if (ServerUtil.mc.field_1705 == null || ServerUtil.mc.field_1705.method_1740() == null) {
            return false;
        }
        return ServerUtil.mc.field_1705.method_1740().field_2060.values().stream().map(c -> c.method_5414().getString().toLowerCase()).anyMatch(s -> {
            if (s.contains("\u0437\u0430\u043a\u043e\u043d\u0447") || s.contains("\u043e\u043a\u043e\u043d\u0447") || s.contains("\u0437\u0430\u0432\u0435\u0440\u0448") || s.contains(": 0") || s.contains(" 0 \u0441") || s.contains("0s") || s.contains(" 0s")) {
                return false;
            }
            return s.contains("pvp") || s.contains("\u043f\u0432\u043f") || s.contains("\u0440\u043f\u0432\u043f") || s.contains("\u0434\u0443\u044d\u043b\u044c") || s.contains("\u0434\u0443\u0435\u043b\u044c");
        });
    }

    public static boolean inPvpEnd() {
        if (ServerUtil.mc.field_1705 == null || ServerUtil.mc.field_1705.method_1740() == null) {
            return true;
        }
        return ServerUtil.mc.field_1705.method_1740().field_2060.values().stream().map(c -> c.method_5414().getString().toLowerCase()).anyMatch(s -> s.contains("\u0437\u0430\u043a\u043e\u043d\u0447") || s.contains("\u043e\u043a\u043e\u043d\u0447") || s.contains("\u0437\u0430\u0432\u0435\u0440\u0448") || s.contains(" 0 ") || s.endsWith(" 0") || s.contains("0s") || s.contains("0\u0441"));
    }

    public static String getWorldType() {
        return ServerUtil.mc.field_1687.method_27983().method_29177().method_12832();
    }

    public static boolean isCopyTime() {
        return server.equals("CopyTime") || server.equals("SpookyTime") || server.equals("FunTime");
    }

    public static boolean isFunTime() {
        return server.equals("FunTime");
    }

    public static boolean isReallyWorld() {
        return server.equals("ReallyWorld");
    }

    public static boolean isHolyWorld() {
        return server.equals("HolyWorld");
    }

    public static boolean isHolyFake() {
        return server.equals("HolyFake");
    }

    public static boolean isVanilla() {
        return server.equals("Vanilla");
    }

    private ServerUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static int getAnarchy() {
        return anarchy;
    }

    public static boolean isPvpEnd() {
        return pvpEnd;
    }
}
