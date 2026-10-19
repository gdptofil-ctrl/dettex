/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.channel.ChannelFutureListener
 *  net.minecraft.class_2535
 *  net.minecraft.class_2547
 *  net.minecraft.class_2596
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.recoin.mixins;

import io.netty.channel.ChannelFutureListener;
import net.minecraft.class_2535;
import net.minecraft.class_2547;
import net.minecraft.class_2596;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.api.event.EventManager;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.features.modules.player.GodModePacketBlocker;

@Mixin(value={class_2535.class})
public class ClientConnectionMixin {
    @Inject(method={"method_10759"}, at={@At(value="HEAD")}, cancellable=true)
    private static <T extends class_2547> void handlePacketPre(class_2596<T> packet, class_2547 listener, CallbackInfo info) {
        PacketEvent packetEvent = new PacketEvent(packet, PacketEvent.Type.RECEIVE);
        EventManager.callEvent(packetEvent);
        if (packetEvent.isCancelled()) {
            info.cancel();
        }
    }

    @Inject(method={"method_10743(Lnet/minecraft/class_2596;)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void sendPre(class_2596<?> packet, CallbackInfo info) {
        PacketEvent packetEvent = new PacketEvent(packet, PacketEvent.Type.SEND);
        EventManager.callEvent(packetEvent);
        if (packetEvent.isCancelled()) {
            info.cancel();
        }
    }

    @Inject(method={"method_52906(Lnet/minecraft/class_2596;Lio/netty/channel/ChannelFutureListener;Z)V", "method_10764(Lnet/minecraft/class_2596;Lio/netty/channel/ChannelFutureListener;Z)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void recoin$blockGodModeMovement(class_2596<?> packet, ChannelFutureListener listener, boolean flush, CallbackInfo info) {
        if (GodModePacketBlocker.isBlocked((class_2535)(Object)this, packet)) {
            info.cancel();
        }
    }
}

