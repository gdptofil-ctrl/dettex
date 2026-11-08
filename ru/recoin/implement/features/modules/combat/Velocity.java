/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1282
 *  net.minecraft.class_1657
 *  net.minecraft.class_1937
 *  net.minecraft.class_2338
 *  net.minecraft.class_2350
 *  net.minecraft.class_2374
 *  net.minecraft.class_243
 *  net.minecraft.class_2596
 *  net.minecraft.class_2708
 *  net.minecraft.class_2743
 *  net.minecraft.class_2828$class_2830
 *  net.minecraft.class_2846
 *  net.minecraft.class_2846$class_2847
 *  net.minecraft.class_3532
 *  net.minecraft.class_8143
 */
package ru.recoin.implement.features.modules.combat;

import com.adl.nativeprotect.Native;
import net.minecraft.class_1282;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2374;
import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_2708;
import net.minecraft.class_2743;
import net.minecraft.class_2828;
import net.minecraft.class_2846;
import net.minecraft.class_3532;
import net.minecraft.class_8143;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.other.Instance;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.events.player.InputEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.features.modules.combat.Aura;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;

public class Velocity
extends Module {
    private final SelectSetting mode = new SelectSetting("Mode", "Velocity mode").value("Jump Reset", "Matrix", "Vanilla", "Grim").selected("Jump Reset");
    private final BooleanSetting correctMovement = new BooleanSetting("Correct Movement", "Automatically correct movement direction during knockback").setValue(true).visible(() -> this.mode.isSelected("Jump Reset"));
    private boolean velocityFlag;
    private int correctionCooldown;
    private class_243 pendingVelocity;
    private class_243 knockbackVelocity = class_243.field_1353;
    private int lastPlayerDamageAge;

    public static Velocity getInstance() {
        return Instance.get(Velocity.class);
    }

    public Velocity() {
        super("Velocity", "Velocity", ModuleCategory.COMBAT);
        this.setup(this.mode, this.correctMovement);
    }

    @Override
    public void deactivate() {
        this.clearJumpReset();
        this.velocityFlag = false;
        this.correctionCooldown = 0;
        this.pendingVelocity = null;
        super.deactivate();
    }

    @Override
    public void activate() {
        this.velocityFlag = false;
        this.correctionCooldown = 0;
        this.pendingVelocity = null;
        this.clearJumpReset();
        super.activate();
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        class_2743 packet;
        class_8143 damage;
        Object object;
        if (event.getType() != PacketEvent.Type.RECEIVE || Velocity.mc.field_1724 == null) {
            return;
        }
        if (!this.mode.isSelected("Jump Reset") && (Velocity.mc.field_1724.method_5799() || Velocity.mc.field_1724.method_5869() || Velocity.mc.field_1724.method_5771())) {
            return;
        }
        if (this.mode.isSelected("Jump Reset") && (object = event.getPacket()) instanceof class_8143 && (damage = (class_8143)object).comp_1267() == Velocity.mc.field_1724.method_5628() && Velocity.mc.field_1687 != null) {
            class_1282 source = damage.method_49071((class_1937)Velocity.mc.field_1687);
            boolean causedByPlayer = source.method_5529() instanceof class_1657;
            int n = this.lastPlayerDamageAge = causedByPlayer ? Velocity.mc.field_1724.field_6012 : Integer.MIN_VALUE;
            if (!causedByPlayer) {
                this.knockbackVelocity = class_243.field_1353;
            }
        }
        if (event.getPacket() instanceof class_2708 && this.mode.isSelected("Grim")) {
            this.correctionCooldown = 5;
        }
        if (!((object = event.getPacket()) instanceof class_2743) || (packet = (class_2743)object).method_11818() != Velocity.mc.field_1724.method_5628()) {
            return;
        }
        if (this.correctionCooldown > 0) {
            return;
        }
        switch (this.mode.getSelected()) {
            case "Vanilla": {
                event.cancel();
                break;
            }
            case "Matrix": {
                event.cancel();
                if (!this.velocityFlag) {
                    this.velocityFlag = true;
                    break;
                }
                this.velocityFlag = false;
                class_243 velocity = packet.method_73085();
                this.pendingVelocity = new class_243(-velocity.field_1352 * 0.1, velocity.field_1351, -velocity.field_1350 * 0.1);
                break;
            }
            case "Grim": {
                event.cancel();
                this.velocityFlag = true;
                break;
            }
            case "Jump Reset": {
                class_243 vel = packet.method_73085();
                this.knockbackVelocity = new class_243(vel.field_1352, 0.0, vel.field_1350);
            }
        }
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (Velocity.mc.field_1724 == null || Velocity.mc.field_1687 == null) {
            return;
        }
        if (!this.mode.isSelected("Jump Reset")) {
            this.clearJumpReset();
        }
        if (this.mode.isSelected("Matrix")) {
            this.handleMatrixTick();
            return;
        }
        if (this.mode.isSelected("Grim")) {
            if (this.correctionCooldown > 0) {
                --this.correctionCooldown;
            }
            this.handleGrimTick();
            return;
        }
    }

    @Native
    private void handleMatrixTick() {
        if (this.pendingVelocity != null) {
            Velocity.mc.field_1724.method_18799(this.pendingVelocity);
            this.pendingVelocity = null;
        }
        if (Velocity.mc.field_1724.field_6235 <= 0 || Velocity.mc.field_1724.method_24828()) {
            return;
        }
        double yaw = Math.toRadians(Velocity.mc.field_1724.method_36454());
        double horizontal = Math.hypot(Velocity.mc.field_1724.method_18798().field_1352, Velocity.mc.field_1724.method_18798().field_1350);
        Velocity.mc.field_1724.method_18800(-Math.sin(yaw) * horizontal, Velocity.mc.field_1724.method_18798().field_1351, Math.cos(yaw) * horizontal);
        Velocity.mc.field_1724.method_5728((Velocity.mc.field_1724.field_6012 & 1) != 0);
    }

    @Native
    private void handleGrimTick() {
        if (!this.velocityFlag) {
            return;
        }
        if (this.correctionCooldown <= 0) {
            PlayerIntersectionUtil.grimSuperBypass$$$(0.0, RotationController.INSTANCE.getServerAngle());
            PlayerIntersectionUtil.sendSequencedPacket(sequence -> new class_2846(class_2846.class_2847.field_12973, class_2338.method_49638((class_2374)Velocity.mc.field_1724.method_73189()), class_2350.field_11033, sequence));
        }
        this.velocityFlag = false;
    }

    @EventHandler
    public void onInput(InputEvent event) {
        if (!this.mode.isSelected("Jump Reset") || Velocity.mc.field_1724 == null) {
            return;
        }
        if (Velocity.mc.field_1724.field_6235 <= 0) {
            this.knockbackVelocity = class_243.field_1353;
            return;
        }
        if (this.knockbackVelocity.method_1027() == 0.0 || (long)Velocity.mc.field_1724.field_6012 - (long)this.lastPlayerDamageAge > 10L) {
            return;
        }
        if (this.correctMovement.isValue()) {
            float yaw = Aura.getInstance().isLegitRotationActive() ? Velocity.mc.field_1724.method_36454() : RotationController.INSTANCE.getRotation().getYaw();
            double angle = class_3532.method_15338((double)(Math.toDegrees(Math.atan2(-this.knockbackVelocity.field_1350, -this.knockbackVelocity.field_1352)) - 90.0 - (double)yaw));
            if (!Velocity.mc.field_1690.field_1894.method_1434() || !(Math.abs(angle) >= 140.0)) {
                int forward = angle <= -45.0 || angle >= 45.0 ? (angle > 135.0 || angle < -135.0 ? -1 : 0) : 1;
                int sideways = angle < 45.0 || angle > 135.0 ? (angle > -45.0 || angle < -135.0 ? 0 : 1) : -1;
                event.setDirectional(forward > 0, forward < 0, sideways > 0, sideways < 0);
            }
        }
        event.setJumping(Velocity.mc.field_1724.method_24828());
    }

    private void clearJumpReset() {
        this.knockbackVelocity = class_243.field_1353;
        this.lastPlayerDamageAge = Integer.MIN_VALUE;
    }
}

