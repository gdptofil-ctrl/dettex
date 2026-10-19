/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1922
 *  net.minecraft.class_2246
 *  net.minecraft.class_2338
 *  net.minecraft.class_2338$class_2339
 *  net.minecraft.class_2350
 *  net.minecraft.class_243
 *  net.minecraft.class_265
 *  net.minecraft.class_2680
 */
package ru.recoin.implement.features.modules.player;

import net.minecraft.class_1268;
import net.minecraft.class_1922;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.render.Render3DUtil;
import ru.recoin.common.util.task.TaskPriority;
import ru.recoin.implement.events.player.RotationUpdateEvent;
import ru.recoin.implement.events.render.WorldRenderEvent;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationConfig;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;

public class Nuker
extends Module {
    private static final class_2338 MINE_MIN = new class_2338(-68, 69, 24);
    private static final class_2338 MINE_MAX = new class_2338(-48, 78, 44);
    public class_2338 pos;
    private class_265 shape;
    private final BooleanSetting rotateSetting = new BooleanSetting("Rotate", "Break blocks lower than the player").setValue(true);
    private final SelectSetting mode = new SelectSetting("Mode", "Nuker target area").value("Normal", "FunTime Mine").selected("Normal");
    private final BooleanSetting downSetting = new BooleanSetting("Down", "Break blocks lower than the player").setValue(true);
    private final ValueSetting radiusSetting = new ValueSetting("Radius", "Breaks blocks in a radius around you").setValue(3.0f).range(1, 6);

    public Nuker() {
        super("Nuker", ModuleCategory.PLAYER);
        this.setup(this.mode, this.rotateSetting, this.downSetting, this.radiusSetting);
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent e) {
        if (this.pos != null && this.shape != null && !this.shape.method_1110()) {
            Render3DUtil.drawShape(this.pos, this.shape, ColorUtil.getClientColor(), 2.0f);
        }
    }

    @EventHandler
    public void onRotationUpdate(RotationUpdateEvent e) {
        if (e.getType() == 0) {
            class_2338 center = Nuker.mc.field_1724.method_24515();
            int radius = this.radiusSetting.getInt();
            int minimumY = this.downSetting.isValue() ? center.method_10264() - radius : center.method_10264();
            boolean mineOnly = this.mode.isSelected("FunTime Mine");
            class_243 playerPos = Nuker.mc.field_1724.method_73189();
            class_2338.class_2339 candidate = new class_2338.class_2339();
            class_2338 best = null;
            double bestPriority = Double.POSITIVE_INFINITY;
            for (int x = center.method_10263() - radius; x <= center.method_10263() + radius; ++x) {
                for (int z = center.method_10260() - radius; z <= center.method_10260() + radius; ++z) {
                    for (int y = minimumY; y <= center.method_10264() + radius; ++y) {
                        double priority;
                        if (!this.insideMine(x, y, z, mineOnly)) continue;
                        candidate.method_10103(x, y, z);
                        class_2680 state = Nuker.mc.field_1687.method_8320((class_2338)candidate);
                        if (!this.validBlock(state) || !((priority = this.blockPriority((class_2338)candidate, state, playerPos)) < bestPriority)) continue;
                        bestPriority = priority;
                        best = candidate.method_10062();
                    }
                }
            }
            this.pos = best;
            if (this.pos != null) {
                if (this.rotateSetting.isValue()) {
                    RotationController.INSTANCE.rotateTo(AngleUtil.calculateAngle(this.pos.method_46558()), RotationConfig.DEFAULT, TaskPriority.HIGH_IMPORTANCE_1, this);
                }
                this.shape = Nuker.mc.field_1687.method_8320(this.pos).method_26218((class_1922)Nuker.mc.field_1687, this.pos);
                Nuker.mc.field_1761.method_2902(this.pos, class_2350.field_11036);
                Nuker.mc.field_1724.method_6104(class_1268.field_5808);
            }
        }
    }

    private double blockPriority(class_2338 pos, class_2680 state, class_243 playerPos) {
        return switch (state.method_26204().method_63499().replace("block.minecraft.", "")) {
            case "ancient_debris" -> 0.0;
            case "diamond_ore" -> 1.0;
            case "emerald_ore" -> 2.0;
            case "gold_ore" -> 3.0;
            case "iron_ore" -> 4.0;
            case "lapis_ore" -> 5.0;
            case "redstone_ore" -> 6.0;
            default -> {
                double dx = (double)pos.method_10263() + 0.5 - playerPos.field_1352;
                double dy = (double)pos.method_10264() + 0.5 - playerPos.field_1351;
                double dz = (double)pos.method_10260() + 0.5 - playerPos.field_1350;
                yield dx * dx + dy * dy + dz * dz;
            }
        };
    }

    private boolean validBlock(class_2680 state) {
        return !PlayerIntersectionUtil.isAir(state) && state.method_26204() != class_2246.field_10382 && state.method_26204() != class_2246.field_10164 && state.method_26204() != class_2246.field_9987 && state.method_26204() != class_2246.field_10499;
    }

    private boolean insideMine(int x, int y, int z, boolean mineOnly) {
        return !mineOnly || x >= MINE_MIN.method_10263() && x <= MINE_MAX.method_10263() && y >= MINE_MIN.method_10264() && y <= MINE_MAX.method_10264() && z >= MINE_MIN.method_10260() && z <= MINE_MAX.method_10260();
    }

    public class_2338 getPos() {
        return this.pos;
    }

    public class_265 getShape() {
        return this.shape;
    }

    public BooleanSetting getRotateSetting() {
        return this.rotateSetting;
    }

    public SelectSetting getMode() {
        return this.mode;
    }

    public BooleanSetting getDownSetting() {
        return this.downSetting;
    }

    public ValueSetting getRadiusSetting() {
        return this.radiusSetting;
    }
}

