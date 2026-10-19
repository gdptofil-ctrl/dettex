/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_1299
 *  net.minecraft.class_1313
 *  net.minecraft.class_1922
 *  net.minecraft.class_1937
 *  net.minecraft.class_2338
 *  net.minecraft.class_2338$class_2339
 *  net.minecraft.class_2350$class_2351
 *  net.minecraft.class_243
 *  net.minecraft.class_265
 *  net.minecraft.class_2680
 *  net.minecraft.class_2960
 *  net.minecraft.class_3414
 *  net.minecraft.class_3532
 *  net.minecraft.class_3619
 *  net.minecraft.class_7102
 */
package ru.recoin.implement.features.modules.render.custompet.entity;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1313;
import net.minecraft.class_1922;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_2960;
import net.minecraft.class_3414;
import net.minecraft.class_3532;
import net.minecraft.class_3619;
import net.minecraft.class_7102;
import ru.recoin.api.mods.geckolib.animatable.GeoAnimatable;
import ru.recoin.api.mods.geckolib.animatable.GeoEntity;
import ru.recoin.api.mods.geckolib.animatable.instance.AnimatableInstanceCache;
import ru.recoin.api.mods.geckolib.animatable.manager.AnimatableManager;
import ru.recoin.api.mods.geckolib.animation.AnimationController;
import ru.recoin.api.mods.geckolib.animation.RawAnimation;
import ru.recoin.api.mods.geckolib.animation.object.PlayState;
import ru.recoin.api.mods.geckolib.animation.state.AnimationTest;
import ru.recoin.api.mods.geckolib.util.GeckoLibUtil;
import ru.recoin.implement.features.modules.render.custompet.CustomPetVariant;
import ru.recoin.implement.features.modules.render.custompet.sync.CustomPetRemoteState;

public class CustomPetEntity
extends class_7102
implements GeoEntity {
    private static final AtomicInteger NEXT_ENTITY_ID = new AtomicInteger(-20000);
    private static final float GROUND_FULL_SPEED_ANGLE = 6.0f;
    private static final float GROUND_MIN_SPEED_ANGLE = 84.0f;
    private static final float AIR_START_MOVE_ANGLE = 9.0f;
    private static final double MOVEMENT_ANIMATION_THRESHOLD_SQR = 2.5E-5;
    private static final double MOVEMENT_VERTICAL_THRESHOLD = 0.003;
    private static class_3414 ambientSound;
    private static class_3414 stepSound;
    private static boolean soundsResolved;
    private static final RawAnimation IDLE;
    private static final RawAnimation WALK;
    private static final RawAnimation RAIN_IDLE;
    private static final RawAnimation RAIN_WALK;
    private static final RawAnimation HAT_IDLE;
    private static final RawAnimation HAT_WALK;
    private static final RawAnimation FISHERMAN_IDLE;
    private static final RawAnimation FISHERMAN_WALK;
    private static final RawAnimation AIR_IDLE;
    private static final RawAnimation AIR_WALK;
    private static final RawAnimation ROBOT_IDLE;
    private static final RawAnimation ROBOT_FLY;
    private static final RawAnimation OWL_IDLE;
    private static final RawAnimation OWL_WALK;
    private static final RawAnimation OWL_RUN;
    private static final RawAnimation OWL_FLY;
    private static final double OWL_RUN_ANIMATION_SPEED = 1.4;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private class_243 desiredPosition = class_243.field_1353;
    private double desiredSpeed;
    private boolean desiredMoving;
    private boolean desiredUmbrella;
    private boolean desiredAirborne;
    private CustomPetVariant variant = CustomPetVariant.NITWIT;
    private boolean owl;
    private int robotType;
    private boolean moving;
    private boolean usingUmbrella;
    private boolean airborneMode;
    private int movingTicks;
    private int ambientSoundCooldown;
    private int stepSoundCooldown;
    private int jumpCooldown;
    private int blockedTicks;
    private int pauseTicks;
    private int sideStepDir;
    private int sideStepTicks;
    private float targetYaw;
    private float smoothedYaw;
    private float lookOverrideYaw;
    private float lookOverrideWeight;
    private double animationSpeed = 1.0;
    private double currentAnimationSpeed = 1.0;
    private double currentGroundSpeed;
    private double verticalVelocity;
    private double lastHorizontalDistance = Double.MAX_VALUE;
    private class_243 airMotion = class_243.field_1353;
    private boolean networkControlled;
    private boolean hasNetworkAnchor;
    private class_243 networkPosition = class_243.field_1353;
    private float networkYaw;
    private boolean networkMoving;
    private boolean networkUmbrella;
    private boolean networkAirborne;
    private double networkAnimationSpeed = 1.0;

    public CustomPetEntity(class_1937 world) {
        super(class_1299.field_37419, world);
        this.field_5960 = false;
        this.method_5875(false);
        this.method_5977(true);
        this.method_5838(NEXT_ENTITY_ID.getAndDecrement());
        this.method_5826(UUID.randomUUID());
        this.method_18799(class_243.field_1353);
        this.targetYaw = this.method_36454();
        this.smoothedYaw = this.method_36454();
        this.ambientSoundCooldown = 70;
        this.verticalVelocity = 0.0;
    }

    private <E extends GeoAnimatable> PlayState predicate(AnimationTest<E> animationTest) {
        RawAnimation rawAnimation;
        AnimationController<E> animationController = animationTest.controller();
        animationController.animationSpeed = this.currentAnimationSpeed;
        if (this.owl) {
            RawAnimation rawAnimation2 = this.airborneMode ? OWL_FLY : (!this.moving ? OWL_IDLE : (this.currentAnimationSpeed >= 1.4 ? OWL_RUN : OWL_WALK));
            animationController.setAnimation(rawAnimation2);
            return PlayState.CONTINUE;
        }
        if (this.variant.isRobot()) {
            rawAnimation = this.moving || this.airborneMode ? ROBOT_FLY : ROBOT_IDLE;
        } else if (this.airborneMode) {
            rawAnimation = AIR_IDLE;
        } else {
            rawAnimation = switch (this.variant) {
                default -> throw new MatchException(null, null);
                case CustomPetVariant.FISHERMAN -> {
                    if (this.usingUmbrella) {
                        if (this.moving) {
                            yield FISHERMAN_WALK;
                        }
                        yield FISHERMAN_IDLE;
                    }
                    if (this.moving) {
                        yield AIR_WALK;
                    }
                    yield AIR_IDLE;
                }
                case CustomPetVariant.GARDENER, CustomPetVariant.SORCERER -> {
                    if (this.usingUmbrella) {
                        if (this.moving) {
                            yield HAT_WALK;
                        }
                        yield HAT_IDLE;
                    }
                    if (this.moving) {
                        yield WALK;
                    }
                    yield IDLE;
                }
                case CustomPetVariant.DEFAULT, CustomPetVariant.NITWIT, CustomPetVariant.MERCHANT -> {
                    if (this.usingUmbrella) {
                        if (this.moving) {
                            yield RAIN_WALK;
                        }
                        yield RAIN_IDLE;
                    }
                    if (this.moving) {
                        yield WALK;
                    }
                    yield IDLE;
                }
                case CustomPetVariant.ROBOT -> this.moving ? ROBOT_FLY : ROBOT_IDLE;
            };
        }
        animationController.setAnimation(rawAnimation);
        return PlayState.CONTINUE;
    }

    private double square(double d) {
        return d * d;
    }

    public boolean isOwl() {
        return this.owl;
    }

    public void setOwl(boolean bl) {
        this.owl = bl;
    }

    private static class_3414 stepSound() {
        CustomPetEntity.resolveSounds();
        return stepSound;
    }

    public void snapTo(class_243 vec3d, float f) {
        this.method_5814(vec3d.field_1352, vec3d.field_1351, vec3d.field_1350);
        this.field_6014 = vec3d.field_1352;
        this.field_6036 = vec3d.field_1351;
        this.field_5969 = vec3d.field_1350;
        this.field_5982 = f;
        this.field_6004 = 0.0f;
        this.moving = false;
        this.usingUmbrella = false;
        this.airborneMode = false;
        this.movingTicks = 0;
        this.stepSoundCooldown = 0;
        this.ambientSoundCooldown = 40;
        this.jumpCooldown = 0;
        this.blockedTicks = 0;
        this.pauseTicks = 0;
        this.sideStepTicks = 0;
        this.targetYaw = f;
        this.smoothedYaw = f;
        this.animationSpeed = 1.0;
        this.currentAnimationSpeed = 1.0;
        this.currentGroundSpeed = 0.0;
        this.verticalVelocity = 0.0;
        this.lastHorizontalDistance = Double.MAX_VALUE;
        this.airMotion = class_243.field_1353;
        this.method_18799(class_243.field_1353);
        this.method_36456(f);
        this.method_5847(f);
        this.method_5636(f);
        this.field_6259 = f;
        this.field_6220 = f;
        this.networkPosition = vec3d;
        this.networkYaw = f;
        this.hasNetworkAnchor = false;
    }

    public int method_5978() {
        return 0;
    }

    public int method_5986() {
        return 0;
    }

    public void method_5773() {
        if (this.networkControlled) {
            super.method_5773();
            this.tickNetworkState();
            return;
        }
        this.method_5875(this.desiredAirborne);
        super.method_5773();
        this.tickCustomMovement();
    }

    public float method_49476() {
        return this.airborneMode ? 0.0f : 1.0f;
    }

    public boolean method_5640(double distance) {
        return true;
    }

    public boolean method_5863() {
        return false;
    }

    public float method_61415(float tickProgress) {
        return class_3532.method_17821((float)tickProgress, (float)this.field_5982, (float)this.method_36454());
    }

    public boolean method_5810() {
        return false;
    }

    public void method_5697(class_1297 entity) {
    }

    public void method_5762(double deltaX, double deltaY, double deltaZ) {
    }

    public void method_60491(class_243 vec) {
    }

    public boolean method_30949(class_1297 other) {
        return false;
    }

    public boolean method_30948(class_1297 entity) {
        return false;
    }

    public boolean method_5732() {
        return false;
    }

    public class_3619 method_5657() {
        return class_3619.field_15975;
    }

    public boolean isPetMoving() {
        return this.moving;
    }

    public double getCurrentAnimationSpeed() {
        return this.currentAnimationSpeed;
    }

    public void applyNetworkState(CustomPetRemoteState customPetRemoteState) {
        if (customPetRemoteState == null) {
            this.networkControlled = false;
            this.hasNetworkAnchor = false;
            return;
        }
        this.networkControlled = true;
        this.setPetVariant(customPetRemoteState.variant());
        this.setRobotType(customPetRemoteState.robotType());
        this.networkPosition = customPetRemoteState.position();
        this.networkYaw = customPetRemoteState.yaw();
        this.networkMoving = customPetRemoteState.moving();
        this.networkUmbrella = customPetRemoteState.umbrella();
        this.networkAirborne = customPetRemoteState.airborne();
        this.networkAnimationSpeed = customPetRemoteState.animationSpeed();
    }

    public void setRobotType(int n) {
        this.robotType = n;
    }

    public boolean shouldUseUmbrella() {
        return this.usingUmbrella;
    }

    public boolean isAirborneMode() {
        return this.airborneMode;
    }

    public void setPetVariant(CustomPetVariant customPetVariant) {
        this.variant = customPetVariant == null ? CustomPetVariant.NITWIT : customPetVariant;
    }

    private static class_3414 ambientSound() {
        CustomPetEntity.resolveSounds();
        return ambientSound;
    }

    public int getRobotType() {
        return this.robotType;
    }

    public boolean isMovementBlocked() {
        return this.pauseTicks > 0 || this.blockedTicks >= 3;
    }

    private void tickNetworkState() {
        double d;
        double d2;
        double d3;
        double d4;
        this.movingTicks = 0;
        this.jumpCooldown = 0;
        this.blockedTicks = 0;
        this.pauseTicks = 0;
        this.currentGroundSpeed = 0.0;
        this.verticalVelocity = 0.0;
        this.airMotion = class_243.field_1353;
        this.lastHorizontalDistance = Double.MAX_VALUE;
        this.airborneMode = this.networkAirborne;
        this.usingUmbrella = this.networkUmbrella && !this.networkAirborne && !this.variant.isRobot();
        this.method_5875(true);
        this.method_18799(class_243.field_1353);
        double d5 = this.method_23317();
        double d6 = this.method_23318();
        double d7 = this.method_23321();
        double d8 = this.networkPosition.field_1352 - d5;
        double d9 = this.networkPosition.field_1351 - d6;
        double d10 = this.networkPosition.field_1350 - d7;
        double d11 = d8 * d8 + d9 * d9 + d10 * d10;
        if (!this.hasNetworkAnchor || d11 > 36.0) {
            d4 = this.networkPosition.field_1352;
            d3 = this.networkPosition.field_1351;
            d2 = this.networkPosition.field_1350;
            this.field_6014 = d4;
            this.field_6036 = d3;
            this.field_5969 = d2;
            this.hasNetworkAnchor = true;
        } else {
            d = 0.35;
            d4 = d5 + d8 * d;
            d3 = d6 + d9 * d;
            d2 = d7 + d10 * d;
            this.field_6014 = d5;
            this.field_6036 = d6;
            this.field_5969 = d7;
        }
        this.method_5814(d4, d3, d2);
        d = (d4 - this.field_6014) * (d4 - this.field_6014) + (d2 - this.field_5969) * (d2 - this.field_5969);
        boolean bl = d > 2.5E-5 || Math.abs(d3 - this.field_6036) > 0.003;
        this.moving = this.networkMoving && bl;
        this.animationSpeed = this.moving ? this.networkAnimationSpeed : 1.0;
        this.currentAnimationSpeed = this.animationSpeed;
        this.field_5982 = this.method_36454();
        this.field_6259 = this.field_6241;
        this.field_6220 = this.field_6283;
        this.targetYaw = this.networkYaw;
        this.smoothedYaw = class_3532.method_15388((float)this.method_36454(), (float)this.networkYaw, (float)18.0f);
        this.method_36456(this.smoothedYaw);
        this.method_5847(this.smoothedYaw);
        this.method_5636(this.smoothedYaw);
    }

    private void tickClientAudio(boolean bl, double d) {
        class_3414 soundEvent;
        if (this.method_73183() == null || !this.emitsAudioCues()) {
            return;
        }
        if (this.variant.isRobot()) {
            return;
        }
        if (this.ambientSoundCooldown > 0) {
            --this.ambientSoundCooldown;
        }
        if (this.stepSoundCooldown > 0) {
            --this.stepSoundCooldown;
        }
        if (bl && d > 4.0E-4 && this.method_24828() && this.stepSoundCooldown <= 0) {
            soundEvent = CustomPetEntity.stepSound();
            if (soundEvent != null) {
                this.method_73183().method_8486(this.method_23317(), this.method_23318(), this.method_23321(), soundEvent, this.method_5634(), 0.55f, 0.94f + this.field_5974.method_43057() * 0.12f, false);
            }
            this.stepSoundCooldown = 8 + this.field_5974.method_43048(4);
        }
        if (!bl && this.ambientSoundCooldown <= 0) {
            soundEvent = CustomPetEntity.ambientSound();
            if (soundEvent != null) {
                this.method_73183().method_8486(this.method_23317(), this.method_23318(), this.method_23321(), soundEvent, this.method_5634(), 0.75f, 0.94f + this.field_5974.method_43057() * 0.14f, false);
            }
            this.ambientSoundCooldown = 120 + this.field_5974.method_43048(100);
        }
    }

    public CustomPetVariant getPetVariant() {
        return this.variant;
    }

    private void snapToGroundIfClose() {
        if (this.method_73183() == null) {
            return;
        }
        double d = this.method_23317();
        double d2 = this.method_23318();
        double d3 = this.method_23321();
        int n = class_3532.method_15357((double)d);
        int n2 = class_3532.method_15357((double)d3);
        int n3 = class_3532.method_15357((double)(d2 + 0.2));
        class_2338.class_2339 mutable = new class_2338.class_2339(n, n3 + 1, n2);
        for (int i = n3 + 1; i >= n3 - 4; --i) {
            mutable.method_10103(n, i, n2);
            class_2680 blockState = this.method_73183().method_8320((class_2338)mutable);
            class_265 voxelShape = blockState.method_26220((class_1922)this.method_73183(), (class_2338)mutable);
            if (voxelShape.method_1110()) continue;
            double d4 = (double)i + voxelShape.method_1105(class_2350.class_2351.field_11052);
            double d5 = d2 - d4;
            if (!(d4 >= -0.08) || !(d5 <= 0.72)) continue;
            this.method_5814(d, d4, d3);
            this.verticalVelocity = 0.0;
            this.field_6017 = 0.0;
            this.method_18799(class_243.field_1353);
            return;
        }
    }

    private static void resolveSounds() {
        if (soundsResolved) {
            return;
        }
        soundsResolved = true;
        try {
            ambientSound = class_3414.method_47908((class_2960)class_2960.method_60655((String)"minecraft", (String)"entity.ribbit.ambient"));
            stepSound = class_3414.method_47908((class_2960)class_2960.method_60655((String)"minecraft", (String)"entity.ribbit.step"));
        }
        catch (Throwable throwable) {
            ambientSound = null;
            stepSound = null;
        }
    }

    protected boolean emitsAudioCues() {
        return !this.owl;
    }

    public void setBehavior(class_243 vec3d, double d, boolean bl, boolean bl2, boolean bl3, double d2) {
        if (vec3d.method_1025(this.desiredPosition) > 0.2) {
            this.blockedTicks = 0;
            this.pauseTicks = 0;
            this.lastHorizontalDistance = Double.MAX_VALUE;
        }
        this.desiredPosition = vec3d;
        this.desiredSpeed = d;
        this.desiredMoving = bl;
        this.desiredUmbrella = bl2;
        this.desiredAirborne = bl3;
        this.animationSpeed = d2;
    }

    private void tickAirMovement() {
        double d;
        double d2;
        float f;
        class_243 vec3d = this.method_73189();
        class_243 vec3d2 = this.desiredPosition.method_1020(vec3d);
        class_243 vec3d3 = new class_243(vec3d2.field_1352, 0.0, vec3d2.field_1350);
        double d3 = vec3d3.method_37267();
        double d4 = vec3d2.method_1033();
        double d5 = Math.abs(vec3d2.field_1351);
        boolean bl2 = this.desiredMoving && d4 > 0.02;
        boolean bl3 = bl2;
        if (this.lookOverrideWeight > 0.001f) {
            this.targetYaw = class_3532.method_15388((float)this.targetYaw, (float)this.lookOverrideYaw, (float)28.0f);
        } else if (d3 > 1.0E-4) {
            f = (float)Math.toDegrees(class_3532.method_15349((double)vec3d3.field_1350, (double)vec3d3.field_1352)) - 90.0f;
            this.targetYaw = class_3532.method_15388((float)this.targetYaw, (float)f, (float)10.5f);
        }
        this.smoothedYaw = class_3532.method_15388((float)this.smoothedYaw, (float)this.targetYaw, (float)24.0f);
        f = Math.abs(class_3532.method_15393((float)(this.targetYaw - this.smoothedYaw)));
        boolean bl4 = bl2 && (f <= 9.0f || d4 <= 0.35 || this.lookOverrideWeight > 0.001f);
        this.movingTicks = 0;
        this.jumpCooldown = 0;
        this.blockedTicks = 0;
        this.pauseTicks = 0;
        this.verticalVelocity = 0.0;
        boolean bl5 = d3 <= 0.16 && d5 <= 0.012;
        boolean bl6 = d3 <= 0.42 && d5 <= 0.09;
        class_243 vec3d4 = class_243.field_1353;
        if (bl4 && d4 > 1.0E-5 && !bl5) {
            d2 = class_3532.method_15350((double)((d4 - 0.2) / 1.6), (double)0.08, (double)1.0);
            d = Math.clamp(this.desiredSpeed * d2, 0.012, 6.5);
            vec3d4 = vec3d2.method_1029().method_1021(Math.min(d4, d));
        } else if (!this.desiredMoving && d5 > 0.001) {
            d2 = class_3532.method_15350((double)(d5 * 0.38), (double)0.0025, (double)0.018);
            vec3d4 = new class_243(0.0, Math.copySign(Math.min(d5, d2), vec3d2.field_1351), 0.0);
        }
        d2 = Math.clamp(0.12 + d4 * 0.06 + this.desiredSpeed * 0.028, 0.12, 0.38);
        this.airMotion = this.airMotion.method_35590(vec3d4, d2);
        if (!this.desiredMoving) {
            this.airMotion = new class_243(this.airMotion.field_1352 * 0.4, this.airMotion.field_1351, this.airMotion.field_1350 * 0.4);
        }
        d = bl5 ? 0.18 : (bl6 ? 0.42 : (d4 < 1.5 ? 0.78 : 0.9));
        boolean bl = !this.desiredMoving && d5 > 0.001;
        boolean bl7 = bl;
        if (!(bl4 && !(vec3d4.method_1027() < 1.0E-6) || bl)) {
            this.airMotion = this.airMotion.method_1021(d);
        } else if (bl) {
            this.airMotion = new class_243(this.airMotion.field_1352, this.airMotion.field_1351 * 0.96, this.airMotion.field_1350);
        }
        if (bl5 && d5 <= 0.0015 || this.airMotion.method_1027() < 4.0E-5) {
            this.airMotion = class_243.field_1353;
        }
        class_243 vec3d5 = this.airMotion.method_1027() > 0.0 ? (this.airMotion.method_1033() > d4 ? vec3d2 : this.airMotion) : class_243.field_1353;
        this.method_5784(class_1313.field_6308, vec3d5);
        if (this.field_5976 && this.desiredMoving) {
            this.method_5784(class_1313.field_6308, new class_243(0.0, 0.12, 0.0));
        }
        class_243 vec3d6 = this.method_73189();
        double d6 = this.square(vec3d6.field_1352 - vec3d.field_1352) + this.square(vec3d6.field_1350 - vec3d.field_1350);
        this.moving = bl2 && (d6 > 2.5E-5 || Math.abs(vec3d6.field_1351 - vec3d.field_1351) > 0.003);
        this.lastHorizontalDistance = bl2 ? d3 : Double.MAX_VALUE;
        double d7 = this.moving ? Math.max(1.0, this.animationSpeed) : 1.0;
        this.currentAnimationSpeed += (d7 - this.currentAnimationSpeed) * (this.moving ? 0.2 : 0.16);
        this.method_18799(class_243.field_1353);
        this.field_5982 = this.method_36454();
        this.field_6259 = this.field_6241;
        this.field_6220 = this.field_6283;
        this.method_36456(this.smoothedYaw);
        this.method_5847(this.smoothedYaw);
        this.method_5636(this.smoothedYaw);
        this.tickClientAudio(false, 0.0);
    }

    public void overrideLookYaw(float f, float f2) {
        this.lookOverrideYaw = f;
        this.lookOverrideWeight = Math.max(0.0f, Math.min(1.0f, f2));
    }

    private void tickCustomMovement() {
        class_243 vec3d;
        float f;
        boolean bl5 = this.airborneMode;
        this.airborneMode = this.desiredAirborne;
        this.usingUmbrella = this.desiredUmbrella && !this.airborneMode && !this.variant.isRobot();
        this.method_5875(this.airborneMode);
        if (!(this.airborneMode || !bl5 && this.desiredMoving)) {
            this.snapToGroundIfClose();
        }
        if (this.jumpCooldown > 0) {
            --this.jumpCooldown;
        }
        if (this.pauseTicks > 0) {
            --this.pauseTicks;
            this.moving = false;
            this.movingTicks = 0;
            this.currentGroundSpeed = 0.0;
            this.verticalVelocity = 0.0;
            this.airMotion = class_243.field_1353;
            this.method_18799(class_243.field_1353);
            this.currentAnimationSpeed += (1.0 - this.currentAnimationSpeed) * 0.28;
            if (this.lookOverrideWeight > 0.001f) {
                this.targetYaw = class_3532.method_15388((float)this.targetYaw, (float)this.lookOverrideYaw, (float)28.0f);
            }
            this.smoothedYaw = class_3532.method_15388((float)this.smoothedYaw, (float)this.targetYaw, (float)24.0f);
            this.field_5982 = this.method_36454();
            this.field_6259 = this.field_6241;
            this.field_6220 = this.field_6283;
            this.method_36456(this.smoothedYaw);
            this.method_5847(this.smoothedYaw);
            this.method_5636(this.smoothedYaw);
            this.tickClientAudio(false, 0.0);
            return;
        }
        if (this.airborneMode) {
            this.tickAirMovement();
            return;
        }
        this.airMotion = class_243.field_1353;
        class_243 vec3d3 = this.method_73189();
        class_243 vec3d4 = this.desiredPosition.method_1020(vec3d3);
        class_243 vec3d5 = new class_243(vec3d4.field_1352, 0.0, vec3d4.field_1350);
        double d = vec3d5.method_37267();
        boolean bl4 = this.desiredMoving && d > 0.045;
        boolean bl6 = bl4;
        if (this.lookOverrideWeight > 0.001f) {
            this.targetYaw = class_3532.method_15388((float)this.targetYaw, (float)this.lookOverrideYaw, (float)28.0f);
        } else if (bl4) {
            float f2 = (float)Math.toDegrees(class_3532.method_15349((double)vec3d5.field_1350, (double)vec3d5.field_1352)) - 90.0f;
            f = (float)Math.clamp(9.5 + d * 2.4 + this.desiredSpeed * 68.0, 9.5, 24.0);
            this.targetYaw = class_3532.method_15388((float)this.targetYaw, (float)f2, (float)f);
        }
        this.smoothedYaw = class_3532.method_15388((float)this.smoothedYaw, (float)this.targetYaw, (float)24.0f);
        f = Math.abs(class_3532.method_15393((float)(this.targetYaw - this.smoothedYaw)));
        double d2 = bl4 ? (this.lookOverrideWeight > 0.001f ? 1.0 : class_3532.method_15350((double)(1.0 - Math.max(0.0, (double)(f - 6.0f)) / 78.0), (double)0.18, (double)1.0)) : 0.0;
        double d3 = bl4 ? class_3532.method_15350((double)((d - 0.04) / 1.6), (double)0.34, (double)1.0) : 0.0;
        double d4 = bl4 ? Math.min(d, this.desiredSpeed * d3 * d2) : 0.0;
        double d5 = bl4 ? Math.clamp(0.18 + this.desiredSpeed * 0.95 + d * 0.06, 0.18, 0.52) : 0.24;
        this.currentGroundSpeed += (d4 - this.currentGroundSpeed) * d5;
        if (Math.abs(this.currentGroundSpeed) < 1.0E-4) {
            this.currentGroundSpeed = 0.0;
        }
        boolean bl3 = this.currentGroundSpeed > 0.012;
        boolean bl7 = bl3;
        this.moving = bl4 ? bl3 || d > 0.18 : bl3;
        class_243 vec3d2 = this.currentGroundSpeed > 1.0E-5 && d > 1.0E-5 ? vec3d5.method_1029().method_1021(Math.min(d, this.currentGroundSpeed)) : class_243.field_1353;
        class_243 vec3d6 = vec3d2;
        if (this.field_5976 && bl4 && this.blockedTicks >= 1 && d > 0.25 && this.currentGroundSpeed > 0.02) {
            if (this.sideStepTicks <= 0) {
                this.sideStepDir = this.field_5974.method_43056() ? 1 : -1;
                this.sideStepTicks = 12;
            }
            class_243 vec3d7 = vec3d5.method_1027() > 1.0E-6 ? vec3d5.method_1029() : new class_243(0.0, 0.0, 1.0);
            class_243 vec3d8 = new class_243(-vec3d7.field_1350 * (double)this.sideStepDir, 0.0, vec3d7.field_1352 * (double)this.sideStepDir);
            vec3d = vec3d7.method_1021(0.45).method_1019(vec3d8.method_1021(0.9));
            if (vec3d.method_1027() > 1.0E-6) {
                double d6 = Math.max(this.currentGroundSpeed, this.desiredSpeed * 0.7);
                vec3d2 = vec3d.method_1029().method_1021(Math.min(d, d6));
                if (this.lookOverrideWeight <= 0.001f) {
                    float moveYaw = (float)Math.toDegrees(class_3532.method_15349((double)vec3d2.field_1350, (double)vec3d2.field_1352)) - 90.0f;
                    this.targetYaw = class_3532.method_15388((float)this.targetYaw, (float)moveYaw, (float)22.0f);
                }
            }
        }
        if (this.sideStepTicks > 0) {
            --this.sideStepTicks;
        }
        double d7 = this.desiredPosition.field_1351 - this.method_23318();
        if (this.method_24828()) {
            if (d7 > 1.15 && d <= 1.6 && bl4 && this.currentGroundSpeed > 0.035 && this.jumpCooldown <= 0 && this.blockedTicks < 3) {
                this.verticalVelocity = 0.42;
                this.jumpCooldown = 11;
            } else {
                this.verticalVelocity = d7 < -0.35 ? Math.max(-0.16, d7) : 0.0;
            }
        } else {
            this.verticalVelocity = Math.max(this.verticalVelocity - 0.08, -0.36);
        }
        vec3d = new class_243(vec3d2.field_1352, this.verticalVelocity, vec3d2.field_1350);
        this.method_5784(class_1313.field_6308, vec3d);
        class_243 vec3d9 = this.method_73189();
        double d8 = this.square(vec3d9.field_1352 - vec3d3.field_1352) + this.square(vec3d9.field_1350 - vec3d3.field_1350);
        boolean bl2 = d8 > 2.5E-5 || Math.abs(vec3d9.field_1351 - vec3d3.field_1351) > 0.003;
        boolean bl8 = bl2;
        if (this.method_24828() && this.verticalVelocity < 0.0) {
            this.verticalVelocity = 0.0;
        }
        if (this.method_24828() && this.verticalVelocity <= 0.0 && this.desiredPosition.field_1351 <= this.method_23318() && Math.abs(this.desiredPosition.field_1351 - this.method_23318()) < 0.18) {
            this.method_5814(this.method_23317(), this.desiredPosition.field_1351, this.method_23321());
        }
        this.moving = bl4 && bl2;
        class_243 vec3d10 = this.desiredPosition.method_1020(this.method_73189());
        double d9 = Math.hypot(vec3d10.field_1352, vec3d10.field_1350);
        boolean bl = d9 + 0.025 < this.lastHorizontalDistance;
        boolean bl9 = bl;
        if (this.currentGroundSpeed > 0.02 && this.field_5976 && this.method_24828()) {
            ++this.blockedTicks;
        } else if (!bl4 || bl || d9 < 0.8) {
            this.blockedTicks = 0;
        }
        if (this.blockedTicks >= 7) {
            this.pauseTicks = 8;
            this.blockedTicks = 0;
            this.sideStepTicks = 0;
            this.moving = false;
            this.movingTicks = 0;
            this.currentGroundSpeed = 0.0;
            this.verticalVelocity = 0.0;
        }
        this.lastHorizontalDistance = bl4 ? d9 : Double.MAX_VALUE;
        double d10 = this.moving ? Math.max(1.0, this.animationSpeed) : 1.0;
        this.currentAnimationSpeed += (d10 - this.currentAnimationSpeed) * (this.moving ? 0.22 : 0.18);
        this.method_18799(class_243.field_1353);
        this.field_5982 = this.method_36454();
        this.field_6259 = this.field_6241;
        this.field_6220 = this.field_6283;
        this.method_36456(this.smoothedYaw);
        this.method_5847(this.smoothedYaw);
        this.method_5636(this.smoothedYaw);
        this.tickClientAudio(this.moving, vec3d2.method_37268());
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController("controller", 5, this::predicate));
    }

    static {
        IDLE = RawAnimation.begin().thenPlay("idle");
        WALK = RawAnimation.begin().thenPlay("walk");
        RAIN_IDLE = RawAnimation.begin().thenPlay("idle_holding_1");
        RAIN_WALK = RawAnimation.begin().thenPlay("walk_holding_1");
        HAT_IDLE = RawAnimation.begin().thenPlay("idle_holding_hat");
        HAT_WALK = RawAnimation.begin().thenPlay("walk_holding_hat");
        FISHERMAN_IDLE = RawAnimation.begin().thenPlay("idle_holding_fisherman");
        FISHERMAN_WALK = RawAnimation.begin().thenPlay("walk_holding_fisherman");
        AIR_IDLE = RawAnimation.begin().thenPlay("idle_holding_2");
        AIR_WALK = RawAnimation.begin().thenPlay("walk_holding_2");
        ROBOT_IDLE = RawAnimation.begin().thenPlay("robot_idle");
        ROBOT_FLY = RawAnimation.begin().thenPlay("robot_fly");
        OWL_IDLE = RawAnimation.begin().thenPlay("animation.owl_jump_rope.skip_idle");
        OWL_WALK = RawAnimation.begin().thenPlay("animation.owl_jump_rope.walk_skip");
        OWL_RUN = RawAnimation.begin().thenPlay("animation.owl_jump_rope.run_skip");
        OWL_FLY = RawAnimation.begin().thenPlay("animation.owl_jump_rope.fly");
    }
}

