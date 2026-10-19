/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2338
 *  net.minecraft.class_243
 *  net.minecraft.class_4184
 *  net.minecraft.class_4587
 *  org.joml.Matrix4f
 *  org.joml.Quaternionfc
 */
package ru.recoin.implement.features.modules.particles.types;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import org.joml.Matrix4f;
import org.joml.Quaternionfc;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.spatial.BlockCollisionGrid;
import ru.recoin.implement.features.modules.particles.render.ParticleBatchRenderer;

public class FireFlyParticle
implements QuickImports {
    private final List<FireFlyTrail> trails = new ArrayList<FireFlyTrail>();
    private double prevPosX;
    private double prevPosY;
    private double prevPosZ;
    private double posX;
    private double posY;
    private double posZ;
    private float motionX;
    private float motionY;
    private float motionZ;
    private int age;
    private final int maxAge;
    private final Color color;
    private final int[] customColors;
    private final float size;
    private final int trailLength;
    private boolean visible = true;

    public FireFlyParticle(double posX, double posY, double posZ, Color color, int[] customColors, float size, int trailLength) {
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.prevPosX = posX;
        this.prevPosY = posY;
        this.prevPosZ = posZ;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        this.motionX = (float)((random.nextDouble() * 2.0 - 1.0) * 0.2);
        this.motionY = (float)((random.nextDouble() * 2.0 - 1.0) * 0.1);
        this.motionZ = (float)((random.nextDouble() * 2.0 - 1.0) * 0.2);
        this.maxAge = this.age = random.nextInt(100, 301);
        this.color = color;
        this.customColors = customColors;
        this.size = size;
        this.trailLength = trailLength;
    }

    public boolean tick(Predicate<FireFlyParticle> visibilityChecker) {
        return this.tick(visibilityChecker, null);
    }

    public boolean tick(Predicate<FireFlyParticle> visibilityChecker, BlockCollisionGrid collisionGrid) {
        if (FireFlyParticle.mc.field_1724 == null || FireFlyParticle.mc.field_1687 == null) {
            return true;
        }
        if (visibilityChecker != null) {
            this.visible = visibilityChecker.test(this);
        }
        double distSq = FireFlyParticle.mc.field_1724.method_5649(this.posX, this.posY, this.posZ);
        this.age = !this.visible ? (this.age -= 2) : (distSq > 100.0 ? (this.age -= 4) : (!FireFlyParticle.mc.field_1687.method_8320(new class_2338((int)this.posX, (int)this.posY, (int)this.posZ)).method_26215() ? (this.age -= 8) : --this.age));
        if (this.age < 0) {
            return true;
        }
        this.trails.removeIf(FireFlyTrail::update);
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        if (collisionGrid != null) {
            this.moveWithCollision(collisionGrid);
        } else {
            this.posX += (double)this.motionX;
            this.posY += (double)this.motionY;
            this.posZ += (double)this.motionZ;
        }
        class_243 lastPos = this.trails.isEmpty() ? new class_243(this.prevPosX, this.prevPosY, this.prevPosZ) : this.trails.getLast().currentPos;
        this.trails.add(new FireFlyTrail(lastPos, new class_243(this.posX, this.posY, this.posZ), this.color, 0.3f, this.trailLength));
        this.motionX *= 0.99f;
        this.motionY *= 0.99f;
        this.motionZ *= 0.99f;
        return false;
    }

    private void moveWithCollision(BlockCollisionGrid collisionGrid) {
        if (!this.tryMove(collisionGrid, this.motionX, 0.0, 0.0)) {
            this.motionX = this.bounce(this.motionX, 0.45f);
        }
        if (!this.tryMove(collisionGrid, 0.0, this.motionY, 0.0)) {
            this.motionY = this.bounce(this.motionY, 0.45f);
            this.motionX *= 0.75f;
            this.motionZ *= 0.75f;
        }
        if (!this.tryMove(collisionGrid, 0.0, 0.0, this.motionZ)) {
            this.motionZ = this.bounce(this.motionZ, 0.45f);
        }
    }

    private boolean tryMove(BlockCollisionGrid collisionGrid, double dx, double dy, double dz) {
        double nextX = this.posX + dx;
        double nextY = this.posY + dy;
        double nextZ = this.posZ + dz;
        if (collisionGrid.hasCollision((float)nextX, (float)nextY, (float)nextZ)) {
            return false;
        }
        this.posX = nextX;
        this.posY = nextY;
        this.posZ = nextZ;
        return true;
    }

    private float bounce(float motion, float strength) {
        float bounced = -motion * strength;
        return Math.abs(bounced) < 0.0025f ? 0.0f : bounced;
    }

    private int getGradientColor(float trailPosition, float alpha) {
        if (this.customColors == null || this.customColors.length == 0) {
            return ColorUtil.replAlpha(this.color.getRGB(), (int)(alpha * 255.0f));
        }
        if (this.customColors.length == 1) {
            return ColorUtil.replAlpha(this.customColors[0], (int)(alpha * 255.0f));
        }
        float normalizedPosition = Math.max(0.0f, Math.min(1.0f, trailPosition));
        float totalSegments = this.customColors.length - 1;
        float scaledPosition = normalizedPosition * totalSegments;
        int segmentIndex = Math.min((int)Math.floor(scaledPosition), this.customColors.length - 2);
        float localPosition = scaledPosition - (float)segmentIndex;
        int color1 = this.customColors[segmentIndex];
        int color2 = this.customColors[segmentIndex + 1];
        int interpolated = this.interpolateColor(color1, color2, localPosition);
        return ColorUtil.replAlpha(interpolated, (int)(alpha * 255.0f));
    }

    private int interpolateColor(int color1, int color2, float factor) {
        int a1 = color1 >> 24 & 0xFF;
        int r1 = color1 >> 16 & 0xFF;
        int g1 = color1 >> 8 & 0xFF;
        int b1 = color1 & 0xFF;
        int a2 = color2 >> 24 & 0xFF;
        int r2 = color2 >> 16 & 0xFF;
        int g2 = color2 >> 8 & 0xFF;
        int b2 = color2 & 0xFF;
        int a = (int)((float)a1 + (float)(a2 - a1) * factor);
        int r = (int)((float)r1 + (float)(r2 - r1) * factor);
        int g = (int)((float)g1 + (float)(g2 - g1) * factor);
        int b = (int)((float)b1 + (float)(b2 - b1) * factor);
        return a << 24 | r << 16 | g << 8 | b;
    }

    public void render(class_4587 stack) {
        if (this.trails.isEmpty()) {
            return;
        }
        class_4184 camera = FireFlyParticle.mc.field_1773.method_19418();
        class_243 cameraPos = camera.method_71156();
        float tickDelta = mc.method_61966().method_60637(true);
        class_4587 matrices = new class_4587();
        int totalTrails = this.trails.size();
        for (int i = 0; i < totalTrails; ++i) {
            FireFlyTrail trail = this.trails.get(i);
            double renderX = trail.prevPos.field_1352 + (trail.currentPos.field_1352 - trail.prevPos.field_1352) * (double)tickDelta;
            double renderY = trail.prevPos.field_1351 + (trail.currentPos.field_1351 - trail.prevPos.field_1351) * (double)tickDelta;
            double renderZ = trail.prevPos.field_1350 + (trail.currentPos.field_1350 - trail.prevPos.field_1350) * (double)tickDelta;
            Matrix4f matrix = matrices.method_23760().method_23761();
            matrix.identity().translate((float)(renderX - cameraPos.field_1352), (float)(renderY - cameraPos.field_1351), (float)(renderZ - cameraPos.field_1350)).rotate((Quaternionfc)camera.method_23767());
            float particleAlpha = (float)this.age / (float)this.maxAge;
            float trailAlpha = (float)trail.animation(tickDelta);
            float finalAlpha = particleAlpha * trailAlpha;
            float trailPosition = 1.0f - (float)i / (float)totalTrails;
            int colorWithAlpha = this.customColors != null && this.customColors.length > 1 ? this.getGradientColor(trailPosition, finalAlpha) : ColorUtil.replAlpha(this.color.getRGB(), (int)(finalAlpha * 255.0f));
            ParticleBatchRenderer.queueFireflyTrail(matrix, this.size, colorWithAlpha);
        }
    }

    public List<FireFlyTrail> getTrails() {
        return this.trails;
    }

    public double getPosX() {
        return this.posX;
    }

    public double getPosY() {
        return this.posY;
    }

    public double getPosZ() {
        return this.posZ;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public static class FireFlyTrail {
        private class_243 prevPos;
        public class_243 currentPos;
        private final Color color;
        private int ticks;
        private int prevTicks;
        private final int maxLifetime;

        public FireFlyTrail(class_243 from, class_243 to, Color color, float entityHeight, int trailLength) {
            this.prevPos = from;
            this.currentPos = to;
            this.ticks = trailLength;
            this.maxLifetime = trailLength;
            this.color = color;
        }

        public class_243 interpolate(float pt) {
            double x = this.prevPos.field_1352 + (this.currentPos.field_1352 - this.prevPos.field_1352) * (double)pt;
            double y = this.prevPos.field_1351 + (this.currentPos.field_1351 - this.prevPos.field_1351) * (double)pt;
            double z = this.prevPos.field_1350 + (this.currentPos.field_1350 - this.prevPos.field_1350) * (double)pt;
            return new class_243(x, y, z);
        }

        public double animation(float pt) {
            float currentTicks = (float)this.prevTicks + (float)(this.ticks - this.prevTicks) * pt;
            return Math.max(0.0f, currentTicks / (float)this.maxLifetime);
        }

        public boolean update() {
            this.prevTicks = this.ticks;
            this.prevPos = this.currentPos;
            return this.ticks-- <= 0;
        }
    }
}

