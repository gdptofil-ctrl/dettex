/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1041
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_1291
 *  net.minecraft.class_1293
 *  net.minecraft.class_1294
 *  net.minecraft.class_1661
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1844
 *  net.minecraft.class_2561
 *  net.minecraft.class_332
 *  net.minecraft.class_3675
 *  net.minecraft.class_3675$class_307
 *  net.minecraft.class_437
 *  net.minecraft.class_4587
 *  net.minecraft.class_6880
 *  net.minecraft.class_9276
 *  net.minecraft.class_9334
 */
package ru.recoin.implement.features.modules.combat;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import net.minecraft.class_1041;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1661;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1844;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_3675;
import net.minecraft.class_437;
import net.minecraft.class_4587;
import net.minecraft.class_6880;
import net.minecraft.class_9276;
import net.minecraft.class_9334;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BindSetting;
import ru.recoin.api.feature.module.setting.implement.TextSetting;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.entity.PlayerInventoryUtil;
import ru.recoin.common.util.item.PotionTrackerUtil;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.common.util.render.pipeline.DirectBlurPipeline;
import ru.recoin.implement.events.keyboard.KeyEvent;
import ru.recoin.implement.features.draggables.Notifications;
import ru.recoin.implement.features.modules.combat.PotionUseHandler;

public final class PotionWheelController
extends Module {
    private static final int MIN_SLOTS = 2;
    private static final String EMPTY_SLOT = "-";
    private static final String DEFAULT_LAYOUT = "-;-";
    private final BindSetting wheelBind = new BindSetting("\u041e\u0442\u043a\u0440\u044b\u0442\u044c \u043c\u0435\u043d\u044e \u0437\u0435\u043b\u0438\u0439", "\u0423\u0434\u0435\u0440\u0436\u0438\u0432\u0430\u0439\u0442\u0435 \u043a\u043b\u0430\u0432\u0438\u0448\u0443 \u0434\u043b\u044f \u043e\u0442\u043a\u0440\u044b\u0442\u0438\u044f \u043a\u043e\u043b\u0435\u0441\u0430").setKey(86).setType(0);
    private final TextSetting savedLayout = new TextSetting("PotionWheelLayout", "Hidden potion wheel layout").visible(() -> false).setText("-;-");
    private final DirectBlurPipeline wheelBlur = new DirectBlurPipeline();
    private final BooleanSupplier enabled;
    private final PotionUseHandler potionUseHandler = new PotionUseHandler();
    private PotionWheelScreen activeScreen;

    public BindSetting getWheelBind() {
        return this.wheelBind;
    }

    public TextSetting getSavedLayout() {
        return this.savedLayout;
    }

    public PotionUseHandler getPotionUseHandler() {
        return this.potionUseHandler;
    }

    public PotionWheelController(BooleanSupplier enabled) {
        super("PotionWheelController", "Potion Wheel Controller", ModuleCategory.MISC);
        this.enabled = enabled;
        this.setup(this.wheelBind, this.savedLayout);
    }

    public void onKey(KeyEvent event) {
        if (!this.enabled.getAsBoolean()) {
            return;
        }
        int key = this.wheelBind.getKey();
        if (key == -1 || event.key() != key || event.type() != PlayerIntersectionUtil.getKeyType(key)) {
            return;
        }
        if (event.action() == 0) {
            PotionWheelScreen screen = this.activeScreen;
            if (screen != null && PotionWheelController.mc.field_1755 == screen) {
                screen.releaseSelection();
            }
            return;
        }
        if (event.action() != 1) {
            return;
        }
        if (event.screen() != null || PotionWheelController.mc.field_1755 != null || PotionWheelController.mc.field_1724 == null || PotionWheelController.mc.field_1687 == null) {
            return;
        }
        List<PotionType> layout = this.readLayout();
        this.activeScreen = new PotionWheelScreen(this, layout, event.type(), key);
        mc.method_1507((class_437)this.activeScreen);
    }

    @Override
    public void deactivate() {
        PotionWheelScreen screen = this.activeScreen;
        this.activeScreen = null;
        if (screen != null && PotionWheelController.mc.field_1755 == screen) {
            mc.method_1507(null);
        }
        super.deactivate();
    }

    private void onScreenRemoved(PotionWheelScreen screen) {
        if (this.activeScreen == screen) {
            this.activeScreen = null;
        }
    }

    private List<PotionType> readLayout() {
        ArrayList<PotionType> result = new ArrayList<PotionType>();
        String encoded = this.savedLayout.getText();
        if (encoded != null && !encoded.isBlank()) {
            String[] tokens;
            for (String token : tokens = encoded.split(";", -1)) {
                result.add(PotionType.fromId(token));
            }
        }
        while (result.size() < 2) {
            result.add(null);
        }
        return result;
    }

    private void saveLayout(List<PotionType> layout) {
        String encoded = layout.stream().map(type -> type == null ? EMPTY_SLOT : type.id).collect(Collectors.joining(";"));
        this.savedLayout.setText(encoded);
    }

    private void throwPotion(PotionType type) {
        if (type == null || PotionWheelController.mc.field_1724 == null || PotionWheelController.mc.field_1761 == null) {
            return;
        }
        if (!this.potionUseHandler.use(type::matches)) {
            Notifications.getInstance().addList((class_2561)class_2561.method_43470((String)(type.displayName + " \u2014 \u043d\u0435\u0442 \u0432 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u0435 \u0438\u043b\u0438 \u043c\u0435\u0448\u043a\u0430\u0445")), 2200L, type.fallbackStack());
        }
    }

    private static final class PotionWheelScreen
    extends class_437 {
        private static final float INNER_RADIUS = 56.0f;
        private static final float OUTER_RADIUS = 86.0f;
        private static final float ITEM_RADIUS = 71.0f;
        private static final float DIRECTION_DEAD_ZONE = 18.0f;
        private static final float SEGMENT_GAP_DEGREES = 3.4f;
        private static final float PICKER_WIDTH = 164.0f;
        private static final float PICKER_ROW_HEIGHT = 19.0f;
        private static final float PICKER_PADDING = 6.0f;
        private final PotionWheelController owner;
        private final List<PotionType> slots;
        private final class_3675.class_307 openingType;
        private final int openingCode;
        private final Map<PotionType, PotionEntry> entries = new EnumMap<PotionType, PotionEntry>(PotionType.class);
        private boolean consumeOpeningMousePress;
        private boolean consumeOpeningKeyPress;
        private int pickerSlot = -1;
        private int draggedSlot = -1;

        private PotionWheelScreen(PotionWheelController owner, List<PotionType> slots, class_3675.class_307 openingType, int openingCode) {
            super((class_2561)class_2561.method_43470((String)"Potion Thrower"));
            this.owner = owner;
            this.slots = new ArrayList<PotionType>(slots);
            this.openingType = openingType;
            this.openingCode = openingCode;
            this.consumeOpeningMousePress = openingType == class_3675.class_307.field_1672;
            this.consumeOpeningKeyPress = openingType == class_3675.class_307.field_1668;
        }

        public void method_25393() {
            if (!this.owner.enabled.getAsBoolean() || this.field_22787 == null || this.field_22787.field_1724 == null || this.field_22787.field_1687 == null) {
                this.method_25419();
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
            this.refreshEntries();
            Render2DUtil.setActiveContext(context);
            Render2DUtil.setQueuedShapeActive(true);
            try {
                class_4587 matrices = MatrixStackCompat.from(context.method_51448());
                float centerX = (float)this.field_22789 / 2.0f;
                float centerY = (float)this.field_22790 / 2.0f - 6.0f;
                int hovered = this.pickerSlot < 0 ? this.getHoverIndex(mouseX, mouseY, centerX, centerY, this.slots.size()) : -1;
                this.owner.wheelBlur.beginFrame();
                this.owner.wheelBlur.drawBlur(centerX - 86.0f, centerY - 86.0f, 172.0f, 172.0f, 7.0f, new float[]{-56.0f, 86.0f, 6.8f, this.slots.size()}, 0);
                this.drawWheelRing(matrices, centerX, centerY, hovered);
                this.drawWheelContents(context, matrices, centerX, centerY);
                this.drawWheelCaption(matrices, centerX, centerY, hovered);
                if (this.pickerSlot >= 0) {
                    this.drawPicker(context, matrices, mouseX, mouseY, centerX, centerY);
                }
            }
            finally {
                this.owner.wheelBlur.setLiveMode(false);
                Render2DUtil.setQueuedShapeActive(false);
                Render2DUtil.setActiveContext(null);
            }
        }

        private void refreshEntries() {
            EnumMap<PotionType, Integer> counts = new EnumMap<PotionType, Integer>(PotionType.class);
            EnumMap displayStacks = new EnumMap(PotionType.class);
            for (PotionType type : PotionType.values()) {
                counts.put(type, 0);
            }
            PlayerInventoryUtil.slots().filter(slot -> slot.field_7871 instanceof class_1661).forEach(slot -> {
                this.collectStack(slot.method_7677(), counts, displayStacks);
                class_9276 contents = (class_9276)slot.method_7677().method_58694(class_9334.field_49650);
                if (contents == null || contents.method_57429()) {
                    return;
                }
                for (int index = 0; index < contents.method_57426(); ++index) {
                    this.collectStack(contents.method_57422(index), counts, displayStacks);
                }
            });
            this.entries.clear();
            PotionType[] potionTypeArray = PotionType.values();
            int n = potionTypeArray.length;
            for (int i = 0; i < n; ++i) {
                PotionType type;
                class_1799 displayStack = (class_1799)displayStacks.get((Object)(type = potionTypeArray[i]));
                this.entries.put(type, new PotionEntry(displayStack == null ? type.fallbackStack() : displayStack, (Integer)counts.get((Object)type)));
            }
        }

        private void collectStack(class_1799 stack, Map<PotionType, Integer> counts, Map<PotionType, class_1799> displayStacks) {
            PotionType type = PotionType.fromStack(stack);
            if (type == null) {
                return;
            }
            counts.merge(type, stack.method_7947(), Integer::sum);
            displayStacks.putIfAbsent(type, stack.method_46651(1));
        }

        private void drawWheelRing(class_4587 matrices, float centerX, float centerY, int hovered) {
            int count = this.slots.size();
            float segment = (float)Math.PI * 2 / (float)count;
            float size = 172.0f;
            float thickness = 30.0f;
            for (int index = 0; index < count; ++index) {
                PotionEntry entry;
                float center = (float)Math.toDegrees(this.getSegmentCenterAngle(index, count)) + 90.0f;
                float start = center - (float)Math.toDegrees(segment) / 2.0f + 3.4f;
                float end = center + (float)Math.toDegrees(segment) / 2.0f - 3.4f;
                PotionType type = this.slots.get(index);
                PotionEntry potionEntry = entry = type == null ? null : this.entries.get((Object)type);
                int color = type != null && (entry == null || entry.count == 0) ? (index == hovered ? -672900507 : -1462154411) : (index == hovered ? ColorUtil.getClientColor(0.62f) : (type == null ? 1517516675 : 2022675100));
                arc.render(ShapeProperties.create(matrices, centerX - 86.0f, centerY - 86.0f, size, size).thickness(thickness).start(start).end(end).color(0x2EFFFFFF).build());
                arc.render(ShapeProperties.create(matrices, centerX - 86.0f, centerY - 86.0f, size, size).thickness(thickness).start(start).end(end).color(color).build());
            }
        }

        private void drawWheelContents(class_332 context, class_4587 matrices, float centerX, float centerY) {
            FontRenderer plusFont = Fonts.getMenuSize(18, Fonts.Type.INTER_SEMI);
            FontRenderer countFont = Fonts.getMenuSize(10, Fonts.Type.INTER_SEMI);
            int count = this.slots.size();
            for (int index = 0; index < count; ++index) {
                float angle = this.getSegmentCenterAngle(index, count);
                float itemX = centerX + (float)Math.cos(angle) * 71.0f;
                float itemY = centerY + (float)Math.sin(angle) * 71.0f;
                PotionType type = this.slots.get(index);
                if (type == null) {
                    plusFont.drawCenteredString(matrices, "+", itemX, itemY - 6.0f, -1907220);
                    continue;
                }
                PotionEntry entry = this.entries.get((Object)type);
                if (entry == null) continue;
                context.method_51428(entry.displayStack, Math.round(itemX - 8.0f), Math.round(itemY - 10.0f), 200);
                if (entry.count <= 0) continue;
                String countText = String.valueOf(entry.count);
                countFont.drawString(matrices, countText, itemX + 7.0f - countFont.getStringWidth(countText), itemY + 4.0f, -1);
            }
        }

        private void drawWheelCaption(class_4587 matrices, float centerX, float centerY, int hovered) {
            PotionType type;
            FontRenderer titleFont = Fonts.getMenuSize(13, Fonts.Type.INTER_SEMI);
            FontRenderer hintFont = Fonts.getMenuSize(10, Fonts.Type.INTER_MEDIUM);
            Object selected = hovered >= 0 && hovered < this.slots.size() ? ((type = this.slots.get(hovered)) == null ? "\u041f\u0443\u0441\u0442\u043e\u0439 \u0441\u043b\u043e\u0442 \u2014 \u041f\u041a\u041c \u0434\u043b\u044f \u0432\u044b\u0431\u043e\u0440\u0430" : "[\u2605] " + type.displayName) : "\u041f\u041a\u041c \u2014 \u0432\u044b\u0431\u0440\u0430\u0442\u044c \u0437\u0435\u043b\u044c\u0435";
            float selectedY = centerY + 86.0f + 12.0f;
            titleFont.drawCenteredString(matrices, (String)selected, centerX, selectedY, -920841);
            float underlineWidth = Math.max(24.0f, titleFont.getStringWidth((String)selected) + 4.0f);
            rectangle.render(ShapeProperties.create(matrices, centerX - underlineWidth / 2.0f, selectedY + 11.0f, underlineWidth, 1.5).round(1.0f).color(ColorUtil.getClientColor(0.95f)).build());
            String addSlot = "\u0421\u041a\u041c \u2014 \u0434\u043e\u0431\u0430\u0432\u0438\u0442\u044c | \u041f\u041a\u041c \u2014 \u043e\u0447\u0438\u0441\u0442\u0438\u0442\u044c/\u0443\u0434\u0430\u043b\u0438\u0442\u044c";
            int addColor = -5393727;
            hintFont.drawCenteredString(matrices, addSlot, centerX, selectedY + 15.0f, addColor);
        }

        private void drawPicker(class_332 context, class_4587 matrices, int mouseX, int mouseY, float centerX, float centerY) {
            float height = 12.0f + (float)PotionType.values().length * 19.0f;
            float x = centerX - 82.0f;
            float y = centerY - height / 2.0f;
            int hoveredRow = this.getPickerRow(mouseX, mouseY, x, y);
            PotionType selected = this.pickerSlot >= 0 && this.pickerSlot < this.slots.size() ? this.slots.get(this.pickerSlot) : null;
            this.owner.wheelBlur.drawBlur(x, y, 164.0f, height, 8.0f, new float[]{9.0f, 9.0f, 9.0f, 9.0f}, 1912997643);
            rectangle.render(ShapeProperties.create(matrices, x, y, 164.0, height).round(9.0f).color(-401994992).build());
            PotionType[] types = PotionType.values();
            FontRenderer pickerFont = Fonts.getMenuSize(12, Fonts.Type.INTER_MEDIUM);
            for (int index = 0; index < types.length; ++index) {
                PotionEntry entry;
                PotionType type = types[index];
                float rowY = y + 6.0f + (float)index * 19.0f;
                if (index == hoveredRow || type == selected) {
                    int rowColor = index == hoveredRow ? ColorUtil.getClientColor(0.34f) : 1177695808;
                    rectangle.render(ShapeProperties.create(matrices, x + 4.0f, rowY, 156.0, 18.0).round(4.5f).color(rowColor).build());
                }
                class_1799 icon = (entry = this.entries.get((Object)type)) == null ? type.fallbackStack() : entry.displayStack;
                context.method_51428(icon, Math.round(x + 7.0f), Math.round(rowY + 1.0f), 300);
                pickerFont.drawString(matrices, "[\u2605] " + type.displayName, x + 28.0f, rowY + 5.0f, -1973270);
            }
        }

        public boolean method_25402(class_11909 click, boolean doubled) {
            int button = click.comp_4800().comp_4801();
            double mouseX = click.comp_4798();
            double mouseY = click.comp_4799();
            if (this.consumeOpeningMousePress && button == this.openingCode) {
                this.consumeOpeningMousePress = false;
                return true;
            }
            this.consumeOpeningMousePress = false;
            float centerX = (float)this.field_22789 / 2.0f;
            float centerY = (float)this.field_22790 / 2.0f - 6.0f;
            if (this.pickerSlot >= 0) {
                if (button == 0) {
                    float pickerX = centerX - 82.0f;
                    float pickerHeight = 12.0f + (float)PotionType.values().length * 19.0f;
                    float pickerY = centerY - pickerHeight / 2.0f;
                    int row = this.getPickerRow(mouseX, mouseY, pickerX, pickerY);
                    if (row >= 0) {
                        this.slots.set(this.pickerSlot, PotionType.values()[row]);
                        this.owner.saveLayout(this.slots);
                    }
                    this.pickerSlot = -1;
                    return true;
                }
                if (button == 1) {
                    this.pickerSlot = -1;
                    return true;
                }
            }
            int hovered = this.getHoverIndex(mouseX, mouseY, centerX, centerY, this.slots.size());
            if (button == 2) {
                this.slots.add(null);
                this.owner.saveLayout(this.slots);
                return true;
            }
            if (button == 1) {
                if (hovered >= 0) {
                    if (this.slots.get(hovered) != null) {
                        this.slots.set(hovered, null);
                        this.owner.saveLayout(this.slots);
                    } else if (this.slots.size() > 2) {
                        this.slots.remove(hovered);
                        this.owner.saveLayout(this.slots);
                    } else {
                        this.pickerSlot = hovered;
                    }
                }
                return true;
            }
            if (button == 0) {
                if (hovered >= 0) {
                    PotionType type = this.slots.get(hovered);
                    if (type != null) {
                        this.draggedSlot = hovered;
                    } else {
                        this.pickerSlot = hovered;
                    }
                }
                return true;
            }
            return true;
        }

        public boolean method_25406(class_11909 click) {
            if (click.comp_4800().comp_4801() != 0 || this.draggedSlot < 0) {
                return super.method_25406(click);
            }
            float centerX = (float)this.field_22789 / 2.0f;
            float centerY = (float)this.field_22790 / 2.0f - 6.0f;
            int target = this.getHoverIndex(click.comp_4798(), click.comp_4799(), centerX, centerY, this.slots.size());
            int source = this.draggedSlot;
            this.draggedSlot = -1;
            if (target >= 0 && target != source) {
                PotionType first = this.slots.get(source);
                this.slots.set(source, this.slots.get(target));
                this.slots.set(target, first);
                this.owner.saveLayout(this.slots);
            } else {
                PotionType type = this.slots.get(source);
                this.method_25419();
                this.owner.throwPotion(type);
            }
            return true;
        }

        private void releaseSelection() {
            if (this.draggedSlot >= 0) {
                PotionType type = this.slots.get(this.draggedSlot);
                this.draggedSlot = -1;
                this.method_25419();
                this.owner.throwPotion(type);
            } else {
                this.method_25419();
            }
        }

        private boolean isShiftDown() {
            return this.field_22787 != null && (class_3675.method_15987((class_1041)this.field_22787.method_22683(), (int)340) || class_3675.method_15987((class_1041)this.field_22787.method_22683(), (int)344));
        }

        public boolean method_25404(class_11908 input) {
            if (this.consumeOpeningKeyPress && input.comp_4795() == this.openingCode) {
                this.consumeOpeningKeyPress = false;
                return true;
            }
            this.consumeOpeningKeyPress = false;
            if (input.comp_4795() == 256) {
                this.method_25419();
                return true;
            }
            return true;
        }

        public boolean method_25421() {
            return false;
        }

        public void method_25419() {
            if (this.field_22787 != null && this.field_22787.field_1755 == this) {
                this.field_22787.method_1507(null);
            }
        }

        public void method_25432() {
            this.owner.onScreenRemoved(this);
            super.method_25432();
        }

        private int getHoverIndex(double mouseX, double mouseY, float centerX, float centerY, int count) {
            float angle;
            float dx = (float)mouseX - centerX;
            float dy = (float)mouseY - centerY;
            float distance = (float)Math.sqrt(dx * dx + dy * dy);
            if (distance < 18.0f) {
                return -1;
            }
            float segment = (float)Math.PI * 2 / (float)count;
            float start = -1.5707964f - segment / 2.0f;
            for (angle = (float)Math.atan2(dy, dx); angle < start; angle += (float)Math.PI * 2) {
            }
            return Math.floorMod((int)Math.floor((angle - start) / segment), count);
        }

        private int getPickerRow(double mouseX, double mouseY, float x, float y) {
            float height = 12.0f + (float)PotionType.values().length * 19.0f;
            if (mouseX < (double)x || mouseX >= (double)(x + 164.0f) || mouseY < (double)(y + 6.0f) || mouseY >= (double)(y + height - 6.0f)) {
                return -1;
            }
            int row = (int)((mouseY - (double)y - 6.0) / 19.0);
            return row >= 0 && row < PotionType.values().length ? row : -1;
        }

        private float getSegmentCenterAngle(int index, int count) {
            float segment = (float)Math.PI * 2 / (float)count;
            return -1.5707964f + (float)index * segment;
        }
    }

    private static enum PotionType {
        FIRECRACKER("firecracker", "\u0425\u043b\u043e\u043f\u0443\u0448\u043a\u0430", 0xFF0000),
        HOLY_WATER("holy_water", "\u0421\u0432\u044f\u0442\u0430\u044f \u0432\u043e\u0434\u0430", 0xFFFFFF),
        WRATH("wrath", "\u0417\u0435\u043b\u044c\u0435 \u0413\u043d\u0435\u0432\u0430", 0x993333),
        PALADIN("paladin", "\u0417\u0435\u043b\u044c\u0435 \u041f\u0430\u043b\u043b\u0430\u0434\u0438\u043d\u0430", 65535),
        ASSASSIN("assassin", "\u0417\u0435\u043b\u044c\u0435 \u0410\u0441\u0441\u0430\u0441\u0438\u043d\u0430", 0x333333),
        RADIATION("radiation", "\u0417\u0435\u043b\u044c\u0435 \u0420\u0430\u0434\u0438\u0430\u0446\u0438\u0438", 0x32CC32),
        SLEEPING("sleeping", "\u0421\u043d\u043e\u0442\u0432\u043e\u0440\u043d\u043e\u0435", 0x484848);

        private final String id;
        private final String displayName;
        private final int iconColor;
        private class_1799 cachedFallback;

        private PotionType(String id, String displayName, int iconColor) {
            this.id = id;
            this.displayName = displayName;
            this.iconColor = iconColor;
        }

        private boolean matches(class_1799 stack) {
            if (stack == null || stack.method_7960() || stack.method_58694(class_9334.field_49651) == null || !PotionTrackerUtil.isSplashLikePotion(stack)) {
                return false;
            }
            String shown = stack.method_7964().getString().toLowerCase(Locale.ROOT);
            return shown.contains(this.displayName.toLowerCase(Locale.ROOT)) || this.matchesEffects(stack);
        }

        private static PotionType fromStack(class_1799 stack) {
            if (stack == null || stack.method_7960() || stack.method_58694(class_9334.field_49651) == null || !PotionTrackerUtil.isSplashLikePotion(stack)) {
                return null;
            }
            for (PotionType type : PotionType.values()) {
                if (!type.matches(stack)) continue;
                return type;
            }
            return null;
        }

        private boolean matchesEffects(class_1799 stack) {
            class_1844 contents = (class_1844)stack.method_58694(class_9334.field_49651);
            if (contents == null) {
                return false;
            }
            ArrayList<class_1293> effects = new ArrayList<class_1293>();
            contents.method_57397().forEach(effects::add);
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> {
                    if (PotionType.effect(effects, (class_6880<class_1291>)class_1294.field_5909, 9, 200) && PotionType.effect(effects, (class_6880<class_1291>)class_1294.field_5904, 4, 0) && PotionType.effect(effects, (class_6880<class_1291>)class_1294.field_5919, 9, 100) && PotionType.effect(effects, (class_6880<class_1291>)class_1294.field_5912, 0, 3600)) {
                        yield true;
                    }
                    yield false;
                }
                case 1 -> {
                    if (PotionType.effect(effects, (class_6880<class_1291>)class_1294.field_5924, 1, 900) && PotionType.effect(effects, (class_6880<class_1291>)class_1294.field_5905, 1, 12000) && PotionType.effect(effects, (class_6880<class_1291>)class_1294.field_5915, 1, 0)) {
                        yield true;
                    }
                    yield false;
                }
                case 2 -> {
                    if (PotionType.effect(effects, (class_6880<class_1291>)class_1294.field_5910, 4, 600) && PotionType.effect(effects, (class_6880<class_1291>)class_1294.field_5909, 3, 600)) {
                        yield true;
                    }
                    yield false;
                }
                case 3 -> {
                    if (PotionType.effect(effects, (class_6880<class_1291>)class_1294.field_5907, 0, 12000) && PotionType.effect(effects, (class_6880<class_1291>)class_1294.field_5918, 0, 12000) && PotionType.effect(effects, (class_6880<class_1291>)class_1294.field_5914, 2, 1200) && PotionType.effect(effects, (class_6880<class_1291>)class_1294.field_5905, 0, 18000)) {
                        yield true;
                    }
                    yield false;
                }
                case 4 -> {
                    if (PotionType.effect(effects, (class_6880<class_1291>)class_1294.field_5910, 3, 1200) && PotionType.effect(effects, (class_6880<class_1291>)class_1294.field_5904, 2, 6000) && PotionType.effect(effects, (class_6880<class_1291>)class_1294.field_5917, 0, 1200) && PotionType.effect(effects, (class_6880<class_1291>)class_1294.field_5921, 1, 0)) {
                        yield true;
                    }
                    yield false;
                }
                case 5 -> {
                    if (PotionType.effect(effects, (class_6880<class_1291>)class_1294.field_5899, 1, 0) && PotionType.effect(effects, (class_6880<class_1291>)class_1294.field_5920, 1, 0) && PotionType.effect(effects, (class_6880<class_1291>)class_1294.field_5909, 2, 0) && PotionType.effect(effects, (class_6880<class_1291>)class_1294.field_5903, 4, 0) && PotionType.effect(effects, (class_6880<class_1291>)class_1294.field_5912, 0, 0)) {
                        yield true;
                    }
                    yield false;
                }
                case 6 -> PotionType.effect(effects, (class_6880<class_1291>)class_1294.field_5911, 1, 1800) && PotionType.effect(effects, (class_6880<class_1291>)class_1294.field_5901, 1, 200) && PotionType.effect(effects, (class_6880<class_1291>)class_1294.field_5920, 2, 1800) && PotionType.effect(effects, (class_6880<class_1291>)class_1294.field_5919, 0, 200);
            };
        }

        private static boolean effect(List<class_1293> effects, class_6880<class_1291> type, int amplifier, int duration) {
            return effects.stream().anyMatch(effect -> effect.method_5579().equals((Object)type) && effect.method_5578() == amplifier && effect.method_5584() >= duration);
        }

        private class_1799 fallbackStack() {
            if (this.cachedFallback == null) {
                class_1799 stack = class_1802.field_8436.method_7854();
                stack.method_57379(class_9334.field_49631, class_2561.method_43470((String)this.displayName));
                stack.method_57379(class_9334.field_49651, new class_1844(Optional.empty(), Optional.of(this.iconColor), List.of(), Optional.empty()));
                this.cachedFallback = stack;
            }
            return this.cachedFallback.method_46651(1);
        }

        private static PotionType fromId(String value) {
            if (value == null || value.isBlank() || PotionWheelController.EMPTY_SLOT.equals(value)) {
                return null;
            }
            String normalized = value.trim().toLowerCase(Locale.ROOT);
            for (PotionType type : PotionType.values()) {
                if (!type.id.equals(normalized)) continue;
                return type;
            }
            return null;
        }
    }

    private record PotionEntry(class_1799 displayStack, int count) {
    }
}

