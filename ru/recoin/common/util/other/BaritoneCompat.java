/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2338
 */
package ru.recoin.common.util.other;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.minecraft.class_2338;

public final class BaritoneCompat {
    private BaritoneCompat() {
    }

    public static Object getPrimaryBaritone() {
        Object provider = BaritoneCompat.invokeStaticNoArgs(BaritoneCompat.findClass("baritone.api.BaritoneAPI"), "getProvider");
        return BaritoneCompat.invokeNoArgs(provider, "getPrimaryBaritone");
    }

    public static Boolean getBooleanSetting(String name) {
        Boolean booleanValue;
        Object setting = BaritoneCompat.getSetting(name);
        Object value = BaritoneCompat.readField(setting, "value");
        return value instanceof Boolean ? (booleanValue = (Boolean)value) : null;
    }

    public static void setBooleanSetting(String name, boolean value) {
        BaritoneCompat.setSetting(name, value);
    }

    public static void setSetting(String name, Object value) {
        BaritoneCompat.writeField(BaritoneCompat.getSetting(name), "value", value);
    }

    public static boolean listSettingContains(String name, Object value) {
        List list;
        Object settingValue = BaritoneCompat.readField(BaritoneCompat.getSetting(name), "value");
        return settingValue instanceof List && (list = (List)settingValue).contains(value);
    }

    public static boolean addToListSetting(String name, Object value) {
        List rawList;
        block7: {
            block6: {
                Object settingValue = BaritoneCompat.readField(BaritoneCompat.getSetting(name), "value");
                if (!(settingValue instanceof List)) break block6;
                rawList = (List)settingValue;
                if (value != null) break block7;
            }
            return false;
        }
        List list = rawList;
        try {
            if (!list.contains(value)) {
                list.add(value);
            }
            return true;
        }
        catch (RuntimeException ignored) {
            return false;
        }
    }

    public static void removeFromListSetting(String name, Object value) {
        Object settingValue = BaritoneCompat.readField(BaritoneCompat.getSetting(name), "value");
        if (settingValue instanceof List) {
            List list = (List)settingValue;
            try {
                list.remove(value);
            }
            catch (RuntimeException runtimeException) {
                // empty catch block
            }
        }
    }

    public static boolean goNear(Object baritone, class_2338 pos, int radius) {
        if (baritone == null || pos == null) {
            return false;
        }
        Object goal = BaritoneCompat.newInstance("baritone.api.pathing.goals.GoalNear", new Class[]{class_2338.class, Integer.TYPE}, pos, radius);
        return BaritoneCompat.setGoalAndPath(baritone, goal);
    }

    public static boolean goBlock(Object baritone, class_2338 pos) {
        if (baritone == null || pos == null) {
            return false;
        }
        Object goal = BaritoneCompat.newInstance("baritone.api.pathing.goals.GoalBlock", new Class[]{class_2338.class}, pos);
        return BaritoneCompat.setGoalAndPath(baritone, goal);
    }

    public static boolean goXZ(Object baritone, int x, int z) {
        if (baritone == null) {
            return false;
        }
        Object goal = BaritoneCompat.newInstance("baritone.api.pathing.goals.GoalXZ", new Class[]{Integer.TYPE, Integer.TYPE}, x, z);
        return BaritoneCompat.setGoalAndPath(baritone, goal);
    }

    public static boolean runAway(Object baritone, double distance, class_2338 ... positions) {
        if (baritone == null || positions == null || positions.length == 0) {
            return false;
        }
        Object goal = BaritoneCompat.newInstance("baritone.api.pathing.goals.GoalRunAway", new Class[]{Double.TYPE, class_2338[].class}, distance, positions);
        return BaritoneCompat.setGoalAndPath(baritone, goal);
    }

    public static void cancelPathing(Object baritone) {
        Object behavior = BaritoneCompat.invokeNoArgs(baritone, "getPathingBehavior");
        BaritoneCompat.invokeNoArgs(behavior, "cancelEverything");
    }

    public static void cancelMining(Object baritone) {
        Object process = BaritoneCompat.invokeNoArgs(baritone, "getMineProcess");
        BaritoneCompat.invokeNoArgs(process, "cancel");
    }

    public static boolean isPathing(Object baritone) {
        Boolean booleanValue;
        Object behavior = BaritoneCompat.invokeNoArgs(baritone, "getPathingBehavior");
        Object value = BaritoneCompat.invokeNoArgs(behavior, "isPathing");
        return value instanceof Boolean && (booleanValue = (Boolean)value) != false;
    }

    public static void pausePathing(Object baritone) {
        BaritoneCompat.invokeNoArgs(BaritoneCompat.invokeNoArgs(baritone, "getPathingBehavior"), "requestPause");
    }

    public static void resumePathing(Object baritone) {
        BaritoneCompat.invokeNoArgs(BaritoneCompat.invokeNoArgs(baritone, "getPathingBehavior"), "requestUnpause");
    }

    public static boolean isMining(Object baritone) {
        Boolean booleanValue;
        Object value = BaritoneCompat.invokeNoArgs(BaritoneCompat.invokeNoArgs(baritone, "getMineProcess"), "isActive");
        return value instanceof Boolean && (booleanValue = (Boolean)value) != false;
    }

    public static boolean minePositions(Object baritone, Object block, List<class_2338> positions) {
        if (baritone == null || block == null || positions == null || positions.isEmpty()) {
            return false;
        }
        Object process = BaritoneCompat.invokeNoArgs(baritone, "getMineProcess");
        return BaritoneCompat.invokeArguments(process, "minePositions", block, positions);
    }

    public static boolean isMineBlacklisted(Object baritone, class_2338 pos) {
        Collection collection;
        Object blacklist = BaritoneCompat.invokeNoArgs(BaritoneCompat.invokeNoArgs(baritone, "getMineProcess"), "getBlacklist");
        return blacklist instanceof Collection && (collection = (Collection)blacklist).contains(pos);
    }

    public static boolean lookAt(Object baritone, float yaw, float pitch) {
        if (baritone == null) {
            return false;
        }
        Object rotation = BaritoneCompat.newInstance("baritone.api.utils.Rotation", new Class[]{Float.TYPE, Float.TYPE}, Float.valueOf(yaw), Float.valueOf(pitch));
        Object lookBehavior = BaritoneCompat.invokeNoArgs(baritone, "getLookBehavior");
        return BaritoneCompat.invokeArguments(lookBehavior, "updateTarget", rotation, true);
    }

    public static List<class_2338> getPathPositions(Object baritone) {
        List list;
        if (baritone == null) {
            return Collections.emptyList();
        }
        Object behavior = BaritoneCompat.invokeNoArgs(baritone, "getPathingBehavior");
        if (behavior == null) {
            return Collections.emptyList();
        }
        Object executor = BaritoneCompat.invokeNoArgs(behavior, "getCurrent");
        if (executor == null) {
            return Collections.emptyList();
        }
        Object path = BaritoneCompat.invokeNoArgs(executor, "getPath");
        if (path == null) {
            return Collections.emptyList();
        }
        Object positions = BaritoneCompat.invokeNoArgs(path, "positions");
        return positions instanceof List ? (list = (List)positions) : Collections.emptyList();
    }

    public static int getPathPositionIndex(Object baritone) {
        int n;
        if (baritone == null) {
            return -1;
        }
        Object behavior = BaritoneCompat.invokeNoArgs(baritone, "getPathingBehavior");
        if (behavior == null) {
            return -1;
        }
        Object executor = BaritoneCompat.invokeNoArgs(behavior, "getCurrent");
        if (executor == null) {
            return -1;
        }
        Object pos = BaritoneCompat.invokeNoArgs(executor, "getPosition");
        if (pos instanceof Integer) {
            Integer intPos = (Integer)pos;
            n = intPos;
        } else {
            n = -1;
        }
        return n;
    }

    public static void forceInput(Object baritone, String inputName, boolean forced) {
        if (baritone == null || inputName == null) {
            return;
        }
        Object handler = BaritoneCompat.invokeNoArgs(baritone, "getInputOverrideHandler");
        if (handler == null) {
            return;
        }
        Class<?> inputClass = BaritoneCompat.findClass("baritone.api.utils.input.Input");
        if (inputClass == null || !inputClass.isEnum()) {
            return;
        }
        try {
            Object inputEnum = null;
            Object[] constants = inputClass.getEnumConstants();
            if (constants != null) {
                for (Object constant : constants) {
                    if (((Enum)constant).name().equals(inputName)) {
                        inputEnum = constant;
                        break;
                    }
                }
            }
            if (inputEnum == null) {
                return;
            }
            BaritoneCompat.invokeArguments(handler, "setInputForceState", inputEnum, forced);
        }
        catch (RuntimeException runtimeException) {
            // empty catch block
        }
    }

    private static Object getSetting(String name) {
        Object settings = BaritoneCompat.invokeStaticNoArgs(BaritoneCompat.findClass("baritone.api.BaritoneAPI"), "getSettings");
        return BaritoneCompat.readField(settings, name);
    }

    private static boolean setGoalAndPath(Object baritone, Object goal) {
        if (goal == null) {
            return false;
        }
        Object process = BaritoneCompat.invokeNoArgs(baritone, "getCustomGoalProcess");
        return BaritoneCompat.invokeSingleArgument(process, "setGoalAndPath", goal);
    }

    private static Class<?> findClass(String name) {
        try {
            return Class.forName(name);
        }
        catch (LinkageError | ReflectiveOperationException ignored) {
            ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
            if (contextLoader == null) {
                return null;
            }
            try {
                return Class.forName(name, true, contextLoader);
            }
            catch (LinkageError | ReflectiveOperationException ignoredAgain) {
                return null;
            }
        }
    }

    private static Object newInstance(String className, Class<?>[] parameterTypes, Object ... arguments) {
        Class<?> type = BaritoneCompat.findClass(className);
        if (type == null) {
            return null;
        }
        try {
            Constructor<?> constructor = type.getConstructor(parameterTypes);
            return constructor.newInstance(arguments);
        }
        catch (LinkageError | ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static Object invokeStaticNoArgs(Class<?> owner, String name) {
        if (owner == null) {
            return null;
        }
        try {
            return owner.getMethod(name, new Class[0]).invoke(null, new Object[0]);
        }
        catch (LinkageError | ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static Object invokeNoArgs(Object target, String name) {
        if (target == null) {
            return null;
        }
        try {
            return target.getClass().getMethod(name, new Class[0]).invoke(target, new Object[0]);
        }
        catch (LinkageError | ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static boolean invokeSingleArgument(Object target, String name, Object argument) {
        if (target == null || argument == null) {
            return false;
        }
        try {
            for (Method method : target.getClass().getMethods()) {
                if (!method.getName().equals(name) || method.getParameterCount() != 1 || !method.getParameterTypes()[0].isInstance(argument)) continue;
                method.invoke(target, argument);
                return true;
            }
        }
        catch (LinkageError | ReflectiveOperationException throwable) {
            // empty catch block
        }
        return false;
    }

    private static boolean invokeArguments(Object target, String name, Object ... arguments) {
        if (target == null || arguments == null) {
            return false;
        }
        try {
            block2: for (Method method : target.getClass().getMethods()) {
                if (!method.getName().equals(name) || method.getParameterCount() != arguments.length) continue;
                Class<?>[] parameters = method.getParameterTypes();
                for (int i = 0; i < parameters.length; ++i) {
                    Class<?> expected;
                    if (arguments[i] == null) continue;
                    Class<?> clazz = expected = parameters[i].isPrimitive() ? BaritoneCompat.primitiveWrapper(parameters[i]) : parameters[i];
                    if (!expected.isInstance(arguments[i])) continue block2;
                }
                method.invoke(target, arguments);
                return true;
            }
        }
        catch (LinkageError | ReflectiveOperationException throwable) {
            // empty catch block
        }
        return false;
    }

    private static Class<?> primitiveWrapper(Class<?> type) {
        if (type == Boolean.TYPE) {
            return Boolean.class;
        }
        if (type == Integer.TYPE) {
            return Integer.class;
        }
        if (type == Float.TYPE) {
            return Float.class;
        }
        if (type == Double.TYPE) {
            return Double.class;
        }
        if (type == Long.TYPE) {
            return Long.class;
        }
        return type;
    }

    private static Object readField(Object target, String name) {
        if (target == null) {
            return null;
        }
        try {
            Field field = target.getClass().getField(name);
            return field.get(target);
        }
        catch (LinkageError | ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static void writeField(Object target, String name, Object value) {
        if (target == null) {
            return;
        }
        try {
            Field field = target.getClass().getField(name);
            field.set(target, value);
        }
        catch (LinkageError | ReflectiveOperationException throwable) {
            // empty catch block
        }
    }
}

