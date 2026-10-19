/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_1676
 *  net.minecraft.class_1684
 *  net.minecraft.class_1735
 *  net.minecraft.class_1802
 *  net.minecraft.class_1937
 *  net.minecraft.class_239
 *  net.minecraft.class_243
 */
package ru.recoin.implement.features.modules.combat;

import java.util.Comparator;
import java.util.Objects;
import java.util.stream.IntStream;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1676;
import net.minecraft.class_1684;
import net.minecraft.class_1735;
import net.minecraft.class_1802;
import net.minecraft.class_1937;
import net.minecraft.class_239;
import net.minecraft.class_243;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BindSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.api.repository.friend.FriendUtils;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.entity.PlayerInventoryUtil;
import ru.recoin.common.util.entity.ScreenWalkInventorySync;
import ru.recoin.common.util.entity.SimulatedPlayer;
import ru.recoin.common.util.other.StopWatch;
import ru.recoin.common.util.task.TaskPriority;
import ru.recoin.common.util.task.scripts.Script;
import ru.recoin.implement.events.player.EntitySpawnEvent;
import ru.recoin.implement.events.player.PostMotionEvent;
import ru.recoin.implement.events.player.RotationUpdateEvent;
import ru.recoin.implement.features.modules.combat.Aura;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationConfig;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.SlothSmoothMode;
import ru.recoin.implement.features.modules.render.ProjectilePrediction;

public class TargetPearl
extends Module {
    private final StopWatch stopWatch = new StopWatch();
    private final Script script = new Script();
    private Angle pendingAngle;
    private final SelectSetting modeSetting = new SelectSetting("Mode", "When will target pearl work").value("Bind", "Always").selected("Always");
    private final SelectSetting targetSetting = new SelectSetting("Targets", "Targets for which pearls will be thrown").value("Aura Target", "All").selected("All");
    private final BindSetting throwSetting = new BindSetting("Throw", "Throw Key").visible(() -> this.modeSetting.isSelected("Bind"));
    private final ValueSetting distanceSetting = new ValueSetting("Distance", "Minimum distance to the enemy pearl's predicted landing point").setValue(10.0f).range(8, 20);

    public TargetPearl() {
        super("TargetPearl", "Target Pearl", ModuleCategory.COMBAT);
        this.setup(this.modeSetting, this.targetSetting, this.throwSetting, this.distanceSetting);
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent e) {
        class_1684 pearl;
        class_1297 class_12972;
        if (TargetPearl.mc.field_1687 != null && (class_12972 = e.getEntity()) instanceof class_1684 && (pearl = (class_1684)class_12972).method_24921() == null) {
            TargetPearl.mc.field_1687.method_18456().stream().filter(p -> (double)p.method_5739((class_1297)pearl) <= 4.5).min(Comparator.comparingDouble(p -> p.method_5739((class_1297)pearl))).ifPresent(arg_0 -> ((class_1684)pearl).method_7432(arg_0));
        }
    }

    @EventHandler
    public void onRotationUpdate(RotationUpdateEvent e) {
        if (e.getType() == 0) {
            if (this.pendingAngle != null) {
                RotationController.INSTANCE.rotateTo(this.pendingAngle, 1, new RotationConfig(new SlothSmoothMode(), true, true), TaskPriority.HIGH_IMPORTANCE_3, this);
                return;
            }
            if (TargetPearl.mc.field_1724 == null || TargetPearl.mc.field_1687 == null || TargetPearl.mc.field_1755 != null || TargetPearl.mc.field_1724.method_6115() || TargetPearl.mc.field_1724.method_6128() || TargetPearl.mc.field_1724.method_7357().method_7904(class_1802.field_8634.method_7854())) {
                return;
            }
            class_1309 target = Aura.getInstance().getLastTarget();
            class_1735 slot = PlayerInventoryUtil.getSlot(class_1802.field_8634);
            if (slot == null || !this.stopWatch.finished(2000.0)) {
                return;
            }
            if (this.modeSetting.isSelected("Bind") && !PlayerIntersectionUtil.isKey(this.throwSetting)) {
                return;
            }
            if (PlayerIntersectionUtil.streamEntities().filter(class_1684.class::isInstance).map(class_1684.class::cast).anyMatch(pearl -> Objects.equals(pearl.method_24921(), TargetPearl.mc.field_1724))) {
                this.stopWatch.reset();
                return;
            }
            ProjectilePrediction prediction = ProjectilePrediction.getInstance();
            PlayerIntersectionUtil.streamEntities().filter(class_1684.class::isInstance).map(class_1684.class::cast).filter(pearl -> pearl.method_24921() != null && pearl.method_24921() != TargetPearl.mc.field_1724 && !FriendUtils.isFriend(pearl.method_24921()) && (this.targetSetting.isSelected("All") || target != null && target.equals((Object)pearl.method_24921()))).filter(pearl -> prediction.calcTrajectory((class_1676)pearl) != null).min(Comparator.comparingDouble(pearl -> TargetPearl.mc.field_1724.method_5707(prediction.calcTrajectory((class_1676)pearl).method_17784()))).ifPresent(pearl -> {
                class_239 targetResult = prediction.calcTrajectory((class_1676)pearl);
                if (targetResult == null || TargetPearl.mc.field_1724.method_73189().method_1022(targetResult.method_17784()) <= (double)this.distanceSetting.getInt() || TargetPearl.mc.field_1724.method_73189().method_1022(targetResult.method_17784()) > 120.0) {
                    return;
                }
                class_243 eyePos = TargetPearl.mc.field_1724.method_33571().method_1019(TargetPearl.mc.field_1724.method_73189().method_1020(SimulatedPlayer.simulateLocalPlayer((int)1).pos));
                float yaw = AngleUtil.fromVec3d(targetResult.method_17784().method_1020(eyePos)).getYaw();
                IntStream.range(-89, 89).mapToObj(pitch -> new Angle(yaw, pitch)).filter(angle -> {
                    class_239 playerResult = prediction.checkTrajectory(angle.toVector(), (class_1676)new class_1684((class_1937)TargetPearl.mc.field_1687, (class_1309)TargetPearl.mc.field_1724, slot.method_7677()), 1.5);
                    return playerResult != null && playerResult.method_17784().method_1022(targetResult.method_17784()) <= 3.0;
                }).max(Comparator.comparingDouble(Angle::getPitch)).ifPresent(angle -> {
                    this.pendingAngle = angle;
                    RotationController.INSTANCE.rotateTo((Angle)angle, 1, new RotationConfig(new SlothSmoothMode(), true, true), TaskPriority.HIGH_IMPORTANCE_3, this);
                });
            });
        }
    }

    @EventHandler
    public void onPostMotion(PostMotionEvent e) {
        this.script.update();
        if (this.pendingAngle == null || TargetPearl.mc.field_1724 == null || TargetPearl.mc.field_1687 == null) {
            return;
        }
        class_1735 slot = PlayerInventoryUtil.getSlot(class_1802.field_8634);
        if (slot == null || TargetPearl.mc.field_1724.method_7357().method_7904(class_1802.field_8634.method_7854())) {
            this.cancelThrow();
            return;
        }
        Angle angle = this.pendingAngle;
        ScreenWalkInventorySync.swapUseRestore(slot, () -> {
            RotationController.setForcedInteractAngle(angle);
            try {
                PlayerIntersectionUtil.interactItemVanilla(class_1268.field_5808, angle);
            }
            finally {
                RotationController.setForcedInteractAngle(null);
            }
        });
        this.stopWatch.reset();
        this.cancelThrow();
    }

    private void cancelThrow() {
        this.pendingAngle = null;
        RotationController.INSTANCE.release(this);
    }

    @Override
    public void deactivate() {
        this.cancelThrow();
        super.deactivate();
    }
}

