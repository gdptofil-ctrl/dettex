/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.BlendFunction
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  com.mojang.blaze3d.platform.DestFactor
 *  com.mojang.blaze3d.platform.SourceFactor
 *  net.minecraft.class_10042
 *  net.minecraft.class_10799
 *  net.minecraft.class_1297
 *  net.minecraft.class_1304
 *  net.minecraft.class_1309
 *  net.minecraft.class_1799
 *  net.minecraft.class_239
 *  net.minecraft.class_2960
 *  net.minecraft.class_332
 *  net.minecraft.class_3532
 *  net.minecraft.class_3966
 *  net.minecraft.class_4587
 *  net.minecraft.class_897
 *  net.minecraft.class_922
 */
package ru.recoin.implement.features.draggables;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.class_10042;
import net.minecraft.class_10799;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_239;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_3966;
import net.minecraft.class_4587;
import net.minecraft.class_897;
import net.minecraft.class_922;
import ru.recoin.api.feature.draggable.AbstractDraggable;
import ru.recoin.api.system.animation.Direction;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.item.FunTimeDurabilityUtil;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.render.ItemStackFade;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.common.util.render.ScissorManager;
import ru.recoin.core.Main;
import ru.recoin.implement.events.player.AttackEvent;
import ru.recoin.implement.features.modules.combat.Aura;
import ru.recoin.implement.features.modules.combat.TriggerBot;
import ru.recoin.implement.features.modules.misc.HealthBypass;
import ru.recoin.implement.features.modules.render.Hud;

public class TargetHud
extends AbstractDraggable {
    private static final class_2960 BLOOM_TEXTURE = class_2960.method_60654((String)"textures/particles/bloom/bloom_small.png");
    private static final RenderPipeline BLOOM_PIPELINE = class_10799.method_67887((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{class_10799.field_56864}).withLocation(class_2960.method_60655((String)"recoin", (String)"pipeline/target_hud_bloom")).withBlend(new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE)).build());
    private final class_1799[] equippedItems = new class_1799[6];
    private final ItemStackFade[] equippedFades = new ItemStackFade[6];
    private final float[] equippedCrossAlpha = new float[6];
    private final List<HitParticle> particles = new ArrayList<HitParticle>();
    private class_1309 pendingParticleTarget;
    private int pendingParticleBursts;
    private class_1309 particleHurtTarget;
    private int previousParticleHurtTime;
    private long lastParticleRequestAt;
    private class_1309 lastTarget;
    private boolean targetVisible;
    private float health;
    private float absorptionHealth;
    private float textHealth = -1.0f;
    private float textAbsorption = -1.0f;
    private class_1309 prevTargetForHpText;

    public TargetHud() {
        super("Target Hud", 10, 40, 110, 29, true);
        for (int i = 0; i < this.equippedItems.length; ++i) {
            this.equippedItems[i] = class_1799.field_8037;
            this.equippedFades[i] = new ItemStackFade();
            this.equippedCrossAlpha[i] = 1.0f;
        }
    }

    @Override
    public boolean visible() {
        return this.targetVisible;
    }

    @Override
    public void attack(AttackEvent event) {
        class_1297 class_12972;
        if (!event.isPre() && (class_12972 = event.getEntity()) instanceof class_1309) {
            class_1309 living = (class_1309)class_12972;
            this.requestParticleBurst(living);
        }
    }

    @Override
    public void tick() {
        class_1309 nextTarget;
        class_239 class_2392;
        class_1309 hoveredTarget;
        class_1309 auraTarget = Aura.getInstance().getTarget();
        class_1309 triggerBotTarget = TriggerBot.getInstance().getTarget();
        class_1309 class_13092 = hoveredTarget = Hud.getInstance().targetShowOnHover.isValue() && (class_2392 = TargetHud.mc.field_1765) instanceof class_3966 hit ? (hit.method_17782() instanceof class_1309 living ? living : null) : null;
        if (auraTarget != null) {
            nextTarget = auraTarget;
        } else if (triggerBotTarget != null) {
            nextTarget = triggerBotTarget;
        } else if (hoveredTarget != null) {
            nextTarget = hoveredTarget;
        } else {
            nextTarget = PlayerIntersectionUtil.isChat(TargetHud.mc.field_1755) ? TargetHud.mc.field_1724 : null;
        }
        if (nextTarget != null) {
            this.lastTarget = nextTarget;
            this.targetVisible = true;
            if (this.particleHurtTarget != nextTarget) {
                this.particleHurtTarget = nextTarget;
                this.previousParticleHurtTime = 0;
            }
            if (nextTarget.field_6235 > this.previousParticleHurtTime && System.currentTimeMillis() - this.lastParticleRequestAt > 250L) {
                this.requestParticleBurst(nextTarget);
            }
            this.previousParticleHurtTime = nextTarget.field_6235;
        } else {
            this.targetVisible = false;
            this.particleHurtTarget = null;
            this.previousParticleHurtTime = 0;
        }
        if (!this.targetVisible && this.scaleAnimation.isFinished(Direction.BACKWARDS)) {
            this.lastTarget = null;
            this.particles.clear();
            this.pendingParticleTarget = null;
            this.pendingParticleBursts = 0;
            this.particleHurtTarget = null;
            this.previousParticleHurtTime = 0;
            this.textHealth = -1.0f;
            this.textAbsorption = -1.0f;
            this.prevTargetForHpText = null;
            this.resetEquippedFades();
        }
    }

    @Override
    public void drawDraggable(class_332 context) {
        if (this.lastTarget != null) {
            if (Hud.getInstance().isHud2()) {
                this.drawHud2(context);
                return;
            }
            class_4587 matrix = MatrixStackCompat.from(context.method_51448());
            this.checkHitParticles();
            this.drawMain(context, matrix);
            this.drawArmor(context, matrix);
            this.drawFace(context);
            this.drawHitParticles(context);
        }
    }

    private void drawHud2(class_332 context) {
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        FontRenderer nameFont = Fonts.getSize(14, Fonts.Type.INTER_BOLD);
        FontRenderer hpFont = Fonts.getSize(13, Fonts.Type.INTER_BOLD);
        FontRenderer iconFont = Fonts.getSize(12, Fonts.Type.COMBINED_ICONS);
        int accent = Hud.getInstance().themeColor();
        float scale = 1.0f;
        float w = 148.0f * scale;
        float h = 44.0f * scale;
        float pad = 7.0f * scale;
        float x = this.getX();
        float y = this.getY();
        this.setWidth((int)Math.ceil(w));
        this.setHeight((int)Math.ceil(h));
        blur.render(ShapeProperties.create(matrix, x, y, w, h).round(10.0f * scale).softness(3.5f).thickness(0.0f).quality(50.0f).outlineColor(0).color(ColorUtil.getColor(15, 15, 15, 0.59f)).build());
        rectangle.render(ShapeProperties.create(matrix, x, y, w, h).round(10.0f * scale).softness(1.0f).thickness(1.0f).quality(50.0f).outlineColor(ColorUtil.getOutline(0.3f)).color(0).build());
        float faceSize = h - pad * 2.0f;
        float faceX = x + pad;
        float faceY = y + pad;
        this.drawFaceHud2(context, faceX, faceY, faceSize);
        float contentX = faceX + faceSize + 8.0f * scale;
        float contentRight = x + w - pad;
        float hp = PlayerIntersectionUtil.getHealth(this.lastTarget);
        float maxHp = Math.max(1.0f, this.lastTarget.method_6063());
        boolean healthKnown = hp >= 0.0f;
        float nameRowY = y + pad;
        String hpStr = healthKnown ? String.valueOf((int)Math.ceil(this.lastTarget.method_6032())) : "?";
        float heartWidth = iconFont.getStringWidth("\ue0bd");
        float hpTextWidth = hpFont.getStringWidth(hpStr);
        float hpBlockWidth = heartWidth + 3.0f * scale + hpTextWidth;
        float hpBlockX = contentRight - hpBlockWidth;
        iconFont.drawString(matrix, "\ue0bd", hpBlockX, nameRowY + 1.0f * scale, -45747);
        hpFont.drawString(matrix, hpStr, hpBlockX + heartWidth + 3.0f * scale, nameRowY, healthKnown ? ColorUtil.getText() : ColorUtil.getText(0.6f));
        String targetName = this.lastTarget.method_5477().getString();
        float maxNameWidth = Math.max(12.0f, hpBlockX - contentX - 6.0f * scale);
        if (nameFont.getStringWidth(targetName) > maxNameWidth) {
            ScissorManager scissorManager = Main.getInstance().getScissorManager();
            scissorManager.push(matrix.method_23760().method_23761(), contentX, y, maxNameWidth, h);
            nameFont.drawGradientString(matrix, targetName, contentX, nameRowY, ColorUtil.getText(), ColorUtil.getText(0.6f));
            scissorManager.pop();
        } else {
            nameFont.drawString(matrix, targetName, contentX, nameRowY, ColorUtil.getText());
        }
        float barH = 5.0f * scale;
        float barW = contentRight - contentX;
        float barY = y + h - pad - barH;
        float barRadius = barH * 0.5f;
        float targetRatio = healthKnown ? class_3532.method_15363((float)(this.lastTarget.method_6032() / maxHp), (float)0.0f, (float)1.0f) : 0.0f;
        this.health = class_3532.method_15363((float)MathUtil.interpolateSmooth(6.0, this.health, (float)(targetRatio * barW)), (float)0.0f, (float)barW);
        rectangle.render(ShapeProperties.create(matrix, contentX, barY, barW, barH).round(barRadius).color(ColorUtil.getColor(25, 20, 25, 0.7f)).build());
        if (this.health > 0.5f) {
            rectangle.render(ShapeProperties.create(matrix, contentX, barY, this.health, barH).softness(2.5f).round(barRadius).color(ColorUtil.multAlpha(accent, 0.3f)).build());
            rectangle.render(ShapeProperties.create(matrix, contentX, barY, this.health, barH).round(barRadius).color(accent).build());
        }
        this.drawArmor(context, matrix);
    }

    private void drawFaceHud2(class_332 context, float faceX, float faceY, float faceSize) {
        class_897 baseRenderer = mc.method_1561().method_3953((class_1297)this.lastTarget);
        if (!(baseRenderer instanceof class_922)) {
            return;
        }
        class_922 renderer = (class_922)baseRenderer;
        class_10042 state = (class_10042)renderer.method_62425((class_1297)this.lastTarget, tickCounter.getTickProgress(false));
        class_2960 textureLocation = renderer.method_3885(state);
        int healthTint = ColorUtil.multRed(-1, 1.0f + (float)this.lastTarget.field_6235 / 4.0f);
        int faceColor = Hud.getInstance().targetRedOnHit.isValue() && this.lastTarget.field_6235 > 0 ? healthTint : -1;
        Render2DUtil.drawRoundedTexture(context, textureLocation, faceX, faceY, faceSize, 6.0f, 8.0f, 8.0f, 8, 8, 64, 64, faceColor);
    }

    private void drawMain(class_332 context, class_4587 matrix) {
        String hpText;
        FontRenderer nameFont = Fonts.getSize(16, Fonts.Type.INTER_BOLD);
        FontRenderer hpFont = Fonts.getSize(13, Fonts.Type.INTER_BOLD);
        FontRenderer iconFont = Fonts.getSize(12, Fonts.Type.COMBINED_ICONS);
        String icon = "\ue0bd";
        float hp = PlayerIntersectionUtil.getHealth(this.lastTarget);
        float textX = (float)this.getX() + 30.5f;
        float textY = (float)this.getY() + 5.0f;
        float hpRightX = (float)(this.getX() + this.getWidth()) - 5.0f;
        float barX = textX;
        float barWidth = hpRightX - barX;
        float barY = (float)this.getY() + 20.0f;
        float barHeight = 3.5f;
        boolean healthKnown = hp >= 0.0f;
        boolean isScoreboardOnly = HealthBypass.getInstance().isScoreboardActive(this.lastTarget);
        float baseHealthForBar = healthKnown ? hp : 0.0f;
        float absorptionForBar = 0.0f;
        if (healthKnown && !isScoreboardOnly) {
            baseHealthForBar = Math.max(0.0f, this.lastTarget.method_6032());
            absorptionForBar = Math.max(0.0f, this.lastTarget.method_6067());
        }
        if (healthKnown) {
            if (this.lastTarget != this.prevTargetForHpText || this.textHealth < 0.0f) {
                this.textHealth = baseHealthForBar;
                this.textAbsorption = absorptionForBar;
                this.prevTargetForHpText = this.lastTarget;
            } else {
                this.textHealth = Math.abs(this.textHealth - baseHealthForBar) < 0.005f ? baseHealthForBar : MathUtil.interpolateSmooth(8.5, this.textHealth, baseHealthForBar);
                this.textAbsorption = Math.abs(this.textAbsorption - absorptionForBar) < 0.005f ? absorptionForBar : MathUtil.interpolateSmooth(8.5, this.textAbsorption, absorptionForBar);
            }
        } else {
            this.textHealth = -1.0f;
            this.textAbsorption = -1.0f;
            this.prevTargetForHpText = this.lastTarget;
        }
        float maxHealthForBar = Math.max(1.0f, this.lastTarget.method_6063());
        int targetHealthWidth = healthKnown ? Math.round(class_3532.method_15363((float)(baseHealthForBar / maxHealthForBar), (float)0.0f, (float)1.0f) * barWidth) : 2;
        int targetAbsorptionWidth = healthKnown && !isScoreboardOnly ? Math.round(class_3532.method_15363((float)(absorptionForBar / maxHealthForBar), (float)0.0f, (float)1.0f) * barWidth) : 0;
        this.health = class_3532.method_15363((float)MathUtil.interpolateSmooth(2.0, this.health, (float)targetHealthWidth), (float)2.0f, (float)barWidth);
        this.absorptionHealth = class_3532.method_15363((float)MathUtil.interpolateSmooth(2.0, this.absorptionHealth, (float)targetAbsorptionWidth), (float)0.0f, (float)barWidth);
        blur.render(ShapeProperties.create(matrix, this.getX(), this.getY(), this.getWidth(), this.getHeight()).round(6.0f).softness(3.5f).thickness(0.0f).quality(50.0f).outlineColor(0).color(-268435456).build());
        rectangle.render(ShapeProperties.create(matrix, this.getX(), this.getY(), this.getWidth(), this.getHeight()).round(6.0f).softness(0.5f).thickness(1.0f).quality(50.0f).outlineColor(ColorUtil.multAlpha(Hud.getInstance().colorSetting.getColor(), 0.25f)).color(0x35000000).build());
        String absHp = null;
        int hpTextColor = ColorUtil.getText();
        int heartColor = -45747;
        if (healthKnown) {
            int intAbs;
            float displayHp = this.textHealth >= 0.0f ? this.textHealth : baseHealthForBar;
            float displayAbs = this.textAbsorption >= 0.0f ? this.textAbsorption : absorptionForBar;
            int intHp = (int)Math.ceil(displayHp);
            hpText = String.valueOf(intHp);
            if (!isScoreboardOnly && displayAbs > 0.05f && (intAbs = (int)Math.ceil(displayAbs)) > 0) {
                absHp = String.valueOf(intAbs);
            }
        } else {
            hpText = "?";
            hpTextColor = ColorUtil.getText(0.6f);
        }
        float iconWidth = iconFont.getStringWidth(icon);
        float heartGap = 2.0f;
        float hpTextWidth = hpFont.getStringWidth(hpText);
        float plusGap = 2.0f;
        float plusWidth = 0.0f;
        float absHpWidth = 0.0f;
        float absBlockWidth = 0.0f;
        if (absHp != null) {
            plusWidth = hpFont.getStringWidth("+");
            absHpWidth = hpFont.getStringWidth(absHp);
            absBlockWidth = plusGap + plusWidth + plusGap + absHpWidth;
        }
        float hpTotalWidth = iconWidth + heartGap + hpTextWidth + absBlockWidth;
        float hpStartX = hpRightX - hpTotalWidth;
        iconFont.drawString(matrix, icon, hpStartX, textY + 1.5f, heartColor);
        float currentTextX = hpStartX + iconWidth + heartGap;
        hpFont.drawString(matrix, hpText, currentTextX, textY + 0.5f, hpTextColor);
        if (absHp != null) {
            float plusX = currentTextX + hpTextWidth + plusGap;
            float absX = plusX + plusWidth + plusGap;
            hpFont.drawString(matrix, "+", plusX, textY + 0.5f, -13261);
            hpFont.drawString(matrix, absHp, absX, textY + 0.5f, -13261);
        }
        String targetName = this.lastTarget.method_5477().getString();
        float maxNameWidth = Math.max(12.0f, hpStartX - textX - 4.0f);
        if (nameFont.getStringWidth(targetName) > maxNameWidth) {
            ScissorManager scissorManager = Main.getInstance().getScissorManager();
            scissorManager.push(matrix.method_23760().method_23761(), textX, this.getY(), maxNameWidth, this.getHeight());
            nameFont.drawGradientString(matrix, targetName, textX, textY, ColorUtil.getText(), ColorUtil.getText(0.6f));
            scissorManager.pop();
        } else {
            nameFont.drawString(matrix, targetName, textX, textY, ColorUtil.getText());
        }
        rectangle.render(ShapeProperties.create(matrix, barX, barY, barWidth, barHeight).round(2.0f).color(-1207630325).build());
        int themeColor = ColorUtil.getClientColor();
        rectangle.render(ShapeProperties.create(matrix, barX, barY, this.health, barHeight).softness(2.5f).round(2.0f).color(ColorUtil.multAlpha(themeColor, 0.3f)).build());
        rectangle.render(ShapeProperties.create(matrix, barX, barY, this.health, barHeight).round(2.0f).color(themeColor).build());
        if (!isScoreboardOnly && this.absorptionHealth > 0.5f) {
            int absorptionColor = -13261;
            rectangle.render(ShapeProperties.create(matrix, barX, barY, this.absorptionHealth, barHeight).softness(2.5f).round(2.0f).color(ColorUtil.multAlpha(absorptionColor, 0.3f)).build());
            rectangle.render(ShapeProperties.create(matrix, barX, barY, this.absorptionHealth, barHeight).round(2.0f).color(absorptionColor).build());
        }
    }

    private void drawArmor(class_332 context, class_4587 matrix) {
        if (!this.shouldDrawTargetItems()) {
            this.resetEquippedFades();
            return;
        }
        this.equippedItems[0] = this.safeEquipment(class_1304.field_6171);
        this.equippedItems[1] = this.safeEquipment(class_1304.field_6173);
        this.equippedItems[2] = this.safeEquipment(class_1304.field_6169);
        this.equippedItems[3] = this.safeEquipment(class_1304.field_6174);
        this.equippedItems[4] = this.safeEquipment(class_1304.field_6172);
        this.equippedItems[5] = this.safeEquipment(class_1304.field_6166);
        float itemStep = 10.0f;
        float itemScale = 0.55f;
        float totalWidth = (float)this.equippedItems.length * itemStep;
        float startX = (float)(this.getX() + this.getWidth()) - totalWidth - 3.0f;
        float itemY = (float)this.getY() - 10.5f;
        float crossY = (float)this.getY() - 5.0f;
        Render2DUtil.onRender(context);
        context.method_73199();
        for (int i = 0; i < this.equippedItems.length; ++i) {
            float slotX = startX + (float)i * itemStep;
            float crossTarget = this.equippedItems[i].method_7960() && this.targetVisible ? 1.0f : 0.0f;
            this.equippedCrossAlpha[i] = MathUtil.interpolateSmooth(1.25, this.equippedCrossAlpha[i], crossTarget);
            if (this.equippedCrossAlpha[i] > 0.01f) {
                this.drawSlotCross(matrix, slotX + 6.4f, crossY, this.equippedCrossAlpha[i]);
            }
            class_1799 stack = this.equippedItems[i];
            class_1799 renderedStack = Hud.getInstance().targetFunTimeDurability.isValue() ? FunTimeDurabilityUtil.forVanillaBar(stack) : stack;
            this.equippedFades[i].draw(context, renderedStack, slotX, itemY, false, true, itemScale);
        }
        context.method_73199();
    }

    private void drawSlotCross(class_4587 matrix, float centerX, float centerY, float alpha) {
        FontRenderer iconFont = Fonts.getSize(12, Fonts.Type.COMBINED_ICONS);
        iconFont.drawCenteredString(matrix, "\ue0c3", centerX, centerY, ColorUtil.multAlpha(-8748656, alpha * 0.59f));
    }

    private void resetEquippedFades() {
        for (int i = 0; i < this.equippedItems.length; ++i) {
            this.equippedItems[i] = class_1799.field_8037;
            this.equippedFades[i].reset();
            this.equippedCrossAlpha[i] = 0.0f;
        }
    }

    private class_1799 safeEquipment(class_1304 slot) {
        class_1799 stack = this.lastTarget.method_6118(slot);
        return stack == null ? class_1799.field_8037 : stack;
    }

    private boolean shouldDrawTargetItems() {
        Hud hud = Hud.getInstance();
        return hud.isState() && hud.interfaceSettings.isSelected(this.getName());
    }

    private void drawFace(class_332 context) {
        class_897 baseRenderer = mc.method_1561().method_3953((class_1297)this.lastTarget);
        if (!(baseRenderer instanceof class_922)) {
            return;
        }
        class_922 renderer = (class_922)baseRenderer;
        class_10042 state = (class_10042)renderer.method_62425((class_1297)this.lastTarget, tickCounter.getTickProgress(false));
        class_2960 textureLocation = renderer.method_3885(state);
        int healthTint = ColorUtil.multRed(-1, 1.0f + (float)this.lastTarget.field_6235 / 4.0f);
        int faceColor = Hud.getInstance().targetRedOnHit.isValue() && this.lastTarget.field_6235 > 0 ? healthTint : -1;
        Render2DUtil.drawRoundedTexture(context, textureLocation, (float)this.getX() + 2.5f, (float)this.getY() + 2.5f, 24.0f, 4.0f, 8.0f, 8.0f, 8, 8, 64, 64, faceColor);
    }

    private void checkHitParticles() {
        if (!Hud.getInstance().targetHitParticles.isValue()) {
            this.pendingParticleTarget = null;
            this.pendingParticleBursts = 0;
            this.particles.clear();
            return;
        }
        if (this.lastTarget == null) {
            return;
        }
        if (this.pendingParticleTarget != null && this.pendingParticleTarget != this.lastTarget) {
            this.pendingParticleTarget = null;
            this.pendingParticleBursts = 0;
            return;
        }
        if (this.pendingParticleBursts == 0) {
            return;
        }
        while (this.pendingParticleBursts > 0) {
            this.spawnHitParticles();
            --this.pendingParticleBursts;
        }
        this.pendingParticleTarget = null;
    }

    private void requestParticleBurst(class_1309 target) {
        this.pendingParticleTarget = target;
        ++this.pendingParticleBursts;
        this.lastParticleRequestAt = System.currentTimeMillis();
    }

    private void spawnHitParticles() {
        float headRelX = 14.5f;
        float headRelY = 14.5f;
        int count = 25 + ThreadLocalRandom.current().nextInt(6);
        for (int i = 0; i < count; ++i) {
            double angle = ThreadLocalRandom.current().nextDouble(Math.PI * 2);
            float speed = 0.5f + ThreadLocalRandom.current().nextFloat() * 1.2f;
            float vx = (float)Math.cos(angle) * speed;
            float vy = (float)Math.sin(angle) * speed;
            float size = 8.0f + ThreadLocalRandom.current().nextFloat() * 6.0f;
            int maxAge = 35 + ThreadLocalRandom.current().nextInt(22);
            int color = ColorUtil.getClientColor();
            this.particles.add(new HitParticle(headRelX, headRelY, vx, vy, size, maxAge, color));
        }
    }

    private void drawHitParticles(class_332 context) {
        if (this.particles.isEmpty()) {
            return;
        }
        float baseX = this.getX();
        float baseY = this.getY();
        Iterator<HitParticle> iterator2 = this.particles.iterator();
        while (iterator2.hasNext()) {
            HitParticle p = iterator2.next();
            if (!p.update()) {
                iterator2.remove();
                continue;
            }
            float progress = (float)p.age / (float)p.maxAge;
            float alpha = 1.0f - progress;
            float currentSize = p.size * (1.0f - 0.25f * progress);
            int renderCol = Render2DUtil.applyRenderAlpha(ColorUtil.multAlpha(p.color, alpha * 0.85f));
            if (ColorUtil.alpha(renderCol) <= 0) continue;
            float px = baseX + p.relX;
            float py = baseY + p.relY;
            int drawSize = Math.max(1, Math.round(currentSize));
            int drawX = Math.round(px - (float)drawSize / 2.0f);
            int drawY = Math.round(py - (float)drawSize / 2.0f);
            context.method_25293(BLOOM_PIPELINE, BLOOM_TEXTURE, drawX, drawY, 0.0f, 0.0f, drawSize, drawSize, drawSize, drawSize, drawSize, drawSize, renderCol);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void withRenderAlpha(float alpha, Runnable runnable) {
        float previous = Render2DUtil.getRenderAlpha();
        Render2DUtil.setRenderAlpha(previous * class_3532.method_15363((float)alpha, (float)0.0f, (float)1.0f));
        try {
            runnable.run();
        }
        finally {
            Render2DUtil.setRenderAlpha(previous);
        }
    }

    private static final class HitParticle {
        float relX;
        float relY;
        float vx;
        float vy;
        float size;
        int maxAge;
        int age;
        int color;

        HitParticle(float relX, float relY, float vx, float vy, float size, int maxAge, int color) {
            this.relX = relX;
            this.relY = relY;
            this.vx = vx;
            this.vy = vy;
            this.size = size;
            this.maxAge = maxAge;
            this.color = color;
        }

        boolean update() {
            this.relX += this.vx;
            this.relY += this.vy;
            this.vx *= 0.92f;
            this.vy *= 0.92f;
            return ++this.age < this.maxAge;
        }
    }
}

