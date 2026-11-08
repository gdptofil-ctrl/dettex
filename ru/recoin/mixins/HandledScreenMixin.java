/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 *  net.minecraft.class_1657
 *  net.minecraft.class_1713
 *  net.minecraft.class_1735
 *  net.minecraft.class_1799
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_4185
 *  net.minecraft.class_437
 *  net.minecraft.class_465
 *  net.minecraft.class_490
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.recoin.mixins;

import net.minecraft.class_124;
import net.minecraft.class_1657;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_465;
import net.minecraft.class_490;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.api.accessor.ContainerButtonHost;
import ru.recoin.api.event.EventManager;
import ru.recoin.implement.events.container.DrawSlotsEvent;
import ru.recoin.implement.events.container.HandledScreenEvent;
import ru.recoin.implement.features.modules.misc.Unhook;
import ru.recoin.implement.features.modules.player.AutoBuy;
import ru.recoin.implement.features.modules.render.UiAnimationManager;
import ru.recoin.implement.screens.autobuy.AuctionControlOverlay;
import ru.recoin.implement.screens.autobuy.ParserDiscountSlider;
import ru.recoin.mixins.HandledScreenAccessor;
import ru.recoin.mixins.ScreenWidgetAccessor;

@Mixin(value={class_465.class})
public abstract class HandledScreenMixin
implements ContainerButtonHost {
    @Unique
    private static final int RECOIN$BUTTON_WIDTH = 96;
    @Unique
    private static final int RECOIN$BUTTON_HEIGHT = 20;
    @Unique
    private static final int RECOIN$BUTTON_GAP = 4;
    @Unique
    private boolean recoin$backgroundAnimationTransform;
    @Unique
    private boolean recoin$foregroundAnimationTransform;
    @Unique
    private class_4185 recoin$depositAllButton;
    @Unique
    private class_4185 recoin$takeAllButton;
    @Unique
    private class_4185 recoin$throwAllButton;
    @Unique
    private class_4185 recoin$inventoryThrowButton;
    @Unique
    private class_4185 recoin$autoBuyToggleButton;
    @Unique
    private class_4185 recoin$autoBuyParseButton;
    @Unique
    private ParserDiscountSlider recoin$parserDiscountSlider;

    @Inject(method={"method_25420"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_465;method_2389(Lnet/minecraft/class_332;FII)V")})
    private void recoin$beginCustomBackground(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        this.recoin$backgroundAnimationTransform = UiAnimationManager.beginInventoryRender(context, (class_437)(Object)this);
    }

    @Inject(method={"method_25420"}, at={@At(value="TAIL")})
    private void recoin$endCustomBackground(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.recoin$backgroundAnimationTransform) {
            UiAnimationManager.endInventoryRender(context);
            this.recoin$backgroundAnimationTransform = false;
        }
    }

    @Inject(method={"method_71085"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_437;method_25394(Lnet/minecraft/class_332;IIF)V")})
    private void recoin$beginCustomForeground(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        this.recoin$updateContainerButtonStates();
        this.recoin$foregroundAnimationTransform = UiAnimationManager.beginInventoryRender(context, (class_437)(Object)this);
    }

    @Inject(method={"method_71085"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_465;method_64508(Lnet/minecraft/class_332;II)V")})
    private void recoin$beforeDrawSlots(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        class_310 client = class_310.method_1551();
        if (client.field_1724 != null && !Unhook.isLocked()) {
            EventManager.callEvent(new DrawSlotsEvent(context, (class_465)(Object)this));
        }
    }

    @Inject(method={"method_71085"}, at={@At(value="TAIL")})
    private void recoin$endCustomForeground(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        class_310 client = class_310.method_1551();
        if (client.field_1724 != null && !Unhook.isLocked()) {
            HandledScreenAccessor accessor = (HandledScreenAccessor)((Object)this);
            class_1735 hoverSlot = accessor.recoin$getSlotAt(mouseX, mouseY);
            EventManager.callEvent(new HandledScreenEvent(context, hoverSlot, accessor.recoin$getBackgroundWidth(), accessor.recoin$getBackgroundHeight(), mouseX, mouseY));
        }
        if (this.recoin$foregroundAnimationTransform) {
            UiAnimationManager.endInventoryRender(context);
            this.recoin$foregroundAnimationTransform = false;
        }
        AutoBuy autoBuy = AutoBuy.getInstance();
        if (client.field_1724 != null && !Unhook.isLocked() && autoBuy != null && autoBuy.state && autoBuy.isAuctionScreen((class_465)(Object)this)) {
            AuctionControlOverlay.renderHistory(context, autoBuy, ((HandledScreenAccessor)((Object)this)).recoin$getX(), ((HandledScreenAccessor)((Object)this)).recoin$getY());
        }
    }

    @Inject(method={"method_25419"}, at={@At(value="HEAD")}, cancellable=true)
    private void recoin$animateCustomClose(CallbackInfo ci) {
        if (UiAnimationManager.requestClose((class_437)(Object)this)) {
            ci.cancel();
        }
    }

    @Inject(method={"method_25426"}, at={@At(value="TAIL")})
    private void recoin$addContainerButtons(CallbackInfo ci) {
        class_465 screen = (class_465)(Object)this;
        ScreenWidgetAccessor widgets = (ScreenWidgetAccessor)((Object)this);
        HandledScreenAccessor accessor = (HandledScreenAccessor)((Object)this);
        if ((Object)this instanceof class_490) {
            int x = Math.round((float)accessor.recoin$getX() + (float)(accessor.recoin$getBackgroundWidth() - 96) / 2.0f);
            int y = Math.round(accessor.recoin$getY() - 20 - 4);
            this.recoin$inventoryThrowButton = widgets.recoin$addDrawableChild(class_4185.method_46430((class_2561)class_2561.method_43470((String)"\u0412\u044b\u0431\u0440\u043e\u0441\u0438\u0442\u044c \u0432\u0441\u0451"), button -> this.recoin$throwSlots(screen, true)).method_46434(x, y, 96, 20).method_46431());
        } else {
            int x = accessor.recoin$getX() + accessor.recoin$getBackgroundWidth() + 4;
            int y = accessor.recoin$getY() + 4;
            this.recoin$depositAllButton = widgets.recoin$addDrawableChild(class_4185.method_46430((class_2561)class_2561.method_43470((String)"\u0421\u043b\u043e\u0436\u0438\u0442\u044c \u0432\u0441\u0451"), button -> this.recoin$quickMove(screen, true)).method_46434(x, y, 96, 20).method_46431());
            this.recoin$takeAllButton = widgets.recoin$addDrawableChild(class_4185.method_46430((class_2561)class_2561.method_43470((String)"\u0412\u0437\u044f\u0442\u044c \u0432\u0441\u0451"), button -> this.recoin$quickMove(screen, false)).method_46434(x, y + 20 + 4, 96, 20).method_46431());
            this.recoin$throwAllButton = widgets.recoin$addDrawableChild(class_4185.method_46430((class_2561)class_2561.method_43470((String)"\u0412\u044b\u0431\u0440\u043e\u0441\u0438\u0442\u044c \u0432\u0441\u0451"), button -> this.recoin$throwSlots(screen, false)).method_46434(x, y + 48, 96, 20).method_46431());
            this.recoin$autoBuyToggleButton = widgets.recoin$addDrawableChild(class_4185.method_46430((class_2561)class_2561.method_43470((String)"AUTOBUY: OFF"), button -> {
                AutoBuy autoBuy = AutoBuy.getInstance();
                if (autoBuy != null) {
                    autoBuy.toggleAuctionEnabled();
                }
            }).method_46434(x, y + 72, 96, 20).method_46431());
            this.recoin$autoBuyParseButton = widgets.recoin$addDrawableChild(class_4185.method_46430((class_2561)class_2561.method_43470((String)"PARSE"), button -> {
                AutoBuy autoBuy = AutoBuy.getInstance();
                if (autoBuy != null) {
                    autoBuy.requestPriceParse();
                }
            }).method_46434(x, y + 96, 96, 20).method_46431());
            AutoBuy autoBuy = AutoBuy.getInstance();
            if (autoBuy != null) {
                this.recoin$parserDiscountSlider = widgets.recoin$addDrawableChild(new ParserDiscountSlider(x, y + 120, 96, 20, autoBuy));
            }
        }
        this.recoin$updateContainerButtonStates();
    }

    @Override
    public void recoin$updateContainerButtonStates() {
        boolean auctionControls;
        boolean visible;
        class_310 client = class_310.method_1551();
        if (client.field_1724 == null) {
            return;
        }
        class_465 screen = (class_465)(Object)this;
        HandledScreenAccessor accessor = (HandledScreenAccessor)((Object)this);
        boolean bl = visible = !Unhook.isLocked();
        if ((Object)this instanceof class_490) {
            if (this.recoin$inventoryThrowButton != null) {
                this.recoin$inventoryThrowButton.method_46421(Math.round((float)accessor.recoin$getX() + (float)(accessor.recoin$getBackgroundWidth() - 96) / 2.0f));
                this.recoin$inventoryThrowButton.method_46419(Math.round(accessor.recoin$getY() - 20 - 4));
                this.recoin$inventoryThrowButton.field_22764 = visible;
                this.recoin$inventoryThrowButton.field_22763 = visible && this.recoin$hasSourceStack(screen, true);
            }
            return;
        }
        int x = accessor.recoin$getX() + accessor.recoin$getBackgroundWidth() + 4;
        int y = accessor.recoin$getY() + 4;
        if (this.recoin$depositAllButton != null) {
            this.recoin$depositAllButton.method_46421(x);
            this.recoin$depositAllButton.method_46419(y);
            this.recoin$depositAllButton.field_22764 = visible;
            boolean bl2 = this.recoin$depositAllButton.field_22763 = visible && this.recoin$canQuickMove(screen, true);
        }
        if (this.recoin$takeAllButton != null) {
            this.recoin$takeAllButton.method_46421(x);
            this.recoin$takeAllButton.method_46419(y + 20 + 4);
            this.recoin$takeAllButton.field_22764 = visible;
            boolean bl3 = this.recoin$takeAllButton.field_22763 = visible && this.recoin$canQuickMove(screen, false);
        }
        if (this.recoin$throwAllButton != null) {
            this.recoin$throwAllButton.method_46421(x);
            this.recoin$throwAllButton.method_46419(y + 48);
            this.recoin$throwAllButton.field_22764 = visible;
            this.recoin$throwAllButton.field_22763 = visible && this.recoin$hasSourceStack(screen, false);
        }
        AutoBuy autoBuy = AutoBuy.getInstance();
        boolean bl4 = auctionControls = visible && autoBuy != null && autoBuy.isState() && autoBuy.isAuctionScreen(screen);
        if (this.recoin$autoBuyToggleButton != null) {
            this.recoin$autoBuyToggleButton.method_46421(Math.round((float)accessor.recoin$getX() + (float)(accessor.recoin$getBackgroundWidth() - 96) / 2.0f));
            this.recoin$autoBuyToggleButton.method_46419(accessor.recoin$getY() + accessor.recoin$getBackgroundHeight() + 4);
            this.recoin$autoBuyToggleButton.field_22764 = auctionControls;
            this.recoin$autoBuyToggleButton.field_22763 = auctionControls;
            if (autoBuy != null) {
                this.recoin$autoBuyToggleButton.method_25355((class_2561)class_2561.method_43470((String)"AUTOBUY: ").method_10852((class_2561)class_2561.method_43470((String)(autoBuy.isAuctionEnabled() ? "ON" : "OFF")).method_27692(autoBuy.isAuctionEnabled() ? class_124.field_1060 : class_124.field_1061)));
            }
        }
        if (this.recoin$autoBuyParseButton != null) {
            this.recoin$autoBuyParseButton.method_46421(Math.round((float)accessor.recoin$getX() + (float)(accessor.recoin$getBackgroundWidth() - 96) / 2.0f));
            this.recoin$autoBuyParseButton.method_46419(accessor.recoin$getY() + accessor.recoin$getBackgroundHeight() + 20 + 4 + 4);
            this.recoin$autoBuyParseButton.field_22764 = auctionControls;
            this.recoin$autoBuyParseButton.field_22763 = auctionControls;
            this.recoin$autoBuyParseButton.method_25355((class_2561)class_2561.method_43470((String)(autoBuy != null && autoBuy.isPriceParseActive() ? "PARSING..." : "PARSE")));
        }
        if (this.recoin$parserDiscountSlider != null) {
            this.recoin$parserDiscountSlider.method_46421(Math.round((float)accessor.recoin$getX() + (float)(accessor.recoin$getBackgroundWidth() - 96) / 2.0f));
            this.recoin$parserDiscountSlider.method_46419(accessor.recoin$getY() + accessor.recoin$getBackgroundHeight() + 48 + 4);
            this.recoin$parserDiscountSlider.field_22764 = auctionControls;
            this.recoin$parserDiscountSlider.field_22763 = auctionControls;
        }
    }

    @Override
    public class_4185 recoin$getInventoryThrowButton() {
        return this.recoin$inventoryThrowButton;
    }

    @Unique
    private boolean recoin$hasSourceStack(class_465<?> screen, boolean fromPlayer) {
        class_310 client = class_310.method_1551();
        if (client.field_1724 == null) {
            return false;
        }
        for (class_1735 slot : screen.method_17577().field_7761) {
            if (!slot.method_7681() || slot.field_7871 == client.field_1724.method_31548() != fromPlayer) continue;
            return true;
        }
        return false;
    }

    @Unique
    private boolean recoin$canQuickMove(class_465<?> screen, boolean fromPlayer) {
        class_310 client = class_310.method_1551();
        if (client.field_1724 == null) {
            return false;
        }
        for (class_1735 source : screen.method_17577().field_7761) {
            if (!source.method_7681() || source.field_7871 == client.field_1724.method_31548() != fromPlayer) continue;
            class_1799 sourceStack = source.method_7677();
            for (class_1735 target : screen.method_17577().field_7761) {
                if (target.field_7871 == client.field_1724.method_31548() == fromPlayer || !target.method_7680(sourceStack)) continue;
                class_1799 targetStack = target.method_7677();
                if (targetStack.method_7960()) {
                    return true;
                }
                if (!class_1799.method_31577((class_1799)sourceStack, (class_1799)targetStack) || targetStack.method_7947() >= target.method_7676(targetStack)) continue;
                return true;
            }
        }
        return false;
    }

    @Unique
    private void recoin$quickMove(class_465<?> screen, boolean fromPlayer) {
        class_310 client = class_310.method_1551();
        if (client.field_1724 == null || client.field_1761 == null) {
            return;
        }
        for (class_1735 slot : screen.method_17577().field_7761) {
            boolean playerSlot;
            if (!slot.method_7681() || (playerSlot = slot.field_7871 == client.field_1724.method_31548()) != fromPlayer) continue;
            client.field_1761.method_2906(screen.method_17577().field_7763, slot.field_7874, 0, class_1713.field_7794, (class_1657)client.field_1724);
        }
    }

    @Unique
    private void recoin$throwSlots(class_465<?> screen, boolean fromPlayer) {
        class_310 client = class_310.method_1551();
        if (client.field_1724 == null || client.field_1761 == null) {
            return;
        }
        for (class_1735 slot : screen.method_17577().field_7761) {
            boolean playerSlot;
            if (!slot.method_7681() || (playerSlot = slot.field_7871 == client.field_1724.method_31548()) != fromPlayer) continue;
            client.field_1761.method_2906(screen.method_17577().field_7763, slot.field_7874, 1, class_1713.field_7795, (class_1657)client.field_1724);
        }
    }
}

