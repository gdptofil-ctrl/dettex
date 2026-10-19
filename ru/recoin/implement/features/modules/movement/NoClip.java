/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10185
 *  net.minecraft.class_124
 *  net.minecraft.class_1268
 *  net.minecraft.class_1297
 *  net.minecraft.class_1799
 *  net.minecraft.class_1890
 *  net.minecraft.class_1893
 *  net.minecraft.class_1922
 *  net.minecraft.class_2338
 *  net.minecraft.class_2350
 *  net.minecraft.class_2374
 *  net.minecraft.class_238
 *  net.minecraft.class_2382
 *  net.minecraft.class_243
 *  net.minecraft.class_2561
 *  net.minecraft.class_2596
 *  net.minecraft.class_2620
 *  net.minecraft.class_2626
 *  net.minecraft.class_2637
 *  net.minecraft.class_265
 *  net.minecraft.class_2673
 *  net.minecraft.class_2680
 *  net.minecraft.class_2767
 *  net.minecraft.class_2827
 *  net.minecraft.class_2846
 *  net.minecraft.class_2846$class_2847
 *  net.minecraft.class_3489
 *  net.minecraft.class_4050
 *  net.minecraft.class_634
 *  net.minecraft.class_6374
 *  net.minecraft.class_638
 *  net.minecraft.class_6880
 *  net.minecraft.class_7924
 */
package ru.recoin.implement.features.modules.movement;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.class_10185;
import net.minecraft.class_124;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1799;
import net.minecraft.class_1890;
import net.minecraft.class_1893;
import net.minecraft.class_1922;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2374;
import net.minecraft.class_238;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2596;
import net.minecraft.class_2620;
import net.minecraft.class_2626;
import net.minecraft.class_2637;
import net.minecraft.class_265;
import net.minecraft.class_2673;
import net.minecraft.class_2680;
import net.minecraft.class_2767;
import net.minecraft.class_2827;
import net.minecraft.class_2846;
import net.minecraft.class_3489;
import net.minecraft.class_4050;
import net.minecraft.class_634;
import net.minecraft.class_6374;
import net.minecraft.class_638;
import net.minecraft.class_6880;
import net.minecraft.class_7924;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.events.player.InputEvent;
import ru.recoin.implement.events.player.MotionEvent;
import ru.recoin.implement.events.player.PostTickEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.events.render.CameraPositionEvent;
import ru.recoin.implement.features.modules.movement.noclip.NoClipCameraSmoothing;
import ru.recoin.implement.features.modules.movement.noclip.NoClipHiddenBlocks;
import ru.recoin.implement.features.modules.movement.noclip.NoClipStopwatch;

public class NoClip
extends Module {
    private static NoClip instance;
    private static final Set<class_2338> BREAKING_BLOCKS;
    private final SelectSetting mode;
    private final ValueSetting flightDelay;
    private final ValueSetting jumpDelay;
    private final ValueSetting breakingSpeed;
    private final BooleanSetting increasePing;
    private final ValueSetting pingDelay;
    private final BooleanSetting reorderBreaking;
    private final BooleanSetting oneBlockCrawling;
    private final BooleanSetting antiCrawl;
    private final BooleanSetting dearBypass;
    private final BooleanSetting alternateDistance;
    private final BooleanSetting breakOccupiedBlocks;
    private final BooleanSetting antiPhantom;
    private final BooleanSetting fixHitboxShrink;
    private final ValueSetting rebreakInterval;
    private final transient NoClipStopwatch jumpTimer;
    private final transient NoClipStopwatch rebreakTimer;
    private final transient List<class_2338> breakingQueue;
    private final ValueSetting blocksPerSecond;
    private final BooleanSetting hideBreaking;
    private final transient List<class_2338> packetBreakingQueue;
    private final transient Map<class_2338, Long> pendingBlocks;
    private final transient NoClipStopwatch bypassTimer;
    private final transient CopyOnWriteArrayList<class_2596<?>> delayedPackets;
    private final transient NoClipStopwatch packetTimer;
    private transient class_2338 currentBreakingBlock;
    private transient long breakingStartedAt;
    private transient int restoreSlot;
    private transient boolean breaking;
    private transient class_2338 currentPacketBlock;
    private transient long packetBreakStartedAt;
    private transient long nextPacketBreakAt;
    private transient boolean bypassBreaking;
    private transient boolean bypassWaiting;
    private transient int bypassDistance;
    private transient class_2338 bypassOrigin;
    private transient boolean flushingPackets;
    private transient boolean instantBreak;
    private transient boolean pendingJump;
    private transient long lastWarningAt;

    private void restoreSelectedSlot() {
        if (this.restoreSlot != -1) {
            if (NoClip.mc.field_1724 != null && NoClip.mc.field_1761 != null) {
                NoClip.mc.field_1724.method_31548().method_61496(this.restoreSlot);
                NoClip.mc.field_1761.method_2911();
            }
            this.restoreSlot = -1;
        }
    }

    private boolean isMiningTool(class_1799 stack) {
        return stack.method_31573(class_3489.field_42614) || stack.method_31573(class_3489.field_42612) || stack.method_31573(class_3489.field_42615);
    }

    private class_2561 enchantmentWarning() {
        return class_2561.method_43470((String)"\u0412\u041d\u0418\u041c\u0410\u041d\u0418\u0415 !!! ").method_27695(new class_124[]{class_124.field_1061, class_124.field_1067}).method_10852((class_2561)class_2561.method_43470((String)"\u0414\u043b\u044f \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u044f \u043c\u043e\u0434\u0443\u043b\u044f \u043d\u0443\u0436\u043d\u043e \u0437\u0430\u0447\u0430\u0440\u043e\u0432\u0430\u043d\u0438\u0435 \u043d\u0430 \u043a\u0438\u0440\u043a\u0435/\u0442\u043e\u043f\u043e\u0440\u0435/\u043b\u043e\u043f\u0430\u0442\u0435 \u044d\u0444\u0444\u0435\u043a\u0442\u0438\u0432\u043d\u043e\u0441\u0442\u044c 7. \u0418\u043d\u0430\u0447\u0435 \u043c\u043e\u0434\u0443\u043b\u044c \u0431\u0443\u0434\u0435\u0442 \u0440\u0430\u0431\u043e\u0442\u0430\u0442\u044c \u0445\u0443\u0436\u0435.").method_27692(class_124.field_1054));
    }

    private void clearSneaking() {
        NoClip.mc.field_1724.method_5660(false);
        if (NoClip.mc.field_1724.field_3913 != null && NoClip.mc.field_1724.field_3913.field_54155 != null) {
            class_10185 input = NoClip.mc.field_1724.field_3913.field_54155;
            NoClip.mc.field_1724.field_3913.field_54155 = new class_10185(input.comp_3159(), input.comp_3160(), input.comp_3161(), input.comp_3162(), input.comp_3163(), false, input.comp_3165());
        }
    }

    private void finishPacketBreak() {
        this.restoreSelectedSlot();
        this.currentPacketBlock = null;
        this.nextPacketBreakAt = System.currentTimeMillis() + (long)(1000.0 / (double)this.blocksPerSecond.getValue());
    }

    private void breakWithProgress(class_2338 pos) {
        if (pos != null && !NoClip.mc.field_1687.method_22347(pos) && this.selectMiningTool(pos)) {
            this.trackBlock(pos);
            class_2350 face = this.closestFace(pos);
            NoClip.mc.field_1761.method_2910(pos, face);
            this.swing();
            NoClip.mc.field_1761.field_3716 = 0;
            NoClip.mc.field_1761.field_3715 = this.breakingSpeed.getValue();
            NoClip.mc.field_1761.method_2902(pos, face);
        }
    }

    private void tickBypass() {
        if (!this.bypassWaiting) {
            if (!this.bypassBreaking) {
                this.queueBypassBlocks(this.bypassDistance);
                this.bypassOrigin = this.playerBlockPos();
                this.bypassBreaking = true;
            }
            this.tickPacketBreaking();
            if (this.packetBreakingQueue.isEmpty() && this.currentPacketBlock == null) {
                this.bypassBreaking = false;
                this.bypassWaiting = true;
                this.bypassTimer.reset();
            }
        } else {
            int travelledDistance = this.distanceFromBypassOrigin(this.playerBlockPos());
            if (travelledDistance >= this.bypassDistance || this.bypassTimer.hasElapsed(4000L)) {
                this.bypassWaiting = false;
                if (this.alternateDistance.isValue()) {
                    this.bypassDistance = this.bypassDistance == 1 ? 2 : 1;
                }
            }
        }
    }

    private void swing() {
        if (NoClip.mc.field_1724 != null) {
            NoClip.mc.field_1724.method_6104(class_1268.field_5808);
        }
    }

    private void expirePendingBlocks() {
        long expirationCutoff = System.currentTimeMillis() - 8000L;
        this.pendingBlocks.forEach((pos, trackedAt) -> {
            if (trackedAt < expirationCutoff && this.pendingBlocks.remove(pos, trackedAt)) {
                BREAKING_BLOCKS.remove(pos);
            }
        });
    }

    private void queueBypassBlocks(int distance) {
        this.packetBreakingQueue.clear();
        class_2338 origin = this.playerBlockPos();
        class_2350 facing = NoClip.mc.field_1724.method_5735();
        for (int forwardOffset = 1; forwardOffset <= distance; ++forwardOffset) {
            class_2338 columnPos = origin.method_10079(facing, forwardOffset);
            for (int heightOffset = 0; heightOffset < 2; ++heightOffset) {
                class_2338 candidatePos = columnPos.method_10086(heightOffset);
                if (NoClip.mc.field_1687.method_22347(candidatePos)) continue;
                this.packetBreakingQueue.add(candidatePos);
            }
        }
        if (this.breakOccupiedBlocks.isValue()) {
            for (int occupiedHeight = 0; occupiedHeight < 2; ++occupiedHeight) {
                class_2338 occupiedPos = origin.method_10086(occupiedHeight);
                if (NoClip.mc.field_1687.method_22347(occupiedPos)) continue;
                this.packetBreakingQueue.add(occupiedPos);
            }
        }
    }

    @EventHandler
    private void onInput(InputEvent event) {
        if (!this.isState()) {
            this.pendingJump = false;
        } else if (this.mode.isSelected("\u0424\u043b\u0430\u0439 (RW)")) {
            this.pendingJump = false;
            NoClip.setSneak(event, false);
        } else {
            boolean blockMode;
            boolean bl = blockMode = this.mode.isSelected("\u0411\u043b\u043e\u043a\u0438") || this.mode.isSelected("\u0411\u043b\u043e\u043a\u0438 2");
            if (!blockMode) {
                this.pendingJump = false;
            } else {
                if (this.fixHitboxShrink.isValue()) {
                    NoClip.setSneak(event, false);
                }
                if (this.pendingJump) {
                    event.setJumping(true);
                    this.pendingJump = false;
                }
            }
        }
    }

    public NoClip() {
        super("NoClip", "NoClip", ModuleCategory.MOVEMENT);
        instance = this;
        this.mode = new SelectSetting("\u0420\u0435\u0436\u0438\u043c", "").value("\u0411\u043b\u043e\u043a\u0438", "\u0424\u043b\u0430\u0439 (RW)");
        this.flightDelay = new ValueSetting("\u0417\u0430\u0434\u0435\u0440\u0436\u043a\u0430", "").setValue(500.0f).range(0.0f, 500.0f).step(50.0f).visible(() -> this.mode.isSelected("\u0424\u043b\u0430\u0439 (RW)"));
        this.jumpDelay = new ValueSetting("\u0417\u0430\u0434\u0435\u0440\u0436\u043a\u0430 \u043f\u0440\u044b\u0436\u043a\u0430", "").setValue(0.0f).range(0.0f, 10.0f).step(1.0f);
        this.breakingSpeed = new ValueSetting("\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u043b\u043e\u043c\u0430\u043d\u0438\u044f", "").setValue(1.0f).range(0.1f, 1.0f).step(0.05f).visible(() -> this.mode.isSelected("\u0411\u043b\u043e\u043a\u0438"));
        this.increasePing = new BooleanSetting("\u0423\u0432\u0435\u043b\u0438\u0447\u0438\u0432\u0430\u0442\u044c \u043f\u0438\u043d\u0433", "").setValue(false).visible(() -> this.mode.isSelected("\u0411\u043b\u043e\u043a\u0438"));
        this.pingDelay = new ValueSetting("\u041f\u0438\u043d\u0433)", "").setValue(200.0f).range(50.0f, 2000.0f).step(25.0f).visible(this.increasePing::isValue);
        this.reorderBreaking = new BooleanSetting("\u041c\u0435\u043d\u044f\u0442\u044c \u043f\u043e\u0440\u044f\u0434\u043e\u043a \u043b\u043e\u043c\u0430\u043d\u0438\u044f", "").setValue(false).visible(() -> this.mode.isSelected("\u0411\u043b\u043e\u043a\u0438"));
        this.oneBlockCrawling = new BooleanSetting("\u041b\u043e\u043c\u0430\u0442\u044c \u0442\u043e\u043b\u044c\u043a\u043e \u043e\u0434\u0438\u043d \u0431\u043b\u043e\u043a \u043f\u0435\u0440\u0435\u0434 \u0441\u043e\u0431\u043e\u0439 \u0432 \u043f\u043e\u0437\u0435 \u043b\u0435\u0436\u0430", "").setValue(false).visible(() -> this.mode.isSelected("\u0411\u043b\u043e\u043a\u0438"));
        this.antiCrawl = new BooleanSetting("\u0410\u043d\u0442\u0438-\u043b\u0435\u0436\u0430\u043a", "").setValue(false).visible(() -> this.mode.isSelected("\u0411\u043b\u043e\u043a\u0438"));
        this.dearBypass = new BooleanSetting("\u0411\u0430\u0439\u043f\u0430\u0441\u0441\u0438\u043a \u0434\u043e\u0440\u043e\u0433\u043e\u0439", "").setValue(false).visible(() -> this.mode.isSelected("\u0411\u043b\u043e\u043a\u0438"));
        this.alternateDistance = new BooleanSetting("\u0411\u0443\u0440\u043c\u0430\u043b\u0434\u0430\u044f\u0446", "").setValue(false).visible(() -> this.mode.isSelected("\u0411\u043b\u043e\u043a\u0438"));
        this.breakOccupiedBlocks = new BooleanSetting("\u041b\u043e\u043c\u0430\u0442\u044c \u0431\u043b\u043e\u043a\u0438 \u0432 \u043a\u043e\u0442\u043e\u0440\u044b\u0445 \u0442\u044b \u043d\u0430\u0445\u043e\u0434\u0438\u0448\u044c\u0441\u044f", "").setValue(false).visible(() -> this.mode.isSelected("\u0411\u043b\u043e\u043a\u0438"));
        this.antiPhantom = new BooleanSetting("\u0410\u043d\u0442\u0438-\u0444\u0430\u043d\u0442\u043e\u043c", "").setValue(false).visible(() -> this.mode.isSelected("\u0411\u043b\u043e\u043a\u0438") || this.mode.isSelected("\u0411\u043b\u043e\u043a\u0438 2"));
        this.fixHitboxShrink = new BooleanSetting("\u0424\u0438\u043a\u0441 \u0443\u0441\u0430\u0434\u043a\u0438 \u0445\u0438\u0442\u0431\u043e\u043a\u0441\u0430", "").setValue(false).visible(() -> this.mode.isSelected("\u0411\u043b\u043e\u043a\u0438") || this.mode.isSelected("\u0411\u043b\u043e\u043a\u0438 2"));
        this.rebreakInterval = new ValueSetting("\u0418\u043d\u0442\u0435\u0440\u0432\u0430\u043b \u043f\u0435\u0440\u0435\u043b\u043e\u043c\u0430 (\u043c\u0441)", "").setValue(200.0f).range(0.0f, 2000.0f).step(10.0f).visible(() -> this.mode.isSelected("\u0411\u043b\u043e\u043a\u0438") || this.mode.isSelected("\u0411\u043b\u043e\u043a\u0438 2"));
        this.jumpTimer = new NoClipStopwatch();
        this.rebreakTimer = new NoClipStopwatch();
        this.breakingQueue = new ArrayList<class_2338>();
        this.blocksPerSecond = new ValueSetting("\u0411\u041f\u0421", "").setValue(12.0f).range(1.0f, 20.0f).step(1.0f).visible(() -> this.mode.isSelected("\u0411\u043b\u043e\u043a\u0438 2"));
        this.hideBreaking = new BooleanSetting("\u0421\u043a\u0440\u044b\u0432\u0430\u0442\u044c \u043b\u043e\u043c\u0430\u043d\u0438\u0435", "").setValue(true).visible(() -> this.mode.isSelected("\u0411\u043b\u043e\u043a\u0438") || this.mode.isSelected("\u0411\u043b\u043e\u043a\u0438 2"));
        this.packetBreakingQueue = new ArrayList<class_2338>();
        this.pendingBlocks = new ConcurrentHashMap<class_2338, Long>();
        this.bypassTimer = new NoClipStopwatch();
        this.delayedPackets = new CopyOnWriteArrayList();
        this.packetTimer = new NoClipStopwatch();
        this.restoreSlot = -1;
        this.bypassDistance = 1;
        this.bypassOrigin = class_2338.field_10980;
        this.setup(this.mode, this.flightDelay, this.jumpDelay, this.breakingSpeed, this.antiCrawl, this.blocksPerSecond, this.hideBreaking, this.fixHitboxShrink, this.rebreakInterval);
    }

    private int findBestTool(class_2680 state) {
        int bestSlot = -1;
        float bestSpeed = 1.0f;
        for (int slot = 0; slot < 9; ++slot) {
            class_1799 stack = NoClip.mc.field_1724.method_31548().method_5438(slot);
            if (stack.method_7960() || !this.isTool(stack)) continue;
            float miningSpeed = stack.method_7924(state);
            int efficiencyLevel = this.inspect(stack);
            if (miningSpeed > 1.0f && efficiencyLevel > 0) {
                miningSpeed += (float)(efficiencyLevel * efficiencyLevel) + 1.0f;
            }
            if (!(miningSpeed > bestSpeed)) continue;
            bestSpeed = miningSpeed;
            bestSlot = slot;
        }
        return bestSlot;
    }

    @Override
    public void deactivate() {
        super.deactivate();
        if (NoClip.mc.field_1724 != null) {
            NoClip.mc.field_1724.field_5960 = false;
        }
        NoClipCameraSmoothing.reset();
        this.finishProgressBreaking();
        this.packetBreakingQueue.clear();
        this.currentPacketBlock = null;
        BREAKING_BLOCKS.clear();
        this.pendingBlocks.clear();
        NoClipHiddenBlocks.POSITIONS.clear();
        this.pendingJump = false;
        this.bypassBreaking = false;
        this.bypassWaiting = false;
        this.bypassDistance = 1;
        this.bypassOrigin = class_2338.field_10980;
        this.restoreSelectedSlot();
        this.flushPackets();
        if (NoClip.mc.field_1724 != null && (this.mode.isSelected("\u0411\u043b\u043e\u043a\u0438") || this.mode.isSelected("\u0411\u043b\u043e\u043a\u0438 2"))) {
            this.clearSneaking();
        }
    }

    private boolean isTool(class_1799 stack) {
        return stack.method_31573(class_3489.field_42614) || stack.method_31573(class_3489.field_42612) || stack.method_31573(class_3489.field_42615) || stack.method_31573(class_3489.field_42613);
    }

    private class_2561 weakToolWarning() {
        return class_2561.method_43470((String)"\u041d\u0430 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u0435 \u043d\u0435\u0442 \u0437\u0430\u0447\u0430\u0440\u043e\u0432\u0430\u043d\u0438\u044f \"\u042d\u0444\u0444\u0435\u043a\u0442\u0438\u0432\u043d\u043e\u0441\u0442\u044c 7\", \u0431\u0443\u0434\u044c\u0442\u0435 \u0430\u043a\u043a\u0443\u0440\u0430\u0442\u043d\u044b \u0442\u043a \u0435\u0441\u0442\u044c \u0448\u0430\u043d\u0441 \u043a\u0438\u043a\u0430.").method_27692(class_124.field_1065);
    }

    private void tickPacketBreaking() {
        long now = System.currentTimeMillis();
        if (this.currentPacketBlock != null) {
            if (NoClip.mc.field_1687.method_22347(this.currentPacketBlock)) {
                this.finishPacketBreak();
            } else if (now - this.packetBreakStartedAt > 200L) {
                mc.method_1562().method_52787((class_2596)new class_2846(class_2846.class_2847.field_12973, this.currentPacketBlock, this.closestFace(this.currentPacketBlock)));
                this.finishPacketBreak();
            }
        } else if (now >= this.nextPacketBreakAt) {
            while (!this.packetBreakingQueue.isEmpty()) {
                class_2338 pos = this.packetBreakingQueue.removeFirst();
                if (NoClip.mc.field_1687.method_22347(pos)) continue;
                int selectedSlot = NoClip.mc.field_1724.method_31548().method_67532();
                int toolSlot = this.findBestTool(NoClip.mc.field_1687.method_8320(pos));
                if (toolSlot != -1 && toolSlot != selectedSlot) {
                    this.restoreSlot = selectedSlot;
                    NoClip.mc.field_1724.method_31548().method_61496(toolSlot);
                    NoClip.mc.field_1761.method_2911();
                } else {
                    this.restoreSlot = -1;
                }
                this.trackBlock(pos);
                mc.method_1562().method_52787((class_2596)new class_2846(class_2846.class_2847.field_12968, pos, this.closestFace(pos)));
                this.swing();
                this.currentPacketBlock = pos;
                this.packetBreakStartedAt = now;
                return;
            }
        }
    }

    private void delayOutgoingPacket(PacketEvent event) {
        class_2596<?> packet;
        if (this.isState() && this.increasePing.isValue() && this.mode.isSelected("\u0411\u043b\u043e\u043a\u0438") && !this.flushingPackets && !((packet = event.getPacket()) instanceof class_2827) && !(packet instanceof class_6374)) {
            event.cancel();
            this.delayedPackets.add(packet);
        }
    }

    private int distanceFromBypassOrigin(class_2338 pos) {
        int offsetX = pos.method_10263() - this.bypassOrigin.method_10263();
        int offsetZ = pos.method_10260() - this.bypassOrigin.method_10260();
        return switch (NoClip.mc.field_1724.method_5735()) {
            case class_2350.field_11034 -> offsetX;
            case class_2350.field_11039 -> -offsetX;
            case class_2350.field_11035 -> offsetZ;
            case class_2350.field_11043 -> -offsetZ;
            default -> 0;
        };
    }

    private void trackBlock(class_2338 pos) {
        if (pos != null) {
            class_2338 immutablePos = pos.method_10062();
            this.pendingBlocks.put(immutablePos, System.currentTimeMillis());
            if (this.hideBreaking.isValue()) {
                BREAKING_BLOCKS.add(immutablePos);
            }
        }
    }

    private boolean isTracked(class_2338 pos) {
        return this.pendingBlocks.containsKey(pos) || BREAKING_BLOCKS.contains(pos) || NoClipHiddenBlocks.POSITIONS.contains(pos);
    }

    private void onIncomingPacket(PacketEvent event) {
        if (this.isState() && (this.mode.isSelected("\u0411\u043b\u043e\u043a\u0438") || this.mode.isSelected("\u0411\u043b\u043e\u043a\u0438 2"))) {
            class_2596<?> packet = event.getPacket();
            if (packet instanceof class_2620) {
                class_2620 breakingProgress = (class_2620)packet;
                if (this.hideBreaking.isValue() && BREAKING_BLOCKS.contains(breakingProgress.method_11277())) {
                    event.cancel();
                }
            } else if (packet instanceof class_2673) {
                class_2673 worldEvent = (class_2673)packet;
                if (this.hideBreaking.isValue() && worldEvent.method_11532() == 2001 && BREAKING_BLOCKS.contains(worldEvent.method_11531())) {
                    event.cancel();
                }
            } else if (packet instanceof class_2767) {
                class_2767 sound = (class_2767)packet;
                class_2338 soundPos = class_2338.method_49637((double)sound.method_11890(), (double)sound.method_11889(), (double)sound.method_11893());
                if (this.hideBreaking.isValue() && BREAKING_BLOCKS.contains(soundPos)) {
                    event.cancel();
                }
            } else if (packet instanceof class_2626) {
                class_2626 blockUpdate = (class_2626)packet;
                class_2338 updatedPos = blockUpdate.method_11309().method_10062();
                if (this.isTracked(updatedPos)) {
                    if (blockUpdate.method_11308().method_26215()) {
                        this.onBlockBroken(updatedPos);
                        if (this.hideBreaking.isValue()) {
                            event.cancel();
                        }
                    } else {
                        this.onBlockRestored(updatedPos);
                        if (this.antiPhantom.isValue()) {
                            event.cancel();
                            this.rebreakBlock(updatedPos);
                        }
                    }
                }
            } else if (packet instanceof class_2637) {
                class_2637 chunkUpdate = (class_2637)packet;
                boolean[] cancelChunkUpdate = new boolean[]{false};
                chunkUpdate.method_30621((changedPos, changedState) -> {
                    class_2338 immutablePos = changedPos.method_10062();
                    if (this.isTracked(immutablePos)) {
                        if (changedState.method_26215()) {
                            this.onBlockBroken(immutablePos);
                            if (this.hideBreaking.isValue()) {
                                cancelChunkUpdate[0] = true;
                            }
                        } else {
                            this.onBlockRestored(immutablePos);
                            if (this.antiPhantom.isValue()) {
                                cancelChunkUpdate[0] = true;
                                this.rebreakBlock(immutablePos);
                            }
                        }
                    }
                });
                if (cancelChunkUpdate[0]) {
                    event.cancel();
                }
            }
        }
    }

    private int inspect(class_1799 stack) {
        return class_1890.method_8225((class_6880)NoClip.mc.field_1687.method_30349().method_30530(class_7924.field_41265).method_46747(class_1893.field_9131), (class_1799)stack);
    }

    private void startNextBlock() {
        if (this.breakingQueue.isEmpty()) {
            this.finishProgressBreaking();
        } else {
            this.currentBreakingBlock = this.breakingQueue.removeFirst();
            if (!this.selectMiningTool(this.currentBreakingBlock)) {
                this.currentBreakingBlock = null;
                if (this.breakingQueue.isEmpty()) {
                    this.finishProgressBreaking();
                } else {
                    this.startNextBlock();
                }
            } else {
                this.trackBlock(this.currentBreakingBlock);
                NoClip.mc.field_1761.method_2910(this.currentBreakingBlock, this.closestFace(this.currentBreakingBlock));
                this.swing();
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (this.isState() && NoClip.mc.field_1724 != null && NoClip.mc.field_1687 != null && NoClip.mc.field_1761 != null) {
            this.pendingJump = false;
            if (this.mode.isSelected("\u0424\u043b\u0430\u0439 (RW)")) {
                boolean forwardPressed = NoClip.mc.field_1690.field_1894.method_1434();
                boolean jumpPressed = NoClip.mc.field_1690.field_1903.method_1434();
                boolean sneakPressed = NoClip.mc.field_1690.field_1832.method_1434();
                if (!NoClip.mc.field_1724.method_24828()) {
                    double verticalOffset = (jumpPressed ? 1.0 : 0.0) - (sneakPressed ? 1.0 : 0.0);
                    if ((forwardPressed || verticalOffset != 0.0) && this.jumpTimer.hasElapsed((long)this.flightDelay.getValue())) {
                        double offsetZ;
                        double yawRadians = Math.toRadians(NoClip.mc.field_1724.method_36454());
                        double offsetX = forwardPressed ? -Math.sin(yawRadians) : 0.0;
                        double d = offsetZ = forwardPressed ? Math.cos(yawRadians) : 0.0;
                        if (NoClip.mc.field_1690.method_31044().method_31034()) {
                            NoClipCameraSmoothing.beginTransition(NoClip.mc.field_1724);
                        } else {
                            NoClipCameraSmoothing.reset();
                        }
                        NoClip.mc.field_1724.method_5814(NoClip.mc.field_1724.method_23317() + offsetX, NoClip.mc.field_1724.method_23318() + verticalOffset, NoClip.mc.field_1724.method_23321() + offsetZ);
                        this.jumpTimer.reset();
                    }
                }
            } else {
                boolean blockMode;
                boolean bl = blockMode = this.mode.isSelected("\u0411\u043b\u043e\u043a\u0438") || this.mode.isSelected("\u0411\u043b\u043e\u043a\u0438 2");
                if (blockMode) {
                    if (this.fixHitboxShrink.isValue()) {
                        this.fixPose();
                    }
                    boolean hideBlockBreaking = this.hideBreaking.isValue();
                    NoClip.mc.field_1724.field_5960 = false;
                    if (!hideBlockBreaking) {
                        BREAKING_BLOCKS.clear();
                        NoClipHiddenBlocks.POSITIONS.clear();
                    }
                    this.expirePendingBlocks();
                    if (this.mode.isSelected("\u0411\u043b\u043e\u043a\u0438") && this.antiCrawl.isValue() && this.isCrawling()) {
                        this.breakOccupiedWhileCrawling();
                    }
                    NoClip.mc.field_1761.field_3716 = 0;
                    NoClip.mc.field_1724.field_6228 = 0;
                    boolean jumpTriggered = false;
                    long jumpDelayMillis = (long)this.jumpDelay.getValue() * 50L;
                    if (NoClip.mc.field_1724.method_24828() && this.jumpTimer.hasElapsed(jumpDelayMillis)) {
                        this.pendingJump = true;
                        this.jumpTimer.reset();
                        jumpTriggered = true;
                    }
                    if (jumpTriggered) {
                        if (this.mode.isSelected("\u0411\u043b\u043e\u043a\u0438") && !this.dearBypass.isValue()) {
                            this.queueProgressBreaking();
                        }
                        if (this.mode.isSelected("\u0411\u043b\u043e\u043a\u0438 2") && !this.dearBypass.isValue()) {
                            this.queuePacketBlocks();
                        }
                    }
                    if (this.mode.isSelected("\u0411\u043b\u043e\u043a\u0438")) {
                        if (this.dearBypass.isValue()) {
                            this.tickBypass();
                        } else if (this.breaking) {
                            this.tickProgressBreaking();
                        }
                    }
                    if (this.mode.isSelected("\u0411\u043b\u043e\u043a\u0438 2")) {
                        if (this.dearBypass.isValue()) {
                            this.tickBypass();
                        } else {
                            this.queuePacketBlocks();
                            this.tickPacketBreaking();
                        }
                    }
                    if (!this.dearBypass.isValue() && this.rebreakInterval.getValue() > 0.0f && !this.breaking && this.rebreakTimer.hasElapsed((long)this.rebreakInterval.getValue())) {
                        if (this.mode.isSelected("\u0411\u043b\u043e\u043a\u0438")) {
                            this.queueProgressBreaking();
                        } else {
                            this.queuePacketBlocks();
                        }
                        this.rebreakTimer.reset();
                    }
                    if (this.increasePing.isValue() && this.mode.isSelected("\u0411\u043b\u043e\u043a\u0438")) {
                        if (!this.delayedPackets.isEmpty() && this.packetTimer.elapsed() > (long)this.pingDelay.getValue()) {
                            this.flushPackets();
                        }
                    } else if (!this.delayedPackets.isEmpty()) {
                        this.flushPackets();
                    }
                }
            }
        }
    }

    private void onBlockRestored(class_2338 pos) {
        this.pendingBlocks.remove(pos);
        NoClipHiddenBlocks.POSITIONS.remove(pos);
        if (!this.antiPhantom.isValue()) {
            BREAKING_BLOCKS.remove(pos);
        }
    }

    private void onBlockBroken(class_2338 pos) {
        this.pendingBlocks.remove(pos);
        if (this.hideBreaking.isValue()) {
            BREAKING_BLOCKS.add(pos);
            NoClipHiddenBlocks.POSITIONS.add(pos);
        } else {
            BREAKING_BLOCKS.remove(pos);
            NoClipHiddenBlocks.POSITIONS.remove(pos);
        }
    }

    private boolean isCrawling() {
        return NoClip.mc.field_1724 != null && NoClip.mc.field_1724.method_18376() == class_4050.field_18079;
    }

    private class_2350 closestFace(class_2338 pos) {
        class_2680 state = NoClip.mc.field_1687.method_8320(pos);
        class_265 collisionShape = state.method_26220((class_1922)NoClip.mc.field_1687, pos);
        class_238 bounds = collisionShape.method_1110() ? new class_238(pos) : collisionShape.method_1107().method_996(pos);
        class_243 eyePos = NoClip.mc.field_1724.method_33571();
        class_2350 closestFace = null;
        double closestDistance = Double.MAX_VALUE;
        if (eyePos.field_1352 <= bounds.field_1323) {
            closestDistance = bounds.field_1323 - eyePos.field_1352;
            closestFace = class_2350.field_11039;
        }
        if (eyePos.field_1352 >= bounds.field_1320 && eyePos.field_1352 - bounds.field_1320 < closestDistance) {
            closestDistance = eyePos.field_1352 - bounds.field_1320;
            closestFace = class_2350.field_11034;
        }
        if (eyePos.field_1350 <= bounds.field_1321 && bounds.field_1321 - eyePos.field_1350 < closestDistance) {
            closestDistance = bounds.field_1321 - eyePos.field_1350;
            closestFace = class_2350.field_11043;
        }
        if (eyePos.field_1350 >= bounds.field_1324 && eyePos.field_1350 - bounds.field_1324 < closestDistance) {
            closestFace = class_2350.field_11035;
        }
        if (closestFace != null) {
            return closestFace;
        }
        if (eyePos.field_1351 >= bounds.field_1325) {
            return class_2350.field_11036;
        }
        return eyePos.field_1351 <= bounds.field_1322 ? class_2350.field_11033 : NoClip.mc.field_1724.method_5735().method_10153();
    }

    private boolean selectMiningTool(class_2338 pos) {
        if (pos != null && !NoClip.mc.field_1687.method_22347(pos)) {
            int toolSlot = this.findBestTool(NoClip.mc.field_1687.method_8320(pos));
            if (toolSlot == -1) {
                return false;
            }
            class_1799 stack = NoClip.mc.field_1724.method_31548().method_5438(toolSlot);
            if (!this.isMiningTool(stack)) {
                return false;
            }
            NoClip.mc.field_1724.method_31548().method_61496(toolSlot);
            NoClip.mc.field_1761.method_2911();
            return true;
        }
        return false;
    }

    private void fixPose() {
        if (this.isState() && (this.mode.isSelected("\u0411\u043b\u043e\u043a\u0438") || this.mode.isSelected("\u0411\u043b\u043e\u043a\u0438 2")) && NoClip.mc.field_1724 != null && NoClip.mc.field_1687 != null) {
            this.clearSneaking();
            class_238 standingBox = NoClip.mc.field_1724.method_18377(class_4050.field_18076).method_30757(NoClip.mc.field_1724.method_73189());
            if (NoClip.mc.field_1724.method_18376() != class_4050.field_18076 && NoClip.mc.field_1687.method_8587((class_1297)NoClip.mc.field_1724, standingBox)) {
                NoClip.mc.field_1724.method_18380(class_4050.field_18076);
            }
        }
    }

    private void queueProgressBreaking() {
        if (!this.breaking) {
            class_2350 facing = NoClip.mc.field_1724.method_5735();
            class_2338 origin = this.playerBlockPos();
            this.breakingQueue.clear();
            if (this.oneBlockCrawling.isValue() && this.isCrawling()) {
                class_2338 frontPos = origin.method_10093(facing);
                if (!NoClip.mc.field_1687.method_22347(frontPos)) {
                    this.breakingQueue.add(frontPos);
                }
            } else {
                int distance = this.alternateDistance.isValue() ? 2 : 1;
                for (int forwardOffset = 1; forwardOffset <= distance; ++forwardOffset) {
                    class_2338 columnPos = origin.method_10079(facing, forwardOffset);
                    for (int heightOffset = 0; heightOffset < 2; ++heightOffset) {
                        class_2338 candidatePos = columnPos.method_10086(heightOffset);
                        if (NoClip.mc.field_1687.method_22347(candidatePos)) continue;
                        this.breakingQueue.add(candidatePos);
                    }
                }
            }
            if (this.breakOccupiedBlocks.isValue()) {
                for (int occupiedHeight = 0; occupiedHeight < 2; ++occupiedHeight) {
                    class_2338 occupiedPos = origin.method_10086(occupiedHeight);
                    if (NoClip.mc.field_1687.method_22347(occupiedPos)) continue;
                    this.breakingQueue.add(occupiedPos);
                }
            }
            if (!this.breakingQueue.isEmpty()) {
                boolean allToolsInstant = true;
                for (class_2338 queuedPos : this.breakingQueue) {
                    class_2680 state = NoClip.mc.field_1687.method_8320(queuedPos);
                    int toolSlot = this.findBestTool(state);
                    if (toolSlot == -1) {
                        allToolsInstant = false;
                        continue;
                    }
                    class_1799 stack = NoClip.mc.field_1724.method_31548().method_5438(toolSlot);
                    if (this.isMiningTool(stack) && this.inspect(stack) >= 7) continue;
                    allToolsInstant = false;
                }
                this.instantBreak = allToolsInstant;
                if (!this.instantBreak && System.currentTimeMillis() - this.lastWarningAt > 3000L) {
                    NoClip.chat(this.weakToolWarning());
                    this.lastWarningAt = System.currentTimeMillis();
                }
                if (this.reorderBreaking.isValue()) {
                    this.breakingQueue.sort(Comparator.comparingInt((class_2382 candidate) -> Math.abs(candidate.method_10263() - origin.method_10263()) + Math.abs(candidate.method_10260() - origin.method_10260())).thenComparingInt(class_2382::method_10264));
                }
                this.restoreSlot = NoClip.mc.field_1724.method_31548().method_67532();
                this.breaking = true;
                this.breakingStartedAt = System.currentTimeMillis();
                NoClip.mc.field_1761.method_2911();
                this.startNextBlock();
            }
        }
    }

    @EventHandler
    private void onPostTick(PostTickEvent event) {
        if (this.isState() && NoClip.mc.field_1724 != null && NoClip.mc.field_1687 != null && (this.mode.isSelected("\u0411\u043b\u043e\u043a\u0438") || this.mode.isSelected("\u0411\u043b\u043e\u043a\u0438 2")) && this.fixHitboxShrink.isValue()) {
            this.fixPose();
        }
    }

    private void breakOccupiedWhileCrawling() {
        int selectedSlot = NoClip.mc.field_1724.method_31548().method_67532();
        class_2338 origin = this.playerBlockPos();
        this.breakWithProgress(origin);
        this.breakWithProgress(origin.method_10084());
        NoClip.mc.field_1724.method_31548().method_61496(selectedSlot);
        NoClip.mc.field_1761.method_2911();
    }

    private void rebreakBlock(class_2338 pos) {
        if (!mc.method_18854()) {
            class_638 world = NoClip.mc.field_1687;
            class_634 connection = mc.method_1562();
            mc.execute(() -> {
                if (this.isState() && NoClip.mc.field_1687 == world && mc.method_1562() == connection) {
                    this.rebreakBlock(pos);
                }
            });
            return;
        }
        if (NoClip.mc.field_1724 != null && mc.method_1562() != null && NoClip.mc.field_1687 != null && !NoClip.mc.field_1687.method_22347(pos)) {
            this.trackBlock(pos);
            class_2350 face = this.closestFace(pos);
            mc.method_1562().method_52787((class_2596)new class_2846(class_2846.class_2847.field_12968, pos, face));
            mc.method_1562().method_52787((class_2596)new class_2846(class_2846.class_2847.field_12973, pos, face));
            this.swing();
        }
    }

    public static boolean isBreakingHidden(class_2338 pos) {
        return pos != null && BREAKING_BLOCKS.contains(pos);
    }

    private void tickProgressBreaking() {
        if (System.currentTimeMillis() - this.breakingStartedAt > 8000L) {
            this.finishProgressBreaking();
        } else if (!this.instantBreak) {
            if (this.currentBreakingBlock != null && !NoClip.mc.field_1687.method_22347(this.currentBreakingBlock)) {
                NoClip.mc.field_1761.field_3716 = 0;
                boolean breakingUpdated = NoClip.mc.field_1761.method_2902(this.currentBreakingBlock, this.closestFace(this.currentBreakingBlock));
                this.swing();
                if (breakingUpdated) {
                    NoClip.mc.field_1761.method_2925();
                    this.currentBreakingBlock = null;
                    if (this.breakingQueue.isEmpty()) {
                        this.finishProgressBreaking();
                    } else {
                        this.startNextBlock();
                    }
                }
            } else {
                this.currentBreakingBlock = null;
                if (this.breakingQueue.isEmpty()) {
                    this.finishProgressBreaking();
                } else {
                    this.startNextBlock();
                }
            }
        } else {
            if (this.currentBreakingBlock != null) {
                this.breakWithProgress(this.currentBreakingBlock);
            }
            for (class_2338 queuedPos : this.breakingQueue) {
                this.breakWithProgress(queuedPos);
            }
            this.finishProgressBreaking();
        }
    }

    public static boolean shouldFlattenHead(int entityId) {
        NoClip module = instance;
        return module != null && module.isState() && (module.mode.isSelected("\u0411\u043b\u043e\u043a\u0438") || module.mode.isSelected("\u0411\u043b\u043e\u043a\u0438 2")) && NoClip.mc.field_1724 != null && NoClip.mc.field_1724.method_5628() == entityId;
    }

    private void flushPackets() {
        if (!this.delayedPackets.isEmpty()) {
            this.flushingPackets = true;
            for (class_2596<?> packet : this.delayedPackets) {
                mc.method_1562().method_52787(packet);
            }
            this.flushingPackets = false;
            this.delayedPackets.clear();
            this.packetTimer.reset();
        }
    }

    private void queuePacketBlocks() {
        if (this.packetBreakingQueue.isEmpty()) {
            class_2338 origin = this.playerBlockPos();
            class_2350 facing = NoClip.mc.field_1724.method_5735();
            for (int forwardOffset = 1; forwardOffset <= 2; ++forwardOffset) {
                class_2338 columnPos = origin.method_10079(facing, forwardOffset);
                for (int heightOffset = 0; heightOffset < 3; ++heightOffset) {
                    class_2338 candidatePos = columnPos.method_10086(heightOffset);
                    if (NoClip.mc.field_1687.method_22347(candidatePos)) continue;
                    this.packetBreakingQueue.add(candidatePos);
                }
            }
        }
    }

    @Override
    public void activate() {
        super.activate();
        if (!(this.mode.isSelected("\u0411\u043b\u043e\u043a\u0438") || this.mode.isSelected("\u0411\u043b\u043e\u043a\u0438 2") || this.mode.isSelected("\u0424\u043b\u0430\u0439 (RW)"))) {
            this.mode.setSelected("\u0411\u043b\u043e\u043a\u0438");
        }
        this.jumpTimer.reset();
        this.pendingJump = false;
        NoClipCameraSmoothing.reset();
        if (this.mode.isSelected("\u0411\u043b\u043e\u043a\u0438") || this.mode.isSelected("\u0411\u043b\u043e\u043a\u0438 2")) {
            NoClip.chat(this.enchantmentWarning());
        }
    }

    private class_2338 playerBlockPos() {
        return class_2338.method_49638((class_2374)NoClip.mc.field_1724.method_73189());
    }

    private void finishProgressBreaking() {
        this.breaking = false;
        this.currentBreakingBlock = null;
        this.breakingQueue.clear();
        this.restoreSelectedSlot();
    }

    @EventHandler
    private void onPacket(PacketEvent event) {
        if (event.isSend()) {
            this.delayOutgoingPacket(event);
        } else if (NoClip.mc.field_1724 != null && NoClip.mc.field_1687 != null) {
            this.onIncomingPacket(event);
        }
    }

    @EventHandler(value=4)
    private void onMotion(MotionEvent event) {
        if (this.isState() && (this.mode.isSelected("\u0411\u043b\u043e\u043a\u0438") || this.mode.isSelected("\u0411\u043b\u043e\u043a\u0438 2"))) {
            event.setPitch(0.0f);
        }
    }

    @EventHandler
    private void onCameraPosition(CameraPositionEvent event) {
        if (NoClip.mc.field_1724 != null && NoClip.mc.field_1690.method_31044().method_31034() && mc.method_1560() == NoClip.mc.field_1724) {
            event.setPos(NoClipCameraSmoothing.smooth(NoClip.mc.field_1724, event.getPos()));
        } else {
            NoClipCameraSmoothing.reset();
        }
    }

    private static void setSneak(InputEvent event, boolean sneak) {
        class_10185 input = event.getInput();
        event.setInput(new class_10185(input.comp_3159(), input.comp_3160(), input.comp_3161(), input.comp_3162(), input.comp_3163(), sneak, input.comp_3165()));
    }

    private static void chat(class_2561 text) {
        NoClip.mc.field_1705.method_1743().method_1812(text);
    }

    static {
        BREAKING_BLOCKS = ConcurrentHashMap.newKeySet();
    }
}

