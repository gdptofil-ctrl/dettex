/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1657
 *  net.minecraft.class_1922
 *  net.minecraft.class_1937
 *  net.minecraft.class_2338
 *  net.minecraft.class_2374
 *  net.minecraft.class_243
 *  net.minecraft.class_2561
 *  net.minecraft.class_2828$class_2829
 *  net.minecraft.class_3532
 *  net.minecraft.class_638
 */
package ru.recoin.common.util.world;

import net.minecraft.class_1657;
import net.minecraft.class_1922;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2374;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2828;
import net.minecraft.class_3532;
import net.minecraft.class_638;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;

public class FlightTeleportController
implements QuickImports {
    private static boolean active;
    private static boolean underground;
    private static boolean finishing;
    private static int targetX;
    private static int targetZ;
    private static int waitTicks;
    private static class_243 pendingPosition;
    private static class_638 teleportWorld;
    private static boolean silent;

    private static void message(String value) {
        if (FlightTeleportController.mc.field_1724 != null) {
            FlightTeleportController.mc.field_1724.method_7353((class_2561)class_2561.method_43470((String)value), false);
        }
    }

    private static int findSurfaceY() {
        for (int index = FlightTeleportController.mc.field_1687.method_31605() - 1; index >= 0; --index) {
            if (!FlightTeleportController.hasCollision(new class_2338(targetX, index, targetZ))) continue;
            return index;
        }
        return Integer.MIN_VALUE;
    }

    public static void tick() {
        if (active) {
            if (FlightTeleportController.mc.field_1724 == null || FlightTeleportController.mc.field_1687 == null || FlightTeleportController.mc.field_1687 != teleportWorld) {
                FlightTeleportController.reset();
            } else if (!FlightTeleportController.mc.field_1724.method_31549().field_7479) {
                FlightTeleportController.reset();
            } else if (waitTicks > 0) {
                --waitTicks;
            } else {
                if (pendingPosition != null) {
                    if (!FlightTeleportController.mc.field_1724.method_24515().equals((Object)class_2338.method_49638((class_2374)pendingPosition))) {
                        FlightTeleportController.mc.field_1724.method_5814(FlightTeleportController.pendingPosition.field_1352, FlightTeleportController.pendingPosition.field_1351, FlightTeleportController.pendingPosition.field_1350);
                        PlayerIntersectionUtil.sendPacketWithOutEvent(new class_2828.class_2829(FlightTeleportController.pendingPosition.field_1352, FlightTeleportController.pendingPosition.field_1351, FlightTeleportController.pendingPosition.field_1350, false, FlightTeleportController.mc.field_1724.field_5976));
                        waitTicks = 2;
                        return;
                    }
                    pendingPosition = null;
                    if (finishing) {
                        if (!silent) {
                            FlightTeleportController.message("\u0422\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0430\u0446\u0438\u044f \u0437\u0430\u0432\u0435\u0440\u0448\u0435\u043d\u0430: " + targetX + " " + targetZ);
                        }
                        FlightTeleportController.reset();
                        return;
                    }
                    underground = true;
                }
                if (FlightTeleportController.isChunkLoaded((double)targetX + 0.5, (double)targetZ + 0.5)) {
                    int count = FlightTeleportController.findSurfaceY();
                    if (count == Integer.MIN_VALUE) {
                        FlightTeleportController.message("\u041d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d \u0431\u043b\u043e\u043a \u043d\u0430 \u043a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u0430\u0445 " + targetX + " " + targetZ);
                        FlightTeleportController.reset();
                    } else {
                        finishing = true;
                        FlightTeleportController.moveTo((double)targetX + 0.5, count, (double)targetZ + 0.5);
                    }
                } else if (!underground) {
                    FlightTeleportController.moveTo(FlightTeleportController.mc.field_1724.method_23317(), 1.0, FlightTeleportController.mc.field_1724.method_23321());
                } else {
                    double distance7;
                    double distance = FlightTeleportController.mc.field_1724.method_23317();
                    double distance2 = FlightTeleportController.mc.field_1724.method_23318();
                    double distance4 = (double)targetX + 0.5 - distance;
                    double distance3 = FlightTeleportController.mc.field_1724.method_23321();
                    double distance5 = (double)targetZ + 0.5 - distance3;
                    double distance6 = Math.hypot(distance4, distance5);
                    if (!(distance6 < 1.0) && !((distance7 = FlightTeleportController.getLoadedTravelDistance(distance, distance3, distance4, distance5, distance6)) <= 1.0)) {
                        FlightTeleportController.moveTo(distance + distance4 / distance6 * distance7, distance2, distance3 + distance5 / distance6 * distance7);
                    }
                }
            }
        }
    }

    private static boolean hasCollision(class_2338 position) {
        return FlightTeleportController.mc.field_1687 == null ? false : !FlightTeleportController.mc.field_1687.method_8320(position).method_26220((class_1922)FlightTeleportController.mc.field_1687, position).method_1110();
    }

    private static void moveTo(double distance, double distance2, double distance3) {
        pendingPosition = new class_243(distance, distance2, distance3);
        FlightTeleportController.mc.field_1724.method_5814(distance, distance2, distance3);
        PlayerIntersectionUtil.sendPacketWithOutEvent(new class_2828.class_2829(distance, distance2, distance3, false, FlightTeleportController.mc.field_1724.field_5976));
        waitTicks = 2;
    }

    public static void reset() {
        active = false;
        underground = false;
        finishing = false;
        pendingPosition = null;
        teleportWorld = null;
        silent = false;
        waitTicks = 0;
    }

    private static boolean isChunkLoaded(double distance, double distance2) {
        int count = class_3532.method_15357((double)distance) >> 4;
        int count2 = class_3532.method_15357((double)distance2) >> 4;
        return FlightTeleportController.mc.field_1687.method_2935().method_12246(count, count2) != null;
    }

    public static void teleportTo(int count, int count2, boolean flag) {
        if (FlightTeleportController.mc.field_1724 != null && FlightTeleportController.mc.field_1687 != null) {
            FlightTeleportController.reset();
            if (!FlightTeleportController.mc.field_1724.method_31549().field_7479) {
                FlightTeleportController.message("\u0412\u044b \u043d\u0435 \u0432 \u0440\u0435\u0436\u0438\u043c\u0435 \u043f\u043e\u043b\u0435\u0442\u0430, \u0442\u0435\u043b\u0435\u043f\u043e\u0440\u0442 \u043d\u0435\u0432\u043e\u0437\u043c\u043e\u0436\u0435\u043d");
            } else if (!class_1937.method_25953((class_2338)new class_2338(count, 1, count2))) {
                FlightTeleportController.message("\u041a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u044b \u043d\u0430\u0445\u043e\u0434\u044f\u0442\u0441\u044f \u0437\u0430 \u0433\u0440\u0430\u043d\u0438\u0446\u0435\u0439 \u043c\u0438\u0440\u0430");
            } else {
                targetX = count;
                targetZ = count2;
                waitTicks = 0;
                pendingPosition = null;
                underground = false;
                finishing = false;
                teleportWorld = FlightTeleportController.mc.field_1687;
                silent = flag;
                active = true;
                if (!flag) {
                    FlightTeleportController.message("\u0422\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0430\u0446\u0438\u044f \u043d\u0430 " + count + " " + count2);
                }
            }
        }
    }

    public static void teleport(int count, int count2) {
        FlightTeleportController.teleportTo(count, count2, false);
    }

    private static double getLoadedTravelDistance(double distance, double distance2, double distance3, double distance4, double distance5) {
        double distance6 = distance3 / distance5;
        double distance7 = distance4 / distance5;
        double distance8 = 0.0;
        for (double distance9 = 0.0; distance9 <= distance5 && FlightTeleportController.isChunkLoaded(distance + distance6 * distance9, distance2 + distance7 * distance9); distance9 += 1.0) {
        }
        return distance8;
    }

    public static boolean isActive() {
        return active;
    }

    public static void teleportToPlayer(String text) {
        if (FlightTeleportController.mc.field_1724 != null && FlightTeleportController.mc.field_1687 != null) {
            FlightTeleportController.reset();
            if (!FlightTeleportController.mc.field_1724.method_31549().field_7479) {
                FlightTeleportController.message("\u0412\u044b \u043d\u0435 \u0432 \u0440\u0435\u0436\u0438\u043c\u0435 \u043f\u043e\u043b\u0435\u0442\u0430, \u0442\u0435\u043b\u0435\u043f\u043e\u0440\u0442 \u043d\u0435\u0432\u043e\u0437\u043c\u043e\u0436\u0435\u043d");
            } else {
                class_1657 player2 = FlightTeleportController.mc.field_1687.method_18456().stream().filter(target -> target.method_5477().getString().equals(text)).findFirst().orElse(null);
                if (player2 == null) {
                    FlightTeleportController.message("\u0418\u0433\u0440\u043e\u043a \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d");
                } else {
                    class_243 position = player2.method_73189();
                    int count = (int)Math.floor(position.field_1352);
                    int count2 = (int)Math.floor(position.field_1350);
                    int count3 = (int)Math.floor(position.field_1351);
                    if (FlightTeleportController.hasCollision(player2.method_24515().method_10084())) {
                        PlayerIntersectionUtil.sendPacketWithOutEvent(new class_2828.class_2829(position.field_1352, position.field_1351, position.field_1350, false, FlightTeleportController.mc.field_1724.field_5976));
                        FlightTeleportController.message("\u041f\u043e\u043f\u044b\u0442\u043a\u0430 \u0442\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0430\u0446\u0438\u0438 \u043a " + text);
                    } else {
                        for (int index = count3; index > 0; --index) {
                            class_2338 position2 = new class_2338(count, index, count2);
                            if (!FlightTeleportController.hasCollision(position2)) continue;
                            PlayerIntersectionUtil.sendPacketWithOutEvent(new class_2828.class_2829((double)count + 0.5, (double)index, (double)count2 + 0.5, false, FlightTeleportController.mc.field_1724.field_5976));
                            FlightTeleportController.message("\u041f\u043e\u043f\u044b\u0442\u043a\u0430 \u0442\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0430\u0446\u0438\u0438 \u043a " + text);
                            return;
                        }
                        FlightTeleportController.message("\u041d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d \u0431\u043b\u043e\u043a \u043f\u043e\u0434 \u0438\u0433\u0440\u043e\u043a\u043e\u043c " + text);
                    }
                }
            }
        }
    }
}

