package com.EvgenWarGold.GregTechNightmare.GregTech.Recipe.RecipeMaps;

import static com.EvgenWarGold.GregTechNightmare.Utils.GTN_InventoryUtils.createItem;
import static com.EvgenWarGold.GregTechNightmare.Utils.GTN_InventoryUtils.setStackSize;
import static com.EvgenWarGold.GregTechNightmare.Utils.GTN_OreDict.getBlock;
import static com.EvgenWarGold.GregTechNightmare.Utils.GTN_OreDict.getFluid;
import static com.EvgenWarGold.GregTechNightmare.Utils.GTN_OreDict.getFrameGt;
import static com.EvgenWarGold.GregTechNightmare.Utils.GTN_OreDict.getGearGt;
import static com.EvgenWarGold.GregTechNightmare.Utils.GTN_OreDict.getNanite;
import static com.EvgenWarGold.GregTechNightmare.Utils.GTN_OreDict.getPlate;
import static com.EvgenWarGold.GregTechNightmare.Utils.GTN_OreDict.getSuperDense;
import static com.EvgenWarGold.GregTechNightmare.Utils.GTN_OreDict.getWireGt01;
import static com.EvgenWarGold.GregTechNightmare.Utils.GTN_OreDict.getWireGt16;
import static com.EvgenWarGold.GregTechNightmare.Utils.GTN_RecipeUtils.getCircuit;
import static com.EvgenWarGold.GregTechNightmare.Utils.GTN_RecipeUtils.getCircuits;
import static gregtech.api.enums.TierEU.RECIPE_EV;
import static gregtech.api.enums.TierEU.RECIPE_HV;
import static gregtech.api.enums.TierEU.RECIPE_IV;
import static gregtech.api.enums.TierEU.RECIPE_LV;
import static gregtech.api.enums.TierEU.RECIPE_LuV;
import static gregtech.api.enums.TierEU.RECIPE_MV;
import static gregtech.api.enums.TierEU.RECIPE_UHV;
import static gregtech.api.enums.TierEU.RECIPE_UV;
import static gregtech.api.util.GTModHandler.addCraftingRecipe;
import static gregtech.api.util.GTRecipeBuilder.MINUTES;
import static gregtech.api.util.GTRecipeConstants.RESEARCH_ITEM;
import static gregtech.api.util.GTRecipeConstants.SCANNING;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import com.EvgenWarGold.GregTechNightmare.GregTech.GTN_ItemList;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses.GTN_Casings;
import com.EvgenWarGold.GregTechNightmare.GregTech.Recipe.GTN_RecipeBuilder;
import com.EvgenWarGold.GregTechNightmare.ModBlocks.BloodMagicBlocks;
import com.EvgenWarGold.GregTechNightmare.ModBlocks.DraconicEvolutionBlocks;
import com.EvgenWarGold.GregTechNightmare.ModBlocks.EnderIOBlocks;
import com.EvgenWarGold.GregTechNightmare.ModBlocks.ThaumcraftBlocks;
import com.EvgenWarGold.GregTechNightmare.ModBlocks.ThaumicBasesBlocks;
import com.EvgenWarGold.GregTechNightmare.ModBlocks.ThaumicEnergeticsBlocks;
import com.EvgenWarGold.GregTechNightmare.ModItems.EnderIOItems;
import com.EvgenWarGold.GregTechNightmare.ModItems.NewHorizonsCoreModItems;
import com.EvgenWarGold.GregTechNightmare.ModItems.TaintedMagicItems;
import com.EvgenWarGold.GregTechNightmare.ModItems.ThaumicHorizons;
import com.EvgenWarGold.GregTechNightmare.ModItems.ThaumicTinkererItems;
import com.EvgenWarGold.GregTechNightmare.Utils.GTN_OreDict;

import goodgenerator.util.ItemRefer;
import gregtech.api.enums.GTValues;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.util.GTRecipeConstants;
import gregtech.api.util.recipe.Scanning;
import gtPlusPlus.core.material.MaterialMisc;
import gtPlusPlus.core.material.MaterialsAlloy;
import gtPlusPlus.xmod.gregtech.api.enums.GregtechItemList;

public class GTN_MultiBlockRecipesPool {

    public static void init() {
        // spotless:off

        // Advanced BBF
        addCraftingRecipe(
            GTN_ItemList.AdvancedBBF.get(1),
            new Object[]{"ABA", "BCB", "BBB",
                'A', OrePrefixes.frameGt.get(Materials.Steel),
                'B', ItemList.Casing_Firebricks,
                'C', ItemList.Machine_Bricked_BlastFurnace
            });

        // Bronze Void Miner
        addCraftingRecipe(
            GTN_ItemList.BronzeVoidMiner.get(1),
            new Object[]{"ABA", "BCB", "EDE",
                'A', OrePrefixes.frameGt.get(Materials.Bronze),
                'B', ItemList.Casing_BronzePlatedBricks,
                'D', ItemList.Machine_Bronze_Boiler.get(1),
                'C', new ItemStack(Items.diamond_pickaxe),
                'E', ItemList.Casing_Firebox_Bronze
            });

        // Low Power Void Miner
        addCraftingRecipe(
            GTN_ItemList.LowPowerVoidMiner.get(1),
            new Object[]{"AAA", "BCB", "DDD",
                'A', GTN_Casings.BoltedCarbonCasing.getItemStack(),
                'B', getCircuit(Materials.LV),
                'C', GTN_ItemList.BronzeVoidMiner.get(1),
                'D', GTN_Casings.SolidSteelMachineCasing.getItemStack()
            });

        // Medium Power Bender
        GTValues.RA.stdBuilder()
            .itemInputs(
                ItemList.Casing_MV.get(10),
                ItemList.Machine_MV_Bender.get(1),
                ItemList.Electric_Piston_MV.get(2),
                ItemList.Electric_Motor_MV.get(2),
                getCircuits(Materials.MV, 2)
            )
            .itemOutputs(GTN_ItemList.MediumPowerBender.get(1))

            .eut(RECIPE_MV)
            .duration(20 * 60)
            .addTo(RecipeMaps.assemblerRecipes);

        // Medium Power Extruder
        GTValues.RA.stdBuilder()
            .itemInputs(
                ItemList.Casing_MV.get(10),
                ItemList.Machine_MV_Extruder.get(1),
                ItemList.Electric_Piston_MV.get(2),
                ItemList.Robot_Arm_MV.get(2),
                getCircuits(Materials.MV, 2)
            )
            .itemOutputs(GTN_ItemList.MediumPowerExtruder.get(1))

            .eut(RECIPE_MV)
            .duration(20 * 60)
            .addTo(RecipeMaps.assemblerRecipes);

        // Medium Power Assembler
        GTValues.RA.stdBuilder()
            .itemInputs(
                ItemList.Casing_MV.get(10),
                ItemList.Machine_MV_Assembler.get(1),
                ItemList.Electric_Pump_MV.get(2),
                ItemList.Robot_Arm_MV.get(2),
                getCircuits(Materials.MV, 2)
            )
            .itemOutputs(GTN_ItemList.MediumPowerAssembler.get(1))

            .eut(RECIPE_MV)
            .duration(20 * 60)
            .addTo(RecipeMaps.assemblerRecipes);

        // Medium Power Circuit Assembler
        GTValues.RA.stdBuilder()
            .itemInputs(
                ItemList.Casing_MV.get(10),
                ItemList.Machine_MV_CircuitAssembler.get(1),
                ItemList.Electric_Pump_MV.get(2),
                ItemList.Robot_Arm_MV.get(2),
                getCircuits(Materials.MV, 2)
            )
            .itemOutputs(GTN_ItemList.MediumPowerCircuitAssembler.get(1))

            .eut(RECIPE_MV)
            .duration(20 * 60)
            .addTo(RecipeMaps.assemblerRecipes);

        // Advanced Coke Oven
        addCraftingRecipe(
            GTN_ItemList.AdvancedCokeOven.get(1),
            new Object[]{"AAA", "A A", "AAA",
                'A', GTN_Casings.CokeOvenCasing.getItemStack()
            });

        // Node Energizer
        addCraftingRecipe(
            GTN_ItemList.NodeEnergizer.get(1),
            new Object[]{"ABA", "BCB", "ABA",
                'A', GTN_Casings.MagicCasing.getItemStack(),
                'B', ItemList.Field_Generator_HV.get(1),
                'C', OrePrefixes.frameGt.get(Materials.Thaumium)
            });

        // Large Arcane Assembler
        GTValues.RA.stdBuilder()
            .itemInputs(
                GTN_Casings.MagicCasing.getItemStack(32),
                ThaumicEnergeticsBlocks.ArcaneAssembler.getItemStack(),
                ItemList.Electric_Piston_LuV.get(8),
                ItemList.Electric_Motor_LuV.get(16),
                getBlock(Materials.Ichorium, 8),
                getCircuits(Materials.ZPM, 4)
            )
            .itemOutputs(GTN_ItemList.LargeArcaneAssembler.get(1))

            .eut(RECIPE_LuV)
            .duration(150 * 60)
            .addTo(RecipeMaps.assemblerRecipes);

        // Tree Sprouter
        GTValues.RA.stdBuilder()
            .itemInputs(
                getCircuits(Materials.LV, 4),
                ItemList.Robot_Arm_LV.get(1),
                ItemList.Conveyor_Module_LV.get(2),
                GTN_Casings.ArborealCasing.getItemStack(4),
                ItemList.Hull_LV.get(1)
            )
            .fluidInputs(
                Materials.Water.getFluid(16_000)
            )
            .itemOutputs(GTN_ItemList.TreeSprouter.get(1))

            .eut(RECIPE_LV)
            .duration(20 * 60)
            .addTo(RecipeMaps.assemblerRecipes);

        // Creosote Engine
        GTValues.RA.stdBuilder()
            .itemInputs(
                GTN_Casings.SolidSteelMachineCasing.getItemStack(8),
                GTN_Casings.SteelGearBoxCasing.getItemStack(4),
                ItemList.Electric_Pump_LV.get(2),
                ItemList.Hull_LV.get(1)
            )
            .fluidInputs(
                Materials.Creosote.getFluid(4_000)
            )
            .itemOutputs(GTN_ItemList.CreosoteEngine.get(1))

            .eut(RECIPE_LV)
            .duration(20 * 60)
            .addTo(RecipeMaps.assemblerRecipes);

        // Medium Power Wiremill
        GTValues.RA.stdBuilder()
            .itemInputs(
                ItemList.Casing_MV.get(10),
                ItemList.Machine_MV_Wiremill.get(1),
                ItemList.Electric_Piston_MV.get(2),
                ItemList.Robot_Arm_MV.get(2),
                getCircuits(Materials.MV, 2)
            )
            .itemOutputs(GTN_ItemList.MediumPowerWireMill.get(1))

            .eut(RECIPE_MV)
            .duration(20 * 60)
            .addTo(RecipeMaps.assemblerRecipes);

        // Medium Power Engraver
        GTValues.RA.stdBuilder()
            .itemInputs(
                ItemList.Casing_MV.get(10),
                ItemList.Machine_MV_LaserEngraver.get(1),
                ItemList.Electric_Pump_MV.get(2),
                ItemList.Electric_Piston_MV.get(2),
                ItemList.Robot_Arm_MV.get(2),
                getCircuits(Materials.HV, 2)
            )
            .itemOutputs(GTN_ItemList.MediumPowerEngraver.get(1))

            .eut(RECIPE_MV)
            .duration(20 * 60)
            .addTo(RecipeMaps.assemblerRecipes);

        // Extreme Power Circuit Assembler
        GTValues.RA.stdBuilder()
            .itemInputs(
                GTN_Casings.StableTitaniumMachineCasing.getItemStack(8),
                ItemList.Emitter_EV.get(2),
                ItemList.Sensor_EV.get(2),
                ItemList.Field_Generator_EV.get(2),
                ItemList.Machine_EV_CircuitAssembler.get(1)
            )
            .fluidInputs(
                Materials.Radon.getGas(8_000)
            )
            .itemOutputs(GTN_ItemList.ExtremePowerCircuitAssembler.get(1))

            .eut(RECIPE_EV)
            .duration(20 * 60)
            .addTo(RecipeMaps.assemblerRecipes);

        // UltimatePrecise
        GTValues.RA.stdBuilder()
            .metadata(RESEARCH_ITEM, ItemList.AssemblingMachineUHV.get(1))
            .metadata(SCANNING, new Scanning(30 * MINUTES, 30))
            .itemInputs(
                ItemRefer.Precise_Assembler.get(64),
                ItemList.Field_Generator_UHV.get(64),
                ItemList.Conveyor_Module_UHV.get(64),
                getCircuits(Materials.UEV, 64),
                ItemRefer.Compassline_Casing_UHV.get(64),
                GTN_Casings.HollowCasing.getItemStack(64),
                ItemList.Circuit_Chip_Stemcell.get(64),
                ItemList.Circuit_Chip_Biocell.get(64),
                getSuperDense(Materials.RadoxPolymer, 24),
                getFrameGt(Materials.CosmicNeutronium, 64),
                NewHorizonsCoreModItems.HighEnergyFlowCircuit.get(64),
                ItemList.Circuit_Chip_BioCPU.get(64),
                getGearGt(Materials.CosmicNeutronium, 64),
                GregtechItemList.Laser_Lens_Special.get(1),
                getWireGt01(Materials.SuperconductorUHV, 64),
                getNanite(Materials.Neutronium, 2)
            )
            .fluidInputs(
                getFluid(Materials.UUMatter, 512_000),
                getFluid(Materials.SuperCoolant, 512_000),
                MaterialMisc.MUTATED_LIVING_SOLDER.getFluidStack(18_000)
            )
            .itemOutputs(GTN_ItemList.UltimatePrecise.get(1))

            .eut(RECIPE_UHV)
            .duration(20 * 60)
            .addTo(GTRecipeConstants.AssemblyLine);

           // Gas Collector
         addCraftingRecipe(
            GTN_ItemList.GasCollector.get(1),
            new Object[]{"ABA", "BCB", "ABA",
                'A', GTN_Casings.HeatProofMachineCasing.getItemStack(),
                'B', getCircuit(Materials.LV),
                'C', ItemList.Electric_Pump_LV.get(1)
            });

        // Laser Meteor Miner
        GTValues.RA.stdBuilder()
            .itemInputs(
                GTN_ItemList.LowPowerVoidMiner.get(16),
                ThaumcraftBlocks.ArcaneBore.getItemStack(48),
                ItemList.Robot_Arm_IV.get(4),
                ItemList.Conveyor_Module_IV.get(4),
                ItemRefer.HiC_T1.get(8),
                getWireGt16(Materials.TungstenSteel, 8),
                TaintedMagicItems.WandFocusTime.get(1),
                ThaumicTinkererItems.WandFocusEfreetFlame.get(1),
                ThaumicHorizons.WandFocusDisintegration.get(1)
            )
            .itemOutputs(GTN_ItemList.LaserMeteorMiner.get(1))

            .eut(RECIPE_IV)
            .duration(20 * 60)
            .addTo(RecipeMaps.assemblerRecipes);

        // Blood Enchanter
        GTValues.RA.stdBuilder()
            .itemInputs(
                GTN_Casings.RobustTungstenSteelMachineCasing.getItemStack(8),
                BloodMagicBlocks.RitualStone.getItemStack(4),
                ThaumicBasesBlocks.Overchanter.getItemStack(),
                DraconicEvolutionBlocks.DissEnchanter.getItemStack(),
                new ItemStack(Blocks.enchanting_table),
                getCircuits(Materials.IV, 4)
            )
            .itemOutputs(GTN_ItemList.BloodEnchanter.get(1))
            .eut(RECIPE_IV)
            .duration(120 * 20)
            .addTo(RecipeMaps.assemblerRecipes);

        // Vacuum Nuke
        GTValues.RA.stdBuilder()
            .itemInputs(
                new ItemStack(gtPlusPlus.core.block.ModBlocks.blockDecayablesChest),
                getCircuits(Materials.EV, 4),
                GTN_OreDict.getDense(Materials.Thorium, 16),
                ItemList.Robot_Arm_HV.get(4),
                ItemList.Electric_Pump_HV.get(4),
                ItemList.Field_Generator_MV.get(2)
            )
            .itemOutputs(GTN_ItemList.VacuumNuke.get(1))
            .eut(RECIPE_EV)
            .duration(120 * 20)
            .addTo(RecipeMaps.assemblerRecipes);

        // Improved Algae Farm
        GTValues.RA.stdBuilder()
            .itemInputs(
                GTN_Casings.SterileFarmCasing.getItemStack(8),
                GTN_Casings.ArborealCasing.getItemStack(8),
                getCircuits(Materials.HV, 4),
                setStackSize(createItem(Blocks.log, 0), 32)
            )
            .fluidInputs(
                getFluid(Materials.Water, 64000)
            )
            .itemOutputs(GTN_ItemList.ImprovedAlgaeFarm.get(1))
            .eut(RECIPE_HV)
            .duration(1200)
            .addTo(RecipeMaps.assemblerRecipes);

        // Improved Slice'N'Splice
        GTValues.RA.stdBuilder()
            .itemInputs(
                getPlate(Materials.EnergeticAlloy, 16),
                GTN_Casings.SoulCasing.getItemStack(16),
                getCircuits(Materials.IV, 4),
                EnderIOBlocks.SliceNSplice.getItemStack(),
                EnderIOItems.OctadicCapacitor.get(8),
                EnderIOItems.PulsatingCrystal.get(16)
            )
            .itemOutputs(GTN_ItemList.ImprovedSliceNSplice.get(1))
            .eut(RECIPE_EV)
            .duration(1200)
            .addTo(RecipeMaps.assemblerRecipes);

        // High Power Component Assembler
        GTValues.RA.stdBuilder()
            .itemInputs(
                ItemRefer.Compassline_Casing_LV.get(8),
                ItemList.Robot_Arm_HV.get(4),
                ItemList.Conveyor_Module_HV.get(4),
                ItemList.Electric_Motor_HV.get(4),
                ItemList.Electric_Piston_HV.get(4),
                ItemList.Electric_Motor_HV.get(4),
                ItemList.Field_Generator_LV.get(4)
            )
            .fluidInputs(Materials.Glass.getMolten(8_000))
            .itemOutputs(GTN_ItemList.HighPowerComponentAssembler.get(1))
            .eut(RECIPE_HV)
            .duration(60 * 20)
            .addTo(RecipeMaps.assemblerRecipes);

        // Zero Power WireMill
        GTN_RecipeBuilder.builder()
            .metadata(RESEARCH_ITEM, GregtechItemList.Industrial_WireFactory.get(1))
            .metadata(SCANNING, new Scanning(30 * MINUTES, 30))
            .itemInputs(
                GregtechItemList.Industrial_WireFactory.get(64),
                ItemList.Field_Generator_ZPM.get(16),
                ItemList.Conveyor_Module_ZPM.get(32),
                getSuperDense(Materials.NaquadahAlloy, 64),
                ItemList.ZPM_Coil.get(16),
                GregtechItemList.Energy_Core_ZPM.get(8)
            )
            .fluidInputs(MaterialsAlloy.PIKYONIUM.getFluidStack(50_000))
            .itemOutputs(GTN_ItemList.ZeroPowerWireMill.get(1))
            .recipeZPM()
            .durationInMinutes(1)
            .addTo(GTRecipeConstants.AssemblyLine);

        // Magic EBF
        addCraftingRecipe(
            GTN_ItemList.MagicEBF.get(1),
            new Object[]{
                "AAA",
                "BCB",
                "DGD",
                'A', ItemList.Machine_Multi_BlastFurnace.get(1),
                'B', getPlate(Materials.Void),
                'C', GTN_Casings.MagicCasing.getItemStack(),
                'D', getCircuit(Materials.MV),
                'G', getGearGt(Materials.Void)
            });

        // Large BioLab
        GTValues.RA.stdBuilder()
            .itemInputs(
                GTN_Casings.ChemicallyInertMachineCasing.getItemStack(16),
                GTN_Casings.CleanStainlessSteelMachineCasing.getItemStack(16),
                GTN_Casings.FilterMachineCasing.getItemStack(16),
                GTN_Casings.UVMachineCasing.getItemStack(8),
                ItemList.Electric_Pump_UV.get(8),
                ItemList.Field_Generator_UV.get(2),
                ItemList.Circuit_Chip_Stemcell.get(16),
                ItemList.Circuit_Chip_Biocell.get(8),
                ItemList.Circuit_Chip_BioCPU.get(4))
            .fluidInputs(Materials.SolderingAlloy.getMolten(2_304))
            .itemOutputs(GTN_ItemList.LargeBioLab.get(1))
            .eut(RECIPE_UV)
            .duration(60 * 20)
            .addTo(RecipeMaps.assemblerRecipes);
        //spotless:on
    }
}
