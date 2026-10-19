/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1309
 *  net.minecraft.class_238
 *  net.minecraft.class_9362
 */
package ru.recoin.implement.features.modules.combat.killaura.attack;

import java.util.List;
import net.minecraft.class_1309;
import net.minecraft.class_238;
import net.minecraft.class_9362;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.common.QuickImports;
import ru.recoin.implement.events.item.UsingItemEvent;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.features.modules.combat.killaura.attack.AttackHandler;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;

public class AttackPerpetrator
implements QuickImports {
    AttackHandler attackHandler = new AttackHandler();

    public void tick() {
        this.attackHandler.tick();
    }

    public void onPacket(PacketEvent e) {
        this.attackHandler.onPacket(e);
    }

    public void onUsingItem(UsingItemEvent e) {
        this.attackHandler.onUsingItem(e);
    }

    public void performAttack(AttackPerpetratorConfigurable configurable) {
        this.attackHandler.handleAttack(configurable);
    }

    public boolean performRecordedAttack(AttackPerpetratorConfigurable configurable) {
        return this.attackHandler.handleRecordedAttack(configurable);
    }

    public AttackHandler getAttackHandler() {
        return this.attackHandler;
    }

    public static class AttackPerpetratorConfigurable {
        private final class_1309 target;
        private final Angle angle;
        private final float maximumRange;
        private final boolean onlyCritical;
        private final boolean critsOnlySpace;
        private final boolean shouldBreakShield;
        private final boolean shouldUnPressShield;
        private final boolean randomizeHits;
        private final boolean eatAndAttack;
        private final boolean noAttackInContainers;
        private final boolean maceAttack;
        private final boolean sprintReset;
        private final boolean ignoreWalls;
        private final boolean partialBlocksOnly;
        private final boolean adaptiveTiming;
        private final class_238 box;
        private final SelectSetting aimMode;
        private final SelectSetting sprintResetMode;
        private final Module attackOwner;

        public AttackPerpetratorConfigurable(class_1309 target, Angle angle, float maximumRange, List<String> options, SelectSetting aimMode, class_238 box, SelectSetting sprintResetMode, boolean randomizeHits, boolean adaptiveTiming, Module attackOwner) {
            this.target = target;
            this.angle = angle;
            this.maximumRange = maximumRange;
            this.onlyCritical = options.contains("Only Critical");
            this.critsOnlySpace = options.contains("Crits Only Space");
            this.shouldBreakShield = options.contains("Break Shield");
            this.shouldUnPressShield = options.contains("UnPress Shield");
            this.randomizeHits = randomizeHits;
            this.eatAndAttack = options.contains("No Attack When Eat");
            this.noAttackInContainers = options.contains("No Attack In Containers");
            this.maceAttack = QuickImports.mc.field_1724 != null && QuickImports.mc.field_1724.method_6047().method_7909() instanceof class_9362;
            this.ignoreWalls = options.contains("Ignore The Walls");
            this.partialBlocksOnly = options.contains("Only Through Partial Blocks");
            this.box = box;
            this.aimMode = aimMode;
            this.sprintResetMode = sprintResetMode;
            this.sprintReset = sprintResetMode != null && !sprintResetMode.isSelected("Off");
            this.adaptiveTiming = adaptiveTiming;
            this.attackOwner = attackOwner;
        }

        public boolean isLegitSprintReset() {
            return this.sprintReset && this.sprintResetMode.isSelected("Legit");
        }

        public boolean isPacketSprintReset() {
            return this.sprintReset && this.sprintResetMode.isSelected("Packet");
        }

        public boolean isFastSprintReset() {
            return this.sprintReset && this.sprintResetMode.isSelected("Fast");
        }

        public boolean isRandomizeHits() {
            return this.randomizeHits;
        }

        public boolean isUseDynamicCooldown() {
            return false;
        }

        public class_1309 getTarget() {
            return this.target;
        }

        public Angle getAngle() {
            return this.angle;
        }

        public float getMaximumRange() {
            return this.maximumRange;
        }

        public boolean isOnlyCritical() {
            return this.onlyCritical;
        }

        public boolean isCritsOnlySpace() {
            return this.critsOnlySpace;
        }

        public boolean isShouldBreakShield() {
            return this.shouldBreakShield;
        }

        public boolean isShouldUnPressShield() {
            return this.shouldUnPressShield;
        }

        public boolean isEatAndAttack() {
            return this.eatAndAttack;
        }

        public boolean isNoAttackInContainers() {
            return this.noAttackInContainers;
        }

        public boolean isMaceAttack() {
            return this.maceAttack;
        }

        public boolean isSprintReset() {
            return this.sprintReset;
        }

        public boolean isIgnoreWalls() {
            return this.ignoreWalls;
        }

        public boolean isPartialBlocksOnly() {
            return this.partialBlocksOnly;
        }

        public boolean isAdaptiveTiming() {
            return this.adaptiveTiming;
        }

        public class_238 getBox() {
            return this.box;
        }

        public SelectSetting getAimMode() {
            return this.aimMode;
        }

        public SelectSetting getSprintResetMode() {
            return this.sprintResetMode;
        }

        public Module getAttackOwner() {
            return this.attackOwner;
        }
    }
}

