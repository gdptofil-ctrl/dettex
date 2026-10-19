/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11908
 *  net.minecraft.class_309
 *  net.minecraft.class_310
 *  net.minecraft.class_3675$class_307
 *  net.minecraft.class_437
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.recoin.mixins;

import net.minecraft.class_11908;
import net.minecraft.class_309;
import net.minecraft.class_310;
import net.minecraft.class_3675;
import net.minecraft.class_437;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.api.event.EventManager;
import ru.recoin.api.feature.module.Module;
import ru.recoin.core.Main;
import ru.recoin.implement.events.keyboard.KeyEvent;
import ru.recoin.implement.features.modules.misc.SelfDestruct;
import ru.recoin.implement.screens.menu.MenuScreen;

@Mixin(value={class_309.class})
public class KeyboardMixin {
    @Final
    @Shadow
    private class_310 field_1678;

    @Inject(method={"method_1466"}, at={@At(value="HEAD")})
    private void onKey(long window, int action, class_11908 input, CallbackInfo ci) {
        int key = input.comp_4795();
        if (key != -1 && window == this.field_1678.method_22683().method_4490()) {
            if (SelfDestruct.isLocked()) {
                return;
            }
            if (action == 0 && key == this.getClickGuiKey()) {
                if (this.field_1678.field_1755 == null) {
                    MenuScreen.INSTANCE.openGui();
                } else {
                    class_437 class_4372 = this.field_1678.field_1755;
                    if (class_4372 instanceof MenuScreen) {
                        MenuScreen menu = (MenuScreen)class_4372;
                        menu.closeSearchOrGui();
                    }
                }
            }
            EventManager.callEvent(new KeyEvent(this.field_1678.field_1755, class_3675.class_307.field_1668, key, action));
        }
    }

    private int getClickGuiKey() {
        Module module = Main.getInstance().getModuleProvider().get("ClickGUI");
        return module != null ? module.getKey() : -1;
    }
}

