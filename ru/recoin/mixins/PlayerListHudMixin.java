/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 *  net.minecraft.class_1297
 *  net.minecraft.class_1657
 *  net.minecraft.class_2561
 *  net.minecraft.class_266
 *  net.minecraft.class_269
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_3532
 *  net.minecraft.class_355
 *  net.minecraft.class_5250
 *  net.minecraft.class_640
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.recoin.mixins;

import net.minecraft.class_124;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_2561;
import net.minecraft.class_266;
import net.minecraft.class_269;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_355;
import net.minecraft.class_5250;
import net.minecraft.class_640;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.recoin.api.repository.friend.FriendUtils;
import ru.recoin.implement.features.modules.misc.SelfDestruct;
import ru.recoin.implement.features.modules.render.UiAnimationManager;

@Mixin(value={class_355.class})
public abstract class PlayerListHudMixin {
    @Unique
    private static final String[] RECOIN_DIRECTIONS = new String[]{"\u2191", "\u2197", "\u2192", "\u2198", "\u2193", "\u2199", "\u2190", "\u2196"};
    @Shadow
    private boolean field_2158;
    @Unique
    private boolean recoin$tabAnimPushed;

    @Inject(method={"method_1918"}, at={@At(value="RETURN")}, cancellable=true)
    private void recoin$decorateNearbyPlayer(class_640 entry, CallbackInfoReturnable<class_2561> cir) {
        double distanceSquared;
        if (SelfDestruct.isLocked()) {
            return;
        }
        class_310 client = class_310.method_1551();
        if (client.field_1724 == null || client.field_1687 == null) {
            return;
        }
        class_5250 result = class_2561.method_43473();
        if (FriendUtils.isFriend(entry.method_2966().name())) {
            result.method_10852((class_2561)class_2561.method_43470((String)"Friend ").method_27692(class_124.field_1060));
        }
        result.method_10852((class_2561)((class_2561)cir.getReturnValue()).method_27661());
        class_1657 player = client.field_1687.method_18470(entry.method_2966().id());
        if (player != null && player != client.field_1724 && player.method_5805() && !player.method_7325() && (distanceSquared = player.method_5858((class_1297)client.field_1724)) <= 10000.0) {
            double dx = player.method_23317() - client.field_1724.method_23317();
            double dz = player.method_23321() - client.field_1724.method_23321();
            float targetYaw = (float)Math.toDegrees(Math.atan2(-dx, dz));
            int index = Math.floorMod(Math.round(class_3532.method_15393((float)(targetYaw - client.field_1724.method_36454())) / 45.0f), 8);
            result.method_10852((class_2561)class_2561.method_43470((String)("  \u0420\u044f\u0434\u043e\u043c " + RECOIN_DIRECTIONS[index] + " " + Math.round(Math.sqrt(distanceSquared)) + "\u043c")).method_27692(class_124.field_1054));
        }
        cir.setReturnValue(result);
    }

    @Inject(method={"method_1921"}, at={@At(value="TAIL")})
    private void recoin$tabVisible(boolean visible, CallbackInfo ci) {
        UiAnimationManager.onTabVisible(visible);
    }

    @Inject(method={"method_1919"}, at={@At(value="HEAD")})
    private void recoin$tabAnimHead(class_332 context, int scaledWindowWidth, class_269 scoreboard, @Nullable class_266 objective, CallbackInfo ci) {
        if (!UiAnimationManager.shouldApplyTabTransform(this.field_2158)) {
            return;
        }
        this.recoin$tabAnimPushed = UiAnimationManager.applyTabTransform(context.method_51448());
    }

    @Inject(method={"method_1919"}, at={@At(value="RETURN")})
    private void recoin$tabAnimReturn(class_332 context, int scaledWindowWidth, class_269 scoreboard, @Nullable class_266 objective, CallbackInfo ci) {
        if (!this.recoin$tabAnimPushed) {
            return;
        }
        UiAnimationManager.endTabTransform(context.method_51448());
        this.recoin$tabAnimPushed = false;
    }
}

