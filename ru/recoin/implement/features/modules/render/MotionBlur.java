/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_243
 *  net.minecraft.class_4184
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.lwjgl.glfw.GLFW
 *  org.lwjgl.glfw.GLFWVidMode
 */
package ru.recoin.implement.features.modules.render;

import net.minecraft.class_243;
import net.minecraft.class_4184;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.render.pipeline.MotionBlurPipeline;

public final class MotionBlur
extends Module {
    private final ValueSetting strength = new ValueSetting("Strength", "Blur trail length per frame").setValue(1.0f).range(0.1f, 3.0f).step(0.05f);
    private final ValueSetting samples = new ValueSetting("Samples", "Per-pixel sample limit").setValue(32.0f).range(4.0f, 128.0f).step(1.0f);
    private final BooleanSetting useDepth = new BooleanSetting("Use Depth", "Depth-aware reprojection").setValue(true);
    private final BooleanSetting centered = new BooleanSetting("Centered", "Bi-directional trail").setValue(false);
    private final BooleanSetting thirdPerson = new BooleanSetting("Third Person", "Apply in third-person view").setValue(false);
    private final BooleanSetting hzCompensation = new BooleanSetting("Hz Compensation", "Scale with monitor refresh rate").setValue(true);
    private static final Matrix4f curView = new Matrix4f();
    private static final Matrix4f curProj = new Matrix4f();
    private static final Vector3f curCam = new Vector3f();
    private static final Matrix4f prvView = new Matrix4f();
    private static final Matrix4f prvProj = new Matrix4f();
    private static final Matrix4f invView = new Matrix4f();
    private static final Matrix4f invProjection = new Matrix4f();
    private static final Vector3f prvCam = new Vector3f();
    private static boolean hasCurrent;
    private static boolean hasPrev;
    private static long monitorNs;
    private static long lastNs;
    private static float hzMul;
    private static final MotionBlurPipeline pipeline;

    public MotionBlur() {
        super("MotionBlur", "Motion Blur", ModuleCategory.RENDER);
        this.setup(this.strength, this.samples, this.useDepth, this.centered, this.thirdPerson, this.hzCompensation);
    }

    public static MotionBlur getInstance() {
        return Instance.get(MotionBlur.class);
    }

    @Override
    public void activate() {
        hasCurrent = false;
        hasPrev = false;
        lastNs = 0L;
        hzMul = 1.0f;
        try {
            GLFWVidMode m;
            long mon = GLFW.glfwGetPrimaryMonitor();
            if (mon != 0L && (m = GLFW.glfwGetVideoMode((long)mon)) != null && m.refreshRate() > 0) {
                monitorNs = 1000000000L / (long)m.refreshRate();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    @Override
    public void deactivate() {
        hasCurrent = false;
        hasPrev = false;
    }

    public static void updateMatrices(Matrix4f positionMatrix, Matrix4f projectionMatrix, class_4184 camera) {
        MotionBlur mod = MotionBlur.getInstance();
        if (mod == null || !mod.isState() || camera == null || positionMatrix == null || projectionMatrix == null) {
            hasCurrent = false;
            hasPrev = false;
            return;
        }
        if (MotionBlur.mc.field_1773.method_35765()) {
            hasCurrent = false;
            hasPrev = false;
            return;
        }
        long now = System.nanoTime();
        long dt = now - lastNs;
        lastNs = now;
        hzMul = mod.hzCompensation.isValue() && dt > 0L && dt < monitorNs ? Math.min((float)monitorNs / (float)dt, 8.0f) : 1.0f;
        if (hasCurrent) {
            prvView.set((Matrix4fc)curView);
            prvProj.set((Matrix4fc)curProj);
            prvCam.set((Vector3fc)curCam);
            hasPrev = true;
        }
        curView.set((Matrix4fc)positionMatrix);
        curProj.set((Matrix4fc)projectionMatrix);
        class_243 p = camera.method_71156();
        curCam.set((float)(p.field_1352 % 30000.0), (float)(p.field_1351 % 30000.0), (float)(p.field_1350 % 30000.0));
        hasCurrent = true;
        if (hasPrev && curCam.distanceSquared((Vector3fc)prvCam) > 16.0f) {
            hasPrev = false;
        }
    }

    public static void render() {
        MotionBlur mod = MotionBlur.getInstance();
        if (mod == null || !mod.isState() || !hasPrev) {
            return;
        }
        if (!mod.thirdPerson.isValue() && !MotionBlur.mc.field_1690.method_31044().method_31034()) {
            return;
        }
        float str = mod.strength.getValue();
        if (mod.hzCompensation.isValue()) {
            str *= hzMul;
        }
        if (str <= 0.0f) {
            return;
        }
        invView.set((Matrix4fc)curView).invert();
        invProjection.set((Matrix4fc)curProj).invert();
        pipeline.draw(invView, invProjection, prvView, prvProj, curCam, prvCam, str, mod.samples.getInt(), mod.centered.isValue(), mod.useDepth.isValue());
    }

    static {
        monitorNs = 16666666L;
        hzMul = 1.0f;
        pipeline = new MotionBlurPipeline();
    }
}

