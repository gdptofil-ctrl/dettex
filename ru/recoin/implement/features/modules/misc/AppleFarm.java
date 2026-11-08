/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1269
 *  net.minecraft.class_1297
 *  net.minecraft.class_1657
 *  net.minecraft.class_1703
 *  net.minecraft.class_1707
 *  net.minecraft.class_1713
 *  net.minecraft.class_1735
 *  net.minecraft.class_1792
 *  net.minecraft.class_1794
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1922
 *  net.minecraft.class_2246
 *  net.minecraft.class_2338
 *  net.minecraft.class_2350
 *  net.minecraft.class_2350$class_2351
 *  net.minecraft.class_2382
 *  net.minecraft.class_239
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_243
 *  net.minecraft.class_2561
 *  net.minecraft.class_2586
 *  net.minecraft.class_2595
 *  net.minecraft.class_2625
 *  net.minecraft.class_2680
 *  net.minecraft.class_3481
 *  net.minecraft.class_3532
 *  net.minecraft.class_3959
 *  net.minecraft.class_3959$class_242
 *  net.minecraft.class_3959$class_3960
 *  net.minecraft.class_3965
 *  net.minecraft.class_9279
 *  net.minecraft.class_9334
 */
package ru.recoin.implement.features.modules.misc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1703;
import net.minecraft.class_1707;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1792;
import net.minecraft.class_1794;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1922;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2382;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2586;
import net.minecraft.class_2595;
import net.minecraft.class_2625;
import net.minecraft.class_2680;
import net.minecraft.class_3481;
import net.minecraft.class_3532;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_9279;
import net.minecraft.class_9334;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.common.QuickLogger;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.entity.PlayerInventoryUtil;
import ru.recoin.common.util.other.StopWatch;
import ru.recoin.common.util.task.TaskPriority;
import ru.recoin.implement.events.player.RotationUpdateEvent;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationConfig;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.AngleSmoothMode;

public class AppleFarm
extends Module {
    private static final List<class_1792> SAPLINGS = List.of(class_1802.field_17535, class_1802.field_17536, class_1802.field_17537, class_1802.field_17538, class_1802.field_17539, class_1802.field_17540, class_1802.field_42688, class_1802.field_37508);
    private static final int OFFHAND_BUTTON = 40;
    private static final int OFFHAND_SLOT_ID = 45;
    private static final int ACTION_DELAY = 0;
    private static final int BREAK_DELAY = 0;
    private static final int TOOL_SWITCH_DELAY = 0;
    private static final int SCAN_RADIUS = 10;
    private static final int SCAN_HEIGHT = 20;
    private static final float MAX_DISTANCE = 5.0f;
    private static final float INTERACT_TOLERANCE = 16.0f;
    private static final float REPAIR_THRESHOLD = 15.0f;
    private static final int REPAIR_USE_DELAY = 50;
    private static final int SUPPLY_SEARCH_RADIUS = 12;
    private static final long REFILL_CLICK_DELAY = 120L;
    private static final long REFILL_OPEN_TIMEOUT = 3000L;
    private static final long REFILL_RETRY_DELAY = 2500L;
    private static final String REPAIR_BOTTLE_MARKER = "\u043f\u043e\u043b\u043d\u043e\u0441\u0442\u044c\u044e \u0440\u0435\u043c\u043e\u043d\u0442\u0438\u0440\u0443\u0435\u0442";
    private static final double[][] FACE_OFFSETS = new double[][]{{0.0, 0.0}, {0.25, 0.0}, {-0.25, 0.0}, {0.0, 0.25}, {0.0, -0.25}};
    private final AngleSmoothMode rotationMode = new AngleSmoothMode("AppleFarm"){

        @Override
        public Angle limitAngleChange(Angle current, Angle target, class_243 aimPoint, class_1297 entity) {
            Angle delta = AngleUtil.calculateDelta(current, target);
            float yawSpeed = AppleFarm.this.randomRotationSpeed();
            float pitchSpeed = AppleFarm.this.randomRotationSpeed();
            return new Angle(current.getYaw() + class_3532.method_15363((float)delta.getYaw(), (float)(-yawSpeed), (float)yawSpeed), class_3532.method_15363((float)(current.getPitch() + class_3532.method_15363((float)delta.getPitch(), (float)(-pitchSpeed), (float)pitchSpeed)), (float)-90.0f, (float)90.0f));
        }

        @Override
        public class_243 randomValue() {
            return class_243.field_1353;
        }
    };
    private final RotationConfig rotationConfig = new RotationConfig(this.rotationMode, true, true);
    private final StopWatch actionTimer = new StopWatch();
    private final StopWatch notifyTimer = new StopWatch();
    private final StopWatch toolSwitchTimer = new StopWatch();
    private final StopWatch breakTimer = new StopWatch();
    private final StopWatch repairTimer = new StopWatch();
    private class_2338 farmPos;
    private class_2338 currentBreakTarget;
    private class_2350 currentBreakSide;
    private class_243 currentAimPos;
    private int targetStuckTicks;
    private final Set<class_2338> unreachableBlocks = new HashSet<class_2338>();
    private long lastBlacklistClearTime;
    private boolean repairingHoe;
    private int repairHoeSourceSlotId = -1;
    private int repairBottleSourceSlotId = -1;
    private int repairSelectedHotbarSlot = -1;
    private Angle requestedRotation;
    private PendingAction pendingAction = PendingAction.NONE;
    private RefillState refillState = RefillState.IDLE;
    private RefillItem refillItem;
    private class_2338 supplyChest;
    private long refillStateSince;
    private long refillNextAction;
    private long refillRetryAt;

    private float randomRotationSpeed() {
        return 30.0f + (float)Math.random() * 50.0f;
    }

    private double getBreakRange() {
        return AppleFarm.mc.field_1724 != null ? Math.min(AppleFarm.mc.field_1724.method_55754(), 4.5) : 4.5;
    }

    public AppleFarm() {
        super("AppleFarm", "Apple Farm", ModuleCategory.PVE);
    }

    @Override
    public void activate() {
        this.resetState();
        this.actionTimer.reset();
        this.notifyTimer.setMs(3000L);
        this.toolSwitchTimer.setMs(0L);
        this.breakTimer.setMs(0L);
        this.repairTimer.setMs(50L);
        this.farmPos = this.resolveFarmPos();
        if (this.farmPos == null) {
            this.chat("\u00a7cAppleFarm: \u043d\u0435 \u043d\u0430\u0448\u0451\u043b \u0442\u043e\u0447\u043a\u0443 \u0444\u0435\u0440\u043c\u044b. \u041d\u0430\u0432\u0435\u0434\u0438\u0441\u044c \u043d\u0430 \u0437\u0435\u043c\u043b\u044e, \u043a\u0443\u0434\u0430 \u043c\u043e\u0436\u043d\u043e \u043f\u043e\u0441\u0430\u0434\u0438\u0442\u044c \u0434\u0435\u0440\u0435\u0432\u043e.");
            this.setState(false);
            return;
        }
        this.chat("\u00a7aAppleFarm \u0432\u043a\u043b\u044e\u0447\u0451\u043d: \u00a7f" + this.farmPos.method_23854());
    }

    @Override
    public void deactivate() {
        this.stopRepairing();
        if (AppleFarm.mc.field_1724 != null && AppleFarm.mc.field_1724.field_7512 != AppleFarm.mc.field_1724.field_7498) {
            AppleFarm.mc.field_1724.method_7346();
        }
        RotationController.INSTANCE.release(this);
        this.resetState();
    }

    @EventHandler
    public void onRotationUpdate(RotationUpdateEvent event) {
        if (event.getType() == 0) {
            this.updateActions();
            if (this.requestedRotation != null) {
                RotationController.INSTANCE.rotateTo(this.requestedRotation, 2, this.rotationConfig, TaskPriority.HIGH_IMPORTANCE_1, this);
            }
            return;
        }
        if (event.getType() == 2) {
            this.executeActions();
        }
    }

    private void updateActions() {
        if (AppleFarm.mc.field_1724 == null || AppleFarm.mc.field_1687 == null || AppleFarm.mc.field_1761 == null) {
            return;
        }
        this.requestedRotation = null;
        this.pendingAction = PendingAction.NONE;
        if (System.currentTimeMillis() - this.lastBlacklistClearTime > 3000L) {
            if (!this.unreachableBlocks.isEmpty()) {
                this.unreachableBlocks.clear();
            }
            this.lastBlacklistClearTime = System.currentTimeMillis();
        }
        if (this.farmPos == null) {
            this.farmPos = this.resolveFarmPos();
            if (this.farmPos == null) {
                return;
            }
        }
        double maxReach = this.getBreakRange() + 1.0;
        if (AppleFarm.mc.field_1724.method_33571().method_1025(this.farmPos.method_46558()) > maxReach * maxReach) {
            this.notify("\u00a7eAppleFarm: \u0442\u044b \u0434\u0430\u043b\u0435\u043a\u043e \u043e\u0442 \u0442\u043e\u0447\u043a\u0438 \u0444\u0435\u0440\u043c\u044b.");
            return;
        }
        if (this.refillState != RefillState.IDLE) {
            this.handleRefill();
            return;
        }
        if (this.handleHoeRepair()) {
            return;
        }
        if (this.hasTree()) {
            this.prepareBreakTree();
            return;
        }
        this.clearBreakingTarget();
        if (!this.unreachableBlocks.isEmpty()) {
            this.unreachableBlocks.clear();
        }
        if (!this.actionTimer.finished(0.0)) {
            return;
        }
        this.actionTimer.reset();
        if (!this.hasSaplingPlanted()) {
            if (this.findSaplingItem() == class_1802.field_8162) {
                this.requestRefill(RefillItem.SAPLING);
                return;
            }
            this.preparePlantSapling();
            return;
        }
        if (!this.hasInventoryItem(class_1802.field_8324)) {
            this.requestRefill(RefillItem.BONE_MEAL);
            return;
        }
        this.prepareUseBoneMeal();
    }

    private void executeActions() {
        if (AppleFarm.mc.field_1724 == null || AppleFarm.mc.field_1687 == null || AppleFarm.mc.field_1761 == null) {
            return;
        }
        if (this.repairingHoe || this.refillState != RefillState.IDLE) {
            return;
        }
        Angle currentAngle = RotationController.INSTANCE.getRotation();
        if (this.pendingAction == PendingAction.BREAK) {
            if (this.currentBreakTarget == null || AppleFarm.mc.field_1687.method_8320(this.currentBreakTarget).method_26215() || !this.isValidBreakTarget(this.currentBreakTarget)) {
                this.clearBreakingTarget();
                this.pendingAction = PendingAction.NONE;
                return;
            }
            class_3965 hit = this.getAimingHit(this.currentBreakTarget, currentAngle);
            if (hit != null && hit.method_17777().equals((Object)this.currentBreakTarget)) {
                this.targetStuckTicks = 0;
                this.currentBreakSide = hit.method_17780();
                this.currentAimPos = hit.method_17784();
                this.switchBestTool(this.currentBreakTarget);
                AppleFarm.mc.field_1724.method_6104(class_1268.field_5808);
                AppleFarm.mc.field_1761.method_2902(this.currentBreakTarget, this.currentBreakSide);
                this.breakTimer.reset();
                this.pendingAction = PendingAction.NONE;
            } else {
                ++this.targetStuckTicks;
                if (this.targetStuckTicks > 12) {
                    this.unreachableBlocks.add(this.currentBreakTarget);
                    this.clearBreakingTarget();
                    this.pendingAction = PendingAction.NONE;
                }
            }
            return;
        }
        if (this.pendingAction == PendingAction.PLANT) {
            class_243 targetVec = this.farmPos.method_10074().method_46558().method_1031(0.0, 0.5, 0.0);
            if (this.canPlantAt(this.farmPos) && this.isAimingNear(targetVec, currentAngle, 16.0f)) {
                this.plantSapling();
                this.pendingAction = PendingAction.NONE;
            }
            return;
        }
        if (this.pendingAction == PendingAction.BONE_MEAL) {
            class_243 targetVec = this.farmPos.method_46558();
            if (this.hasSaplingPlanted() && this.isAimingNear(targetVec, currentAngle, 16.0f)) {
                this.useBoneMeal();
                this.pendingAction = PendingAction.NONE;
            }
        }
    }

    private void prepareBreakTree() {
        if (!this.breakTimer.finished(0.0)) {
            return;
        }
        if (this.currentBreakTarget == null || AppleFarm.mc.field_1687.method_8320(this.currentBreakTarget).method_26215()) {
            BreakTarget target = this.findBreakTarget();
            if (target == null) {
                this.clearBreakingTarget();
                return;
            }
            this.currentBreakTarget = target.pos();
            this.currentBreakSide = target.side();
            this.currentAimPos = this.getRandomizedAimPoint(this.currentBreakTarget, this.currentBreakSide, target.hitPos());
            this.targetStuckTicks = 0;
        } else if (!this.isValidBreakTarget(this.currentBreakTarget)) {
            this.clearBreakingTarget();
            return;
        }
        if (this.currentBreakTarget != null && this.currentAimPos != null) {
            this.pendingAction = PendingAction.BREAK;
            this.requestedRotation = AngleUtil.calculateAngle(this.currentAimPos);
        }
    }

    private void preparePlantSapling() {
        if (!this.canPlantAt(this.farmPos)) {
            return;
        }
        this.pendingAction = PendingAction.PLANT;
        class_2338 ground = this.farmPos.method_10074();
        class_243 groundCenter = ground.method_46558().method_1031((Math.random() - 0.5) * 0.4, 0.5, (Math.random() - 0.5) * 0.4);
        this.requestedRotation = AngleUtil.calculateAngle(groundCenter);
    }

    private void prepareUseBoneMeal() {
        this.pendingAction = PendingAction.BONE_MEAL;
        class_243 aim = this.farmPos.method_46558().method_1031((Math.random() - 0.5) * 0.4, (Math.random() - 0.5) * 0.3, (Math.random() - 0.5) * 0.4);
        this.requestedRotation = AngleUtil.calculateAngle(aim);
    }

    private boolean handleHoeRepair() {
        if (this.repairingHoe) {
            return this.continueHoeRepair();
        }
        class_1735 hoeSlot = this.findDamagedHoeSlot();
        if (hoeSlot == null) {
            return false;
        }
        this.clearBreakingTarget();
        if (!this.hasExperienceBottle()) {
            this.requestRefill(RefillItem.EXPERIENCE_BOTTLE);
            return true;
        }
        if (!this.prepareHoeRepair(hoeSlot)) {
            this.notify("\u00a7cAppleFarm: \u043d\u0435 \u0441\u043c\u043e\u0433 \u043f\u043e\u0434\u0433\u043e\u0442\u043e\u0432\u0438\u0442\u044c \u043c\u043e\u0442\u044b\u0433\u0443 \u0438 \u043e\u043f\u044b\u0442 \u0434\u043b\u044f \u043f\u043e\u0447\u0438\u043d\u043a\u0438.");
            return true;
        }
        this.repairingHoe = true;
        this.repairTimer.setMs(50L);
        return this.continueHoeRepair();
    }

    private boolean continueHoeRepair() {
        class_1799 hoe = AppleFarm.mc.field_1724.method_6047();
        if (!this.isHoe(hoe)) {
            this.stopRepairing();
            return false;
        }
        if (!AppleFarm.mc.field_1724.method_6079().method_31574(class_1802.field_8287)) {
            this.stopRepairing();
            this.clearRepairState();
            return false;
        }
        if (hoe.method_7919() <= 0) {
            this.stopRepairing();
            return true;
        }
        this.clearBreakingTarget();
        if (!this.repairTimer.finished(50.0)) {
            return true;
        }
        if (!this.isUsableExperienceBottle(AppleFarm.mc.field_1724.method_6079())) {
            this.notify(this.hasExperienceBottle() ? "\u00a7eAppleFarm: \u0431\u0443\u0442\u044b\u043b\u043a\u0430 \u043e\u043f\u044b\u0442\u0430 \u0432 \u043a\u0443\u043b\u0434\u0430\u0443\u043d\u0435." : "\u00a7eAppleFarm: \u0437\u0430\u043a\u043e\u043d\u0447\u0438\u043b\u0438\u0441\u044c \u0431\u0443\u0442\u044b\u043b\u043a\u0438 \u043e\u043f\u044b\u0442\u0430.");
            this.repairTimer.reset();
            return true;
        }
        this.useExperienceBottle();
        this.repairTimer.reset();
        return true;
    }

    private boolean prepareHoeRepair(class_1735 slot) {
        boolean hoeAlreadySelected;
        this.repairHoeSourceSlotId = -1;
        this.repairBottleSourceSlotId = -1;
        this.repairSelectedHotbarSlot = AppleFarm.mc.field_1724.method_31548().method_67532();
        boolean bl = hoeAlreadySelected = slot.field_7871 == AppleFarm.mc.field_1724.method_31548() && slot.method_34266() == this.repairSelectedHotbarSlot;
        if (!hoeAlreadySelected) {
            this.repairHoeSourceSlotId = slot.field_7874;
            PlayerInventoryUtil.clickSlot(slot.field_7874, this.repairSelectedHotbarSlot, class_1713.field_7791, false);
        }
        if (!this.isHoe(AppleFarm.mc.field_1724.method_6047())) {
            this.restoreRepairSlots();
            return false;
        }
        class_1735 bottleSlot = this.findExperienceBottleSlot();
        if (bottleSlot == null) {
            this.restoreRepairSlots();
            return false;
        }
        if (bottleSlot.field_7874 != 45) {
            this.repairBottleSourceSlotId = bottleSlot.field_7874;
            PlayerInventoryUtil.clickSlot(bottleSlot.field_7874, 40, class_1713.field_7791, false);
        }
        if (!AppleFarm.mc.field_1724.method_6079().method_31574(class_1802.field_8287)) {
            this.restoreRepairSlots();
            return false;
        }
        return true;
    }

    private void restoreRepairSlots() {
        if (this.repairBottleSourceSlotId != -1) {
            PlayerInventoryUtil.clickSlot(this.repairBottleSourceSlotId, 40, class_1713.field_7791, false);
        }
        if (this.repairHoeSourceSlotId != -1 && this.repairSelectedHotbarSlot >= 0) {
            this.selectSlot(this.repairSelectedHotbarSlot);
            PlayerInventoryUtil.clickSlot(this.repairHoeSourceSlotId, this.repairSelectedHotbarSlot, class_1713.field_7791, false);
        }
    }

    private void stopRepairing() {
        this.restoreRepairSlots();
        this.clearRepairState();
    }

    private void clearRepairState() {
        this.repairingHoe = false;
        this.repairHoeSourceSlotId = -1;
        this.repairBottleSourceSlotId = -1;
        this.repairSelectedHotbarSlot = -1;
    }

    private class_1735 findDamagedHoeSlot() {
        class_1735 offhand = this.findSlotById(45);
        if (offhand != null && this.needsRepair(offhand.method_7677())) {
            return offhand;
        }
        for (class_1735 slot : AppleFarm.mc.field_1724.field_7512.field_7761) {
            if (slot.field_7874 == 45 || !this.needsRepair(slot.method_7677())) continue;
            return slot;
        }
        return null;
    }

    private class_1735 findSlotById(int id) {
        return AppleFarm.mc.field_1724.field_7512.field_7761.stream().filter(slot -> slot.field_7874 == id).findFirst().orElse(null);
    }

    private boolean needsRepair(class_1799 stack) {
        return this.isHoe(stack) && this.durabilityPercent(stack) < 15.0f;
    }

    private boolean isHoe(class_1799 stack) {
        return !stack.method_7960() && stack.method_7963() && stack.method_7909() instanceof class_1794;
    }

    private float durabilityPercent(class_1799 stack) {
        return stack.method_7963() && stack.method_7936() > 0 ? (float)(stack.method_7936() - stack.method_7919()) * 100.0f / (float)stack.method_7936() : 100.0f;
    }

    private boolean hasExperienceBottle() {
        return AppleFarm.mc.field_1724.field_7512.field_7761.stream().anyMatch(slot -> slot.field_7871 == AppleFarm.mc.field_1724.method_31548() && this.isExperienceBottle(slot.method_7677()));
    }

    private class_1735 findExperienceBottleSlot() {
        class_1735 normal = null;
        for (class_1735 slot : AppleFarm.mc.field_1724.field_7512.field_7761) {
            if (slot.field_7871 != AppleFarm.mc.field_1724.method_31548() || !this.isUsableExperienceBottle(slot.method_7677())) continue;
            if (this.isRepairBottle(slot.method_7677())) {
                return slot;
            }
            if (normal != null) continue;
            normal = slot;
        }
        return normal;
    }

    private boolean isExperienceBottle(class_1799 stack) {
        return !stack.method_7960() && stack.method_31574(class_1802.field_8287);
    }

    private boolean isUsableExperienceBottle(class_1799 stack) {
        return this.isExperienceBottle(stack) && !AppleFarm.mc.field_1724.method_7357().method_7904(stack);
    }

    private boolean isRepairBottle(class_1799 stack) {
        class_9279 customData = (class_9279)stack.method_58694(class_9334.field_49628);
        return customData != null && customData.toString().toLowerCase().contains(REPAIR_BOTTLE_MARKER);
    }

    private void useExperienceBottle() {
        PlayerIntersectionUtil.interactItemVanilla(class_1268.field_5810, AngleUtil.pitch(90.0f));
    }

    private void requestRefill(RefillItem item) {
        if (System.currentTimeMillis() < this.refillRetryAt) {
            return;
        }
        class_2338 chest = this.findSupplyChest();
        if (chest == null) {
            this.notify("\u00a7eAppleFarm: \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d \u0441\u0443\u043d\u0434\u0443\u043a \u0441 \u0442\u0430\u0431\u043b\u0438\u0447\u043a\u043e\u0439 \u00ab\u0420\u0430\u0441\u0445\u043e\u0434\u043d\u0438\u043a\u0438\u00bb.");
            this.refillRetryAt = System.currentTimeMillis() + 2500L;
            return;
        }
        double interactionRange = AppleFarm.mc.field_1724.method_55754();
        if (AppleFarm.mc.field_1724.method_5707(chest.method_46558()) > interactionRange * interactionRange) {
            this.notify("\u00a7eAppleFarm: \u0441\u0443\u043d\u0434\u0443\u043a \u00ab\u0420\u0430\u0441\u0445\u043e\u0434\u043d\u0438\u043a\u0438\u00bb \u043d\u0430\u0445\u043e\u0434\u0438\u0442\u0441\u044f \u0441\u043b\u0438\u0448\u043a\u043e\u043c \u0434\u0430\u043b\u0435\u043a\u043e.");
            this.refillRetryAt = System.currentTimeMillis() + 2500L;
            return;
        }
        this.stopRepairing();
        this.clearBreakingTarget();
        if (AppleFarm.mc.field_1724.field_7512 != AppleFarm.mc.field_1724.field_7498) {
            AppleFarm.mc.field_1724.method_7346();
        }
        this.refillItem = item;
        this.supplyChest = chest;
        this.setRefillState(RefillState.OPENING);
    }

    private void handleRefill() {
        if (this.refillItem == null || this.supplyChest == null || !(AppleFarm.mc.field_1687.method_8321(this.supplyChest) instanceof class_2595)) {
            this.failRefill("\u0441\u0443\u043d\u0434\u0443\u043a \u00ab\u0420\u0430\u0441\u0445\u043e\u0434\u043d\u0438\u043a\u0438\u00bb \u043f\u0440\u043e\u043f\u0430\u043b");
            return;
        }
        if (System.currentTimeMillis() < this.refillNextAction) {
            return;
        }
        switch (this.refillState.ordinal()) {
            case 1: {
                this.openSupplyChest();
                break;
            }
            case 2: {
                this.waitForSupplyGui();
                break;
            }
            case 3: {
                this.takeSupplyItem();
                break;
            }
        }
    }

    private void openSupplyChest() {
        if (System.currentTimeMillis() - this.refillStateSince >= 3000L) {
            this.failRefill("\u043d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043e\u0442\u043a\u0440\u044b\u0442\u044c \u0441\u0443\u043d\u0434\u0443\u043a \u00ab\u0420\u0430\u0441\u0445\u043e\u0434\u043d\u0438\u043a\u0438\u00bb");
            return;
        }
        double interactionRange = AppleFarm.mc.field_1724.method_55754();
        if (AppleFarm.mc.field_1724.method_5707(this.supplyChest.method_46558()) > interactionRange * interactionRange) {
            this.failRefill("\u0441\u0443\u043d\u0434\u0443\u043a \u00ab\u0420\u0430\u0441\u0445\u043e\u0434\u043d\u0438\u043a\u0438\u00bb \u043d\u0430\u0445\u043e\u0434\u0438\u0442\u0441\u044f \u0441\u043b\u0438\u0448\u043a\u043e\u043c \u0434\u0430\u043b\u0435\u043a\u043e");
            return;
        }
        class_243 center = this.supplyChest.method_46558();
        class_2350 side = class_2350.method_58251((class_243)AppleFarm.mc.field_1724.method_33571().method_1020(center));
        class_243 hitPos = center.method_1019(class_243.method_24954((class_2382)side.method_62675()).method_1021(0.5));
        this.requestedRotation = AngleUtil.calculateAngle(hitPos);
        Angle currentAngle = RotationController.INSTANCE.getRotation();
        if (!this.isAimingNear(hitPos, currentAngle, 16.0f)) {
            return;
        }
        AppleFarm.mc.field_1761.method_2896(AppleFarm.mc.field_1724, class_1268.field_5808, new class_3965(hitPos, side, this.supplyChest, false));
        AppleFarm.mc.field_1724.method_6104(class_1268.field_5808);
        this.setRefillState(RefillState.WAITING_GUI);
        this.refillNextAction = System.currentTimeMillis() + 250L;
    }

    private void waitForSupplyGui() {
        if (AppleFarm.mc.field_1724.field_7512 instanceof class_1707) {
            this.setRefillState(RefillState.TAKING);
            return;
        }
        if (System.currentTimeMillis() - this.refillStateSince >= 3000L) {
            this.failRefill("\u043d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043e\u0442\u043a\u0440\u044b\u0442\u044c \u0441\u0443\u043d\u0434\u0443\u043a \u00ab\u0420\u0430\u0441\u0445\u043e\u0434\u043d\u0438\u043a\u0438\u00bb");
        }
    }

    private void takeSupplyItem() {
        class_1703 class_17032 = AppleFarm.mc.field_1724.field_7512;
        if (!(class_17032 instanceof class_1707)) {
            this.failRefill("\u0441\u0443\u043d\u0434\u0443\u043a \u00ab\u0420\u0430\u0441\u0445\u043e\u0434\u043d\u0438\u043a\u0438\u00bb \u0437\u0430\u043a\u0440\u044b\u043b\u0441\u044f");
            return;
        }
        class_1707 handler = (class_1707)class_17032;
        if (this.hasRefillItem(this.refillItem)) {
            this.finishRefill();
            return;
        }
        if (System.currentTimeMillis() - this.refillStateSince >= 3000L) {
            this.failRefill("\u043d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0437\u0430\u0431\u0440\u0430\u0442\u044c \u0440\u0430\u0441\u0445\u043e\u0434\u043d\u0438\u043a\u0438 \u2014 \u043f\u0440\u043e\u0432\u0435\u0440\u044c \u0441\u0432\u043e\u0431\u043e\u0434\u043d\u043e\u0435 \u043c\u0435\u0441\u0442\u043e \u0432 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u0435");
            return;
        }
        int containerSlots = handler.method_17388() * 9;
        class_1735 candidate = null;
        for (int i = 0; i < containerSlots; ++i) {
            class_1735 slot = handler.method_7611(i);
            if (!slot.method_7681() || !this.matchesRefillItem(slot.method_7677(), this.refillItem) || candidate != null && (this.refillItem != RefillItem.EXPERIENCE_BOTTLE || !this.isRepairBottle(slot.method_7677()))) continue;
            candidate = slot;
            if (this.refillItem != RefillItem.EXPERIENCE_BOTTLE || this.isRepairBottle(slot.method_7677())) break;
        }
        if (candidate == null) {
            this.failRefill("\u0432 \u0441\u0443\u043d\u0434\u0443\u043a\u0435 \u043d\u0435\u0442: " + this.refillItem.displayName);
            return;
        }
        AppleFarm.mc.field_1761.method_2906(handler.field_7763, candidate.field_7874, 0, class_1713.field_7794, (class_1657)AppleFarm.mc.field_1724);
        this.refillNextAction = System.currentTimeMillis() + 120L;
    }

    private void finishRefill() {
        if (AppleFarm.mc.field_1724.field_7512 != AppleFarm.mc.field_1724.field_7498) {
            AppleFarm.mc.field_1724.method_7346();
        }
        this.refillRetryAt = 0L;
        this.refillState = RefillState.IDLE;
        this.refillItem = null;
        this.supplyChest = null;
    }

    private void failRefill(String reason) {
        if (AppleFarm.mc.field_1724.field_7512 != AppleFarm.mc.field_1724.field_7498) {
            AppleFarm.mc.field_1724.method_7346();
        }
        this.notify("\u00a7eAppleFarm: " + reason + ".");
        this.refillRetryAt = System.currentTimeMillis() + 2500L;
        this.refillState = RefillState.IDLE;
        this.refillItem = null;
        this.supplyChest = null;
    }

    private void setRefillState(RefillState state) {
        this.refillState = state;
        this.refillStateSince = System.currentTimeMillis();
    }

    private class_2338 findSupplyChest() {
        class_2338 center = this.farmPos != null ? this.farmPos : AppleFarm.mc.field_1724.method_24515();
        class_2338 nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (class_2338 signPos : class_2338.method_10097((class_2338)center.method_10069(-12, -12, -12), (class_2338)center.method_10069(12, 12, 12))) {
            class_2625 sign;
            class_2586 class_25862 = AppleFarm.mc.field_1687.method_8321(signPos);
            if (!(class_25862 instanceof class_2625) || !this.isSupplySign(sign = (class_2625)class_25862)) continue;
            for (class_2338 chestPos : class_2338.method_10097((class_2338)signPos.method_10069(-2, -2, -2), (class_2338)signPos.method_10069(2, 2, 2))) {
                double distance;
                if (!(AppleFarm.mc.field_1687.method_8321(chestPos) instanceof class_2595) || !((distance = AppleFarm.mc.field_1724.method_5707(chestPos.method_46558())) < nearestDistance)) continue;
                nearestDistance = distance;
                nearest = chestPos.method_10062();
            }
        }
        return nearest;
    }

    private boolean isSupplySign(class_2625 sign) {
        return this.containsSupplyLabel(sign.method_49853().method_49877(false)) || this.containsSupplyLabel(sign.method_49854().method_49877(false));
    }

    private boolean containsSupplyLabel(class_2561[] lines) {
        for (class_2561 line : lines) {
            if (line == null || !line.getString().trim().toLowerCase(Locale.ROOT).contains("\u0440\u0430\u0441\u0445\u043e\u0434\u043d\u0438\u043a\u0438")) continue;
            return true;
        }
        return false;
    }

    private boolean hasInventoryItem(class_1792 item) {
        for (int slot = 0; slot < 36; ++slot) {
            if (!AppleFarm.mc.field_1724.method_31548().method_5438(slot).method_31574(item)) continue;
            return true;
        }
        return false;
    }

    private boolean hasRefillItem(RefillItem item) {
        if (item == RefillItem.SAPLING) {
            return this.findSaplingItem() != class_1802.field_8162;
        }
        return this.hasInventoryItem(item.item);
    }

    private boolean matchesRefillItem(class_1799 stack, RefillItem item) {
        return item == RefillItem.SAPLING ? SAPLINGS.contains(stack.method_7909()) : stack.method_31574(item.item);
    }

    private boolean plantSapling() {
        class_1792 sapling = this.findSaplingItem();
        if (sapling == class_1802.field_8162) {
            return false;
        }
        if (AppleFarm.mc.field_1687.method_8320(this.farmPos).method_26164(class_3481.field_15462)) {
            return true;
        }
        if (!this.canPlantAt(this.farmPos)) {
            return false;
        }
        class_2338 ground = this.farmPos.method_10074();
        return this.interactWithItemOnBlock(sapling, new class_3965(ground.method_46558().method_1031(0.0, 0.5, 0.0), class_2350.field_11036, ground, false));
    }

    private boolean useBoneMeal() {
        class_3965 hit = new class_3965(this.farmPos.method_46558(), class_2350.field_11036, this.farmPos, false);
        return this.interactWithItemOnBlock(class_1802.field_8324, hit);
    }

    private boolean interactWithItemOnBlock(class_1792 item, class_3965 hit) {
        int slot = this.findHotbarSlot(item);
        if (slot == -1) {
            int inventorySlot = this.findInventoryItem(item);
            if (inventorySlot == -1) {
                return false;
            }
            slot = this.swapInventoryStackToHotbar(inventorySlot);
            if (slot == -1) {
                return false;
            }
        }
        int oldSlot = AppleFarm.mc.field_1724.method_31548().method_67532();
        this.selectSlot(slot);
        class_1269 result = AppleFarm.mc.field_1761.method_2896(AppleFarm.mc.field_1724, class_1268.field_5808, hit);
        AppleFarm.mc.field_1724.method_6104(class_1268.field_5808);
        if (oldSlot != slot) {
            this.selectSlot(oldSlot);
        }
        return result.method_23665();
    }

    private boolean hasTree() {
        return this.scanTree(false);
    }

    private boolean hasLeaves() {
        return this.scanTree(true);
    }

    private boolean scanTree(boolean leavesOnly) {
        int radius = 10;
        int height = 20;
        for (int x = -radius; x <= radius; ++x) {
            for (int z = -radius; z <= radius; ++z) {
                for (int y = 0; y <= height; ++y) {
                    class_2680 state = AppleFarm.mc.field_1687.method_8320(this.farmPos.method_10069(x, y, z));
                    if (state.method_26164(class_3481.field_15503)) {
                        return true;
                    }
                    if (leavesOnly || !state.method_26164(class_3481.field_15475)) continue;
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasSaplingPlanted() {
        return AppleFarm.mc.field_1687.method_8320(this.farmPos).method_26164(class_3481.field_15462);
    }

    private boolean canPlantAt(class_2338 pos) {
        class_2680 state = AppleFarm.mc.field_1687.method_8320(pos);
        if (!state.method_26215() && !state.method_45474()) {
            return false;
        }
        class_2680 ground = AppleFarm.mc.field_1687.method_8320(pos.method_10074());
        return ground.method_26164(class_3481.field_29822) || ground.method_27852(class_2246.field_10219) || ground.method_27852(class_2246.field_10362) || ground.method_27852(class_2246.field_37576);
    }

    private class_2338 resolveFarmPos() {
        class_2338 inFront;
        class_3965 hit;
        class_239 class_2392 = AppleFarm.mc.field_1765;
        if (class_2392 instanceof class_3965 && (hit = (class_3965)class_2392).method_17783() == class_239.class_240.field_1332) {
            class_2338 block = hit.method_17777();
            class_2338 above = block.method_10084();
            if (this.canPlantAt(above)) {
                return above;
            }
            if (this.canPlantAt(block)) {
                return block;
            }
        }
        return this.canPlantAt(inFront = AppleFarm.mc.field_1724.method_24515().method_10093(AppleFarm.mc.field_1724.method_5735())) ? inFront : null;
    }

    private class_1792 findSaplingItem() {
        for (class_1792 item : SAPLINGS) {
            if (this.findHotbarSlot(item) == -1 && this.findInventoryItem(item) == -1) continue;
            return item;
        }
        return class_1802.field_8162;
    }

    private boolean isValidBreakTarget(class_2338 pos) {
        if (pos == null || this.unreachableBlocks.contains(pos)) {
            return false;
        }
        class_2680 state = AppleFarm.mc.field_1687.method_8320(pos);
        if (!state.method_26164(class_3481.field_15475) && !state.method_26164(class_3481.field_15503)) {
            return false;
        }
        ValidFace face = this.findBestVisibleFace(pos);
        if (face == null) {
            return false;
        }
        this.currentBreakSide = face.side();
        if (this.currentAimPos == null) {
            this.currentAimPos = this.getRandomizedAimPoint(pos, this.currentBreakSide, face.hitPos());
        }
        return true;
    }

    private BreakTarget findBreakTarget() {
        ArrayList<BreakTarget> leaves = new ArrayList<BreakTarget>();
        ArrayList<BreakTarget> logs = new ArrayList<BreakTarget>();
        int radius = 10;
        int height = 20;
        class_243 eye = AppleFarm.mc.field_1724.method_33571();
        double reach = Math.min(this.getBreakRange(), 4.5) - 0.05;
        double maxBound = (reach + 0.87) * (reach + 0.87);
        for (int x = -radius; x <= radius; ++x) {
            for (int z = -radius; z <= radius; ++z) {
                for (int y = 0; y <= height; ++y) {
                    ValidFace face;
                    class_2680 state;
                    class_2338 pos = this.farmPos.method_10069(x, y, z);
                    if (this.unreachableBlocks.contains(pos) || !(state = AppleFarm.mc.field_1687.method_8320(pos)).method_26164(class_3481.field_15503) && !state.method_26164(class_3481.field_15475) || eye.method_1025(pos.method_46558()) > maxBound || (face = this.findBestVisibleFace(pos)) == null) continue;
                    BreakTarget target = new BreakTarget(pos, face.side(), face.hitPos(), face.distanceSq());
                    if (state.method_26164(class_3481.field_15503)) {
                        leaves.add(target);
                        continue;
                    }
                    logs.add(target);
                }
            }
        }
        Comparator<BreakTarget> comparator = Comparator.comparingDouble(BreakTarget::distance);
        BreakTarget leaf = leaves.stream().min(comparator).orElse(null);
        if (leaf != null) {
            return leaf;
        }
        BreakTarget log = logs.stream().min(comparator).orElse(null);
        if (log != null) {
            return log;
        }
        if (!this.unreachableBlocks.isEmpty()) {
            this.unreachableBlocks.clear();
        }
        return null;
    }

    private ValidFace findBestVisibleFace(class_2338 pos) {
        if (AppleFarm.mc.field_1724 == null || AppleFarm.mc.field_1687 == null || pos == null) {
            return null;
        }
        class_243 eye = AppleFarm.mc.field_1724.method_33571();
        class_243 center = pos.method_46558();
        double reach = Math.min(this.getBreakRange(), 4.5) - 0.05;
        double reachSq = reach * reach;
        if (eye.method_1025(center) > (reach + 0.87) * (reach + 0.87)) {
            return null;
        }
        ValidFace best = null;
        double bestDistSq = Double.MAX_VALUE;
        block0: for (class_2350 side : class_2350.values()) {
            class_243 normal;
            class_243 faceCenter;
            class_2338 neighborPos = pos.method_10093(side);
            class_2680 neighborState = AppleFarm.mc.field_1687.method_8320(neighborPos);
            if (neighborState.method_26164(class_3481.field_15475) || neighborState.method_26164(class_3481.field_15503) || neighborState.method_26212((class_1922)AppleFarm.mc.field_1687, neighborPos) || eye.method_1020(faceCenter = center.method_1019((normal = class_243.method_24954((class_2382)side.method_62675())).method_1021(0.5))).method_1026(normal) <= 0.001) continue;
            for (double[] offset : FACE_OFFSETS) {
                double distSq;
                class_243 rayEnd;
                class_3965 hit;
                class_243 samplePoint = this.getFaceSamplePoint(faceCenter, side, offset[0], offset[1]);
                if (eye.method_1025(samplePoint) > reachSq || (hit = AppleFarm.mc.field_1687.method_17742(new class_3959(eye, rayEnd = samplePoint.method_1020(normal.method_1021(0.01)), class_3959.class_3960.field_17559, class_3959.class_242.field_1348, (class_1297)AppleFarm.mc.field_1724))) == null || hit.method_17783() != class_239.class_240.field_1332 || !hit.method_17777().equals((Object)pos) || hit.method_17780() != side || !((distSq = eye.method_1025(hit.method_17784())) <= reachSq) || !(distSq < bestDistSq)) continue;
                bestDistSq = distSq;
                best = new ValidFace(side, hit.method_17784(), distSq);
                continue block0;
            }
        }
        return best;
    }

    private class_243 getFaceSamplePoint(class_243 faceCenter, class_2350 side, double u, double v) {
        return switch (side.method_10166()) {
            default -> throw new MatchException(null, null);
            case class_2350.class_2351.field_11052 -> faceCenter.method_1031(u, 0.0, v);
            case class_2350.class_2351.field_11048 -> faceCenter.method_1031(0.0, u, v);
            case class_2350.class_2351.field_11051 -> faceCenter.method_1031(u, v, 0.0);
        };
    }

    private class_243 getRandomizedAimPoint(class_2338 pos, class_2350 side, class_243 fallback) {
        if (pos == null || side == null || fallback == null) {
            return fallback;
        }
        class_243 eye = AppleFarm.mc.field_1724.method_33571();
        class_243 normal = class_243.method_24954((class_2382)side.method_62675());
        class_243 faceCenter = pos.method_46558().method_1019(normal.method_1021(0.5));
        double reach = Math.min(this.getBreakRange(), 4.5) - 0.05;
        double reachSq = reach * reach;
        for (int i = 0; i < 4; ++i) {
            class_243 rayEnd;
            class_3965 hit;
            double v;
            double u = (Math.random() - 0.5) * 0.44;
            class_243 sample = this.getFaceSamplePoint(faceCenter, side, u, v = (Math.random() - 0.5) * 0.44);
            if (eye.method_1025(sample) > reachSq || (hit = AppleFarm.mc.field_1687.method_17742(new class_3959(eye, rayEnd = sample.method_1020(normal.method_1021(0.01)), class_3959.class_3960.field_17559, class_3959.class_242.field_1348, (class_1297)AppleFarm.mc.field_1724))) == null || hit.method_17783() != class_239.class_240.field_1332 || !hit.method_17777().equals((Object)pos) || hit.method_17780() != side || !(eye.method_1025(hit.method_17784()) <= reachSq)) continue;
            return hit.method_17784();
        }
        return fallback;
    }

    private boolean isAimingNear(class_243 target, Angle angle, float tolerance) {
        if (AppleFarm.mc.field_1724 == null || angle == null) {
            return false;
        }
        double reach = this.getBreakRange();
        if (AppleFarm.mc.field_1724.method_33571().method_1025(target) > reach * reach) {
            return false;
        }
        Angle targetAngle = AngleUtil.calculateAngle(target);
        Angle delta = AngleUtil.calculateDelta(angle, targetAngle);
        return Math.abs(delta.getYaw()) <= tolerance && Math.abs(delta.getPitch()) <= tolerance;
    }

    private class_3965 getAimingHit(class_2338 pos, Angle angle) {
        double reach;
        class_243 look;
        class_243 end;
        if (AppleFarm.mc.field_1724 == null || AppleFarm.mc.field_1687 == null || angle == null || pos == null) {
            return null;
        }
        class_243 eye = AppleFarm.mc.field_1724.method_33571();
        class_3965 hit = AppleFarm.mc.field_1687.method_17742(new class_3959(eye, end = eye.method_1019((look = class_243.method_1030((float)angle.getPitch(), (float)angle.getYaw())).method_1021((reach = Math.min(this.getBreakRange(), 4.5) - 0.05) + 0.2)), class_3959.class_3960.field_17559, class_3959.class_242.field_1348, (class_1297)AppleFarm.mc.field_1724));
        if (hit != null && hit.method_17783() == class_239.class_240.field_1332 && hit.method_17777().equals((Object)pos) && eye.method_1025(hit.method_17784()) <= reach * reach) {
            return hit;
        }
        return null;
    }

    private void switchBestTool(class_2338 pos) {
        if (!this.toolSwitchTimer.finished(0.0)) {
            return;
        }
        class_2680 state = AppleFarm.mc.field_1687.method_8320(pos);
        int bestSlot = -1;
        float bestSpeed = 1.0f;
        for (int slot = 0; slot < 9; ++slot) {
            class_1799 stack = AppleFarm.mc.field_1724.method_31548().method_5438(slot);
            float speed = stack.method_7924(state);
            if (!(speed > bestSpeed)) continue;
            bestSpeed = speed;
            bestSlot = slot;
        }
        if (bestSlot != -1 && bestSlot != AppleFarm.mc.field_1724.method_31548().method_67532()) {
            this.selectSlot(bestSlot);
            this.toolSwitchTimer.reset();
        }
    }

    private int findHotbarSlot(class_1792 item) {
        return this.findHotbarSlot((class_1799 stack) -> stack.method_31574(item));
    }

    private int findHotbarSlot(Predicate<class_1799> predicate) {
        for (int slot = 0; slot < 9; ++slot) {
            if (!predicate.test(AppleFarm.mc.field_1724.method_31548().method_5438(slot))) continue;
            return slot;
        }
        return -1;
    }

    private int findInventoryItem(class_1792 item) {
        return this.findInventoryStack(stack -> stack.method_31574(item));
    }

    private int findInventoryStack(Predicate<class_1799> predicate) {
        for (int slot = 9; slot < 36; ++slot) {
            if (!predicate.test(AppleFarm.mc.field_1724.method_31548().method_5438(slot))) continue;
            return slot;
        }
        return -1;
    }

    private int swapInventoryStackToHotbar(int inventorySlotId) {
        int hotbarSlot = this.findSafeHotbarSlot();
        if (hotbarSlot == -1) {
            return -1;
        }
        PlayerInventoryUtil.clickSlot(inventorySlotId, hotbarSlot, class_1713.field_7791, false);
        return hotbarSlot;
    }

    private int findSafeHotbarSlot() {
        for (int slot = 0; slot < 9; ++slot) {
            class_1792 item = AppleFarm.mc.field_1724.method_31548().method_5438(slot).method_7909();
            if (item != class_1802.field_8162 && this.isImportantFarmItem(item)) continue;
            return slot;
        }
        return -1;
    }

    private boolean isImportantFarmItem(class_1792 item) {
        return item == class_1802.field_8556 || item == class_1802.field_22025 || item == class_1802.field_8527 || item == class_1802.field_22026 || item == class_1802.field_8324 || item == class_1802.field_8287 || SAPLINGS.contains(item);
    }

    private void selectSlot(int slot) {
        if (slot < 0 || slot > 8 || AppleFarm.mc.field_1724.method_31548().method_67532() == slot) {
            return;
        }
        AppleFarm.mc.field_1724.method_31548().method_61496(slot);
    }

    private void clearBreakingTarget() {
        this.currentBreakTarget = null;
        this.currentBreakSide = null;
        this.currentAimPos = null;
        this.targetStuckTicks = 0;
    }

    private void resetState() {
        this.clearBreakingTarget();
        this.unreachableBlocks.clear();
        this.lastBlacklistClearTime = 0L;
        this.pendingAction = PendingAction.NONE;
        this.requestedRotation = null;
        this.repairingHoe = false;
        this.repairHoeSourceSlotId = -1;
        this.repairBottleSourceSlotId = -1;
        this.repairSelectedHotbarSlot = -1;
        this.refillState = RefillState.IDLE;
        this.refillItem = null;
        this.supplyChest = null;
        this.refillStateSince = 0L;
        this.refillNextAction = 0L;
        this.refillRetryAt = 0L;
        this.farmPos = null;
    }

    private void notify(String message) {
        if (this.notifyTimer.finished(2500.0)) {
            this.chat(message);
            this.notifyTimer.reset();
        }
    }

    private void chat(String message) {
        if (AppleFarm.mc.field_1724 != null) {
            AppleFarm.mc.field_1724.method_7353(QuickLogger.clientMessage((class_2561)class_2561.method_43470((String)message)), false);
        }
    }

    private static enum PendingAction {
        NONE,
        BREAK,
        PLANT,
        BONE_MEAL;

    }

    private static enum RefillState {
        IDLE,
        OPENING,
        WAITING_GUI,
        TAKING;

    }

    private static enum RefillItem {
        SAPLING(class_1802.field_8162, "\u0441\u0430\u0436\u0435\u043d\u0446\u044b"),
        BONE_MEAL(class_1802.field_8324, "\u043a\u043e\u0441\u0442\u043d\u0430\u044f \u043c\u0443\u043a\u0430"),
        EXPERIENCE_BOTTLE(class_1802.field_8287, "\u043f\u0443\u0437\u044b\u0440\u044c\u043a\u0438 \u043e\u043f\u044b\u0442\u0430");

        private final class_1792 item;
        private final String displayName;

        private RefillItem(class_1792 item, String displayName) {
            this.item = item;
            this.displayName = displayName;
        }
    }

    private record BreakTarget(class_2338 pos, class_2350 side, class_243 hitPos, double distance) {
    }

    private record ValidFace(class_2350 side, class_243 hitPos, double distanceSq) {
    }
}

