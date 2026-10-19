/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1309
 *  net.minecraft.class_1661
 *  net.minecraft.class_1735
 *  net.minecraft.class_1796
 *  net.minecraft.class_1796$class_1797
 *  net.minecraft.class_1802
 *  net.minecraft.class_1937
 *  net.minecraft.class_243
 *  net.minecraft.class_2596
 *  net.minecraft.class_2886
 */
package ru.recoin.implement.features.modules.combat;

import java.util.Optional;
import net.minecraft.class_1268;
import net.minecraft.class_1309;
import net.minecraft.class_1661;
import net.minecraft.class_1735;
import net.minecraft.class_1796;
import net.minecraft.class_1802;
import net.minecraft.class_1937;
import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_2886;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.common.util.entity.PlayerInventoryComponent;
import ru.recoin.common.util.entity.PlayerInventoryUtil;
import ru.recoin.common.util.entity.ScreenWalkInventorySync;
import ru.recoin.common.util.other.Instance;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.features.modules.combat.Aura;
import ru.recoin.implement.features.modules.combat.MaceUtil;
import ru.recoin.implement.features.modules.combat.TriggerBot;

public final class MaceHelper
extends Module {
    private boolean windChargePending;
    private boolean swapped;
    private boolean airborneAfterSwap;
    private int groundGraceTicks;
    private int previousSlot = -1;
    private class_1735 inventoryMaceSlot;

    public MaceHelper() {
        super("MaceHelper", "Mace Helper", ModuleCategory.COMBAT);
    }

    public static MaceHelper getInstance() {
        return Instance.get(MaceHelper.class);
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        block3: {
            block2: {
                class_2596<?> class_25962;
                if (!event.isSend() || !((class_25962 = event.getPacket()) instanceof class_2886)) break block2;
                class_2886 packet = (class_2886)class_25962;
                if (MaceHelper.mc.field_1724 != null && MaceHelper.mc.field_1724.method_5998(packet.method_12551()).method_31574(class_1802.field_49098)) break block3;
            }
            return;
        }
        this.windChargePending = this.hasCombatModule();
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (MaceHelper.mc.field_1724 == null || MaceHelper.mc.field_1687 == null) {
            return;
        }
        if (this.swapped && !MaceHelper.mc.field_1724.method_24828()) {
            this.airborneAfterSwap = true;
        }
        if (this.groundGraceTicks > 0) {
            --this.groundGraceTicks;
        }
        if (this.shouldRestore()) {
            this.restoreSlot();
            if (!this.swapped) {
                this.windChargePending = false;
            }
            if (this.swapped) {
                return;
            }
        }
        if (this.windChargePending) {
            if (!this.hasCombatModule()) {
                this.windChargePending = false;
                return;
            }
            if (PlayerInventoryComponent.isInventoryActionBusy()) {
                return;
            }
            if (this.equipMace()) {
                this.windChargePending = false;
            }
            return;
        }
        class_1309 target = this.getCombatTarget();
        if (this.shouldArmForFall(target) && !PlayerInventoryComponent.isInventoryActionBusy()) {
            this.equipMace();
        }
    }

    private boolean shouldRestore() {
        return this.swapped && (MaceHelper.mc.field_1724.method_5799() || MaceHelper.mc.field_1724.field_6012 < 5 || MaceHelper.mc.field_1724.method_24828() && this.groundGraceTicks <= 0 && (this.airborneAfterSwap || !this.windChargePending));
    }

    private boolean shouldArmForFall(class_1309 target) {
        if (this.swapped || MaceUtil.isHoldingMace() || target == null || !target.method_5805() || target.method_7325() || MaceHelper.mc.field_1724.method_24828() || MaceHelper.mc.field_1724.field_6017 <= 0.0 || MaceHelper.mc.field_1724.method_18798().field_1351 >= 0.0 || this.horizontalDistanceSquared(target) > 36.0 || this.maceCooldownTicks() > 10) {
            return false;
        }
        Optional<class_243> landing = MaceUtil.predictLanding(MaceHelper.mc.field_1724, (class_1937)MaceHelper.mc.field_1687);
        return landing.isPresent() && MaceHelper.mc.field_1724.method_23318() - landing.get().field_1351 > 2.5;
    }

    private boolean equipMace() {
        if (this.swapped || MaceUtil.isHoldingMace()) {
            return true;
        }
        class_1735 hotbarMace = this.findMace(true);
        if (hotbarMace != null) {
            this.previousSlot = MaceHelper.mc.field_1724.method_31548().method_67532();
            this.swapped = true;
            this.airborneAfterSwap = false;
            this.groundGraceTicks = 2;
            this.selectSlot(hotbarMace.method_34266());
            return true;
        }
        class_1735 inventoryMace = this.findMace(false);
        if (inventoryMace == null) {
            return false;
        }
        this.previousSlot = MaceHelper.mc.field_1724.method_31548().method_67532();
        this.inventoryMaceSlot = inventoryMace;
        this.swapped = true;
        this.airborneAfterSwap = false;
        this.groundGraceTicks = 2;
        ScreenWalkInventorySync.swapHand(this.inventoryMaceSlot, class_1268.field_5808, true);
        return true;
    }

    private void restoreSlot() {
        if (!this.swapped) {
            return;
        }
        if (this.inventoryMaceSlot != null) {
            ScreenWalkInventorySync.swapHand(this.inventoryMaceSlot, class_1268.field_5808, true);
        } else if (this.previousSlot >= 0) {
            this.selectSlot(this.previousSlot);
        }
        this.swapped = false;
        this.airborneAfterSwap = false;
        this.groundGraceTicks = 0;
        this.previousSlot = -1;
        this.inventoryMaceSlot = null;
    }

    private class_1735 findMace(boolean hotbar) {
        return PlayerInventoryUtil.slots().filter(slot -> slot.field_7871 instanceof class_1661).filter(slot -> slot.method_7677().method_31574(class_1802.field_49814)).filter(slot -> hotbar == (slot.method_34266() >= 0 && slot.method_34266() < 9)).findFirst().orElse(null);
    }

    private class_1309 getCombatTarget() {
        class_1309 auraTarget = Aura.getInstance().getTarget();
        return auraTarget != null ? auraTarget : TriggerBot.getInstance().getTarget();
    }

    private boolean hasCombatModule() {
        Aura aura = Aura.getInstance();
        TriggerBot triggerBot = TriggerBot.getInstance();
        return aura != null && aura.isState() || triggerBot != null && triggerBot.isState();
    }

    private double horizontalDistanceSquared(class_1309 target) {
        double dx = MaceHelper.mc.field_1724.method_23317() - target.method_23317();
        double dz = MaceHelper.mc.field_1724.method_23321() - target.method_23321();
        return dx * dx + dz * dz;
    }

    private int maceCooldownTicks() {
        class_1796 manager = MaceHelper.mc.field_1724.method_7357();
        class_1796.class_1797 entry = (class_1796.class_1797)manager.field_8024.get(manager.method_62836(class_1802.field_49814.method_7854()));
        return entry == null ? 0 : entry.comp_3084() - manager.field_8025;
    }

    public float getTargetDistanceBonus() {
        if (!this.isState() || MaceHelper.mc.field_1724 == null || MaceHelper.mc.field_1687 == null || this.findMace(true) == null && this.findMace(false) == null) {
            return 0.0f;
        }
        Optional<class_243> landing = MaceUtil.predictLanding(MaceHelper.mc.field_1724, (class_1937)MaceHelper.mc.field_1687);
        return landing.isPresent() && MaceHelper.mc.field_1724.method_23318() - landing.get().field_1351 > 2.0 ? 10.0f : 0.0f;
    }

    private void selectSlot(int slot) {
        MaceHelper.mc.field_1724.method_31548().method_61496(slot);
    }

    @Override
    public void deactivate() {
        if (MaceHelper.mc.field_1724 != null) {
            this.restoreSlot();
        }
        this.windChargePending = false;
        super.deactivate();
    }
}

