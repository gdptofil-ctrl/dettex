/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2338
 *  net.minecraft.class_2586
 *  net.minecraft.class_2591
 *  net.minecraft.class_2680
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.recoin.mixins;

import net.minecraft.class_2338;
import net.minecraft.class_2586;
import net.minecraft.class_2591;
import net.minecraft.class_2680;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.api.event.EventManager;
import ru.recoin.implement.events.block.BlockEntityProgressEvent;

@Mixin(value={class_2586.class})
public class BlockEntityMixin {
    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    public void initHook(class_2591<?> type, class_2338 pos, class_2680 state, CallbackInfo ci) {
        class_2586 blockEntity = (class_2586)(Object)this;
        if (blockEntity != null && EventManager.hasSubscribers(BlockEntityProgressEvent.class)) {
            EventManager.callEvent(new BlockEntityProgressEvent(blockEntity, BlockEntityProgressEvent.Type.ADD));
        }
    }

    @Inject(method={"method_11012"}, at={@At(value="HEAD")})
    private void markRemovedHook(CallbackInfo ci) {
        class_2586 blockEntity = (class_2586)(Object)this;
        if (blockEntity != null && EventManager.hasSubscribers(BlockEntityProgressEvent.class)) {
            EventManager.callEvent(new BlockEntityProgressEvent(blockEntity, BlockEntityProgressEvent.Type.REMOVE));
        }
    }
}

