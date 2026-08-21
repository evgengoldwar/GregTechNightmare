package com.EvgenWarGold.GregTechNightmare.GregTech.Recipe;

import com.EvgenWarGold.GregTechNightmare.GregTech.Recipe.RecipeMaps.AdvancedAssemblyLineRecipesPool;
import com.EvgenWarGold.GregTechNightmare.GregTech.Recipe.RecipeMaps.AdvancedBBFRecipesPool;
import com.EvgenWarGold.GregTechNightmare.GregTech.Recipe.RecipeMaps.AdvancedCokeOvenRecipesPool;
import com.EvgenWarGold.GregTechNightmare.GregTech.Recipe.RecipeMaps.GTN_ItemRecipesPool;
import com.EvgenWarGold.GregTechNightmare.GregTech.Recipe.RecipeMaps.GTN_META_BLOCK_CASING_01_RecipesPool;
import com.EvgenWarGold.GregTechNightmare.GregTech.Recipe.RecipeMaps.GTN_META_ITEM_01_RecipesPool;
import com.EvgenWarGold.GregTechNightmare.GregTech.Recipe.RecipeMaps.GTN_MTERecipesPool;
import com.EvgenWarGold.GregTechNightmare.GregTech.Recipe.RecipeMaps.GTN_MultiBlockRecipesPool;
import com.EvgenWarGold.GregTechNightmare.GregTech.Recipe.RecipeMaps.GasCollectorRecipesPool;
import com.EvgenWarGold.GregTechNightmare.GregTech.Recipe.RecipeMaps.ImprovedAlgaeFarmRecipesPool;
import com.EvgenWarGold.GregTechNightmare.GregTech.Recipe.RecipeMaps.ImprovedSliceNSpliceRecipesPool;
import com.EvgenWarGold.GregTechNightmare.GregTech.Recipe.RecipeMaps.LargeArcaneAssemblerRecipesPool;
import com.EvgenWarGold.GregTechNightmare.GregTech.Recipe.RecipeMaps.LargeBioLabRecipesPool;
import com.EvgenWarGold.GregTechNightmare.GregTech.Recipe.RecipeMaps.VacuumNukeRecipesPool;

public class RecipeLoader {

    public static void init() {
        GTN_MultiBlockRecipesPool.init();
        GTN_META_ITEM_01_RecipesPool.init();
        GTN_META_BLOCK_CASING_01_RecipesPool.init();
        AdvancedBBFRecipesPool.init();
        AdvancedCokeOvenRecipesPool.init();
        LargeArcaneAssemblerRecipesPool.init();
        GasCollectorRecipesPool.init();
        VacuumNukeRecipesPool.init();
        ImprovedAlgaeFarmRecipesPool.init();
        GTN_MTERecipesPool.init();
        ImprovedSliceNSpliceRecipesPool.init();
        GTN_ItemRecipesPool.init();
        AdvancedAssemblyLineRecipesPool.init();
        LargeBioLabRecipesPool.init();
    }
}
