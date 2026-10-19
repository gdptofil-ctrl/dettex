/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  net.minecraft.class_10017
 *  net.minecraft.class_10034
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_897
 *  net.minecraft.class_922
 *  net.minecraft.class_970
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.Constant
 *  org.spongepowered.asm.mixin.injection.ModifyConstant
 */
package ru.recoin.mixins.geckolib.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.class_10017;
import net.minecraft.class_10034;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_897;
import net.minecraft.class_922;
import net.minecraft.class_970;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import ru.recoin.api.mods.geckolib.renderer.GeoArmorRenderer;
import ru.recoin.api.mods.geckolib.renderer.GeoEntityRenderer;

@Mixin(value={class_897.class})
public class EntityRendererMixin<T extends class_1297, S extends class_10017> {
    @ModifyConstant(method={"method_3921(Lnet/minecraft/class_1297;D)Z"}, constant={@Constant(doubleValue=4096.0)}, require=0)
    public double modifyMaxNameplateDistance(double constant) {
        return (Object)this instanceof GeoEntityRenderer ? 65536.0 : constant;
    }

    @WrapMethod(method={"method_62354(Lnet/minecraft/class_1297;Lnet/minecraft/class_10017;F)V"})
    public void geckolib_captureDataForArmorLayer(T entity, S state, float partialTick, Operation<Void> original) {
        Object object;
        original.call(new Object[]{entity, state, Float.valueOf(partialTick)});
        if (state instanceof class_10034 && (object = this) instanceof class_922) {
            class_922 livingRenderer = (class_922)object;
            for (Object layer : livingRenderer.field_4738) {
                if (!(layer instanceof class_970)) continue;
                class_970 armorLayer = (class_970)layer;
                GeoArmorRenderer.captureRenderStates(EntityRendererMixin.geckolib_castRenderState(state), (class_1309)entity, partialTick, (renderState2, slot) -> armorLayer.method_4172(renderState2, slot), slot -> EntityRendererMixin.geckolib_castRenderState(state));
                break;
            }
        }
    }

    @Unique
    private static <R extends class_10034> R geckolib_castRenderState(class_10017 renderState) {
        return (R)((class_10034)renderState);
    }
}

