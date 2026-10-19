/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_156
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_3532
 *  net.minecraft.class_4011
 *  net.minecraft.class_425
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.recoin.mixins;

import net.minecraft.class_156;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_4011;
import net.minecraft.class_425;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.common.util.render.LoadingScreenRenderer;

@Mixin(value={class_425.class})
public abstract class SplashOverlayMixin {
    @Shadow
    @Final
    private class_310 field_18217;
    @Shadow
    @Final
    private class_4011 field_17767;
    @Shadow
    @Final
    private boolean field_18219;
    @Shadow
    private float field_17770;
    @Shadow
    private long field_17771;
    @Shadow
    private long field_18220;

    @Inject(method={"method_25394"}, at={@At(value="HEAD")}, cancellable=true)
    private void recoin$renderLoading(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        float entering;
        if (!LoadingScreenRenderer.prepare()) {
            return;
        }
        long now = class_156.method_658();
        if (this.field_18219 && this.field_18220 == -1L) {
            this.field_18220 = now;
        }
        float complete = this.field_17771 < 0L ? -1.0f : (float)(now - this.field_17771) / 1000.0f;
        float f = entering = this.field_18220 < 0L ? 1.0f : class_3532.method_15363((float)((float)(now - this.field_18220) / 850.0f), (float)0.0f, (float)1.0f);
        if (complete >= 1.0f || this.field_18219 && entering < 1.0f) {
            if (this.field_18217.field_1755 != null) {
                this.field_18217.field_1755.method_47413(context, mouseX, mouseY, delta);
            } else {
                this.field_18217.field_1705.method_72738();
            }
        }
        float easedEntering = entering * entering * (3.0f - 2.0f * entering);
        float leaving = class_3532.method_15363((float)(complete - 1.0f), (float)0.0f, (float)1.0f);
        float easedLeaving = leaving * leaving * (3.0f - 2.0f * leaving);
        float opacity = complete >= 1.0f ? 1.0f - easedLeaving : (this.field_18219 ? easedEntering : 1.0f);
        this.field_17770 = class_3532.method_15363((float)class_3532.method_16439((float)0.1f, (float)this.field_17770, (float)this.field_17767.method_18229()), (float)0.0f, (float)1.0f);
        LoadingScreenRenderer.draw(context, opacity, complete >= 0.0f ? 1.0f : Math.min(1.0f, this.field_17767.method_18229()));
        if (complete >= 2.0f) {
            this.field_18217.method_18502(null);
        }
        ci.cancel();
    }
}

