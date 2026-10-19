/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.api.feature.module.setting.implement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import ru.recoin.api.feature.module.setting.Setting;

public class GroupSetting
extends Setting {
    private boolean value;
    private boolean checkbox = true;
    private List<Setting> subSettings = new ArrayList<Setting>();

    public GroupSetting(String name, String description) {
        super(name, description);
    }

    public GroupSetting(String name, String description, boolean checkbox) {
        super(name, description);
        this.checkbox = checkbox;
    }

    public GroupSetting settings(Setting ... setting) {
        this.subSettings.addAll(Arrays.asList(setting));
        return this;
    }

    public GroupSetting visible(Supplier<Boolean> visible) {
        this.setVisible(visible);
        return this;
    }

    public Setting getSubSetting(String name) {
        return this.subSettings.stream().filter(setting -> setting.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public boolean isValue() {
        return this.value;
    }

    public boolean isCheckbox() {
        return this.checkbox;
    }

    public List<Setting> getSubSettings() {
        return this.subSettings;
    }

    public GroupSetting setValue(boolean value) {
        this.value = value;
        return this;
    }

    public GroupSetting setCheckbox(boolean checkbox) {
        this.checkbox = checkbox;
        return this;
    }

    public GroupSetting setSubSettings(List<Setting> subSettings) {
        this.subSettings = subSettings;
        return this;
    }
}

