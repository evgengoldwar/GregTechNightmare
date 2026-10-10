package com.EvgenWarGold.GregTechNightmare.GregTech.Items;

import static com.EvgenWarGold.GregTechNightmare.GregTech.Items.GTN_Items.HANDY_BAG;
import static com.EvgenWarGold.GregTechNightmare.GregTech.Items.GTN_Items.LINK_TOOL;
import static com.EvgenWarGold.GregTechNightmare.GregTech.Items.GTN_Items.MANA_PROSPECTOR;
import static com.EvgenWarGold.GregTechNightmare.GregTech.Items.GTN_Items.MEMORY_CARD;
import static com.EvgenWarGold.GregTechNightmare.GregTech.Items.GTN_Items.META_ITEM_01;
import static com.EvgenWarGold.GregTechNightmare.GregTech.Items.GTN_Items.WILDCARD_PREFIX;

import net.minecraft.item.Item;

import com.EvgenWarGold.GregTechNightmare.GregTech.GTN_ItemList;
import com.EvgenWarGold.GregTechNightmare.GregTech.Wildcard.WildcardPrefix;

import cpw.mods.fml.common.registry.GameRegistry;

public class GTN_ItemsRegister {

    public static void init() {
        registryItems();
        registryItemContainers();
    }

    private static void registryItems() {
        Item[] itemsToReg = { META_ITEM_01, LINK_TOOL, MANA_PROSPECTOR, WILDCARD_PREFIX, HANDY_BAG, MEMORY_CARD };

        for (Item item : itemsToReg) {
            GameRegistry.registerItem(item, item.unlocalizedName);
        }
    }

    private static void registryItemContainers() {
        // spotless:off
        // Test Item
        GTN_ItemList.TestItem.set(META_ITEM_01.registerVariantWithTooltips(
            0,
            new String[]{
                "Test"
            }));

        // Advanced Clay
        GTN_ItemList.AdvancedClay.set(META_ITEM_01.registerVariant(1));

        // MeteorMinerSchematic1
        GTN_ItemList.MeteorMinerSchematic1.set(META_ITEM_01.registerVariant(2));

        // MeteorMinerSchematic2
        GTN_ItemList.MeteorMinerSchematic2.set(META_ITEM_01.registerVariant(3));

        GTN_ItemList.ManaProspector.set(MANA_PROSPECTOR);

        GTN_ItemList.HandyBag.set(new net.minecraft.item.ItemStack(HANDY_BAG, 1, 0));
        GTN_ItemList.HandyBagLarge.set(new net.minecraft.item.ItemStack(HANDY_BAG, 1, 1));
        GTN_ItemList.MemoryCard6b.set(new net.minecraft.item.ItemStack(MEMORY_CARD, 1, 0));
        GTN_ItemList.MemoryCard8b.set(new net.minecraft.item.ItemStack(MEMORY_CARD, 1, 1));
        GTN_ItemList.MemoryCard10b.set(new net.minecraft.item.ItemStack(MEMORY_CARD, 1, 2));
        GTN_ItemList.MemoryCard12b.set(new net.minecraft.item.ItemStack(MEMORY_CARD, 1, 3));

        GTN_ItemList.WildcardIngot.set(new net.minecraft.item.ItemStack(WILDCARD_PREFIX, 1, WildcardPrefix.INGOT.getMeta()));
        GTN_ItemList.WildcardPlate.set(new net.minecraft.item.ItemStack(WILDCARD_PREFIX, 1, WildcardPrefix.PLATE.getMeta()));
        GTN_ItemList.WildcardDust.set(new net.minecraft.item.ItemStack(WILDCARD_PREFIX, 1, WildcardPrefix.DUST.getMeta()));
        GTN_ItemList.WildcardStick.set(new net.minecraft.item.ItemStack(WILDCARD_PREFIX, 1, WildcardPrefix.STICK.getMeta()));
        GTN_ItemList.WildcardScrew.set(new net.minecraft.item.ItemStack(WILDCARD_PREFIX, 1, WildcardPrefix.SCREW.getMeta()));
        GTN_ItemList.WildcardBolt.set(new net.minecraft.item.ItemStack(WILDCARD_PREFIX, 1, WildcardPrefix.BOLT.getMeta()));
        GTN_ItemList.WildcardRing.set(new net.minecraft.item.ItemStack(WILDCARD_PREFIX, 1, WildcardPrefix.RING.getMeta()));
        GTN_ItemList.WildcardFoil.set(new net.minecraft.item.ItemStack(WILDCARD_PREFIX, 1, WildcardPrefix.FOIL.getMeta()));
        GTN_ItemList.WildcardGear.set(new net.minecraft.item.ItemStack(WILDCARD_PREFIX, 1, WildcardPrefix.GEAR.getMeta()));
        GTN_ItemList.WildcardWire1x.set(new net.minecraft.item.ItemStack(WILDCARD_PREFIX, 1, WildcardPrefix.WIRE_1X.getMeta()));
        GTN_ItemList.WildcardCable1x.set(new net.minecraft.item.ItemStack(WILDCARD_PREFIX, 1, WildcardPrefix.CABLE_1X.getMeta()));
        GTN_ItemList.WildcardFrame.set(new net.minecraft.item.ItemStack(WILDCARD_PREFIX, 1, WildcardPrefix.FRAME.getMeta()));
        GTN_ItemList.WildcardGem.set(new net.minecraft.item.ItemStack(WILDCARD_PREFIX, 1, WildcardPrefix.GEM.getMeta()));
        GTN_ItemList.WildcardBlock.set(new net.minecraft.item.ItemStack(WILDCARD_PREFIX, 1, WildcardPrefix.BLOCK.getMeta()));
        GTN_ItemList.WildcardNugget.set(new net.minecraft.item.ItemStack(WILDCARD_PREFIX, 1, WildcardPrefix.NUGGET.getMeta()));
        GTN_ItemList.WildcardDensePlate.set(new net.minecraft.item.ItemStack(WILDCARD_PREFIX, 1, WildcardPrefix.DENSE_PLATE.getMeta()));

        // spotless:on
    }
}
