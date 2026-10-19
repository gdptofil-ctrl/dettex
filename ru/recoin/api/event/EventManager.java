/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 *  net.minecraft.class_2561
 */
package ru.recoin.api.event;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.event.events.Event;
import ru.recoin.api.event.events.EventStoppable;
import ru.recoin.api.event.types.Priority;
import ru.recoin.api.feature.module.exception.ModuleException;
import ru.recoin.api.system.logger.implement.MinecraftLogger;
import ru.recoin.common.util.logger.LoggerUtil;

public final class EventManager {
    private static final Map<Class<? extends Event>, List<MethodData>> REGISTRY_MAP = new HashMap<Class<? extends Event>, List<MethodData>>();
    private static volatile Map<Class<? extends Event>, MethodData[]> CACHE_MAP = new HashMap<Class<? extends Event>, MethodData[]>();
    private static final MinecraftLogger MINECRAFT_LOGGER = new MinecraftLogger();

    public static boolean hasSubscribers(Class<? extends Event> eventClass) {
        return CACHE_MAP.containsKey(eventClass);
    }

    private static synchronized void updateCache() {
        HashMap<Class<? extends Event>, MethodData[]> newCache = new HashMap<Class<? extends Event>, MethodData[]>();
        for (Map.Entry<Class<? extends Event>, List<MethodData>> entry : REGISTRY_MAP.entrySet()) {
            List<MethodData> list = entry.getValue();
            if (list == null || list.isEmpty()) continue;
            newCache.put(entry.getKey(), list.toArray(new MethodData[0]));
        }
        CACHE_MAP = newCache;
    }

    public synchronized void register(Object object) {
        for (Method method : object.getClass().getDeclaredMethods()) {
            if (EventManager.isMethodBad(method)) continue;
            this.register(method, object);
        }
        EventManager.updateCache();
    }

    public synchronized void register(Object object, Class<? extends Event> eventClass) {
        for (Method method : object.getClass().getDeclaredMethods()) {
            if (EventManager.isMethodBad(method, eventClass)) continue;
            this.register(method, object);
        }
        EventManager.updateCache();
    }

    public synchronized void unregister(Object object) {
        for (List<MethodData> dataList : REGISTRY_MAP.values()) {
            for (MethodData data : dataList) {
                if (!data.source().equals(object)) continue;
                dataList.remove(data);
            }
        }
        EventManager.cleanMap(true);
        EventManager.updateCache();
    }

    public synchronized void unregister(Object object, Class<? extends Event> eventClass) {
        if (REGISTRY_MAP.containsKey(eventClass)) {
            for (MethodData data : REGISTRY_MAP.get(eventClass)) {
                if (!data.source().equals(object)) continue;
                REGISTRY_MAP.get(eventClass).remove(data);
            }
            EventManager.cleanMap(true);
            EventManager.updateCache();
        }
    }

    private void register(Method method, Object object) {
        EventInvoker invoker;
        Class<? extends Event> indexClass = (Class<? extends Event>)method.getParameterTypes()[0];
        try {
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            MethodHandle raw = MethodHandles.lookup().unreflect(method).bindTo(object);
            MethodHandle adapted = raw.asType(MethodType.methodType(Void.TYPE, Event.class));
            invoker = e -> adapted.invoke(e);
        }
        catch (Throwable t) {
            invoker = e -> method.invoke(object, e);
        }
        final MethodData data = new MethodData(object, method, invoker, method.getAnnotation(EventHandler.class).value());
        if (REGISTRY_MAP.containsKey(indexClass)) {
            if (!REGISTRY_MAP.get(indexClass).contains(data)) {
                REGISTRY_MAP.get(indexClass).add(data);
                EventManager.sortListValue(indexClass);
            }
        } else {
            REGISTRY_MAP.put(indexClass, (List<MethodData>)new CopyOnWriteArrayList<MethodData>(){
                private static final long serialVersionUID = 666L;
                {
                    this.add(data);
                }
            });
        }
    }

    public synchronized void removeEntry(Class<? extends Event> indexClass) {
        Iterator<Map.Entry<Class<? extends Event>, List<MethodData>>> mapIterator = REGISTRY_MAP.entrySet().iterator();
        while (mapIterator.hasNext()) {
            if (!mapIterator.next().getKey().equals(indexClass)) continue;
            mapIterator.remove();
            break;
        }
        EventManager.updateCache();
    }

    public static synchronized void cleanMap(boolean onlyEmptyEntries) {
        Iterator<Map.Entry<Class<? extends Event>, List<MethodData>>> mapIterator = REGISTRY_MAP.entrySet().iterator();
        while (mapIterator.hasNext()) {
            if (onlyEmptyEntries && !mapIterator.next().getValue().isEmpty()) continue;
            mapIterator.remove();
        }
        EventManager.updateCache();
    }

    private static void sortListValue(Class<? extends Event> indexClass) {
        CopyOnWriteArrayList<MethodData> sortedList = new CopyOnWriteArrayList<MethodData>();
        for (byte priority : Priority.VALUE_ARRAY) {
            for (MethodData data : REGISTRY_MAP.get(indexClass)) {
                if (data.priority() != priority) continue;
                sortedList.add(data);
            }
        }
        REGISTRY_MAP.put(indexClass, sortedList);
    }

    private static boolean isMethodBad(Method method) {
        return method.getParameterTypes().length != 1 || !method.isAnnotationPresent(EventHandler.class);
    }

    private static boolean isMethodBad(Method method, Class<? extends Event> eventClass) {
        return EventManager.isMethodBad(method) || !method.getParameterTypes()[0].equals(eventClass);
    }

    public static Event callEvent(Event event) {
        MethodData[] listeners = CACHE_MAP.get(event.getClass());
        if (listeners != null) {
            boolean stoppable = event instanceof EventStoppable;
            for (int i = 0; i < listeners.length; ++i) {
                MethodData data = listeners[i];
                try {
                    data.invoker().invoke(event);
                }
                catch (InvocationTargetException e) {
                    EventManager.handleInvocationException(data, event, e.getCause());
                }
                catch (Throwable t) {
                    EventManager.handleInvocationException(data, event, t);
                }
                if (stoppable && ((EventStoppable)event).isStopped()) break;
            }
        }
        return event;
    }

    private static void handleInvocationException(MethodData data, Event argument, Throwable cause) {
        if (cause instanceof ModuleException) {
            ModuleException moduleException = (ModuleException)cause;
            MINECRAFT_LOGGER.minecraftLog(new class_2561[]{class_2561.method_43470((String)("[" + moduleException.getModuleName() + "] " + String.valueOf(class_124.field_1061) + moduleException.getMessage()))});
        } else {
            LoggerUtil.error("Exception occurred within invoked method: " + data.target().getName() + ", Argument: " + String.valueOf(argument), cause);
        }
    }

    private record MethodData(Object source, Method target, EventInvoker invoker, byte priority) {
    }

    @FunctionalInterface
    public static interface EventInvoker {
        public void invoke(Event var1) throws Throwable;
    }
}

