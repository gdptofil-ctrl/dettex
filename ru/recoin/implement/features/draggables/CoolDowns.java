/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 *  net.minecraft.class_2596
 *  net.minecraft.class_2656
 *  net.minecraft.class_2724
 *  net.minecraft.class_332
 *  net.minecraft.class_4587
 *  net.minecraft.class_7923
 *  org.joml.Matrix3x2fStack
 */
package ru.recoin.implement.features.draggables;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_2596;
import net.minecraft.class_2656;
import net.minecraft.class_2724;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import net.minecraft.class_7923;
import org.joml.Matrix3x2fStack;
import ru.recoin.api.feature.draggable.AbstractDraggable;
import ru.recoin.api.system.animation.Animation;
import ru.recoin.api.system.animation.Direction;
import ru.recoin.api.system.animation.implement.DecelerateAnimation;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.other.StopWatch;
import ru.recoin.common.util.other.StringUtil;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.features.draggables.HudIconRail;

public class CoolDowns
extends AbstractDraggable {
    private static final int HUD_ICON_SIZE = 15;
    private static final float HUD_ICON_OFFSET_Y = 0.8f;
    public final List<CoolDown> list = new CopyOnWriteArrayList<CoolDown>();

    public static CoolDowns getInstance() {
        return Instance.getDraggable(CoolDowns.class);
    }

    public CoolDowns() {
        super("Cooldowns", 120, 10, 100, 23, true);
    }

    @Override
    public boolean visible() {
        return this.list.stream().anyMatch(c -> !c.anim.isFinished(Direction.BACKWARDS) && this.hasItemInInventory(c.item)) || PlayerIntersectionUtil.isChat(CoolDowns.mc.field_1755);
    }

    @Override
    public void tick() {
        this.list.removeIf(c -> c.anim.isFinished(Direction.BACKWARDS));
        if (CoolDowns.mc.field_1724 == null) {
            return;
        }
        for (CoolDown coolDown : this.list) {
            if (CoolDowns.mc.field_1724.method_7357().method_7904(coolDown.item.method_7854()) && this.hasItemInInventory(coolDown.item)) continue;
            coolDown.anim.setDirection(Direction.BACKWARDS);
        }
    }

    @Override
    public void packet(PacketEvent e) {
        if (PlayerIntersectionUtil.nullCheck()) {
            return;
        }
        class_2596<?> class_25962 = e.getPacket();
        Objects.requireNonNull(class_25962);
        class_2596<?> class_25963 = class_25962;
        if (class_25963 instanceof class_2656 c) {
            class_1792 item = (class_1792)class_7923.field_41178.method_63535(c.comp_3082());
            for (CoolDown coolDown : this.list) {
                if (!coolDown.item.equals(item)) continue;
                coolDown.anim.setDirection(Direction.BACKWARDS);
            }
            if (!(c.comp_2199() == 0 || !this.hasItemInInventory(item))) {
                long cooldownMillis = Math.max(0L, (long)c.comp_2199()) * 50L;
                this.list.add(new CoolDown(item, new StopWatch().setMs(-cooldownMillis), new DecelerateAnimation().setMs(150).setValue(1.0), cooldownMillis));
            }
        } else if (class_25963 instanceof class_2724 p) {
            this.list.clear();
        }
    }

    private boolean hasItemInInventory(class_1792 item) {
        if (CoolDowns.mc.field_1724 == null || item == null) {
            return false;
        }
        for (int i = 0; i < CoolDowns.mc.field_1724.method_31548().method_5439(); ++i) {
            if (!CoolDowns.mc.field_1724.method_31548().method_5438(i).method_31574(item)) continue;
            return true;
        }
        return CoolDowns.mc.field_1724.method_6079().method_31574(item);
    }

    @Override
    public void drawDraggable(class_332 context) {
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        FontRenderer font = Fonts.getSize(15, Fonts.Type.INTER_BOLD);
        FontRenderer fontCoolDown = Fonts.getSize(13, Fonts.Type.INTER_BOLD);
        FontRenderer iconFont = Fonts.getSize(15, Fonts.Type.COMBINED_ICONS);
        int maxWidth = HudIconRail.headerWidth(font, this.getName());
        float rows = 0.0f;
        for (CoolDown coolDown : this.list) {
            String name = coolDown.item.method_7854().method_7964().getString();
            long elapsed = coolDown.time.elapsedTime();
            int seconds = elapsed >= 0L ? 0 : (int)Math.min(Integer.MAX_VALUE, -elapsed / 1000L);
            maxWidth = Math.max(maxWidth, (int)Math.ceil(24.0f + fontCoolDown.getStringWidth(name) + 4.0f + HudIconRail.timerWidth(fontCoolDown, StringUtil.getDuration(seconds)) + 4.0f));
            rows += Math.max(0.0f, Math.min(1.0f, coolDown.anim.getOutput().floatValue()));
        }
        this.setWidth(maxWidth);
        this.setHeight(HudIconRail.height(rows));
        HudIconRail.draw(this, matrix, blur, rectangle, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        HudIconRail.drawHeader(this, matrix, font, iconFont, this.getName(), "\ue003");
        float offset = 17.0f;
        for (CoolDown coolDown : this.list) {
            float rowAlpha = Math.max(0.0f, Math.min(1.0f, coolDown.anim.getOutput().floatValue()));
            float rowTop = (float)this.getY() + offset;
            long elapsed = coolDown.time.elapsedTime();
            int seconds = elapsed >= 0L ? 0 : (int)Math.min(Integer.MAX_VALUE, -elapsed / 1000L);
            String name = coolDown.item.method_7854().method_7964().getString();
            String duration = StringUtil.getDuration(seconds);
            matrix.method_22903();
            this.withRenderAlpha(rowAlpha, () -> {
                this.drawItemIcon(context, coolDown.item.method_7854(), HudIconRail.iconX(this, this.getX(), 8.0f) - 1.0f, rowTop + 0.5f - 1.0f, rowAlpha);
                HudIconRail.drawText(matrix, fontCoolDown, name, (float)this.getX() + 24.0f, rowTop, 9.0f, ColorUtil.getText());
                float progress = coolDown.totalMillis <= 0L ? 0.0f : Math.max(0.0f, Math.min(1.0f, (float)Math.max(0L, -elapsed) / (float)coolDown.totalMillis));
                float warning = seconds <= 5 ? MathUtil.blinking(1000.0, 8.0f) : 1.0f;
                HudIconRail.drawTimer(matrix, rectangle, arc, fontCoolDown, duration, (float)(this.getX() + this.getWidth()) - 4.0f, rowTop, progress, ColorUtil.multGreen(ColorUtil.getText(), warning));
            });
            matrix.method_22909();
            offset += rowAlpha * 11.0f;
        }
    }

    private void drawItemIcon(class_332 context, class_1799 stack, float x, float y, float animation) {
        float scale = Math.max(0.0f, Math.min(1.0f, animation));
        if (scale <= 0.01f) {
            return;
        }
        Matrix3x2fStack matrices = context.method_51448();
        float centerX = x + 5.0f;
        float centerY = y + 5.0f;
        matrices.pushMatrix();
        matrices.translate(centerX, centerY);
        matrices.scale(scale, scale);
        matrices.translate(-centerX, -centerY);
        Render2DUtil.defaultDrawStack(context, stack, x, y, false, false, 0.5f);
        matrices.popMatrix();
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

    public record CoolDown(class_1792 item, StopWatch time, Animation anim, long totalMillis) {
    }
}

