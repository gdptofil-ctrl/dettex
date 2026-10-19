/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  com.llamalad7.mixinextras.sugar.Local
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_1531
 *  net.minecraft.class_238
 *  net.minecraft.class_243
 *  net.minecraft.class_4587
 *  net.minecraft.class_4588
 *  net.minecraft.class_5498
 *  net.minecraft.class_898
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.recoin.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1531;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_5498;
import net.minecraft.class_898;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.api.repository.friend.FriendUtils;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.math.ProjectionUtil;
import ru.recoin.common.util.render.Render3DUtil;
import ru.recoin.core.Main;
import ru.recoin.implement.features.modules.render.custompet.entity.CustomPetEntity;

@Mixin(value={class_898.class})
public class EntityRenderDispatcherMixin
implements QuickImports {
    @ModifyExpressionValue(method={"render(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/EntityRenderer;)V"}, at={@At(value="FIELD", target="Lnet/minecraft/class_10017;field_53333:Z")}, require=0)
    private boolean renderHitboxHook(boolean original, @Local(ordinal=0, argsOnly=true) class_1297 entity) {
        return entity instanceof class_1531;
    }

    @Inject(method={"renderHitbox"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private static void renderHitboxHook(class_4587 matrices, class_4588 vertices, class_1297 entity, float tickDelta, float red, float green, float blue, CallbackInfo ci) {
        if (entity instanceof CustomPetEntity) {
            ci.cancel();
            return;
        }
        if (!Main.getInstance().getBoxESPRepository().entities.containsKey(entity.method_5864())) {
            EntityRenderDispatcherMixin.renderBox(entity);
        }
        ci.cancel();
    }

    @Unique
    private static void renderBox(class_1297 entity) {
        if (entity != EntityRenderDispatcherMixin.mc.field_1724 || !EntityRenderDispatcherMixin.mc.field_1690.method_31044().equals((Object)class_5498.field_26664)) {
            int color = FriendUtils.isFriend(entity) ? ColorUtil.getFriendColor() : ColorUtil.getClientColor();
            class_243 offset = MathUtil.interpolate(entity).method_1020(entity.method_73189());
            class_238 box = entity.method_5829().method_997(offset);
            if (ProjectionUtil.canSee(box)) {
                if (entity instanceof class_1309) {
                    class_1309 living = (class_1309)entity;
                    float width = entity.method_17681();
                    class_243 eyeMin = entity.method_33571().method_1019(offset).method_1031((double)(-width / 2.0f), 0.0, (double)(-width / 2.0f));
                    Render3DUtil.drawBox(box, ColorUtil.multRed(color, 1 + living.field_6235), 2.0f, true, true, true);
                    Render3DUtil.drawLine(eyeMin, eyeMin.method_1031((double)width, 0.0, 0.0), ColorUtil.RED, 2.0f, true);
                    Render3DUtil.drawLine(eyeMin.method_1031((double)width, 0.0, 0.0), eyeMin.method_1031((double)width, 0.0, (double)width), ColorUtil.RED, 2.0f, true);
                    Render3DUtil.drawLine(eyeMin, eyeMin.method_1031(0.0, 0.0, (double)width), ColorUtil.RED, 2.0f, true);
                    Render3DUtil.drawLine(eyeMin.method_1031(0.0, 0.0, (double)width), eyeMin.method_1031((double)width, 0.0, (double)width), ColorUtil.RED, 2.0f, true);
                } else {
                    Render3DUtil.drawBox(box, color, 2.0f, true, true, true);
                }
            }
        }
    }
}

