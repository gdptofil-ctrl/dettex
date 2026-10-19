/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 *  net.minecraft.class_3532
 *  net.minecraft.class_4587
 *  org.joml.Matrix4f
 */
package ru.recoin.implement.screens.menu.components.implement.category;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import org.joml.Matrix4f;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.system.animation.Animation;
import ru.recoin.api.system.animation.Direction;
import ru.recoin.api.system.animation.implement.DecelerateAnimation;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.render.ScissorManager;
import ru.recoin.core.Main;
import ru.recoin.implement.screens.menu.MenuScreen;
import ru.recoin.implement.screens.menu.MenuStyle;
import ru.recoin.implement.screens.menu.components.AbstractComponent;
import ru.recoin.implement.screens.menu.components.implement.module.ModuleComponent;

public class CategoryComponent
extends AbstractComponent {
    private static final float CONTENT_SCISSOR_PADDING = 3.0f;
    private final List<ModuleComponent> moduleComponents = new ArrayList<ModuleComponent>();
    private final List<ModuleComponent> renderedComponents = new ArrayList<ModuleComponent>();
    private static final Set<ModuleComponent> globalModuleComponents = new HashSet<ModuleComponent>();
    private final ModuleCategory category;
    private final Animation alphaAnimation = new DecelerateAnimation().setMs(300).setValue(1.0);

    private void initialize() {
        List<Module> modules = Main.getInstance().getModuleRepository().modules();
        for (Module module : modules) {
            ModuleComponent newComponent = new ModuleComponent(module);
            if (!globalModuleComponents.add(newComponent)) continue;
            this.moduleComponents.add(newComponent);
        }
    }

    public CategoryComponent(ModuleCategory category) {
        this.category = category;
        this.initialize();
    }

    @Override
    public void render(class_332 context, int mouseX, int mouseY, float delta) {
        MenuScreen menuScreen = MenuScreen.INSTANCE;
        globalModuleComponents.clear();
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        Matrix4f positionMatrix = matrix.method_23760().method_23761();
        ScissorManager scissorManager = Main.getInstance().getScissorManager();
        this.drawCategoryTab(context, matrix);
        int sidePadding = 8;
        int columnGap = 8;
        int columnWidth = (menuScreen.width - sidePadding * 2 - columnGap * 2) / 3;
        int cardGap = 8;
        float offsetX = sidePadding;
        float offsetY = 38.0f;
        float contentX = menuScreen.x + sidePadding;
        float contentY = menuScreen.y + 42;
        float viewportHeight = (float)menuScreen.height - offsetY - 6.0f;
        int[] columnHeights = new int[3];
        this.renderedComponents.clear();
        scissorManager.push(positionMatrix, (float)menuScreen.x + offsetX - 3.0f, (float)menuScreen.y + offsetY, (float)menuScreen.width - offsetX * 2.0f + 6.0f, viewportHeight);
        for (ModuleComponent moduleComponent : this.moduleComponents) {
            if (!this.shouldRenderComponent(moduleComponent)) continue;
            int column = this.getShortestColumn(columnHeights);
            int componentHeight = moduleComponent.getComponentHeight();
            moduleComponent.x = contentX + (float)(column * (columnWidth + columnGap));
            moduleComponent.y = (float)((double)(contentY + (float)columnHeights[column]) + this.smoothedScroll);
            moduleComponent.width = columnWidth;
            if (moduleComponent.y + (float)componentHeight >= (float)menuScreen.y + offsetY && moduleComponent.y <= (float)(menuScreen.y + menuScreen.height - 4)) {
                moduleComponent.render(context, mouseX, mouseY, delta);
                this.renderedComponents.add(moduleComponent);
            }
            int n = column;
            columnHeights[n] = columnHeights[n] + (componentHeight + cardGap);
        }
        scissorManager.pop();
        for (ModuleComponent moduleComponent : this.renderedComponents) {
            moduleComponent.renderOverlays(context, mouseX, mouseY, delta);
        }
        int maxColumnHeight = 0;
        for (int columnHeight : columnHeights) {
            maxColumnHeight = Math.max(maxColumnHeight, columnHeight);
        }
        int n = class_3532.method_15340((int)((int)((float)maxColumnHeight - viewportHeight)), (int)0, (int)maxColumnHeight);
        this.scroll = class_3532.method_15350((double)this.scroll, (double)(-n), (double)0.0);
        this.smoothedScroll = MathUtil.interpolateSmooth(2.0, this.smoothedScroll, this.scroll);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        MenuScreen menuScreen = MenuScreen.INSTANCE;
        if (MathUtil.isHovered(mouseX, mouseY, this.x, this.y, this.width, this.height) && button == 0) {
            MenuScreen.INSTANCE.setCategory(this.category);
        }
        boolean isAnyComponentHovered = false;
        for (ModuleComponent moduleComponent : this.moduleComponents) {
            if (!this.shouldRenderComponent(moduleComponent) || !moduleComponent.isHover(mouseX, mouseY)) continue;
            isAnyComponentHovered = true;
            break;
        }
        if (isAnyComponentHovered) {
            for (ModuleComponent moduleComponent : this.moduleComponents) {
                if (!this.shouldRenderComponent(moduleComponent) || !moduleComponent.isHover(mouseX, mouseY)) continue;
                moduleComponent.mouseClicked(mouseX, mouseY, button);
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
        float offsetX = 8.0f;
        float offsetY = 38.0f;
        if (MathUtil.isHovered(mouseX, mouseY, (float)menuScreen.x + offsetX, (float)menuScreen.y + offsetY, (float)menuScreen.width - offsetX * 2.0f, (float)menuScreen.height - offsetY - 6.0f)) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isHover(double mouseX, double mouseY) {
        for (ModuleComponent moduleComponent : this.moduleComponents) {
            if (!moduleComponent.isHover(mouseX, mouseY)) continue;
            return true;
        }
        return super.isHover(mouseX, mouseY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (ModuleComponent moduleComponent : this.moduleComponents) {
            moduleComponent.mouseReleased(mouseX, mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        MenuScreen menuScreen = MenuScreen.INSTANCE;
        float offsetX = 8.0f;
        float offsetY = 38.0f;
        if (MathUtil.isHovered(mouseX, mouseY, (float)menuScreen.x + offsetX, (float)menuScreen.y + offsetY, (float)menuScreen.width - offsetX * 2.0f, (float)menuScreen.height - offsetY - 6.0f)) {
            this.scroll += amount * 20.0;
        }
        for (ModuleComponent moduleComponent : this.moduleComponents) {
            if (!this.shouldRenderComponent(moduleComponent)) continue;
            moduleComponent.mouseScrolled(mouseX, mouseY, amount);
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (ModuleComponent moduleComponent : this.moduleComponents) {
            if (!this.shouldRenderComponent(moduleComponent) || !moduleComponent.keyPressed(keyCode, scanCode, modifiers)) continue;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        for (ModuleComponent moduleComponent : this.moduleComponents) {
            if (!this.shouldRenderComponent(moduleComponent)) continue;
            moduleComponent.charTyped(chr, modifiers);
        }
        return super.charTyped(chr, modifiers);
    }

    private void drawCategoryTab(class_332 context, class_4587 matrix) {
        this.alphaAnimation.setDirection(MenuScreen.INSTANCE.getCategory().equals((Object)this.category) ? Direction.FORWARDS : Direction.BACKWARDS);
        float anim = this.alphaAnimation.getOutput().floatValue();
        boolean selected = MenuScreen.INSTANCE.getCategory().equals((Object)this.category);
        int accent = MenuStyle.accent();
        int selectColor = selected ? accent : ColorUtil.multAlpha(accent, 0.55f);
        FontRenderer iconFont = Fonts.getMenuSize(20, Fonts.Type.COMBINED_ICONS);
        String icon = switch (this.category) {
            default -> throw new MatchException(null, null);
            case ModuleCategory.COMBAT -> "\ue007";
            case ModuleCategory.MOVEMENT -> "\ue044";
            case ModuleCategory.PLAYER -> "\ue08a";
            case ModuleCategory.RENDER -> "\ue07a";
            case ModuleCategory.MISC -> "";
        };
        rectangle.render(ShapeProperties.create(matrix, this.x, this.y, this.width, this.height).round(6.0f).thickness(1.1f).outlineColor(selected ? ColorUtil.multAlpha(accent, 0.8f * anim + 0.2f) : -14473678).color(selected ? ColorUtil.multAlpha(accent, 0.18f * anim + 0.04f) : -15263199).build());
        float iconOffsetX = this.category == ModuleCategory.COMBAT ? -1.0f : 0.0f;
        iconFont.drawString(matrix, icon, this.x + this.width / 2.0f - iconFont.getStringWidth(icon) / 2.0f + iconOffsetX, this.y + 7.7f, selectColor);
    }

    private int getShortestColumn(int[] columnHeights) {
        int shortest = 0;
        for (int i = 1; i < columnHeights.length; ++i) {
            if (columnHeights[i] >= columnHeights[shortest]) continue;
            shortest = i;
        }
        return shortest;
    }

    private boolean shouldRenderComponent(ModuleComponent component) {
        MenuScreen menuScreen = MenuScreen.INSTANCE;
        ModuleCategory moduleCategory = component.getModule().getCategory();
        String text = menuScreen.getSearchComponent().getText().toLowerCase();
        String moduleName = component.getModule().getVisibleName().toLowerCase();
        return text.equalsIgnoreCase("") ? moduleCategory.equals((Object)menuScreen.getCategory()) : moduleName.contains(text);
    }
}

