package com.EvgenWarGold.GregTechNightmare.GregTech.Items;

import static com.EvgenWarGold.GregTechNightmare.GregTech.Items.GTN_Items.LINK_TOOL;
import static com.EvgenWarGold.GregTechNightmare.GregTech.Items.GTN_Items.META_ITEM_01;

import net.minecraft.item.Item;

import com.EvgenWarGold.GregTechNightmare.GregTech.GTN_ItemList;

import cpw.mods.fml.common.registry.GameRegistry;

public class GTN_ItemsRegister {

    public static void init() {
        registryItems();
        registryItemContainers();
    }

    private static void registryItems() {
        Item[] itemsToReg = { META_ITEM_01, LINK_TOOL };

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

        // Vortex Token
        GTN_ItemList.VortexToken.set(META_ITEM_01.registerVariant(4));

        // Rait Token
        GTN_ItemList.RaitToken.set(META_ITEM_01.registerVariant(5));

        // Faotik Token
        GTN_ItemList.FaotikToken.set(META_ITEM_01.registerVariant(6));

        // Crazer Token
        GTN_ItemList.CrazerToken.set(META_ITEM_01.registerVariant(7));

        // Dom Token
        GTN_ItemList.DomToken.set(META_ITEM_01.registerVariant(8));

        // Soul Token
        GTN_ItemList.SoulToken.set(META_ITEM_01.registerVariant(9));

        // Ya9yu Token
        GTN_ItemList.Ya9yuToken.set(META_ITEM_01.registerVariant(10));

        // Voider Token
        GTN_ItemList.VoiderToken.set(META_ITEM_01.registerVariant(11));

        // OmnymToken
        GTN_ItemList.OmnymToken.set(META_ITEM_01.registerVariant(12));

        // Cinobi Token
        GTN_ItemList.CinobiToken.set(META_ITEM_01.registerVariant(13));

        // Quetz4l Token
        GTN_ItemList.Quetz4lToken.set(META_ITEM_01.registerVariant(14));

        // Potato Token
        GTN_ItemList.PotatoToken.set(META_ITEM_01.registerVariant(15));
        // spotless:on
    }
}
