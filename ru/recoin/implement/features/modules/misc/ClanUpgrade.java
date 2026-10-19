/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 *  net.minecraft.class_1268
 *  net.minecraft.class_1802
 *  net.minecraft.class_2338
 *  net.minecraft.class_2350
 *  net.minecraft.class_2846
 *  net.minecraft.class_2846$class_2847
 *  net.minecraft.class_2885
 *  net.minecraft.class_3965
 */
package ru.recoin.implement.features.modules.misc;

import net.minecraft.class_124;
import net.minecraft.class_1268;
import net.minecraft.class_1802;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2846;
import net.minecraft.class_2885;
import net.minecraft.class_3965;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.entity.PlayerInventoryUtil;
import ru.recoin.common.util.other.StopWatch;
import ru.recoin.common.util.task.TaskPriority;
import ru.recoin.common.util.world.ServerUtil;
import ru.recoin.implement.events.player.RotationUpdateEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.features.draggables.Notifications;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationConfig;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.SlothSmoothMode;

public class ClanUpgrade
extends Module {
    private final StopWatch stopWatch = new StopWatch();
    private boolean shouldLookDown = false;
    private class_2338 targetPos = null;

    public ClanUpgrade() {
        super("ClanUpgrade", "Clan Upgrade", ModuleCategory.MISC);
    }

    @Override
    public void deactivate() {
        this.shouldLookDown = false;
        this.targetPos = null;
        super.deactivate();
    }

    @EventHandler
    public void onTick(TickEvent event) {
        this.shouldLookDown = false;
        this.targetPos = null;
        if (ServerUtil.getWorldType().equals("lobby") && this.stopWatch.every(5000.0)) {
            Notifications.getInstance().addList("\u0412 \u044d\u0442\u043e\u043c \u043c\u0438\u0440\u0435 \u043d\u0435\u043b\u044c\u0437\u044f " + String.valueOf(class_124.field_1061) + "\u043f\u0440\u043e\u043a\u0430\u0447\u0438\u0432\u0430\u0442\u044c " + String.valueOf(class_124.field_1070) + "\u043a\u043b\u0430\u043d", 2500L);
            return;
        }
        int slotId = PlayerInventoryUtil.getHotbarSlotId(slot -> ClanUpgrade.mc.field_1724.method_31548().method_5438(slot).method_31574(class_1802.field_8810) || ClanUpgrade.mc.field_1724.method_31548().method_5438(slot).method_31574(class_1802.field_8530) || ClanUpgrade.mc.field_1724.method_31548().method_5438(slot).method_31574(class_1802.field_8725));
        if (slotId == -1) {
            if (this.stopWatch.every(5000.0)) {
                Notifications.getInstance().addList("\u041d\u0443\u0436\u0435\u043d " + String.valueOf(class_124.field_1061) + "\u0444\u0430\u043a\u0435\u043b, \u0440\u0435\u0434\u0441\u0442\u043e\u0443\u043d\u043e\u0432\u044b\u0439 \u0444\u0430\u043a\u0435\u043b \u0438\u043b\u0438 \u0440\u0435\u0434\u0441\u0442\u043e\u0443\u043d " + String.valueOf(class_124.field_1070) + "\u0432 \u0445\u043e\u0442\u0431\u0430\u0440\u0435", 2500L);
            }
            return;
        }
        if (ClanUpgrade.mc.field_1724.method_31548().method_67532() != slotId) {
            ClanUpgrade.mc.field_1724.method_31548().method_61496(slotId);
            return;
        }
        class_2338 pos = ClanUpgrade.mc.field_1724.method_24515().method_10074();
        if (!ClanUpgrade.mc.field_1687.method_8320(pos).method_51367()) {
            return;
        }
        this.shouldLookDown = true;
        this.targetPos = pos;
        if (RotationController.INSTANCE.getServerAngle().getPitch() >= 89.0f) {
            PlayerIntersectionUtil.sendSequencedPacket(sequence -> new class_2885(class_1268.field_5808, new class_3965(ClanUpgrade.mc.field_1724.method_73189(), class_2350.field_11036, pos, false), sequence));
            PlayerIntersectionUtil.sendSequencedPacket(sequence -> new class_2846(class_2846.class_2847.field_12968, pos.method_10084(), class_2350.field_11036, sequence));
        }
    }

    @EventHandler
    public void onRotationUpdate(RotationUpdateEvent e) {
        if (e.getType() != 0 || !this.shouldLookDown || this.targetPos == null) {
            return;
        }
        RotationController.INSTANCE.rotateTo(AngleUtil.pitch(90.0f), new RotationConfig(new SlothSmoothMode(), true, true), TaskPriority.HIGH_IMPORTANCE_1, this);
    }
}

