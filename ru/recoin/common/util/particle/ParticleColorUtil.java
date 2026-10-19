/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.common.util.particle;

import ru.recoin.common.util.color.ColorUtil;

public class ParticleColorUtil {
    public static int getWaveColor(int[] colors, float alpha, int offset) {
        if (colors == null || colors.length == 0) {
            return ColorUtil.multAlpha(-1, alpha);
        }
        if (colors.length == 1) {
            return ColorUtil.multAlpha(colors[0], alpha);
        }
        if (colors.length == 2) {
            int angle = (int)((System.currentTimeMillis() / 8L + (long)offset) % 360L);
            angle = angle >= 180 ? 360 - angle : angle;
            return ColorUtil.multAlpha(ColorUtil.overCol(colors[0], colors[1], (float)angle / 180.0f), alpha);
        }
        float timeProgress = (float)((System.currentTimeMillis() / 10L + (long)offset) % ((long)colors.length * 360L)) / 360.0f;
        int index1 = (int)Math.floor(timeProgress) % colors.length;
        int index2 = (index1 + 1) % colors.length;
        float lerp = timeProgress - (float)((int)Math.floor(timeProgress));
        return ColorUtil.multAlpha(ColorUtil.overCol(colors[index1], colors[index2], lerp), alpha);
    }

    public static int getWaveColor(int[] colors, float alpha) {
        return ParticleColorUtil.getWaveColor(colors, alpha, 0);
    }

    public static int getVertexGradientColor(int angleOffset, int[] colors, float alpha) {
        if (colors == null || colors.length == 0) {
            return ColorUtil.multAlpha(-1, alpha);
        }
        if (colors.length == 1) {
            return ColorUtil.multAlpha(colors[0], alpha);
        }
        int angle = (int)((System.currentTimeMillis() / 8L + (long)angleOffset) % 360L);
        if (colors.length == 2) {
            angle = angle >= 180 ? 360 - angle : angle;
            return ColorUtil.multAlpha(ColorUtil.overCol(colors[0], colors[1], (float)angle / 180.0f), alpha);
        }
        float progress = (float)angle / 360.0f;
        float colorIndex = progress * (float)colors.length;
        int index1 = (int)colorIndex % colors.length;
        int index2 = (index1 + 1) % colors.length;
        float lerp = colorIndex - (float)((int)colorIndex);
        return ColorUtil.multAlpha(ColorUtil.overCol(colors[index1], colors[index2], lerp), alpha);
    }
}

