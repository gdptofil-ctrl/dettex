/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11905
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_2561
 *  net.minecraft.class_332
 *  net.minecraft.class_3532
 *  net.minecraft.class_437
 */
package ru.recoin.implement.screens.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_437;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.render.GuiRenderFlusher;
import ru.recoin.common.util.render.ItemGuiAlpha;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.implement.features.modules.misc.SelfDestruct;
import ru.recoin.implement.features.modules.render.Hud;
import ru.recoin.implement.screens.menu.MenuCloseEffect;
import ru.recoin.implement.screens.menu.MenuStyle;
import ru.recoin.implement.screens.menu.components.AbstractComponent;
import ru.recoin.implement.screens.menu.components.implement.other.DropdownMenuComponent;
import ru.recoin.implement.screens.menu.components.implement.other.SearchComponent;
import ru.recoin.implement.screens.menu.components.implement.settings.TextComponent;

public class MenuScreen
extends class_437
implements QuickImports {
    private static final int BASE_WIDTH = 640;
    private static final int BASE_HEIGHT = 402;
    private static final float GUI_SCALE = 1.0f;
    private static final float VIEWPORT_WIDTH = 0.703f;
    private static final float VIEWPORT_HEIGHT = 0.687f;
    private static final long OPEN_ANIMATION_MS = 190L;
    private static final long CLOSE_ANIMATION_MS = 165L;
    private static final long CLOSE_3D_ANIMATION_MS = 650L;
    public static MenuScreen INSTANCE = new MenuScreen();
    private final List<AbstractComponent> components = new ArrayList<AbstractComponent>();
    private final SearchComponent searchComponent = new SearchComponent();
    private final DropdownMenuComponent dropdownMenuComponent = new DropdownMenuComponent(this.searchComponent);
    private final MenuCloseEffect closeEffect = new MenuCloseEffect();
    private float smoothConfiguredScale = 0.85f;
    private float viewportScale = 1.0f;
    private float smoothPickerOffset;
    private long openAnimStart;
    private long closeAnimStart;
    private boolean closing;
    private boolean closing3D;
    private float closeStartAlpha = 1.0f;
    public ModuleCategory category = ModuleCategory.COMBAT;
    public int x;
    public int y;
    public int width;
    public int height;

    public void closeSearchOrGui() {
        if (SearchComponent.typing) {
            this.searchComponent.reset();
        } else {
            this.startCloseAnimation();
        }
    }

    public void startCloseAnimation() {
        if (this.closing) {
            return;
        }
        this.closeStartAlpha = this.getGuiAlpha();
        Hud hud = Hud.getInstance();
        this.closing3D = hud != null && hud.interface3DSetting.isValue();
        this.closing = true;
        this.closeAnimStart = System.currentTimeMillis();
    }

    public void initialize() {
        this.components.clear();
        this.openAnimStart = System.currentTimeMillis();
        this.closing = false;
        this.closing3D = false;
        this.closeEffect.close();
        this.dropdownMenuComponent.rebuildIfNeeded();
        this.components.addAll(Arrays.asList(this.dropdownMenuComponent, this.searchComponent));
    }

    public MenuScreen() {
        super(class_2561.method_30163((String)"MenuScreen"));
        this.initialize();
    }

    public void setCategory(ModuleCategory category) {
        if (this.category != category) {
            this.searchComponent.reset();
        }
        this.category = category;
    }

    public void method_25393() {
        this.finishCloseAnimation();
        this.components.forEach(AbstractComponent::tick);
        super.method_25393();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
        float guiAlpha = this.getGuiAlpha();
        float backdropAlpha = MenuStyle.backgroundOpacity() * (this.closing && this.closing3D ? 1.0f - this.getCloseProgress() : guiAlpha);
        Render2DUtil.setRenderAlpha(1.0f);
        context.method_25294(0, 0, window.getScaledWidth(), window.getScaledHeight(), ColorUtil.multAlpha(0x66000000, backdropAlpha));
        Render2DUtil.setActiveContext(context);
        Render2DUtil.setQueuedShapeActive(true);
        Render2DUtil.setGuiDecorationActive(true);
        blur.setShadowEnabled(false);
        try {
            Render2DUtil.onRender(context);
            FontRenderer.flushQueuedText(context);
            context.method_73199();
            GuiRenderFlusher.flush();
            if (this.closing && this.closing3D && this.closeEffect.isCaptured()) {
                this.closeEffect.draw(this.getCloseProgress());
                return;
            }
            int editorWidth = 0;
            this.width = Math.round((float)(640 + editorWidth) * 1.0f);
            this.height = Math.round(402.0f);
            float centeredX = (float)window.getScaledWidth() / 2.0f - (float)Math.round(640.0f) / 2.0f - (float)editorWidth / 2.0f;
            this.x = Math.round(centeredX);
            this.y = window.getScaledHeight() / 2 - this.height / 2;
            this.viewportScale = Math.min((float)window.getScaledWidth() * 0.703f / 640.0f, (float)window.getScaledHeight() * 0.687f / 402.0f);
            this.dropdownMenuComponent.position(this.x, this.y).size(this.width, this.height);
            this.searchComponent.position((float)(this.x + 640) - 239.0f, (float)this.y + 12.0f);
            blur.beginFrame();
            float centerX = (float)this.x + (float)this.width / 2.0f;
            float centerY = (float)this.y + (float)this.height / 2.0f;
            this.smoothConfiguredScale += (this.dropdownMenuComponent.getAppliedGuiScale() - this.smoothConfiguredScale) * Math.min(1.0f, Math.max(0.08f, delta * 0.18f));
            float configuredScale = this.smoothConfiguredScale * this.viewportScale;
            int guiMouseX = (int)Math.round(this.transformMouseX(mouseX));
            int guiMouseY = (int)Math.round(this.transformMouseY(mouseY));
            context.method_51448().pushMatrix();
            context.method_51448().translate(centerX, centerY);
            context.method_51448().scale(configuredScale, configuredScale);
            context.method_51448().translate(-centerX, -centerY);
            try {
                FontRenderer.setMenuTextBatchActive(true);
                Render2DUtil.setRenderAlpha(guiAlpha);
                ItemGuiAlpha.render(guiAlpha, () -> {
                    this.components.forEach(component -> component.render(context, guiMouseX, guiMouseY, delta));
                    Render2DUtil.onRender(context);
                    context.method_73199();
                    FontRenderer.flushQueuedText(context);
                    GuiRenderFlusher.flush();
                    if (this.dropdownMenuComponent.renderProfileSettingsOverlay(context, guiMouseX, guiMouseY)) {
                        Render2DUtil.onRender(context);
                        context.method_73199();
                        FontRenderer.flushQueuedText(context);
                        GuiRenderFlusher.flush();
                    }
                    windowManager.render(context, guiMouseX, guiMouseY, delta);
                    Render2DUtil.onRender(context);
                    context.method_73199();
                    FontRenderer.flushQueuedText(context);
                    GuiRenderFlusher.flush();
                    this.dropdownMenuComponent.renderHoveredDescription(context);
                    Render2DUtil.onRender(context);
                    context.method_73199();
                    FontRenderer.flushQueuedText(context);
                    GuiRenderFlusher.flush();
                    Render2DUtil.setRenderAlpha(1.0f);
                });
            }
            finally {
                FontRenderer.setMenuTextBatchActive(false);
                context.method_51448().popMatrix();
            }
            super.method_25394(context, mouseX, mouseY, delta);
            if (this.closing && this.closing3D) {
                Render2DUtil.onRender(context);
                context.method_73199();
                FontRenderer.flushQueuedText(context);
                GuiRenderFlusher.flush();
                float snapshotScale = configuredScale;
                this.closeEffect.capture(centerX - (float)this.width * snapshotScale * 0.5f, centerY - (float)this.height * snapshotScale * 0.5f, (float)this.width * snapshotScale, (float)this.height * snapshotScale, 16.0f * snapshotScale);
            }
        }
        finally {
            blur.setLiveMode(false);
            blur.setShadowEnabled(true);
            Render2DUtil.setActiveContext(null);
            Render2DUtil.setGuiDecorationActive(false);
            Render2DUtil.setQueuedShapeActive(false);
            Render2DUtil.setRenderAlpha(1.0f);
        }
    }

    public void openGui() {
        if (SelfDestruct.isLocked()) {
            return;
        }
        this.closing = false;
        this.closing3D = false;
        this.closeEffect.close();
        this.openAnimStart = System.currentTimeMillis();
        this.dropdownMenuComponent.rebuildIfNeeded();
        mc.method_1507((class_437)this);
    }

    public float getScaleAnimation() {
        return 1.0f;
    }

    public float getGuiAlpha() {
        if (this.closing && this.closing3D) {
            return this.closeStartAlpha;
        }
        float t = this.closing ? this.getCloseProgress() : class_3532.method_15363((float)((float)(System.currentTimeMillis() - this.openAnimStart) / 190.0f), (float)0.0f, (float)1.0f);
        float eased = t * t * (3.0f - 2.0f * t);
        return this.closing ? this.closeStartAlpha * (1.0f - eased) : eased;
    }

    public float getRenderScale() {
        return this.getScaleAnimation();
    }

    private double transformMouseX(double mouseX) {
        return this.transformMouse(mouseX, (double)this.x + (double)this.width / 2.0);
    }

    private double transformMouseY(double mouseY) {
        return this.transformMouse(mouseY, (double)this.y + (double)this.height / 2.0);
    }

    private double transformMouse(double coordinate, double center) {
        float scale = Math.max(this.getMouseTransformScale(), 0.001f);
        return center + (coordinate - center) / (double)scale;
    }

    private float getMouseTransformScale() {
        return this.smoothConfiguredScale * this.viewportScale;
    }

    public boolean method_25402(class_11909 click, boolean doubleClick) {
        if (this.closing) {
            return true;
        }
        double mouseX = click.comp_4798();
        double mouseY = click.comp_4799();
        int button = click.comp_4800().comp_4801();
        double guiMouseX = this.transformMouseX(mouseX);
        double guiMouseY = this.transformMouseY(mouseY);
        if (this.searchComponent.getExpansion() > 0.1f && MathUtil.isHovered(guiMouseX, guiMouseY, this.searchComponent.x, this.searchComponent.y, this.searchComponent.width, this.searchComponent.height)) {
            this.searchComponent.mouseClicked(guiMouseX, guiMouseY, button);
            return true;
        }
        if (!windowManager.mouseClicked(guiMouseX, guiMouseY, button)) {
            this.components.forEach(component -> component.mouseClicked(guiMouseX, guiMouseY, button));
        }
        return super.method_25402(click, doubleClick);
    }

    public boolean method_25406(class_11909 click) {
        double mouseX = click.comp_4798();
        double mouseY = click.comp_4799();
        int button = click.comp_4800().comp_4801();
        double guiMouseX = this.transformMouseX(mouseX);
        double guiMouseY = this.transformMouseY(mouseY);
        this.components.forEach(component -> component.mouseReleased(guiMouseX, guiMouseY, button));
        windowManager.mouseReleased(guiMouseX, guiMouseY, button);
        return super.method_25406(click);
    }

    public boolean method_25403(class_11909 click, double deltaX, double deltaY) {
        double guiDeltaY;
        double guiDeltaX;
        double guiMouseY;
        if (this.closing) {
            return true;
        }
        double mouseX = click.comp_4798();
        double mouseY = click.comp_4799();
        int button = click.comp_4800().comp_4801();
        double guiMouseX = this.transformMouseX(mouseX);
        if (!windowManager.mouseDragged(guiMouseX, guiMouseY = this.transformMouseY(mouseY), button, guiDeltaX = deltaX / (double)Math.max(this.getMouseTransformScale(), 0.001f), guiDeltaY = deltaY / (double)Math.max(this.getMouseTransformScale(), 0.001f))) {
            this.components.forEach(component -> component.mouseDragged(guiMouseX, guiMouseY, button, guiDeltaX, guiDeltaY));
        }
        return super.method_25403(click, deltaX, deltaY);
    }

    public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        double guiMouseY;
        if (this.closing) {
            return true;
        }
        double guiMouseX = this.transformMouseX(mouseX);
        if (windowManager.mouseScrolled(guiMouseX, guiMouseY = this.transformMouseY(mouseY), verticalAmount)) {
            return true;
        }
        boolean handled = false;
        for (AbstractComponent component : this.components) {
            if (!component.mouseScrolled(guiMouseX, guiMouseY, verticalAmount)) continue;
            handled = true;
        }
        return handled || super.method_25401(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public boolean method_25404(class_11908 input) {
        if (this.closing) {
            return true;
        }
        int keyCode = input.comp_4795();
        int scanCode = input.comp_4796();
        int modifiers = input.comp_4797();
        if (keyCode == 256 && this.method_25422()) {
            this.closeSearchOrGui();
            return true;
        }
        if (keyCode == 344) {
            return true;
        }
        if (SearchComponent.typing || keyCode == 70 && (modifiers & 2) != 0) {
            return this.searchComponent.keyPressed(keyCode, scanCode, modifiers);
        }
        if (windowManager.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        for (AbstractComponent component : this.components) {
            if (!component.keyPressed(keyCode, scanCode, modifiers)) continue;
            return true;
        }
        return super.method_25404(input);
    }

    public boolean method_25400(class_11905 input) {
        int modifiers;
        char chr;
        if (this.closing) {
            return true;
        }
        String value = input.method_74226();
        if (!value.isEmpty() && !windowManager.charTyped(chr = value.charAt(0), modifiers = input.comp_4794())) {
            this.components.forEach(component -> component.charTyped(chr, modifiers));
        }
        return super.method_25400(input);
    }

    public boolean method_25421() {
        return false;
    }

    public void method_25419() {
        this.startCloseAnimation();
    }

    private void finishCloseAnimation() {
        if (this.closing && this.getCloseProgress() >= 1.0f) {
            TextComponent.typing = false;
            super.method_25419();
        }
    }

    private float getCloseProgress() {
        long duration = this.closing3D ? 650L : 165L;
        return class_3532.method_15363((float)((float)(System.currentTimeMillis() - this.closeAnimStart) / (float)duration), (float)0.0f, (float)1.0f);
    }

    public void method_25432() {
        this.closeEffect.close();
        TextComponent.typing = false;
        super.method_25432();
    }

    public void setSmoothConfiguredScale(float smoothConfiguredScale) {
        this.smoothConfiguredScale = smoothConfiguredScale;
    }

    public void setViewportScale(float viewportScale) {
        this.viewportScale = viewportScale;
    }

    public void setSmoothPickerOffset(float smoothPickerOffset) {
        this.smoothPickerOffset = smoothPickerOffset;
    }

    public void setOpenAnimStart(long openAnimStart) {
        this.openAnimStart = openAnimStart;
    }

    public void setCloseAnimStart(long closeAnimStart) {
        this.closeAnimStart = closeAnimStart;
    }

    public void setClosing(boolean closing) {
        this.closing = closing;
    }

    public void setClosing3D(boolean closing3D) {
        this.closing3D = closing3D;
    }

    public void setCloseStartAlpha(float closeStartAlpha) {
        this.closeStartAlpha = closeStartAlpha;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public List<AbstractComponent> getComponents() {
        return this.components;
    }

    public SearchComponent getSearchComponent() {
        return this.searchComponent;
    }

    public DropdownMenuComponent getDropdownMenuComponent() {
        return this.dropdownMenuComponent;
    }

    public MenuCloseEffect getCloseEffect() {
        return this.closeEffect;
    }

    public float getSmoothConfiguredScale() {
        return this.smoothConfiguredScale;
    }

    public float getViewportScale() {
        return this.viewportScale;
    }

    public float getSmoothPickerOffset() {
        return this.smoothPickerOffset;
    }

    public long getOpenAnimStart() {
        return this.openAnimStart;
    }

    public long getCloseAnimStart() {
        return this.closeAnimStart;
    }

    public boolean isClosing() {
        return this.closing;
    }

    public boolean isClosing3D() {
        return this.closing3D;
    }

    public float getCloseStartAlpha() {
        return this.closeStartAlpha;
    }

    public ModuleCategory getCategory() {
        return this.category;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }
}

