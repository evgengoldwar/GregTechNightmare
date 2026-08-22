package com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Generators.LV;

import static gregtech.api.enums.HatchElement.Dynamo;
import static gregtech.api.enums.HatchElement.InputHatch;
import static gregtech.api.enums.HatchElement.Maintenance;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import com.EvgenWarGold.GregTechNightmare.GregTech.Api.MultiblockArea;
import com.EvgenWarGold.GregTechNightmare.GregTech.Api.MultiblockOffsets;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses.CasingData;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses.GTN_Casings;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses.GTN_MultiBlockBase;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses.GTN_MultiBlockTooltipBuilder;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses.OverclockType;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses.StructureVariant;
import com.EvgenWarGold.GregTechNightmare.Utils.Authors;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.enums.Materials;
import gregtech.api.enums.SoundResource;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;

public class GTN_CreosoteEngine extends GTN_MultiBlockBase<GTN_CreosoteEngine> {

    private static int DYNAMO_TIER;
    private static long DYNAMO_AMP;
    private static final int CREOSOTE_USAGE_PER_SEC = 25;
    private final static FluidStack CREOSOTE;

    static {
        CREOSOTE = Materials.Creosote.getFluid(CREOSOTE_USAGE_PER_SEC);
    }

    public GTN_CreosoteEngine(int id, String name) {
        super(id, name);
    }

    public GTN_CreosoteEngine(String name) {
        super(name);
    }

    @Override
    public List<StructureVariant<GTN_CreosoteEngine>> getStructureVariants() {
        return List.of(
            new StructureVariant<>(
                "CreosoteEngine",
                // spotless:off
                new String[][]{
                    {"      B ", "     E B", "     E B", "      B "},
                    {"BFFFB~B ", "     E B", "     E B", "BFFFBBB "},
                    {"BCCCBD  ", "AAAAABBB", "AAAAABBB", "BCCCBD  "}},
                //spotless:on
                new MultiblockOffsets(5, 1, 0),
                new MultiblockArea(8, 3, 4),
                1,
                GTN_Casings.SolidSteelMachineCasing));
    }

    @Override
    public GTN_CreosoteEngine createNewMetaEntity() {
        return new GTN_CreosoteEngine(this.mName);
    }

    @Override
    public void createGtnTooltip(GTN_MultiBlockTooltipBuilder builder) {
        builder.addInputHatch()
            .addDynamoHatch()
            .addMaintenanceHatch();
    }

    @Override
    public Authors getAuthor() {
        return Authors.EVGEN_WAR_GOLD;
    }

    @Override
    public IStructureDefinition<GTN_CreosoteEngine> getStructureDefinition() {
        return buildStructureDefinition(
            builder -> builder.addMainCasing('B', b -> b.hatches(InputHatch, Dynamo, Maintenance))
                .addFrame('D', Materials.Iron)
                .addFrame('E', Materials.Steel)
                .addAllGlasses('F')
                .addCasing('C', GTN_Casings.SteelGearBoxCasing)
                .addCasing('A', GTN_Casings.ULVMachineCasing));
    }

    @Override
    public boolean isEnergyMultiBlock() {
        return false;
    }

    @Override
    public @NotNull CheckRecipeResult checkProcessing() {
        if (getAllMaxDynamoBuffer() == getAllDynamoBuffer()) {
            return processingHelper.resultNoRecipe();
        }

        switch (DYNAMO_TIER) {
            case 1 -> {
                CREOSOTE.amount = Math.toIntExact(CREOSOTE_USAGE_PER_SEC * DYNAMO_AMP);
                processingHelper.setEnergyGenerate(32 * DYNAMO_AMP);
            }
            case 2 -> {
                CREOSOTE.amount = Math.toIntExact(CREOSOTE_USAGE_PER_SEC * DYNAMO_AMP) * 4;
                processingHelper.setEnergyGenerate(128 * DYNAMO_AMP);
            }
        }

        if (processingHelper.consumeFluid(CREOSOTE, CREOSOTE.amount)) {
            processingHelper.setDurationInSeconds(1);
            return processingHelper.resultGenerating();
        }

        return CheckRecipeResultRegistry.NO_FUEL_FOUND;
    }

    @Override
    protected boolean GTN_checkMachine(IGregTechTileEntity gte, ItemStack stack) {
        DYNAMO_AMP = getDynamoAmperage();
        DYNAMO_TIER = getTierDynamo();
        return checkCountDynamo(4) && setDynamoTier(2, false);
    }

    @Override
    public OverclockType getOverclockType() {
        return OverclockType.NONE;
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected SoundResource getActivitySoundLoop() {
        return SoundResource.GTCEU_LOOP_BOILER;
    }
}
