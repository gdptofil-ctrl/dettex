/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1291
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_1657
 *  net.minecraft.class_2246
 *  net.minecraft.class_2248
 *  net.minecraft.class_2338
 *  net.minecraft.class_238
 *  net.minecraft.class_243
 *  net.minecraft.class_2596
 *  net.minecraft.class_2680
 *  net.minecraft.class_2824
 *  net.minecraft.class_2828$class_2830
 *  net.minecraft.class_2828$class_2831
 *  net.minecraft.class_2848
 *  net.minecraft.class_2848$class_2849
 *  net.minecraft.class_304
 *  net.minecraft.class_3532
 *  net.minecraft.class_3675$class_307
 *  net.minecraft.class_4050
 *  net.minecraft.class_408
 *  net.minecraft.class_437
 *  net.minecraft.class_6880
 *  net.minecraft.class_7204
 *  org.lwjgl.glfw.GLFW
 */
package ru.recoin.common.util.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.class_1268;
import net.minecraft.class_1291;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_2680;
import net.minecraft.class_2824;
import net.minecraft.class_2828;
import net.minecraft.class_2848;
import net.minecraft.class_304;
import net.minecraft.class_3532;
import net.minecraft.class_3675;
import net.minecraft.class_4050;
import net.minecraft.class_408;
import net.minecraft.class_437;
import net.minecraft.class_6880;
import net.minecraft.class_7204;
import org.lwjgl.glfw.GLFW;
import ru.recoin.api.feature.module.setting.implement.BindSetting;
import ru.recoin.common.QuickImports;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.misc.HealthBypass;

public final class PlayerIntersectionUtil
implements QuickImports {
    public static void sendSequencedPacket(class_7204 packetCreator) {
        PlayerIntersectionUtil.mc.field_1761.method_41931(PlayerIntersectionUtil.mc.field_1687, packetCreator);
    }

    public static void interactItem(class_1268 hand) {
        PlayerIntersectionUtil.interactItem(hand, AngleUtil.cameraAngle());
    }

    public static void interactItem(class_1268 hand, Angle angle) {
        if (PlayerIntersectionUtil.mc.field_1724 == null || PlayerIntersectionUtil.mc.field_1761 == null) {
            return;
        }
        PlayerIntersectionUtil.mc.field_1761.method_2919((class_1657)PlayerIntersectionUtil.mc.field_1724, hand);
    }

    public static void interactItemVanilla(class_1268 hand) {
        if (PlayerIntersectionUtil.mc.field_1724 == null || PlayerIntersectionUtil.mc.field_1761 == null) {
            return;
        }
        PlayerIntersectionUtil.mc.field_1761.method_2919((class_1657)PlayerIntersectionUtil.mc.field_1724, hand);
    }

    public static void interactItemVanilla(class_1268 hand, Angle angle) {
        if (PlayerIntersectionUtil.mc.field_1724 == null || PlayerIntersectionUtil.mc.field_1761 == null) {
            return;
        }
        PlayerIntersectionUtil.mc.field_1724.field_3944.method_52787((class_2596)new class_2828.class_2831(angle.getYaw(), angle.getPitch(), PlayerIntersectionUtil.mc.field_1724.method_24828(), PlayerIntersectionUtil.mc.field_1724.field_5976));
        PlayerIntersectionUtil.mc.field_1761.method_2919((class_1657)PlayerIntersectionUtil.mc.field_1724, hand);
    }

    public static void interactEntity(class_1297 entity) {
        PlayerIntersectionUtil.mc.field_1724.field_3944.method_52787((class_2596)class_2824.method_34208((class_1297)entity, (boolean)false, (class_1268)class_1268.field_5808, (class_243)entity.method_5829().method_1005()));
        PlayerIntersectionUtil.mc.field_1724.field_3944.method_52787((class_2596)class_2824.method_34207((class_1297)entity, (boolean)false, (class_1268)class_1268.field_5808));
    }

    public static void startFallFlying() {
        PlayerIntersectionUtil.mc.field_1724.field_3944.method_52787((class_2596)new class_2848((class_1297)PlayerIntersectionUtil.mc.field_1724, class_2848.class_2849.field_12982));
        PlayerIntersectionUtil.mc.field_1724.method_23669();
    }

    public static void sendPacketWithOutEvent(class_2596<?> packet) {
        mc.method_1562().method_48296().method_10752(packet, null);
    }

    public static void grimSuperBypass$$$(double y, Angle angle) {
        PlayerIntersectionUtil.mc.field_1724.field_3944.method_52787((class_2596)new class_2828.class_2830(PlayerIntersectionUtil.mc.field_1724.method_23317(), PlayerIntersectionUtil.mc.field_1724.method_23318() + y, PlayerIntersectionUtil.mc.field_1724.method_23321(), angle.getYaw(), angle.getPitch(), PlayerIntersectionUtil.mc.field_1724.method_24828(), PlayerIntersectionUtil.mc.field_1724.field_5976));
    }

    public static String getHealthString(class_1309 entity) {
        return PlayerIntersectionUtil.getHealthString(PlayerIntersectionUtil.getHealth(entity));
    }

    public static String getHealthString(float hp) {
        if (hp < 0.0f) {
            return "Unknown";
        }
        return String.format("%.1f", Float.valueOf(hp)).replace(",", ".").replace(".0", "");
    }

    public static float getHealth(class_1309 entity) {
        if (entity == null) {
            return 0.0f;
        }
        HealthBypass healthBypass = HealthBypass.getInstance();
        if (healthBypass != null && healthBypass.enabled()) {
            if ((healthBypass.isScoreboardMode() || healthBypass.isScoreboardActive(entity)) && entity.method_5767()) {
                return -1.0f;
            }
            float resolved = healthBypass.resolveHealth(entity);
            if (resolved >= 0.0f) {
                return resolved;
            }
            if (healthBypass.isScoreboardMode()) {
                return -1.0f;
            }
        }
        return class_3532.method_15363((float)entity.method_6032(), (float)0.0f, (float)entity.method_6063()) + entity.method_6067();
    }

    public static void jump() {
        if (PlayerIntersectionUtil.mc.field_1724.method_5624()) {
            float g = PlayerIntersectionUtil.mc.field_1724.method_36454() * ((float)Math.PI / 180);
            PlayerIntersectionUtil.mc.field_1724.method_45319(new class_243((double)(-class_3532.method_15374((double)g) * 0.2f), 0.0, (double)(class_3532.method_15362((double)g) * 0.2f)));
        }
        PlayerIntersectionUtil.mc.field_1724.field_64356 = true;
    }

    public static List<class_2338> getCube(class_2338 center, float radius) {
        return PlayerIntersectionUtil.getCube(center, radius, radius, true);
    }

    public static List<class_2338> getCube(class_2338 center, float radiusXZ, float radiusY) {
        return PlayerIntersectionUtil.getCube(center, radiusXZ, radiusY, true);
    }

    public static List<class_2338> getCube(class_2338 center, float radiusXZ, float radiusY, boolean down) {
        ArrayList<class_2338> positions = new ArrayList<class_2338>();
        int centerX = center.method_10263();
        int centerY = center.method_10264();
        int centerZ = center.method_10260();
        int posY = down ? centerY - (int)radiusY : centerY;
        int x = centerX - (int)radiusXZ;
        while ((float)x <= (float)centerX + radiusXZ) {
            int z = centerZ - (int)radiusXZ;
            while ((float)z <= (float)centerZ + radiusXZ) {
                int y = posY;
                while ((float)y <= (float)centerY + radiusY) {
                    positions.add(new class_2338(x, y, z));
                    ++y;
                }
                ++z;
            }
            ++x;
        }
        return positions;
    }

    public static List<class_2338> getCube(class_2338 start, class_2338 end) {
        ArrayList<class_2338> positions = new ArrayList<class_2338>();
        for (int x = start.method_10263(); x <= end.method_10263(); ++x) {
            for (int z = start.method_10260(); z <= end.method_10260(); ++z) {
                for (int y = start.method_10264(); y <= end.method_10264(); ++y) {
                    positions.add(new class_2338(x, y, z));
                }
            }
        }
        return positions;
    }

    public static class_3675.class_307 getKeyType(int key) {
        return key < 8 ? class_3675.class_307.field_1672 : class_3675.class_307.field_1668;
    }

    public static Stream<class_1297> streamEntities() {
        return StreamSupport.stream(PlayerIntersectionUtil.mc.field_1687.method_18112().spliterator(), false);
    }

    public static boolean canChangeIntoPose(class_4050 pose, class_243 pos) {
        return PlayerIntersectionUtil.mc.field_1724.method_73183().method_8587((class_1297)PlayerIntersectionUtil.mc.field_1724, PlayerIntersectionUtil.mc.field_1724.method_18377(pose).method_30757(pos).method_1011(1.0E-7));
    }

    public static boolean isPotionActive(class_6880<class_1291> statusEffect) {
        return PlayerIntersectionUtil.mc.field_1724.method_6088().containsKey(statusEffect);
    }

    public static boolean isPlayerInBlock(class_2248 block) {
        return PlayerIntersectionUtil.isBoxInBlock(PlayerIntersectionUtil.mc.field_1724.method_5829().method_1014(-0.001), block);
    }

    public static boolean isBoxInBlock(class_238 box, class_2248 block) {
        return PlayerIntersectionUtil.isBox(box, pos -> PlayerIntersectionUtil.mc.field_1687.method_8320(pos).method_26204().equals(block));
    }

    public static boolean isBoxInBlocks(class_238 box, List<class_2248> blocks) {
        return PlayerIntersectionUtil.isBox(box, pos -> blocks.contains(PlayerIntersectionUtil.mc.field_1687.method_8320(pos).method_26204()));
    }

    public static boolean isBox(class_238 box, Predicate<class_2338> pos) {
        return class_2338.method_29715((class_238)box).anyMatch(pos);
    }

    public static boolean isKey(BindSetting setting) {
        int key = setting.getKey();
        return PlayerIntersectionUtil.mc.field_1755 == null && setting.isVisible() && PlayerIntersectionUtil.isKey(PlayerIntersectionUtil.getKeyType(key), key);
    }

    public static boolean isKey(class_304 key) {
        return PlayerIntersectionUtil.isKey(key.method_1429().method_1442(), key.method_1429().method_1444());
    }

    public static boolean isKey(class_3675.class_307 type, int keyCode) {
        if (keyCode != -1) {
            switch (type) {
                case field_1668: {
                    return GLFW.glfwGetKey((long)mc.method_22683().method_4490(), (int)keyCode) == 1;
                }
                case field_1672: {
                    return GLFW.glfwGetMouseButton((long)mc.method_22683().method_4490(), (int)keyCode) == 1;
                }
            }
        }
        return false;
    }

    public static boolean isAir(class_2338 blockPos) {
        return PlayerIntersectionUtil.isAir(PlayerIntersectionUtil.mc.field_1687.method_8320(blockPos));
    }

    public static boolean isAir(class_2680 state) {
        return state.method_26215() || state.method_26204().equals(class_2246.field_10543) || state.method_26204().equals(class_2246.field_10243);
    }

    public static boolean isChat(class_437 screen) {
        return screen instanceof class_408;
    }

    public static boolean nullCheck() {
        return PlayerIntersectionUtil.mc.field_1724 == null || PlayerIntersectionUtil.mc.field_1687 == null;
    }

    private PlayerIntersectionUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

