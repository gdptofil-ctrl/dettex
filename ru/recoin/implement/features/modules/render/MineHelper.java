/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2246
 *  net.minecraft.class_2248
 *  net.minecraft.class_2338
 *  net.minecraft.class_2338$class_2339
 *  net.minecraft.class_238
 *  net.minecraft.class_2680
 */
package ru.recoin.implement.features.modules.render;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_2680;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.MultiSelectSetting;
import ru.recoin.common.util.render.Render3DUtil;
import ru.recoin.common.util.world.ServerUtil;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.events.render.WorldRenderEvent;

public final class MineHelper
extends Module {
    private static final int BLOCKS_PER_TICK = 4096;
    private static final class_2338 MINE_MIN = new class_2338(-68, 69, 24);
    private static final class_2338 MINE_MAX = new class_2338(-48, 78, 44);
    private final MultiSelectSetting ores = new MultiSelectSetting("Ores", "Ores shown by the scanner").value("Diamond", "Gold", "Iron", "Redstone", "Lapis", "Coal", "Ancient Debris").selected("Diamond", "Gold", "Iron", "Ancient Debris");
    private final Map<class_2338, Integer> found = new HashMap<class_2338, Integer>();
    private int cursor;

    public MineHelper() {
        super("MineHelper", "Mine Helper", ModuleCategory.MISC);
        this.setup(this.ores);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (MineHelper.mc.field_1724 == null || MineHelper.mc.field_1687 == null) {
            return;
        }
        if (!this.isMineWorld()) {
            this.cursor = 0;
            this.found.clear();
            return;
        }
        int sizeX = MINE_MAX.method_10263() - MINE_MIN.method_10263() + 1;
        int sizeY = MINE_MAX.method_10264() - MINE_MIN.method_10264() + 1;
        int sizeZ = MINE_MAX.method_10260() - MINE_MIN.method_10260() + 1;
        int total = sizeX * sizeY * sizeZ;
        class_2338.class_2339 mutable = new class_2338.class_2339();
        int n = 0;
        while (n < 4096 && this.cursor < total) {
            int color;
            int x = this.cursor % sizeX;
            int zy = this.cursor / sizeX;
            int z = zy % sizeZ;
            int y = zy / sizeZ;
            mutable.method_10103(MINE_MIN.method_10263() + x, MINE_MIN.method_10264() + y, MINE_MIN.method_10260() + z);
            if (MineHelper.mc.field_1687.method_22340((class_2338)mutable) && (color = this.color(MineHelper.mc.field_1687.method_8320((class_2338)mutable))) != 0) {
                this.found.put(mutable.method_10062(), color);
            }
            ++n;
            ++this.cursor;
        }
        if (this.cursor >= total) {
            this.cursor = 0;
            Iterator<class_2338> iterator2 = this.found.keySet().iterator();
            while (iterator2.hasNext()) {
                class_2338 pos = iterator2.next();
                if (this.color(MineHelper.mc.field_1687.method_8320(pos)) != 0) continue;
                iterator2.remove();
            }
        }
    }

    @EventHandler
    public void onRender(WorldRenderEvent event) {
        this.found.forEach((pos, color) -> Render3DUtil.drawBox(new class_238(pos), color, 1.5f, true, true, false));
    }

    private int color(class_2680 state) {
        class_2248 b = state.method_26204();
        if (this.ores.isSelected("Diamond") && (b == class_2246.field_10442 || b == class_2246.field_29029)) {
            return -2008620058;
        }
        if (this.ores.isSelected("Gold") && (b == class_2246.field_10571 || b == class_2246.field_29026)) {
            return -1996500146;
        }
        if (this.ores.isSelected("Iron") && (b == class_2246.field_10212 || b == class_2246.field_29027)) {
            return -1999063654;
        }
        if (this.ores.isSelected("Redstone") && (b == class_2246.field_10080 || b == class_2246.field_29030)) {
            return -1996541133;
        }
        if (this.ores.isSelected("Lapis") && (b == class_2246.field_10090 || b == class_2246.field_29028)) {
            return -2009439256;
        }
        if (this.ores.isSelected("Coal") && (b == class_2246.field_10418 || b == class_2246.field_29219)) {
            return -2005436553;
        }
        if (this.ores.isSelected("Ancient Debris") && b == class_2246.field_22109) {
            return -2002359470;
        }
        return 0;
    }

    private boolean isMineWorld() {
        String server = ServerUtil.server;
        String address = mc.method_1562() != null && mc.method_1562().method_45734() != null ? MineHelper.mc.method_1562().method_45734().field_3761.toLowerCase() : "";
        boolean supportedServer = "FunTime".equals(server) || "SpookyTime".equals(server) || address.contains("funtime") || address.contains("spookytime");
        return supportedServer && "minecraft:lobby".equals(MineHelper.mc.field_1687.method_27983().method_29177().toString());
    }

    @Override
    public void activate() {
        this.cursor = 0;
        this.found.clear();
    }

    @Override
    public void deactivate() {
        this.cursor = 0;
        this.found.clear();
    }
}

