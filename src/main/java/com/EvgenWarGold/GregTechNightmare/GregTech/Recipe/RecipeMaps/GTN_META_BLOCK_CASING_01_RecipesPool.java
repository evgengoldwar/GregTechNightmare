package com.EvgenWarGold.GregTechNightmare.GregTech.Recipe.RecipeMaps;

import static com.EvgenWarGold.GregTechNightmare.Utils.GTN_InventoryUtils.createItem;
import static com.EvgenWarGold.GregTechNightmare.Utils.GTN_InventoryUtils.setStackSize;
import static com.EvgenWarGold.GregTechNightmare.Utils.GTN_OreDict.getPlate;
import static gregtech.api.enums.TierEU.RECIPE_EV;
import static gregtech.api.enums.TierEU.RECIPE_LV;
import static gregtech.api.enums.TierEU.RECIPE_MV;
import static gregtech.api.util.GTModHandler.addShapelessCraftingRecipe;

import net.minecraft.init.Blocks;

import com.EvgenWarGold.GregTechNightmare.GregTech.GTN_ItemList;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses.GTN_Casings;

import gregtech.api.enums.GTValues;
import gregtech.api.enums.Materials;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.util.GTUtility;
import gtPlusPlus.core.material.MaterialsAlloy;

public class GTN_META_BLOCK_CASING_01_RecipesPool {

    public static void init() {
        // spotless:off

        // Coke Oven Casing
        addShapelessCraftingRecipe(
            GTN_ItemList.CokeOvenCasing.get(1),
            new Object[]{ GTN_ItemList.AdvancedClay.get(1),
                GTN_ItemList.AdvancedClay.get(1),
                GTN_ItemList.AdvancedClay.get(1),
                GTN_ItemList.AdvancedClay.get(1)});

        // Arboreal Casing
        GTValues.RA.stdBuilder()
            .itemInputs(
                MaterialsAlloy.TUMBAGA.getFrameBox(1),
                MaterialsAlloy.TUMBAGA.getPlate(4),
                MaterialsAlloy.TUMBAGA.getRing(1),
                MaterialsAlloy.TUMBAGA.getScrew(2),
                setStackSize(createItem(Blocks.log, 0), 4)
            )
            .fluidInputs(
                Materials.Steam.getGas(16_000)
            )
            .itemOutputs(GTN_ItemList.ArborealCasing.get(1))

            .eut(RECIPE_LV)
            .duration(20 * 5)
            .addTo(RecipeMaps.assemblerRecipes);

        // Soul Casing
        GTValues.RA.stdBuilder()
            .itemInputs(
                Materials.Soularium.getPlates(2),
                Materials.Soularium.getIngots(4),
                Materials.Steel.getBlocks(1)
            )
            .itemOutputs(GTN_ItemList.SoulCasing.get(1))

            .eut(RECIPE_MV)
            .duration(20 * 10)
            .addTo(RecipeMaps.assemblerRecipes);

        // Runic Matrix Casing
        GTValues.RA.stdBuilder()
            .itemInputs(
                GTN_Casings.MagicCasing.getItemStack(1),
                getPlate(Materials.Thaumium, 6),
                GTUtility.getIntegratedCircuit(1)
            )
            .itemOutputs(GTN_ItemList.RunicMatrixCasing.get(1))

            .eut(RECIPE_EV)
            .duration(20 * 10)
            .addTo(RecipeMaps.assemblerRecipes);

        //spotless:on
    }
}
