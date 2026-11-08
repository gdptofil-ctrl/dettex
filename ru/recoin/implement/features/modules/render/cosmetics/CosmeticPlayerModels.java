package ru.recoin.implement.features.modules.render.cosmetics;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_10055;
import net.minecraft.class_11659;
import net.minecraft.class_12249;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_591;
import net.minecraft.class_630;
import net.minecraft.class_7833;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import ru.recoin.implement.features.modules.render.cosmetics.geo.GeoBone;
import ru.recoin.implement.features.modules.render.cosmetics.geo.GeoCube;
import ru.recoin.implement.features.modules.render.cosmetics.geo.GeoModel;
import ru.recoin.implement.features.modules.render.cosmetics.geo.GeoModelParser;
import ru.recoin.implement.features.modules.render.cosmetics.geo.GeoQuad;
import ru.recoin.implement.features.modules.render.cosmetics.geo.GeoVertex;
import ru.recoin.implement.features.modules.render.cosmetics.loader.CosmeticLoader;
import ru.recoin.implement.features.modules.render.cosmetics.model.CosmeticModel;
import ru.recoin.implement.features.modules.render.cosmetics.render.GeckoRenderHelper;

/**
 * Replaces the player model with a geo model from assets/recoin/cosmetics/playermodels.
 * The JSON holds the usual cosmetic data plus "scale" and "bones": vanilla part -> bone name(s).
 */
public final class CosmeticPlayerModels {
    private static final String[] PARTS = new String[]{"head", "right_arm", "left_arm", "right_leg", "left_leg"};
    private static final Map<String, Loaded> CACHE = new ConcurrentHashMap<String, Loaded>();
    private static final Set<String> FAILED = ConcurrentHashMap.newKeySet();

    private CosmeticPlayerModels() {
    }

    public static boolean render(String name, class_591 vanilla, class_10055 state, class_4587 matrices, class_11659 queue, int light, int overlay) {
        Loaded loaded = CosmeticPlayerModels.get(name);
        if (loaded == null || loaded.cosmetic.getTextureId() == null || vanilla == null) {
            return false;
        }
        // capture the pose now: the queued geometry is drawn after other players changed the shared model
        float[][] pose = new float[][]{CosmeticPlayerModels.angles(vanilla.field_3398), CosmeticPlayerModels.angles(vanilla.field_3401), CosmeticPlayerModels.angles(vanilla.field_27433), CosmeticPlayerModels.angles(vanilla.field_3392), CosmeticPlayerModels.angles(vanilla.field_3397)};
        matrices.method_22903();
        matrices.method_22907((Quaternionfc)class_7833.field_40718.rotationDegrees(180.0f));
        matrices.method_46416(0.0f, -1.501f, 0.0f);
        matrices.method_22905(loaded.scale, loaded.scale, loaded.scale);
        class_1921 layer = class_12249.method_75994((class_2960)loaded.cosmetic.getTextureId());
        queue.method_73483(matrices, layer, (entry, vertexConsumer) -> {
            class_4587 localMatrices = new class_4587();
            localMatrices.method_23760().method_23761().set((Matrix4fc)entry.method_23761());
            localMatrices.method_23760().method_23762().set((Matrix3fc)entry.method_23762());
            synchronized (loaded) {
                for (int i = 0; i < PARTS.length; ++i) {
                    List<GeoBone> bones = loaded.parts.get(PARTS[i]);
                    if (bones == null) continue;
                    for (GeoBone bone : bones) {
                        float[] base = loaded.rest.get(bone);
                        bone.setRotationX(base[0] - pose[i][0]);
                        bone.setRotationY(base[1] - pose[i][1]);
                        bone.setRotationZ(base[2] + pose[i][2]);
                    }
                }
                for (GeoBone bone : loaded.geo.topLevelBones) {
                    CosmeticPlayerModels.renderBone(bone, localMatrices, vertexConsumer, light, overlay);
                }
            }
        });
        matrices.method_22909();
        return true;
    }

    private static float[] angles(class_630 part) {
        return part == null ? new float[3] : new float[]{part.field_3654, part.field_3675, part.field_3674};
    }

    private static Loaded get(String name) {
        if (name == null || name.isEmpty() || FAILED.contains(name)) {
            return null;
        }
        Loaded cached = CACHE.get(name);
        if (cached != null) {
            return cached;
        }
        CosmeticCatalog.Entry entry = CosmeticCatalog.find(CosmeticCatalog.PLAYER_MODELS, name);
        Loaded loaded = entry == null ? null : CosmeticPlayerModels.load(entry.resourcePath());
        if (loaded == null) {
            FAILED.add(name);
            return null;
        }
        CACHE.put(name, loaded);
        return loaded;
    }

    private static Loaded load(String path) {
        try (InputStream in = CosmeticPlayerModels.class.getResourceAsStream(path)) {
            if (in == null) {
                return null;
            }
            JsonObject root = JsonParser.parseString((String)new String(in.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
            CosmeticModel cosmetic = CosmeticLoader.getInstance().loadFromJson(root);
            if (cosmetic == null) {
                return null;
            }
            GeoModel geo = GeoModelParser.parse(cosmetic.getRawModelJson());
            if (geo == null) {
                return null;
            }
            float scale = root.has("scale") ? root.get("scale").getAsFloat() : 1.0f;
            HashMap<String, List<GeoBone>> parts = new HashMap<String, List<GeoBone>>();
            HashMap<GeoBone, float[]> rest = new HashMap<GeoBone, float[]>();
            if (root.has("bones") && root.get("bones").isJsonObject()) {
                for (Map.Entry<String, JsonElement> part : root.getAsJsonObject("bones").entrySet()) {
                    ArrayList<String> names = new ArrayList<String>();
                    if (part.getValue().isJsonArray()) {
                        for (JsonElement element : part.getValue().getAsJsonArray()) {
                            names.add(element.getAsString());
                        }
                    } else {
                        names.add(part.getValue().getAsString());
                    }
                    ArrayList<GeoBone> bones = new ArrayList<GeoBone>();
                    for (String boneName : names) {
                        geo.getBone(boneName).ifPresent(bone -> {
                            bones.add(bone);
                            rest.put(bone, new float[]{bone.getRotationX(), bone.getRotationY(), bone.getRotationZ()});
                        });
                    }
                    parts.put(part.getKey(), bones);
                }
            }
            return new Loaded(cosmetic, geo, scale, parts, rest);
        }
        catch (Exception e) {
            return null;
        }
    }

    private static void renderBone(GeoBone bone, class_4587 matrices, class_4588 consumer, int light, int overlay) {
        if (bone.isHidden) {
            return;
        }
        matrices.method_22903();
        GeckoRenderHelper.translate(bone, matrices);
        GeckoRenderHelper.moveToPivot(bone, matrices);
        GeckoRenderHelper.rotate(bone, matrices);
        GeckoRenderHelper.scale(bone, matrices);
        GeckoRenderHelper.moveBackFromPivot(bone, matrices);
        for (GeoCube cube : bone.childCubes) {
            CosmeticPlayerModels.renderCube(cube, matrices, consumer, light, overlay);
        }
        for (GeoBone child : bone.childBones) {
            CosmeticPlayerModels.renderBone(child, matrices, consumer, light, overlay);
        }
        matrices.method_22909();
    }

    private static void renderCube(GeoCube cube, class_4587 matrices, class_4588 consumer, int light, int overlay) {
        matrices.method_22903();
        GeckoRenderHelper.moveToPivot(cube, matrices);
        GeckoRenderHelper.rotate(cube, matrices);
        GeckoRenderHelper.moveBackFromPivot(cube, matrices);
        Matrix4f posMatrix = matrices.method_23760().method_23761();
        Matrix3f normMatrix = matrices.method_23760().method_23762();
        for (GeoQuad quad : cube.quads) {
            if (quad == null) continue;
            Vector3f normal = new Vector3f(quad.normal.getX(), quad.normal.getY(), quad.normal.getZ());
            normMatrix.transform(normal);
            float nx = normal.x();
            float ny = normal.y();
            float nz = normal.z();
            if ((cube.size.getY() == 0.0f || cube.size.getZ() == 0.0f) && nx < 0.0f) {
                nx = -nx;
            }
            if ((cube.size.getX() == 0.0f || cube.size.getZ() == 0.0f) && ny < 0.0f) {
                ny = -ny;
            }
            if ((cube.size.getX() == 0.0f || cube.size.getY() == 0.0f) && nz < 0.0f) {
                nz = -nz;
            }
            for (GeoVertex v : quad.vertices) {
                consumer.method_22918((Matrix4fc)posMatrix, v.position.getX(), v.position.getY(), v.position.getZ()).method_22915(1.0f, 1.0f, 1.0f, 1.0f).method_22913(v.textureU, v.textureV).method_22922(overlay).method_60803(light).method_22914(nx, ny, nz);
            }
        }
        matrices.method_22909();
    }

    private static final class Loaded {
        final CosmeticModel cosmetic;
        final GeoModel geo;
        final float scale;
        final Map<String, List<GeoBone>> parts;
        final Map<GeoBone, float[]> rest;

        Loaded(CosmeticModel cosmetic, GeoModel geo, float scale, Map<String, List<GeoBone>> parts, Map<GeoBone, float[]> rest) {
            this.cosmetic = cosmetic;
            this.geo = geo;
            this.scale = scale;
            this.parts = parts;
            this.rest = rest;
        }
    }
}
