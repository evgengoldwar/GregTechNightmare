package com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Generators.HV;

import static com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses.GTN_HatchElement.DynamoMulti;
import static com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses.GTN_HatchElement.SensorHatch;
import static gregtech.api.enums.HatchElement.Dynamo;
import static gregtech.api.enums.HatchElement.InputBus;
import static gregtech.api.enums.HatchElement.InputHatch;
import static gregtech.api.enums.HatchElement.Maintenance;
import static gregtech.api.enums.HatchElement.OutputHatch;
import static gregtech.api.util.GTStructureUtility.ofFrame;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import com.EvgenWarGold.GregTechNightmare.GregTech.Api.MultiblockArea;
import com.EvgenWarGold.GregTechNightmare.GregTech.Api.MultiblockOffsets;
import com.EvgenWarGold.GregTechNightmare.GregTech.Hatch.GTN_SensorHatch;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses.GTN_Casings;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses.GTN_MultiBlockBase;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses.GTN_MultiBlockTooltipBuilder;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses.GTN_StructureUtility;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses.StructureVariant;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses.TierData;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses.TieredElementBuilder;
import com.EvgenWarGold.GregTechNightmare.GregTech.Recipe.GTN_Recipe;
import com.EvgenWarGold.GregTechNightmare.GregTech.Recipe.MetaData.SimpleMetaData;
import com.EvgenWarGold.GregTechNightmare.Utils.Authors;
import com.EvgenWarGold.GregTechNightmare.Utils.GTN_Utils;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;

import gregtech.api.enums.HeatingCoilLevel;
import gregtech.api.enums.Materials;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.logic.ProcessingLogic;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.recipe.check.SimpleCheckRecipeResult;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.OverclockCalculator;
import gregtech.api.util.ParallelHelper;
import gregtech.api.util.shutdown.ShutDownReasonRegistry;
import gtPlusPlus.xmod.thermalfoundation.fluid.TFFluids;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;

public class GTN_VacuumNuke extends GTN_MultiBlockBase<GTN_VacuumNuke> {

    TierData globalCasing = createTierData("casing", true);
    TierData itemPipe = createTierData("itemPipe");
    TierData coilBlock = createTierData("coil");
    TierData glass = createTierData("glass");
    private double heat = 0;
    private long generating = 0;
    private double heatIncrease = 0;
    private FluidStack inputFluid;
    private FluidStack outputFluid;
    private final static DecimalFormat df = new DecimalFormat("0.#");
    private final static FluidStack IC2_COOLANT;
    private final static FluidStack SUPER_COOLANT;
    private final static FluidStack GELID_CRYOTHEUM;

    static {
        IC2_COOLANT = FluidRegistry.getFluidStack("ic2coolant", 0);
        SUPER_COOLANT = Materials.SuperCoolant.getFluid(0);
        GELID_CRYOTHEUM = new FluidStack(TFFluids.fluidCryotheum, 0);
    }

    public GTN_VacuumNuke(int id, String name) {
        super(id, name);
    }

    public GTN_VacuumNuke(String name) {
        super(name);
    }

    @Override
    public List<StructureVariant<GTN_VacuumNuke>> getStructureVariants() {
        return Arrays.asList(
            new StructureVariant<>(
                "VacuumNuke",
                // spotless:off
                new String[][]{
                    {"   E E   ","   CBC   ","   CBC   ","   CBC   ","         ","         ","         ","         ","   CBC   ","   CBC   ","   CBC   ","   E E   "},
                    {"   E E   ","  CCBCC  ","  C   C  ","  C   C  ","  CFFFC  ","  CFFFC  ","  CFFFC  ","  CFFFC  ","  C   C  ","  C   C  ","  CCBCC  ","   E E   "},
                    {"  E B E  "," CCCDCCC "," C  DD C "," CD  DDC "," CDD  DC "," C DD  C "," C  DD C "," CD  DDC "," CDD  DC "," C DD  C "," CCCDCCC ","  E B E  "},
                    {"EE FFF EE","CCC   CCC","C D     C","C D     C"," F    DF "," F    DF "," FD    F "," FD    F ","C     D C","C     D C","CCC   CCC","EE FFF EE"},
                    {"  BF~FB  ","BBD A DBB","B D A D B","B   A   B"," F  A  F "," FD A DF "," FD A DF "," F  A  F ","B   A   B","B D A D B","BBD A DBB","  BFBFB  "},
                    {"EE FFF EE","CCC   CCC","C     D C","C     D C"," FD    F "," FD    F "," F    DF "," F    DF ","C D     C","C D     C","CC    CCC","EE FFF EE"},
                    {"  E B E  "," CCCDCCC "," C DD  C "," CDD  DC "," CD  DDC "," C  DD C "," C DD  C "," CDD  DC "," CD  DDC "," C  DD C "," CCCDCCC ","  E B E  "},
                    {"   E E   ","  CCBCC  ","  C   C  ","  C   C  ","  CFFFC  ","  CFFFC  ","  CFFFC  ","  CFFFC  ","  C   C  ","  C   C  ","  CCBCC  ","   E E   "},
                    {"   E E   ","   CBC   ","   CBC   ","   CBC   ","         ","         ","         ","         ","   CBC   ","   CBC   ","   CBC   ","   E E   "}
                },
                //spotless:on
                new MultiblockOffsets(4, 4, 0),
                new MultiblockArea(9, 9, 12),
                1,
                GTN_Casings.FrostProofMachineCasing));
    }

    @Override
    public GTN_VacuumNuke createNewMetaEntity() {
        return new GTN_VacuumNuke(this.mName);
    }

    @Override
    public void createGtnTooltip(GTN_MultiBlockTooltipBuilder builder) {
        builder.addInputBus()
            .addInputHatch()
            .addOutputHatch()
            .addDynamoOrBufferedHatch()
            .addMaintenanceHatch();
    }

    @Override
    public Authors getAuthor() {
        return Authors.EVGEN_WAR_GOLD;
    }

    @Override
    public IStructureDefinition<GTN_VacuumNuke> getStructureDefinition() {
        return buildStructureDefinition(
            builder -> builder.addElement(
                'A',
                TieredElementBuilder.create(itemPipe, GTN_VacuumNuke.class)
                    .casings(
                        GTN_Casings.TinItemPipeCasing,
                        GTN_Casings.BrassItemPipeCasing,
                        GTN_Casings.ElectrumItemPipeCasing,
                        GTN_Casings.PlatinumItemPipeCasing,
                        GTN_Casings.OsmiumItemPipeCasing,
                        GTN_Casings.QuantiumItemPipeCasing,
                        GTN_Casings.FluxedElectrumItemPipeCasing,
                        GTN_Casings.BlackPlutoniumItemPipeCasing)
                    .build())
                .addElement('B', GTN_Casings.SolidSteelMachineCasing.asElement())
                .addElement('D', GTN_StructureUtility.createAllTierCoilBlock(coilBlock))
                .addElement('E', ofFrame(Materials.Steel))
                .addElement('F', GTN_StructureUtility.createAllTieredGlass(glass))
                .addElement(
                    'C',
                    TieredElementBuilder.create(globalCasing, GTN_VacuumNuke.class)
                        .casings(
                            GTN_Casings.FrostProofMachineCasing,
                            GTN_Casings.StableTitaniumMachineCasing,
                            GTN_Casings.RobustTungstenSteelMachineCasing)
                        .hatches(InputBus, Dynamo, InputHatch, OutputHatch, Maintenance, SensorHatch, DynamoMulti)
                        .build()));
    }

    @Override
    public boolean isEnergyMultiBlock() {
        return false;
    }

    @Override
    public RecipeMap<?> getRecipeMap() {
        return GTN_Recipe.VacuumNukeRecipes;
    }

    @Override
    protected ProcessingLogic createProcessingLogic() {
        return new ProcessingLogic() {

            @Override
            protected @NotNull CheckRecipeResult applyRecipe(@NotNull GTRecipe recipe, @NotNull ParallelHelper helper,
                @NotNull OverclockCalculator calculator, @NotNull CheckRecipeResult result) {
                CheckRecipeResult res = super.applyRecipe(recipe, helper, calculator, result);

                this.duration = (int) (this.duration * (1 + 0.1 * itemPipe.getCasingTier()));

                return res;
            }

            @Override
            protected @NotNull CheckRecipeResult onRecipeStart(@NotNull GTRecipe recipe) {
                generating = recipe.getMetadataOrDefault(SimpleMetaData.GENERATING_EU, 0);
                heatIncrease = recipe.getMetadataOrDefault(SimpleMetaData.TEMPERATURE_INCREASE, 0.0);
                inputFluid = recipe.getMetadataOrDefault(SimpleMetaData.FLUID_INPUT, null);
                outputFluid = recipe.getMetadataOrDefault(SimpleMetaData.FLUID_OUTPUT, null);
                return CheckRecipeResultRegistry.SUCCESSFUL;
            }

            @Override
            protected @Nonnull CheckRecipeResult validateRecipe(@Nonnull GTRecipe recipe) {
                int tier = recipe.getMetadataOrDefault(SimpleMetaData.MULTIBLOCK_TIER, 0);

                return globalCasing.getCasingTier() >= tier ? CheckRecipeResultRegistry.SUCCESSFUL
                    : SimpleCheckRecipeResult.ofFailure("invalid_casing");
            }
        }.setMaxParallelSupplier(this::getTrueParallel);
    }

    @Override
    public void getWailaBody(ItemStack itemStack, List<String> currentTip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currentTip, accessor, config);
        final NBTTagCompound tag = accessor.getNBTData();

        double heat = tag.getDouble("heat");

        if (heat > 0) {
            currentTip.add(GTN_Utils.tr("multiblock.VacuumNuke.waila.heat", df.format(heat)));
        }
    }

    @Override
    public void getWailaNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x, int y,
        int z) {
        super.getWailaNBTData(player, tile, tag, world, x, y, z);
        tag.setDouble("heat", heat);
    }

    @Override
    protected void runMachine(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        super.runMachine(aBaseMetaTileEntity, aTick);
        if (mMaxProgresstime > 0 && aTick % 20 == 0) {
            heat += heatIncrease;

            ArrayList<FluidStack> inputFluids = getStoredFluids();
            long fluidAmount = 0;

            if (!inputFluids.isEmpty()) {
                for (FluidStack fluid : inputFluids) {
                    if (fluid.isFluidEqual(inputFluid)) {
                        fluidAmount += fluid.amount;
                    }
                }
            }

            if (fluidAmount > 0) {
                if (inputFluid.isFluidEqual(IC2_COOLANT)) {
                    long decreaseTemp = fluidAmount / 10_000;
                    FluidStack drain = inputFluid.copy();
                    drain.amount = Math.toIntExact(fluidAmount);
                    if (depleteInput(drain, true) && decreaseTemp > 0) {
                        heat -= decreaseTemp;
                        depleteInput(drain);
                        outputFluid.amount = Math.toIntExact(10_000 * decreaseTemp);
                        addOutput(outputFluid);
                    }
                }

                if (inputFluid.isFluidEqual(SUPER_COOLANT)) {
                    long decreaseTemp = fluidAmount / 1_000;
                    FluidStack drain = inputFluid.copy();
                    drain.amount = Math.toIntExact(fluidAmount);
                    if (depleteInput(drain, true) && decreaseTemp > 0) {
                        heat -= decreaseTemp;
                        depleteInput(drain);
                        outputFluid.amount = Math.toIntExact(10_000 * decreaseTemp);
                        addOutput(outputFluid);
                    }
                }

                if (inputFluid.isFluidEqual(GELID_CRYOTHEUM)) {
                    FluidStack drain = inputFluid.copy();
                    drain.amount = Math.toIntExact(fluidAmount);
                    if (depleteInput(drain, true)) {
                        heat -= fluidAmount;
                        depleteInput(drain);
                    }
                }
            }

            if (heat >= 100) {
                stopMachine(ShutDownReasonRegistry.CRITICAL_NONE);
                causeMaintenanceIssue();
                heat = 0;
            }

            if (heat < 0) {
                heat = 0;
            }

            HeatingCoilLevel level = coilBlock.getCoilLevel();

            long eu = (long) (generating * (1 + 0.1 * level.getTier()));

            setEnergyGenerate(checkEnergy(eu, heat));

            for (GTN_SensorHatch hatch : mSensorHatches) {
                hatch.updateRedstoneOutput(heat);
            }
        }
    }

    private long checkEnergy(long generation, double temperature) {
        if (temperature <= 0) return 0L;

        if (temperature <= 70) return (long) (generation * temperature / 70.0);

        if (temperature <= 99) {
            return (long) (generation + (temperature - 70) / (99 - 70) * (generation * 3 - generation));
        }

        return generation * 3;
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        aNBT.setLong("generating", generating);
        aNBT.setDouble("heat", heat);
        aNBT.setDouble("heat_increase", heatIncrease);
        if (inputFluid != null) {
            NBTTagCompound fluidInput = new NBTTagCompound();
            inputFluid.writeToNBT(fluidInput);
            aNBT.setTag("fluidInput", fluidInput);
        }

        if (outputFluid != null) {
            NBTTagCompound fluidOutput = new NBTTagCompound();
            outputFluid.writeToNBT(fluidOutput);
            aNBT.setTag("fluidOutput", fluidOutput);
        }
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        generating = aNBT.getLong("generating");
        heat = aNBT.getDouble("heat");
        heatIncrease = aNBT.getDouble("heat_increase");
        if (aNBT.hasKey("fluidInput")) {
            inputFluid = FluidStack.loadFluidStackFromNBT(aNBT.getCompoundTag("fluidInput"));
        }

        if (aNBT.hasKey("fluidOutput")) {
            outputFluid = FluidStack.loadFluidStackFromNBT(aNBT.getCompoundTag("fluidOutput"));
        }

    }
}
