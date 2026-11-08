/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_1304
 *  net.minecraft.class_1308
 *  net.minecraft.class_1429
 *  net.minecraft.class_1542
 *  net.minecraft.class_1657
 *  net.minecraft.class_238
 *  net.minecraft.class_243
 *  net.minecraft.class_3532
 *  net.minecraft.class_4184
 *  org.joml.Vector3f
 */
package ru.recoin.implement.features.modules.render;

import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1308;
import net.minecraft.class_1429;
import net.minecraft.class_1542;
import net.minecraft.class_1657;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_4184;
import org.joml.Vector3f;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.MultiSelectSetting;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.render.Render3DUtil;
import ru.recoin.implement.events.render.WorldRenderEvent;

public class Box
extends Module {
    private final MultiSelectSetting targets = new MultiSelectSetting("Targets", "Entities highlighted by boxes").value("Self", "Players", "Naked Players", "Mobs", "Animals", "Items").selected("Players");

    public Box() {
        super("Box", "Box", ModuleCategory.RENDER);
        this.setup(this.targets);
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        if (Box.mc.field_1687 == null || Box.mc.field_1724 == null) {
            return;
        }
        class_4184 camera = Box.mc.field_1773.method_19418();
        class_243 camPos = camera.method_71156();
        Vector3f forward = camera.method_23767().transform(new Vector3f(0.0f, 0.0f, -1.0f));
        float tickDelta = mc.method_61966().method_60637(false);
        int color = ColorUtil.replAlpha(ColorUtil.getClientColor(), 1.0f);
        boolean renderSelf = this.targets.isSelected("Self");
        boolean renderPlayers = this.targets.isSelected("Players");
        boolean renderNakedPlayers = this.targets.isSelected("Naked Players");
        boolean renderItems = this.targets.isSelected("Items");
        boolean renderAnimals = this.targets.isSelected("Animals");
        boolean renderMobs = this.targets.isSelected("Mobs");
        for (class_1297 entity : Box.mc.field_1687.method_18112()) {
            double extent;
            double halfZ;
            double halfY;
            if (!this.shouldRender(entity, renderSelf, renderPlayers, renderNakedPlayers, renderItems, renderAnimals, renderMobs)) continue;
            double ex = entity.method_23317();
            double ey = entity.method_23318();
            double ez = entity.method_23321();
            double dx = class_3532.method_16436((double)tickDelta, (double)entity.field_6038, (double)ex) - ex;
            double dy = class_3532.method_16436((double)tickDelta, (double)entity.field_5971, (double)ey) - ey;
            double dz = class_3532.method_16436((double)tickDelta, (double)entity.field_5989, (double)ez) - ez;
            class_238 box = entity.method_5829().method_989(dx, dy, dz);
            double halfX = box.method_17939() * 0.5;
            double dot = (box.field_1323 + halfX - camPos.field_1352) * (double)forward.x + (box.field_1322 + (halfY = box.method_17940() * 0.5) - camPos.field_1351) * (double)forward.y + (box.field_1321 + (halfZ = box.method_17941() * 0.5) - camPos.field_1350) * (double)forward.z;
            if (dot < -(extent = halfX * (double)Math.abs(forward.x) + halfY * (double)Math.abs(forward.y) + halfZ * (double)Math.abs(forward.z)) - 1.0 && entity != Box.mc.field_1724) continue;
            Render3DUtil.drawBox(box, ru.recoin.implement.features.modules.render.hitcolor.HitColorHitbox.colorFor(entity, color), 1.5f, true, true, false);
        }
    }

    private boolean shouldRender(class_1297 entity, boolean renderSelf, boolean renderPlayers, boolean renderNakedPlayers, boolean renderItems, boolean renderAnimals, boolean renderMobs) {
        if (entity.method_31481() || !entity.method_5805()) {
            return false;
        }
        if (entity == Box.mc.field_1724) {
            return renderSelf && !Box.mc.field_1690.method_31044().method_31034();
        }
        if (entity instanceof class_1657) {
            class_1657 player = (class_1657)entity;
            if (player.method_7325()) {
                return false;
            }
            if (!renderPlayers && !renderNakedPlayers) {
                return false;
            }
            if (renderPlayers && renderNakedPlayers) {
                return true;
            }
            boolean naked = player.method_6118(class_1304.field_6169).method_7960() && player.method_6118(class_1304.field_6174).method_7960() && player.method_6118(class_1304.field_6172).method_7960() && player.method_6118(class_1304.field_6166).method_7960();
            return naked ? renderNakedPlayers : renderPlayers;
        }
        if (entity instanceof class_1542) {
            return renderItems;
        }
        if (entity instanceof class_1429) {
            return renderAnimals;
        }
        return entity instanceof class_1308 && renderMobs;
    }
}

