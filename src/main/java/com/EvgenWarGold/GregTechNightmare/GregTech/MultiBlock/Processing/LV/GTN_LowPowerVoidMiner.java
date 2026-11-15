package com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Processing.LV;

import static gregtech.api.enums.HatchElement.Energy;
import static gregtech.api.enums.HatchElement.Maintenance;
import static gregtech.api.enums.HatchElement.OutputBus;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import com.EvgenWarGold.GregTechNightmare.GregTech.Api.MultiblockArea;
import com.EvgenWarGold.GregTechNightmare.GregTech.Api.MultiblockOffsets;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses.GTN_Casings;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses.GTN_MultiBlockBase;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses.GTN_MultiBlockTooltipBuilder;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses.StructureVariant;
import com.EvgenWarGold.GregTechNightmare.Utils.Authors;
import com.EvgenWarGold.GregTechNightmare.Utils.VoidMinerUtils;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.enums.Materials;
import gregtech.api.enums.SoundResource;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.util.GTUtility;

public class GTN_LowPowerVoidMiner extends GTN_MultiBlockBase<GTN_LowPowerVoidMiner> {

    private VoidMinerUtils voidMiner = null;
    private static final List<Integer> ALLOW_DIMENSION = Arrays.asList(-1, 7, 0);
    private boolean preGenerated = false;

    public GTN_LowPowerVoidMiner(int id, String name) {
        super(id, name);
    }

    public GTN_LowPowerVoidMiner(String name) {
        super(name);
    }

    @Override
    public List<StructureVariant<GTN_LowPowerVoidMiner>> getStructureVariants() {
        return List.of(
            new StructureVariant<>(
                "LowPowerVoidMiner",
                // spotless:off
                new String[][]{
                    {"   ", " B ", "   "},
                    {"   ", " B ", "   "},
                    {"   ", " B ", "   "},
                    {" B ", "BCB", " B "},
                    {" B ", "BCB", " B "},
                    {" B ", "BCB", " B "},
                    {"A~A", "AAA", "AAA"}
                },
                //spotless:on
                new MultiblockOffsets(1, 6, 0),
                new MultiblockArea(3, 7, 3),
                1,
                GTN_Casings.SolidSteelMachineCasing));
    }

    @Override
    public GTN_LowPowerVoidMiner createNewMetaEntity() {
        return new GTN_LowPowerVoidMiner(this.mName);
    }

    @Override
    public void createGtnTooltip(GTN_MultiBlockTooltipBuilder builder) {
        builder.addOutputBus()
            .addEnergyHatch()
            .addMaintenanceHatch();
    }

    @Override
    public Authors getAuthor() {
        return Authors.EVGEN_WAR_GOLD;
    }

    @Override
    public IStructureDefinition<GTN_LowPowerVoidMiner> getStructureDefinition() {
        return buildStructureDefinition(
            builder -> builder.addFrame('B', Materials.Cobalt)
                .addCasing('C', GTN_Casings.BoltedCarbonCasing)
                .addMainCasing('A', b -> b.hatches(OutputBus, Energy, Maintenance)));
    }

    @Override
    public void GTN_FirstTick(IGregTechTileEntity gte) {
        super.GTN_FirstTick(gte);
        int dimId = gte.getWorld().provider.dimensionId;

        if (gte.isServerSide()) {
            boolean matchFound = ALLOW_DIMENSION.stream()
                .anyMatch(n -> n.equals(dimId));

            if (!matchFound) {
                explodeMultiblock();
            }

            if (!preGenerated) {
                voidMiner = new VoidMinerUtils(Collections.singletonList(dimId));
                voidMiner.initDropMap(gte);
                preGenerated = true;
            }
        }
    }

    @Override
    public @NotNull CheckRecipeResult checkProcessing() {
        long availableEUt = getMaxInputPower();

        if (availableEUt < 128) {
            return CheckRecipeResultRegistry.insufficientPower(128);
        }

        Map<ItemStack, Integer> result = voidMiner.generateStackOre(8)
            .stream()
            .filter(stack -> !GTUtility.isStackInvalid(stack))
            .collect(Collectors.toMap(ItemStack::copy, stack -> stack.stackSize));

        if (processingHelper.outputItem(result)) {
            processingHelper.setDurationInSeconds(1);
            processingHelper.setEnergyUsageWithoutLoss(128);
            return processingHelper.resultSuccess();
        }

        return processingHelper.resultNoRecipe();
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected SoundResource getActivitySoundLoop() {
        return SoundResource.GTCEU_LOOP_MINER;
    }
}
