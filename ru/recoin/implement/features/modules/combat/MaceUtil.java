/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1294
 *  net.minecraft.class_1297
 *  net.minecraft.class_1802
 *  net.minecraft.class_1937
 *  net.minecraft.class_2246
 *  net.minecraft.class_2338
 *  net.minecraft.class_238
 *  net.minecraft.class_243
 *  net.minecraft.class_2680
 *  net.minecraft.class_310
 *  net.minecraft.class_3486
 *  net.minecraft.class_746
 */
package ru.recoin.implement.features.modules.combat;

import java.util.List;
import java.util.Optional;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1802;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_3486;
import net.minecraft.class_746;

public final class MaceUtil {
    private static final double DRAG = 0.9799995613681012;

    private MaceUtil() {
    }

    public static boolean isHoldingMace() {
        class_310 client = class_310.method_1551();
        return client.field_1724 != null && client.field_1724.method_6047().method_31574(class_1802.field_49814);
    }

    public static Optional<class_243> predictLanding(class_746 player, class_1937 world) {
        if (world == null || player.method_24828() || player.method_31549().field_7479 || player.method_6128() || player.method_6101() || player.method_5681() || player.method_6059(class_1294.field_5906) || player.method_5799() || player.method_5771()) {
            return Optional.empty();
        }
        class_243 position = new class_243(player.method_23317(), player.method_23318(), player.method_23321());
        class_243 velocity = player.method_18798();
        class_238 box = player.method_5829();
        for (int tick = 0; tick < 400; ++tick) {
            velocity = new class_243(velocity.field_1352 * 0.9799995613681012, (velocity.field_1351 - 0.08) * 0.9799995613681012, velocity.field_1350 * 0.9799995613681012);
            class_243 movement = class_1297.method_20736((class_1297)player, (class_243)velocity, (class_238)box, (class_1937)world, (List)world.method_20743((class_1297)player, box.method_18804(velocity)));
            if (velocity.field_1351 < 0.0 && movement.field_1351 > velocity.field_1351) {
                return Optional.of(position.method_1019(movement));
            }
            position = position.method_1019(movement);
            if (MaceUtil.intersectsFluid(world, box = box.method_997(movement))) {
                return Optional.of(position);
            }
            velocity = new class_243(movement.field_1352 == velocity.field_1352 ? velocity.field_1352 : 0.0, movement.field_1351 == velocity.field_1351 ? velocity.field_1351 : 0.0, movement.field_1350 == velocity.field_1350 ? velocity.field_1350 : 0.0);
            if (velocity.method_1027() < 1.0E-7) break;
        }
        return Optional.empty();
    }

    public static boolean landsInHazardSoon(class_746 player, class_1937 world) {
        class_243 velocity = player.method_18798();
        class_238 box = player.method_5829();
        for (int tick = 0; tick < 2; ++tick) {
            velocity = new class_243(velocity.field_1352 * 0.9799995613681012, (velocity.field_1351 - 0.08) * 0.9799995613681012, velocity.field_1350 * 0.9799995613681012);
            class_243 movement = class_1297.method_20736((class_1297)player, (class_243)velocity, (class_238)box, (class_1937)world, (List)world.method_20743((class_1297)player, box.method_18804(velocity)));
            class_238 moved = box.method_997(movement);
            if (movement.field_1351 > velocity.field_1351 || MaceUtil.intersectsHazard(world, moved)) {
                return true;
            }
            box = moved;
        }
        return false;
    }

    private static boolean intersectsFluid(class_1937 world, class_238 box) {
        return class_2338.method_29715((class_238)box.method_1011(1.0E-7)).map(arg_0 -> ((class_1937)world).method_8316(arg_0)).anyMatch(state -> state.method_15767(class_3486.field_15517) || state.method_15767(class_3486.field_15518));
    }

    private static boolean intersectsHazard(class_1937 world, class_238 box) {
        return class_2338.method_29715((class_238)box.method_1011(1.0E-7)).anyMatch(pos -> {
            class_2680 state = world.method_8320(pos);
            return state.method_27852(class_2246.field_10343) || state.method_27852(class_2246.field_27879) || state.method_26227().method_15767(class_3486.field_15517) || state.method_26227().method_15767(class_3486.field_15518);
        });
    }
}

