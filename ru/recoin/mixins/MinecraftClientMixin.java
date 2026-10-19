/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_1041
 *  net.minecraft.class_1268
 *  net.minecraft.class_1309
 *  net.minecraft.class_155
 *  net.minecraft.class_1657
 *  net.minecraft.class_1764
 *  net.minecraft.class_1799
 *  net.minecraft.class_1835
 *  net.minecraft.class_310
 *  net.minecraft.class_3262
 *  net.minecraft.class_437
 *  net.minecraft.class_542
 *  net.minecraft.class_634
 *  net.minecraft.class_636
 *  net.minecraft.class_746
 *  net.minecraft.class_757
 *  net.minecraft.class_8518
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.recoin.mixins;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1041;
import net.minecraft.class_1268;
import net.minecraft.class_1309;
import net.minecraft.class_155;
import net.minecraft.class_1657;
import net.minecraft.class_1764;
import net.minecraft.class_1799;
import net.minecraft.class_1835;
import net.minecraft.class_310;
import net.minecraft.class_3262;
import net.minecraft.class_437;
import net.minecraft.class_542;
import net.minecraft.class_634;
import net.minecraft.class_636;
import net.minecraft.class_746;
import net.minecraft.class_757;
import net.minecraft.class_8518;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.recoin.api.event.EventManager;
import ru.recoin.api.feature.draggable.AbstractDraggable;
import ru.recoin.api.file.exception.FileProcessingException;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.entity.PlayerInventoryComponent;
import ru.recoin.common.util.logger.LoggerUtil;
import ru.recoin.common.util.render.WindowResizeCallback;
import ru.recoin.common.util.render.WindowsTitleBar;
import ru.recoin.common.util.world.ServerUtil;
import ru.recoin.core.Main;
import ru.recoin.implement.events.container.SetScreenEvent;
import ru.recoin.implement.events.player.GlobalTickEvent;
import ru.recoin.implement.events.player.HotBarUpdateEvent;
import ru.recoin.implement.features.modules.combat.SpearBoost;
import ru.recoin.implement.features.modules.misc.LockSlot;
import ru.recoin.implement.features.modules.player.AutoUse;
import ru.recoin.implement.features.modules.player.ItemHelper;
import ru.recoin.implement.features.modules.player.OpenWalls;
import ru.recoin.implement.features.modules.render.UiAnimationManager;
import ru.recoin.implement.screens.menu.MenuScreen;

@Environment(value=EnvType.CLIENT)
@Mixin(value={class_310.class})
public abstract class MinecraftClientMixin
implements QuickImports {
    private int recoin$lastScaledWidth;
    private int recoin$lastScaledHeight;
    @Shadow
    @Nullable
    public class_636 field_1761;
    @Shadow
    @Nullable
    public class_746 field_1724;
    @Shadow
    @Final
    public class_757 field_1773;
    @Shadow
    @Nullable
    public class_437 field_1755;
    @Shadow
    @Final
    private class_1041 field_1704;

    @Inject(method={"method_1536"}, at={@At(value="HEAD")}, cancellable=true)
    private void recoin$spearAttributeSwap(CallbackInfoReturnable<Boolean> cir) {
        SpearBoost boost = SpearBoost.getInstance();
        if (boost != null && boost.handleAttackClick()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method={"method_1583"}, at={@At(value="HEAD")}, cancellable=true)
    private void recoin$protectAutomatedItemTransaction(CallbackInfo ci) {
        if (PlayerInventoryComponent.isInventoryActionBusy()) {
            ci.cancel();
        }
    }

    @Shadow
    @Nullable
    public abstract class_634 method_1562();

    @Inject(at={@At(value="TAIL")}, method={"<init>"})
    private void onInit(class_542 args2, CallbackInfo ci) {
        Fonts.init();
        this.recoin$lastScaledWidth = this.field_1704.method_4486();
        this.recoin$lastScaledHeight = this.field_1704.method_4502();
        WindowsTitleBar.applyDarkTitleBar(this.field_1704.method_4490());
    }

    @Inject(method={"method_1523"}, at={@At(value="HEAD")})
    private void recoin$ensureDarkTitleBar(boolean tick, CallbackInfo ci) {
        if (!WindowsTitleBar.isApplied()) {
            WindowsTitleBar.applyDarkTitleBar(this.field_1704.method_4490());
        }
    }

    @Inject(method={"method_1574"}, at={@At(value="HEAD")})
    private void recoin$globalTick(CallbackInfo ci) {
        if (this.field_1724 != null && MinecraftClientMixin.mc.field_1687 != null) {
            EventManager.callEvent(new GlobalTickEvent());
        }
    }

    @Inject(method={"method_15993"}, at={@At(value="TAIL")})
    private void onResolutionChangedHook(CallbackInfo ci) {
        WindowsTitleBar.applyDarkTitleBar(this.field_1704.method_4490());
        if (!Main.getInstance().isInitialized()) {
            return;
        }
        for (AbstractDraggable draggable : Main.getInstance().getDraggableRepository().draggable()) {
            draggable.validPosition();
        }
        this.recoin$lastScaledWidth = this.field_1704.method_4486();
        this.recoin$lastScaledHeight = this.field_1704.method_4502();
        ((WindowResizeCallback)WindowResizeCallback.EVENT.invoker()).onResized((class_310)(Object)this, this.field_1704);
    }

    @Inject(at={@At(value="HEAD")}, method={"method_1490"})
    private void stop(CallbackInfo ci) {
        LoggerUtil.info("Stopping for MinecraftClient");
        if (Main.getInstance().isInitialized()) {
            try {
                Main.getInstance().getFileController().saveFiles();
            }
            catch (FileProcessingException e) {
                LoggerUtil.error("Error occurred while saving files: " + e.getMessage() + " " + String.valueOf(e.getCause()));
            }
            finally {
                Main.getInstance().getFileController().stopAutoSave();
            }
        }
    }

    @Inject(at={@At(value="TAIL")}, method={"method_1490"})
    private void stopTail(CallbackInfo ci) {
        System.exit(0);
    }

    @Inject(method={"method_1583"}, at={@At(value="HEAD")}, cancellable=true)
    private void recoin$crossbowCooldownCheck(CallbackInfo ci) {
        if (this.field_1724 != null && ServerUtil.isSpookyTime()) {
            class_1799 mainStack = this.field_1724.method_5998(class_1268.field_5808);
            if (mainStack.method_7909() instanceof class_1764 && this.field_1724.method_7357().method_7904(mainStack)) {
                ci.cancel();
                return;
            }
            if (mainStack.method_7909() instanceof class_1835 && this.field_1724.method_7357().method_7904(mainStack)) {
                ci.cancel();
                return;
            }
            class_1799 offStack = this.field_1724.method_5998(class_1268.field_5810);
            if (offStack.method_7909() instanceof class_1764 && this.field_1724.method_7357().method_7904(offStack)) {
                ci.cancel();
                return;
            }
        }
    }

    @Inject(method={"method_1583"}, at={@At(value="HEAD")}, cancellable=true)
    private void recoin$openContainerThroughWalls(CallbackInfo ci) {
        OpenWalls openWalls = OpenWalls.getInstance();
        if (openWalls != null && openWalls.tryOpen()) {
            ci.cancel();
        }
    }

    @Inject(method={"method_1507"}, at={@At(value="HEAD")}, cancellable=true)
    public void setScreenHook(class_437 screen, CallbackInfo ci) {
        class_437 class_4372 = this.field_1755;
        if (class_4372 instanceof MenuScreen) {
            MenuScreen menuScreen = (MenuScreen)class_4372;
            if (screen == null && !menuScreen.isClosing()) {
                ci.cancel();
                return;
            }
        }
        if (screen instanceof MenuScreen) {
            return;
        }
        if (screen != this.field_1755) {
            if (screen == null) {
                UiAnimationManager.onScreenClosed();
            } else {
                UiAnimationManager.onScreenOpened(screen);
            }
        }
        SetScreenEvent event = new SetScreenEvent(screen);
        EventManager.callEvent(event);
        Main.getInstance().getDraggableRepository().draggable().forEach(drag -> drag.setScreen(event));
        class_437 eventScreen = event.getScreen();
        if (screen != eventScreen) {
            mc.method_1507(eventScreen);
            ci.cancel();
        }
    }

    @Redirect(method={"<init>"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_1041;method_4491(Lnet/minecraft/class_3262;Lnet/minecraft/class_8518;)V"))
    private void onChangeIcon(class_1041 instance, class_3262 resourcePack, class_8518 icons) throws IOException {
        instance.method_4491(resourcePack, icons);
    }

    @Inject(method={"method_24287"}, at={@At(value="HEAD")}, cancellable=true)
    private void onWindowTitle(CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(("Minecraft " + class_155.method_16673().comp_4025()));
    }

    @Inject(method={"method_1508"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_746;method_31548()Lnet/minecraft/class_1661;")}, cancellable=true)
    public void handleInputEventsHook(CallbackInfo ci) {
        HotBarUpdateEvent event = new HotBarUpdateEvent();
        EventManager.callEvent(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Redirect(method={"method_1508"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_746;method_7290(Z)Z"))
    private boolean handleDropSelectedItem(class_746 player, boolean entireStack) {
        if (LockSlot.getInstance().shouldCancelSelectedSlotDrop(player.method_31548().method_67532())) {
            return false;
        }
        return player.method_7290(entireStack);
    }

    @Redirect(method={"method_1508"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_636;method_2897(Lnet/minecraft/class_1657;)V"))
    private void redirectStopUsingItem(class_636 manager, class_1657 player) {
        class_1799 stack;
        if (AutoUse.getInstance().isActivelyUsing() || ItemHelper.getInstance().isActivelyUsing()) {
            return;
        }
        if (player.method_6115() && (stack = player.method_6030()).method_7909() instanceof class_1764 && stack.method_7935((class_1309)player) - player.method_6014() <= class_1764.method_7775((class_1799)stack, (class_1309)player)) {
            return;
        }
        manager.method_2897(player);
    }
}

