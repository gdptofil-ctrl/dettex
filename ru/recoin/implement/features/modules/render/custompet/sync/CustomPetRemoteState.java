/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_243
 */
package ru.recoin.implement.features.modules.render.custompet.sync;

import net.minecraft.class_243;
import ru.recoin.implement.features.modules.render.custompet.CustomPetVariant;

public record CustomPetRemoteState(String identityKey, String profileUsername, String minecraftUsername, CustomPetVariant variant, int robotType, boolean active, double x, double y, double z, float yaw, boolean moving, boolean umbrella, boolean airborne, double animationSpeed, String petKind) {
    public CustomPetRemoteState {
        identityKey = identityKey == null ? "" : identityKey.trim();
        profileUsername = profileUsername == null ? "" : profileUsername.trim();
        minecraftUsername = minecraftUsername == null ? "" : minecraftUsername.trim();
        variant = variant == null ? CustomPetVariant.NITWIT : variant;
        animationSpeed = animationSpeed <= 0.0 ? 1.0 : animationSpeed;
        petKind = petKind == null || petKind.isBlank() ? "frog" : petKind.trim();
    }

    public class_243 position() {
        return new class_243(this.x, this.y, this.z);
    }

    public boolean isOwl() {
        return "owl".equals(this.petKind);
    }
}

