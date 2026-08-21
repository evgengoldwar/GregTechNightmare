package com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Processing.UV;

import static gregtech.api.enums.HatchElement.Energy;
import static gregtech.api.enums.HatchElement.InputBus;
import static gregtech.api.enums.HatchElement.InputHatch;
import static gregtech.api.enums.HatchElement.Maintenance;
import static gregtech.api.enums.HatchElement.OutputBus;
import static gregtech.api.util.GTStructureUtility.chainAllGlasses;

import java.util.List;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import com.EvgenWarGold.GregTechNightmare.GregTech.Api.MultiblockArea;
import com.EvgenWarGold.GregTechNightmare.GregTech.Api.MultiblockOffsets;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses.GTN_Casings;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses.GTN_MultiBlockBase;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses.GTN_MultiBlockTooltipBuilder;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses.GTN_ProcessingLogic;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses.StructureVariant;
import com.EvgenWarGold.GregTechNightmare.GregTech.Recipe.GTN_Recipe;
import com.EvgenWarGold.GregTechNightmare.Utils.Authors;
import com.EvgenWarGold.GregTechNightmare.Utils.GTN_Utils;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.enums.GTValues;
import gregtech.api.enums.SoundResource;
import gregtech.api.enums.VoltageIndex;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.logic.ProcessingLogic;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.structure.error.StructureError;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.ParallelHelper;
import gregtech.nei.RecipeDisplayInfo;
import it.unimi.dsi.fastutil.Pair;

public class GTN_LargeBioLab extends GTN_MultiBlockBase<GTN_LargeBioLab> {

    private int glassTier = -1;

    public GTN_LargeBioLab(int id, String name) {
        super(id, name);
    }

    public GTN_LargeBioLab(String name) {
        super(name);
    }

    @Override
    public List<StructureVariant<GTN_LargeBioLab>> getStructureVariants() {
        return List.of(
            new StructureVariant<>(
                "LargeBioLab",
                // spotless:off
                new String[][] {
                    {"AAAAA", "AAAAA", "AAFAA", "AAAAA", "AAAAA"},
                    {"AAAAA", "G   G", "G C G", "G   G", "AAAAA"},
                    {"AAAAA", "G C G", "F U F", "G C G", "AAAAA"},
                    {"AAAAA", "G   G", "G C G", "G   G", "AAAAA"},
                    {"AA~AA", "AAAAA", "AAFAA", "AAAAA", "AAAAA"}
                },
                // spotless:on
                new MultiblockOffsets(2, 4, 0),
                new MultiblockArea(5, 5, 5),
                1,
                GTN_Casings.ChemicallyInertMachineCasing));
    }

    @Override
    public GTN_LargeBioLab createNewMetaEntity() {
        return new GTN_LargeBioLab(this.mName);
    }

    @Override
    public void createGtnTooltip(GTN_MultiBlockTooltipBuilder builder) {
        builder.addInputBus()
            .addOutputBus()
            .addInputHatch()
            .addEnergyHatch()
            .addMaintenanceHatch();
    }

    @Override
    public Authors getAuthor() {
        return Authors.CRAZER;
    }

    public static List<String> getRecipeInfo(RecipeDisplayInfo recipeInfo) {
        return List.of(
            GTN_Utils.tr(
                "multiblock.LargeBioLab.recipe_info.minimum_glass_tier",
                GTValues.VN[recipeInfo.recipe.mSpecialValue]),
            GTN_Utils.tr("multiblock.LargeBioLab.recipe_info.glass_tier_bonus"),
            GTN_Utils.tr("multiblock.LargeBioLab.recipe_info.fluid_discount"));
    }

    @Override
    public void checkMachine(IGregTechTileEntity gte, ItemStack stack, List<StructureError> errors) {
        glassTier = -1;
        super.checkMachine(gte, stack, errors);
    }

    @Override
    public IStructureDefinition<GTN_LargeBioLab> getStructureDefinition() {
        return buildStructureDefinition(
            builder -> builder
                .addElement('G', chainAllGlasses(-1, (te, tier) -> te.glassTier = tier, te -> te.glassTier))
                .addCasing('C', GTN_Casings.CleanStainlessSteelMachineCasing)
                .addCasing('F', GTN_Casings.FilterMachineCasing)
                .addCasing('U', GTN_Casings.UVMachineCasing)
                .addMainCasing('A', b -> b.hatches(InputBus, OutputBus, Energy, Maintenance, InputHatch)));
    }

    @Override
    protected ProcessingLogic createProcessingLogic() {
        return new GTN_ProcessingLogic() {

            @Override
            public CheckRecipeResult process() {
                setEuModifier(GTN_LargeBioLab.this.getEuModifier());
                setSpeedBonus(1F / GTN_LargeBioLab.this.getSpeedBonus());
                setOverclockType(GTN_LargeBioLab.this.getOverclockType());
                return super.process();
            }

            @Nonnull
            @Override
            protected CheckRecipeResult validateRecipe(@Nonnull GTRecipe recipe) {
                if (glassTier < recipe.mSpecialValue) {
                    return CheckRecipeResultRegistry.insufficientMachineTier(recipe.mSpecialValue);
                }
                return CheckRecipeResultRegistry.SUCCESSFUL;
            }

            @Nonnull
            @Override
            protected Stream<GTRecipe> findRecipeMatches(RecipeMap<?> map) {
                if (map == null) return Stream.empty();
                return map.getAllRecipes()
                    .stream()
                    .filter(
                        recipe -> GTN_LargeBioLab.this.applyGlassDiscount(recipe)
                            .maxParallelCalculatedByInputs(1, inputFluids, inputItems) >= 1);
            }

            @Nonnull
            @Override
            protected ParallelHelper createParallelHelper(@Nonnull GTRecipe recipe) {
                return super.createParallelHelper(GTN_LargeBioLab.this.applyGlassDiscount(recipe));
            }
        }.setMaxParallelSupplier(this::getMaxParallelRecipes)
            .noRecipeCaching();
    }

    private GTRecipe applyGlassDiscount(GTRecipe baseRecipe) {
        GTRecipe recipe = baseRecipe.copy();
        int tierDifference = Math.max(glassTier - recipe.mSpecialValue, 0);
        int discountPercent = Math.min(tierDifference * 10, 80);
        int remainingPercent = 100 - discountPercent;

        for (int i = 0; i < recipe.mFluidInputs.length; i++) {
            if (recipe.mFluidInputs[i] != null && recipe.mFluidInputs[i].amount > 0) {
                recipe.mFluidInputs[i].amount = applyPercent(recipe.mFluidInputs[i].amount, remainingPercent);
            }
        }

        recipe.mCanBeBuffered = false;
        return recipe;
    }

    private static int applyPercent(int value, int percent) {
        long scaled = (long) value * percent;
        long result = (scaled + 99L) / 100L;
        if (result <= 0) return 1;
        return result > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) result;
    }

    @Override
    public RecipeMap<?> getRecipeMap() {
        return GTN_Recipe.LargeBioLabRecipes;
    }

    @Override
    public float getSpeedBonus() {
        return 2F;
    }

    @Override
    protected Pair<Integer, Integer> getMinMaxEnergyTier() {
        return Pair.of(VoltageIndex.UV, VoltageIndex.UIV);
    }

    @Override
    public int getMaxParallelRecipes() {
        return 16;
    }

    @Override
    public boolean supportsInputSeparation() {
        return false;
    }

    @Override
    public boolean supportsBatchMode() {
        return false;
    }

    @Override
    public boolean supportsSingleRecipeLocking() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected SoundResource getActivitySoundLoop() {
        return SoundResource.GT_MACHINES_ALGAE_LOOP;
    }
}
