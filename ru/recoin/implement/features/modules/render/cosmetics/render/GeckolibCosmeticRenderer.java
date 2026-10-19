/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  net.minecraft.class_12249
 *  net.minecraft.class_1921
 *  net.minecraft.class_2960
 *  net.minecraft.class_4587
 *  net.minecraft.class_4588
 *  net.minecraft.class_4597
 *  net.minecraft.class_4608
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 */
package ru.recoin.implement.features.modules.render.cosmetics.render;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_12249;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_4608;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import ru.recoin.implement.features.modules.render.cosmetics.geo.GeoBone;
import ru.recoin.implement.features.modules.render.cosmetics.geo.GeoCube;
import ru.recoin.implement.features.modules.render.cosmetics.geo.GeoModel;
import ru.recoin.implement.features.modules.render.cosmetics.geo.GeoModelParser;
import ru.recoin.implement.features.modules.render.cosmetics.geo.GeoQuad;
import ru.recoin.implement.features.modules.render.cosmetics.geo.GeoVertex;
import ru.recoin.implement.features.modules.render.cosmetics.model.CosmeticModel;
import ru.recoin.implement.features.modules.render.cosmetics.render.GeckoRenderHelper;

public class GeckolibCosmeticRenderer {
    private static GeckolibCosmeticRenderer instance;
    private final Map<Integer, GeoModel> modelCache = new ConcurrentHashMap<Integer, GeoModel>();
    private final Map<Integer, CosmeticAnimationData> animationCache = new ConcurrentHashMap<Integer, CosmeticAnimationData>();
    private final Set<Integer> noAnimationSet = ConcurrentHashMap.newKeySet();
    private final Map<Integer, Long> animationStartTime = new ConcurrentHashMap<Integer, Long>();
    private final Map<Integer, Map<String, float[]>> initialBoneTransforms = new ConcurrentHashMap<Integer, Map<String, float[]>>();

    public static GeckolibCosmeticRenderer getInstance() {
        if (instance == null) {
            instance = new GeckolibCosmeticRenderer();
        }
        return instance;
    }

    public void renderCosmetic(CosmeticModel cosmetic, class_4587 matrices, class_4597 provider, int light) {
        GeoModel model;
        if (cosmetic != null && cosmetic.getTextureId() != null && (model = this.getOrParseModel(cosmetic)) != null) {
            CosmeticAnimationData anim = this.getOrParseAnimation(cosmetic);
            if (anim != null) {
                this.applyAnimations(model, anim, cosmetic.getId());
            }
            class_2960 tex = cosmetic.getTextureId();
            class_1921 layer = class_12249.method_75994((class_2960)tex);
            class_4588 consumer = provider.method_73477(layer);
            for (GeoBone bone : model.topLevelBones) {
                this.renderBone(bone, matrices, consumer, light, class_4608.field_21444, 1.0f, 1.0f, 1.0f, 1.0f);
            }
        }
    }

    public void renderCosmetic(CosmeticModel cosmetic, class_4587 matrices, class_4588 consumer, int light) {
        GeoModel model;
        if (cosmetic != null && cosmetic.getTextureId() != null && consumer != null && (model = this.getOrParseModel(cosmetic)) != null) {
            CosmeticAnimationData anim = this.getOrParseAnimation(cosmetic);
            if (anim != null) {
                this.applyAnimations(model, anim, cosmetic.getId());
            }
            for (GeoBone bone : model.topLevelBones) {
                this.renderBone(bone, matrices, consumer, light, class_4608.field_21444, 1.0f, 1.0f, 1.0f, 1.0f);
            }
        }
    }

    private GeoModel getOrParseModel(CosmeticModel cosmetic) {
        int id = cosmetic.getId();
        if (this.modelCache.containsKey(id)) {
            return this.modelCache.get(id);
        }
        String rawJson = cosmetic.getRawModelJson();
        if (rawJson == null) {
            return null;
        }
        GeoModel model = GeoModelParser.parse(rawJson);
        if (model != null) {
            this.modelCache.put(id, model);
            this.saveInitialBoneTransforms(id, model);
        }
        return model;
    }

    private void saveInitialBoneTransforms(int id, GeoModel model) {
        HashMap<String, float[]> transforms = new HashMap<String, float[]>();
        for (GeoBone bone : model.topLevelBones) {
            this.saveBonesRecursive(bone, transforms);
        }
        this.initialBoneTransforms.put(id, transforms);
    }

    private void saveBonesRecursive(GeoBone bone, Map<String, float[]> map) {
        map.put(bone.name, new float[]{bone.getRotationX(), bone.getRotationY(), bone.getRotationZ(), bone.getPositionX(), bone.getPositionY(), bone.getPositionZ(), bone.getScaleX(), bone.getScaleY(), bone.getScaleZ()});
        for (GeoBone child : bone.childBones) {
            this.saveBonesRecursive(child, map);
        }
    }

    private void renderBone(GeoBone bone, class_4587 matrices, class_4588 consumer, int light, int overlay, float r, float g, float b, float a) {
        if (!bone.isHidden) {
            matrices.method_22903();
            GeckoRenderHelper.translate(bone, matrices);
            GeckoRenderHelper.moveToPivot(bone, matrices);
            GeckoRenderHelper.rotate(bone, matrices);
            GeckoRenderHelper.scale(bone, matrices);
            GeckoRenderHelper.moveBackFromPivot(bone, matrices);
            for (GeoCube cube : bone.childCubes) {
                this.renderCube(cube, matrices, consumer, light, overlay, r, g, b, a);
            }
            for (GeoBone child : bone.childBones) {
                this.renderBone(child, matrices, consumer, light, overlay, r, g, b, a);
            }
            matrices.method_22909();
        }
    }

    private void renderCube(GeoCube cube, class_4587 matrices, class_4588 consumer, int light, int overlay, float r, float g, float b, float a) {
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
                consumer.method_22918((Matrix4fc)posMatrix, v.position.getX(), v.position.getY(), v.position.getZ()).method_22915(r, g, b, a).method_22913(v.textureU, v.textureV).method_22922(overlay).method_60803(light).method_22914(nx, ny, nz);
            }
        }
        matrices.method_22909();
    }

    private CosmeticAnimationData getOrParseAnimation(CosmeticModel cosmetic) {
        int id = cosmetic.getId();
        if (this.animationCache.containsKey(id)) {
            return this.animationCache.get(id);
        }
        if (this.noAnimationSet.contains(id)) {
            return null;
        }
        JsonObject animJson = cosmetic.getAnimationJson();
        if (animJson == null) {
            this.noAnimationSet.add(id);
            return null;
        }
        try {
            CosmeticAnimationData data = this.parseAnimationData(animJson);
            if (data != null) {
                this.animationCache.put(id, data);
            } else {
                this.noAnimationSet.add(id);
            }
            return data;
        }
        catch (Exception e) {
            this.noAnimationSet.add(id);
            return null;
        }
    }

    private CosmeticAnimationData parseAnimationData(JsonObject root) {
        CosmeticAnimationData data = new CosmeticAnimationData();
        if (!root.has("animations")) {
            return null;
        }
        JsonObject animations = root.getAsJsonObject("animations");
        Iterator it = animations.entrySet().iterator();
        if (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            String animName = (String)entry.getKey();
            JsonObject animObj = ((JsonElement)entry.getValue()).getAsJsonObject();
            data.animationName = animName;
            data.loop = animObj.has("loop") && animObj.get("loop").getAsBoolean();
            float f = data.length = animObj.has("animation_length") ? animObj.get("animation_length").getAsFloat() : 1.0f;
            if (animObj.has("bones")) {
                JsonObject bones = animObj.getAsJsonObject("bones");
                for (Map.Entry boneEntry : bones.entrySet()) {
                    String boneName = (String)boneEntry.getKey();
                    JsonObject boneObj = ((JsonElement)boneEntry.getValue()).getAsJsonObject();
                    BoneAnimationData bData = new BoneAnimationData();
                    if (boneObj.has("rotation")) {
                        bData.rotationKeyframes = this.parseKeyframes(boneObj.get("rotation"));
                    }
                    if (boneObj.has("position")) {
                        bData.positionKeyframes = this.parseKeyframes(boneObj.get("position"));
                    }
                    if (boneObj.has("scale")) {
                        bData.scaleKeyframes = this.parseKeyframes(boneObj.get("scale"));
                    }
                    data.boneAnimations.put(boneName, bData);
                }
            }
        }
        return data;
    }

    private Map<Float, float[]> parseKeyframes(JsonElement elem) {
        HashMap<Float, float[]> map = new HashMap<Float, float[]>();
        if (elem.isJsonObject()) {
            JsonObject obj = elem.getAsJsonObject();
            for (Map.Entry entry : obj.entrySet()) {
                try {
                    float time = Float.parseFloat((String)entry.getKey());
                    JsonElement val = (JsonElement)entry.getValue();
                    float[] vec = new float[3];
                    if (val.isJsonObject()) {
                        JsonObject vObj = val.getAsJsonObject();
                        if (vObj.has("vector")) {
                            JsonArray vArr = vObj.getAsJsonArray("vector");
                            vec[0] = vArr.get(0).getAsFloat();
                            vec[1] = vArr.get(1).getAsFloat();
                            vec[2] = vArr.get(2).getAsFloat();
                        }
                    } else if (val.isJsonArray()) {
                        JsonArray vArr = val.getAsJsonArray();
                        vec[0] = vArr.get(0).getAsFloat();
                        vec[1] = vArr.get(1).getAsFloat();
                        vec[2] = vArr.get(2).getAsFloat();
                    }
                    map.put(Float.valueOf(time), vec);
                }
                catch (NumberFormatException numberFormatException) {}
            }
        }
        return map;
    }

    private void applyAnimations(GeoModel model, CosmeticAnimationData anim, int id) {
        Map<String, float[]> initTransforms = this.initialBoneTransforms.get(id);
        if (initTransforms != null) {
            long startTime = this.animationStartTime.computeIfAbsent(id, k -> System.currentTimeMillis());
            float elapsed = (float)(System.currentTimeMillis() - startTime) / 1000.0f;
            float time = anim.loop && anim.length > 0.0f ? elapsed % anim.length : Math.min(elapsed, anim.length);
            for (GeoBone geoBone : model.topLevelBones) {
                this.resetBoneRecursive(geoBone, initTransforms);
            }
            for (Map.Entry entry : anim.boneAnimations.entrySet()) {
                String boneName = (String)entry.getKey();
                BoneAnimationData bData = (BoneAnimationData)entry.getValue();
                GeoBone bone = this.findBone(model, boneName);
                if (bone == null) continue;
                float[] base = initTransforms.get(boneName);
                if (base == null) {
                    base = new float[]{0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f};
                }
                if (!bData.rotationKeyframes.isEmpty()) {
                    float[] rot = this.interpolateKeyframes(bData.rotationKeyframes, time);
                    bone.setRotationX(base[0] + (float)Math.toRadians(-rot[0]));
                    bone.setRotationY(base[1] + (float)Math.toRadians(-rot[1]));
                    bone.setRotationZ(base[2] + (float)Math.toRadians(rot[2]));
                }
                if (!bData.positionKeyframes.isEmpty()) {
                    float[] pos = this.interpolateKeyframes(bData.positionKeyframes, time);
                    bone.setPositionX(base[3] + pos[0]);
                    bone.setPositionY(base[4] + pos[1]);
                    bone.setPositionZ(base[5] + pos[2]);
                }
                if (bData.scaleKeyframes.isEmpty()) continue;
                float[] scl = this.interpolateKeyframes(bData.scaleKeyframes, time);
                bone.setScaleX(base[6] * scl[0]);
                bone.setScaleY(base[7] * scl[1]);
                bone.setScaleZ(base[8] * scl[2]);
            }
        }
    }

    private void resetBoneRecursive(GeoBone bone, Map<String, float[]> transforms) {
        float[] base = transforms.get(bone.name);
        if (base != null) {
            bone.setRotationX(base[0]);
            bone.setRotationY(base[1]);
            bone.setRotationZ(base[2]);
            bone.setPositionX(base[3]);
            bone.setPositionY(base[4]);
            bone.setPositionZ(base[5]);
            bone.setScaleX(base[6]);
            bone.setScaleY(base[7]);
            bone.setScaleZ(base[8]);
        }
        for (GeoBone child : bone.childBones) {
            this.resetBoneRecursive(child, transforms);
        }
    }

    private GeoBone findBone(GeoModel model, String name) {
        for (GeoBone bone : model.topLevelBones) {
            GeoBone found = this.findBoneRecursive(bone, name);
            if (found == null) continue;
            return found;
        }
        return null;
    }

    private GeoBone findBoneRecursive(GeoBone current, String name) {
        if (current.name.equals(name)) {
            return current;
        }
        for (GeoBone child : current.childBones) {
            GeoBone found = this.findBoneRecursive(child, name);
            if (found == null) continue;
            return found;
        }
        return null;
    }

    private float[] interpolateKeyframes(Map<Float, float[]> keyframes, float time) {
        if (keyframes.isEmpty()) {
            return new float[]{0.0f, 0.0f, 0.0f};
        }
        Float tPrev = null;
        Float tNext = null;
        float[] vPrev = null;
        float[] vNext = null;
        for (Map.Entry<Float, float[]> entry : keyframes.entrySet()) {
            float kTime = entry.getKey().floatValue();
            if (kTime <= time && (tPrev == null || kTime > tPrev.floatValue())) {
                tPrev = Float.valueOf(kTime);
                vPrev = entry.getValue();
            }
            if (!(kTime >= time) || tNext != null && !(kTime < tNext.floatValue())) continue;
            tNext = Float.valueOf(kTime);
            vNext = entry.getValue();
        }
        if (vPrev == null && vNext == null) {
            return new float[]{0.0f, 0.0f, 0.0f};
        }
        if (vPrev == null) {
            return vNext;
        }
        if (vNext == null || tPrev.equals(tNext)) {
            return vPrev;
        }
        float factor = (time - tPrev.floatValue()) / (tNext.floatValue() - tPrev.floatValue());
        return new float[]{vPrev[0] + factor * (vNext[0] - vPrev[0]), vPrev[1] + factor * (vNext[1] - vPrev[1]), vPrev[2] + factor * (vNext[2] - vPrev[2])};
    }

    private static class CosmeticAnimationData {
        String animationName;
        boolean loop;
        float length;
        Map<String, BoneAnimationData> boneAnimations = new HashMap<String, BoneAnimationData>();

        private CosmeticAnimationData() {
        }
    }

    private static class BoneAnimationData {
        Map<Float, float[]> rotationKeyframes = new HashMap<Float, float[]>();
        Map<Float, float[]> positionKeyframes = new HashMap<Float, float[]>();
        Map<Float, float[]> scaleKeyframes = new HashMap<Float, float[]>();

        private BoneAnimationData() {
        }
    }
}

