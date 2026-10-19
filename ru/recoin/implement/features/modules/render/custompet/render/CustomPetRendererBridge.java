/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10017
 *  net.minecraft.class_10201
 *  net.minecraft.class_10442
 *  net.minecraft.class_11697
 *  net.minecraft.class_11786
 *  net.minecraft.class_1297
 *  net.minecraft.class_310
 *  net.minecraft.class_327
 *  net.minecraft.class_330
 *  net.minecraft.class_5599
 *  net.minecraft.class_5617$class_5618
 *  net.minecraft.class_776
 *  net.minecraft.class_897
 *  net.minecraft.class_898
 */
package ru.recoin.implement.features.modules.render.custompet.render;

import java.util.function.Supplier;
import net.minecraft.class_10017;
import net.minecraft.class_10201;
import net.minecraft.class_10442;
import net.minecraft.class_11697;
import net.minecraft.class_11786;
import net.minecraft.class_1297;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_330;
import net.minecraft.class_5599;
import net.minecraft.class_5617;
import net.minecraft.class_776;
import net.minecraft.class_897;
import net.minecraft.class_898;
import ru.recoin.implement.features.modules.render.custompet.entity.CustomPetEntity;
import ru.recoin.implement.features.modules.render.custompet.render.CustomPetRenderState;
import ru.recoin.implement.features.modules.render.custompet.render.CustomPetRenderer;

public final class CustomPetRendererBridge {
    private static class_310 minecraft;
    private static class_898 dispatcher;
    private static class_776 blockRenderDispatcher;
    private static class_10442 itemModelResolver;
    private static class_330 mapRenderer;
    private static class_327 font;
    private static Supplier<class_5599> modelSetSupplier;
    private static class_10201 equipmentAssetManager;
    private static class_11697 atlasManager;
    private static class_11786 playerSkinRenderCache;
    private static class_897<? super CustomPetEntity, ?> renderer;

    private CustomPetRendererBridge() {
    }

    public static void bootstrap(class_898 entityRenderManager, class_310 minecraftClient, class_776 blockRenderManager, class_10442 itemModelManager, class_330 mapRenderer, class_11697 atlasManager, class_327 textRenderer, Supplier<class_5599> supplier, class_10201 equipmentModelLoader, class_11786 playerSkinCache) {
        dispatcher = entityRenderManager;
        minecraft = minecraftClient;
        blockRenderDispatcher = blockRenderManager;
        itemModelResolver = itemModelManager;
        CustomPetRendererBridge.mapRenderer = mapRenderer;
        CustomPetRendererBridge.atlasManager = atlasManager;
        font = textRenderer;
        modelSetSupplier = supplier;
        equipmentAssetManager = equipmentModelLoader;
        playerSkinRenderCache = playerSkinCache;
        renderer = null;
    }

    public static void reload() {
        renderer = null;
    }

    private static class_897 getOrCreateRenderer() {
        if (renderer == null) {
            class_5617.class_5618 context = new class_5617.class_5618(dispatcher, itemModelResolver, mapRenderer, blockRenderDispatcher, minecraft.method_1478(), modelSetSupplier.get(), equipmentAssetManager, atlasManager, font, playerSkinRenderCache);
            renderer = new CustomPetRenderer(context);
        }
        return renderer;
    }

    public static <S extends class_10017> class_897<?, ? super S> getCustomRenderer(S s) {
        if (dispatcher == null || minecraft == null) {
            return null;
        }
        if (s instanceof CustomPetRenderState) {
            return CustomPetRendererBridge.getOrCreateRenderer();
        }
        return null;
    }

    public static <T extends class_1297> class_897<? super T, ?> getCustomRenderer(T t) {
        if (dispatcher == null || minecraft == null) {
            return null;
        }
        if (t instanceof CustomPetEntity) {
            return CustomPetRendererBridge.getOrCreateRenderer();
        }
        return null;
    }
}

