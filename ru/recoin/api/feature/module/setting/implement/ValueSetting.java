/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.api.feature.module.setting.implement;

import java.util.function.Supplier;
import ru.recoin.api.feature.module.setting.Setting;

public class ValueSetting
extends Setting {
    private float value;
    private float min;
    private float max;
    private float step;
    private float defaultValue;
    private boolean integer;
    private boolean defaultValueInitialized;

    public ValueSetting(String name, String description) {
        super(name, description);
    }

    public ValueSetting range(float min, float max) {
        this.min = min;
        this.max = max;
        this.value = this.normalizeValue(this.value);
        return this;
    }

    public ValueSetting range(int min, int max) {
        this.min = min;
        this.max = max;
        this.integer = true;
        this.value = this.normalizeValue(this.value);
        return this;
    }

    public int getInt() {
        return (int)this.value;
    }

    public ValueSetting setValue(float value) {
        this.value = this.normalizeValue(value);
        if (!this.defaultValueInitialized) {
            this.defaultValue = this.value;
            this.defaultValueInitialized = true;
        }
        return this;
    }

    public ValueSetting step(float step) {
        this.step = step;
        this.value = this.normalizeValue(this.value);
        this.defaultValue = this.normalizeValue(this.defaultValue);
        return this;
    }

    public ValueSetting visible(Supplier<Boolean> visible) {
        this.setVisible(visible);
        return this;
    }

    private float normalizeValue(float value) {
        float normalized = value;
        if (this.max > this.min) {
            normalized = Math.max(this.min, Math.min(this.max, normalized));
        }
        if (this.integer) {
            normalized = (int)normalized;
        } else if (this.step > 0.0f) {
            normalized = this.min + (float)Math.round((normalized - this.min) / this.step) * this.step;
            if (this.max > this.min) {
                normalized = Math.max(this.min, Math.min(this.max, normalized));
            }
        }
        return normalized;
    }

    public float getValue() {
        return this.value;
    }

    public float getMin() {
        return this.min;
    }

    public float getMax() {
        return this.max;
    }

    public float getStep() {
        return this.step;
    }

    public float getDefaultValue() {
        return this.defaultValue;
    }

    public boolean isInteger() {
        return this.integer;
    }

    public boolean isDefaultValueInitialized() {
        return this.defaultValueInitialized;
    }

    public ValueSetting setMin(float min) {
        this.min = min;
        return this;
    }

    public ValueSetting setMax(float max) {
        this.max = max;
        return this;
    }

    public ValueSetting setStep(float step) {
        this.step = step;
        return this;
    }

    public ValueSetting setDefaultValue(float defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public ValueSetting setInteger(boolean integer) {
        this.integer = integer;
        return this;
    }

    public ValueSetting setDefaultValueInitialized(boolean defaultValueInitialized) {
        this.defaultValueInitialized = defaultValueInitialized;
        return this;
    }
}

