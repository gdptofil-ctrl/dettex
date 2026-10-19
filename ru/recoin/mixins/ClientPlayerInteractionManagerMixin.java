/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  com.llamalad7.mixinextras.sugar.Local
 *  net.minecraft.class_1268
 *  net.minecraft.class_1269
 *  net.minecraft.class_1269$class_9860
 *  net.minecraft.class_1269$class_9861
 *  net.minecraft.class_1297
 *  net.minecraft.class_1531
 *  net.minecraft.class_1533
 *  net.minecraft.class_1657
 *  net.minecraft.class_1713
 *  net.minecraft.class_1799
 *  net.minecraft.class_2199
 *  net.minecraft.class_2248
 *  net.minecraft.class_2281
 *  net.minecraft.class_2323
 *  net.minecraft.class_2336
 *  net.minecraft.class_2338
 *  net.minecraft.class_2349
 *  net.minecraft.class_2350
 *  net.minecraft.class_2377
 *  net.minecraft.class_2480
 *  net.minecraft.class_2531
 *  net.minecraft.class_2533
 *  net.minecraft.class_310
 *  net.minecraft.class_3865
 *  net.minecraft.class_3965
 *  net.minecraft.class_636
 *  net.minecraft.class_746
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.recoin.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.List;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1297;
import net.minecraft.class_1531;
import net.minecraft.class_1533;
import net.minecraft.class_1657;
import net.minecraft.class_1713;
import net.minecraft.class_1799;
import net.minecraft.class_2199;
import net.minecraft.class_2248;
import net.minecraft.class_2281;
import net.minecraft.class_2323;
import net.minecraft.class_2336;
import net.minecraft.class_2338;
import net.minecraft.class_2349;
import net.minecraft.class_2350;
import net.minecraft.class_2377;
import net.minecraft.class_2480;
import net.minecraft.class_2531;
import net.minecraft.class_2533;
import net.minecraft.class_310;
import net.minecraft.class_3865;
import net.minecraft.class_3965;
import net.minecraft.class_636;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.recoin.api.event.EventManager;
import ru.recoin.common.util.item.ItemUsage;
import ru.recoin.implement.events.block.BlockBreakingEvent;
import ru.recoin.implement.events.block.BreakBlockEvent;
import ru.recoin.implement.events.item.ClickSlotEvent;
import ru.recoin.implement.events.item.UsingItemEvent;
import ru.recoin.implement.events.player.AttackEvent;
import ru.recoin.implement.features.modules.combat.NoInteract;
import ru.recoin.implement.features.modules.movement.ScreenWalk;
import ru.recoin.implement.features.modules.player.AutoUse;
import ru.recoin.implement.features.modules.player.ItemHelper;

@Mixin(value={class_636.class})
public class ClientPlayerInteractionManagerMixin {
    @Shadow
    @Final
    private class_310 field_3712;

    @Inject(method={"method_2918"}, at={@At(value="HEAD")}, cancellable=true)
    public void attackEntityHook(class_1657 player, class_1297 target, CallbackInfo info) {
        AttackEvent event = new AttackEvent(target, true);
        EventManager.callEvent(event);
        if (event.isCancelled()) {
            info.cancel();
        }
    }

    @Inject(method={"method_2896"}, at={@At(value="HEAD")}, cancellable=true)
    private void interactBlockHook(class_746 player, class_1268 hand, class_3965 hitResult, CallbackInfoReturnable<class_1269> cir) {
        class_2248 block;
        if (NoInteract.getInstance().isState() && this.field_3712.field_1687 != null && ((block = this.field_3712.field_1687.method_8320(hitResult.method_17777()).method_26204()) instanceof class_2281 || block instanceof class_2336 || block instanceof class_2531 || block instanceof class_2480 || block instanceof class_2199 || block instanceof class_2323 || block instanceof class_2533 || block instanceof class_2349 || block instanceof class_3865 || block instanceof class_2377)) {
            cir.setReturnValue(class_1269.field_5811);
        }
    }

    @Inject(method={"method_2905"}, at={@At(value="HEAD")}, cancellable=true)
    private void interactEntityHook(class_1657 player, class_1297 entity, class_1268 hand, CallbackInfoReturnable<class_1269> cir) {
        if (NoInteract.getInstance().isState() && (entity instanceof class_1531 || entity instanceof class_1533)) {
            cir.setReturnValue(class_1269.field_5811);
        }
    }

    @Inject(method={"method_2918"}, at={@At(value="RETURN")})
    public void attackEntityPostHook(class_1657 player, class_1297 target, CallbackInfo info) {
        EventManager.callEvent(new AttackEvent(target, false));
    }

    @Inject(method={"method_2919"}, at={@At(value="RETURN")})
    public void interactItemHook(class_1657 player, class_1268 hand, CallbackInfoReturnable<class_1269> cir) {
        class_1269.class_9860 success;
        Object object = cir.getReturnValue();
        if (object instanceof class_1269.class_9860 && !(success = (class_1269.class_9860)object).comp_2909().equals((Object)class_1269.class_9861.field_52427)) {
            UsingItemEvent event = new UsingItemEvent((byte)0);
            EventManager.callEvent(event);
        }
    }

    @Inject(method={"method_2897"}, at={@At(value="HEAD")}, cancellable=true)
    public void stopUsingItemHook(CallbackInfo ci) {
        UsingItemEvent event = new UsingItemEvent((byte)2);
        EventManager.callEvent(event);
        if (AutoUse.getInstance().isActivelyUsing() || ItemHelper.getInstance().isActivelyUsing()) {
            ci.cancel();
            return;
        }
        if (ItemUsage.INSTANCE.isUseItem()) {
            ItemUsage.INSTANCE.setUseItem(false);
            ci.cancel();
        }
    }

    @Inject(method={"method_2919"}, at={@At(value="HEAD")}, cancellable=true)
    private void gameModeHook(class_1657 player, class_1268 hand, CallbackInfoReturnable<class_1269> cir) {
        UsingItemEvent event = new UsingItemEvent((byte)-1);
        EventManager.callEvent(event);
        if (event.isCancelled()) {
            cir.setReturnValue(class_1269.field_5811);
        }
    }

    @Inject(method={"method_2906"}, at={@At(value="HEAD")}, cancellable=true)
    public void clickSlotHook(int syncId, int slotId, int button, class_1713 actionType, class_1657 player, CallbackInfo info) {
        ClickSlotEvent event = new ClickSlotEvent(syncId, slotId, button, actionType);
        EventManager.callEvent(event);
        if (event.isCancelled()) {
            info.cancel();
        }
    }

    @ModifyExpressionValue(method={"method_2906"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_1703;method_34255()Lnet/minecraft/class_1799;")})
    private class_1799 recoin$screenWalkCursorStack(class_1799 original, @Local(ordinal=1, argsOnly=true) int slotId, @Local List<class_1799> stacks, @Local(argsOnly=true) class_1713 actionType) {
        ScreenWalk screenWalk = ScreenWalk.getInstance();
        return slotId < 0 || slotId >= stacks.size() || screenWalk == null || !screenWalk.isState() || screenWalk.getBypass().isSelected("Reallyworld") || screenWalk.isExternalInventorySwapSyncing() || actionType != class_1713.field_7790 && actionType != class_1713.field_7793 ? original : stacks.get(slotId);
    }

    @Inject(method={"method_2910"}, at={@At(value="HEAD")}, cancellable=true)
    private void injectAttackBlock(class_2338 pos, class_2350 direction, CallbackInfoReturnable<Boolean> cir) {
        BlockBreakingEvent event = new BlockBreakingEvent(pos, direction);
        EventManager.callEvent(event);
        if (event.isCancelled()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method={"method_2902"}, at={@At(value="HEAD")}, cancellable=true)
    private void injectBlockBreaking(class_2338 pos, class_2350 direction, CallbackInfoReturnable<Boolean> cir) {
        BlockBreakingEvent event = new BlockBreakingEvent(pos, direction);
        EventManager.callEvent(event);
        if (event.isCancelled()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method={"method_2899"}, at={@At(value="RETURN")})
    private void injectBreakBlock(class_2338 pos, CallbackInfoReturnable<Boolean> cir) {
        EventManager.callEvent(new BreakBlockEvent(pos));
    }
}

