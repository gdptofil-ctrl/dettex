/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2960
 */
package ru.recoin.implement.features.modules.render.custompet.model;

import net.minecraft.class_2960;
import ru.recoin.api.mods.geckolib.constant.dataticket.DataTicket;
import ru.recoin.api.mods.geckolib.model.GeoModel;
import ru.recoin.api.mods.geckolib.renderer.base.GeoRenderState;
import ru.recoin.implement.features.modules.render.custompet.CustomPetVariant;
import ru.recoin.implement.features.modules.render.custompet.entity.CustomPetEntity;
import ru.recoin.implement.features.modules.render.custompet.model.FrogModel;
import ru.recoin.implement.features.modules.render.custompet.model.OwlModel;
import ru.recoin.implement.features.modules.render.custompet.model.RobotModel;

public class CustomPetModel
extends GeoModel<CustomPetEntity> {
    public static final DataTicket<Boolean> UMBRELLA = DataTicket.create("recoin_custom_pet.umbrella", Boolean.class);
    public static final DataTicket<Boolean> AIRBORNE = DataTicket.create("recoin_custom_pet.airborne", Boolean.class);
    public static final DataTicket<String> VARIANT = DataTicket.create("recoin_custom_pet.variant", String.class);
    public static final DataTicket<Integer> ROBOT_TYPE = DataTicket.create("recoin_custom_pet.robot_type", Integer.class);
    public static final DataTicket<Boolean> OWL = DataTicket.create("recoin_custom_pet.owl", Boolean.class);
    private final FrogModel frog = new FrogModel();
    private final RobotModel robot = new RobotModel();
    private final OwlModel owl = new OwlModel();

    private GeoModel<CustomPetEntity> pick(GeoRenderState geoRenderState) {
        if (Boolean.TRUE.equals(geoRenderState.getGeckolibData(OWL))) {
            return this.owl;
        }
        return CustomPetVariant.ROBOT.name().equals(geoRenderState.getGeckolibData(VARIANT)) ? this.robot : this.frog;
    }

    @Override
    public void addAdditionalStateData(CustomPetEntity customPetEntity, Object object, GeoRenderState geoRenderState) {
        geoRenderState.addGeckolibData(UMBRELLA, customPetEntity.shouldUseUmbrella());
        geoRenderState.addGeckolibData(AIRBORNE, customPetEntity.isAirborneMode());
        geoRenderState.addGeckolibData(VARIANT, customPetEntity.getPetVariant().name());
        geoRenderState.addGeckolibData(ROBOT_TYPE, customPetEntity.getRobotType());
        geoRenderState.addGeckolibData(OWL, customPetEntity.isOwl());
    }

    @Override
    public class_2960 getTextureResource(GeoRenderState geoRenderState) {
        return this.pick(geoRenderState).getTextureResource(geoRenderState);
    }

    @Override
    public class_2960 getModelResource(GeoRenderState geoRenderState) {
        return this.pick(geoRenderState).getModelResource(geoRenderState);
    }

    @Override
    public class_2960 getAnimationResource(CustomPetEntity customPetEntity) {
        if (customPetEntity.isOwl()) {
            return this.owl.getAnimationResource(customPetEntity);
        }
        return customPetEntity.getPetVariant().isRobot() ? this.robot.getAnimationResource(customPetEntity) : this.frog.getAnimationResource(customPetEntity);
    }
}

