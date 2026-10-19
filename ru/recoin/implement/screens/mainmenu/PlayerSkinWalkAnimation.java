/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_591
 */
package ru.recoin.implement.screens.mainmenu;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.class_591;

public final class PlayerSkinWalkAnimation {
    private static final Set<class_591> MODELS = Collections.newSetFromMap(new WeakHashMap());

    private PlayerSkinWalkAnimation() {
    }

    public static void register(class_591 model) {
        MODELS.add(model);
    }

    public static void applyIfRegistered(class_591 model) {
        float swing;
        if (!MODELS.contains(model)) {
            return;
        }
        float phase = (float)System.currentTimeMillis() / 155.0f;
        model.field_3401.field_3654 = swing = (float)Math.sin(phase) * 1.08f;
        model.field_27433.field_3654 = -swing;
        model.field_3392.field_3654 = -swing;
        model.field_3397.field_3654 = swing;
        model.field_3401.field_3674 = 0.035f;
        model.field_27433.field_3674 = -0.035f;
        model.field_3391.field_3674 = swing * 0.025f;
        model.field_3394.field_3675 = model.field_3398.field_3675 = swing * 0.055f;
        model.field_3486.field_3654 = model.field_3401.field_3654;
        model.field_3486.field_3674 = model.field_3401.field_3674;
        model.field_3484.field_3654 = model.field_27433.field_3654;
        model.field_3484.field_3674 = model.field_27433.field_3674;
        model.field_3479.field_3654 = model.field_3392.field_3654;
        model.field_3482.field_3654 = model.field_3397.field_3654;
        model.field_3483.field_3674 = model.field_3391.field_3674;
    }
}

