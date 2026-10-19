/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.class_2246
 *  net.minecraft.class_2666
 *  net.minecraft.class_2672
 *  net.minecraft.class_2818
 *  net.minecraft.class_3414
 *  net.minecraft.class_3417
 *  net.minecraft.class_3419
 *  net.minecraft.class_634
 *  net.minecraft.class_638
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.recoin.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.class_2246;
import net.minecraft.class_2666;
import net.minecraft.class_2672;
import net.minecraft.class_2818;
import net.minecraft.class_3414;
import net.minecraft.class_3417;
import net.minecraft.class_3419;
import net.minecraft.class_634;
import net.minecraft.class_638;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.api.event.EventManager;
import ru.recoin.api.system.sound.SoundManager;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.other.Instance;
import ru.recoin.core.Main;
import ru.recoin.implement.events.block.BlockUpdateEvent;
import ru.recoin.implement.events.chat.ChatCommandEvent;
import ru.recoin.implement.events.chat.ChatEvent;
import ru.recoin.implement.features.modules.render.XRay;

@Mixin(value={class_634.class})
public class ClientPlayNetworkHandlerMixin
implements QuickImports {
    @Inject(method={"method_11128"}, at={@At(value="RETURN")})
    private void onChunkDataHook(class_2672 packet, CallbackInfo ci) {
        if (ClientPlayNetworkHandlerMixin.mc.field_1687 == null) {
            return;
        }
        if (Main.getInstance().getBoxESPRepository().blocks.isEmpty()) {
            return;
        }
        this.scanChunk(ClientPlayNetworkHandlerMixin.mc.field_1687.method_8497(packet.method_11523(), packet.method_11524()), BlockUpdateEvent.Type.LOAD);
    }

    @Inject(method={"method_11107"}, at={@At(value="HEAD")})
    private void onUnloadChunkHook(class_2666 packet, CallbackInfo ci) {
        if (ClientPlayNetworkHandlerMixin.mc.field_1687 == null) {
            return;
        }
        Main.getInstance().getBoxESPRepository().removeChunk(packet.comp_1726().field_9181, packet.comp_1726().field_9180);
        XRay xray = Instance.get(XRay.class);
        if (xray != null) {
            xray.removeChunk(packet.comp_1726().field_9181, packet.comp_1726().field_9180);
        }
    }

    @Inject(method={"method_45729(Ljava/lang/String;)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void sendChatMessage(String string, CallbackInfo ci) {
        ChatEvent event = new ChatEvent(string);
        EventManager.callEvent(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @WrapOperation(method={"method_11148"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_638;method_8486(DDDLnet/minecraft/class_3414;Lnet/minecraft/class_3419;FFZ)V")})
    private void recoin$replaceTotemSound(class_638 world, double x, double y, double z, class_3414 sound, class_3419 category, float volume, float pitch, boolean useDistance, Operation<Void> original) {
        if (sound == class_3417.field_14931 && SoundManager.shouldReplaceCustomTotemSound()) {
            return;
        }
        original.call(new Object[]{world, x, y, z, sound, category, Float.valueOf(volume), Float.valueOf(pitch), useDistance});
    }

    @WrapMethod(method={"method_45730(Ljava/lang/String;)V"})
    private void recoin$sendChatCommand(String command, Operation<Void> original) {
        ChatCommandEvent event = new ChatCommandEvent(command);
        EventManager.callEvent(event);
        if (!event.isCancelled()) {
            original.call(new Object[]{event.getCommand()});
        }
    }

    @Unique
    private void scanChunk(class_2818 worldChunk, BlockUpdateEvent.Type type) {
        worldChunk.method_51525(state -> Main.getInstance().getBoxESPRepository().blocks.containsKey(state.method_26204()), (pos, state) -> EventManager.callEvent(new BlockUpdateEvent(type == BlockUpdateEvent.Type.LOAD ? state : class_2246.field_10124.method_9564(), pos.method_10062(), type)));
    }
}

