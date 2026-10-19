/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1297
 *  net.minecraft.class_1657
 *  net.minecraft.class_1802
 *  net.minecraft.class_2338
 *  net.minecraft.class_2350
 *  net.minecraft.class_2374
 *  net.minecraft.class_2382
 *  net.minecraft.class_243
 *  net.minecraft.class_3965
 *  net.minecraft.class_746
 */
package ru.recoin.implement.features.modules.combat;

import java.util.Comparator;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1802;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2374;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_3965;
import net.minecraft.class_746;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.api.repository.friend.FriendUtils;
import ru.recoin.common.util.task.TaskPriority;
import ru.recoin.implement.events.player.PostMotionEvent;
import ru.recoin.implement.events.player.RotationUpdateEvent;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationConfig;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.SlothSmoothMode;

public class WebTrap
extends Module {
    private final ValueSetting range = new ValueSetting("Range", "Target range").range(2, 6).setValue(5.0f).step(0.1f);
    private final ValueSetting prediction = new ValueSetting("Prediction", "Movement prediction in ticks").range(0, 5).setValue(2.0f).step(1.0f);
    private final RotationConfig rotation = new RotationConfig(new SlothSmoothMode(), true, true);
    private Placement planned;
    private int lastPlaceTick = -100;

    public WebTrap() {
        super("WebTrap", "Web Trap", ModuleCategory.COMBAT);
        this.setup(this.range, this.prediction);
    }

    @EventHandler
    public void onRotation(RotationUpdateEvent event) {
        if (event.getType() != 0) {
            return;
        }
        this.planned = null;
        if (WebTrap.mc.field_1724 == null || WebTrap.mc.field_1687 == null || WebTrap.mc.field_1761 == null || WebTrap.mc.field_1755 != null || WebTrap.mc.field_1724.field_6012 - this.lastPlaceTick < 2) {
            return;
        }
        int slot = -1;
        for (int i = 0; i < 9; ++i) {
            if (!WebTrap.mc.field_1724.method_31548().method_5438(i).method_31574(class_1802.field_8786)) continue;
            slot = i;
            break;
        }
        if (slot < 0) {
            return;
        }
        class_1657 target = WebTrap.mc.field_1687.method_18456().stream().filter(player -> player != WebTrap.mc.field_1724 && player.method_5805() && !FriendUtils.isFriend((class_1297)player)).filter(player -> WebTrap.mc.field_1724.method_5858((class_1297)player) <= (double)(this.range.getValue() * this.range.getValue())).min(Comparator.comparingDouble(arg_0 -> ((class_746)WebTrap.mc.field_1724).method_5858(arg_0))).orElse(null);
        if (target == null) {
            return;
        }
        class_243 predicted = target.method_73189().method_1019(target.method_18798().method_1021((double)this.prediction.getValue()));
        class_2338 feet = class_2338.method_49638((class_2374)predicted);
        Placement place = this.findPlacement(feet, slot);
        if (place == null) {
            place = this.findPlacement(feet.method_10084(), slot);
        }
        if (place == null) {
            return;
        }
        this.planned = place;
        RotationController.INSTANCE.rotateTo(AngleUtil.calculateAngle(place.hit().method_17784()), 2, this.rotation, TaskPriority.HIGH_IMPORTANCE_3, this);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @EventHandler
    public void onPostMotion(PostMotionEvent event) {
        Placement place = this.planned;
        this.planned = null;
        if (place == null || WebTrap.mc.field_1724 == null || WebTrap.mc.field_1687 == null || WebTrap.mc.field_1761 == null || !WebTrap.mc.field_1687.method_8320(place.position()).method_45474() || !WebTrap.mc.field_1724.method_31548().method_5438(place.slot()).method_31574(class_1802.field_8786)) {
            return;
        }
        int previous = WebTrap.mc.field_1724.method_31548().method_67532();
        try {
            WebTrap.mc.field_1724.method_31548().method_61496(place.slot());
            WebTrap.mc.field_1761.method_2896(WebTrap.mc.field_1724, class_1268.field_5808, place.hit());
            WebTrap.mc.field_1724.method_6104(class_1268.field_5808);
            this.lastPlaceTick = WebTrap.mc.field_1724.field_6012;
        }
        finally {
            WebTrap.mc.field_1724.method_31548().method_61496(previous);
        }
    }

    private Placement findPlacement(class_2338 position, int slot) {
        if (!WebTrap.mc.field_1687.method_8320(position).method_45474() || WebTrap.mc.field_1724.method_5707(class_243.method_24953((class_2382)position)) > 20.25) {
            return null;
        }
        for (class_2350 side : class_2350.values()) {
            class_2338 support = position.method_10093(side);
            if (WebTrap.mc.field_1687.method_8320(support).method_45474()) continue;
            class_2350 face = side.method_10153();
            class_243 hit = class_243.method_24953((class_2382)support).method_1019(class_243.method_24954((class_2382)face.method_62675()).method_1021(0.5));
            if (WebTrap.mc.field_1724.method_33571().method_1025(hit) > 20.25) continue;
            return new Placement(position, slot, new class_3965(hit, face, support, false));
        }
        return null;
    }

    @Override
    public void deactivate() {
        this.planned = null;
        RotationController.INSTANCE.release(this);
        super.deactivate();
    }

    private record Placement(class_2338 position, int slot, class_3965 hit) {
    }
}

