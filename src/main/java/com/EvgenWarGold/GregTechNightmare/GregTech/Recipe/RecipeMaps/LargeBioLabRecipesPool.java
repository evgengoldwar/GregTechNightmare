package com.EvgenWarGold.GregTechNightmare.GregTech.Recipe.RecipeMaps;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.EvgenWarGold.GregTechNightmare.GregTech.Recipe.GTN_Recipe;

import bartworks.API.recipe.BartWorksRecipeMaps;
import gregtech.api.enums.ItemList;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTUtility;

public final class LargeBioLabRecipesPool {

    private static final int FULL_CHANCE = 10_000;

    private LargeBioLabRecipesPool() {}

    public static void init() {
        for (GTRecipe source : BartWorksRecipeMaps.bioLabRecipes.getAllRecipes()) {
            int petriOutputIndex = getFilledPetriDishOutputIndex(source);
            if (petriOutputIndex < 0 || !isPetriReplicationRecipe(source)) continue;

            int chance = source.getOutputChance(petriOutputIndex);
            if (chance <= 0) continue;

            GTRecipe recipe = source.copy();
            recipe.mFakeRecipe = false;
            recipe.mHidden = false;
            recipe.mEnabled = true;
            recipe.mCanBeBuffered = false;
            recipe.mNeedsEmptyOutput = false;
            recipe.isNBTSensitive = false;
            recipe.mSpecialItems = null;
            recipe.mSpecialValue = GTUtility.getTier(Math.max(recipe.mEUt, 1));

            makePetriDishGuaranteed(recipe, petriOutputIndex);
            convertFluidContainers(recipe);
            scaleFluidInputsForChance(recipe.mFluidInputs, chance);

            GTN_Recipe.LargeBioLabRecipes.add(recipe);
        }
    }

    private static boolean isPetriReplicationRecipe(GTRecipe recipe) {
        if (recipe.mSpecialItems != null || recipe.mInputs == null) return false;

        ItemStack emptyPetriDish = ItemList.EmptyPetriDish.get(1);
        for (ItemStack input : recipe.mInputs) {
            if (input != null && input.isItemEqual(emptyPetriDish) && !input.hasTagCompound()) {
                return true;
            }
        }
        return false;
    }

    private static int getFilledPetriDishOutputIndex(GTRecipe recipe) {
        if (recipe.mOutputs == null) return -1;

        ItemStack emptyPetriDish = ItemList.EmptyPetriDish.get(1);
        for (int i = 0; i < recipe.mOutputs.length; i++) {
            ItemStack output = recipe.mOutputs[i];
            if (output != null && output.isItemEqual(emptyPetriDish)
                && output.hasTagCompound()
                && !output.getTagCompound()
                    .hasKey("NEI")) {
                return i;
            }
        }
        return -1;
    }

    private static void makePetriDishGuaranteed(GTRecipe recipe, int petriOutputIndex) {
        if (recipe.mOutputChances == null) return;
        recipe.mOutputChances = recipe.mOutputChances.clone();
        recipe.mOutputChances[petriOutputIndex] = FULL_CHANCE;
    }

    private static void convertFluidContainers(GTRecipe recipe) {
        List<ItemStack> itemInputs = new ArrayList<>();
        List<FluidStack> fluidInputs = new ArrayList<>();
        List<ItemStack> containersToRemove = new ArrayList<>();

        if (recipe.mFluidInputs != null) {
            for (FluidStack fluid : recipe.mFluidInputs) {
                if (fluid != null) addFluid(fluidInputs, fluid.copy());
            }
        }

        if (recipe.mInputs != null) {
            for (ItemStack input : recipe.mInputs) {
                if (input == null) continue;

                FluidStack fluid = GTUtility.convertCellToFluid(input);
                if (fluid == null) {
                    itemInputs.add(input);
                    continue;
                }

                addFluid(fluidInputs, fluid);

                ItemStack singleInput = input.copy();
                singleInput.stackSize = 1;
                ItemStack container = GTUtility.getContainerForFilledItem(singleInput, true);
                if (container != null) {
                    container.stackSize = input.stackSize;
                    containersToRemove.add(container);
                }
            }
        }

        recipe.mInputs = itemInputs.toArray(new ItemStack[0]);
        recipe.mFluidInputs = fluidInputs.toArray(new FluidStack[0]);
        removeReturnedContainers(recipe, containersToRemove);
    }

    private static void addFluid(List<FluidStack> fluids, FluidStack fluid) {
        for (FluidStack existing : fluids) {
            if (existing.isFluidEqual(fluid)) {
                long amount = (long) existing.amount + fluid.amount;
                existing.amount = amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;
                return;
            }
        }
        fluids.add(fluid);
    }

    private static void removeReturnedContainers(GTRecipe recipe, List<ItemStack> containersToRemove) {
        if (recipe.mOutputs == null || recipe.mOutputs.length == 0 || containersToRemove.isEmpty()) return;

        ItemStack[] outputs = recipe.mOutputs.clone();
        for (ItemStack container : containersToRemove) {
            int remaining = container.stackSize;
            for (int i = 0; i < outputs.length && remaining > 0; i++) {
                ItemStack output = outputs[i];
                if (output == null || !output.isItemEqual(container)) continue;

                int removed = Math.min(output.stackSize, remaining);
                output = output.copy();
                output.stackSize -= removed;
                remaining -= removed;
                outputs[i] = output.stackSize > 0 ? output : null;
            }
        }

        List<ItemStack> compactOutputs = new ArrayList<>();
        List<Integer> compactChances = new ArrayList<>();
        for (int i = 0; i < outputs.length; i++) {
            if (outputs[i] == null) continue;
            compactOutputs.add(outputs[i]);
            compactChances.add(recipe.getOutputChance(i));
        }

        recipe.mOutputs = compactOutputs.toArray(new ItemStack[0]);
        recipe.mOutputChances = new int[compactChances.size()];
        for (int i = 0; i < compactChances.size(); i++) {
            recipe.mOutputChances[i] = compactChances.get(i);
        }
    }

    private static void scaleFluidInputsForChance(FluidStack[] fluids, int chance) {
        if (fluids == null) return;
        for (FluidStack fluid : fluids) {
            if (fluid != null && fluid.amount > 0) {
                fluid.amount = scaleForChance(fluid.amount, chance);
            }
        }
    }

    private static int scaleForChance(int value, int chance) {
        long numerator = (long) value * FULL_CHANCE;
        long result = (numerator + chance - 1L) / chance;
        if (result <= 0) return 1;
        return result > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) result;
    }
}
