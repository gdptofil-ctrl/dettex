/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1309
 *  net.minecraft.class_3532
 */
package ru.recoin.implement.features.modules.render;

import net.minecraft.class_1309;
import net.minecraft.class_3532;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.api.system.animation.Animation;
import ru.recoin.api.system.animation.Direction;
import ru.recoin.api.system.animation.implement.DecelerateAnimation;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.render.Render3DUtil;
import ru.recoin.implement.events.render.WorldRenderEvent;
import ru.recoin.implement.features.modules.combat.AimAssist;
import ru.recoin.implement.features.modules.combat.Aura;
import ru.recoin.implement.features.modules.render.esp.BloomTargetESP;
import ru.recoin.implement.features.modules.render.esp.CrystalTargetESP;
import ru.recoin.implement.features.modules.render.esp.LightningTargetESP;
import ru.recoin.implement.features.modules.render.esp.SwordTargetESP;

public class TargetESP
extends Module {
    private final SelectSetting mode = new SelectSetting("Mode", "Selects the target esp style").value("Ghosts", "Cube", "Crystals", "Circle", "Black Flash", "Bloom", "Swords", "\u041c\u043e\u043b\u043d\u0438\u0438", "Helix", "Atom", "Pulse").selected("Ghosts");
    private final SelectSetting cubeType = new SelectSetting("Cube Type", "Selects the cube style").value("\u0412\u0438\u0434 1", "\u0412\u0438\u0434 2").selected("\u0412\u0438\u0434 1").visible(() -> this.mode.isSelected("Cube"));
    private final SelectSetting ghostType = new SelectSetting("Ghost Type", "Selects the ghost style").value("\u0412\u0438\u0434 1", "\u0412\u0438\u0434 2", "\u0412\u0438\u0434 3").selected("\u0412\u0438\u0434 1").visible(() -> this.mode.isSelected("Ghosts"));
    private final ValueSetting animSpeed = new ValueSetting("Animation Speed", "Speed of Helix / Atom / Pulse").setValue(1.0f).range(0.3f, 3.0f).visible(() -> this.mode.isSelected("Helix") || this.mode.isSelected("Atom") || this.mode.isSelected("Pulse"));
    private final ValueSetting ghostSpeed = new ValueSetting("Ghost Speed", "Speed of ghost flying around the target").setValue(1.0f).range(0.5f, 2.0f).visible(() -> this.mode.isSelected("Ghosts"));
    private final ValueSetting crystalDistortion = new ValueSetting("\u0418\u0441\u043a\u0430\u0436\u0435\u043d\u0438\u0435 \u043a\u0440\u0438\u0441\u0442\u0430\u043b\u043b\u043e\u0432", "\u0421\u0438\u043b\u0430 \u043f\u0440\u0435\u043b\u043e\u043c\u043b\u0435\u043d\u0438\u044f \u0436\u0438\u0434\u043a\u043e\u0433\u043e \u0441\u0442\u0435\u043a\u043b\u0430 \u0432\u043e\u043a\u0440\u0443\u0433 \u043a\u0440\u0438\u0441\u0442\u0430\u043b\u043b\u043e\u0432").setValue(1.0f).range(0.1f, 3.0f).step(0.05f).visible(() -> this.mode.isSelected("Crystals"));
    private final ValueSetting bloomParticleCount = new ValueSetting("Bloom Particle Count", "\u041a\u043e\u043b\u0438\u0447\u0435\u0441\u0442\u0432\u043e \u0447\u0430\u0441\u0442\u0438\u0446 \u0432\u043e\u043a\u0440\u0443\u0433 \u0446\u0435\u043b\u0438").range(1, 64).setValue(36.0f).visible(() -> this.mode.isSelected("Bloom") || this.mode.isSelected("\u0411\u043b\u0443\u043c"));
    private final Animation animation = new DecelerateAnimation().setMs(400).setValue(1.0);
    private final LightningTargetESP lightningTargetESP = new LightningTargetESP();
    private final BloomTargetESP bloomTargetESP = new BloomTargetESP();
    private final SwordTargetESP swordTargetESP = new SwordTargetESP();
    private final CrystalTargetESP crystalTargetESP = new CrystalTargetESP();
    private class_1309 lastTarget;
    private float hitAnim;
    private long lastRenderTime = System.currentTimeMillis();

    public static TargetESP getInstance() {
        return Instance.get(TargetESP.class);
    }

    public TargetESP() {
        super("TargetESP", "Target ESP", ModuleCategory.RENDER);
        this.setup(this.mode, this.cubeType, this.ghostType, this.ghostSpeed, this.animSpeed, this.crystalDistortion, this.bloomParticleCount);
    }

    @Override
    public void deactivate() {
        this.hitAnim = 0.0f;
        this.lastTarget = null;
        this.lightningTargetESP.reset();
        this.crystalTargetESP.reset();
        super.deactivate();
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        String selectedMode;
        class_1309 target = this.currentTarget();
        if (target != null) {
            if (this.lastTarget != target) {
                this.hitAnim = 0.0f;
            }
            this.lastTarget = target;
        }
        this.animation.setDirection(target != null ? Direction.FORWARDS : Direction.BACKWARDS);
        if (this.animation.isFinished(Direction.BACKWARDS)) {
            this.lastTarget = null;
            this.hitAnim = 0.0f;
            Render3DUtil.resetCircleSmoothing();
            this.lightningTargetESP.reset();
            this.crystalTargetESP.reset();
            return;
        }
        if (this.lastTarget == null) {
            return;
        }
        Render3DUtil.updateTargetEsp();
        float anim = this.animation.getOutput().floatValue();
        float tickDelta = tickCounter.getTickProgress(false);
        float red = class_3532.method_15363((float)(((float)this.lastTarget.field_6235 - tickDelta) / 10.0f), (float)0.0f, (float)1.0f);
        long now = System.currentTimeMillis();
        float delta = class_3532.method_15363((float)((float)(now - this.lastRenderTime) / 1000.0f), (float)0.001f, (float)0.1f);
        this.lastRenderTime = now;
        float speed = 20.0f;
        this.hitAnim += (red - this.hitAnim) * (1.0f - (float)Math.exp(-speed * delta));
        if (Math.abs(this.hitAnim - red) < 0.001f) {
            this.hitAnim = red;
        }
        this.hitAnim = class_3532.method_15363((float)this.hitAnim, (float)0.0f, (float)1.0f);
        float cubeScale = this.cubeScale(anim, this.hitAnim);
        int glowPasses = 2;
        switch (selectedMode = this.mode.getSelected()) {
            case "Cube": {
                if (this.cubeType.isSelected("\u0412\u0438\u0434 2")) {
                    Render3DUtil.drawCube2(event.getStack(), this.lastTarget, anim, red, cubeScale, glowPasses);
                    break;
                }
                Render3DUtil.drawCube(event.getStack(), this.lastTarget, anim, red, cubeScale, glowPasses);
                break;
            }
            case "Black Flash": {
                Render3DUtil.drawBlackFlash(event.getStack(), this.lastTarget, anim, red);
                break;
            }
            case "Crystals": {
                this.crystalTargetESP.render(event.getStack(), this.lastTarget, anim, this.crystalDistortion.getValue(), true);
                break;
            }
            case "Circle": {
                int color = ColorUtil.getClientColor();
                Render3DUtil.drawCrystalCircle(event.getStack(), this.lastTarget, anim, color);
                break;
            }
            case "\u041c\u043e\u043b\u043d\u0438\u0438": {
                this.lightningTargetESP.render(event.getStack(), tickDelta, this.lastTarget, anim, 1.0f);
                break;
            }
            case "Bloom": 
            case "\u0411\u043b\u0443\u043c": {
                this.bloomTargetESP.render(event.getStack(), tickDelta, this.lastTarget, anim, this.hitAnim, this.bloomParticleCount.getInt());
                break;
            }
            case "Helix": {
                ru.recoin.implement.features.modules.render.esp.AnimatedTargetESP.helix(this.lastTarget, anim, this.hitAnim, this.animSpeed.getValue());
                break;
            }
            case "Atom": {
                ru.recoin.implement.features.modules.render.esp.AnimatedTargetESP.atom(this.lastTarget, anim, this.hitAnim, this.animSpeed.getValue());
                break;
            }
            case "Pulse": {
                ru.recoin.implement.features.modules.render.esp.AnimatedTargetESP.pulse(this.lastTarget, anim, this.hitAnim, this.animSpeed.getValue());
                break;
            }
            case "Swords": {
                this.swordTargetESP.render(event.getStack(), tickDelta, this.lastTarget, anim, this.hitAnim);
                break;
            }
            default: {
                if (this.ghostType.isSelected("\u0412\u0438\u0434 3")) {
                    Render3DUtil.drawGhosts3(event.getStack(), this.lastTarget, anim, this.ghostSpeed.getValue());
                    break;
                }
                if (this.ghostType.isSelected("\u0412\u0438\u0434 2")) {
                    Render3DUtil.drawGhosts2(event.getStack(), this.lastTarget, anim, red, this.ghostSpeed.getValue());
                    break;
                }
                Render3DUtil.drawGhosts(event.getStack(), this.lastTarget, anim, red, this.ghostSpeed.getValue());
            }
        }
    }

    private class_1309 currentTarget() {
        class_1309 target;
        class_1309 target2;
        Aura aura = Aura.getInstance();
        if (aura != null && aura.isState() && this.isValid(target2 = aura.getTarget())) {
            return target2;
        }
        AimAssist aimAssist = AimAssist.getInstance();
        if (aimAssist != null && aimAssist.isState() && this.isValid(target = aimAssist.getTarget())) {
            return target;
        }
        return null;
    }

    private boolean isValid(class_1309 entity) {
        return entity != null && entity.method_5805() && !entity.method_31481();
    }

    private float cubeScale(float anim, float hitProgress) {
        float reverse = 1.0f - class_3532.method_15363((float)anim, (float)0.0f, (float)1.0f);
        float spawnScale = 1.0f + reverse * reverse * 0.35f;
        float hitScale = 1.0f - 0.3333333f * hitProgress;
        return spawnScale * hitScale;
    }
}

