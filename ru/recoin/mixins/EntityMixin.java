/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  net.minecraft.class_1297
 *  net.minecraft.class_1657
 *  net.minecraft.class_2246
 *  net.minecraft.class_238
 *  net.minecraft.class_243
 *  net.minecraft.class_2680
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.recoin.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_2246;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.recoin.api.accessor.IEntityTrails;
import ru.recoin.api.event.EventManager;
import ru.recoin.common.QuickImports;
import ru.recoin.implement.events.player.BoundingBoxControlEvent;
import ru.recoin.implement.events.player.PlayerVelocityStrafeEvent;
import ru.recoin.implement.features.modules.movement.NoWeb;
import ru.recoin.implement.features.modules.render.NoRender;
import ru.recoin.implement.features.modules.render.ShaderESP;
import ru.recoin.implement.features.modules.render.Trails;

@Mixin(value={class_1297.class})
public abstract class EntityMixin
implements QuickImports,
IEntityTrails {
    @Shadow
    private class_238 field_6005;
    @Unique
    private final List<Trails.Trail> recoin$trails = new ArrayList<Trails.Trail>();

    @Inject(method={"method_5697"}, at={@At(value="HEAD")}, cancellable=true)
    private void recoin$cancelFakePlayerPush(class_1297 entity, CallbackInfo ci) {
        if (entity != null && entity.method_5628() == 1337228) {
            ci.cancel();
        } else if (((class_1297)(Object)this).method_5628() == 1337228) {
            ci.cancel();
        }
    }

    @Inject(method={"method_5844"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private void recoin$noWebVanilla(class_2680 state, class_243 multiplier, CallbackInfo ci) {
        NoWeb noWeb = NoWeb.getInstance();
        if ((Object)this == EntityMixin.mc.field_1724 && noWeb != null && noWeb.isVanilla() && state.method_27852(class_2246.field_10343)) {
            ci.cancel();
        }
    }

    @Override
    public List<Trails.Trail> getTrails() {
        return this.recoin$trails;
    }

    @Redirect(method={"method_5724"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_1297;method_18795(Lnet/minecraft/class_243;FF)Lnet/minecraft/class_243;"), require=0)
    public class_243 hookVelocity(class_243 movementInput, float speed, float yaw) {
        class_243 original = class_1297.method_18795((class_243)movementInput, (float)speed, (float)yaw);
        if ((Object)this == EntityMixin.mc.field_1724) {
            PlayerVelocityStrafeEvent event = new PlayerVelocityStrafeEvent(movementInput, speed, yaw, original);
            EventManager.callEvent(event);
            return event.getVelocity();
        }
        return original;
    }

    @Inject(method={"method_5829"}, at={@At(value="HEAD")}, cancellable=true)
    public final void getBoundingBox(CallbackInfoReturnable<class_238> cir) {
        BoundingBoxControlEvent event = new BoundingBoxControlEvent(this.field_6005, (class_1297)(Object)this);
        EventManager.callEvent(event);
        cir.setReturnValue(event.getBox());
    }

    @Inject(method={"method_5851"}, at={@At(value="HEAD")}, cancellable=true)
    private void recoin$disablePlayerGlow(CallbackInfoReturnable<Boolean> cir) {
        ShaderESP shaderESP = ShaderESP.getInstance();
        if (shaderESP != null && shaderESP.shouldHighlight((class_1297)(Object)this)) {
            cir.setReturnValue(true);
            return;
        }
        NoRender noRender = NoRender.getInstance();
        if (noRender.isState() && noRender.modeSetting.isSelected("Glowing Players") && (Object)this instanceof class_1657) {
            cir.setReturnValue(false);
        }
    }

    @ModifyExpressionValue(method={"method_5784"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_1297;method_65038()Z")}, require=0)
    public boolean isControlledByPlayerHook(boolean original) {
        if ((Object)this == EntityMixin.mc.field_1724) {
            return false;
        }
        return original;
    }
}

