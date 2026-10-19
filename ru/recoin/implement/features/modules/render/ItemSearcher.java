/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 *  net.minecraft.class_1263
 *  net.minecraft.class_1268
 *  net.minecraft.class_1269
 *  net.minecraft.class_1735
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_2338
 *  net.minecraft.class_2338$class_2339
 *  net.minecraft.class_2350
 *  net.minecraft.class_2350$class_2353
 *  net.minecraft.class_2374
 *  net.minecraft.class_238
 *  net.minecraft.class_2382
 *  net.minecraft.class_243
 *  net.minecraft.class_2561
 *  net.minecraft.class_2586
 *  net.minecraft.class_2595
 *  net.minecraft.class_2596
 *  net.minecraft.class_2879
 *  net.minecraft.class_3965
 *  net.minecraft.class_437
 *  net.minecraft.class_465
 *  net.minecraft.class_5250
 */
package ru.recoin.implement.features.modules.render;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_124;
import net.minecraft.class_1263;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1735;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2374;
import net.minecraft.class_238;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2586;
import net.minecraft.class_2595;
import net.minecraft.class_2596;
import net.minecraft.class_2879;
import net.minecraft.class_3965;
import net.minecraft.class_437;
import net.minecraft.class_465;
import net.minecraft.class_5250;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.common.QuickLogger;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.render.Render3DUtil;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.events.render.WorldLoadEvent;
import ru.recoin.implement.events.render.WorldRenderEvent;
import ru.recoin.implement.features.draggables.Notifications;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;

public class ItemSearcher
extends Module {
    static int SEARCH_RADIUS = 20;
    static int WALK_SEARCH_RADIUS = 6;
    static int WALK_SEARCH_Y = 3;
    static double OPEN_DISTANCE = 4.6;
    static int FOUND_COLOR = -1878982810;
    private final Set<class_2338> foundContainers = ConcurrentHashMap.newKeySet();
    private final Set<class_2338> queuedContainers = ConcurrentHashMap.newKeySet();
    private final Queue<class_2338> containerQueue = new ArrayDeque<class_2338>();
    private final Set<class_2338> checkedContainers = ConcurrentHashMap.newKeySet();
    private class_1792 targetItem = class_1802.field_8162;
    private String targetName = "";
    private class_2338 currentTarget;
    private class_2338 currentWalkPos;
    private boolean searching;
    private boolean baritoneAvailable;
    private boolean sentGoto;
    private boolean baritoneConfigured;
    private boolean manualWalking;
    private int totalContainers;
    private int foundItemCount;
    private long nextActionAt;
    private long openDeadline;
    private long lastGotoAt;
    private long navigationStartedAt;
    private double lastWalkDistance;

    public ItemSearcher() {
        super("ItemSearcher", "Item Searcher", ModuleCategory.RENDER);
    }

    public static ItemSearcher getInstance() {
        return Instance.get(ItemSearcher.class);
    }

    public void startSearch(class_1792 item, String name) {
        this.targetItem = item;
        this.targetName = name;
        this.foundContainers.clear();
        this.queuedContainers.clear();
        this.checkedContainers.clear();
        this.containerQueue.clear();
        this.currentTarget = null;
        this.currentWalkPos = null;
        this.sentGoto = false;
        this.baritoneConfigured = false;
        this.manualWalking = false;
        this.foundItemCount = 0;
        this.baritoneAvailable = this.isBaritoneAvailable();
        this.collectContainers();
        this.totalContainers = this.containerQueue.size();
        this.searching = this.totalContainers > 0;
        this.nextActionAt = System.currentTimeMillis();
        this.openDeadline = 0L;
        this.lastGotoAt = 0L;
        this.navigationStartedAt = 0L;
        this.lastWalkDistance = Double.MAX_VALUE;
        if (!this.isState()) {
            this.setState(true);
        }
        this.sendChat((class_2561)class_2561.method_43470((String)"[ItemSearcher] ").method_27692(class_124.field_1075).method_10852((class_2561)class_2561.method_43470((String)"\u041a\u043e\u043d\u0442\u0435\u0439\u043d\u0435\u0440\u043e\u0432 \u0440\u044f\u0434\u043e\u043c: ").method_27692(class_124.field_1080)).method_10852((class_2561)class_2561.method_43470((String)String.valueOf(this.totalContainers)).method_27692(class_124.field_1068)));
        if (!this.searching) {
            this.finishSearch();
        }
    }

    @Override
    public void deactivate() {
        this.foundContainers.clear();
        this.queuedContainers.clear();
        this.checkedContainers.clear();
        this.containerQueue.clear();
        this.currentTarget = null;
        this.currentWalkPos = null;
        this.searching = false;
        this.baritoneConfigured = false;
        this.manualWalking = false;
        this.foundItemCount = 0;
        this.targetItem = class_1802.field_8162;
        this.targetName = "";
        this.stopManualWalking();
        this.stopBaritone();
        super.deactivate();
    }

    @EventHandler
    public void onTick(TickEvent event) {
        class_437 class_4372;
        if (!this.searching || ItemSearcher.mc.field_1724 == null || ItemSearcher.mc.field_1687 == null || ItemSearcher.mc.field_1761 == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now < this.nextActionAt && !this.manualWalking) {
            return;
        }
        if (this.currentTarget != null && (class_4372 = ItemSearcher.mc.field_1755) instanceof class_465) {
            class_465 screen = (class_465)class_4372;
            this.stopManualWalking();
            int foundInContainer = this.checkOpenedContainer(screen);
            ItemSearcher.mc.field_1724.method_7346();
            this.finishCurrentTarget();
            this.sendProgressLog(foundInContainer);
            this.nextActionAt = now + 250L;
            return;
        }
        if (this.currentTarget != null && this.openDeadline > 0L) {
            if (now > this.openDeadline) {
                this.sendChat((class_2561)class_2561.method_43470((String)"[ItemSearcher] \u041d\u0435 \u0441\u043c\u043e\u0433 \u043e\u0442\u043a\u0440\u044b\u0442\u044c \u043a\u043e\u043d\u0442\u0435\u0439\u043d\u0435\u0440: ").method_27692(class_124.field_1054).method_10852((class_2561)class_2561.method_43470((String)this.currentTarget.method_23854()).method_27692(class_124.field_1080)));
                this.finishCurrentTarget();
                this.nextActionAt = now + 150L;
            }
            return;
        }
        if (this.currentTarget == null) {
            this.currentTarget = this.pollNextContainer();
            this.currentWalkPos = null;
            this.sentGoto = false;
            this.manualWalking = false;
            this.navigationStartedAt = 0L;
            this.lastWalkDistance = Double.MAX_VALUE;
            this.openDeadline = 0L;
            if (this.currentTarget == null) {
                this.finishSearch();
                return;
            }
        }
        if (ItemSearcher.mc.field_1755 != null) {
            return;
        }
        if (this.isNear(this.currentTarget)) {
            if (this.sentGoto) {
                this.stopBaritone();
                this.sentGoto = false;
            }
            this.stopManualWalking();
            this.openCurrentTarget(now);
            return;
        }
        if (!this.sentGoto) {
            this.configureBaritoneForSafeWalking();
            this.currentWalkPos = this.findWalkPos(this.currentTarget);
            if (this.currentWalkPos == null) {
                this.sendChat((class_2561)class_2561.method_43470((String)"[ItemSearcher] ").method_27692(class_124.field_1075).method_10852((class_2561)class_2561.method_43470((String)"\u041d\u0435 \u043d\u0430\u0448\u0451\u043b \u0442\u043e\u0447\u043a\u0443 \u043f\u043e\u0434\u0445\u043e\u0434\u0430 \u043a \u043a\u043e\u043d\u0442\u0435\u0439\u043d\u0435\u0440\u0443: ").method_27692(class_124.field_1054)).method_10852((class_2561)class_2561.method_43470((String)this.currentTarget.method_23854()).method_27692(class_124.field_1080)));
                this.finishCurrentTarget();
                this.nextActionAt = now + 150L;
                return;
            }
            if (!this.baritoneAvailable || this.isContainerFloor(this.currentWalkPos)) {
                this.manualWalking = true;
                this.sentGoto = true;
                this.navigationStartedAt = now;
                this.lastWalkDistance = this.distanceToWalkPos();
                this.sendChat((class_2561)class_2561.method_43470((String)"[ItemSearcher] ").method_27692(class_124.field_1075).method_10852((class_2561)class_2561.method_43470((String)"\u0418\u0434\u0443 \u0441\u0430\u043c \u043f\u043e \u043a\u043e\u043d\u0442\u0435\u0439\u043d\u0435\u0440\u0430\u043c \u043a: ").method_27692(class_124.field_1054)).method_10852((class_2561)class_2561.method_43470((String)this.currentWalkPos.method_23854()).method_27692(class_124.field_1080)));
                this.manualWalkTo(this.currentWalkPos);
                return;
            }
            this.sendBaritone("#goto " + this.currentWalkPos.method_10263() + " " + this.currentWalkPos.method_10264() + " " + this.currentWalkPos.method_10260());
            this.sentGoto = true;
            this.lastGotoAt = now;
            this.navigationStartedAt = now;
            this.lastWalkDistance = this.distanceToWalkPos();
            this.nextActionAt = now + 250L;
            return;
        }
        double distance = this.distanceToWalkPos();
        if (!this.manualWalking && now - this.navigationStartedAt > 2000L && distance >= this.lastWalkDistance - 0.15) {
            this.manualWalking = true;
            this.stopBaritone();
            this.sendChat((class_2561)class_2561.method_43470((String)"[ItemSearcher] ").method_27692(class_124.field_1075).method_10852((class_2561)class_2561.method_43470((String)"Baritone \u0437\u0430\u0432\u0438\u0441, \u0438\u0434\u0443 \u0441\u0430\u043c \u043a: ").method_27692(class_124.field_1054)).method_10852((class_2561)class_2561.method_43470((String)this.currentWalkPos.method_23854()).method_27692(class_124.field_1080)));
        }
        this.lastWalkDistance = Math.min(this.lastWalkDistance, distance);
        if (this.manualWalking) {
            this.manualWalkTo(this.currentWalkPos);
        }
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        if (ItemSearcher.mc.field_1687 == null || this.foundContainers.isEmpty()) {
            return;
        }
        Iterator<class_2338> iterator2 = this.foundContainers.iterator();
        while (iterator2.hasNext()) {
            class_2338 pos = iterator2.next();
            if (ItemSearcher.mc.field_1687.method_8321(pos) == null) {
                iterator2.remove();
                continue;
            }
            Render3DUtil.drawBox(new class_238(pos), FOUND_COLOR, 2.0f, true, true, true);
        }
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        this.foundContainers.clear();
        this.queuedContainers.clear();
        this.checkedContainers.clear();
        this.containerQueue.clear();
        this.currentTarget = null;
        this.searching = false;
        this.baritoneConfigured = false;
        this.foundItemCount = 0;
    }

    private void collectContainers() {
        ArrayList<class_2338> containers = new ArrayList<class_2338>();
        class_2338 center = ItemSearcher.mc.field_1724.method_24515();
        class_2338.class_2339 mutable = new class_2338.class_2339();
        for (int x = -SEARCH_RADIUS; x <= SEARCH_RADIUS; ++x) {
            for (int y = -SEARCH_RADIUS; y <= SEARCH_RADIUS; ++y) {
                for (int z = -SEARCH_RADIUS; z <= SEARCH_RADIUS; ++z) {
                    class_2586 blockEntity;
                    mutable.method_10103(center.method_10263() + x, center.method_10264() + y, center.method_10260() + z);
                    class_2338 pos2 = mutable.method_10062();
                    if (!this.isInSearchRadius(pos2) || !((blockEntity = ItemSearcher.mc.field_1687.method_8321(pos2)) instanceof class_1263) || !this.queuedContainers.add(pos2)) continue;
                    containers.add(pos2);
                }
            }
        }
        containers.stream().sorted(Comparator.comparingDouble(pos -> pos.method_10262((class_2382)ItemSearcher.mc.field_1724.method_24515()))).forEach(this.containerQueue::offer);
    }

    private class_2338 pollNextContainer() {
        while (!this.containerQueue.isEmpty()) {
            class_2338 pos = this.containerQueue.poll();
            if (pos == null || this.checkedContainers.contains(pos) || !(ItemSearcher.mc.field_1687.method_8321(pos) instanceof class_1263)) continue;
            return pos;
        }
        return null;
    }

    private void openCurrentTarget(long now) {
        class_2350 side = this.getInteractSide(this.currentTarget);
        class_3965 hit = new class_3965(this.currentTarget.method_46558().method_1031((double)side.method_10148() * 0.5, (double)side.method_10164() * 0.5, (double)side.method_10165() * 0.5), side, this.currentTarget, false);
        Angle angle = AngleUtil.calculateAngle(hit.method_17784());
        ItemSearcher.mc.field_1724.method_36456(angle.getYaw());
        ItemSearcher.mc.field_1724.method_36457(angle.getPitch());
        class_1269 result = ItemSearcher.mc.field_1761.method_2896(ItemSearcher.mc.field_1724, class_1268.field_5808, hit);
        if (result.method_23665()) {
            ItemSearcher.mc.field_1724.method_6104(class_1268.field_5808);
            ItemSearcher.mc.field_1724.field_3944.method_52787((class_2596)new class_2879(class_1268.field_5808));
        }
        this.openDeadline = now + 1500L;
        this.nextActionAt = now + 100L;
    }

    private int checkOpenedContainer(class_465<?> screen) {
        int count = 0;
        int containerSlots = Math.max(0, screen.method_17577().field_7761.size() - 36);
        for (int i = 0; i < containerSlots; ++i) {
            class_1735 slot = screen.method_17577().method_7611(i);
            class_1799 stack = slot.method_7677();
            if (stack.method_7960() || !stack.method_31574(this.targetItem)) continue;
            count += stack.method_7947();
        }
        if (count > 0 && this.currentTarget != null) {
            this.foundContainers.add(this.currentTarget.method_10062());
            this.foundItemCount += count;
        }
        return count;
    }

    private void finishCurrentTarget() {
        if (this.currentTarget != null) {
            this.markChecked(this.currentTarget);
        }
        this.stopManualWalking();
        this.currentTarget = null;
        this.currentWalkPos = null;
        this.openDeadline = 0L;
        this.sentGoto = false;
    }

    private void finishSearch() {
        this.searching = false;
        this.stopManualWalking();
        this.stopBaritone();
        int checked = this.checkedContainers.size();
        int found = this.foundContainers.size();
        class_5250 message = class_2561.method_43470((String)"[ItemSearcher] ").method_27692(class_124.field_1075).method_10852((class_2561)class_2561.method_43470((String)"\u041f\u0440\u043e\u0432\u0435\u0440\u0435\u043d\u043e: ").method_27692(class_124.field_1080)).method_10852((class_2561)class_2561.method_43470((String)(checked + "/" + this.totalContainers)).method_27692(class_124.field_1068)).method_10852((class_2561)class_2561.method_43470((String)", \u043d\u0430\u0439\u0434\u0435\u043d\u043e: ").method_27692(class_124.field_1080)).method_10852((class_2561)class_2561.method_43470((String)String.valueOf(found)).method_27692(found > 0 ? class_124.field_1060 : class_124.field_1061));
        this.sendChat((class_2561)message);
        this.sendChat((class_2561)class_2561.method_43470((String)"[ItemSearcher] ").method_27692(class_124.field_1075).method_10852((class_2561)class_2561.method_43470((String)"\u0418\u0442\u043e\u0433: \u043f\u0440\u043e\u0432\u0435\u0440\u0435\u043d\u043e ").method_27692(class_124.field_1080)).method_10852((class_2561)class_2561.method_43470((String)(checked + "/" + this.totalContainers)).method_27692(class_124.field_1068)).method_10852((class_2561)class_2561.method_43470((String)", \u043a\u043e\u043d\u0442\u0435\u0439\u043d\u0435\u0440\u043e\u0432 \u0441 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u043e\u043c ").method_27692(class_124.field_1080)).method_10852((class_2561)class_2561.method_43470((String)String.valueOf(found)).method_27692(found > 0 ? class_124.field_1060 : class_124.field_1061)).method_10852((class_2561)class_2561.method_43470((String)", \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u043e\u0432 \u0432\u0441\u0435\u0433\u043e ").method_27692(class_124.field_1080)).method_10852((class_2561)class_2561.method_43470((String)String.valueOf(this.foundItemCount)).method_27692(this.foundItemCount > 0 ? class_124.field_1060 : class_124.field_1061)));
        if (found > 0) {
            Notifications.getInstance().addList((class_2561)class_2561.method_43470((String)"ItemSearcher: \u043d\u0430\u0439\u0434\u0435\u043d\u043e ").method_10852((class_2561)class_2561.method_43470((String)String.valueOf(found)).method_27692(class_124.field_1060)).method_10852((class_2561)class_2561.method_43470((String)" \u043a\u043e\u043d\u0442\u0435\u0439\u043d\u0435\u0440(\u043e\u0432)")), 3000L, this.targetItem);
        } else {
            Notifications.getInstance().addList((class_2561)class_2561.method_43470((String)"ItemSearcher: ").method_10852((class_2561)class_2561.method_43470((String)this.targetName).method_27692(class_124.field_1061)).method_10852((class_2561)class_2561.method_43470((String)" \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d")), 3000L, this.targetItem);
            this.setState(false);
        }
    }

    private boolean isInSearchRadius(class_2338 pos) {
        return ItemSearcher.mc.field_1724 != null && pos.method_19769((class_2374)ItemSearcher.mc.field_1724.method_73189(), (double)SEARCH_RADIUS);
    }

    private boolean isNear(class_2338 pos) {
        return ItemSearcher.mc.field_1724 != null && pos.method_46558().method_1025(ItemSearcher.mc.field_1724.method_33571()) <= OPEN_DISTANCE * OPEN_DISTANCE;
    }

    private class_2350 getInteractSide(class_2338 pos) {
        if (ItemSearcher.mc.field_1724 == null) {
            return class_2350.field_11036;
        }
        return class_2350.method_10142((double)(ItemSearcher.mc.field_1724.method_33571().field_1352 - (double)pos.method_10263() - 0.5), (double)(ItemSearcher.mc.field_1724.method_33571().field_1351 - (double)pos.method_10264() - 0.5), (double)(ItemSearcher.mc.field_1724.method_33571().field_1350 - (double)pos.method_10260() - 0.5));
    }

    private class_2338 findWalkPos(class_2338 container) {
        class_2338 best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int x = -WALK_SEARCH_RADIUS; x <= WALK_SEARCH_RADIUS; ++x) {
            for (int y = -WALK_SEARCH_Y; y <= WALK_SEARCH_Y; ++y) {
                for (int z = -WALK_SEARCH_RADIUS; z <= WALK_SEARCH_RADIUS; ++z) {
                    double distance;
                    class_2338 candidate = container.method_10069(x, y, z);
                    if (candidate.equals((Object)container) || !this.canStandAt(candidate) || !this.canOpenFrom(candidate, container) || !((distance = candidate.method_10262((class_2382)ItemSearcher.mc.field_1724.method_24515())) < bestDistance)) continue;
                    bestDistance = distance;
                    best = candidate;
                }
            }
        }
        return best;
    }

    private boolean canStandAt(class_2338 pos) {
        if (ItemSearcher.mc.field_1687 == null) {
            return false;
        }
        return ItemSearcher.mc.field_1687.method_8320(pos).method_26215() && ItemSearcher.mc.field_1687.method_8320(pos.method_10084()).method_26215() && !ItemSearcher.mc.field_1687.method_8320(pos.method_10074()).method_26215();
    }

    private boolean canOpenFrom(class_2338 walkPos, class_2338 container) {
        if (ItemSearcher.mc.field_1724 == null) {
            return false;
        }
        return walkPos.method_46558().method_1031(0.0, (double)ItemSearcher.mc.field_1724.method_18381(ItemSearcher.mc.field_1724.method_18376()), 0.0).method_1025(container.method_46558()) <= OPEN_DISTANCE * OPEN_DISTANCE;
    }

    private boolean isContainerFloor(class_2338 walkPos) {
        return ItemSearcher.mc.field_1687 != null && ItemSearcher.mc.field_1687.method_8321(walkPos.method_10074()) instanceof class_1263;
    }

    private double distanceToWalkPos() {
        if (ItemSearcher.mc.field_1724 == null || this.currentWalkPos == null) {
            return Double.MAX_VALUE;
        }
        return ItemSearcher.mc.field_1724.method_73189().method_1025(this.currentWalkPos.method_46558());
    }

    private void manualWalkTo(class_2338 walkPos) {
        if (ItemSearcher.mc.field_1724 == null || walkPos == null) {
            this.stopManualWalking();
            return;
        }
        class_243 target = walkPos.method_46558();
        Angle angle = AngleUtil.calculateAngle(target);
        ItemSearcher.mc.field_1724.method_36456(angle.getYaw());
        ItemSearcher.mc.field_1724.method_36457(0.0f);
        ItemSearcher.mc.field_1690.field_1894.method_23481(true);
        ItemSearcher.mc.field_1690.field_1867.method_23481(true);
        ItemSearcher.mc.field_1690.field_1881.method_23481(false);
        ItemSearcher.mc.field_1690.field_1913.method_23481(false);
        ItemSearcher.mc.field_1690.field_1849.method_23481(false);
        boolean shouldJump = ItemSearcher.mc.field_1724.field_5976 || target.field_1351 > ItemSearcher.mc.field_1724.method_23318() + 0.3;
        ItemSearcher.mc.field_1690.field_1903.method_23481(shouldJump);
        class_243 forward = angle.toVector();
        class_243 currentVelocity = ItemSearcher.mc.field_1724.method_18798();
        ItemSearcher.mc.field_1724.method_18800(forward.field_1352 * 0.22, currentVelocity.field_1351, forward.field_1350 * 0.22);
        if (ItemSearcher.mc.field_1724.method_73189().method_1025(target) <= 1.0) {
            this.stopManualWalking();
        }
    }

    private void stopManualWalking() {
        this.manualWalking = false;
        if (ItemSearcher.mc.field_1690 == null) {
            return;
        }
        ItemSearcher.mc.field_1690.field_1894.method_23481(false);
        ItemSearcher.mc.field_1690.field_1881.method_23481(false);
        ItemSearcher.mc.field_1690.field_1913.method_23481(false);
        ItemSearcher.mc.field_1690.field_1849.method_23481(false);
        ItemSearcher.mc.field_1690.field_1903.method_23481(false);
        ItemSearcher.mc.field_1690.field_1867.method_23481(false);
    }

    private void sendProgressLog(int foundInContainer) {
        this.sendChat((class_2561)class_2561.method_43470((String)"[ItemSearcher] ").method_27692(class_124.field_1075).method_10852((class_2561)class_2561.method_43470((String)"\u041f\u0440\u043e\u0432\u0435\u0440\u0435\u043d\u043e: ").method_27692(class_124.field_1080)).method_10852((class_2561)class_2561.method_43470((String)(this.checkedContainers.size() + "/" + this.totalContainers)).method_27692(class_124.field_1068)).method_10852((class_2561)class_2561.method_43470((String)", \u0432 \u044d\u0442\u043e\u043c \u043a\u043e\u043d\u0442\u0435\u0439\u043d\u0435\u0440\u0435: ").method_27692(class_124.field_1080)).method_10852((class_2561)class_2561.method_43470((String)String.valueOf(foundInContainer)).method_27692(foundInContainer > 0 ? class_124.field_1060 : class_124.field_1061)).method_10852((class_2561)class_2561.method_43470((String)", \u0432\u0441\u0435\u0433\u043e \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u043e\u0432: ").method_27692(class_124.field_1080)).method_10852((class_2561)class_2561.method_43470((String)String.valueOf(this.foundItemCount)).method_27692(this.foundItemCount > 0 ? class_124.field_1060 : class_124.field_1061)));
    }

    private void markChecked(class_2338 pos) {
        this.checkedContainers.add(pos.method_10062());
        if (!(ItemSearcher.mc.field_1687.method_8321(pos) instanceof class_2595)) {
            return;
        }
        for (class_2350 direction : class_2350.class_2353.field_11062) {
            class_2338 adjacent = pos.method_10093(direction);
            if (!(ItemSearcher.mc.field_1687.method_8321(adjacent) instanceof class_2595)) continue;
            this.checkedContainers.add(adjacent.method_10062());
        }
    }

    private void configureBaritoneForSafeWalking() {
        if (!this.baritoneAvailable || this.baritoneConfigured) {
            return;
        }
        this.sendBaritone("#set allowBreak false");
        this.sendBaritone("#set allowPlace false");
        this.sendBaritone("#set allowInventory false");
        this.sendBaritone("#set allowParkour false");
        this.baritoneConfigured = true;
    }

    private void sendBaritone(String command) {
        if (ItemSearcher.mc.field_1724 != null && ItemSearcher.mc.field_1724.field_3944 != null) {
            ItemSearcher.mc.field_1724.field_3944.method_45729(command);
        }
    }

    private void stopBaritone() {
        if (this.baritoneAvailable) {
            this.sendBaritone("#stop");
        }
    }

    private boolean isBaritoneAvailable() {
        try {
            Class.forName("baritone.api.BaritoneAPI");
            return true;
        }
        catch (Throwable ignored) {
            return false;
        }
    }

    private void sendChat(class_2561 text) {
        if (ItemSearcher.mc.field_1705 != null) {
            ItemSearcher.mc.field_1705.method_1743().method_1812(QuickLogger.clientMessage(text));
        }
    }

    public Set<class_2338> getFoundContainers() {
        return this.foundContainers;
    }

    public Set<class_2338> getQueuedContainers() {
        return this.queuedContainers;
    }

    public Queue<class_2338> getContainerQueue() {
        return this.containerQueue;
    }

    public Set<class_2338> getCheckedContainers() {
        return this.checkedContainers;
    }

    public class_1792 getTargetItem() {
        return this.targetItem;
    }

    public String getTargetName() {
        return this.targetName;
    }

    public class_2338 getCurrentTarget() {
        return this.currentTarget;
    }

    public class_2338 getCurrentWalkPos() {
        return this.currentWalkPos;
    }

    public boolean isSearching() {
        return this.searching;
    }

    public boolean isSentGoto() {
        return this.sentGoto;
    }

    public boolean isBaritoneConfigured() {
        return this.baritoneConfigured;
    }

    public boolean isManualWalking() {
        return this.manualWalking;
    }

    public int getTotalContainers() {
        return this.totalContainers;
    }

    public int getFoundItemCount() {
        return this.foundItemCount;
    }

    public long getNextActionAt() {
        return this.nextActionAt;
    }

    public long getOpenDeadline() {
        return this.openDeadline;
    }

    public long getLastGotoAt() {
        return this.lastGotoAt;
    }

    public long getNavigationStartedAt() {
        return this.navigationStartedAt;
    }

    public double getLastWalkDistance() {
        return this.lastWalkDistance;
    }
}

