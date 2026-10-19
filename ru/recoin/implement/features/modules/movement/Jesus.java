/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1041
 *  net.minecraft.class_1291
 *  net.minecraft.class_1293
 *  net.minecraft.class_1294
 *  net.minecraft.class_1304
 *  net.minecraft.class_1309
 *  net.minecraft.class_1322$class_1323
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_2246
 *  net.minecraft.class_243
 *  net.minecraft.class_2487
 *  net.minecraft.class_2520
 *  net.minecraft.class_2708
 *  net.minecraft.class_3675
 *  net.minecraft.class_3675$class_306
 *  net.minecraft.class_3675$class_307
 *  net.minecraft.class_6880
 *  net.minecraft.class_9279
 *  net.minecraft.class_9285
 *  net.minecraft.class_9285$class_9287
 *  net.minecraft.class_9334
 *  org.lwjgl.glfw.GLFW
 */
package ru.recoin.implement.features.modules.movement;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.class_1041;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1304;
import net.minecraft.class_1309;
import net.minecraft.class_1322;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_243;
import net.minecraft.class_2487;
import net.minecraft.class_2520;
import net.minecraft.class_2708;
import net.minecraft.class_3675;
import net.minecraft.class_6880;
import net.minecraft.class_9279;
import net.minecraft.class_9285;
import net.minecraft.class_9334;
import org.lwjgl.glfw.GLFW;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.MultiSelectSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.entity.MovingUtil;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.events.player.MotionEvent;
import ru.recoin.implement.events.player.MoveEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.events.render.WorldLoadEvent;
import ru.recoin.implement.features.modules.combat.Aura;

public class Jesus
extends Module {
    private final SelectSetting mode = new SelectSetting("Mode", "\u0425\u043e\u0434\u044c\u0431\u0430 \u043f\u043e \u0432\u043e\u0434\u0435").value("MetaHvH");
    private final MultiSelectSetting additions = new MultiSelectSetting("\u0414\u043e\u043f\u043e\u043b\u043d\u0435\u043d\u0438\u044f", "\u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438 \u0431\u0443\u0441\u0442\u0430").value("\u0410\u0432\u0442\u043e \u0431\u0443\u0441\u0442");
    private final ValueSetting interval = new ValueSetting("\u0418\u043d\u0442\u0435\u0440\u0432\u0430\u043b \u0431\u0443\u0441\u0442\u043e\u0432", "\u0418\u043d\u0442\u0435\u0440\u0432\u0430\u043b \u0432 \u043c\u0438\u043b\u043b\u0438\u0441\u0435\u043a\u0443\u043d\u0434\u0430\u0445").setValue(1200.0f).range(800.0f, 2000.0f).step(50.0f).visible(this::autoBoost);
    private final ValueSetting strength = new ValueSetting("\u0421\u0438\u043b\u0430 \u0430\u0432\u0442\u043e \u0431\u0443\u0441\u0442\u0430", "\u0414\u043e\u0431\u0430\u0432\u043e\u0447\u043d\u0430\u044f \u0441\u043a\u043e\u0440\u043e\u0441\u0442\u044c").setValue(0.1f).range(0.05f, 1.5f).step(0.01f).visible(this::autoBoost);
    private final ValueSetting duration = new ValueSetting("\u0414\u043b\u0438\u0442\u0435\u043b\u044c\u043d\u043e\u0441\u0442\u044c \u0431\u0443\u0441\u0442\u0430", "\u0414\u043b\u0438\u0442\u0435\u043b\u044c\u043d\u043e\u0441\u0442\u044c \u0432 \u043c\u0438\u043b\u043b\u0438\u0441\u0435\u043a\u0443\u043d\u0434\u0430\u0445").setValue(300.0f).range(200.0f, 1000.0f).step(50.0f).visible(this::autoBoost);
    private final ValueSetting antiFlag = new ValueSetting("\u0410\u043d\u0442\u0438\u0444\u043b\u0430\u0433", "\u041f\u0430\u0443\u0437\u0430 \u043f\u043e\u0441\u043b\u0435 \u043a\u043e\u0440\u0440\u0435\u043a\u0446\u0438\u0438 \u043f\u043e\u0437\u0438\u0446\u0438\u0438").setValue(4000.0f).range(2000.0f, 15000.0f).step(500.0f).visible(this::autoBoost);
    private transient long lastBoost;
    private transient long boostUntil;
    private transient long lastCorrection;
    private transient long attributeCheck;
    private transient float boostAmount;
    private transient float offhandBonus;
    private transient float helmetPenalty;
    private transient boolean recoveryBoost;
    private transient Boolean savedJump;
    private transient int recoveryMoves;

    public Jesus() {
        super("Jesus", "Jesus", ModuleCategory.MOVEMENT);
        this.setup(this.mode, this.additions, this.strength, this.duration, this.interval, this.antiFlag);
    }

    private boolean autoBoost() {
        return this.additions.isSelected("\u0410\u0432\u0442\u043e \u0431\u0443\u0441\u0442");
    }

    private boolean dolphinsGrace() {
        return Jesus.mc.field_1724.method_6059(class_1294.field_5900);
    }

    private boolean activeInWater() {
        class_1293 slowness = Jesus.mc.field_1724.method_6112(class_1294.field_5909);
        int level = slowness == null ? 0 : slowness.method_5578() + 1;
        return Jesus.mc.field_1687.method_8320(Jesus.mc.field_1724.method_24515()).method_27852(class_2246.field_10382) && this.mode.isSelected("MetaHvH") && level < 4 && !Jesus.mc.field_1724.method_5681();
    }

    private boolean coolingDown(long now) {
        long delay = (long)this.antiFlag.getValue();
        return delay > 0L && now - this.lastCorrection < delay;
    }

    private void updateBoost() {
        if (!this.autoBoost() || this.dolphinsGrace()) {
            this.boostAmount = 0.0f;
        } else if (this.recoveryMoves <= 0) {
            long now = System.currentTimeMillis();
            if (this.coolingDown(now)) {
                this.boostAmount = 0.0f;
            } else {
                long delay;
                long l = delay = this.recoveryBoost ? 1500L : (long)this.interval.getValue();
                if (now - this.lastBoost >= delay) {
                    this.lastBoost = now;
                    this.boostUntil = now + (long)this.duration.getValue();
                    this.boostAmount = this.strength.getValue();
                    this.recoveryBoost = false;
                }
                if (this.boostUntil > 0L && now >= this.boostUntil) {
                    this.boostAmount = 0.0f;
                }
            }
        }
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (Jesus.mc.field_1724 != null && Jesus.mc.field_1687 != null) {
            if (!Jesus.mc.field_1724.method_6118(class_1304.field_6166).method_7960()) {
                this.applyWaterState();
            }
        } else {
            this.setJump(false);
        }
    }

    @EventHandler
    public void onMotion(MotionEvent event) {
        if (Jesus.mc.field_1724 != null && Jesus.mc.field_1687 != null) {
            this.applyWaterState();
        } else {
            this.setJump(false);
        }
    }

    private void applyWaterState() {
        boolean active = this.activeInWater();
        if (active) {
            this.updateBoost();
            class_243 velocity = Jesus.mc.field_1724.method_18798();
            Jesus.mc.field_1724.method_18800(velocity.field_1352, -0.01, velocity.field_1350);
        }
        this.setJump(active);
    }

    private void setJump(boolean active) {
        if (Jesus.mc.field_1690 == null) {
            this.savedJump = null;
        } else if (active) {
            if (this.savedJump == null) {
                this.savedJump = Jesus.mc.field_1690.field_1903.method_1434();
            }
            Jesus.mc.field_1690.field_1903.method_23481(true);
        } else if (this.savedJump != null) {
            class_3675.class_306 key = class_3675.method_15981((String)Jesus.mc.field_1690.field_1903.method_1428());
            boolean physical = key.method_1442() == class_3675.class_307.field_1672 ? GLFW.glfwGetMouseButton((long)mc.method_22683().method_4490(), (int)key.method_1444()) == 1 : key.method_1444() >= 0 && class_3675.method_15987((class_1041)mc.method_22683(), (int)key.method_1444());
            Jesus.mc.field_1690.field_1903.method_23481(this.savedJump != false || physical);
            this.savedJump = null;
        }
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        if (event.isReceive() && event.getPacket() instanceof class_2708) {
            mc.execute(() -> {
                if (!this.isState() || Jesus.mc.field_1724 == null) {
                    return;
                }
                this.lastCorrection = System.currentTimeMillis();
                this.boostAmount = 0.0f;
                this.recoveryMoves = 5;
                this.recoveryBoost = true;
            });
        }
    }

    @EventHandler
    public void onMove(MoveEvent event) {
        if (Jesus.mc.field_1724 == null || Jesus.mc.field_1687 == null || !this.activeInWater()) {
            return;
        }
        double speed = this.movementSpeed();
        if (speed == (double)0.3f && !this.dolphinsGrace()) {
            this.setMovement(event, 0.0, 0.0);
        }
        Aura aura = Aura.getInstance();
        if (Jesus.mc.field_1690.field_1894.method_1434() && aura != null && aura.getTarget() != null && aura.getAimMode().isSelected("Smooth")) {
            double[] direction = this.targetDirection(aura.getTarget());
            this.setMovement(event, direction[0] * speed, direction[1] * speed);
        } else if (!MovingUtil.hasPlayerMovement()) {
            this.setMovement(event, 0.0, 0.0);
        } else {
            float forward = Jesus.mc.field_1724.field_3913.method_3128().field_1342;
            float sideways = Jesus.mc.field_1724.field_3913.method_3128().field_1343;
            float yaw = Jesus.mc.field_1724.method_36454();
            if (forward < 0.0f) {
                yaw += 180.0f;
            }
            float scale = forward < 0.0f ? -0.5f : (forward > 0.0f ? 0.5f : 1.0f);
            if (sideways > 0.0f) {
                yaw -= 90.0f * scale;
            }
            if (sideways < 0.0f) {
                yaw += 90.0f * scale;
            }
            double radians = Math.toRadians(yaw);
            this.setMovement(event, -Math.sin(radians) * speed, Math.cos(radians) * speed);
        }
    }

    private double[] targetDirection(class_1309 target) {
        double[] dArray;
        double z;
        double yaw = Math.toRadians(target.method_36454());
        double x = target.method_23317() - Math.sin(yaw) * 0.1 - Jesus.mc.field_1724.method_23317();
        double length = Math.hypot(x, z = target.method_23321() + Math.cos(yaw) * 0.1 - Jesus.mc.field_1724.method_23321());
        if (length < 1.0E-4) {
            double[] dArray2 = new double[2];
            dArray2[0] = 0.0;
            dArray = dArray2;
            dArray2[1] = 0.0;
        } else {
            double[] dArray3 = new double[2];
            dArray3[0] = x / length;
            dArray = dArray3;
            dArray3[1] = z / length;
        }
        return dArray;
    }

    private void setMovement(MoveEvent event, double x, double z) {
        Jesus.mc.field_1724.method_18800(x, Jesus.mc.field_1724.method_18798().field_1351, z);
        event.setMovement(new class_243(x, event.getMovement().field_1351, z));
    }

    private double movementSpeed() {
        long now = System.currentTimeMillis();
        if (this.coolingDown(now)) {
            return this.baseSpeed();
        }
        if (this.recoveryMoves > 0) {
            --this.recoveryMoves;
            return 0.25;
        }
        this.updateBoost();
        if (this.dolphinsGrace()) {
            return this.baseSpeed();
        }
        this.updateAttributes(now);
        float base = 0.25f;
        if (now < this.boostUntil) {
            float boost = this.boostAmount;
            class_1293 speed = Jesus.mc.field_1724.method_6112(class_1294.field_5904);
            if (speed != null && speed.method_5578() >= 2) {
                boost *= 0.9445f;
            }
            boost *= 1.012f + this.offhandBonus * 0.595f;
            boost += boost * this.effectAmount((class_6880<class_1291>)class_1294.field_5904, 0.2f);
            boost -= boost * this.effectAmount((class_6880<class_1291>)class_1294.field_5909, 0.15f);
            boost -= boost * this.helmetPenalty * 1.25f;
            return base + boost;
        }
        return base;
    }

    private float baseSpeed() {
        if (this.dolphinsGrace()) {
            return 2.1f;
        }
        this.updateAttributes(System.currentTimeMillis());
        float speed = 0.36f;
        speed *= 1.0f + this.offhandBonus;
        speed += speed * this.effectAmount((class_6880<class_1291>)class_1294.field_5904, 0.2f);
        speed -= speed * this.effectAmount((class_6880<class_1291>)class_1294.field_5909, 0.15f);
        speed -= speed * this.helmetPenalty * 1.25f;
        if (this.boostAmount == 0.0f && this.specialHead(Jesus.mc.field_1724.method_6118(class_1304.field_6169))) {
            speed *= 0.8f;
        }
        return speed;
    }

    private float effectAmount(class_6880<class_1291> effect, float amount) {
        class_1293 instance = Jesus.mc.field_1724.method_6112(effect);
        return instance == null ? 0.0f : amount * (float)(instance.method_5578() + 1);
    }

    private void updateAttributes(long now) {
        if (now - this.attributeCheck < 30L) {
            return;
        }
        this.offhandBonus = 0.0f;
        for (AttributeModifier modifier : this.attributes(Jesus.mc.field_1724.method_6079())) {
            if (!modifier.matches("minecraft:generic.movement_speed", "minecraft:movement_speed", "generic.movement_speed")) continue;
            this.offhandBonus = switch (modifier.operation()) {
                case 0 -> (float)(modifier.amount() / 0.1);
                case 1, 2 -> (float)modifier.amount();
                default -> 0.0f;
            };
            break;
        }
        this.helmetPenalty = 0.0f;
        for (AttributeModifier modifier : this.attributes(Jesus.mc.field_1724.method_6118(class_1304.field_6169))) {
            if (!modifier.matches("minecraft:generic.movement_speed", "minecraft:movement_speed", "generic.movement_speed") || !(modifier.amount() < 0.0)) continue;
            this.helmetPenalty = (float)Math.abs(modifier.amount());
            break;
        }
        this.attributeCheck = now;
    }

    private List<AttributeModifier> attributes(class_1799 stack) {
        class_9279 custom;
        ArrayList<AttributeModifier> result = new ArrayList<AttributeModifier>();
        if (stack == null || stack.method_7960()) {
            return result;
        }
        class_9285 component = (class_9285)stack.method_58694(class_9334.field_49636);
        if (component != null) {
            for (class_9285.class_9287 entry : component.comp_2393()) {
                String name = entry.comp_2395().method_40230().map(key -> key.method_29177().toString()).orElse("");
                int operation = switch (entry.comp_2396().comp_2450()) {
                    default -> throw new MatchException(null, null);
                    case class_1322.class_1323.field_6328 -> 0;
                    case class_1322.class_1323.field_6330 -> 1;
                    case class_1322.class_1323.field_6331 -> 2;
                };
                result.add(new AttributeModifier(name, entry.comp_2396().comp_2449(), operation));
            }
        }
        if ((custom = (class_9279)stack.method_58694(class_9334.field_49628)) != null) {
            custom.method_57461().method_10554("AttributeModifiers").ifPresent(list -> {
                for (class_2520 element : list) {
                    if (!(element instanceof class_2487)) continue;
                    class_2487 compound = (class_2487)element;
                    result.add(new AttributeModifier(compound.method_10558("AttributeName").orElse(""), compound.method_10574("Amount").orElse(0.0), compound.method_10550("Operation").orElse(0)));
                }
            });
        }
        return result;
    }

    private boolean specialHead(class_1799 stack) {
        if (stack == null || stack.method_7960() || !stack.method_31574(class_1802.field_8575)) {
            return false;
        }
        boolean armor = false;
        boolean toughness = false;
        boolean damage = false;
        for (AttributeModifier modifier : this.attributes(stack)) {
            if (modifier.matches("minecraft:generic.armor", "minecraft:armor", "generic.armor") && modifier.amount() == 3.5) {
                armor = true;
            }
            if (modifier.matches("minecraft:generic.armor_toughness", "minecraft:armor_toughness", "generic.armor_toughness") && modifier.amount() == 3.0) {
                toughness = true;
            }
            if (!modifier.matches("minecraft:generic.attack_damage", "minecraft:attack_damage", "generic.attack_damage") || modifier.amount() != 3.0) continue;
            damage = true;
        }
        return armor && toughness && damage;
    }

    @Override
    public void deactivate() {
        this.setJump(false);
        this.boostAmount = 0.0f;
        this.attributeCheck = 0L;
        this.offhandBonus = 0.0f;
        this.helmetPenalty = 0.0f;
        super.deactivate();
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        this.deactivate();
        this.lastBoost = 0L;
        this.boostUntil = 0L;
        this.lastCorrection = 0L;
        this.recoveryBoost = false;
        this.recoveryMoves = 0;
    }

    private record AttributeModifier(String name, double amount, int operation) {
        boolean matches(String ... names) {
            String value = this.name == null ? "" : this.name.toLowerCase(Locale.ROOT);
            for (String candidate : names) {
                String normalized = candidate.toLowerCase(Locale.ROOT);
                if (!value.equals(normalized) && !value.endsWith(":" + normalized) && !value.endsWith("." + normalized)) continue;
                return true;
            }
            return false;
        }
    }
}

