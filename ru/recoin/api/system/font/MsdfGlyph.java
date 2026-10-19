/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.api.system.font;

final class MsdfGlyph {
    final int id;
    final float x;
    final float y;
    final float width;
    final float height;
    final float xOffset;
    final float yOffset;
    final float xAdvance;
    final float u0;
    final float v0;
    final float u1;
    final float v1;

    MsdfGlyph(int id, float x, float y, float width, float height, float xOffset, float yOffset, float xAdvance, float atlasWidth, float atlasHeight) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.xAdvance = xAdvance;
        this.u0 = x / atlasWidth;
        this.v0 = y / atlasHeight;
        this.u1 = (x + width) / atlasWidth;
        this.v1 = (y + height) / atlasHeight;
    }
}

