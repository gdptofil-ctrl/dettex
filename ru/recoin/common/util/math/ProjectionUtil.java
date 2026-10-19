/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_2338
 *  net.minecraft.class_2374
 *  net.minecraft.class_238
 *  net.minecraft.class_243
 *  net.minecraft.class_3532
 *  net.minecraft.class_4184
 *  net.minecraft.class_4604
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector4d
 *  org.joml.Vector4f
 */
package ru.recoin.common.util.math;

import net.minecraft.class_1297;
import net.minecraft.class_2338;
import net.minecraft.class_2374;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_4184;
import net.minecraft.class_4604;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector4d;
import org.joml.Vector4f;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.render.Render3DUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;

public final class ProjectionUtil
implements QuickImports {
    private static final ThreadLocal<Quaternionf> ROTATION_CACHE = ThreadLocal.withInitial(Quaternionf::new);
    private static final ThreadLocal<Vector4f> CLIP_CACHE = ThreadLocal.withInitial(Vector4f::new);

    @NotNull
    public static class_243 worldSpaceToScreenSpace(class_243 pos) {
        return pos == null ? new class_243(-1.0, -1.0, 2.0) : ProjectionUtil.worldSpaceToScreenSpace(pos.field_1352, pos.field_1351, pos.field_1350);
    }

    @NotNull
    public static class_243 worldSpaceToScreenSpace(double posX, double posY, double posZ) {
        class_4184 camera = ProjectionUtil.mc.field_1773.method_19418();
        class_243 camPos = camera.method_71156();
        float vx = (float)(posX - camPos.field_1352);
        float vy = (float)(posY - camPos.field_1351);
        float vz = (float)(posZ - camPos.field_1350);
        Vector3f view = new Vector3f(vx, vy, vz);
        Quaternionf rot = ROTATION_CACHE.get().set((Quaternionfc)camera.method_23767()).conjugate();
        view.rotate((Quaternionfc)rot);
        Vector4f clip = CLIP_CACHE.get().set(view.x, view.y, view.z, 1.0f);
        Render3DUtil.lastProjMat.transform(clip);
        if (clip.w <= 0.0f) {
            return new class_243(-1.0, -1.0, 2.0);
        }
        float invW = 1.0f / clip.w;
        float ndcX = clip.x * invW;
        float ndcY = clip.y * invW;
        float ndcZ = clip.z * invW;
        double screenX = (ndcX * 0.5f + 0.5f) * (float)mc.method_22683().method_4486();
        double screenY = (0.5f - ndcY * 0.5f) * (float)mc.method_22683().method_4502();
        return new class_243(screenX, screenY, (double)ndcZ);
    }

    public static Vector4d getVector4D(class_1297 ent) {
        if (ent == null) {
            return null;
        }
        Vector4d position = null;
        for (class_243 vector : ProjectionUtil.getVec3ds(ent, MathUtil.interpolate(ent))) {
            class_243 screen = ProjectionUtil.worldSpaceToScreenSpace(vector.field_1352, vector.field_1351, vector.field_1350);
            if (!(screen.field_1350 > 0.0) || !(screen.field_1350 < 1.0)) continue;
            if (position == null) {
                position = new Vector4d(screen.field_1352, screen.field_1351, screen.field_1350, 0.0);
            }
            position.x = Math.min(screen.field_1352, position.x);
            position.y = Math.min(screen.field_1351, position.y);
            position.z = Math.max(screen.field_1352, position.z);
            position.w = Math.max(screen.field_1351, position.w);
        }
        return position;
    }

    @NotNull
    public static class_243[] getVec3ds(class_1297 ent, class_243 pos) {
        class_238 axisAlignedBB2 = ent.method_5829();
        class_238 axisAlignedBB = new class_238(axisAlignedBB2.field_1323 - ent.method_23317() + pos.field_1352 - (double)0.1f, axisAlignedBB2.field_1322 - ent.method_23318() + pos.field_1351 - (double)0.1f, axisAlignedBB2.field_1321 - ent.method_23321() + pos.field_1350 - (double)0.1f, axisAlignedBB2.field_1320 - ent.method_23317() + pos.field_1352 + (double)0.1f, axisAlignedBB2.field_1325 - ent.method_23318() + pos.field_1351 + (double)0.1f, axisAlignedBB2.field_1324 - ent.method_23321() + pos.field_1350 + (double)0.1f);
        return new class_243[]{new class_243(axisAlignedBB.field_1323, axisAlignedBB.field_1322, axisAlignedBB.field_1321), new class_243(axisAlignedBB.field_1323, axisAlignedBB.field_1325, axisAlignedBB.field_1321), new class_243(axisAlignedBB.field_1320, axisAlignedBB.field_1322, axisAlignedBB.field_1321), new class_243(axisAlignedBB.field_1320, axisAlignedBB.field_1325, axisAlignedBB.field_1321), new class_243(axisAlignedBB.field_1323, axisAlignedBB.field_1322, axisAlignedBB.field_1324), new class_243(axisAlignedBB.field_1323, axisAlignedBB.field_1325, axisAlignedBB.field_1324), new class_243(axisAlignedBB.field_1320, axisAlignedBB.field_1322, axisAlignedBB.field_1324), new class_243(axisAlignedBB.field_1320, axisAlignedBB.field_1325, axisAlignedBB.field_1324)};
    }

    public static boolean canSee(class_243 vec3d) {
        class_4184 camera = ProjectionUtil.mc.method_1561().field_4686;
        Angle angle = AngleUtil.fromVec3d(vec3d.method_1020(camera.method_71156()));
        return Math.abs(class_3532.method_15393((float)(angle.getYaw() - camera.method_19330()))) < 90.0f && Math.abs(class_3532.method_15393((float)(angle.getPitch() - camera.method_19329()))) < 60.0f || ProjectionUtil.canSee(new class_238(class_2338.method_49638((class_2374)vec3d)));
    }

    public static boolean canSee(class_238 box) {
        class_4604 frustum = ProjectionUtil.mc.field_1769.method_62222();
        return box != null && frustum != null && frustum.method_23093(box);
    }

    public static boolean cantSee(Vector4d vec) {
        return vec == null || vec.x < 0.0 && vec.z < 1.0 || vec.y < 0.0 && vec.w < 1.0;
    }

    public static double centerX(Vector4d vec) {
        return vec.x + (vec.z - vec.x) / 2.0;
    }

    private ProjectionUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

