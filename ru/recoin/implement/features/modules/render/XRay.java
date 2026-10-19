/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2338
 *  net.minecraft.class_238
 *  net.minecraft.class_2680
 */
package ru.recoin.implement.features.modules.render;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_2680;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.MultiSelectSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.common.util.render.Render3DUtil;
import ru.recoin.implement.events.block.BlockUpdateEvent;
import ru.recoin.implement.events.render.WorldLoadEvent;
import ru.recoin.implement.events.render.WorldRenderEvent;

public class XRay
extends Module {
    private final Map<class_2338, class_2680> map = new HashMap<class_2338, class_2680>();
    private final SelectSetting modeSetting = new SelectSetting("Mode", "Operating mode for XRay").value("Block Update");
    private final MultiSelectSetting blockTypeSetting = new MultiSelectSetting("Blocks", "Blocks that will be displayed").value("Ancient Debris", "Diamond", "Emerald", "Iron", "Gold");

    public XRay() {
        super("XRay", ModuleCategory.RENDER);
        this.setup(this.modeSetting, this.blockTypeSetting);
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent e) {
        this.map.forEach((key, value) -> {
            if (this.blockTypeSetting.getSelected().toString().toLowerCase().contains(this.getBlockName((class_2680)value))) {
                Render3DUtil.drawBox(new class_238(key), this.getColorByBlock((class_2680)value), 1.0f);
            }
        });
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent e) {
        this.map.clear();
    }

    @EventHandler
    public void onBlockUpdate(BlockUpdateEvent e) {
        class_2680 state = e.state();
        class_2338 pos = e.pos();
        switch (e.type()) {
            case UPDATE: {
                if (this.getColorByBlock(state) != -1 && !this.map.containsKey(pos)) {
                    this.map.put(pos, state);
                }
                if (!this.map.containsKey(pos) || this.map.get(pos).equals((Object)state)) break;
                this.map.remove(pos);
                break;
            }
            case UNLOAD: {
                this.map.remove(pos);
            }
        }
    }

    private int getColorByBlock(class_2680 block) {
        return switch (this.getBlockName(block)) {
            case "ancient debris" -> -5868204;
            case "diamond" -> -15107199;
            case "emerald" -> -12482789;
            case "iron" -> -9090017;
            case "gold" -> -3819208;
            default -> -1;
        };
    }

    public void removeChunk(int chunkX, int chunkZ) {
        this.map.keySet().removeIf(pos -> pos.method_10263() >> 4 == chunkX && pos.method_10260() >> 4 == chunkZ);
    }

    private String getBlockName(class_2680 state) {
        return state.method_26204().method_8389().toString().replace("minecraft:", "").replace("_ore", "").replace("_", " ");
    }
}

