/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2960
 *  net.minecraft.class_4587
 */
package ru.recoin.implement.features.modules.particles.render;

import java.awt.Color;
import net.minecraft.class_2960;
import net.minecraft.class_4587;
import ru.recoin.api.feature.module.IParticleModule;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.particle.ParticleColorUtil;
import ru.recoin.implement.features.modules.particles.render.ParticleBatchRenderer;

public class ParticleRenderer {
    public static void render(class_4587 matrices, String mode, float size, float alpha, Color baseColor, String glyphTexture, float health, IParticleModule module, int colorOffset) {
        switch (mode) {
            case "Stars": {
                ParticleRenderer.queueLayeredTexturedParticle(matrices, class_2960.method_60654((String)"textures/particles/star.png"), size, alpha, module, baseColor, colorOffset);
                break;
            }
            case "Hearts": {
                ParticleRenderer.queueLayeredTexturedParticle(matrices, class_2960.method_60654((String)"textures/particles/heart.png"), size, alpha, module, baseColor, colorOffset);
                break;
            }
            case "Bloom": {
                ParticleRenderer.queueLayeredTexturedParticle(matrices, class_2960.method_60654((String)"textures/particles/firefly.png"), size, alpha, module, baseColor, colorOffset);
                break;
            }
            case "Blink": {
                ParticleRenderer.queueLayeredTexturedParticle(matrices, class_2960.method_60654((String)"textures/particles/blink.png"), size, alpha, module, baseColor, colorOffset);
                break;
            }
            case "Coron": {
                ParticleRenderer.queueLayeredTexturedParticle(matrices, class_2960.method_60654((String)"textures/particles/coron.png"), size, alpha, module, baseColor, colorOffset);
                break;
            }
            case "Dollar": {
                ParticleRenderer.queueLayeredTexturedParticle(matrices, class_2960.method_60654((String)"textures/particles/dollar.png"), size, alpha, module, baseColor, colorOffset);
                break;
            }
            case "Flame": {
                ParticleRenderer.queueLayeredTexturedParticle(matrices, class_2960.method_60654((String)"textures/particles/flame.png"), size, alpha, module, baseColor, colorOffset);
                break;
            }
            case "Snowflake": {
                ParticleRenderer.queueLayeredTexturedParticle(matrices, class_2960.method_60654((String)"textures/particles/snowflake.png"), size, alpha, module, baseColor, colorOffset);
                break;
            }
            case "Virus": {
                ParticleRenderer.queueLayeredTexturedParticle(matrices, class_2960.method_60654((String)"textures/particles/virus.png"), size, alpha, module, baseColor, colorOffset);
                break;
            }
            case "Sword": {
                ParticleRenderer.queueLayeredTexturedParticle(matrices, class_2960.method_60654((String)"textures/vanilla/sword.png"), size, alpha, module, baseColor, colorOffset);
                break;
            }
            case "Network": {
                matrices.method_22903();
                matrices.method_46416(size / 2.0f, size / 2.0f, 0.0f);
                matrices.method_22909();
                break;
            }
            case "Text": {
                String text = String.format("%.1f", Float.valueOf(health));
                Color textColor = health > 0.0f ? new Color(module.getHealColor()) : new Color(module.getDamageColor());
                int colorWithAlpha = ColorUtil.replAlpha(textColor.getRGB(), (int)((float)textColor.getAlpha() * alpha));
                ParticleBatchRenderer.queueTextParticle(matrices, text, colorWithAlpha);
                break;
            }
            case "Cube": {
                int color = ParticleRenderer.getColor(alpha, module, baseColor, colorOffset);
                ParticleBatchRenderer.queueCubeParticle(matrices, size, alpha, color);
                break;
            }
            case "Pyramid": {
                int color = ParticleRenderer.getColor(alpha, module, baseColor, colorOffset);
                ParticleBatchRenderer.queuePyramidParticle(matrices, size, alpha, color);
                break;
            }
            case "3D Lines": {
                int color = ParticleRenderer.getColor(alpha, module, baseColor, colorOffset);
                ParticleBatchRenderer.queueLine3DParticle(matrices, size, alpha, color);
            }
        }
    }

    private static void queueLayeredTexturedParticle(class_4587 matrices, class_2960 texture, float size, float alpha, IParticleModule module, Color baseColor, int colorOffset) {
        ParticleRenderer.queueTexturedParticle(matrices, texture, size, alpha, module, baseColor, colorOffset);
    }

    private static void queueTexturedParticle(class_4587 matrices, class_2960 texture, float size, float alpha, IParticleModule module, Color baseColor, int colorOffset) {
        if (module.getColorMode().isSelected("Sync")) {
            int color = ColorUtil.multAlpha(ColorUtil.getClientColor(), alpha);
            ParticleBatchRenderer.queueTexturedParticle(matrices, texture, size, alpha, color, false, null, null);
        } else if (module.getColorMode().isSelected("Vanilla")) {
            int color = ColorUtil.multAlpha(baseColor.getRGB(), alpha);
            ParticleBatchRenderer.queueTexturedParticle(matrices, texture, size, alpha, color, false, null, null);
        } else {
            int[] colors = module.getCustomColors();
            if (colors != null && colors.length > 0) {
                if (module.getColorAnimation().isSelected("Vertex")) {
                    ParticleBatchRenderer.queueTexturedParticle(matrices, texture, size, alpha, 0, true, colors, module);
                } else {
                    int color = ParticleColorUtil.getWaveColor(colors, alpha, colorOffset);
                    ParticleBatchRenderer.queueTexturedParticle(matrices, texture, size, alpha, color, false, null, null);
                }
            } else {
                int color = ColorUtil.multAlpha(-1, alpha);
                ParticleBatchRenderer.queueTexturedParticle(matrices, texture, size, alpha, color, false, null, null);
            }
        }
    }

    private static int getColor(float alpha, IParticleModule module, Color baseColor, int colorOffset) {
        if (module.getColorMode().isSelected("Sync")) {
            return ColorUtil.multAlpha(ColorUtil.getClientColor(), alpha);
        }
        if (module.getColorMode().isSelected("Vanilla")) {
            return ColorUtil.multAlpha(baseColor.getRGB(), alpha);
        }
        int[] colors = module.getCustomColors();
        if (colors != null && colors.length > 0) {
            if (module.getColorAnimation().isSelected("Wave")) {
                return ParticleColorUtil.getWaveColor(colors, alpha, colorOffset);
            }
            return ParticleColorUtil.getVertexGradientColor(colorOffset, colors, alpha);
        }
        return ColorUtil.multAlpha(-1, alpha);
    }

    public static void drawCenteredBloom(class_4587 matrices, float size, float alpha, IParticleModule module, Color baseColor, int colorOffset) {
        ParticleRenderer.queueBloomEffect(matrices, size, alpha, module, baseColor, colorOffset);
    }

    public static void drawCubeBloom(class_4587 matrices, float size, float alpha, IParticleModule module, Color baseColor, int colorOffset) {
        ParticleRenderer.drawCenteredBloom(matrices, size * 4.2f, alpha, module, baseColor, colorOffset);
    }

    public static void drawPyramidBloom(class_4587 matrices, float size, float alpha, IParticleModule module, Color baseColor, int colorOffset) {
        ParticleRenderer.drawCenteredBloom(matrices, size * 4.2f, alpha, module, baseColor, colorOffset);
    }

    private static void queueBloomEffect(class_4587 matrices, float size, float alpha, IParticleModule module, Color baseColor, int colorOffset) {
        int[] colors;
        if (module.getColorMode().isSelected("Custom") && (colors = module.getCustomColors()) != null && colors.length > 0 && module.getColorAnimation().isSelected("Vertex")) {
            ParticleBatchRenderer.queueBloomParticle(matrices, size, alpha, true, colors, module, colorOffset);
            return;
        }
        if (module.getColorMode().isSelected("Vanilla")) {
            int[] vanillaColor = new int[]{baseColor.getRGB()};
            ParticleBatchRenderer.queueBloomParticle(matrices, size, alpha, true, vanillaColor, module, colorOffset);
        } else {
            ParticleBatchRenderer.queueBloomParticle(matrices, size, alpha, false, null, module, colorOffset);
        }
    }
}

