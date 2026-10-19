/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1294
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_2246
 *  net.minecraft.class_243
 *  net.minecraft.class_2596
 *  net.minecraft.class_2828$class_2829
 *  net.minecraft.class_2828$class_2830
 */
package ru.recoin.implement.features.modules.combat;

import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_2246;
import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_2828;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.implement.events.player.AttackEvent;
import ru.recoin.implement.features.modules.combat.Aura;
import ru.recoin.mixins.ClientPlayerEntityRotationAccessor;

public final class PacketCriticals
extends Module {
    private transient int lastAttackAge = -1;
    private final SelectSetting mode = new SelectSetting("Mode", "Critical hit mode").value("Reallyworld", "Grim old").selected("Reallyworld");

    public PacketCriticals() {
        super("PacketCriticals", "Packet Criticals", ModuleCategory.COMBAT);
        this.setup(this.mode);
    }

    @EventHandler
    public void onAttack(AttackEvent event) {
        class_1309 target;
        class_1297 mineblaze2;
        if (this.mode.isSelected("Grim old")) {
            boolean onMineblaze;
            if (!event.isPre() || event.isCancelled() || PacketCriticals.mc.field_1724 == null || PacketCriticals.mc.field_1687 == null) {
                return;
            }
            Aura aura = Aura.getInstance();
            boolean bl = onMineblaze = mc.method_1558() != null && PacketCriticals.mc.method_1558().field_3761.contains("mineblaze");
            if (aura.getTarget() == null || event.getEntity() != aura.getTarget() || PacketCriticals.mc.field_1724.method_5799() || !onMineblaze && (PacketCriticals.mc.field_1724.method_24828() || PacketCriticals.mc.field_1724.method_6128())) {
                return;
            }
            PacketCriticals.mc.field_1724.field_6017 = 0.01f;
            ClientPlayerEntityRotationAccessor accessor = (ClientPlayerEntityRotationAccessor)PacketCriticals.mc.field_1724;
            PacketCriticals.mc.field_1724.field_3944.method_52787((class_2596)new class_2828.class_2830(PacketCriticals.mc.field_1724.method_23317(), PacketCriticals.mc.field_1724.method_23318() - 1.0E-6, PacketCriticals.mc.field_1724.method_23321(), accessor.recoin$getLastYaw(), accessor.recoin$getLastPitch(), false, false));
            return;
        }
        if (!event.isPre() || PacketCriticals.mc.field_1724 == null || PacketCriticals.mc.field_1687 == null || !((mineblaze2 = event.getEntity()) instanceof class_1309) || !(target = (class_1309)mineblaze2).method_5805() || this.lastAttackAge == PacketCriticals.mc.field_1724.field_6012 || PacketCriticals.mc.field_1724.method_7261(0.5f) < 0.9f || PacketCriticals.mc.field_1724.method_5624() || PacketCriticals.mc.field_1724.method_5799() || PacketCriticals.mc.field_1724.method_5771() || PacketCriticals.mc.field_1724.method_6101() || PacketCriticals.mc.field_1724.method_6128() || PacketCriticals.mc.field_1724.method_5765() || PacketCriticals.mc.field_1724.method_31549().field_7479) {
            return;
        }
        boolean web = PacketCriticals.mc.field_1687.method_8320(PacketCriticals.mc.field_1724.method_24515()).method_27852(class_2246.field_10343) || PacketCriticals.mc.field_1687.method_8320(PacketCriticals.mc.field_1724.method_24515().method_10084()).method_27852(class_2246.field_10343);
        boolean slowFalling = PacketCriticals.mc.field_1724.method_6059(class_1294.field_5906);
        if (!web && !slowFalling || !PacketCriticals.mc.field_1724.method_24828() && PacketCriticals.mc.field_1724.field_6017 > 0.0 || !PacketCriticals.mc.field_1724.method_56094((class_1297)target, 0.0)) {
            return;
        }
        double offset = web ? 0.003 : 0.001;
        class_243 pos = PacketCriticals.mc.field_1724.method_73189();
        if (!PacketCriticals.mc.field_1687.method_8587((class_1297)PacketCriticals.mc.field_1724, PacketCriticals.mc.field_1724.method_5829().method_989(0.0, offset, 0.0))) {
            return;
        }
        this.lastAttackAge = PacketCriticals.mc.field_1724.field_6012;
        PacketCriticals.mc.field_1724.field_3944.method_52787((class_2596)new class_2828.class_2829(pos.field_1352, pos.field_1351 + offset, pos.field_1350, false, PacketCriticals.mc.field_1724.field_5976));
        PacketCriticals.mc.field_1724.field_3944.method_52787((class_2596)new class_2828.class_2829(pos.field_1352, pos.field_1351, pos.field_1350, false, PacketCriticals.mc.field_1724.field_5976));
    }
}

