package com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses;

import static blockrenderer6343.client.utils.BRUtil.AUTO_PLACE_ENVIRONMENT;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import com.google.common.collect.Iterables;
import com.gtnewhorizon.structurelib.alignment.constructable.IConstructable;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.IStructureElement;
import com.gtnewhorizon.structurelib.structure.IStructureElementChain;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;

import blockrenderer6343.client.utils.BRUtil;
import blockrenderer6343.client.utils.ConstructableData;
import blockrenderer6343.client.world.DummyWorld;
import blockrenderer6343.integration.nei.StructureHacks;

public class MultiblockBlockCounter {

    private static final int MAX_TIERS_TO_CHECK = 50;

    private final Map<String, Integer> blockCounts = new HashMap<>();
    private static final ConstructableData data = new ConstructableData();
    private final Map<IStructureElement<IConstructable>, Integer> elementCountMap = new HashMap<>();

    private static final String LAZY_ELEMENT = "com.gtnewhorizon.structurelib.structure.LazyStructureElement";
    private static final String ON_ELEMENT_PASS = "com.gtnewhorizon.structurelib.structure.OnElementPass";

    public Map<String, Integer> getBlockCounts(IConstructable multiblock) {
        analyzeMultiblock(multiblock);
        return new HashMap<>(blockCounts);
    }

    @SuppressWarnings("unchecked")
    private void analyzeMultiblock(IConstructable multiblock) {
        blockCounts.clear();
        elementCountMap.clear();

        IStructureDefinition<?> structure = multiblock.getStructureDefinition();

        if (structure instanceof StructureDefinition) {
            StructureDefinition<IConstructable> structDef = (StructureDefinition<IConstructable>) structure;

            Collection<IStructureElement<IConstructable>[]> structures = structDef.getStructures()
                .values();

            for (IStructureElement<IConstructable>[] elementArray : structures) {
                if (elementArray == null) continue;

                for (IStructureElement<IConstructable> element : elementArray) {
                    if (element == null) continue;

                    elementCountMap.put(element, elementCountMap.getOrDefault(element, 0) + 1);
                }
            }

            for (Map.Entry<IStructureElement<IConstructable>, Integer> entry : elementCountMap.entrySet()) {
                IStructureElement<IConstructable> element = entry.getKey();
                Integer count = entry.getValue();

                processElement(multiblock, element, count);
            }
        }
    }

    private void processElement(IConstructable multiblock, IStructureElement<IConstructable> element, Integer count) {
        String elementName = element.getClass()
            .getName();

        if (StructureHacks.SKIP_ELEMENTS.contains(elementName)) {
            return;
        }

        if (element instanceof IStructureElementChain) {
            IStructureElement<IConstructable>[] elements = ((IStructureElementChain<IConstructable>) element)
                .fallbacks();

            if (elements != null) {
                for (IStructureElement<IConstructable> e : elements) {
                    processElement(multiblock, e, count);
                }
            }
            return;
        }

        if (elementName.equals(LAZY_ELEMENT)) {
            IStructureElement<IConstructable> realElement = StructureHacks.getUnderlyingElement(multiblock, element);

            if (realElement != null) {
                processElement(multiblock, realElement, count);
            }
            return;
        }

        if (elementName.equals(ON_ELEMENT_PASS)) {
            IStructureElement<IConstructable> innerElement = getOnElementPassInnerElement(element);
            if (innerElement != null) {
                processElement(multiblock, innerElement, count);
            }
            return;
        }

        IStructureElement<IConstructable> realElement = StructureHacks.getUnderlyingElement(multiblock, element);

        if (realElement != null && realElement != element) {
            processElement(multiblock, realElement, count);
            return;
        }

        Iterable<ItemStack> stacks = getStacksForElement(multiblock, element, data);

        if (stacks != null) {
            for (ItemStack stack : stacks) {
                if (stack != null && stack.getItem() != null) {
                    String blockKey = stack.getDisplayName();

                    if (Iterables.size(stacks) > 1) {
                        blockKey = blockKey + EnumChatFormatting.RED + " (Tiered)";
                    }

                    blockCounts.put(blockKey, count);
                    return;
                }
            }
        }
    }

    @SuppressWarnings({ "unchecked" })
    private IStructureElement<IConstructable> getOnElementPassInnerElement(IStructureElement<IConstructable> element) {
        try {
            Field elementField = element.getClass()
                .getDeclaredField("val$element");
            elementField.setAccessible(true);
            return (IStructureElement<IConstructable>) elementField.get(element);
        } catch (Exception e) {
            try {
                Field elementField = element.getClass()
                    .getDeclaredField("element");
                elementField.setAccessible(true);
                return (IStructureElement<IConstructable>) elementField.get(element);
            } catch (Exception ex) {
                return null;
            }
        }
    }

    @SuppressWarnings({ "unchecked" })
    private <T> Iterable<ItemStack> getStacksForElement(T multi, IStructureElement<T> element, ConstructableData data) {
        String name = element.getClass()
            .getName();

        if (name.equals(LAZY_ELEMENT) || name.equals(ON_ELEMENT_PASS)) {
            element = StructureHacks.getUnderlyingElement(multi, element);
            if (element == null) return Collections.emptyList();
            name = element.getClass()
                .getName();
        }

        if (StructureHacks.SKIP_ELEMENTS.contains(name)) return Collections.emptyList();

        if (element instanceof IStructureElementChain) {
            IStructureElement<T>[] elements = ((IStructureElementChain<T>) element).fallbacks();
            if (elements == null) return Collections.emptyList();
            LinkedHashSet<ItemStack> chainStacks = new LinkedHashSet<>();
            for (IStructureElement<T> e : elements) {
                Iterable<ItemStack> stacks = getStacksForElement(multi, e, data);
                if (stacks != null) {
                    stacks.forEach(chainStacks::add);
                }
            }
            return chainStacks;
        }

        return extractTieredBlocks(
            multi,
            element,
            data,
            getChannelFromElement((IStructureElement<IConstructable>) element));
    }

    private <T> LinkedHashSet<ItemStack> extractTieredBlocks(T multi, IStructureElement<T> element,
        ConstructableData data, String channel) {
        LinkedHashSet<ItemStack> result = new LinkedHashSet<>();
        ItemStack holo = StructureHacks.HOLO_STACK.copy();
        int tier = 0;
        ItemStack lastStack = null;
        Collection<String> channels = channel == null ? Collections.emptyList() : Collections.singletonList(channel);

        do {
            holo.stackSize = tier + 1;
            IStructureElement.BlocksToPlace toPlace = element
                .getBlocksToPlace(multi, DummyWorld.INSTANCE, 0, 0, 0, holo, AUTO_PLACE_ENVIRONMENT);

            if (toPlace == null || toPlace.getStacks() == null) break;

            Iterator<ItemStack> iterator = toPlace.getStacks()
                .iterator();
            if (!iterator.hasNext()) break;

            ItemStack firstStack = iterator.next();

            if (lastStack != null) {
                long currentHash = BRUtil.hashStack(firstStack);
                long lastHash = BRUtil.hashStack(lastStack);
                if (currentHash == lastHash) {
                    break;
                }
            }

            data.addItemTier(firstStack, lastStack, channels, tier + 1);
            result.add(firstStack);
            lastStack = firstStack.copy();

            while (iterator.hasNext()) {
                ItemStack stack = iterator.next();
                data.addItemTier(stack, channels, tier + 1);
                result.add(stack);
            }

            tier++;

        } while (tier < MAX_TIERS_TO_CHECK);

        data.setMaxTier(tier, channels);
        return result;
    }

    private String getChannelFromElement(IStructureElement<IConstructable> element) {
        try {
            Class<?> elementClass = element.getClass();
            Field channelField;
            try {
                channelField = elementClass.getDeclaredField("val$channel");
            } catch (NoSuchFieldException e1) {
                try {
                    channelField = elementClass.getDeclaredField("channel");
                } catch (NoSuchFieldException e2) {
                    return "";
                }
            }

            channelField.setAccessible(true);
            Object channel = channelField.get(element);
            return channel != null ? channel.toString() : "";

        } catch (Exception e) {
            return "";
        }
    }
}
