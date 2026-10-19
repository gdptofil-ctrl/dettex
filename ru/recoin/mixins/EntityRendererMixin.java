/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10017
 *  net.minecraft.class_1297
 *  net.minecraft.class_1665
 *  net.minecraft.class_243
 *  net.minecraft.class_4604
 *  net.minecraft.class_897
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.recoin.mixins;

import net.minecraft.class_10017;
import net.minecraft.class_1297;
import net.minecraft.class_1665;
import net.minecraft.class_243;
import net.minecraft.class_4604;
import net.minecraft.class_897;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.recoin.implement.features.modules.render.EntityESP;
import ru.recoin.implement.features.modules.render.ShaderESP;

@Mixin(value={class_897.class})
public abstract class EntityRendererMixin {
    @Inject(method={"method_3933"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private void recoin$neverCullShaderEsp(class_1297 entity, class_4604 frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        ShaderESP shader = ShaderESP.getInstance();
        if (shader != null && shader.shouldHighlight(entity)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method={"method_3921"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private void recoin$hasLabelForArrows(class_1297 entity, double squaredDistanceToCamera, CallbackInfoReturnable<Boolean> cir) {
        EntityESP esp;
        if (entity instanceof class_1665 && (esp = EntityESP.getInstance()) != null && esp.isState() && esp.entityType.isSelected("Item")) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method={"method_62354(Lnet/minecraft/class_1297;Lnet/minecraft/class_10017;F)V"}, at={@At(value="TAIL")}, require=0)
    private void recoin$shaderEspOutline(class_1297 entity, class_10017 state, float tickDelta, CallbackInfo ci) {
        EntityESP esp;
        ShaderESP shader = ShaderESP.getInstance();
        if (shader != null && shader.shouldHighlight(entity)) {
            state.field_61821 = shader.getOutlineColor();
        }
        if (entity instanceof class_1665 && (esp = EntityESP.getInstance()) != null && esp.isState() && esp.entityType.isSelected("Item")) {
            if (state.field_53338 == null) {
                state.field_53338 = new class_243(0.0, 0.35, 0.0);
            }
            if (state.field_53337 == null) {
                state.field_53337 = entity.method_5476();
            }
        }
    }
}

