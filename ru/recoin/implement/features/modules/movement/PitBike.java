/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1132
 *  net.minecraft.class_1297
 *  net.minecraft.class_1937
 *  net.minecraft.class_2561
 *  net.minecraft.class_3218
 *  net.minecraft.class_3222
 *  net.minecraft.class_3532
 *  net.minecraft.server.MinecraftServer
 */
package ru.recoin.implement.features.modules.movement;

import java.util.UUID;
import net.minecraft.class_1132;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_2561;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_3532;
import net.minecraft.server.MinecraftServer;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BindSetting;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.other.StringUtil;
import ru.recoin.implement.events.keyboard.KeyEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.events.render.DrawEvent;
import ru.recoin.implement.events.render.WorldRenderEvent;
import ru.recoin.implement.features.draggables.Notifications;
import ru.recoin.implement.features.modules.movement.pitbike.PitBikeEffects;
import ru.recoin.implement.features.modules.movement.pitbike.PitBikeEntity;
import ru.recoin.implement.features.modules.movement.pitbike.PitBikeRegistry;

public final class PitBike
extends Module {
    private final BindSetting wheelieBind = new BindSetting("Wheelie Bind", "Hold to ride on the rear wheel").setKey(82);
    private final ValueSetting topSpeed = new ValueSetting("Bike Top Speed", "Maximum forward speed in blocks per tick").setValue(0.75f).range(0.2f, 1.2f).step(0.05f);
    private final ValueSetting acceleration = new ValueSetting("Bike Acceleration", "Throttle acceleration per tick").setValue(0.018f).range(0.005f, 0.04f).step(0.001f);
    private final BooleanSetting manualGears = new BooleanSetting("Bike Manual Gears", "N and four forward gears; S brakes").setValue(true);
    private final BindSetting gearUp = new BindSetting("Bike Gear Up", "Shift up one gear").setKey(88);
    private final BindSetting gearDown = new BindSetting("Bike Gear Down", "Shift down one gear").setKey(90);
    private final BooleanSetting dashboard = new BooleanSetting("Bike Dashboard", "Speed, engine RPM and selected gear").setValue(true);
    private final BooleanSetting particles = new BooleanSetting("Bike Particles", "Dust on land and splashes in water").setValue(true);
    private final BooleanSetting tireTracks = new BooleanSetting("Bike Tire Tracks", "Short-lived tire marks on solid ground").setValue(true);
    private final BooleanSetting handlebarView = new BooleanSetting("Bike Handlebar View", "First-person camera behind the handlebars with hands on the grips").setValue(true);
    private final ValueSetting cameraShake = new ValueSetting("Bike Camera Shake", "Subtle first-person vibration; zero disables vibration").setValue(0.35f).range(0, 1).step(0.05f);
    private final transient PitBikeEffects effects = new PitBikeEffects();
    private volatile transient UUID bikeId;
    private transient MinecraftServer bikeServer;
    private transient UUID controlledBike;
    private transient boolean lastWheelie;
    private transient boolean lastManual;
    private transient float lastTopSpeed = -1.0f;
    private transient float lastAcceleration = -1.0f;
    private volatile transient int generation;

    public PitBike() {
        super("PITBIKE", "Pitbike", ModuleCategory.MOVEMENT);
        this.setup(this.wheelieBind, this.topSpeed, this.acceleration, this.manualGears, this.gearUp, this.gearDown, this.dashboard, this.particles, this.tireTracks, this.handlebarView, this.cameraShake);
    }

    public static PitBike getInstance() {
        return Instance.get(PitBike.class);
    }

    public boolean isHandlebarViewActive() {
        return this.isState() && this.handlebarView.isValue() && PitBike.mc.field_1690.method_31044().method_31034() && PitBike.mc.field_1724 != null && mc.method_1560() == PitBike.mc.field_1724 && PitBike.mc.field_1724.method_5854() instanceof PitBikeEntity;
    }

    public float getCameraShake() {
        return this.cameraShake.getValue();
    }

    @EventHandler
    public void onKey(KeyEvent event) {
        PitBikeEntity bike;
        class_1297 class_12972;
        if (!(this.manualGears.isValue() && PitBike.mc.field_1724 != null && this.bikeServer != null && mc.method_1576() == this.bikeServer && (class_12972 = PitBike.mc.field_1724.method_5854()) instanceof PitBikeEntity && (bike = (PitBikeEntity)class_12972).canShift())) {
            return;
        }
        int change = event.isKeyDown(this.gearUp.getKey()) ? 1 : (event.isKeyDown(this.gearDown.getKey()) ? -1 : 0);
        if (change == 0) {
            return;
        }
        int selected = class_3532.method_15340((int)(bike.getGear() + change), (int)0, (int)4);
        bike.setGear(selected);
        UUID playerId = PitBike.mc.field_1724.method_5667();
        UUID vehicleId = bike.method_5667();
        int controlGeneration = this.generation;
        MinecraftServer server = this.bikeServer;
        server.execute(() -> {
            PitBikeEntity vehicle;
            class_1297 patt0$temp;
            if (!this.isState() || this.generation != controlGeneration) {
                return;
            }
            class_3222 player = server.method_3760().method_14602(playerId);
            if (player != null && (patt0$temp = player.method_5854()) instanceof PitBikeEntity && (vehicle = (PitBikeEntity)patt0$temp).method_5667().equals(vehicleId) && vehicle.canShift()) {
                vehicle.setGear(selected);
            }
        });
    }

    @EventHandler
    public void onDraw(DrawEvent event) {
        class_1297 class_12972;
        if (this.dashboard.isValue() && PitBike.mc.field_1724 != null && (class_12972 = PitBike.mc.field_1724.method_5854()) instanceof PitBikeEntity) {
            PitBikeEntity bike = (PitBikeEntity)class_12972;
            this.effects.drawDashboard(event, bike, this.manualGears.isValue(), StringUtil.getBindName(this.gearDown.getKey()), StringUtil.getBindName(this.gearUp.getKey()));
        }
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        if (this.tireTracks.isValue()) {
            this.effects.renderTracks(event);
        }
    }

    @Override
    public void activate() {
        class_1132 server = mc.method_1576();
        if (!mc.method_1542() || server == null || PitBike.mc.field_1724 == null) {
            Notifications.getInstance().addIconList((class_2561)class_2561.method_43470((String)"PITBIKE \u0440\u0430\u0431\u043e\u0442\u0430\u0435\u0442 \u0442\u043e\u043b\u044c\u043a\u043e \u0432 \u043e\u0434\u0438\u043d\u043e\u0447\u043d\u043e\u043c \u043c\u0438\u0440\u0435"), 3000L, "\ue07e", -45737);
            mc.execute(() -> this.setState(false));
            return;
        }
        if (this.bikeServer != server) {
            this.bikeId = null;
        }
        this.bikeServer = server;
        int activation = ++this.generation;
        UUID parkedBike = this.bikeId;
        UUID playerId = PitBike.mc.field_1724.method_5667();
        server.execute(() -> this.lambda$activate$4(activation, (MinecraftServer)server, playerId, parkedBike));
    }

    @EventHandler
    public void onTick(TickEvent event) {
        PitBikeEntity bike;
        class_1297 class_12972;
        PitBikeEntity ridden = PitBike.mc.field_1724 != null && (class_12972 = PitBike.mc.field_1724.method_5854()) instanceof PitBikeEntity ? (bike = (PitBikeEntity)class_12972) : null;
        this.effects.tick(ridden, this.particles.isValue(), this.tireTracks.isValue());
        if (PitBike.mc.field_1724 == null || mc.method_1576() != this.bikeServer) {
            this.controlledBike = null;
            this.lastWheelie = false;
            return;
        }
        class_12972 = PitBike.mc.field_1724.method_5854();
        if (!(class_12972 instanceof PitBikeEntity)) {
            this.releaseControls();
            return;
        }
        bike = (PitBikeEntity)class_12972;
        boolean wheelie = PlayerIntersectionUtil.isKey(this.wheelieBind);
        float speed = this.topSpeed.getValue();
        float throttle = this.acceleration.getValue();
        boolean manual = this.manualGears.isValue();
        bike.setManualTransmission(manual);
        bike.setDriveSettings(speed, throttle);
        bike.setWheelieRequested(wheelie);
        if (bike.method_5667().equals(this.controlledBike) && this.lastWheelie == wheelie && this.lastTopSpeed == speed && this.lastAcceleration == throttle && this.lastManual == manual) {
            return;
        }
        this.bikeId = this.controlledBike = bike.method_5667();
        this.lastWheelie = wheelie;
        this.lastTopSpeed = speed;
        this.lastAcceleration = throttle;
        this.lastManual = manual;
        UUID vehicleId = this.controlledBike;
        UUID playerId = PitBike.mc.field_1724.method_5667();
        MinecraftServer server = this.bikeServer;
        int controlGeneration = this.generation;
        server.execute(() -> {
            PitBikeEntity vehicle;
            class_1297 patt0$temp;
            if (!this.isState() || this.generation != controlGeneration) {
                return;
            }
            class_3222 player = server.method_3760().method_14602(playerId);
            if (player != null && (patt0$temp = player.method_5854()) instanceof PitBikeEntity && (vehicle = (PitBikeEntity)patt0$temp).method_5667().equals(vehicleId)) {
                vehicle.setDriveSettings(speed, throttle);
                vehicle.setManualTransmission(manual);
                vehicle.setWheelieRequested(wheelie);
            }
        });
    }

    @Override
    public void deactivate() {
        class_1297 class_12972;
        ++this.generation;
        this.effects.clear();
        if (PitBike.mc.field_1724 != null && (class_12972 = PitBike.mc.field_1724.method_5854()) instanceof PitBikeEntity) {
            PitBikeEntity bike = (PitBikeEntity)class_12972;
            bike.setWheelieRequested(false);
        }
        this.releaseControls();
    }

    private void releaseControls() {
        UUID id = this.controlledBike != null ? this.controlledBike : this.bikeId;
        this.controlledBike = null;
        this.bikeId = null;
        this.lastWheelie = false;
        this.lastAcceleration = -1.0f;
        this.lastTopSpeed = -1.0f;
        MinecraftServer server = this.bikeServer;
        if (id == null || server == null || mc.method_1576() != server) {
            return;
        }
        server.execute(() -> {
            for (class_3218 world : server.method_3738()) {
                class_1297 patt0$temp = world.method_66347(id);
                if (!(patt0$temp instanceof PitBikeEntity)) continue;
                PitBikeEntity bike = (PitBikeEntity)patt0$temp;
                if (bike.method_5782()) {
                    bike.method_5772();
                }
                bike.setWheelieRequested(false);
                bike.method_31472();
                break;
            }
        });
    }

    private /* synthetic */ void lambda$activate$4(int activation, MinecraftServer server, UUID playerId, UUID parkedBike) {
        PitBikeEntity parked;
        class_1297 patt0$temp;
        if (!this.isState() || this.generation != activation) {
            return;
        }
        class_3222 player = server.method_3760().method_14602(playerId);
        if (player == null || player.method_5765()) {
            mc.execute(() -> this.setState(false));
            return;
        }
        class_3218 world = player.method_51469();
        if (parkedBike != null && (patt0$temp = world.method_66347(parkedBike)) instanceof PitBikeEntity && !(parked = (PitBikeEntity)patt0$temp).method_31481() && !parked.method_5782()) {
            player.method_5804((class_1297)parked);
            return;
        }
        PitBikeEntity bike = new PitBikeEntity(PitBikeRegistry.TYPE, (class_1937)world);
        bike.method_5814(player.method_23317(), player.method_23318(), player.method_23321());
        bike.method_36456(player.method_36454());
        if (world.method_8649((class_1297)bike)) {
            this.bikeId = bike.method_5667();
            player.method_5804((class_1297)bike);
        } else {
            mc.execute(() -> this.setState(false));
        }
    }
}

