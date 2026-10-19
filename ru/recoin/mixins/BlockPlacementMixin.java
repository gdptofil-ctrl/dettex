/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1309
 *  net.minecraft.class_1799
 *  net.minecraft.class_1937
 *  net.minecraft.class_2246
 *  net.minecraft.class_2248
 *  net.minecraft.class_2338
 *  net.minecraft.class_2680
 *  net.minecraft.class_310
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.recoin.mixins;

import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.api.event.EventManager;
import ru.recoin.implement.events.block.PlaceBlockEvent;

@Mixin(value={class_2248.class})
public class BlockPlacementMixin {
    @Inject(method={"method_9567"}, at={@At(value="HEAD")})
    private void recoin$blockPlaced(class_1937 world, class_2338 pos, class_2680 state, class_1309 placer, class_1799 stack, CallbackInfo ci) {
        if (world.method_8608() && placer == class_310.method_1551().field_1724 && state.method_27852(class_2246.field_10540)) {
            EventManager.callEvent(new PlaceBlockEvent(state.method_26204(), pos.method_10062()));
        }
    }
}

