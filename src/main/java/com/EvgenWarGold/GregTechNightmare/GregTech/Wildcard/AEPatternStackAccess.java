package com.EvgenWarGold.GregTechNightmare.GregTech.Wildcard;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;

public final class AEPatternStackAccess {

    private static final Map<Class<?>, Accessors> ACCESSORS = new ConcurrentHashMap<>();

    private AEPatternStackAccess() {}

    public static IAEStack<?>[] getInputs(ICraftingPatternDetails details) {
        return read(details, true);
    }

    public static IAEStack<?>[] getOutputs(ICraftingPatternDetails details) {
        return read(details, false);
    }

    private static IAEStack<?>[] read(ICraftingPatternDetails details, boolean inputs) {
        if (details == null) {
            return new IAEStack[0];
        }

        Accessors accessors = getAccessors(details.getClass());
        Method method = inputs ? accessors.inputs : accessors.outputs;
        if (method != null) {
            try {
                Object value = method.invoke(details);
                if (value instanceof IAEStack<?>[]aeStack) {
                    return copy(aeStack);
                }
            } catch (ReflectiveOperationException | RuntimeException ignored) {}
        }

        IAEItemStack[] fallback = inputs ? details.getInputs() : details.getOutputs();
        if (fallback == null) {
            return new IAEStack[0];
        }

        IAEStack<?>[] result = new IAEStack[fallback.length];
        for (int i = 0; i < fallback.length; i++) {
            result[i] = fallback[i] == null ? null : fallback[i].copy();
        }
        return result;
    }

    private static Accessors getAccessors(Class<?> type) {
        Accessors accessors = ACCESSORS.get(type);
        if (accessors != null) return accessors;

        Accessors resolved = new Accessors(findMethod(type, "getAEInputs"), findMethod(type, "getAEOutputs"));
        Accessors existing = ACCESSORS.putIfAbsent(type, resolved);
        return existing == null ? resolved : existing;
    }

    private static Method findMethod(Class<?> type, String name) {
        try {
            Method method = type.getMethod(name);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException ignored) {}

        Class<?> current = type;
        while (current != null && current != Object.class) {
            try {
                Method method = current.getDeclaredMethod(name);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    public static IAEStack<?>[] copy(IAEStack<?>[] source) {
        if (source == null) {
            return new IAEStack[0];
        }

        IAEStack<?>[] result = new IAEStack[source.length];
        for (int i = 0; i < source.length; i++) {
            result[i] = source[i] == null ? null : source[i].copy();
        }
        return result;
    }

    private static final class Accessors {

        private final Method inputs;
        private final Method outputs;

        private Accessors(Method inputs, Method outputs) {
            this.inputs = inputs;
            this.outputs = outputs;
        }
    }
}
