/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1937
 *  net.minecraft.class_2338
 *  net.minecraft.class_2680
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.recoin.mixins;

import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.api.event.EventManager;
import ru.recoin.common.QuickImports;
import ru.recoin.implement.events.block.BlockUpdateEvent;

@Mixin(value={class_1937.class})
public abstract class WorldMixin
implements QuickImports {
    @Inject(method={"method_8413"}, at={@At(value="RETURN")}, require=0)
    private void onBlockChangedHook(class_2338 pos, class_2680 oldState, class_2680 newState, int flags, CallbackInfo ci) {
        if (WorldMixin.mc.field_1687 != (Object)this) {
            return;
        }
        EventManager.callEvent(new BlockUpdateEvent(newState, pos.method_10062(), BlockUpdateEvent.Type.UPDATE));
    }
}

