/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_5250
 */
package ru.recoin.common;

import java.util.Arrays;
import java.util.stream.Stream;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_5250;
import ru.recoin.implement.features.modules.render.Hud;

public interface QuickLogger {
    public static class_2561 getPrefix() {
        int start = -9663233;
        try {
            start = Hud.getInstance().themeGradientStart();
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        int end = QuickLogger.darkenColor(start, 0.45f);
        String prefix = "DettexClient";
        class_5250 text = class_2561.method_43473();
        for (int i = 0; i < prefix.length(); ++i) {
            float progress = prefix.length() <= 1 ? 0.0f : (float)i / (float)(prefix.length() - 1);
            int color = QuickLogger.interpolateColor(start, end, progress);
            text.method_10852((class_2561)class_2561.method_43470((String)String.valueOf(prefix.charAt(i))).method_27694(style -> style.method_36139(color).method_10982(Boolean.valueOf(true))));
        }
        return text.method_10852((class_2561)class_2561.method_43470((String)" \u00bb ").method_27692(class_124.field_1063));
    }

    public static class_2561 clientMessage(class_2561 message) {
        return class_2561.method_43473().method_10852(QuickLogger.getPrefix()).method_10852((class_2561)class_2561.method_43470((String)" ")).method_10852(message);
    }

    private static int interpolateColor(int from, int to, float progress) {
        int fromRed = from >> 16 & 0xFF;
        int fromGreen = from >> 8 & 0xFF;
        int fromBlue = from & 0xFF;
        int red = Math.round((float)fromRed + (float)((to >> 16 & 0xFF) - fromRed) * progress);
        int green = Math.round((float)fromGreen + (float)((to >> 8 & 0xFF) - fromGreen) * progress);
        int blue = Math.round((float)fromBlue + (float)((to & 0xFF) - fromBlue) * progress);
        return red << 16 | green << 8 | blue;
    }

    private static int darkenColor(int color, float brightness) {
        int red = Math.round((float)(color >> 16 & 0xFF) * brightness);
        int green = Math.round((float)(color >> 8 & 0xFF) * brightness);
        int blue = Math.round((float)(color & 0xFF) * brightness);
        return red << 16 | green << 8 | blue;
    }

    default public void logDirect(class_2561 ... components) {
        class_5250 message = class_2561.method_43470((String)"");
        Arrays.asList(components).forEach(arg_0 -> ((class_5250)message).method_10852(arg_0));
        class_2561 component = QuickLogger.clientMessage((class_2561)message);
        if (class_310.method_1551().field_1724 != null) {
            class_310.method_1551().field_1705.method_1743().method_1812(component);
        }
    }

    default public void logDirect(String message, class_124 color) {
        Stream.of(message.split("\n")).forEach(line -> {
            class_5250 component = class_2561.method_43470((String)line.replace("\t", "    "));
            component.method_10862(component.method_10866().method_10977(color));
            this.logDirect(new class_2561[]{component});
        });
    }

    default public void logDirect(String message) {
        this.logDirect(message, class_124.field_1080);
    }
}

