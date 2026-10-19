/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.api.system.font.entry;

import ru.recoin.api.system.font.glyph.Glyph;

public record DrawEntry(float atX, float atY, int color, Glyph toDraw) {
}

