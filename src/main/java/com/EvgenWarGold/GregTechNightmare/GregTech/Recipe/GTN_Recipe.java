package com.EvgenWarGold.GregTechNightmare.GregTech.Recipe;

import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Processing.UV.GTN_LargeBioLab;
import com.EvgenWarGold.GregTechNightmare.GregTech.Recipe.RecipeMapFrontends.GasCollectorRecipesFrontend;

import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.RecipeMapBackend;
import gregtech.api.recipe.RecipeMapBuilder;

public class GTN_Recipe {

    public static final RecipeMap<RecipeMapBackend> AdvancedBBFRecipes = RecipeMapBuilder
        .of("gtn.recipe.AdvancedBBFRecipes")
        .maxIO(2, 1, 0, 0)
        .progressBar(GTUITextures.PROGRESSBAR_ARROW_MULTIPLE)
        .build();

    public static final RecipeMap<RecipeMapBackend> CokeOvenRecipes = RecipeMapBuilder
        .of("gtn.recipe.AdvancedCokeOvenRecipes")
        .maxIO(1, 1, 0, 1)
        .progressBar(GTUITextures.PROGRESSBAR_ARROW_MULTIPLE)
        .build();
    public static final RecipeMap<?> ArcaneAssemblerRecipes = RecipeMapBuilder
        .of("gtn.recipe.LargeArcaneAssemblerRecipes")
        .maxIO(9, 1, 0, 0)
        .minInputs(1, 0)
        .build();
    public static final RecipeMap<?> GasCollectorRecipes = RecipeMapBuilder.of("gtn.recipe.GasCollectorRecipes")
        .maxIO(1, 0, 0, 11)
        .minInputs(1, 0)
        .progressBar(GTUITextures.PROGRESSBAR_ARROW_MULTIPLE)
        .frontend(GasCollectorRecipesFrontend::new)
        .build();
    public static final RecipeMap<?> VacuumNukeRecipes = RecipeMapBuilder.of("gtn.recipe.VacuumNukeRecipes")
        .maxIO(1, 1, 1, 1)
        .minInputs(1, 0)
        .progressBar(GTUITextures.PROGRESSBAR_ARROW_MULTIPLE)
        .build();

    public static final RecipeMap<?> ImprovedAlgaeFarmRecipes = RecipeMapBuilder
        .of("gtn.recipe.ImprovedAlgaeFarmRecipes")
        .maxIO(1, 5, 0, 0)
        .minInputs(0, 0)
        .progressBar(GTUITextures.PROGRESSBAR_ARROW_MULTIPLE)
        .build();
    public static final RecipeMap<?> ImprovedSliceNSpliceRecipes = RecipeMapBuilder
        .of("gtn.recipe.ImprovedSliceNSpliceRecipes")
        .maxIO(6, 1, 1, 0)
        .minInputs(0, 0)
        .progressBar(GTUITextures.PROGRESSBAR_ARROW)
        .build();

    public static final RecipeMap<?> LargeBioLabRecipes = RecipeMapBuilder.of("gtn.recipe.LargeBioLabRecipes")
        .maxIO(6, 1, 6, 0)
        .minInputs(1, 1)
        .progressBar(GTUITextures.PROGRESSBAR_ARROW_MULTIPLE)
        .neiSpecialInfoFormatter(GTN_LargeBioLab::getRecipeInfo)
        .build();
}
