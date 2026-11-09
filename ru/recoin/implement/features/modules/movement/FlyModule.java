package ru.recoin.implement.features.modules.movement;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.class_10185;
import net.minecraft.class_243;
import net.minecraft.class_746;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.network.PacketTiming;
import ru.recoin.common.util.other.Instance;
import ru.recoin.implement.events.player.MotionEvent;
import ru.recoin.implement.events.player.MoveEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.features.modules.combat.Aura;
import ru.recoin.implement.features.modules.combat.KillAuraModule;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;

/**
 * Packet based flight.
 *
 * <p>The module never touches vanilla abilities and never teleports. Every tick it replaces the
 * movement vector that {@code Entity.move} receives, so the server only sees ordinary movement
 * packets inside the Grim thresholds:
 *
 * <ul>
 *   <li>horizontal 0.28 .. 0.34 blocks/tick</li>
 *   <li>vertical 0.35 .. 0.42 blocks/tick</li>
 *   <li>combined vector kept below 0.49 blocks/tick (Position check)</li>
 *   <li>the on-ground flag is only spoofed on every 3rd/4th airborne packet (FlyB)</li>
 * </ul>
 *
 * <p>A local anomaly counter watches our own deltas. When it grows too large the module drops into
 * a 40 tick slow-fall fallback instead of pushing a value that a real fly check would flag.
 */
public class FlyModule extends Module {

    private static final class_243 ZERO = new class_243(0.0, 0.0, 0.0);

    private final ValueSetting horizontal = new ValueSetting("Horizontal", "Horizontal flight speed in blocks per tick")
            .setValue(0.31f).range(0.28f, 0.34f).step(0.005f);
    private final ValueSetting vertical = new ValueSetting("Vertical", "Vertical flight speed in blocks per tick")
            .setValue(0.38f).range(0.35f, 0.42f).step(0.005f);
    private final BooleanSetting jitter = new BooleanSetting("Jitter", "Randomize speed and packet rotation")
            .setValue(true);
    private final BooleanSetting fallback = new BooleanSetting("Slow Fall", "Use the slow-fall fallback when the anomaly counter is high")
            .setValue(true);

    private final Angle[] jitterAngles = {new Angle(0.0f, 0.0f), new Angle(0.0f, 0.0f)};
    private int jitterIndex;
    private int anomalyScore;
    private int cleanTicks;
    private int slowFallTicks;
    private int groundCounter;
    private double lastY = Double.NaN;

    public static FlyModule getInstance() {
        return Instance.get(FlyModule.class);
    }

    public FlyModule() {
        super("Fly", "Fly", ModuleCategory.MOVEMENT);
        this.setup(this.horizontal, this.vertical, this.jitter, this.fallback);
    }

    @Override
    public void activate() {
        this.anomalyScore = 0;
        this.cleanTicks = 0;
        this.slowFallTicks = 0;
        this.groundCounter = 0;
        this.lastY = Double.NaN;
        this.jitterIndex = 0;
        PacketTiming.setEnabled(true);
    }

    @Override
    public void deactivate() {
        PacketTiming.setEnabled(false);
        this.anomalyScore = 0;
        this.cleanTicks = 0;
        this.slowFallTicks = 0;
        this.groundCounter = 0;
        this.lastY = Double.NaN;
        if (mc.field_1724 != null) {
            mc.field_1724.method_18800(0.0, 0.0, 0.0);
        }
        super.deactivate();
    }

    /**
     * Called from {@code FlyMixin} at the head of {@code ClientPlayerEntity.tick()}.
     */
    public void onClientTick() {
        class_746 player = mc.field_1724;
        if (player == null || mc.field_1687 == null || !this.isState()) {
            return;
        }
        if (!PacketTiming.isEnabled()) {
            PacketTiming.setEnabled(true);
        }
        if (this.slowFallTicks > 0) {
            if (--this.slowFallTicks == 0) {
                this.anomalyScore = Math.min(this.anomalyScore, 40);
                this.lastY = Double.NaN;
            }
            return;
        }
        // Remove momentum so gravity and knockback cannot accumulate while we drive the move vector.
        player.method_18800(0.0, 0.0, 0.0);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        class_746 player = mc.field_1724;
        if (player == null || !this.isState() || this.slowFallTicks > 0) {
            return;
        }
        double y = player.method_23318();
        if (!Double.isNaN(this.lastY)) {
            double delta = y - this.lastY;
            if (delta > 0.42 || delta < -0.51) {
                this.addAnomaly(3);
            } else {
                this.decayAnomaly();
            }
        }
        this.lastY = y;
        if (this.anomalyScore > 100) {
            this.anomalyScore = 0;
        }
        if (this.anomalyScore > 70 && this.fallback.isValue()) {
            this.slowFallTicks = 40;
            this.anomalyScore = Math.min(this.anomalyScore, 60);
        }
    }

    @EventHandler
    public void onMove(MoveEvent event) {
        class_746 player = mc.field_1724;
        if (player == null || !this.isState()) {
            return;
        }
        class_243 movement = event.getMovement();
        if (this.slowFallTicks > 0) {
            if (this.fallback.isValue()) {
                double y = Math.max(movement.field_1351, -0.12);
                event.setMovement(new class_243(movement.field_1352 * 0.6, y, movement.field_1350 * 0.6));
            }
            return;
        }

        class_10185 input = player.field_3913 == null ? null : player.field_3913.field_54155;
        double forward = 0.0;
        double strafe = 0.0;
        double up = 0.0;
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
                up += 1.0;
            }
            if (input.comp_3164()) {
                up -= 1.0;
            }
        }

        double h = this.horizontal.getValue();
        if (this.jitter.isValue()) {
            h += ThreadLocalRandom.current().nextDouble(-0.02, 0.02);
        }
        h = Math.clamp(h, 0.28, 0.34);

        double v = this.vertical.getValue();
        if (this.jitter.isValue()) {
            v += ThreadLocalRandom.current().nextDouble(-0.015, 0.015);
        }
        v = Math.clamp(v, 0.35, 0.42);

        double dy = up * v;
        if (up != 0.0) {
            // Keep the total movement under Grim's 0.5 position delta.
            double maxHorizontal = Math.sqrt(Math.max(0.0, 0.49 * 0.49 - dy * dy));
            if (h > maxHorizontal) {
                h = maxHorizontal;
            }
        }

        double yaw = Math.toRadians(player.method_36454() + 90.0f);
        double sin = Math.sin(yaw);
        double cos = Math.cos(yaw);
        double dx = forward * h * cos + strafe * h * sin;
        double dz = forward * h * sin - strafe * h * cos;

        event.setMovement(new class_243(dx, dy, dz));
    }

    @EventHandler
    public void onMotion(MotionEvent event) {
        class_746 player = mc.field_1724;
        if (player == null || !this.isState()) {
            return;
        }
        if (this.slowFallTicks > 0) {
            event.setOnGround(false);
            return;
        }

        if (player.method_24828()) {
            this.groundCounter = 0;
        } else if (++this.groundCounter >= 3 + ThreadLocalRandom.current().nextInt(2)) {
            this.groundCounter = 0;
            event.setOnGround(true);
        } else {
            event.setOnGround(false);
        }

        if (this.jitter.isValue()) {
            Aura aura = Aura.getInstance();
            KillAuraModule killAura = KillAuraModule.getInstance();
            if ((aura == null || !aura.isState()) && (killAura == null || !killAura.isState())) {
                float j = (float) (ThreadLocalRandom.current().nextDouble(-0.015, 0.015) * 57.29577951308232);
                Angle angle = this.jitterAngles[this.jitterIndex];
                this.jitterIndex ^= 1;
                angle.setYaw(event.getYaw() + j);
                angle.setPitch(Math.clamp(event.getPitch() + j, -89.5f, 89.5f));
                RotationController.INSTANCE.setRotation(angle);
                event.setPitch(angle.getPitch());
            }
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
}
