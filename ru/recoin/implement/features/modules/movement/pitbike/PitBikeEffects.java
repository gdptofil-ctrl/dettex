/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1922
 *  net.minecraft.class_2338
 *  net.minecraft.class_2350$class_2351
 *  net.minecraft.class_2374
 *  net.minecraft.class_2388
 *  net.minecraft.class_2394
 *  net.minecraft.class_2398
 *  net.minecraft.class_243
 *  net.minecraft.class_265
 *  net.minecraft.class_2680
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_3532
 *  net.minecraft.class_4587
 *  net.minecraft.class_638
 */
package ru.recoin.implement.features.modules.movement.pitbike;

import java.util.ArrayDeque;
import java.util.UUID;
import net.minecraft.class_1922;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2374;
import net.minecraft.class_2388;
import net.minecraft.class_2394;
import net.minecraft.class_2398;
import net.minecraft.class_243;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_638;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.common.util.other.StringUtil;
import ru.recoin.common.util.render.Render3DUtil;
import ru.recoin.implement.events.render.DrawEvent;
import ru.recoin.implement.events.render.WorldRenderEvent;
import ru.recoin.implement.features.modules.movement.pitbike.PitBikeEntity;
import ru.recoin.implement.screens.menu.MenuDescriptionLocalizer;

public final class PitBikeEffects {
    private static final int TRACK_LIFETIME = 120;
    private static final int MAX_TRACKS = 240;
    private final ArrayDeque<Track> tracks = new ArrayDeque();
    private class_638 world;
    private UUID bikeId;
    private class_243 lastRear;
    private class_243 lastFront;
    private int age;
    private double displayedSpeed;
    private final class_4587 hudMatrices = new class_4587();

    public void clear() {
        this.tracks.clear();
        this.world = null;
        this.bikeId = null;
        this.lastFront = null;
        this.lastRear = null;
        this.displayedSpeed = 0.0;
        this.age = 0;
    }

    public void tick(PitBikeEntity bike, boolean particles, boolean marks) {
        class_310 client = class_310.method_1551();
        if (client.field_1687 != this.world) {
            this.clear();
            this.world = client.field_1687;
        }
        if (this.world == null) {
            return;
        }
        ++this.age;
        while (!this.tracks.isEmpty() && this.age - this.tracks.peekFirst().born >= 120) {
            this.tracks.removeFirst();
        }
        if (!marks) {
            this.tracks.clear();
        }
        if (bike == null) {
            this.bikeId = null;
            this.lastFront = null;
            this.lastRear = null;
            this.displayedSpeed = 0.0;
            return;
        }
        if (!bike.method_5667().equals(this.bikeId)) {
            this.bikeId = bike.method_5667();
            this.lastFront = null;
            this.lastRear = null;
            this.displayedSpeed = 0.0;
        }
        double speed = bike.method_18798().method_37267();
        this.displayedSpeed += (speed * 72.0 - this.displayedSpeed) * 0.3;
        class_243 forward = new class_243(-Math.sin(Math.toRadians(bike.method_36454())), 0.0, Math.cos(Math.toRadians(bike.method_36454())));
        class_243 rear = bike.method_73189().method_1019(forward.method_1021(-0.7));
        class_243 front = bike.method_73189().method_1019(forward.method_1021(0.73));
        if (particles && speed > 0.035 && this.age % 2 == 0) {
            class_2680 surface;
            if (bike.method_5799()) {
                for (int i = 0; i < 4; ++i) {
                    double side = (this.world.field_9229.method_43058() - 0.5) * 0.45;
                    this.world.method_8406((class_2394)class_2398.field_11202, rear.field_1352 + forward.field_1350 * side, rear.field_1351 + 0.12, rear.field_1350 - forward.field_1352 * side, -bike.method_18798().field_1352 * 0.3 + forward.field_1350 * side, 0.1 + Math.min(0.15, speed * 0.2), -bike.method_18798().field_1350 * 0.3 - forward.field_1352 * side);
                }
            } else if (bike.method_24828() && !(surface = this.world.method_8320(class_2338.method_49638((class_2374)rear.method_1031(0.0, -0.1, 0.0)))).method_26215() && surface.method_26227().method_15769()) {
                for (int i = 0; i < (speed > 0.3 ? 3 : 1); ++i) {
                    this.world.method_8406((class_2394)new class_2388(class_2398.field_11217, surface), rear.field_1352 + (this.world.field_9229.method_43058() - 0.5) * 0.15, rear.field_1351 + 0.08, rear.field_1350 + (this.world.field_9229.method_43058() - 0.5) * 0.15, -bike.method_18798().field_1352 * 0.18, 0.035 + speed * 0.08, -bike.method_18798().field_1350 * 0.18);
                }
            }
        }
        if (!marks || !bike.method_24828() || bike.method_5799() || speed < 0.025) {
            this.lastFront = null;
            this.lastRear = null;
            return;
        }
        rear = this.contact(rear);
        front = bike.getWheelieAngle(1.0f) < 8.0f ? this.contact(front) : null;
        this.addTrack(this.lastRear, rear);
        this.addTrack(this.lastFront, front);
        this.lastRear = rear;
        this.lastFront = front;
    }

    private class_243 contact(class_243 wheel) {
        class_2338 pos = class_2338.method_49638((class_2374)wheel.method_1031(0.0, -0.08, 0.0));
        class_2680 state = this.world.method_8320(pos);
        if (!state.method_26227().method_15769()) {
            return null;
        }
        class_265 shape = state.method_26220((class_1922)this.world, pos);
        if (shape.method_1110()) {
            return null;
        }
        double y = (double)pos.method_10264() + shape.method_1105(class_2350.class_2351.field_11052);
        if (Math.abs(y - wheel.field_1351) > 0.18) {
            return null;
        }
        return new class_243(wheel.field_1352, y + 0.008, wheel.field_1350);
    }

    private void addTrack(class_243 from, class_243 to) {
        if (from == null || to == null || Math.abs(from.field_1351 - to.field_1351) > 0.1) {
            return;
        }
        double distance = from.method_1025(to);
        if (distance < 4.0E-4 || distance > 1.5) {
            return;
        }
        class_243 direction = to.method_1020(from).method_1021(1.0 / Math.sqrt(distance));
        class_243 width = new class_243(-direction.field_1350 * 0.045, 0.0, direction.field_1352 * 0.045);
        this.tracks.addLast(new Track(from.method_1019(width), to.method_1019(width), to.method_1020(width), from.method_1020(width), this.age));
        while (this.tracks.size() > 240) {
            this.tracks.removeFirst();
        }
    }

    public void renderTracks(WorldRenderEvent event) {
        if (class_310.method_1551().field_1687 != this.world) {
            return;
        }
        for (Track mark : this.tracks) {
            float life = 1.0f - ((float)(this.age - mark.born) + event.getPartialTicks()) / 120.0f;
            int alpha = (int)(class_3532.method_15363((float)life, (float)0.0f, (float)1.0f) * 100.0f);
            Render3DUtil.drawQuad(mark.a, mark.b, mark.c, mark.d, alpha << 24 | 0x171B20, true);
        }
    }

    public void drawDashboard(DrawEvent event, PitBikeEntity bike, boolean manual, String downBind, String upBind) {
        class_310 client = class_310.method_1551();
        if (client.field_1690.field_1842 || client.field_1755 != null) {
            return;
        }
        class_332 draw = event.getDrawContext();
        int x = client.method_22683().method_4486() / 2 - 110;
        int y = client.method_22683().method_4502() - 130;
        draw.method_25294(x, y, x + 220, y + 75, -652862686);
        draw.method_25294(x, y, x + 220, y + 2, -1029338);
        String speed = Integer.toString((int)Math.round(this.displayedSpeed));
        boolean russian = MenuDescriptionLocalizer.isRussian();
        Fonts.getSize(10, Fonts.Type.INTER_SEMI).drawString(this.hudMatrices, "PITBIKE", x + 10, y + 10, -4603185);
        Fonts.getSize(32, Fonts.Type.INTER_BOLD).drawString(this.hudMatrices, speed, x + 10, y + 30, -1);
        Fonts.getSize(11, Fonts.Type.INTER_MEDIUM).drawString(this.hudMatrices, russian ? "\u043a\u043c/\u0447" : "km/h", x + 64, y + 37, -7365717);
        int rpm = Math.round(bike.getEngineRpm() / 100.0f) * 100;
        Fonts.getSize(12, Fonts.Type.INTER_SEMI).drawString(this.hudMatrices, rpm + " RPM", x + 100, y + 32, -4603185);
        String gear = manual ? (bike.getGear() == 0 ? "N" : Integer.toString(bike.getGear())) : "AUTO";
        Fonts.getSize(9, Fonts.Type.INTER_MEDIUM).drawString(this.hudMatrices, russian ? "\u041f\u0415\u0420\u0415\u0414\u0410\u0427\u0410" : "GEAR", x + 170, y + 10, -7365717);
        Fonts.getSize(manual ? 28 : 14, Fonts.Type.INTER_BOLD).drawString(this.hudMatrices, gear, x + 175, y + 31, bike.getGear() == 0 && manual ? -9384035 : -1);
        int filled = class_3532.method_15340((int)Math.round(bike.getEngineRpm() / 9500.0f * 20.0f), (int)0, (int)20);
        for (int i = 0; i < 20; ++i) {
            int color = i >= filled ? -13617084 : (i < 13 ? -9384035 : (i < 17 ? -932005 : -1087917));
            draw.method_25294(x + 10 + i * 10, y + 49, x + 18 + i * 10, y + 55, color);
        }
        Object hint = manual ? downBind + " - / " + upBind + " +     N  1  2  3  4" : (russian ? "\u0410\u0412\u0422\u041e\u041c\u0410\u0422\u0418\u0427\u0415\u0421\u041a\u0418\u0419 \u041f\u0420\u0418\u0412\u041e\u0414" : "AUTOMATIC DRIVE");
        Fonts.getSize(10, Fonts.Type.INTER_MEDIUM).drawString(this.hudMatrices, StringUtil.ellipsize((String)hint, 200.0f, 10), x + 10, y + 66, -7365717);
    }

    private record Track(class_243 a, class_243 b, class_243 c, class_243 d, int born) {
    }
}

