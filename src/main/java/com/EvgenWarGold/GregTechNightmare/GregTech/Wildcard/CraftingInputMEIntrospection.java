package com.EvgenWarGold.GregTechNightmare.GregTech.Wildcard;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import appeng.api.networking.crafting.ICraftingPatternDetails;

public final class CraftingInputMEIntrospection {

    private static final Map<Class<?>, List<Field>> MAP_FIELDS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Field> PATTERN_SLOT_MAP_FIELDS = new ConcurrentHashMap<>();

    private CraftingInputMEIntrospection() {}

    public static Object findLivePatternSlot(Object host, ICraftingPatternDetails source) {
        if (host == null || source == null) {
            return null;
        }

        return findPatternSlot(host, source).slot;
    }

    private static PatternSlotLookup findPatternSlot(Object host, ICraftingPatternDetails source) {
        Class<?> hostType = host.getClass();
        Field cachedField = PATTERN_SLOT_MAP_FIELDS.get(hostType);
        if (cachedField != null) {
            return readPatternSlotMap(host, source, cachedField);
        }

        for (Field field : getMapFields(hostType)) {
            try {
                Object value = field.get(host);
                if (!(value instanceof Map<?, ?>map)) {
                    continue;
                }

                if (!isPatternSlotMap(field, map)) {
                    continue;
                }

                PATTERN_SLOT_MAP_FIELDS.putIfAbsent(hostType, field);
                return new PatternSlotLookup(map.get(source));
            } catch (ReflectiveOperationException | RuntimeException ignored) {}
        }

        return new PatternSlotLookup(null);
    }

    private static PatternSlotLookup readPatternSlotMap(Object host, ICraftingPatternDetails source, Field field) {
        try {
            Object value = field.get(host);
            if (!(value instanceof Map<?, ?>map)) {
                return new PatternSlotLookup(null);
            }

            return new PatternSlotLookup(map.get(source));
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return new PatternSlotLookup(null);
        }
    }

    private static List<Field> getMapFields(Class<?> hostType) {
        List<Field> fields = MAP_FIELDS.get(hostType);
        if (fields != null) {
            return fields;
        }

        List<Field> resolved = new ArrayList<>();
        Class<?> type = hostType;
        while (type != null && type != Object.class) {
            for (Field field : type.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || !Map.class.isAssignableFrom(field.getType())) {
                    continue;
                }

                try {
                    field.setAccessible(true);
                    resolved.add(field);
                } catch (RuntimeException ignored) {}
            }
            type = type.getSuperclass();
        }

        List<Field> immutable = Collections.unmodifiableList(resolved);
        List<Field> existing = MAP_FIELDS.putIfAbsent(hostType, immutable);
        return existing == null ? immutable : existing;
    }

    private record PatternSlotLookup(Object slot) {}

    private static boolean isPatternSlotMap(Field field, Map<?, ?> map) {
        String fieldName = field.getName()
            .toLowerCase(Locale.ROOT);
        if (fieldName.contains("pattern") && (fieldName.contains("slot") || fieldName.contains("map"))) {
            return true;
        }

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() instanceof ICraftingPatternDetails) {
                return true;
            }

            Object value = entry.getValue();
            if (value != null && value.getClass()
                .getSimpleName()
                .toLowerCase(Locale.ROOT)
                .contains("patternslot")) {
                return true;
            }
        }
        return false;
    }
}
