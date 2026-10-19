/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1922
 *  net.minecraft.class_2338
 *  net.minecraft.class_2680
 *  net.minecraft.class_5329
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package ru.recoin.mixins;

import net.minecraft.class_1922;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_5329;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.recoin.api.event.EventManager;
import ru.recoin.implement.events.block.BlockCollisionEvent;

@Mixin(value={class_5329.class})
public abstract class BlockCollisionSpliteratorMixin {
    @Redirect(method={"computeNext"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_1922;method_8320(Lnet/minecraft/class_2338;)Lnet/minecraft/class_2680;"))
    private class_2680 computeNext(class_1922 instance, class_2338 blockPos) {
        if (!EventManager.hasSubscribers(BlockCollisionEvent.class)) {
            return instance.method_8320(blockPos);
        }
        BlockCollisionEvent event = new BlockCollisionEvent(blockPos, instance.method_8320(blockPos));
        EventManager.callEvent(event);
        return event.state();
    }
}

