/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_1531
 *  net.minecraft.class_1690
 *  net.minecraft.class_1802
 *  net.minecraft.class_2338
 *  net.minecraft.class_2350
 *  net.minecraft.class_243
 *  net.minecraft.class_2596
 *  net.minecraft.class_2708
 *  net.minecraft.class_2846
 *  net.minecraft.class_2846$class_2847
 *  net.minecraft.class_2848
 *  net.minecraft.class_2848$class_2849
 *  net.minecraft.class_2868
 *  net.minecraft.class_3532
 */
package ru.recoin.implement.features.modules.movement;

import com.adl.nativeprotect.Native;
import net.minecraft.class_1268;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1531;
import net.minecraft.class_1690;
import net.minecraft.class_1802;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_2708;
import net.minecraft.class_2846;
import net.minecraft.class_2848;
import net.minecraft.class_2868;
import net.minecraft.class_3532;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.entity.MovingUtil;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.task.TaskPriority;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.events.player.MoveEvent;
import ru.recoin.implement.events.player.PostMotionEvent;
import ru.recoin.implement.events.player.RotationUpdateEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.events.render.WorldLoadEvent;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationConfig;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;

public class Speed
extends Module {
    private final SelectSetting mode = new SelectSetting("Mode", "Select speed mode").value("Vanilla", "Grim Collision", "Grim 72", "Apextime").selected("Vanilla");
    private final ValueSetting speed = new ValueSetting("Speed", "Vanilla movement speed").setValue(0.35f).range(0.1f, 2.0f).step(0.05f).visible(() -> this.mode.isSelected("Vanilla"));
    private final ValueSetting grimSlowRate = new ValueSetting("\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c", "Grim 72 timer after synchronization").setValue(0.4f).range(0.0f, 2.0f).step(0.1f).visible(() -> this.mode.isSelected("Grim 72"));
    private final ValueSetting grimFastRate = new ValueSetting("\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c 2", "Grim 72 timer after relative movement").setValue(3.4f).range(1.0f, 5.0f).step(0.1f).visible(() -> this.mode.isSelected("Grim 72"));
    private final SelectSetting apexMode = new SelectSetting("Apextime Mode", "Apextime movement mode").value("Vanilla", "Motion").selected("Vanilla").visible(() -> this.mode.isSelected("Apextime"));
    private final BooleanSetting apexBuffOnly = new BooleanSetting("Apextime Buff Only", "Work only while under the speed effect").setValue(true).visible(() -> this.mode.isSelected("Apextime"));
    private final SelectSetting apexBuffLevel = new SelectSetting("Apextime Buff Level", "Required speed effect level").value("Any", "Speed 1", "Speed 2", "Speed 3").selected("Any").visible(() -> this.mode.isSelected("Apextime"));
    private final ValueSetting apexSpeed = new ValueSetting("Apextime Speed", "Base horizontal speed").setValue(0.4f).range(0.2f, 1.0f).step(0.01f).visible(() -> this.mode.isSelected("Apextime"));
    private final ValueSetting apexBuffPower = new ValueSetting("Apextime Buff Power", "Speed bonus per effect level").setValue(0.15f).range(0.0f, 0.5f).step(0.01f).visible(() -> this.mode.isSelected("Apextime"));
    private final ValueSetting apexAcceleration = new ValueSetting("Apextime Acceleration", "Speed gain per tick in Motion mode").setValue(0.25f).range(0.05f, 0.6f).step(0.01f).visible(() -> this.mode.isSelected("Apextime"));
    private final BooleanSetting apexJumpBoost = new BooleanSetting("Apextime Jump Boost", "Auto jump while on ground").setValue(true).visible(() -> this.mode.isSelected("Apextime"));
    private transient int grim72Moves;
    private volatile transient float grim72Rate = 1.0f;
    private int restoreSlot = -1;

    public static Speed getInstance() {
        return Instance.get(Speed.class);
    }

    public Speed() {
        super("Speed", "Speed", ModuleCategory.MOVEMENT);
        this.setup(this.mode, this.speed, this.grimSlowRate, this.grimFastRate, this.apexMode, this.apexBuffOnly, this.apexBuffLevel, this.apexSpeed, this.apexBuffPower, this.apexAcceleration, this.apexJumpBoost);
    }

    @EventHandler
    public void onMove(MoveEvent event) {
        if (Speed.mc.field_1724 == null) {
            return;
        }
        if (this.mode.isSelected("Apextime")) {
            this.apextimeMove(event);
            return;
        }
        if (!MovingUtil.hasPlayerMovement()) {
            return;
        }
        if (this.mode.isSelected("Vanilla")) {
            double[] motion = MovingUtil.calculateDirection(this.speed.getValue());
            event.setMovement(new class_243(motion[0], event.getMovement().field_1351, motion[1]));
        }
    }

    private void apextimeMove(MoveEvent event) {
        if (Speed.mc.field_1724 == null || !this.apextimeCanRun() || !MovingUtil.hasPlayerMovement()) {
            return;
        }
        float target = this.apextimeTargetSpeed();
        if (this.apexMode.isSelected("Vanilla")) {
            double[] motion = MovingUtil.calculateDirection(target);
            event.setMovement(new class_243(motion[0], event.getMovement().field_1351, motion[1]));
            return;
        }
        class_243 velocity = Speed.mc.field_1724.method_18798();
        double current = Math.sqrt(velocity.field_1352 * velocity.field_1352 + velocity.field_1350 * velocity.field_1350);
        if (current < (double)target) {
            current = Math.min((double)target, current + (double)this.apexAcceleration.getValue());
        }
        if (this.apexJumpBoost.isValue() && Speed.mc.field_1724.method_24828()) {
            Speed.mc.field_1724.method_6043();
        }
        double[] motion = MovingUtil.calculateDirection(current);
        event.setMovement(new class_243(motion[0], event.getMovement().field_1351, motion[1]));
    }

    private int apextimeBuffLevel() {
        if (Speed.mc.field_1724 == null) {
            return 0;
        }
        class_1293 effect = Speed.mc.field_1724.method_6112(class_1294.field_5904);
        return effect == null ? 0 : effect.method_5578() + 1;
    }

    private int apextimeRequiredBuffLevel() {
        if (this.apexBuffLevel.isSelected("Speed 1")) {
            return 1;
        }
        if (this.apexBuffLevel.isSelected("Speed 2")) {
            return 2;
        }
        if (this.apexBuffLevel.isSelected("Speed 3")) {
            return 3;
        }
        return 1;
    }

    private boolean apextimeCanRun() {
        if (Speed.mc.field_1724 == null) {
            return false;
        }
        if (Speed.mc.field_1724.method_31549().field_7479) {
            return false;
        }
        if (Speed.mc.field_1724.method_5799() || Speed.mc.field_1724.method_5771()) {
            return false;
        }
        if (!this.apexBuffOnly.isValue()) {
            return true;
        }
        return this.apextimeBuffLevel() >= this.apextimeRequiredBuffLevel();
    }

    private float apextimeTargetSpeed() {
        int level = this.apextimeBuffLevel();
        return this.apexSpeed.getValue() * (1.0f + this.apexBuffPower.getValue() * (float)level);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (Speed.mc.field_1724 == null || Speed.mc.field_1687 == null) {
            return;
        }
        if (!this.mode.isSelected("Grim 72")) {
            this.resetGrim72();
        }
        if (this.mode.isSelected("RW")) {
            this.mode.setSelected("Vanilla");
        }
        if (this.mode.isSelected("FunTime Elytra")) {
            this.tickFunTimeElytra();
            return;
        }
        if (!MovingUtil.hasPlayerMovement()) {
            return;
        }
        if (this.mode.isSelected("Grim Collision") || this.mode.isSelected("Grim")) {
            double radius = 0.5;
            double boost = 0.07;
            int collisions = 0;
            for (class_1297 entity : Speed.mc.field_1687.method_18112()) {
                if (entity == Speed.mc.field_1724 || entity instanceof class_1531 || !(entity instanceof class_1309) && !(entity instanceof class_1690) || !Speed.mc.field_1724.method_5829().method_1014(radius).method_994(entity.method_5829())) continue;
                ++collisions;
            }
            if (collisions > 0) {
                double[] motion = MovingUtil.calculateDirection(boost * (double)collisions);
                Speed.mc.field_1724.method_5762(motion[0], 0.0, motion[1]);
            }
        }
    }

    @EventHandler
    public void onRotationUpdate(RotationUpdateEvent event) {
        if (!this.mode.isSelected("FunTime Elytra") || event.getType() != 0 || Speed.mc.field_1724 == null || this.findElytraHotbarSlot() == -1) {
            return;
        }
        RotationController.INSTANCE.rotateTo(AngleUtil.pitch(90.0f), 2, new RotationConfig(true, true), TaskPriority.HIGH_IMPORTANCE_3, this);
    }

    @EventHandler
    public void onPostMotion(PostMotionEvent event) {
        if (this.mode.isSelected("Grim 72")) {
            if (!this.canGrim72Move()) {
                this.resetGrim72();
            } else if (this.grim72Moves % 2 == 0) {
                this.grim72Rate = this.grimSlowRate.getValue();
                Speed.mc.field_1724.field_3944.method_52787((class_2596)new class_2848((class_1297)Speed.mc.field_1724, class_2848.class_2849.field_12982));
                Speed.mc.field_1724.field_3944.method_52787((class_2596)new class_2848((class_1297)Speed.mc.field_1724, class_2848.class_2849.field_12982));
            }
        }
        if (this.restoreSlot == -1 || Speed.mc.field_1724 == null || Speed.mc.field_1724.field_3944 == null) {
            return;
        }
        Speed.mc.field_1724.field_3944.method_52787((class_2596)new class_2868(this.restoreSlot));
        this.restoreSlot = -1;
    }

    @Native
    private void tickFunTimeElytra() {
        if (Speed.mc.field_1724.field_3944 == null || Speed.mc.field_1761 == null) {
            return;
        }
        int elytraSlot = this.findElytraHotbarSlot();
        if (elytraSlot == -1) {
            return;
        }
        if (Math.abs(RotationController.INSTANCE.getServerAngle().getPitch() - 90.0f) > 2.0f) {
            return;
        }
        this.restoreSlot = Speed.mc.field_1724.method_31548().method_67532();
        Speed.mc.field_1724.field_3944.method_52787((class_2596)new class_2868(elytraSlot));
        this.swapWithOffhand();
        PlayerIntersectionUtil.interactItem(class_1268.field_5808);
        Speed.mc.field_1724.method_23669();
        Speed.mc.field_1724.field_3944.method_52787((class_2596)new class_2848((class_1297)Speed.mc.field_1724, class_2848.class_2849.field_12982));
        this.swapWithOffhand();
    }

    private int findElytraHotbarSlot() {
        for (int slot = 0; slot < 9; ++slot) {
            if (!Speed.mc.field_1724.method_31548().method_5438(slot).method_31574(class_1802.field_8833)) continue;
            return slot;
        }
        return -1;
    }

    private void swapWithOffhand() {
        Speed.mc.field_1724.field_3944.method_52787((class_2596)new class_2846(class_2846.class_2847.field_12969, class_2338.field_10980, class_2350.field_11033));
    }

    public boolean isFunTimeElytraMode() {
        return this.isState() && this.mode.isSelected("FunTime Elytra");
    }

    public void afterGrim72RelativeMovement() {
        if (!this.isState() || !this.mode.isSelected("Grim 72")) {
            return;
        }
        if (!this.canGrim72Move()) {
            this.resetGrim72();
            return;
        }
        this.grim72Rate = this.grimFastRate.getValue();
        if (this.grim72Moves > 3) {
            double boost = 0.03;
            if (this.grim72Moves % 2 == 0) {
                Speed.mc.field_1724.method_18799(Speed.mc.field_1724.method_18798().method_1031(0.0, 0.03, 0.0));
                boost = Speed.mc.field_1724.method_24828() ? 0.085 : 0.03;
            }
            double[] direction = this.grim72KeyDirection();
            Speed.mc.field_1724.method_18799(Speed.mc.field_1724.method_18798().method_1031(direction[0] * boost, 0.0, direction[1] * boost));
        }
        ++this.grim72Moves;
    }

    private double[] grim72KeyDirection() {
        boolean right = Speed.mc.field_1690.field_1849.method_1434();
        boolean left = Speed.mc.field_1690.field_1913.method_1434();
        boolean forward = Speed.mc.field_1690.field_1894.method_1434();
        boolean back = Speed.mc.field_1690.field_1881.method_1434();
        if (!(right || left || forward || back)) {
            return new double[]{0.0, 0.0};
        }
        float yaw = Speed.mc.field_1724.method_36454();
        if (back) {
            yaw -= 180.0f;
        }
        if (!(!back && !forward || back && forward)) {
            if (yaw == Speed.mc.field_1724.method_36454()) {
                if (left) {
                    yaw -= 45.0f;
                }
                if (right) {
                    yaw += 45.0f;
                }
            } else {
                if (left) {
                    yaw += 45.0f;
                }
                if (right) {
                    yaw -= 45.0f;
                }
            }
        } else {
            if (left) {
                yaw -= 90.0f;
            }
            if (right) {
                yaw += 90.0f;
            }
        }
        if ((yaw = class_3532.method_15393((float)yaw)) == -1.0f) {
            return new double[]{0.0, 0.0};
        }
        double radians = Math.toRadians(yaw);
        return new double[]{-Math.sin(radians), Math.cos(radians)};
    }

    private boolean canGrim72Move() {
        return Speed.mc.field_1724 != null && Speed.mc.field_1687 != null && Speed.mc.field_1724.field_3944 != null && MovingUtil.hasPlayerMovement() && !Speed.mc.field_1724.method_5765() && !Speed.mc.field_1724.method_31549().field_7479;
    }

    public boolean isGrim72Mode() {
        return this.isState() && this.mode.isSelected("Grim 72");
    }

    public float getGrim72TimerRate() {
        return this.isGrim72Mode() && Speed.mc.field_1724 != null && Speed.mc.field_1687 != null ? this.grim72Rate : 1.0f;
    }

    private void resetGrim72() {
        this.grim72Moves = 0;
        this.grim72Rate = 1.0f;
    }

    @EventHandler
    public void onGrim72Packet(PacketEvent event) {
        if (event.isReceive() && event.getPacket() instanceof class_2708 && this.isGrim72Mode()) {
            mc.execute(() -> {
                if (!this.isGrim72Mode() || !this.canGrim72Move()) {
                    return;
                }
                if (this.grim72Moves % 2 == 1) {
                    ++this.grim72Moves;
                }
                this.grim72Rate = 1.0f;
            });
        }
    }

    @EventHandler
    public void onGrim72WorldLoad(WorldLoadEvent event) {
        this.resetGrim72();
    }

    @Override
    public void activate() {
        this.resetGrim72();
        super.activate();
    }

    @Override
    public void deactivate() {
        this.resetGrim72();
        if (this.restoreSlot != -1 && Speed.mc.field_1724 != null && Speed.mc.field_1724.field_3944 != null) {
            Speed.mc.field_1724.field_3944.method_52787((class_2596)new class_2868(this.restoreSlot));
        }
        this.restoreSlot = -1;
        RotationController.INSTANCE.release(this);
        super.deactivate();
    }
}

