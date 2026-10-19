/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2596
 *  net.minecraft.class_2848
 *  net.minecraft.class_2848$class_2849
 *  net.minecraft.class_2868
 */
package ru.recoin.core.listener.impl;

import java.util.List;
import java.util.Objects;
import net.minecraft.class_2596;
import net.minecraft.class_2848;
import net.minecraft.class_2868;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.draggable.AbstractDraggable;
import ru.recoin.common.util.entity.PlayerInventoryComponent;
import ru.recoin.common.util.world.ServerUtil;
import ru.recoin.core.Main;
import ru.recoin.core.listener.Listener;
import ru.recoin.implement.events.item.UsingItemEvent;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.events.player.AttackEvent;
import ru.recoin.implement.events.player.GlobalTickEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.features.modules.combat.killaura.rotation.ai.NeuroRotation;

public class EventListener
implements Listener {
    public static boolean serverSprint;
    public static int selectedSlot;

    @EventHandler
    public void onTick(TickEvent e) {
        ServerUtil.tick();
        NeuroRotation.tickRecording();
        PlayerInventoryComponent.tick();
        List<AbstractDraggable> draggables = Main.getInstance().getDraggableRepository().draggable();
        for (int i = 0; i < draggables.size(); ++i) {
            draggables.get(i).tick();
        }
    }

    @EventHandler
    public void onGlobalTick(GlobalTickEvent e) {
        Main.getInstance().getAttackPerpetrator().tick();
    }

    @EventHandler
    public void onAttack(AttackEvent event) {
        NeuroRotation.onAttack(event);
        List<AbstractDraggable> draggables = Main.getInstance().getDraggableRepository().draggable();
        for (int i = 0; i < draggables.size(); ++i) {
            draggables.get(i).attack(event);
        }
    }

    @EventHandler
    public void onPacket(PacketEvent e) {
        class_2596<?> class_25962 = e.getPacket();
        Objects.requireNonNull(class_25962);
        class_2596<?> class_25963 = class_25962;
        if (class_25963 instanceof class_2848 command) {
            serverSprint = switch (command.method_12365()) {
                case class_2848.class_2849.field_12981 -> true;
                case class_2848.class_2849.field_12985 -> false;
                default -> serverSprint;
            };
        } else if (class_25963 instanceof class_2868 slot) {
            selectedSlot = slot.method_12442();
        }
        ServerUtil.packet(e);
        Main.getInstance().getAttackPerpetrator().onPacket(e);
        List<AbstractDraggable> draggables = Main.getInstance().getDraggableRepository().draggable();
        for (int i = 0; i < draggables.size(); ++i) {
            draggables.get(i).packet(e);
        }
    }

    @EventHandler
    public void onUsingItemEvent(UsingItemEvent e) {
        Main.getInstance().getAttackPerpetrator().onUsingItem(e);
    }
}

