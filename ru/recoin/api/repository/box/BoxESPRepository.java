/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1299
 *  net.minecraft.class_1922
 *  net.minecraft.class_2248
 *  net.minecraft.class_2338
 *  net.minecraft.class_238
 *  net.minecraft.class_265
 *  net.minecraft.class_2680
 *  net.minecraft.class_2806
 *  net.minecraft.class_2818
 *  net.minecraft.class_310
 */
package ru.recoin.api.repository.box;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_1299;
import net.minecraft.class_1922;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_2806;
import net.minecraft.class_2818;
import net.minecraft.class_310;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.event.EventManager;
import ru.recoin.common.QuickLogger;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.render.Render3DUtil;
import ru.recoin.implement.events.block.BlockUpdateEvent;
import ru.recoin.implement.events.render.WorldLoadEvent;
import ru.recoin.implement.events.render.WorldRenderEvent;

public class BoxESPRepository
implements QuickLogger {
    private final Map<class_2338, BoxEntry> boxes = new HashMap<class_2338, BoxEntry>();
    public final Map<class_1299<?>, Integer> entities = new HashMap();
    public final Map<class_2248, Integer> blocks = new HashMap<class_2248, Integer>();
    public boolean drawFill = true;

    public BoxESPRepository(EventManager eventManager) {
        eventManager.register(this);
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent e) {
        class_310 client = class_310.method_1551();
        if (client.field_1687 == null) {
            this.boxes.clear();
            return;
        }
        this.boxes.entrySet().removeIf(entry -> !client.field_1687.method_8320((class_2338)entry.getKey()).method_27852(((BoxEntry)entry.getValue()).block()));
        this.boxes.forEach((pos, entry) -> entry.shape().method_1090().forEach(shapeBox -> Render3DUtil.drawBox(shapeBox.method_996(pos), entry.color(), 1.0f, true, this.drawFill, false)));
        PlayerIntersectionUtil.streamEntities().filter(ent -> this.entities.containsKey(ent.method_5864()) && ent != client.field_1724).forEach(ent -> {
            int entityColor = this.entities.get(ent.method_5864());
            int color = entityColor == 0 ? ColorUtil.getClientColor() : entityColor;
            class_238 box = ent.method_5829().method_997(MathUtil.interpolate(ent).method_1020(ent.method_73189()));
            Render3DUtil.drawBox(box, color, 1.0f);
        });
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent e) {
        this.boxes.clear();
    }

    @EventHandler
    public void onBlockUpdate(BlockUpdateEvent e) {
        class_2338 pos = e.pos();
        class_2680 state = e.state();
        class_2248 block = state.method_26204();
        switch (e.type()) {
            case LOAD: {
                if (!this.blocks.containsKey(block)) break;
                this.putBox(pos, state, block);
                break;
            }
            case UPDATE: {
                if (this.blocks.containsKey(block)) {
                    this.putBox(pos, state, block);
                    break;
                }
                this.boxes.remove(pos);
                break;
            }
            case UNLOAD: {
                this.boxes.remove(pos);
            }
        }
    }

    public void addBlock(class_2248 block, int color) {
        this.blocks.put(block, color);
        this.refreshBlock(block);
    }

    public void removeBlock(class_2248 block) {
        this.blocks.remove(block);
        this.boxes.entrySet().removeIf(entry -> ((BoxEntry)entry.getValue()).block() == block);
    }

    public void clearBlocks() {
        this.blocks.clear();
        this.boxes.clear();
    }

    public void removeChunk(int chunkX, int chunkZ) {
        this.boxes.keySet().removeIf(pos -> pos.method_10263() >> 4 == chunkX && pos.method_10260() >> 4 == chunkZ);
    }

    public void refreshBlock(class_2248 block) {
        class_310 client = class_310.method_1551();
        if (client.field_1687 == null || client.field_1724 == null || !this.blocks.containsKey(block)) {
            return;
        }
        int centerX = client.field_1724.method_31476().field_9181;
        int centerZ = client.field_1724.method_31476().field_9180;
        int viewDistance = (Integer)client.field_1690.method_42503().method_41753();
        for (int chunkX = centerX - viewDistance; chunkX <= centerX + viewDistance; ++chunkX) {
            for (int chunkZ = centerZ - viewDistance; chunkZ <= centerZ + viewDistance; ++chunkZ) {
                class_2818 chunk = client.field_1687.method_2935().method_2857(chunkX, chunkZ, class_2806.field_12803, false);
                if (chunk == null || chunk.method_12223()) continue;
                chunk.method_51525(state -> state.method_27852(block), (pos, state) -> this.putBox(pos.method_10062(), (class_2680)state, block));
            }
        }
    }

    private void putBox(class_2338 pos, class_2680 state, class_2248 block) {
        class_310 client = class_310.method_1551();
        if (client.field_1687 == null) {
            return;
        }
        class_265 shape = state.method_26218((class_1922)client.field_1687, pos);
        int blockColor = this.blocks.get(block);
        int color = blockColor == 0 ? ColorUtil.replAlpha(state.method_26205((class_1922)client.field_1687, (class_2338)pos).field_16011, 1.0f) : blockColor;
        this.boxes.put(pos.method_10062(), new BoxEntry(block, shape, color));
    }

    private record BoxEntry(class_2248 block, class_265 shape, int color) {
    }
}

