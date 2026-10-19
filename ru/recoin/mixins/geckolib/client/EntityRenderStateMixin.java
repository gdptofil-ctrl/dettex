/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
 *  net.minecraft.class_10017
 *  org.jetbrains.annotations.ApiStatus$Internal
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 */
package ru.recoin.mixins.geckolib.client;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.Map;
import net.minecraft.class_10017;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import ru.recoin.api.mods.geckolib.constant.DataTickets;
import ru.recoin.api.mods.geckolib.constant.dataticket.DataTicket;
import ru.recoin.api.mods.geckolib.renderer.base.GeoRenderState;

@Mixin(value={class_10017.class})
public class EntityRenderStateMixin
implements GeoRenderState {
    @Shadow
    public int field_61820;
    @Shadow
    public float field_53328;
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
        return this.getOrDefaultGeckolibData(DataTickets.PACKED_LIGHT, this.field_61820);
    }

    @Override
    public double getAnimatableAge() {
        return this.getOrDefaultGeckolibData(DataTickets.TICK, Double.valueOf(this.field_53328));
    }

    @Override
    @ApiStatus.Internal
    public Map<DataTicket<?>, Object> getDataMap() {
        return this.geckolib_data;
    }
}

