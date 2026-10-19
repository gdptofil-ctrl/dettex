/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_1429
 *  net.minecraft.class_1588
 *  net.minecraft.class_1657
 *  net.minecraft.class_238
 */
package ru.recoin.implement.features.modules.combat;

import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1429;
import net.minecraft.class_1588;
import net.minecraft.class_1657;
import net.minecraft.class_238;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.MultiSelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.api.repository.friend.FriendUtils;
import ru.recoin.implement.events.player.BoundingBoxControlEvent;

public class HitBoxModule
extends Module {
    private final MultiSelectSetting targets = new MultiSelectSetting("Targets", "\u0424\u0438\u043b\u044c\u0442\u0440 \u0441\u0443\u0449\u043d\u043e\u0441\u0442\u0435\u0439 \u0434\u043b\u044f \u0440\u0430\u0441\u0448\u0438\u0440\u0435\u043d\u0438\u044f").value("Players", "Mobs", "Animals").selected("Players");
    private final ValueSetting xzExpandSetting = new ValueSetting("XZ Expand", "Allows the box to be extended in the XZ axis").setValue(0.2f).range(0.0f, 3.0f).step(0.05f);
    private final ValueSetting yExpandSetting = new ValueSetting("Y Expand", "Allows the box to be extended in the Y axis").setValue(0.0f).range(0.0f, 3.0f).step(0.05f);

    public HitBoxModule() {
        super("HitBox", ModuleCategory.COMBAT);
        this.setup(this.targets, this.xzExpandSetting, this.yExpandSetting);
    }

    private boolean isValidTarget(class_1309 entity) {
        if (entity == null || entity == HitBoxModule.mc.field_1724 || FriendUtils.isFriend((class_1297)entity)) {
            return false;
        }
        if (entity instanceof class_1657) {
            return this.targets.isSelected("Players");
        }
        if (entity instanceof class_1588) {
            return this.targets.isSelected("Mobs");
        }
        if (entity instanceof class_1429) {
            return this.targets.isSelected("Animals");
        }
        return false;
    }

    @EventHandler
    public void onBoundingBoxControl(BoundingBoxControlEvent event) {
        class_1309 living;
        class_1297 class_12972 = event.getEntity();
        if (class_12972 instanceof class_1309 && this.isValidTarget(living = (class_1309)class_12972)) {
            class_238 box = event.getBox();
            float xzExpand = this.xzExpandSetting.getValue();
            float yExpand = this.yExpandSetting.getValue();
            class_238 changedBox = new class_238(box.field_1323 - (double)(xzExpand / 2.0f), box.field_1322, box.field_1321 - (double)(xzExpand / 2.0f), box.field_1320 + (double)(xzExpand / 2.0f), box.field_1325 + (double)yExpand, box.field_1324 + (double)(xzExpand / 2.0f));
            event.setBox(changedBox);
        }
    }
}

