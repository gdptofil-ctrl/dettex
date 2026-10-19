/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.api.feature.module.setting.implement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import ru.recoin.api.feature.module.setting.Setting;
import ru.recoin.api.feature.module.setting.implement.ColorSetting;

public class MultiColorSetting
extends Setting {
    private final List<ColorSetting> colors = new ArrayList<ColorSetting>();
    private int selectedColorIndex = 0;

    public MultiColorSetting(String name, String description) {
        super(name, description);
    }

    public MultiColorSetting colors(String ... colorNames) {
        this.colors.clear();
        for (int i = 0; i < colorNames.length; ++i) {
            this.colors.add(new ColorSetting(colorNames[i], "Color " + (i + 1) + " for " + this.getName()));
        }
        return this;
    }

    public MultiColorSetting defaultColors(int ... defaultColors) {
        for (int i = 0; i < Math.min(this.colors.size(), defaultColors.length); ++i) {
            this.colors.get(i).setColor(defaultColors[i]);
        }
        return this;
    }

    public MultiColorSetting visible(Supplier<Boolean> visible) {
        this.setVisible(visible);
        for (ColorSetting color : this.colors) {
            color.setVisible(visible);
        }
        return this;
    }

    public ColorSetting getColor1() {
        return !this.colors.isEmpty() ? this.colors.getFirst() : null;
    }

    public ColorSetting getColor2() {
        return this.colors.size() > 1 ? this.colors.get(1) : null;
    }

    public ColorSetting getColor3() {
        return this.colors.size() > 2 ? this.colors.get(2) : null;
    }

    public ColorSetting getColor(int index) {
        if (index >= 0 && index < this.colors.size()) {
            return this.colors.get(index);
        }
        return null;
    }

    public int[] getColorValues() {
        return this.colors.stream().mapToInt(ColorSetting::getColor).toArray();
    }

    public int getColorCount() {
        return this.colors.size();
    }

    public List<ColorSetting> getAllColors() {
        return new ArrayList<ColorSetting>(this.colors);
    }

    public boolean isModified() {
        return false;
    }

    public void reset() {
    }

    public List<ColorSetting> getColors() {
        return this.colors;
    }

    public int getSelectedColorIndex() {
        return this.selectedColorIndex;
    }

    public void setSelectedColorIndex(int selectedColorIndex) {
        this.selectedColorIndex = selectedColorIndex;
    }
}

