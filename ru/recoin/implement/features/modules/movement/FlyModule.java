package ru.recoin.implement.features.modules.movement;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.class_10185;
import net.minecraft.class_1656;
import net.minecraft.class_243;
import net.minecraft.class_746;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.network.PacketTiming;
import ru.recoin.common.util.other.Instance;
import ru.recoin.implement.events.player.MotionEvent;
import ru.recoin.implement.events.player.MoveEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.features.modules.combat.Aura;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;

/**
 * Client side flight with a Grim / ApexTime safe-ish movement envelope.
 *
 * <p>The module keeps the vanilla client in the "abilities flying" state without sending an
 * abilities packet, so the local player never accumulates gravity, while the actual displacement
 * is written into the {@code Entity.move} vector. The server therefore only sees ordinary
 * {@code PlayerMoveC2SPacket}s:
 *
 * <ul>
 *   <li>horizontal 0.28 .. 0.34 blocks/tick (capped by the selected bypass)</li>
 *   <li>vertical 0.35 .. 0.42 blocks/tick (capped by the selected bypass)</li>
 *   <li>combined vector kept below 0.49 blocks/tick (Grim Position)</li>
 *   <li>on-ground flag spoofed on a preset dependent cadence (Grim FlyB / Sentinel ground)</li>
 *   <li>movement packets optionally delayed with small jitter (Grim timer / Sentinel micro-lag)</li>
 * </ul>
 *
 * <p>An anomaly counter compares the position delta of every tick with the vector the module
 * submitted. Server teleports (setbacks) are detected by their large delta and are ignored instead
 * of being counted as cheating, so a single correction can no longer push the module into the
 * fallback state. If the counter still grows, a short slow-fall phase is used instead of dropping
 * the player.
 */
public class FlyModule extends Module {

    private static final class_243 ZERO = new class_243(0.0, 0.0, 0.0);
    private static final double MAX_COMBINED = 0.49;
    private static final double SLOW_FALL_DY = -0.06;
    private static final int SLOW_FALL_TICKS = 20;

    private final SelectSetting preset = new SelectSetting("Bypass", "Anticheat profile (Grim, ApexTime)")
            .value("Default", "Grim", "ApexTime").selected("Grim");
    private final ValueSetting horizontal = new ValueSetting("Horizontal", "Horizontal flight speed in blocks per tick")
            .setValue(0.31f).range(0.28f, 0.34f).step(0.005f);
    private final ValueSetting vertical = new ValueSetting("Vertical", "Vertical flight speed in blocks per tick")
            .setValue(0.38f).range(0.35f, 0.42f).step(0.005f);
    private final BooleanSetting jitter = new BooleanSetting("Jitter", "Randomize speed, rotation and packet timing")
            .setValue(true);
    private final BooleanSetting fallback = new BooleanSetting("Slow Fall", "Use the slow-fall recovery when the anomaly counter is high")
            .setValue(true);

    private final Angle[] jitterAngles = {new Angle(0.0f, 0.0f), new Angle(0.0f, 0.0f)};

    private double capHorizontal = 0.34;
    private double capVertical = 0.42;
    private int spoofMin = 3;
    private int spoofExtra = 1;
    private double rotationJitter = 0.6;
    private int anomalyLimit = 70;
    private boolean useTiming = true;
    private long timingJitter = 4_000_000L;
    private int timingEvery = 8;
    private long timingBurst = 8_000_000L;

    private String lastPreset;
    private int jitterIndex;
    private int anomalyScore;
    private int cleanTicks;
    private int slowFallTicks;
    private int groundCounter;
    private int blockedTicks;
    private double lastY = Double.NaN;
    private double lastMoveY;
    private boolean hadAllowFlying;
    private boolean hadFlying;
    private boolean abilitiesCaptured;

    public static FlyModule getInstance() {
        return Instance.get(FlyModule.class);
    }

    public FlyModule() {
        super("Fly", "Fly", ModuleCategory.MOVEMENT);
        this.setup(this.preset, this.horizontal, this.vertical, this.jitter, this.fallback);
    }

    @Override
    public void activate() {
        this.resetState();
        this.lastPreset = null;
        class_746 player = mc.field_1724;
        if (player != null) {
            class_1656 abilities = player.method_31549();
            this.hadAllowFlying = abilities.field_7478;
            this.hadFlying = abilities.field_7479;
            this.abilitiesCaptured = true;
        }
        this.applyPreset();
        PacketTiming.setEnabled(this.useTiming);
    }

    @Override
    public void deactivate() {
        PacketTiming.setEnabled(false);
        this.resetState();
        this.lastPreset = null;
        class_746 player = mc.field_1724;
        if (player != null && !player.method_68878() && !player.method_7325()) {
            class_1656 abilities = player.method_31549();
            abilities.field_7479 = this.abilitiesCaptured && this.hadFlying;
            abilities.field_7478 = this.abilitiesCaptured && this.hadAllowFlying;
            player.method_18800(0.0, 0.0, 0.0);
        }
        this.abilitiesCaptured = false;
        if (RotationController.INSTANCE.getLastRotationProvider() == null) {
            RotationController.INSTANCE.setRotation(null);
        }
        super.deactivate();
    }

    private void resetState() {
        this.anomalyScore = 0;
        this.cleanTicks = 0;
        this.slowFallTicks = 0;
        this.groundCounter = 0;
        this.blockedTicks = 0;
        this.lastY = Double.NaN;
        this.lastMoveY = 0.0;
        this.jitterIndex = 0;
    }

    private void applyPreset() {
        String selected = this.preset.getSelected();
        if (selected == this.lastPreset) {
            return;
        }
        this.lastPreset = selected;
        if (selected.equals("Grim")) {
            this.capHorizontal = 0.30;
            this.capVertical = 0.36;
            this.spoofMin = 2;
            this.spoofExtra = 2;
            this.rotationJitter = 0.7;
            this.anomalyLimit = 85;
            this.useTiming = true;
            this.timingJitter = 4_000_000L;
            this.timingEvery = 8;
            this.timingBurst = 8_000_000L;
        } else if (selected.equals("ApexTime")) {
            this.capHorizontal = 0.32;
            this.capVertical = 0.38;
            this.spoofMin = 3;
            this.spoofExtra = 3;
            this.rotationJitter = 0.9;
            this.anomalyLimit = 95;
            this.useTiming = true;
            this.timingJitter = 6_000_000L;
            this.timingEvery = 12;
            this.timingBurst = 12_000_000L;
        } else {
            this.capHorizontal = 0.34;
            this.capVertical = 0.42;
            this.spoofMin = 4;
            this.spoofExtra = 3;
            this.rotationJitter = 0.35;
            this.anomalyLimit = 60;
            this.useTiming = false;
            this.timingJitter = 0L;
            this.timingEvery = 0;
            this.timingBurst = 0L;
        }
        PacketTiming.setProfile(this.timingJitter, this.timingEvery, this.timingBurst);
        if (this.isState()) {
            PacketTiming.setEnabled(this.useTiming);
        }
    }

    /**
     * Called from {@code FlyMixin} at the head of {@code ClientPlayerEntity.tick()}, before the
     * vanilla movement calculation runs.
     */
    public void onClientTick() {
        class_746 player = mc.field_1724;
        if (player == null || mc.field_1687 == null || !this.isState()) {
            return;
        }
        this.applyPreset();
        if (!this.useTiming) {
            if (PacketTiming.isEnabled()) {
                PacketTiming.setEnabled(false);
            }
        } else if (!PacketTiming.isEnabled()) {
            PacketTiming.setEnabled(true);
        }
        // Drop the gravity/knockback that was applied after the previous move. The movement vector
        // below is authoritative, so stale velocity must not leak into it.
        player.method_18800(0.0, 0.0, 0.0);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        class_746 player = mc.field_1724;
        if (player == null || !this.isState()) {
            return;
        }
        this.applyPreset();
        this.forceFlight(player);

        double y = player.method_23318();
        double expected = this.lastMoveY;
        this.lastMoveY = 0.0;
        if (!Double.isNaN(this.lastY) && this.slowFallTicks == 0) {
            double delta = y - this.lastY;
            double diff = delta - expected;
            if (diff < -1.25) {
                // A hard teleport / Grim setback. Do not count it as our own movement, otherwise a
                // single correction would keep the module in the recovery loop forever.
                this.anomalyScore = Math.max(0, this.anomalyScore - 8);
                this.blockedTicks = 0;
                this.cleanTicks = 0;
            } else if (diff > 0.25) {
                // We moved further than the submitted vector allows: a velocity leak.
                this.addAnomaly(2);
                this.blockedTicks = 0;
            } else if (diff < -0.22) {
                // Blocked by a block or pulled back by the server. One short tick is normal, a long
                // series is not, so accumulate slowly instead of triggering the fallback instantly.
                if (++this.blockedTicks >= 8) {
                    this.addAnomaly(1);
                    this.blockedTicks = 0;
                }
            } else {
                this.blockedTicks = 0;
                this.decayAnomaly();
            }
        }
        this.lastY = y;

        if (player.method_24828() && this.slowFallTicks == 0) {
            this.anomalyScore = 0;
            this.cleanTicks = 0;
            this.blockedTicks = 0;
        }
        if (this.anomalyScore >= this.anomalyLimit && this.fallback.isValue()) {
            this.slowFallTicks = SLOW_FALL_TICKS;
            this.anomalyScore = this.anomalyLimit / 2;
        }
        if (this.slowFallTicks > 0) {
            if (--this.slowFallTicks == 0) {
                this.anomalyScore = Math.min(this.anomalyScore, 20);
                this.lastY = Double.NaN;
            }
        }
    }

    @EventHandler
    public void onMove(MoveEvent event) {
        class_746 player = mc.field_1724;
        if (player == null || !this.isState()) {
            return;
        }
        this.applyPreset();
        this.forceFlight(player);

        double forward = 0.0;
        double strafe = 0.0;
        int up = 0;
        class_10185 input = player.field_3913 == null ? null : player.field_3913.field_54155;
        if (input != null) {
            if (input.comp_3159()) {
                forward += 1.0;
            }
            if (input.comp_3160()) {
                forward -= 1.0;
            }
            if (input.comp_3161()) {
                strafe += 1.0;
            }
            if (input.comp_3162()) {
                strafe -= 1.0;
            }
            if (input.comp_3163()) {
                up += 1;
            }
            if (input.comp_3164()) {
                up -= 1;
            }
        }

        double h = Math.min(this.horizontal.getValue(), this.capHorizontal);
        double v = Math.min(this.vertical.getValue(), this.capVertical);
        if (this.jitter.isValue()) {
            h += ThreadLocalRandom.current().nextDouble(-0.012, 0.012);
            v += ThreadLocalRandom.current().nextDouble(-0.010, 0.010);
        }
        h = Math.clamp(h, 0.24, this.capHorizontal);
        v = Math.clamp(v, 0.30, this.capVertical);

        if (this.slowFallTicks > 0) {
            double slowH = h * 0.45;
            this.applyDirection(event, player, forward, strafe, slowH, SLOW_FALL_DY);
            return;
        }

        double dy = up * v;
        double maxHorizontal = h;
        double combined = maxHorizontal * maxHorizontal + dy * dy;
        if (combined > MAX_COMBINED * MAX_COMBINED) {
            maxHorizontal = Math.sqrt(Math.max(0.0, MAX_COMBINED * MAX_COMBINED - dy * dy));
        }
        this.applyDirection(event, player, forward, strafe, maxHorizontal, dy);
    }

    private void applyDirection(MoveEvent event, class_746 player, double forward, double strafe, double speed, double dy) {
        double length = Math.sqrt(forward * forward + strafe * strafe);
        if (length > 1.0) {
            forward /= length;
            strafe /= length;
        }
        double yaw = Math.toRadians(player.method_36454() + 90.0f);
        double sin = Math.sin(yaw);
        double cos = Math.cos(yaw);
        double dx = forward * speed * cos + strafe * speed * sin;
        double dz = forward * speed * sin - strafe * speed * cos;
        this.lastMoveY = dy;
        event.setMovement(new class_243(dx, dy, dz));
    }

    @EventHandler
    public void onMotion(MotionEvent event) {
        class_746 player = mc.field_1724;
        if (player == null || !this.isState()) {
            return;
        }
        this.applyPreset();

        if (this.slowFallTicks > 0) {
            event.setOnGround(false);
            return;
        }

        if (player.method_24828()) {
            this.groundCounter = 0;
            event.setOnGround(true);
        } else {
            int interval = this.spoofMin + ThreadLocalRandom.current().nextInt(this.spoofExtra + 1);
            if (++this.groundCounter >= interval) {
                this.groundCounter = 0;
                event.setOnGround(true);
            } else {
                event.setOnGround(false);
            }
        }

        if (this.jitter.isValue() && this.rotationJitter > 0.0) {
            Aura aura = Aura.getInstance();
            Module provider = RotationController.INSTANCE.getLastRotationProvider();
            if ((aura == null || !aura.isState()) && (provider == null || provider == this)) {
                float j = (float) (ThreadLocalRandom.current().nextDouble(-this.rotationJitter, this.rotationJitter));
                Angle angle = this.jitterAngles[this.jitterIndex];
                this.jitterIndex ^= 1;
                angle.setYaw(event.getYaw() + j);
                angle.setPitch(Math.clamp(event.getPitch() + j, -89.5f, 89.5f));
                RotationController.INSTANCE.setRotation(angle);
                event.setYaw(angle.getYaw());
                event.setPitch(angle.getPitch());
            }
        }
    }

    private void forceFlight(class_746 player) {
        if (player.method_68878() || player.method_7325()) {
            return;
        }
        class_1656 abilities = player.method_31549();
        if (!abilities.field_7478) {
            abilities.field_7478 = true;
        }
        if (!abilities.field_7479) {
            abilities.field_7479 = true;
        }
    }

    private void addAnomaly(int amount) {
        this.anomalyScore += amount;
        if (this.anomalyScore > 100) {
            this.anomalyScore = 100;
        }
        this.cleanTicks = 0;
    }

    private void decayAnomaly() {
        if (this.anomalyScore > 0) {
            --this.anomalyScore;
        }
        if (++this.cleanTicks >= 30) {
            this.anomalyScore = 0;
            this.cleanTicks = 0;
        }
    }

    public static class_243 zero() {
        return ZERO;
    }

    public int getAnomalyScore() {
        return this.anomalyScore;
    }

    public int getSlowFallTicks() {
        return this.slowFallTicks;
    }

    public ValueSetting getHorizontal() {
        return this.horizontal;
    }

    public ValueSetting getVertical() {
        return this.vertical;
    }

    public SelectSetting getPreset() {
        return this.preset;
    }
}
