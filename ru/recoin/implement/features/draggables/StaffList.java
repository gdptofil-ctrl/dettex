/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.class_1934
 *  net.minecraft.class_2561
 *  net.minecraft.class_268
 *  net.minecraft.class_332
 *  net.minecraft.class_4587
 *  net.minecraft.class_640
 */
package ru.recoin.implement.features.draggables;

import com.mojang.authlib.GameProfile;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import net.minecraft.class_1934;
import net.minecraft.class_2561;
import net.minecraft.class_268;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import net.minecraft.class_640;
import ru.recoin.api.feature.draggable.AbstractDraggable;
import ru.recoin.api.repository.staff.StaffRegistry;
import ru.recoin.api.system.animation.Animation;
import ru.recoin.api.system.animation.Direction;
import ru.recoin.api.system.animation.implement.DecelerateAnimation;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.implement.features.draggables.HudIconRail;
import ru.recoin.implement.features.draggables.Notifications;
import ru.recoin.implement.features.modules.render.Hud;

public class StaffList
extends AbstractDraggable {
    private static final int HUD_ICON_SIZE = 15;
    private static final float HUD_ICON_OFFSET_Y = 0.8f;
    public final Map<class_640, Animation> list = new LinkedHashMap<class_640, Animation>();
    private final Set<UUID> onlinePlayers = new HashSet<UUID>();
    private final Set<String> scoreboardPlayers = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
    private final List<String> staffPrefix = List.of("helper", "moder", "staff", "admin", "curator", "\u0441\u0442\u0430\u0436\u0451\u0440", "\u0441\u043e\u0442\u0440\u0443\u0434\u043d\u0438\u043a", "\u043f\u043e\u043c\u043e\u0449\u043d\u0438\u043a", "\u0430\u0434\u043c\u0438\u043d", "\u043c\u043e\u0434\u0435\u0440", "\u0445\u0435\u043b\u043f\u0435\u0440");

    public static StaffList getInstance() {
        return Instance.getDraggable(StaffList.class);
    }

    public StaffList() {
        super("Staff", 130, 40, 100, 23, true);
    }

    @Override
    public boolean visible() {
        return !this.list.isEmpty() || PlayerIntersectionUtil.isChat(StaffList.mc.field_1755);
    }

    @Override
    public void tick() {
        if (StaffList.mc.field_1724 == null || StaffList.mc.field_1687 == null) {
            this.list.clear();
            return;
        }
        Collection<class_640> playerList = StaffList.mc.field_1724.field_3944.method_2880();
        this.onlinePlayers.clear();
        HashMap<UUID, class_640> staffById = new HashMap<UUID, class_640>();
        HashMap<String, class_640> staffByName = new HashMap<String, class_640>();
        this.list.keySet().removeIf(entry -> {
            GameProfile profile = entry.method_2966();
            String name = profile.name().toLowerCase(Locale.ROOT);
            if (staffById.containsKey(profile.id()) || staffByName.containsKey(name)) {
                return true;
            }
            staffById.put(profile.id(), (class_640)entry);
            staffByName.put(name, (class_640)entry);
            return false;
        });
        for (class_640 entry2 : playerList) {
            String prefix;
            GameProfile profile = entry2.method_2966();
            class_2561 displayName = entry2.method_2971();
            if (profile == null) continue;
            this.onlinePlayers.add(profile.id());
            boolean manuallyAdded = StaffRegistry.contains(profile.name());
            if (displayName == null && !manuallyAdded) continue;
            String string = prefix = displayName == null ? "" : displayName.getString().replace(profile.name(), "");
            if (prefix.length() < 2 && !manuallyAdded) continue;
            class_2561 staffDisplayName = displayName == null ? class_2561.method_43470((String)profile.name()) : displayName;
            String name = profile.name().toLowerCase(Locale.ROOT);
            class_640 existing = (class_640)staffById.get(profile.id());
            if (existing == null) {
                existing = (class_640)staffByName.get(name);
            }
            if (existing != null) {
                existing.method_2962(staffDisplayName);
                continue;
            }
            String lowerPrefix = prefix.toLowerCase(Locale.ROOT);
            boolean isStaff = manuallyAdded;
            if (!isStaff) {
                for (String staff : this.staffPrefix) {
                    if (!lowerPrefix.contains(staff)) continue;
                    isStaff = true;
                    break;
                }
            }
            if (!isStaff) continue;
            class_640 player = new class_640(profile, false);
            player.method_2962(staffDisplayName);
            this.list.put(player, new DecelerateAnimation().setMs(150).setValue(1.0));
            staffById.put(profile.id(), player);
            staffByName.put(name, player);
            if (!Hud.getInstance().notificationSettings.isSelected("Staff Join")) continue;
            Notifications.getInstance().addList((class_2561)class_2561.method_43473().method_10852(player.method_2971()).method_27693(" - \u0417\u0430\u0448\u0435\u043b \u043d\u0430 \u0441\u0435\u0440\u0432\u0435\u0440!"), 5000L);
        }
        boolean scoreboardIndexed = false;
        for (Map.Entry<class_640, Animation> staff : this.list.entrySet()) {
            String name;
            boolean onScoreboard;
            boolean inTab = this.onlinePlayers.contains(staff.getKey().method_2966().id());
            if (!inTab && !scoreboardIndexed) {
                this.scoreboardPlayers.clear();
                if (StaffList.mc.field_1687 != null) {
                    for (class_268 team : StaffList.mc.field_1687.method_8428().method_1159()) {
                        this.scoreboardPlayers.addAll(team.method_1204());
                    }
                }
                scoreboardIndexed = true;
            }
            boolean bl = onScoreboard = (name = staff.getKey().method_2966().name()) != null && !name.isBlank() && this.scoreboardPlayers.contains(name);
            if (!inTab && !onScoreboard) {
                staff.getValue().setDirection(Direction.BACKWARDS);
                continue;
            }
            staff.getValue().setDirection(Direction.FORWARDS);
        }
        this.list.values().removeIf(s -> s.isFinished(Direction.BACKWARDS));
        super.tick();
    }

    @Override
    public void drawDraggable(class_332 context) {
        if (Hud.getInstance().isHud2()) {
            this.drawHud2(context);
            return;
        }
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        FontRenderer font = Fonts.getSize(15, Fonts.Type.INTER_BOLD);
        FontRenderer fontPlayer = Fonts.getSize(13, Fonts.Type.INTER_BOLD);
        FontRenderer iconFont = Fonts.getSize(15, Fonts.Type.COMBINED_ICONS);
        int maxWidth = HudIconRail.headerWidth(font, this.getName());
        float rows = 0.0f;
        for (Map.Entry<class_640, Animation> staff : this.list.entrySet()) {
            class_640 player = staff.getKey();
            if (player == null) continue;
            class_2561 text = this.staffName(player);
            maxWidth = Math.max(maxWidth, (int)Math.ceil(24.0f + fontPlayer.getStringWidth(text) + 4.0f + 3.2f + 4.0f));
            rows += Math.max(0.0f, Math.min(1.0f, staff.getValue().getOutput().floatValue()));
        }
        this.setWidth(maxWidth);
        this.setHeight(HudIconRail.height(rows));
        HudIconRail.draw(this, matrix, blur, rectangle, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        HudIconRail.drawHeader(this, matrix, font, iconFont, this.getName(), "\ue04a");
        float offset = 17.0f;
        for (Map.Entry<class_640, Animation> staff : this.list.entrySet()) {
            class_640 player = staff.getKey();
            if (player == null) continue;
            class_2561 text = this.staffName(player);
            float rowTop = (float)this.getY() + offset;
            float rowAlpha = Math.max(0.0f, Math.min(1.0f, staff.getValue().getOutput().floatValue()));
            matrix.method_22903();
            this.withRenderAlpha(rowAlpha, () -> {
                Render2DUtil.drawRoundedTexture(context, player.method_52810().comp_1626().comp_3626(), HudIconRail.iconX(this, this.getX(), 7.0f), rowTop + 1.0f, 7.0f, 2.5f, 8.0f, 8.0f, 8, 8, 64, 64, -1);
                fontPlayer.drawText(matrix, text, (float)this.getX() + 24.0f - fontPlayer.getStringBounds(text.getString()).x(), HudIconRail.textY(fontPlayer, text.getString(), rowTop, 9.0f));
                rectangle.render(ShapeProperties.create(matrix, (float)(this.getX() + this.getWidth()) - 4.0f - 3.2f, rowTop + 2.9f, 3.2f, 3.2f).round(1.6f).color(this.getStaffStatusColor(player)).build());
            });
            matrix.method_22909();
            offset += rowAlpha * 11.0f;
        }
    }

    private void drawHud2(class_332 context) {
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        FontRenderer headerFont = Fonts.getSize(12, Fonts.Type.INTER_BOLD);
        FontRenderer rowFont = Fonts.getSize(11, Fonts.Type.INTER_BOLD);
        FontRenderer iconFont = Fonts.getSize(12, Fonts.Type.COMBINED_ICONS);
        int accent = Hud.getInstance().themeColor();
        int bg = ColorUtil.getColor(20, 20, 25, 0.47f);
        float headerH = 10.0f;
        float rowH = 12.0f;
        float gap = 3.0f;
        float width = 55.0f;
        for (Map.Entry<class_640, Animation> staff : this.list.entrySet()) {
            class_640 player = staff.getKey();
            if (player == null) continue;
            width = Math.max(width, 8.0f + 4.0f + rowFont.getStringWidth(this.staffName(player).getString()) + 6.0f + 4.0f + 6.0f);
        }
        float height = this.list.isEmpty() ? headerH + gap + rowH : headerH + gap + (float)this.list.size() * (rowH + gap);
        float x = this.getX();
        float y = this.getY();
        this.setWidth((int)Math.ceil(width));
        this.setHeight((int)Math.ceil(height));
        blur.render(ShapeProperties.create(matrix, x, y, width, headerH).round(5.0f).softness(3.5f).thickness(0.0f).quality(50.0f).outlineColor(0).color(bg).build());
        iconFont.drawString(matrix, "\ue04a", x + 5.0f, y + 1.5f, accent);
        headerFont.drawString(matrix, "Staff", x + 15.0f, y + 2.5f, ColorUtil.getText());
        float rowY = y + headerH + gap;
        if (this.list.isEmpty()) {
            blur.render(ShapeProperties.create(matrix, x, rowY, width, rowH).round(5.0f).softness(3.5f).thickness(0.0f).quality(50.0f).outlineColor(0).color(bg).build());
            rowFont.drawString(matrix, "\u041d\u0435\u0442 \u0441\u0442\u0430\u0444\u0444\u0430", x + 6.0f, rowY + 3.0f, ColorUtil.getText(0.7f));
            return;
        }
        for (Map.Entry<class_640, Animation> staff : this.list.entrySet()) {
            class_640 player = staff.getKey();
            if (player == null) continue;
            blur.render(ShapeProperties.create(matrix, x, rowY, width, rowH).round(5.0f).softness(3.5f).thickness(0.0f).quality(50.0f).outlineColor(0).color(bg).build());
            Render2DUtil.drawRoundedTexture(context, player.method_52810().comp_1626().comp_3626(), x + 5.0f, rowY + 2.0f, 8.0f, 2.5f, 8.0f, 8.0f, 8, 8, 64, 64, -1);
            rowFont.drawString(matrix, this.staffName(player).getString(), x + 5.0f + 8.0f + 4.0f, rowY + 3.0f, ColorUtil.getText());
            float dot = 3.2f;
            rectangle.render(ShapeProperties.create(matrix, (x + width) - 4.0f - dot, rowY + (rowH - dot) * 0.5f, dot, dot).round(dot * 0.5f).color(this.getStaffStatusColor(player)).build());
            rowY += rowH + gap;
        }
    }

    private class_2561 staffName(class_640 player) {
        return player.method_2971() != null ? player.method_2971() : class_2561.method_43470((String)player.method_2966().name());
    }

    private int getStaffStatusColor(class_640 entry) {
        class_640 onlineEntry;
        class_640 class_6402 = onlineEntry = mc.method_1562() == null ? null : mc.method_1562().method_2871(entry.method_2966().id());
        if (onlineEntry == null || onlineEntry.method_2958() == class_1934.field_9219) {
            return -43691;
        }
        if (StaffList.mc.field_1687 != null && StaffList.mc.field_1687.method_18470(entry.method_2966().id()) != null) {
            return -22746;
        }
        return -11141291;
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
}

