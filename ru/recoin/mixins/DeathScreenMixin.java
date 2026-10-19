/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 *  net.minecraft.class_418
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.recoin.mixins;

import net.minecraft.class_332;
import net.minecraft.class_418;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.api.event.EventManager;
import ru.recoin.implement.events.player.DeathScreenEvent;

@Mixin(value={class_418.class})
public class DeathScreenMixin {
    @Shadow
    private int field_2451;

    @Inject(method={"method_25394"}, at={@At(value="HEAD")})
    public void render(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        EventManager.callEvent(new DeathScreenEvent(this.field_2451));
    }
}

