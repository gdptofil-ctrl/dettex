/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.features.modules.render.cosmetics.geo;

import ru.recoin.implement.features.modules.render.cosmetics.geo.GeoVertex;
import ru.recoin.implement.features.modules.render.cosmetics.geo.Vec3F;

public class GeoQuad {
    public GeoVertex[] vertices;
    public Vec3F normal;

    public GeoQuad(GeoVertex[] vertices, Vec3F normal) {
        this.vertices = vertices;
        this.normal = normal;
    }

    public GeoQuad(GeoVertex[] vertices, float nx, float ny, float nz) {
        this.vertices = vertices;
        this.normal = new Vec3F(nx, ny, nz);
    }
}

