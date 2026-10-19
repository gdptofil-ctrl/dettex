/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.api.system.font.glyph;

import ru.recoin.api.system.font.glyph.GlyphMap;

public record Glyph(int u, int v, int width, int height, char value, GlyphMap owner) {
}

