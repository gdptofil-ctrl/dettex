/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_243
 *  net.minecraft.class_4184
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 */
package ru.recoin.implement.features.modules.render;

import net.minecraft.class_243;
import net.minecraft.class_4184;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.render.pipeline.WetWorldPipeline;

public final class WetWorld
extends Module {
    private static final Matrix4f curProj = new Matrix4f();
    private static final Matrix4f invProj = new Matrix4f();
    private static final Matrix4f viewRot = new Matrix4f();
    private static final Vector3f curCam = new Vector3f();
    private static float curFov = 70.0f;
    private static boolean hasCurrent;
    private static final WetWorldPipeline pipeline;
    private static final long startTime;

    public WetWorld() {
        super("WetWorld", "WetWorld", ModuleCategory.RENDER);
    }

    public static WetWorld getInstance() {
        return Instance.get(WetWorld.class);
    }

    @Override
    public void activate() {
        hasCurrent = false;
    }

    @Override
    public void deactivate() {
        hasCurrent = false;
    }

    public static void updateMatrices(Matrix4f positionMatrix, Matrix4f projectionMatrix, class_4184 camera, float fov) {
        WetWorld mod = WetWorld.getInstance();
        if (mod == null || !mod.isState() || camera == null || positionMatrix == null || projectionMatrix == null) {
            hasCurrent = false;
            return;
        }
        if (WetWorld.mc.field_1773.method_35765()) {
            hasCurrent = false;
            return;
        }
        if (!hasCurrent || !curProj.equals((Object)projectionMatrix)) {
            curProj.set((Matrix4fc)projectionMatrix);
            invProj.set((Matrix4fc)projectionMatrix).invert();
        }
        class_243 p = camera.method_71156();
        curCam.set((float)p.field_1352, (float)p.field_1351, (float)p.field_1350);
        curFov = fov;
        hasCurrent = true;
    }

    public static void render() {
        WetWorld mod = WetWorld.getInstance();
        if (mod == null || !mod.isState() || WetWorld.mc.field_1687 == null || WetWorld.mc.field_1773 == null) {
            return;
        }
        class_4184 camera = WetWorld.mc.field_1773.method_19418();
        if (camera == null || !hasCurrent) {
            return;
        }
        viewRot.rotation((Quaternionfc)camera.method_23767());
        float time = (float)((double)(System.currentTimeMillis() - startTime) / 1000.0);
        pipeline.draw(curProj, invProj, viewRot, curCam, curFov, time);
    }

    static {
        pipeline = new WetWorldPipeline();
        startTime = System.currentTimeMillis();
    }
}

