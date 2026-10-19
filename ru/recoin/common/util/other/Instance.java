/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.common.util.other;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import ru.recoin.api.feature.draggable.AbstractDraggable;
import ru.recoin.api.feature.module.Module;
import ru.recoin.core.Main;

public final class Instance {
    private static final ConcurrentMap<Class<? extends Module>, Module> instanceModules = new ConcurrentHashMap<Class<? extends Module>, Module>();
    private static final ConcurrentMap<Class<? extends AbstractDraggable>, AbstractDraggable> instanceDraggables = new ConcurrentHashMap<Class<? extends AbstractDraggable>, AbstractDraggable>();

    public static <T extends Module> T get(Class<T> clazz) {
        return (T)((Module)clazz.cast(instanceModules.computeIfAbsent(clazz, instance -> Main.getInstance().getModuleProvider().get(instance))));
    }

    public static <T extends Module> T get(String module) {
        return Main.getInstance().getModuleProvider().get(module);
    }

    public static <T extends AbstractDraggable> T getDraggable(Class<T> clazz) {
        return (T)((AbstractDraggable)clazz.cast(instanceDraggables.computeIfAbsent(clazz, instance -> Main.getInstance().getDraggableRepository().get(instance))));
    }

    public static <T extends AbstractDraggable> T getDraggable(String draggable) {
        return Main.getInstance().getDraggableRepository().get(draggable);
    }

    private Instance() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

