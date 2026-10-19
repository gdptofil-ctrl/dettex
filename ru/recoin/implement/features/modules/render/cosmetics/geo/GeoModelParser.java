/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 */
package ru.recoin.implement.features.modules.render.cosmetics.geo;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.HashMap;
import ru.recoin.implement.features.modules.render.cosmetics.geo.GeoBone;
import ru.recoin.implement.features.modules.render.cosmetics.geo.GeoCube;
import ru.recoin.implement.features.modules.render.cosmetics.geo.GeoModel;
import ru.recoin.implement.features.modules.render.cosmetics.geo.GeoQuad;
import ru.recoin.implement.features.modules.render.cosmetics.geo.GeoVertex;
import ru.recoin.implement.features.modules.render.cosmetics.geo.Vec3F;

public class GeoModelParser {
    public static GeoModel parse(String jsonStr) {
        try {
            JsonObject json = JsonParser.parseString((String)jsonStr).getAsJsonObject();
            return GeoModelParser.parseModel(json);
        }
        catch (Exception e) {
            return null;
        }
    }

    private static GeoModel parseModel(JsonObject root) {
        JsonArray geometryArray = root.getAsJsonArray("minecraft:geometry");
        if (geometryArray != null && !geometryArray.isEmpty()) {
            JsonObject geom = geometryArray.get(0).getAsJsonObject();
            JsonObject desc = geom.getAsJsonObject("description");
            int texWidth = desc.has("texture_width") ? desc.get("texture_width").getAsInt() : 64;
            int texHeight = desc.has("texture_height") ? desc.get("texture_height").getAsInt() : 64;
            GeoModel model = new GeoModel();
            model.textureWidth = texWidth;
            model.textureHeight = texHeight;
            JsonArray bonesArray = geom.getAsJsonArray("bones");
            if (bonesArray != null) {
                JsonObject boneJson;
                HashMap<String, GeoBone> boneMap = new HashMap<String, GeoBone>();
                for (JsonElement elem : bonesArray) {
                    boneJson = elem.getAsJsonObject();
                    GeoBone bone = GeoModelParser.parseBone(boneJson, texWidth, texHeight);
                    boneMap.put(bone.name, bone);
                }
                for (JsonElement elem : bonesArray) {
                    boneJson = elem.getAsJsonObject();
                    String name = boneJson.get("name").getAsString();
                    GeoBone bone = (GeoBone)boneMap.get(name);
                    if (boneJson.has("parent")) {
                        String parentName = boneJson.get("parent").getAsString();
                        GeoBone parent = (GeoBone)boneMap.get(parentName);
                        if (parent == null) continue;
                        parent.childBones.add(bone);
                        bone.parent = parent;
                        continue;
                    }
                    model.topLevelBones.add(bone);
                }
            }
            return model;
        }
        return null;
    }

    private static GeoBone parseBone(JsonObject boneJson, int texWidth, int texHeight) {
        String name = boneJson.get("name").getAsString();
        GeoBone bone = new GeoBone(name);
        if (boneJson.has("pivot")) {
            JsonArray pivot = boneJson.getAsJsonArray("pivot");
            bone.rotationPointX = -pivot.get(0).getAsFloat();
            bone.rotationPointY = pivot.get(1).getAsFloat();
            bone.rotationPointZ = pivot.get(2).getAsFloat();
        }
        if (boneJson.has("rotation")) {
            JsonArray rot = boneJson.getAsJsonArray("rotation");
            bone.setRotationX((float)Math.toRadians(-rot.get(0).getAsFloat()));
            bone.setRotationY((float)Math.toRadians(-rot.get(1).getAsFloat()));
            bone.setRotationZ((float)Math.toRadians(rot.get(2).getAsFloat()));
        }
        if (boneJson.has("cubes")) {
            for (JsonElement cubeElem : boneJson.getAsJsonArray("cubes")) {
                JsonObject cubeJson = cubeElem.getAsJsonObject();
                GeoCube cube = GeoModelParser.parseCube(cubeJson, texWidth, texHeight);
                bone.childCubes.add(cube);
            }
        }
        return bone;
    }

    private static GeoCube parseCube(JsonObject cubeJson, int texWidth, int texHeight) {
        float[] origin = GeoModelParser.parseFloatArray(cubeJson, "origin", new float[]{0.0f, 0.0f, 0.0f});
        float[] size = GeoModelParser.parseFloatArray(cubeJson, "size", new float[]{1.0f, 1.0f, 1.0f});
        float[] pivot = GeoModelParser.parseFloatArray(cubeJson, "pivot", (float[])origin.clone());
        float[] rotation = GeoModelParser.parseFloatArray(cubeJson, "rotation", new float[]{0.0f, 0.0f, 0.0f});
        float inflate = cubeJson.has("inflate") ? cubeJson.get("inflate").getAsFloat() : 0.0f;
        boolean mirror = cubeJson.has("mirror") && cubeJson.get("mirror").getAsBoolean();
        GeoCube cube = new GeoCube(size[0], size[1], size[2]);
        cube.pivot = new Vec3F(-pivot[0], pivot[1], pivot[2]);
        cube.rotation = new Vec3F((float)Math.toRadians(-rotation[0]), (float)Math.toRadians(-rotation[1]), (float)Math.toRadians(rotation[2]));
        cube.inflate = inflate;
        cube.mirror = mirror;
        GeoModelParser.buildCubeQuads(cube, origin, size, inflate, mirror, cubeJson, texWidth, texHeight);
        return cube;
    }

    private static void buildCubeQuads(GeoCube cube, float[] origin, float[] size, float inflate, boolean mirror, JsonObject cubeJson, int texWidth, int texHeight) {
        float ox = origin[0] - inflate;
        float oy = origin[1] - inflate;
        float oz = origin[2] - inflate;
        float sx = size[0] + inflate * 2.0f;
        float sy = size[1] + inflate * 2.0f;
        float sz = size[2] + inflate * 2.0f;
        float u = 0.0f;
        float v = 0.0f;
        boolean perFace = false;
        JsonObject faces = null;
        if (cubeJson.has("uv")) {
            JsonElement uvElem = cubeJson.get("uv");
            if (uvElem.isJsonArray()) {
                JsonArray uvArr = uvElem.getAsJsonArray();
                u = uvArr.get(0).getAsFloat();
                v = uvArr.get(1).getAsFloat();
            } else if (uvElem.isJsonObject()) {
                perFace = true;
                faces = uvElem.getAsJsonObject();
            }
        }
        float minX = -(ox + sx) / 16.0f;
        float minY = oy / 16.0f;
        float minZ = oz / 16.0f;
        float maxX = -ox / 16.0f;
        float maxY = (oy + sy) / 16.0f;
        float maxZ = (oz + sz) / 16.0f;
        if (perFace && faces != null) {
            cube.quads[0] = GeoModelParser.buildQuadPerFace(faces, "west", minX, minY, minZ, minX, maxY, maxZ, -1.0f, 0.0f, 0.0f, texWidth, texHeight);
            cube.quads[1] = GeoModelParser.buildQuadPerFace(faces, "east", maxX, minY, minZ, maxX, maxY, maxZ, 1.0f, 0.0f, 0.0f, texWidth, texHeight);
            cube.quads[2] = GeoModelParser.buildQuadPerFace(faces, "down", minX, minY, minZ, maxX, minY, maxZ, 0.0f, -1.0f, 0.0f, texWidth, texHeight);
            cube.quads[3] = GeoModelParser.buildQuadPerFace(faces, "up", minX, maxY, minZ, maxX, maxY, maxZ, 0.0f, 1.0f, 0.0f, texWidth, texHeight);
            cube.quads[4] = GeoModelParser.buildQuadPerFace(faces, "north", minX, minY, minZ, maxX, maxY, minZ, 0.0f, 0.0f, -1.0f, texWidth, texHeight);
            cube.quads[5] = GeoModelParser.buildQuadPerFace(faces, "south", minX, minY, maxZ, maxX, maxY, maxZ, 0.0f, 0.0f, 1.0f, texWidth, texHeight);
        } else {
            cube.quads[0] = GeoModelParser.buildQuadBox(minX, minY, minZ, minX, maxY, maxZ, -1.0f, 0.0f, 0.0f, u, v, sz, sy, sx, texWidth, texHeight, "west");
            cube.quads[1] = GeoModelParser.buildQuadBox(maxX, minY, minZ, maxX, maxY, maxZ, 1.0f, 0.0f, 0.0f, u, v, sz, sy, sx, texWidth, texHeight, "east");
            cube.quads[2] = GeoModelParser.buildQuadBox(minX, minY, minZ, maxX, minY, maxZ, 0.0f, -1.0f, 0.0f, u, v, sz, sy, sx, texWidth, texHeight, "down");
            cube.quads[3] = GeoModelParser.buildQuadBox(minX, maxY, minZ, maxX, maxY, maxZ, 0.0f, 1.0f, 0.0f, u, v, sz, sy, sx, texWidth, texHeight, "up");
            cube.quads[4] = GeoModelParser.buildQuadBox(minX, minY, minZ, maxX, maxY, minZ, 0.0f, 0.0f, -1.0f, u, v, sz, sy, sx, texWidth, texHeight, "north");
            cube.quads[5] = GeoModelParser.buildQuadBox(minX, minY, maxZ, maxX, maxY, maxZ, 0.0f, 0.0f, 1.0f, u, v, sz, sy, sx, texWidth, texHeight, "south");
        }
    }

    private static GeoQuad buildQuadPerFace(JsonObject faces, String face, float x1, float y1, float z1, float x2, float y2, float z2, float nx, float ny, float nz, int tw, int th) {
        if (!faces.has(face)) {
            return null;
        }
        JsonObject faceObj = faces.getAsJsonObject(face);
        JsonArray uv = faceObj.getAsJsonArray("uv");
        JsonArray uvSize = faceObj.getAsJsonArray("uv_size");
        float u = uv.get(0).getAsFloat() / (float)tw;
        float v = uv.get(1).getAsFloat() / (float)th;
        float us = uvSize.get(0).getAsFloat() / (float)tw;
        float vs = uvSize.get(1).getAsFloat() / (float)th;
        GeoVertex[] vertices = GeoModelParser.buildFaceVertices(face, x1, y1, z1, x2, y2, z2, u, v, us, vs);
        return new GeoQuad(vertices, nx, ny, nz);
    }

    private static GeoQuad buildQuadBox(float x1, float y1, float z1, float x2, float y2, float z2, float nx, float ny, float nz, float u, float v, float sz, float sy, float sx, float tw, float th, String face) {
        float fus;
        float fv;
        float fu;
        float fvs;
        switch (face) {
            case "north" -> {
                fu = (u + sz + sx) / tw;
                fv = (v + sz) / th;
                fus = sx / tw;
                fvs = sy / th;
            }
            case "south" -> {
                fu = (u + sz + sx + sz) / tw;
                fv = (v + sz) / th;
                fus = sx / tw;
                fvs = sy / th;
            }
            case "east" -> {
                fu = u / tw;
                fv = (v + sz) / th;
                fus = sz / tw;
                fvs = sy / th;
            }
            case "west" -> {
                fu = (u + sz + sx) / tw;
                fv = (v + sz) / th;
                fus = sz / tw;
                fvs = sy / th;
            }
            case "up" -> {
                fu = (u + sz) / tw;
                fv = v / th;
                fus = sx / tw;
                fvs = sz / th;
            }
            case "down" -> {
                fu = (u + sz + sx) / tw;
                fv = v / th;
                fus = sx / tw;
                fvs = sz / th;
            }
            default -> {
                fu = 0.0f;
                fv = 0.0f;
                fus = 0.0f;
                fvs = 0.0f;
            }
        }
        GeoVertex[] vertices = GeoModelParser.buildFaceVertices(face, x1, y1, z1, x2, y2, z2, fu, fv, fus, fvs);
        return new GeoQuad(vertices, nx, ny, nz);
    }

    private static GeoVertex[] buildFaceVertices(String face, float x1, float y1, float z1, float x2, float y2, float z2, float u, float v, float us, float vs) {
        GeoVertex[] vertices = new GeoVertex[4];
        float u2 = u + us;
        float v2 = v + vs;
        switch (face) {
            case "north": {
                vertices[0] = new GeoVertex(x2, y2, z1, u, v);
                vertices[1] = new GeoVertex(x1, y2, z1, u2, v);
                vertices[2] = new GeoVertex(x1, y1, z1, u2, v2);
                vertices[3] = new GeoVertex(x2, y1, z1, u, v2);
                break;
            }
            case "south": {
                vertices[0] = new GeoVertex(x1, y2, z2, u, v);
                vertices[1] = new GeoVertex(x2, y2, z2, u2, v);
                vertices[2] = new GeoVertex(x2, y1, z2, u2, v2);
                vertices[3] = new GeoVertex(x1, y1, z2, u, v2);
                break;
            }
            case "east": {
                vertices[0] = new GeoVertex(x2, y2, z2, u, v);
                vertices[1] = new GeoVertex(x2, y2, z1, u2, v);
                vertices[2] = new GeoVertex(x2, y1, z1, u2, v2);
                vertices[3] = new GeoVertex(x2, y1, z2, u, v2);
                break;
            }
            case "west": {
                vertices[0] = new GeoVertex(x1, y2, z1, u, v);
                vertices[1] = new GeoVertex(x1, y2, z2, u2, v);
                vertices[2] = new GeoVertex(x1, y1, z2, u2, v2);
                vertices[3] = new GeoVertex(x1, y1, z1, u, v2);
                break;
            }
            case "up": {
                vertices[0] = new GeoVertex(x1, y2, z1, u, v);
                vertices[1] = new GeoVertex(x1, y2, z2, u, v2);
                vertices[2] = new GeoVertex(x2, y2, z2, u2, v2);
                vertices[3] = new GeoVertex(x2, y2, z1, u2, v);
                break;
            }
            case "down": {
                vertices[0] = new GeoVertex(x2, y1, z1, u, v);
                vertices[1] = new GeoVertex(x2, y1, z2, u, v2);
                vertices[2] = new GeoVertex(x1, y1, z2, u2, v2);
                vertices[3] = new GeoVertex(x1, y1, z1, u2, v);
            }
        }
        return vertices;
    }

    private static float[] parseFloatArray(JsonObject obj, String name, float[] def) {
        if (!obj.has(name)) {
            return def;
        }
        JsonArray arr = obj.getAsJsonArray(name);
        float[] result = new float[arr.size()];
        for (int i = 0; i < arr.size(); ++i) {
            result[i] = arr.get(i).getAsFloat();
        }
        return result;
    }
}

