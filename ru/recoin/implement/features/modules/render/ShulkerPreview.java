/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10799
 *  net.minecraft.class_1297
 *  net.minecraft.class_1542
 *  net.minecraft.class_1735
 *  net.minecraft.class_1747
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_243
 *  net.minecraft.class_2480
 *  net.minecraft.class_2960
 *  net.minecraft.class_332
 *  net.minecraft.class_4587
 *  net.minecraft.class_5537
 *  net.minecraft.class_9276
 *  net.minecraft.class_9279
 *  net.minecraft.class_9288
 *  net.minecraft.class_9334
 *  org.joml.Matrix3x2fStack
 */
package ru.recoin.implement.features.modules.render;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_10799;
import net.minecraft.class_1297;
import net.minecraft.class_1542;
import net.minecraft.class_1735;
import net.minecraft.class_1747;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2248;
import net.minecraft.class_243;
import net.minecraft.class_2480;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import net.minecraft.class_5537;
import net.minecraft.class_9276;
import net.minecraft.class_9279;
import net.minecraft.class_9288;
import net.minecraft.class_9334;
import org.joml.Matrix3x2fStack;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.math.ProjectionUtil;
import ru.recoin.common.util.render.InventorySlotRenderer;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.implement.events.container.HandledScreenEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.events.render.DrawEvent;
import ru.recoin.implement.events.render.WorldLoadEvent;
import ru.recoin.implement.features.draggables.HudIconRail;

public class ShulkerPreview
extends Module {
    static class_2960 TEXTURE = class_2960.method_60655((String)"recoin", (String)"textures/gui/3x9.png");
    private final transient Map<String, List<class_1799>> bundleMemory = new LinkedHashMap<String, List<class_1799>>();
    private final BooleanSetting inInventory = new BooleanSetting("In Inventory", "\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0442\u044c \u0432 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u0435").setValue(true);
    private final BooleanSetting inWorld = new BooleanSetting("In World", "\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0442\u044c \u0432 \u043c\u0438\u0440\u0435").setValue(true);

    public ShulkerPreview() {
        super("ShulkerPreview", "Shulker Preview", ModuleCategory.RENDER);
        this.setup(this.inInventory, this.inWorld);
    }

    public static List<class_1799> getContainerItems(class_1799 stack) {
        class_1747 bi;
        if (stack == null || stack.method_7960()) {
            return null;
        }
        if (ShulkerPreview.isBundle(stack)) {
            class_9276 contents = (class_9276)stack.method_58694(class_9334.field_49650);
            ArrayList<class_1799> items = new ArrayList<class_1799>(4);
            if (contents != null) {
                contents.method_59707().limit(4L).forEach(items::add);
            }
            while (items.size() < 4) {
                items.add(class_1799.field_8037);
            }
            return items;
        }
        class_1792 items = stack.method_7909();
        if (!(items instanceof class_1747) || !((bi = (class_1747)items).method_7711() instanceof class_2480)) {
            return null;
        }
        class_9288 container = (class_9288)stack.method_58694(class_9334.field_49622);
        if (container != null) {
            ArrayList<class_1799> list = new ArrayList<class_1799>();
            container.method_57489().forEach(list::add);
            return list;
        }
        return null;
    }

    private static boolean isBundle(class_1799 stack) {
        return stack.method_7909() instanceof class_5537 || stack.method_57826(class_9334.field_49650);
    }

    private static boolean isHidden(List<class_1799> items) {
        boolean nonEmpty = false;
        for (class_1799 item : items) {
            if (item.method_7960()) continue;
            nonEmpty = true;
            if (item.method_31574(class_1802.field_8407) && item.method_57380().method_57848()) continue;
            return false;
        }
        return nonEmpty;
    }

    private static String memoryKey(class_1799 stack, List<class_1799> items) {
        class_9279 data = (class_9279)stack.method_58694(class_9334.field_49628);
        StringBuilder key = new StringBuilder(stack.method_7964().getString()).append('|').append(data);
        for (class_1799 item : items) {
            key.append('|').append(item.method_7947());
        }
        return key.toString();
    }

    private List<class_1799> previewItems(class_1799 stack) {
        List<class_1799> items = ShulkerPreview.getContainerItems(stack);
        if (items != null && ShulkerPreview.isBundle(stack) && ShulkerPreview.isHidden(items)) {
            return this.bundleMemory.getOrDefault(ShulkerPreview.memoryKey(stack, items), items);
        }
        return items;
    }

    private void rememberBundle(class_1799 stack) {
        if (stack.method_7960() || !ShulkerPreview.isBundle(stack)) {
            return;
        }
        List<class_1799> items = ShulkerPreview.getContainerItems(stack);
        if (ShulkerPreview.isHidden(items) || items.stream().allMatch(class_1799::method_7960)) {
            return;
        }
        this.bundleMemory.put(ShulkerPreview.memoryKey(stack, items), items.stream().map(class_1799::method_7972).toList());
        if (this.bundleMemory.size() > 256) {
            this.bundleMemory.remove(this.bundleMemory.keySet().iterator().next());
        }
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (ShulkerPreview.mc.field_1724 == null || ShulkerPreview.mc.field_1687 == null) {
            this.bundleMemory.clear();
            return;
        }
        for (int i = 0; i < ShulkerPreview.mc.field_1724.method_31548().method_5439(); ++i) {
            this.rememberBundle(ShulkerPreview.mc.field_1724.method_31548().method_5438(i));
        }
        for (class_1735 slot : ShulkerPreview.mc.field_1724.field_7512.field_7761) {
            this.rememberBundle(slot.method_7677());
        }
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        this.bundleMemory.clear();
    }

    @Override
    public void deactivate() {
        this.bundleMemory.clear();
        super.deactivate();
    }

    @EventHandler
    public void onHandledScreen(HandledScreenEvent event) {
        if (!this.inInventory.isValue() || event.getSlotHover() == null || !event.getSlotHover().method_7681()) {
            return;
        }
        class_1799 stack = event.getSlotHover().method_7677();
        if (ShulkerPreview.isBundle(stack)) {
            return;
        }
        List<class_1799> items = this.previewItems(stack);
        if (items == null || items.isEmpty()) {
            return;
        }
        float mouseX = event.getMouseX();
        float mouseY = event.getMouseY();
        float width = 175.0f;
        float height = 70.0f;
        float x = mouseX + 8.0f;
        float y = mouseY - height - 16.0f;
        if (x + width > (float)(mc.method_22683().method_4486() - 4)) {
            x = mouseX - width - 8.0f;
        }
        if (y < 4.0f) {
            y = mouseY + 16.0f;
        }
        x = Math.max(4.0f, Math.min(x, (float)mc.method_22683().method_4486() - width - 4.0f));
        y = Math.max(4.0f, Math.min(y, (float)mc.method_22683().method_4502() - height - 4.0f));
        this.renderPreview(event.getDrawContext(), stack, items, x, y, 1.0f);
    }

    @EventHandler
    public void onDraw(DrawEvent event) {
        if (!this.inWorld.isValue() || ShulkerPreview.mc.field_1687 == null || ShulkerPreview.mc.field_1724 == null) {
            return;
        }
        for (class_1297 entity : ShulkerPreview.mc.field_1687.method_18112()) {
            class_1799 stack;
            List<class_1799> items;
            class_1542 itemEntity;
            if (!(entity instanceof class_1542) || ShulkerPreview.mc.field_1724.method_5858((class_1297)(itemEntity = (class_1542)entity)) > 4096.0 || (items = this.previewItems(stack = itemEntity.method_6983())) == null || items.isEmpty()) continue;
            class_243 pos = MathUtil.interpolate((class_1297)itemEntity).method_1031(0.0, 0.85, 0.0);
            class_243 screen = ProjectionUtil.worldSpaceToScreenSpace(pos);
            if (screen.field_1350 <= 0.0) continue;
            boolean bundle = ShulkerPreview.isBundle(stack);
            float scale = bundle ? 1.0f : 0.45f;
            float width = (bundle ? 56.0f : 175.0f) * scale;
            float height = (bundle ? 17.0f : 70.0f) * scale;
            float x = (float)screen.field_1352 - width * 0.5f;
            float y = (float)screen.field_1351 - height;
            if (bundle) {
                float pixelScale = mc.method_22683().method_4495();
                x = (float)Math.round(x * pixelScale) / pixelScale;
                y = (float)Math.round(y * pixelScale) / pixelScale;
            }
            this.renderPreview(event.getDrawContext(), stack, items, x, y, scale);
        }
    }

    public void renderPreview(class_332 context, class_1799 containerStack, List<class_1799> items, float x, float y, float scale) {
        class_2480 shulker;
        class_1747 bi;
        if (items == null || items.isEmpty()) {
            return;
        }
        if (ShulkerPreview.isBundle(containerStack)) {
            this.renderBundle(context, items, x, y, scale);
            return;
        }
        Matrix3x2fStack matrices = context.method_51448();
        matrices.pushMatrix();
        matrices.translate(x, y);
        matrices.scale(scale, scale);
        int tintColor = -1;
        class_1792 class_17922 = containerStack.method_7909();
        if (class_17922 instanceof class_1747) {
            bi = (class_1747)class_17922;
            class_2248 block = bi.method_7711();
            if (block instanceof class_2480 && (shulker = (class_2480)block).method_10528() != null) {
                tintColor = 0xFF000000 | shulker.method_10528().method_7787();
            }
        }
        context.method_25291(class_10799.field_56883, TEXTURE, 0, 0, 0.0f, 0.0f, 175, 70, 256, 256, tintColor);
        for (int i = 0; i < Math.min(items.size(), 27); ++i) {
            class_1799 item = items.get(i);
            if (item == null || item.method_7960()) continue;
            int slotX = 10 + i % 9 * 18;
            int slotY = 10 + i / 9 * 18;
            Render2DUtil.defaultDrawStack(context, item, slotX, slotY, false, true, 0.75f, 1.0f, 500);
        }
        matrices.popMatrix();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void renderBundle(class_332 context, List<class_1799> items, float x, float y, float scale) {
        Matrix3x2fStack matrices = context.method_51448();
        boolean previousHudContext = Render2DUtil.isHudDecorationActive();
        matrices.pushMatrix();
        try {
            Render2DUtil.setHudDecorationActive(true);
            matrices.translate(x, y);
            matrices.scale(scale, scale);
            class_4587 matrix = MatrixStackCompat.from(matrices);
            HudIconRail.drawPanel(matrix, blur, rectangle, 0.0f, 0.0f, 56.0f, 17.0f);
            boolean hidden = ShulkerPreview.isHidden(items);
            for (int i = 0; i < 4; ++i) {
                class_1799 item = i < items.size() ? items.get(i) : class_1799.field_8037;
                float slotX = 4.0f + (float)i * 13.0f;
                matrix = MatrixStackCompat.from(matrices);
                InventorySlotRenderer.background(matrix, slotX, 4.0f);
                if (item.method_7960()) {
                    InventorySlotRenderer.cross(matrix, slotX, 4.0f, 1.0f);
                    continue;
                }
                if (hidden) {
                    context.method_51433(ShulkerPreview.mc.field_1772, "?", (int)slotX + 2, 4, -1, true);
                    continue;
                }
                Render2DUtil.defaultDrawStack(context, item, slotX - 0.5f, 3.5f, false, true, 0.5f);
            }
        }
        finally {
            Render2DUtil.setHudDecorationActive(previousHudContext);
            matrices.popMatrix();
        }
    }
}

