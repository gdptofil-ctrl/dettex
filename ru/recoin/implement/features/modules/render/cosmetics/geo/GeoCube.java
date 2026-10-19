/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.features.modules.render.cosmetics.geo;

import ru.recoin.implement.features.modules.render.cosmetics.geo.GeoQuad;
import ru.recoin.implement.features.modules.render.cosmetics.geo.Vec3F;

public class GeoCube {
    public GeoQuad[] quads = new GeoQuad[6];
    public Vec3F size;
    public Vec3F pivot;
    public Vec3F rotation;
    public float inflate;
    public boolean mirror;

    public GeoCube(float x, float y, float z) {
        this.size = new Vec3F(x, y, z);
        this.pivot = new Vec3F(0.0f, 0.0f, 0.0f);
        this.rotation = new Vec3F(0.0f, 0.0f, 0.0f);
        this.inflate = 0.0f;
        this.mirror = false;
    }
}

