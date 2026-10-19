package net.maksaidev.atmosphere.weather;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.class_10789;
import net.minecraft.class_10799;
import net.minecraft.class_12137;
import net.minecraft.class_243;
import net.minecraft.class_276;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import net.minecraft.class_6367;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.lwjgl.system.MemoryUtil;

public final class WeatherRenderer
implements AutoCloseable {
    private static final RenderPipeline MATERIALS = WeatherRenderer.build(true);
    private static final RenderPipeline PRECIPITATION = WeatherRenderer.build(false);
    private class_6367 scene;
    private class_6367 terrain;
    private GpuBuffer uniforms;
    private GpuBuffer triangle;
    private final PrecipitationMesh mesh = new PrecipitationMesh();
    private final ByteBuffer data = MemoryUtil.memAlloc((int)512).order(ByteOrder.nativeOrder());
    private final Matrix4f inverseProjection = new Matrix4f();
    private final Matrix4f inverseView = new Matrix4f();
    private final long start = System.nanoTime();
    private boolean captured;

    private static RenderPipeline build(boolean materials) {
        RenderPipeline.Builder builder = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(class_2960.method_60655((String)"maksatmosphere", (String)(materials ? "weather_materials" : "weather_precipitation"))).withVertexShader(class_2960.method_60655((String)"maksatmosphere", (String)(materials ? "weather/fullscreen" : "weather/precipitation"))).withFragmentShader(class_2960.method_60655((String)"maksatmosphere", (String)(materials ? "weather/materials" : "weather/precipitation"))).withUniform("Weather", class_10789.field_60031).withDepthWrite(false).withCull(false);
        if (materials) {
            builder.withSampler("SceneColor").withSampler("SceneDepth").withSampler("TerrainDepth").withVertexFormat(class_290.field_1592, VertexFormat.class_5596.field_27379).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST);
        } else {
            builder.withVertexFormat(class_290.field_1575, VertexFormat.class_5596.field_27379).withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST).withBlend(BlendFunction.TRANSLUCENT);
        }
        return WeatherRenderer.register(builder.build());
    }

    private static RenderPipeline register(RenderPipeline pipeline) {
        for (String name : new String[]{"register", "method_67887"}) {
            try {
                Method m = class_10799.class.getDeclaredMethod(name, RenderPipeline.class);
                m.setAccessible(true);
                return (RenderPipeline)m.invoke(null, pipeline);
            }
            catch (Throwable throwable) {
            }
        }
        return pipeline;
    }

    private static boolean active() {
        WeatherConfig c = WeatherModule.config();
        return c != null && c.enabled && (c.mode != WeatherConfig.Mode.OFF || c.wetGround);
    }

    private static boolean materialsNeeded() {
        WeatherConfig c = WeatherModule.config();
        if (c == null || !c.enabled) {
            return false;
        }
        return switch (c.mode) {
            case OFF -> c.wetGround;
            case RAIN, WINTER, SUNRISE, DUST -> true;
            case SAKURA -> c.sakuraTint && c.sakuraTintStrength > 0;
            case ECLIPSE, METEOR, BLOOD_MOON -> true;
        };
    }

    public void captureTerrain(WorldRenderContext context) {
        this.captured = false;
        if (WeatherRenderer.active()) {
            class_276 target = class_310.method_1551().method_1522();
            this.ensureTargets(target);
            if (WeatherRenderer.materialsNeeded()) {
                this.terrain.method_29329(target);
            }
            this.captured = true;
        }
    }

    private void ensureTargets(class_276 target) {
        if (this.scene == null) {
            this.scene = new class_6367("MaksaiAtmosphere weather scene", target.field_1482, target.field_1481, true);
            this.terrain = new class_6367("MaksaiAtmosphere weather terrain mask", target.field_1482, target.field_1481, true);
        } else if (this.scene.field_1482 != target.field_1482 || this.scene.field_1481 != target.field_1481) {
            this.scene.method_1234(target.field_1482, target.field_1481);
            this.terrain.method_1234(target.field_1482, target.field_1481);
        }
        if (this.uniforms == null) {
            this.uniforms = RenderSystem.getDevice().createBuffer(() -> "MaksaiAtmosphere weather uniforms", 136, 512L);
            ByteBuffer vertices = MemoryUtil.memAlloc((int)36).order(ByteOrder.nativeOrder());
            vertices.putFloat(-1.0f).putFloat(-1.0f).putFloat(0.0f);
            vertices.putFloat(3.0f).putFloat(-1.0f).putFloat(0.0f);
            vertices.putFloat(-1.0f).putFloat(3.0f).putFloat(0.0f).flip();
            this.triangle = RenderSystem.getDevice().createBuffer(() -> "MaksaiAtmosphere fullscreen triangle", 32, vertices);
            MemoryUtil.memFree((Buffer)vertices);
        }
    }

    public void render(WorldRenderContext context) {
        RenderPass pass;
        if (!WeatherRenderer.active() || !this.captured) {
            return;
        }
        this.captured = false;
        class_310 client = class_310.method_1551();
        if (client.field_1687 == null) {
            return;
        }
        class_276 target = client.method_1522();
        WeatherConfig c = WeatherModule.config();
        class_4184 camera = client.field_1773.method_19418();
        class_243 cameraPos = context.worldState() != null && context.worldState().field_63082 != null && context.worldState().field_63082.field_63078 != null ? context.worldState().field_63082.field_63078 : camera.method_71156();
        int radius = client.field_1690.method_38521() * 16;
        this.mesh.update(client.field_1687, cameraPos, radius, c);
        this.data.clear();
        float fov = ((Integer)client.field_1690.method_41808().method_41753()).intValue();
        Matrix4f projectionMatrix = client.field_1773.method_22973(fov);
        Quaternionf rotation = camera.method_23767().conjugate(new Quaternionf());
        Matrix4f positionMatrix = new Matrix4f().rotation((Quaternionfc)rotation);
        projectionMatrix.get(0, this.data);
        this.inverseProjection.set((Matrix4fc)projectionMatrix).invert().get(64, this.data);
        positionMatrix.get(128, this.data);
        this.inverseView.set((Matrix4fc)positionMatrix).invert().get(192, this.data);
        this.data.position(256);
        this.data.putFloat((float)cameraPos.field_1352).putFloat((float)cameraPos.field_1351).putFloat((float)cameraPos.field_1350).putFloat((float)(System.nanoTime() - this.start) / 1.0E9f);
        this.data.putFloat(c.mode == WeatherConfig.Mode.RAIN ? (float)c.rainIntensity / 100.0f : 0.0f).putFloat(c.mode == WeatherConfig.Mode.WINTER && c.snowCover ? 1.0f : 0.0f).putFloat((c.mode == WeatherConfig.Mode.OFF || c.mode == WeatherConfig.Mode.RAIN) && c.wetGround ? 1.0f : 0.0f).putFloat((float)c.puddles / 100.0f);
        this.data.putFloat(target.field_1482).putFloat(target.field_1481).putFloat(c.splashes ? 1.0f : 0.0f).putFloat((float)c.wind / 100.0f);
        float sunAngle = context.worldState() != null && context.worldState().field_63087 != null ? context.worldState().field_63087.field_63091 : (float)((double)(client.field_1687.method_8532() % 24000L) / 24000.0);
        float daylight = (float)Math.max(0.08, Math.min(1.0, Math.cos((double)sunAngle * Math.PI * 2.0) * 1.5 + 0.25));
        this.data.putFloat(daylight).putFloat(radius).putFloat((float)c.snowDensity / 100.0f).putFloat(c.mode.ordinal());
        this.data.putFloat(this.mesh.originX()).putFloat(0.0f).putFloat(this.mesh.originZ()).putFloat(0.0f);
        this.data.putFloat(c.quality.ordinal()).putFloat(c.mode == WeatherConfig.Mode.WINTER ? (float)c.blizzard / 100.0f : 0.0f).putFloat(c.splashes ? (float)c.splashDensity / 100.0f : 0.0f).putFloat((float)c.ringVisibility / 100.0f);
        this.data.putFloat((float)c.sunlight / 100.0f).putFloat((float)c.sunRays / 100.0f).putFloat((float)c.dew / 100.0f).putFloat((float)c.dustDensity / 100.0f);
        this.data.putFloat((float)c.dustLight / 100.0f).putFloat(0.0f).putFloat((float)c.sunHeight / 100.0f).putFloat((float)c.dustSize / 100.0f);
        this.data.putFloat((float)c.sakuraDensity / 100.0f).putFloat((float)c.sakuraSize / 100.0f).putFloat((float)c.sakuraGroundLife * 0.3f).putFloat((float)c.sakuraWind / 100.0f);
        this.data.putFloat((float)c.sakuraHue / 100.0f).putFloat((float)c.sakuraSaturation / 100.0f).putFloat(c.sakuraTint ? (float)c.sakuraTintStrength / 100.0f : 0.0f).putFloat((float)c.sakuraFlutter / 100.0f);
        this.data.putFloat((float)c.eclipseDarkness / 100.0f).putFloat((float)c.eclipseCorona / 100.0f).putFloat((float)c.eclipseStars / 100.0f).putFloat((float)c.eclipseDust / 100.0f);
        this.data.putFloat((float)c.meteorRate / 100.0f).putFloat((float)c.meteorBrightness / 100.0f).putFloat((float)c.meteorTrail / 100.0f).putFloat((float)c.meteorStars / 100.0f);
        this.data.putFloat((float)c.meteorDebris / 100.0f).putFloat((float)c.meteorSize / 100.0f).putFloat((float)c.meteorVariation / 100.0f).putFloat((float)c.meteorProximity / 100.0f);
        this.data.putFloat((float)c.bloodMoonSize / 100.0f).putFloat((float)c.bloodMoonGlow / 100.0f).putFloat((float)c.bloodSky / 100.0f).putFloat((float)c.bloodClouds / 100.0f);
        this.data.putFloat((float)c.bloodMist / 100.0f).putFloat((float)c.bloodGround / 100.0f).putFloat((float)c.bloodEmbers / 100.0f).putFloat((float)c.bloodHeight / 100.0f);
        this.data.putFloat((float)c.meteorFog / 100.0f).putFloat((float)c.meteorFogDistance / 100.0f).putFloat((float)c.meteorSkyGlow / 100.0f).putFloat((float)c.meteorNebula / 100.0f).flip();
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        encoder.writeToBuffer(this.uniforms.slice(), this.data);
        if (WeatherRenderer.materialsNeeded()) {
            encoder.copyTextureToTexture(target.method_30277(), this.scene.method_30277(), 0, 0, 0, 0, 0, target.field_1482, target.field_1481);
            this.scene.method_29329(target);
            pass = encoder.createRenderPass(() -> "MaksaiAtmosphere weather materials and reflections", target.method_71639(), OptionalInt.empty());
            try {
                pass.setPipeline(MATERIALS);
                pass.setUniform("Weather", this.uniforms);
                class_12137 sampler = RenderSystem.getSamplerCache().method_75294(FilterMode.NEAREST);
                pass.bindTexture("SceneColor", this.scene.method_71639(), sampler);
                pass.bindTexture("SceneDepth", this.scene.method_71640(), sampler);
                pass.bindTexture("TerrainDepth", this.terrain.method_71640(), sampler);
                pass.setVertexBuffer(0, this.triangle);
                pass.draw(0, 3);
            }
            finally {
                if (pass != null) {
                    pass.close();
                }
            }
        }
        if (this.mesh.buffer() != null && this.mesh.vertexCount() > 0 && c.mode != WeatherConfig.Mode.OFF) {
            pass = encoder.createRenderPass(() -> "MaksaiAtmosphere physical precipitation", target.method_71639(), OptionalInt.empty(), target.method_71640(), OptionalDouble.empty());
            try {
                pass.setPipeline(PRECIPITATION);
                pass.setUniform("Weather", this.uniforms);
                pass.setVertexBuffer(0, this.mesh.buffer());
                pass.draw(0, this.mesh.vertexCount());
            }
            finally {
                if (pass != null) {
                    pass.close();
                }
            }
        }
    }

    @Override
    public void close() {
        this.mesh.close();
        if (this.uniforms != null) {
            this.uniforms.close();
            this.uniforms = null;
        }
        if (this.triangle != null) {
            this.triangle.close();
            this.triangle = null;
        }
        if (this.scene != null) {
            this.scene.method_1238();
            this.scene = null;
        }
        if (this.terrain != null) {
            this.terrain.method_1238();
            this.terrain = null;
        }
        MemoryUtil.memFree((Buffer)this.data);
    }
}
