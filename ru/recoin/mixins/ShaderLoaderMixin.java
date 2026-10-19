/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.class_10151
 *  net.minecraft.class_2960
 *  net.minecraft.class_3298
 *  net.minecraft.class_7367
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyArgs
 *  org.spongepowered.asm.mixin.injection.invoke.arg.Args
 */
package ru.recoin.mixins;

import java.nio.file.Path;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_10151;
import net.minecraft.class_2960;
import net.minecraft.class_3298;
import net.minecraft.class_7367;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value={class_10151.class})
public abstract class ShaderLoaderMixin {
    private static final String RECOIN_NAMESPACE = "insidethesystem";
    private static final Set<String> PROTECTED_MENU_SHADERS = Set.of("shaders/core/recoin_menu_background.vsh", "shaders/core/recoin_menu_background.fsh", "shaders/core/nebula_sky.fsh", "shaders/core/recoin_menu_glow.vsh", "shaders/core/recoin_menu_glow.fsh", "shaders/core/recoin_menu_logo.fsh");

    @ModifyArgs(method={"method_62942"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_10151;method_62939(Lnet/minecraft/class_2960;Lnet/minecraft/class_3298;Lcom/mojang/blaze3d/shaders/ShaderType;Ljava/util/Map;Lcom/google/common/collect/ImmutableMap$Builder;)V"))
    private void recoin$keepBundledMenuBackgroundShader(Args args2) {
        class_2960 id = (class_2960)args2.get(0);
        if (!RECOIN_NAMESPACE.equals(id.method_12836()) || !PROTECTED_MENU_SHADERS.contains(id.method_12832())) {
            return;
        }
        FabricLoader.getInstance().getModContainer(RECOIN_NAMESPACE).flatMap(container -> container.findPath("assets/" + id.method_12836() + "/" + id.method_12832())).ifPresent(path -> ShaderLoaderMixin.replaceResource(args2, path));
    }

    private static void replaceResource(Args args2, Path bundledShader) {
        class_3298 selectedResource = (class_3298)args2.get(1);
        args2.set(1, (Object)new class_3298(selectedResource.method_45304(), class_7367.create((Path)bundledShader)));
    }
}

