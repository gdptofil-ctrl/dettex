package ru.recoin.implement.features.modules.render.hitcolor;

/** Added to living entity render states: the Hit Color tint for this frame (0 = none). */
public interface IHitColorState {
    int recoin$getHitTint();

    void recoin$setHitTint(int tint);
}
