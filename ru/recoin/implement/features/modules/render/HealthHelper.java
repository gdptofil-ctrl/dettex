/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1291
 *  net.minecraft.class_1735
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1844
 *  net.minecraft.class_332
 *  net.minecraft.class_4081
 *  net.minecraft.class_465
 *  net.minecraft.class_9334
 */
package ru.recoin.implement.features.modules.render;

import java.util.stream.StreamSupport;
import net.minecraft.class_1291;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1844;
import net.minecraft.class_332;
import net.minecraft.class_4081;
import net.minecraft.class_465;
import net.minecraft.class_9334;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.ColorSetting;
import ru.recoin.api.feature.module.setting.implement.MultiSelectSetting;
import ru.recoin.implement.events.container.DrawSlotsEvent;

public class HealthHelper
extends Module {
    private final MultiSelectSetting targets = new MultiSelectSetting("\u041f\u0440\u0435\u0434\u043c\u0435\u0442\u044b", "\u041a\u0430\u043a\u0438\u0435 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u044b \u043f\u043e\u0434\u0441\u0432\u0435\u0447\u0438\u0432\u0430\u0442\u044c").value("\u0412\u0440\u0435\u0434\u043d\u044b\u0435 \u0437\u0435\u043b\u044c\u044f", "\u041f\u043e\u043b\u0435\u0437\u043d\u044b\u0435 \u0437\u0435\u043b\u044c\u044f", "\u0417\u043e\u043b\u043e\u0442\u044b\u0435 \u044f\u0431\u043b\u043e\u043a\u0438", "\u0417\u0430\u0447\u0430\u0440\u043e\u0432\u0430\u043d\u043d\u044b\u0435 \u0437\u043e\u043b\u043e\u0442\u044b\u0435 \u044f\u0431\u043b\u043e\u043a\u0438").selected("\u0412\u0440\u0435\u0434\u043d\u044b\u0435 \u0437\u0435\u043b\u044c\u044f", "\u041f\u043e\u043b\u0435\u0437\u043d\u044b\u0435 \u0437\u0435\u043b\u044c\u044f", "\u0417\u043e\u043b\u043e\u0442\u044b\u0435 \u044f\u0431\u043b\u043e\u043a\u0438", "\u0417\u0430\u0447\u0430\u0440\u043e\u0432\u0430\u043d\u043d\u044b\u0435 \u0437\u043e\u043b\u043e\u0442\u044b\u0435 \u044f\u0431\u043b\u043e\u043a\u0438");
    private final ColorSetting usefulPotionColor = new ColorSetting("\u0426\u0432\u0435\u0442 \u043f\u043e\u043b\u0435\u0437\u043d\u044b\u0445 \u0437\u0435\u043b\u0438\u0439", "\u0426\u0432\u0435\u0442 \u043f\u043e\u0434\u0441\u0432\u0435\u0442\u043a\u0438 \u043f\u043e\u043b\u0435\u0437\u043d\u044b\u0445 \u0437\u0435\u043b\u0438\u0439").value(1715273592);

    public HealthHelper() {
        super("HealthHelper", "Health Helper", ModuleCategory.RENDER);
        this.setup(this.targets, this.usefulPotionColor);
    }

    @EventHandler
    public void onDrawSlots(DrawSlotsEvent event) {
        class_465<?> screen = event.getScreen();
        class_332 context = event.getDrawContext();
        for (class_1735 slot : screen.method_17577().field_7761) {
            class_1799 stack = slot.method_7677();
            int color = this.highlightColor(stack);
            if (color == 0) continue;
            context.method_25294(slot.field_7873, slot.field_7872, slot.field_7873 + 16, slot.field_7872 + 16, color);
        }
    }

    private int highlightColor(class_1799 stack) {
        long beneficial;
        if (stack.method_7960()) {
            return 0;
        }
        if (stack.method_31574(class_1802.field_8107) || stack.method_31574(class_1802.field_8236) || stack.method_31574(class_1802.field_8087)) {
            return 0;
        }
        if (stack.method_31574(class_1802.field_8367) && this.targets.isSelected("\u0417\u0430\u0447\u0430\u0440\u043e\u0432\u0430\u043d\u043d\u044b\u0435 \u0437\u043e\u043b\u043e\u0442\u044b\u0435 \u044f\u0431\u043b\u043e\u043a\u0438")) {
            return 1721457151;
        }
        if (stack.method_31574(class_1802.field_8463) && this.targets.isSelected("\u0417\u043e\u043b\u043e\u0442\u044b\u0435 \u044f\u0431\u043b\u043e\u043a\u0438")) {
            return 1728042043;
        }
        class_1844 potion = (class_1844)stack.method_58694(class_9334.field_49651);
        if (potion == null) {
            return 0;
        }
        long harmful = StreamSupport.stream(potion.method_57397().spliterator(), false).filter(effect -> ((class_1291)effect.method_5579().comp_349()).method_18792() == class_4081.field_18272).count();
        if (harmful > (beneficial = StreamSupport.stream(potion.method_57397().spliterator(), false).filter(effect -> ((class_1291)effect.method_5579().comp_349()).method_18792() == class_4081.field_18271).count()) && this.targets.isSelected("\u0412\u0440\u0435\u0434\u043d\u044b\u0435 \u0437\u0435\u043b\u044c\u044f")) {
            return 1728003405;
        }
        if (beneficial > 0L && this.targets.isSelected("\u041f\u043e\u043b\u0435\u0437\u043d\u044b\u0435 \u0437\u0435\u043b\u044c\u044f")) {
            return this.usefulPotionColor.getColor();
        }
        return 0;
    }
}

