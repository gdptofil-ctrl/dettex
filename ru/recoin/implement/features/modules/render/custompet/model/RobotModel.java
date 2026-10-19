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
import ru.recoin.implement.features.modules.render.custompet.model.CustomPetModel;

public final class RobotModel
extends GeoModel<CustomPetEntity> {
    public static final int TYPE_COUNT = 4;
    private static final class_2960 MODEL = class_2960.method_60655((String)"minecraft", (String)"robot/robot");
    private static final class_2960 ANIMATIONS = class_2960.method_60655((String)"minecraft", (String)"robot/robot");
    private static final class_2960[] TEXTURES = RobotModel.buildTextures();

    @Override
    public class_2960 getTextureResource(GeoRenderState geoRenderState) {
        Integer n = geoRenderState.getGeckolibData(CustomPetModel.ROBOT_TYPE);
        int n2 = n == null ? 0 : Math.floorMod(n, 4);
        return TEXTURES[n2];
    }

    @Override
    public class_2960 getModelResource(GeoRenderState geoRenderState) {
        return MODEL;
    }

    @Override
    public class_2960 getAnimationResource(CustomPetEntity customPetEntity) {
        return ANIMATIONS;
    }

    private static class_2960[] buildTextures() {
        class_2960[] identifierArray = new class_2960[4];
        for (int i = 0; i < 4; ++i) {
            identifierArray[i] = class_2960.method_60655((String)"minecraft", (String)("textures/entity/robot/type_" + (i + 1) + ".png"));
        }
        return identifierArray;
    }
}

