/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.v2.WrapWithCondition
 *  net.minecraft.class_10034
 *  net.minecraft.class_11659
 *  net.minecraft.class_1304
 *  net.minecraft.class_1799
 *  net.minecraft.class_3883
 *  net.minecraft.class_3887
 *  net.minecraft.class_4587
 *  net.minecraft.class_572
 *  net.minecraft.class_970
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package ru.recoin.mixins.geckolib.client;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.class_10034;
import net.minecraft.class_11659;
import net.minecraft.class_1304;
import net.minecraft.class_1799;
import net.minecraft.class_3883;
import net.minecraft.class_3887;
import net.minecraft.class_4587;
import net.minecraft.class_572;
import net.minecraft.class_970;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.recoin.api.mods.geckolib.renderer.GeoArmorRenderer;

@Mixin(value={class_970.class})
public abstract class HumanoidArmorLayerMixin<S extends class_10034, M extends class_572<S>, A extends class_572<S>>
extends class_3887<S, M> {
    public HumanoidArmorLayerMixin(class_3883<S, M> renderer) {
        super(renderer);
    }

    @WrapWithCondition(method={"method_17157(Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;ILnet/minecraft/class_10034;FF)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_970;renderArmor(Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_1799;Lnet/minecraft/class_1304;ILnet/minecraft/class_572;)V")}, require=0)
    public boolean geckolib_wrapArmorPieceRender(class_970<S, M, A> layer, class_4587 poseStack, class_11659 renderTasks, class_1799 stack, class_1304 slot, int packedLight, S entityRenderState) {
        return entityRenderState instanceof class_10034 && !GeoArmorRenderer.tryRenderGeoArmorPiece((renderState, equipmentSlot) -> layer.method_4172(renderState, equipmentSlot), poseStack, renderTasks, stack, slot, packedLight, entityRenderState);
    }
}

