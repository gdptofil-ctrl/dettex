/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10039
 *  net.minecraft.class_10428
 *  net.minecraft.class_11659
 *  net.minecraft.class_1297
 *  net.minecraft.class_1542
 *  net.minecraft.class_238
 *  net.minecraft.class_310
 *  net.minecraft.class_4587
 *  net.minecraft.class_4608
 *  net.minecraft.class_5819
 *  net.minecraft.class_7833
 *  net.minecraft.class_916
 *  org.joml.Quaternionfc
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.recoin.mixins;

import net.minecraft.class_10039;
import net.minecraft.class_10428;
import net.minecraft.class_11659;
import net.minecraft.class_1297;
import net.minecraft.class_1542;
import net.minecraft.class_238;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4608;
import net.minecraft.class_5819;
import net.minecraft.class_7833;
import net.minecraft.class_916;
import org.joml.Quaternionfc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.implement.features.modules.render.Items2D;
import ru.recoin.implement.features.modules.render.ShaderESP;

@Mixin(value={class_916.class})
public abstract class ItemEntityRendererMixin {
    @Inject(method={"method_62470(Lnet/minecraft/class_1542;Lnet/minecraft/class_10039;F)V"}, at={@At(value="TAIL")}, require=0)
    private void recoin$keepShaderEspOutline(class_1542 entity, class_10039 state, float tickDelta, CallbackInfo ci) {
        ShaderESP shader = ShaderESP.getInstance();
        if (shader != null && shader.shouldHighlight((class_1297)entity)) {
            state.field_61821 = shader.getOutlineColor();
        }
    }

    @Redirect(method={"method_3996(Lnet/minecraft/class_10039;Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_12075;)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_916;method_72986(Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;ILnet/minecraft/class_10428;Lnet/minecraft/class_5819;Lnet/minecraft/class_238;)V"), require=0)
    private void recoin$render2dItems(class_4587 matrices, class_11659 queue, int light, class_10428 state, class_5819 random, class_238 modelBounds) {
        Items2D module = Items2D.getInstance();
        if (module == null || !module.isState() || !(state instanceof class_10039)) {
            class_916.method_72986((class_4587)matrices, (class_11659)queue, (int)light, (class_10428)state, (class_5819)random, (class_238)modelBounds);
            return;
        }
        class_10039 itemState = (class_10039)state;
        class_310 client = class_310.method_1551();
        if (itemState.field_55310.method_65606() || client.field_1773 == null || client.field_1773.method_19418() == null) {
            class_916.method_72986((class_4587)matrices, (class_11659)queue, (int)light, (class_10428)state, (class_5819)random, (class_238)modelBounds);
            return;
        }
        float vanillaRotation = class_1542.method_27314((float)itemState.field_53328, (float)itemState.field_53435);
        matrices.method_22903();
        matrices.method_22907((Quaternionfc)class_7833.field_40716.rotation(-vanillaRotation));
        matrices.method_22907((Quaternionfc)client.field_1773.method_19418().method_23767());
        random.method_43052((long)itemState.field_55312);
        int amount = Math.max(1, itemState.field_55311);
        for (int i = 0; i < amount; ++i) {
            matrices.method_22903();
            if (i > 0) {
                matrices.method_46416((random.method_43057() * 2.0f - 1.0f) * 0.12f, (random.method_43057() * 2.0f - 1.0f) * 0.08f, (float)i * 0.015f);
            }
            matrices.method_22905(0.75f, 0.75f, 0.06f);
            itemState.field_55310.method_65604(matrices, queue, light, class_4608.field_21444, itemState.field_61821);
            matrices.method_22909();
        }
        matrices.method_22909();
    }
}

