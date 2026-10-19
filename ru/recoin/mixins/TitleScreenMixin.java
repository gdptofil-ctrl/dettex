/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_437
 *  net.minecraft.class_442
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.recoin.mixins;

import net.minecraft.class_310;
import net.minecraft.class_437;
import net.minecraft.class_442;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.implement.features.modules.misc.SelfDestruct;
import ru.recoin.implement.screens.mainmenu.RecoinMainMenuScreen;

@Mixin(value={class_442.class})
public abstract class TitleScreenMixin {
    @Inject(method={"method_25426"}, at={@At(value="HEAD")}, cancellable=true)
    private void recoin$openMainMenu(CallbackInfo ci) {
        if (SelfDestruct.isLocked()) {
            return;
        }
        ci.cancel();
        class_310.method_1551().method_1507((class_437)new RecoinMainMenuScreen());
    }
}

