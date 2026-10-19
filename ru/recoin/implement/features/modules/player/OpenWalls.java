/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1269
 *  net.minecraft.class_1297
 *  net.minecraft.class_1922
 *  net.minecraft.class_2199
 *  net.minecraft.class_2238
 *  net.minecraft.class_2260
 *  net.minecraft.class_2281
 *  net.minecraft.class_2304
 *  net.minecraft.class_2315
 *  net.minecraft.class_2325
 *  net.minecraft.class_2331
 *  net.minecraft.class_2336
 *  net.minecraft.class_2338
 *  net.minecraft.class_2377
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_243
 *  net.minecraft.class_2480
 *  net.minecraft.class_2531
 *  net.minecraft.class_259
 *  net.minecraft.class_265
 *  net.minecraft.class_2680
 *  net.minecraft.class_3708
 *  net.minecraft.class_3710
 *  net.minecraft.class_3716
 *  net.minecraft.class_3865
 *  net.minecraft.class_3959
 *  net.minecraft.class_3959$class_242
 *  net.minecraft.class_3959$class_3960
 *  net.minecraft.class_3965
 *  net.minecraft.class_746
 *  net.minecraft.class_8886
 */
package ru.recoin.implement.features.modules.player;

import java.util.Set;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1297;
import net.minecraft.class_1922;
import net.minecraft.class_2199;
import net.minecraft.class_2238;
import net.minecraft.class_2260;
import net.minecraft.class_2281;
import net.minecraft.class_2304;
import net.minecraft.class_2315;
import net.minecraft.class_2325;
import net.minecraft.class_2331;
import net.minecraft.class_2336;
import net.minecraft.class_2338;
import net.minecraft.class_2377;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2480;
import net.minecraft.class_2531;
import net.minecraft.class_259;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_3708;
import net.minecraft.class_3710;
import net.minecraft.class_3716;
import net.minecraft.class_3865;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_746;
import net.minecraft.class_8886;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.common.util.other.Instance;

public final class OpenWalls
extends Module {
    private static final Set<Class<?>> CONTAINER_BLOCKS = Set.of(class_2281.class, class_2531.class, class_2336.class, class_3708.class, class_2480.class, class_3865.class, class_3710.class, class_3716.class, class_2377.class, class_2315.class, class_2325.class, class_2260.class, class_2238.class, class_2199.class, class_2331.class, class_8886.class, class_2304.class);

    public OpenWalls() {
        super("OpenWalls", "Open Walls", ModuleCategory.PLAYER);
    }

    public static OpenWalls getInstance() {
        return Instance.get(OpenWalls.class);
    }

    public class_3965 raycast(class_746 player) {
        class_243 start = player.method_5836(1.0f);
        class_243 end = start.method_1019(player.method_5828(1.0f).method_1021(player.method_55754()));
        return OpenWalls.mc.field_1687.method_17742(new class_3959(start, end, class_3959.class_3960.field_17559, class_3959.class_242.field_1348, (class_1297)player){

            public class_265 method_17748(class_2680 state, class_1922 world, class_2338 pos) {
                for (Class<?> clazz : CONTAINER_BLOCKS) {
                    if (!clazz.isInstance(state.method_26204())) continue;
                    return super.method_17748(state, world, pos);
                }
                return class_259.method_1073();
            }
        });
    }

    public boolean tryOpen() {
        class_1269 result;
        if (!this.isState() || OpenWalls.mc.field_1724 == null || OpenWalls.mc.field_1687 == null || OpenWalls.mc.field_1761 == null) {
            return false;
        }
        class_3965 hit = this.raycast(OpenWalls.mc.field_1724);
        if (hit.method_17783() == class_239.class_240.field_1332 && (result = OpenWalls.mc.field_1761.method_2896(OpenWalls.mc.field_1724, class_1268.field_5808, hit)).method_23665()) {
            OpenWalls.mc.field_1724.method_6104(class_1268.field_5808);
            return true;
        }
        return false;
    }
}

