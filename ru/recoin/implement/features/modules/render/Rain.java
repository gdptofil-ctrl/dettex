/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_1922
 *  net.minecraft.class_2338
 *  net.minecraft.class_2338$class_2339
 *  net.minecraft.class_243
 *  net.minecraft.class_2680
 *  net.minecraft.class_287
 *  net.minecraft.class_289
 *  net.minecraft.class_290
 *  net.minecraft.class_2902$class_2903
 *  net.minecraft.class_3532
 *  net.minecraft.class_4184
 *  net.minecraft.class_4587
 *  net.minecraft.class_7833
 *  net.minecraft.class_9801
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 */
package ru.recoin.implement.features.modules.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.class_1922;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_2902;
import net.minecraft.class_3532;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_7833;
import net.minecraft.class_9801;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.ColorSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.render.LegacyRenderSystem;
import ru.recoin.compat.render.BufferRenderer;
import ru.recoin.compat.render.GlStateManager;
import ru.recoin.compat.render.ShaderProgramKeys;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.events.render.WorldRenderEvent;

public class Rain
extends Module {
    private final SelectSetting preset = new SelectSetting("Preset", "\u0422\u0438\u043f \u043e\u0441\u0430\u0434\u043a\u043e\u0432").value("Drizzle", "Rain", "Downpour", "Storm").selected("Rain");
    private final ValueSetting density = new ValueSetting("Density", "\u041f\u043b\u043e\u0442\u043d\u043e\u0441\u0442\u044c \u0434\u043e\u0436\u0434\u044f").range(0.1f, 3.0f).setValue(1.0f);
    private final ValueSetting radius = new ValueSetting("Radius", "\u0420\u0430\u0434\u0438\u0443\u0441 \u0437\u043e\u043d\u044b \u0434\u043e\u0436\u0434\u044f (\u0431\u043b\u043e\u043a\u0438)").range(8, 64).setValue(26.0f);
    private final ValueSetting altitude = new ValueSetting("Altitude", "\u0412\u044b\u0441\u043e\u0442\u0430 \u0441\u043f\u0430\u0432\u043d\u0430 \u043a\u0430\u043f\u0435\u043b\u044c").range(6, 40).setValue(16.0f);
    private final ValueSetting dropSize = new ValueSetting("Drop Size", "\u041c\u0430\u0441\u0448\u0442\u0430\u0431 \u043a\u0430\u043f\u043b\u0438").range(0.3f, 2.5f).setValue(1.0f);
    private final ValueSetting fallSpeed = new ValueSetting("Fall Speed", "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u043f\u0430\u0434\u0435\u043d\u0438\u044f").range(0.3f, 2.5f).setValue(1.0f);
    private final ValueSetting wind = new ValueSetting("Wind", "\u0421\u0438\u043b\u0430 \u0432\u0435\u0442\u0440\u0430").range(0.0f, 3.0f).setValue(1.0f);
    private final ValueSetting opacity = new ValueSetting("Opacity", "\u0412\u0438\u0434\u0438\u043c\u043e\u0441\u0442\u044c \u043a\u0430\u043f\u0435\u043b\u044c").range(0.1f, 2.0f).setValue(1.0f);
    private final BooleanSetting splashes = new BooleanSetting("Splashes", "\u0412\u0441\u043f\u043b\u0435\u0441\u043a\u0438 \u0438 \u0440\u044f\u0431\u044c \u043d\u0430 \u043c\u0435\u0441\u0442\u0435 \u0443\u0434\u0430\u0440\u0430").setValue(true);
    private final BooleanSetting droplets = new BooleanSetting("Splash Droplets", "\u0420\u0430\u0437\u043b\u0435\u0442\u0430\u044e\u0449\u0438\u0435\u0441\u044f \u0431\u0440\u044b\u0437\u0433\u0438").setValue(true).visible(this.splashes::isValue);
    private final BooleanSetting groundMist = new BooleanSetting("Ground Mist", "\u041f\u0440\u0438\u0437\u0435\u043c\u043d\u0430\u044f \u0434\u044b\u043c\u043a\u0430").setValue(true);
    private final BooleanSetting lightning = new BooleanSetting("Lightning", "\u0412\u0441\u043f\u044b\u0448\u043a\u0438 \u0433\u0440\u043e\u0437\u044b").setValue(false);
    private final BooleanSetting skyCheck = new BooleanSetting("Sky Check", "\u0414\u043e\u0436\u0434\u044c \u043d\u0435 \u0438\u0434\u0451\u0442 \u043f\u043e\u0434 \u043a\u0440\u044b\u0448\u0435\u0439").setValue(true);
    private final BooleanSetting onlyWhenRaining = new BooleanSetting("Only When Raining", "\u0422\u043e\u043b\u044c\u043a\u043e \u043f\u0440\u0438 \u0434\u043e\u0436\u0434\u0435 \u0432 \u043c\u0438\u0440\u0435").setValue(false);
    private final SelectSetting colorMode = new SelectSetting("Color Mode", "\u0420\u0435\u0436\u0438\u043c \u0446\u0432\u0435\u0442\u0430").value("Realistic", "Client", "Custom").selected("Realistic");
    private final ColorSetting customColor = new ColorSetting("Color", "\u0426\u0432\u0435\u0442 \u043a\u0430\u043f\u0435\u043b\u044c").value(-4204314).visible(() -> this.colorMode.isSelected("Custom"));
    private static final double GRAVITY = 22.0;
    private static final double TERMINAL_VELOCITY = 34.0;
    private static final int RIPPLE_SEGMENTS = 14;
    private static final float[] RIPPLE_COS = new float[15];
    private static final float[] RIPPLE_SIN = new float[15];
    private static final int MAX_DROPS = 4200;
    private static final int MAX_MIST = 90;
    private final List<Drop> drops = new ArrayList<Drop>();
    private final List<Splash> splashList = new ArrayList<Splash>();
    private final List<Droplet> dropletList = new ArrayList<Droplet>();
    private final List<Mist> mistList = new ArrayList<Mist>();
    private final ArrayDeque<Drop> dropPool = new ArrayDeque();
    private final ArrayDeque<Splash> splashPool = new ArrayDeque();
    private final ArrayDeque<Droplet> dropletPool = new ArrayDeque();
    private final ArrayDeque<Mist> mistPool = new ArrayDeque();
    private final Map<Long, Integer> groundCache = new HashMap<Long, Integer>();
    private final class_2338.class_2339 scratchPos = new class_2338.class_2339();
    private final Random random = new Random();
    private final transient Matrix4f particleMatrix = new Matrix4f();
    private long lastFrameNanos;
    private float clock;
    private float flash;
    private float nextFlashIn = 9.0f;
    private int cacheTicks;

    public static Rain getInstance() {
        return Instance.get(Rain.class);
    }

    public Rain() {
        super("Rain", "Rain", ModuleCategory.RENDER);
        this.setup(this.preset, this.density, this.radius, this.altitude, this.dropSize, this.fallSpeed, this.wind, this.opacity, this.splashes, this.droplets, this.groundMist, this.lightning, this.skyCheck, this.onlyWhenRaining, this.colorMode, this.customColor);
    }

    @Override
    public void activate() {
        super.activate();
        this.lastFrameNanos = 0L;
        this.clearAll();
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.clearAll();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void clearAll() {
        List<Drop> list = this.drops;
        synchronized (list) {
            this.drops.clear();
        }
        this.splashList.clear();
        this.dropletList.clear();
        this.mistList.clear();
        this.groundCache.clear();
        this.flash = 0.0f;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (Rain.mc.field_1724 == null || Rain.mc.field_1687 == null) {
            this.clearAll();
            return;
        }
        if (++this.cacheTicks > 30) {
            this.cacheTicks = 0;
            this.groundCache.clear();
        }
    }

    private float presetDensity() {
        if (this.preset.isSelected("Drizzle")) {
            return 0.45f;
        }
        if (this.preset.isSelected("Downpour")) {
            return 1.9f;
        }
        if (this.preset.isSelected("Storm")) {
            return 2.8f;
        }
        return 1.0f;
    }

    private float presetWind() {
        if (this.preset.isSelected("Drizzle")) {
            return 0.5f;
        }
        if (this.preset.isSelected("Downpour")) {
            return 1.35f;
        }
        if (this.preset.isSelected("Storm")) {
            return 2.4f;
        }
        return 1.0f;
    }

    private float presetMass() {
        if (this.preset.isSelected("Drizzle")) {
            return 0.55f;
        }
        if (this.preset.isSelected("Downpour")) {
            return 1.25f;
        }
        if (this.preset.isSelected("Storm")) {
            return 1.5f;
        }
        return 1.0f;
    }

    private int targetDropCount() {
        float r = this.radius.getValue();
        float area = r * r * 0.0155f;
        int count = (int)(area * this.density.getValue() * this.presetDensity() * 26.0f);
        return class_3532.method_15340((int)count, (int)40, (int)4200);
    }

    private float[] resolveColor() {
        int color = this.colorMode.isSelected("Custom") ? this.customColor.getColor() : (this.colorMode.isSelected("Client") ? ColorUtil.getClientColor() : -3613464);
        float r = (float)(color >> 16 & 0xFF) / 255.0f;
        float g = (float)(color >> 8 & 0xFF) / 255.0f;
        float b = (float)(color & 0xFF) / 255.0f;
        return new float[]{r, g, b};
    }

    private float random(float min, float max) {
        return min + this.random.nextFloat() * (max - min);
    }

    private int surfaceY(int x, int z) {
        int top;
        long key = (long)x << 32 ^ (long)z & 0xFFFFFFFFL;
        Integer cached = this.groundCache.get(key);
        if (cached != null) {
            return cached;
        }
        try {
            top = Rain.mc.field_1687.method_8624(class_2902.class_2903.field_13202, x, z);
        }
        catch (Throwable ignored) {
            top = Rain.mc.field_1687.method_31607();
        }
        if (this.groundCache.size() < 20000) {
            this.groundCache.put(key, top);
        }
        return top;
    }

    private boolean isBlocking(double x, double y, double z) {
        if (Rain.mc.field_1687 == null) {
            return false;
        }
        this.scratchPos.method_10103(class_3532.method_15357((double)x), class_3532.method_15357((double)y), class_3532.method_15357((double)z));
        if (!Rain.mc.field_1687.method_2935().method_12123(this.scratchPos.method_10263() >> 4, this.scratchPos.method_10260() >> 4)) {
            return false;
        }
        class_2680 state = Rain.mc.field_1687.method_8320((class_2338)this.scratchPos);
        if (!state.method_26227().method_15769()) {
            return true;
        }
        if (state.method_26215()) {
            return false;
        }
        try {
            return !state.method_26220((class_1922)Rain.mc.field_1687, (class_2338)this.scratchPos).method_1110();
        }
        catch (Throwable ignored) {
            return true;
        }
    }

    private boolean isWater(double x, double y, double z) {
        if (Rain.mc.field_1687 == null) {
            return false;
        }
        this.scratchPos.method_10103(class_3532.method_15357((double)x), class_3532.method_15357((double)y), class_3532.method_15357((double)z));
        try {
            return !Rain.mc.field_1687.method_8320((class_2338)this.scratchPos).method_26227().method_15769();
        }
        catch (Throwable ignored) {
            return false;
        }
    }

    private boolean skyVisible(double x, double y, double z) {
        if (!this.skyCheck.isValue() || Rain.mc.field_1687 == null) {
            return true;
        }
        this.scratchPos.method_10103(class_3532.method_15357((double)x), class_3532.method_15357((double)y), class_3532.method_15357((double)z));
        try {
            return Rain.mc.field_1687.method_8311((class_2338)this.scratchPos);
        }
        catch (Throwable ignored) {
            return true;
        }
    }

    private void spawnDrop(class_243 center, boolean fromTop) {
        double y;
        float r = this.radius.getValue();
        double angle = this.random.nextDouble() * Math.PI * 2.0;
        double dist = Math.sqrt(this.random.nextDouble()) * (double)r;
        double x = center.field_1352 + Math.cos(angle) * dist;
        double z = center.field_1350 + Math.sin(angle) * dist;
        double d = y = fromTop ? center.field_1351 + (double)(this.altitude.getValue() * this.random(0.75f, 1.0f)) : center.field_1351 + (double)this.random(-4.0f, this.altitude.getValue());
        if (!this.skyVisible(x, y, z)) {
            return;
        }
        Drop drop = this.dropPool.pollFirst();
        if (drop == null) {
            drop = new Drop();
        }
        drop.x = x;
        drop.y = y;
        drop.z = z;
        drop.prevX = x;
        drop.prevY = y;
        drop.prevZ = z;
        drop.vx = 0.0;
        drop.vz = 0.0;
        drop.mass = this.random(0.6f, 1.4f) * this.presetMass();
        drop.vy = (double)(-this.random(6.0f, 11.0f)) * drop.mass;
        drop.phase = this.random(0.0f, 62.8f);
        drop.brightness = this.random(0.55f, 1.0f);
        drop.ground = this.surfaceY(class_3532.method_15357((double)x), class_3532.method_15357((double)z));
        drop.dead = false;
        this.drops.add(drop);
    }

    private void updateDrops(class_243 center, float dt) {
        int target = this.targetDropCount();
        this.drops.removeIf(d -> {
            double dx = d.x - center.field_1352;
            double dz = d.z - center.field_1350;
            float r = this.radius.getValue() + 6.0f;
            if (d.dead || dx * dx + dz * dz > (double)(r * r) || d.y < d.ground - 1.0 || d.y > center.field_1351 + (double)this.altitude.getValue() + 20.0) {
                if (this.dropPool.size() < 4200) {
                    this.dropPool.offerFirst((Drop)d);
                }
                return true;
            }
            return false;
        });
        boolean fill = this.drops.size() < target / 2;
        int spawnBudget = fill ? Math.min(target - this.drops.size(), 900) : Math.min(target - this.drops.size(), 260);
        for (int i = 0; i < spawnBudget; ++i) {
            this.spawnDrop(center, !fill);
        }
        float speedMul = this.fallSpeed.getValue();
        float windMul = this.wind.getValue() * this.presetWind();
        int size = this.drops.size();
        for (int i = 0; i < size; ++i) {
            Drop d2 = this.drops.get(i);
            double gust = 0.65 + 0.35 * Math.sin((double)this.clock * 0.55 + (double)d2.phase * 0.15) + 0.18 * Math.sin((double)this.clock * 1.7 + (double)d2.phase);
            double windX = (double)windMul * 3.1 * gust;
            double windZ = (double)windMul * 1.7 * Math.sin((double)this.clock * 0.31 + (double)d2.phase * 0.05) * gust;
            double drag = 1.0 - Math.min(0.92, Math.abs(d2.vy) / (34.0 * d2.mass));
            d2.vy -= 22.0 * d2.mass * drag * (double)dt;
            if (d2.vy < -34.0 * d2.mass) {
                d2.vy = -34.0 * d2.mass;
            }
            d2.vx += (windX - d2.vx) * Math.min(1.0, (double)dt * 3.2);
            d2.vz += (windZ - d2.vz) * Math.min(1.0, (double)dt * 3.2);
            double nx = d2.x + d2.vx * (double)dt * (double)speedMul;
            double ny = d2.y + d2.vy * (double)dt * (double)speedMul;
            double nz = d2.z + d2.vz * (double)dt * (double)speedMul;
            d2.prevX = d2.x;
            d2.prevY = d2.y;
            d2.prevZ = d2.z;
            boolean hit = false;
            if (ny <= d2.ground + 2.5) {
                if (this.isBlocking(nx, ny, nz)) {
                    hit = true;
                } else if (ny <= d2.ground - 3.0) {
                    d2.dead = true;
                }
            }
            if (hit) {
                if (this.splashes.isValue()) {
                    this.addSplash(nx, ny, nz, d2);
                }
                d2.dead = true;
                continue;
            }
            d2.x = nx;
            d2.y = ny;
            d2.z = nz;
        }
    }

    private void addSplash(double x, double y, double z, Drop d) {
        if (this.splashList.size() > 700) {
            return;
        }
        double impactY = Math.floor(y) + 1.0005;
        Splash splash = this.splashPool.pollFirst();
        if (splash == null) {
            splash = new Splash();
        }
        splash.x = x;
        splash.y = impactY;
        splash.z = z;
        splash.life = 0.0f;
        splash.maxLife = this.random(0.3f, 0.52f);
        splash.scale = (float)(0.32 + 0.22 * d.mass);
        splash.water = this.isWater(x, y - 0.15, z);
        this.splashList.add(splash);
        if (!this.droplets.isValue() || this.dropletList.size() > 1400) {
            return;
        }
        int shards = splash.water ? 4 : 3;
        for (int i = 0; i < shards; ++i) {
            double a = this.random.nextDouble() * Math.PI * 2.0;
            double sp = (double)this.random(1.1f, 2.6f) * d.mass;
            Droplet drop = this.dropletPool.pollFirst();
            if (drop == null) {
                drop = new Droplet();
            }
            drop.x = x;
            drop.y = impactY + 0.02;
            drop.z = z;
            drop.vx = Math.cos(a) * sp * 0.45 + d.vx * 0.08;
            drop.vz = Math.sin(a) * sp * 0.45 + d.vz * 0.08;
            drop.vy = this.random(1.9f, 4.1f);
            drop.life = 0.0f;
            drop.maxLife = this.random(0.28f, 0.5f);
            this.dropletList.add(drop);
        }
    }

    private void updateDroplets(float dt) {
        for (int i = this.dropletList.size() - 1; i >= 0; --i) {
            Droplet dr = this.dropletList.get(i);
            dr.life += dt;
            if (dr.life >= dr.maxLife) {
                this.dropletList.remove(i);
                if (this.dropletPool.size() >= 1400) continue;
                this.dropletPool.offerFirst(dr);
                continue;
            }
            dr.prevX = dr.x;
            dr.prevY = dr.y;
            dr.prevZ = dr.z;
            dr.vy -= 12.100000000000001 * (double)dt;
            dr.x += dr.vx * (double)dt;
            dr.y += dr.vy * (double)dt;
            dr.z += dr.vz * (double)dt;
        }
    }

    private void updateSplashes(float dt) {
        for (int i = this.splashList.size() - 1; i >= 0; --i) {
            Splash s = this.splashList.get(i);
            s.life += dt;
            if (!(s.life >= s.maxLife)) continue;
            this.splashList.remove(i);
            if (this.splashPool.size() >= 700) continue;
            this.splashPool.offerFirst(s);
        }
    }

    private void updateMist(class_243 center, float dt) {
        if (!this.groundMist.isValue()) {
            this.mistList.clear();
            return;
        }
        int target = (int)class_3532.method_15363((float)(22.0f * this.density.getValue() * this.presetDensity()), (float)6.0f, (float)90.0f);
        for (int i = this.mistList.size() - 1; i >= 0; --i) {
            Mist m = this.mistList.get(i);
            m.life += dt;
            double dx = m.x - center.field_1352;
            double dz = m.z - center.field_1350;
            float r = this.radius.getValue();
            m.x += m.vx * (double)dt;
            m.z += m.vz * (double)dt;
            if (!(m.life > m.maxLife) && !(dx * dx + dz * dz > (double)(r * r))) continue;
            this.mistList.remove(i);
            if (this.mistPool.size() >= 90) continue;
            this.mistPool.offerFirst(m);
        }
        while (this.mistList.size() < target) {
            double angle = this.random.nextDouble() * Math.PI * 2.0;
            double dist = Math.sqrt(this.random.nextDouble()) * (double)this.radius.getValue() * 0.85;
            double x = center.field_1352 + Math.cos(angle) * dist;
            double z = center.field_1350 + Math.sin(angle) * dist;
            int ground = this.surfaceY(class_3532.method_15357((double)x), class_3532.method_15357((double)z));
            Mist m = this.mistPool.pollFirst();
            if (m == null) {
                m = new Mist();
            }
            m.x = x;
            m.z = z;
            m.y = (float)ground + this.random(0.05f, 0.9f);
            m.size = this.random(2.2f, 5.6f);
            m.maxLife = this.random(2.4f, 5.5f);
            m.life = this.random(0.0f, 1.0f);
            m.vx = this.random(-0.35f, 0.35f) * this.wind.getValue();
            m.vz = this.random(-0.35f, 0.35f) * this.wind.getValue();
            this.mistList.add(m);
        }
    }

    /**
     * Draws this rain for the Atmosphere module (Rain/Storm modes) while the Rain module itself is off.
     * The user's Rain settings are kept; only preset, lightning and the "only when raining" check are overridden.
     */
    public void renderForAtmosphere(WorldRenderEvent event, boolean storm, float amount) {
        if (this.isState()) {
            return;   // the Rain module is on and draws by itself
        }
        String oldPreset = this.preset.getSelected();
        boolean oldLightning = this.lightning.isValue();
        boolean oldOnlyRaining = this.onlyWhenRaining.isValue();
        float oldDensity = this.density.getValue();
        try {
            this.preset.setSelected(storm ? "Storm" : "Downpour");
            this.lightning.setValue(storm);
            this.onlyWhenRaining.setValue(false);
            this.density.setValue(Math.max(0.1f, Math.min(3.0f, amount)));
            if (++this.cacheTicks > 60) {
                this.cacheTicks = 0;
                this.groundCache.clear();
            }
            this.onWorldRender(event);
        } finally {
            this.preset.setSelected(oldPreset);
            this.lightning.setValue(oldLightning);
            this.onlyWhenRaining.setValue(oldOnlyRaining);
            this.density.setValue(oldDensity);
        }
    }

    /** Clears drops left over from Atmosphere rain. */
    public void clearForAtmosphere() {
        if (!this.isState()) {
            this.clearAll();
            this.lastFrameNanos = 0L;
        }
    }

    /** Current lightning flash (0..1), used by Atmosphere to light up the screen. */
    public float getFlash() {
        return this.flash;
    }

    private void updateLightning(float dt) {
        if (!this.lightning.isValue()) {
            this.flash = 0.0f;
            return;
        }
        this.flash = Math.max(0.0f, this.flash - dt * 3.4f);
        this.nextFlashIn -= dt;
        if (this.nextFlashIn <= 0.0f) {
            this.flash = this.random(0.55f, 1.0f);
            this.nextFlashIn = this.random(6.0f, 16.0f);
        }
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        if (Rain.mc.field_1724 == null || Rain.mc.field_1687 == null || Rain.mc.field_1773 == null || event.getStack() == null) {
            return;
        }
        if (this.onlyWhenRaining.isValue() && !Rain.mc.field_1687.method_8419()) {
            this.clearAll();
            return;
        }
        class_4184 camera = Rain.mc.field_1773.method_19418();
        if (camera == null) {
            return;
        }
        long now = System.nanoTime();
        if (this.lastFrameNanos == 0L) {
            this.lastFrameNanos = now;
        }
        float dt = (float)((double)(now - this.lastFrameNanos) / 1.0E9);
        this.lastFrameNanos = now;
        dt = class_3532.method_15363((float)dt, (float)0.0f, (float)0.05f);
        this.clock += dt;
        class_243 cam = camera.method_71156();
        this.updateDrops(cam, dt);
        this.updateSplashes(dt);
        this.updateDroplets(dt);
        this.updateMist(cam, dt);
        this.updateLightning(dt);
        if (this.drops.isEmpty() && this.splashList.isEmpty() && this.dropletList.isEmpty() && this.mistList.isEmpty()) {
            return;
        }
        float[] rgb = this.resolveColor();
        float ambient = this.ambientLight(camera);
        float brightnessMul = class_3532.method_15363((float)(ambient + this.flash * 0.9f), (float)0.0f, (float)1.65f);
        float alphaMul = this.opacity.getValue() * brightnessMul;
        class_4587 matrices = event.getStack();
        matrices.method_22903();
        LegacyRenderSystem.enableBlend();
        LegacyRenderSystem.enableDepthTest();
        LegacyRenderSystem.depthMask(false);
        LegacyRenderSystem.disableCull();
        LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        this.prepareStreaks(matrices, camera, cam, alphaMul);
        LegacyRenderSystem.blendFunc((Object)GlStateManager.SrcFactor.SRC_ALPHA, (Object)GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        class_287 translucent = class_289.method_1348().method_60827(VertexFormat.class_5596.field_27382, class_290.field_1576);
        if (this.groundMist.isValue()) {
            this.drawMist(translucent, matrices, camera, cam, rgb, alphaMul);
        }
        this.drawStreaks(translucent, rgb, false);
        if (this.splashes.isValue()) {
            this.drawRipples(translucent, matrices, cam, rgb, alphaMul);
        }
        Rain.drawBatch(translucent);
        LegacyRenderSystem.blendFunc((Object)GlStateManager.SrcFactor.SRC_ALPHA, (Object)GlStateManager.DstFactor.ONE);
        class_287 additive = class_289.method_1348().method_60827(VertexFormat.class_5596.field_27382, class_290.field_1576);
        this.drawStreaks(additive, rgb, true);
        if (this.splashes.isValue()) {
            this.drawCrowns(additive, matrices, camera, cam, rgb, alphaMul);
            if (this.droplets.isValue()) {
                this.drawDroplets(additive, matrices, camera, cam, rgb, alphaMul);
            }
        }
        Rain.drawBatch(additive);
        LegacyRenderSystem.depthMask(true);
        LegacyRenderSystem.enableCull();
        LegacyRenderSystem.defaultBlendFunc();
        LegacyRenderSystem.disableBlend();
        matrices.method_22909();
    }

    private static void drawBatch(class_287 buffer) {
        class_9801 built = buffer.method_60794();
        if (built != null) {
            BufferRenderer.drawWithGlobalProgram(built);
        }
    }

    private void prepareStreaks(class_4587 ms, class_4184 camera, class_243 cam, float alphaMul) {
        if (this.drops.isEmpty()) {
            return;
        }
        float sizeMul = this.dropSize.getValue();
        float r = this.radius.getValue();
        float fadeStart = r * 0.62f;
        float cameraYaw = camera.method_19330();
        float yawRad = (float)Math.toRadians(cameraYaw);
        double rightX = Math.cos(yawRad);
        double rightZ = Math.sin(yawRad);
        Quaternionf yawRotation = class_7833.field_40716.rotationDegrees(-cameraYaw);
        Quaternionf tiltRotation = new Quaternionf();
        int size = this.drops.size();
        for (int i = 0; i < size; ++i) {
            float distanceFade;
            Drop d = this.drops.get(i);
            d.renderAlpha = 0.0f;
            double dx = d.x - cam.field_1352;
            double dy = d.y - cam.field_1351;
            double dz = d.z - cam.field_1350;
            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq > ((double)r + 4.0) * ((double)r + 4.0)) continue;
            double dist = Math.sqrt(distSq);
            float f = distanceFade = dist <= (double)fadeStart ? 1.0f : 1.0f - class_3532.method_15363((float)((float)((dist - (double)fadeStart) / (double)(r - fadeStart + 1.0f))), (float)0.0f, (float)1.0f);
            if (distanceFade <= 0.02f) continue;
            float nearFade = dist < 0.7 ? (float)(dist / 0.7) : 1.0f;
            double speed = Math.sqrt(d.vx * d.vx + d.vy * d.vy + d.vz * d.vz);
            if (speed < 1.0E-4) continue;
            float length = (float)class_3532.method_15350((double)(speed * 0.055 * (double)sizeMul), (double)0.16, (double)2.6);
            float width = (float)(0.014 + 0.019 * d.mass) * sizeMul;
            float alpha = alphaMul * d.brightness * distanceFade * nearFade;
            if (alpha * 0.42f <= 0.006f) continue;
            double horizontal = d.vx * rightX + d.vz * rightZ;
            float tilt = (float)Math.toDegrees(Math.atan2(horizontal, Math.abs(d.vy) + 1.0E-4));
            tilt = class_3532.method_15363((float)tilt, (float)-55.0f, (float)55.0f);
            d.renderMatrix.set((Matrix4fc)ms.method_23760().method_23761()).translate((float)dx, (float)dy, (float)dz).rotate((Quaternionfc)yawRotation).rotate((Quaternionfc)tiltRotation.rotationZ((float)Math.toRadians(-tilt)));
            d.renderAlpha = alpha;
            d.renderLength = length;
            d.renderWidth = width;
        }
    }

    private void drawStreaks(class_287 buffer, float[] rgb, boolean highlight) {
        int size = this.drops.size();
        for (int i = 0; i < size; ++i) {
            Drop d = this.drops.get(i);
            float alpha = d.renderAlpha * (highlight ? 0.3f : 0.42f);
            if (alpha <= 0.006f) continue;
            alpha = Math.min(alpha, 0.95f);
            float length = d.renderLength;
            float width = d.renderWidth * (highlight ? 0.45f : 1.0f);
            Matrix4f m = d.renderMatrix;
            float half = width * 0.5f;
            float top = length * 0.5f;
            float bottom = -length * 0.5f;
            float headAlpha = alpha;
            float tailAlpha = 0.0f;
            float headWidth = half;
            float tailWidth = half * 0.35f;
            buffer.method_22918((Matrix4fc)m, -tailWidth, top, 0.0f).method_22915(rgb[0], rgb[1], rgb[2], tailAlpha);
            buffer.method_22918((Matrix4fc)m, tailWidth, top, 0.0f).method_22915(rgb[0], rgb[1], rgb[2], tailAlpha);
            buffer.method_22918((Matrix4fc)m, headWidth, bottom, 0.0f).method_22915(rgb[0], rgb[1], rgb[2], headAlpha);
            buffer.method_22918((Matrix4fc)m, -headWidth, bottom, 0.0f).method_22915(rgb[0], rgb[1], rgb[2], headAlpha);
            if (!highlight) {
                float halo = half * 2.6f;
                float haloAlpha = alpha * 0.22f;
                buffer.method_22918((Matrix4fc)m, -halo * 0.5f, top, 0.0f).method_22915(rgb[0], rgb[1], rgb[2], 0.0f);
                buffer.method_22918((Matrix4fc)m, halo * 0.5f, top, 0.0f).method_22915(rgb[0], rgb[1], rgb[2], 0.0f);
                buffer.method_22918((Matrix4fc)m, halo, bottom, 0.0f).method_22915(rgb[0], rgb[1], rgb[2], haloAlpha);
                buffer.method_22918((Matrix4fc)m, -halo, bottom, 0.0f).method_22915(rgb[0], rgb[1], rgb[2], haloAlpha);
                continue;
            }
            float lensTop = bottom + length * 0.34f;
            float lensWidth = half * 1.25f;
            buffer.method_22918((Matrix4fc)m, -lensWidth, lensTop, 0.0f).method_22915(1.0f, 1.0f, 1.0f, 0.0f);
            buffer.method_22918((Matrix4fc)m, lensWidth, lensTop, 0.0f).method_22915(1.0f, 1.0f, 1.0f, 0.0f);
            buffer.method_22918((Matrix4fc)m, lensWidth, bottom, 0.0f).method_22915(1.0f, 1.0f, 1.0f, alpha * 0.85f);
            buffer.method_22918((Matrix4fc)m, -lensWidth, bottom, 0.0f).method_22915(1.0f, 1.0f, 1.0f, alpha * 0.85f);
        }
    }

    private void drawRipples(class_287 buffer, class_4587 ms, class_243 cam, float[] rgb, float alphaMul) {
        if (this.splashList.isEmpty()) {
            return;
        }
        int size = this.splashList.size();
        for (int i = 0; i < size; ++i) {
            Splash s = this.splashList.get(i);
            float progress = class_3532.method_15363((float)(s.life / s.maxLife), (float)0.0f, (float)1.0f);
            float eased = 1.0f - (1.0f - progress) * (1.0f - progress);
            float outer = s.scale * (0.18f + eased * (s.water ? 1.35f : 0.85f));
            float inner = outer * (0.55f + 0.35f * eased);
            float alpha = alphaMul * (1.0f - progress) * (s.water ? 0.5f : 0.38f);
            if (alpha <= 0.01f) continue;
            double dx = s.x - cam.field_1352;
            double dy = s.y - cam.field_1351;
            double dz = s.z - cam.field_1350;
            Matrix4f m = this.particleMatrix.set((Matrix4fc)ms.method_23760().method_23761()).translate((float)dx, (float)dy, (float)dz);
            for (int seg = 0; seg < 14; ++seg) {
                float ix0 = RIPPLE_COS[seg] * inner;
                float iz0 = RIPPLE_SIN[seg] * inner;
                float ix1 = RIPPLE_COS[seg + 1] * inner;
                float iz1 = RIPPLE_SIN[seg + 1] * inner;
                float ox0 = RIPPLE_COS[seg] * outer;
                float oz0 = RIPPLE_SIN[seg] * outer;
                float ox1 = RIPPLE_COS[seg + 1] * outer;
                float oz1 = RIPPLE_SIN[seg + 1] * outer;
                buffer.method_22918((Matrix4fc)m, ix0, 0.0f, iz0).method_22915(rgb[0], rgb[1], rgb[2], alpha);
                buffer.method_22918((Matrix4fc)m, ix1, 0.0f, iz1).method_22915(rgb[0], rgb[1], rgb[2], alpha);
                buffer.method_22918((Matrix4fc)m, ox1, 0.0f, oz1).method_22915(rgb[0], rgb[1], rgb[2], 0.0f);
                buffer.method_22918((Matrix4fc)m, ox0, 0.0f, oz0).method_22915(rgb[0], rgb[1], rgb[2], 0.0f);
            }
        }
    }

    private void drawCrowns(class_287 buffer, class_4587 ms, class_4184 camera, class_243 cam, float[] rgb, float alphaMul) {
        if (this.splashList.isEmpty()) {
            return;
        }
        Quaternionf yawRotation = class_7833.field_40716.rotationDegrees(-camera.method_19330());
        int size = this.splashList.size();
        for (int i = 0; i < size; ++i) {
            Splash s = this.splashList.get(i);
            float progress = class_3532.method_15363((float)(s.life / s.maxLife), (float)0.0f, (float)1.0f);
            if (progress > 0.55f) continue;
            float local = progress / 0.55f;
            float height = s.scale * (0.55f * (float)Math.sin((double)local * Math.PI));
            float alpha = alphaMul * (1.0f - local) * 0.32f;
            if (alpha <= 0.01f || height <= 0.005f) continue;
            Matrix4f m = this.particleMatrix.set((Matrix4fc)ms.method_23760().method_23761()).translate((float)(s.x - cam.field_1352), (float)(s.y - cam.field_1351), (float)(s.z - cam.field_1350)).rotate((Quaternionfc)yawRotation);
            float base = s.scale * 0.16f;
            buffer.method_22918((Matrix4fc)m, -base * 0.25f, height, 0.0f).method_22915(rgb[0], rgb[1], rgb[2], 0.0f);
            buffer.method_22918((Matrix4fc)m, base * 0.25f, height, 0.0f).method_22915(rgb[0], rgb[1], rgb[2], 0.0f);
            buffer.method_22918((Matrix4fc)m, base, 0.0f, 0.0f).method_22915(rgb[0], rgb[1], rgb[2], alpha);
            buffer.method_22918((Matrix4fc)m, -base, 0.0f, 0.0f).method_22915(rgb[0], rgb[1], rgb[2], alpha);
        }
    }

    private void drawDroplets(class_287 buffer, class_4587 ms, class_4184 camera, class_243 cam, float[] rgb, float alphaMul) {
        if (this.dropletList.isEmpty()) {
            return;
        }
        float sizeMul = this.dropSize.getValue();
        float cameraYaw = camera.method_19330();
        float yawRad = (float)Math.toRadians(cameraYaw);
        double rightX = Math.cos(yawRad);
        double rightZ = Math.sin(yawRad);
        Quaternionf yawRotation = class_7833.field_40716.rotationDegrees(-cameraYaw);
        Quaternionf tiltRotation = new Quaternionf();
        int size = this.dropletList.size();
        for (int i = 0; i < size; ++i) {
            Droplet d = this.dropletList.get(i);
            float progress = class_3532.method_15363((float)(d.life / d.maxLife), (float)0.0f, (float)1.0f);
            float alpha = alphaMul * (1.0f - progress) * 0.5f;
            if (alpha <= 0.01f) continue;
            double speed = Math.sqrt(d.vx * d.vx + d.vy * d.vy + d.vz * d.vz);
            float length = (float)class_3532.method_15350((double)(speed * 0.03 * (double)sizeMul), (double)0.04, (double)0.4);
            float width = 0.016f * sizeMul;
            double horizontal = d.vx * rightX + d.vz * rightZ;
            float tilt = (float)Math.toDegrees(Math.atan2(horizontal, -d.vy + 1.0E-4));
            Matrix4f m = this.particleMatrix.set((Matrix4fc)ms.method_23760().method_23761()).translate((float)(d.x - cam.field_1352), (float)(d.y - cam.field_1351), (float)(d.z - cam.field_1350)).rotate((Quaternionfc)yawRotation).rotate((Quaternionfc)tiltRotation.rotationZ((float)Math.toRadians(-tilt)));
            buffer.method_22918((Matrix4fc)m, -width * 0.4f, length * 0.5f, 0.0f).method_22915(rgb[0], rgb[1], rgb[2], 0.0f);
            buffer.method_22918((Matrix4fc)m, width * 0.4f, length * 0.5f, 0.0f).method_22915(rgb[0], rgb[1], rgb[2], 0.0f);
            buffer.method_22918((Matrix4fc)m, width, -length * 0.5f, 0.0f).method_22915(rgb[0], rgb[1], rgb[2], alpha);
            buffer.method_22918((Matrix4fc)m, -width, -length * 0.5f, 0.0f).method_22915(rgb[0], rgb[1], rgb[2], alpha);
        }
    }

    private void drawMist(class_287 buffer, class_4587 ms, class_4184 camera, class_243 cam, float[] rgb, float alphaMul) {
        if (this.mistList.isEmpty()) {
            return;
        }
        Quaternionf yawRotation = class_7833.field_40716.rotationDegrees(-camera.method_19330());
        int size = this.mistList.size();
        for (int i = 0; i < size; ++i) {
            Mist mist = this.mistList.get(i);
            float progress = class_3532.method_15363((float)(mist.life / mist.maxLife), (float)0.0f, (float)1.0f);
            float fade = (float)Math.sin((double)progress * Math.PI);
            float alpha = alphaMul * fade * 0.05f;
            if (alpha <= 0.004f) continue;
            Matrix4f m = this.particleMatrix.set((Matrix4fc)ms.method_23760().method_23761()).translate((float)(mist.x - cam.field_1352), (float)(mist.y - cam.field_1351), (float)(mist.z - cam.field_1350)).rotate((Quaternionfc)yawRotation);
            float half = mist.size * 0.5f;
            float top = mist.size * 0.42f;
            buffer.method_22918((Matrix4fc)m, -half, top, 0.0f).method_22915(rgb[0], rgb[1], rgb[2], 0.0f);
            buffer.method_22918((Matrix4fc)m, half, top, 0.0f).method_22915(rgb[0], rgb[1], rgb[2], 0.0f);
            buffer.method_22918((Matrix4fc)m, half, 0.0f, 0.0f).method_22915(rgb[0], rgb[1], rgb[2], alpha);
            buffer.method_22918((Matrix4fc)m, -half, 0.0f, 0.0f).method_22915(rgb[0], rgb[1], rgb[2], alpha);
        }
    }

    private float ambientLight(class_4184 camera) {
        try {
            int light = Rain.mc.field_1687.method_22339(camera.method_19328());
            return class_3532.method_15363((float)((float)light / 15.0f), (float)0.3f, (float)1.0f);
        }
        catch (Throwable ignored) {
            return 1.0f;
        }
    }

    static {
        for (int i = 0; i <= 14; ++i) {
            double angle = Math.PI * 2 * (double)i / 14.0;
            Rain.RIPPLE_COS[i] = (float)Math.cos(angle);
            Rain.RIPPLE_SIN[i] = (float)Math.sin(angle);
        }
    }

    private static final class Drop {
        final Matrix4f renderMatrix = new Matrix4f();
        float renderAlpha;
        float renderLength;
        float renderWidth;
        double x;
        double y;
        double z;
        double prevX;
        double prevY;
        double prevZ;
        double vx;
        double vy;
        double vz;
        double mass = 1.0;
        double ground;
        float phase;
        float brightness = 1.0f;
        boolean dead;

        private Drop() {
        }
    }

    private static final class Splash {
        double x;
        double y;
        double z;
        float life;
        float maxLife = 0.4f;
        float scale = 0.4f;
        boolean water;

        private Splash() {
        }
    }

    private static final class Droplet {
        double x;
        double y;
        double z;
        double prevX;
        double prevY;
        double prevZ;
        double vx;
        double vy;
        double vz;
        float life;
        float maxLife = 0.4f;

        private Droplet() {
        }
    }

    private static final class Mist {
        double x;
        double y;
        double z;
        double vx;
        double vz;
        float size = 3.0f;
        float life;
        float maxLife = 4.0f;

        private Mist() {
        }
    }
}

