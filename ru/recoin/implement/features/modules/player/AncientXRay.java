/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 *  net.minecraft.class_2246
 *  net.minecraft.class_2248
 *  net.minecraft.class_2338
 *  net.minecraft.class_2338$class_2339
 *  net.minecraft.class_2350
 *  net.minecraft.class_2374
 *  net.minecraft.class_238
 *  net.minecraft.class_2561
 *  net.minecraft.class_2596
 *  net.minecraft.class_2626
 *  net.minecraft.class_2637
 *  net.minecraft.class_2664
 *  net.minecraft.class_2846
 *  net.minecraft.class_2846$class_2847
 *  net.minecraft.class_5250
 */
package ru.recoin.implement.features.modules.player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_124;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2374;
import net.minecraft.class_238;
import net.minecraft.class_2561;
import net.minecraft.class_2596;
import net.minecraft.class_2626;
import net.minecraft.class_2637;
import net.minecraft.class_2664;
import net.minecraft.class_2846;
import net.minecraft.class_5250;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.common.QuickLogger;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.render.Render3DUtil;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.events.render.WorldRenderEvent;
import ru.recoin.implement.features.modules.misc.AncientFarmer;

public class AncientXRay
extends Module {
    static int SEARCH_RADIUS = 28;
    static long CLICK_DELAY_MS = 50L;
    static int DEBRIS_COLOR = -2147418368;
    static int[] DELAY_TICKS = new int[]{4, 10, 20, 40};
    private final Set<class_2338> foundDebris = ConcurrentHashMap.newKeySet();
    private final Set<class_2338> clickedDebris = ConcurrentHashMap.newKeySet();
    private final List<DelayBlock> delayedChecks = new ArrayList<DelayBlock>();
    private long lastClickTime;

    public AncientXRay() {
        super("AncientXRay", "Ancient XRay", ModuleCategory.MISC);
    }

    public static AncientXRay getInstance() {
        return Instance.get(AncientXRay.class);
    }

    public Set<class_2338> getFoundDebris() {
        return Set.copyOf(this.foundDebris);
    }

    @Override
    public void activate() {
        this.clear();
        super.activate();
    }

    @Override
    public void deactivate() {
        this.clear();
        super.deactivate();
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (AncientXRay.mc.field_1724 == null || AncientXRay.mc.field_1687 == null) {
            return;
        }
        Iterator<DelayBlock> iterator2 = this.delayedChecks.iterator();
        while (iterator2.hasNext()) {
            DelayBlock delayBlock = iterator2.next();
            --delayBlock.ticks;
            if (delayBlock.ticks > 0) continue;
            this.checkBlocks(delayBlock.pos, SEARCH_RADIUS);
            iterator2.remove();
        }
        if (mc.method_1562() == null || System.currentTimeMillis() - this.lastClickTime <= CLICK_DELAY_MS) {
            return;
        }
        if (AncientFarmer.getInstance().isState()) {
            return;
        }
        for (class_2338 pos : this.foundDebris) {
            if (this.clickedDebris.contains(pos)) continue;
            this.clickedDebris.add(pos);
            this.startStopDestroy(pos);
            this.lastClickTime = System.currentTimeMillis();
            break;
        }
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        if (AncientXRay.mc.field_1724 == null || AncientXRay.mc.field_1687 == null || this.foundDebris.isEmpty()) {
            return;
        }
        Iterator<class_2338> iterator2 = this.foundDebris.iterator();
        while (iterator2.hasNext()) {
            class_2338 pos = iterator2.next();
            if (!AncientXRay.mc.field_1687.method_8320(pos).method_27852(class_2246.field_22109)) {
                iterator2.remove();
                this.clickedDebris.remove(pos);
                continue;
            }
            Render3DUtil.drawBox(new class_238(pos), DEBRIS_COLOR, 2.0f);
        }
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        if (AncientXRay.mc.field_1687 == null || event.isSend()) {
            return;
        }
        class_2596<?> packet = event.getPacket();
        if (packet instanceof class_2664) {
            class_2664 explosion = (class_2664)packet;
            class_2338 center = class_2338.method_49638((class_2374)explosion.comp_2883());
            for (int ticks : DELAY_TICKS) {
                this.delayedChecks.add(new DelayBlock(center.method_10062(), ticks));
            }
        } else if (packet instanceof class_2626) {
            class_2626 blockUpdate = (class_2626)packet;
            this.handleBlockUpdate(blockUpdate.method_11309(), blockUpdate.method_11308().method_26204());
        } else if (packet instanceof class_2637) {
            class_2637 chunkDelta = (class_2637)packet;
            chunkDelta.method_30621((pos, state) -> this.handleBlockUpdate((class_2338)pos, state.method_26204()));
        }
    }

    private void startStopDestroy(class_2338 pos) {
        if (mc.method_1562() == null) {
            return;
        }
        mc.method_1562().method_52787((class_2596)new class_2846(class_2846.class_2847.field_12968, pos, class_2350.field_11036));
        mc.method_1562().method_52787((class_2596)new class_2846(class_2846.class_2847.field_12971, pos, class_2350.field_11036));
    }

    private void handleBlockUpdate(class_2338 pos, class_2248 block) {
        class_2338 immutable = pos.method_10062();
        if (block == class_2246.field_22109) {
            this.addDebrisIfValid(immutable, true);
        } else {
            this.foundDebris.remove(immutable);
            this.clickedDebris.remove(immutable);
        }
    }

    private void checkBlocks(class_2338 center, int radius) {
        if (AncientXRay.mc.field_1687 == null) {
            return;
        }
        class_2338.class_2339 mutable = new class_2338.class_2339();
        for (int x = -radius; x <= radius; ++x) {
            for (int y = -radius; y <= radius; ++y) {
                for (int z = -radius; z <= radius; ++z) {
                    mutable.method_10103(center.method_10263() + x, center.method_10264() + y, center.method_10260() + z);
                    this.addDebrisIfValid(mutable.method_10062(), false);
                }
            }
        }
    }

    private void addDebrisIfValid(class_2338 pos, boolean fromPacketUpdate) {
        if (!this.isValidDebris(pos) || !this.foundDebris.add(pos)) {
            return;
        }
        if (AncientFarmer.getInstance().isState()) {
            return;
        }
        class_5250 message = class_2561.method_43473().method_10852((class_2561)class_2561.method_43470((String)"[AncientXRay] ").method_27692(class_124.field_1065)).method_10852((class_2561)class_2561.method_43470((String)(fromPacketUpdate ? "\u041e\u0431\u043b\u043e\u043c\u043e\u043a \u043d\u0430\u0439\u0434\u0435\u043d " : "\u041e\u0431\u043d\u0430\u0440\u0443\u0436\u0435\u043d \u043e\u0431\u043b\u043e\u043c\u043e\u043a: ")).method_27692(class_124.field_1068)).method_10852((class_2561)class_2561.method_43470((String)pos.method_23854()).method_27692(class_124.field_1054));
        AncientXRay.mc.field_1705.method_1743().method_1812(QuickLogger.clientMessage((class_2561)message));
    }

    private boolean isValidDebris(class_2338 pos) {
        if (AncientXRay.mc.field_1687 == null || !AncientXRay.mc.field_1687.method_8320(pos).method_27852(class_2246.field_22109)) {
            return false;
        }
        return this.checkAirNearby(pos) && !this.checkQuartzOrGold(pos) && this.checkAirLavaNearbyBig(pos) && !this.checkDebrisNearby(pos);
    }

    private boolean checkDebrisNearby(class_2338 pos) {
        int count = 0;
        for (int x = -3; x <= 2; ++x) {
            for (int y = -2; y <= 2; ++y) {
                for (int z = -2; z <= 3; ++z) {
                    if (!AncientXRay.mc.field_1687.method_8320(pos.method_10069(x, y, z)).method_27852(class_2246.field_22109) || ++count <= 6) continue;
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkAirNearby(class_2338 pos) {
        int count = 0;
        for (class_2350 direction : class_2350.values()) {
            if (!this.isOpenBlock(AncientXRay.mc.field_1687.method_8320(pos.method_10093(direction)).method_26204()) || ++count < 2) continue;
            return true;
        }
        return false;
    }

    private boolean checkQuartzOrGold(class_2338 pos) {
        int count = 0;
        for (int x = -1; x <= 1; ++x) {
            for (int y = -1; y <= 1; ++y) {
                for (int z = -1; z <= 1; ++z) {
                    class_2248 block = AncientXRay.mc.field_1687.method_8320(pos.method_10069(x, y, z)).method_26204();
                    if (block != class_2246.field_10213 && block != class_2246.field_23077 || ++count < 4) continue;
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkAirLavaNearbyBig(class_2338 pos) {
        int count = 0;
        for (int x = -1; x <= 1; ++x) {
            for (int y = -1; y <= 1; ++y) {
                for (int z = -1; z <= 1; ++z) {
                    if (!this.isOpenBlock(AncientXRay.mc.field_1687.method_8320(pos.method_10069(x, y, z)).method_26204()) || ++count < 4) continue;
                    return true;
                }
            }
        }
        return count >= 4;
    }

    private boolean isOpenBlock(class_2248 block) {
        return block == class_2246.field_10124 || block == class_2246.field_10164 || block == class_2246.field_10543;
    }

    private void clear() {
        this.foundDebris.clear();
        this.clickedDebris.clear();
        this.delayedChecks.clear();
        this.lastClickTime = 0L;
    }

    private static class DelayBlock {
        class_2338 pos;
        int ticks;

        DelayBlock(class_2338 pos, int ticks) {
            this.pos = pos;
            this.ticks = ticks;
        }
    }
}

