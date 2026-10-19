/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10774
 *  net.minecraft.class_1297
 *  net.minecraft.class_1937
 *  net.minecraft.class_2248
 *  net.minecraft.class_2338
 *  net.minecraft.class_2464
 *  net.minecraft.class_4970$class_4971
 *  net.minecraft.class_5703
 *  net.minecraft.class_7130
 *  net.minecraft.class_7268
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.recoin.mixins;

import net.minecraft.class_10774;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2464;
import net.minecraft.class_4970;
import net.minecraft.class_5703;
import net.minecraft.class_7130;
import net.minecraft.class_7268;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.recoin.api.event.EventManager;
import ru.recoin.common.QuickImports;
import ru.recoin.implement.events.player.PlayerCollisionEvent;
import ru.recoin.implement.features.modules.render.NoRender;

@Mixin(value={class_4970.class_4971.class})
public abstract class AbstractBlockStateMixin
implements QuickImports {
    @Shadow
    public abstract class_2248 method_26204();

    @Inject(method={"method_26217"}, at={@At(value="HEAD")}, cancellable=true)
    private void recoin$hideSculk(CallbackInfoReturnable<class_2464> cir) {
        class_2248 block = this.method_26204();
        if (!(block instanceof class_7130 || block instanceof class_7268 || block instanceof class_5703)) {
            return;
        }
        NoRender noRender = NoRender.getInstance();
        if (noRender != null && noRender.hidesSculk(block)) {
            cir.setReturnValue(class_2464.field_11455);
        }
    }

    @Inject(method={"method_26178"}, at={@At(value="HEAD")}, cancellable=true)
    public void onEntityCollision(class_1937 world, class_2338 pos, class_1297 entity, class_10774 handler, boolean bl, CallbackInfo ci) {
        if (entity != AbstractBlockStateMixin.mc.field_1724 || !EventManager.hasSubscribers(PlayerCollisionEvent.class)) {
            return;
        }
        PlayerCollisionEvent event = new PlayerCollisionEvent(this.method_26204());
        EventManager.callEvent(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}

