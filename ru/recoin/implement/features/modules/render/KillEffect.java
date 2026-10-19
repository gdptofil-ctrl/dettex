/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_1297
 *  net.minecraft.class_1297$class_5529
 *  net.minecraft.class_1309
 *  net.minecraft.class_1657
 *  net.minecraft.class_1937
 *  net.minecraft.class_243
 *  net.minecraft.class_2596
 *  net.minecraft.class_2663
 *  net.minecraft.class_287
 *  net.minecraft.class_289
 *  net.minecraft.class_290
 *  net.minecraft.class_2960
 *  net.minecraft.class_3532
 *  net.minecraft.class_4184
 *  net.minecraft.class_638
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Vector3f
 */
package ru.recoin.implement.features.modules.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_2663;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_4184;
import net.minecraft.class_638;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3f;
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
import ru.recoin.common.util.render.Render3DUtil;
import ru.recoin.common.util.render.effects.BloodKillEffect;
import ru.recoin.compat.render.BufferRenderer;
import ru.recoin.compat.render.GlStateManager;
import ru.recoin.compat.render.ShaderProgramKeys;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.events.player.AttackEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.events.render.WorldRenderEvent;
import ru.recoin.implement.features.modules.combat.Aura;

public class KillEffect
extends Module {
    private static final class_2960 BLOOM_TEXTURE = class_2960.method_60655((String)"minecraft", (String)"textures/bloom.png");
    private final SelectSetting mode = new SelectSetting("Mode", "Style of kill effect").value("Blood", "Black Flash", "Lightning").selected("Blood");
    private final ValueSetting duration = new ValueSetting("Duration", "Kill effect duration in seconds").setValue(0.5f).range(0.2f, 3.0f).step(0.1f).visible(() -> !this.mode.isSelected("Blood"));
    private final SelectSetting colorMode = new SelectSetting("Color Mode", "Lightning color source").value("Theme", "Custom").selected("Theme").visible(() -> this.mode.isSelected("Lightning"));
    private final ColorSetting customColor = new ColorSetting("Custom Color", "Custom lightning color").setColor(-11141121).visible(() -> this.mode.isSelected("Lightning") && this.colorMode.isSelected("Custom"));
    private final ColorSetting bloodColor = new ColorSetting("Blood Color", "Color of the liquid").setColor(-7732477).visible(() -> this.mode.isSelected("Blood"));
    private final ValueSetting bloodAmount = new ValueSetting("Blood Amount", "Number of primary droplets").setValue(110.0f).range(10, 250).step(5.0f).visible(() -> this.mode.isSelected("Blood"));
    private final ValueSetting bloodPower = new ValueSetting("Blood Spread", "Initial spray strength").setValue(1.0f).range(0.3f, 2.5f).step(0.1f).visible(() -> this.mode.isSelected("Blood"));
    private final ValueSetting bloodSize = new ValueSetting("Droplet Size", "Scale of droplets and their deposited liquid").setValue(1.0f).range(0.3f, 3.0f).step(0.1f).visible(() -> this.mode.isSelected("Blood"));
    private final ValueSetting bloodTime = new ValueSetting("Stain Lifetime", "Seconds before surface stains dry and fade").setValue(12.0f).range(1.0f, 30.0f).step(0.5f).visible(() -> this.mode.isSelected("Blood"));
    private final BooleanSetting wallDrips = new BooleanSetting("Wall Drips", "Liquid flows down walls and drips from edges").setValue(true).visible(() -> this.mode.isSelected("Blood"));
    private final SelectSetting bloodTargets = new SelectSetting("Blood Targets", "Entities that trigger the effect").value("All Living", "Players").selected("All Living").visible(() -> this.mode.isSelected("Blood"));
    private final BooleanSetting onlyMyKills = new BooleanSetting("Only My Kills", "Only show deaths of recently attacked targets").setValue(false).visible(() -> this.mode.isSelected("Blood"));
    private final BloodKillEffect blood = new BloodKillEffect();
    private final ArrayBlockingQueue<DeathNotification> pendingDeaths = new ArrayBlockingQueue(64);
    private final Map<Integer, ProcessedKill> processedKills = new HashMap<Integer, ProcessedKill>();
    private class_638 effectWorld;
    private final List<ActiveKillEffect> activeEffects = new ArrayList<ActiveKillEffect>();
    private int lastAttackedId = -1;
    private long lastAttackTime;
    private int lastAuraTargetId = -1;
    private long lastAuraTargetTime;
    private long lastSparkTime;

    public static KillEffect getInstance() {
        return Instance.get(KillEffect.class);
    }

    public KillEffect() {
        super("KillEffect", "Kill Effect", ModuleCategory.RENDER);
        this.setup(this.mode, this.duration, this.colorMode, this.customColor, this.bloodColor, this.bloodAmount, this.bloodPower, this.bloodSize, this.bloodTime, this.wallDrips, this.bloodTargets, this.onlyMyKills);
    }

    @Override
    public void deactivate() {
        this.resetEffects();
        this.blood.close();
        this.effectWorld = null;
        super.deactivate();
    }

    @Override
    public void activate() {
        this.resetEffects();
        this.effectWorld = KillEffect.mc.field_1687;
    }

    private void resetEffects() {
        this.blood.clear();
        this.pendingDeaths.clear();
        this.processedKills.clear();
        this.activeEffects.clear();
        this.lastAttackedId = -1;
        this.lastAuraTargetId = -1;
        this.lastSparkTime = 0L;
    }

    @EventHandler
    public void onAttack(AttackEvent event) {
        class_1309 living;
        class_1297 target = event.getTarget();
        if (target instanceof class_1309 && this.acceptsTarget(living = (class_1309)target)) {
            this.lastAttackedId = living.method_5628();
            this.lastAttackTime = System.currentTimeMillis();
        }
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        DeathNotification notification;
        class_2663 status;
        class_2596<?> class_25962;
        if (event.isReceive() && (class_25962 = event.getPacket()) instanceof class_2663 && (status = (class_2663)class_25962).method_11470() == 3 && KillEffect.mc.field_1687 != null && !this.pendingDeaths.offer(notification = new DeathNotification(status, KillEffect.mc.field_1687))) {
            this.pendingDeaths.poll();
            this.pendingDeaths.offer(notification);
        }
    }

    @EventHandler
    public void onTick(TickEvent event) {
        class_1309 living;
        DeathNotification notification;
        class_1309 living2;
        class_1309 auraTarget;
        if (KillEffect.mc.field_1687 == null || KillEffect.mc.field_1724 == null) {
            this.resetEffects();
            this.effectWorld = null;
            return;
        }
        if (this.effectWorld != KillEffect.mc.field_1687) {
            this.resetEffects();
            this.effectWorld = KillEffect.mc.field_1687;
        }
        long now = System.currentTimeMillis();
        float dt = this.lastSparkTime == 0L ? 0.05f : Math.min(0.1f, (float)(now - this.lastSparkTime) / 1000.0f);
        this.lastSparkTime = now;
        this.blood.tick(this.wallDrips.isValue());
        this.processedKills.entrySet().removeIf(entry -> {
            class_1309 living5;
            class_1297 entity = KillEffect.mc.field_1687.method_8469(((Integer)entry.getKey()).intValue());
            return entity == null || !entity.method_5667().equals(((ProcessedKill)entry.getValue()).uuid) || entity instanceof class_1309 && (living5 = (class_1309)entity).method_6032() > 0.0f && now - ((ProcessedKill)entry.getValue()).time > 250L;
        });
        for (ActiveKillEffect effect2 : this.activeEffects) {
            if (!"Lightning".equalsIgnoreCase(effect2.effectMode)) continue;
            for (SparkParticle p : effect2.sparks) {
                p.update(dt);
            }
        }
        Aura aura = Aura.getInstance();
        class_1309 class_13092 = auraTarget = aura != null && aura.isState() ? aura.getTarget() : null;
        if (auraTarget instanceof class_1309 && this.acceptsTarget(living2 = auraTarget)) {
            this.lastAuraTargetId = living2.method_5628();
            this.lastAuraTargetTime = now;
        }
        for (int i = 0; i < 64 && (notification = this.pendingDeaths.poll()) != null; ++i) {
            class_1297 entity;
            if (notification.world != KillEffect.mc.field_1687 || !((entity = notification.packet.method_11469((class_1937)KillEffect.mc.field_1687)) instanceof class_1309)) continue;
            class_1309 living3 = (class_1309)entity;
            this.trySpawnEffect(living3, now);
        }
        if (this.mode.isSelected("Blood") && !this.onlyMyKills.isValue()) {
            for (class_1297 entity : KillEffect.mc.field_1687.method_18112()) {
                class_1309 living4;
                if (!(entity instanceof class_1309) || !this.isConfirmedDead(living4 = (class_1309)entity)) continue;
                this.trySpawnEffect(living4, now);
            }
        }
        if (this.lastAttackedId != -1) {
            class_1297 entity = KillEffect.mc.field_1687.method_8469(this.lastAttackedId);
            if (entity instanceof class_1309) {
                living = (class_1309)entity;
                if (this.isConfirmedDead(living)) {
                    if (now - this.lastAttackTime <= 4000L) {
                        this.trySpawnEffect(living, now);
                    }
                    this.lastAttackedId = -1;
                } else if (living.method_31481() || now - this.lastAttackTime > 4000L) {
                    this.lastAttackedId = -1;
                }
            } else if (now - this.lastAttackTime > 4000L) {
                this.lastAttackedId = -1;
            }
        }
        if (this.lastAuraTargetId != -1 && (auraTarget == null || auraTarget.method_5628() != this.lastAuraTargetId)) {
            class_1297 entity = KillEffect.mc.field_1687.method_8469(this.lastAuraTargetId);
            if (entity instanceof class_1309) {
                living = (class_1309)entity;
                if (this.isConfirmedDead(living)) {
                    if (now - this.lastAuraTargetTime <= 4000L) {
                        this.trySpawnEffect(living, now);
                    }
                    this.lastAuraTargetId = -1;
                } else if (living.method_31481() || now - this.lastAuraTargetTime > 4000L) {
                    this.lastAuraTargetId = -1;
                }
            } else if (now - this.lastAuraTargetTime > 4000L) {
                this.lastAuraTargetId = -1;
            }
        }
        this.activeEffects.removeIf(effect -> now - effect.startTime > effect.duration);
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        if (KillEffect.mc.field_1773 == null) {
            return;
        }
        this.blood.render(event, this.wallDrips.isValue());
        if (this.activeEffects.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        ArrayList<ActiveKillEffect> lightningEffects = null;
        for (ActiveKillEffect effect : this.activeEffects) {
            float progress = (float)(now - effect.startTime) / (float)effect.duration;
            if (progress >= 1.0f) continue;
            if ("Black Flash".equalsIgnoreCase(effect.effectMode)) {
                float fade = (float)Math.pow(Math.max(0.0f, 1.0f - progress), 1.4f);
                Render3DUtil.drawBlackFlashAt(event.getStack(), effect.pos, progress, fade, effect.seed);
                continue;
            }
            if (!"Lightning".equalsIgnoreCase(effect.effectMode)) continue;
            if (lightningEffects == null) {
                lightningEffects = new ArrayList<ActiveKillEffect>();
            }
            lightningEffects.add(effect);
        }
        if (lightningEffects != null && !lightningEffects.isEmpty()) {
            this.renderLightningEffects(event, (List<ActiveKillEffect>)lightningEffects, now);
        }
    }

    private void renderLightningEffects(WorldRenderEvent event, List<ActiveKillEffect> effects, long now) {
        class_4184 camera = KillEffect.mc.field_1773.method_19418();
        class_243 cameraPos = camera.method_71156();
        Quaternionf rot = camera.method_23767();
        Vector3f rightVec = rot.transform(new Vector3f(1.0f, 0.0f, 0.0f));
        Vector3f upVec = rot.transform(new Vector3f(0.0f, 1.0f, 0.0f));
        Matrix4f matrix = event.getStack().method_23760().method_23761();
        LegacyRenderSystem.enableBlend();
        LegacyRenderSystem.disableCull();
        LegacyRenderSystem.disableDepthTest();
        LegacyRenderSystem.depthMask(false);
        LegacyRenderSystem.setShaderTexture(0, BLOOM_TEXTURE);
        LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        LegacyRenderSystem.blendFunc((Object)GlStateManager.SrcFactor.SRC_ALPHA, (Object)GlStateManager.DstFactor.ONE);
        class_287 buffer = class_289.method_1348().method_60827(VertexFormat.class_5596.field_27382, class_290.field_1575);
        for (ActiveKillEffect effect : effects) {
            float progress = (float)(now - effect.startTime) / (float)effect.duration;
            if (progress >= 1.0f) continue;
            float flash = (1.0f - progress) * (ThreadLocalRandom.current().nextFloat() > 0.12f ? 1.0f : 0.4f);
            int baseCol = effect.color;
            int boltCol = ColorUtil.replAlpha(baseCol, Math.max(0.0f, Math.min(1.0f, flash)));
            int whiteCore = ColorUtil.getColor(255, 255, 255, (int)(255.0f * flash));
            KillEffect.renderDenseLaserLine(buffer, matrix, cameraPos, rightVec, upVec, effect.segments, boltCol, whiteCore, 0.55f, 0.16f);
            if (!effect.branchSegments.isEmpty()) {
                KillEffect.renderDenseLaserLine(buffer, matrix, cameraPos, rightVec, upVec, effect.branchSegments, boltCol, whiteCore, 0.35f, 0.1f);
            }
            float alphaFactor = (float)Math.sin((double)(1.0f - progress) * 1.5707963267948966);
            int sparkCol = ColorUtil.replAlpha(baseCol, Math.max(0.0f, Math.min(1.0f, alphaFactor)));
            for (SparkParticle p : effect.sparks) {
                float currentSize = p.baseSize * (0.5f + 0.5f * alphaFactor);
                KillEffect.emitBillboard(buffer, matrix, p.pos.method_1020(cameraPos), rightVec, upVec, currentSize, sparkCol);
            }
        }
        BufferRenderer.drawWithGlobalProgram(buffer.method_60800());
        LegacyRenderSystem.depthMask(true);
        LegacyRenderSystem.enableDepthTest();
        LegacyRenderSystem.enableCull();
        LegacyRenderSystem.defaultBlendFunc();
        LegacyRenderSystem.disableBlend();
    }

    private static void renderDenseLaserLine(class_287 buffer, Matrix4f matrix, class_243 cameraPos, Vector3f rightVec, Vector3f upVec, List<class_243> points, int outerColor, int coreColor, float outerR, float coreR) {
        for (int i = 0; i < points.size() - 1; ++i) {
            class_243 p1 = points.get(i);
            class_243 p2 = points.get(i + 1);
            double dist = p1.method_1022(p2);
            double step = 0.04;
            for (double d = 0.0; d < dist; d += step) {
                class_243 pos = p1.method_35590(p2, d / dist);
                class_243 rel = pos.method_1020(cameraPos);
                KillEffect.emitBillboard(buffer, matrix, rel, rightVec, upVec, outerR, outerColor);
                KillEffect.emitBillboard(buffer, matrix, rel, rightVec, upVec, coreR, coreColor);
            }
        }
    }

    private static void emitBillboard(class_287 buffer, Matrix4f matrix, class_243 rel, Vector3f rightVec, Vector3f upVec, float size, int color) {
        float half = size * 0.5f;
        float rx = rightVec.x * half;
        float ry = rightVec.y * half;
        float rz = rightVec.z * half;
        float ux = upVec.x * half;
        float uy = upVec.y * half;
        float uz = upVec.z * half;
        float cx = (float)rel.field_1352;
        float cy = (float)rel.field_1351;
        float cz = (float)rel.field_1350;
        buffer.method_22918((Matrix4fc)matrix, cx - rx - ux, cy - ry - uy, cz - rz - uz).method_22913(0.0f, 1.0f).method_39415(color);
        buffer.method_22918((Matrix4fc)matrix, cx + rx - ux, cy + ry - uy, cz + rz - uz).method_22913(1.0f, 1.0f).method_39415(color);
        buffer.method_22918((Matrix4fc)matrix, cx + rx + ux, cy + ry + uy, cz + rz + uz).method_22913(1.0f, 0.0f).method_39415(color);
        buffer.method_22918((Matrix4fc)matrix, cx - rx + ux, cy - ry + uy, cz - rz + uz).method_22913(0.0f, 0.0f).method_39415(color);
    }

    private static void generateLightningSegments(class_243 targetPos, List<class_243> segments, List<class_243> branchSegments, ThreadLocalRandom rnd) {
        class_243 top = targetPos.method_1031((rnd.nextDouble() - 0.5) * 3.0, 28.0, (rnd.nextDouble() - 0.5) * 3.0);
        int count = 28;
        class_243 current = top;
        segments.add(current);
        for (int i = 1; i <= count; ++i) {
            float frac = (float)i / (float)count;
            class_243 ideal = top.method_35590(targetPos, (double)frac);
            if (i < count) {
                double spread = (double)(1.0f - frac) * 1.3 + 0.3;
                ideal = ideal.method_1031((rnd.nextDouble() - 0.5) * spread, (rnd.nextDouble() - 0.5) * 0.4, (rnd.nextDouble() - 0.5) * spread);
                if (i == 9 || i == 18) {
                    class_243 branchEnd = ideal.method_1031((rnd.nextDouble() - 0.5) * 3.8, -3.5 - rnd.nextDouble(3.0), (rnd.nextDouble() - 0.5) * 3.8);
                    branchSegments.add(ideal);
                    branchSegments.add(ideal.method_35590(branchEnd, 0.5).method_1031(0.2, 0.2, -0.2));
                    branchSegments.add(branchEnd);
                }
            }
            segments.add(ideal);
        }
    }

    private static class_243 randomSphereVector(ThreadLocalRandom rnd) {
        double theta = rnd.nextDouble() * Math.PI * 2.0;
        double phi = Math.acos(rnd.nextDouble() * 2.0 - 1.0);
        double sinPhi = Math.sin(phi);
        return new class_243(sinPhi * Math.cos(theta), Math.cos(phi), sinPhi * Math.sin(theta));
    }

    private boolean isConfirmedDead(class_1309 entity) {
        if (entity.method_35049() == class_1297.class_5529.field_26998) {
            return true;
        }
        if (!entity.method_31481()) {
            return entity.method_6032() <= 0.0f && entity.field_6213 > 0;
        }
        return false;
    }

    private boolean acceptsTarget(class_1309 entity) {
        return entity != KillEffect.mc.field_1724 && (entity instanceof class_1657 || this.mode.isSelected("Blood") && this.bloodTargets.isSelected("All Living"));
    }

    private void trySpawnEffect(class_1309 entity, long now) {
        boolean auraTarget;
        if (!this.acceptsTarget(entity) || entity.method_5858((class_1297)KillEffect.mc.field_1724) > 4096.0) {
            return;
        }
        boolean attacked = entity.method_5628() == this.lastAttackedId && now - this.lastAttackTime <= 4000L;
        boolean bl = auraTarget = entity.method_5628() == this.lastAuraTargetId && now - this.lastAuraTargetTime <= 4000L;
        if (!(this.mode.isSelected("Blood") && !this.onlyMyKills.isValue() || attacked || auraTarget)) {
            return;
        }
        ProcessedKill previous = this.processedKills.get(entity.method_5628());
        if (previous != null && previous.uuid.equals(entity.method_5667())) {
            return;
        }
        this.processedKills.put(entity.method_5628(), new ProcessedKill(entity.method_5667(), now));
        if (this.mode.isSelected("Blood")) {
            float width = class_3532.method_15363((float)entity.method_17681(), (float)0.3f, (float)1.6f);
            float height = class_3532.method_15363((float)entity.method_17682(), (float)0.3f, (float)2.6f);
            class_243 center = entity.method_73189().method_1031(0.0, (double)height * 0.6, 0.0);
            class_243 away = entity.method_73189().method_1020(KillEffect.mc.field_1724.method_73189()).method_18805(1.0, 0.0, 1.0).method_1029();
            class_243 impulse = away.method_1021(0.065 * (double)this.bloodPower.getValue()).method_1019(entity.method_18798().method_1021(0.25));
            this.blood.burst(center, width, height, this.bloodAmount.getInt(), this.bloodPower.getValue(), this.bloodSize.getValue(), this.bloodColor.getColor(), this.bloodTime.getValue(), impulse);
        } else {
            this.spawnEffect(entity.method_73189());
        }
        if (attacked) {
            this.lastAttackedId = -1;
        }
        if (auraTarget) {
            this.lastAuraTargetId = -1;
        }
    }

    private void spawnEffect(class_243 pos) {
        long now = System.currentTimeMillis();
        long seed = ThreadLocalRandom.current().nextLong();
        long durationMs = Math.max(100L, (long)(this.duration.getValue() * 1000.0f));
        String selectedMode = this.mode.getSelected();
        int effColor = this.colorMode.isSelected("Custom") ? this.customColor.getColor() : ColorUtil.getClientColor();
        ActiveKillEffect effect = new ActiveKillEffect(pos, now, durationMs, seed, selectedMode, effColor);
        if ("Lightning".equalsIgnoreCase(selectedMode)) {
            ThreadLocalRandom rnd = ThreadLocalRandom.current();
            KillEffect.generateLightningSegments(pos, effect.segments, effect.branchSegments, rnd);
            for (int i = 0; i < 25; ++i) {
                class_243 vel = KillEffect.randomSphereVector(rnd).method_1021(0.4);
                effect.sparks.add(new SparkParticle(pos, vel, 0.4f));
            }
        }
        this.activeEffects.add(effect);
        if (this.activeEffects.size() > 32) {
            this.activeEffects.removeFirst();
        }
    }

    public SelectSetting getMode() {
        return this.mode;
    }

    public ValueSetting getDuration() {
        return this.duration;
    }

    public SelectSetting getColorMode() {
        return this.colorMode;
    }

    public ColorSetting getCustomColor() {
        return this.customColor;
    }

    public ColorSetting getBloodColor() {
        return this.bloodColor;
    }

    public ValueSetting getBloodAmount() {
        return this.bloodAmount;
    }

    public ValueSetting getBloodPower() {
        return this.bloodPower;
    }

    public ValueSetting getBloodSize() {
        return this.bloodSize;
    }

    public ValueSetting getBloodTime() {
        return this.bloodTime;
    }

    public BooleanSetting getWallDrips() {
        return this.wallDrips;
    }

    public SelectSetting getBloodTargets() {
        return this.bloodTargets;
    }

    public BooleanSetting getOnlyMyKills() {
        return this.onlyMyKills;
    }

    public BloodKillEffect getBlood() {
        return this.blood;
    }

    public ArrayBlockingQueue<DeathNotification> getPendingDeaths() {
        return this.pendingDeaths;
    }

    public Map<Integer, ProcessedKill> getProcessedKills() {
        return this.processedKills;
    }

    public class_638 getEffectWorld() {
        return this.effectWorld;
    }

    public List<ActiveKillEffect> getActiveEffects() {
        return this.activeEffects;
    }

    public int getLastAttackedId() {
        return this.lastAttackedId;
    }

    public long getLastAttackTime() {
        return this.lastAttackTime;
    }

    public int getLastAuraTargetId() {
        return this.lastAuraTargetId;
    }

    public long getLastAuraTargetTime() {
        return this.lastAuraTargetTime;
    }

    public long getLastSparkTime() {
        return this.lastSparkTime;
    }

    private record DeathNotification(class_2663 packet, class_638 world) {
    }

    private static class ActiveKillEffect {
        final class_243 pos;
        final long startTime;
        final long duration;
        final long seed;
        final String effectMode;
        final int color;
        final List<class_243> segments = new ArrayList<class_243>();
        final List<class_243> branchSegments = new ArrayList<class_243>();
        final List<SparkParticle> sparks = new ArrayList<SparkParticle>();

        ActiveKillEffect(class_243 pos, long startTime, long duration, long seed, String effectMode, int color) {
            this.pos = pos;
            this.startTime = startTime;
            this.duration = duration;
            this.seed = seed;
            this.effectMode = effectMode;
            this.color = color;
        }
    }

    private static class SparkParticle {
        class_243 pos;
        class_243 velocity;
        final float baseSize;

        SparkParticle(class_243 pos, class_243 velocity, float baseSize) {
            this.pos = pos;
            this.velocity = velocity;
            this.baseSize = baseSize;
        }

        void update(float dt) {
            this.velocity = this.velocity.method_1021(Math.pow(0.5, dt)).method_1031(0.0, -0.65 * (double)dt, 0.0);
            this.pos = this.pos.method_1019(this.velocity.method_1021((double)dt * 20.0));
        }
    }

    private record ProcessedKill(UUID uuid, long time) {
    }
}

