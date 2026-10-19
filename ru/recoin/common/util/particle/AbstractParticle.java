/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1922
 *  net.minecraft.class_2338
 *  net.minecraft.class_2350$class_2351
 *  net.minecraft.class_238
 *  net.minecraft.class_265
 *  net.minecraft.class_2680
 *  net.minecraft.class_310
 *  net.minecraft.class_4587
 */
package ru.recoin.common.util.particle;

import java.awt.Color;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.class_1922;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import ru.recoin.common.util.spatial.BlockCollisionGrid;

public abstract class AbstractParticle {
    protected static final class_310 mc = class_310.method_1551();
    protected double x;
    protected double y;
    protected double z;
    protected double px;
    protected double py;
    protected double pz;
    protected float motionX;
    protected float motionY;
    protected float motionZ;
    protected float rotationAngle;
    protected float prevRotationAngle;
    protected float rotationSpeed;
    protected long time;
    protected float lifeTime;
    protected int age;
    protected int maxAge;
    protected Color color;
    protected float scale;
    protected float cachedAlpha = 1.0f;
    protected boolean isVisible = true;
    protected int ticksSinceLastUpdate = 0;
    protected int colorOffset;
    protected String physicsMode;
    protected boolean enableCollision;

    public AbstractParticle(double x, double y, double z, Color color, float rotationAngle, float rotationSpeed, float lifeTime, float scale, String physicsMode, boolean enableCollision) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.px = x;
        this.py = y;
        this.pz = z;
        this.color = color;
        this.rotationAngle = rotationAngle;
        this.rotationSpeed = rotationSpeed;
        this.time = System.currentTimeMillis();
        this.lifeTime = lifeTime;
        this.scale = scale;
        this.physicsMode = physicsMode;
        this.enableCollision = enableCollision;
        this.colorOffset = ThreadLocalRandom.current().nextInt(3600);
        if (lifeTime > 0.0f) {
            this.maxAge = (int)(lifeTime * 20.0f);
            this.age = this.maxAge + ThreadLocalRandom.current().nextInt(21);
        } else {
            this.maxAge = this.age = ThreadLocalRandom.current().nextInt(100, 201);
        }
    }

    protected void initMotion(float speed) {
        boolean isDrop = "Fall".equals(this.physicsMode);
        boolean isEmerge = "Emerge".equals(this.physicsMode);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        if (isDrop) {
            this.motionX = 0.0f;
            this.motionY = (float)random.nextDouble(-0.2, -0.05);
            this.motionZ = 0.0f;
        } else if (isEmerge) {
            this.motionX = (float)random.nextDouble(-0.2, 0.2);
            this.motionY = (float)random.nextDouble(0.4, 0.7);
            this.motionZ = (float)random.nextDouble(-0.2, 0.2);
        } else {
            this.motionX = (float)random.nextDouble(-0.4, 0.4);
            this.motionY = (float)random.nextDouble(-0.1, 0.1);
            this.motionZ = (float)random.nextDouble(-0.4, 0.4);
        }
    }

    public boolean update(long currentTime, boolean isVisible) {
        return this.update(currentTime, isVisible, null);
    }

    public boolean update(long currentTime, boolean isVisible, BlockCollisionGrid collisionGrid) {
        this.isVisible = isVisible;
        if (!isVisible) {
            ++this.ticksSinceLastUpdate;
            if (this.ticksSinceLastUpdate < 20) {
                return false;
            }
            this.age -= this.ticksSinceLastUpdate;
            this.ticksSinceLastUpdate = 0;
        } else {
            --this.age;
            this.ticksSinceLastUpdate = 0;
        }
        if (this.age < 0) {
            return true;
        }
        this.px = this.x;
        this.py = this.y;
        this.pz = this.z;
        if (this.enableCollision && collisionGrid != null) {
            float[] repulsion = collisionGrid.calculateRepulsion((float)this.x, (float)this.y, (float)this.z);
            this.motionX += repulsion[0];
            this.motionY += repulsion[1];
            this.motionZ += repulsion[2];
        }
        if (this.enableCollision) {
            this.moveWithBlockCollision(collisionGrid);
        } else {
            this.x += (double)this.motionX;
            this.y += (double)this.motionY;
            this.z += (double)this.motionZ;
        }
        this.motionX *= 0.9f;
        this.motionZ *= 0.9f;
        if ("Fly".equals(this.physicsMode)) {
            this.motionY *= 0.9f;
            this.motionY -= 0.001f;
        } else {
            this.motionY = "Emerge".equals(this.physicsMode) ? (this.motionY *= 0.85f) : (this.motionY -= 0.001f);
        }
        this.prevRotationAngle = this.rotationAngle;
        this.rotationAngle += this.rotationSpeed;
        this.updateAlpha(currentTime);
        return false;
    }

    private void moveWithBlockCollision(BlockCollisionGrid collisionGrid) {
        if (!this.tryMove(collisionGrid, this.motionX, 0.0, 0.0)) {
            this.motionX = this.bounce(this.motionX, 0.45f);
        }
        if (!this.tryMove(collisionGrid, 0.0, this.motionY, 0.0)) {
            this.motionY = this.bounce(this.motionY, this.motionY < 0.0f ? 0.3f : 0.45f);
            this.motionX *= 0.65f;
            this.motionZ *= 0.65f;
        }
        if (!this.tryMove(collisionGrid, 0.0, 0.0, this.motionZ)) {
            this.motionZ = this.bounce(this.motionZ, 0.45f);
        }
        this.preventGroundPenetration();
    }

    private void preventGroundPenetration() {
        double topY;
        class_265 shape;
        if (AbstractParticle.mc.field_1687 == null) {
            return;
        }
        class_2338 blockPos = class_2338.method_49637((double)this.x, (double)this.y, (double)this.z);
        class_2680 state = AbstractParticle.mc.field_1687.method_8320(blockPos);
        if (!state.method_26215() && !(shape = state.method_26220((class_1922)AbstractParticle.mc.field_1687, blockPos)).method_1110() && this.y < (topY = (double)blockPos.method_10264() + shape.method_1105(class_2350.class_2351.field_11052))) {
            this.y = topY + 0.01;
            if (this.motionY < 0.0f) {
                this.motionY = 0.0f;
            }
        }
    }

    private boolean tryMove(BlockCollisionGrid collisionGrid, double dx, double dy, double dz) {
        class_265 shape;
        class_2338 blockPos;
        class_2680 state;
        double nextX = this.x + dx;
        double nextY = this.y + dy;
        double nextZ = this.z + dz;
        if (collisionGrid != null && collisionGrid.hasCollision((float)nextX, (float)nextY, (float)nextZ)) {
            return false;
        }
        if (AbstractParticle.mc.field_1687 != null && !(state = AbstractParticle.mc.field_1687.method_8320(blockPos = class_2338.method_49637((double)nextX, (double)nextY, (double)nextZ))).method_26215() && !(shape = state.method_26220((class_1922)AbstractParticle.mc.field_1687, blockPos)).method_1110()) {
            for (class_238 box : shape.method_1090()) {
                class_238 offsetBox = box.method_996(blockPos);
                double margin = 0.02;
                if (!(nextX >= offsetBox.field_1323 - margin) || !(nextX <= offsetBox.field_1320 + margin) || !(nextY >= offsetBox.field_1322 - margin) || !(nextY <= offsetBox.field_1325 + margin) || !(nextZ >= offsetBox.field_1321 - margin) || !(nextZ <= offsetBox.field_1324 + margin)) continue;
                return false;
            }
        }
        this.x = nextX;
        this.y = nextY;
        this.z = nextZ;
        return true;
    }

    private float bounce(float motion, float strength) {
        float bounced = -motion * strength;
        return Math.abs(bounced) < 0.0025f ? 0.0f : bounced;
    }

    protected void updateAlpha(long currentTime) {
        float life = this.maxAge <= 0 ? 0.0f : Math.max(0.0f, Math.min(1.0f, (float)this.age / (float)this.maxAge));
        this.cachedAlpha = life * life * (3.0f - 2.0f * life);
    }

    public abstract void render(class_4587 var1, long var2);

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public double getPx() {
        return this.px;
    }

    public double getPy() {
        return this.py;
    }

    public double getPz() {
        return this.pz;
    }

    public long getTime() {
        return this.time;
    }

    public float getLifeTime() {
        return this.lifeTime;
    }

    public Color getColor() {
        return this.color;
    }

    public float getScale() {
        return this.scale;
    }

    public float getAlpha() {
        return this.cachedAlpha;
    }

    public int getColorOffset() {
        return this.colorOffset;
    }

    public boolean isVisible() {
        return this.isVisible;
    }
}

