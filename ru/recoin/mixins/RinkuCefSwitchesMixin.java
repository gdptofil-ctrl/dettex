package ru.recoin.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import ru.recoin.implement.features.modules.misc.browser.BrowserNet;

/**
 * Adds the Browser's proxy choice (Auto / Manual / Direct) to the switches Rinku starts Chromium
 * with. Pseudo + require 0: does nothing when Rinku is not installed.
 */
@Pseudo
@Mixin(targets = "de.keksuccino.rinku.util.CefUtil", remap = false)
public class RinkuCefSwitchesMixin {
    @ModifyArg(method = "init", at = @At(value = "INVOKE", target = "Lorg/cef/CefApp;startup([Ljava/lang/String;)Z", remap = false), index = 0, require = 0, remap = false)
    private static String[] dettex$proxyOnStartup(String[] switches) {
        return BrowserNet.cefSwitches(switches);
    }

    @ModifyArg(method = "init", at = @At(value = "INVOKE", target = "Lorg/cef/CefApp;getInstance([Ljava/lang/String;Lorg/cef/CefSettings;)Lorg/cef/CefApp;", remap = false), index = 0, require = 0, remap = false)
    private static String[] dettex$proxyOnInstance(String[] switches) {
        return BrowserNet.cefSwitches(switches);
    }
}
