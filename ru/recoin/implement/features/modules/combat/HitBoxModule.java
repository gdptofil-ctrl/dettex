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
import net.minecraft.class_243;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.MultiSelectSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.api.repository.friend.FriendUtils;
import ru.recoin.implement.events.player.BoundingBoxControlEvent;

public class HitBoxModule
extends Module {
    private final MultiSelectSetting targets = new MultiSelectSetting("Targets", "\u0424\u0438\u043b\u044c\u0442\u0440 \u0441\u0443\u0449\u043d\u043e\u0441\u0442\u0435\u0439 \u0434\u043b\u044f \u0440\u0430\u0441\u0448\u0438\u0440\u0435\u043d\u0438\u044f").value("Players", "Mobs", "Animals").selected("Players");
    private final ValueSetting xzExpandSetting = new ValueSetting("XZ Expand", "Allows the box to be extended in the XZ axis").setValue(0.2f).range(0.0f, 3.0f).step(0.05f);
    private final ValueSetting yExpandSetting = new ValueSetting("Y Expand", "Allows the box to be extended in the Y axis").setValue(0.0f).range(0.0f, 3.0f).step(0.05f);
    private final SelectSetting bypass = new SelectSetting("Bypass", "Keeps the expanded box inside the reach the server accepts (Shard / Shard 2.0)").value("None", "Shard", "Shard 2.0", "Shard 2.1").selected("None");
    private final ValueSetting safeReach = new ValueSetting("Safe Reach", "Maximum real attack distance while the box is expanded").setValue(3.0f).range(3.0f, 4.0f).step(0.05f);

    public HitBoxModule() {
        super("HitBox", ModuleCategory.COMBAT);
        this.setup(this.targets, this.xzExpandSetting, this.yExpandSetting, this.bypass, this.safeReach);
    }

    private float modeReach() {
        if (this.bypass.isSelected("Shard 2.1")) {
            return 3.15f;
        }
        if (this.bypass.isSelected("Shard 2.0")) {
            return 3.09f;
        }
        if (this.bypass.isSelected("Shard")) {
            return 3.05f;
        }
        return this.safeReach.getValue();
    }

    private double distanceToBox(class_243 eye, class_238 box) {
        double x = eye.field_1352 - Math.clamp(eye.field_1352, box.field_1323, box.field_1320);
        double y = eye.field_1351 - Math.clamp(eye.field_1351, box.field_1322, box.field_1325);
        double z = eye.field_1350 - Math.clamp(eye.field_1350, box.field_1321, box.field_1324);
        return Math.sqrt(x * x + y * y + z * z);
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
            if (!this.bypass.isSelected("None") && HitBoxModule.mc.field_1724 != null) {
                // The server measures reach against the real hitbox. Give the expansion only the
                // part of the legal distance that is still left, so an expanded box can never
                // turn into a 4-block reach flag.
                double distance = this.distanceToBox(HitBoxModule.mc.field_1724.method_33571(), box);
                float budget = Math.max(0.0f, this.modeReach() - (float)distance);
                xzExpand = Math.min(xzExpand, budget * 2.0f);
                yExpand = Math.min(yExpand, budget);
            }
            class_238 changedBox = new class_238(box.field_1323 - (double)(xzExpand / 2.0f), box.field_1322, box.field_1321 - (double)(xzExpand / 2.0f), box.field_1320 + (double)(xzExpand / 2.0f), box.field_1325 + (double)yExpand, box.field_1324 + (double)(xzExpand / 2.0f));
            event.setBox(changedBox);
        }
    }
}

