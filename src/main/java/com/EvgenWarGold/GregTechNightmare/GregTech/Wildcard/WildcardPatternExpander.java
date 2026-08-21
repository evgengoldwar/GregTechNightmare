package com.EvgenWarGold.GregTechNightmare.GregTech.Wildcard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.EvgenWarGold.GregTechNightmare.GregTech.Items.GTN_Items;

import appeng.api.AEApi;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.GTUtility;

public final class WildcardPatternExpander {

    private static final Map<OrePrefixes, Map<Materials, ItemStack>> RESOLVED_ITEMS = new IdentityHashMap<>();

    private WildcardPatternExpander() {}

    public static boolean containsWildcard(ICraftingPatternDetails details) {
        return details != null && (containsWildcard(AEPatternStackAccess.getInputs(details))
            || containsWildcard(AEPatternStackAccess.getOutputs(details)));
    }

    static List<WildcardPatternVariant> resolveVariants(ICraftingPatternDetails source,
        WildcardPatternBlacklist blacklist) {
        if (source == null || source.isCraftable()) {
            return Collections.emptyList();
        }

        IAEStack<?>[] sourceInputs = AEPatternStackAccess.getInputs(source);
        IAEStack<?>[] sourceOutputs = AEPatternStackAccess.getOutputs(source);

        List<WildcardPatternVariant> result = new ArrayList<>();
        Set<String> fingerprints = new HashSet<>();
        for (Materials material : Materials.values()) {
            if (material == null || blacklist != null && blacklist.blocksMaterial(material)) {
                continue;
            }

            IAEStack<?>[] resolvedInputs = resolve(sourceInputs, material);
            if (resolvedInputs == null) {
                continue;
            }

            IAEStack<?>[] resolvedOutputs = resolve(sourceOutputs, material);
            if (resolvedOutputs == null || blacklist != null && blacklist.blocksOutputs(resolvedOutputs)) {
                continue;
            }

            String fingerprint = fingerprint(resolvedInputs, resolvedOutputs);
            if (fingerprints.add(fingerprint)) {
                result.add(new WildcardPatternVariant(material, resolvedInputs, resolvedOutputs));
            }
        }
        return Collections.unmodifiableList(result);
    }

    static List<ICraftingPatternDetails> bind(ICraftingPatternDetails source, List<WildcardPatternVariant> variants) {
        if (source == null || variants == null || variants.isEmpty()) {
            return Collections.emptyList();
        }

        List<ICraftingPatternDetails> result = new ArrayList<>(variants.size());
        for (WildcardPatternVariant variant : variants) {
            result.add(variant.bind(source));
        }

        return Collections.unmodifiableList(result);
    }

    public static String fingerprintSourcePattern(ICraftingPatternDetails details) {
        if (details == null) {
            return "null";
        }

        StringBuilder builder = new StringBuilder(256);
        appendPatternStack(builder, details.getPattern());
        builder.append('|')
            .append(details.isCraftable())
            .append('|')
            .append(details.canSubstitute())
            .append('|')
            .append(details.getPriority())
            .append('|');
        appendStacks(builder, AEPatternStackAccess.getInputs(details));
        builder.append(" -> ");
        appendStacks(builder, AEPatternStackAccess.getOutputs(details));
        return builder.toString();
    }

    public static String fingerprintPatternStack(ItemStack pattern) {
        StringBuilder builder = new StringBuilder(96);
        appendPatternStack(builder, pattern);
        return builder.toString();
    }

    private static boolean containsWildcard(IAEStack<?>[] stacks) {
        if (stacks == null) {
            return false;
        }

        for (IAEStack<?> stack : stacks) {
            if (!(stack instanceof IAEItemStack)) {
                continue;
            }

            if (isWildcard(((IAEItemStack) stack).getItemStack())) {
                return true;
            }

        }
        return false;
    }

    private static IAEStack<?>[] resolve(IAEStack<?>[] source, Materials material) {
        if (source == null) {
            return new IAEStack[0];
        }

        IAEStack<?>[] result = new IAEStack[source.length];

        for (int i = 0; i < source.length; i++) {
            IAEStack<?> input = source[i];
            if (input == null) {
                continue;
            }

            if (!(input instanceof IAEItemStack aeItemStack)) {
                result[i] = input.copy();
                continue;
            }

            ItemStack itemStack = aeItemStack.getItemStack();
            if (!isWildcard(itemStack)) {
                result[i] = input.copy();
                continue;
            }

            WildcardPrefix wildcard = WildcardPrefix.byMeta(itemStack.getItemDamage());
            if (wildcard == null) {
                return null;
            }

            if (wildcard.isFluid()) {
                FluidStack fluid = WildcardFluidResolver
                    .resolve(material, wildcard.getFluidMode(), input.getStackSize());
                if (fluid == null || fluid.getFluid() == null || fluid.amount <= 0) {
                    return null;
                }

                IAEFluidStack aeFluid = AEApi.instance()
                    .storage()
                    .createFluidStack(fluid);
                if (aeFluid == null) {
                    return null;
                }

                aeFluid.setStackSize(input.getStackSize());
                result[i] = aeFluid;
                continue;
            }

            OrePrefixes orePrefix = wildcard.getOrePrefix();
            if (orePrefix == null) {
                return null;
            }

            ItemStack resolved = resolveItem(orePrefix, material);
            if (resolved == null) {
                return null;
            }

            IAEItemStack aeResolved = AEApi.instance()
                .storage()
                .createItemStack(resolved);
            if (aeResolved == null) {
                return null;
            }

            aeResolved.setStackSize(input.getStackSize());
            result[i] = aeResolved;
        }

        return result;
    }

    private static ItemStack resolveItem(OrePrefixes prefix, Materials material) {
        synchronized (RESOLVED_ITEMS) {
            Map<Materials, ItemStack> byMaterial = RESOLVED_ITEMS.get(prefix);
            if (byMaterial != null && byMaterial.containsKey(material)) {
                ItemStack cached = byMaterial.get(material);
                return cached == null ? null : cached.copy();
            }
        }

        ItemStack resolved = GTOreDictUnificator.get(prefix, material, 1L);
        ItemStack cached = GTUtility.isStackInvalid(resolved) ? null : GTUtility.copyAmount(1, resolved);

        synchronized (RESOLVED_ITEMS) {
            Map<Materials, ItemStack> byMaterial = RESOLVED_ITEMS.computeIfAbsent(prefix, k -> new IdentityHashMap<>());
            byMaterial.put(material, cached);
        }
        return cached == null ? null : cached.copy();
    }

    private static boolean isWildcard(ItemStack stack) {
        return stack != null && stack.getItem() == GTN_Items.WILDCARD_PREFIX;
    }

    private static String fingerprint(IAEStack<?>[] inputs, IAEStack<?>[] outputs) {
        StringBuilder builder = new StringBuilder(256);
        appendStacks(builder, inputs);
        builder.append(" -> ");
        appendStacks(builder, outputs);
        return builder.toString();
    }

    private static void appendPatternStack(StringBuilder builder, ItemStack pattern) {
        if (pattern == null) {
            builder.append("null");
            return;
        }
        builder.append(Item.getIdFromItem(pattern.getItem()))
            .append(':')
            .append(pattern.getItemDamage())
            .append(':')
            .append(pattern.stackSize)
            .append(':')
            .append(
                pattern.hasTagCompound() ? pattern.getTagCompound()
                    .toString() : "");
    }

    private static void appendStacks(StringBuilder builder, IAEStack<?>[] stacks) {
        if (stacks == null) {
            return;
        }

        for (IAEStack<?> stack : stacks) {
            if (stack == null) {
                continue;
            }

            if (stack instanceof IAEItemStack aeItemStack) {
                ItemStack item = aeItemStack.getItemStack();
                if (item == null) {
                    continue;
                }

                builder.append('I')
                    .append(Item.getIdFromItem(item.getItem()))
                    .append(':')
                    .append(item.getItemDamage())
                    .append(':')
                    .append(stack.getStackSize())
                    .append(':')
                    .append(
                        item.hasTagCompound() ? item.getTagCompound()
                            .toString() : "")
                    .append(';');
            } else if (stack instanceof IAEFluidStack aeFluidStack) {
                FluidStack fluid = aeFluidStack.getFluidStack();
                if (fluid == null || fluid.getFluid() == null) {
                    continue;
                }

                builder.append('F')
                    .append(
                        fluid.getFluid()
                            .getName())
                    .append(':')
                    .append(stack.getStackSize())
                    .append(':')
                    .append(fluid.tag == null ? "" : fluid.tag.toString())
                    .append(';');
            }
        }
    }
}
