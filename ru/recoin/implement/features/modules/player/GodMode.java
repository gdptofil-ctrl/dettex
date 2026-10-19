/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.class_1297
 *  net.minecraft.class_1297$class_5529
 *  net.minecraft.class_1934
 *  net.minecraft.class_238
 *  net.minecraft.class_243
 *  net.minecraft.class_2561
 *  net.minecraft.class_2661
 *  net.minecraft.class_2678
 *  net.minecraft.class_2708
 *  net.minecraft.class_2724
 *  net.minecraft.class_419
 *  net.minecraft.class_634
 *  net.minecraft.class_636
 *  net.minecraft.class_638
 *  net.minecraft.class_640
 *  net.minecraft.class_745
 *  net.minecraft.class_746
 */
package ru.recoin.implement.features.modules.player;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.class_1297;
import net.minecraft.class_1934;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2661;
import net.minecraft.class_2678;
import net.minecraft.class_2708;
import net.minecraft.class_2724;
import net.minecraft.class_419;
import net.minecraft.class_634;
import net.minecraft.class_636;
import net.minecraft.class_638;
import net.minecraft.class_640;
import net.minecraft.class_745;
import net.minecraft.class_746;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.entity.MovingUtil;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.render.Render3DUtil;
import ru.recoin.core.Main;
import ru.recoin.implement.events.container.SetScreenEvent;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.events.player.GlobalTickEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.events.render.CameraEvent;
import ru.recoin.implement.events.render.DrawEvent;
import ru.recoin.implement.events.render.WorldLoadEvent;
import ru.recoin.implement.events.render.WorldRenderEvent;
import ru.recoin.implement.features.modules.misc.FreeCam;
import ru.recoin.implement.features.modules.movement.AirStuck;
import ru.recoin.implement.features.modules.movement.Blink;
import ru.recoin.implement.features.modules.player.GodModePacketBlocker;
import ru.recoin.mixins.PlayerListEntryAccessor;

public final class GodMode
extends Module {
    private static final float SPEED = 1.5f;
    private static final long WAIT_NANOS = 7000000000L;
    private volatile Session session;
    private final SessionListener sessionListener = new SessionListener();
    private boolean listenerAttached;
    private boolean keepGhost;
    private class_238 ghostBox;
    private class_638 ghostWorld;

    public GodMode() {
        super("GodMode", "God Mode", ModuleCategory.PLAYER);
    }

    @Override
    public void activate() {
        Session current;
        this.clearGhost();
        if (this.session != null) {
            this.finishSession(this.session, true, false);
        }
        if (GodMode.mc.field_1724 == null || GodMode.mc.field_1687 == null || mc.method_1562() == null || GodMode.mc.field_1761 == null || !GodMode.mc.field_1724.method_5805() || GodMode.mc.field_1724.method_5765()) {
            this.setState(false);
            return;
        }
        this.disableConflict(AirStuck.getInstance());
        this.disableConflict(Instance.get(Blink.class));
        this.disableConflict(FreeCam.getInstance());
        this.session = current = new Session(GodMode.mc.field_1724, GodMode.mc.field_1687, mc.method_1562(), GodMode.mc.field_1761);
        this.attachListener();
        GodModePacketBlocker.attach(current.handler.method_48296());
        if (current.entry != null) {
            ((PlayerListEntryAccessor)current.entry).recoin$setGameMode(class_1934.field_9219);
        }
        current.world.method_53875((class_1297)current.fakePlayer);
        current.interactionManager.method_2907(class_1934.field_9216);
        current.player.method_5660(true);
        this.message(current, "\u041a\u0430\u043a \u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u0442\u044c\u0441\u044f GodMode:");
        this.message(current, "1. \u0416\u0434\u0438 7 \u0441\u0435\u043a\u0443\u043d\u0434 \u2014 \u0438\u0434\u0451\u0442 \u043e\u0442\u0441\u0447\u0451\u0442.");
        this.message(current, "2. \u0417\u0430\u0439\u0434\u0438 \u0437\u0430 \u0441\u0442\u0435\u043d\u043a\u0443 \u0441\u0432\u043e\u0435\u0439 \u043c\u043e\u0434\u0435\u043b\u044c\u043a\u0438.");
        this.message(current, "3. \u0425\u043e\u0447\u0435\u0448\u044c \u0432\u044b\u0439\u0442\u0438 \u2014 \u0441\u044a\u0435\u0448\u044c \u0445\u043e\u0440\u0443\u0441.");
        this.message(current, "\u041e\u0442\u0441\u0447\u0451\u0442: 7 \u0441\u0435\u043a");
    }

    @Override
    public void deactivate() {
        Session current = this.session;
        if (current != null) {
            this.finishSession(current, true, this.keepGhost);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void onTick(TickEvent event) {
        Session current = this.session;
        if (current == null) {
            return;
        }
        if (!this.matches(current) || !current.player.method_5805() || current.player.method_31481()) {
            this.stop(current, this.matches(current));
            return;
        }
        long elapsed = System.nanoTime() - current.startedAt;
        if (elapsed >= 7000000000L) {
            this.message(current, "7 \u0441\u0435\u043a\u0443\u043d\u0434 \u043f\u0440\u043e\u0448\u043b\u0438 \u2014 GodMode \u0432\u044b\u043a\u043b\u044e\u0447\u0435\u043d.");
            this.keepGhost = true;
            try {
                this.setState(false);
            }
            finally {
                this.keepGhost = false;
            }
            return;
        }
        int left = (int)((7000000000L - elapsed + 999999999L) / 1000000000L);
        if (left < current.lastSeconds) {
            current.lastSeconds = left;
            this.message(current, "\u041e\u0442\u0441\u0447\u0451\u0442: " + left + " \u0441\u0435\u043a");
        }
        double vertical = GodMode.mc.field_1690.field_1832.method_1434() ? -1.125 : (GodMode.mc.field_1690.field_1903.method_1434() ? 1.125 : 0.0);
        MovingUtil.setVelocity(1.5, vertical);
    }

    public void onPacket(PacketEvent event) {
        Session current = this.session;
        if (current == null) {
            return;
        }
        if (event.isReceive() && (event.getPacket() instanceof class_2678 || event.getPacket() instanceof class_2724 || event.getPacket() instanceof class_2661)) {
            mc.execute(() -> this.stop(current, false));
            return;
        }
        if (!this.matches(current)) {
            return;
        }
        if (event.isSend() && GodModePacketBlocker.isBlocked(current.handler.method_48296(), event.getPacket())) {
            event.cancel();
        } else if (event.isReceive() && event.getPacket() instanceof class_2708) {
            event.cancel();
        }
    }

    public void onCamera(CameraEvent event) {
        Session current = this.session;
        if (current == null || !this.matches(current)) {
            return;
        }
        event.setCameraClip(true);
        event.cancel();
    }

    public void onDraw(DrawEvent event) {
        Session current = this.session;
        if (current == null || !this.matches(current)) {
            return;
        }
        long remaining = Math.max(0L, 7000000000L - (System.nanoTime() - current.startedAt));
        long seconds = (remaining + 999999999L) / 1000000000L;
        Fonts.getSize(16, Fonts.Type.INTER_BOLD).drawCenteredString(MatrixStackCompat.from(event.getDrawContext().method_51448()), "\u0416\u0434\u0438\u0442\u0435 " + seconds + " \u0441\u0435\u043a\u0443\u043d\u0434", (float)window.getScaledWidth() / 2.0f, (float)window.getScaledHeight() / 2.0f - 30.0f, ColorUtil.getText());
    }

    private boolean matches(Session current) {
        return GodMode.mc.field_1724 == current.player && GodMode.mc.field_1687 == current.world && mc.method_1562() == current.handler && GodMode.mc.field_1761 == current.interactionManager;
    }

    private void stop(Session current, boolean restorePlayer) {
        if (this.session != current) {
            return;
        }
        this.finishSession(current, restorePlayer, false);
        this.setState(false);
    }

    private void finishSession(Session current, boolean restorePlayer, boolean leaveGhost) {
        if (this.session != current) {
            return;
        }
        this.session = null;
        GodModePacketBlocker.detach(current.handler.method_48296());
        if (leaveGhost && this.matches(current)) {
            this.ghostBox = current.fakePlayer.method_5829();
            this.ghostWorld = current.world;
        }
        if (current.world.method_8469(current.fakePlayer.method_5628()) == current.fakePlayer) {
            current.world.method_2945(current.fakePlayer.method_5628(), class_1297.class_5529.field_26999);
        } else {
            current.fakePlayer.method_31472();
        }
        if (current.entry != null && current.previousEntryMode != null) {
            ((PlayerListEntryAccessor)current.entry).recoin$setGameMode(current.previousEntryMode);
        }
        if (GodMode.mc.field_1761 == current.interactionManager && mc.method_1562() == current.handler && current.previousGameMode != null) {
            current.interactionManager.method_2907(current.previousGameMode);
        }
        if (restorePlayer && this.matches(current)) {
            current.player.method_18799(class_243.field_1353);
            if (current.player.method_5805()) {
                current.player.method_5814(current.origin.field_1352, current.origin.field_1351, current.origin.field_1350);
            }
            current.player.method_5660(current.previousSneaking);
        }
        this.detachListenerIfIdle();
    }

    private void clearGhost() {
        this.ghostBox = null;
        this.ghostWorld = null;
        this.detachListenerIfIdle();
    }

    private void attachListener() {
        if (this.listenerAttached) {
            return;
        }
        Main.getInstance().getEventManager().register(this.sessionListener);
        this.listenerAttached = true;
    }

    private void detachListenerIfIdle() {
        if (!this.listenerAttached || this.session != null || this.ghostBox != null) {
            return;
        }
        Main.getInstance().getEventManager().unregister(this.sessionListener);
        this.listenerAttached = false;
    }

    private void disableConflict(Module module) {
        if (module != null && module.isState()) {
            module.setState(false);
        }
    }

    private void message(Session current, String text) {
        current.player.method_7353((class_2561)class_2561.method_43470((String)text), false);
    }

    private final class SessionListener {
        private SessionListener() {
        }

        @EventHandler(value=4)
        public void onTick(TickEvent event) {
            GodMode.this.onTick(event);
        }

        @EventHandler
        public void onGlobalTick(GlobalTickEvent event) {
            Session current = GodMode.this.session;
            if (!(current == null || GodMode.this.isState() && GodMode.this.matches(current))) {
                GodMode.this.stop(current, GodMode.this.matches(current));
            }
            if (GodMode.this.ghostBox != null && (QuickImports.mc.field_1687 != GodMode.this.ghostWorld || QuickImports.mc.field_1724 == null)) {
                GodMode.this.clearGhost();
            }
        }

        @EventHandler(value=4)
        public void onPacket(PacketEvent event) {
            GodMode.this.onPacket(event);
        }

        @EventHandler(value=4)
        public void onCamera(CameraEvent event) {
            GodMode.this.onCamera(event);
        }

        @EventHandler
        public void onWorldLoad(WorldLoadEvent event) {
            Session current = GodMode.this.session;
            if (current != null) {
                GodMode.this.stop(current, false);
            }
            GodMode.this.clearGhost();
        }

        @EventHandler
        public void onSetScreen(SetScreenEvent event) {
            if (event.getScreen() instanceof class_419) {
                Session current = GodMode.this.session;
                if (current != null) {
                    GodMode.this.stop(current, false);
                }
                GodMode.this.clearGhost();
            }
        }

        @EventHandler
        public void onWorldRender(WorldRenderEvent event) {
            if (GodMode.this.ghostBox != null && QuickImports.mc.field_1687 == GodMode.this.ghostWorld && QuickImports.mc.field_1724 != null) {
                Render3DUtil.drawBox(GodMode.this.ghostBox, ColorUtil.getClientColor(), 1.5f, true, true, false);
            }
        }

        @EventHandler
        public void onDraw(DrawEvent event) {
            GodMode.this.onDraw(event);
        }
    }

    private static final class Session {
        private final class_746 player;
        private final class_638 world;
        private final class_634 handler;
        private final class_636 interactionManager;
        private final class_243 origin;
        private final class_1934 previousGameMode;
        private final class_640 entry;
        private final class_1934 previousEntryMode;
        private final boolean previousSneaking;
        private final class_745 fakePlayer;
        private final long startedAt = System.nanoTime();
        private int lastSeconds = 7;

        private Session(class_746 player, class_638 world, class_634 handler, class_636 interactionManager) {
            this.player = player;
            this.world = world;
            this.handler = handler;
            this.interactionManager = interactionManager;
            this.origin = player.method_73189();
            this.previousGameMode = interactionManager.method_2920();
            this.entry = handler.method_2871(player.method_5667());
            this.previousEntryMode = this.entry == null ? null : this.entry.method_2958();
            this.previousSneaking = player.method_5715();
            this.fakePlayer = new class_745(world, new GameProfile(UUID.randomUUID(), player.method_7334().name()));
            int entityId = -7340;
            while (world.method_8469(entityId) != null) {
                --entityId;
            }
            this.fakePlayer.method_5838(entityId);
            this.fakePlayer.method_31548().method_7377(player.method_31548());
            this.fakePlayer.method_6033(player.method_6032());
            this.fakePlayer.method_6073(player.method_6067());
            this.fakePlayer.method_5808(this.origin.field_1352, this.origin.field_1351, this.origin.field_1350, player.method_36454(), player.method_36455());
            this.fakePlayer.method_5847(player.method_5791());
            this.fakePlayer.method_5636(player.method_73188());
        }
    }
}

