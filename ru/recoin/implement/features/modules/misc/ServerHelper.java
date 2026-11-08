/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_124
 *  net.minecraft.class_1268
 *  net.minecraft.class_1297
 *  net.minecraft.class_1304
 *  net.minecraft.class_1309
 *  net.minecraft.class_1542
 *  net.minecraft.class_1657
 *  net.minecraft.class_1661
 *  net.minecraft.class_1713
 *  net.minecraft.class_1735
 *  net.minecraft.class_1792
 *  net.minecraft.class_1796
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1890
 *  net.minecraft.class_1893
 *  net.minecraft.class_2246
 *  net.minecraft.class_2338
 *  net.minecraft.class_2338$class_2339
 *  net.minecraft.class_2374
 *  net.minecraft.class_238
 *  net.minecraft.class_2382
 *  net.minecraft.class_243
 *  net.minecraft.class_2561
 *  net.minecraft.class_2596
 *  net.minecraft.class_2637
 *  net.minecraft.class_2653
 *  net.minecraft.class_2656
 *  net.minecraft.class_2680
 *  net.minecraft.class_2767
 *  net.minecraft.class_2775
 *  net.minecraft.class_2824
 *  net.minecraft.class_2828$class_2831
 *  net.minecraft.class_2868
 *  net.minecraft.class_287
 *  net.minecraft.class_289
 *  net.minecraft.class_290
 *  net.minecraft.class_304
 *  net.minecraft.class_332
 *  net.minecraft.class_3414
 *  net.minecraft.class_3532
 *  net.minecraft.class_3675
 *  net.minecraft.class_3675$class_306
 *  net.minecraft.class_3944
 *  net.minecraft.class_3988
 *  net.minecraft.class_437
 *  net.minecraft.class_4587
 *  net.minecraft.class_4588
 *  net.minecraft.class_476
 *  net.minecraft.class_6880
 *  net.minecraft.class_7439
 *  net.minecraft.class_746
 *  net.minecraft.class_7924
 *  net.minecraft.class_9279
 *  net.minecraft.class_9288
 *  net.minecraft.class_9290
 *  net.minecraft.class_9331
 *  net.minecraft.class_9334
 *  org.apache.commons.lang3.StringUtils
 *  org.joml.Vector4f
 *  org.lwjgl.glfw.GLFW
 */
package ru.recoin.implement.features.modules.misc;

import com.mojang.blaze3d.vertex.VertexFormat;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;
import net.minecraft.class_124;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1309;
import net.minecraft.class_1542;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1792;
import net.minecraft.class_1796;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1890;
import net.minecraft.class_1893;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2374;
import net.minecraft.class_238;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2596;
import net.minecraft.class_2637;
import net.minecraft.class_2653;
import net.minecraft.class_2656;
import net.minecraft.class_2680;
import net.minecraft.class_2767;
import net.minecraft.class_2775;
import net.minecraft.class_2824;
import net.minecraft.class_2828;
import net.minecraft.class_2868;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_304;
import net.minecraft.class_332;
import net.minecraft.class_3414;
import net.minecraft.class_3532;
import net.minecraft.class_3675;
import net.minecraft.class_3944;
import net.minecraft.class_3988;
import net.minecraft.class_437;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_476;
import net.minecraft.class_6880;
import net.minecraft.class_7439;
import net.minecraft.class_746;
import net.minecraft.class_7924;
import net.minecraft.class_9279;
import net.minecraft.class_9288;
import net.minecraft.class_9290;
import net.minecraft.class_9331;
import net.minecraft.class_9334;
import org.apache.commons.lang3.StringUtils;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BindSetting;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.repository.friend.FriendUtils;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.api.system.sound.SoundManager;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.entity.EntityCompat;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.entity.PlayerInventoryComponent;
import ru.recoin.common.util.entity.PlayerInventoryUtil;
import ru.recoin.common.util.entity.ScreenWalkInventorySync;
import ru.recoin.common.util.entity.SimulatedPlayer;
import ru.recoin.common.util.item.PotionTrackerUtil;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.math.ProjectionUtil;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.other.StopWatch;
import ru.recoin.common.util.render.LegacyRenderSystem;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.common.util.render.Render3DUtil;
import ru.recoin.common.util.task.TaskPriority;
import ru.recoin.common.util.task.scripts.Script;
import ru.recoin.common.util.world.ServerUtil;
import ru.recoin.compat.render.BufferRenderer;
import ru.recoin.compat.render.GlStateManager;
import ru.recoin.compat.render.ShaderProgramKeys;
import ru.recoin.core.Main;
import ru.recoin.implement.events.container.SetScreenEvent;
import ru.recoin.implement.events.keyboard.KeyEvent;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.events.player.RotationUpdateEvent;
import ru.recoin.implement.events.render.DrawEvent;
import ru.recoin.implement.events.render.WorldRenderEvent;
import ru.recoin.implement.features.draggables.CoolDowns;
import ru.recoin.implement.features.modules.combat.PotionWheelController;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.PointFinder;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationConfig;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.LinearSmoothMode;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.WindChargeSmoothMode;
import ru.recoin.implement.features.modules.render.ProjectilePrediction;

public class ServerHelper
extends Module {
    private ActionState actionState = ActionState.IDLE;
    private int originalSlot = -1;
    private int targetSlotId = -1;
    private int sourceSlotId = -1;
    private long actionTimer = 0L;
    private boolean keysOverridden = false;
    private final SelectSetting serverSelect = new SelectSetting("Server", "\u0412\u044b\u0431\u043e\u0440 \u0441\u0435\u0440\u0432\u0435\u0440\u0430").value("HolyWorld", "FunTime", "ReallyWorld", "Space").selected("HolyWorld");
    private final SelectSetting donationThrowMode = new SelectSetting("\u0420\u0435\u0436\u0438\u043c \u0434\u043e\u043d\u0431\u0430\u0444\u0444\u043e\u0432", "\u0421\u043f\u043e\u0441\u043e\u0431 \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u044f \u0434\u043e\u043d \u0437\u0435\u043b\u0438\u0439").value("\u041a\u043e\u043b\u0435\u0441\u043e \u0432\u044b\u0431\u043e\u0440\u0430", "\u0411\u0438\u043d\u0434\u044b").selected("\u0411\u0438\u043d\u0434\u044b").visible(() -> this.isFunTimeMode());
    private final PotionWheelController funTimeWheel = new PotionWheelController(() -> this.isState() && this.isFunTimeMode() && this.donationThrowMode.isSelected("\u041a\u043e\u043b\u0435\u0441\u043e \u0432\u044b\u0431\u043e\u0440\u0430"));
    private final BooleanSetting autoLootSetting = new BooleanSetting("Auto Loot", "Steals loot from bots at the event").setValue(true).visible(() -> this.serverSelect.isSelected("HolyWorld"));
    private final BooleanSetting autoShulkerSetting = new BooleanSetting("Auto Shulker", "Automatically puts loot into shulker").setValue(true).visible(() -> this.serverSelect.isSelected("HolyWorld"));
    private final BooleanSetting autoRepairSetting = new BooleanSetting("Auto Repair", "Auto repairs armor with experience bubble at low durability").setValue(true).visible(() -> this.serverSelect.isSelected("HolyWorld"));
    private final BooleanSetting consumablesSetting = new BooleanSetting("Consumables Timer", "Displays the time until the consumable runs out").setValue(true).visible(() -> this.isFunTimeMode());
    private final BooleanSetting autoPointSetting = new BooleanSetting("Auto Point", "Displays information on the event").setValue(true).visible(() -> this.isFunTimeMode());
    private final BindSetting trapAndPlastBind = new BindSetting("Trap+Plast", "Place plast upward, then use trap").visible(() -> this.isFunTimeMode() || ServerUtil.isSpookyTime());
    private final transient RotationConfig trapPlastRotation = new RotationConfig(new WindChargeSmoothMode(), true, true);
    private transient Angle trapPlastAngle;
    private transient boolean trapPlastBusy;
    private transient int trapPlastGeneration;
    private transient int trapPlastStarted;
    private final BindSetting autoDebuffBind = new BindSetting("AutoDebuff", "\u0421\u043d\u043e\u0442\u0432\u043e\u0440\u043d\u043e\u0435, \u0411\u043e\u0436\u044c\u044f \u0430\u0443\u0440\u0430 \u0438 \u0417\u0435\u043b\u044c\u0435 \u0410\u0441\u0441\u0430\u0441\u0438\u043d\u0430").visible(() -> this.isFunTimeMode());
    private final BooleanSetting donationPotionsUnderSelf = new BooleanSetting("\u041a\u0438\u0434\u0430\u0442\u044c \u043f\u043e\u0434 \u0441\u0435\u0431\u044f \u0434\u043e\u043d\u043a\u0438", "\u0411\u0440\u043e\u0441\u0430\u0435\u0442 \u0432\u044b\u0431\u0440\u0430\u043d\u043d\u044b\u0435 \u0434\u043e\u043d\u0430\u0442\u043d\u044b\u0435 \u0437\u0435\u043b\u044c\u044f \u043f\u043e\u0434 \u0438\u0433\u0440\u043e\u043a\u0430").setValue(false).visible(() -> this.isFunTimeMode() && this.donationThrowMode.isSelected("\u0411\u0438\u043d\u0434\u044b"));
    private final BindSetting windChargeBind = new BindSetting("Wind Charge", "Wind Charge Key").visible(() -> this.isSpaceMode());
    private final SelectSetting windChargeDirection = new SelectSetting("Wind Charge Mode", "Wind Charge direction").value("\u041a\u0443\u0434\u0430 \u0446\u0435\u043b\u044e\u0441\u044c", "\u041f\u043e\u0434 \u0441\u0435\u0431\u044f").selected("\u041a\u0443\u0434\u0430 \u0446\u0435\u043b\u044e\u0441\u044c").visible(() -> this.isSpaceMode());
    private DebuffStage debuffStage = DebuffStage.IDLE;
    private int debuffTicks = 0;
    private transient class_1735 autoShulkerSource;
    private transient int autoShulkerHotbar = -1;
    private transient class_1799 autoShulkerDisplaced;
    private transient long autoShulkerStartedAt;
    private class_1735 pendingDonationPotionSlot;
    private Angle pendingDonationPotionAngle;
    private boolean pendingDonationPotionScheduled;
    private int pendingDonationPotionStartedAt;
    private final Map<class_2338, class_2680> blockStateMap = new HashMap<class_2338, class_2680>();
    private final List<List<class_2338>> pendingStructureDetections = new ArrayList<List<class_2338>>();
    private final List<PendingStructureSound> pendingStructureSounds = new ArrayList<PendingStructureSound>();
    private final List<ServerEvent> serverEvents = new ArrayList<ServerEvent>();
    private final List<Structure> structures = new ArrayList<Structure>();
    private final List<KeyBind> keyBindings = new ArrayList<KeyBind>();
    private final PointFinder pointFinder = new PointFinder();
    private final StopWatch itemsWatch = new StopWatch();
    private final StopWatch shulkerWatch = new StopWatch();
    private final StopWatch repairWatch = new StopWatch();
    private final Script script = new Script();
    private final Script script2 = new Script();
    private UUID entityUUID;
    private final Map<Integer, class_1792> stacks = new HashMap<Integer, class_1792>();
    private static final Pattern GPS_PATTERN = Pattern.compile("/gps\\s+(-?\\d+)\\s+(-?\\d+)\\s+(-?\\d+)");
    private static final Pattern EVENT_DELAY_PATTERN = Pattern.compile("(?iu)(?:(\\d+)\\s*(?:\u043c\u0438\u043d(?:\u0443\u0442[\u0430\u044b]?)?|m))?\\s*(?:(\\d+)\\s*(?:\u0441\u0435\u043a(?:\u0443\u043d\u0434[\u0430\u044b]?)?|\u0441|s))?");
    private long lastEventSoundAt;
    private long lastEventDelayRequestAt;
    private String lastEventSoundMessage = "";

    public static ServerHelper getInstance() {
        return Instance.get(ServerHelper.class);
    }

    public SelectSetting getServerSelect() {
        return this.serverSelect;
    }

    public boolean isSpaceMode() {
        return this.serverSelect.isSelected("Space");
    }

    public boolean isFunTimeMode() {
        return this.serverSelect.isSelected("FunTime") || this.serverSelect.isSelected("Space");
    }

    public SelectSetting getDonationThrowMode() {
        return this.donationThrowMode;
    }

    public PotionWheelController getFunTimeWheel() {
        return this.funTimeWheel;
    }

    public void initialize() {
        this.keyBindings.add(new KeyBind(class_1802.field_8450, new BindSetting("Anti Fly", "Anti Fly Key").visible(() -> this.serverSelect.isSelected("ReallyWorld")), 0.0f));
        this.keyBindings.add(new KeyBind(class_1802.field_8498, new BindSetting("Experience Scroll", "Experience Scroll Key").visible(() -> this.serverSelect.isSelected("ReallyWorld")), 0.0f));
        this.keyBindings.add(new KeyBind(class_1802.field_8662, new BindSetting("Explosive Trap", "Explosive Trap Key").visible(() -> this.serverSelect.isSelected("HolyWorld")), 5.0f));
        this.keyBindings.add(new KeyBind(class_1802.field_8882, new BindSetting("Default Trap", "Default Trap Key").visible(() -> this.serverSelect.isSelected("HolyWorld")), 0.0f));
        this.keyBindings.add(new KeyBind(class_1802.field_8137, new BindSetting("Stun", "Stun Key").visible(() -> this.serverSelect.isSelected("HolyWorld")), 30.0f));
        this.keyBindings.add(new KeyBind(class_1802.field_8814, new BindSetting("Explosive Thing", "Explosive Thing Key").visible(() -> this.serverSelect.isSelected("HolyWorld")), 0.0f));
        this.keyBindings.add(new KeyBind(class_1802.field_8543, new BindSetting("SnowBall", "SnowBall Key").visible(() -> this.isFunTimeMode() || this.serverSelect.isSelected("HolyWorld")), 0.0f));
        this.keyBindings.add(new KeyBind(class_1802.field_8614, new BindSetting("Gods Aura", "God's Aura Key").visible(() -> this.isFunTimeMode()), 0.0f));
        this.keyBindings.add(new KeyBind(class_1802.field_22021, new BindSetting("Trap", "Trap Key").visible(() -> this.isFunTimeMode()), 0.0f));
        this.keyBindings.add(new KeyBind(class_1802.field_8551, new BindSetting("Plast", "Plast Key").visible(() -> this.isFunTimeMode()), 0.0f));
        this.keyBindings.add(new KeyBind(class_1802.field_8479, new BindSetting("Clear Dust", "Clear Dust Key").visible(() -> this.isFunTimeMode()), 10.0f));
        this.keyBindings.add(new KeyBind(class_1802.field_8814, new BindSetting("Fire Charge", "Fire Charge Key").visible(() -> this.isFunTimeMode()), 10.0f));
        this.keyBindings.add(new KeyBind(class_1802.field_8449, new BindSetting("Disorientation", "Disorientation Key").visible(() -> this.isFunTimeMode()), 10.0f));
        this.addFunTimePotionBind("\u0425\u043b\u043e\u043f\u0443\u0448\u043a\u0430");
        this.addFunTimePotionBind("\u0421\u0432\u044f\u0442\u0430\u044f \u0432\u043e\u0434\u0430");
        this.addFunTimePotionBind("\u0417\u0435\u043b\u044c\u0435 \u0413\u043d\u0435\u0432\u0430");
        this.addFunTimePotionBind("\u0417\u0435\u043b\u044c\u0435 \u041f\u0430\u043b\u043b\u0430\u0434\u0438\u043d\u0430");
        this.addFunTimePotionBind("\u0417\u0435\u043b\u044c\u0435 \u0410\u0441\u0441\u0430\u0441\u0438\u043d\u0430");
        this.addFunTimePotionBind("\u0417\u0435\u043b\u044c\u0435 \u0420\u0430\u0434\u0438\u0430\u0446\u0438\u0438");
        this.addFunTimePotionBind("\u0421\u043d\u043e\u0442\u0432\u043e\u0440\u043d\u043e\u0435");
        this.setup(this.serverSelect, this.donationThrowMode);
        this.funTimeWheel.getWheelBind().visible(() -> this.isFunTimeMode() && this.donationThrowMode.isSelected("\u041a\u043e\u043b\u0435\u0441\u043e \u0432\u044b\u0431\u043e\u0440\u0430"));
        this.setup(this.funTimeWheel.getWheelBind(), this.funTimeWheel.getSavedLayout());
        this.keyBindings.forEach(bind -> this.setup(bind.setting));
        this.setup(this.autoLootSetting, this.consumablesSetting, this.autoPointSetting, this.autoShulkerSetting, this.autoRepairSetting, this.trapAndPlastBind, this.autoDebuffBind, this.donationPotionsUnderSelf, this.windChargeBind, this.windChargeDirection);
    }

    public ServerHelper() {
        super("ServerHelper", "Server Helper", ModuleCategory.MISC);
        this.initialize();
    }

    private void addFunTimePotionBind(String potionName) {
        this.keyBindings.add(new KeyBind(class_1802.field_8436, new BindSetting(potionName, potionName).visible(() -> this.isFunTimeMode() && this.donationThrowMode.isSelected("\u0411\u0438\u043d\u0434\u044b")), 0.0f, potionName));
    }

    @Override
    public void activate() {
        this.script2.cleanup();
        this.stacks.clear();
        this.structures.clear();
        this.blockStateMap.clear();
        this.pendingStructureDetections.clear();
        this.pendingStructureSounds.clear();
        this.actionState = ActionState.IDLE;
        this.keysOverridden = false;
        this.pendingDonationPotionSlot = null;
        this.pendingDonationPotionAngle = null;
        this.pendingDonationPotionScheduled = false;
    }

    @Override
    public void deactivate() {
        ++this.trapPlastGeneration;
        this.trapPlastBusy = false;
        this.trapPlastAngle = null;
        this.script.cleanup();
        RotationController.INSTANCE.release(this);
        this.finishAutoShulker();
        this.structures.clear();
        this.blockStateMap.clear();
        this.pendingStructureDetections.clear();
        this.pendingStructureSounds.clear();
        this.resetAutoDebuff();
        this.pendingDonationPotionSlot = null;
        this.pendingDonationPotionAngle = null;
        this.pendingDonationPotionScheduled = false;
        this.funTimeWheel.deactivate();
        super.deactivate();
    }

    @EventHandler
    public void onKey(KeyEvent e) {
        if (this.isFunTimeMode() && this.donationThrowMode.isSelected("\u041a\u043e\u043b\u0435\u0441\u043e \u0432\u044b\u0431\u043e\u0440\u0430")) {
            this.funTimeWheel.onKey(e);
        }
        if (ServerHelper.mc.field_1755 != null || this.actionState != ActionState.IDLE || this.trapPlastBusy) {
            return;
        }
        if (e.isKeyDown(this.autoDebuffBind.getKey()) && this.autoDebuffBind.isVisible()) {
            this.useAutoDebuff();
            return;
        }
        if (e.isKeyDown(this.trapAndPlastBind.getKey()) && this.trapAndPlastBind.isVisible()) {
            this.useTrapAndPlast();
            return;
        }
        if (e.isKeyDown(this.windChargeBind.getKey()) && this.windChargeBind.isVisible()) {
            this.useWindCharge();
            return;
        }
        this.keyBindings.stream().filter(bind -> e.isKeyDown(bind.setting.getKey()) && bind.setting.isVisible() && this.validDistance(bind.distance)).forEach(bind -> {
            if (!(bind.donationPotionName == null || this.donationPotionsUnderSelf.isValue() && this.isDonationPotionUnderSelf((KeyBind)bind) || !this.funTimeWheel.getPotionUseHandler().use(stack -> this.matchesDonationPotion(bind.donationPotionName, (class_1799)stack)))) {
                return;
            }
            class_1735 slot = this.findSlot((KeyBind)bind);
            if (slot == null) {
                return;
            }
            if (ServerHelper.mc.field_1724.method_7357().method_7904(slot.method_7677())) {
                return;
            }
            if (this.donationPotionsUnderSelf.isValue() && this.isDonationPotionUnderSelf((KeyBind)bind)) {
                this.queueDonationPotionUnderSelf(slot);
            } else {
                this.useSlotPacketDirect(slot, null);
            }
        });
    }

    private boolean isDonationPotionUnderSelf(KeyBind bind) {
        boolean bl;
        block10: {
            block9: {
                if (bind.donationPotionName == null) break block9;
                switch (bind.donationPotionName) {
                    case "\u0421\u0432\u044f\u0442\u0430\u044f \u0432\u043e\u0434\u0430": 
                    case "\u0417\u0435\u043b\u044c\u0435 \u0413\u043d\u0435\u0432\u0430": 
                    case "\u0417\u0435\u043b\u044c\u0435 \u041f\u0430\u043b\u043b\u0430\u0434\u0438\u043d\u0430": 
                    case "\u0417\u0435\u043b\u044c\u0435 \u0410\u0441\u0441\u0430\u0441\u0438\u043d\u0430": {
                        break;
                    }
                    default: {
                        break block9;
                    }
                }
                bl = true;
                break block10;
            }
            bl = false;
        }
        return bl;
    }

    private void queueDonationPotionUnderSelf(class_1735 slot) {
        if (this.pendingDonationPotionSlot != null) {
            return;
        }
        this.pendingDonationPotionSlot = slot;
        this.pendingDonationPotionAngle = this.getDonationPotionUnderSelfAngle();
        this.pendingDonationPotionScheduled = false;
        this.pendingDonationPotionStartedAt = ServerHelper.mc.field_1724.field_6012;
        RotationController.INSTANCE.rotateTo(this.pendingDonationPotionAngle, 1, new RotationConfig(true, true), TaskPriority.CRITICAL_FOR_USER_PROTECTION, this);
    }

    private Angle getDonationPotionUnderSelfAngle() {
        float time = (float)ServerHelper.mc.field_1724.field_6012 + mc.method_61966().method_60637(false);
        float noise = (float)(Math.sin(time * 0.31f) * 6.600001001477404 + Math.sin(time * 0.73f + 1.1f) * 0.3000001491338646);
        Angle camera = AngleUtil.cameraAngle();
        float pitch = class_3532.method_15363((float)(90.0f + noise / 2.0f), (float)-90.0f, (float)90.0f);
        return new Angle(camera.getYaw() + noise, pitch);
    }

    private void useDonationPotionUnderSelf(class_1735 slot, Angle angle) {
        Runnable useAction = () -> {
            RotationController.setForcedInteractAngle(angle);
            try {
                PlayerIntersectionUtil.interactItem(class_1268.field_5808, angle);
            }
            finally {
                RotationController.setForcedInteractAngle(null);
                this.clearPendingDonationPotion();
            }
        };
        if (PlayerInventoryUtil.isBundleBackedSlot(slot)) {
            ScreenWalkInventorySync.bundleUseFromHandRestore(slot, useAction);
        } else {
            ScreenWalkInventorySync.swapUseRestore(slot, useAction);
        }
    }

    private void useAutoDebuff() {
        boolean hasAssassin;
        if (ServerHelper.mc.field_1724 == null || ServerHelper.mc.field_1761 == null) {
            return;
        }
        if (this.debuffStage != DebuffStage.IDLE) {
            return;
        }
        class_1735 sleepingPotionSlot = this.findSleepingPotionSlot();
        class_1735 auraSlot = this.findAuraSlot();
        class_1735 assassinPotionSlot = this.findAssassinPotionSlot();
        class_1796 cooldowns = ServerHelper.mc.field_1724.method_7357();
        boolean hasSleeping = sleepingPotionSlot != null && !cooldowns.method_7904(sleepingPotionSlot.method_7677());
        boolean hasAura = auraSlot != null && !cooldowns.method_7904(auraSlot.method_7677());
        boolean bl = hasAssassin = assassinPotionSlot != null && !cooldowns.method_7904(assassinPotionSlot.method_7677());
        if (!(hasSleeping || hasAura || hasAssassin)) {
            return;
        }
        this.debuffTicks = 0;
        this.debuffStage = hasSleeping ? DebuffStage.SLEEPING : (hasAura ? DebuffStage.AURA : DebuffStage.ASSASSIN);
        this.handleAutoDebuffTick();
    }

    private void handleAutoDebuffTick() {
        if (ServerHelper.mc.field_1724 == null || ServerHelper.mc.field_1761 == null) {
            this.resetAutoDebuff();
            return;
        }
        if (PlayerInventoryComponent.isInventoryActionBusy()) {
            return;
        }
        if (this.debuffTicks > 0) {
            --this.debuffTicks;
            return;
        }
        class_1796 cooldowns = ServerHelper.mc.field_1724.method_7357();
        int delay = 2;
        switch (this.debuffStage.ordinal()) {
            case 1: {
                class_1735 slot = this.findSleepingPotionSlot();
                if (slot != null && !cooldowns.method_7904(slot.method_7677())) {
                    this.useSlotPacketDirect(slot, Float.valueOf(90.0f));
                }
                this.debuffStage = DebuffStage.AURA;
                this.debuffTicks = delay;
                break;
            }
            case 2: {
                class_1735 slot = this.findAuraSlot();
                if (slot != null && !cooldowns.method_7904(slot.method_7677())) {
                    this.useSlotPacketDirect(slot, null);
                }
                this.debuffStage = DebuffStage.ASSASSIN;
                this.debuffTicks = delay;
                break;
            }
            case 3: {
                class_1735 slot = this.findAssassinPotionSlot();
                if (slot != null && !cooldowns.method_7904(slot.method_7677())) {
                    this.useSlotPacketDirect(slot, Float.valueOf(90.0f));
                }
                this.resetAutoDebuff();
                break;
            }
        }
    }

    private void resetAutoDebuff() {
        this.debuffStage = DebuffStage.IDLE;
        this.debuffTicks = 0;
    }

    private class_1735 findSleepingPotionSlot() {
        return PlayerInventoryUtil.getSlot(class_1802.field_8436, slot -> slot.field_7871 instanceof class_1661 && this.matchesDonationPotion("\u0421\u043d\u043e\u0442\u0432\u043e\u0440\u043d\u043e\u0435", slot.method_7677()));
    }

    private class_1735 findAuraSlot() {
        return PlayerInventoryUtil.getSlot(class_1802.field_8614, slot -> slot.field_7871 instanceof class_1661);
    }

    private class_1735 findAssassinPotionSlot() {
        return PlayerInventoryUtil.getSlot(class_1802.field_8436, slot -> slot.field_7871 instanceof class_1661 && this.matchesDonationPotion("\u0417\u0435\u043b\u044c\u0435 \u0410\u0441\u0441\u0430\u0441\u0438\u043d\u0430", slot.method_7677()));
    }

    private void useSlotPacketDirect(class_1735 rawSlot, Float pitch) {
        PlayerInventoryComponent.runInventoryAction(() -> this.performSlotPacketDirect(rawSlot, pitch));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void performSlotPacketDirect(class_1735 rawSlot, Float pitch) {
        block19: {
            if (rawSlot == null || ServerHelper.mc.field_1724 == null || ServerHelper.mc.field_1761 == null) {
                return;
            }
            class_1735 slot = PlayerInventoryUtil.resolveSlot(rawSlot);
            if (slot == null || !ServerHelper.mc.field_1724.field_7512.method_34255().method_7960()) {
                return;
            }
            int returnSlot = ServerHelper.mc.field_1724.method_31548().method_67532();
            float originalPitch = ServerHelper.mc.field_1724.method_36455();
            float originalYaw = ServerHelper.mc.field_1724.method_36454();
            if (pitch != null) {
                Angle downAngle = new Angle(originalYaw, pitch.floatValue());
                RotationController.setForcedInteractAngle(downAngle);
                ServerHelper.mc.field_1724.field_3944.method_52787((class_2596)new class_2828.class_2831(originalYaw, pitch.floatValue(), ServerHelper.mc.field_1724.method_24828(), ServerHelper.mc.field_1724.field_5976));
            }
            try {
                boolean isHotbar;
                if (slot.field_7871 instanceof class_1661 && slot.method_34266() == 40) {
                    ServerHelper.mc.field_1761.method_2919((class_1657)ServerHelper.mc.field_1724, class_1268.field_5810);
                    ServerHelper.mc.field_1724.method_6104(class_1268.field_5810);
                    return;
                }
                boolean bl = isHotbar = slot.field_7871 instanceof class_1661 && slot.method_34266() >= 0 && slot.method_34266() < 9;
                if (isHotbar) {
                    int hotbarIndex = slot.method_34266();
                    if (ServerHelper.mc.field_1724.method_31548().method_67532() != hotbarIndex) {
                        ServerHelper.mc.field_1724.method_31548().method_61496(hotbarIndex);
                        ServerHelper.mc.field_1724.field_3944.method_52787((class_2596)new class_2868(hotbarIndex));
                    }
                    try {
                        ServerHelper.mc.field_1761.method_2919((class_1657)ServerHelper.mc.field_1724, class_1268.field_5808);
                        ServerHelper.mc.field_1724.method_6104(class_1268.field_5808);
                        break block19;
                    }
                    finally {
                        if (ServerHelper.mc.field_1724.method_31548().method_67532() != returnSlot) {
                            ServerHelper.mc.field_1724.method_31548().method_61496(returnSlot);
                            ServerHelper.mc.field_1724.field_3944.method_52787((class_2596)new class_2868(returnSlot));
                        }
                    }
                }
                if (returnSlot >= 0 && ServerHelper.mc.field_1724.method_31548().method_67532() != returnSlot) {
                    ServerHelper.mc.field_1724.method_31548().method_61496(returnSlot);
                    ServerHelper.mc.field_1724.field_3944.method_52787((class_2596)new class_2868(returnSlot));
                }
                int targetHotbar = returnSlot >= 0 ? returnSlot : ServerHelper.mc.field_1724.method_31548().method_67532();
                PlayerInventoryUtil.clickSlot(slot, targetHotbar, class_1713.field_7791, false);
                try {
                    ServerHelper.mc.field_1761.method_2919((class_1657)ServerHelper.mc.field_1724, class_1268.field_5808);
                    ServerHelper.mc.field_1724.method_6104(class_1268.field_5808);
                }
                finally {
                    PlayerInventoryUtil.clickSlot(slot, targetHotbar, class_1713.field_7791, false);
                }
            }
            finally {
                if (pitch != null) {
                    RotationController.setForcedInteractAngle(null);
                    ServerHelper.mc.field_1724.field_3944.method_52787((class_2596)new class_2828.class_2831(originalYaw, originalPitch, ServerHelper.mc.field_1724.method_24828(), ServerHelper.mc.field_1724.field_5976));
                }
            }
        }
    }

    private void useTrapAndPlast() {
        if (!this.script.isFinished() || this.trapPlastBusy || ServerHelper.mc.field_1724 == null) {
            return;
        }
        class_1735 trapSlot = PlayerInventoryUtil.getSlot(class_1802.field_22021);
        class_1735 plastSlot = this.findFunTimePlastSlot();
        if (trapSlot == null || plastSlot == null) {
            return;
        }
        if (PlayerInventoryUtil.notifyCooldown(trapSlot.method_7677(), "Trap")) {
            return;
        }
        if (PlayerInventoryUtil.notifyCooldown(plastSlot.method_7677(), "Plast")) {
            return;
        }
        this.startTrapAndPlast(trapSlot, plastSlot);
    }

    private void startTrapAndPlast(class_1735 trapSlot, class_1735 plastSlot) {
        class_746 player = ServerHelper.mc.field_1724;
        Angle trapAngle = AngleUtil.cameraAngle();
        float t = (float)player.field_6012 + mc.method_61966().method_60637(false);
        float noise = (float)(Math.sin(t * 0.31f) * 6.600001001477404 + Math.sin(t * 0.73f + 1.1f) * 0.3000001491338646);
        Angle plastAngle = ServerUtil.isSpookyTime() ? new Angle(trapAngle.getYaw(), -90.0f) : new Angle(trapAngle.getYaw() + noise, class_3532.method_15363((float)(-90.0f + noise / 2.0f), (float)-90.0f, (float)90.0f));
        int generation = ++this.trapPlastGeneration;
        this.trapPlastBusy = true;
        this.trapPlastStarted = player.field_6012;
        Runnable place = () -> {
            if (!this.isState() || ServerHelper.mc.field_1724 != player || generation != this.trapPlastGeneration) {
                return;
            }
            this.trapPlastAngle = null;
            this.useSlot(plastSlot, plastAngle);
            ScreenWalkInventorySync.whenIdle(() -> {
                if (!this.isState() || ServerHelper.mc.field_1724 != player || generation != this.trapPlastGeneration) {
                    return;
                }
                this.useSlot(trapSlot, trapAngle);
                ScreenWalkInventorySync.whenIdle(() -> {
                    if (generation == this.trapPlastGeneration) {
                        this.trapPlastBusy = false;
                    }
                });
            });
        };
        if (ServerUtil.isSpookyTime()) {
            place.run();
        } else {
            this.trapPlastAngle = plastAngle;
            this.script.cleanup().addTickStep(0, place::run, () -> Math.abs(class_3532.method_15393((float)(RotationController.INSTANCE.getRotation().getYaw() - plastAngle.getYaw()))) <= 2.0f && Math.abs(RotationController.INSTANCE.getRotation().getPitch() - plastAngle.getPitch()) <= 2.0f);
        }
    }

    private void interactTrapPlast(Angle angle) {
        this.trapPlastAngle = null;
        RotationController.setForcedInteractAngle(angle);
        try {
            PlayerIntersectionUtil.interactItem(class_1268.field_5808, angle);
        }
        finally {
            RotationController.setForcedInteractAngle(null);
        }
    }

    private void useSlot(class_1735 slot, Angle angle) {
        if (slot == null || ServerHelper.mc.field_1724 == null) {
            return;
        }
        ScreenWalkInventorySync.swapUseRestore(slot, () -> this.useItemAtAngle(angle));
    }

    private void useItemAtAngle(Angle angle) {
        RotationController.setForcedInteractAngle(angle);
        try {
            PlayerIntersectionUtil.interactItem(class_1268.field_5808, angle);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        finally {
            RotationController.setForcedInteractAngle(null);
        }
    }

    private void useWindCharge() {
        if (ServerHelper.mc.field_1724 == null || ServerHelper.mc.field_1761 == null) {
            return;
        }
        if (PlayerInventoryUtil.notifyCooldown(class_1802.field_49098, "Wind Charge")) {
            return;
        }
        class_1735 slot = PlayerInventoryUtil.getSlot(class_1802.field_49098);
        if (slot == null) {
            return;
        }
        Angle angle = this.windChargeDirection.isSelected("\u041f\u043e\u0434 \u0441\u0435\u0431\u044f") ? this.getDonationPotionUnderSelfAngle() : AngleUtil.cameraAngle();
        ScreenWalkInventorySync.swapUseRestore(slot, () -> this.useItemAtAngle(angle));
    }

    @EventHandler
    public void onRotationUpdate(RotationUpdateEvent e) {
        if (ServerHelper.mc.field_1724 == null || ServerHelper.mc.field_1687 == null) {
            return;
        }
        if (this.trapPlastBusy && ServerHelper.mc.field_1724.field_6012 - this.trapPlastStarted > 60) {
            ++this.trapPlastGeneration;
            this.trapPlastBusy = false;
            this.trapPlastAngle = null;
            this.script.cleanup();
            RotationController.INSTANCE.release(this);
        }
        if (this.trapPlastAngle != null) {
            RotationController.INSTANCE.rotateTo(this.trapPlastAngle, 2, this.trapPlastRotation, TaskPriority.CRITICAL_FOR_USER_PROTECTION, this);
        }
        if (this.pendingDonationPotionScheduled && ServerHelper.mc.field_1724.field_6012 - this.pendingDonationPotionStartedAt > 40) {
            this.clearPendingDonationPotion();
        }
        if (this.pendingDonationPotionSlot != null && this.pendingDonationPotionAngle != null) {
            float pitchError;
            RotationController.INSTANCE.rotateTo(this.pendingDonationPotionAngle, 1, new RotationConfig(true, true), TaskPriority.CRITICAL_FOR_USER_PROTECTION, this);
            if (e.getType() == 2 && !this.pendingDonationPotionScheduled && (pitchError = Math.abs(RotationController.INSTANCE.getRotation().getPitch() - this.pendingDonationPotionAngle.getPitch())) <= 5.0f) {
                this.pendingDonationPotionScheduled = true;
                class_1735 slot = this.pendingDonationPotionSlot;
                Angle angle = this.pendingDonationPotionAngle;
                this.useDonationPotionUnderSelf(slot, angle);
            }
        }
        if (e.getType() != 0) {
            return;
        }
        if (this.autoShulkerSource != null) {
            if (ServerHelper.mc.field_1724 == null || this.autoShulkerSource.field_7871 != ServerHelper.mc.field_1724.method_31548() || System.currentTimeMillis() - this.autoShulkerStartedAt > 3000L) {
                this.finishAutoShulker();
            } else {
                PlayerInventoryComponent.lockInventoryActions(2);
            }
        }
        this.processPendingStructureDetections();
        this.processPendingStructureSounds();
        if (this.keysOverridden) {
            ServerHelper.mc.field_1690.field_1894.method_23481(false);
            ServerHelper.mc.field_1690.field_1881.method_23481(false);
            ServerHelper.mc.field_1690.field_1913.method_23481(false);
            ServerHelper.mc.field_1690.field_1849.method_23481(false);
        }
        if (this.actionState != ActionState.IDLE) {
            this.handleSmartSwap();
            return;
        }
        if (this.pendingDonationPotionSlot != null) {
            return;
        }
        if (this.debuffStage != DebuffStage.IDLE) {
            this.handleAutoDebuffTick();
        }
        this.handleAutoRepair();
        this.handleAutoShulker();
        this.handleAutoLoot();
        this.script.cleanupIfFinished().update();
        this.structures.removeIf(cons -> cons.time - System.currentTimeMillis() <= 0L);
        this.cleanupExpiredAutoPoints();
    }

    private void clearPendingDonationPotion() {
        this.pendingDonationPotionSlot = null;
        this.pendingDonationPotionAngle = null;
        this.pendingDonationPotionScheduled = false;
    }

    private void handleSmartSwap() {
        long elapsed = System.currentTimeMillis() - this.actionTimer;
        switch (this.actionState.ordinal()) {
            case 0: {
                break;
            }
            case 1: {
                if (elapsed <= 50L) break;
                this.actionState = ActionState.WAITING_STOP;
                break;
            }
            case 2: {
                if (!(ServerHelper.mc.field_1724.method_18798().method_37267() < 0.1) && elapsed <= 150L) break;
                this.actionState = ActionState.SWAP_TO_ITEM;
                this.actionTimer = System.currentTimeMillis();
                break;
            }
            case 3: {
                if (elapsed <= 1L) break;
                this.performItemSwap(this.targetSlotId);
                this.actionState = ActionState.USE_ITEM;
                this.actionTimer = System.currentTimeMillis();
                break;
            }
            case 4: {
                if (elapsed <= 2L) break;
                PlayerIntersectionUtil.interactItem(class_1268.field_5808, RotationController.INSTANCE.getRotation());
                ServerHelper.mc.field_1724.method_6104(class_1268.field_5808);
                this.actionState = ActionState.SWAP_BACK;
                this.actionTimer = System.currentTimeMillis();
                break;
            }
            case 5: {
                if (elapsed <= 1L) break;
                this.restoreOriginalSlot();
                this.actionState = ActionState.SPEEDING_UP;
                this.actionTimer = System.currentTimeMillis();
                break;
            }
            case 6: {
                if (elapsed <= 50L) break;
                this.actionState = ActionState.IDLE;
                this.keysOverridden = false;
                this.updateKey(ServerHelper.mc.field_1690.field_1894);
                this.updateKey(ServerHelper.mc.field_1690.field_1881);
                this.updateKey(ServerHelper.mc.field_1690.field_1913);
                this.updateKey(ServerHelper.mc.field_1690.field_1849);
            }
        }
    }

    private void updateKey(class_304 keyBinding) {
        if (keyBinding == null) {
            return;
        }
        class_3675.class_306 key = class_3675.method_15981((String)keyBinding.method_1428());
        int keyCode = key.method_1444();
        long window = mc.method_22683().method_4490();
        boolean isPressed = GLFW.glfwGetKey((long)window, (int)keyCode) == 1;
        keyBinding.method_23481(isPressed);
    }

    private void performItemSwap(int slotId) {
        if (slotId >= 36 && slotId <= 44) {
            int hotbarSlot = slotId - 36;
            ServerHelper.mc.field_1724.method_31548().method_61496(hotbarSlot);
            ServerHelper.mc.field_1724.field_3944.method_52787((class_2596)new class_2868(hotbarSlot));
        } else {
            PlayerInventoryUtil.clickSlot(slotId, ServerHelper.mc.field_1724.method_31548().method_67532(), class_1713.field_7791, false);
        }
    }

    private void restoreOriginalSlot() {
        if (this.sourceSlotId < 36 || this.sourceSlotId > 44) {
            PlayerInventoryUtil.clickSlot(this.sourceSlotId, ServerHelper.mc.field_1724.method_31548().method_67532(), class_1713.field_7791, false);
        }
        if (ServerHelper.mc.field_1724.method_31548().method_67532() != this.originalSlot) {
            ServerHelper.mc.field_1724.method_31548().method_61496(this.originalSlot);
            ServerHelper.mc.field_1724.field_3944.method_52787((class_2596)new class_2868(this.originalSlot));
        }
    }

    private void handleAutoRepair() {
        if (this.autoRepairSetting.isValue() && this.autoRepairSetting.isVisible() && StreamSupport.stream(EntityCompat.getArmorItems((class_1309)ServerHelper.mc.field_1724).spliterator(), false).anyMatch(stack -> {
            if ((double)stack.method_7919() / (double)stack.method_7936() < 0.94) {
                return false;
            }
            class_6880 mendingEntry = ServerHelper.mc.field_1687.method_30349().method_30530(class_7924.field_41265).method_10223(class_1893.field_9101.method_29177()).orElse(null);
            return mendingEntry != null && class_1890.method_8225((class_6880)mendingEntry, (class_1799)stack) > 0;
        })) {
            PlayerInventoryUtil.slots().filter(slot -> {
                class_1799 stack = slot.method_7677();
                class_9279 component = (class_9279)stack.method_58694(class_9334.field_49628);
                return !ServerHelper.mc.field_1724.method_7357().method_7904(stack) && stack.method_7909().equals(class_1802.field_8287) && component != null && component.toString().contains("\"text\":\" - \u043f\u0440\u0438 \u043d\u0430\u0436\u0430\u0442\u0438\u0435 \u041f\u041a\u041c, \u043f\u043e\u043b\u043d\u043e\u0441\u0442\u044c\u044e \u0440\u0435\u043c\u043e\u043d\u0442\u0438\u0440\u0443\u0435\u0442\"") && this.repairWatch.every(5000.0);
            }).findFirst().ifPresent(slot -> ScreenWalkInventorySync.swapUseRestore(slot, () -> PlayerIntersectionUtil.interactItem(class_1268.field_5808)));
        }
    }

    private void handleAutoShulker() {
        if (this.autoShulkerSource != null || PlayerInventoryComponent.isInventoryActionBusy() || !ServerHelper.mc.field_1724.field_7512.method_34255().method_7960()) {
            return;
        }
        if (!PlayerInventoryUtil.isServerScreen() && !this.stacks.isEmpty() && this.script2.isFinished() && this.shulkerWatch.finished(300.0)) {
            PlayerInventoryUtil.slots().filter(s -> s.method_7677().method_58694(class_9334.field_49622) != null).max(Comparator.comparingDouble(s -> ((class_9288)s.method_7677().method_58695((class_9331)class_9334.field_49622, null)).field_49338.stream().filter(item -> !item.method_7960()).toList().size())).ifPresent(shulker -> {
                this.autoShulkerSource = shulker;
                this.autoShulkerHotbar = ServerHelper.mc.field_1724.method_31548().method_67532();
                this.autoShulkerDisplaced = ServerHelper.mc.field_1724.method_6047().method_7972();
                this.autoShulkerStartedAt = System.currentTimeMillis();
                PlayerInventoryComponent.lockInventoryActions(2);
                PlayerInventoryUtil.swap(shulker, this.autoShulkerHotbar, false);
                PlayerInventoryUtil.closeScreen(false);
                PlayerIntersectionUtil.interactItem(class_1268.field_5808);
                this.script2.cleanup().addTickStep(0, () -> {
                    ArrayList integers = new ArrayList();
                    PlayerInventoryUtil.slots().forEach(slot -> this.stacks.entrySet().stream().filter(entry -> slot.field_7871.equals((Object)ServerHelper.mc.field_1724.field_7514) && ((class_1792)entry.getValue()).equals(slot.method_7677().method_7909()) && (Integer)entry.getKey() == slot.field_7874).forEach(entry -> {
                        PlayerInventoryUtil.clickSlot(slot, 0, class_1713.field_7794, false);
                        integers.add(slot.field_7874);
                    }));
                    integers.forEach(this.stacks::remove);
                    PlayerInventoryUtil.closeScreen(false);
                    this.finishAutoShulker();
                    PlayerInventoryUtil.closeScreen(false);
                    this.shulkerWatch.reset();
                });
            });
        }
    }

    private void finishAutoShulker() {
        if (this.autoShulkerSource == null) {
            return;
        }
        if (ServerHelper.mc.field_1724 != null && this.autoShulkerSource.field_7871 == ServerHelper.mc.field_1724.method_31548()) {
            class_1735 source = PlayerInventoryUtil.resolveSlot(this.autoShulkerSource);
            if (source != null && this.autoShulkerHotbar >= 0 && this.autoShulkerDisplaced != null && PlayerInventoryUtil.matchesSwapStack(source.method_7677(), this.autoShulkerDisplaced) && ServerHelper.mc.field_1724.field_7512.method_34255().method_7960()) {
                PlayerInventoryUtil.swap(source, this.autoShulkerHotbar, false);
            }
            PlayerInventoryComponent.unlockInventoryActions();
        }
        this.autoShulkerSource = null;
        this.autoShulkerHotbar = -1;
        this.autoShulkerDisplaced = null;
        this.script2.cleanup();
    }

    private void handleAutoLoot() {
        if (this.autoLootSetting.isValue() && this.autoLootSetting.isVisible()) {
            PlayerIntersectionUtil.streamEntities().filter(class_3988.class::isInstance).map(class_3988.class::cast).filter(m -> m.method_6084(class_1304.field_6173) || m.method_6084(class_1304.field_6171)).findFirst().ifPresent(merchant -> {
                class_243 attackVector = (class_243)this.pointFinder.computeVector((class_1309)merchant, 6.0f, RotationController.INSTANCE.getRotation(), new LinearSmoothMode().randomValue(), true).method_15442();
                Angle angle = AngleUtil.calculateAngle(attackVector);
                this.itemsWatch.reset();
                this.entityUUID = merchant.method_5667();
                if (ServerHelper.mc.field_1724.method_33571().method_1022(merchant.method_5829().method_1005()) <= 6.0) {
                    ServerHelper.mc.field_1724.field_3944.method_52787((class_2596)class_2824.method_34208((class_1297)merchant, (boolean)false, (class_1268)class_1268.field_5808, (class_243)merchant.method_5829().method_1005()));
                    ServerHelper.mc.field_1724.field_3944.method_52787((class_2596)class_2824.method_34207((class_1297)merchant, (boolean)false, (class_1268)class_1268.field_5808));
                    RotationController.INSTANCE.rotateTo(angle, RotationConfig.DEFAULT, TaskPriority.HIGH_IMPORTANCE_3, this);
                }
            });
        }
    }

    @EventHandler
    public void onPacket(PacketEvent e) {
        if (!PlayerIntersectionUtil.nullCheck()) {
            class_2596<?> class_25962 = e.getPacket();
            Objects.requireNonNull(class_25962);
            class_2596<?> class_25963 = class_25962;
            if (class_25963 instanceof class_2775 item && this.autoShulkerSetting.isValue() && this.autoShulkerSetting.isVisible() && item.method_11912() == ServerHelper.mc.field_1724.method_5628() && ServerHelper.mc.field_1687.method_8469(item.method_11915()) instanceof class_1542 entity) {
                class_1799 stack = entity.method_6983();
                if (stack.method_58694(class_9334.field_49622) == null) {
                    this.stacks.put(-MathUtil.getRandom(1, 999999999), stack.method_7909());
                    this.shulkerWatch.reset();
                }
            } else if (class_25963 instanceof class_2653 slot) {
                if (slot.method_11452() == 0) {
                    class_1792 item = slot.method_11449().method_7909();
                    this.stacks.entrySet().stream().filter(entry -> (Integer)entry.getKey() < 0 && ((class_1792)entry.getValue()).equals(item)).findFirst().ifPresent(entry -> {
                        this.stacks.put(slot.method_11450() + 18, item);
                        this.stacks.remove(entry.getKey());
                    });
                }
            } else if (class_25963 instanceof class_2637 chunkDelta && this.consumablesSetting.isValue()) {
                ArrayList updated = new ArrayList();
                chunkDelta.method_30621((pos, state) -> {
                    class_2338 immutable = pos.method_10062();
                    this.blockStateMap.put(immutable, (class_2680)state);
                    updated.add(immutable);
                });
                this.pendingStructureDetections.add(updated);
            } else if (class_25963 instanceof class_2767 sound && this.consumablesSetting.isValue() && this.isFunTimeMode() && this.isFunTimePlastSound(sound)) {
                this.pendingStructureSounds.add(new PendingStructureSound(new class_243(sound.method_11890(), sound.method_11889(), sound.method_11893()), System.currentTimeMillis()));
            } else if (class_25963 instanceof class_7439 gameMessage && this.autoPointSetting.isValue() && this.autoPointSetting.isVisible()) {
                        class_2561 content = gameMessage.comp_763();
                        String contentString = content.toString();
                        String message = content.getString();
                        this.playEventAnnouncementSound(message);
                        this.updateEventDelayFromMessage(message);
                        String name = this.extractAutoPointName(message);
                        if (name != null) {
                            String lvl = this.extractAutoPointLine(message, "\u0423\u0440\u043e\u0432\u0435\u043d\u044c \u043b\u0443\u0442\u0430: ");
                            String owner = this.extractAutoPointLine(message, "\u041f\u0440\u0438\u0437\u0432\u0430\u043d \u0438\u0433\u0440\u043e\u043a\u043e\u043c: ");
                            class_243 center = this.extractGpsCenter(contentString);
                            if (center == null) {
                                center = this.extractGpsCenter(message);
                            }
                            if (center != null) {
                                switch (name) {
                                    case "\u041c\u0438\u0441\u0442\u0438\u0447\u0435\u0441\u043a\u0438\u0439 \u0441\u0443\u043d\u0434\u0443\u043a": {
                                        this.addEvent(name, lvl, owner, center, "overworld", 300, 0);
                                        break;
                                    }
                                    case "\u0412\u0443\u043b\u043a\u0430\u043d": {
                                        this.addEvent(name, lvl, owner, center, "overworld", 300, 120);
                                        break;
                                    }
                                    case "\u041c\u0435\u0442\u0435\u043e\u0440\u0438\u0442\u043d\u044b\u0439 \u0434\u043e\u0436\u0434\u044c": 
                                    case "\u041c\u0430\u044f\u043a \u0443\u0431\u0438\u0439\u0446\u0430": 
                                    case "\u041c\u0438\u0441\u0442\u0438\u0447\u0435\u0441\u043a\u0438\u0439 \u0410\u043b\u0442\u0430\u0440\u044c": {
                                        this.addEvent(name, lvl, owner, center, "overworld", 360, 0);
                                        break;
                                    }
                                    case "\u0417\u0430\u0433\u0430\u0434\u043e\u0447\u043d\u044b\u0439 \u043c\u0430\u044f\u043a": {
                                        this.addEvent(name, lvl, owner, center, "overworld", 60, 180);
                                        break;
                                    }
                                    case "\u0413\u0435\u0439\u0437\u0435\u0440": {
                                        this.addEvent(name, lvl, owner, center, "overworld", 60, 180);
                                    }
                                }
                            }
                            switch (name) {
                                case "\u0421\u0443\u043d\u0434\u0443\u043a \u0441\u043c\u0435\u0440\u0442\u0438": {
                                    this.addEvent(name, lvl, owner, class_2338.method_49637((double)-155.0, (double)64.0, (double)205.0).method_46558(), "lobby", 300, 0);
                                    break;
                                }
                                case "\u0410\u0434\u0441\u043a\u0430\u044f \u0440\u0435\u0437\u043d\u044f": {
                                    this.addEvent(name, lvl, owner, class_2338.method_49637((double)48.0, (double)87.0, (double)73.0).method_46558(), "lobby", 180, 120);
                                }
                            }
                        }
                    } else if (class_25963 instanceof class_7439 gameMessage) {
                        String subString;
                        String message = gameMessage.comp_763().getString();
                        if (message.contains("\u25b6 \u041f\u043e\u0432\u0442\u043e\u0440\u043d\u043e \u0430\u043a\u0442\u0438\u0432\u0438\u0440\u043e\u0432\u0430\u0442\u044c \u041f\u0443\u0437\u044b\u0440\u044c \u043e\u043f\u044b\u0442\u0430 \u0432\u043e\u0437\u043c\u043e\u0436\u043d\u043e \u0447\u0435\u0440\u0435\u0437") && (subString = StringUtils.substringBetween((String)message, (String)"\u0447\u0435\u0440\u0435\u0437 ", (String)" \u0441\u0435\u043a\u0443\u043d\u0434")) != null && !subString.isEmpty()) {
                            int duration = Integer.parseInt(subString) * 20;
                            class_1796 manager = ServerHelper.mc.field_1724.method_7357();
                            manager.method_62835(class_1802.field_8287.method_7854(), duration);
                            CoolDowns.getInstance().packet(new PacketEvent((class_2596<?>)new class_2656(manager.method_62836(class_1802.field_8287.method_7854()), duration), PacketEvent.Type.RECEIVE));
                        }
                    } else if (class_25963 instanceof class_3944 openScreen && openScreen.method_17594().getString().contains("\u0420\u044e\u043a\u0437\u0430\u043a") && !this.stacks.isEmpty()) {
                        this.script.cleanup().addTickStep(0, this.script2::update);
                    }
        }
    }

    private String extractAutoPointName(String message) {
        String name = StringUtils.substringBetween((String)message, (String)"|||   [", (String)"]   ");
        if (name != null && !name.isBlank()) {
            return name.trim();
        }
        for (String known : List.of("\u041c\u0438\u0441\u0442\u0438\u0447\u0435\u0441\u043a\u0438\u0439 \u0441\u0443\u043d\u0434\u0443\u043a", "\u0412\u0443\u043b\u043a\u0430\u043d", "\u041c\u0435\u0442\u0435\u043e\u0440\u0438\u0442\u043d\u044b\u0439 \u0434\u043e\u0436\u0434\u044c", "\u041c\u0430\u044f\u043a \u0443\u0431\u0438\u0439\u0446\u0430", "\u041c\u0438\u0441\u0442\u0438\u0447\u0435\u0441\u043a\u0438\u0439 \u0410\u043b\u0442\u0430\u0440\u044c", "\u0417\u0430\u0433\u0430\u0434\u043e\u0447\u043d\u044b\u0439 \u043c\u0430\u044f\u043a", "\u0413\u0435\u0439\u0437\u0435\u0440", "\u0421\u0443\u043d\u0434\u0443\u043a \u0441\u043c\u0435\u0440\u0442\u0438", "\u0410\u0434\u0441\u043a\u0430\u044f \u0440\u0435\u0437\u043d\u044f")) {
            if (!message.contains(known)) continue;
            return known;
        }
        return null;
    }

    private void playEventAnnouncementSound(String message) {
        if (message == null || !this.isFunTimeMode()) {
            return;
        }
        String normalized = class_124.method_539((String)message);
        if (normalized == null) {
            return;
        }
        if (!(normalized = normalized.replace('\u00a0', ' ').toLowerCase(Locale.ROOT)).contains("\u043f\u043e\u044f\u0432\u0438\u0442\u0441\u044f") || !normalized.matches("(?s).*\\b3\\s*\u043c\u0438\u043d\u0443\u0442.*")) {
            return;
        }
        long now = System.currentTimeMillis();
        if (normalized.equals(this.lastEventSoundMessage) && now - this.lastEventSoundAt < 5000L) {
            return;
        }
        this.lastEventSoundMessage = normalized;
        this.lastEventSoundAt = now;
        SoundManager.playSound(SoundManager.EVENT);
    }

    private void updateEventDelayFromMessage(String message) {
        if (message == null || this.serverEvents.isEmpty()) {
            return;
        }
        String lower = message.toLowerCase(Locale.ROOT);
        if (!(lower.contains("\u0434\u043e \u043e\u0442\u043a\u0440\u044b\u0442\u0438\u044f") || lower.contains("\u0434\u043e \u0438\u0437\u0432\u0435\u0440\u0436\u0435\u043d\u0438\u044f") || lower.contains("\u0434\u043e \u043f\u043e\u044f\u0432\u043b\u0435\u043d\u0438\u044f") || lower.contains("event delay"))) {
            return;
        }
        int seconds = this.parseEventDelaySeconds(lower);
        if (seconds <= 0) {
            return;
        }
        String eventName = this.extractAutoPointName(message);
        long now = System.currentTimeMillis();
        for (int i = 0; i < this.serverEvents.size(); ++i) {
            ServerEvent event = this.serverEvents.get(i);
            if (eventName != null && !event.name.equalsIgnoreCase(eventName)) continue;
            long activeDuration = Math.max(0L, event.timeEnd - event.timeOpen);
            long openAt = now + (long)seconds * 1000L;
            this.serverEvents.set(i, new ServerEvent(event.name, null, event.owner, event.vec, event.world, event.anarchy, openAt, openAt + activeDuration));
            if (eventName != null) break;
        }
    }

    private int parseEventDelaySeconds(String message) {
        Matcher matcher = EVENT_DELAY_PATTERN.matcher(message);
        while (matcher.find()) {
            int seconds;
            int minutes;
            int total;
            if (matcher.group(1) == null && matcher.group(2) == null || (total = (minutes = matcher.group(1) == null ? 0 : Integer.parseInt(matcher.group(1))) * 60 + (seconds = matcher.group(2) == null ? 0 : Integer.parseInt(matcher.group(2)))) <= 0) continue;
            return total;
        }
        return 0;
    }

    private void requestEventDelay() {
        long now = System.currentTimeMillis();
        if (ServerHelper.mc.field_1724 == null || now - this.lastEventDelayRequestAt < 2000L) {
            return;
        }
        this.lastEventDelayRequestAt = now;
        ServerHelper.mc.field_1724.field_3944.method_45730("event delay");
    }

    private String extractAutoPointLine(String message, String label) {
        int start = message.indexOf(label);
        if (start < 0) {
            return null;
        }
        int end = message.indexOf(10, start += label.length());
        String value = (end >= 0 ? message.substring(start, end) : message.substring(start)).replace("\u2551", "").trim();
        return value.isEmpty() ? null : value;
    }

    private class_243 extractGpsCenter(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        Matcher matcher = GPS_PATTERN.matcher(text);
        if (!matcher.find()) {
            return null;
        }
        try {
            int x = Integer.parseInt(matcher.group(1));
            int y = Integer.parseInt(matcher.group(2));
            int z = Integer.parseInt(matcher.group(3));
            return class_2338.method_49637((double)x, (double)y, (double)z).method_46558();
        }
        catch (NumberFormatException ignored) {
            return null;
        }
    }

    @EventHandler
    public void onSetScreen(SetScreenEvent e) {
        class_476 screen;
        class_437 class_4372 = e.getScreen();
        if (class_4372 instanceof class_476 && (screen = (class_476)class_4372).method_25440().getString().contains("\u0420\u044e\u043a\u0437\u0430\u043a") && !this.script2.isFinished()) {
            e.setScreen(null);
        }
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent e) {
        class_4587 matrix = e.getStack();
        this.keyBindings.stream().filter(bind -> PlayerIntersectionUtil.isKey(bind.setting) && this.findSlot((KeyBind)bind) != null).forEach(bind -> {
            class_2338 playerPos = ServerHelper.mc.field_1724.method_24515();
            class_243 smooth = MathUtil.interpolate(class_243.method_24954((class_2382)class_2338.method_49637((double)ServerHelper.mc.field_1724.field_6014, (double)ServerHelper.mc.field_1724.field_6036, (double)ServerHelper.mc.field_1724.field_5969)), class_243.method_24954((class_2382)playerPos)).method_1020(class_243.method_24954((class_2382)playerPos));
            switch (bind.setting.getName()) {
                case "Trap": 
                case "Default Trap": {
                    this.drawItemCube(playerPos, smooth, 1.99f, ColorUtil.getClientColor());
                    break;
                }
                case "Disorientation": 
                case "Clear Dust": {
                    this.drawItemRadius(matrix, bind.distance, ColorUtil.LIGHT_RED);
                    break;
                }
                case "Explosive Thing": {
                    this.drawItemRadius(matrix, 5.0f, ColorUtil.getClientColor());
                    break;
                }
                case "Plast": {
                    float yaw = class_3532.method_15393((float)ServerHelper.mc.field_1724.method_36454());
                    if (Math.abs(ServerHelper.mc.field_1724.method_36455()) > 60.0f) {
                        class_2338 blockPos = playerPos.method_10084().method_10079(ServerHelper.mc.field_1724.method_58149(), 3);
                        class_243 pos1 = class_243.method_24954((class_2382)blockPos.method_10089(3).method_10077(3).method_10074()).method_1019(smooth);
                        class_243 pos2 = class_243.method_24954((class_2382)blockPos.method_10088(2).method_10076(2).method_10084()).method_1019(smooth);
                        Render3DUtil.drawBox(new class_238(pos1, pos2), ColorUtil.getClientColor(), 3.0f, true, true, true);
                        break;
                    }
                    if (yaw <= -157.5f || yaw >= 157.5f) {
                        class_2338 blockPos = playerPos.method_10076(3).method_10084();
                        class_243 pos1 = class_243.method_24954((class_2382)blockPos.method_10087(2).method_10089(3)).method_1019(smooth);
                        class_243 pos2 = class_243.method_24954((class_2382)blockPos.method_10086(3).method_10088(2).method_10077(2)).method_1019(smooth);
                        Render3DUtil.drawBox(new class_238(pos1, pos2), ColorUtil.getClientColor(), 3.0f, true, true, true);
                        break;
                    }
                    if (yaw <= -112.5f) {
                        this.drawSidePlast(playerPos.method_10089(5).method_10072().method_10074(), smooth, ColorUtil.getClientColor(), -1, true);
                        break;
                    }
                    if (yaw <= -67.5f) {
                        class_2338 blockPos = playerPos.method_10089(2).method_10084();
                        class_243 pos1 = class_243.method_24954((class_2382)blockPos.method_10087(2).method_10077(3)).method_1019(smooth);
                        class_243 pos2 = class_243.method_24954((class_2382)blockPos.method_10086(3).method_10076(2).method_10089(2)).method_1019(smooth);
                        Render3DUtil.drawBox(new class_238(pos1, pos2), ColorUtil.getClientColor(), 3.0f, true, true, true);
                        break;
                    }
                    if (yaw <= -22.5f) {
                        this.drawSidePlast(playerPos.method_10089(5).method_10074(), smooth, ColorUtil.getClientColor(), 1, false);
                        break;
                    }
                    if ((double)yaw >= -22.5 && (double)yaw <= 22.5) {
                        class_2338 blockPos = playerPos.method_10077(2).method_10084();
                        class_243 pos1 = class_243.method_24954((class_2382)blockPos.method_10087(2).method_10089(3)).method_1019(smooth);
                        class_243 pos2 = class_243.method_24954((class_2382)blockPos.method_10086(3).method_10088(2).method_10077(2)).method_1019(smooth);
                        Render3DUtil.drawBox(new class_238(pos1, pos2), ColorUtil.getClientColor(), 3.0f, true, true, true);
                        break;
                    }
                    if (yaw <= 67.5f) {
                        this.drawSidePlast(playerPos.method_10088(4).method_10074(), smooth, ColorUtil.getClientColor(), 1, true);
                        break;
                    }
                    if (yaw <= 112.5f) {
                        class_2338 blockPos = playerPos.method_10088(3).method_10084();
                        class_243 pos1 = class_243.method_24954((class_2382)blockPos.method_10087(2).method_10077(3)).method_1019(smooth);
                        class_243 pos2 = class_243.method_24954((class_2382)blockPos.method_10086(3).method_10076(2).method_10089(2)).method_1019(smooth);
                        Render3DUtil.drawBox(new class_238(pos1, pos2), ColorUtil.getClientColor(), 3.0f, true, true, true);
                        break;
                    }
                    if (!(yaw <= 157.5f)) break;
                    this.drawSidePlast(playerPos.method_10088(4).method_10072().method_10074(), smooth, ColorUtil.getClientColor(), -1, false);
                    break;
                }
                case "Explosive Trap": {
                    this.drawItemCube(playerPos, smooth, 3.99f, ColorUtil.LIGHT_RED);
                    break;
                }
                case "Stun": {
                    this.drawItemCube(playerPos, smooth, 15.01f, ColorUtil.LIGHT_RED);
                    break;
                }
                case "SnowBall": {
                    ProjectilePrediction.getInstance().drawPredictionInHand(matrix, List.of(class_1802.field_8543.method_7854()), AngleUtil.cameraAngle());
                }
            }
        });
    }

    @EventHandler
    public void onDraw(DrawEvent e) {
        class_332 context = e.getDrawContext();
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        this.structures.forEach(cons -> {
            double time = (double)(cons.time - System.currentTimeMillis()) / 1000.0;
            class_243 displayPos = cons.item.equals(class_1802.field_22021) ? cons.vec.method_1031(0.0, 2.0, 0.0) : cons.vec;
            class_243 vec3d = ProjectionUtil.worldSpaceToScreenSpace(displayPos);
            String text = String.format(Locale.ROOT, "%.1f\u0441", Math.max(0.0, time));
            FontRenderer font = Fonts.getSize(14);
            float width = font.getStringWidth(text);
            float posX = (float)(vec3d.field_1352 - (double)(width / 2.0f));
            float posY = (float)vec3d.field_1351;
            float padding = 2.0f;
            if (ProjectionUtil.canSee(displayPos) && cons.anarchy == ServerUtil.getAnarchy() && ServerUtil.getWorldType().equals(cons.world)) {
                blur.render(ShapeProperties.create(matrix, posX - padding, posY - padding, width + padding * 2.0f, 10.0).round(1.5f).color(ColorUtil.HALF_BLACK).build());
                font.drawString(matrix, text, posX, posY + 1.0f, ColorUtil.getText());
                Render2DUtil.defaultDrawStack(context, cons.item.method_7854(), posX - 14.0f, posY - 2.5f, true, false, 0.5f);
            }
        });
    }

    private void drawItemCube(class_2338 playerPos, class_243 smooth, float size, int color) {
        class_238 box = new class_238(playerPos.method_10084()).method_997(smooth).method_1014((double)size);
        boolean inBox = ServerHelper.mc.field_1687.method_18456().stream().map(player -> SimulatedPlayer.simulateOtherPlayer((class_1657)player, 2)).anyMatch(simulated -> simulated.player != ServerHelper.mc.field_1724 && box.method_994(simulated.boundingBox) && !FriendUtils.isFriend((class_1297)simulated.player));
        Render3DUtil.drawBox(box, inBox ? ColorUtil.getFriendColor() : color, 3.0f, true, true, true);
    }

    private void drawItemRadius(class_4587 matrix, float distance, int clr) {
        class_243 cosSin;
        int i;
        float playerHalfWidth = ServerHelper.mc.field_1724.method_17681() / 2.0f;
        int color = this.validDistance(distance) ? ColorUtil.getFriendColor() : clr;
        class_243 pos = MathUtil.interpolate((class_1297)ServerHelper.mc.field_1724).method_1031((double)playerHalfWidth, 0.02, (double)playerHalfWidth);
        LegacyRenderSystem.enableBlend();
        LegacyRenderSystem.enableDepthTest();
        LegacyRenderSystem.depthMask(false);
        LegacyRenderSystem.disableCull();
        LegacyRenderSystem.blendFunc((Object)GlStateManager.SrcFactor.SRC_ALPHA, (Object)GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        class_287 buffer = ((class_289)tessellator.get()).method_60827(VertexFormat.class_5596.field_27380, class_290.field_1576);
        int size = 90;
        for (i = 0; i <= size; ++i) {
            cosSin = MathUtil.cosSin(i, size, distance);
            class_243 nextCosSin = MathUtil.cosSin(i + 1, size, distance);
            Render3DUtil.vertexLine(matrix, (class_4588)buffer, pos.method_1019(cosSin), pos.method_1031(cosSin.field_1352, cosSin.field_1351 + 2.0, cosSin.field_1350), ColorUtil.multAlpha(color, 0.2f), ColorUtil.multAlpha(color, 0.0f));
            Render3DUtil.drawLine(pos.method_1019(cosSin), pos.method_1019(nextCosSin), color, 2.0f, true);
        }
        size = 90;
        for (i = 0; i <= size; ++i) {
            cosSin = MathUtil.cosSin(i, size, distance);
            Render3DUtil.vertexLine(matrix, (class_4588)buffer, pos.method_1019(cosSin), pos.method_1031(cosSin.field_1352, cosSin.field_1351 - 2.0, cosSin.field_1350), ColorUtil.multAlpha(color, 0.2f), ColorUtil.multAlpha(color, 0.0f));
        }
        BufferRenderer.drawWithGlobalProgram(buffer.method_60800());
        LegacyRenderSystem.disableDepthTest();
        LegacyRenderSystem.depthMask(true);
        LegacyRenderSystem.disableBlend();
    }

    private void draw(class_4587 matrix, FontRenderer font, List<String> list, class_243 vec3d) {
        float offsetY = 0.0f;
        for (int i = 0; i < list.size(); ++i) {
            String string = list.get(i);
            float width = font.getStringWidth(string);
            float posX = (float)(vec3d.field_1352 - (double)(width / 2.0f));
            blur.render(ShapeProperties.create(matrix, posX - 2.0f, vec3d.field_1351 - 2.0 + (double)offsetY, width + 4.0f, 10.0).softness(3.0f).round(this.getRound(font, list, i, width)).color(ColorUtil.HALF_BLACK).build());
            font.drawString(matrix, string, posX, vec3d.field_1351 + 1.0 + (double)offsetY, ColorUtil.getText());
            offsetY += 10.0f;
        }
    }

    public void drawSidePlast(class_2338 blockPos, class_243 smooth, int color, int i, boolean ff) {
        class_243 vec3d = class_243.method_24954((class_2382)blockPos).method_1019(smooth);
        float width = 2.0f;
        int quadColor = ColorUtil.multAlpha(color, 0.15f);
        this.drawHorizontalLines(vec3d, color, width, i, ff);
        this.drawVerticalLines(vec3d, color, width, i, ff);
        this.drawHorizontalQuads(vec3d, quadColor, i, ff);
        this.drawVerticalQuads(vec3d, quadColor, i, ff);
    }

    private void drawHorizontalLines(class_243 vec3d, int color, float width, int i, boolean ff) {
        int f;
        float x = ff ? (float)i : (float)(-i);
        class_243 class_2432 = vec3d;
        vec3d = vec3d.method_1031((double)x, 0.0, 0.0);
        Render3DUtil.drawLine(class_2432, vec3d, color, width, true);
        for (f = 0; f < 4; ++f) {
            class_243 class_2433 = vec3d;
            vec3d = vec3d.method_1031(0.0, 0.0, (double)i);
            Render3DUtil.drawLine(class_2433, vec3d, color, width, true);
            class_243 class_2434 = vec3d;
            vec3d = vec3d.method_1031((double)x, 0.0, 0.0);
            Render3DUtil.drawLine(class_2434, vec3d, color, width, true);
        }
        class_243 class_2435 = vec3d;
        vec3d = vec3d.method_1031(0.0, 0.0, (double)i);
        Render3DUtil.drawLine(class_2435, vec3d, color, width, true);
        class_243 class_2436 = vec3d;
        vec3d = vec3d.method_1031((double)(x * -2.0f), 0.0, 0.0);
        Render3DUtil.drawLine(class_2436, vec3d, color, width, true);
        for (f = 0; f < 3; ++f) {
            class_243 class_2437 = vec3d;
            vec3d = vec3d.method_1031(0.0, 0.0, (double)(i * -1));
            Render3DUtil.drawLine(class_2437, vec3d, color, width, true);
            class_243 class_2438 = vec3d;
            vec3d = vec3d.method_1031((double)(x * -1.0f), 0.0, 0.0);
            Render3DUtil.drawLine(class_2438, vec3d, color, width, true);
        }
        Render3DUtil.drawLine(vec3d, vec3d.method_1031(0.0, 0.0, (double)(i * -2)), color, width, true);
    }

    private void drawVerticalLines(class_243 vec3d, int color, float width, int i, boolean ff) {
        int f;
        float x = ff ? (float)i : (float)(-i);
        Render3DUtil.drawLine(vec3d, vec3d.method_1031(0.0, 5.0, 0.0), color, width, true);
        vec3d = vec3d.method_1031((double)x, 0.0, 0.0);
        Render3DUtil.drawLine(vec3d, vec3d.method_1031(0.0, 5.0, 0.0), color, width, true);
        for (f = 0; f < 4; ++f) {
            vec3d = vec3d.method_1031((double)x, 0.0, (double)i);
            Render3DUtil.drawLine(vec3d, vec3d.method_1031(0.0, 5.0, 0.0), color, width, true);
        }
        vec3d = vec3d.method_1031(0.0, 0.0, (double)i);
        Render3DUtil.drawLine(vec3d, vec3d.method_1031(0.0, 5.0, 0.0), color, width, true);
        vec3d = vec3d.method_1031((double)(x * -2.0f), 0.0, 0.0);
        Render3DUtil.drawLine(vec3d, vec3d.method_1031(0.0, 5.0, 0.0), color, width, true);
        for (f = 0; f < 3; ++f) {
            vec3d = vec3d.method_1031((double)(x * -1.0f), 0.0, (double)(i * -1));
            Render3DUtil.drawLine(vec3d, vec3d.method_1031(0.0, 5.0, 0.0), color, width, true);
        }
    }

    private void drawHorizontalQuads(class_243 vec3d, int color, int i, boolean ff) {
        vec3d = vec3d.method_1031(0.0, 0.001, 0.0);
        float x = ff ? (float)i : (float)(-i);
        Render3DUtil.drawQuad(vec3d, vec3d.method_1031((double)x, 0.0, 0.0), vec3d.method_1031((double)x, 0.0, (double)(i * 2)), vec3d.method_1031(0.0, 0.0, (double)(i * 2)), color, true);
        for (int f = 0; f < 3; ++f) {
            vec3d = vec3d.method_1031((double)x, 0.0, (double)i);
            Render3DUtil.drawQuad(vec3d, vec3d.method_1031((double)x, 0.0, 0.0), vec3d.method_1031((double)x, 0.0, (double)(i * 2)), vec3d.method_1031(0.0, 0.0, (double)(i * 2)), color, true);
        }
        vec3d = vec3d.method_1031((double)x, 0.0, (double)i);
        Render3DUtil.drawQuad(vec3d, vec3d.method_1031((double)x, 0.0, 0.0), vec3d.method_1031((double)x, 0.0, (double)i), vec3d.method_1031(0.0, 0.0, (double)i), color, true);
    }

    private void drawVerticalQuads(class_243 vec3d, int color, int i, boolean ff) {
        int f;
        float x = ff ? (float)i : (float)(-i);
        Render3DUtil.drawQuad(vec3d, vec3d.method_1031((double)x, 0.0, 0.0), vec3d.method_1031((double)x, 5.0, 0.0), vec3d.method_1031(0.0, 5.0, 0.0), color, true);
        for (f = 0; f < 4; ++f) {
            vec3d = vec3d.method_1031((double)x, 0.0, (double)i);
            Render3DUtil.drawQuad(vec3d, vec3d.method_1031(0.0, 0.0, 0.0).method_1023((double)x, 0.0, (double)i), vec3d.method_1031(0.0, 5.0, 0.0).method_1023((double)x, 0.0, (double)i), vec3d.method_1031(0.0, 5.0, 0.0), color, true);
        }
        vec3d = vec3d.method_1031(0.0, 0.0, (double)i);
        Render3DUtil.drawQuad(vec3d, vec3d.method_1031((double)(x * -2.0f), 0.0, 0.0), vec3d.method_1031((double)(x * -2.0f), 5.0, 0.0), vec3d.method_1031(0.0, 5.0, 0.0), color, true);
        for (f = 0; f < 3; ++f) {
            vec3d = vec3d.method_1031((double)(x * -1.0f), 0.0, (double)(i * -1));
            Render3DUtil.drawQuad(vec3d, vec3d.method_1031(0.0, 0.0, 0.0).method_1023((double)(x * -1.0f), 0.0, (double)(i * -1)), vec3d.method_1031(0.0, 5.0, 0.0).method_1023((double)(x * -1.0f), 0.0, (double)(i * -1)), vec3d.method_1031(0.0, 5.0, 0.0), color, true);
        }
    }

    private boolean validDistance(float distance) {
        return distance == 0.0f || ServerHelper.mc.field_1724.method_73189().method_1022(ServerHelper.mc.field_1724.method_73189()) <= (double)distance;
    }

    private void processPendingStructureDetections() {
        if (this.pendingStructureDetections.isEmpty()) {
            this.blockStateMap.clear();
            return;
        }
        HashMap<class_2338, class_2680> updates = new HashMap<class_2338, class_2680>(this.blockStateMap);
        ArrayList<List<class_2338>> pending = new ArrayList<List<class_2338>>(this.pendingStructureDetections);
        this.pendingStructureDetections.clear();
        this.blockStateMap.clear();
        pending.forEach(positions -> this.detectConsumableStructure((List<class_2338>)positions, (Map<class_2338, class_2680>)updates));
    }

    private void processPendingStructureSounds() {
        long now = System.currentTimeMillis();
        Iterator<PendingStructureSound> iterator2 = this.pendingStructureSounds.iterator();
        while (iterator2.hasNext()) {
            PendingStructureSound sound = iterator2.next();
            long age = now - sound.time();
            if (age < 180L) continue;
            if (age <= 1500L && this.isFunTimeMode() && this.consumablesSetting.isValue()) {
                this.addPlastStructure(sound.pos(), 20000L);
            }
            if (age < 500L) continue;
            PlastShape shape = this.detectFunTimePlastShape(sound.pos());
            if (shape != null) {
                this.replacePlastStructure(sound.pos(), shape.center(), shape.duration(), sound.time());
            }
            iterator2.remove();
        }
    }

    private boolean isFunTimePlastSound(class_2767 sound) {
        return ((class_3414)sound.method_11894().comp_349()).comp_3319().method_12832().equals("block.anvil.place") && Math.abs(sound.method_11892() - 1.1f) < 0.01f && Math.abs(sound.method_11891() - 0.7f) < 0.01f;
    }

    private PlastShape detectFunTimePlastShape(class_243 origin) {
        if (ServerHelper.mc.field_1687 == null) {
            return null;
        }
        int originX = class_3532.method_15357((double)origin.field_1352);
        int originY = class_3532.method_15357((double)origin.field_1351);
        int originZ = class_3532.method_15357((double)origin.field_1350);
        class_2338.class_2339 cursor = new class_2338.class_2339();
        long seed = Long.MIN_VALUE;
        double nearestDistance = Double.MAX_VALUE;
        for (int x = -3; x <= 3; ++x) {
            for (int y = -3; y <= 3; ++y) {
                for (int z = -3; z <= 3; ++z) {
                    double distance;
                    cursor.method_10103(originX + x, originY + y, originZ + z);
                    if (!this.isFunTimePlastBlock(ServerHelper.mc.field_1687.method_8320((class_2338)cursor)) || !((distance = (double)(x * x + y * y + z * z)) < nearestDistance)) continue;
                    nearestDistance = distance;
                    seed = cursor.method_10063();
                }
            }
        }
        if (seed == Long.MIN_VALUE) {
            return null;
        }
        ArrayDeque<Long> pending = new ArrayDeque<Long>();
        HashSet<Long> visited = new HashSet<Long>();
        pending.add(seed);
        visited.add(seed);
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        int coralBlocks = 0;
        int[][] directions = new int[][]{{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
        while (!pending.isEmpty() && visited.size() <= 6000) {
            class_2338 current = class_2338.method_10092((long)((Long)pending.remove()));
            cursor.method_10101((class_2382)current);
            if (ServerHelper.mc.field_1687.method_8320((class_2338)cursor).method_27852(class_2246.field_10614)) {
                ++coralBlocks;
                minX = Math.min(minX, current.method_10263());
                minY = Math.min(minY, current.method_10264());
                minZ = Math.min(minZ, current.method_10260());
                maxX = Math.max(maxX, current.method_10263());
                maxY = Math.max(maxY, current.method_10264());
                maxZ = Math.max(maxZ, current.method_10260());
            }
            for (int[] direction : directions) {
                int x = current.method_10263() + direction[0];
                int y = current.method_10264() + direction[1];
                int z = current.method_10260() + direction[2];
                if (Math.abs(x - originX) > 16 || Math.abs(y - originY) > 16 || Math.abs(z - originZ) > 16) continue;
                cursor.method_10103(x, y, z);
                long packed = cursor.method_10063();
                if (!visited.add(packed) || !this.isFunTimePlastBlock(ServerHelper.mc.field_1687.method_8320((class_2338)cursor))) continue;
                pending.add(packed);
            }
        }
        if (coralBlocks < 3) {
            return null;
        }
        class_243 center = new class_243((double)(minX + maxX) / 2.0 + 0.5, (double)(minY + maxY) / 2.0 + 0.5, (double)(minZ + maxZ) / 2.0 + 0.5);
        int width = maxX - minX;
        int height = maxY - minY;
        int depth = maxZ - minZ;
        return new PlastShape(center, height < width && height < depth ? 60000L : 20000L);
    }

    private boolean isFunTimePlastBlock(class_2680 state) {
        return state.method_27852(class_2246.field_10445) || state.method_27852(class_2246.field_10115) || state.method_27852(class_2246.field_10614);
    }

    private void detectConsumableStructure(List<class_2338> updated, Map<class_2338, class_2680> updates) {
        if (updated == null || updated.isEmpty() || ServerHelper.mc.field_1687 == null || !this.isFunTimeMode()) {
            return;
        }
        if (updates.size() <= 50 || updates.size() >= 600) {
            return;
        }
        for (class_2338 updatedPos : updated) {
            class_2338 smallCenter = updatedPos.method_10086(2);
            if (this.isSmallTrapUpdate(smallCenter, updates)) {
                this.addTrapStructure(updatedPos, 15000L);
                continue;
            }
            class_2338 largeCenter = updatedPos.method_10086(3);
            if (!this.isLargeTrapUpdate(largeCenter, updates)) continue;
            this.addTrapStructure(updatedPos, 30000L);
        }
    }

    private boolean isSmallTrapUpdate(class_2338 center, Map<class_2338, class_2680> updates) {
        int mismatches = 0;
        for (class_2338 pos : PlayerIntersectionUtil.getCube(center, 2.0f)) {
            class_2680 state = updates.get(pos);
            if (pos.method_46558().method_1022(center.method_46558()) < 2.0) {
                if (state != null && !state.method_26215()) {
                    ++mismatches;
                }
            } else {
                boolean allowedOpening;
                boolean bl = allowedOpening = pos.equals((Object)center.method_10086(2).method_10095().method_10078()) || pos.equals((Object)center.method_10086(2).method_10095().method_10067()) || pos.equals((Object)center.method_10086(2).method_10072().method_10078()) || pos.equals((Object)center.method_10086(2).method_10072().method_10067());
                if (!allowedOpening && (state == null || state.method_26215())) {
                    ++mismatches;
                }
            }
            if (mismatches <= 5) continue;
            return false;
        }
        return true;
    }

    private boolean isLargeTrapUpdate(class_2338 center, Map<class_2338, class_2680> updates) {
        int mismatches = 0;
        for (class_2338 pos : PlayerIntersectionUtil.getCube(center, 3.0f)) {
            boolean inner;
            class_2680 state = updates.get(pos);
            boolean bl = inner = Math.abs(pos.method_10263() - center.method_10263()) <= 2 && Math.abs(pos.method_10264() - center.method_10264()) <= 2 && Math.abs(pos.method_10260() - center.method_10260()) <= 2;
            if (inner) {
                if (state != null && !state.method_26215()) {
                    ++mismatches;
                }
            } else if (!pos.equals((Object)center.method_10086(3)) && (state == null || state.method_26215())) {
                ++mismatches;
            }
            if (mismatches <= 5) continue;
            return false;
        }
        return true;
    }

    private void addTrapStructure(class_2338 center, long duration) {
        long now = System.currentTimeMillis();
        boolean duplicate = this.structures.stream().anyMatch(structure -> structure.item().equals(class_1802.field_22021) && structure.vec().method_1025(center.method_46558()) < 16.0 && structure.time() > now);
        if (!duplicate) {
            this.addStructure(class_1802.field_22021, center.method_46558(), now + duration);
        }
    }

    private void addPlastStructure(class_243 center, long duration) {
        long now = System.currentTimeMillis();
        boolean duplicate = this.structures.stream().anyMatch(structure -> structure.item().equals(class_1802.field_8551) && structure.vec().method_1025(center) < 2.25 && structure.time() > now);
        if (!duplicate) {
            this.addStructure(class_1802.field_8551, center, now + duration);
        }
    }

    private void replacePlastStructure(class_243 source, class_243 center, long duration, long startedAt) {
        this.structures.removeIf(structure -> structure.item().equals(class_1802.field_8551) && structure.vec().method_1025(source) < 2.25);
        this.structures.add(new Structure(class_1802.field_8551, center, startedAt + duration, ServerUtil.getAnarchy(), ServerUtil.getWorldType()));
    }

    private class_2338 averagePos(List<class_2338> positions) {
        long x = 0L;
        long y = 0L;
        long z = 0L;
        for (class_2338 pos : positions) {
            x += (long)pos.method_10263();
            y += (long)pos.method_10264();
            z += (long)pos.method_10260();
        }
        int count = Math.max(1, positions.size());
        return new class_2338(Math.round((float)x / (float)count), Math.round((float)y / (float)count), Math.round((float)z / (float)count));
    }

    private void addStructure(class_1792 item, class_243 vec, long time) {
        this.structures.add(new Structure(item, vec, time, ServerUtil.getAnarchy(), ServerUtil.getWorldType()));
    }

    private void addEvent(String name, String lvl, String owner, class_243 vec, String world, int timeOpen, int timeEnd) {
        long now = System.currentTimeMillis();
        long openAt = now + (long)timeOpen * 1000L;
        long endAt = openAt + (long)Math.max(0, timeEnd) * 1000L;
        this.serverEvents.removeIf(event -> event.name.equalsIgnoreCase(name) && event.anarchy == ServerUtil.getAnarchy() && event.world.equals(world));
        this.serverEvents.add(new ServerEvent(name, null, owner, vec, world, ServerUtil.getAnarchy(), openAt, endAt));
        if (mc.method_1562() != null && mc.method_1562().method_45734() != null) {
            Main.getInstance().getWayRepository().addTemporaryWay(name, class_2338.method_49638((class_2374)vec), ServerHelper.mc.method_1562().method_45734().field_3761);
        }
        this.requestEventDelay();
    }

    private Vector4f getRound(FontRenderer font, List<String> list, int i, float width) {
        return new Vector4f(2.0f, 2.0f, 2.0f, 2.0f);
    }

    public class_1735 findSlot(KeyBind bind) {
        if (bind == null) {
            return null;
        }
        return PlayerInventoryUtil.getSlot(bind.item, slot -> !(!(slot.field_7871 instanceof class_1661) || bind.item == class_1802.field_8551 && !this.isFunTimePlast(slot.method_7677()) || bind.donationPotionName != null && !this.matchesDonationPotion(bind.donationPotionName, slot.method_7677())));
    }

    private class_1735 findFunTimePlastSlot() {
        return PlayerInventoryUtil.getSlot(class_1802.field_8551, slot -> slot.field_7871 instanceof class_1661 && this.isFunTimePlast(slot.method_7677()));
    }

    private boolean isFunTimePlast(class_1799 stack) {
        if (!stack.method_31574(class_1802.field_8551) || !stack.method_7964().getString().contains("\u041f\u043b\u0430\u0441\u0442")) {
            return false;
        }
        class_9290 lore = (class_9290)stack.method_58694(class_9334.field_49632);
        return lore != null && lore.comp_2400().stream().map(class_2561::getString).anyMatch(line -> line.contains("\u041a\u0430\u0441\u0442: \u041d\u0435\u0440\u0443\u0448\u0438\u043c\u0430\u044f \u0441\u0442\u0435\u043d\u0430"));
    }

    private boolean matchesDonationPotion(String expectedName, class_1799 stack) {
        String expected;
        String keyword;
        String display = stack.method_7964().getString().toLowerCase(Locale.ROOT);
        if (display.contains(keyword = (expected = expectedName.toLowerCase(Locale.ROOT)).replace("\u0437\u0435\u043b\u044c\u0435 ", "").replace("\u0441\u0432\u044f\u0442\u0430\u044f ", "").replace("\u0445\u043b\u043e\u043f\u0443\u0448\u043a\u0430", "\u0445\u043b\u043e\u043f\u0443\u0448").replace("\u0441\u043d\u043e\u0442\u0432\u043e\u0440\u043d\u043e\u0435", "\u0441\u043d\u043e\u0442\u0432\u043e\u0440").replace("\u0430\u0441\u0441\u0430\u0441\u0438\u043d\u0430", "\u0430\u0441\u0441\u0430\u0441\u0438\u043d"))) {
            return true;
        }
        String resolved = PotionTrackerUtil.resolveDonationPotionNameStrict(stack);
        if (expectedName.equals(resolved)) {
            return true;
        }
        if (resolved == null) {
            return false;
        }
        try {
            String repaired = new String(resolved.getBytes("windows-1251"), StandardCharsets.UTF_8);
            return expectedName.equals(repaired);
        }
        catch (UnsupportedEncodingException ignored) {
            return false;
        }
    }

    public List<KeyBind> getKeyBindings() {
        return Collections.unmodifiableList(this.keyBindings);
    }

    public List<String> getAutoPointLines(String name) {
        long now = System.currentTimeMillis();
        return this.serverEvents.stream().filter(event -> event.name.equalsIgnoreCase(name)).findFirst().map(event -> {
            ArrayList<String> lines = new ArrayList<String>();
            if (event.owner != null && !event.owner.isBlank()) {
                lines.add((String)((Object)("\u041f\u0440\u0438\u0437\u0432\u0430\u043d: " + event.owner)));
            }
            return lines;
        }).orElseGet(ArrayList::new);
    }

    public String getAutoPointTime(String name) {
        long now = System.currentTimeMillis();
        return this.serverEvents.stream().filter(event -> event.name.equalsIgnoreCase(name)).findFirst().map(event -> {
            double openSeconds = (double)(event.timeOpen - now) / 1000.0;
            double endSeconds = (double)(event.timeEnd - now) / 1000.0;
            if (openSeconds > 0.0) {
                return this.formatAutoPointTime(openSeconds);
            }
            if (endSeconds > 0.0) {
                return this.formatAutoPointTime(endSeconds);
            }
            return null;
        }).orElse(null);
    }

    private String formatAutoPointTime(double seconds) {
        if (seconds >= 60.0) {
            int total = Math.max(0, (int)Math.ceil(seconds));
            return total / 60 + ":" + String.format(Locale.ROOT, "%02d", total % 60);
        }
        return String.valueOf(MathUtil.round(seconds, seconds < 30.0 ? (double)0.1f : 1.0)).replace(".0", "") + "\u0441";
    }

    private void cleanupExpiredAutoPoints() {
        long now = System.currentTimeMillis();
        Iterator<ServerEvent> iterator2 = this.serverEvents.iterator();
        while (iterator2.hasNext()) {
            ServerEvent event = iterator2.next();
            if (event.timeEnd + 90000L > now) continue;
            iterator2.remove();
            Main.getInstance().getWayRepository().deleteTemporaryWay(event.name);
        }
    }

    public int clearAutoPoints() {
        int count = this.serverEvents.size();
        this.serverEvents.clear();
        Main.getInstance().getWayRepository().clearTemporaryWays();
        return count;
    }

    public record KeyBind(class_1792 item, BindSetting setting, float distance, String donationPotionName) {
        public KeyBind(class_1792 item, BindSetting setting, float distance) {
            this(item, setting, distance, null);
        }
    }

    private static enum ActionState {
        IDLE,
        SLOWING_DOWN,
        WAITING_STOP,
        SWAP_TO_ITEM,
        USE_ITEM,
        SWAP_BACK,
        SPEEDING_UP;

    }

    private static enum DebuffStage {
        IDLE,
        SLEEPING,
        AURA,
        ASSASSIN;

    }

    private record PendingStructureSound(class_243 pos, long time) {
    }

    public record ServerEvent(String name, String lvl, String owner, class_243 vec, String world, int anarchy, long timeOpen, long timeEnd) {
    }

    private record PlastShape(class_243 center, long duration) {
    }

    public record Structure(class_1792 item, class_243 vec, long time, int anarchy, String world) {
    }
}
