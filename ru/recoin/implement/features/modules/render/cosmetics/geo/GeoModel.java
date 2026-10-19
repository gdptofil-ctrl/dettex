/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.features.modules.render.cosmetics.geo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ru.recoin.implement.features.modules.render.cosmetics.geo.GeoBone;

public class GeoModel {
    public List<GeoBone> topLevelBones = new ArrayList<GeoBone>();
    public int textureWidth = 64;
    public int textureHeight = 64;

    public Optional<GeoBone> getBone(String name) {
        for (GeoBone bone : this.topLevelBones) {
            GeoBone found = this.getBoneRecursively(name, bone);
            if (found == null) continue;
            return Optional.of(found);
        }
        return Optional.empty();
    }

    private GeoBone getBoneRecursively(String name, GeoBone current) {
        if (current.name.equals(name)) {
            return current;
        }
        for (GeoBone child : current.childBones) {
            GeoBone found = this.getBoneRecursively(name, child);
            if (found == null) continue;
            return found;
        }
        return null;
    }
}

