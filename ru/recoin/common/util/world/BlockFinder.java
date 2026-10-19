/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2338
 *  net.minecraft.class_2586
 */
package ru.recoin.common.util.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_2338;
import net.minecraft.class_2586;
import ru.recoin.api.event.EventHandler;
import ru.recoin.common.QuickImports;
import ru.recoin.core.Main;
import ru.recoin.implement.events.block.BlockEntityProgressEvent;
import ru.recoin.implement.events.render.WorldLoadEvent;

public class BlockFinder
implements QuickImports {
    public final List<class_2586> blockEntities = new ArrayList<class_2586>();
    private final Map<class_2338, class_2586> entitiesByPosition = new HashMap<class_2338, class_2586>();
    public static BlockFinder INSTANCE = new BlockFinder();

    public BlockFinder() {
        Main.getInstance().getEventManager().register(this);
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent e) {
        this.blockEntities.clear();
        this.entitiesByPosition.clear();
    }

    @EventHandler
    public void onBlockEntityProgress(BlockEntityProgressEvent e) {
        class_2586 blockEntity = e.blockEntity();
        switch (e.type()) {
            case ADD: {
                if (this.entitiesByPosition.putIfAbsent(blockEntity.method_11016().method_10062(), blockEntity) != null) break;
                this.blockEntities.add(blockEntity);
                break;
            }
            case REMOVE: {
                if (!this.entitiesByPosition.remove(blockEntity.method_11016(), blockEntity)) break;
                this.blockEntities.remove(blockEntity);
            }
        }
    }
}

