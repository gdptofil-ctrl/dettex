/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonIOException
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  com.google.gson.JsonSyntaxException
 */
package ru.recoin.api.file.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import ru.recoin.api.feature.draggable.AbstractDraggable;
import ru.recoin.api.feature.draggable.DraggableRepository;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleRepository;
import ru.recoin.api.feature.module.setting.Setting;
import ru.recoin.api.feature.module.setting.implement.BindSetting;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.ColorSetting;
import ru.recoin.api.feature.module.setting.implement.GroupSetting;
import ru.recoin.api.feature.module.setting.implement.MultiColorSetting;
import ru.recoin.api.feature.module.setting.implement.MultiSelectSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.TextSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.api.file.ClientFile;
import ru.recoin.api.file.exception.FileLoadException;
import ru.recoin.api.file.exception.FileSaveException;
import ru.recoin.implement.features.modules.misc.AnyDesk;

public class ModuleFile
extends ClientFile {
    private final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final ModuleRepository moduleRepository;
    private final DraggableRepository draggableRepository;
    private final JsonObject factoryDefaults;

    public ModuleFile(ModuleRepository moduleRepository, DraggableRepository draggableRepository) {
        super("autoCfg");
        this.moduleRepository = moduleRepository;
        this.draggableRepository = draggableRepository;
        this.factoryDefaults = this.createJsonObjectFromModules();
    }

    public void resetToFactoryDefaults() {
        this.updateModulesFromJsonObject(this.factoryDefaults);
    }

    @Override
    public void saveToFile(File path) throws FileSaveException {
        this.saveToFile(path, this.getName() + ".json");
    }

    @Override
    public void loadFromFile(File path) throws FileLoadException {
        this.loadFromFile(path, this.getName() + ".json");
    }

    @Override
    public void saveToFile(File path, String fileName) throws FileSaveException {
        JsonObject functionObject = this.createJsonObjectFromModules();
        File file = new File(path, fileName);
        this.writeJsonToFile(functionObject, file);
        super.saveToFile(path, fileName);
    }

    @Override
    public void loadFromFile(File path, String fileName) throws FileLoadException {
        File file = new File(path, fileName);
        if (!file.exists()) {
            return;
        }
        JsonObject functionObject = this.readJsonFromFile(file);
        if (functionObject != null) {
            this.updateModulesFromJsonObject(functionObject);
        }
        super.loadFromFile(path, fileName);
    }

    private JsonObject createJsonObjectFromModules() {
        JsonObject functionObject = new JsonObject();
        for (Module module : this.moduleRepository.modules()) {
            JsonObject moduleObject = new JsonObject();
            moduleObject.addProperty("bind", (Number)module.getKey());
            moduleObject.addProperty("type", (Number)module.getType());
            moduleObject.addProperty("state", Boolean.valueOf(module.isState()));
            module.settings().forEach(setting -> this.addSettingToJsonObject(moduleObject, (Setting)setting));
            functionObject.add(module.getName().toLowerCase(), (JsonElement)moduleObject);
        }
        for (AbstractDraggable draggable : this.draggableRepository.draggable()) {
            JsonObject draggableObject = new JsonObject();
            draggableObject.addProperty("posX", (Number)draggable.getPreferredX());
            draggableObject.addProperty("posY", (Number)draggable.getPreferredY());
            draggableObject.addProperty("scale", (Number)Float.valueOf(draggable.getElementScale()));
            functionObject.add(draggable.getName().toLowerCase(), (JsonElement)draggableObject);
        }
        return functionObject;
    }

    private void addSettingToJsonObject(JsonObject moduleObject, Setting setting) {
        if (setting instanceof BooleanSetting) {
            BooleanSetting booleanSetting = (BooleanSetting)setting;
            JsonObject booleanObject = new JsonObject();
            booleanObject.addProperty("value", Boolean.valueOf(booleanSetting.isValue()));
            booleanObject.addProperty("bind", (Number)booleanSetting.getKey());
            booleanObject.addProperty("type", (Number)booleanSetting.getType());
            moduleObject.add(setting.getName(), (JsonElement)booleanObject);
        }
        if (setting instanceof ValueSetting) {
            ValueSetting valueSetting = (ValueSetting)setting;
            moduleObject.addProperty(setting.getName(), (Number)Float.valueOf(valueSetting.getValue()));
        }
        if (setting instanceof ColorSetting) {
            ColorSetting colorSetting = (ColorSetting)setting;
            moduleObject.addProperty(setting.getName(), (Number)colorSetting.getColor());
        }
        if (setting instanceof MultiColorSetting) {
            MultiColorSetting multiColorSetting = (MultiColorSetting)setting;
            JsonObject colorObject = new JsonObject();
            for (ColorSetting colorSetting : multiColorSetting.getAllColors()) {
                colorObject.addProperty(colorSetting.getName(), (Number)colorSetting.getColor());
            }
            moduleObject.add(setting.getName(), (JsonElement)colorObject);
        }
        if (setting instanceof BindSetting) {
            BindSetting bindSetting = (BindSetting)setting;
            moduleObject.addProperty(setting.getName(), (Number)bindSetting.getKey());
        }
        if (setting instanceof TextSetting) {
            TextSetting textSetting = (TextSetting)setting;
            moduleObject.addProperty(setting.getName(), textSetting.getText());
        }
        if (setting instanceof SelectSetting) {
            SelectSetting selectSetting = (SelectSetting)setting;
            moduleObject.addProperty(setting.getName(), selectSetting.getSelected());
        }
        if (setting instanceof MultiSelectSetting) {
            MultiSelectSetting multiSelectSetting = (MultiSelectSetting)setting;
            List<String> selected = multiSelectSetting.getSelected();
            String selectedAsString = String.join((CharSequence)",", selected);
            moduleObject.addProperty(setting.getName(), selectedAsString);
        }
        if (setting instanceof GroupSetting) {
            GroupSetting groupSetting = (GroupSetting)setting;
            JsonObject groupObject = new JsonObject();
            groupObject.addProperty("state", Boolean.valueOf(groupSetting.isValue()));
            for (Setting subSetting : groupSetting.getSubSettings()) {
                this.addSettingToJsonObject(groupObject, subSetting);
            }
            moduleObject.add(setting.getName(), (JsonElement)groupObject);
        }
    }

    private void writeJsonToFile(JsonObject functionObject, File file) throws FileSaveException {
        try (FileWriter writer = new FileWriter(file);){
            this.GSON.toJson((JsonElement)functionObject, (Appendable)writer);
            writer.flush();
        }
        catch (IOException e) {
            throw new FileSaveException("Failed to save module to file", e);
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private JsonObject readJsonFromFile(File file) throws FileLoadException {
        if (!file.exists()) return null;
        if (file.length() == 0L) {
            return null;
        }
        try (FileReader reader = new FileReader(file);){
            JsonElement element = JsonParser.parseReader((Reader)reader);
            if (element != null && element.isJsonObject()) {
                JsonObject jsonObject = element.getAsJsonObject();
                return jsonObject;
            }
            file.delete();
            JsonObject jsonObject = null;
            return jsonObject;
        }
        catch (IOException e) {
            throw new FileLoadException("Failed to load module from file", e);
        }
        catch (JsonIOException | JsonSyntaxException | IllegalStateException e) {
            file.delete();
            return null;
        }
    }

    private void updateModulesFromJsonObject(JsonObject functionObject) {
        for (Module module : this.moduleRepository.modules()) {
            JsonObject moduleObject = this.getModuleObject(functionObject, module);
            if (moduleObject == null) continue;
            if (!moduleObject.has("\u0412\u0440\u0435\u043c\u044f \u0436\u0438\u0437\u043d\u0438")) {
                String oldLifetime = null;
                switch (module.getName()) {
                    case "HitBubble": {
                        String string = "\u0414\u043b\u0438\u0442\u0435\u043b\u044c\u043d\u043e\u0441\u0442\u044c";
                        break;
                    }
                    case "JumpCircle": {
                        String string = "Duration";
                        break;
                    }
                    default: {
                        String string = oldLifetime = null;
                    }
                }
                if (oldLifetime != null && moduleObject.has(oldLifetime)) {
                    moduleObject.add("\u0412\u0440\u0435\u043c\u044f \u0436\u0438\u0437\u043d\u0438", moduleObject.get(oldLifetime));
                }
            }
            if (moduleObject.has("bind")) {
                module.setKey(moduleObject.get("bind").getAsInt());
            }
            if (moduleObject.has("type")) {
                module.setType(moduleObject.get("type").getAsInt());
            }
            if (moduleObject.has("state")) {
                module.setState(moduleObject.get("state").getAsBoolean());
            }
            module.settings().forEach(setting -> this.updateSettingFromJsonObject(moduleObject, (Setting)setting));
            if (!(module instanceof AnyDesk)) continue;
            AnyDesk anyDesk = (AnyDesk)module;
            anyDesk.loadConfig(moduleObject);
        }
        for (AbstractDraggable draggable : this.draggableRepository.draggable()) {
            JsonObject draggableObject = functionObject.getAsJsonObject(draggable.getName().toLowerCase());
            if (draggableObject == null) continue;
            if (draggableObject.has("posX") && draggableObject.has("posY")) {
                draggable.setX(draggableObject.get("posX").getAsInt());
                draggable.setY(draggableObject.get("posY").getAsInt());
            }
            if (!draggableObject.has("scale")) continue;
            draggable.setElementScale(draggableObject.get("scale").getAsFloat());
        }
    }

    private JsonObject getModuleObject(JsonObject functionObject, Module module) {
        String moduleName = module.getName().toLowerCase();
        JsonObject moduleObject = functionObject.getAsJsonObject(moduleName);
        if (moduleObject == null && "interface".equals(moduleName)) {
            moduleObject = functionObject.getAsJsonObject("hud");
        }
        return moduleObject;
    }

    private void updateSettingFromJsonObject(JsonObject moduleObject, Setting setting) {
        JsonElement settingElement = moduleObject.get(setting.getName());
        if (settingElement == null && "\u0420\u0435\u043f\u043e\u0440\u0442\u0438\u0442\u044c \u043e\u0431\u0438\u0434\u0447\u0438\u043a\u043e\u0432".equals(setting.getName())) {
            settingElement = moduleObject.get("\u0420\u0435\u043f\u043e\u0440\u0442\u0438\u0442\u044c \u043a\u0438\u043b\u043b\u0435\u0440\u043e\u0432");
        }
        if (settingElement == null && "Backtrack".equals(setting.getName())) {
            settingElement = moduleObject.get("Historical Backtracks");
        }
        if (settingElement == null || settingElement.isJsonNull()) {
            return;
        }
        if (setting instanceof GroupSetting) {
            GroupSetting groupSetting = (GroupSetting)setting;
            if (!settingElement.isJsonObject()) {
                return;
            }
            JsonObject groupObject = settingElement.getAsJsonObject();
            if (groupObject.has("state")) {
                Setting setting2;
                String enabledName = null;
                groupSetting.setValue(groupObject.get("state").getAsBoolean());
                switch (groupSetting.getName()) {
                    case "\u041c\u0435\u0447\u0438": {
                        Object object = "\u0412\u043a\u043b\u044e\u0447\u0438\u0442\u044c \u043c\u0435\u0447\u0438";
                        break;
                    }
                    case "\u0411\u0443\u043b\u0430\u0432\u044b": {
                        Object object = "\u0412\u043a\u043b\u044e\u0447\u0438\u0442\u044c \u0431\u0443\u043b\u0430\u0432\u044b";
                        break;
                    }
                    default: {
                        Object object = enabledName = "";
                    }
                }
                if (!groupSetting.isCheckbox() && !groupObject.has((String)enabledName) && (setting2 = groupSetting.getSubSetting((String)enabledName)) instanceof BooleanSetting) {
                    BooleanSetting enabled = (BooleanSetting)setting2;
                    enabled.setValue(groupSetting.isValue());
                }
            }
            for (Setting subSetting : groupSetting.getSubSettings()) {
                this.updateSettingFromJsonObject(groupObject, subSetting);
            }
            return;
        }
        if (setting instanceof MultiColorSetting) {
            MultiColorSetting multiColorSetting = (MultiColorSetting)setting;
            if (!settingElement.isJsonObject()) {
                return;
            }
            JsonObject colorObject = settingElement.getAsJsonObject();
            for (ColorSetting colorSetting : multiColorSetting.getAllColors()) {
                JsonElement colorElement = colorObject.get(colorSetting.getName());
                if (colorElement == null || !colorElement.isJsonPrimitive()) continue;
                colorSetting.setColor(colorElement.getAsInt());
            }
            return;
        }
        if (setting instanceof BooleanSetting) {
            BooleanSetting booleanSetting = (BooleanSetting)setting;
            if (settingElement.isJsonObject()) {
                JsonObject booleanObject = settingElement.getAsJsonObject();
                if (booleanObject.has("value")) {
                    booleanSetting.setValue(booleanObject.get("value").getAsBoolean());
                }
                if (booleanObject.has("bind")) {
                    booleanSetting.setKey(booleanObject.get("bind").getAsInt());
                }
                if (booleanObject.has("type")) {
                    booleanSetting.setType(booleanObject.get("type").getAsInt());
                }
            } else if (settingElement.isJsonPrimitive()) {
                booleanSetting.setValue(settingElement.getAsBoolean());
            }
            return;
        }
        if (!settingElement.isJsonPrimitive()) {
            return;
        }
        if (setting instanceof ValueSetting) {
            ValueSetting valueSetting = (ValueSetting)setting;
            valueSetting.setValue(settingElement.getAsFloat());
        }
        if (setting instanceof ColorSetting) {
            ColorSetting colorSetting = (ColorSetting)setting;
            colorSetting.setColor(settingElement.getAsInt());
        }
        if (setting instanceof BindSetting) {
            BindSetting bindSetting = (BindSetting)setting;
            bindSetting.setKey(settingElement.getAsInt());
        }
        if (setting instanceof TextSetting) {
            TextSetting textSetting = (TextSetting)setting;
            textSetting.setText(settingElement.getAsString());
        }
        if (setting instanceof SelectSetting) {
            SelectSetting selectSetting = (SelectSetting)setting;
            String selected = settingElement.getAsString();
            if ("Bypass".equals(setting.getName()) && selectSetting.getList().equals(List.of("Default", "Vanilla"))) {
                selected = "Vanilla".equalsIgnoreCase(selected) ? "Vanilla" : "Default";
            } else if ("Sprint Reset".equals(setting.getName()) && selectSetting.getList().contains("Packet")) {
                selected = switch (selected) {
                    case "Old" -> "Legit";
                    case "TEST" -> "Fast";
                    case "SpookyTime", "New" -> "Off";
                    default -> selected;
                };
            } else if ("Mode".equals(setting.getName()) && selectSetting.getList().equals(List.of("Normal", "Universal")) && ("RW 1.16".equals(selected) || "RW 1.21".equals(selected))) {
                selected = "Normal";
            }
            if (selectSetting.getList().contains(selected)) {
                selectSetting.setSelected(selected);
            }
        }
        if (setting instanceof MultiSelectSetting) {
            MultiSelectSetting multiSelectSetting = (MultiSelectSetting)setting;
            String asString2 = settingElement.getAsString();
            ArrayList<String> selectedList = new ArrayList<String>(Arrays.asList(asString2.split(",")));
            selectedList.removeIf(s -> !multiSelectSetting.getList().contains(s));
            multiSelectSetting.setSelected(selectedList);
        }
    }
}

