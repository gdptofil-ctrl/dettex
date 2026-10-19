/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11621
 *  net.minecraft.class_11630
 *  net.minecraft.class_1297
 *  net.minecraft.class_1923
 *  net.minecraft.class_1937
 *  net.minecraft.class_2338
 *  net.minecraft.class_2374
 *  net.minecraft.class_243
 *  net.minecraft.class_2818
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_3532
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.recoin.mixins;

import java.util.List;
import java.util.Locale;
import net.minecraft.class_11621;
import net.minecraft.class_11630;
import net.minecraft.class_1297;
import net.minecraft.class_1923;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2374;
import net.minecraft.class_243;
import net.minecraft.class_2818;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.implement.features.modules.misc.FreeCam;

@Mixin(value={class_11621.class})
public class PlayerPositionDebugHudEntryMixin {
    @Inject(method={"method_72751"}, at={@At(value="HEAD")}, cancellable=true)
    private void recoin$freeCamCoordinates(class_11630 lines, class_1937 world, class_2818 clientChunk, class_2818 serverChunk, CallbackInfo ci) {
        FreeCam freeCam = FreeCam.getInstance();
        if (freeCam == null || !freeCam.isState() || freeCam.pos == null) {
            return;
        }
        class_310 client = class_310.method_1551();
        class_1297 camera = client.method_1560();
        class_243 pos = freeCam.pos;
        class_2338 block = class_2338.method_49638((class_2374)pos);
        class_1923 chunk = new class_1923(block);
        String facing = camera == null ? "Unknown" : camera.method_5735().toString();
        float yaw = camera == null ? 0.0f : class_3532.method_15393((float)camera.method_36454());
        float pitch = camera == null ? 0.0f : class_3532.method_15393((float)camera.method_36455());
        lines.method_72744(class_2960.method_60656((String)"position"), List.of(String.format(Locale.ROOT, "XYZ: %.3f / %.5f / %.3f", pos.field_1352, pos.field_1351, pos.field_1350), String.format(Locale.ROOT, "Block: %d %d %d", block.method_10263(), block.method_10264(), block.method_10260()), String.format(Locale.ROOT, "Chunk: %d %d %d", chunk.field_9181, block.method_10264() >> 4, chunk.field_9180), String.format(Locale.ROOT, "Facing: %s (%.1f / %.1f)", facing, Float.valueOf(yaw), Float.valueOf(pitch)), String.valueOf(world.method_27983().method_29177())));
        ci.cancel();
    }
}

