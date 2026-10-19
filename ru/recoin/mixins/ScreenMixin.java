/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.recoin.mixins;

import net.minecraft.class_332;
import net.minecraft.class_437;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.implement.screens.autobuy.AutoBuyScreen;
import ru.recoin.implement.screens.autoinventory.AutoInventoryScreen;
import ru.recoin.implement.screens.inventorymanager.InventoryManagerScreen;
import ru.recoin.implement.screens.menu.MenuScreen;

@Mixin(value={class_437.class})
public class ScreenMixin {
    @Inject(method={"method_25420"}, at={@At(value="HEAD")}, cancellable=true)
    private void disableBackgroundBlurAndDimmingForMenu(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if ((Object)this instanceof MenuScreen || (Object)this instanceof AutoBuyScreen || (Object)this instanceof AutoInventoryScreen || (Object)this instanceof InventoryManagerScreen) {
            ci.cancel();
        }
    }
}

