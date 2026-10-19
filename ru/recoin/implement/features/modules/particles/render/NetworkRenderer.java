/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_243
 *  net.minecraft.class_287
 *  net.minecraft.class_289
 *  net.minecraft.class_290
 *  net.minecraft.class_310
 *  net.minecraft.class_4587
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package ru.recoin.implement.features.modules.particles.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.HashSet;
import java.util.List;
import net.minecraft.class_243;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import ru.recoin.api.feature.module.IParticleModule;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.particle.ParticleColorUtil;
import ru.recoin.common.util.render.LegacyRenderSystem;
import ru.recoin.common.util.spatial.SpatialGrid3D;
import ru.recoin.compat.render.BufferRenderer;
import ru.recoin.compat.render.ShaderProgramKeys;
import ru.recoin.implement.features.modules.particles.types.WorldParticle;

public class NetworkRenderer {
    public static void renderNetworkLinks(class_4587 matrices, List<WorldParticle> networkParticles, SpatialGrid3D<WorldParticle> spatialGrid, long currentTime, float maxLinkDistance, IParticleModule module) {
        if (networkParticles.size() < 2 || spatialGrid == null) {
            return;
        }
        LegacyRenderSystem.enableBlend();
        LegacyRenderSystem.defaultBlendFunc();
        LegacyRenderSystem.disableDepthTest();
        LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        Matrix4f matrix = matrices.method_23760().method_23761();
        class_287 bufferBuilder = class_289.method_1348().method_60827(VertexFormat.class_5596.field_29344, class_290.field_1576);
        LegacyRenderSystem.enableDepthTest();
        int lineCount = 0;
        HashSet<ParticlePair> processedPairs = new HashSet<ParticlePair>();
        class_243 cameraPos = class_310.method_1551().field_1773.method_19418().method_71156();
        for (WorldParticle p1 : networkParticles) {
            double posX1 = MathUtil.interpolate(p1.getPx(), p1.getX());
            double posY1 = MathUtil.interpolate(p1.getPy(), p1.getY()) + 0.1;
            double posZ1 = MathUtil.interpolate(p1.getPz(), p1.getZ());
            List<SpatialGrid3D.GridEntry<WorldParticle>> nearbyEntries = spatialGrid.queryRadiusWithPositions(posX1, posY1, posZ1, maxLinkDistance);
            for (SpatialGrid3D.GridEntry<WorldParticle> entry : nearbyEntries) {
                int color2;
                int color1;
                double posZ2;
                double dz;
                double posY2;
                double dy;
                ParticlePair pair;
                WorldParticle p2 = entry.getObject();
                if (p1 == p2 || processedPairs.contains(pair = new ParticlePair(p1, p2))) continue;
                processedPairs.add(pair);
                double posX2 = MathUtil.interpolate(p2.getPx(), p2.getX());
                double dx = posX2 - posX1;
                double dist = Math.sqrt(dx * dx + (dy = (posY2 = MathUtil.interpolate(p2.getPy(), p2.getY()) + 0.1) - posY1) * dy + (dz = (posZ2 = MathUtil.interpolate(p2.getPz(), p2.getZ())) - posZ1) * dz);
                if (!(dist < (double)maxLinkDistance) || !(dist > 0.01)) continue;
                float lineAlpha = (float)(1.0 - dist / (double)maxLinkDistance);
                float particleAlpha1 = p1.getAlpha();
                float particleAlpha2 = p2.getAlpha();
                float alpha = Math.min(lineAlpha, Math.min(particleAlpha1, particleAlpha2));
                if (module.getColorMode().isSelected("Sync")) {
                    int baseColor = ColorUtil.getClientColor();
                    color2 = color1 = ColorUtil.multAlpha(baseColor, alpha);
                } else if (module.getColorMode().isSelected("Vanilla")) {
                    color1 = ColorUtil.multAlpha(p1.getColor().getRGB(), alpha);
                    color2 = ColorUtil.multAlpha(p2.getColor().getRGB(), alpha);
                } else {
                    int[] colors = module.getCustomColors();
                    if (colors != null && colors.length > 0) {
                        if (module.getColorAnimation().isSelected("Vertex")) {
                            int offset1 = System.identityHashCode(p1) % 360;
                            int offset2 = System.identityHashCode(p2) % 360;
                            color1 = ParticleColorUtil.getVertexGradientColor(offset1, colors, alpha);
                            color2 = ParticleColorUtil.getVertexGradientColor(offset2, colors, alpha);
                        } else {
                            color1 = ParticleColorUtil.getWaveColor(colors, alpha, p1.getColorOffset());
                            color2 = ParticleColorUtil.getWaveColor(colors, alpha, p2.getColorOffset());
                        }
                    } else {
                        color2 = color1 = ColorUtil.multAlpha(-1, alpha);
                    }
                }
                bufferBuilder.method_22918((Matrix4fc)matrix, (float)(posX1 - cameraPos.field_1352), (float)(posY1 - cameraPos.field_1351), (float)(posZ1 - cameraPos.field_1350)).method_22915((float)(color1 >> 16 & 0xFF) / 255.0f, (float)(color1 >> 8 & 0xFF) / 255.0f, (float)(color1 & 0xFF) / 255.0f, (float)(color1 >> 24 & 0xFF) / 255.0f);
                bufferBuilder.method_22918((Matrix4fc)matrix, (float)(posX2 - cameraPos.field_1352), (float)(posY2 - cameraPos.field_1351), (float)(posZ2 - cameraPos.field_1350)).method_22915((float)(color2 >> 16 & 0xFF) / 255.0f, (float)(color2 >> 8 & 0xFF) / 255.0f, (float)(color2 & 0xFF) / 255.0f, (float)(color2 >> 24 & 0xFF) / 255.0f);
                ++lineCount;
            }
        }
        if (lineCount > 0) {
            BufferRenderer.drawWithGlobalProgram(bufferBuilder.method_60800());
        }
        LegacyRenderSystem.enableDepthTest();
        LegacyRenderSystem.disableBlend();
        LegacyRenderSystem.disableDepthTest();
    }

    private record ParticlePair(WorldParticle p1, WorldParticle p2) {
        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        @Override
        public boolean equals(Object obj) {
            WorldParticle other1;
            WorldParticle worldParticle;
            if (!(obj instanceof ParticlePair)) return false;
            ParticlePair particlePair = (ParticlePair)obj;
            try {
                other1 = worldParticle = particlePair.p1();
            }
            catch (Throwable throwable) {
                throw new MatchException(throwable.toString(), throwable);
            }
            WorldParticle other2 = worldParticle = particlePair.p2();
            if (this.p1 == other1) {
                if (this.p2 == other2) return true;
            }
            if (this.p1 != other2) return false;
            if (this.p2 != other1) return false;
            return true;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this.p1) + System.identityHashCode(this.p2);
        }
    }
}

