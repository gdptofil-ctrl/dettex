/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10799
 *  net.minecraft.class_124
 *  net.minecraft.class_1291
 *  net.minecraft.class_1293
 *  net.minecraft.class_1294
 *  net.minecraft.class_2596
 *  net.minecraft.class_2678
 *  net.minecraft.class_2718
 *  net.minecraft.class_2724
 *  net.minecraft.class_2783
 *  net.minecraft.class_329
 *  net.minecraft.class_332
 *  net.minecraft.class_4587
 *  net.minecraft.class_6880
 *  net.minecraft.class_9848
 *  org.joml.Matrix3x2fStack
 */
package ru.recoin.implement.features.draggables;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.class_10799;
import net.minecraft.class_124;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_2596;
import net.minecraft.class_2678;
import net.minecraft.class_2718;
import net.minecraft.class_2724;
import net.minecraft.class_2783;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import net.minecraft.class_6880;
import net.minecraft.class_9848;
import org.joml.Matrix3x2fStack;
import ru.recoin.api.feature.draggable.AbstractDraggable;
import ru.recoin.api.system.animation.Animation;
import ru.recoin.api.system.animation.Direction;
import ru.recoin.api.system.animation.implement.DecelerateAnimation;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.features.draggables.HudIconRail;
import ru.recoin.implement.features.modules.render.Hud;

public class Potions
extends AbstractDraggable {
    private static final int HUD_ICON_SIZE = 15;
    private static final float HUD_ICON_OFFSET_Y = 0.8f;
    private final List<Potion> list = new CopyOnWriteArrayList<Potion>();
    private final Potion sidePreview = Potions.createSidePreview();
    private boolean sideWasEnabled;
    private int normalX = 210;
    private int normalY = 10;

    public Potions() {
        super("Potions", 210, 10, 100, 23, true);
    }

    @Override
    public boolean canDragX() {
        return !Hud.getInstance().potionsSide.isValue();
    }

    @Override
    public boolean canDragY() {
        return !Hud.getInstance().potionsSide.isValue();
    }

    @Override
    public boolean visible() {
        return !this.list.isEmpty() || PlayerIntersectionUtil.isChat(Potions.mc.field_1755);
    }

    @Override
    public void tick() {
        this.list.removeIf(p -> p.anim.isFinished(Direction.BACKWARDS));
        if (Potions.mc.field_1724 == null || Potions.mc.field_1687 == null) {
            return;
        }
        this.list.replaceAll(p -> {
            class_1293 effect = p.effect;
            if (!effect.method_48559() && effect.method_5584() <= 1) {
                p.anim.setDirection(Direction.BACKWARDS);
            }
            if (!effect.method_48559() && effect.method_5584() > 0) {
                effect = new class_1293(effect.method_5579(), effect.method_5584() - 1, effect.method_5578(), effect.method_5591(), effect.method_5581(), effect.method_5592());
            }
            return new Potion(effect, p.anim, p.maxDuration);
        });
    }

    @Override
    public void packet(PacketEvent e) {
        class_2596<?> class_25962 = e.getPacket();
        Objects.requireNonNull(class_25962);
        class_2596<?> class_25963 = class_25962;
        if (class_25963 instanceof class_2783 effect) {
            if (!(PlayerIntersectionUtil.nullCheck() || effect.method_11943() != Objects.requireNonNull(Potions.mc.field_1724).method_5628())) {
                class_6880 effectId = effect.method_11946();
                for (Potion potion : this.list) {
                    if (!potion.effect.method_5579().method_55840().equals(effectId.method_55840())) continue;
                    potion.anim.setDirection(Direction.BACKWARDS);
                }
                this.list.add(new Potion(new class_1293(effectId, effect.method_11944(), effect.method_11945(), effect.method_11950(), effect.method_11949(), effect.method_11942()), new DecelerateAnimation().setMs(150).setValue(1.0), effect.method_11944()));
            }
        } else if (class_25963 instanceof class_2718 effect) {
            for (Potion potion : this.list) {
                if (!potion.effect.method_5579().method_55840().equals(effect.comp_2176().method_55840())) continue;
                potion.anim.setDirection(Direction.BACKWARDS);
            }
        } else if (class_25963 instanceof class_2724 p) {
            this.list.clear();
        } else if (class_25963 instanceof class_2678 p) {
            this.list.clear();
        }
    }

    @Override
    public void drawDraggable(class_332 context) {
        if (Hud.getInstance().isHud2() && !Hud.getInstance().potionsSide.isValue()) {
            this.drawHud2(context);
            return;
        }
        if (Hud.getInstance().potionsSide.isValue()) {
            if (!this.sideWasEnabled) {
                this.normalX = this.getX();
                this.normalY = this.getY();
                this.sideWasEnabled = true;
            }
            this.setX(6);
            this.setY(Math.max(4, (context.method_51443() - this.sideHeight()) / 2));
            this.drawSide(context);
            return;
        }
        if (this.sideWasEnabled) {
            this.setX(this.normalX);
            this.setY(this.normalY);
            this.sideWasEnabled = false;
        }
        List<Potion> displayed = this.list;
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        FontRenderer font = Fonts.getSize(15, Fonts.Type.INTER_BOLD);
        FontRenderer fontPotion = Fonts.getSize(13, Fonts.Type.INTER_BOLD);
        FontRenderer iconFont = Fonts.getSize(15, Fonts.Type.COMBINED_ICONS);
        int maxWidth = HudIconRail.headerWidth(font, this.getName());
        float rows = 0.0f;
        for (Potion potion : displayed) {
            class_1293 effect = potion.effect;
            String name = ((class_1291)effect.method_5579().comp_349()).method_5560().getString();
            String level = effect.method_5578() > 0 ? " " + (effect.method_5578() + 1) : "";
            maxWidth = Math.max(maxWidth, (int)Math.ceil(24.0f + fontPotion.getStringWidth(name + level) + 4.0f + HudIconRail.timerWidth(fontPotion, this.getDuration(effect)) + 4.0f));
            rows += Math.max(0.0f, Math.min(1.0f, potion.anim.getOutput().floatValue()));
        }
        this.setWidth(maxWidth);
        this.setHeight(HudIconRail.height(rows));
        HudIconRail.draw(this, matrix, blur, rectangle, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        HudIconRail.drawHeader(this, matrix, font, iconFont, this.getName(), "\ue004");
        float offset = 17.0f;
        for (Potion potion : displayed) {
            class_1293 effect = potion.effect;
            float rowAlpha = Math.max(0.0f, Math.min(1.0f, potion.anim.getOutput().floatValue()));
            float rowTop = (float)this.getY() + offset;
            String name = ((class_1291)effect.method_5579().comp_349()).method_5560().getString();
            String duration = this.getDuration(effect);
            String level = effect.method_5578() > 0 ? String.valueOf(class_124.field_1061) + " " + (effect.method_5578() + 1) + String.valueOf(class_124.field_1070) : "";
            matrix.method_22903();
            this.withRenderAlpha(rowAlpha, () -> {
                float warningAlpha = this.expirationBlink(effect);
                Matrix3x2fStack iconMatrices = context.method_51448();
                iconMatrices.pushMatrix();
                try {
                    iconMatrices.translate(HudIconRail.iconX(this, this.getX(), 8.0f), rowTop + 0.5f);
                    context.method_52707(class_10799.field_56883, class_329.method_71644((class_6880)effect.method_5579()), 0, 0, 8, 8, class_9848.method_61317((float)Render2DUtil.getRenderAlpha()));
                }
                finally {
                    iconMatrices.popMatrix();
                }
                HudIconRail.drawText(matrix, fontPotion, name + level, (float)this.getX() + 24.0f, rowTop, 9.0f, ColorUtil.multAlpha(ColorUtil.getText(), warningAlpha));
                float progress = effect.method_48559() || potion.maxDuration <= 0 ? 1.0f : Math.max(0.0f, Math.min(1.0f, (float)effect.method_5584() / (float)potion.maxDuration));
                HudIconRail.drawTimer(matrix, rectangle, arc, fontPotion, duration, (float)(this.getX() + this.getWidth()) - 4.0f, rowTop, progress, ColorUtil.multAlpha(ColorUtil.getText(), warningAlpha));
            });
            matrix.method_22909();
            offset += rowAlpha * 11.0f;
        }
    }

    private void drawHud2(class_332 context) {
        List<Potion> displayed = this.list.isEmpty() ? List.of(this.sidePreview) : this.list;
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        FontRenderer headerFont = Fonts.getSize(12, Fonts.Type.INTER_BOLD);
        FontRenderer rowFont = Fonts.getSize(11, Fonts.Type.INTER_BOLD);
        FontRenderer iconFont = Fonts.getSize(12, Fonts.Type.COMBINED_ICONS);
        int accent = Hud.getInstance().themeColor();
        int bg = ColorUtil.getColor(20, 20, 25, 0.47f);
        float headerH = 14.0f;
        float rowH = 15.0f;
        float iconBox = 11.0f;
        String headerIcon = "\ue004";
        float width = headerFont.getStringWidth(this.getName()) + 6.0f + iconFont.getStringWidth(headerIcon) + 4.0f + 6.0f;
        for (Potion potion : displayed) {
            class_1293 effect = potion.effect;
            String name = ((class_1291)effect.method_5579().comp_349()).method_5560().getString();
            String level = effect.method_5578() > 0 ? " " + (effect.method_5578() + 1) : "";
            width = Math.max(width, 6.0f + iconBox + 5.0f + rowFont.getStringWidth(name + level) + 12.0f + rowFont.getStringWidth(this.getDuration(effect)) + 8.0f);
        }
        float height = headerH + (float)displayed.size() * rowH + 2.0f;
        float x = this.getX();
        float y = this.getY();
        this.setWidth((int)Math.ceil(width));
        this.setHeight((int)Math.ceil(height));
        blur.render(ShapeProperties.create(matrix, x, y, width, height).round(6.0f).softness(3.5f).thickness(0.0f).quality(50.0f).outlineColor(0).color(bg).build());
        rectangle.render(ShapeProperties.create(matrix, x, y, width, height).round(6.0f).softness(1.0f).thickness(1.0f).quality(50.0f).outlineColor(ColorUtil.getOutline(0.3f)).color(0).build());
        iconFont.drawString(matrix, headerIcon, x + 6.0f, y + 2.5f, accent);
        headerFont.drawString(matrix, this.getName(), x + 6.0f + iconFont.getStringWidth(headerIcon) + 4.0f, y + 3.0f, ColorUtil.getText());
        rectangle.render(ShapeProperties.create(matrix, x + 5.0f, y + headerH, width - 10.0f, 0.5f).color(ColorUtil.getOutline(0.25f)).build());
        float rowY = y + headerH + 1.0f;
        for (Potion potion : displayed) {
            class_1293 effect = potion.effect;
            float alpha = Math.max(0.0f, Math.min(1.0f, potion.anim.getOutput().floatValue()));
            float warningAlpha = this.expirationBlink(effect);
            String name = ((class_1291)effect.method_5579().comp_349()).method_5560().getString();
            String level = effect.method_5578() > 0 ? " " + (effect.method_5578() + 1) : "";
            String duration = this.getDuration(effect);
            Matrix3x2fStack iconMatrices = context.method_51448();
            iconMatrices.pushMatrix();
            try {
                iconMatrices.translate(x + 6.0f, rowY + (rowH - iconBox) * 0.5f);
                context.method_52707(class_10799.field_56883, class_329.method_71644((class_6880)effect.method_5579()), 0, 0, (int)iconBox, (int)iconBox, class_9848.method_61317(alpha));
            }
            finally {
                iconMatrices.popMatrix();
            }
            HudIconRail.drawText(matrix, rowFont, name + level, x + 6.0f + iconBox + 5.0f, rowY, rowH, ColorUtil.multAlpha(ColorUtil.getText(), alpha * warningAlpha));
            float durationWidth = rowFont.getStringWidth(duration);
            HudIconRail.drawText(matrix, rowFont, duration, x + width - 8.0f - durationWidth, rowY, rowH, ColorUtil.multAlpha(accent, alpha * warningAlpha));
            rowY += rowH * alpha;
        }
    }

    private void drawSide(class_332 context) {
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        FontRenderer font = Fonts.getSize(13, Fonts.Type.INTER_BOLD);
        float y = this.getY();
        int widest = 0;
        List<Potion> displayed = this.list.isEmpty() && PlayerIntersectionUtil.isChat(Potions.mc.field_1755) ? List.of(this.sidePreview) : this.list;
        for (Potion potion : displayed) {
            class_1293 effect = potion.effect;
            float alpha = Math.max(0.0f, Math.min(1.0f, potion.anim.getOutput().floatValue()));
            float warningAlpha = this.expirationBlink(effect);
            String name = ((class_1291)effect.method_5579().comp_349()).method_5560().getString();
            String duration = this.getDuration(effect);
            String lvl = effect.method_5578() > 0 ? String.valueOf(class_124.field_1061) + " " + (effect.method_5578() + 1) + String.valueOf(class_124.field_1070) : "";
            int width = (int)Math.ceil(24.0f + Math.max(font.getStringWidth(name + lvl), font.getStringWidth(duration)) + 7.0f);
            float x = this.getX();
            blur.render(ShapeProperties.create(matrix, x, y, width, 24.0).round(5.0f).softness(3.5f).color(ColorUtil.multAlpha(-268435456, alpha)).build());
            context.method_52707(class_10799.field_56883, class_329.method_71644((class_6880)effect.method_5579()), Math.round(x + 4.0f), Math.round(y + 4.0f), 16, 16, class_9848.method_61317((float)alpha));
            font.drawString(matrix, name + lvl, x + 24.0f, y + 6.0f, ColorUtil.multAlpha(ColorUtil.getText(), alpha * warningAlpha));
            font.drawString(matrix, duration, x + 24.0f, y + 15.0f, ColorUtil.multAlpha(-8419434, alpha * warningAlpha));
            y += 26.0f * alpha;
            widest = Math.max(widest, width);
        }
        this.setWidth(widest);
        this.setHeight(Math.max(24, Math.round(y - (float)this.getY())));
    }

    private int sideHeight() {
        List<Potion> displayed = this.list.isEmpty() && PlayerIntersectionUtil.isChat(Potions.mc.field_1755) ? List.of(this.sidePreview) : this.list;
        float height = 0.0f;
        for (Potion potion : displayed) {
            height += 26.0f * Math.max(0.0f, Math.min(1.0f, potion.anim.getOutput().floatValue()));
        }
        return Math.max(24, Math.round(height));
    }

    private String getDuration(class_1293 effect) {
        int duration = effect.method_5584();
        int mins = duration / 1200;
        if (effect.method_48559() || mins > 60) {
            return "**:**";
        }
        int seconds = duration % 1200 / 20;
        return mins + ":" + (seconds < 10 ? "0" : "") + seconds;
    }

    private float expirationBlink(class_1293 effect) {
        if (effect.method_5584() == -1 || effect.method_5584() > 120) {
            return 1.0f;
        }
        return MathUtil.blinking(1000.0, 8.0f);
    }

    private static Potion createSidePreview() {
        List<class_6880> examples = List.of(class_1294.field_5904, class_1294.field_5910, class_1294.field_5918, class_1294.field_5924, class_1294.field_5898);
        class_6880 effect = examples.get(ThreadLocalRandom.current().nextInt(examples.size()));
        Animation animation = new DecelerateAnimation().setMs(1).setValue(1.0).complete();
        return new Potion(new class_1293(effect, 1800, 0), animation, 1800);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void withRenderAlpha(float alpha, Runnable runnable) {
        float previous = Render2DUtil.getRenderAlpha();
        Render2DUtil.setRenderAlpha(previous * alpha);
        try {
            runnable.run();
        }
        finally {
            Render2DUtil.setRenderAlpha(previous);
        }
    }

    private record Potion(class_1293 effect, Animation anim, int maxDuration) {
    }
}

