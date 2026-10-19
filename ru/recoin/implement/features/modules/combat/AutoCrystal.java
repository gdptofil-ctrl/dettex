/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1041
 *  net.minecraft.class_12206
 *  net.minecraft.class_1268
 *  net.minecraft.class_1293
 *  net.minecraft.class_1294
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_1511
 *  net.minecraft.class_1657
 *  net.minecraft.class_1735
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1922
 *  net.minecraft.class_2246
 *  net.minecraft.class_2338
 *  net.minecraft.class_2350
 *  net.minecraft.class_2350$class_2351
 *  net.minecraft.class_2350$class_2353
 *  net.minecraft.class_2374
 *  net.minecraft.class_238
 *  net.minecraft.class_2382
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_243
 *  net.minecraft.class_2596
 *  net.minecraft.class_2680
 *  net.minecraft.class_2769
 *  net.minecraft.class_2824
 *  net.minecraft.class_2868
 *  net.minecraft.class_2885
 *  net.minecraft.class_332
 *  net.minecraft.class_3489
 *  net.minecraft.class_3675
 *  net.minecraft.class_3959
 *  net.minecraft.class_3959$class_242
 *  net.minecraft.class_3959$class_3960
 *  net.minecraft.class_3965
 *  net.minecraft.class_4587
 *  net.minecraft.class_4969
 *  net.minecraft.class_638
 *  net.minecraft.class_640
 *  org.lwjgl.glfw.GLFW
 */
package ru.recoin.implement.features.modules.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.class_1041;
import net.minecraft.class_12206;
import net.minecraft.class_1268;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1511;
import net.minecraft.class_1657;
import net.minecraft.class_1735;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1922;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2374;
import net.minecraft.class_238;
import net.minecraft.class_2382;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_2680;
import net.minecraft.class_2769;
import net.minecraft.class_2824;
import net.minecraft.class_2868;
import net.minecraft.class_2885;
import net.minecraft.class_332;
import net.minecraft.class_3489;
import net.minecraft.class_3675;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_4587;
import net.minecraft.class_4969;
import net.minecraft.class_638;
import net.minecraft.class_640;
import org.lwjgl.glfw.GLFW;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BindSetting;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.MultiSelectSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.api.repository.friend.FriendUtils;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.entity.EntityCompat;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.entity.PlayerInventoryComponent;
import ru.recoin.common.util.entity.PlayerInventoryUtil;
import ru.recoin.common.util.entity.ScreenWalkInventorySync;
import ru.recoin.common.util.math.ProjectionUtil;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.render.Render3DUtil;
import ru.recoin.common.util.task.TaskPriority;
import ru.recoin.core.Main;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.events.player.InputEvent;
import ru.recoin.implement.events.player.RotationUpdateEvent;
import ru.recoin.implement.events.render.DrawEvent;
import ru.recoin.implement.events.render.WorldRenderEvent;
import ru.recoin.implement.features.modules.combat.Aura;
import ru.recoin.implement.features.modules.combat.AutoCrystalDamage;
import ru.recoin.implement.features.modules.combat.killaura.attack.AttackHandler;
import ru.recoin.implement.features.modules.combat.killaura.attack.AttackPerpetrator;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationConfig;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.InstantCrystalSmoothMode;

public class AutoCrystal
extends Module {
    private static final int CANDIDATES = 48;
    private static final float PLACE_RANGE = 4.5f;
    private static final float BREAK_RANGE = 3.0f;
    private static final float BREAK_WALL_RANGE = 3.0f;
    private static final float SELF_WEIGHT = 0.9f;
    private static final float FACE_PLACE_HEALTH = 8.0f;
    private static final float ARMOR_SCALE = 5.0f;
    private static final int FACE_PLACE_KEY = 340;
    private static final int RENDER_COLOR = -1259646382;
    private static final float CURRENT_ANGLE_TOLERANCE = 8.0f;
    private static final boolean DISABLE_ON_KILL = true;
    private static final boolean OLD_VERSION = false;
    private static final boolean ARMOR_BREAKER = true;
    private static final boolean PROTECT_FRIENDS = true;
    private static final boolean ANTI_WEAKNESS = true;
    private final ValueSetting targetRange = new ValueSetting("Radius", "Target search range").range(3.0f, 12.0f).setValue(8.0f).step(0.1f);
    private final BooleanSetting aiming = new BooleanSetting("\u041d\u0430\u0432\u043e\u0434\u043a\u0430", "Automatically aim at the selected action").setValue(true);
    private final SelectSetting targetPriority = new SelectSetting("Target Priority", "Target sorting").value("Distance", "Health").selected("Distance");
    private final BooleanSetting useAnchors = new BooleanSetting("\u0424\u043e\u0440\u0441 \u044f\u043a\u043e\u0440\u0435\u0439", "Allow respawn anchors as attack actions").setValue(true);
    private final BindSetting anchorKey = new BindSetting("\u041a\u043b\u0430\u0432\u0438\u0448\u0430 \u044f\u043a\u043e\u0440\u0435\u0439", "Use anchors only while this key is held when assigned");
    private final ValueSetting anchorsPerAction = new ValueSetting("\u042f\u043a\u043e\u0440\u0435\u0439 \u0437\u0430 \u0440\u0430\u0437", "Anchor charge count before detonation").setValue(1.0f).range(1.0f, 4.0f).step(1.0f);
    private final ValueSetting anchorCover = new ValueSetting("\u041f\u0440\u0438\u043a\u0440\u044b\u0442\u0438\u0435 \u044f\u043a\u043e\u0440\u044f", "Extra self-damage safety margin for anchors").setValue(1.0f).range(0.0f, 5.0f).step(0.5f);
    private final BooleanSetting throughWalls = new BooleanSetting("\u0421\u0442\u0430\u0432\u0438\u0442\u044c \u0447\u0435\u0440\u0435\u0437 \u0441\u0442\u0435\u043d\u044b", "Allow block placement attempts through walls").setValue(false);
    private final BooleanSetting silentSlotSwitch = new BooleanSetting("\u0421\u043a\u0440\u044b\u0442\u0430\u044f \u0441\u043c\u0435\u043d\u0430 \u0441\u043b\u043e\u0442\u043e\u0432", "Switch the server hotbar slot without changing the visible selection").setValue(true);
    private final ValueSetting minDamage = new ValueSetting("\u041c\u0438\u043d. \u0443\u0440\u043e\u043d \u043f\u043e \u0446\u0435\u043b\u0438", "Minimum target damage for an explosion").setValue(2.0f).range(0.0f, 20.0f).step(0.5f);
    private final ValueSetting maxSelfDamage = new ValueSetting("\u041c\u0430\u043a\u0441. \u0443\u0440\u043e\u043d \u043f\u043e \u0441\u0435\u0431\u0435", "Maximum accepted self damage").setValue(8.0f).range(0.0f, 20.0f).step(0.5f);
    private final BooleanSetting coverWithBlock = new BooleanSetting("\u041f\u0440\u0438\u043a\u0440\u044b\u0432\u0430\u0442\u044c \u0431\u043b\u043e\u043a\u043e\u043c", "Place obsidian before a crystal when needed").setValue(true);
    private final MultiSelectSetting pauseConditions = new MultiSelectSetting("\u041f\u0430\u0443\u0437\u0430 \u043f\u0440\u0438", "Pause while selected actions are active").value("\u0415\u0434\u0435 / \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0438", "\u041b\u043e\u043c\u0430\u043d\u0438\u0438 \u0431\u043b\u043e\u043a\u0430").selected("\u0415\u0434\u0435 / \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0438", "\u041b\u043e\u043c\u0430\u043d\u0438\u0438 \u0431\u043b\u043e\u043a\u0430");
    private final BooleanSetting holeRetreat = new BooleanSetting("\u0423\u0445\u043e\u0434 \u0432 \u0445\u043e\u043b\u043a\u0443", "Move into a nearby safe hole while a target is close").setValue(false);
    private final BooleanSetting showDamage = new BooleanSetting("\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0442\u044c \u0443\u0440\u043e\u043d", "Show predicted target and self damage").setValue(true);
    private final ValueSetting actionDelay = new ValueSetting("\u0417\u0430\u0434\u0435\u0440\u0436\u043a\u0430", "Minimum delay between combat actions in ticks").setValue(0.0f).range(0.0f, 10.0f).step(1.0f);
    private final transient RotationConfig rotationConfig = new RotationConfig(new InstantCrystalSmoothMode(), true, true);
    private final transient AutoCrystalDamage damage = new AutoCrystalDamage();
    private final transient Map<Integer, Integer> attacked = new HashMap<Integer, Integer>();
    private final transient Map<Integer, Integer> killWatch = new HashMap<Integer, Integer>();
    private final transient Map<class_2338, Integer> rejected = new HashMap<class_2338, Integer>();
    private transient class_638 world;
    private transient class_1657 target;
    private transient List<class_1657> friends = List.of();
    private transient boolean facePlaceActive;
    private transient Plan plan;
    private transient Plan aimingPlan;
    private transient int aimingPlanUntil = -1;
    private transient Plan damagePreview;
    private transient Confirmation confirmation;
    private transient InventoryExchange inventorySwap;
    private transient InventoryExchange returningEquipment;
    private transient int originalSlot = -1;
    private transient int automaticSlot = -1;
    private transient int lastActionTick = -100;
    private transient int lastPlanTick = -100;
    private transient boolean restorePending;
    private transient class_2338 lastRenderBase;
    private transient class_2338 retreatHole;
    private transient long lastRenderTime;
    private transient class_1657 comboTarget;
    private transient int comboUntil = -1;
    private transient class_2338 chainBase;
    private transient int chainTarget = -1;
    private transient int chainUntil = -1;
    private transient int combatCycle;
    private transient int lastActionCycle = -1;
    private transient int lastEntityInteractionCycle = -1;
    private transient int serverSlot = -1;
    private transient boolean actionReserved;

    public AutoCrystal() {
        super("AutoCrystal", "Auto Crystal", ModuleCategory.COMBAT);
        this.setup(this.aiming, this.useAnchors, this.anchorKey, this.anchorsPerAction, this.anchorCover, this.throughWalls, this.silentSlotSwitch, this.minDamage, this.maxSelfDamage, this.coverWithBlock, this.pauseConditions, this.holeRetreat, this.showDamage, this.actionDelay, this.targetRange, this.targetPriority);
    }

    public static AutoCrystal getInstance() {
        return Instance.get(AutoCrystal.class);
    }

    @Override
    public void activate() {
        this.reset();
        this.world = AutoCrystal.mc.field_1687;
        super.activate();
    }

    @Override
    public void deactivate() {
        if (AutoCrystal.mc.field_1724 != null && this.world == AutoCrystal.mc.field_1687) {
            this.restoreEquipment();
        }
        this.reset();
        RotationController.INSTANCE.release(this);
        super.deactivate();
    }

    @EventHandler(value=4)
    public void onRotationUpdate(RotationUpdateEvent event) {
        if (event.getType() == 0) {
            ++this.combatCycle;
            this.actionReserved = false;
            this.preparePlan();
        } else if (event.getType() == 2 && this.actionReserved && this.lastEntityInteractionCycle != this.combatCycle) {
            this.execute();
        }
    }

    private void preparePlan() {
        if (this.world != AutoCrystal.mc.field_1687) {
            this.reset();
            this.world = AutoCrystal.mc.field_1687;
            RotationController.INSTANCE.release(this);
        }
        if (AutoCrystal.mc.field_1724 == null || AutoCrystal.mc.field_1687 == null || AutoCrystal.mc.field_1761 == null) {
            return;
        }
        if (this.lastPlanTick == AutoCrystal.mc.field_1724.field_6012) {
            return;
        }
        this.lastPlanTick = AutoCrystal.mc.field_1724.field_6012;
        this.damage.clear();
        this.plan = null;
        this.attacked.entrySet().removeIf(entry -> AutoCrystal.mc.field_1724.field_6012 - (Integer)entry.getValue() >= this.attackRetryTicks() || AutoCrystal.mc.field_1687.method_8469(((Integer)entry.getKey()).intValue()) == null);
        this.rejected.entrySet().removeIf(entry -> AutoCrystal.mc.field_1724.field_6012 > (Integer)entry.getValue());
        this.killWatch.entrySet().removeIf(entry -> AutoCrystal.mc.field_1724.field_6012 > (Integer)entry.getValue());
        if (this.killWatch.keySet().stream().map(arg_0 -> ((class_638)AutoCrystal.mc.field_1687).method_8469(arg_0)).anyMatch(entity -> {
            class_1657 player;
            return entity instanceof class_1657 && !(player = (class_1657)entity).method_5805();
        })) {
            this.setState(false);
            return;
        }
        if (this.restorePending) {
            this.restorePending = false;
            this.restoreEquipment();
        }
        if (this.returningEquipment != null && this.returningEquipment.pending) {
            RotationController.INSTANCE.release(this);
            return;
        }
        this.returningEquipment = null;
        this.target = this.findTarget();
        if (this.target == null || this.paused()) {
            this.aimingPlan = null;
            this.damagePreview = null;
            this.restoreEquipment();
            this.confirmation = null;
            RotationController.INSTANCE.release(this);
            return;
        }
        if (!this.confirmationReady() || PlayerInventoryComponent.isInventoryActionBusy() || this.inventorySwap != null && this.inventorySwap.pending) {
            this.aimingPlan = null;
            RotationController.INSTANCE.release(this);
            return;
        }
        this.facePlaceActive = this.facePlacing();
        ArrayList<class_1657> nearbyFriends = new ArrayList<class_1657>();
        for (class_1657 player : AutoCrystal.mc.field_1687.method_18456()) {
            if (player == AutoCrystal.mc.field_1724 || !player.method_5805() || !FriendUtils.isFriend((class_1297)player)) continue;
            nearbyFriends.add(player);
        }
        this.friends = nearbyFriends;
        this.plan = this.findPlan();
        if (this.plan == null) {
            this.aimingPlan = null;
            this.damagePreview = null;
            this.restoreEquipment();
            RotationController.INSTANCE.release(this);
            return;
        }
        this.damagePreview = this.plan;
        if (!this.actionReady() || this.yieldToMeleeAttack()) {
            RotationController.INSTANCE.release(this);
            return;
        }
        this.actionReserved = true;
        if (this.aimingPlan == null || this.aimingPlan.action() != this.plan.action() || !this.aimingPlan.pos().equals((Object)this.plan.pos()) || this.aimingPlan.crystal() != this.plan.crystal() || this.aimingPlan.target() != this.plan.target()) {
            this.aimingPlan = this.plan;
            this.aimingPlanUntil = AutoCrystal.mc.field_1724.field_6012 + 8;
        }
        if (this.aiming.isValue()) {
            RotationController.INSTANCE.rotateTo(AngleUtil.calculateAngle(this.aim(this.plan)), 2, this.rotationConfig, TaskPriority.HIGH_IMPORTANCE_2, this);
        } else {
            RotationController.INSTANCE.release(this);
        }
    }

    private class_1657 findTarget() {
        class_1657 player;
        class_1309 class_13092;
        if (this.comboActive()) {
            return this.comboTarget;
        }
        Aura aura = Aura.getInstance();
        if (aura != null && aura.isState() && (class_13092 = aura.getTarget()) instanceof class_1657 && this.validTarget(player = (class_1657)class_13092)) {
            return player;
        }
        boolean healthPriority = this.targetPriority.isSelected("Health");
        double rangeSquared = this.targetRange.getValue() * this.targetRange.getValue();
        class_1657 best = null;
        double bestScore = Double.POSITIVE_INFINITY;
        for (class_1657 player2 : AutoCrystal.mc.field_1687.method_18456()) {
            double distanceSquared;
            if (player2 == AutoCrystal.mc.field_1724 || !player2.method_5805() || player2.method_7325() || player2.method_68878() || FriendUtils.isFriend((class_1297)player2) || (distanceSquared = AutoCrystal.mc.field_1724.method_5858((class_1297)player2)) > rangeSquared) continue;
            double d = healthPriority ? (double)(player2.method_6032() + player2.method_6067()) : distanceSquared;
            double score = d;
            if (!(score < bestScore)) continue;
            bestScore = score;
            best = player2;
        }
        return best;
    }

    private boolean validTarget(class_1657 player) {
        return player != null && player != AutoCrystal.mc.field_1724 && player.method_5805() && !player.method_31481() && !player.method_7325() && !player.method_68878() && !FriendUtils.isFriend((class_1297)player) && AutoCrystal.mc.field_1687.method_8469(player.method_5628()) == player && AutoCrystal.mc.field_1724.method_5858((class_1297)player) <= (double)(this.targetRange.getValue() * this.targetRange.getValue());
    }

    private boolean comboActive() {
        return AutoCrystal.mc.field_1724 != null && AutoCrystal.mc.field_1687 != null && AutoCrystal.mc.field_1724.field_6012 <= this.comboUntil && this.validTarget(this.comboTarget);
    }

    private boolean cooperatingWithAura() {
        Aura aura = Aura.getInstance();
        return aura != null && aura.isState() && aura.getTarget() != null && aura.getTarget().method_5805();
    }

    private boolean yieldToMeleeAttack() {
        if (!this.cooperatingWithAura() || this.comboActive() || this.plan.action() == Action.CRYSTAL_BREAK) {
            return false;
        }
        Aura aura = Aura.getInstance();
        AttackPerpetrator.AttackPerpetratorConfigurable config = aura.getConfig();
        AttackHandler handler = Main.getInstance().getAttackPerpetrator().getAttackHandler();
        return handler.isWithinAttackRange(config) && handler.canAttack(config, 0);
    }

    public boolean isAuraAttackBlocked() {
        return this.isState() && AutoCrystal.mc.field_1724 != null && AutoCrystal.mc.field_1687 == this.world && (this.lastActionCycle == this.combatCycle || this.actionReserved && this.plan != null && (!this.aiming.isValue() || RotationController.INSTANCE.getRotationPlanTaskProcessor().fetchActiveTaskProvider() == this));
    }

    public void onAuraHit(class_1309 attackedTarget) {
        class_1657 player;
        if (!(this.isState() && AutoCrystal.mc.field_1724 != null && AutoCrystal.mc.field_1687 != null && attackedTarget instanceof class_1657 && this.validTarget(player = (class_1657)attackedTarget))) {
            return;
        }
        this.comboTarget = player;
        this.comboUntil = AutoCrystal.mc.field_1724.field_6012 + 10;
        this.lastEntityInteractionCycle = this.combatCycle;
    }

    @EventHandler(value=4)
    public void onPacket(PacketEvent event) {
        if (!event.isSend() || event.isCancelled()) {
            return;
        }
        class_2596<?> class_25962 = event.getPacket();
        if (class_25962 instanceof class_2868) {
            class_2868 selected = (class_2868)class_25962;
            this.serverSlot = selected.method_12442();
        }
        if (event.getPacket() instanceof class_2824) {
            this.lastEntityInteractionCycle = this.combatCycle;
        }
    }

    private boolean paused() {
        return this.pauseConditions.isSelected("\u0415\u0434\u0435 / \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0438") && AutoCrystal.mc.field_1724.method_6115() || this.pauseConditions.isSelected("\u041b\u043e\u043c\u0430\u043d\u0438\u0438 \u0431\u043b\u043e\u043a\u0430") && AutoCrystal.mc.field_1761.method_2923() || PlayerInventoryComponent.shouldSkipExecution();
    }

    private boolean confirmationReady() {
        boolean ready;
        if (this.confirmation == null) {
            return true;
        }
        class_2338 pos = this.confirmation.pos();
        class_2680 state = AutoCrystal.mc.field_1687.method_8320(pos);
        switch (this.confirmation.action().ordinal()) {
            default: {
                throw new MatchException(null, null);
            }
            case 2: {
                ready = state.method_27852(class_2246.field_10540);
                break;
            }
            case 1: {
                if (!AutoCrystal.mc.field_1687.method_8390(class_1511.class, new class_238(pos.method_10084()).method_1014(1.0), crystal -> crystal.method_5805() && crystal.method_24515().method_10074().equals((Object)pos)).isEmpty()) {
                    ready = true;
                    break;
                }
                ready = false;
                break;
            }
            case 3: {
                ready = state.method_27852(class_2246.field_23152);
                break;
            }
            case 4: {
                if (!state.method_27852(class_2246.field_23152) || (Integer)state.method_11654((class_2769)class_4969.field_23153) > this.confirmation.charges()) {
                    ready = true;
                    break;
                }
                ready = false;
                break;
            }
            case 5: {
                if (!state.method_27852(class_2246.field_23152)) {
                    ready = true;
                    break;
                }
                ready = false;
                break;
            }
            case 0: {
                ready = true;
            }
        }
        if (!ready && AutoCrystal.mc.field_1724.field_6012 <= this.confirmation.deadline()) {
            return false;
        }
        if (!ready) {
            this.rejected.put(pos, AutoCrystal.mc.field_1724.field_6012 + 10);
            this.restorePending = true;
        }
        this.confirmation = null;
        return true;
    }

    private Plan findPlan() {
        Plan bestCrystal;
        ArrayList<Seed> seeds = new ArrayList<Seed>();
        class_243 eye = AutoCrystal.mc.field_1724.method_33571();
        double maxPlaceDistanceSquared = (this.placeReach() + 0.9) * (this.placeReach() + 0.9);
        class_238 crystalSearch = AutoCrystal.mc.field_1724.method_5829().method_1014(4.0);
        Plan bestBreak = null;
        for (class_1511 crystal : AutoCrystal.mc.field_1687.method_8390(class_1511.class, crystalSearch, this::canReachCrystal)) {
            Plan candidate = this.evaluate(this.seed(Action.CRYSTAL_BREAK, crystal.method_24515().method_10074(), crystal.method_5628()), null);
            if (candidate == null || bestBreak != null && !(candidate.score() > bestBreak.score())) continue;
            bestBreak = candidate;
        }
        if (bestBreak != null) {
            return bestBreak;
        }
        Plan retained = this.retainAimingPlan();
        if (retained != null && AutoCrystal.mc.field_1724.field_6012 <= this.aimingPlanUntil) {
            return retained;
        }
        Plan continuation = this.continueCrystalChain();
        boolean crystals = this.available(class_1802.field_8301);
        boolean obsidian = this.coverWithBlock.isValue() && crystals && this.available(class_1802.field_8281);
        boolean anchors = this.useAnchors.isValue() && !AutoCrystal.mc.field_1724.method_5715() && (this.anchorKey.getKey() == -1 || class_3675.method_15987((class_1041)mc.method_22683(), (int)this.anchorKey.getKey()));
        boolean glowstone = anchors && this.available(class_1802.field_8801);
        boolean newAnchors = glowstone && this.available(class_1802.field_23141);
        int anchorCharges = anchors ? this.requiredCharges() : 0;
        class_2338 center = class_2338.method_49638((class_2374)eye);
        for (int x = -5; x <= 5; ++x) {
            for (int y = -5; y <= 5; ++y) {
                for (int z = -5; z <= 5; ++z) {
                    boolean existingAnchor;
                    boolean crystalBase;
                    double dz;
                    double dy;
                    class_2338 pos = center.method_10069(x, y, z);
                    double dx = (double)pos.method_10263() + 0.5 - eye.field_1352;
                    if (dx * dx + (dy = (double)pos.method_10264() + 0.5 - eye.field_1351) * dy + (dz = (double)pos.method_10260() + 0.5 - eye.field_1350) * dz > maxPlaceDistanceSquared || this.rejected.containsKey(pos)) continue;
                    class_2680 state = AutoCrystal.mc.field_1687.method_8320(pos);
                    boolean bl = crystalBase = state.method_27852(class_2246.field_10540) || state.method_27852(class_2246.field_9987);
                    if (crystals && (crystalBase || obsidian && state.method_45474()) && this.crystalSpace(pos)) {
                        if (crystalBase) {
                            seeds.add(this.seed(Action.CRYSTAL_PLACE, pos, -1));
                        } else if (obsidian && this.replaceable(pos) && this.futureBaseReachable(pos)) {
                            seeds.add(this.seed(Action.OBSIDIAN_PLACE, pos, -1));
                        }
                    }
                    if (!anchors || !(existingAnchor = state.method_27852(class_2246.field_23152)) && (!newAnchors || !state.method_45474()) || !this.anchorExplodes(pos)) continue;
                    if (existingAnchor) {
                        int charges = (Integer)state.method_11654((class_2769)class_4969.field_23153);
                        if (charges >= anchorCharges) {
                            seeds.add(this.seed(Action.ANCHOR_DETONATE, pos, -1));
                            continue;
                        }
                        if (!glowstone) continue;
                        seeds.add(this.seed(Action.ANCHOR_CHARGE, pos, -1));
                        continue;
                    }
                    if (!newAnchors || !this.replaceable(pos)) continue;
                    seeds.add(this.seed(Action.ANCHOR_PLACE, pos, -1));
                }
            }
        }
        seeds.sort(Comparator.comparingDouble(Seed::roughScore).reversed());
        EnumMap<Action, Integer> evaluated = new EnumMap<Action, Integer>(Action.class);
        Plan plan = bestCrystal = retained != null && !retained.action().anchor ? retained : continuation;
        if (continuation != null && (bestCrystal == null || continuation.score() > bestCrystal.score())) {
            bestCrystal = continuation;
        }
        Plan bestAnchor = anchors && retained != null && retained.action().anchor ? retained : null;
        for (Seed seed : seeds) {
            class_3965 hit;
            int count = evaluated.getOrDefault((Object)seed.action(), 0);
            if (count >= 48) continue;
            class_3965 class_39652 = hit = seed.action() == Action.CRYSTAL_BREAK ? null : this.interactionHit(seed.action(), seed.pos());
            if (seed.action() != Action.CRYSTAL_BREAK && hit == null) continue;
            evaluated.put(seed.action(), count + 1);
            Plan candidate = this.evaluate(seed, hit);
            if (candidate == null) continue;
            if (candidate.action().anchor) {
                if (bestAnchor != null && !(candidate.score() > bestAnchor.score() * 1.04 + 0.1)) continue;
                bestAnchor = candidate;
                continue;
            }
            if (bestCrystal != null && !(candidate.score() > bestCrystal.score() * 1.04 + 0.1)) continue;
            bestCrystal = candidate;
        }
        if (bestCrystal == null) {
            return bestAnchor;
        }
        if (bestAnchor != null) {
            boolean worthwhile;
            float health = this.target.method_6032() + this.target.method_6067();
            boolean finishing = bestAnchor.targetDamage() >= health && bestCrystal.targetDamage() < health;
            boolean bl = worthwhile = bestAnchor.targetDamage() >= bestCrystal.targetDamage() + 3.0f && bestAnchor.score() > bestCrystal.score() * 1.6 + 1.0;
            if (finishing || worthwhile) {
                return bestAnchor;
            }
        }
        return bestCrystal;
    }

    private Plan continueCrystalChain() {
        if (this.chainBase == null || this.target.method_5628() != this.chainTarget || AutoCrystal.mc.field_1724.field_6012 > this.chainUntil || this.rejected.containsKey(this.chainBase) || !this.available(class_1802.field_8301) || !this.crystalSpace(this.chainBase)) {
            return null;
        }
        class_2680 state = AutoCrystal.mc.field_1687.method_8320(this.chainBase);
        if (!state.method_27852(class_2246.field_10540) && !state.method_27852(class_2246.field_9987)) {
            return null;
        }
        class_3965 hit = this.interactionHit(Action.CRYSTAL_PLACE, this.chainBase);
        return hit == null ? null : this.evaluate(new Seed(Action.CRYSTAL_PLACE, this.chainBase, -1, 0.0), hit);
    }

    private Plan retainAimingPlan() {
        boolean availableAction;
        if (this.aimingPlan == null || this.aimingPlan.target() != this.target.method_5628() || AutoCrystal.mc.field_1724.field_6012 > this.aimingPlanUntil || this.rejected.containsKey(this.aimingPlan.pos())) {
            return null;
        }
        Action action = this.aimingPlan.action();
        class_2338 pos = this.aimingPlan.pos();
        class_2680 state = AutoCrystal.mc.field_1687.method_8320(pos);
        switch (action.ordinal()) {
            default: {
                throw new MatchException(null, null);
            }
            case 1: {
                if (this.available(class_1802.field_8301) && this.crystalSpace(pos) && (state.method_27852(class_2246.field_10540) || state.method_27852(class_2246.field_9987))) {
                    availableAction = true;
                    break;
                }
                availableAction = false;
                break;
            }
            case 2: {
                if (this.coverWithBlock.isValue() && this.available(class_1802.field_8281) && this.available(class_1802.field_8301) && this.replaceable(pos) && this.crystalSpace(pos)) {
                    availableAction = true;
                    break;
                }
                availableAction = false;
                break;
            }
            case 3: {
                if (this.useAnchors.isValue() && this.available(class_1802.field_23141) && this.replaceable(pos)) {
                    availableAction = true;
                    break;
                }
                availableAction = false;
                break;
            }
            case 4: {
                if (this.useAnchors.isValue() && this.available(class_1802.field_8801) && state.method_27852(class_2246.field_23152) && (Integer)state.method_11654((class_2769)class_4969.field_23153) < this.requiredCharges()) {
                    availableAction = true;
                    break;
                }
                availableAction = false;
                break;
            }
            case 5: {
                if (this.useAnchors.isValue() && state.method_27852(class_2246.field_23152) && (Integer)state.method_11654((class_2769)class_4969.field_23153) >= this.requiredCharges()) {
                    availableAction = true;
                    break;
                }
                availableAction = false;
                break;
            }
            case 0: {
                availableAction = false;
            }
        }
        if (!availableAction) {
            return null;
        }
        class_3965 hit = this.interactionHit(action, pos);
        return hit == null ? null : this.evaluate(new Seed(action, pos, this.aimingPlan.crystal(), 0.0), hit);
    }

    private double placeReach() {
        return Math.min(4.5, AutoCrystal.mc.field_1724.method_55754());
    }

    private Seed seed(Action action, class_2338 pos, int crystal) {
        class_243 explosion = action.anchor ? pos.method_46558() : class_243.method_24955((class_2382)pos.method_10084());
        class_243 future = this.damage.predict((class_1309)this.target, this.explosionHorizon(action, pos));
        double targetDistance = future.method_1022(explosion);
        double selfDistance = AutoCrystal.mc.field_1724.method_73189().method_1022(explosion);
        return new Seed(action, pos, crystal, selfDistance * 0.15 - targetDistance - (double)action.steps * 0.2);
    }

    private Plan evaluate(Seed seed, class_3965 hit) {
        class_243 explosion;
        Action action = seed.action();
        class_243 class_2432 = explosion = action.anchor ? seed.pos().method_46558() : class_243.method_24955((class_2382)seed.pos().method_10084());
        if (action == Action.CRYSTAL_BREAK) {
            class_1511 crystal;
            class_1297 class_12972 = AutoCrystal.mc.field_1687.method_8469(seed.crystal());
            if (!(class_12972 instanceof class_1511) || !this.canReachCrystal(crystal = (class_1511)class_12972)) {
                return null;
            }
            explosion = crystal.method_73189();
        }
        class_2338 cover = action == Action.OBSIDIAN_PLACE ? seed.pos() : null;
        class_2338 removed = action.anchor ? seed.pos() : null;
        float power = action.anchor ? 5.0f : 6.0f;
        int steps = this.remainingActions(action, seed.pos());
        int horizon = this.explosionHorizon(action, seed.pos());
        if (!action.anchor && action != Action.CRYSTAL_BREAK) {
            class_243 futureEye = this.damage.predict((class_1309)AutoCrystal.mc.field_1724, horizon).method_1031(0.0, AutoCrystal.mc.field_1724.method_23320() - AutoCrystal.mc.field_1724.method_23318(), 0.0);
            class_238 crystalBox = new class_238(explosion.field_1352 - 1.0, explosion.field_1351, explosion.field_1350 - 1.0, explosion.field_1352 + 1.0, explosion.field_1351 + 2.0, explosion.field_1350 + 1.0);
            class_243 closest = new class_243(Math.clamp(futureEye.field_1352, crystalBox.field_1323, crystalBox.field_1320), Math.clamp(futureEye.field_1351, crystalBox.field_1322, crystalBox.field_1325), Math.clamp(futureEye.field_1350, crystalBox.field_1321, crystalBox.field_1324));
            if (futureEye.method_1022(closest) > Math.min(3.0, AutoCrystal.mc.field_1724.method_55755())) {
                return null;
            }
        }
        float self = this.damage.estimate(explosion, (class_1309)AutoCrystal.mc.field_1724, power, cover, removed, horizon, true);
        float f = action.anchor ? this.anchorCover.getValue() : 0.0f;
        if (self + f > this.maxSelfDamage.getValue() || self + 1.0f >= AutoCrystal.mc.field_1724.method_6032() + AutoCrystal.mc.field_1724.method_6067()) {
            return null;
        }
        float enemy = this.damage.estimate(explosion, (class_1309)this.target, power, cover, removed, horizon, false);
        float health = this.target.method_6032() + this.target.method_6067();
        float f2 = this.facePlaceActive || enemy >= health ? 1.0f : this.minDamage.getValue();
        if (enemy < f2 || enemy <= self && enemy < health) {
            return null;
        }
        float friendDamage = 0.0f;
        for (class_1657 friend : this.friends) {
            if (!friend.method_5805()) continue;
            float dealt = this.damage.estimate(explosion, (class_1309)friend, power, cover, removed, horizon, true);
            if (dealt > this.maxSelfDamage.getValue() || dealt + 1.0f >= friend.method_6032() + friend.method_6067()) {
                return null;
            }
            friendDamage = Math.max(friendDamage, dealt);
        }
        double utility = (double)(Math.min(enemy, health + 4.0f) - self * 0.9f) - (double)friendDamage * 0.75;
        if (enemy >= health) {
            utility += 4.0;
        }
        if (utility <= 0.0) {
            return null;
        }
        double distance = explosion.method_1022(this.damage.predict((class_1309)this.target, horizon));
        double score = utility / (1.0 + (double)(steps - 1) * 0.22) - distance * (this.target.method_24828() ? 0.03 : 0.15) - (double)horizon * 0.025;
        if (action == Action.ANCHOR_PLACE) {
            score -= 1.25;
        } else if (action == Action.ANCHOR_CHARGE) {
            score -= 0.4;
        }
        if (seed.pos().equals((Object)this.lastRenderBase) && AutoCrystal.mc.field_1724.field_6012 - this.lastActionTick <= 12 && action != Action.OBSIDIAN_PLACE && action != Action.ANCHOR_PLACE) {
            score += 0.1;
        }
        if (score <= 0.0) {
            return null;
        }
        return new Plan(action, seed.pos(), seed.crystal(), this.target.method_5628(), hit, enemy, self, score);
    }

    private int explosionHorizon(Action action, class_2338 pos) {
        class_640 entry = mc.method_1562() == null ? null : mc.method_1562().method_2871(AutoCrystal.mc.field_1724.method_5667());
        double latency = entry == null ? 0.0 : Math.clamp((double)entry.method_2959() / 50.0, 0.0, 8.0);
        int steps = this.remainingActions(action, pos);
        double interval = Math.max(1.0 + latency, (double)Math.max(1.0f, this.actionDelay.getValue()));
        double cooldown = Math.max(0.0f, this.actionDelay.getValue() - (float)(AutoCrystal.mc.field_1724.field_6012 - this.lastActionTick));
        return (int)Math.clamp(Math.ceil(1.0 + latency * 0.5 + (double)(steps - 1) * interval + cooldown), 1.0, 24.0);
    }

    private int remainingActions(Action action, class_2338 pos) {
        if (!action.anchor) {
            return action.steps;
        }
        class_2680 state = AutoCrystal.mc.field_1687.method_8320(pos);
        int charges = state.method_27852(class_2246.field_23152) ? (Integer)state.method_11654((class_2769)class_4969.field_23153) : 0;
        return (action == Action.ANCHOR_PLACE ? 1 : 0) + Math.max(0, this.requiredCharges() - charges) + 1;
    }

    private boolean facePlacing() {
        if (this.target.method_6032() + this.target.method_6067() <= 8.0f) {
            return true;
        }
        if (GLFW.glfwGetKey((long)mc.method_22683().method_4490(), (int)340) == 1) {
            return true;
        }
        for (class_1799 armor : EntityCompat.getArmorItems((class_1309)this.target)) {
            if (armor.method_7960() || armor.method_7936() <= 0 || !(100.0f * (float)(armor.method_7936() - armor.method_7919()) / (float)armor.method_7936() < 5.0f)) continue;
            return true;
        }
        return false;
    }

    private boolean canReachCrystal(class_1511 crystal) {
        if (!crystal.method_5805() || this.attacked.containsKey(crystal.method_5628())) {
            return false;
        }
        class_243 eye = AutoCrystal.mc.field_1724.method_33571();
        class_238 box = crystal.method_5829();
        class_243 closest = new class_243(Math.clamp(eye.field_1352, box.field_1323, box.field_1320), Math.clamp(eye.field_1351, box.field_1322, box.field_1325), Math.clamp(eye.field_1350, box.field_1321, box.field_1324));
        boolean visible = AutoCrystal.mc.field_1687.method_17742(new class_3959(eye, box.method_1005(), class_3959.class_3960.field_17558, class_3959.class_242.field_1348, (class_1297)AutoCrystal.mc.field_1724)).method_17783() == class_239.class_240.field_1333;
        return eye.method_1022(closest) <= (double)(visible ? 3.0f : 3.0f);
    }

    private boolean crystalSpace(class_2338 pos) {
        return AutoCrystal.mc.field_1687.method_8320(pos.method_10084()).method_26215() && AutoCrystal.mc.field_1687.method_8335(null, new class_238(pos.method_10084()).method_1012(0.0, 1.0, 0.0)).isEmpty();
    }

    private boolean replaceable(class_2338 pos) {
        return AutoCrystal.mc.field_1687.method_8320(pos).method_45474() && AutoCrystal.mc.field_1687.method_8316(pos).method_15769() && AutoCrystal.mc.field_1687.method_8335(null, new class_238(pos)).isEmpty();
    }

    private boolean futureBaseReachable(class_2338 pos) {
        class_243 point = new class_243((double)pos.method_10263() + 0.5, (double)pos.method_10264() + 0.99, (double)pos.method_10260() + 0.5);
        return AutoCrystal.mc.field_1724.method_33571().method_1022(point) <= this.placeReach() && (this.throughWalls.isValue() || AutoCrystal.mc.field_1687.method_17742(new class_3959(AutoCrystal.mc.field_1724.method_33571(), point, class_3959.class_3960.field_17559, class_3959.class_242.field_1348, (class_1297)AutoCrystal.mc.field_1724)).method_17783() == class_239.class_240.field_1333);
    }

    @EventHandler
    public void onInput(InputEvent event) {
        double dz;
        if (!this.holeRetreat.isValue() || AutoCrystal.mc.field_1724 == null || AutoCrystal.mc.field_1687 == null || this.target == null || AutoCrystal.mc.field_1724.method_5858((class_1297)this.target) > 36.0 || AutoCrystal.mc.field_1755 != null) {
            this.retreatHole = null;
            return;
        }
        if (this.isSafeHole(AutoCrystal.mc.field_1724.method_24515())) {
            this.retreatHole = null;
            return;
        }
        if (this.retreatHole == null || !this.isSafeHole(this.retreatHole)) {
            this.retreatHole = this.findNearestHole();
        }
        if (this.retreatHole == null) {
            return;
        }
        double dx = (double)this.retreatHole.method_10263() + 0.5 - AutoCrystal.mc.field_1724.method_23317();
        if (dx * dx + (dz = (double)this.retreatHole.method_10260() + 0.5 - AutoCrystal.mc.field_1724.method_23321()) * dz < 0.12) {
            event.inputNone();
            this.retreatHole = null;
            return;
        }
        double yaw = Math.toRadians(AutoCrystal.mc.field_1724.method_36454());
        double forward = -dx * Math.sin(yaw) + dz * Math.cos(yaw);
        double left = dx * Math.cos(yaw) + dz * Math.sin(yaw);
        event.setDirectional(forward > 0.12, forward < -0.12, left > 0.12, left < -0.12);
    }

    private class_2338 findNearestHole() {
        class_2338 origin = AutoCrystal.mc.field_1724.method_24515();
        class_2338 best = null;
        double bestDistance = Double.POSITIVE_INFINITY;
        for (int x = -4; x <= 4; ++x) {
            for (int y = -1; y <= 1; ++y) {
                for (int z = -4; z <= 4; ++z) {
                    double distance;
                    class_2338 candidate = origin.method_10069(x, y, z);
                    if (!this.isSafeHole(candidate) || !((distance = AutoCrystal.mc.field_1724.method_5649((double)candidate.method_10263() + 0.5, (double)candidate.method_10264(), (double)candidate.method_10260() + 0.5)) < bestDistance)) continue;
                    best = candidate.method_10062();
                    bestDistance = distance;
                }
            }
        }
        return best;
    }

    private boolean isSafeHole(class_2338 pos) {
        if (!(AutoCrystal.mc.field_1687.method_8320(pos).method_26215() && AutoCrystal.mc.field_1687.method_8320(pos.method_10084()).method_26215() && this.isHoleBlock(pos.method_10074()))) {
            return false;
        }
        int walls = 0;
        for (class_2350 direction : class_2350.class_2353.field_11062) {
            class_2338 wall = pos.method_10093(direction);
            if (!this.isHoleBlock(wall) || !this.isHoleBlock(wall.method_10084())) continue;
            ++walls;
        }
        return walls >= 3;
    }

    private boolean isHoleBlock(class_2338 pos) {
        class_2680 state = AutoCrystal.mc.field_1687.method_8320(pos);
        return state.method_27852(class_2246.field_9987) || state.method_27852(class_2246.field_10540);
    }

    private boolean anchorExplodes(class_2338 pos) {
        return (Boolean)AutoCrystal.mc.field_1687.method_75728().method_75697(class_12206.field_63757, pos) == false;
    }

    private int requiredCharges() {
        return Math.clamp((long)this.anchorsPerAction.getInt(), 1, 4);
    }

    private class_3965 interactionHit(Action action, class_2338 pos) {
        boolean placement = action == Action.OBSIDIAN_PLACE || action == Action.ANCHOR_PLACE;
        class_3965 best = null;
        double nearest = Double.POSITIVE_INFINITY;
        class_243 eye = AutoCrystal.mc.field_1724.method_33571();
        for (class_2350 side : class_2350.values()) {
            class_2338 support = placement ? pos.method_10093(side.method_10153()) : pos;
            class_2680 state = AutoCrystal.mc.field_1687.method_8320(support);
            if (state.method_26220((class_1922)AutoCrystal.mc.field_1687, support).method_1110() || placement && (state.method_27852(class_2246.field_23152) || AutoCrystal.mc.field_1687.method_8321(support) != null || state.method_27852(class_2246.field_9980))) continue;
            for (class_238 shape : state.method_26218((class_1922)AutoCrystal.mc.field_1687, support).method_1090()) {
                double distance;
                class_3965 hit = this.faceHit(support, shape.method_996(support), side, eye);
                if (hit == null || !((distance = eye.method_1025(hit.method_17784())) < nearest)) continue;
                nearest = distance;
                best = hit;
            }
        }
        return best;
    }

    private class_3965 faceHit(class_2338 support, class_238 box, class_2350 side, class_243 eye) {
        class_243 normal = class_243.method_24954((class_2382)side.method_62675());
        double insetX = Math.min(0.05, box.method_17939() * 0.25);
        double insetY = Math.min(0.05, box.method_17940() * 0.25);
        double insetZ = Math.min(0.05, box.method_17941() * 0.25);
        double[] samples = new double[]{0.5, 0.15, 0.85};
        for (int sample = -1; sample < 9; ++sample) {
            double x;
            double a = sample < 0 ? 0.0 : samples[sample / 3];
            double b = sample < 0 ? 0.0 : samples[sample % 3];
            double d = x = sample < 0 ? Math.clamp(eye.field_1352, box.field_1323 + insetX, box.field_1320 - insetX) : box.field_1323 + box.method_17939() * a;
            double y = sample < 0 ? Math.clamp(eye.field_1351, box.field_1322 + insetY, box.field_1325 - insetY) : box.field_1322 + box.method_17940() * (side.method_10166() == class_2350.class_2351.field_11048 ? a : b);
            double z = sample < 0 ? Math.clamp(eye.field_1350, box.field_1321 + insetZ, box.field_1324 - insetZ) : box.field_1321 + box.method_17941() * b;
            switch (side) {
                case field_11033: {
                    y = box.field_1322;
                    break;
                }
                case field_11036: {
                    y = box.field_1325;
                    break;
                }
                case field_11043: {
                    z = box.field_1321;
                    break;
                }
                case field_11035: {
                    z = box.field_1324;
                    break;
                }
                case field_11039: {
                    x = box.field_1323;
                    break;
                }
                case field_11034: {
                    x = box.field_1320;
                }
            }
            class_243 point = new class_243(x, y, z);
            if (eye.method_1025(point) > this.placeReach() * this.placeReach() || eye.method_1020(point).method_1026(normal) <= 1.0E-7) continue;
            class_3965 hit = AutoCrystal.mc.field_1687.method_17742(new class_3959(eye, point.method_1020(normal.method_1021(0.001)), class_3959.class_3960.field_17559, class_3959.class_242.field_1348, (class_1297)AutoCrystal.mc.field_1724));
            if (hit.method_17783() == class_239.class_240.field_1332 && hit.method_17777().equals((Object)support) && hit.method_17780() == side && !hit.method_17781()) {
                return hit;
            }
            if (!this.throughWalls.isValue()) continue;
            return new class_3965(point, side, support, false);
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void execute() {
        class_1268 hand;
        class_1293 strong;
        float aimTolerance;
        class_3965 hit;
        if (!this.isState() || AutoCrystal.mc.field_1724 == null || AutoCrystal.mc.field_1687 == null || AutoCrystal.mc.field_1761 == null || this.plan == null || this.target == null || this.paused() || this.confirmation != null || !this.validTarget(this.target) || !this.actionReady() || PlayerInventoryComponent.isInventoryActionBusy() || this.target.method_5628() != this.plan.target() || this.returningEquipment != null && this.returningEquipment.pending || this.inventorySwap != null && this.inventorySwap.pending) {
            return;
        }
        Action action = this.plan.action();
        class_2338 pos = this.plan.pos();
        class_2680 state = AutoCrystal.mc.field_1687.method_8320(pos);
        if (action.anchor && (!this.useAnchors.isValue() || AutoCrystal.mc.field_1724.method_5715() || this.anchorKey.getKey() != -1 && !class_3675.method_15987((class_1041)mc.method_22683(), (int)this.anchorKey.getKey()) || !this.anchorExplodes(pos))) {
            return;
        }
        if (!(action != Action.CRYSTAL_PLACE || (state.method_27852(class_2246.field_10540) || state.method_27852(class_2246.field_9987)) && this.crystalSpace(pos))) {
            return;
        }
        if (!(action != Action.OBSIDIAN_PLACE && action != Action.ANCHOR_PLACE || this.replaceable(pos))) {
            return;
        }
        if (action == Action.OBSIDIAN_PLACE && !this.crystalSpace(pos)) {
            return;
        }
        if (!(action != Action.ANCHOR_CHARGE && action != Action.ANCHOR_DETONATE || state.method_27852(class_2246.field_23152))) {
            return;
        }
        if (action == Action.ANCHOR_DETONATE && (Integer)state.method_11654((class_2769)class_4969.field_23153) < this.requiredCharges()) {
            return;
        }
        if (action == Action.ANCHOR_CHARGE && (Integer)state.method_11654((class_2769)class_4969.field_23153) >= this.requiredCharges()) {
            return;
        }
        class_3965 class_39652 = hit = action == Action.CRYSTAL_BREAK ? null : this.interactionHit(action, pos);
        if (action != Action.CRYSTAL_BREAK && hit == null) {
            return;
        }
        Plan checked = this.evaluate(new Seed(action, pos, this.plan.crystal(), 0.0), hit);
        if (checked == null) {
            return;
        }
        Angle desiredAngle = AngleUtil.calculateAngle(this.aim(checked));
        if (this.aiming.isValue() && RotationController.INSTANCE.getRotationPlanTaskProcessor().fetchActiveTaskProvider() != this) {
            return;
        }
        Angle currentAngle = this.aiming.isValue() ? RotationController.INSTANCE.getRotation() : AngleUtil.cameraAngle();
        Angle delta = AngleUtil.calculateDelta(currentAngle, desiredAngle);
        float f = aimTolerance = this.aiming.isValue() ? 8.0f : 1.75f;
        if (Math.abs(delta.getYaw()) > aimTolerance || Math.abs(delta.getPitch()) > aimTolerance) {
            return;
        }
        if (hit != null && !this.throughWalls.isValue()) {
            class_243 eye = AutoCrystal.mc.field_1724.method_33571();
            class_3965 actualHit = AutoCrystal.mc.field_1687.method_17742(new class_3959(eye, eye.method_1019(RotationController.INSTANCE.getServerAngle().toVector().method_1021(this.placeReach())), class_3959.class_3960.field_17559, class_3959.class_242.field_1348, (class_1297)AutoCrystal.mc.field_1724));
            if (actualHit.method_17783() != class_239.class_240.field_1332 || actualHit.method_17781() || !actualHit.method_17777().equals((Object)hit.method_17777())) {
                return;
            }
            hit = actualHit;
        }
        if (action == Action.CRYSTAL_BREAK) {
            class_1293 weak = AutoCrystal.mc.field_1724.method_6112(class_1294.field_5911);
            strong = AutoCrystal.mc.field_1724.method_6112(class_1294.field_5910);
            if (weak != null && (strong == null || strong.method_5578() < weak.method_5578())) {
                if (!this.equipMain(this::weapon)) {
                    return;
                }
            } else if (this.automaticSlot >= 0) {
                this.selectActionSlot(AutoCrystal.mc.field_1724.method_31548().method_67532());
            }
            hand = class_1268.field_5808;
        } else if (action == Action.ANCHOR_DETONATE) {
            if (!this.equipMain(stack -> !stack.method_31574(class_1802.field_8801) && !stack.method_31574(class_1802.field_23141))) {
                return;
            }
            hand = class_1268.field_5808;
        } else {
            class_1792 item = switch (action.ordinal()) {
                case 1 -> class_1802.field_8301;
                case 2 -> class_1802.field_8281;
                case 3 -> class_1802.field_23141;
                case 4 -> class_1802.field_8801;
                default -> class_1802.field_8162;
            };
            hand = this.equip(item);
            if (hand == null) {
                return;
            }
        }
        if (PlayerInventoryComponent.isInventoryActionBusy()) {
            return;
        }
        if (action == Action.CRYSTAL_BREAK) {
            class_1511 crystal;
            class_1297 strongEntity = AutoCrystal.mc.field_1687.method_8469(this.plan.crystal());
            if (!(strongEntity instanceof class_1511) || !this.canReachCrystal(crystal = (class_1511)strongEntity)) {
                return;
            }
            class_243 eye = AutoCrystal.mc.field_1724.method_33571();
            double reach = Math.min(3.0, AutoCrystal.mc.field_1724.method_55755());
            if (!crystal.method_5829().method_992(eye, eye.method_1019(currentAngle.toVector().method_1021(reach))).isPresent() || !crystal.method_5829().method_992(eye, eye.method_1019(RotationController.INSTANCE.getServerAngle().toVector().method_1021(reach))).isPresent()) {
                return;
            }
            int visibleSlot = AutoCrystal.mc.field_1724.method_31548().method_67532();
            try {
                if (this.silentSlotSwitch.isValue() && this.automaticSlot >= 0) {
                    AutoCrystal.mc.field_1724.method_31548().method_61496(this.automaticSlot);
                }
                AutoCrystal.mc.field_1761.method_2918((class_1657)AutoCrystal.mc.field_1724, (class_1297)crystal);
            }
            finally {
                if (this.silentSlotSwitch.isValue()) {
                    AutoCrystal.mc.field_1724.method_31548().method_61496(visibleSlot);
                }
            }
            this.attacked.put(crystal.method_5628(), AutoCrystal.mc.field_1724.field_6012);
            this.killWatch.put(this.target.method_5628(), AutoCrystal.mc.field_1724.field_6012 + 40);
            this.restorePending = true;
        } else {
            class_3965 actionHit = hit;
            PlayerIntersectionUtil.sendSequencedPacket(sequence -> new class_2885(hand, actionHit, sequence));
            int charges = state.method_27852(class_2246.field_23152) ? (Integer)state.method_11654((class_2769)class_4969.field_23153) : 0;
            this.confirmation = new Confirmation(action, pos, charges, AutoCrystal.mc.field_1724.field_6012 + this.confirmationTicks());
            if (action == Action.OBSIDIAN_PLACE || action == Action.CRYSTAL_PLACE) {
                this.chainBase = pos.method_10062();
                this.chainTarget = this.target.method_5628();
                this.chainUntil = this.confirmation.deadline() + 4;
            }
            if (action == Action.ANCHOR_DETONATE) {
                this.killWatch.put(this.target.method_5628(), AutoCrystal.mc.field_1724.field_6012 + 40);
                this.restorePending = true;
            }
        }
        AutoCrystal.mc.field_1724.method_6104(hand);
        this.lastActionTick = AutoCrystal.mc.field_1724.field_6012;
        this.lastActionCycle = this.combatCycle;
        this.lastRenderBase = pos;
        this.lastRenderTime = System.currentTimeMillis();
        this.plan = null;
        this.aimingPlan = null;
    }

    private boolean actionReady() {
        return this.lastActionCycle != this.combatCycle && AutoCrystal.mc.field_1724.field_6012 - this.lastActionTick >= Math.max(1, this.actionDelay.getInt());
    }

    private int confirmationTicks() {
        class_640 entry = AutoCrystal.mc.field_1724.field_3944.method_2871(AutoCrystal.mc.field_1724.method_5667());
        return Math.clamp((long)((entry == null ? 0 : entry.method_2959() + 49) / 50 + 2), 2, 12);
    }

    private int attackRetryTicks() {
        class_640 entry = AutoCrystal.mc.field_1724.field_3944.method_2871(AutoCrystal.mc.field_1724.method_5667());
        return Math.clamp((long)(((entry == null ? 0 : entry.method_2959()) + 49) / 50 + 1), 2, 10);
    }

    private class_243 aim(Plan action) {
        class_1297 class_12972;
        if (action.action() == Action.CRYSTAL_BREAK && (class_12972 = AutoCrystal.mc.field_1687.method_8469(action.crystal())) instanceof class_1511) {
            class_1511 crystal = (class_1511)class_12972;
            return crystal.method_5829().method_1005();
        }
        return action.hit() == null ? action.pos().method_46558() : action.hit().method_17784();
    }

    private boolean available(class_1792 item) {
        return AutoCrystal.mc.field_1724.method_6047().method_31574(item) || AutoCrystal.mc.field_1724.method_6079().method_31574(item) || PlayerInventoryUtil.getSlot(item, this::usableInventorySlot) != null;
    }

    private boolean usableInventorySlot(class_1735 slot) {
        return !PlayerInventoryUtil.isBundleBackedSlot(slot) && slot.field_7871 == AutoCrystal.mc.field_1724.method_31548() && slot.method_34266() >= 0 && slot.method_34266() < 36;
    }

    private class_1268 equip(class_1792 item) {
        if (AutoCrystal.mc.field_1724.method_6079().method_31574(item)) {
            return class_1268.field_5810;
        }
        return this.equipMain(stack -> stack.method_31574(item)) ? class_1268.field_5808 : null;
    }

    private boolean equipMain(Predicate<class_1799> predicate) {
        InventoryExchange exchange;
        if (PlayerInventoryComponent.isInventoryActionBusy()) {
            return false;
        }
        if (predicate.test(AutoCrystal.mc.field_1724.method_6047())) {
            this.selectActionSlot(AutoCrystal.mc.field_1724.method_31548().method_67532());
            return true;
        }
        if (this.inventorySwap != null) {
            this.restoreEquipment();
            return false;
        }
        class_1735 slot = PlayerInventoryUtil.getSlot(candidate -> this.usableInventorySlot((class_1735)candidate) && candidate.method_34266() < 9 && predicate.test(candidate.method_7677()));
        if (slot != null) {
            this.selectActionSlot(slot.method_34266());
            return this.silentSlotSwitch.isValue() || predicate.test(AutoCrystal.mc.field_1724.method_6047());
        }
        if (slot == null) {
            slot = PlayerInventoryUtil.getSlot(candidate -> this.usableInventorySlot((class_1735)candidate) && candidate.method_34266() >= 9 && candidate.method_34266() < 36 && predicate.test(candidate.method_7677()));
        }
        if (slot == null) {
            return false;
        }
        this.inventorySwap = exchange = new InventoryExchange(slot, AutoCrystal.mc.field_1724.method_31548().method_67532(), false, AutoCrystal.mc.field_1687, (class_1657)AutoCrystal.mc.field_1724);
        Runnable swap = () -> {
            if (!exchange.cancelled && AutoCrystal.mc.field_1687 == exchange.world && AutoCrystal.mc.field_1724 == exchange.player) {
                PlayerInventoryUtil.swap(exchange.slot, exchange.hotbar, true);
                exchange.applied = true;
            }
            exchange.pending = false;
        };
        if (exchange.screenWalk) {
            ScreenWalkInventorySync.run(swap);
        } else {
            PlayerInventoryComponent.runInventoryAction(swap);
        }
        return false;
    }

    private void selectActionSlot(int slot) {
        if (this.originalSlot == -1) {
            this.originalSlot = AutoCrystal.mc.field_1724.method_31548().method_67532();
        }
        this.automaticSlot = slot;
        if (!this.silentSlotSwitch.isValue()) {
            AutoCrystal.mc.field_1724.method_31548().method_61496(slot);
        }
        if (this.serverSlot != slot) {
            AutoCrystal.mc.field_1724.field_3944.method_52787((class_2596)new class_2868(slot));
        }
    }

    private boolean weapon(class_1799 stack) {
        return stack.method_31573(class_3489.field_42611) || stack.method_31573(class_3489.field_42612) || stack.method_31573(class_3489.field_42614) || stack.method_31573(class_3489.field_42615);
    }

    private void restoreEquipment() {
        if (this.inventorySwap != null) {
            InventoryExchange exchange = this.inventorySwap;
            exchange.cancelled = true;
            this.inventorySwap = null;
            if (exchange.applied) {
                exchange.pending = true;
                this.returningEquipment = exchange;
                Runnable restore = () -> {
                    if (AutoCrystal.mc.field_1687 == exchange.world && AutoCrystal.mc.field_1724 == exchange.player) {
                        PlayerInventoryUtil.swap(exchange.slot, exchange.hotbar, true);
                    }
                    exchange.pending = false;
                };
                if (exchange.screenWalk) {
                    ScreenWalkInventorySync.run(restore);
                } else {
                    PlayerInventoryComponent.runInventoryAction(restore);
                }
            }
        }
        if (this.originalSlot >= 0 && (this.silentSlotSwitch.isValue() || AutoCrystal.mc.field_1724.method_31548().method_67532() == this.automaticSlot)) {
            if (!this.silentSlotSwitch.isValue()) {
                AutoCrystal.mc.field_1724.method_31548().method_61496(this.originalSlot);
            }
            if (this.serverSlot != this.originalSlot) {
                AutoCrystal.mc.field_1724.field_3944.method_52787((class_2596)new class_2868(this.originalSlot));
            }
        }
        this.originalSlot = -1;
        this.automaticSlot = -1;
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        class_2338 pos;
        if (AutoCrystal.mc.field_1724 == null || AutoCrystal.mc.field_1687 == null || !this.isState()) {
            return;
        }
        pos = this.plan != null ? this.plan.pos() : (this.confirmation != null ? this.confirmation.pos() : this.lastRenderBase);
        if (pos == null) {
            return;
        }
        int color = -1259646382;
        if (this.plan == null && this.confirmation == null) {
            long elapsed = System.currentTimeMillis() - this.lastRenderTime;
            if (elapsed >= 500L) {
                return;
            }
            color = color & 0xFFFFFF | (int)((double)(color >>> 24) * (1.0 - (double)elapsed / 500.0)) << 24;
        }
        Render3DUtil.drawBox(new class_238(pos), color, 1.5f, true, true, false);
    }

    @EventHandler
    public void onDraw(DrawEvent event) {
        Plan displayed;
        Plan plan = displayed = this.plan != null ? this.plan : this.damagePreview;
        if (!this.showDamage.isValue() || AutoCrystal.mc.field_1724 == null || AutoCrystal.mc.field_1687 == null || displayed == null) {
            return;
        }
        class_243 screen = ProjectionUtil.worldSpaceToScreenSpace(displayed.pos().method_46558().method_1031(0.0, 1.35, 0.0));
        if (screen.field_1350 < 0.0 || screen.field_1350 > 1.0) {
            return;
        }
        class_332 context = event.getDrawContext();
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        FontRenderer font = Fonts.getSize(12, Fonts.Type.INTER_BOLD);
        String text = String.format(Locale.ROOT, "%.1f | %.1f", Float.valueOf(displayed.targetDamage()), Float.valueOf(displayed.selfDamage()));
        float width = font.getStringWidth(text);
        float x = (float)screen.field_1352;
        float y = (float)screen.field_1351;
        context.method_25294(Math.round(x - width * 0.5f - 4.0f), Math.round(y - 2.0f), Math.round(x + width * 0.5f + 4.0f), Math.round(y + 11.0f), -1207959552);
        font.drawCenteredString(matrix, text, x, y, ColorUtil.getClientColor());
    }

    private void reset() {
        this.comboTarget = null;
        this.comboUntil = -1;
        this.chainBase = null;
        this.chainTarget = -1;
        this.chainUntil = -1;
        this.combatCycle = 0;
        this.lastActionCycle = -1;
        this.lastEntityInteractionCycle = -1;
        this.serverSlot = -1;
        this.actionReserved = false;
        this.target = null;
        this.friends = List.of();
        this.facePlaceActive = false;
        this.plan = null;
        this.aimingPlan = null;
        this.aimingPlanUntil = -1;
        this.damagePreview = null;
        this.confirmation = null;
        this.inventorySwap = null;
        this.returningEquipment = null;
        this.originalSlot = -1;
        this.automaticSlot = -1;
        this.lastActionTick = -100;
        this.lastPlanTick = -100;
        this.restorePending = false;
        this.lastRenderBase = null;
        this.attacked.clear();
        this.killWatch.clear();
        this.rejected.clear();
        this.damage.clear();
    }

    public ValueSetting getTargetRange() {
        return this.targetRange;
    }

    public BooleanSetting getAiming() {
        return this.aiming;
    }

    public SelectSetting getTargetPriority() {
        return this.targetPriority;
    }

    public BooleanSetting getUseAnchors() {
        return this.useAnchors;
    }

    public BindSetting getAnchorKey() {
        return this.anchorKey;
    }

    public ValueSetting getAnchorsPerAction() {
        return this.anchorsPerAction;
    }

    public ValueSetting getAnchorCover() {
        return this.anchorCover;
    }

    public BooleanSetting getThroughWalls() {
        return this.throughWalls;
    }

    public BooleanSetting getSilentSlotSwitch() {
        return this.silentSlotSwitch;
    }

    public ValueSetting getMinDamage() {
        return this.minDamage;
    }

    public ValueSetting getMaxSelfDamage() {
        return this.maxSelfDamage;
    }

    public BooleanSetting getCoverWithBlock() {
        return this.coverWithBlock;
    }

    public MultiSelectSetting getPauseConditions() {
        return this.pauseConditions;
    }

    public BooleanSetting getHoleRetreat() {
        return this.holeRetreat;
    }

    public BooleanSetting getShowDamage() {
        return this.showDamage;
    }

    public ValueSetting getActionDelay() {
        return this.actionDelay;
    }

    public RotationConfig getRotationConfig() {
        return this.rotationConfig;
    }

    public AutoCrystalDamage getDamage() {
        return this.damage;
    }

    public Map<Integer, Integer> getAttacked() {
        return this.attacked;
    }

    public Map<Integer, Integer> getKillWatch() {
        return this.killWatch;
    }

    public Map<class_2338, Integer> getRejected() {
        return this.rejected;
    }

    public class_638 getWorld() {
        return this.world;
    }

    public class_1657 getTarget() {
        return this.target;
    }

    public List<class_1657> getFriends() {
        return this.friends;
    }

    public boolean isFacePlaceActive() {
        return this.facePlaceActive;
    }

    public Plan getPlan() {
        return this.plan;
    }

    public Plan getAimingPlan() {
        return this.aimingPlan;
    }

    public int getAimingPlanUntil() {
        return this.aimingPlanUntil;
    }

    public Plan getDamagePreview() {
        return this.damagePreview;
    }

    public Confirmation getConfirmation() {
        return this.confirmation;
    }

    public InventoryExchange getInventorySwap() {
        return this.inventorySwap;
    }

    public InventoryExchange getReturningEquipment() {
        return this.returningEquipment;
    }

    public int getOriginalSlot() {
        return this.originalSlot;
    }

    public int getAutomaticSlot() {
        return this.automaticSlot;
    }

    public int getLastActionTick() {
        return this.lastActionTick;
    }

    public int getLastPlanTick() {
        return this.lastPlanTick;
    }

    public boolean isRestorePending() {
        return this.restorePending;
    }

    public class_2338 getLastRenderBase() {
        return this.lastRenderBase;
    }

    public class_2338 getRetreatHole() {
        return this.retreatHole;
    }

    public long getLastRenderTime() {
        return this.lastRenderTime;
    }

    public class_1657 getComboTarget() {
        return this.comboTarget;
    }

    public int getComboUntil() {
        return this.comboUntil;
    }

    public class_2338 getChainBase() {
        return this.chainBase;
    }

    public int getChainTarget() {
        return this.chainTarget;
    }

    public int getChainUntil() {
        return this.chainUntil;
    }

    public int getCombatCycle() {
        return this.combatCycle;
    }

    public int getLastActionCycle() {
        return this.lastActionCycle;
    }

    public int getLastEntityInteractionCycle() {
        return this.lastEntityInteractionCycle;
    }

    public int getServerSlot() {
        return this.serverSlot;
    }

    public boolean isActionReserved() {
        return this.actionReserved;
    }

    private record Plan(Action action, class_2338 pos, int crystal, int target, class_3965 hit, float targetDamage, float selfDamage, double score) {
    }

    private static final class InventoryExchange {
        private final class_1735 slot;
        private final int hotbar;
        private final boolean screenWalk;
        private final class_638 world;
        private final class_1657 player;
        private boolean pending = true;
        private boolean applied;
        private boolean cancelled;

        private InventoryExchange(class_1735 slot, int hotbar, boolean screenWalk, class_638 world, class_1657 player) {
            this.slot = slot;
            this.hotbar = hotbar;
            this.screenWalk = screenWalk;
            this.world = world;
            this.player = player;
        }
    }

    private record Confirmation(Action action, class_2338 pos, int charges, int deadline) {
    }

    private static enum Action {
        CRYSTAL_BREAK(false, 1),
        CRYSTAL_PLACE(false, 2),
        OBSIDIAN_PLACE(false, 3),
        ANCHOR_PLACE(true, 3),
        ANCHOR_CHARGE(true, 2),
        ANCHOR_DETONATE(true, 1);

        private final boolean anchor;
        private final int steps;

        private Action(boolean anchor, int steps) {
            this.anchor = anchor;
            this.steps = steps;
        }
    }

    private record Seed(Action action, class_2338 pos, int crystal, double roughScore) {
    }
}

