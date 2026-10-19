/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1049
 *  net.minecraft.class_10537
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 */
package ru.recoin.implement.features.modules.render.custompet;

import net.minecraft.class_1049;
import net.minecraft.class_10537;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import ru.recoin.api.mods.geckolib.cache.GeckoLibResources;

public final class CustomPetWarmup {
    private static final class_2960 TEXTURE = class_2960.method_60655((String)"minecraft", (String)"textures/entity/frog/custom_pet.png");
    private static final class_2960 MODEL = class_2960.method_60655((String)"minecraft", (String)"frog/custom_pet");

    private CustomPetWarmup() {
    }

    public static void warmup() {
        class_310 minecraftClient = class_310.method_1551();
        if (minecraftClient == null) {
            return;
        }
        try {
            if (minecraftClient.method_1531() != null) {
                minecraftClient.method_1531().method_65876(TEXTURE, (class_10537)new class_1049(TEXTURE));
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            GeckoLibResources.getBakedModels().getModel(MODEL);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }
}

