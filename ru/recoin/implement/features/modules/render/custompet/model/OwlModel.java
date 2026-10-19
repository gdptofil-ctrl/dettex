/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2960
 */
package ru.recoin.implement.features.modules.render.custompet.model;

import net.minecraft.class_2960;
import ru.recoin.api.mods.geckolib.model.GeoModel;
import ru.recoin.api.mods.geckolib.renderer.base.GeoRenderState;
import ru.recoin.implement.features.modules.render.custompet.entity.CustomPetEntity;

public class OwlModel
extends GeoModel<CustomPetEntity> {
    private static final class_2960 MODEL = class_2960.method_60655((String)"minecraft", (String)"owl/owl_jump_rope");
    private static final class_2960 ANIMATIONS = class_2960.method_60655((String)"minecraft", (String)"owl/owl_jump_rope");
    private static final class_2960 TEXTURE = class_2960.method_60655((String)"minecraft", (String)"textures/entity/owl/owl_jump_rope.png");

    @Override
    public class_2960 getTextureResource(GeoRenderState geoRenderState) {
        return TEXTURE;
    }

    @Override
    public class_2960 getModelResource(GeoRenderState geoRenderState) {
        return MODEL;
    }

    @Override
    public class_2960 getAnimationResource(CustomPetEntity customPetEntity) {
        return ANIMATIONS;
    }
}

