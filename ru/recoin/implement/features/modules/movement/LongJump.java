/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1294
 *  net.minecraft.class_243
 */
package ru.recoin.implement.features.modules.movement;

import net.minecraft.class_1294;
import net.minecraft.class_243;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.entity.MovingUtil;
import ru.recoin.common.util.other.Instance;
import ru.recoin.implement.events.player.TickEvent;

public class LongJump
extends Module {
    private final SelectSetting mode = new SelectSetting("\u0420\u0435\u0436\u0438\u043c", "\u0423\u0432\u0435\u043b\u0438\u0447\u0438\u0432\u0430\u0435\u0442 \u0434\u0430\u043b\u044c\u043d\u043e\u0441\u0442\u044c \u043f\u0440\u044b\u0436\u043a\u0430").value("\u041e\u0431\u044b\u0447\u043d\u044b\u0439", "\u041d\u0435\u0437\u0430\u043c\u0435\u0442\u043d\u044b\u0439").selected("\u041e\u0431\u044b\u0447\u043d\u044b\u0439");
    private final BooleanSetting useTimer = new BooleanSetting("\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u0442\u044c \u0442\u0430\u0439\u043c\u0435\u0440", "").setValue(false).visible(() -> this.mode.isSelected("\u041e\u0431\u044b\u0447\u043d\u044b\u0439"));
    private final ValueSetting timerSpeed = new ValueSetting("\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u0442\u0430\u0439\u043c\u0435\u0440\u0430", "").setValue(1.0f).range(0.5f, 3.0f).step(0.1f).visible(() -> this.mode.isSelected("\u041e\u0431\u044b\u0447\u043d\u044b\u0439") && this.useTimer.isValue());
    private final BooleanSetting disableAfterJump = new BooleanSetting("\u041e\u0442\u043a\u043b\u044e\u0447\u0430\u0442\u044c \u043f\u043e\u0441\u043b\u0435 \u043f\u0440\u044b\u0436\u043a\u0430", "").setValue(true).visible(() -> this.mode.isSelected("\u041e\u0431\u044b\u0447\u043d\u044b\u0439"));
    private final BooleanSetting infiniteDistance = new BooleanSetting("\u0411\u0435\u0441\u043a\u043e\u043d\u0435\u0447\u043d\u0430\u044f \u0434\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f", "").setValue(false).visible(() -> this.mode.isSelected("\u041e\u0431\u044b\u0447\u043d\u044b\u0439"));
    private final ValueSetting maxDistance = new ValueSetting("\u041c\u0430\u043a\u0441. \u0434\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f", "").setValue(10.0f).range(5.0f, 40.0f).step(1.0f).visible(() -> this.mode.isSelected("\u041e\u0431\u044b\u0447\u043d\u044b\u0439") && !this.infiniteDistance.isValue());
    private final ValueSetting speed = new ValueSetting("\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c", "").setValue(1.35f).range(0.1f, 10.0f).visible(() -> this.mode.isSelected("\u041e\u0431\u044b\u0447\u043d\u044b\u0439"));
    private final ValueSetting subtleSpeed = new ValueSetting("\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c (\u041d\u0435\u0437\u0430\u043c\u0435\u0442\u043d\u044b\u0439)", "").setValue(0.45f).range(0.1f, 2.0f).step(0.01f).visible(() -> this.mode.isSelected("\u041d\u0435\u0437\u0430\u043c\u0435\u0442\u043d\u044b\u0439"));
    private final BooleanSetting boostY = new BooleanSetting("\u0411\u0443\u0441\u0442 \u043f\u043e Y", "").setValue(false).visible(() -> this.mode.isSelected("\u041e\u0431\u044b\u0447\u043d\u044b\u0439"));
    private final ValueSetting yBoost = new ValueSetting("\u0421\u0438\u043b\u0430 \u0431\u0443\u0441\u0442\u0430 Y", "").setValue(0.5f).range(0.1f, 2.0f).step(0.1f).visible(() -> this.mode.isSelected("\u041e\u0431\u044b\u0447\u043d\u044b\u0439") && this.boostY.isValue());
    private transient int stage;
    private transient double movementSpeed;
    private transient double lastDistance;
    private transient class_243 start = class_243.field_1353;
    private transient boolean subtleJump;
    private transient boolean timedDisable;
    private transient long jumpTime;
    private transient float timerRate = 1.0f;

    public LongJump() {
        super("LongJump", ModuleCategory.MOVEMENT);
        this.speed.setStep(0.1f);
        this.setup(this.mode, this.useTimer, this.timerSpeed, this.disableAfterJump, this.infiniteDistance, this.maxDistance, this.speed, this.subtleSpeed, this.boostY, this.yBoost);
    }

    public static float getTimerRate() {
        LongJump module = Instance.get(LongJump.class);
        return module != null && module.isState() && LongJump.mc.field_1724 != null && LongJump.mc.field_1687 != null ? module.timerRate : 1.0f;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (LongJump.mc.field_1724 == null || LongJump.mc.field_1687 == null) {
            return;
        }
        double dx = LongJump.mc.field_1724.method_23317() - LongJump.mc.field_1724.field_6014;
        double dz = LongJump.mc.field_1724.method_23321() - LongJump.mc.field_1724.field_5969;
        this.lastDistance = Math.sqrt(dx * dx + dz * dz);
        if (this.mode.isSelected("\u041d\u0435\u0437\u0430\u043c\u0435\u0442\u043d\u044b\u0439")) {
            this.tickSubtle();
        } else {
            this.tickNormal();
        }
    }

    private void tickNormal() {
        if (!this.infiniteDistance.isValue() && LongJump.mc.field_1724.method_73189().method_1025(this.start) > (double)(this.maxDistance.getValue() * this.maxDistance.getValue())) {
            this.setState(false);
            return;
        }
        if (!MovingUtil.hasPlayerMovement()) {
            this.stage = 0;
            this.movementSpeed = 0.0;
            return;
        }
        if (this.useTimer.isValue()) {
            this.timerRate = this.timerSpeed.getValue();
        }
        switch (this.stage) {
            case 0: {
                this.movementSpeed = (double)this.speed.getValue() * 0.2873;
                this.stage = 1;
                break;
            }
            case 1: {
                class_243 velocity = LongJump.mc.field_1724.method_18798();
                LongJump.mc.field_1724.method_18800(velocity.field_1352, 0.42 + (double)this.jumpBoost() + (this.boostY.isValue() ? (double)this.yBoost.getValue() : 0.0), velocity.field_1350);
                this.movementSpeed *= 2.149;
                if (this.disableAfterJump.isValue() && !this.timedDisable) {
                    this.jumpTime = System.currentTimeMillis();
                    this.timedDisable = true;
                }
                this.stage = 2;
                break;
            }
            case 2: {
                this.movementSpeed = this.lastDistance - 0.66 * (this.lastDistance - 0.2873);
                if (!(LongJump.mc.field_1724.method_18798().field_1351 < 0.0)) break;
                this.stage = 3;
                break;
            }
            case 3: {
                this.movementSpeed = Math.max(0.2873, this.movementSpeed);
                if (!LongJump.mc.field_1724.method_24828()) break;
                if (this.disableAfterJump.isValue()) {
                    this.setState(false);
                }
                this.stage = 0;
            }
        }
        this.setHorizontalSpeed(this.movementSpeed);
        if (this.isState() && this.timedDisable && this.jumpTime > 0L && System.currentTimeMillis() - this.jumpTime >= 10L) {
            this.setState(false);
        }
    }

    private void tickSubtle() {
        if (LongJump.mc.field_1724.method_24828() && LongJump.mc.field_1690.field_1903.method_1434()) {
            LongJump.mc.field_1724.method_6043();
            this.subtleJump = true;
            this.stage = 1;
        }
        if (!this.subtleJump) {
            return;
        }
        switch (this.stage) {
            case 1: {
                this.setHorizontalSpeed((double)this.subtleSpeed.getValue() * 0.2873);
                class_243 velocity = LongJump.mc.field_1724.method_18798();
                LongJump.mc.field_1724.method_18800(velocity.field_1352, 0.4 + (double)this.jumpBoost(), velocity.field_1350);
                this.stage = 2;
                break;
            }
            case 2: {
                if (!(LongJump.mc.field_1724.method_18798().field_1351 < 0.0)) break;
                this.stage = 3;
                break;
            }
            case 3: {
                if (!LongJump.mc.field_1724.method_24828()) break;
                this.subtleJump = false;
                this.stage = 0;
                if (!this.disableAfterJump.isValue()) break;
                this.setState(false);
            }
        }
    }

    private float jumpBoost() {
        return LongJump.mc.field_1724.method_6059(class_1294.field_5913) ? 0.2f : 0.0f;
    }

    private void setHorizontalSpeed(double value) {
        if (MovingUtil.hasPlayerMovement()) {
            MovingUtil.setVelocity(value);
        }
    }

    @Override
    public void activate() {
        this.reset();
        if (LongJump.mc.field_1724 != null) {
            this.start = LongJump.mc.field_1724.method_73189();
        }
        this.jumpTime = 0L;
        super.activate();
    }

    @Override
    public void deactivate() {
        this.reset();
        super.deactivate();
    }

    private void reset() {
        this.timerRate = 1.0f;
        this.stage = 0;
        this.movementSpeed = 0.0;
        this.lastDistance = 0.0;
        this.subtleJump = false;
        this.timedDisable = false;
    }
}

