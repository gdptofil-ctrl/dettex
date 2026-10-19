/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.api.feature.module;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ru.recoin.api.feature.module.Module;

public class ModuleProvider {
    private final List<Module> modules;
    private final Map<String, Module> nameMap = new HashMap<String, Module>();
    private final Map<Class<?>, Module> classMap = new HashMap();

    public ModuleProvider(List<Module> modules) {
        this.modules = modules;
        for (Module module : modules) {
            this.nameMap.put(module.getName().toLowerCase(), module);
            this.classMap.put(module.getClass(), module);
        }
    }

    public <T extends Module> T get(String name) {
        if (name == null) {
            return null;
        }
        return (T)this.nameMap.get(name.toLowerCase());
    }

    public <T extends Module> T get(Class<T> clazz) {
        if (clazz == null) {
            return null;
        }
        Module mod = this.classMap.get(clazz);
        if (mod != null) {
            return (T)mod;
        }
        for (Module m : this.modules) {
            if (!clazz.isAssignableFrom(m.getClass())) continue;
            this.classMap.put(clazz, m);
            return (T)m;
        }
        return null;
    }

    public List<Module> getModules() {
        return this.modules;
    }

    public Map<String, Module> getNameMap() {
        return this.nameMap;
    }

    public Map<Class<?>, Module> getClassMap() {
        return this.classMap;
    }
}

