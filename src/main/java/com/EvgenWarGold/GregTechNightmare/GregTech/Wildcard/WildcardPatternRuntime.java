package com.EvgenWarGold.GregTechNightmare.GregTech.Wildcard;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import gregtech.api.enums.GTValues;
import gregtech.api.objects.GTDualInputPattern;
import gregtech.common.tileentities.machines.MTEHatchCraftingInputME;

public final class WildcardPatternRuntime {

    private static final Map<Object, ActivePattern> ACTIVE_PATTERNS = new WeakHashMap<>();
    private static final Map<Class<?>, Method> IS_EMPTY_METHODS = new ConcurrentHashMap<>();
    private static final Set<Class<?>> MISSING_IS_EMPTY_METHODS = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private WildcardPatternRuntime() {}

    public static boolean preparePush(Object host, WildcardPatternDetails details) {
        if (host == null || details == null) {
            return false;
        }

        Object patternSlot = CraftingInputMEIntrospection.findLivePatternSlot(host, details.getDelegate());
        if (patternSlot == null) {
            return false;
        }

        synchronized (ACTIVE_PATTERNS) {
            ActivePattern active = ACTIVE_PATTERNS.get(patternSlot);
            MTEHatchCraftingInputME hatch = host instanceof MTEHatchCraftingInputME ? (MTEHatchCraftingInputME) host
                : null;

            if (active == null || isPatternSlotEmpty(patternSlot)) {
                ACTIVE_PATTERNS.put(patternSlot, new ActivePattern(details, hatch));
                return true;
            }

            if (hatch != null) {
                active.host = new WeakReference<>(hatch);
            }
            return sameResolvedPattern(active.details, details);
        }
    }

    public static WildcardPatternDetails getActiveDetails(Object patternSlot) {
        if (patternSlot == null) {
            return null;
        }
        synchronized (ACTIVE_PATTERNS) {
            ActivePattern active = ACTIVE_PATTERNS.get(patternSlot);
            return active == null ? null : active.details;
        }
    }

    public static GTDualInputPattern buildResolvedPatternInputs(Object patternSlot, WildcardPatternDetails details) {
        if (patternSlot == null || details == null) {
            return null;
        }

        ItemStack[] sharedItems = null;
        MTEHatchCraftingInputME host;
        synchronized (ACTIVE_PATTERNS) {
            ActivePattern active = ACTIVE_PATTERNS.get(patternSlot);
            host = active == null || active.host == null ? null : active.host.get();
            if (host != null) {
                try {
                    sharedItems = host.getSharedItems();
                } catch (RuntimeException ignored) {}
            }
        }

        List<ItemStack> items = new ArrayList<>();
        appendSharedItems(sharedItems, items);
        if (!containsIntegratedCircuit(items)) {
            ItemStack circuit = CraftingInputMECircuitResolver.find(host, patternSlot);
            if (circuit != null) {
                items.add(circuit);
            }
        }

        List<FluidStack> fluids = new ArrayList<>();
        for (IAEStack<?> aeStack : details.getAEInputs()) {
            if (aeStack == null || aeStack.getStackSize() <= 0) {
                continue;
            }

            if (aeStack instanceof IAEItemStack aeItemStack) {
                ItemStack stack = aeItemStack.getItemStack();
                if (stack == null) {
                    continue;
                }
                ItemStack copy = stack.copy();
                long amount = aeStack.getStackSize();
                copy.stackSize = amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;
                items.add(copy);
            } else if (aeStack instanceof IAEFluidStack aeFluidStack) {
                FluidStack stack = aeFluidStack.getFluidStack();
                if (stack == null) {
                    continue;
                }

                FluidStack copy = stack.copy();
                long amount = aeStack.getStackSize();
                copy.amount = amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;
                fluids.add(copy);
            }
        }

        GTDualInputPattern result = new GTDualInputPattern();
        result.inputItems = items.toArray(new ItemStack[0]);
        result.inputFluid = fluids.isEmpty() ? GTValues.emptyFluidStackArray : fluids.toArray(new FluidStack[0]);
        return result;
    }

    private static void appendSharedItems(ItemStack[] sharedItems, List<ItemStack> output) {
        if (sharedItems == null) {
            return;
        }

        for (ItemStack stack : sharedItems) {
            if (stack != null && stack.stackSize > 0) output.add(stack.copy());
        }
    }

    private static boolean isPatternSlotEmpty(Object patternSlot) {
        Method method = getIsEmptyMethod(patternSlot.getClass());
        if (method != null) {
            try {
                Object unchecked_result = method.invoke(patternSlot);
                if (unchecked_result instanceof Boolean result) {
                    return result;
                }
            } catch (ReflectiveOperationException | RuntimeException ignored) {}
        }

        return !ACTIVE_PATTERNS.containsKey(patternSlot);
    }

    private static Method getIsEmptyMethod(Class<?> type) {
        Method cached = IS_EMPTY_METHODS.get(type);
        if (cached != null) {
            return cached;
        }

        if (MISSING_IS_EMPTY_METHODS.contains(type)) {
            return null;
        }

        Method resolved = findIsEmptyMethod(type);
        if (resolved == null) {
            MISSING_IS_EMPTY_METHODS.add(type);
            return null;
        }
        Method existing = IS_EMPTY_METHODS.putIfAbsent(type, resolved);
        return existing == null ? resolved : existing;
    }

    private static Method findIsEmptyMethod(Class<?> type) {
        Class<?> current = type;
        while (current != null && current != Object.class) {
            try {
                Method method = current.getDeclaredMethod("isEmpty");
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
                current = current.getSuperclass();
            } catch (RuntimeException ignored) {
                return null;
            }
        }
        return null;
    }

    private static boolean containsIntegratedCircuit(List<ItemStack> items) {
        for (ItemStack stack : items) {
            if (CraftingInputMECircuitResolver.isIntegratedCircuit(stack)) {
                return true;
            }
        }
        return false;
    }

    private static boolean sameResolvedPattern(WildcardPatternDetails first, WildcardPatternDetails second) {
        return sameStacks(first.getCondensedAEInputsView(), second.getCondensedAEInputsView())
            && sameStacks(first.getCondensedAEOutputsView(), second.getCondensedAEOutputsView());
    }

    private static boolean sameStacks(IAEStack<?>[] first, IAEStack<?>[] second) {
        if (first == null || second == null) {
            return first == second;
        }

        if (first.length != second.length) {
            return false;
        }

        boolean[] used = new boolean[second.length];
        for (IAEStack<?> wanted : first) {
            boolean found = false;
            for (int i = 0; i < second.length; i++) {
                if (used[i] || !sameStack(wanted, second[i])) {
                    continue;
                }

                used[i] = true;
                found = true;
                break;
            }

            if (!found) {
                return false;
            }
        }
        return true;
    }

    private static boolean sameStack(IAEStack<?> first, IAEStack<?> second) {
        if (first == null || second == null) {
            return first == second;
        }

        if (first.getStackSize() != second.getStackSize()) {
            return false;
        }

        if (first instanceof IAEItemStack firstItemStack && second instanceof IAEItemStack secondItemStack) {
            ItemStack firstItem = firstItemStack.getItemStack();
            ItemStack secondItem = secondItemStack.getItemStack();
            return firstItem != null && secondItem != null
                && firstItem.isItemEqual(secondItem)
                && ItemStack.areItemStackTagsEqual(firstItem, secondItem);
        }
        if (first instanceof IAEFluidStack firstFluidStack && second instanceof IAEFluidStack secondFluidStack) {
            FluidStack firstFluid = firstFluidStack.getFluidStack();
            FluidStack secondFluid = secondFluidStack.getFluidStack();
            return firstFluid != null && secondFluid != null && firstFluid.isFluidEqual(secondFluid);
        }
        return false;
    }

    private static final class ActivePattern {

        private final WildcardPatternDetails details;
        private WeakReference<MTEHatchCraftingInputME> host;

        private ActivePattern(WildcardPatternDetails details, MTEHatchCraftingInputME host) {
            this.details = details;
            this.host = host == null ? null : new WeakReference<>(host);
        }
    }
}
