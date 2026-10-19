/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_1657
 *  net.minecraft.class_1743
 *  net.minecraft.class_239
 *  net.minecraft.class_3489
 *  net.minecraft.class_3966
 *  net.minecraft.class_9362
 */
package ru.recoin.implement.features.modules.combat;

import java.util.ArrayList;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1743;
import net.minecraft.class_239;
import net.minecraft.class_3489;
import net.minecraft.class_3966;
import net.minecraft.class_9362;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.MultiSelectSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.repository.friend.FriendUtils;
import ru.recoin.common.util.other.Instance;
import ru.recoin.core.Main;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.features.modules.combat.MaceHelper;
import ru.recoin.implement.features.modules.combat.killaura.attack.AttackPerpetrator;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RaytracingUtil;

public class TriggerBot
extends Module {
    private class_1309 target = null;
    private long groundedSince = -1L;
    private final MultiSelectSetting attackSetting = new MultiSelectSetting("Attack setting", "Allows you to customize the attack").value("Only Critical", "Crits Only Space", "Break Shield", "UnPress Shield", "No Attack When Eat", "No Attack In Containers", "Only Weapon", "Ignore The Walls").selected("Only Critical", "Break Shield", "No Attack When Eat", "No Attack In Containers");
    private final BooleanSetting onlyThroughPartialBlocks = new BooleanSetting("Only Through Partial Blocks", "Allows attacks only through non-full blocks while Ignore The Walls is enabled").setValue(false).visible(() -> this.attackSetting.isSelected("Ignore The Walls"));
    private final BooleanSetting randomizeHits = new BooleanSetting("\u0420\u0430\u043d\u0434\u043e\u043c\u0438\u0437\u0438\u0440\u043e\u0432\u0430\u0442\u044c \u0443\u0434\u0430\u0440\u044b", "\u0420\u0430\u043d\u0434\u043e\u043c\u0438\u0437\u0430\u0446\u0438\u044f \u0432\u044b\u0441\u043e\u0442\u044b \u043f\u0430\u0434\u0435\u043d\u0438\u044f \u0434\u043b\u044f \u043a\u0440\u0438\u0442\u043e\u0432").setValue(false);
    private final SelectSetting sprintResetMode = new SelectSetting("Sprint Reset", "Selects sprint reset behavior before attack").value("Off", "Legit", "Packet", "Fast").selected("Legit");

    public static TriggerBot getInstance() {
        return Instance.get(TriggerBot.class);
    }

    public TriggerBot() {
        super("TriggerBot", "Trigger Bot", ModuleCategory.COMBAT);
        this.setup(this.attackSetting, this.onlyThroughPartialBlocks, this.randomizeHits, this.sprintResetMode);
    }

    @Override
    public void deactivate() {
        this.target = null;
        this.groundedSince = -1L;
        Main.getInstance().getAttackPerpetrator().getAttackHandler().clearPacketSprintReset();
        Main.getInstance().getAttackPerpetrator().getAttackHandler().clearFastSprintReset(this);
        super.deactivate();
    }

    @EventHandler
    public void onTick(TickEvent e) {
        class_3966 r;
        class_1297 class_12972;
        class_3966 result;
        if (TriggerBot.mc.field_1724 == null || !this.canAttackWithHeldItem()) {
            this.target = null;
            Main.getInstance().getAttackPerpetrator().getAttackHandler().clearPacketSprintReset();
            Main.getInstance().getAttackPerpetrator().getAttackHandler().clearFastSprintReset(this);
            return;
        }
        boolean mace = TriggerBot.mc.field_1724.method_6047().method_7909() instanceof class_9362;
        float range = 3.0f + (mace ? MaceHelper.getInstance().getTargetDistanceBonus() : 0.0f);
        boolean ignoreWalls = this.attackSetting.isSelected("Ignore The Walls");
        if (ignoreWalls) {
            result = RaytracingUtil.raytraceEntity(range, AngleUtil.cameraAngle(), entity -> {
                class_1657 player;
                return !(entity instanceof class_1657) || !FriendUtils.isFriend((player = (class_1657)entity).method_5477().getString());
            });
        } else {
            class_3966 entityHit;
            class_239 hit = RaytracingUtil.raycastCrosshair((class_1297)TriggerBot.mc.field_1724, range, range, 1.0f, AngleUtil.cameraAngle());
            class_3966 class_39662 = result = hit instanceof class_3966 ? (entityHit = (class_3966)hit) : null;
        }
        if (result instanceof class_3966 && (class_12972 = (r = result).method_17782()) instanceof class_1309) {
            AttackPerpetrator.AttackPerpetratorConfigurable config;
            class_1309 entity2 = (class_1309)class_12972;
            if (ignoreWalls && this.onlyThroughPartialBlocks.isValue() && !RaytracingUtil.canSeeThroughPartialBlocks(TriggerBot.mc.field_1724.method_33571(), r.method_17784())) {
                this.target = null;
                Main.getInstance().getAttackPerpetrator().getAttackHandler().clearPacketSprintReset();
                Main.getInstance().getAttackPerpetrator().getAttackHandler().clearFastSprintReset(this);
                return;
            }
            ArrayList<String> options = new ArrayList<String>(this.attackSetting.getSelected());
            if (this.onlyThroughPartialBlocks.isValue() && ignoreWalls) {
                options.add("Only Through Partial Blocks");
            }
            if (options.contains("Only Critical") && this.isInWater()) {
                options.remove("Only Critical");
            }
            if (options.contains("Only Critical") && options.contains("Crits Only Space") && this.hasWaitedOnGroundForSplash()) {
                options.remove("Only Critical");
            }
            if ((config = new AttackPerpetrator.AttackPerpetratorConfigurable(this.target = entity2, AngleUtil.cameraAngle(), mace ? 3.0f : 3.3f, options, new SelectSetting("Mode", "").value("Default"), this.target.method_5829(), this.sprintResetMode, this.randomizeHits.isValue(), false, this)).isFastSprintReset()) {
                Main.getInstance().getAttackPerpetrator().getAttackHandler().updateFastSprintReset(config);
            } else {
                Main.getInstance().getAttackPerpetrator().getAttackHandler().clearFastSprintReset(this);
            }
            Main.getInstance().getAttackPerpetrator().performAttack(config);
        } else {
            this.target = null;
            Main.getInstance().getAttackPerpetrator().getAttackHandler().clearPacketSprintReset();
            Main.getInstance().getAttackPerpetrator().getAttackHandler().clearFastSprintReset(this);
        }
    }

    private boolean hasWaitedOnGroundForSplash() {
        if (TriggerBot.mc.field_1724 == null || !TriggerBot.mc.field_1724.method_24828()) {
            this.groundedSince = -1L;
            return false;
        }
        if (this.groundedSince < 0L) {
            this.groundedSince = System.currentTimeMillis();
        }
        return System.currentTimeMillis() - this.groundedSince >= 200L;
    }

    private boolean canAttackWithHeldItem() {
        if (!this.attackSetting.isSelected("Only Weapon")) {
            return true;
        }
        return TriggerBot.mc.field_1724.method_6047().method_31573(class_3489.field_42611) || TriggerBot.mc.field_1724.method_6047().method_7909() instanceof class_1743 || TriggerBot.mc.field_1724.method_6047().method_7909() instanceof class_9362;
    }

    private boolean isInWater() {
        return TriggerBot.mc.field_1724 != null && (TriggerBot.mc.field_1724.method_5799() || TriggerBot.mc.field_1724.method_5869() || TriggerBot.mc.field_1724.method_5681());
    }

    public class_1309 getTarget() {
        return this.target;
    }

    public long getGroundedSince() {
        return this.groundedSince;
    }

    public MultiSelectSetting getAttackSetting() {
        return this.attackSetting;
    }

    public BooleanSetting getOnlyThroughPartialBlocks() {
        return this.onlyThroughPartialBlocks;
    }

    public BooleanSetting getRandomizeHits() {
        return this.randomizeHits;
    }

    public SelectSetting getSprintResetMode() {
        return this.sprintResetMode;
    }
}

