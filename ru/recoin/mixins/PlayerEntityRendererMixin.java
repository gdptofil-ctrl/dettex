/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10055
 *  net.minecraft.class_1007
 *  net.minecraft.class_11659
 *  net.minecraft.class_11890
 *  net.minecraft.class_12079$class_10726
 *  net.minecraft.class_12079$class_12081
 *  net.minecraft.class_1268
 *  net.minecraft.class_1297
 *  net.minecraft.class_1306
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_238
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_3532
 *  net.minecraft.class_3883
 *  net.minecraft.class_408
 *  net.minecraft.class_4587
 *  net.minecraft.class_5617$class_5618
 *  net.minecraft.class_591
 *  net.minecraft.class_630
 *  net.minecraft.class_742
 *  net.minecraft.class_7833
 *  net.minecraft.class_8685
 *  org.joml.Quaternionfc
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.recoin.mixins;

import net.minecraft.class_10055;
import net.minecraft.class_1007;
import net.minecraft.class_11659;
import net.minecraft.class_11890;
import net.minecraft.class_12079;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1306;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_238;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_3883;
import net.minecraft.class_408;
import net.minecraft.class_4587;
import net.minecraft.class_5617;
import net.minecraft.class_591;
import net.minecraft.class_630;
import net.minecraft.class_742;
import net.minecraft.class_7833;
import net.minecraft.class_8685;
import org.joml.Quaternionfc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.api.accessor.ICustomPlayerModelState;
import ru.recoin.api.accessor.IPitBikeRiderState;
import ru.recoin.api.repository.friend.FriendUtils;
import ru.recoin.common.util.render.ChinaHatFeatureRenderer;
import ru.recoin.common.util.render.PenisESPFeatureRenderer;
import ru.recoin.common.util.render.WaveyCapeRenderer;
import ru.recoin.implement.features.modules.movement.Speed;
import ru.recoin.implement.features.modules.movement.pitbike.PitBikeEntity;
import ru.recoin.implement.features.modules.render.ViewModel;
import ru.recoin.implement.features.modules.render.cosmetics.Cosmetics;
import ru.recoin.implement.features.modules.render.cosmetics.sync.DettexBadge;
import ru.recoin.implement.features.modules.render.cosmetics.render.CosmeticFeatureRenderer;
import ru.recoin.mixins.LivingEntityRendererAccessor;

@Mixin(value={class_1007.class})
public class PlayerEntityRendererMixin {
    @Unique
    private static final class_12079.class_10726 RECOIN_CAPE = new class_12079.class_10726(class_2960.method_60655((String)"minecraft", (String)"capes/recoin"));

    @Inject(method={"method_23205(Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;ILnet/minecraft/class_2960;Lnet/minecraft/class_630;Z)V"}, at={@At(value="TAIL")})
    private void recoin$captureArmGeometry(class_4587 matrices, class_11659 queue, int light, class_2960 texture, class_630 arm, boolean sleeve, CallbackInfo ci) {
        ViewModel viewModel = ViewModel.getInstance();
        class_310 client = class_310.method_1551();
        if (viewModel == null || !viewModel.isState() || client.field_1724 == null || !(client.field_1755 instanceof class_408)) {
            return;
        }
        class_591 model = (class_591)((class_1007)(Object)this).method_4038();
        boolean right = arm == model.field_3401;
        class_1268 hand = right == (client.field_1724.method_6068() == class_1306.field_6183) ? class_1268.field_5808 : class_1268.field_5810;
        arm.method_35745(matrices, (entry, path, index, cuboid) -> viewModel.getHandGeometry().capture(hand, entry.method_23761(), new class_238((double)cuboid.field_3645 / 16.0, (double)cuboid.field_3644 / 16.0, (double)cuboid.field_3643 / 16.0, (double)cuboid.field_3648 / 16.0, (double)cuboid.field_3647 / 16.0, (double)cuboid.field_3646 / 16.0)));
    }

    @Inject(method={"<init>"}, at={@At(value="TAIL")})
    private void recoin$addChinaHatFeature(class_5617.class_5618 ctx, boolean slim, CallbackInfo ci) {
        class_1007 renderer = (class_1007)(Object)this;
        ((LivingEntityRendererAccessor)renderer).recoin$addFeature(new ChinaHatFeatureRenderer((class_3883<class_10055, class_591>)renderer));
        ((LivingEntityRendererAccessor)renderer).recoin$addFeature(new PenisESPFeatureRenderer((class_3883<class_10055, class_591>)renderer));
        ((LivingEntityRendererAccessor)renderer).recoin$addFeature(new CosmeticFeatureRenderer((class_3883<class_10055, class_591>)renderer));
    }

    @Inject(method={"method_62604(Lnet/minecraft/class_11890;Lnet/minecraft/class_10055;F)V"}, at={@At(value="TAIL")})
    private void recoin$applyCapeTextures(class_11890 entity, class_10055 state, float tickDelta, CallbackInfo ci) {
        class_742 player;
        IPitBikeRiderState bikeState = (IPitBikeRiderState)state;
        class_1297 class_12972 = entity.method_5854();
        if (class_12972 instanceof PitBikeEntity) {
            PitBikeEntity bike = (PitBikeEntity)class_12972;
            bikeState.recoin$setBikePose(true, bike.getWheelieAngle(tickDelta) + bike.getTerrainPitch(tickDelta), bike.getLeanAngle(tickDelta), bike.getSteeringAngle(tickDelta), bike.getSuspension(tickDelta));
            state.field_53446 = bike.method_61415(tickDelta);
            state.field_53447 = class_3532.method_15363((float)class_3532.method_15393((float)(entity.method_61415(tickDelta) - state.field_53446)), (float)-55.0f, (float)55.0f);
            state.field_53410 = false;
            state.field_53412 = false;
            state.field_53411 = false;
            state.field_53451 = 0.0f;
        } else {
            bikeState.recoin$setBikePose(false, 0.0f, 0.0f, 0.0f, 0.0f);
        }
        Cosmetics cosmetics = Cosmetics.getInstance();
        String customModel = cosmetics == null ? null : cosmetics.customModelFor(entity);
        ((ICustomPlayerModelState)state).recoin$setCustomModel(customModel != null, customModel != null ? customModel : "");
        state.field_53337 = DettexBadge.decorate(entity.method_5667(), state.field_53337);
        if (entity == class_310.method_1551().field_1724 && Speed.getInstance() != null && Speed.getInstance().isFunTimeElytraMode()) {
            state.field_53411 = false;
            state.field_53534 = 0.0f;
            state.field_53403 = 0.0f;
            state.field_53412 = false;
            state.field_53410 = false;
            state.field_53535 = false;
            state.field_53521 = 0.0f;
            if (state.field_53418.method_31574(class_1802.field_8833)) {
                state.field_53418 = class_1799.field_8037;
            }
        }
        if (!(entity instanceof class_742)) {
            return;
        }
        player = (class_742)entity;
        class_12079.class_10726 selectedCape = PlayerEntityRendererMixin.recoin$selectedCape(player);
        if (selectedCape == null) {
            return;
        }
        class_8685 original = state.field_53520;
        if (original != null) {
            state.field_53520 = new class_8685(original.comp_1626(), (class_12079.class_12081)selectedCape, (class_12079.class_12081)selectedCape, original.comp_1629(), original.comp_1630());
        }
        state.field_53532 = true;
        WaveyCapeRenderer.update(player, tickDelta);
    }

    @Unique
    private static class_12079.class_10726 recoin$selectedCape(class_742 player) {
        Cosmetics cosmetics = Cosmetics.getInstance();
        if (cosmetics == null) {
            return null;
        }
        class_310 client = class_310.method_1551();
        boolean self = client.field_1724 != null && player.method_5667().equals(client.field_1724.method_5667());
        String name = player.method_5477() == null ? null : player.method_5477().getString();
        return cosmetics.getCapeFor(self, player.method_5667(), name);
    }

    @Inject(method={"method_4212(Lnet/minecraft/class_10055;Lnet/minecraft/class_4587;FF)V"}, at={@At(value="TAIL")})
    private void recoin$bikeRiderTransforms(class_10055 state, class_4587 matrices, float bodyYaw, float scale, CallbackInfo ci) {
        IPitBikeRiderState bike = (IPitBikeRiderState)state;
        if (!bike.recoin$isBikeRider()) {
            return;
        }
        matrices.method_22907((Quaternionfc)class_7833.field_40714.rotationDegrees(bike.recoin$getBikePitch() * 0.65f));
        matrices.method_22907((Quaternionfc)class_7833.field_40718.rotationDegrees(-bike.recoin$getBikeLean() * 0.85f));
    }
}

