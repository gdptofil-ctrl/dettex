/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_3532
 *  net.minecraft.class_5498
 */
package ru.recoin.implement.features.modules.render;

import net.minecraft.class_3532;
import net.minecraft.class_5498;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BindSetting;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.other.Instance;
import ru.recoin.implement.events.keyboard.HotBarScrollEvent;
import ru.recoin.implement.events.keyboard.KeyEvent;
import ru.recoin.implement.events.keyboard.MouseRotationEvent;
import ru.recoin.implement.events.render.CameraEvent;
import ru.recoin.implement.events.render.FovEvent;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;

public class CameraTweaks
extends Module {
    private float fov = 110.0f;
    private float smoothFov = 30.0f;
    private float lastChangedFov = 30.0f;
    private class_5498 perspective;
    private Angle angle;
    private final BooleanSetting clipSetting = new BooleanSetting("Camera Clip", "The camera passes through the blocks").setValue(true);
    private final BindSetting zoomSetting = new BindSetting("Zoom", "Key to zoom in camera");
    private final BindSetting freeLookSetting = new BindSetting("Free Look", "Key to free look");

    public CameraTweaks() {
        super("CameraTweaks", "Camera Tweaks", ModuleCategory.RENDER);
        this.setup(this.clipSetting, this.zoomSetting, this.freeLookSetting);
    }

    public static CameraTweaks getInstance() {
        return Instance.get(CameraTweaks.class);
    }

    @EventHandler
    public void onKey(KeyEvent e) {
        if (e.isKeyDown(this.zoomSetting.getKey())) {
            this.fov = Math.min(this.lastChangedFov, (float)((Integer)CameraTweaks.mc.field_1690.method_41808().method_41753() - 20));
        }
        if (e.isKeyReleased(this.zoomSetting.getKey(), true)) {
            this.lastChangedFov = this.fov;
            this.fov = ((Integer)CameraTweaks.mc.field_1690.method_41808().method_41753()).intValue();
        }
        if (e.isKeyDown(this.freeLookSetting.getKey())) {
            this.perspective = CameraTweaks.mc.field_1690.method_31044();
        }
    }

    @EventHandler
    public void onHotBarScroll(HotBarScrollEvent e) {
        if (PlayerIntersectionUtil.isKey(this.zoomSetting)) {
            this.fov = (int)class_3532.method_15350((double)((double)this.fov - e.getVertical() * 10.0), (double)10.0, (double)((Integer)CameraTweaks.mc.field_1690.method_41808().method_41753()).intValue());
            e.cancel();
        }
    }

    @EventHandler
    public void onFov(FovEvent e) {
        if (PlayerIntersectionUtil.isKey(this.freeLookSetting)) {
            if (CameraTweaks.mc.field_1690.method_31044().method_31034()) {
                CameraTweaks.mc.field_1690.method_31043(class_5498.field_26665);
            }
        } else if (this.perspective != null) {
            CameraTweaks.mc.field_1690.method_31043(this.perspective);
            this.perspective = null;
        }
        this.smoothFov = MathUtil.interpolateSmooth(1.6, this.smoothFov, this.fov);
        e.setFov((int)class_3532.method_15363((float)(this.smoothFov + 1.0f), (float)10.0f, (float)((Integer)CameraTweaks.mc.field_1690.method_41808().method_41753()).intValue()));
        e.cancel();
    }

    @EventHandler
    public void onMouseRotation(MouseRotationEvent e) {
        if (PlayerIntersectionUtil.isKey(this.freeLookSetting)) {
            if (this.angle == null) {
                this.angle = AngleUtil.cameraAngle();
            }
            this.angle.setYaw(this.angle.getYaw() + e.getCursorDeltaX() * 0.15f);
            this.angle.setPitch(class_3532.method_15363((float)(this.angle.getPitch() + e.getCursorDeltaY() * 0.15f), (float)-90.0f, (float)90.0f));
            e.cancel();
        } else {
            this.angle = AngleUtil.cameraAngle();
        }
    }

    @EventHandler
    public void onCamera(CameraEvent e) {
        e.setCameraClip(this.clipSetting.isValue());
        e.setDistance(4.0f);
        e.setAngle(this.angle);
        e.cancel();
    }
}

