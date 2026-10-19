/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.class_1109
 *  net.minecraft.class_1113
 *  net.minecraft.class_1268
 *  net.minecraft.class_1297
 *  net.minecraft.class_1297$class_5529
 *  net.minecraft.class_1313
 *  net.minecraft.class_1922
 *  net.minecraft.class_2338
 *  net.minecraft.class_2350$class_2351
 *  net.minecraft.class_243
 *  net.minecraft.class_265
 *  net.minecraft.class_3414
 *  net.minecraft.class_3417
 *  net.minecraft.class_3419
 *  net.minecraft.class_3532
 *  net.minecraft.class_5819
 *  net.minecraft.class_745
 */
package ru.recoin.implement.features.modules.player;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.class_1109;
import net.minecraft.class_1113;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1313;
import net.minecraft.class_1922;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_265;
import net.minecraft.class_3414;
import net.minecraft.class_3417;
import net.minecraft.class_3419;
import net.minecraft.class_3532;
import net.minecraft.class_5819;
import net.minecraft.class_745;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.implement.events.block.PushEvent;
import ru.recoin.implement.events.player.AttackEvent;

public class FakePlayer
extends Module {
    private static final int ENTITY_ID = -7331;
    public final BooleanSetting strafeTraining = new BooleanSetting("\u0411\u0435\u0433\u0430\u0442\u044c \u0440\u044f\u0434\u043e\u043c", "\u0414\u0432\u0438\u0433\u0430\u0442\u044c\u0441\u044f \u0438\u0437 \u0441\u0442\u043e\u0440\u043e\u043d\u044b \u0432 \u0441\u0442\u043e\u0440\u043e\u043d\u0443 \u0432\u043e\u0437\u043b\u0435 \u0438\u0433\u0440\u043e\u043a\u0430").setValue(false);
    private class_745 fake;
    private class_243 spawnPosition;
    private double motionX;
    private double motionY;
    private double motionZ;
    private double preferredDistance = 3.0;
    private int strafeDirection = 1;
    private int directionTicks;
    private int distanceTicks;
    private int pauseTicks;
    private int jumpCooldown;

    public FakePlayer() {
        super("FakePlayer", "Fake Player", ModuleCategory.PLAYER);
        this.setup(this.strafeTraining);
    }

    @Override
    public void activate() {
        if (FakePlayer.mc.field_1724 == null || FakePlayer.mc.field_1687 == null) {
            return;
        }
        GameProfile profile = new GameProfile(UUID.randomUUID(), "FakePlayer");
        class_243 forward = FakePlayer.mc.field_1724.method_5828(1.0f).method_18805(1.0, 0.0, 1.0);
        if (forward.method_1027() < 1.0E-6) {
            forward = new class_243(0.0, 0.0, 1.0);
        }
        this.spawnPosition = FakePlayer.mc.field_1724.method_73189().method_1019(forward.method_1029().method_1021(2.5));
        this.motionZ = 0.0;
        this.motionY = 0.0;
        this.motionX = 0.0;
        this.jumpCooldown = 0;
        this.pauseTicks = 0;
        this.distanceTicks = 0;
        this.directionTicks = 0;
        this.fake = new class_745(FakePlayer.mc.field_1687, profile){

            public void method_5773() {
                super.method_5773();
                FakePlayer.this.updateFake(this);
            }

            public boolean method_5810() {
                return false;
            }

            public boolean method_30949(class_1297 other) {
                return false;
            }

            public void method_5697(class_1297 entity) {
            }
        };
        this.fake.field_5960 = false;
        this.spawnPosition = this.resolveGroundedPosition(this.spawnPosition, this.fake);
        this.fake.method_5838(-7331);
        this.fake.method_5808(this.spawnPosition.field_1352, this.spawnPosition.field_1351, this.spawnPosition.field_1350, FakePlayer.mc.field_1724.method_36454() + 180.0f, 0.0f);
        this.fake.method_24830(true);
        this.fake.method_6033(20.0f);
        FakePlayer.mc.field_1687.method_53875((class_1297)this.fake);
    }

    @Override
    public void deactivate() {
        if (FakePlayer.mc.field_1687 != null && this.fake != null) {
            FakePlayer.mc.field_1687.method_2945(this.fake.method_5628(), class_1297.class_5529.field_26999);
        }
        this.fake = null;
        this.spawnPosition = null;
    }

    @EventHandler
    public void onPush(PushEvent event) {
        if (event.getType() == PushEvent.Type.COLLISION && this.fake != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onAttack(AttackEvent event) {
        boolean critical;
        if (!event.isPre() || this.fake == null || event.getEntity() != this.fake) {
            return;
        }
        event.cancel();
        this.fake.method_5879(0.0f);
        if (FakePlayer.mc.field_1724 == null || FakePlayer.mc.field_1687 == null) {
            return;
        }
        FakePlayer.mc.field_1724.method_7350();
        FakePlayer.mc.field_1724.method_6104(class_1268.field_5808);
        boolean bl = critical = FakePlayer.mc.field_1724.field_6017 > 0.0 && !FakePlayer.mc.field_1724.method_24828() && !FakePlayer.mc.field_1724.method_5799() && !FakePlayer.mc.field_1724.method_6101();
        if (critical) {
            FakePlayer.mc.field_1724.method_7277((class_1297)this.fake);
        }
        this.playHitSound();
    }

    private void playHitSound() {
        if (this.fake == null) {
            return;
        }
        boolean critical = FakePlayer.mc.field_1724.field_6017 > 0.0 && !FakePlayer.mc.field_1724.method_24828() && !FakePlayer.mc.field_1724.method_5799() && !FakePlayer.mc.field_1724.method_6101();
        this.playLocal(critical ? class_3417.field_15016 : class_3417.field_14840, 1.0f, 1.0f);
        this.playLocal(class_3417.field_15115, 0.8f, 1.0f);
    }

    private void playLocal(class_3414 sound, float volume, float pitch) {
        if (this.fake == null) {
            return;
        }
        mc.method_1483().method_4873((class_1113)new class_1109(sound, class_3419.field_15248, volume, pitch, class_5819.method_43047(), this.fake.method_24515()));
    }

    private void updateFake(class_745 entity) {
        if (FakePlayer.mc.field_1724 == null || this.spawnPosition == null) {
            return;
        }
        entity.method_6033(20.0f);
        entity.method_6012();
        entity.field_5960 = false;
        if (this.strafeTraining.isValue()) {
            this.tickGroundMovement(entity);
            entity.method_5784(class_1313.field_6308, new class_243(this.motionX, 0.0, this.motionZ));
            entity.method_18800(this.motionX, 0.0, this.motionZ);
        } else {
            this.motionZ = 0.0;
            this.motionY = 0.0;
            this.motionX = 0.0;
            entity.method_23327(this.spawnPosition.field_1352, this.spawnPosition.field_1351, this.spawnPosition.field_1350);
            entity.method_18800(0.0, 0.0, 0.0);
            entity.method_24830(true);
        }
        class_243 delta = FakePlayer.mc.field_1724.method_33571().method_1020(entity.method_33571());
        double horizontal = Math.sqrt(delta.field_1352 * delta.field_1352 + delta.field_1350 * delta.field_1350);
        float lookYaw = (float)Math.toDegrees(Math.atan2(delta.field_1350, delta.field_1352)) - 90.0f;
        float lookPitch = class_3532.method_15363((float)((float)(-Math.toDegrees(Math.atan2(delta.field_1351, horizontal)))), (float)-60.0f, (float)60.0f);
        entity.field_6241 += class_3532.method_15363((float)class_3532.method_15393((float)(lookYaw - entity.field_6241)), (float)-10.0f, (float)10.0f);
        entity.method_36456(entity.field_6241);
        entity.method_36457(entity.method_36455() + class_3532.method_15363((float)(lookPitch - entity.method_36455()), (float)-6.0f, (float)6.0f));
        if (Math.hypot(this.motionX, this.motionZ) > 0.01) {
            float movementYaw = (float)Math.toDegrees(Math.atan2(-this.motionX, this.motionZ));
            entity.field_6283 += class_3532.method_15363((float)class_3532.method_15393((float)(movementYaw - entity.field_6283)), (float)-5.0f, (float)5.0f);
        }
        double movedDist = Math.hypot(entity.method_23317() - entity.field_6014, entity.method_23321() - entity.field_5969);
        float limbDistance = (float)(movedDist * 5.2);
        entity.field_42108.method_48568(limbDistance, 0.45f, 1.0f);
    }

    private void tickGroundMovement(class_745 entity) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double dx = FakePlayer.mc.field_1724.method_23317() - entity.method_23317();
        double dz = FakePlayer.mc.field_1724.method_23321() - entity.method_23321();
        double distance = Math.hypot(dx, dz);
        if (entity.method_23318() < FakePlayer.mc.field_1724.method_23318() - 24.0 || distance > 16.0) {
            double angle = random.nextDouble(Math.PI * 2);
            class_243 respawn = this.resolveGroundedPosition(new class_243(FakePlayer.mc.field_1724.method_23317() + Math.cos(angle) * 3.0, FakePlayer.mc.field_1724.method_23318(), FakePlayer.mc.field_1724.method_23321() + Math.sin(angle) * 3.0), entity);
            entity.method_5808(respawn.field_1352, respawn.field_1351, respawn.field_1350, entity.method_36454(), entity.method_36455());
            this.motionZ = 0.0;
            this.motionY = 0.0;
            this.motionX = 0.0;
            return;
        }
        if (--this.directionTicks <= 0) {
            this.strafeDirection = random.nextBoolean() ? 1 : -1;
            this.directionTicks = random.nextInt(18, 60);
        }
        if (--this.distanceTicks <= 0) {
            this.preferredDistance = random.nextDouble(1.8, 4.0);
            this.distanceTicks = random.nextInt(40, 110);
        }
        if (this.pauseTicks > 0) {
            --this.pauseTicks;
        } else if (random.nextFloat() < 0.005f) {
            this.pauseTicks = random.nextInt(6, 18);
        }
        double inverse = distance < 1.0E-4 ? 0.0 : 1.0 / distance;
        double towardX = dx * inverse;
        double towardZ = dz * inverse;
        double radial = class_3532.method_15350((double)((distance - this.preferredDistance) * 0.45), (double)-1.0, (double)1.0);
        double desiredX = towardX * radial - towardZ * (double)this.strafeDirection * 0.9;
        double desiredZ = towardZ * radial + towardX * (double)this.strafeDirection * 0.9;
        double length = Math.hypot(desiredX, desiredZ);
        if (length > 1.0) {
            desiredX /= length;
            desiredZ /= length;
        }
        double speed = this.pauseTicks > 0 ? 0.0 : 0.12;
        double acceleration = entity.method_24828() ? 0.3 : 0.1;
        this.motionX += (desiredX * speed - this.motionX) * acceleration;
        this.motionZ += (desiredZ * speed - this.motionZ) * acceleration;
        if (this.jumpCooldown > 0) {
            --this.jumpCooldown;
        }
        if (entity.method_24828()) {
            this.motionY = -0.0784;
            if (this.jumpCooldown == 0 && entity.field_5976) {
                this.motionY = 0.42;
                this.jumpCooldown = random.nextInt(25, 55);
            }
        } else {
            this.motionY = (this.motionY - 0.08) * 0.98;
        }
        entity.method_5728(false);
        entity.method_5784(class_1313.field_6308, new class_243(this.motionX, this.motionY, this.motionZ));
        entity.method_18800(entity.method_23317() - entity.field_6014, entity.method_23318() - entity.field_6036, entity.method_23321() - entity.field_5969);
    }

    private class_243 resolveGroundedPosition(class_243 desired, class_745 entity) {
        if (FakePlayer.mc.field_1687 == null) {
            return desired;
        }
        int centerX = class_3532.method_15357((double)desired.field_1352);
        int centerZ = class_3532.method_15357((double)desired.field_1350);
        int highestY = class_3532.method_15357((double)desired.field_1351) + 4;
        int lowestY = Math.max(FakePlayer.mc.field_1687.method_31607(), class_3532.method_15357((double)desired.field_1351) - 8);
        class_243 best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int offsetX = -2; offsetX <= 2; ++offsetX) {
            block1: for (int offsetZ = -2; offsetZ <= 2; ++offsetZ) {
                double x = (double)(centerX + offsetX) + 0.5;
                double z = (double)(centerZ + offsetZ) + 0.5;
                for (int y = highestY; y >= lowestY; --y) {
                    class_2338 support = new class_2338(centerX + offsetX, y, centerZ + offsetZ);
                    class_265 shape = FakePlayer.mc.field_1687.method_8320(support).method_26220((class_1922)FakePlayer.mc.field_1687, support);
                    if (shape.method_1110()) continue;
                    class_243 candidate = new class_243(x, (double)support.method_10264() + shape.method_1105(class_2350.class_2351.field_11052), z);
                    if (!FakePlayer.mc.field_1687.method_8587((class_1297)entity, entity.method_18377(entity.method_18376()).method_30757(candidate).method_1011(1.0E-7))) continue;
                    double distance = candidate.method_1025(desired);
                    if (!(distance < bestDistance)) continue block1;
                    best = candidate;
                    bestDistance = distance;
                    continue block1;
                }
            }
        }
        return best == null ? desired : best;
    }
}

