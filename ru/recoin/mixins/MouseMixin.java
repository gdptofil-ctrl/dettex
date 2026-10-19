/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.v2.WrapWithCondition
 *  net.minecraft.class_11910
 *  net.minecraft.class_310
 *  net.minecraft.class_312
 *  net.minecraft.class_3675$class_307
 *  net.minecraft.class_746
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.recoin.mixins;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.class_11910;
import net.minecraft.class_310;
import net.minecraft.class_312;
import net.minecraft.class_3675;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.api.event.EventManager;
import ru.recoin.implement.events.keyboard.HotBarScrollEvent;
import ru.recoin.implement.events.keyboard.KeyEvent;
import ru.recoin.implement.events.keyboard.MouseRotationEvent;
import ru.recoin.implement.events.render.FovEvent;
import ru.recoin.implement.features.modules.misc.SelfDestruct;

@Mixin(value={class_312.class})
public class MouseMixin {
    @Final
    @Shadow
    private class_310 field_1779;
    @Shadow
    public double field_1789;
    @Shadow
    public double field_1787;

    @Inject(method={"method_1601"}, at={@At(value="HEAD")})
    public void onMouseButtonHook(long window, class_11910 input, int action, CallbackInfo ci) {
        int button = input.comp_4801();
        if (button != -1 && window == this.field_1779.method_22683().method_4490()) {
            if (SelfDestruct.isLocked()) {
                return;
            }
            EventManager.callEvent(new KeyEvent(this.field_1779.field_1755, class_3675.class_307.field_1672, button, action));
        }
    }

    @Inject(method={"method_1598"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_746;method_31548()Lnet/minecraft/class_1661;")}, cancellable=true, require=0)
    public void onMouseScrollHook(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (SelfDestruct.isLocked()) {
            return;
        }
        HotBarScrollEvent event = new HotBarScrollEvent(horizontal, vertical);
        EventManager.callEvent(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method={"method_1606"}, at={@At(value="HEAD")}, require=0)
    private void onUpdateMouse(double timeDelta, CallbackInfo ci) {
        if (SelfDestruct.isLocked()) {
            return;
        }
        FovEvent event = new FovEvent();
        EventManager.callEvent(event);
        if (event.isCancelled()) {
            double slowdown = (double)event.getFov() / (double)((Integer)this.field_1779.field_1690.method_41808().method_41753()).intValue();
            this.field_1789 *= slowdown;
            this.field_1787 *= slowdown;
        }
    }

    @WrapWithCondition(method={"method_1606"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_746;method_5872(DD)V")}, require=0, allow=1)
    private boolean modifyMouseRotationInput(class_746 instance, double cursorDeltaX, double cursorDeltaY) {
        if (SelfDestruct.isLocked()) {
            return true;
        }
        MouseRotationEvent event = new MouseRotationEvent((float)cursorDeltaX, (float)cursorDeltaY);
        EventManager.callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        instance.method_5872((double)event.getCursorDeltaX(), (double)event.getCursorDeltaY());
        return false;
    }
}

