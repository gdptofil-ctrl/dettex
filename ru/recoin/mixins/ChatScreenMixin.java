/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11909
 *  net.minecraft.class_2561
 *  net.minecraft.class_332
 *  net.minecraft.class_338
 *  net.minecraft.class_342
 *  net.minecraft.class_408
 *  net.minecraft.class_437
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.recoin.mixins;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_338;
import net.minecraft.class_342;
import net.minecraft.class_408;
import net.minecraft.class_437;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.recoin.api.feature.draggable.AbstractDraggable;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.core.Main;
import ru.recoin.implement.features.modules.render.AuctionHelper;
import ru.recoin.implement.features.modules.render.Hud;
import ru.recoin.implement.features.modules.render.ViewModel;
import ru.recoin.implement.screens.hud.HudEditorOverlay;

@Mixin(value={class_408.class})
public class ChatScreenMixin
extends class_437
implements QuickImports {
    @Shadow
    protected class_342 field_2382;

    protected ChatScreenMixin() {
        super((class_2561)class_2561.method_43473());
    }

    @Inject(method={"method_25394"}, at={@At(value="HEAD")})
    private void onRender(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Hud hud = Hud.getInstance();
        List<AbstractDraggable> draggables = Main.getInstance().getDraggableRepository().draggable();
        new ArrayList<AbstractDraggable>(draggables).stream().filter(draggable -> draggable.canDraw(hud, (AbstractDraggable)draggable) && draggable.isDragging()).reduce((first, second) -> second).ifPresent(active -> draggables.forEach(draggable -> {
            if (active == draggable) {
                draggable.render(context, mouseX, mouseY, delta);
            }
        }));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Inject(method={"method_25394"}, at={@At(value="TAIL")})
    private void recoin$renderAuctionCalculator(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Render2DUtil.setActiveContext(context);
        Render2DUtil.setQueuedShapeActive(true);
        float previousTextYOffset = Render2DUtil.getTextYOffset();
        Render2DUtil.setTextYOffset(-2.5f);
        try {
            HudEditorOverlay.render(context, mouseX, mouseY);
            Render2DUtil.onRender(context);
            context.method_73199();
            FontRenderer.flushQueuedText(context);
        }
        finally {
            Render2DUtil.setQueuedShapeActive(false);
            Render2DUtil.setTextYOffset(previousTextYOffset);
            Render2DUtil.setActiveContext(null);
            Render2DUtil.setRenderAlpha(1.0f);
            Render2DUtil.setHudDecorationActive(false);
        }
        if (this.field_2382 != null) {
            String preview;
            AuctionHelper helper = AuctionHelper.getInstance();
            String string = preview = helper == null ? null : helper.getCalculatorPreview(this.field_2382.method_1882());
            if (preview != null) {
                int x = this.field_2382.method_1889(this.field_2382.method_1882().length()) + 5;
                int y = this.field_2382.method_46427();
                context.method_25303(this.field_22793, preview, x, y, -11141291);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Inject(method={"method_25402"}, at={@At(value="HEAD")}, cancellable=true)
    private void onMouseClicked(class_11909 click, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        List<AbstractDraggable> draggables;
        int button;
        double mouseY;
        double mouseX = click.comp_4798();
        if (HudEditorOverlay.click(mouseX, mouseY = click.comp_4799(), button = click.comp_4800().comp_4801())) {
            cir.setReturnValue(true);
            return;
        }
        Hud hud = Hud.getInstance();
        boolean handled = false;
        List<AbstractDraggable> list = draggables = Main.getInstance().getDraggableRepository().draggable();
        synchronized (list) {
            for (int i = draggables.size() - 1; i >= 0; --i) {
                AbstractDraggable draggable2 = draggables.get(i);
                if (!draggable2.canDraw(hud, draggable2) || !draggable2.isHovered(mouseX, mouseY)) continue;
                Main.getInstance().getDraggableRepository().bringToFront(draggable2);
                draggable2.mouseClicked(mouseX, mouseY, button);
                handled = true;
                break;
            }
        }
        if (handled) {
            cir.setReturnValue(true);
            return;
        }
        ViewModel viewModel = ViewModel.getInstance();
        if (viewModel != null && viewModel.isState() && viewModel.onMouseClick(mouseX, mouseY, button)) {
            cir.setReturnValue(true);
            return;
        }
        new ArrayList<AbstractDraggable>(draggables).forEach(draggable -> draggable.mouseClicked(mouseX, mouseY, button));
    }

    public boolean method_25406(class_11909 click) {
        double mouseX = click.comp_4798();
        double mouseY = click.comp_4799();
        int button = click.comp_4800().comp_4801();
        new ArrayList<AbstractDraggable>(Main.getInstance().getDraggableRepository().draggable()).forEach(draggable -> draggable.mouseReleased(mouseX, mouseY, button));
        return super.method_25406(click);
    }

    @Inject(method={"method_25401"}, at={@At(value="HEAD")}, cancellable=true)
    private void recoin$onMouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount, CallbackInfoReturnable<Boolean> cir) {
        ViewModel viewModel = ViewModel.getInstance();
        if (viewModel != null && viewModel.isState() && viewModel.onMouseScroll(mouseX, mouseY, verticalAmount)) {
            cir.setReturnValue(true);
            return;
        }
        if (this.field_22787 != null && this.field_22787.field_1705 != null && this.field_22787.field_1705.method_1743() != null && this.field_22787.field_1690 != null) {
            double chatWidth = (double)class_338.method_1806((double)((Double)this.field_22787.field_1690.method_42556().method_41753())) + 30.0;
            double screenHeight = this.field_22787.method_22683().method_4502();
            if (mouseX <= chatWidth && mouseY <= screenHeight - 20.0) {
                return;
            }
        }
    }
}

