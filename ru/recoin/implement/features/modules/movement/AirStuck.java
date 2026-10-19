/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_243
 *  net.minecraft.class_2596
 *  net.minecraft.class_2828
 *  net.minecraft.class_2828$class_2829
 *  net.minecraft.class_2828$class_2830
 *  net.minecraft.class_2828$class_2831
 *  net.minecraft.class_2828$class_5911
 *  net.minecraft.class_2851
 *  net.minecraft.class_9836
 */
package ru.recoin.implement.features.modules.movement;

import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_2828;
import net.minecraft.class_2851;
import net.minecraft.class_9836;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.other.Instance;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.events.player.MoveEvent;

public final class AirStuck
extends Module {
    private final SelectSetting modeSetting = new SelectSetting("Mode", "").value("Normal", "Universal");
    private final BooleanSetting cancelPackets = new BooleanSetting("Cancel Packets", "Cancel movement packets completely").setValue(true).visible(() -> this.modeSetting.isSelected("Normal"));
    private final BooleanSetting fallCheck = new BooleanSetting("Fall Check", "Requires player to be in air").setValue(true).visible(() -> this.modeSetting.isSelected("Universal"));
    private transient class_243 savedVelocity = class_243.field_1353;
    private transient class_243 freezePosition = class_243.field_1353;
    private transient boolean frozen;

    public static AirStuck getInstance() {
        return Instance.get(AirStuck.class);
    }

    public boolean isFrozen() {
        return this.isState() && this.frozen;
    }

    public AirStuck() {
        super("AirStuck", "Air Stuck", ModuleCategory.MOVEMENT);
        this.setup(this.modeSetting, this.cancelPackets, this.fallCheck);
    }

    @EventHandler
    public void onMove(MoveEvent event) {
        if (AirStuck.mc.field_1724 == null) {
            return;
        }
        if (this.modeSetting.isSelected("Universal")) {
            if (this.fallCheck.isValue()) {
                if (!this.frozen && !AirStuck.mc.field_1724.method_24828() && (AirStuck.mc.field_1724.field_6017 > 0.0 || AirStuck.mc.field_1724.method_18798().field_1351 < 0.0)) {
                    this.freezePosition = AirStuck.mc.field_1724.method_73189();
                    this.frozen = true;
                }
                if (!this.frozen) {
                    return;
                }
            }
            event.setMovement(class_243.field_1353);
            AirStuck.mc.field_1724.method_18799(class_243.field_1353);
            return;
        }
        if (!this.frozen) {
            return;
        }
        event.setMovement(class_243.field_1353);
        AirStuck.mc.field_1724.method_5814(this.freezePosition.field_1352, this.freezePosition.field_1351, this.freezePosition.field_1350);
        AirStuck.mc.field_1724.method_18799(class_243.field_1353);
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        class_2596<?> class_25962;
        if (!this.isState() || AirStuck.mc.field_1724 == null) {
            return;
        }
        if (this.modeSetting.isSelected("Universal")) {
            if (this.fallCheck.isValue() && !this.frozen) {
                return;
            }
            if (event.isSend() && (event.getPacket() instanceof class_2828 || event.getPacket() instanceof class_2851 || event.getPacket() instanceof class_9836)) {
                event.setCancelled(true);
            }
        } else if (this.frozen && event.isSend() && (class_25962 = event.getPacket()) instanceof class_2828) {
            class_2828 packet = (class_2828)class_25962;
            event.cancel();
            if (this.modeSetting.isSelected("Normal") && !this.cancelPackets.isValue()) {
                PlayerIntersectionUtil.sendPacketWithOutEvent(this.createFrozenPacket(packet));
            }
        }
    }

    @Override
    public void activate() {
        super.activate();
        this.frozen = false;
        if (this.modeSetting.isSelected("Universal")) {
            if (AirStuck.mc.field_1724 != null) {
                this.savedVelocity = AirStuck.mc.field_1724.method_18798();
                if (!this.fallCheck.isValue() || !AirStuck.mc.field_1724.method_24828() && (AirStuck.mc.field_1724.field_6017 > 0.0 || AirStuck.mc.field_1724.method_18798().field_1351 < 0.0)) {
                    this.freezePosition = AirStuck.mc.field_1724.method_73189();
                    this.frozen = true;
                }
            }
            return;
        }
        if (AirStuck.mc.field_1724 != null && this.modeSetting.isSelected("Normal")) {
            this.freezePosition = AirStuck.mc.field_1724.method_73189();
            this.frozen = true;
        }
    }

    @Override
    public void deactivate() {
        super.deactivate();
        if (this.modeSetting.isSelected("Universal") && AirStuck.mc.field_1724 != null && this.savedVelocity != null) {
            AirStuck.mc.field_1724.method_18799(this.savedVelocity);
        }
        this.frozen = false;
    }

    private class_2828 createFrozenPacket(class_2828 packet) {
        boolean onGround = packet.method_12273();
        boolean horizontalCollision = packet.method_61225();
        if (packet.method_36171() && packet.method_36172()) {
            return new class_2828.class_2830(this.freezePosition.field_1352, this.freezePosition.field_1351, this.freezePosition.field_1350, packet.method_12271(AirStuck.mc.field_1724.method_36454()), packet.method_12270(AirStuck.mc.field_1724.method_36455()), onGround, horizontalCollision);
        }
        if (packet.method_36171()) {
            return new class_2828.class_2829(this.freezePosition.field_1352, this.freezePosition.field_1351, this.freezePosition.field_1350, onGround, horizontalCollision);
        }
        if (packet.method_36172()) {
            return new class_2828.class_2831(packet.method_12271(AirStuck.mc.field_1724.method_36454()), packet.method_12270(AirStuck.mc.field_1724.method_36455()), onGround, horizontalCollision);
        }
        return new class_2828.class_5911(onGround, horizontalCollision);
    }
}

