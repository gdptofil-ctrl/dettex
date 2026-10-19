/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1263
 *  net.minecraft.class_1268
 *  net.minecraft.class_1269
 *  net.minecraft.class_1657
 *  net.minecraft.class_1713
 *  net.minecraft.class_1735
 *  net.minecraft.class_1792
 *  net.minecraft.class_1792$class_9635
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1836
 *  net.minecraft.class_1844
 *  net.minecraft.class_1887
 *  net.minecraft.class_1890
 *  net.minecraft.class_1893
 *  net.minecraft.class_1922
 *  net.minecraft.class_1937
 *  net.minecraft.class_2248
 *  net.minecraft.class_2281
 *  net.minecraft.class_2338
 *  net.minecraft.class_2338$class_2339
 *  net.minecraft.class_2350
 *  net.minecraft.class_2350$class_2353
 *  net.minecraft.class_2378
 *  net.minecraft.class_2382
 *  net.minecraft.class_243
 *  net.minecraft.class_2480
 *  net.minecraft.class_2561
 *  net.minecraft.class_2586
 *  net.minecraft.class_2596
 *  net.minecraft.class_2879
 *  net.minecraft.class_3708
 *  net.minecraft.class_3965
 *  net.minecraft.class_437
 *  net.minecraft.class_465
 *  net.minecraft.class_5321
 *  net.minecraft.class_6880
 *  net.minecraft.class_7924
 *  net.minecraft.class_9334
 */
package ru.recoin.implement.features.modules.misc;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.class_1263;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1657;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1836;
import net.minecraft.class_1844;
import net.minecraft.class_1887;
import net.minecraft.class_1890;
import net.minecraft.class_1893;
import net.minecraft.class_1922;
import net.minecraft.class_1937;
import net.minecraft.class_2248;
import net.minecraft.class_2281;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2378;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_2480;
import net.minecraft.class_2561;
import net.minecraft.class_2586;
import net.minecraft.class_2596;
import net.minecraft.class_2879;
import net.minecraft.class_3708;
import net.minecraft.class_3965;
import net.minecraft.class_437;
import net.minecraft.class_465;
import net.minecraft.class_5321;
import net.minecraft.class_6880;
import net.minecraft.class_7924;
import net.minecraft.class_9334;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.ButtonSetting;
import ru.recoin.api.feature.module.setting.implement.MultiSelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.QuickLogger;
import ru.recoin.common.util.other.BaritoneCompat;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.events.render.WorldLoadEvent;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.screens.autoinventory.AutoInventoryScreen;

public final class AutoInventory
extends Module {
    private static final double OPEN_DISTANCE_SQ = 21.159999999999997;
    private static final int WALK_SEARCH_RADIUS = 5;
    private static final int WALK_SEARCH_Y = 3;
    private static final int PATH_NODE_LIMIT = 8000;
    public final ButtonSetting openCollector = new ButtonSetting("Collector Menu", "Open inventory collector").setButtonName("\u041e\u0442\u043a\u0440\u044b\u0442\u044c").setRunnable(() -> mc.method_1507((class_437)new AutoInventoryScreen(this)));
    public final ValueSetting radius = new ValueSetting("Search Radius", "Container scan radius").setValue(24.0f).range(6.0f, 64.0f).step(2.0f);
    public final ValueSetting delay = new ValueSetting("Delay", "Delay between container actions").setValue(140.0f).range(60.0f, 700.0f).step(20.0f);
    public final MultiSelectSetting selectedItems;
    public final MultiSelectSetting armorEnchants = new MultiSelectSetting("Armor Enchants", "Required armor enchants").value("\u0417\u0430\u0449\u0438\u0442\u0430 5", "\u041f\u0440\u043e\u0447\u043d\u043e\u0441\u0442\u044c 5", "\u041f\u043e\u0447\u0438\u043d\u043a\u0430").selected("\u0417\u0430\u0449\u0438\u0442\u0430 5", "\u041f\u0440\u043e\u0447\u043d\u043e\u0441\u0442\u044c 5", "\u041f\u043e\u0447\u0438\u043d\u043a\u0430").visible(() -> false);
    public final MultiSelectSetting swordEnchants = new MultiSelectSetting("Sword Enchants", "Required sword enchants").value("\u042f\u0434", "\u041e\u043a\u0438\u0441\u043b\u0435\u043d\u0438\u0435", "\u0417\u0430\u0433\u043e\u0432\u043e\u0440 \u043e\u0433\u043d\u044f 2", "\u041e\u0441\u0442\u0440\u043e\u0442\u0430 7").selected("\u042f\u0434", "\u041e\u043a\u0438\u0441\u043b\u0435\u043d\u0438\u0435", "\u0417\u0430\u0433\u043e\u0432\u043e\u0440 \u043e\u0433\u043d\u044f 2", "\u041e\u0441\u0442\u0440\u043e\u0442\u0430 7").visible(() -> false);
    public final BooleanSetting rejectThorns = new BooleanSetting("No Thorns", "Do not take armor with Thorns").setValue(true).visible(() -> false);
    public final BooleanSetting repairedArmor = new BooleanSetting("Repaired Armor", "Only take armor at full durability").setValue(false).visible(() -> false);
    private final List<Entry> catalog = AutoInventory.createCatalog();
    private final Map<String, Integer> targets = new LinkedHashMap<String, Integer>();
    private final Map<String, class_1799> catalogIcons = new HashMap<String, class_1799>();
    private final Queue<class_2338> queue = new ArrayDeque<class_2338>();
    private final Set<class_2338> visited = new HashSet<class_2338>();
    private final Deque<class_2338> manualPath = new ArrayDeque<class_2338>();
    private class_2338 current;
    private class_2338 currentWalkPos;
    private class_2338 manualPathGoal;
    private long nextAction;
    private long openDeadline;
    private long navigationStartedAt;
    private double lastWalkDistance = Double.MAX_VALUE;
    private boolean manualWalking;
    private Object baritone;
    private int taken;
    private int scanned;

    public AutoInventory() {
        super("AutoInventory", "Auto Inventory", ModuleCategory.MISC);
        String[] names = (String[])this.catalog.stream().map(Entry::name).toArray(String[]::new);
        this.selectedItems = new MultiSelectSetting("Collector Items", "Items selected in collector menu").value(names).visible(() -> false);
        for (Entry entry : this.catalog) {
            this.targets.put(entry.name(), entry.defaultTarget());
        }
        this.setup(this.openCollector, this.radius, this.delay, this.selectedItems, this.armorEnchants, this.swordEnchants, this.rejectThorns, this.repairedArmor);
    }

    @Override
    public void activate() {
        if (AutoInventory.mc.field_1724 == null || AutoInventory.mc.field_1687 == null) {
            return;
        }
        this.queue.clear();
        this.visited.clear();
        this.current = null;
        this.currentWalkPos = null;
        this.clearManualPath();
        this.manualWalking = false;
        this.navigationStartedAt = 0L;
        this.lastWalkDistance = Double.MAX_VALUE;
        this.scanned = 0;
        this.taken = 0;
        this.baritone = BaritoneCompat.getPrimaryBaritone();
        this.collectContainers();
        this.nextAction = System.currentTimeMillis();
        if (this.selectedItems.getSelected().isEmpty()) {
            this.message("\u0421\u043f\u0438\u0441\u043e\u043a \u0441\u0431\u043e\u0440\u0449\u0438\u043a\u0430 \u043f\u0443\u0441\u0442. \u041e\u0442\u043a\u0440\u043e\u0439\u0442\u0435 Collector Menu.");
            this.setState(false);
        } else if (this.queue.isEmpty()) {
            this.message("\u0412 \u0440\u0430\u0434\u0438\u0443\u0441\u0435 \u043f\u043e\u0438\u0441\u043a\u0430 \u043d\u0435\u0442 \u0441\u0443\u043d\u0434\u0443\u043a\u043e\u0432 \u0438\u043b\u0438 \u0448\u0430\u043b\u043a\u0435\u0440\u043e\u0432.");
            this.setState(false);
        } else {
            this.message("\u041d\u0430\u0439\u0434\u0435\u043d\u043e \u043a\u043e\u043d\u0442\u0435\u0439\u043d\u0435\u0440\u043e\u0432: " + this.queue.size());
        }
    }

    @Override
    public void deactivate() {
        BaritoneCompat.cancelPathing(this.baritone);
        this.stopManualWalking();
        if (AutoInventory.mc.field_1724 != null && AutoInventory.mc.field_1724.field_7512 != AutoInventory.mc.field_1724.field_7498) {
            AutoInventory.mc.field_1724.method_7346();
        }
        this.queue.clear();
        this.current = null;
        this.currentWalkPos = null;
        this.clearManualPath();
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        this.queue.clear();
        this.visited.clear();
        this.current = null;
        this.currentWalkPos = null;
        this.catalogIcons.clear();
        this.clearManualPath();
        this.stopManualWalking();
    }

    @EventHandler
    public void onTick(TickEvent event) {
        class_437 class_4372;
        if (AutoInventory.mc.field_1724 == null || AutoInventory.mc.field_1687 == null || AutoInventory.mc.field_1761 == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now < this.nextAction && !this.manualWalking) {
            return;
        }
        if (this.requirementsMet()) {
            this.finish("\u0418\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u044c \u0441\u043e\u0431\u0440\u0430\u043d");
            return;
        }
        if (this.current != null && (class_4372 = AutoInventory.mc.field_1755) instanceof class_465) {
            class_465 screen = (class_465)class_4372;
            this.stopManualWalking();
            int slot = this.findWantedSlot(screen);
            if (slot >= 0) {
                class_1799 stack = screen.method_17577().method_7611(slot).method_7677();
                this.taken += stack.method_7947();
                AutoInventory.mc.field_1761.method_2906(screen.method_17577().field_7763, slot, 0, class_1713.field_7794, (class_1657)AutoInventory.mc.field_1724);
                this.nextAction = now + (long)this.delay.getInt();
                return;
            }
            AutoInventory.mc.field_1724.method_7346();
            ++this.scanned;
            this.visited.add(this.current);
            this.current = null;
            this.currentWalkPos = null;
            this.nextAction = now + (long)this.delay.getInt();
            return;
        }
        if (this.current != null && this.openDeadline > 0L && now > this.openDeadline) {
            this.stopManualWalking();
            this.visited.add(this.current);
            this.current = null;
            this.currentWalkPos = null;
            this.openDeadline = 0L;
        }
        if (this.current == null) {
            this.current = this.pollContainer();
            if (this.current == null) {
                this.finish("\u041a\u043e\u043d\u0442\u0435\u0439\u043d\u0435\u0440\u044b \u0437\u0430\u043a\u043e\u043d\u0447\u0438\u043b\u0438\u0441\u044c");
                return;
            }
            this.currentWalkPos = this.findWalkPos(this.current);
            this.manualWalking = this.baritone == null;
            this.clearManualPath();
            this.navigationStartedAt = 0L;
            this.lastWalkDistance = Double.MAX_VALUE;
        }
        if (AutoInventory.mc.field_1755 != null) {
            return;
        }
        if (this.current.method_46558().method_1025(AutoInventory.mc.field_1724.method_33571()) > 21.159999999999997) {
            if (this.currentWalkPos == null) {
                this.stopManualWalking();
                this.visited.add(this.current);
                this.current = null;
                return;
            }
            if (!this.manualWalking) {
                double distance = this.distanceToWalkPos();
                if (this.navigationStartedAt == 0L) {
                    BaritoneCompat.goNear(this.baritone, this.current, 3);
                    this.navigationStartedAt = now;
                    this.lastWalkDistance = distance;
                    this.nextAction = now + 300L;
                    return;
                }
                if (now - this.navigationStartedAt > 2000L && distance >= this.lastWalkDistance - 0.15) {
                    BaritoneCompat.cancelPathing(this.baritone);
                    this.manualWalking = true;
                    this.clearManualPath();
                } else {
                    this.lastWalkDistance = Math.min(this.lastWalkDistance, distance);
                    this.nextAction = now + 250L;
                    return;
                }
            }
            if (!this.manualWalkTo(this.currentWalkPos)) {
                this.stopManualWalking();
                this.visited.add(this.current);
                this.current = null;
                this.currentWalkPos = null;
            }
            return;
        }
        BaritoneCompat.cancelPathing(this.baritone);
        this.stopManualWalking();
        this.open(this.current);
        this.openDeadline = now + 1800L;
        this.nextAction = now + 180L;
    }

    private void collectContainers() {
        int r = this.radius.getInt();
        class_2338 center = AutoInventory.mc.field_1724.method_24515();
        HashSet<class_2338> found = new HashSet<class_2338>();
        class_2338.class_2339 mutable = new class_2338.class_2339();
        for (int x = -r; x <= r; ++x) {
            for (int y = -r; y <= r; ++y) {
                for (int z = -r; z <= r; ++z) {
                    if (x * x + y * y + z * z > r * r) continue;
                    mutable.method_10103(center.method_10263() + x, center.method_10264() + y, center.method_10260() + z);
                    if (!AutoInventory.mc.field_1687.method_22340((class_2338)mutable) || !this.isSupportedContainer((class_2338)mutable)) continue;
                    found.add(mutable.method_10062());
                }
            }
        }
        found.stream().sorted(Comparator.comparingDouble(pos -> pos.method_10262((class_2382)center))).forEach(this.queue::offer);
    }

    private class_2338 pollContainer() {
        while (!this.queue.isEmpty()) {
            class_2338 pos = this.queue.poll();
            if (pos == null || this.visited.contains(pos) || !this.isSupportedContainer(pos)) continue;
            return pos;
        }
        return null;
    }

    private boolean isSupportedContainer(class_2338 pos) {
        class_2586 entity = AutoInventory.mc.field_1687.method_8321(pos);
        if (entity instanceof class_1263) {
            return true;
        }
        class_2248 block = AutoInventory.mc.field_1687.method_8320(pos).method_26204();
        return block instanceof class_2480 || block instanceof class_2281 || block instanceof class_3708;
    }

    private class_2338 findWalkPos(class_2338 container) {
        class_2338 best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int x = -5; x <= 5; ++x) {
            for (int y = -3; y <= 3; ++y) {
                for (int z = -5; z <= 5; ++z) {
                    double distance;
                    class_2338 candidate = container.method_10069(x, y, z);
                    if (candidate.equals((Object)container) || !this.canStandAt(candidate) || !this.canOpenFrom(candidate, container) || !((distance = candidate.method_10262((class_2382)AutoInventory.mc.field_1724.method_24515())) < bestDistance)) continue;
                    bestDistance = distance;
                    best = candidate;
                }
            }
        }
        return best;
    }

    private boolean canStandAt(class_2338 pos) {
        if (!AutoInventory.mc.field_1687.method_22340(pos)) {
            return false;
        }
        return this.isPassable(pos) && this.isPassable(pos.method_10084()) && !AutoInventory.mc.field_1687.method_8320(pos.method_10074()).method_26220((class_1922)AutoInventory.mc.field_1687, pos.method_10074()).method_1110();
    }

    private boolean isPassable(class_2338 pos) {
        return AutoInventory.mc.field_1687.method_8320(pos).method_26220((class_1922)AutoInventory.mc.field_1687, pos).method_1110() && AutoInventory.mc.field_1687.method_8316(pos).method_15769();
    }

    private boolean canOpenFrom(class_2338 walkPos, class_2338 container) {
        class_243 eye = walkPos.method_46558().method_1031(0.0, (double)AutoInventory.mc.field_1724.method_18381(AutoInventory.mc.field_1724.method_18376()), 0.0);
        return eye.method_1025(container.method_46558()) <= 21.159999999999997;
    }

    private double distanceToWalkPos() {
        return this.currentWalkPos == null ? Double.MAX_VALUE : AutoInventory.mc.field_1724.method_73189().method_1025(this.currentWalkPos.method_46558());
    }

    private boolean manualWalkTo(class_2338 walkPos) {
        if (walkPos == null) {
            return false;
        }
        if (!walkPos.equals((Object)this.manualPathGoal)) {
            List<class_2338> path = this.findManualPath(this.normalizePathStart(AutoInventory.mc.field_1724.method_24515()), walkPos);
            if (path.isEmpty()) {
                return false;
            }
            this.manualPath.clear();
            for (int i = 1; i < path.size(); ++i) {
                this.manualPath.addLast(path.get(i));
            }
            this.manualPathGoal = walkPos;
        }
        while (!this.manualPath.isEmpty() && AutoInventory.mc.field_1724.method_73189().method_1025(this.manualPath.peekFirst().method_46558()) <= 0.55) {
            this.manualPath.removeFirst();
        }
        if (this.manualPath.isEmpty()) {
            this.releaseManualKeys();
            return AutoInventory.mc.field_1724.method_73189().method_1025(walkPos.method_46558()) <= 1.25;
        }
        class_2338 waypoint = this.manualPath.peekFirst();
        class_243 target = waypoint.method_46558();
        Angle angle = AngleUtil.calculateAngle(target);
        AutoInventory.mc.field_1724.method_36456(angle.getYaw());
        AutoInventory.mc.field_1724.method_36457(0.0f);
        AutoInventory.mc.field_1690.field_1894.method_23481(true);
        AutoInventory.mc.field_1690.field_1867.method_23481(true);
        AutoInventory.mc.field_1690.field_1881.method_23481(false);
        AutoInventory.mc.field_1690.field_1913.method_23481(false);
        AutoInventory.mc.field_1690.field_1849.method_23481(false);
        AutoInventory.mc.field_1690.field_1903.method_23481(waypoint.method_10264() > AutoInventory.mc.field_1724.method_31478() || AutoInventory.mc.field_1724.field_5976 && waypoint.method_10264() >= AutoInventory.mc.field_1724.method_31478());
        return true;
    }

    private class_2338 normalizePathStart(class_2338 start) {
        if (this.canStandAt(start)) {
            return start;
        }
        for (int offset : new int[]{1, -1, 2, -2}) {
            class_2338 candidate = start.method_10086(offset);
            if (!this.canStandAt(candidate)) continue;
            return candidate;
        }
        return start;
    }

    private List<class_2338> findManualPath(class_2338 start, class_2338 goal) {
        if (start == null || goal == null || !this.canStandAt(start) || !this.canStandAt(goal)) {
            return List.of();
        }
        PriorityQueue<PathNode> open = new PriorityQueue<PathNode>(Comparator.comparingDouble(PathNode::score));
        HashMap<class_2338, class_2338> cameFrom = new HashMap<class_2338, class_2338>();
        HashMap<class_2338, Double> costs = new HashMap<class_2338, Double>();
        HashSet<class_2338> closed = new HashSet<class_2338>();
        costs.put(start, 0.0);
        open.add(new PathNode(start, this.pathHeuristic(start, goal)));
        int horizontalLimit = Math.max(24, this.radius.getInt() + 10);
        int expanded = 0;
        while (!open.isEmpty() && expanded++ < 8000) {
            PathNode node = open.poll();
            class_2338 pos = node.pos();
            if (!closed.add(pos)) continue;
            if (pos.equals((Object)goal)) {
                return this.reconstructPath(cameFrom, pos);
            }
            for (class_2350 direction : class_2350.class_2353.field_11062) {
                class_2338 horizontal = pos.method_10093(direction);
                for (int yOffset : new int[]{0, 1, -1}) {
                    class_2338 next = horizontal.method_10086(yOffset);
                    if (closed.contains(next) || !this.insidePathBounds(start, next, horizontalLimit) || !this.canStandAt(next)) continue;
                    double stepCost = yOffset == 0 ? 1.0 : 1.35;
                    double candidateCost = costs.getOrDefault(pos, (Double)Double.MAX_VALUE) + stepCost;
                    if (candidateCost >= costs.getOrDefault(next, (Double)Double.MAX_VALUE)) continue;
                    cameFrom.put(next, pos);
                    costs.put(next, candidateCost);
                    open.add(new PathNode(next, candidateCost + this.pathHeuristic(next, goal)));
                }
            }
        }
        return List.of();
    }

    private boolean insidePathBounds(class_2338 start, class_2338 pos, int horizontalLimit) {
        return Math.abs(pos.method_10263() - start.method_10263()) <= horizontalLimit && Math.abs(pos.method_10260() - start.method_10260()) <= horizontalLimit && Math.abs(pos.method_10264() - start.method_10264()) <= 10;
    }

    private double pathHeuristic(class_2338 from, class_2338 to) {
        return (double)(Math.abs(from.method_10263() - to.method_10263()) + Math.abs(from.method_10260() - to.method_10260())) + (double)Math.abs(from.method_10264() - to.method_10264()) * 1.25;
    }

    private List<class_2338> reconstructPath(Map<class_2338, class_2338> cameFrom, class_2338 goal) {
        LinkedList<class_2338> path = new LinkedList<class_2338>();
        class_2338 cursor = goal;
        path.addFirst(cursor);
        while (cameFrom.containsKey(cursor)) {
            cursor = cameFrom.get(cursor);
            path.addFirst(cursor);
        }
        return path;
    }

    private void clearManualPath() {
        this.manualPath.clear();
        this.manualPathGoal = null;
    }

    private void releaseManualKeys() {
        if (AutoInventory.mc.field_1690 == null) {
            return;
        }
        AutoInventory.mc.field_1690.field_1894.method_23481(false);
        AutoInventory.mc.field_1690.field_1881.method_23481(false);
        AutoInventory.mc.field_1690.field_1913.method_23481(false);
        AutoInventory.mc.field_1690.field_1849.method_23481(false);
        AutoInventory.mc.field_1690.field_1903.method_23481(false);
        AutoInventory.mc.field_1690.field_1867.method_23481(false);
    }

    private void stopManualWalking() {
        this.manualWalking = false;
        this.clearManualPath();
        this.releaseManualKeys();
    }

    private void open(class_2338 pos) {
        class_2350 side = class_2350.method_58251((class_243)AutoInventory.mc.field_1724.method_33571().method_1020(pos.method_46558()));
        class_243 hitPos = pos.method_46558().method_1019(class_243.method_24954((class_2382)side.method_62675()).method_1021(0.5));
        Angle angle = AngleUtil.calculateAngle(hitPos);
        AutoInventory.mc.field_1724.method_36456(angle.getYaw());
        AutoInventory.mc.field_1724.method_36457(angle.getPitch());
        class_1269 result = AutoInventory.mc.field_1761.method_2896(AutoInventory.mc.field_1724, class_1268.field_5808, new class_3965(hitPos, side, pos, false));
        if (result.method_23665()) {
            AutoInventory.mc.field_1724.method_6104(class_1268.field_5808);
            AutoInventory.mc.field_1724.field_3944.method_52787((class_2596)new class_2879(class_1268.field_5808));
        }
    }

    private int findWantedSlot(class_465<?> screen) {
        int containerSlots = Math.max(0, screen.method_17577().field_7761.size() - 36);
        for (int i = 0; i < containerSlots; ++i) {
            class_1735 slot = screen.method_17577().method_7611(i);
            if (!slot.method_7681()) continue;
            this.rememberCatalogIcon(slot.method_7677());
            Entry entry = this.matchingSelectedEntry(slot.method_7677());
            if (entry == null || this.countMatching(entry) >= this.target(entry.name()) || !this.passesEnchants(entry, slot.method_7677()) || !this.canAccept(slot.method_7677())) continue;
            return i;
        }
        return -1;
    }

    private Entry matchingSelectedEntry(class_1799 stack) {
        for (Entry entry : this.catalog) {
            if (!this.selected(entry.name()) || !entry.matcher().test(stack)) continue;
            return entry;
        }
        return null;
    }

    private boolean passesEnchants(Entry entry, class_1799 stack) {
        if (entry.filter() == Filter.ARMOR) {
            if (this.rejectThorns.isValue() && this.enchantLevel(stack, (class_5321<class_1887>)class_1893.field_9097) > 0) {
                return false;
            }
            if (this.repairedArmor.isValue() && stack.method_7963() && stack.method_7919() > 0) {
                return false;
            }
            for (String required : this.armorEnchants.getSelected()) {
                if (required.equals("\u0417\u0430\u0449\u0438\u0442\u0430 5") && this.enchantLevel(stack, (class_5321<class_1887>)class_1893.field_9111) < 5) {
                    return false;
                }
                if (required.equals("\u041f\u0440\u043e\u0447\u043d\u043e\u0441\u0442\u044c 5") && this.enchantLevel(stack, (class_5321<class_1887>)class_1893.field_9119) < 5) {
                    return false;
                }
                if (!required.equals("\u041f\u043e\u0447\u0438\u043d\u043a\u0430") || this.enchantLevel(stack, (class_5321<class_1887>)class_1893.field_9101) >= 1) continue;
                return false;
            }
        }
        if (entry.filter() == Filter.SWORD) {
            for (String required : this.swordEnchants.getSelected()) {
                if (required.equals("\u0417\u0430\u0433\u043e\u0432\u043e\u0440 \u043e\u0433\u043d\u044f 2") && this.enchantLevel(stack, (class_5321<class_1887>)class_1893.field_9124) < 2) {
                    return false;
                }
                if (required.equals("\u041e\u0441\u0442\u0440\u043e\u0442\u0430 7") && this.enchantLevel(stack, (class_5321<class_1887>)class_1893.field_9118) < 7) {
                    return false;
                }
                if (!required.equals("\u042f\u0434") && !required.equals("\u041e\u043a\u0438\u0441\u043b\u0435\u043d\u0438\u0435") || this.tooltipContains(stack, required)) continue;
                return false;
            }
        }
        return true;
    }

    private int enchantLevel(class_1799 stack, class_5321<class_1887> key) {
        if (AutoInventory.mc.field_1687 == null) {
            return 0;
        }
        class_6880 entry = AutoInventory.mc.field_1687.method_30349().method_30530(class_7924.field_41265).method_10223(key.method_29177()).orElse(null);
        return entry == null ? 0 : class_1890.method_8225((class_6880)entry, (class_1799)stack);
    }

    private boolean tooltipContains(class_1799 stack, String needle) {
        String value = needle.toLowerCase(Locale.ROOT);
        try {
            return stack.method_7950(class_1792.class_9635.method_59528((class_1937)AutoInventory.mc.field_1687), (class_1657)AutoInventory.mc.field_1724, (class_1836)class_1836.field_41070).stream().map(class_2561::getString).map(s -> s.toLowerCase(Locale.ROOT)).anyMatch(s -> s.contains(value));
        }
        catch (Exception ignored) {
            return stack.method_7964().getString().toLowerCase(Locale.ROOT).contains(value);
        }
    }

    private boolean requirementsMet() {
        boolean any = false;
        for (Entry entry : this.catalog) {
            if (!this.selected(entry.name())) continue;
            any = true;
            if (this.countMatching(entry) >= this.target(entry.name())) continue;
            return false;
        }
        return any;
    }

    private int countMatching(Entry entry) {
        int count = 0;
        for (int i = 0; i < 36; ++i) {
            class_1799 stack = AutoInventory.mc.field_1724.method_31548().method_5438(i);
            if (!entry.matcher().test(stack) || !this.passesEnchants(entry, stack)) continue;
            count += stack.method_7947();
        }
        return count;
    }

    private boolean canAccept(class_1799 incoming) {
        for (int i = 0; i < 36; ++i) {
            class_1799 currentStack = AutoInventory.mc.field_1724.method_31548().method_5438(i);
            if (currentStack.method_7960()) {
                return true;
            }
            if (!class_1799.method_31577((class_1799)currentStack, (class_1799)incoming) || currentStack.method_7947() >= currentStack.method_7914()) continue;
            return true;
        }
        return false;
    }

    private void finish(String reason) {
        this.message(reason + ". \u041f\u0440\u043e\u0432\u0435\u0440\u0435\u043d\u043e: " + this.scanned + ", \u0432\u0437\u044f\u0442\u043e: " + this.taken);
        this.setState(false);
    }

    private void message(String value) {
        if (AutoInventory.mc.field_1724 != null) {
            AutoInventory.mc.field_1724.method_7353(QuickLogger.clientMessage((class_2561)class_2561.method_43470((String)("\u00a75[AutoInventory] \u00a7f" + value))), false);
        }
    }

    public List<Entry> catalog() {
        return Collections.unmodifiableList(this.catalog);
    }

    public boolean selected(String name) {
        return this.selectedItems.isSelected(name);
    }

    public int target(String name) {
        return this.targets.getOrDefault(name, 1);
    }

    public boolean configurable(Entry entry) {
        return entry.filter() != Filter.NONE;
    }

    public void refreshCatalogIcons() {
        if (AutoInventory.mc.field_1724 == null) {
            return;
        }
        for (int i = 0; i < AutoInventory.mc.field_1724.method_31548().method_5439(); ++i) {
            this.rememberCatalogIcon(AutoInventory.mc.field_1724.method_31548().method_5438(i));
        }
        this.rememberCatalogIcon(AutoInventory.mc.field_1724.method_6047());
        this.rememberCatalogIcon(AutoInventory.mc.field_1724.method_6079());
    }

    public class_1799 iconStack(Entry entry) {
        return this.catalogIcons.getOrDefault(entry.name(), entry.stack());
    }

    public boolean matches(Entry entry, class_1799 stack) {
        return stack != null && !stack.method_7960() && entry.matcher().test(stack) && this.passesEnchants(entry, stack);
    }

    public int countInInventory(Entry entry) {
        if (AutoInventory.mc.field_1724 == null) {
            return 0;
        }
        int count = 0;
        for (int slot = 0; slot < 36; ++slot) {
            class_1799 stack = AutoInventory.mc.field_1724.method_31548().method_5438(slot);
            if (!this.matches(entry, stack)) continue;
            count += stack.method_7947();
        }
        return count;
    }

    private void rememberCatalogIcon(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return;
        }
        for (Entry entry : this.catalog) {
            if (!entry.matcher().test(stack)) continue;
            class_1799 icon = stack.method_7972();
            icon.method_7939(1);
            this.catalogIcons.put(entry.name(), icon);
            return;
        }
    }

    public void toggle(String name) {
        if (this.selected(name)) {
            this.selectedItems.getSelected().remove(name);
        } else {
            this.selectedItems.getSelected().add(name);
        }
    }

    public void changeTarget(Entry entry, int amount) {
        int max = entry.stack().method_7914() > 1 ? entry.stack().method_7914() * 36 : 4;
        this.targets.put(entry.name(), Math.max(1, Math.min(max, this.target(entry.name()) + amount)));
    }

    public void toggleArmorEnchant(String enchant) {
        this.toggleSelection(this.armorEnchants, enchant);
    }

    public void toggleSwordEnchant(String enchant) {
        this.toggleSelection(this.swordEnchants, enchant);
    }

    private void toggleSelection(MultiSelectSetting setting, String value) {
        if (setting.isSelected(value)) {
            setting.getSelected().remove(value);
        } else {
            setting.getSelected().add(value);
        }
    }

    private static List<Entry> createCatalog() {
        ArrayList<Entry> list = new ArrayList<Entry>();
        AutoInventory.add(list, "\u0417\u0430\u0447\u0430\u0440\u043e\u0432\u0430\u043d\u043d\u043e\u0435 \u0437\u043e\u043b\u043e\u0442\u043e\u0435 \u044f\u0431\u043b\u043e\u043a\u043e", class_1802.field_8367);
        AutoInventory.add(list, "\u0417\u043e\u043b\u043e\u0442\u043e\u0435 \u044f\u0431\u043b\u043e\u043a\u043e", class_1802.field_8463);
        AutoInventory.namedDrinkablePotion(list, "\u0417\u0435\u043b\u044c\u0435 \u0438\u0441\u0446\u0435\u043b\u0435\u043d\u0438\u044f", 15743032, "\u0438\u0441\u0446\u0435\u043b");
        AutoInventory.namedDrinkablePotion(list, "\u0417\u0435\u043b\u044c\u0435 \u0441\u043a\u043e\u0440\u043e\u0441\u0442\u044c + \u0441\u0438\u043b\u0430", 4442458, "\u0441\u043a\u043e\u0440\u043e\u0441\u0442", "\u0441\u0438\u043b");
        AutoInventory.named(list, "\u0414\u0435\u0437\u043e\u0440\u0438\u0435\u043d\u0442\u0430\u0446\u0438\u044f", class_1802.field_8449, "\u0434\u0435\u0437\u043e\u0440\u0438\u0435\u043d\u0442");
        AutoInventory.named(list, "\u042f\u0432\u043d\u0430\u044f \u043f\u044b\u043b\u044c", class_1802.field_8479, "\u044f\u0432\u043d", "\u043f\u044b\u043b");
        AutoInventory.add(list, "\u042d\u043d\u0434\u0435\u0440-\u0436\u0435\u043c\u0447\u0443\u0433", class_1802.field_8634);
        AutoInventory.named(list, "\u0422\u0440\u0430\u043f\u043a\u0430", class_1802.field_22021, "\u0442\u0440\u0430\u043f");
        AutoInventory.named(list, "\u0411\u043e\u0436\u044c\u044f \u0430\u0443\u0440\u0430", class_1802.field_8614, "\u0431\u043e\u0436", "\u0430\u0443\u0440");
        AutoInventory.named(list, "\u0421\u043d\u0435\u0436\u043e\u043a \u0437\u0430\u043c\u043e\u0440\u043e\u0437\u043a\u0438", class_1802.field_8543, "\u0437\u0430\u043c\u043e\u0440\u043e\u0437");
        AutoInventory.named(list, "\u041f\u043b\u0430\u0441\u0442", class_1802.field_8551, "\u043f\u043b\u0430\u0441\u0442");
        AutoInventory.add(list, "\u0422\u043e\u0442\u0435\u043c \u0431\u0435\u0441\u0441\u043c\u0435\u0440\u0442\u0438\u044f", class_1802.field_8288);
        AutoInventory.add(list, "\u041c\u0435\u0448\u043e\u043a", class_1802.field_27023);
        AutoInventory.named(list, "\u0421\u0442\u0440\u0435\u043b\u0430 \u043e\u0431\u043b\u0435\u0434\u0435\u043d\u0435\u043d\u0438\u044f", class_1802.field_8087, "\u043e\u0431\u043b\u0435\u0434\u0435\u043d");
        AutoInventory.add(list, "\u041d\u0435\u0437\u0435\u0440\u0438\u0442\u043e\u0432\u0430\u044f \u043a\u0438\u0440\u043a\u0430", class_1802.field_22024);
        AutoInventory.add(list, "\u0410\u043b\u043c\u0430\u0437\u043d\u0430\u044f \u043a\u0438\u0440\u043a\u0430", class_1802.field_8377);
        AutoInventory.add(list, "\u042d\u043b\u0438\u0442\u0440\u044b", class_1802.field_8833);
        AutoInventory.add(list, "\u0424\u0435\u0439\u0435\u0440\u0432\u0435\u0440\u043a\u0438", class_1802.field_8639);
        AutoInventory.donation(list, "\u0421\u0432\u044f\u0442\u0430\u044f \u0432\u043e\u0434\u0430", class_1802.field_8436);
        AutoInventory.donation(list, "\u0417\u0435\u043b\u044c\u0435 \u0410\u0441\u0441\u0430\u0441\u0438\u043d\u0430", class_1802.field_8436);
        AutoInventory.donation(list, "\u0417\u0435\u043b\u044c\u0435 \u0420\u0430\u0434\u0438\u0430\u0446\u0438\u0438", class_1802.field_8436);
        AutoInventory.donation(list, "\u0421\u043d\u043e\u0442\u0432\u043e\u0440\u043d\u043e\u0435", class_1802.field_8436);
        AutoInventory.donation(list, "\u0417\u0435\u043b\u044c\u0435 \u041f\u0430\u043b\u0430\u0434\u0438\u043d\u0430", class_1802.field_8436);
        AutoInventory.donation(list, "\u0425\u043b\u043e\u043f\u0443\u0448\u043a\u0430", class_1802.field_8436);
        AutoInventory.donation(list, "\u0417\u0435\u043b\u044c\u0435 \u0413\u043d\u0435\u0432\u0430", class_1802.field_8436);
        AutoInventory.namedDrinkablePotion(list, "\u0417\u0435\u043b\u044c\u0435 \u043d\u0435\u0432\u0438\u0434\u0438\u043c\u043e\u0441\u0442\u0438", 8356754, "\u043d\u0435\u0432\u0438\u0434\u0438\u043c");
        AutoInventory.filtered(list, "\u041d\u0435\u0437\u0435\u0440\u0438\u0442\u043e\u0432\u044b\u0439 \u0448\u043b\u0435\u043c", class_1802.field_22027, Filter.ARMOR);
        AutoInventory.filtered(list, "\u041d\u0435\u0437\u0435\u0440\u0438\u0442\u043e\u0432\u044b\u0439 \u043d\u0430\u0433\u0440\u0443\u0434\u043d\u0438\u043a", class_1802.field_22028, Filter.ARMOR);
        AutoInventory.filtered(list, "\u041d\u0435\u0437\u0435\u0440\u0438\u0442\u043e\u0432\u044b\u0435 \u043f\u043e\u043d\u043e\u0436\u0438", class_1802.field_22029, Filter.ARMOR);
        AutoInventory.filtered(list, "\u041d\u0435\u0437\u0435\u0440\u0438\u0442\u043e\u0432\u044b\u0435 \u0431\u043e\u0442\u0438\u043d\u043a\u0438", class_1802.field_22030, Filter.ARMOR);
        AutoInventory.filtered(list, "\u041d\u0435\u0437\u0435\u0440\u0438\u0442\u043e\u0432\u044b\u0439 \u043c\u0435\u0447", class_1802.field_22022, Filter.SWORD);
        AutoInventory.named(list, "\u0410\u0440\u0431\u0430\u043b\u0435\u0442 \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f", class_1802.field_8399, "\u043a\u0440\u0443\u0448\u0438\u0442\u0435\u043b");
        AutoInventory.named(list, "\u041c\u0435\u0447 \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f", class_1802.field_22022, "\u043a\u0440\u0443\u0448\u0438\u0442\u0435\u043b");
        AutoInventory.named(list, "\u0428\u043b\u0435\u043c \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f", class_1802.field_22027, "\u043a\u0440\u0443\u0448\u0438\u0442\u0435\u043b");
        AutoInventory.named(list, "\u041d\u0430\u0433\u0440\u0443\u0434\u043d\u0438\u043a \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f", class_1802.field_22028, "\u043a\u0440\u0443\u0448\u0438\u0442\u0435\u043b");
        AutoInventory.named(list, "\u041f\u043e\u043d\u043e\u0436\u0438 \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f", class_1802.field_22029, "\u043a\u0440\u0443\u0448\u0438\u0442\u0435\u043b");
        AutoInventory.named(list, "\u0411\u043e\u0442\u0438\u043d\u043a\u0438 \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f", class_1802.field_22030, "\u043a\u0440\u0443\u0448\u0438\u0442\u0435\u043b");
        AutoInventory.add(list, "\u0417\u043e\u043b\u043e\u0442\u0430\u044f \u043c\u043e\u0440\u043a\u043e\u0432\u044c", class_1802.field_8071);
        AutoInventory.add(list, "\u0428\u0430\u043b\u043a\u0435\u0440\u043e\u0432\u044b\u0439 \u044f\u0449\u0438\u043a", class_1802.field_8545);
        for (String name : List.of("\u0422\u0430\u043b\u0438\u0441\u043c\u0430\u043d \u0414\u0435\u043c\u043e\u043d\u0430", "\u0422\u0430\u043b\u0438\u0441\u043c\u0430\u043d \u0420\u0430\u0437\u0434\u043e\u0440\u0430", "\u0422\u0430\u043b\u0438\u0441\u043c\u0430\u043d \u042f\u0440\u043e\u0441\u0442\u0438", "\u0422\u0430\u043b\u0438\u0441\u043c\u0430\u043d \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f", "\u0422\u0430\u043b\u0438\u0441\u043c\u0430\u043d \u041a\u0430\u0440\u0430\u0442\u0435\u043b\u044f", "\u0422\u0430\u043b\u0438\u0441\u043c\u0430\u043d \u0412\u0438\u0445\u0440\u044f")) {
            AutoInventory.named(list, name, class_1802.field_8288, name.toLowerCase(Locale.ROOT).replace("\u0442\u0430\u043b\u0438\u0441\u043c\u0430\u043d ", ""));
        }
        for (String name : List.of("\u0421\u0444\u0435\u0440\u0430 \u0413\u0438\u0434\u0440\u044b", "\u0421\u0444\u0435\u0440\u0430 \u0410\u0440\u0435\u0441\u0430", "\u0421\u0444\u0435\u0440\u0430 \u0411\u0435\u0441\u0442\u0438\u0438", "\u0421\u0444\u0435\u0440\u0430 \u0418\u043a\u0430\u0440\u0430", "\u0421\u0444\u0435\u0440\u0430 \u0421\u0430\u0442\u0438\u0440\u0430", "\u0421\u0444\u0435\u0440\u0430 \u042d\u0440\u0438\u0434\u0430")) {
            AutoInventory.namedTarget(list, name, class_1802.field_8575, 1, name.toLowerCase(Locale.ROOT).replace("\u0441\u0444\u0435\u0440\u0430 ", ""));
        }
        AutoInventory.add(list, "\u0425\u043e\u0440\u0443\u0441", class_1802.field_8233);
        AutoInventory.add(list, "\u0417\u0430\u0440\u044f\u0434 \u0432\u0435\u0442\u0440\u0430", class_1802.field_49098);
        return list;
    }

    private static void add(List<Entry> list, String name, class_1792 item) {
        list.add(new Entry(name, item.method_7854(), stack -> stack.method_31574(item), Filter.NONE, item.method_7854().method_7914() > 1 ? item.method_7854().method_7914() : 1));
    }

    private static void filtered(List<Entry> list, String name, class_1792 item, Filter filter) {
        list.add(new Entry(name, item.method_7854(), stack -> stack.method_31574(item) && !stack.method_7964().getString().toLowerCase(Locale.ROOT).contains("\u043a\u0440\u0443\u0448\u0438\u0442\u0435\u043b"), filter, 1));
    }

    private static void donation(List<Entry> list, String name, class_1792 item) {
        AutoInventory.named(list, name, item, name.toLowerCase(Locale.ROOT).replace("\u0437\u0435\u043b\u044c\u0435 ", ""));
    }

    private static void namedDrinkablePotion(List<Entry> list, String name, int color, String ... fragments) {
        class_1799 icon = class_1802.field_8574.method_7854();
        icon.method_57379(class_9334.field_49631, class_2561.method_43470((String)name));
        icon.method_57379(class_9334.field_49651, new class_1844(Optional.empty(), Optional.of(color), List.of(), Optional.empty()));
        list.add(new Entry(name, icon, stack -> {
            if (!stack.method_31574(class_1802.field_8574)) {
                return false;
            }
            String display = AutoInventory.searchableText(stack);
            for (String fragment : fragments) {
                if (display.contains(fragment)) continue;
                return false;
            }
            return true;
        }, Filter.NONE, 1));
    }

    private static void named(List<Entry> list, String name, class_1792 item, String ... fragments) {
        int defaultTarget = item.method_7854().method_7914() > 1 ? item.method_7854().method_7914() : 1;
        AutoInventory.namedTarget(list, name, item, defaultTarget, fragments);
    }

    private static void namedTarget(List<Entry> list, String name, class_1792 item, int defaultTarget, String ... fragments) {
        list.add(new Entry(name, item.method_7854(), stack -> {
            if (!stack.method_31574(item)) {
                return false;
            }
            String display = AutoInventory.searchableText(stack);
            for (String fragment : fragments) {
                if (display.contains(fragment)) continue;
                return false;
            }
            return true;
        }, Filter.NONE, defaultTarget));
    }

    private static String searchableText(class_1799 stack) {
        StringBuilder text = new StringBuilder(stack.method_7964().getString().toLowerCase(Locale.ROOT));
        try {
            if (AutoInventory.mc.field_1687 != null) {
                for (class_2561 line : stack.method_7950(class_1792.class_9635.method_59528((class_1937)AutoInventory.mc.field_1687), (class_1657)AutoInventory.mc.field_1724, (class_1836)class_1836.field_41070)) {
                    text.append(' ').append(line.getString().toLowerCase(Locale.ROOT));
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return text.toString();
    }

    public record Entry(String name, class_1799 stack, Predicate<class_1799> matcher, Filter filter, int defaultTarget) {
    }

    private record PathNode(class_2338 pos, double score) {
    }

    public static enum Filter {
        NONE,
        ARMOR,
        SWORD;

    }
}

