/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.MinecraftServer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.recoin.mixins;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.api.file.exception.FileProcessingException;
import ru.recoin.common.util.logger.LoggerUtil;
import ru.recoin.core.Main;

@Mixin(value={MinecraftServer.class})
public class MinecraftServerMixin {
    @Inject(method={"method_3782"}, at={@At(value="HEAD")})
    public void shutdown(CallbackInfo ci) {
        if (Main.getInstance().isInitialized()) {
            try {
                Main.getInstance().getFileController().saveFiles();
            }
            catch (FileProcessingException e) {
                LoggerUtil.error("Error occurred while saving files: " + e.getMessage());
            }
        }
    }
}

