package ru.recoin.mixins;

import net.minecraft.class_10042;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import ru.recoin.implement.features.modules.render.hitcolor.IHitColorState;

@Mixin(value={class_10042.class})
public class HitColorStateMixin implements IHitColorState {
    @Unique
    private int recoin$hitTint;

    @Override
    public int recoin$getHitTint() {
        return this.recoin$hitTint;
    }

    @Override
    public void recoin$setHitTint(int tint) {
        this.recoin$hitTint = tint;
    }
}
