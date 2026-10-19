/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_339
 *  net.minecraft.class_342
 *  net.minecraft.class_408
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.recoin.mixins;

import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_339;
import net.minecraft.class_342;
import net.minecraft.class_408;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.api.system.sound.SoundManager;
import ru.recoin.implement.features.modules.render.UiAnimationManager;

@Mixin(value={class_339.class})
public abstract class ClickableWidgetMixin {
    @Unique
    private boolean recoin$scaled;
    @Unique
    private boolean recoin$lastHovered;
    @Unique
    private long recoin$hoverChangedAt = System.currentTimeMillis();

    @Shadow
    public abstract int method_46426();

    @Shadow
    public abstract int method_46427();

    @Shadow
    public abstract int method_25368();

    @Shadow
    public abstract int method_25364();

    @Shadow
    public abstract boolean method_49606();

    @Inject(method={"method_25354"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private void recoin$interfaceClickSound(CallbackInfo ci) {
        if (SoundManager.playInterfaceClickSound()) {
            ci.cancel();
        }
    }

    @Inject(method={"method_25394"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_339;method_48579(Lnet/minecraft/class_332;IIF)V")}, require=0)
    private void recoin$beginButtonAnimation(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        float scale;
        if ((Object)this instanceof class_342 && class_310.method_1551().field_1755 instanceof class_408) {
            this.recoin$scaled = false;
            return;
        }
        boolean hovered = this.method_49606();
        if (hovered != this.recoin$lastHovered) {
            this.recoin$lastHovered = hovered;
            this.recoin$hoverChangedAt = System.currentTimeMillis();
        }
        if (Math.abs((scale = UiAnimationManager.buttonScale(hovered, this.recoin$hoverChangedAt)) - 1.0f) < 1.0E-4f) {
            return;
        }
        float x = (float)this.method_46426() + (float)this.method_25368() * 0.5f;
        float y = (float)this.method_46427() + (float)this.method_25364() * 0.5f;
        context.method_51448().pushMatrix();
        context.method_51448().translate(x, y);
        context.method_51448().scale(scale, scale);
        context.method_51448().translate(-x, -y);
        this.recoin$scaled = true;
    }

    @Inject(method={"method_25394"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_339;method_48579(Lnet/minecraft/class_332;IIF)V", shift=At.Shift.AFTER)}, require=0)
    private void recoin$endButtonAnimation(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!this.recoin$scaled) {
            return;
        }
        context.method_51448().popMatrix();
        this.recoin$scaled = false;
    }
}

