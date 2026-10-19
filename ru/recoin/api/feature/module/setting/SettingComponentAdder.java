/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.api.feature.module.setting;

import java.util.List;
import ru.recoin.api.feature.module.setting.Setting;
import ru.recoin.api.feature.module.setting.implement.BindSetting;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.ButtonSetting;
import ru.recoin.api.feature.module.setting.implement.ColorSetting;
import ru.recoin.api.feature.module.setting.implement.GroupSetting;
import ru.recoin.api.feature.module.setting.implement.MultiColorSetting;
import ru.recoin.api.feature.module.setting.implement.MultiSelectSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.TextSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.implement.features.modules.movement.fireworkbooster.BoosterSectionHeaderComponent;
import ru.recoin.implement.features.modules.movement.fireworkbooster.BoosterSectionHeaderSetting;
import ru.recoin.implement.screens.menu.components.implement.settings.AbstractSettingComponent;
import ru.recoin.implement.screens.menu.components.implement.settings.BindComponent;
import ru.recoin.implement.screens.menu.components.implement.settings.CheckboxComponent;
import ru.recoin.implement.screens.menu.components.implement.settings.ColorComponent;
import ru.recoin.implement.screens.menu.components.implement.settings.GroupComponent;
import ru.recoin.implement.screens.menu.components.implement.settings.SButtonComponent;
import ru.recoin.implement.screens.menu.components.implement.settings.TextComponent;
import ru.recoin.implement.screens.menu.components.implement.settings.ValueComponent;
import ru.recoin.implement.screens.menu.components.implement.settings.multiselect.MultiSelectComponent;
import ru.recoin.implement.screens.menu.components.implement.settings.select.SelectComponent;

public class SettingComponentAdder {
    public void addSettingComponent(List<Setting> settings, List<AbstractSettingComponent> components) {
        settings.forEach(setting -> {
            if (setting instanceof BoosterSectionHeaderSetting) {
                BoosterSectionHeaderSetting header = (BoosterSectionHeaderSetting)setting;
                components.add(new BoosterSectionHeaderComponent(header));
            }
            if (setting instanceof BooleanSetting) {
                BooleanSetting booleanSetting = (BooleanSetting)setting;
                components.add(new CheckboxComponent(booleanSetting));
            }
            if (setting instanceof BindSetting) {
                BindSetting bindSetting = (BindSetting)setting;
                components.add(new BindComponent(bindSetting));
            }
            if (setting instanceof ColorSetting) {
                ColorSetting colorSetting2 = (ColorSetting)setting;
                components.add(new ColorComponent(colorSetting2));
            }
            if (setting instanceof MultiColorSetting) {
                MultiColorSetting multiColorSetting = (MultiColorSetting)setting;
                multiColorSetting.getAllColors().forEach(colorSetting -> components.add(new ColorComponent((ColorSetting)colorSetting)));
            }
            if (setting instanceof TextSetting) {
                TextSetting textSetting = (TextSetting)setting;
                components.add(new TextComponent(textSetting));
            }
            if (setting instanceof ValueSetting) {
                ValueSetting valueSetting = (ValueSetting)setting;
                components.add(new ValueComponent(valueSetting));
            }
            if (setting instanceof GroupSetting) {
                GroupSetting groupSetting = (GroupSetting)setting;
                components.add(new GroupComponent(groupSetting));
            }
            if (setting instanceof ButtonSetting) {
                ButtonSetting buttonSetting = (ButtonSetting)setting;
                components.add(new SButtonComponent(buttonSetting));
            }
            if (setting instanceof SelectSetting) {
                SelectSetting selectSetting = (SelectSetting)setting;
                components.add(new SelectComponent(selectSetting));
            }
            if (setting instanceof MultiSelectSetting) {
                MultiSelectSetting multiSelectSetting = (MultiSelectSetting)setting;
                components.add(new MultiSelectComponent(multiSelectSetting));
            }
        });
    }
}

