/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 *  net.minecraft.class_1297
 *  net.minecraft.class_1694
 *  net.minecraft.class_2246
 *  net.minecraft.class_2281
 *  net.minecraft.class_2338
 *  net.minecraft.class_2338$class_2339
 *  net.minecraft.class_238
 *  net.minecraft.class_243
 *  net.minecraft.class_2561
 *  net.minecraft.class_3532
 */
package ru.recoin.implement.features.modules.misc;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.class_124;
import net.minecraft.class_1297;
import net.minecraft.class_1694;
import net.minecraft.class_2246;
import net.minecraft.class_2281;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_3532;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BindSetting;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.common.util.render.Render3DUtil;
import ru.recoin.common.util.world.FlightTeleportController;
import ru.recoin.implement.events.keyboard.KeyEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.events.render.WorldRenderEvent;

public class KeyFinder
extends Module {
    private final BindSetting searchBind = new BindSetting("\u041a\u043d\u043e\u043f\u043a\u0430 \u043f\u043e\u0438\u0441\u043a\u0430", "");
    private final BindSetting clearBind = new BindSetting("\u041e\u0447\u0438\u0441\u0442\u0438\u0442\u044c \u043d\u0430\u0439\u0434\u0435\u043d\u043d\u043e\u0435", "");
    private final SelectSetting searchType = new SelectSetting("\u0422\u0438\u043f \u043f\u043e\u0438\u0441\u043a\u0430", "").value("\u0412\u0441\u0451", "\u0412\u0430\u0433\u043e\u043d\u0435\u0442\u043a\u0430 \u0441 \u0441\u0443\u043d\u0434\u0443\u043a\u043e\u043c", "\u0421\u0443\u043d\u0434\u0443\u043a \u0440\u044f\u0434\u043e\u043c \u0441\u043e \u0441\u043f\u0430\u0432\u043d\u0435\u0440\u043e\u043c").selected("\u0412\u0441\u0451");
    private final BooleanSetting displayFound = new BooleanSetting("\u041e\u0442\u043e\u0431\u0440\u0430\u0436\u0430\u0442\u044c \u043d\u0430\u0439\u0434\u0435\u043d\u043d\u043e\u0435", "").setValue(true);
    private final transient Set<Long> visitedChunks = new HashSet<Long>();
    private final transient Set<Long> foundSpawners;
    private final transient Set<class_2338> foundChests;
    private final transient Set<class_2338> foundMinecarts;
    private transient boolean searching;
    private transient boolean waitingTeleport;
    private transient int scanX;
    private transient int scanZ;
    private transient int originX;
    private transient int originZ;
    private transient int spiralX;
    private transient int spiralZ;
    private transient int directionX;
    private transient int directionZ;
    private transient int segmentLength;
    private transient int segmentSteps;
    private transient int spiralTurns;
    private transient class_2338 pendingTeleport;

    private void finishSearch() {
        this.message("\u041f\u043e\u0438\u0441\u043a \u0437\u0430\u0432\u0435\u0440\u0448\u0451\u043d. \u041d\u0430\u0439\u0434\u0435\u043d\u043e: " + (this.foundChests.size() + this.foundMinecarts.size()), class_124.field_1054);
        this.stopSearch();
    }

    @EventHandler
    private void onKey(KeyEvent event) {
        if (event.action() == 1 && event.screen() == null) {
            if (this.clearBind.getKey() != -1 && event.key() == this.clearBind.getKey()) {
                this.clearFound();
                this.message("\u041d\u0430\u0439\u0434\u0435\u043d\u043d\u044b\u0435 \u043e\u0431\u044a\u0435\u043a\u0442\u044b \u043e\u0447\u0438\u0449\u0435\u043d\u044b", class_124.field_1054);
            } else if (this.searchBind.getKey() != -1 && event.key() == this.searchBind.getKey()) {
                if (!this.searching && !FlightTeleportController.isActive()) {
                    this.startSearch();
                } else {
                    this.stopSearch();
                    this.message("\u041f\u043e\u0438\u0441\u043a \u043e\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d", class_124.field_1061);
                }
            }
        }
    }

    private boolean advanceSpiral() {
        int count3;
        ++this.segmentSteps;
        this.spiralX += this.directionX;
        this.spiralZ += this.directionZ;
        if (this.segmentSteps == this.segmentLength) {
            this.segmentSteps = 0;
            int count = -this.directionZ;
            int count2 = this.directionX;
            this.directionX = count;
            this.directionZ = count2;
            if (++this.spiralTurns == 2) {
                this.spiralTurns = 0;
                ++this.segmentLength;
            }
        }
        return (count3 = Math.max(Math.abs(this.spiralX), Math.abs(this.spiralZ))) * 16 <= 32000;
    }

    private int alignToChunk(int count) {
        int count2 = Math.floorDiv(count, 16) * 16;
        return class_3532.method_15340((int)count2, (int)-16000, (int)16000);
    }

    private void scanChunk(int count, int count2) {
        if (this.visitedChunks.add(this.chunkKey(count >> 4, count2 >> 4))) {
            String text = this.searchType.getSelected();
            int count3 = KeyFinder.mc.field_1687.method_31607();
            int count4 = count3 + KeyFinder.mc.field_1687.method_31605() - 1;
            int count5 = Math.min(60, count4);
            if ("\u0412\u0441\u0451".equals(text) || "\u0412\u0430\u0433\u043e\u043d\u0435\u0442\u043a\u0430 \u0441 \u0441\u0443\u043d\u0434\u0443\u043a\u043e\u043c".equals(text)) {
                class_238 box = new class_238((double)(count - 8), (double)count3, (double)(count2 - 8), (double)(count + 8), (double)(count5 + 1), (double)(count2 + 8));
                for (class_1694 value : KeyFinder.mc.field_1687.method_8390(class_1694.class, box, class_1297::method_5805)) {
                    class_2338 position = value.method_24515();
                    if (!this.foundMinecarts.add(position)) continue;
                    this.recordFound(position, "\u0412\u0430\u0433\u043e\u043d\u0435\u0442\u043a\u0430 \u0441 \u0441\u0443\u043d\u0434\u0443\u043a\u043e\u043c");
                    return;
                }
            }
            if ("\u0412\u0441\u0451".equals(text) || "\u0421\u0443\u043d\u0434\u0443\u043a \u0440\u044f\u0434\u043e\u043c \u0441\u043e \u0441\u043f\u0430\u0432\u043d\u0435\u0440\u043e\u043c".equals(text)) {
                this.scanSpawnerChests(count, count2, count3, count5);
            }
        }
    }

    private void scanSpawnerChests(int count, int count2, int count3, int count4) {
        class_2338.class_2339 cursor = new class_2338.class_2339();
        for (int index = 0; index < 16; ++index) {
            for (int index2 = 0; index2 < 16; ++index2) {
                for (int index3 = count3; index3 <= count4; ++index3) {
                    class_2338 position;
                    class_2338 position2;
                    cursor.method_10103(count + index, index3, count2 + index2);
                    if (!(KeyFinder.mc.field_1687.method_8320((class_2338)cursor).method_26204() instanceof class_2281) || (position2 = this.findNearbySpawner(position = cursor.method_10062())) == null) continue;
                    if (this.foundSpawners.add(position2.method_10063())) {
                        this.foundChests.add(position);
                        this.recordFound(position, "\u0421\u0443\u043d\u0434\u0443\u043a \u0443 \u0441\u043f\u0430\u0432\u043d\u0435\u0440\u0430");
                        return;
                    }
                    this.foundChests.add(position);
                }
            }
        }
    }

    private boolean findNextChunk() {
        while (this.advanceSpiral()) {
            int count = this.originX + this.spiralX * 16;
            int count2 = this.originZ + this.spiralZ * 16;
            if (count < -16000 || count > 16000 || count2 < -16000 || count2 > 16000 || this.visitedChunks.contains(this.chunkKey(count >> 4, count2 >> 4))) continue;
            this.scanX = count;
            this.scanZ = count2;
            return true;
        }
        return false;
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (KeyFinder.mc.field_1724 != null && KeyFinder.mc.field_1687 != null) {
            FlightTeleportController.tick();
            if (this.pendingTeleport != null) {
                KeyFinder.mc.field_1724.method_5814((double)this.pendingTeleport.method_10263() + 0.5, (double)this.pendingTeleport.method_10264(), (double)this.pendingTeleport.method_10260() + 0.5);
                this.pendingTeleport = null;
                this.searching = false;
                this.waitingTeleport = false;
                this.message("\u0422\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0430\u0446\u0438\u044f \u043a \u043d\u0430\u0439\u0434\u0435\u043d\u043d\u043e\u043c\u0443 \u0437\u0430\u0432\u0435\u0440\u0448\u0435\u043d\u0430", class_124.field_1060);
            } else if (this.searching) {
                if (!KeyFinder.mc.field_1724.method_31549().field_7479) {
                    this.message("\u041f\u043e\u0438\u0441\u043a \u043e\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d: \u0442\u0440\u0435\u0431\u0443\u0435\u0442\u0441\u044f \u0440\u0435\u0436\u0438\u043c \u043f\u043e\u043b\u0451\u0442\u0430", class_124.field_1061);
                    this.stopSearch();
                } else if (!FlightTeleportController.isActive()) {
                    if (this.waitingTeleport) {
                        this.waitingTeleport = false;
                        this.scanChunk(this.scanX, this.scanZ);
                        if (!this.searching) {
                            return;
                        }
                    }
                    if (!this.findNextChunk()) {
                        this.finishSearch();
                    } else {
                        FlightTeleportController.teleportTo(this.scanX, this.scanZ, true);
                        this.waitingTeleport = FlightTeleportController.isActive();
                        if (!this.waitingTeleport) {
                            this.stopSearch();
                        }
                    }
                }
            }
        } else {
            this.stopSearch();
        }
    }

    private void startSearch() {
        if (KeyFinder.mc.field_1724 != null && KeyFinder.mc.field_1687 != null) {
            if (!KeyFinder.mc.field_1724.method_31549().field_7479) {
                this.message("\u0414\u043b\u044f \u043f\u043e\u0438\u0441\u043a\u0430 \u0442\u0440\u0435\u0431\u0443\u0435\u0442\u0441\u044f \u0440\u0435\u0436\u0438\u043c \u043f\u043e\u043b\u0451\u0442\u0430", class_124.field_1061);
            } else {
                this.originX = this.alignToChunk(class_3532.method_15357((double)KeyFinder.mc.field_1724.method_23317()));
                this.originZ = this.alignToChunk(class_3532.method_15357((double)KeyFinder.mc.field_1724.method_23321()));
                this.scanX = this.originX;
                this.scanZ = this.originZ;
                this.spiralX = 0;
                this.spiralZ = 0;
                this.directionX = 1;
                this.directionZ = 0;
                this.segmentLength = 1;
                this.segmentSteps = 0;
                this.spiralTurns = 0;
                this.waitingTeleport = false;
                this.searching = true;
                this.pendingTeleport = null;
                this.message("\u041d\u0430\u0447\u0438\u043d\u0430\u0435\u043c \u043f\u043e\u0438\u0441\u043a...", class_124.field_1060);
                this.scanChunk(this.originX, this.originZ);
            }
        }
    }

    private long chunkKey(int count, int count2) {
        return (long)count << 32 ^ (long)count2 & 0xFFFFFFFFL;
    }

    private void clearFound() {
        this.visitedChunks.clear();
        this.foundSpawners.clear();
        this.foundChests.clear();
        this.foundMinecarts.clear();
        this.stopSearch();
    }

    private void stopSearch() {
        this.searching = false;
        this.waitingTeleport = false;
        this.pendingTeleport = null;
    }

    @EventHandler
    private void onWorldRender(WorldRenderEvent event) {
        if (!(!this.displayFound.isValue() || KeyFinder.mc.field_1687 == null || this.foundChests.isEmpty() && this.foundMinecarts.isEmpty())) {
            class_243 vector = KeyFinder.mc.field_1773.method_19418().method_71156();
            int count = -1275002881;
            event.getStack().method_22903();
            event.getStack().method_22904(-vector.field_1352, -vector.field_1351, -vector.field_1350);
            for (class_2338 position : this.foundChests) {
                Render3DUtil.drawBox(new class_238(position), count, 1.0f, true, true, false);
            }
            for (class_2338 position2 : this.foundMinecarts) {
                Render3DUtil.drawBox(new class_238(position2), count, 1.0f, true, true, false);
            }
            event.getStack().method_22909();
        }
    }

    @Override
    public void deactivate() {
        this.stopSearch();
        FlightTeleportController.reset();
        super.deactivate();
    }

    private void recordFound(class_2338 position, String text) {
        this.message("\u041d\u0430\u0439\u0434\u0435\u043d(\u0430) " + text + " \u043d\u0430 " + position.method_10263() + " " + position.method_10264() + " " + position.method_10260(), class_124.field_1060);
        this.pendingTeleport = "\u0412\u0430\u0433\u043e\u043d\u0435\u0442\u043a\u0430 \u0441 \u0441\u0443\u043d\u0434\u0443\u043a\u043e\u043c".equals(text) ? position.method_10074() : position;
        this.visitedChunks.add(this.chunkKey(position.method_10263() >> 4, position.method_10260() >> 4));
        this.waitingTeleport = false;
        this.searching = false;
    }

    private class_2338 findNearbySpawner(class_2338 position) {
        for (int index = -5; index <= 5; ++index) {
            for (int index2 = -5; index2 <= 5; ++index2) {
                class_2338 position2 = position.method_10069(index, 0, index2);
                if (!KeyFinder.mc.field_1687.method_8320(position2).method_27852(class_2246.field_10260)) continue;
                return position2;
            }
        }
        return null;
    }

    private void message(String text, class_124 value) {
        if (KeyFinder.mc.field_1724 != null) {
            KeyFinder.mc.field_1724.method_7353((class_2561)class_2561.method_43470((String)text).method_27692(value), false);
        }
    }

    public KeyFinder() {
        super("KeyFinder", "KeyFinder", ModuleCategory.PVE);
        this.setup(this.searchBind, this.clearBind, this.searchType, this.displayFound);
        this.foundSpawners = new HashSet<Long>();
        this.foundChests = new HashSet<class_2338>();
        this.foundMinecarts = new HashSet<class_2338>();
        this.directionX = 1;
        this.segmentLength = 1;
    }
}

