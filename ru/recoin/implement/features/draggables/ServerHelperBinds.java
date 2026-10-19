/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1735
 *  net.minecraft.class_1796
 *  net.minecraft.class_1796$class_1797
 *  net.minecraft.class_1799
 *  net.minecraft.class_332
 *  net.minecraft.class_4587
 */
package ru.recoin.implement.features.draggables;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1735;
import net.minecraft.class_1796;
import net.minecraft.class_1799;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import ru.recoin.api.feature.draggable.AbstractDraggable;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.other.StringUtil;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.implement.features.modules.misc.ServerHelper;
import ru.recoin.implement.features.modules.render.Hud;

public class ServerHelperBinds
extends AbstractDraggable {
    private static final int MIN_WIDTH = 72;
    private static final int HEIGHT = 20;
    private static final float ITEM_SCALE = 0.55f;
    private static final float PADDING = 3.5f;
    private static final float GAP = 2.5f;
    private static final float CHIP_HEIGHT = 14.0f;
    private static final float CHIP_PADDING_X = 5.0f;
    private static final float ICON_SLOT = 10.0f;
    private static final float DIVIDER_WIDTH = 0.5f;
    private static final float DIVIDER_HEIGHT = 7.5f;
    private static final float ICON_DIVIDER_GAP = 2.0f;
    private final List<ServerHelper.KeyBind> binds = new ArrayList<ServerHelper.KeyBind>();
    private final List<class_1799> stacks = new ArrayList<class_1799>();
    private float[] cachedWidths = new float[4];

    public ServerHelperBinds() {
        super("ServerHelper Binds", 210, 10, 72, 20, true);
    }

    @Override
    public boolean visible() {
        return !this.binds.isEmpty() || PlayerIntersectionUtil.isChat(ServerHelperBinds.mc.field_1755);
    }

    @Override
    public void tick() {
        this.binds.clear();
        this.stacks.clear();
        ServerHelper serverHelper = Instance.get(ServerHelper.class);
        if (serverHelper == null || !serverHelper.isState() || ServerHelperBinds.mc.field_1724 == null) {
            return;
        }
        for (ServerHelper.KeyBind bind : serverHelper.getKeyBindings()) {
            class_1735 slot;
            if (!bind.setting().isVisible() || bind.setting().getKey() <= 0 || (slot = serverHelper.findSlot(bind)) == null) continue;
            this.binds.add(bind);
            this.stacks.add(slot.method_7677().method_46651(1));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void drawDraggable(class_332 context) {
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        FontRenderer font = Fonts.getSize(11, Fonts.Type.INTER_BOLD);
        List<DisplayBind> displayBinds = this.getDisplayBinds();
        if (displayBinds.isEmpty()) {
            return;
        }
        this.ensureWidthCapacity(displayBinds.size());
        float totalWidth = 3.5f;
        for (int i = 0; i < displayBinds.size(); ++i) {
            float width;
            DisplayBind bind = displayBinds.get(i);
            String key = bind.key();
            this.cachedWidths[i] = width = Math.max(24.0f, 24.5f + font.getStringWidth(key));
            totalWidth += width + 2.5f;
        }
        if (!displayBinds.isEmpty()) {
            totalWidth -= 2.5f;
        }
        this.setWidth((int)Math.max(72.0, Math.ceil(totalWidth += 3.5f)));
        this.setHeight(20);
        float x = (float)this.getX() + 3.5f;
        float y = (float)this.getY() + 3.0f;
        for (int i = 0; i < displayBinds.size(); ++i) {
            DisplayBind bind = displayBinds.get(i);
            String key = bind.key();
            float width = this.cachedWidths[i];
            ShapeProperties chipShape = ShapeProperties.create(matrix, x, y - 0.5f, width, 14.0).round(5.0f).softness(2.0f).thickness(0.0f).quality(24.0f).outlineColor(0).color(ColorUtil.DARK_BLACK).build();
            if (Hud.getInstance().shouldDisableBlur(true, false)) {
                rectangle.render(chipShape.toBuilder().color(Render2DUtil.applyRenderAlpha(1543701770)).build());
            } else {
                boolean hudDecoration = Render2DUtil.isHudDecorationActive();
                Render2DUtil.setHudDecorationActive(false);
                try {
                    blur.render(chipShape);
                }
                finally {
                    Render2DUtil.setHudDecorationActive(hudDecoration);
                }
            }
            float iconX = x + 2.5f;
            float dividerX = iconX + 10.0f + 2.0f;
            float textX = dividerX + 0.5f + 2.0f;
            float dividerY = y + 2.826087f;
            Render2DUtil.defaultDrawStack(context, bind.stack(), iconX, y + 1.0f, false, false, 0.55f);
            rectangle.render(ShapeProperties.create(matrix, dividerX, dividerY, 0.5, 7.5).color(ColorUtil.getOutline(0.75f, 0.5f)).build());
            font.drawString(matrix, key, textX, y + 5.7f, ColorUtil.getText());
            x += width + 2.5f;
        }
    }

    private List<DisplayBind> getDisplayBinds() {
        if (!this.binds.isEmpty()) {
            ArrayList<DisplayBind> result = new ArrayList<DisplayBind>(this.binds.size());
            for (int i = 0; i < this.binds.size(); ++i) {
                ServerHelper.KeyBind bind = this.binds.get(i);
                result.add(new DisplayBind(this.stacks.get(i), this.getBindOrCooldown(i, bind), this.isCooldown(i)));
            }
            return result;
        }
        return List.of();
    }

    private void ensureWidthCapacity(int size) {
        if (this.cachedWidths.length >= size) {
            return;
        }
        this.cachedWidths = new float[Math.max(size, this.cachedWidths.length * 2)];
    }

    private String getBindOrCooldown(int index, ServerHelper.KeyBind bind) {
        if (ServerHelperBinds.mc.field_1724 == null) {
            return StringUtil.getBindName(bind.setting().getKey());
        }
        class_1799 stack = this.stacks.get(index);
        class_1796 manager = ServerHelperBinds.mc.field_1724.method_7357();
        if (manager.method_7904(stack)) {
            class_1796.class_1797 entry = (class_1796.class_1797)manager.field_8024.get(manager.method_62836(stack));
            if (entry != null) {
                int ticks = Math.max(1, entry.comp_3084() - manager.field_8025);
                return Math.max(1, (int)Math.ceil((double)ticks / 20.0)) + "\u0441";
            }
            return "1\u0441";
        }
        return StringUtil.getBindName(bind.setting().getKey());
    }

    private boolean isCooldown(int index) {
        if (ServerHelperBinds.mc.field_1724 == null) {
            return false;
        }
        return ServerHelperBinds.mc.field_1724.method_7357().method_7904(this.stacks.get(index));
    }

    private record DisplayBind(class_1799 stack, String key, boolean cooldown) {
    }
}

