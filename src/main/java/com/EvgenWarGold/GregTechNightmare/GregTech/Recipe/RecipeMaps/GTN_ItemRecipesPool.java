package com.EvgenWarGold.GregTechNightmare.GregTech.Recipe.RecipeMaps;

import static com.EvgenWarGold.GregTechNightmare.Utils.GTN_OreDict.getDense;
import static gregtech.api.enums.TierEU.RECIPE_EV;
import static gregtech.api.enums.TierEU.RECIPE_HV;
import static gregtech.api.enums.TierEU.RECIPE_IV;
import static gregtech.api.enums.TierEU.RECIPE_MV;
import static gregtech.api.util.GTModHandler.addCraftingRecipe;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import com.EvgenWarGold.GregTechNightmare.GregTech.GTN_ItemList;
import com.EvgenWarGold.GregTechNightmare.ModBlocks.BotaniaBlocks;

import gregtech.api.enums.GTValues;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.recipe.RecipeMaps;

public class GTN_ItemRecipesPool {

    public static void init() {
        // spotless:off
        // Mana Prospector
        addCraftingRecipe(
            GTN_ItemList.ManaProspector.get(1),
            new Object[]{"ABC", "GDG", "FWE",
                'A', Materials.LightFuel.getCells(1),
                'B', getDense(Materials.Manasteel),
                'C', Materials.Benzene.getCells(1),
                'G', getDense(Materials.RedstoneAlloy),
                'D', BotaniaBlocks.ManaPool.getItemStack(),
                'F', ItemList.Sensor_LV.get(1),
                'W', getDense(Materials.Thaumium),
                'E', ItemList.Emitter_LV.get(1)
            });

        GTValues.RA.stdBuilder()
            .itemInputs(
                new ItemStack(Items.leather, 8),
                new ItemStack(Blocks.chest, 1),
                Materials.Steel.getPlates(2),
                ItemList.Emitter_MV.get(1)
            )
            .itemOutputs(GTN_ItemList.HandyBag.get(1))

            .eut(RECIPE_MV)
            .duration(20 * 15)
            .addTo(RecipeMaps.assemblerRecipes);

        GTValues.RA.stdBuilder()
            .itemInputs(
                new ItemStack(Items.leather, 16),
                new ItemStack(Blocks.chest, 2),
                Materials.Aluminium.getPlates(4),
                ItemList.Emitter_HV.get(2)
            )
            .itemOutputs(GTN_ItemList.HandyBagLarge.get(1))

            .eut(RECIPE_HV)
            .duration(20 * 30)
            .addTo(RecipeMaps.assemblerRecipes);

        GTValues.RA.stdBuilder()
            .itemInputs(
                Materials.Copper.getPlates(4),
                new ItemStack(Items.redstone, 16),
                ItemList.Sensor_MV.get(1)
            )
            .itemOutputs(GTN_ItemList.MemoryCard6b.get(1))

            .eut(RECIPE_MV)
            .duration(20 * 5)
            .addTo(RecipeMaps.assemblerRecipes);

        GTValues.RA.stdBuilder()
            .itemInputs(
                Materials.Aluminium.getPlates(4),
                new ItemStack(Items.redstone, 24),
                ItemList.Sensor_HV.get(1)
            )
            .itemOutputs(GTN_ItemList.MemoryCard8b.get(1))

            .eut(RECIPE_HV)
            .duration(20 * 10)
            .addTo(RecipeMaps.assemblerRecipes);

        GTValues.RA.stdBuilder()
            .itemInputs(
                Materials.StainlessSteel.getPlates(4),
                new ItemStack(Items.redstone, 32),
                ItemList.Sensor_EV.get(1)
            )
            .itemOutputs(GTN_ItemList.MemoryCard10b.get(1))

            .eut(RECIPE_EV)
            .duration(20 * 15)
            .addTo(RecipeMaps.assemblerRecipes);

        GTValues.RA.stdBuilder()
            .itemInputs(
                Materials.Titanium.getPlates(4),
                new ItemStack(Items.redstone, 48),
                ItemList.Sensor_IV.get(1)
            )
            .itemOutputs(GTN_ItemList.MemoryCard12b.get(1))

            .eut(RECIPE_IV)
            .duration(20 * 20)
            .addTo(RecipeMaps.assemblerRecipes);
        // spotless:on
    }
}
