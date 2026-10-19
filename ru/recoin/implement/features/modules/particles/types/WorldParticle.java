/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_243
 *  net.minecraft.class_4587
 *  net.minecraft.class_7833
 *  org.joml.Quaternionfc
 */
package ru.recoin.implement.features.modules.particles.types;

import java.awt.Color;
import net.minecraft.class_243;
import net.minecraft.class_4587;
import net.minecraft.class_7833;
import org.joml.Quaternionfc;
import ru.recoin.api.feature.module.IParticleModule;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.particle.AbstractParticle;
import ru.recoin.implement.features.modules.particles.render.ParticleRenderer;

public class WorldParticle
extends AbstractParticle {
    private final String particleMode;
    private final String glyphTexture;
    private final float health;
    private final IParticleModule module;

    public WorldParticle(double x, double y, double z, Color color, float rotationAngle, float rotationSpeed, float health, String mode, String physics, String glyphTexture, float lifeTime, float scale, float speed, IParticleModule module) {
        super(x, y, z, color, rotationAngle, rotationSpeed, lifeTime, scale, physics, !mode.equals("Network"));
        this.health = health;
        this.particleMode = mode;
        this.glyphTexture = glyphTexture;
        this.module = module;
        this.initMotion(speed);
    }

    @Override
    public void render(class_4587 matrixStack, long currentTime) {
        boolean is3DGeometric;
        float size = this.scale;
        float renderScale = this.particleMode.equals("Text") ? 0.025f * size : 0.07f;
        float alpha = this.cachedAlpha;
        double posX = MathUtil.interpolate(this.px, this.x);
        double posY = MathUtil.interpolate(this.py, this.y);
        double posZ = MathUtil.interpolate(this.pz, this.z);
        class_243 cameraPos = WorldParticle.mc.field_1773.method_19418().method_71156();
        float interpolatedAngle = MathUtil.interpolate(this.prevRotationAngle, this.rotationAngle);
        matrixStack.method_22903();
        matrixStack.method_22904(posX - cameraPos.field_1352, posY - cameraPos.field_1351, posZ - cameraPos.field_1350);
        matrixStack.method_22905(renderScale, renderScale, renderScale);
        boolean bl = is3DGeometric = this.particleMode.equals("Cube") || this.particleMode.equals("Pyramid") || this.particleMode.equals("3D Lines");
        if (is3DGeometric) {
            matrixStack.method_22903();
            matrixStack.method_22907((Quaternionfc)WorldParticle.mc.field_1773.method_19418().method_23767());
            float bloomScale = this.particleMode.equals("3D Lines") ? size * 3.5f : size * 4.2f;
            ParticleRenderer.drawCenteredBloom(matrixStack, bloomScale, alpha, this.module, this.color, this.colorOffset);
            matrixStack.method_22909();
            if (this.particleMode.equals("Cube")) {
                matrixStack.method_22907((Quaternionfc)class_7833.field_40716.rotationDegrees(interpolatedAngle));
                matrixStack.method_22907((Quaternionfc)class_7833.field_40714.rotationDegrees(interpolatedAngle * 0.7f));
                matrixStack.method_22907((Quaternionfc)class_7833.field_40718.rotationDegrees(interpolatedAngle * 0.35f));
            } else if (this.particleMode.equals("Pyramid")) {
                matrixStack.method_22907((Quaternionfc)class_7833.field_40716.rotationDegrees(interpolatedAngle));
                matrixStack.method_22907((Quaternionfc)class_7833.field_40714.rotationDegrees(interpolatedAngle * 0.6f));
            } else {
                matrixStack.method_22907((Quaternionfc)class_7833.field_40716.rotationDegrees(interpolatedAngle));
                matrixStack.method_22907((Quaternionfc)class_7833.field_40714.rotationDegrees(interpolatedAngle * 0.8f));
                matrixStack.method_22907((Quaternionfc)class_7833.field_40718.rotationDegrees(interpolatedAngle * 0.5f));
            }
            ParticleRenderer.render(matrixStack, this.particleMode, size, alpha, this.color, this.glyphTexture, this.health, this.module, this.colorOffset);
        } else {
            boolean isZAxisLocked;
            matrixStack.method_22907((Quaternionfc)WorldParticle.mc.field_1773.method_19418().method_23767());
            matrixStack.method_46416(size / 2.0f, size / 2.0f, size / 2.0f);
            boolean bl2 = isZAxisLocked = this.particleMode.equals("Text") || this.particleMode.equals("Flame");
            if (isZAxisLocked) {
                matrixStack.method_22907((Quaternionfc)class_7833.field_40718.rotationDegrees(180.0f));
            } else {
                matrixStack.method_22907((Quaternionfc)class_7833.field_40718.rotationDegrees(interpolatedAngle));
            }
            matrixStack.method_46416(-size / 2.0f, -size / 2.0f, -size / 2.0f);
            ParticleRenderer.render(matrixStack, this.particleMode, size, alpha, this.color, this.glyphTexture, this.health, this.module, this.colorOffset);
        }
        matrixStack.method_22909();
    }

    public String getParticleMode() {
        return this.particleMode;
    }

    public float getHealth() {
        return this.health;
    }
}

