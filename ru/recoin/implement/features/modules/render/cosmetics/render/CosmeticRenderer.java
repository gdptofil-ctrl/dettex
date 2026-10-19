/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_4587
 *  net.minecraft.class_4588
 *  net.minecraft.class_4597
 *  net.minecraft.class_591
 *  net.minecraft.class_630
 *  net.minecraft.class_742
 */
package ru.recoin.implement.features.modules.render.cosmetics.render;

import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_591;
import net.minecraft.class_630;
import net.minecraft.class_742;
import ru.recoin.implement.features.modules.render.cosmetics.model.CosmeticModel;
import ru.recoin.implement.features.modules.render.cosmetics.model.ModelPosition;
import ru.recoin.implement.features.modules.render.cosmetics.render.GeckolibCosmeticRenderer;
import ru.recoin.implement.features.modules.render.cosmetics.render.RenderStack;

public class CosmeticRenderer {
    private static CosmeticRenderer instance;
    private final GeckolibCosmeticRenderer geckolibRenderer = GeckolibCosmeticRenderer.getInstance();
    private final RenderStack stack = new RenderStack();

    public static CosmeticRenderer getInstance() {
        if (instance == null) {
            instance = new CosmeticRenderer();
        }
        return instance;
    }

    public void renderCosmetic(CosmeticModel cosmetic, class_742 player, class_4587 matrices, class_4597 provider, int light, class_591 playerModel, float tickDelta) {
        if (cosmetic != null && cosmetic.getTextureId() != null) {
            this.stack.update(matrices);
            this.stack.push();
            float yOffset = this.transformToPosition(cosmetic, playerModel);
            this.stack.rotateZDegrees(180.0f);
            this.stack.translate(cosmetic.getX(), cosmetic.getY() + yOffset, cosmetic.getZ());
            this.stack.rotateYDegrees(cosmetic.getYaw());
            this.stack.rotateXDegrees(cosmetic.getPitch());
            this.stack.rotateZDegrees(cosmetic.getRoll());
            this.stack.scale(cosmetic.getScale(), cosmetic.getScale(), cosmetic.getScale());
            this.geckolibRenderer.renderCosmetic(cosmetic, matrices, provider, light);
            this.stack.pop();
        }
    }

    public void renderCosmetic(CosmeticModel cosmetic, class_742 player, class_4587 matrices, class_4588 vertexConsumer, int light, class_591 playerModel, float tickDelta) {
        if (cosmetic != null && cosmetic.getTextureId() != null && vertexConsumer != null) {
            this.stack.update(matrices);
            this.stack.push();
            float yOffset = this.transformToPosition(cosmetic, playerModel);
            this.stack.rotateZDegrees(180.0f);
            this.stack.translate(cosmetic.getX(), cosmetic.getY() + yOffset, cosmetic.getZ());
            this.stack.rotateYDegrees(cosmetic.getYaw());
            this.stack.rotateXDegrees(cosmetic.getPitch());
            this.stack.rotateZDegrees(cosmetic.getRoll());
            this.stack.scale(cosmetic.getScale(), cosmetic.getScale(), cosmetic.getScale());
            this.geckolibRenderer.renderCosmetic(cosmetic, matrices, vertexConsumer, light);
            this.stack.pop();
        }
    }

    private float transformToPosition(CosmeticModel cosmetic, class_591 model) {
        float yOffset = 0.0f;
        ModelPosition pos = cosmetic.getPosition();
        if (model == null) {
            return yOffset;
        }
        switch (pos) {
            case HEAD: {
                this.transformToModelPart(model.field_3398);
                yOffset = 0.5f;
                break;
            }
            case ABOVE_HEAD: {
                yOffset = 0.75f;
                break;
            }
            case BODY: {
                this.transformToModelPart(model.field_3391);
                yOffset = -0.3f;
                break;
            }
            case RIGHT_ARM: {
                this.transformToModelPart(model.field_3401);
                yOffset = -0.25f;
                break;
            }
            case LEFT_ARM: {
                this.transformToModelPart(model.field_27433);
                yOffset = -0.25f;
                break;
            }
            case RIGHT_LEG: {
                this.transformToModelPart(model.field_3392);
                yOffset = -0.35f;
                break;
            }
            case LEFT_LEG: {
                this.transformToModelPart(model.field_3397);
                yOffset = -0.35f;
                break;
            }
        }
        return yOffset;
    }

    private void transformToModelPart(class_630 part) {
        part.method_22703(this.stack.get());
    }
}

