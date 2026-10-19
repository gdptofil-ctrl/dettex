/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1297
 *  net.minecraft.class_1511
 *  net.minecraft.class_1542
 *  net.minecraft.class_1657
 *  net.minecraft.class_1802
 *  net.minecraft.class_2246
 *  net.minecraft.class_2338
 *  net.minecraft.class_2350
 *  net.minecraft.class_2382
 *  net.minecraft.class_243
 *  net.minecraft.class_2596
 *  net.minecraft.class_2885
 *  net.minecraft.class_3965
 */
package ru.recoin.implement.features.modules.combat;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1511;
import net.minecraft.class_1542;
import net.minecraft.class_1657;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_2885;
import net.minecraft.class_3965;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.repository.friend.FriendUtils;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.events.player.EntitySpawnEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.features.modules.combat.AutoCrystal;

public class AutoExplosion
extends Module {
    private final BooleanSetting saveSelf = new BooleanSetting("Save Self", "Do not explode a crystal below your feet").setValue(false);
    private final BooleanSetting saveFriend = new BooleanSetting("Save Friend", "Do not explode a crystal below a friend").setValue(false);
    private final BooleanSetting saveResources = new BooleanSetting("Save Resources", "Do not explode close to dropped items").setValue(false);
    private final Map<class_2338, Integer> pendingObsidian = new HashMap<class_2338, Integer>();
    private final Map<class_2338, Integer> ownCrystals = new HashMap<class_2338, Integer>();
    private class_1511 pendingAttack;

    public AutoExplosion() {
        super("AutoExplosion", "Auto Explosion", ModuleCategory.COMBAT);
        this.setup(this.saveSelf, this.saveFriend, this.saveResources);
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        class_2596<?> class_25962;
        if (this.combatControlled() || !event.isSend() || AutoExplosion.mc.field_1724 == null || AutoExplosion.mc.field_1687 == null || !((class_25962 = event.getPacket()) instanceof class_2885)) {
            return;
        }
        class_2885 packet = (class_2885)class_25962;
        class_1268 hand = packet.method_12546();
        if (AutoExplosion.mc.field_1724.method_5998(hand).method_31574(class_1802.field_8281)) {
            class_3965 hit = packet.method_12543();
            this.pendingObsidian.put(hit.method_17777().method_10093(hit.method_17780()), AutoExplosion.mc.field_1724.field_6012);
        } else if (AutoExplosion.mc.field_1724.method_5998(hand).method_31574(class_1802.field_8301)) {
            this.ownCrystals.put(packet.method_12543().method_17777().method_10084(), AutoExplosion.mc.field_1724.field_6012);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @EventHandler
    public void onTick(TickEvent event) {
        if (AutoExplosion.mc.field_1724 == null || AutoExplosion.mc.field_1687 == null || AutoExplosion.mc.field_1761 == null) {
            this.reset();
            return;
        }
        if (this.combatControlled()) {
            this.reset();
            return;
        }
        int age = AutoExplosion.mc.field_1724.field_6012;
        this.pendingObsidian.entrySet().removeIf(entry -> age - (Integer)entry.getValue() > 12);
        this.ownCrystals.entrySet().removeIf(entry -> age - (Integer)entry.getValue() > 25);
        if (this.pendingAttack != null) {
            class_1511 crystal = this.pendingAttack;
            this.pendingAttack = null;
            if (crystal.method_5805() && AutoExplosion.mc.field_1724.method_33571().method_1025(crystal.method_5829().method_1005()) < 9.0 && this.safe(crystal.method_24515())) {
                AutoExplosion.mc.field_1761.method_2918((class_1657)AutoExplosion.mc.field_1724, (class_1297)crystal);
                AutoExplosion.mc.field_1724.method_6104(class_1268.field_5808);
            }
        }
        int slot = -1;
        for (int i = 0; i < 9; ++i) {
            if (!AutoExplosion.mc.field_1724.method_31548().method_5438(i).method_31574(class_1802.field_8301)) continue;
            slot = i;
            break;
        }
        if (slot < 0) {
            return;
        }
        for (class_2338 pos : (class_2338[])this.pendingObsidian.keySet().toArray(class_2338[]::new)) {
            if (!AutoExplosion.mc.field_1687.method_8320(pos).method_27852(class_2246.field_10540)) continue;
            this.pendingObsidian.remove(pos);
            if (!this.safe(pos) || AutoExplosion.mc.field_1724.method_5707(class_243.method_24953((class_2382)pos)) > 20.25 || !AutoExplosion.mc.field_1687.method_8320(pos.method_10084()).method_26215()) continue;
            int previous = AutoExplosion.mc.field_1724.method_31548().method_67532();
            try {
                AutoExplosion.mc.field_1724.method_31548().method_61496(slot);
                class_243 hit = class_243.method_24953((class_2382)pos).method_1031(0.0, 0.5, 0.0);
                AutoExplosion.mc.field_1761.method_2896(AutoExplosion.mc.field_1724, class_1268.field_5808, new class_3965(hit, class_2350.field_11036, pos, false));
                AutoExplosion.mc.field_1724.method_6104(class_1268.field_5808);
                this.ownCrystals.put(pos.method_10084(), age);
                break;
            }
            finally {
                AutoExplosion.mc.field_1724.method_31548().method_61496(previous);
            }
        }
    }

    @EventHandler
    public void onSpawn(EntitySpawnEvent event) {
        class_1297 class_12972;
        if (this.combatControlled() || AutoExplosion.mc.field_1724 == null || !((class_12972 = event.getEntity()) instanceof class_1511)) {
            return;
        }
        class_1511 crystal = (class_1511)class_12972;
        if (this.ownCrystals.remove(crystal.method_24515()) != null) {
            this.pendingAttack = crystal;
        }
    }

    private boolean safe(class_2338 pos) {
        if (this.saveSelf.isValue() && AutoExplosion.mc.field_1724.method_23318() > (double)pos.method_10264()) {
            return false;
        }
        for (class_1297 entity : AutoExplosion.mc.field_1687.method_18112()) {
            class_1657 player;
            if (this.saveResources.isValue() && entity instanceof class_1542 && Math.abs(entity.method_23317() - (double)pos.method_10263()) < 4.0 && Math.abs(entity.method_23318() - (double)pos.method_10264()) < 2.0 && Math.abs(entity.method_23321() - (double)pos.method_10260()) < 4.0) {
                return false;
            }
            if (!this.saveFriend.isValue() || !(entity instanceof class_1657) || !FriendUtils.isFriend((class_1297)(player = (class_1657)entity)) || !(player.method_23318() > (double)pos.method_10264())) continue;
            return false;
        }
        return true;
    }

    private boolean combatControlled() {
        return AutoCrystal.getInstance() != null && AutoCrystal.getInstance().isState();
    }

    private void reset() {
        this.pendingObsidian.clear();
        this.ownCrystals.clear();
        this.pendingAttack = null;
    }

    @Override
    public void deactivate() {
        this.reset();
        super.deactivate();
    }
}

