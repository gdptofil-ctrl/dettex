/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_1657
 *  net.minecraft.class_1799
 *  net.minecraft.class_266
 *  net.minecraft.class_310
 *  net.minecraft.class_329
 *  net.minecraft.class_332
 *  net.minecraft.class_355
 *  net.minecraft.class_408
 *  net.minecraft.class_4587
 *  net.minecraft.class_9779
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.recoin.mixins;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_266;
import net.minecraft.class_310;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_355;
import net.minecraft.class_408;
import net.minecraft.class_4587;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.api.event.EventManager;
import ru.recoin.implement.features.modules.render.perf.FrameProfiler;
import ru.recoin.api.feature.draggable.AbstractDraggable;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.logger.LoggerUtil;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.render.GuiRenderFlusher;
import ru.recoin.common.util.render.HudFinalPass;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.core.Main;
import ru.recoin.implement.events.render.DrawEvent;
import ru.recoin.implement.features.draggables.Notifications;
import ru.recoin.implement.features.modules.render.AttackEffect;
import ru.recoin.implement.features.modules.render.CooldownHotbar;
import ru.recoin.implement.features.modules.render.CrossHair;
import ru.recoin.implement.features.modules.render.Hud;
import ru.recoin.implement.features.modules.render.NewHud;
import ru.recoin.implement.features.modules.render.NoRender;
import ru.recoin.implement.features.modules.render.UiAnimationManager;
import ru.recoin.implement.screens.hud.HudEditorOverlay;
import ru.recoin.implement.screens.menu.MenuScreen;

@Mixin(value={class_329.class})
public abstract class InGameHudMixin
implements QuickImports {
    @Unique
    private final Hud hud = Hud.getInstance();
    @Unique
    private final Set<String> recoin$loggedDraggableErrors = new HashSet<String>();
    @Final
    @Shadow
    private class_310 field_2035;
    @Shadow
    @Final
    private class_355 field_2015;

    @Shadow
    protected abstract void method_1760(class_332 var1);

    @Shadow
    protected abstract void method_1741(class_332 var1);

    @Inject(method={"method_1753"}, at={@At(value="TAIL")})
    private void recoin$tabClosing(class_332 context, class_9779 tickCounter, CallbackInfo ci) {
        UiAnimationManager.renderClosingTab(context, this.field_2015);
    }

    @Inject(method={"method_1762"}, at={@At(value="TAIL")}, require=0)
    private void recoin$renderHotbarCooldown(class_332 context, int x, int y, class_9779 tickCounter, class_1657 player, class_1799 stack, int seed, CallbackInfo ci) {
        CooldownHotbar cooldownHotbar = CooldownHotbar.getInstance();
        if (cooldownHotbar != null && cooldownHotbar.isState()) {
            cooldownHotbar.queueVanillaSlot(stack, (float)x - 2.0f, y);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Inject(method={"method_1753"}, at={@At(value="HEAD")})
    private void recoin$renderDrawEvent(class_332 context, class_9779 tickCounter, CallbackInfo ci) {
        if (this.field_2035.field_1724 == null || this.field_2035.field_1687 == null) {
            return;
        }
        Render2DUtil.setActiveContext(context);
        Render2DUtil.setTextYOffset(-2.5f);
        try {
            long t0 = FrameProfiler.enabled ? System.nanoTime() : 0L;
            DrawEvent event = new DrawEvent(context, drawEngine, tickCounter.method_60637(false));
            EventManager.callEvent(event);
            long t1 = FrameProfiler.enabled ? System.nanoTime() : 0L;
            Render2DUtil.onRender(context);
            context.method_73199();
            FontRenderer.flushQueuedText(context);
            GuiRenderFlusher.flush();
            if (FrameProfiler.enabled) {
                FrameProfiler.section("HUD modules", t1 - t0);
                FrameProfiler.section("HUD shapes+text flush", System.nanoTime() - t1);
            }
        }
        finally {
            Render2DUtil.setTextYOffset(0.0f);
            Render2DUtil.setActiveContext(null);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Inject(method={"method_1753"}, at={@At(value="RETURN")})
    public void onRender(class_332 context, class_9779 tickCounter, CallbackInfo ci) {
        Render2DUtil.setActiveContext(context);
        HudFinalPass.beginFrame(context, tickCounter.method_60637(false));
        Render2DUtil.setTextYOffset(-2.5f);
        Render2DUtil.setRenderAlpha(1.0f);
        try {
            if (!this.hud.shouldDisableBlur(true, false)) {
                blur.setup();
                blur.beginFrame();
            }
            context.method_71048();
            CooldownHotbar.getInstance().renderQueuedVanillaSlots(context);
            Render2DUtil.onRender(context);
            context.method_73199();
            FontRenderer.flushQueuedText(context);
            GuiRenderFlusher.flush();
            if (this.field_2035.field_1755 != null || !HudFinalPass.shouldRenderDraggablesInFinalPass()) {
                float renderedHudScale = this.hud.updateHudScale(tickCounter.method_60637(false));
                class_4587 matrix = MatrixStackCompat.from(context.method_51448());
                if (this.field_2035.field_1755 instanceof class_408) {
                    HudEditorOverlay.renderHintBehind(context);
                }
                new ArrayList<AbstractDraggable>(Main.getInstance().getDraggableRepository().draggable()).forEach(draggable -> {
                    block8: {
                        if (draggable.canDraw(this.hud, (AbstractDraggable)draggable)) {
                            draggable.startAnimation();
                        } else {
                            draggable.stopAnimation();
                        }
                        float scale = draggable.getScaleAnimation().getOutput().floatValue();
                        if (!draggable.isCloseAnimationFinished()) {
                            draggable.validPosition();
                            try {
                                Runnable draw = () -> {
                                    float centerX = (float)draggable.getX() + (float)draggable.getWidth() / 2.0f;
                                    float centerY = (float)draggable.getY() + (float)draggable.getHeight() / 2.0f;
                                    context.method_51448().pushMatrix();
                                    context.method_51448().translate(centerX, centerY);
                                    float elementScale = draggable.getRenderedScale();
                                    context.method_51448().scale(elementScale, elementScale);
                                    context.method_51448().translate(-centerX, -centerY);
                                    Render2DUtil.setRenderAlpha(scale);
                                    Render2DUtil.setHudDecorationActive(true);
                                    long td = FrameProfiler.enabled ? System.nanoTime() : 0L;
                                    try {
                                        draggable.drawDraggable(context);
                                        Render2DUtil.onRender(context);
                                        context.method_73199();
                                        FontRenderer.flushQueuedText(context);
                                        GuiRenderFlusher.flush();
                                        if (FrameProfiler.enabled) {
                                            FrameProfiler.section("HUD " + draggable.getClass().getSimpleName(), System.nanoTime() - td);
                                        }
                                    }
                                    finally {
                                        Render2DUtil.setHudDecorationActive(false);
                                        Render2DUtil.setRenderAlpha(1.0f);
                                        context.method_51448().popMatrix();
                                    }
                                };
                                if (draggable instanceof Notifications) {
                                    draw.run();
                                } else {
                                    MathUtil.scale(matrix, (float)draggable.getX() + (float)draggable.getWidth() / 2.0f, (float)draggable.getY() + (float)draggable.getHeight() / 2.0f, scale, draw);
                                }
                            }
                            catch (ConcurrentModificationException draw) {
                            }
                            catch (RuntimeException exception) {
                                String name = draggable.getClass().getSimpleName();
                                if (!this.recoin$loggedDraggableErrors.add(name)) break block8;
                                LoggerUtil.error("Skipped broken HUD draggable: " + name, exception);
                            }
                        }
                    }
                });
            }
            if (!(this.field_2035.field_1755 instanceof class_408)) {
                HudEditorOverlay.renderClosing(context);
                Render2DUtil.onRender(context);
                context.method_73199();
                FontRenderer.flushQueuedText(context);
            }
            AttackEffect.renderScreenFlash(context);
        }
        finally {
            Render2DUtil.setTextYOffset(0.0f);
            Render2DUtil.setRenderAlpha(1.0f);
            Render2DUtil.setHudDecorationActive(false);
            blur.setLiveMode(false);
            Render2DUtil.setActiveContext(null);
            HudFinalPass.clearFrame(context);
        }
    }

    @Inject(method={"method_1736"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    public void renderCrosshairHook(class_332 context, class_9779 tickCounter, CallbackInfo ci) {
        if (this.field_2035.field_1755 instanceof MenuScreen) {
            ci.cancel();
            return;
        }
        if (this.field_2035.field_1690 != null && !this.field_2035.field_1690.method_31044().method_31034()) {
            ci.cancel();
            return;
        }
        CrossHair crossHair = CrossHair.getInstance();
        if (crossHair.isState()) {
            crossHair.onRenderCrossHair(context);
            ci.cancel();
        }
    }

    @Inject(method={"method_1765"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    public void renderStatusEffectOverlayHook(class_332 context, class_9779 tickCounter, CallbackInfo ci) {
        if (this.hud.isState() && this.hud.interfaceSettings.isSelected("Potions")) {
            ci.cancel();
        }
    }

    @Inject(method={"method_1757(Lnet/minecraft/class_332;Lnet/minecraft/class_266;)V"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private void renderScoreboardSidebarHook(class_332 context, class_266 objective, CallbackInfo ci) {
        NoRender noRender = NoRender.getInstance();
        if (this.hud.isState() && this.hud.interfaceSettings.isSelected("Score Board") || noRender.isState() && noRender.modeSetting.isSelected("Scoreboard")) {
            ci.cancel();
        }
    }

    @Inject(method={"method_1735"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private void renderVignetteOverlayHook(class_332 context, class_1297 entity, CallbackInfo ci) {
        NoRender noRender = NoRender.getInstance();
        if (noRender.isState() && noRender.modeSetting.isSelected("Vignette")) {
            ci.cancel();
        }
    }

    @Inject(method={"method_55800"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private void renderOverlayMessage(class_332 context, class_9779 tickCounter, CallbackInfo ci) {
        if (this.hud.isState() && this.hud.interfaceSettings.isSelected("HotBar")) {
            ci.cancel();
        }
    }

    @Inject(method={"method_1759"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private void recoin$renderHotbar(class_332 context, class_9779 tickCounter, CallbackInfo ci) {
        if (this.hud.isState() && this.hud.interfaceSettings.isSelected("HotBar")) {
            ci.cancel();
        }
    }

    @Inject(method={"method_55805"}, at={@At(value="HEAD")}, require=0)
    private void recoin$preRenderMainHud(class_332 context, class_9779 tickCounter, CallbackInfo ci) {
        if (this.hud.isState() && this.hud.interfaceSettings.isSelected("HotBar")) {
            context.method_51448().pushMatrix();
            context.method_51448().translate(0.0f, -6.0f);
        }
    }

    @Inject(method={"method_55805"}, at={@At(value="RETURN")}, require=0)
    private void recoin$postRenderMainHud(class_332 context, class_9779 tickCounter, CallbackInfo ci) {
        if (this.hud.isState() && this.hud.interfaceSettings.isSelected("HotBar")) {
            context.method_51448().popMatrix();
        }
    }
}

