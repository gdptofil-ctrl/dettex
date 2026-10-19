/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1309
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_4185
 *  net.minecraft.class_437
 *  net.minecraft.class_490
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.recoin.mixins;

import net.minecraft.class_1309;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_490;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.api.accessor.ContainerButtonHost;
import ru.recoin.implement.features.modules.misc.SelfDestruct;
import ru.recoin.implement.features.modules.render.UiAnimationManager;

@Mixin(value={class_490.class})
public abstract class InventoryScreenMixin {
    @Redirect(method={"method_2389"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_490;method_2486(Lnet/minecraft/class_332;IIIIIFFFLnet/minecraft/class_1309;)V"))
    private void recoin$scalePlayerModel(class_332 context, int x1, int y1, int x2, int y2, int size, float f, float mouseX, float mouseY, class_1309 entity) {
        float scale = UiAnimationManager.getInventoryScale((class_437)(Object)this);
        if (scale > 0.999f && scale < 1.001f) {
            class_490.method_2486((class_332)context, (int)x1, (int)y1, (int)x2, (int)y2, (int)size, (float)f, (float)mouseX, (float)mouseY, (class_1309)entity);
            return;
        }
        float cx = (float)context.method_51421() * 0.5f;
        float cy = (float)context.method_51443() * 0.5f;
        int newX1 = Math.round(cx + ((float)x1 - cx) * scale);
        int newY1 = Math.round(cy + ((float)y1 - cy) * scale);
        int newX2 = Math.round(cx + ((float)x2 - cx) * scale);
        int newY2 = Math.round(cy + ((float)y2 - cy) * scale);
        int newSize = Math.max(1, Math.round((float)size * scale));
        class_490.method_2486((class_332)context, (int)newX1, (int)newY1, (int)newX2, (int)newY2, (int)newSize, (float)f, (float)mouseX, (float)mouseY, (class_1309)entity);
    }

    @Inject(method={"method_25394"}, at={@At(value="RETURN")})
    private void recoin$renderThrowAllButton(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ContainerButtonHost buttons = (ContainerButtonHost)((Object)this);
        buttons.recoin$updateContainerButtonStates();
        class_310 client = class_310.method_1551();
        if (client.field_1724 == null || SelfDestruct.isLocked()) {
            return;
        }
        class_4185 button = buttons.recoin$getInventoryThrowButton();
        if (button != null && button.field_22764) {
            boolean animated = UiAnimationManager.beginInventoryRender(context, (class_437)(Object)this);
            button.method_25394(context, mouseX, mouseY, delta);
            if (animated) {
                UiAnimationManager.endInventoryRender(context);
            }
        }
    }
}

