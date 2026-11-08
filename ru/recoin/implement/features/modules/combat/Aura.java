/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_1657
 *  net.minecraft.class_1735
 *  net.minecraft.class_1743
 *  net.minecraft.class_1792
 *  net.minecraft.class_1835
 *  net.minecraft.class_1937
 *  net.minecraft.class_238
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_243
 *  net.minecraft.class_2561
 *  net.minecraft.class_2596
 *  net.minecraft.class_2663
 *  net.minecraft.class_332
 *  net.minecraft.class_3489
 *  net.minecraft.class_3532
 *  net.minecraft.class_3545
 *  net.minecraft.class_3959$class_3960
 *  net.minecraft.class_465
 *  net.minecraft.class_490
 *  net.minecraft.class_5321
 *  net.minecraft.class_9362
 */
package ru.recoin.implement.features.modules.combat;

import java.util.ArrayList;
import java.util.Objects;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1735;
import net.minecraft.class_1743;
import net.minecraft.class_1792;
import net.minecraft.class_1835;
import net.minecraft.class_1937;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2596;
import net.minecraft.class_2663;
import net.minecraft.class_332;
import net.minecraft.class_3489;
import net.minecraft.class_3532;
import net.minecraft.class_3545;
import net.minecraft.class_3959;
import net.minecraft.class_465;
import net.minecraft.class_490;
import net.minecraft.class_5321;
import net.minecraft.class_9362;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BindSetting;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.GroupSetting;
import ru.recoin.api.feature.module.setting.implement.MultiSelectSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.api.repository.friend.FriendUtils;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.entity.EntityCompat;
import ru.recoin.common.util.entity.PlayerInventoryUtil;
import ru.recoin.common.util.entity.ScreenWalkInventorySync;
import ru.recoin.common.util.item.VortexTalismanMatcher;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.render.Render3DUtil;
import ru.recoin.common.util.task.TaskPriority;
import ru.recoin.core.Main;
import ru.recoin.implement.events.keyboard.KeyEvent;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.events.player.RotationUpdateEvent;
import ru.recoin.implement.events.render.DrawEvent;
import ru.recoin.implement.events.render.WorldRenderEvent;
import ru.recoin.implement.features.draggables.Notifications;
import ru.recoin.implement.features.modules.combat.MaceHelper;
import ru.recoin.implement.features.modules.combat.killaura.aim.LegitAimAssistEngine;
import ru.recoin.implement.features.modules.combat.killaura.attack.AttackHandler;
import ru.recoin.implement.features.modules.combat.killaura.attack.AttackPerpetrator;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.PointFinder;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RaytracingUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationConfig;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;
import ru.recoin.implement.features.modules.combat.killaura.rotation.ai.NeuroRotation;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.AiRotationSmoothMode;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.AngleSmoothMode;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.CustomSmoothMode;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.FunTimeSmoothMode;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.HvhSmoothMode;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.LinearSmoothMode;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.NewSlothSmoothMode;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.PolarSmoothMode;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.ReallyWorldSmoothMode;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.SmoothMode;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.SlothSmoothMode;
import ru.recoin.implement.features.modules.combat.killaura.rotation.shard.Shard21SmoothMode;
import ru.recoin.implement.features.modules.combat.killaura.rotation.shard.Shard2SmoothMode;
import ru.recoin.implement.features.modules.combat.killaura.rotation.shard.ShardAiSmoothMode;
import ru.recoin.implement.features.modules.combat.killaura.rotation.shard.SpacetimeSmoothMode;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.ApexTimeSmoothMode;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.SpookyTimeOldSmoothMode;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.SpookyTimeSmoothMode;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.TestSmoothMode;
import ru.recoin.implement.features.modules.combat.killaura.target.BacktrackPositionResolver;
import ru.recoin.implement.features.modules.combat.killaura.target.TargetBacktrackController;
import ru.recoin.implement.features.modules.combat.killaura.target.TargetSelector;
import ru.recoin.implement.features.modules.misc.ClickAction;
import ru.recoin.implement.features.modules.movement.ElytraTarget;
import ru.recoin.implement.features.modules.render.Hud;

public class Aura
extends Module {
    private final TargetSelector targetSelector = new TargetSelector();
    private final transient TargetBacktrackController targetBacktrack = new TargetBacktrackController();
    private final PointFinder pointFinder = new PointFinder();
    private final LegitAimAssistEngine legitAimAssist = new LegitAimAssistEngine();
    private class_1309 target;
    private class_1309 lastTarget;
    private String cachedSmoothModeName;
    private AngleSmoothMode cachedSmoothMode;
    private class_5321<class_1937> legitWorldKey;
    private final ValueSetting attackDistance = new ValueSetting("Attack Distance", "Maximum attack distance").setValue(3.0f).range(0.1f, 6.0f).step(0.1f).visible(() -> !this.isHistoricalBacktrackEnabled());
    private final BooleanSetting historicalBacktracks = new BooleanSetting("Backtrack", "Adaptive reach and target history").setValue(false);
    private final ValueSetting extraAimDistance = new ValueSetting("Extra Aim Distance", "Additional distance for target aiming").setValue(0.5f).range(0.0f, 3.0f).step(0.1f);
    private final MultiSelectSetting targetType = new MultiSelectSetting("Target Type", "Entity types; Naked Player includes players without armor").value("Players", "Mobs", "Animals", "Friends", "Naked Player").selected("Players");
    private final SelectSetting priority = new SelectSetting("Priority", "Target selection priority").value("Distance", "FOV", "Health", "Armor").selected("FOV");
    private final MultiSelectSetting attackSetting = new MultiSelectSetting("Attack Setting", "Allows you to customize the attack").value("Only Critical", "Crits Only Space", "Smart Crits", "Break Shield", "UnPress Shield", "No Attack When Eat", "No Attack In Containers", "Only Weapon", "Ignore The Walls").selected("Only Critical", "Smart Crits", "Break Shield", "No Attack When Eat", "No Attack In Containers");
    private final BooleanSetting onlyThroughPartialBlocks = new BooleanSetting("Only Through Partial Blocks", "Allows attacks only through non-full blocks while Ignore The Walls is enabled").setValue(false).visible(() -> this.attackSetting.isSelected("Ignore The Walls"));
    private final BooleanSetting randomizeHits = new BooleanSetting("\u0420\u0430\u043d\u0434\u043e\u043c\u0438\u0437\u0438\u0440\u043e\u0432\u0430\u0442\u044c \u0443\u0434\u0430\u0440\u044b", "\u0420\u0430\u043d\u0434\u043e\u043c\u0438\u0437\u0430\u0446\u0438\u044f \u0432\u044b\u0441\u043e\u0442\u044b \u043f\u0430\u0434\u0435\u043d\u0438\u044f \u0434\u043b\u044f \u043a\u0440\u0438\u0442\u043e\u0432").setValue(false);
    private final SelectSetting correctionType = new SelectSetting("Correction Type", "Selects the type of correction").value("Free", "Focused", "Target").selected("Focused");
    private final GroupSetting correctionGroup = new GroupSetting("Move correction", "Prevents detection by movement sensitive anti-cheats").settings(this.correctionType).setValue(true);
    private final SelectSetting pvpMode = new SelectSetting("PvP Mode", "Combat attack style").value("1.9", "1.8").selected("1.9");
    private final ValueSetting minCps = new ValueSetting("Min CPS", "Minimum CPS in 1.8 mode").setValue(10.0f).range(1.0f, 20.0f).step(1.0f).visible(() -> this.pvpMode.isSelected("1.8"));
    private final ValueSetting maxCps = new ValueSetting("Max CPS", "Maximum CPS in 1.8 mode").setValue(14.0f).range(1.0f, 20.0f).step(1.0f).visible(() -> this.pvpMode.isSelected("1.8"));
    private final SelectSetting aimMode = new SelectSetting("Rotation Type", "Allows you to select the rotation type").value("FunTime", "ReallyWorld", "Polar", "sloth", "New Sloth", "Smooth", "SpookyTime", "ApexTime", "HVH", "Custom", "AI", "Shard AI", "Shard-2", "Shard 2.1", "Spacetime", "Test").selected("FunTime");
    private final ValueSetting newSlothSpeed = new ValueSetting("New Sloth Speed", "New Sloth rotation speed multiplier").setValue(1.0f).range(0.1f, 2.0f).step(0.05f).visible(() -> this.aimMode.isSelected("New Sloth"));
    private final BooleanSetting aiDebugLog = new BooleanSetting("AI Debug Log", "Write AI inference telemetry to the profile log").setValue(false).visible(() -> this.aimMode.isSelected("AI"));
    private final BooleanSetting aiHumanMisses = new BooleanSetting("AI Human Misses", "Reproduce the miss rate learned from the recording").setValue(false).visible(() -> this.aimMode.isSelected("AI"));
    private final ValueSetting spookyFollowRange = new ValueSetting("Follow Range", "Target follow range").setValue(1.5f).range(0.0f, 3.0f).step(0.1f).visible(() -> this.aimMode.isSelected("SpookyTime Old"));
    private final ValueSetting legitDistance = this.legitValue("Distance", 4.0f, 1.0f, 6.0f, 0.1f);
    private final ValueSetting legitFov = this.legitValue("FOV", 90.0f, 20.0f, 360.0f, 1.0f);
    private final ValueSetting legitSpeed = this.legitValue("Speed", 4.0f, 0.5f, 10.0f, 0.1f);
    private final BooleanSetting legitDrawFov = this.legitBoolean("Draw FOV radius", false);
    private final ValueSetting legitAimHeight = this.legitValue("Aim Height", 0.85f, 0.0f, 1.0f, 0.05f);
    private final SelectSetting legitPriority = new SelectSetting("Priority", "AimAssist target selection").value("Distance", "Health").selected("Distance").visible(() -> this.aimMode.isSelected("Legit"));
    private final BooleanSetting legitWeaponOnly = this.legitBoolean("Weapon Only", true);
    private final BooleanSetting legitPlayersOnly = this.legitBoolean("Players Only", true);
    private final BooleanSetting legitNoAimInventory = this.legitBoolean("No Aim in Inventory", true);
    private final BooleanSetting legitHitInvisible = this.legitBoolean("Hit Invisible", false);
    private final BooleanSetting legitArmoredOnly = this.legitBoolean("Armored Only", false);
    private final BooleanSetting legitIgnoreNaked = this.legitBoolean("Ignore Naked", false).visible(() -> this.aimMode.isSelected("Legit") && !this.legitArmoredOnly.isValue());
    private final BooleanSetting legitOnlyX = this.legitBoolean("X Only", false);
    private final BooleanSetting legitMultipoint = this.legitBoolean("Multi-Point", true);
    private final BooleanSetting legitWallCheck = this.legitBoolean("Wall Check", true);
    private final BooleanSetting legitDisableWorldChange = this.legitBoolean("Disable on World Change", false);
    private final BooleanSetting legitMicroMovements = this.legitBoolean("Micro Movements", true);
    private final SelectSetting customEngine = this.customSelect("Engine", "FunTime", "FunTime", "ReallyWorld", "Spooky", "Holy", "Smooth", "sloth", "Custom");
    private final SelectSetting customPointMode = this.customSelect("Point Mode", "Multipoint", "Multipoint", "Center", "Eyes", "Closest");
    private final ValueSetting customYawSpeedMin = this.customValue("Yaw Speed Min", 35.0f, 0.0f, 200.0f, 1.0f);
    private final ValueSetting customYawSpeedMax = this.customValue("Yaw Speed Max", 55.0f, 0.0f, 200.0f, 1.0f);
    private final ValueSetting customPitchSpeedMin = this.customValue("Pitch Speed Min", 6.0f, 0.0f, 200.0f, 1.0f);
    private final ValueSetting customPitchSpeedMax = this.customValue("Pitch Speed Max", 12.0f, 0.0f, 200.0f, 1.0f);
    private final ValueSetting customAttackYawSpeed = this.customValue("Attack Yaw Speed", 65.0f, 0.0f, 240.0f, 1.0f);
    private final ValueSetting customAttackPitchSpeed = this.customValue("Attack Pitch Speed", 22.0f, 0.0f, 240.0f, 1.0f);
    private final ValueSetting customYawRandom = this.customValue("Yaw Random", 4.0f, 0.0f, 20.0f, 0.1f);
    private final ValueSetting customPitchRandom = this.customValue("Pitch Random", 3.0f, 0.0f, 20.0f, 0.1f);
    private final ValueSetting customOscillateX = this.customValue("Oscillate X", 0.2f, 0.0f, 1.0f, 0.01f);
    private final ValueSetting customOscillateY = this.customValue("Oscillate Y", 0.12f, 0.0f, 1.0f, 0.01f);
    private final ValueSetting customOscillateSpeed = this.customValue("Oscillate Speed", 1.0f, 0.2f, 3.0f, 0.1f);
    private final ValueSetting customSidePointOffset = this.customValue("Side Point Offset", 0.0f, 0.0f, 0.6f, 0.01f);
    private final ValueSetting customReturnSpeed = this.customValue("Return Speed", 30.0f, 5.0f, 120.0f, 1.0f);
    private final BooleanSetting customMoveHead = this.customBoolean("Move Head", false);
    private final SelectSetting customPointCycle = this.customSelect("Multipoint Mode", "Cycle", "Cycle", "Closest", "Random");
    private final ValueSetting customPointDwell = this.customValue("Point Dwell", 0.9f, 0.1f, 3.0f, 0.1f);
    private final SelectSetting customPitchFollow = this.customSelect("Pitch Follow", "Smooth", "Smooth", "Static", "Locked");
    private final ValueSetting customYawOffset = this.customValue("Yaw Offset", 0.0f, -30.0f, 30.0f, 0.1f);
    private final ValueSetting customPitchOffset = this.customValue("Pitch Offset", 0.0f, -30.0f, 30.0f, 0.1f);
    private final ValueSetting customPitchMin = this.customValue("Pitch Min", -90.0f, -90.0f, 0.0f, 1.0f);
    private final ValueSetting customPitchMax = this.customValue("Pitch Max", 90.0f, 0.0f, 90.0f, 1.0f);
    private final ValueSetting customHeadLead = this.customValue("Head Lead", 0.0f, 0.0f, 0.6f, 0.01f);
    private final ValueSetting customOverlayLerp = this.customValue("Overlay Lerp", 0.35f, 0.05f, 1.0f, 0.01f);
    private final ValueSetting customOverlayAimSpeed = this.customValue("Overlay Aim Speed", 1.0f, 0.2f, 3.0f, 0.1f);
    private final ValueSetting customPointSwitchSpeed = this.customValue("Point Switch Speed", 1.0f, 0.1f, 3.0f, 0.1f);
    private final BooleanSetting customLookAway = this.customBoolean("Look Away", false);
    private final ValueSetting customLookAwayAngle = this.customValue("Look Away Angle", 80.0f, 0.0f, 90.0f, 1.0f);
    private final ValueSetting customLookAwayInterval = this.customValue("Look Away Interval", 5.0f, 1.5f, 15.0f, 0.1f);
    private final BooleanSetting cameraRotation = new BooleanSetting("Camera Rotation", "Applies aura rotations to your camera").setValue(false);
    public final BooleanSetting maxDamage = new BooleanSetting("Max Damage", "Temporarily equips the damage item in the offhand for an attack").setValue(false);
    private final BindSetting vortexBind = new BindSetting("\u0412\u0438\u0445\u0440\u044c", "\u0411\u0435\u0440\u0435\u0442 \u0442\u0430\u043b\u0438\u0441\u043c\u0430\u043d \u0412\u0438\u0445\u0440\u044f \u0432\u043e \u0432\u0442\u043e\u0440\u0443\u044e \u0440\u0443\u043a\u0443");
    private final SelectSetting sprintResetMode = new SelectSetting("Sprint Reset", "Selects sprint reset behavior before attack").value("Off", "Legit", "Packet", "Fast").selected("Legit");
    public final BooleanSetting otvodka = new BooleanSetting("Aim Reset", "\u041f\u043b\u0430\u0432\u043d\u044b\u0439 \u043e\u0442\u0432\u043e\u0434 \u043f\u0440\u0438\u0446\u0435\u043b\u0430 \u043f\u043e\u0441\u043b\u0435 \u043e\u043a\u043e\u043d\u0447\u0430\u043d\u0438\u044f \u0430\u0442\u0430\u043a\u0438").setValue(true);
    private transient class_1309 rotationHoldTarget;
    private transient long rotationHoldStartedAt;

    public static Aura getInstance() {
        return Instance.get(Aura.class);
    }

    public Aura() {
        super("Attack Aura", ModuleCategory.COMBAT);
        this.setup(this.targetType, this.priority, this.aimMode, this.newSlothSpeed, this.attackDistance, this.extraAimDistance, this.spookyFollowRange, this.pvpMode, this.minCps, this.maxCps, this.attackSetting, this.onlyThroughPartialBlocks, this.randomizeHits, this.historicalBacktracks, this.correctionGroup, this.aiDebugLog, this.aiHumanMisses, this.legitDistance, this.legitFov, this.legitDrawFov, this.legitSpeed, this.legitAimHeight, this.legitPriority, this.legitWeaponOnly, this.legitPlayersOnly, this.legitNoAimInventory, this.legitHitInvisible, this.legitArmoredOnly, this.legitIgnoreNaked, this.legitOnlyX, this.legitMultipoint, this.legitWallCheck, this.legitDisableWorldChange, this.legitMicroMovements, this.customEngine, this.customPointMode, this.customYawSpeedMin, this.customYawSpeedMax, this.customPitchSpeedMin, this.customPitchSpeedMax, this.customAttackYawSpeed, this.customAttackPitchSpeed, this.customYawRandom, this.customPitchRandom, this.customOscillateX, this.customOscillateY, this.customOscillateSpeed, this.customSidePointOffset, this.customReturnSpeed, this.customMoveHead, this.customPointCycle, this.customPointDwell, this.customPitchFollow, this.customYawOffset, this.customPitchOffset, this.customPitchMin, this.customPitchMax, this.customHeadLead, this.customOverlayLerp, this.customOverlayAimSpeed, this.customPointSwitchSpeed, this.customLookAway, this.customLookAwayAngle, this.customLookAwayInterval, this.cameraRotation, this.maxDamage, this.vortexBind, this.sprintResetMode, this.otvodka);
    }

    private ValueSetting customValue(String name, float value, float min, float max, float step) {
        return new ValueSetting(name, "Custom rotation setting").setValue(value).range(min, max).step(step).visible(() -> this.aimMode.isSelected("Custom"));
    }

    private SelectSetting customSelect(String name, String selected, String ... values2) {
        return new SelectSetting(name, "Custom rotation setting").value(values2).selected(selected).visible(() -> this.aimMode.isSelected("Custom"));
    }

    private BooleanSetting customBoolean(String name, boolean value) {
        return new BooleanSetting(name, "Custom rotation setting").setValue(value).visible(() -> this.aimMode.isSelected("Custom"));
    }

    @Override
    public void activate() {
        super.activate();
        this.targetBacktrack.resetSession();
        this.rotationHoldTarget = null;
        this.rotationHoldStartedAt = 0L;
        this.target = null;
        this.lastTarget = null;
        this.targetSelector.resetFovLock();
    }

    @Override
    public void deactivate() {
        this.targetBacktrack.resetSession();
        this.rotationHoldTarget = null;
        this.rotationHoldStartedAt = 0L;
        if (!this.otvodka.isValue()) {
            if (this.target != null || RotationController.INSTANCE.getRotation() != null && RotationController.INSTANCE.getCurrentAngle() != null) {
                this.teleportCameraToLastHit();
            }
            RotationController.INSTANCE.clear();
            RotationController.INSTANCE.setRotation(null);
        }
        this.legitAimAssist.reset();
        this.legitWorldKey = null;
        this.targetSelector.releaseTarget();
        this.target = null;
        this.lastTarget = null;
        Main.getInstance().getAttackPerpetrator().getAttackHandler().clearPacketSprintReset();
        Main.getInstance().getAttackPerpetrator().getAttackHandler().clearFastSprintReset();
        super.deactivate();
    }

    public void onVortexKey(KeyEvent event) {
        if (Aura.mc.field_1724 == null || Aura.mc.field_1687 == null || Aura.mc.field_1761 == null || !event.isKeyDown(this.vortexBind.getKey(), event.screen() == null || event.screen() instanceof class_490)) {
            return;
        }
        ScreenWalkInventorySync.whenIdle(this::equipVortexTalisman);
    }

    private void equipVortexTalisman() {
        if (Aura.mc.field_1724 == null || Aura.mc.field_1687 == null || Aura.mc.field_1761 == null || Aura.mc.field_1755 != null && !(Aura.mc.field_1755 instanceof class_490) || Aura.mc.field_1724.field_7512 != Aura.mc.field_1724.field_7498 || !Aura.mc.field_1724.field_7512.method_34255().method_7960() || VortexTalismanMatcher.matches(Aura.mc.field_1724.method_6079())) {
            return;
        }
        class_1735 slot = PlayerInventoryUtil.getSlot(candidate -> candidate.field_7871 == Aura.mc.field_1724.method_31548() && candidate.method_34266() >= 0 && candidate.method_34266() < 36 && VortexTalismanMatcher.matches(candidate.method_7677()));
        if (slot != null) {
            ScreenWalkInventorySync.swapHand(slot, class_1268.field_5810, true);
        }
    }

    @EventHandler
    public void onRender2D(DrawEvent e) {
        int height;
        int width;
        float radius;
        class_332 context = e.getDrawContext();
        if (this.aimMode.isSelected("Legit") && this.legitDrawFov.isValue() && Aura.mc.field_1724 != null && Aura.mc.field_1755 == null && (radius = this.getLegitFovRadius(width = mc.method_22683().method_4486(), height = mc.method_22683().method_4502())) >= 1.0f) {
            int color = this.target != null ? -866779307 : -855638017;
            this.draw2DCircle(context, (float)width / 2.0f, (float)height / 2.0f, radius, color, 1.5f);
        }
    }

    private float getLegitFovRadius(int width, int height) {
        double gameFov;
        float maximum = Math.max(0.0f, (float)Math.min(width, height) * 0.5f - 2.0f);
        if (maximum <= 0.0f) {
            return 0.0f;
        }
        double aimFov = this.legitFov.getValue();
        if (aimFov >= (gameFov = class_3532.method_15350((double)((Integer)Aura.mc.field_1690.method_41808().method_41753()).intValue(), (double)30.0, (double)170.0))) {
            return maximum;
        }
        double aimHalf = class_3532.method_15350((double)(aimFov * 0.5), (double)0.1, (double)89.9);
        double gameHalf = class_3532.method_15350((double)(gameFov * 0.5), (double)1.0, (double)89.9);
        double radius = Math.tan(Math.toRadians(aimHalf)) / Math.tan(Math.toRadians(gameHalf)) * ((double)Math.min(width, height) * 0.5);
        return (float)class_3532.method_15350((double)radius, (double)0.0, (double)maximum);
    }

    private void draw2DCircle(class_332 context, float x, float y, float radius, int color) {
        this.draw2DCircle(context, x, y, radius, color, 1.0f);
    }

    private void draw2DCircle(class_332 context, float x, float y, float radius, int color, float thickness) {
        float half = radius + thickness / 2.0f;
        arc.render(ShapeProperties.create(MatrixStackCompat.from(context.method_51448()), x - half, y - half, half * 2.0f, half * 2.0f).thickness(thickness).start(0.0f).end(360.0f).color(color).build());
    }

    @EventHandler
    public void onPacket(PacketEvent e) {
        class_1297 entity;
        class_2663 status;
        this.targetBacktrack.onPacket(e);
        class_2596<?> class_25962 = e.getPacket();
        if (class_25962 instanceof class_2663 && (status = (class_2663)class_25962).method_11470() == 30 && (entity = status.method_11469((class_1937)Aura.mc.field_1687)) != null && entity.equals((Object)this.target) && Hud.getInstance().notificationSettings.isSelected("Break Shield")) {
            Notifications.getInstance().addList((class_2561)class_2561.method_43470((String)"\u0421\u043b\u043e\u043c\u0430\u043b\u0438 \u0449\u0438\u0442 \u0438\u0433\u0440\u043e\u043a\u0443 - ").method_10852(entity.method_5476()), 3000L);
        }
    }

    @EventHandler
    public void onRotationUpdate(RotationUpdateEvent e) {
        if (Aura.mc.field_1724 == null || Aura.mc.field_1687 == null) {
            this.targetBacktrack.resetSession();
            this.targetSelector.releaseTarget();
            this.target = null;
            this.lastTarget = null;
            return;
        }
        ClickAction clickAction = ClickAction.getInstance();
        if (clickAction != null && clickAction.isSpookyWindChargeActive()) {
            return;
        }
        switch (e.getType()) {
            case 0: {
                if (this.aimMode.isSelected("Legit")) {
                    RotationController.INSTANCE.release(this);
                }
                if (!this.canAttackWithHeldItem()) {
                    this.targetBacktrack.reset();
                    boolean lostTarget = this.target != null;
                    this.targetSelector.releaseTarget();
                    this.target = null;
                    Main.getInstance().getAttackPerpetrator().getAttackHandler().clearPacketSprintReset();
                    if (lostTarget && !this.otvodka.isValue()) {
                        this.teleportCameraToLastHit();
                    }
                    return;
                }
                class_1309 previousTarget = this.target;
                this.target = this.updateTarget();
                if (this.isHistoricalBacktrackEnabled()) {
                    this.targetBacktrack.updateTarget(this.target, RotationController.INSTANCE.getRotation());
                } else {
                    this.targetBacktrack.reset();
                }
                if (this.target != previousTarget) {
                    this.rotationHoldTarget = null;
                    this.rotationHoldStartedAt = 0L;
                }
                if (this.target != null) {
                    AttackPerpetrator.AttackPerpetratorConfigurable config = this.getConfig();
                    this.rotateToTarget(config);
                    this.lastTarget = this.target;
                    break;
                }
                Main.getInstance().getAttackPerpetrator().getAttackHandler().clearPacketSprintReset();
                if (previousTarget == null || this.otvodka.isValue()) break;
                this.teleportCameraToLastHit();
                break;
            }
            case 2: {
                Main.getInstance().getAttackPerpetrator().getAttackHandler().updateFastSprintReset(this.target != null && this.canAttackWithHeldItem() ? this.getConfig() : null);
                Render3DUtil.updateTargetEsp();
                if (this.target == null || !this.canAttackWithHeldItem() || this.aimMode.isSelected("AI") && !NeuroRotation.isRunning()) break;
                Main.getInstance().getAttackPerpetrator().performAttack(this.getConfig());
            }
        }
    }

    private boolean canAttackWithHeldItem() {
        if (!this.attackSetting.isSelected("Only Weapon")) {
            return true;
        }
        if (Aura.mc.field_1724 == null) {
            return false;
        }
        return Aura.mc.field_1724.method_6047().method_31573(class_3489.field_42611) || Aura.mc.field_1724.method_6047().method_7909() instanceof class_1743 || Aura.mc.field_1724.method_6047().method_7909() instanceof class_9362;
    }

    private class_1309 updateTarget() {
        if (this.aimMode.isSelected("Legit")) {
            return this.updateLegitTarget();
        }
        TargetSelector.EntityFilter filter = new TargetSelector.EntityFilter(this.targetType.getSelected());
        float currentFov = this.getCurrentFov();
        this.targetSelector.searchTargets(Aura.mc.field_1687.method_18112(), this.getAimDistance(), currentFov, this.attackSetting.isSelected("Ignore The Walls"), this.onlyThroughPartialBlocks.isValue(), this.priority.getSelected(), filter::isValid);
        this.targetSelector.validateTarget(filter::isValid);
        return this.targetSelector.getCurrentTarget();
    }

    private class_1309 updateLegitTarget() {
        this.targetSelector.releaseTarget();
        if (Aura.mc.field_1724 == null || Aura.mc.field_1687 == null) {
            return null;
        }
        if (this.legitWeaponOnly.isValue() && !this.isLegitWeaponHeld()) {
            return null;
        }
        if (this.legitNoAimInventory.isValue() && Aura.mc.field_1755 instanceof class_465) {
            return null;
        }
        class_243 eyes = Aura.mc.field_1724.method_33571();
        class_243 view = Aura.mc.field_1724.method_5828(1.0f);
        double maximumDistance = this.legitDistance.getValue();
        double maximumDistanceSquared = maximumDistance * maximumDistance;
        double maximumAngle = (double)this.legitFov.getValue() * 0.5;
        boolean healthPriority = this.legitPriority.isSelected("Health");
        class_1309 best = null;
        double bestScore = Double.MAX_VALUE;
        for (class_1297 entity : Aura.mc.field_1687.method_18112()) {
            double distanceSquared;
            class_1657 player;
            class_1309 living;
            if (!(entity instanceof class_1309) || (living = (class_1309)entity) == Aura.mc.field_1724 || !living.method_5805() || living.method_6032() <= 0.0f || this.legitPlayersOnly.isValue() && !(living instanceof class_1657) || living instanceof class_1657 && FriendUtils.isFriend((class_1297)(player = (class_1657)living)) || !this.legitHitInvisible.isValue() && living.method_5767() || (distanceSquared = Aura.mc.field_1724.method_5858((class_1297)living)) > maximumDistanceSquared || distanceSquared < 0.25) continue;
            boolean armored = this.hasArmor(living);
            if (this.legitArmoredOnly.isValue() && !armored || !this.legitArmoredOnly.isValue() && this.legitIgnoreNaked.isValue() && !armored) continue;
            class_243 aimPoint = living.method_73189().method_1031(0.0, (double)(living.method_17682() * this.legitAimHeight.getValue()), 0.0);
            if (this.legitWallCheck.isValue() && RaytracingUtil.raycast(eyes, aimPoint, class_3959.class_3960.field_17558).method_17783() == class_239.class_240.field_1332 || this.angleFromView(eyes, view, aimPoint) > maximumAngle) continue;
            double d = healthPriority ? (double)living.method_6032() : distanceSquared;
            double score = d;
            if (!(score < bestScore)) continue;
            bestScore = score;
            best = living;
        }
        return best;
    }

    private boolean hasArmor(class_1309 entity) {
        return EntityCompat.getArmorItems(entity).stream().anyMatch(stack -> !stack.method_7960());
    }

    private boolean isLegitWeaponHeld() {
        if (Aura.mc.field_1724 == null) {
            return false;
        }
        class_1792 item = Aura.mc.field_1724.method_6047().method_7909();
        return Aura.mc.field_1724.method_6047().method_31573(class_3489.field_42611) || item instanceof class_1743 || item instanceof class_1835 || item instanceof class_9362;
    }

    private double angleFromView(class_243 eyes, class_243 view, class_243 point) {
        class_243 direction = point.method_1020(eyes);
        double length = direction.method_1033();
        if (length < 0.001) {
            return 0.0;
        }
        direction = direction.method_1021(1.0 / length);
        return Math.toDegrees(Math.acos(class_3532.method_15350((double)view.method_1026(direction), (double)-1.0, (double)1.0)));
    }

    public void onRotationAttack(class_1309 attackedTarget) {
        AngleSmoothMode angleSmoothMode;
        if (!this.isState() || attackedTarget != this.target) {
            return;
        }
        if (this.aimMode.isSelected("SpookyTime") && (angleSmoothMode = this.getSmoothMode()) instanceof SpookyTimeSmoothMode) {
            SpookyTimeSmoothMode mode = (SpookyTimeSmoothMode)angleSmoothMode;
            mode.onAttack(RotationController.INSTANCE.getRotation());
            return;
        }
        if (this.aimMode.isSelected("ApexTime") && (angleSmoothMode = this.getSmoothMode()) instanceof ApexTimeSmoothMode) {
            ApexTimeSmoothMode mode = (ApexTimeSmoothMode)angleSmoothMode;
            mode.onAttack(RotationController.INSTANCE.getRotation());
            return;
        }
        if (!this.aimMode.isSelected("Polar") && !this.aimMode.isSelected("sloth")) {
            return;
        }
        this.rotationHoldTarget = attackedTarget;
        this.rotationHoldStartedAt = System.nanoTime();
    }

    private boolean isRotationHoldActive(long durationMs) {
        return this.target != null && this.target == this.rotationHoldTarget && this.rotationHoldStartedAt != 0L && System.nanoTime() - this.rotationHoldStartedAt < durationMs * 1000000L;
    }

    private void rotateToTarget(AttackPerpetrator.AttackPerpetratorConfigurable config) {
        AttackHandler attackHandler = Main.getInstance().getAttackPerpetrator().getAttackHandler();
        RotationController controller = RotationController.INSTANCE;
        if (MaceHelper.getInstance().getTargetDistanceBonus() > 0.0f && !this.pointFinder.hasValidPoint(this.target, this.getRotationAimDistance(), this.attackSetting.isSelected("Ignore The Walls"), this.onlyThroughPartialBlocks.isValue())) {
            controller.release(this);
            return;
        }
        class_243 rotationPoint = config.getAngle().toVector();
        Angle.VecRotation rotation = new Angle.VecRotation(config.getAngle(), rotationPoint);
        RotationConfig rotationConfig = this.getRotationConfig();
        switch (this.aimMode.getSelected()) {
            case "Legit": {
                controller.release(this);
                break;
            }
            case "sloth": {
                if ((!attackHandler.isWithinAttackRange(config) || !attackHandler.canAttack(config, 1)) && !this.isRotationHoldActive(100L)) break;
                controller.rotateTo(rotation, this.target, 1, rotationConfig, TaskPriority.HIGH_IMPORTANCE_1, this);
                break;
            }
            case "HVH": {
                controller.rotateTo(rotation, this.target, 1, rotationConfig, TaskPriority.HIGH_IMPORTANCE_1, this);
                break;
            }
            case "Polar": {
                if (!attackHandler.isWithinAttackRange(config) || !attackHandler.canAttack(config, 5)) break;
                controller.rotateTo(rotation, this.target, 20, rotationConfig, TaskPriority.HIGH_IMPORTANCE_1, this);
                break;
            }
            case "Custom": 
            case "FunTime": 
            case "ReallyWorld": 
            case "Smooth": 
            case "SpookyTime": 
            case "ApexTime": 
            case "SpookyTime Old": 
            case "Shard AI": 
            case "Shard 2.1": 
            case "Spacetime": 
            case "AI": {
                controller.rotateTo(rotation, this.target, 1, rotationConfig, TaskPriority.HIGH_IMPORTANCE_1, this);
                break;
            }
            default: {
                controller.rotateTo(rotation, this.target, 1, rotationConfig, TaskPriority.HIGH_IMPORTANCE_1, this);
            }
        }
    }

    public AttackPerpetrator.AttackPerpetratorConfigurable getConfig() {
        class_3545<class_243, class_238> computedPoint;
        class_238 targetBox = BacktrackPositionResolver.boundingBox((class_1297)this.target);
        class_243 pointOffset = this.getSmoothMode().randomValue();
        float aimDistance = this.getRotationAimDistance();
        if (this.aimMode.isSelected("SpookyTime Old")) {
            pointOffset = new class_243(0.0, pointOffset.field_1351, 0.0);
        }
        if (this.aimMode.isSelected("sloth")) {
            computedPoint = this.pointFinder.computeMultiPointVector(this.target, aimDistance, RotationController.INSTANCE.getRotation(), pointOffset, this.attackSetting.isSelected("Ignore The Walls"), this.onlyThroughPartialBlocks.isValue());
        } else if (this.aimMode.isSelected("HVH")) {
            computedPoint = new class_3545((Object)targetBox.method_1005(), (Object)targetBox);
        } else if (this.aimMode.isSelected("Custom")) {
            computedPoint = switch (this.customPointMode.getSelected()) {
                case "Closest" -> this.pointFinder.computeSnapHitVector(this.target, aimDistance, AngleUtil.cameraAngle(), this.attackSetting.isSelected("Ignore The Walls"), this.onlyThroughPartialBlocks.isValue());
                case "Multipoint" -> this.pointFinder.computeMultiPointVector(this.target, aimDistance, RotationController.INSTANCE.getRotation(), pointOffset, this.attackSetting.isSelected("Ignore The Walls"), this.onlyThroughPartialBlocks.isValue());
                default -> this.pointFinder.computeVector(this.target, aimDistance, RotationController.INSTANCE.getRotation(), pointOffset, this.attackSetting.isSelected("Ignore The Walls"), this.onlyThroughPartialBlocks.isValue());
            };
        } else if (this.aimMode.isSelected("SpookyTime") || this.aimMode.isSelected("ApexTime")) {
            double yExpand = class_3532.method_15350((double)(this.target.method_23320() - this.target.method_23318()), (double)0.0, (double)this.target.method_17682());
            class_243 eyePos = BacktrackPositionResolver.position((class_1297)this.target).method_1031(0.0, yExpand, 0.0);
            computedPoint = new class_3545((Object)eyePos, (Object)targetBox);
        } else {
            computedPoint = this.aimMode.isSelected("AI") && NeuroRotation.isRunning() ? new class_3545((Object)targetBox.method_1005(), (Object)targetBox) : this.pointFinder.computeVector(this.target, aimDistance, RotationController.INSTANCE.getRotation(), pointOffset, this.attackSetting.isSelected("Ignore The Walls"), this.onlyThroughPartialBlocks.isValue());
        }
        class_243 point = (class_243)computedPoint.method_15442();
        if (this.target != null) {
            ElytraTarget elytraTarget = ElytraTarget.getInstance();
            if (Aura.mc.field_1724 != null && Aura.mc.field_1724.method_6128() && elytraTarget != null && elytraTarget.isState()) {
                point = point.method_1019(this.target.method_18798().method_1021((double)elytraTarget.getPredictionTicks()));
            }
            if (this.aimMode.isSelected("Custom") && this.customHeadLead.getValue() > 0.001f) {
                point = point.method_1019(this.target.method_18798().method_1021((double)(this.customHeadLead.getValue() * 3.0f)));
            }
        }
        class_238 box = (class_238)computedPoint.method_15441();
        Angle angle = AngleUtil.fromVec3d(point.method_1020(Objects.requireNonNull(Aura.mc.field_1724).method_33571()));
        return this.createAttackConfig(angle, box);
    }

    private AttackPerpetrator.AttackPerpetratorConfigurable createAttackConfig(Angle angle, class_238 box) {
        ArrayList<String> options = new ArrayList<String>(this.attackSetting.getSelected());
        if (this.onlyThroughPartialBlocks.isValue() && this.attackSetting.isSelected("Ignore The Walls")) {
            options.add("Only Through Partial Blocks");
        }
        if (options.contains("Only Critical") && this.isInWater()) {
            options.remove("Only Critical");
        }
        float attackRange = this.getAttackDistance();
        return new AttackPerpetrator.AttackPerpetratorConfigurable(this.target, angle, attackRange, options, this.aimMode, box, this.sprintResetMode, this.randomizeHits.isValue(), false, this);
    }

    public Angle getApexTimeServerAngle() {
        if (this.target == null || Aura.mc.field_1724 == null) {
            return RotationController.INSTANCE.getRotation();
        }
        class_3545<class_243, class_238> point = this.pointFinder.computeVector(this.target, this.getRotationAimDistance(), RotationController.INSTANCE.getRotation(), class_243.field_1353, false, false);
        return AngleUtil.fromVec3d(((class_243)point.method_15442()).method_1020(Aura.mc.field_1724.method_33571()));
    }

    private boolean isInWater() {
        return Aura.mc.field_1724 != null && (Aura.mc.field_1724.method_5799() || Aura.mc.field_1724.method_5869() || Aura.mc.field_1724.method_5681());
    }

    private float getAimDistance() {
        if (this.isHistoricalBacktrackEnabled()) {
            return 8.0f;
        }
        return this.getRotationAimDistance() + this.getMaceAimDistanceBonus();
    }

    private float getRotationAimDistance() {
        if (this.isHistoricalBacktrackEnabled()) {
            return 4.2f;
        }
        ElytraTarget elytraTarget = ElytraTarget.getInstance();
        if (Aura.mc.field_1724 != null && Aura.mc.field_1724.method_6128() && elytraTarget != null && elytraTarget.isState()) {
            return elytraTarget.findRange.getValue();
        }
        if (this.aimMode.isSelected("SpookyTime Old")) {
            return this.getAttackDistance() + this.spookyFollowRange.getValue();
        }
        return this.getAttackDistance() + this.extraAimDistance.getValue();
    }

    private float getMaceAimDistanceBonus() {
        if (Aura.mc.field_1724 != null && Aura.mc.field_1724.method_6128() || this.aimMode.isSelected("SpookyTime Old")) {
            return 0.0f;
        }
        return MaceHelper.getInstance().getTargetDistanceBonus();
    }

    private double getFunTimeAimDistance() {
        if (this.isHistoricalBacktrackEnabled()) {
            return 4.2f;
        }
        double velocityExtension = Aura.mc.field_1724 == null ? 0.0 : Aura.mc.field_1724.method_18798().method_1033() * 3.0;
        return (double)(this.getAttackDistance() + this.extraAimDistance.getValue()) + velocityExtension;
    }

    public float getAttackDistance() {
        if (this.isHistoricalBacktrackEnabled()) {
            return this.targetBacktrack.attackReach((class_1297)this.target);
        }
        if (this.aimMode.isSelected("ApexTime")) {
            return this.attackDistance.getValue() + 1.0f;
        }
        return this.attackDistance.getValue();
    }

    public boolean isHistoricalBacktrackEnabled() {
        return this.isState() && this.historicalBacktracks.isValue();
    }

    @EventHandler
    public void onBacktrackRender(WorldRenderEvent event) {
        if (this.isHistoricalBacktrackEnabled() && this.targetBacktrack.isHistorical((class_1297)this.target)) {
            Render3DUtil.drawBox(this.targetBacktrack.targetBox((class_1297)this.target), ColorUtil.getClientColor(), 1.5f, true, true, false);
        }
    }

    public RotationConfig getRotationConfig() {
        return new RotationConfig(this.getSmoothMode(), this.correctionGroup.isValue(), this.correctionType.isSelected("Free"), this.correctionType.isSelected("Target"));
    }

    public boolean isTargetCorrectionEnabled() {
        return this.correctionGroup.isValue() && this.correctionType.isSelected("Target");
    }

    public AngleSmoothMode getSmoothMode() {
        String selected = this.aimMode.getSelected();
        String cacheKey = selected + (String)(this.aimMode.isSelected("Custom") ? ":" + this.customEngine.getSelected() : "");
        if (this.cachedSmoothMode != null && Objects.equals(this.cachedSmoothModeName, cacheKey)) {
            return this.cachedSmoothMode;
        }
        this.cachedSmoothModeName = cacheKey;
        this.cachedSmoothMode = switch (selected) {
            case "FunTime" -> new FunTimeSmoothMode();
            case "ReallyWorld" -> new ReallyWorldSmoothMode();
            case "Polar" -> new PolarSmoothMode();
            case "Legit" -> new LinearSmoothMode();
            case "sloth" -> new SlothSmoothMode();
            case "New Sloth" -> new NewSlothSmoothMode();
            case "Smooth" -> new SmoothMode();
            case "SpookyTime" -> new SpookyTimeSmoothMode();
            case "ApexTime" -> new ApexTimeSmoothMode();
            case "SpookyTime Old" -> new SpookyTimeOldSmoothMode();
            case "HVH" -> new HvhSmoothMode();
            case "AI" -> new AiRotationSmoothMode();
            case "Shard AI" -> new ShardAiSmoothMode();
            case "Shard-2" -> new Shard2SmoothMode();
            case "Shard 2.1" -> new Shard21SmoothMode();
            case "Spacetime" -> new SpacetimeSmoothMode();
            case "Test" -> new TestSmoothMode();
            case "Custom" -> {
                switch (this.customEngine.getSelected()) {
                    case "FunTime": 
                    case "Holy": {
                        yield new FunTimeSmoothMode();
                    }
                    case "ReallyWorld": 
                    case "Matrix": {
                        yield new ReallyWorldSmoothMode();
                    }
                    case "Spooky": {
                        yield new SpookyTimeOldSmoothMode();
                    }
                    case "Smooth": {
                        yield new SmoothMode();
                    }
                    case "sloth": {
                        yield new SlothSmoothMode();
                    }
                }
                yield new CustomSmoothMode();
            }
            default -> new SlothSmoothMode();
        };
        return this.cachedSmoothMode;
    }

    public boolean isCameraRotationEnabled() {
        return this.cameraRotation.isValue() || this.aimMode.isSelected("Custom") && this.customMoveHead.isValue();
    }

    public boolean isAiDebugLog() {
        return this.aiDebugLog.isValue();
    }

    public boolean isAiHumanMisses() {
        return this.aiHumanMisses.isValue();
    }

    public void selectAiRotation() {
        this.aimMode.setSelected("AI");
        this.cachedSmoothMode = null;
        this.cachedSmoothModeName = null;
    }

    public boolean isRenderCameraRotationActive() {
        return this.isState() && this.cameraRotation.isValue() && this.target != null && Aura.mc.field_1724 != null && Aura.mc.field_1687 != null;
    }

    public Angle getRenderCameraRotation(float tickProgress) {
        RotationController controller = RotationController.INSTANCE;
        Angle current = controller.getAuraCameraAngle();
        Angle previous = controller.getPreviousAuraCameraAngle();
        if (current == null) {
            return AngleUtil.cameraAngle();
        }
        if (previous == null) {
            return current;
        }
        float progress = class_3532.method_15363((float)tickProgress, (float)0.0f, (float)1.0f);
        return new Angle(previous.getYaw() + class_3532.method_15393((float)(current.getYaw() - previous.getYaw())) * progress, class_3532.method_16439((float)progress, (float)previous.getPitch(), (float)current.getPitch()));
    }

    public void notifyRotationAttack() {
        AngleSmoothMode angleSmoothMode;
        if (this.aimMode.isSelected("AI")) {
            NeuroRotation.attacked();
        }
        if (this.aimMode.isSelected("SpookyTime Old") && (angleSmoothMode = this.getSmoothMode()) instanceof SpookyTimeOldSmoothMode) {
            SpookyTimeOldSmoothMode mode = (SpookyTimeOldSmoothMode)angleSmoothMode;
            mode.rememberAttack(RotationController.INSTANCE.getRotation());
        }
    }

    private ValueSetting legitValue(String name, float value, float min, float max, float step) {
        return new ValueSetting(name, "AimAssist setting ported from system.7z").setValue(value).range(min, max).step(step).visible(() -> this.aimMode.isSelected("Legit"));
    }

    private BooleanSetting legitBoolean(String name, boolean value) {
        return new BooleanSetting(name, "AimAssist setting ported from system.7z").setValue(value).visible(() -> this.aimMode.isSelected("Legit"));
    }

    private void teleportCameraToLastHit() {
        if (Aura.mc.field_1724 == null) {
            return;
        }
        ClickAction clickAction = ClickAction.getInstance();
        if (clickAction != null && clickAction.isSpookyWindChargeActive()) {
            return;
        }
        if (RotationController.INSTANCE.getLastRotationProvider() instanceof ClickAction) {
            return;
        }
        if (this.otvodka.isValue()) {
            return;
        }
        Angle angle = null;
        AngleSmoothMode angleSmoothMode = this.cachedSmoothMode;
        if (angleSmoothMode instanceof SpookyTimeOldSmoothMode) {
            SpookyTimeOldSmoothMode mode = (SpookyTimeOldSmoothMode)angleSmoothMode;
            angle = mode.consumeLastAttackAngle();
        }
        if (angle == null && RotationController.INSTANCE.getRotation() != null) {
            angle = RotationController.INSTANCE.getCurrentAngle();
        }
        if (angle == null) {
            return;
        }
        float yaw = angle.getYaw();
        float pitch = class_3532.method_15363((float)angle.getPitch(), (float)-90.0f, (float)90.0f);
        Aura.mc.field_1724.method_36456(yaw);
        Aura.mc.field_1724.method_36457(pitch);
        Aura.mc.field_1724.field_5982 = yaw;
        Aura.mc.field_1724.field_6004 = pitch;
        Aura.mc.field_1724.field_6241 = yaw;
        Aura.mc.field_1724.field_6259 = yaw;
        RotationController.INSTANCE.clear();
        RotationController.INSTANCE.setRotation(null);
    }

    public boolean isLegitRotationActive() {
        return this.isState() && this.aimMode.isSelected("Legit") && this.target != null && Aura.mc.field_1724 != null && Aura.mc.field_1687 != null;
    }

    public boolean isLegitSprintReset() {
        return this.sprintResetMode.isSelected("Legit");
    }

    public boolean isPacketSprintReset() {
        return this.sprintResetMode.isSelected("Packet");
    }

    public void updateLegitRotationFrame() {
        ClickAction clickAction = ClickAction.getInstance();
        if (clickAction != null && clickAction.isSpookyWindChargeActive()) {
            return;
        }
        if (!this.isState() || !this.aimMode.isSelected("Legit") || Aura.mc.field_1724 == null || Aura.mc.field_1687 == null) {
            this.legitAimAssist.reset();
            return;
        }
        class_5321 currentWorldKey = Aura.mc.field_1687.method_27983();
        if (this.legitWorldKey == null) {
            this.legitWorldKey = currentWorldKey;
        } else if (!Objects.equals(this.legitWorldKey, currentWorldKey)) {
            this.legitWorldKey = currentWorldKey;
            this.legitAimAssist.reset();
            this.target = null;
            if (this.legitDisableWorldChange.isValue()) {
                this.setState(false);
                return;
            }
        }
        this.target = this.updateLegitTarget();
        if (this.target == null) {
            this.legitAimAssist.reset();
            return;
        }
        this.lastTarget = this.target;
        this.legitAimAssist.update((class_1297)this.target, this.legitSpeed.getValue(), this.legitAimHeight.getValue(), this.legitOnlyX.isValue(), this.legitMultipoint.isValue(), this.legitMicroMovements.isValue());
    }

    public void updateSpookyRotationFrame() {
        if (!this.isState() || !this.aimMode.isSelected("SpookyTime Old") || this.target == null || Aura.mc.field_1724 == null || Aura.mc.field_1687 == null) {
            return;
        }
        float followRange = this.spookyFollowRange.getValue();
        if (Aura.mc.field_1724.method_5739((class_1297)this.target) > this.getAttackDistance() + followRange) {
            return;
        }
        if (this.attackSetting.isSelected("Ignore The Walls") ? this.onlyThroughPartialBlocks.isValue() && !RaytracingUtil.canSeeThroughPartialBlocks(Aura.mc.field_1724.method_33571(), this.target.method_33571()) : RaytracingUtil.raycast(Aura.mc.field_1724.method_33571(), this.target.method_33571(), class_3959.class_3960.field_17558).method_17783() == class_239.class_240.field_1332) {
            return;
        }
        AngleSmoothMode angleSmoothMode = this.getSmoothMode();
        if (angleSmoothMode instanceof SpookyTimeOldSmoothMode) {
            SpookyTimeOldSmoothMode mode = (SpookyTimeOldSmoothMode)angleSmoothMode;
            RotationController controller = RotationController.INSTANCE;
            Angle next = mode.updateFrame(controller.getRotation(), (class_1297)this.target, this.getAttackDistance(), followRange);
            controller.setRotation(next);
        }
    }

    public boolean isPolarCritReady() {
        return this.target != null && Main.getInstance().getAttackPerpetrator().getAttackHandler().canCrit(this.getConfig(), 0);
    }

    public float getCustomYawSpeedMin() {
        return this.customYawSpeedMin.getValue();
    }

    public float getCustomYawSpeedMax() {
        return Math.max(this.customYawSpeedMin.getValue(), this.customYawSpeedMax.getValue());
    }

    public float getCustomPitchSpeedMin() {
        return this.customPitchSpeedMin.getValue();
    }

    public float getCustomPitchSpeedMax() {
        return Math.max(this.customPitchSpeedMin.getValue(), this.customPitchSpeedMax.getValue());
    }

    public float getCustomAttackYawSpeed() {
        return this.customAttackYawSpeed.getValue();
    }

    public float getCustomAttackPitchSpeed() {
        return this.customAttackPitchSpeed.getValue();
    }

    public float getCustomYawRandom() {
        return this.customYawRandom.getValue();
    }

    public float getCustomPitchRandom() {
        return this.customPitchRandom.getValue();
    }

    public float getCustomOscillateX() {
        return this.customOscillateX.getValue();
    }

    public float getCustomOscillateY() {
        return this.customOscillateY.getValue();
    }

    public float getCustomOscillateSpeed() {
        return this.customOscillateSpeed.getValue();
    }

    public float getCustomSidePointOffset() {
        return this.customSidePointOffset.getValue();
    }

    public String getCustomPitchFollow() {
        return this.customPitchFollow.getSelected();
    }

    public float getCustomYawOffset() {
        return this.customYawOffset.getValue();
    }

    public float getCustomPitchOffset() {
        return this.customPitchOffset.getValue();
    }

    public float getCustomPitchMin() {
        return this.customPitchMin.getValue();
    }

    public float getCustomPitchMax() {
        return this.customPitchMax.getValue();
    }

    public boolean isCustomLookAway() {
        return this.customLookAway.isValue();
    }

    public float getCustomLookAwayAngle() {
        return this.customLookAwayAngle.getValue();
    }

    public float getCustomLookAwayInterval() {
        return this.customLookAwayInterval.getValue();
    }

    private float getCurrentFov() {
        return 360.0f;
    }

    public void setTarget(class_1309 target) {
        this.target = target;
    }

    public void setLastTarget(class_1309 lastTarget) {
        this.lastTarget = lastTarget;
    }

    public void setCachedSmoothModeName(String cachedSmoothModeName) {
        this.cachedSmoothModeName = cachedSmoothModeName;
    }

    public void setCachedSmoothMode(AngleSmoothMode cachedSmoothMode) {
        this.cachedSmoothMode = cachedSmoothMode;
    }

    public void setLegitWorldKey(class_5321<class_1937> legitWorldKey) {
        this.legitWorldKey = legitWorldKey;
    }

    public void setRotationHoldTarget(class_1309 rotationHoldTarget) {
        this.rotationHoldTarget = rotationHoldTarget;
    }

    public void setRotationHoldStartedAt(long rotationHoldStartedAt) {
        this.rotationHoldStartedAt = rotationHoldStartedAt;
    }

    public TargetSelector getTargetSelector() {
        return this.targetSelector;
    }

    public TargetBacktrackController getTargetBacktrack() {
        return this.targetBacktrack;
    }

    public PointFinder getPointFinder() {
        return this.pointFinder;
    }

    public LegitAimAssistEngine getLegitAimAssist() {
        return this.legitAimAssist;
    }

    public class_1309 getTarget() {
        return this.target;
    }

    public class_1309 getLastTarget() {
        return this.lastTarget;
    }

    public String getCachedSmoothModeName() {
        return this.cachedSmoothModeName;
    }

    public AngleSmoothMode getCachedSmoothMode() {
        return this.cachedSmoothMode;
    }

    public class_5321<class_1937> getLegitWorldKey() {
        return this.legitWorldKey;
    }

    public BooleanSetting getHistoricalBacktracks() {
        return this.historicalBacktracks;
    }

    public ValueSetting getExtraAimDistance() {
        return this.extraAimDistance;
    }

    public MultiSelectSetting getTargetType() {
        return this.targetType;
    }

    public SelectSetting getPriority() {
        return this.priority;
    }

    public MultiSelectSetting getAttackSetting() {
        return this.attackSetting;
    }

    public BooleanSetting getOnlyThroughPartialBlocks() {
        return this.onlyThroughPartialBlocks;
    }

    public BooleanSetting getRandomizeHits() {
        return this.randomizeHits;
    }

    public SelectSetting getCorrectionType() {
        return this.correctionType;
    }

    public GroupSetting getCorrectionGroup() {
        return this.correctionGroup;
    }

    public SelectSetting getPvpMode() {
        return this.pvpMode;
    }

    public ValueSetting getMinCps() {
        return this.minCps;
    }

    public ValueSetting getMaxCps() {
        return this.maxCps;
    }

    public SelectSetting getAimMode() {
        return this.aimMode;
    }

    public float getNewSlothSpeed() {
        return this.newSlothSpeed.getValue();
    }

    public BooleanSetting getAiDebugLog() {
        return this.aiDebugLog;
    }

    public BooleanSetting getAiHumanMisses() {
        return this.aiHumanMisses;
    }

    public ValueSetting getSpookyFollowRange() {
        return this.spookyFollowRange;
    }

    public ValueSetting getLegitDistance() {
        return this.legitDistance;
    }

    public ValueSetting getLegitFov() {
        return this.legitFov;
    }

    public ValueSetting getLegitSpeed() {
        return this.legitSpeed;
    }

    public BooleanSetting getLegitDrawFov() {
        return this.legitDrawFov;
    }

    public ValueSetting getLegitAimHeight() {
        return this.legitAimHeight;
    }

    public SelectSetting getLegitPriority() {
        return this.legitPriority;
    }

    public BooleanSetting getLegitWeaponOnly() {
        return this.legitWeaponOnly;
    }

    public BooleanSetting getLegitPlayersOnly() {
        return this.legitPlayersOnly;
    }

    public BooleanSetting getLegitNoAimInventory() {
        return this.legitNoAimInventory;
    }

    public BooleanSetting getLegitHitInvisible() {
        return this.legitHitInvisible;
    }

    public BooleanSetting getLegitArmoredOnly() {
        return this.legitArmoredOnly;
    }

    public BooleanSetting getLegitIgnoreNaked() {
        return this.legitIgnoreNaked;
    }

    public BooleanSetting getLegitOnlyX() {
        return this.legitOnlyX;
    }

    public BooleanSetting getLegitMultipoint() {
        return this.legitMultipoint;
    }

    public BooleanSetting getLegitWallCheck() {
        return this.legitWallCheck;
    }

    public BooleanSetting getLegitDisableWorldChange() {
        return this.legitDisableWorldChange;
    }

    public BooleanSetting getLegitMicroMovements() {
        return this.legitMicroMovements;
    }

    public SelectSetting getCustomEngine() {
        return this.customEngine;
    }

    public SelectSetting getCustomPointMode() {
        return this.customPointMode;
    }

    public ValueSetting getCustomReturnSpeed() {
        return this.customReturnSpeed;
    }

    public BooleanSetting getCustomMoveHead() {
        return this.customMoveHead;
    }

    public SelectSetting getCustomPointCycle() {
        return this.customPointCycle;
    }

    public ValueSetting getCustomPointDwell() {
        return this.customPointDwell;
    }

    public ValueSetting getCustomHeadLead() {
        return this.customHeadLead;
    }

    public ValueSetting getCustomOverlayLerp() {
        return this.customOverlayLerp;
    }

    public ValueSetting getCustomOverlayAimSpeed() {
        return this.customOverlayAimSpeed;
    }

    public ValueSetting getCustomPointSwitchSpeed() {
        return this.customPointSwitchSpeed;
    }

    public BooleanSetting getCustomLookAway() {
        return this.customLookAway;
    }

    public BooleanSetting getCameraRotation() {
        return this.cameraRotation;
    }

    public BooleanSetting getMaxDamage() {
        return this.maxDamage;
    }

    public BindSetting getVortexBind() {
        return this.vortexBind;
    }

    public SelectSetting getSprintResetMode() {
        return this.sprintResetMode;
    }

    public BooleanSetting getOtvodka() {
        return this.otvodka;
    }

    public class_1309 getRotationHoldTarget() {
        return this.rotationHoldTarget;
    }

    public long getRotationHoldStartedAt() {
        return this.rotationHoldStartedAt;
    }
}

