/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1306
 *  net.minecraft.class_3532
 *  net.minecraft.class_408
 *  net.minecraft.class_4587
 *  org.lwjgl.glfw.GLFW
 */
package ru.recoin.implement.features.modules.render;

import java.util.List;
import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_3532;
import net.minecraft.class_408;
import net.minecraft.class_4587;
import org.lwjgl.glfw.GLFW;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.draggable.AbstractDraggable;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.other.Instance;
import ru.recoin.core.Main;
import ru.recoin.implement.events.item.HandOffsetEvent;
import ru.recoin.implement.events.render.WorldRenderEvent;
import ru.recoin.implement.features.modules.render.Hud;
import ru.recoin.implement.features.modules.render.viewmodel.HandDragGeometry;

public class ViewModel
extends Module {
    private final transient HandDragGeometry handGeometry = new HandDragGeometry();
    private transient HandDragGeometry.Region dragGeometry;
    private final SelectSetting controlMode = new SelectSetting("Mode", "Adjustment method").value("Sliders", "Cursor").selected("Cursor");
    private final ValueSetting mainX = ViewModel.value("Main Hand X", -1.0f, 1.0f, 0.0f);
    private final ValueSetting mainY = ViewModel.value("Main Hand Y", -1.0f, 1.0f, 0.0f);
    private final ValueSetting mainZ = ViewModel.value("Main Hand Z", -2.5f, 2.5f, 0.0f);
    private final ValueSetting offX = ViewModel.value("Off Hand X", -1.0f, 1.0f, 0.0f);
    private final ValueSetting offY = ViewModel.value("Off Hand Y", -1.0f, 1.0f, 0.0f);
    private final ValueSetting offZ = ViewModel.value("Off Hand Z", -2.5f, 2.5f, 0.0f);
    private final ValueSetting mainSize = ViewModel.value("Main Hand Size", 0.1f, 2.0f, 1.0f);
    private final ValueSetting offSize = ViewModel.value("Off Hand Size", 0.1f, 2.0f, 1.0f);
    private transient boolean dragging;
    private transient boolean wasDown;
    private transient boolean draggingMain;
    private transient double dragStartMouseX;
    private transient double dragStartMouseY;
    private transient float dragStartValueX;
    private transient float dragStartValueY;
    private static final float SIZE_STEP = 0.05f;

    public HandDragGeometry getHandGeometry() {
        return this.handGeometry;
    }

    public ViewModel() {
        super("ViewModel", "View Model", ModuleCategory.RENDER);
        this.setup(this.controlMode, this.mainX, this.mainY, this.mainZ, this.offX, this.offY, this.offZ, this.mainSize, this.offSize);
    }

    private static ValueSetting value(String name, float min, float max, float current) {
        return new ValueSetting(name, name).setValue(current).range(min, max).step(0.001f);
    }

    public static ViewModel getInstance() {
        return Instance.get(ViewModel.class);
    }

    @Override
    public void activate() {
        this.handGeometry.reset();
        this.dragGeometry = null;
        this.dragging = false;
        this.wasDown = false;
        this.draggingMain = false;
    }

    @Override
    public void deactivate() {
        this.handGeometry.reset();
        this.dragGeometry = null;
        this.dragging = false;
        this.wasDown = false;
        this.draggingMain = false;
    }

    public boolean isHoveringHandRegion(double mouseX, double mouseY, boolean mainHand) {
        if (this.isHudElementHovered(mouseX, mouseY)) {
            return false;
        }
        if (ViewModel.mc.field_1724 == null) {
            return false;
        }
        if (!mainHand && ViewModel.mc.field_1724.method_6079().method_7960()) {
            return false;
        }
        HandDragGeometry.Region region = this.handGeometry.region(mainHand);
        return region != null && region.draggable() && region.contains(mouseX, mouseY);
    }

    public boolean isHudElementHovered(double mouseX, double mouseY) {
        Hud hud = Hud.getInstance();
        if (hud == null) {
            return false;
        }
        List<AbstractDraggable> draggables = Main.getInstance().getDraggableRepository().draggable();
        for (AbstractDraggable draggable : draggables) {
            if (!draggable.canDraw(hud, draggable) || !draggable.isHovered(mouseX, mouseY)) continue;
            return true;
        }
        return false;
    }

    public boolean onMouseScroll(double mouseX, double mouseY, double verticalAmount) {
        if (!this.isState() || !this.controlMode.isSelected("Cursor") || verticalAmount == 0.0) {
            return false;
        }
        boolean hoveringMain = this.isHoveringHandRegion(mouseX, mouseY, true);
        boolean hoveringOff = this.isHoveringHandRegion(mouseX, mouseY, false);
        if (!hoveringMain && !hoveringOff) {
            return false;
        }
        ValueSetting sizeSetting = hoveringMain ? this.mainSize : this.offSize;
        double direction = verticalAmount > 0.0 ? 1.0 : -1.0;
        float newSize = (float)((double)sizeSetting.getValue() + direction * (double)0.05f);
        newSize = class_3532.method_15363((float)newSize, (float)0.1f, (float)2.0f);
        sizeSetting.setValue(newSize);
        return true;
    }

    public boolean onMouseClick(double mouseX, double mouseY, int button) {
        if (!this.isState() || !this.controlMode.isSelected("Cursor") || button != 2) {
            return false;
        }
        boolean hoveringMain = this.isHoveringHandRegion(mouseX, mouseY, true);
        boolean hoveringOff = this.isHoveringHandRegion(mouseX, mouseY, false);
        if (!hoveringMain && !hoveringOff) {
            return false;
        }
        if (hoveringMain) {
            this.mainX.setValue(0.0f);
            this.mainY.setValue(0.0f);
            this.mainZ.setValue(0.0f);
            this.mainSize.setValue(1.0f);
        } else {
            this.offX.setValue(0.0f);
            this.offY.setValue(0.0f);
            this.offZ.setValue(0.0f);
            this.offSize.setValue(1.0f);
        }
        return true;
    }

    public float getHandScale(class_1268 hand) {
        if (!this.isState()) {
            return 1.0f;
        }
        return hand == class_1268.field_5808 ? this.mainSize.getValue() : this.offSize.getValue();
    }

    @EventHandler
    public void onHandOffset(HandOffsetEvent event) {
        class_4587 matrix = event.getMatrices();
        this.handGeometry.captureTranslation(event.getHand(), matrix);
        if (event.getHand() == class_1268.field_5808) {
            matrix.method_46416(this.mainX.getValue(), this.mainY.getValue(), this.mainZ.getValue());
            float scale = this.mainSize.getValue();
            if (scale != 1.0f) {
                boolean right = ViewModel.mc.field_1724 == null || ViewModel.mc.field_1724.method_6068() == class_1306.field_6183;
                boolean empty = event.getStack() == null || event.getStack().method_7960();
                float px = (right ? 1.0f : -1.0f) * (empty ? 0.64f : 0.56f);
                float py = empty ? -0.6f : -0.52f;
                float pz = -0.72f;
                matrix.method_46416(px, py, pz);
                matrix.method_22905(scale, scale, scale);
                matrix.method_46416(-px, -py, -pz);
            }
        } else {
            matrix.method_46416(this.offX.getValue(), this.offY.getValue(), this.offZ.getValue());
            float scale = this.offSize.getValue();
            if (scale != 1.0f) {
                boolean right = ViewModel.mc.field_1724 != null && ViewModel.mc.field_1724.method_6068() == class_1306.field_6182;
                boolean empty = event.getStack() == null || event.getStack().method_7960();
                float px = (right ? 1.0f : -1.0f) * (empty ? 0.64f : 0.56f);
                float py = empty ? -0.6f : -0.52f;
                float pz = -0.72f;
                matrix.method_46416(px, py, pz);
                matrix.method_22905(scale, scale, scale);
                matrix.method_46416(-px, -py, -pz);
            }
        }
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        boolean lmbDown;
        if (!this.controlMode.isSelected("Cursor")) {
            this.dragging = false;
            this.wasDown = false;
            return;
        }
        long windowHandle = mc.method_22683().method_4490();
        if (!(ViewModel.mc.field_1755 instanceof class_408)) {
            this.dragging = false;
            this.wasDown = false;
            return;
        }
        double[] xBuffer = new double[1];
        double[] yBuffer = new double[1];
        GLFW.glfwGetCursorPos((long)windowHandle, (double[])xBuffer, (double[])yBuffer);
        double rawX = xBuffer[0];
        double rawY = yBuffer[0];
        double guiScale = mc.method_22683().method_4495();
        double guiMouseX = rawX / guiScale;
        double guiMouseY = rawY / guiScale;
        boolean hoveringMain = this.isHoveringHandRegion(guiMouseX, guiMouseY, true);
        boolean hoveringOff = this.isHoveringHandRegion(guiMouseX, guiMouseY, false);
        boolean bl = lmbDown = GLFW.glfwGetMouseButton((long)windowHandle, (int)0) == 1;
        if (lmbDown && !this.wasDown && (hoveringMain || hoveringOff)) {
            this.dragging = true;
            this.draggingMain = hoveringMain;
            this.dragStartMouseX = rawX;
            this.dragStartMouseY = rawY;
            ValueSetting xSetting = this.draggingMain ? this.mainX : this.offX;
            ValueSetting ySetting = this.draggingMain ? this.mainY : this.offY;
            this.dragStartValueX = xSetting.getValue();
            this.dragStartValueY = ySetting.getValue();
            this.dragGeometry = this.handGeometry.region(this.draggingMain);
        }
        if (this.dragging && lmbDown) {
            ValueSetting ySetting;
            double deltaGuiX = (rawX - this.dragStartMouseX) / Math.max(1.0, guiScale);
            double deltaGuiY = (rawY - this.dragStartMouseY) / Math.max(1.0, guiScale);
            ValueSetting xSetting = this.draggingMain ? this.mainX : this.offX;
            ValueSetting valueSetting = ySetting = this.draggingMain ? this.mainY : this.offY;
            if (this.dragGeometry == null || !this.dragGeometry.draggable()) {
                this.dragging = false;
                return;
            }
            float newX = (float)((double)this.dragStartValueX + this.dragGeometry.localX(deltaGuiX, deltaGuiY));
            float newY = (float)((double)this.dragStartValueY + this.dragGeometry.localY(deltaGuiX, deltaGuiY));
            xSetting.setValue(class_3532.method_15363((float)newX, (float)-1.0f, (float)1.0f));
            ySetting.setValue(class_3532.method_15363((float)newY, (float)-1.0f, (float)1.0f));
        }
        if (!lmbDown) {
            this.dragging = false;
        }
        this.wasDown = lmbDown;
    }
}

