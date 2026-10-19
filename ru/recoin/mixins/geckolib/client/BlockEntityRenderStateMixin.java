/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
 *  net.minecraft.class_11954
 *  org.jetbrains.annotations.ApiStatus$Internal
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 */
package ru.recoin.mixins.geckolib.client;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.Map;
import net.minecraft.class_11954;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import ru.recoin.api.mods.geckolib.constant.DataTickets;
import ru.recoin.api.mods.geckolib.constant.dataticket.DataTicket;
import ru.recoin.api.mods.geckolib.renderer.base.GeoRenderState;

@Mixin(value={class_11954.class})
public class BlockEntityRenderStateMixin
implements GeoRenderState {
    @Shadow
    public int field_62676;
    @Unique
    private final Map<DataTicket<?>, Object> geckolib_data = new Reference2ObjectOpenHashMap();

    @Override
    @Unique
    public <D> void addGeckolibData(DataTicket<D> dataTicket, D data) {
        this.geckolib_data.put(dataTicket, data);
    }

    @Override
    @Unique
    public boolean hasGeckolibData(DataTicket<?> dataTicket) {
        return this.geckolib_data.containsKey(dataTicket);
    }

    @Override
    @Unique
    public int getPackedLight() {
        return this.getOrDefaultGeckolibData(DataTickets.PACKED_LIGHT, this.field_62676);
    }

    @Override
    @ApiStatus.Internal
    public Map<DataTicket<?>, Object> getDataMap() {
        return this.geckolib_data;
    }
}

