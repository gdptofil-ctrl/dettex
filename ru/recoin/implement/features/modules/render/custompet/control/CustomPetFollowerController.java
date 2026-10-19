/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1293
 *  net.minecraft.class_1294
 *  net.minecraft.class_1297
 *  net.minecraft.class_1297$class_5529
 *  net.minecraft.class_1657
 *  net.minecraft.class_1922
 *  net.minecraft.class_1937
 *  net.minecraft.class_2338
 *  net.minecraft.class_2338$class_2339
 *  net.minecraft.class_2350$class_2351
 *  net.minecraft.class_243
 *  net.minecraft.class_265
 *  net.minecraft.class_2680
 *  net.minecraft.class_310
 *  net.minecraft.class_3532
 *  net.minecraft.class_5819
 */
package ru.recoin.implement.features.modules.render.custompet.control;

import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1922;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_5819;
import ru.recoin.implement.features.modules.render.custompet.CustomPetVariant;
import ru.recoin.implement.features.modules.render.custompet.entity.CustomPetEntity;

public final class CustomPetFollowerController {
    private static final double MAX_WANDER_DISTANCE = 15.0;
    private static final double SNAP_BACK_DISTANCE = 18.0;
    private static final double FOLLOW_BREAK_DISTANCE = 13.0;
    private static final double FOLLOW_NEAR_DISTANCE = 6.0;
    private static final double TARGET_REACHED_DISTANCE = 0.9;
    private static final double PLAYER_PERSONAL_SPACE = 1.35;
    private static final double OWL_HOVER_RADIUS_MIN = 1.6;
    private static final double OWL_HOVER_RADIUS_MAX = 2.8;
    private static final double OWL_HOVER_HEIGHT_MIN = 1.6;
    private static final double OWL_HOVER_HEIGHT_MAX = 3.2;
    private static final int OWL_FLIGHT_COOLDOWN_MIN = 300;
    private static final int OWL_FLIGHT_COOLDOWN_MAX = 640;
    private static final int OWL_FLIGHT_DURATION_MIN = 90;
    private static final int OWL_FLIGHT_DURATION_MAX = 190;
    private final class_310 mc = class_310.method_1551();
    private final class_5819 random = class_5819.method_43047();
    private CustomPetEntity pet;
    private boolean petIsOwl;
    private class_243 roamTarget;
    private class_243 airHoverOffset;
    private class_243 airSmoothedTarget;
    private class_243 airFlightDirection;
    private boolean airChasing;
    private int idleTicks;
    private int followRefreshTicks;
    private int followSide = 1;
    private int ticks;
    private int flightTicksRemaining;
    private int nextFlightAtTick;
    private int gazeBuildTicks;
    private int gazeLookActiveTicks;
    private int gazeCooldownTicks;

    public void reset() {
        if (this.pet != null && !this.pet.method_31481()) {
            if (this.mc.field_1687 != null) {
                this.mc.field_1687.method_2945(this.pet.method_5628(), class_1297.class_5529.field_26999);
            }
            this.pet.method_31472();
        }
        this.pet = null;
        this.roamTarget = null;
        this.airHoverOffset = null;
        this.airSmoothedTarget = null;
        this.airFlightDirection = null;
        this.airChasing = false;
        this.idleTicks = 0;
        this.followRefreshTicks = 0;
        this.followSide = 1;
        this.flightTicksRemaining = 0;
    }

    private void ensurePet(class_1657 playerEntity, CustomPetVariant customPetVariant, boolean bl) {
        if (this.pet != null && this.pet.method_73183() == this.mc.field_1687 && !this.pet.method_31481()) {
            this.pet.setPetVariant(customPetVariant);
            this.pet.setOwl(bl);
            this.petIsOwl = bl;
            return;
        }
        this.reset();
        this.petIsOwl = bl;
        this.pet = new CustomPetEntity((class_1937)this.mc.field_1687);
        this.pet.setPetVariant(customPetVariant);
        this.pet.setOwl(bl);
        this.roamTarget = this.computeFollowTarget(playerEntity, 0.0, 0.0);
        this.airHoverOffset = null;
        this.airSmoothedTarget = null;
        this.airFlightDirection = null;
        this.airChasing = false;
        this.idleTicks = 0;
        this.followRefreshTicks = 0;
        this.followSide = 1;
        float yawToPlayer = (float)Math.toDegrees(class_3532.method_15349((double)(playerEntity.method_23321() - this.roamTarget.field_1350), (double)(playerEntity.method_23317() - this.roamTarget.field_1352))) - 90.0f;
        this.pet.snapTo(this.roamTarget, yawToPlayer);
        this.pet.overrideLookYaw(yawToPlayer, 1.0f);
        this.mc.field_1687.method_53875((class_1297)this.pet);
    }

    private void tickPet(class_1657 playerEntity, CustomPetVariant customPetVariant) {
        boolean shouldMove;
        boolean playerMoving;
        class_243 followTarget;
        if (this.pet == null) {
            return;
        }
        this.pet.setPetVariant(customPetVariant);
        class_243 vec3d = this.pet.method_73189();
        class_243 vec3d2 = this.snapCurrentToGround(vec3d, playerEntity.method_23318());
        class_243 vec3d3 = playerEntity.method_73189();
        double d2 = vec3d.method_1025(vec3d3);
        double d3 = Math.sqrt(d2);
        double d4 = playerEntity.method_18798().method_37267();
        boolean bl = customPetVariant != null && customPetVariant.isRobot();
        boolean bl2 = bl;
        if (bl) {
            this.tickRobotFlight(playerEntity, vec3d, vec3d3, d3, d4);
            return;
        }
        ++this.ticks;
        boolean bl3 = playerEntity.method_6128() || playerEntity.method_31549().field_7479;
        boolean bl4 = false;
        if (this.petIsOwl) {
            if (this.flightTicksRemaining > 0) {
                --this.flightTicksRemaining;
                bl4 = true;
                if (this.flightTicksRemaining == 0) {
                    this.nextFlightAtTick = this.ticks + this.randomBetween(300, 640);
                }
            } else if (!bl3 && this.ticks >= this.nextFlightAtTick && d4 < 0.12 && d3 < 8.0) {
                this.flightTicksRemaining = this.randomBetween(90, 190);
                this.nextFlightAtTick = this.ticks + this.flightTicksRemaining + this.randomBetween(300, 640);
                bl4 = true;
            }
        }
        boolean bl5 = bl3 || bl4;
        boolean bl6 = d4 > 0.025 || Math.abs(playerEntity.method_18798().field_1351) > 0.05;
        boolean bl7 = d4 > 0.17 || Math.abs(playerEntity.method_18798().field_1351) > 0.08;
        boolean bl9 = this.isRainingAbove(vec3d);
        if (d2 > 256.0) {
            this.roamTarget = this.computeFollowTarget(playerEntity, d3, d4);
            this.airFlightDirection = null;
            this.airChasing = false;
            this.flightTicksRemaining = 0;
            this.idleTicks = 0;
            this.followRefreshTicks = 0;
            float yawToPlayer = (float)Math.toDegrees(class_3532.method_15349((double)(playerEntity.method_23321() - this.roamTarget.field_1350), (double)(playerEntity.method_23317() - this.roamTarget.field_1352))) - 90.0f;
            this.pet.snapTo(this.roamTarget, yawToPlayer);
            this.pet.overrideLookYaw(yawToPlayer, 1.0f);
            this.pet.setBehavior(this.roamTarget, 0.0, false, bl9, false, 1.0);
            return;
        }
        if (this.pet.isMovementBlocked() && d2 <= 169.0) {
            this.roamTarget = null;
            this.airFlightDirection = null;
            this.airChasing = false;
            this.followRefreshTicks = 0;
            if (this.idleTicks <= 0) {
                this.idleTicks = this.randomBetween(18, 38);
            }
            --this.idleTicks;
            float yawToPlayer = (float)Math.toDegrees(class_3532.method_15349((double)(playerEntity.method_23321() - vec3d.field_1350), (double)(playerEntity.method_23317() - vec3d.field_1352))) - 90.0f;
            this.pet.overrideLookYaw(yawToPlayer, 1.0f);
            this.pet.setBehavior(vec3d2, 0.0, false, bl9, false, 1.0);
            return;
        }
        if (bl5) {
            boolean bl11;
            class_243 vec3d4;
            boolean isMoving;
            this.idleTicks = 0;
            boolean bl8 = isMoving = d4 > 0.08 || Math.abs(playerEntity.method_18798().field_1351) > 0.08;
            if (isMoving) {
                this.followRefreshTicks = 0;
                this.airHoverOffset = null;
                vec3d4 = this.computeAirFollowTarget(playerEntity, d3, d4);
            } else {
                if (this.airHoverOffset == null || this.followRefreshTicks <= 0) {
                    this.airHoverOffset = this.pickAirHoverOffset(playerEntity, vec3d);
                    this.followRefreshTicks = this.randomBetween(20, 42);
                } else {
                    --this.followRefreshTicks;
                }
                vec3d4 = this.computeAirIdleTarget(playerEntity);
            }
            if (this.airSmoothedTarget == null) {
                this.airSmoothedTarget = vec3d;
            }
            double d = Math.clamp(0.07 + playerEntity.method_18798().method_1033() * (playerEntity.method_6128() ? 0.025 : 0.05) + Math.max(0.0, d3 - 1.5) * 0.012, 0.07, playerEntity.method_6128() ? 0.2 : 0.28);
            this.roamTarget = this.airSmoothedTarget = this.airSmoothedTarget.method_35590(vec3d4, d);
            float yawToPlayer = (float)Math.toDegrees(class_3532.method_15349((double)(playerEntity.method_23321() - vec3d.field_1350), (double)(playerEntity.method_23317() - vec3d.field_1352))) - 90.0f;
            this.pet.overrideLookYaw(yawToPlayer, 1.0f);
            class_243 vec3d6 = this.roamTarget;
            class_243 vec3d7 = vec3d6.method_1020(vec3d);
            double d5 = vec3d7.method_1033();
            class_1293 statusEffectInstance = playerEntity.method_6112(class_1294.field_5904);
            int n = statusEffectInstance != null ? statusEffectInstance.method_5578() + 1 : 0;
            double d6 = playerEntity.method_18798().method_1033();
            double d7 = playerEntity.method_6128() ? 0.82 : 0.48;
            double d8 = playerEntity.method_6128() ? 1.28 : 0.78;
            boolean bl10 = bl11 = d3 > 4.0 || d6 > 1.2 || d4 > 0.55;
            if (bl11 || d5 >= d8) {
                this.airChasing = true;
            } else if (d5 <= d7) {
                this.airChasing = false;
            }
            double d9 = Math.clamp(0.22 + d6 * (playerEntity.method_6128() ? 2.45 : 1.65) + Math.max(0.0, d3 - 1.0) * (playerEntity.method_6128() ? 0.24 : 0.18) + (double)n * 0.07 + (playerEntity.method_5624() ? 0.08 : 0.0), 0.16, playerEntity.method_6128() ? (d3 > 18.0 ? 6.8 : (d3 > 12.0 ? 5.0 : (d3 > 7.0 ? 3.6 : 1.9))) : (d3 > 16.0 ? 3.2 : (d3 > 10.0 ? 2.4 : (d3 > 6.0 ? 1.55 : 0.95))));
            double d10 = Math.clamp(1.15 + Math.max(0.0, d3 - 1.2) * 0.22 + d6 * (playerEntity.method_6128() ? 1.8 : 1.35) + (double)n * 0.16, 1.0, playerEntity.method_6128() ? 3.1 : 2.4);
            this.pet.setBehavior(vec3d6, d9, this.airChasing, false, true, d10);
            return;
        }
        this.airHoverOffset = null;
        this.airSmoothedTarget = null;
        this.airFlightDirection = null;
        this.airChasing = false;
        this.idleTicks = 0;
        this.followRefreshTicks = 0;
        this.roamTarget = followTarget = this.computeFollowTarget(playerEntity, d3, d4);
        float yawToPlayer = (float)Math.toDegrees(class_3532.method_15349((double)(playerEntity.method_23321() - vec3d.field_1350), (double)(playerEntity.method_23317() - vec3d.field_1352))) - 90.0f;
        this.pet.overrideLookYaw(yawToPlayer, 1.0f);
        class_243 diff = followTarget.method_1020(vec3d);
        double distToTarget = Math.hypot(diff.field_1352, diff.field_1350);
        boolean bl11 = playerMoving = d4 > 0.02;
        if (playerMoving) {
            shouldMove = distToTarget > 0.25 || d3 > 2.4;
        } else {
            boolean bl12 = shouldMove = distToTarget > 0.35 || d3 < 1.5 || d3 > 2.5;
        }
        if (!shouldMove) {
            this.pet.setBehavior(vec3d2, 0.0, false, bl9, false, 1.0);
            this.pushOutOfPlayer(playerEntity);
            return;
        }
        class_1293 statusEffectInstance = playerEntity.method_6112(class_1294.field_5904);
        int n = statusEffectInstance != null ? statusEffectInstance.method_5578() + 1 : 0;
        double moveSpeed = Math.clamp(0.08 + d4 * 0.45 + Math.max(0.0, distToTarget - 0.4) * 0.14 + (double)n * 0.03 + (playerEntity.method_5624() ? 0.06 : 0.0), 0.06, distToTarget > 6.0 ? 0.65 : (distToTarget > 3.5 ? 0.48 : 0.32));
        double animSpeed = Math.clamp(1.0 + Math.max(0.0, distToTarget - 0.8) * 0.25 + d4 * 0.9 + (double)n * 0.15 + (playerEntity.method_5624() ? 0.1 : 0.0), 1.0, 2.2);
        this.pet.setBehavior(followTarget, moveSpeed, true, bl9, false, animSpeed);
        this.pushOutOfPlayer(playerEntity);
    }

    public CustomPetEntity getPet() {
        return this.pet;
    }

    private class_243 computeFollowTarget(class_1657 playerEntity, double d, double d2) {
        double dz;
        double dx;
        if (this.pet != null) {
            dx = this.pet.method_23317() - playerEntity.method_23317();
            dz = this.pet.method_23321() - playerEntity.method_23321();
        } else {
            float f = playerEntity.method_36454() * ((float)Math.PI / 180);
            dx = (double)class_3532.method_15374((double)f) * 2.0;
            dz = (double)(-class_3532.method_15362((double)f)) * 2.0;
        }
        double dist = Math.hypot(dx, dz);
        if (dist < 1.0E-4) {
            dx = 1.0;
            dz = 0.0;
            dist = 1.0;
        }
        double followDist = 2.0;
        double targetX = playerEntity.method_23317() + dx / dist * followDist;
        double targetZ = playerEntity.method_23321() + dz / dist * followDist;
        double d6 = this.findGroundY(targetX, targetZ, playerEntity.method_23318());
        return CustomPetFollowerController.enforcePersonalSpace(new class_243(targetX, d6, targetZ), playerEntity);
    }

    private boolean isRainingAbove(class_243 vec3d) {
        if (this.mc.field_1687 == null) {
            return false;
        }
        return this.mc.field_1687.method_8520(class_2338.method_49637((double)vec3d.field_1352, (double)(vec3d.field_1351 + 1.1), (double)vec3d.field_1350));
    }

    private class_243 pickRobotRoamTarget(class_1657 playerEntity) {
        double dz;
        double dx;
        if (this.pet != null) {
            dx = this.pet.method_23317() - playerEntity.method_23317();
            dz = this.pet.method_23321() - playerEntity.method_23321();
        } else {
            float f = playerEntity.method_36454() * ((float)Math.PI / 180);
            dx = (double)class_3532.method_15374((double)f) * 2.0;
            dz = (double)(-class_3532.method_15362((double)f)) * 2.0;
        }
        double dist = Math.hypot(dx, dz);
        if (dist < 1.0E-4) {
            dx = 1.0;
            dz = 0.0;
            dist = 1.0;
        }
        class_243 target = new class_243(playerEntity.method_23317() + dx / dist * 2.0, playerEntity.method_23318() + 1.4, playerEntity.method_23321() + dz / dist * 2.0);
        double d6 = this.ceilingClearance(target.field_1352, target.field_1350, target.field_1351, playerEntity.method_23318());
        return CustomPetFollowerController.enforcePersonalSpace(new class_243(target.field_1352, Math.max(target.field_1351, d6), target.field_1350), playerEntity);
    }

    private double ceilingClearance(double d, double d2, double d3, double d4) {
        if (this.mc.field_1687 == null) {
            return d3;
        }
        int n = class_3532.method_15357((double)d);
        int n2 = class_3532.method_15357((double)d2);
        int n3 = class_3532.method_15357((double)d3);
        class_2338.class_2339 mutable = new class_2338.class_2339(n, n3, n2);
        for (int i = n3; i <= n3 + 3; ++i) {
            mutable.method_33098(i);
            class_2680 blockState = this.mc.field_1687.method_8320((class_2338)mutable);
            class_265 voxelShape = blockState.method_26220((class_1922)this.mc.field_1687, (class_2338)mutable);
            if (voxelShape.method_1110()) continue;
            return (double)i + voxelShape.method_1105(class_2350.class_2351.field_11052) + 0.6;
        }
        return d3;
    }

    private static class_243 enforcePersonalSpace(class_243 vec3d, class_1657 playerEntity) {
        double d;
        double d2 = vec3d.field_1352 - playerEntity.method_23317();
        double d3 = d2 * d2 + (d = vec3d.field_1350 - playerEntity.method_23321()) * d;
        if (d3 >= 1.8225000000000002) {
            return vec3d;
        }
        double d4 = Math.sqrt(d3);
        if (d4 < 1.0E-4) {
            d2 = 1.0;
            d = 0.0;
            d4 = 1.0;
        }
        double d5 = 1.35 / d4;
        return new class_243(playerEntity.method_23317() + d2 * d5, vec3d.field_1351, playerEntity.method_23321() + d * d5);
    }

    private void tickRobotFlight(class_1657 playerEntity, class_243 vec3d, class_243 vec3d2, double d, double d2) {
        boolean bl;
        class_243 vec3d3;
        class_243 vec3d4;
        double d3;
        double d4;
        if (d > 18.0) {
            class_243 vec3d5;
            this.airSmoothedTarget = vec3d5 = this.pickRobotRoamTarget(playerEntity);
            this.roamTarget = vec3d5;
            this.airFlightDirection = null;
            this.followRefreshTicks = 0;
            this.idleTicks = this.randomBetween(30, 60);
            float yawToPlayer = (float)Math.toDegrees(class_3532.method_15349((double)(playerEntity.method_23321() - vec3d5.field_1350), (double)(playerEntity.method_23317() - vec3d5.field_1352))) - 90.0f;
            this.pet.snapTo(vec3d5, yawToPlayer);
            this.pet.overrideLookYaw(yawToPlayer, 1.0f);
            this.pet.setBehavior(vec3d5, 0.0, false, false, true, 1.0);
            return;
        }
        class_243 vec3d6 = playerEntity.method_18798();
        class_243 vec3d7 = new class_243(vec3d6.field_1352, 0.0, vec3d6.field_1350);
        if (vec3d7.method_1027() < 1.0E-4) {
            double dx = this.pet != null ? this.pet.method_23317() - playerEntity.method_23317() : 1.0;
            double dz = this.pet != null ? this.pet.method_23321() - playerEntity.method_23321() : 0.0;
            vec3d7 = new class_243(dx, 0.0, dz);
        }
        vec3d7 = vec3d7.method_1029();
        this.airFlightDirection = this.airFlightDirection == null ? vec3d7 : this.airFlightDirection.method_35590(vec3d7, 0.18).method_1029();
        boolean bl3 = d2 > 0.08;
        boolean bl2 = bl3 && d > 4.0 || d > 15.0;
        boolean bl4 = bl2;
        if (bl2) {
            this.roamTarget = null;
            this.idleTicks = 0;
        }
        if (bl2) {
            class_243 vec3d8 = this.airFlightDirection;
            double d5 = d > 5.0 ? 0.5 : 1.2;
            d4 = 0.0;
            d3 = Math.clamp(d2 * 4.5, 0.0, 2.2);
            vec3d4 = vec3d2.method_1019(vec3d8.method_1021(d3 - d5));
            double d6 = playerEntity.method_23318() + 1.5;
            double d7 = this.ceilingClearance(vec3d4.field_1352, vec3d4.field_1350, d6, playerEntity.method_23318());
            vec3d3 = CustomPetFollowerController.enforcePersonalSpace(new class_243(vec3d4.field_1352, Math.max(d6, d7), vec3d4.field_1350), playerEntity);
            bl = true;
        } else if (this.idleTicks > 0) {
            --this.idleTicks;
            if (this.idleTicks <= 0) {
                this.roamTarget = null;
            }
            vec3d3 = this.roamTarget != null ? this.roamTarget : vec3d;
            bl = false;
        } else if (this.roamTarget != null) {
            double d8 = vec3d.method_1022(this.roamTarget);
            if (d8 < 1.5) {
                this.roamTarget = vec3d;
                this.idleTicks = this.randomBetween(60, 140);
                vec3d3 = vec3d;
                bl = false;
            } else {
                vec3d3 = this.roamTarget;
                bl = true;
            }
        } else {
            vec3d3 = this.roamTarget = this.pickRobotRoamTarget(playerEntity);
            bl = true;
        }
        d4 = Math.sin((double)playerEntity.field_6012 * 0.125) * 0.15;
        d3 = 0.0;
        vec3d4 = new class_243(-this.airFlightDirection.field_1350, 0.0, this.airFlightDirection.field_1352);
        class_243 vec3d10 = new class_243(vec3d3.field_1352, vec3d3.field_1351 + d4, vec3d3.field_1350);
        if (this.airSmoothedTarget == null) {
            this.airSmoothedTarget = vec3d;
        }
        double d10 = bl ? Math.clamp(0.1 + d2 * 0.08 + Math.max(0.0, d - 1.5) * 0.018, 0.1, 0.34) : 0.07;
        this.airSmoothedTarget = this.airSmoothedTarget.method_35590(vec3d10, d10);
        class_243 vec3d11 = this.airSmoothedTarget.method_1020(vec3d2);
        if (vec3d11.method_1033() > 17.5) {
            this.airSmoothedTarget = vec3d2.method_1019(vec3d11.method_1029().method_1021(17.5));
        }
        double d11 = vec3d.method_1022(this.airSmoothedTarget);
        double d12 = bl ? Math.clamp(0.08 + d11 * 0.06 + d2 * 0.8 + (playerEntity.method_5624() ? 0.06 : 0.0), 0.06, bl2 ? 1.2 : 0.25) : 0.04;
        double d13 = Math.clamp(1.0 + d11 * 0.08 + d2 * 0.6, 1.0, 1.8);
        float robotYaw = (float)Math.toDegrees(class_3532.method_15349((double)(playerEntity.method_23321() - vec3d.field_1350), (double)(playerEntity.method_23317() - vec3d.field_1352))) - 90.0f;
        this.pet.overrideLookYaw(robotYaw, 1.0f);
        this.pet.setBehavior(this.airSmoothedTarget, d12, bl || d11 > 0.3, false, true, d13);
        this.pushOutOfPlayer(playerEntity);
    }

    private class_243 pickAirHoverOffset(class_1657 playerEntity, class_243 vec3d) {
        if (this.petIsOwl) {
            double d = this.random.method_43058() * 6.2831854820251465;
            double d2 = class_3532.method_16436((double)this.random.method_43058(), (double)1.6, (double)2.8);
            double d3 = class_3532.method_16436((double)this.random.method_43058(), (double)1.6, (double)3.2);
            return new class_243(Math.cos(d) * d2, d3, Math.sin(d) * d2);
        }
        class_243 vec3d2 = vec3d.method_1020(playerEntity.method_73189());
        class_243 vec3d3 = new class_243(vec3d2.field_1352, 0.0, vec3d2.field_1350);
        double d = vec3d3.method_1033();
        if (d < 0.9 || d > 2.5) {
            double d4 = this.random.method_43058() * 6.2831854820251465;
            double d5 = class_3532.method_16436((double)this.random.method_43058(), (double)1.35, (double)1.95);
            return new class_243(Math.cos(d4) * d5, class_3532.method_16436((double)this.random.method_43058(), (double)0.28, (double)0.44), Math.sin(d4) * d5);
        }
        class_243 vec3d4 = vec3d3.method_1029().method_1021(class_3532.method_15350((double)d, (double)1.25, (double)1.95));
        double d6 = class_3532.method_15350((double)vec3d2.field_1351, (double)0.24, (double)0.46);
        return new class_243(vec3d4.field_1352, d6, vec3d4.field_1350);
    }

    private int randomBetween(int n, int n2) {
        return n + this.random.method_43048(n2 - n + 1);
    }

    private class_243 pickRoamTarget(class_1657 playerEntity, boolean bl) {
        class_243 vec3d = playerEntity.method_18798();
        double d = vec3d.method_37268() > 0.0025 ? Math.atan2(vec3d.field_1350, vec3d.field_1352) : this.random.method_43058() * 6.2831854820251465;
        double d2 = d + class_3532.method_16436((double)this.random.method_43058(), (double)-1.65, (double)1.65);
        double d3 = bl ? class_3532.method_16436((double)this.random.method_43058(), (double)1.6, (double)3.0) : class_3532.method_16436((double)this.random.method_43058(), (double)2.5, (double)14.75);
        double d4 = playerEntity.method_23317() + Math.cos(d2) * d3;
        double d5 = playerEntity.method_23321() + Math.sin(d2) * d3;
        double d6 = this.findGroundY(d4, d5, playerEntity.method_23318());
        return CustomPetFollowerController.enforcePersonalSpace(new class_243(d4, d6, d5), playerEntity);
    }

    private class_243 snapCurrentToGround(class_243 vec3d, double d) {
        double d2 = this.findGroundY(vec3d.field_1352, vec3d.field_1350, Math.max(vec3d.field_1351, d));
        return new class_243(vec3d.field_1352, d2, vec3d.field_1350);
    }

    private boolean shouldPickNewTarget(class_1657 playerEntity, class_243 vec3d) {
        if (this.roamTarget == null) {
            return true;
        }
        if (this.roamTarget.method_1025(playerEntity.method_73189()) > 225.0) {
            return true;
        }
        return vec3d.method_1025(playerEntity.method_73189()) > 169.0 && vec3d.method_1025(this.roamTarget) > 25.0;
    }

    private class_243 computeAirFollowTarget(class_1657 playerEntity, double d, double d2) {
        class_243 vec3d = playerEntity.method_18798();
        double d3 = vec3d.method_1033();
        class_243 vec3d2 = new class_243(vec3d.field_1352, 0.0, vec3d.field_1350);
        if (vec3d2.method_1027() < 1.0E-4) {
            double dx = this.pet != null ? this.pet.method_23317() - playerEntity.method_23317() : 1.0;
            double dz = this.pet != null ? this.pet.method_23321() - playerEntity.method_23321() : 0.0;
            vec3d2 = new class_243(dx, 0.0, dz);
        }
        vec3d2 = vec3d2.method_1029();
        if (this.airFlightDirection == null || this.airFlightDirection.method_1027() < 1.0E-4) {
            this.airFlightDirection = vec3d2;
        } else {
            double d4 = playerEntity.method_6128() ? 0.14 : 0.26;
            this.airFlightDirection = this.airFlightDirection.method_35590(vec3d2, d4);
            if (this.airFlightDirection.method_1027() < 1.0E-4) {
                this.airFlightDirection = vec3d2;
            }
        }
        class_243 vec3d3 = this.airFlightDirection.method_1029();
        double d6 = (playerEntity.method_6128() ? 1.85 : 1.45) + Math.min(d * 0.08, playerEntity.method_6128() ? 0.55 : 0.35);
        double d7 = playerEntity.method_6128() ? 0.2 : 0.45;
        double d8 = Math.clamp(0.45 + d2 * (playerEntity.method_6128() ? 5.8 : 4.8) + Math.max(0.0, d3 - 0.8) * (playerEntity.method_6128() ? 0.45 : 0.35), 0.45, playerEntity.method_6128() ? 2.8 : 1.6);
        class_243 vec3d5 = playerEntity.method_73189().method_1019(vec3d3.method_1021(d8));
        double d9 = Math.sin((double)playerEntity.field_6012 * 0.1) * 0.028;
        return vec3d5.method_1020(vec3d3.method_1021(d6)).method_1031(0.0, playerEntity.method_23318() - vec3d5.field_1351 + d7 + d9, 0.0);
    }

    private void pushOutOfPlayer(class_1657 playerEntity) {
        double d;
        if (this.pet == null) {
            return;
        }
        double d2 = this.pet.method_23317() - playerEntity.method_23317();
        double d3 = d2 * d2 + (d = this.pet.method_23321() - playerEntity.method_23321()) * d;
        if (d3 >= 1.8225000000000002) {
            return;
        }
        double d4 = Math.sqrt(d3);
        if (d4 < 1.0E-4) {
            d2 = 1.0;
            d = 0.0;
            d4 = 1.0;
        }
        double d5 = 1.35 / d4;
        double d6 = playerEntity.method_23317() + d2 * d5;
        double d7 = playerEntity.method_23321() + d * d5;
        this.pet.method_5814(d6, this.pet.method_23318(), d7);
    }

    private class_243 computeAirIdleTarget(class_1657 playerEntity) {
        double dz;
        double dx;
        if (this.pet != null) {
            dx = this.pet.method_23317() - playerEntity.method_23317();
            dz = this.pet.method_23321() - playerEntity.method_23321();
        } else {
            float f = playerEntity.method_36454() * ((float)Math.PI / 180);
            dx = (double)class_3532.method_15374((double)f) * 2.0;
            dz = (double)(-class_3532.method_15362((double)f)) * 2.0;
        }
        double dist = Math.hypot(dx, dz);
        if (dist < 1.0E-4) {
            dx = 1.0;
            dz = 0.0;
            dist = 1.0;
        }
        double height = this.petIsOwl ? 1.8 : 0.5;
        return new class_243(playerEntity.method_23317() + dx / dist * 2.0, playerEntity.method_23318() + height, playerEntity.method_23321() + dz / dist * 2.0);
    }

    private void tickGazeReaction(class_1657 playerEntity, class_243 vec3d) {
        if (this.pet == null) {
            return;
        }
        class_243 vec3d2 = playerEntity.method_5720();
        class_243 vec3d3 = vec3d.method_1020(playerEntity.method_73189());
        double d = Math.hypot(vec3d3.field_1352, vec3d3.field_1350);
        boolean bl = false;
        if (d > 0.05 && d < 3.5) {
            class_243 vec3d4 = new class_243(vec3d2.field_1352, 0.0, vec3d2.field_1350);
            class_243 vec3d5 = new class_243(vec3d3.field_1352, 0.0, vec3d3.field_1350);
            if (vec3d4.method_1027() > 1.0E-4 && vec3d5.method_1027() > 1.0E-4) {
                vec3d4 = vec3d4.method_1029();
                vec3d5 = vec3d5.method_1029();
                double d2 = vec3d4.field_1352 * vec3d5.field_1352 + vec3d4.field_1350 * vec3d5.field_1350;
                bl = d2 > 0.88;
                boolean bl2 = bl;
            }
        }
        if (this.gazeCooldownTicks > 0) {
            --this.gazeCooldownTicks;
        }
        if (this.gazeLookActiveTicks > 0) {
            --this.gazeLookActiveTicks;
            float f = (float)Math.toDegrees(class_3532.method_15349((double)(playerEntity.method_23321() - vec3d.field_1350), (double)(playerEntity.method_23317() - vec3d.field_1352))) - 90.0f;
            this.pet.overrideLookYaw(f, 1.0f);
            if (this.gazeLookActiveTicks == 0) {
                this.gazeCooldownTicks = this.randomBetween(80, 160);
            }
            return;
        }
        this.pet.overrideLookYaw(0.0f, 0.0f);
        if (!bl || this.gazeCooldownTicks > 0) {
            if (!bl) {
                this.gazeBuildTicks = 0;
            }
            return;
        }
        ++this.gazeBuildTicks;
        if (this.gazeBuildTicks >= 25) {
            this.gazeBuildTicks = 0;
            if (this.random.method_43057() < 0.65f) {
                this.gazeLookActiveTicks = this.randomBetween(40, 70);
            } else {
                this.gazeCooldownTicks = this.randomBetween(40, 90);
            }
        }
    }

    private double findGroundY(double d, double d2, double d3) {
        if (this.mc.field_1687 == null) {
            return d3;
        }
        int n = class_3532.method_15357((double)d);
        int n2 = class_3532.method_15357((double)d2);
        int n3 = class_3532.method_15357((double)d3);
        class_2338.class_2339 mutable = new class_2338.class_2339(n, n3 + 2, n2);
        for (int i = n3 + 2; i >= n3 - 6; --i) {
            mutable.method_33098(i);
            class_2680 blockState = this.mc.field_1687.method_8320((class_2338)mutable);
            class_265 voxelShape = blockState.method_26220((class_1922)this.mc.field_1687, (class_2338)mutable);
            if (voxelShape.method_1110()) continue;
            return (double)i + voxelShape.method_1105(class_2350.class_2351.field_11052);
        }
        return d3;
    }

    public void tick(class_1657 playerEntity, CustomPetVariant customPetVariant, boolean bl) {
        if (playerEntity == null || playerEntity.method_73183() == null || this.mc.field_1687 == null) {
            this.reset();
            return;
        }
        this.ensurePet(playerEntity, customPetVariant, bl);
        this.tickPet(playerEntity, customPetVariant);
    }
}

