/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_1297$class_5529
 *  net.minecraft.class_2338
 *  net.minecraft.class_2350
 *  net.minecraft.class_2680
 *  net.minecraft.class_638
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.recoin.mixins;

import net.minecraft.class_1297;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2680;
import net.minecraft.class_638;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.api.event.EventManager;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.implement.events.player.EntityRemovalEvent;
import ru.recoin.implement.events.player.EntitySpawnEvent;
import ru.recoin.implement.events.render.WorldLoadEvent;
import ru.recoin.implement.features.modules.render.NoRender;

@Mixin(value={class_638.class})
public class ClientWorldMixin
implements QuickImports {
    @Inject(method={"method_2945"}, at={@At(value="HEAD")})
    private void recoin$entityRemoval(int id, class_1297.class_5529 reason, CallbackInfo ci) {
        class_1297 entity = ((class_638)(Object)this).method_8469(id);
        if (entity != null) {
            EventManager.callEvent(new EntityRemovalEvent(entity));
        }
    }

    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    public void initHook(CallbackInfo info) {
        EventManager.callEvent(new WorldLoadEvent());
    }

    @Inject(method={"method_53875"}, at={@At(value="HEAD")}, cancellable=true)
    public void addEntityHook(class_1297 entity, CallbackInfo ci) {
        if (PlayerIntersectionUtil.nullCheck()) {
            return;
        }
        EntitySpawnEvent event = new EntitySpawnEvent(entity);
        EventManager.callEvent(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method={"method_31595"}, at={@At(value="HEAD")}, cancellable=true)
    private void recoin$cancelBlockBreakParticles(class_2338 pos, class_2680 state, CallbackInfo ci) {
        NoRender noRender = NoRender.getInstance();
        if (noRender != null && noRender.isState() && noRender.modeSetting.isSelected("Block Breaking Particles")) {
            ci.cancel();
        }
    }

    @Inject(method={"method_74254"}, at={@At(value="HEAD")}, cancellable=true)
    private void recoin$cancelBlockBreakingParticle(class_2338 pos, class_2350 direction, CallbackInfo ci) {
        NoRender noRender = NoRender.getInstance();
        if (noRender != null && noRender.isState() && noRender.modeSetting.isSelected("Block Breaking Particles")) {
            ci.cancel();
        }
    }
}

