package com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.transpose;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_ELECTRIC_BLAST_FURNACE;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_ELECTRIC_BLAST_FURNACE_ACTIVE;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_ELECTRIC_BLAST_FURNACE_ACTIVE_GLOW;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_ELECTRIC_BLAST_FURNACE_GLOW;
import static gregtech.api.util.GTUtility.validMTEList;
import static mcp.mobius.waila.api.SpecialChars.RED;
import static mcp.mobius.waila.api.SpecialChars.RESET;
import static net.minecraft.util.StatCollector.translateToLocalFormatted;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.EvgenWarGold.GregTechNightmare.GregTech.Hatch.GTN_DataInput;
import com.EvgenWarGold.GregTechNightmare.GregTech.Hatch.GTN_DataOutput;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import org.jetbrains.annotations.NotNull;

import com.EvgenWarGold.GregTechNightmare.GregTech.Api.MultiblockArea;
import com.EvgenWarGold.GregTechNightmare.GregTech.Hatch.GTN_SensorHatch;
import com.EvgenWarGold.GregTechNightmare.GregTech.Recipe.RecipeResult.ResultInsufficientRangeTier;
import com.EvgenWarGold.GregTechNightmare.Utils.Authors;
import com.EvgenWarGold.GregTechNightmare.Utils.Constants;
import com.EvgenWarGold.GregTechNightmare.Utils.GTN_Utils;
import com.gtnewhorizon.structurelib.alignment.constructable.IConstructable;
import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;

import gregtech.api.enums.Textures;
import gregtech.api.interfaces.IIconContainer;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.logic.ProcessingLogic;
import gregtech.api.metatileentity.implementations.MTEExtendedPowerMultiBlockBase;
import gregtech.api.metatileentity.implementations.MTEHatch;
import gregtech.api.metatileentity.implementations.MTEHatchDynamo;
import gregtech.api.metatileentity.implementations.MTEHatchEnergy;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.api.util.VoidProtectionHelper;
import gregtech.common.tileentities.machines.IDualInputHatch;
import gtPlusPlus.xmod.gregtech.api.metatileentity.implementations.MTEHatchSteamBusInput;
import gtPlusPlus.xmod.gregtech.api.metatileentity.implementations.MTEHatchSteamBusOutput;
import gtPlusPlus.xmod.gregtech.api.metatileentity.implementations.base.MTEHatchCustomFluidBase;
import it.unimi.dsi.fastutil.Pair;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import tectech.thing.metaTileEntity.hatch.MTEHatchDynamoMulti;

public abstract class GTN_MultiBlockBase<T extends GTN_MultiBlockBase<T>> extends MTEExtendedPowerMultiBlockBase<T>
    implements IConstructable, ISurvivalConstructable {

    // region Abstract
    public abstract List<StructureVariant<T>> getStructureVariants();

    public abstract T createNewMetaEntity();

    public abstract void createGtnTooltip(GTN_MultiBlockTooltipBuilder builder);

    public abstract Authors getAuthor();
    // endregion

    // region Variables
    // Translate
    private static final String TRANSLATE_KEY = "multiblock.";
    private final String MULTIBLOCK_NAME_KEY;
    // Hatches
    public ArrayList<MTEHatchSteamBusInput> mSteamInputBusses = new ArrayList<>();
    public ArrayList<MTEHatchSteamBusOutput> mSteamOutputBusses = new ArrayList<>();
    public ArrayList<MTEHatchCustomFluidBase> mSteamInputFluids = new ArrayList<>();
    public ArrayList<GTN_SensorHatch> mSensorHatch = new ArrayList<>();
    public ArrayList<MTEHatchDynamoMulti> mDynamoMultiHatches = new ArrayList<>();
    public ArrayList<GTN_DataInput> mDataInputHatches = new ArrayList<>();
    public ArrayList<GTN_DataOutput> mDataOutputHatches = new ArrayList<>();
    // Processing
    private int maxParallel = 1;
    private float euModifier = 1;
    private float speedBonus = 1;
    // Global Variable
    public final Map<CoordMultiBlock, IGregTechTileEntity> multiBlocks = new HashMap<>();
    protected int multiBlockTier = 0;
    protected GTN_Casings mainCasing;
    protected int mainCasingCount = 0;
    protected int mainCasingTextureId = 0;
    protected final List<TierData> registeredTierData = new ArrayList<>();
    // endregion

    // region Class Construct
    public GTN_MultiBlockBase(int id, String name) {
        super(id, TRANSLATE_KEY + name, GTN_Utils.tr(TRANSLATE_KEY + name));
        MULTIBLOCK_NAME_KEY = TRANSLATE_KEY + name;
        initDefaultVariant();
    }

    public GTN_MultiBlockBase(String name) {
        super(name);
        MULTIBLOCK_NAME_KEY = TRANSLATE_KEY + name;
        initDefaultVariant();
    }
    // endregion

    // region Create Meta
    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return createNewMetaEntity();
    }
    // endregion

    // region Construct MultiBlock
    @Override
    public void clearHatches() {
        super.clearHatches();
        this.mSteamInputFluids.clear();
        this.mSteamInputBusses.clear();
        this.mSteamOutputBusses.clear();
        this.mSensorHatch.clear();
        this.mDynamoMultiHatches.clear();
        this.mDataInputHatches.clear();
        this.mDataOutputHatches.clear();
        mainCasingCount = 0;

        for (TierData tierData : registeredTierData) {
            tierData.reset();
        }
    }

    protected boolean GTN_checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
        return true;
    }

    @Override
    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
        List<StructureVariant<T>> variants = getStructureVariants();
        boolean built = false;

        for (StructureVariant<T> variant : variants) {
            if (variant.check(self())) {
                built = true;
                break;
            }
        }

        boolean GTN_checkMachine = GTN_checkMachine(aBaseMetaTileEntity, aStack);

        if (isNoMaintenanceIssue()) {
            repairMachine();
        }

        updateCasingTextureFromTierData();
        updateHatchTexture();

        if (getBaseMetaTileEntity() != null && mainCasingTextureId > 0) {
            getBaseMetaTileEntity().issueTileUpdate();
        }

        return built && GTN_checkMachine;
    }

    @Override
    public void construct(ItemStack itemStack, boolean hintsOnly) {
        List<StructureVariant<T>> variants = getStructureVariants();

        int index = Math.min(itemStack.stackSize - 1, variants.size() - 1);
        StructureVariant<T> variant = variants.get(index);

        buildPiece(
            variant.piece,
            itemStack,
            hintsOnly,
            variant.multiblockOffsets.offsetHorizontal,
            variant.multiblockOffsets.offsetVertical,
            variant.multiblockOffsets.offsetDepth);
    }

    @Override
    public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env) {
        if (this.mMachine) return -1;
        List<StructureVariant<T>> variants = getStructureVariants();

        int index = Math.min(stackSize.stackSize - 1, variants.size() - 1);
        StructureVariant<T> variant = variants.get(index);

        return this.survivalBuildPiece(
            variant.piece,
            stackSize,
            variant.multiblockOffsets.offsetHorizontal,
            variant.multiblockOffsets.offsetVertical,
            variant.multiblockOffsets.offsetDepth,
            elementBudget,
            env,
            false,
            true);
    }
    // endregion

    // region Textures
    public IIconContainer getMainOverlay() {
        return OVERLAY_FRONT_ELECTRIC_BLAST_FURNACE;
    }

    public IIconContainer getMainOverlayActive() {
        return OVERLAY_FRONT_ELECTRIC_BLAST_FURNACE_ACTIVE;
    }

    public IIconContainer getMainOverlayGlow() {
        return OVERLAY_FRONT_ELECTRIC_BLAST_FURNACE_GLOW;
    }

    public IIconContainer getMainOverlayActiveGlow() {
        return OVERLAY_FRONT_ELECTRIC_BLAST_FURNACE_ACTIVE_GLOW;
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, ForgeDirection side, ForgeDirection facing,
        int colorIndex, boolean aActive, boolean aRedstone) {

        StructureVariant<T> variant = null;

        int textureId = mainCasing.textureId;

        if (multiBlockTier > 0 && mainCasingTextureId == 0) {
            variant = getStructureVariants().get(multiBlockTier - 1);
        } else if (mainCasingTextureId != 0) {
            textureId = mainCasingTextureId;
        }

        if (variant != null) {
            textureId = variant.casing.textureId;
        }

        if (side == facing) {
            if (aActive) return new ITexture[] { Textures.BlockIcons.getCasingTextureForId(textureId),
                TextureFactory.builder()
                    .addIcon(getMainOverlayActive())
                    .extFacing()
                    .build(),
                TextureFactory.builder()
                    .addIcon(getMainOverlayActiveGlow())
                    .extFacing()
                    .glow()
                    .build() };
            return new ITexture[] { Textures.BlockIcons.getCasingTextureForId(textureId), TextureFactory.builder()
                .addIcon(getMainOverlay())
                .extFacing()
                .build(),
                TextureFactory.builder()
                    .addIcon(getMainOverlayGlow())
                    .extFacing()
                    .glow()
                    .build() };
        }
        return new ITexture[] { Textures.BlockIcons.getCasingTextureForId(textureId) };
    }
    // endregion

    // region ProcessingLogic
    public int getMaxParallelRecipes() {
        return maxParallel;
    }

    public float getEuModifier() {
        return euModifier;
    }

    public float getSpeedBonus() {
        return speedBonus;
    }

    protected ProcessingLogic createProcessingLogic() {
        return new GTN_ProcessingLogic() {

            @NotNull
            @Override
            public CheckRecipeResult process() {

                setEuModifier(isEnergyMultiBlock() ? getEuModifier() : 0);
                setSpeedBonus(isEnergyMultiBlock() ? 1F / getSpeedBonus() : 1);
                setOverclockType(getOverclockType());
                return super.process();
            }

        }.setMaxParallelSupplier(this::getMaxParallelRecipes);
    }

    @Override
    public @NotNull CheckRecipeResult checkProcessing() {
        Pair<Integer, Integer> energyTier = getMinMaxEnergyTier();
        int minTierEnergyHatch = mEnergyHatches.stream()
            .mapToInt(MTEHatchEnergy::getTierForStructure)
            .min()
            .orElse(-1);

        int maxTierEnergyHatch = mEnergyHatches.stream()
            .mapToInt(MTEHatchEnergy::getTierForStructure)
            .max()
            .orElse(-1);

        int minTierExoticEnergyHatch = mExoticEnergyHatches.stream()
            .mapToInt(MTEHatch::getTierForStructure)
            .min()
            .orElse(-1);

        int maxTierExoticEnergyHatch = mExoticEnergyHatches.stream()
            .mapToInt(MTEHatch::getTierForStructure)
            .max()
            .orElse(-1);

        boolean validEnergyHatch = false;
        boolean validExoticEnergyHatch = false;

        if (energyTier != null) {
            if (!(minTierEnergyHatch >= energyTier.left() && maxTierEnergyHatch <= energyTier.right())) {
                validEnergyHatch = true;
            }
        }

        if (energyTier != null) {
            if (!(minTierExoticEnergyHatch >= energyTier.left() && maxTierExoticEnergyHatch <= energyTier.right())) {
                validExoticEnergyHatch = true;
            }
        }

        if (validEnergyHatch && validExoticEnergyHatch) {
            return ResultInsufficientRangeTier.of(energyTier.left(), energyTier.right());
        }

        return super.checkProcessing();
    }

    protected Pair<Integer, Integer> getMinMaxEnergyTier() {
        return null;
    }
    // endregion

    // region NBT
    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        aNBT.setInteger("maxParallel", getMaxParallelRecipes());
        aNBT.setFloat("euModifier", getEuModifier());
        aNBT.setFloat("speedBonus", 1F / getSpeedBonus());
        aNBT.setInteger("multiblockTier", multiBlockTier);
        aNBT.setInteger("mainCasingTextureId", mainCasingTextureId);

        NBTTagList multiBlockList = new NBTTagList();
        for (CoordMultiBlock coordMultiBlock : multiBlocks.keySet()) {
            NBTTagCompound blockData = new NBTTagCompound();
            IGregTechTileEntity gte = multiBlocks.get(coordMultiBlock);
            IMetaTileEntity mte = gte.getMetaTileEntity();
            if (mte == null) {
                blockData.setString("type", "MTE is NULL");
            } else {
                blockData.setString("type", mte.getLocalName());
            }
            blockData.setInteger("dim", coordMultiBlock.dim);
            blockData.setInteger("x", coordMultiBlock.x);
            blockData.setInteger("y", coordMultiBlock.y);
            blockData.setInteger("z", coordMultiBlock.z);

            multiBlockList.appendTag(blockData);
        }

        aNBT.setTag("multiBlocks", multiBlockList);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        maxParallel = Math.max(aNBT.getInteger("maxParallel"), 1);
        euModifier = aNBT.getFloat("euModifier");
        if (euModifier <= 0) euModifier = 1;
        speedBonus = aNBT.getFloat("speedBonus");
        if (speedBonus <= 0) speedBonus = 1;
        multiBlockTier = aNBT.getInteger("multiblockTier");
        mainCasingTextureId = aNBT.getInteger("mainCasingTextureId");

        multiBlocks.clear();
        NBTTagList multiBlockList = aNBT.getTagList("multiBlocks", 10);

        for (int i = 0; i < multiBlockList.tagCount(); i++) {
            NBTTagCompound blockData = multiBlockList.getCompoundTagAt(i);

            int dim = blockData.getInteger("dim");
            int x = blockData.getInteger("x");
            int y = blockData.getInteger("y");
            int z = blockData.getInteger("z");

            CoordMultiBlock coordMultiBlock = new CoordMultiBlock(dim, x, y, z);

            multiBlocks.put(coordMultiBlock, null);
        }
    }
    // endregion

    // region Tooltip
    public String getMachineType() {
        return tr("machine_type");
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        final GTN_MultiBlockTooltipBuilder tt = new GTN_MultiBlockTooltipBuilder();
        tt.addMachineType(getMachineType());
        tt.addInfoMultiLineTranslated(tr("tooltip"));
        tt.addAuthor(getAuthor());
        addMultiBlockAreaInfo(tt);
        if (isEnergyMultiBlock()) {
            addMultiBlockBasicInfo(tt);
        }
        tt.beginStructureBlock();
        createGtnTooltip(tt);
        tt.toolTipFinisher(Constants.MOD_NAME);
        return tt;
    }
    // endregion

    // region Waila
    @Override
    public void getWailaBody(ItemStack itemStack, List<String> currentTip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        final NBTTagCompound tag = accessor.getNBTData();

        int trueParallel = tag.getInteger("trueParallel");
        float euModifier = tag.getFloat("euModifier");
        float speedBonus = tag.getFloat("speedBonus");
        int timeReduction = tag.getInteger("timeReduction");
        int powerIncrease = tag.getInteger("powerIncrease");
        int multiblockTier = tag.getInteger("multiblockTier");

        if (tag.getBoolean("incompleteStructure")) {
            currentTip.add(RED + translateToLocalFormatted("GT5U.waila.multiblock.status.incomplete") + RESET);
        } else {
            if (isEnergyMultiBlock()) {
                if (trueParallel > 0) {
                    currentTip.add(GTN_Utils.tr("multiblock.waila.max_parallel", trueParallel));
                }

                if (euModifier > 0) {
                    currentTip.add(GTN_Utils.tr("multiblock.waila.eu_modifier", Math.round(euModifier * 100)));
                }

                if (speedBonus > 0) {
                    currentTip.add(GTN_Utils.tr("multiblock.waila.speed_bonus", (int) Math.round(100.0 / speedBonus)));
                }

                if (getOverclockType() != null) {
                    currentTip.add(GTN_Utils.tr("multiblock.waila.overclock", timeReduction, powerIncrease));
                }

                if (multiblockTier > 0 && getStructureVariants().size() > 1) {
                    currentTip.add(GTN_Utils.tr("multiblock.waila.tier", multiblockTier));
                }
            }
            super.getWailaBody(itemStack, currentTip, accessor, config);
        }
    }

    @Override
    public void getWailaNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x, int y,
        int z) {
        super.getWailaNBTData(player, tile, tag, world, x, y, z);

        tag.setInteger("trueParallel", getMaxParallelRecipes());
        tag.setFloat("euModifier", getEuModifier());
        tag.setFloat("speedBonus", 1F / getSpeedBonus());
        tag.setInteger("timeReduction", getOverclockType().timeReduction);
        tag.setInteger("powerIncrease", getOverclockType().powerIncrease);
        tag.setInteger("multiblockTier", multiBlockTier);
    }
    // endregion

    // region Buttons
    @Override
    public boolean supportsVoidProtection() {
        return true;
    }

    @Override
    public boolean supportsInputSeparation() {
        return true;
    }

    @Override
    public boolean supportsBatchMode() {
        return true;
    }

    @Override
    public boolean supportsSingleRecipeLocking() {
        return true;
    }
    // endregion

    // region Nei
    @Override
    public int getRecipeCatalystPriority() {
        return -1;
    }
    // endregion

    // region Hatches
    private boolean baseCheckHatch(IGregTechTileEntity tileEntity) {
        if (tileEntity == null) {
            return false;
        }
        IMetaTileEntity aMetaTileEntity = tileEntity.getMetaTileEntity();

        return aMetaTileEntity == null;
    }

    public final boolean addSteamInputBusToMachineList(IGregTechTileEntity tileEntity) {
        if (baseCheckHatch(tileEntity)) return false;

        if (!(tileEntity.getMetaTileEntity() instanceof MTEHatchSteamBusInput steamBusInput)) return false;

        mInputBusses.add(steamBusInput);

        return mSteamInputBusses.add(steamBusInput);
    }

    public final boolean addSteamInputHatchToMachineList(IGregTechTileEntity tileEntity) {
        if (baseCheckHatch(tileEntity)) return false;

        if (!(tileEntity.getMetaTileEntity() instanceof MTEHatchCustomFluidBase steamHatchInput)) return false;

        return mSteamInputFluids.add(steamHatchInput);
    }

    public final boolean addSteamOutputBusToMachineList(IGregTechTileEntity tileEntity) {
        if (baseCheckHatch(tileEntity)) return false;

        if (!(tileEntity.getMetaTileEntity() instanceof MTEHatchSteamBusOutput steamBusOutput)) return false;

        mOutputBusses.add(steamBusOutput);

        return mSteamOutputBusses.add(steamBusOutput);
    }

    public final boolean addSensorHatchToMachineList(IGregTechTileEntity tileEntity) {
        if (baseCheckHatch(tileEntity)) return false;

        if (!(tileEntity.getMetaTileEntity() instanceof GTN_SensorHatch sensorHatch)) return false;

        return mSensorHatch.add(sensorHatch);
    }

    public final boolean addDynamoMultiHatchToMachineList(IGregTechTileEntity tileEntity) {
        if (baseCheckHatch(tileEntity)) return false;

        if (!(tileEntity.getMetaTileEntity() instanceof MTEHatchDynamoMulti dynamoMulti)) return false;

        return mDynamoMultiHatches.add(dynamoMulti);
    }

    public final boolean addDataInputHatchToMachineList(IGregTechTileEntity tileEntity) {
        if (baseCheckHatch(tileEntity)) return false;

        if (!(tileEntity.getMetaTileEntity() instanceof GTN_DataInput dataInput)) return false;

        return mDataInputHatches.add(dataInput);
    }

    public final boolean addDataOutputHatchToMachineList(IGregTechTileEntity tileEntity) {
        if (baseCheckHatch(tileEntity)) return false;

        if (!(tileEntity.getMetaTileEntity() instanceof GTN_DataOutput dataOutput)) return false;

        return mDataOutputHatches.add(dataOutput);
    }
    // endregion

    // region Energy
    public boolean addEnergyOutput(long aEU) {
        if (aEU <= 0) {
            return true;
        }
        if (!mDynamoHatches.isEmpty() || !mDynamoMultiHatches.isEmpty()) {
            return addEnergyOutputMultipleDynamos(aEU, true);
        }
        return false;
    }

    @Override
    public boolean addEnergyOutputMultipleDynamos(long aEU, boolean aAllowMixedVoltageDynamos) {
        long injected = 0;
        long totalOutput = 0;
        long aFirstVoltageFound = -1;
        boolean aFoundMixedDynamos = false;

        List<MTEHatch> allDynamos = new ArrayList<>();

        for (MTEHatchDynamoMulti hatch : validMTEList(mDynamoMultiHatches)) {
            allDynamos.add(hatch);
        }

        for (MTEHatchDynamo hatch : validMTEList(mDynamoHatches)) {
            allDynamos.add(hatch);
        }

        for (MTEHatch aDynamo : allDynamos) {
            long aVoltage = aDynamo.maxEUOutput();
            long aTotal = aDynamo.maxAmperesOut() * aVoltage;

            if (aFirstVoltageFound == -1) {
                aFirstVoltageFound = aVoltage;
            } else if (aFirstVoltageFound != aVoltage) {
                aFoundMixedDynamos = true;
            }

            totalOutput += aTotal;
        }

        if (totalOutput < aEU || (aFoundMixedDynamos && !aAllowMixedVoltageDynamos)) {
            explodeMultiblock();
            return false;
        }

        for (MTEHatch aDynamo : allDynamos) {
            if (injected >= aEU) break;

            IGregTechTileEntity base = aDynamo.getBaseMetaTileEntity();
            if (base == null) continue;

            long leftToInject = aEU - injected;
            long aVoltage = aDynamo.maxEUOutput();

            long aAmpsToInject = leftToInject / aVoltage;
            long aRemainder = leftToInject - (aAmpsToInject * aVoltage);

            long ampsOnCurrentHatch = Math.min(aDynamo.maxAmperesOut(), aAmpsToInject);

            for (int i = 0; i < ampsOnCurrentHatch; i++) {
                base.increaseStoredEnergyUnits(aVoltage, false);
            }

            injected += aVoltage * ampsOnCurrentHatch;

            if (aRemainder > 0 && ampsOnCurrentHatch < aDynamo.maxAmperesOut()) {
                base.increaseStoredEnergyUnits(aRemainder, false);
                injected += aRemainder;
            }
        }

        return injected > 0;
    }

    protected void setEnergyUsageWithoutLoss(long lEUt) {
        this.lEUt = (long) (-lEUt * 0.95);
    }

    protected void setEnergyGenerate(long lEUt) {
        this.lEUt = lEUt;
    }

    public long getAllDynamoBuffer() {
        long buffer = 0;
        for (MTEHatch tHatch : validMTEList(mDynamoHatches)) {
            buffer += tHatch.getEUVar();
        }
        return buffer;
    }

    public long getAllMaxDynamoBuffer() {
        long buffer = 0;
        for (MTEHatch tHatch : validMTEList(mDynamoHatches)) {
            buffer += tHatch.maxEUStore();
        }
        return buffer;
    }

    public long getDynamoAmperage() {
        long dynamoAmperage = 0;
        for (MTEHatch tHatch : validMTEList(mDynamoHatches)) {
            assert tHatch.getBaseMetaTileEntity() != null;
            dynamoAmperage += tHatch.getBaseMetaTileEntity()
                .getOutputAmperage();
        }
        return dynamoAmperage;
    }

    public boolean checkMixedDynamo() {
        long firstVoltage = -1;
        for (MTEHatchDynamo tHatch : validMTEList(mDynamoHatches)) {
            long aVoltage = tHatch.maxEUOutput();
            if (firstVoltage == -1) {
                firstVoltage = aVoltage;
            } else {
                if (firstVoltage != aVoltage) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean checkCountDynamo(int countAvaliableDynamo) {
        int count = 0;
        for (MTEHatchDynamo tHatch : validMTEList(mDynamoHatches)) {
            count++;
            if (count > countAvaliableDynamo) return false;
        }
        return true;
    }

    public int getTierDynamo() {
        if (!checkMixedDynamo()) {
            return mDynamoHatches.stream()
                .mapToInt(MTEHatchDynamo::getTierForStructure)
                .distinct()
                .reduce((a, b) -> 0)
                .orElse(0);
        }
        return 0;
    }

    public boolean setDynamoTier(int tier, boolean onlyThisTier) {
        if (onlyThisTier) {
            return mDynamoHatches.stream()
                .allMatch(dynamo -> dynamo.getTierForStructure() == tier);
        }
        return mDynamoHatches.stream()
            .allMatch(dynamo -> dynamo.getTierForStructure() <= tier);
    }
    // endregion

    // region Void Helper
    protected boolean isItemOutputFull(ItemStack[] itemOutputs) {
        VoidProtectionHelper voidProtectionHelper = new VoidProtectionHelper();

        voidProtectionHelper.setMachine(this)
            .setItemOutputs(itemOutputs)
            .build();

        return voidProtectionHelper.isItemFull();
    }
    // endregion

    // region Sync Data
    @Override
    public NBTTagCompound getDescriptionData() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("multiBlockTier", multiBlockTier);
        tag.setInteger("mainCasingTextureId", mainCasingTextureId);
        return tag;
    }

    @Override
    public void onDescriptionPacket(NBTTagCompound data) {
        super.onDescriptionPacket(data);
        multiBlockTier = data.getInteger("multiBlockTier");
        mainCasingTextureId = data.getInteger("mainCasingTextureId");
    }
    // endregion

    // region Ticks
    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTimer) {
        super.onPostTick(aBaseMetaTileEntity, aTimer);
        if (aTimer % 100 == 5) {
            for (CoordMultiBlock coord : multiBlocks.keySet()) {
                tryLink(coord);
            }
        }
    }
    // endregion

    // region Block
    @Override
    public void onBlockDestroyed() {
        super.onBlockDestroyed();
        multiBlocks.clear();
        registeredTierData.clear();
    }
    // endregion

    // region Other methods
    private void addMultiBlockBasicInfo(GTN_MultiBlockTooltipBuilder tt) {
        tt.addExtraInfo(GTN_Utils.tr("GTN.TooltipBuilder.basic_info"));
        tt.addExtraInfoWithSpace(GTN_Utils.tr("multiblock.waila.max_parallel", getTrueParallel()));
        tt.addExtraInfoWithSpace(GTN_Utils.tr("multiblock.waila.eu_modifier", Math.round(getEuModifier() * 100)));
        tt.addExtraInfoWithSpace(
            GTN_Utils.tr("multiblock.waila.speed_bonus", (int) Math.round(100.0 / (1F / getSpeedBonus()))));
        tt.addExtraInfoWithSpace(
            GTN_Utils
                .tr("multiblock.waila.overclock", getOverclockType().timeReduction, getOverclockType().powerIncrease));
    }

    private void addMultiBlockAreaInfo(GTN_MultiBlockTooltipBuilder tt) {
        List<StructureVariant<T>> variants = getStructureVariants();

        if (variants.isEmpty()) {
            return;
        }

        if (variants.size() == 1) {
            StructureVariant<T> variant = variants.get(0);
            MultiblockArea area = variant.multiblockArea;
            tt.addMultiBlockAreaInfo(area.width, area.height, area.length);
            return;
        }

        for (StructureVariant<T> variant : variants) {
            MultiblockArea area = variant.multiblockArea;
            tt.addMultiBlockAreaInfoWithName(variant.piece, area.width, area.height, area.length);
        }
    }

    public void setMainCasingCount(int mainCasingCount) {
        this.mainCasingCount = mainCasingCount;
    }

    public int getMainCasingCount() {
        return mainCasingCount;
    }

    protected void updateCasingTextureFromTierData() {
        for (TierData casing : registeredTierData) {
            if (casing.getCasingTier() > 0 && casing.getIsMainCasing()) {
                setMainCasingTextureId(casing.getCasingTextureId());
            }
        }
    }

    protected TierData createTierData(String channelName, boolean isMainCasing) {
        TierData data = new TierData();
        data.setChannelName(channelName);
        data.setIsMainCasing(isMainCasing);
        registeredTierData.add(data);
        return data;
    }

    protected TierData createTierData(String channelName) {
        return createTierData(channelName, false);
    }

    public OverclockType getOverclockType() {
        return OverclockType.NormalOverclock;
    }

    public void setMainCasingTextureId(int mainCasingTextureId) {
        this.mainCasingTextureId = mainCasingTextureId;
    }

    private void initDefaultVariant() {
        List<StructureVariant<T>> variants = getStructureVariants();
        if (!variants.isEmpty()) {
            mainCasing = variants.get(0).casing;
        }
    }

    protected IStructureDefinition<T> buildStructureDefinition(
        Consumer<StructureDefinition.Builder<T>> elementBuilder) {
        StructureDefinition.Builder<T> builder = StructureDefinition.builder();

        List<StructureVariant<T>> variants = getStructureVariants();

        for (StructureVariant<T> variant : variants) {
            builder.addShape(variant.piece, transpose(variant.shape));
        }

        elementBuilder.accept(builder);

        return builder.build();
    }

    @SuppressWarnings("unchecked")
    protected final T self() {
        return (T) this;
    }

    public void setMainCasing(GTN_Casings mainCasing) {
        this.mainCasing = mainCasing;
    }

    public GTN_Casings getMainCasing() {
        return mainCasing;
    }

    public void setMultiBlockTier(int globalMultiBlockTier) {
        this.multiBlockTier = globalMultiBlockTier;
    }

    public int getMultiBlockTier() {
        return multiBlockTier;
    }

    public boolean checkPieceProxy(String piece, int h, int v, int d) {
        return checkPiece(piece, h, v, d);
    }

    protected int getEfficiency() {
        return getCurrentEfficiency(this.getStackForm(1));
    }

    protected String tr(String key) {
        return GTN_Utils.tr(this.MULTIBLOCK_NAME_KEY + "." + key);
    }

    protected String tr(String key, Object... formatted) {
        return GTN_Utils.tr(this.MULTIBLOCK_NAME_KEY + "." + key, formatted);
    }

    public void setDurationInTicks(int ticks) {
        mMaxProgresstime = ticks;
    }

    public void setDurationInSeconds(int seconds) {
        setDurationInTicks(seconds * 20);
    }

    public void setDurationInMinutes(int minutes) {
        setDurationInSeconds(minutes * 60);
    }

    public void setDurationInHours(int hours) {
        setDurationInMinutes(hours * 60);
    }

    public void setDurationInDays(int days) {
        setDurationInHours(days * 24);
    }

    public void repairMachine() {
        mHardHammer = true;
        mSoftMallet = true;
        mScrewdriver = true;
        mCrowbar = true;
        mSolderingTool = true;
        mWrench = true;
    }

    public boolean isNoMaintenanceIssue() {
        return false;
    }

    public boolean isEnergyMultiBlock() {
        return true;
    }

    public void updateHatchTexture() {
        int textureId = mainCasing.textureId;

        if (mainCasingTextureId != 0) {
            textureId = mainCasingTextureId;
        }

        for (IDualInputHatch h : mDualInputHatches) h.updateTexture(textureId);
        for (MTEHatch h : mInputBusses) h.updateTexture(textureId);
        for (MTEHatch h : mMaintenanceHatches) h.updateTexture(textureId);
        for (MTEHatch h : mEnergyHatches) h.updateTexture(textureId);
        for (MTEHatch h : mOutputBusses) h.updateTexture(textureId);
        for (MTEHatch h : mInputHatches) h.updateTexture(textureId);
        for (MTEHatch h : mOutputHatches) h.updateTexture(textureId);
        for (MTEHatch h : mMufflerHatches) h.updateTexture(textureId);
        for (MTEHatch h : mExoticEnergyHatches) h.updateTexture(textureId);
        for (MTEHatch h : mSensorHatch) h.updateTexture(textureId);
        for (MTEHatch h : mDynamoHatches) h.updateTexture(textureId);
        for (MTEHatch h : mDynamoMultiHatches) h.updateTexture(textureId);
        for (MTEHatch h : mDataInputHatches) h.updateTexture(textureId);
        for (MTEHatch h : mDataOutputHatches) h.updateTexture(textureId);
    }

    public CoordMultiBlock getCoord() {
        IGregTechTileEntity gte = getBaseMetaTileEntity();

        if (gte == null) return null;

        return new CoordMultiBlock(
            gte.getWorld().provider.dimensionId,
            gte.getXCoord(),
            gte.getYCoord(),
            gte.getZCoord());
    }

    public boolean tryLink(CoordMultiBlock coord) {
        if (getCoord().equals(coord)) return false;

        IGregTechTileEntity gte = multiBlocks.get(coord);

        if (gte == null) {
            IGregTechTileEntity newGte = coord.getMTEMultiBlockBase();
            if (newGte != null) {
                multiBlocks.put(coord, newGte);
                return true;
            }
            return false;
        }

        IMetaTileEntity mte = gte.getMetaTileEntity();
        if (mte != null) return false;

        multiBlocks.remove(coord);
        IGregTechTileEntity newGte = coord.getMTEMultiBlockBase();
        if (newGte != null) {
            multiBlocks.put(coord, newGte);
            return true;
        }

        return false;
    }
    // endregion
}
