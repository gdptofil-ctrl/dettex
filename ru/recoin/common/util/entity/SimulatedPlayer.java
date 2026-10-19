/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap
 *  it.unimi.dsi.fastutil.objects.Object2DoubleMap
 *  net.minecraft.class_10185
 *  net.minecraft.class_1291
 *  net.minecraft.class_1293
 *  net.minecraft.class_1294
 *  net.minecraft.class_1297
 *  net.minecraft.class_1320
 *  net.minecraft.class_1657
 *  net.minecraft.class_1690
 *  net.minecraft.class_1922
 *  net.minecraft.class_1937
 *  net.minecraft.class_2246
 *  net.minecraft.class_2248
 *  net.minecraft.class_2338
 *  net.minecraft.class_2338$class_2339
 *  net.minecraft.class_2350
 *  net.minecraft.class_238
 *  net.minecraft.class_2399
 *  net.minecraft.class_243
 *  net.minecraft.class_2533
 *  net.minecraft.class_2680
 *  net.minecraft.class_2769
 *  net.minecraft.class_3481
 *  net.minecraft.class_3486
 *  net.minecraft.class_3532
 *  net.minecraft.class_3610
 *  net.minecraft.class_3611
 *  net.minecraft.class_5134
 *  net.minecraft.class_5635
 *  net.minecraft.class_6862
 *  net.minecraft.class_6880
 *  net.minecraft.class_744
 *  net.minecraft.class_746
 */
package ru.recoin.common.util.entity;

import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import net.minecraft.class_10185;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1320;
import net.minecraft.class_1657;
import net.minecraft.class_1690;
import net.minecraft.class_1922;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_2399;
import net.minecraft.class_243;
import net.minecraft.class_2533;
import net.minecraft.class_2680;
import net.minecraft.class_2769;
import net.minecraft.class_3481;
import net.minecraft.class_3486;
import net.minecraft.class_3532;
import net.minecraft.class_3610;
import net.minecraft.class_3611;
import net.minecraft.class_5134;
import net.minecraft.class_5635;
import net.minecraft.class_6862;
import net.minecraft.class_6880;
import net.minecraft.class_744;
import net.minecraft.class_746;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.entity.MovingUtil;
import ru.recoin.common.util.entity.PlayerSimulation;
import ru.recoin.implement.features.modules.player.NoDelay;

public class SimulatedPlayer
implements PlayerSimulation,
QuickImports {
    public final class_1657 player;
    public final SimulatedPlayerInput input;
    public class_243 pos;
    public class_243 velocity;
    public class_238 boundingBox;
    public float yaw;
    public float pitch;
    public boolean sprinting;
    public float fallDistance;
    public int jumpingCooldown;
    public boolean isJumping;
    public boolean isFallFlying;
    public boolean onGround;
    public boolean horizontalCollision;
    public boolean verticalCollision;
    public boolean touchingWater;
    public boolean isSwimming;
    public boolean submergedInWater;
    private final Object2DoubleMap<class_6862<class_3611>> fluidHeight;
    private final HashSet<class_6862<class_3611>> submergedFluidTag;
    private int simulatedTicks = 0;
    private boolean clipLedged = false;
    private static final double STEP_HEIGHT = 0.5;

    public SimulatedPlayer(class_1657 player, SimulatedPlayerInput input, class_243 pos, class_243 velocity, class_238 boundingBox, float yaw, float pitch, boolean sprinting, float fallDistance, int jumpingCooldown, boolean isJumping, boolean isFallFlying, boolean onGround, boolean horizontalCollision, boolean verticalCollision, boolean touchingWater, boolean isSwimming, boolean submergedInWater, Object2DoubleMap<class_6862<class_3611>> fluidHeight, HashSet<class_6862<class_3611>> submergedFluidTag) {
        this.player = player;
        this.input = input;
        this.pos = pos;
        this.velocity = velocity;
        this.boundingBox = boundingBox;
        this.yaw = yaw;
        this.pitch = pitch;
        this.sprinting = sprinting;
        this.fallDistance = fallDistance;
        this.jumpingCooldown = jumpingCooldown;
        this.isJumping = isJumping;
        this.isFallFlying = isFallFlying;
        this.onGround = onGround;
        this.horizontalCollision = horizontalCollision;
        this.verticalCollision = verticalCollision;
        this.touchingWater = touchingWater;
        this.isSwimming = isSwimming;
        this.submergedInWater = submergedInWater;
        this.fluidHeight = fluidHeight;
        this.submergedFluidTag = submergedFluidTag;
    }

    public static SimulatedPlayer simulateLocalPlayer(int ticks) {
        SimulatedPlayer simulatedPlayer = SimulatedPlayer.fromClientPlayer(SimulatedPlayerInput.fromClientPlayer(SimulatedPlayer.mc.field_1724.field_3913.field_54155));
        for (int i = 0; i < ticks; ++i) {
            simulatedPlayer.tick();
        }
        return simulatedPlayer;
    }

    public static SimulatedPlayer simulateOtherPlayer(class_1657 player, int ticks) {
        SimulatedPlayer simulatedPlayer = SimulatedPlayer.fromOtherPlayer(player, SimulatedPlayerInput.guessInput(player));
        for (int i = 0; i < ticks; ++i) {
            simulatedPlayer.tick();
        }
        return simulatedPlayer;
    }

    public static SimulatedPlayer fromClientPlayer(SimulatedPlayerInput input) {
        class_746 player = SimulatedPlayer.mc.field_1724;
        return new SimulatedPlayer((class_1657)player, input, player.method_73189(), player.method_18798(), player.method_5829(), player.method_36454(), player.method_36455(), player.method_5624(), (float)player.field_6017, player.field_6228, player.field_6282, player.method_6128(), player.method_24828(), player.field_5976, player.field_5992, player.method_5799(), player.method_5681(), player.method_5869(), (Object2DoubleMap<class_6862<class_3611>>)new Object2DoubleArrayMap(player.field_5964), new HashSet<class_6862<class_3611>>(player.field_25599));
    }

    public static SimulatedPlayer fromOtherPlayer(class_1657 player, SimulatedPlayerInput input) {
        return new SimulatedPlayer(player, input, player.method_73189(), player.method_73189().method_1020(new class_243(player.field_6014, player.field_6036, player.field_5969)), player.method_5829(), player.method_36454(), player.method_36455(), player.method_5624(), (float)player.field_6017, player.field_6228, player.field_6282, player.method_6128(), player.method_24828(), player.field_5976, player.field_5992, player.method_5799(), player.method_5681(), player.method_5869(), (Object2DoubleMap<class_6862<class_3611>>)new Object2DoubleArrayMap(player.field_5964), new HashSet<class_6862<class_3611>>(player.field_25599));
    }

    @Override
    public class_243 pos() {
        return this.player.method_73189();
    }

    @Override
    public void tick() {
        ++this.simulatedTicks;
        this.clipLedged = false;
        if (this.pos.field_1351 <= -70.0) {
            return;
        }
        this.input.update();
        this.checkWaterState();
        this.updateSubmergedInWaterState();
        this.updateSwimming();
        if (this.jumpingCooldown > 0) {
            --this.jumpingCooldown;
        }
        this.isJumping = this.input.playerInput.comp_3163();
        double newX = this.velocity.field_1352;
        double newY = this.velocity.field_1351;
        double newZ = this.velocity.field_1350;
        if (Math.abs(this.velocity.field_1352) < 0.003) {
            newX = 0.0;
        }
        if (Math.abs(this.velocity.field_1351) < 0.003) {
            newY = 0.0;
        }
        if (Math.abs(this.velocity.field_1350) < 0.003) {
            newZ = 0.0;
        }
        if (this.onGround) {
            this.isFallFlying = false;
        }
        this.velocity = new class_243(newX, newY, newZ);
        if (this.isJumping) {
            double fluidLevel = this.isInLava() ? this.getFluidHeight((class_6862<class_3611>)class_3486.field_15518) : this.getFluidHeight((class_6862<class_3611>)class_3486.field_15517);
            boolean inWater = this.isTouchingWater() && fluidLevel > 0.0;
            double swimHeight = this.getSwimHeight();
            if (inWater && (!this.onGround || fluidLevel > swimHeight)) {
                this.swimUpward((class_6862<class_3611>)class_3486.field_15517);
            } else if (this.isInLava() && (!this.onGround || fluidLevel > swimHeight)) {
                this.swimUpward((class_6862<class_3611>)class_3486.field_15518);
            } else if ((this.onGround || inWater && fluidLevel <= swimHeight) && this.jumpingCooldown == 0) {
                this.jump();
                if (!(!this.player.equals((Object)SimulatedPlayer.mc.field_1724) || NoDelay.getInstance().isState() && NoDelay.getInstance().ignoreSetting.isSelected("Jump"))) {
                    this.jumpingCooldown = 10;
                }
            }
        }
        float sidewaysSpeed = this.input.movementSideways * 0.98f;
        float forwardSpeed = this.input.movementForward * 0.98f;
        float upwardsSpeed = 0.0f;
        if (this.hasStatusEffect((class_6880<class_1291>)class_1294.field_5906) || this.hasStatusEffect((class_6880<class_1291>)class_1294.field_5902)) {
            this.onLanding();
        }
        this.travel(new class_243((double)sidewaysSpeed, (double)upwardsSpeed, (double)forwardSpeed));
    }

    private void travel(class_243 movementInput) {
        boolean falling;
        if (this.isSwimming && !this.player.method_5765()) {
            double g = this.getRotationVector().field_1351;
            double h = g < -0.2 ? 0.085 : 0.06;
            class_2338 posAbove = new class_2338(class_3532.method_15357((double)this.pos.field_1352), class_3532.method_15357((double)(this.pos.field_1351 + 1.0 - 0.1)), class_3532.method_15357((double)this.pos.field_1350));
            if (g <= 0.0 || this.input.playerInput.comp_3163() || !this.player.method_73183().method_8320(posAbove).method_26227().method_15769()) {
                this.velocity = this.velocity.method_1031(0.0, (g - this.velocity.field_1351) * h, 0.0);
            }
        }
        double beforeTravelVelocityY = this.velocity.field_1351;
        double d = 0.08;
        boolean bl = falling = this.velocity.field_1351 <= 0.0;
        if (this.velocity.field_1351 <= 0.0 && this.hasStatusEffect((class_6880<class_1291>)class_1294.field_5906)) {
            d = 0.01;
            this.onLanding();
        }
        if (this.isTouchingWater() && this.player.method_29920()) {
            class_243 vec3d2;
            double e = this.pos.field_1351;
            float f = this.isSprinting() ? 0.9f : 0.8f;
            float g = 0.02f;
            float h = (float)this.getAttributeValue((class_6880<class_1320>)class_5134.field_51578);
            if (!this.onGround) {
                h *= 0.5f;
            }
            if (h > 0.0f) {
                f += (0.54600006f - f) * h / 3.0f;
                g += (this.getMovementSpeed() - g) * h / 3.0f;
            }
            if (this.hasStatusEffect((class_6880<class_1291>)class_1294.field_5900)) {
                f = 0.96f;
            }
            this.updateVelocity(g, movementInput);
            this.move(this.velocity);
            class_243 tempVel = this.velocity;
            if (this.horizontalCollision && this.isClimbing()) {
                tempVel = new class_243(tempVel.field_1352, 0.2, tempVel.field_1350);
            }
            this.velocity = tempVel.method_18805((double)f, 0.8, (double)f);
            this.velocity = vec3d2 = this.player.method_26317(d, falling, this.velocity);
            if (this.horizontalCollision && this.doesNotCollide(vec3d2.field_1352, vec3d2.field_1351 + 0.6 - this.pos.field_1351 + e, vec3d2.field_1350)) {
                this.velocity = new class_243(vec3d2.field_1352, 0.3, vec3d2.field_1350);
            }
        } else if (this.isInLava() && this.player.method_29920()) {
            double e = this.pos.field_1351;
            this.updateVelocity(0.02f, movementInput);
            this.move(this.velocity);
            if (this.getFluidHeight((class_6862<class_3611>)class_3486.field_15518) <= this.getSwimHeight()) {
                this.velocity = this.velocity.method_18805(0.5, 0.8, 0.5);
                this.velocity = this.player.method_26317(d, falling, this.velocity);
            } else {
                this.velocity = this.velocity.method_1021(0.5);
            }
            if (!this.player.method_5740()) {
                this.velocity = this.velocity.method_1031(0.0, -d / 4.0, 0.0);
            }
            if (this.horizontalCollision && this.doesNotCollide(this.velocity.field_1352, this.velocity.field_1351 + 0.6 - this.pos.field_1351 + e, this.velocity.field_1350)) {
                this.velocity = new class_243(this.velocity.field_1352, 0.3, this.velocity.field_1350);
            }
        } else if (this.isFallFlying) {
            double k;
            class_243 e = this.velocity;
            if (e.field_1351 > -0.5) {
                this.fallDistance = 1.0f;
            }
            class_243 vec3d3 = this.getRotationVector();
            float f = this.pitch * ((float)Math.PI / 180);
            double g = Math.sqrt(vec3d3.field_1352 * vec3d3.field_1352 + vec3d3.field_1350 * vec3d3.field_1350);
            double horizontalSpeed = this.velocity.method_37267();
            double i = vec3d3.method_1033();
            float j = class_3532.method_15362((double)f);
            j = (float)((double)j * ((double)j * Math.min(1.0, i / 0.4)));
            e = this.velocity.method_1031(0.0, d * (-1.0 + (double)j * 0.75), 0.0);
            if (e.field_1351 < 0.0 && g > 0.0) {
                k = e.field_1351 * -0.1 * (double)j;
                e = e.method_1031(vec3d3.field_1352 * k / g, k, vec3d3.field_1350 * k / g);
            }
            if (f < 0.0f && g > 0.0) {
                k = horizontalSpeed * (double)(-class_3532.method_15374((double)f)) * 0.04;
                e = e.method_1031(-vec3d3.field_1352 * k / g, k * 3.2, -vec3d3.field_1350 * k / g);
            }
            if (g > 0.0) {
                e = e.method_1031((vec3d3.field_1352 / g * horizontalSpeed - e.field_1352) * 0.1, 0.0, (vec3d3.field_1350 / g * horizontalSpeed - e.field_1350) * 0.1);
            }
            this.velocity = e.method_18805(0.99, 0.98, 0.99);
            this.move(this.velocity);
        } else {
            class_2338 blockPos = this.getVelocityAffectingPos();
            float p = this.player.method_73183().method_8320(blockPos).method_26204().method_9499();
            float f = this.onGround ? p * 0.91f : 0.91f;
            class_243 vec3d6 = this.applyMovementInput(movementInput, p);
            double q = vec3d6.field_1351;
            if (this.hasStatusEffect((class_6880<class_1291>)class_1294.field_5902)) {
                class_1293 levitation = this.getStatusEffect((class_6880<class_1291>)class_1294.field_5902);
                if (levitation != null) {
                    q += (0.05 * (double)(levitation.method_5578() + 1) - vec3d6.field_1351) * 0.2;
                }
            } else if (this.player.method_73183().method_8608() && !this.player.method_73183().method_22340(blockPos)) {
                q = this.pos.field_1351 > (double)this.player.method_73183().method_31607() ? -0.1 : 0.0;
            } else if (!this.player.method_5740()) {
                q -= d;
            }
            this.velocity = this.player.method_35053() ? new class_243(vec3d6.field_1352, q, vec3d6.field_1350) : new class_243(vec3d6.field_1352 * (double)f, q * (double)0.98f, vec3d6.field_1350 * (double)f);
        }
        if (this.player.method_31549().field_7479 && !this.player.method_5765()) {
            this.velocity = new class_243(this.velocity.field_1352, beforeTravelVelocityY * 0.6, this.velocity.field_1350);
            this.onLanding();
        }
    }

    private class_243 applyMovementInput(class_243 movementInput, float slipperiness) {
        this.updateVelocity(this.getMovementSpeed(slipperiness), movementInput);
        this.velocity = this.applyClimbingSpeed(this.velocity);
        this.move(this.velocity);
        class_243 result = this.velocity;
        class_2338 posBlock = this.posToBlockPos(this.pos);
        class_2680 state = this.getState(posBlock);
        if ((this.horizontalCollision || this.isJumping) && (this.isClimbing() || state != null && state.method_27852(class_2246.field_27879) && class_5635.method_32355((class_1297)this.player))) {
            result = new class_243(result.field_1352, 0.2, result.field_1350);
        }
        return result;
    }

    private void updateVelocity(float speed, class_243 movementInput) {
        class_243 vec = class_1297.method_18795((class_243)movementInput, (float)speed, (float)this.yaw);
        this.velocity = this.velocity.method_1019(vec);
    }

    private float getMovementSpeed(float slipperiness) {
        return this.onGround ? this.getMovementSpeed() * (0.21600002f / (slipperiness * slipperiness * slipperiness)) : this.getAirStrafingSpeed();
    }

    private float getAirStrafingSpeed() {
        float speed = 0.02f;
        if (this.input.playerInput.comp_3165()) {
            return speed + 0.006f;
        }
        return speed;
    }

    private float getMovementSpeed() {
        return 0.1f;
    }

    private void move(class_243 movement) {
        class_243 modifiedMovement = movement;
        class_243 adjustedMovement = this.adjustMovementForCollisions(modifiedMovement = this.adjustMovementForSneaking(modifiedMovement));
        if (adjustedMovement.method_1027() > 1.0E-7) {
            this.pos = this.pos.method_1019(adjustedMovement);
            this.boundingBox = this.player.field_18065.method_30757(this.pos);
        }
        boolean xCollision = !class_3532.method_20390((double)movement.field_1352, (double)adjustedMovement.field_1352);
        boolean zCollision = !class_3532.method_20390((double)movement.field_1350, (double)adjustedMovement.field_1350);
        this.horizontalCollision = xCollision || zCollision;
        this.verticalCollision = movement.field_1351 != adjustedMovement.field_1351;
        boolean bl = this.onGround = this.verticalCollision && movement.field_1351 < 0.0;
        if (!this.isTouchingWater()) {
            this.checkWaterState();
        }
        if (this.onGround) {
            this.onLanding();
        } else if (movement.field_1351 < 0.0) {
            this.fallDistance -= (float)movement.field_1351;
        }
        class_243 currentVel = this.velocity;
        if (this.horizontalCollision || this.verticalCollision) {
            this.velocity = new class_243(xCollision ? 0.0 : currentVel.field_1352, this.onGround ? 0.0 : currentVel.field_1351, zCollision ? 0.0 : currentVel.field_1350);
        }
    }

    private class_243 adjustMovementForCollisions(class_243 movement) {
        boolean stepPossible;
        class_238 box = new class_238(-0.3, 0.0, -0.3, 0.3, 1.8, 0.3).method_997(this.pos);
        List collisionShapes = Collections.emptyList();
        class_243 adjusted = movement.method_1027() == 0.0 ? movement : class_1297.method_20736((class_1297)this.player, (class_243)movement, (class_238)box, (class_1937)this.player.method_73183(), collisionShapes);
        boolean xCollide = movement.field_1352 != adjusted.field_1352;
        boolean yCollide = movement.field_1351 != adjusted.field_1351;
        boolean zCollide = movement.field_1350 != adjusted.field_1350;
        boolean bl = stepPossible = this.onGround || yCollide && movement.field_1351 < 0.0;
        if (this.player.method_49476() > 0.0f && stepPossible && (xCollide || zCollide)) {
            class_243 stepAdjust = class_1297.method_20736((class_1297)this.player, (class_243)new class_243(movement.field_1352, (double)this.player.method_49476(), movement.field_1350), (class_238)box, (class_1937)this.player.method_73183(), collisionShapes);
            class_243 stepOffset = class_1297.method_20736((class_1297)this.player, (class_243)new class_243(0.0, (double)this.player.method_49476(), 0.0), (class_238)box.method_1012(movement.field_1352, 0.0, movement.field_1350), (class_1937)this.player.method_73183(), collisionShapes);
            class_243 combined = class_1297.method_20736((class_1297)this.player, (class_243)new class_243(movement.field_1352, 0.0, movement.field_1350), (class_238)box.method_997(stepOffset), (class_1937)this.player.method_73183(), collisionShapes).method_1019(stepOffset);
            if (stepOffset.field_1351 < (double)this.player.method_49476() && combined.method_37268() > stepAdjust.method_37268()) {
                stepAdjust = combined;
            }
            if (stepAdjust.method_37268() > adjusted.method_37268()) {
                return stepAdjust.method_1019(class_1297.method_20736((class_1297)this.player, (class_243)new class_243(0.0, -stepAdjust.field_1351 + movement.field_1351, 0.0), (class_238)box.method_997(stepAdjust), (class_1937)this.player.method_73183(), collisionShapes));
            }
        }
        return adjusted;
    }

    private void onLanding() {
        this.fallDistance = 0.0f;
    }

    public void jump() {
        this.velocity = this.velocity.method_1031(0.0, (double)this.getJumpVelocity() - this.velocity.field_1351, 0.0);
        if (this.isSprinting()) {
            float rad = (float)Math.toRadians(this.yaw);
            this.velocity = this.velocity.method_1031((double)(-class_3532.method_15374((double)rad)) * 0.2, 0.0, (double)class_3532.method_15362((double)rad) * 0.2);
        }
    }

    private class_243 applyClimbingSpeed(class_243 motion) {
        if (!this.isClimbing()) {
            return motion;
        }
        this.onLanding();
        double clampedX = class_3532.method_15350((double)motion.field_1352, (double)-0.15f, (double)0.15f);
        double clampedZ = class_3532.method_15350((double)motion.field_1350, (double)-0.15f, (double)0.15f);
        double clampedY = Math.max(motion.field_1351, (double)-0.15f);
        if (clampedY < 0.0 && !this.getState(this.posToBlockPos(this.pos)).method_27852(class_2246.field_16492) && this.player.method_21754()) {
            clampedY = 0.0;
        }
        return new class_243(clampedX, clampedY, clampedZ);
    }

    public boolean isClimbing() {
        class_2338 posBlock = this.posToBlockPos(this.pos);
        class_2680 state = this.getState(posBlock);
        if (state.method_26164(class_3481.field_22414)) {
            return true;
        }
        return state.method_26204() instanceof class_2533 && this.canEnterTrapdoor(posBlock, state);
    }

    private boolean canEnterTrapdoor(class_2338 pos, class_2680 state) {
        if (!((Boolean)state.method_11654((class_2769)class_2533.field_11631)).booleanValue()) {
            return false;
        }
        class_2680 below = this.player.method_73183().method_8320(pos.method_10074());
        return below.method_27852(class_2246.field_9983) && ((class_2350)below.method_11654((class_2769)class_2399.field_11253)).equals((Object)state.method_11654((class_2769)class_2533.field_11177));
    }

    private class_243 adjustMovementForSneaking(class_243 movement) {
        if (movement.field_1351 <= 0.0 && this.method_30263()) {
            double dx;
            double dz = movement.field_1350;
            double step = 0.05;
            for (dx = movement.field_1352; dx != 0.0 && this.player.method_73183().method_8587((class_1297)this.player, this.boundingBox.method_989(dx, -0.5, 0.0)); dx += dx > 0.0 ? -step : step) {
                if (!(dx < step) || !(dx >= -step)) continue;
                dx = 0.0;
                break;
            }
            while (dz != 0.0 && this.player.method_73183().method_8587((class_1297)this.player, this.boundingBox.method_989(0.0, -0.5, dz))) {
                if (dz < step && dz >= -step) {
                    dz = 0.0;
                    break;
                }
                dz += dz > 0.0 ? -step : step;
            }
            while (dx != 0.0 && dz != 0.0 && this.player.method_73183().method_8587((class_1297)this.player, this.boundingBox.method_989(dx, -0.5, dz))) {
                double d = dx < step && dx >= -step ? 0.0 : (dx = dx > 0.0 ? dx - step : dx + step);
                if (dz < step && dz >= -step) {
                    dz = 0.0;
                    break;
                }
                dz += dz > 0.0 ? -step : step;
            }
            if (movement.field_1352 != dx || movement.field_1350 != dz) {
                this.clipLedged = true;
            }
            if (this.shouldClipAtLedge()) {
                movement = new class_243(dx, movement.field_1351, dz);
            }
        }
        return movement;
    }

    protected boolean shouldClipAtLedge() {
        return this.input.playerInput.comp_3164() || this.input.forceSafeWalk;
    }

    private boolean method_30263() {
        return this.onGround || (double)this.fallDistance < 0.5 && !this.player.method_73183().method_8587((class_1297)this.player, this.boundingBox.method_989(0.0, (double)this.fallDistance - 0.5, 0.0));
    }

    private boolean isSprinting() {
        return this.sprinting;
    }

    private float getJumpVelocity() {
        return 0.42f * this.getJumpVelocityMultiplier() + this.getJumpBoostVelocityModifier();
    }

    private float getJumpBoostVelocityModifier() {
        if (this.hasStatusEffect((class_6880<class_1291>)class_1294.field_5913)) {
            class_1293 boost = this.getStatusEffect((class_6880<class_1291>)class_1294.field_5913);
            return 0.1f * (float)(boost.method_5578() + 1);
        }
        return 0.0f;
    }

    private float getJumpVelocityMultiplier() {
        float multiplier1 = 0.0f;
        class_2248 block = this.getState(this.posToBlockPos(this.pos)).method_26204();
        if (block != null) {
            multiplier1 = block.method_23350();
        }
        float multiplier2 = 0.0f;
        class_2248 block2 = this.getState(this.getVelocityAffectingPos()).method_26204();
        if (block2 != null) {
            multiplier2 = block2.method_23350();
        }
        return multiplier1 == 1.0f ? multiplier2 : multiplier1;
    }

    private boolean doesNotCollide(double offsetX, double offsetY, double offsetZ) {
        return this.doesNotCollide(this.boundingBox.method_989(offsetX, offsetY, offsetZ));
    }

    private boolean doesNotCollide(class_238 box) {
        return this.player.method_73183().method_8587((class_1297)this.player, box) && !this.player.method_73183().method_22345(box);
    }

    private void swimUpward(class_6862<class_3611> fluidTag) {
        this.velocity = this.velocity.method_1031(0.0, (double)0.04f, 0.0);
    }

    private class_2338 getVelocityAffectingPos() {
        return class_2338.method_49637((double)this.pos.field_1352, (double)(this.boundingBox.field_1322 - 0.5000001), (double)this.pos.field_1350);
    }

    private double getSwimHeight() {
        return (double)this.player.method_5751() < 0.4 ? 0.0 : 0.4;
    }

    private boolean isTouchingWater() {
        return this.touchingWater;
    }

    public boolean isInLava() {
        return this.fluidHeight.getDouble((Object)class_3486.field_15518) > 0.0;
    }

    private void checkWaterState() {
        class_1690 boat;
        if (this.player.method_5854() instanceof class_1690 && !(boat = (class_1690)this.player.method_5854()).method_5869()) {
            this.touchingWater = false;
            return;
        }
        if (this.updateMovementInFluid((class_6862<class_3611>)class_3486.field_15517, 0.014)) {
            this.onLanding();
            this.touchingWater = true;
        } else {
            this.touchingWater = false;
        }
    }

    private void updateSwimming() {
        this.isSwimming = this.isSwimming ? this.isSprinting() && this.isTouchingWater() && !this.player.method_5765() : this.isSprinting() && this.isSubmergedInWater() && !this.player.method_5765() && this.player.method_73183().method_8316(this.posToBlockPos(this.pos)).method_15767(class_3486.field_15517);
    }

    private void updateSubmergedInWaterState() {
        class_1690 boat;
        this.submergedInWater = this.submergedFluidTag.contains(class_3486.field_15517);
        this.submergedFluidTag.clear();
        double eyeLevel = this.getEyeY() - 0.1111111119389534;
        class_1297 vehicle = this.player.method_5854();
        if (vehicle instanceof class_1690 && !(boat = (class_1690)vehicle).method_5869() && boat.method_5829().field_1325 >= eyeLevel && boat.method_5829().field_1322 <= eyeLevel) {
            return;
        }
        class_2338 posEye = class_2338.method_49637((double)this.pos.field_1352, (double)eyeLevel, (double)this.pos.field_1350);
        class_3610 fluidState = this.player.method_73183().method_8316(posEye);
        double height = (float)posEye.method_10264() + fluidState.method_15763((class_1922)this.player.method_73183(), posEye);
        if (height > eyeLevel) {
            this.submergedFluidTag.addAll(fluidState.method_40181().toList());
        }
    }

    private double getEyeY() {
        return this.pos.field_1351 + (double)this.player.method_5751();
    }

    public boolean isSubmergedInWater() {
        return this.submergedInWater && this.isTouchingWater();
    }

    private double getFluidHeight(class_6862<class_3611> tag) {
        return this.fluidHeight.getDouble(tag);
    }

    private boolean updateMovementInFluid(class_6862<class_3611> tag, double speed) {
        if (this.isRegionUnloaded()) {
            return false;
        }
        class_238 box = this.boundingBox.method_1011(0.001);
        int i = class_3532.method_15357((double)box.field_1323);
        int j = class_3532.method_15384((double)box.field_1320);
        int k = class_3532.method_15357((double)box.field_1322);
        int l = class_3532.method_15384((double)box.field_1325);
        int m = class_3532.method_15357((double)box.field_1321);
        int n = class_3532.method_15384((double)box.field_1324);
        double d = 0.0;
        boolean pushedByFluids = true;
        boolean foundFluid = false;
        class_243 fluidVelocity = class_243.field_1353;
        int count = 0;
        class_2338.class_2339 mutable = new class_2338.class_2339();
        for (int p = i; p < j; ++p) {
            for (int q = k; q < l; ++q) {
                for (int r = m; r < n; ++r) {
                    double e;
                    mutable.method_10103(p, q, r);
                    class_3610 fluidState = this.player.method_73183().method_8316((class_2338)mutable);
                    if (!fluidState.method_15767(tag) || !((e = (double)((float)q + fluidState.method_15763((class_1922)this.player.method_73183(), (class_2338)mutable))) >= box.field_1322)) continue;
                    foundFluid = true;
                    d = Math.max(e - box.field_1322, d);
                    if (!pushedByFluids) continue;
                    class_243 vel = fluidState.method_15758((class_1922)this.player.method_73183(), (class_2338)mutable);
                    if (d < 0.4) {
                        vel = vel.method_1021(d);
                    }
                    fluidVelocity = fluidVelocity.method_1019(vel);
                    ++count;
                }
            }
        }
        if (fluidVelocity.method_1033() > 0.0) {
            if (count > 0) {
                fluidVelocity = fluidVelocity.method_1021(1.0 / (double)count);
            }
            fluidVelocity = fluidVelocity.method_1021(speed);
            if (Math.abs(this.velocity.field_1352) < 0.003 && Math.abs(this.velocity.field_1350) < 0.003 && fluidVelocity.method_1033() < 0.0045) {
                fluidVelocity = fluidVelocity.method_1029().method_1021(0.0045);
            }
            this.velocity = this.velocity.method_1019(fluidVelocity);
        }
        this.fluidHeight.put(tag, d);
        return foundFluid;
    }

    private boolean isRegionUnloaded() {
        class_238 box = this.boundingBox.method_1014(1.0);
        int i = class_3532.method_15357((double)box.field_1323);
        int j = class_3532.method_15384((double)box.field_1320);
        int k = class_3532.method_15357((double)box.field_1321);
        int l = class_3532.method_15384((double)box.field_1324);
        return !this.player.method_73183().method_33597(i, k, j, l);
    }

    private class_243 getRotationVector() {
        return this.getRotationVector(this.pitch, this.yaw);
    }

    private class_243 getRotationVector(float pitch, float yaw) {
        float f = (float)((double)pitch * Math.PI / 180.0);
        float g = (float)((double)(-yaw) * Math.PI / 180.0);
        float h = class_3532.method_15362((double)g);
        float i = class_3532.method_15374((double)g);
        float j = class_3532.method_15362((double)f);
        float k = class_3532.method_15374((double)f);
        return new class_243((double)(i * j), (double)(-k), (double)(h * j));
    }

    public boolean hasStatusEffect(class_6880<class_1291> effect) {
        class_1293 instance = this.player.method_6112(effect);
        return instance != null && instance.method_5584() >= this.simulatedTicks;
    }

    private class_1293 getStatusEffect(class_6880<class_1291> effect) {
        class_1293 instance = this.player.method_6112(effect);
        if (instance == null || instance.method_5584() < this.simulatedTicks) {
            return null;
        }
        return instance;
    }

    public double getAttributeValue(class_6880<class_1320> attribute) {
        return this.player.method_6127().method_26852(attribute);
    }

    public SimulatedPlayer clone() {
        return new SimulatedPlayer(this.player, this.input, this.pos, this.velocity, this.boundingBox, this.yaw, this.pitch, this.sprinting, this.fallDistance, this.jumpingCooldown, this.isJumping, this.isFallFlying, this.onGround, this.horizontalCollision, this.verticalCollision, this.touchingWater, this.isSwimming, this.submergedInWater, (Object2DoubleMap<class_6862<class_3611>>)new Object2DoubleArrayMap(this.fluidHeight), new HashSet<class_6862<class_3611>>(this.submergedFluidTag));
    }

    public class_2338 posToBlockPos(class_243 pos) {
        return new class_2338(class_3532.method_15357((double)pos.field_1352), class_3532.method_15357((double)pos.field_1351), class_3532.method_15357((double)pos.field_1350));
    }

    public class_2680 getState(class_2338 pos) {
        return this.player.method_73183().method_8320(pos);
    }

    public static class SimulatedPlayerInput
    extends class_744 {
        public boolean forceSafeWalk = false;
        public float movementForward;
        public float movementSideways;
        public class_10185 playerInput;
        public static final double MAX_WALKING_SPEED = 0.121;

        public SimulatedPlayerInput(class_10185 input) {
            this.playerInput = input;
        }

        public void update() {
            this.movementForward = this.playerInput.comp_3159() != this.playerInput.comp_3160() ? (this.playerInput.comp_3159() ? 1.0f : -1.0f) : 0.0f;
            if (this.playerInput.comp_3161() == this.playerInput.comp_3162()) {
                this.movementSideways = 0.0f;
            } else {
                float f = this.movementSideways = this.playerInput.comp_3161() ? 1.0f : -1.0f;
            }
            if (this.playerInput.comp_3164()) {
                this.movementSideways *= 0.3f;
                this.movementForward *= 0.3f;
            }
        }

        public String toString() {
            return "SimulatedPlayerInput(forwards={" + this.playerInput.comp_3159() + "}, backwards={" + this.playerInput.comp_3160() + "}, left={" + this.playerInput.comp_3161() + "}, right={" + this.playerInput.comp_3162() + "}, jumping={" + this.playerInput.comp_3163() + "}, sprinting=" + this.playerInput.comp_3165() + ", slowDown=" + this.playerInput.comp_3164() + ")";
        }

        public static SimulatedPlayerInput fromClientPlayer(class_10185 input) {
            return new SimulatedPlayerInput(input);
        }

        public static SimulatedPlayerInput guessInput(class_1657 entity) {
            class_243 velocity = entity.method_73189().method_1020(new class_243(entity.field_6014, entity.field_6036, entity.field_5969));
            double horizontalVelocity = velocity.method_37268();
            class_10185 input = new class_10185(false, false, false, false, !entity.method_24828(), entity.method_5715(), horizontalVelocity >= 0.014641);
            if (horizontalVelocity > 0.0025000000000000005) {
                double velocityAngle = MovingUtil.getDegreesRelativeToView(velocity, entity.method_36454());
                double wrappedAngle = class_3532.method_15338((double)velocityAngle);
                input = MovingUtil.getDirectionalInputForDegrees(input, wrappedAngle);
            }
            return new SimulatedPlayerInput(input);
        }
    }
}

