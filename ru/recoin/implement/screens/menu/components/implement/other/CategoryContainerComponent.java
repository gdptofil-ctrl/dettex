/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 */
package ru.recoin.implement.screens.menu.components.implement.other;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_332;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.common.util.entity.PlayerInventoryComponent;
import ru.recoin.implement.screens.menu.MenuScreen;
import ru.recoin.implement.screens.menu.components.AbstractComponent;
import ru.recoin.implement.screens.menu.components.implement.category.CategoryComponent;
import ru.recoin.implement.screens.menu.components.implement.other.SearchComponent;
import ru.recoin.implement.screens.menu.components.implement.settings.TextComponent;

public class CategoryContainerComponent
extends AbstractComponent {
    private static final float CATEGORY_WIDTH = 22.0f;
    private static final float CATEGORY_GAP = 6.0f;
    private final List<CategoryComponent> categoryComponents = new ArrayList<CategoryComponent>();

    public void initializeCategoryComponents() {
        this.categoryComponents.clear();
        for (ModuleCategory category : ModuleCategory.values()) {
            this.categoryComponents.add(new CategoryComponent(category));
        }
    }

    @Override
    public void render(class_332 context, int mouseX, int mouseY, float delta) {
        float offset = 0.0f;
        float totalWidth = (float)this.categoryComponents.size() * 22.0f + (float)Math.max(0, this.categoryComponents.size() - 1) * 6.0f;
        float startX = this.x + ((float)MenuScreen.INSTANCE.getWidth() - totalWidth) / 2.0f;
        for (CategoryComponent component : this.categoryComponents) {
            component.x = startX + offset;
            component.y = this.y + 7.0f;
            component.width = 22.0f;
            component.height = 15.0f;
            component.render(context, mouseX, mouseY, delta);
            offset += component.width + 6.0f;
        }
    }

    @Override
    public void tick() {
        if (TextComponent.typing || SearchComponent.typing) {
            PlayerInventoryComponent.unPressMoveKeys();
        } else {
            PlayerInventoryComponent.updateMoveKeys();
        }
        this.categoryComponents.forEach(AbstractComponent::tick);
        super.tick();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.categoryComponents.forEach(categoryComponent -> categoryComponent.mouseClicked(mouseX, mouseY, button));
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.categoryComponents.forEach(categoryComponent -> categoryComponent.mouseReleased(mouseX, mouseY, button));
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        this.categoryComponents.forEach(categoryComponent -> categoryComponent.mouseDragged(mouseX, mouseY, button, deltaX, deltaY));
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        this.categoryComponents.forEach(categoryComponent -> categoryComponent.mouseScrolled(mouseX, mouseY, amount));
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (CategoryComponent categoryComponent : this.categoryComponents) {
            if (!categoryComponent.keyPressed(keyCode, scanCode, modifiers)) continue;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        this.categoryComponents.forEach(categoryComponent -> categoryComponent.charTyped(chr, modifiers));
        return super.charTyped(chr, modifiers);
    }
}

