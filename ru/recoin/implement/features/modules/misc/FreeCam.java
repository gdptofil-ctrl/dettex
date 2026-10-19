/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_243
 *  net.minecraft.class_2596
 *  net.minecraft.class_2678
 *  net.minecraft.class_2724
 *  net.minecraft.class_2828
 *  net.minecraft.class_4587
 *  net.minecraft.class_5498
 */
package ru.recoin.implement.features.modules.misc;

import java.util.Locale;
import java.util.Objects;
import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_2678;
import net.minecraft.class_2724;
import net.minecraft.class_2828;
import net.minecraft.class_4587;
import net.minecraft.class_5498;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.entity.MovingUtil;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.task.TaskPriority;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.events.player.InputEvent;
import ru.recoin.implement.events.player.MoveEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.events.render.CameraPositionEvent;
import ru.recoin.implement.events.render.DrawEvent;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationConfig;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.LinearSmoothMode;

public class FreeCam
extends Module {
    private final ValueSetting speedSetting = new ValueSetting("Speed", "Select debug camera speed").setValue(2.0f).range(0.5f, 5.0f).step(0.05f);
    private final BooleanSetting freezeSetting = new BooleanSetting("Freeze", "You freeze in place").setValue(false);
    private final BooleanSetting coordinatesSetting = new BooleanSetting("Coordinates", "Show camera position coordinates").setValue(true);
    public class_243 pos;
    public class_243 prevPos;
    private class_243 frozenPlayerPos;
    private Angle lockedRotation;

    public static FreeCam getInstance() {
        return Instance.get(FreeCam.class);
    }

    public FreeCam() {
        super("Free Cam", "Free Cam", ModuleCategory.MISC);
        this.setup(this.speedSetting, this.freezeSetting, this.coordinatesSetting);
    }

    @Override
    public void activate() {
        if (FreeCam.mc.field_1724 == null || FreeCam.mc.field_1687 == null) {
            return;
        }
        this.prevPos = this.pos = FreeCam.mc.field_1724.method_33571();
        this.frozenPlayerPos = FreeCam.mc.field_1724.method_73189();
        this.lockedRotation = new Angle(FreeCam.mc.field_1724.method_36454(), FreeCam.mc.field_1724.method_36455());
        super.activate();
    }

    @Override
    public void deactivate() {
        Angle serverRotation = RotationController.INSTANCE.getRotation();
        RotationController.INSTANCE.release(this);
        if (FreeCam.mc.field_1724 != null && serverRotation != null) {
            FreeCam.mc.field_1724.method_36456(serverRotation.getYaw());
            FreeCam.mc.field_1724.method_36457(serverRotation.getPitch());
        }
        this.pos = null;
        this.prevPos = null;
        this.frozenPlayerPos = null;
        this.lockedRotation = null;
        super.deactivate();
    }

    @EventHandler
    public void onPacket(PacketEvent e) {
        class_2596<?> class_25962 = e.getPacket();
        Objects.requireNonNull(class_25962);
        class_2596<?> class_25963 = class_25962;
        if (class_25963 instanceof class_2828 move) {
            if (this.freezeSetting.isValue()) {
                e.cancel();
            }
        } else if (class_25963 instanceof class_2724 respawn) {
            this.setState(false);
        } else if (class_25963 instanceof class_2678 join) {
            this.setState(false);
        }
    }

    @EventHandler
    public void onMove(MoveEvent e) {
        if (!this.freezeSetting.isValue()) {
            return;
        }
        e.setMovement(class_243.field_1353);
        if (FreeCam.mc.field_1724 != null && this.frozenPlayerPos != null) {
            FreeCam.mc.field_1724.method_18799(class_243.field_1353);
            FreeCam.mc.field_1724.method_5814(this.frozenPlayerPos.field_1352, this.frozenPlayerPos.field_1351, this.frozenPlayerPos.field_1350);
        }
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (FreeCam.mc.field_1724 == null || this.lockedRotation == null) {
            return;
        }
        RotationController.INSTANCE.rotateTo(this.lockedRotation, 1, new RotationConfig(new LinearSmoothMode(), false, true), TaskPriority.LOW_PRIORITY, this);
        if (this.freezeSetting.isValue() && this.frozenPlayerPos != null) {
            FreeCam.mc.field_1724.method_18799(class_243.field_1353);
            FreeCam.mc.field_1724.method_5814(this.frozenPlayerPos.field_1352, this.frozenPlayerPos.field_1351, this.frozenPlayerPos.field_1350);
        }
    }

    @EventHandler
    public void onInput(InputEvent e) {
        float speed = this.speedSetting.getValue();
        double[] motion = MovingUtil.calculateDirection(e.forward(), e.sideways(), speed);
        this.prevPos = this.pos;
        this.pos = this.pos.method_1031(motion[0], e.getInput().comp_3163() ? (double)speed : (e.getInput().comp_3164() ? (double)(-speed) : 0.0), motion[1]);
        e.inputNone();
    }

    @EventHandler
    public void onCameraPosition(CameraPositionEvent e) {
        if (this.pos == null || this.prevPos == null) {
            return;
        }
        e.setPos(MathUtil.interpolate(this.prevPos, this.pos));
        FreeCam.mc.field_1690.method_31043(class_5498.field_26664);
    }

    @EventHandler
    public void onDraw(DrawEvent event) {
        if (!this.coordinatesSetting.isValue() || FreeCam.mc.field_1724 == null || this.pos == null) {
            return;
        }
        class_243 delta = this.pos.method_1020(FreeCam.mc.field_1724.method_73189());
        String text = String.format(Locale.ROOT, "X:%+.1f, Y:%+.1f, Z:%+.1f", delta.field_1352, delta.field_1351, delta.field_1350);
        class_4587 matrix = MatrixStackCompat.from(event.getDrawContext().method_51448());
        Fonts.getSize(14, Fonts.Type.INTER_BOLD).drawCenteredString(matrix, text, (float)window.getScaledWidth() / 2.0f, (float)window.getScaledHeight() / 2.0f + 15.0f, ColorUtil.getText());
    }
}

