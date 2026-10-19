/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_4587
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 */
package ru.recoin.implement.features.modules.render.cosmetics.render;

import net.minecraft.class_4587;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import ru.recoin.implement.features.modules.render.cosmetics.geo.GeoBone;
import ru.recoin.implement.features.modules.render.cosmetics.geo.GeoCube;

public class GeckoRenderHelper {
    private static final Vector3f POSITIVE_X = new Vector3f(1.0f, 0.0f, 0.0f);
    private static final Vector3f POSITIVE_Y = new Vector3f(0.0f, 1.0f, 0.0f);
    private static final Vector3f POSITIVE_Z = new Vector3f(0.0f, 0.0f, 1.0f);

    public static void translate(GeoBone bone, class_4587 stack) {
        stack.method_46416(-bone.getPositionX() / 16.0f, bone.getPositionY() / 16.0f, bone.getPositionZ() / 16.0f);
    }

    public static void moveToPivot(GeoBone bone, class_4587 stack) {
        stack.method_46416(bone.getPivotX() / 16.0f, bone.getPivotY() / 16.0f, bone.getPivotZ() / 16.0f);
    }

    public static void moveBackFromPivot(GeoBone bone, class_4587 stack) {
        stack.method_46416(-bone.getPivotX() / 16.0f, -bone.getPivotY() / 16.0f, -bone.getPivotZ() / 16.0f);
    }

    public static void rotate(GeoBone bone, class_4587 stack) {
        if (bone.getRotationZ() != 0.0f) {
            stack.method_22907((Quaternionfc)GeckoRenderHelper.getRadialQuaternion(POSITIVE_Z, bone.getRotationZ()));
        }
        if (bone.getRotationY() != 0.0f) {
            stack.method_22907((Quaternionfc)GeckoRenderHelper.getRadialQuaternion(POSITIVE_Y, bone.getRotationY()));
        }
        if (bone.getRotationX() != 0.0f) {
            stack.method_22907((Quaternionfc)GeckoRenderHelper.getRadialQuaternion(POSITIVE_X, bone.getRotationX()));
        }
    }

    public static void scale(GeoBone bone, class_4587 stack) {
        stack.method_22905(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());
    }

    public static void moveToPivot(GeoCube cube, class_4587 stack) {
        stack.method_46416(cube.pivot.getX() / 16.0f, cube.pivot.getY() / 16.0f, cube.pivot.getZ() / 16.0f);
    }

    public static void moveBackFromPivot(GeoCube cube, class_4587 stack) {
        stack.method_46416(-cube.pivot.getX() / 16.0f, -cube.pivot.getY() / 16.0f, -cube.pivot.getZ() / 16.0f);
    }

    public static void rotate(GeoCube cube, class_4587 stack) {
        if (cube.rotation.getZ() != 0.0f) {
            stack.method_22907((Quaternionfc)GeckoRenderHelper.getRadialQuaternion(POSITIVE_Z, cube.rotation.getZ()));
        }
        if (cube.rotation.getY() != 0.0f) {
            stack.method_22907((Quaternionfc)GeckoRenderHelper.getRadialQuaternion(POSITIVE_Y, cube.rotation.getY()));
        }
        if (cube.rotation.getX() != 0.0f) {
            stack.method_22907((Quaternionfc)GeckoRenderHelper.getRadialQuaternion(POSITIVE_X, cube.rotation.getX()));
        }
    }

    public static Quaternionf getRadialQuaternion(Vector3f axis, float angleRad) {
        float s = (float)Math.sin(angleRad / 2.0f);
        float x = axis.x() * s;
        float y = axis.y() * s;
        float z = axis.z() * s;
        float w = (float)Math.cos(angleRad / 2.0f);
        return new Quaternionf(x, y, z, w);
    }
}

