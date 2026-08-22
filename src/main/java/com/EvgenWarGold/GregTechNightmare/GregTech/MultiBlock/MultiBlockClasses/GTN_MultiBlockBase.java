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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.EvgenWarGold.GregTechNightmare.Api.Dimensions;
import com.EvgenWarGold.GregTechNightmare.GregTech.Api.MultiblockArea;
import com.EvgenWarGold.GregTechNightmare.GregTech.Hatch.GTN_AspectHatch;
import com.EvgenWarGold.GregTechNightmare.GregTech.Hatch.GTN_ManaHatch;
import com.EvgenWarGold.GregTechNightmare.GregTech.Hatch.GTN_MeAspectHatch;
import com.EvgenWarGold.GregTechNightmare.GregTech.Hatch.GTN_SensorHatch;
import com.EvgenWarGold.GregTechNightmare.GregTech.Recipe.RecipeResult.ResultInsufficientRangeTier;
import com.EvgenWarGold.GregTechNightmare.Utils.Authors;
import com.EvgenWarGold.GregTechNightmare.Utils.Constants;
import com.EvgenWarGold.GregTechNightmare.Utils.GTN_Utils;
import com.gtnewhorizon.structurelib.alignment.constructable.IConstructable;
import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;

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
import gregtech.api.structure.error.StructureError;
import gregtech.api.structure.error.StructureErrorRegistry;
import gregtech.api.util.MultiblockTooltipBuilder;
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
    public ArrayList<MTEHatchSteamBusInput> steamInputBusses = new ArrayList<>();
    public ArrayList<MTEHatchSteamBusOutput> steamOutputBusses = new ArrayList<>();
    public ArrayList<MTEHatchCustomFluidBase> steamOutputFluids = new ArrayList<>();
    public ArrayList<GTN_SensorHatch> sensorHatches = new ArrayList<>();
    public ArrayList<GTN_ManaHatch> manaHatches = new ArrayList<>();
    public ArrayList<GTN_AspectHatch> aspectHatches = new ArrayList<>();
    public ArrayList<GTN_MeAspectHatch> meAspectHatches = new ArrayList<>();
    public ArrayList<MTEHatchDynamoMulti> dynamoMultiHatches = new ArrayList<>();
    protected final GTN_HatchControl<?> hatchControl;
    // Processing
    private int maxParallel = 1;
    private float euModifier = 1;
    private float speedBonus = 1;
    protected final GTN_ProcessingHelper<?> processingHelper;
    // Global Variable
    public final Map<CoordMultiBlock, IGregTechTileEntity> multiBlocks = new HashMap<>();
    protected int multiBlockTier = 0;
    protected GTN_Casings mainCasing;
    protected int mainCasingCount = 0;
    protected int mainCasingTextureId = 0;
    protected final List<CasingData> registeredCasingData = new ArrayList<>();
    protected final MultiblockBlockCounter multiblockBlockCounter = new MultiblockBlockCounter();
    protected StructureVariant<T> neiVariant = null;
    protected boolean initialized = false;
    // endregion

    // region Class Construct
    public GTN_MultiBlockBase(int id, String name) {
        super(id, TRANSLATE_KEY + name, GTN_Utils.tr(TRANSLATE_KEY + name));
        MULTIBLOCK_NAME_KEY = TRANSLATE_KEY + name;
        processingHelper = new GTN_ProcessingHelper<>(self());
        hatchControl = new GTN_HatchControl<>(self());
        initDefaultVariant();
    }

    public GTN_MultiBlockBase(String name) {
        super(name);
        MULTIBLOCK_NAME_KEY = TRANSLATE_KEY + name;
        processingHelper = new GTN_ProcessingHelper<>(self());
        hatchControl = new GTN_HatchControl<>(self());
        initDefaultVariant();
    }
    // endregion

    // region Create Meta
    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity gte) {
        return createNewMetaEntity();
    }
    // endregion

    // region Construct MultiBlock
    @Override
    public void clearHatches() {
        super.clearHatches();
        hatchControl.clearHatches();
        mainCasingCount = 0;

        for (CasingData casingData : registeredCasingData) {
            casingData.reset();
        }
    }

    protected boolean GTN_checkMachine(IGregTechTileEntity gte, ItemStack stack) {
        return true;
    }

    @Override
    public void checkMachine(IGregTechTileEntity gte, ItemStack stack, List<StructureError> errors) {
        List<StructureVariant<T>> variants = getStructureVariants();
        boolean built = false;

        if (neiVariant == null) {
            for (StructureVariant<T> variant : variants) {
                if (variant.check(self())) {
                    built = true;
                    break;
                }
            }
        } else if (neiVariant.check(self())) {
            built = true;
            neiVariant = null;
        }

        if (!built || !GTN_checkMachine(gte, stack)) {
            if (errors.isEmpty()) {
                errors.add(StructureErrorRegistry.UNKNOWN_STRUCTURE_ERROR);
            }
        }

        if (isNoMaintenanceIssue()) {
            repairMachine();
        }

        updateCasingTextureFromTierData();
        updateHatchTexture();

        if (getBaseMetaTileEntity() != null && mainCasingTextureId > 0) {
            getBaseMetaTileEntity().issueTileUpdate();
        }
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
        neiVariant = variant;

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

    protected IStructureDefinition<T> buildStructureDefinition(Consumer<GTN_StructureBuilder<T>> elementBuilder) {
        GTN_StructureBuilder<T> builder = new GTN_StructureBuilder<>(this);

        List<StructureVariant<T>> variants = getStructureVariants();

        for (StructureVariant<T> variant : variants) {
            builder.addShape(variant.piece, transpose(variant.shape));
        }

        elementBuilder.accept(builder);

        return builder.build();
    }

    public boolean checkPieceProxy(String piece, int h, int v, int d, @Nullable List<StructureError> errors) {
        return checkPiece(piece, h, v, d, errors);
    }

    private void initDefaultVariant() {
        List<StructureVariant<T>> variants = getStructureVariants();
        if (!variants.isEmpty()) {
            mainCasing = variants.getFirst().casing;
        }
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
    public ITexture[] getTexture(IGregTechTileEntity gte, ForgeDirection side, ForgeDirection facing, int colorIndex,
        boolean aActive, boolean aRedstone) {

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

    public void updateHatchTexture() {
        int textureId = mainCasing.textureId;

        if (mainCasingTextureId != 0) {
            textureId = mainCasingTextureId;
        }

        hatchControl.updateHatches(textureId);
    }

    protected void updateCasingTextureFromTierData() {
        for (CasingData casing : registeredCasingData) {
            if (casing.getCasingTier() > 0 && casing.getIsMainCasing()) {
                setMainCasingTextureId(casing.getCasingTextureId());
            }
        }
    }

    public void setMainCasingTextureId(int mainCasingTextureId) {
        this.mainCasingTextureId = mainCasingTextureId;
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

    public OverclockType getOverclockType() {
        return OverclockType.NormalOverclock;
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

    protected int getEfficiency() {
        return getCurrentEfficiency(this.getStackForm(1));
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
    // endregion

    // region NBT
    @Override
    public void saveNBTData(NBTTagCompound nbt) {
        super.saveNBTData(nbt);
        nbt.setInteger("maxParallel", getMaxParallelRecipes());
        nbt.setFloat("euModifier", getEuModifier());
        nbt.setFloat("speedBonus", 1F / getSpeedBonus());
        nbt.setInteger("multiblockTier", multiBlockTier);
        nbt.setInteger("mainCasingTextureId", mainCasingTextureId);

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

        nbt.setTag("multiBlocks", multiBlockList);
    }

    @Override
    public void loadNBTData(NBTTagCompound nbt) {
        super.loadNBTData(nbt);
        maxParallel = Math.max(nbt.getInteger("maxParallel"), 1);
        euModifier = nbt.getFloat("euModifier");
        if (euModifier <= 0) euModifier = 1;
        speedBonus = nbt.getFloat("speedBonus");
        if (speedBonus <= 0) speedBonus = 1;
        multiBlockTier = nbt.getInteger("multiblockTier");
        mainCasingTextureId = nbt.getInteger("mainCasingTextureId");

        multiBlocks.clear();
        NBTTagList multiBlockList = nbt.getTagList("multiBlocks", 10);

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
        for (Map.Entry<String, Integer> entry : multiblockBlockCounter.getBlockCounts(this)
            .entrySet()) {
            String blockName = entry.getKey();
            Integer count = entry.getValue();
            tt.addExtraInfoWithSpace(
                EnumChatFormatting.GOLD + count.toString() + "x " + EnumChatFormatting.AQUA + blockName);
        }
        createGtnTooltip(tt);
        tt.toolTipFinisher(Constants.MOD_NAME);
        return tt;
    }

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
            StructureVariant<T> variant = variants.getFirst();
            MultiblockArea area = variant.multiblockArea;
            tt.addMultiBlockAreaInfo(area.width, area.height, area.length);
            return;
        }

        for (StructureVariant<T> variant : variants) {
            MultiblockArea area = variant.multiblockArea;
            tt.addMultiBlockAreaInfoWithName(variant.piece, area.width, area.height, area.length);
        }
    }
    // endregion

    // region Waila
    @Override
    public void getWailaBody(ItemStack itemStack, List<String> info, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        final NBTTagCompound tag = accessor.getNBTData();

        int trueParallel = tag.getInteger("trueParallel");
        float euModifier = tag.getFloat("euModifier");
        float speedBonus = tag.getFloat("speedBonus");
        int timeReduction = tag.getInteger("timeReduction");
        int powerIncrease = tag.getInteger("powerIncrease");
        int multiblockTier = tag.getInteger("multiblockTier");

        if (tag.getBoolean("incompleteStructure")) {
            info.add(RED + translateToLocalFormatted("GT5U.waila.multiblock.status.incomplete") + RESET);
        } else {
            if (isEnergyMultiBlock()) {
                if (trueParallel > 0) {
                    info.add(GTN_Utils.tr("multiblock.waila.max_parallel", trueParallel));
                }

                if (euModifier > 0) {
                    info.add(GTN_Utils.tr("multiblock.waila.eu_modifier", Math.round(euModifier * 100)));
                }

                if (speedBonus > 0) {
                    info.add(GTN_Utils.tr("multiblock.waila.speed_bonus", (int) Math.round(100.0 / speedBonus)));
                }

                if (getOverclockType() != null) {
                    info.add(GTN_Utils.tr("multiblock.waila.overclock", timeReduction, powerIncrease));
                }

                if (multiblockTier > 0 && getStructureVariants().size() > 1) {
                    info.add(GTN_Utils.tr("multiblock.waila.tier", multiblockTier));
                }
            }

            super.getWailaBody(itemStack, info, accessor, config);

            GTN_WailaBody(itemStack, info, tag);
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

        GTN_WailaNBT(tile, new Dimensions(world, x, y, z), tag);
    }

    public void GTN_WailaBody(ItemStack itemStack, List<String> info, NBTTagCompound tag) {}

    public void GTN_WailaNBT(TileEntity tile, Dimensions dimensions, NBTTagCompound tag) {}
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

    // region Energy
    @Override
    public boolean addEnergyOutput(long eu) {
        if (eu <= 0L) {
            return true;
        }

        if (!dynamoMultiHatches.isEmpty() || !mDynamoHatches.isEmpty() || !mExoticDynamoHatches.isEmpty()) {
            addEnergyOutputMultipleDynamos(eu, true);
        }
        return false;
    }

    @Override
    public boolean addEnergyOutputMultipleDynamos(long eu, boolean aAllowMixedVoltageDynamos) {
        long injected = 0;
        long totalOutput = 0;
        long firstVoltageFound = -1;
        boolean foundMixedDynamos = false;

        List<MTEHatch> allDynamos = new ArrayList<>();

        for (MTEHatchDynamoMulti hatch : validMTEList(dynamoMultiHatches)) {
            allDynamos.add(hatch);
        }

        for (MTEHatchDynamo hatch : validMTEList(mDynamoHatches)) {
            allDynamos.add(hatch);
        }

        for (MTEHatch hatch : validMTEList(mExoticDynamoHatches)) {
            allDynamos.add(hatch);
        }

        for (MTEHatch dynamo : allDynamos) {
            long aVoltage = dynamo.maxEUOutput();
            long aTotal = dynamo.maxAmperesOut() * aVoltage;

            if (firstVoltageFound == -1) {
                firstVoltageFound = aVoltage;
            } else if (firstVoltageFound != aVoltage) {
                foundMixedDynamos = true;
            }

            totalOutput += aTotal;
        }

        if (totalOutput < eu || (foundMixedDynamos && !aAllowMixedVoltageDynamos)) {
            explodeMultiblock();
            return false;
        }

        for (MTEHatch dynamo : allDynamos) {
            if (injected >= eu) break;

            IGregTechTileEntity base = dynamo.getBaseMetaTileEntity();
            if (base == null) continue;

            long leftToInject = eu - injected;
            long voltage = dynamo.maxEUOutput();

            long ampsToInject = leftToInject / voltage;
            long remainder = leftToInject - (ampsToInject * voltage);

            long ampsOnCurrentHatch = Math.min(dynamo.maxAmperesOut(), ampsToInject);

            for (int i = 0; i < ampsOnCurrentHatch; i++) {
                base.increaseStoredEnergyUnits(voltage, false);
            }

            injected += voltage * ampsOnCurrentHatch;

            if (remainder > 0 && ampsOnCurrentHatch < dynamo.maxAmperesOut()) {
                base.increaseStoredEnergyUnits(remainder, false);
                injected += remainder;
            }
        }

        return injected > 0;
    }

    public long getAllDynamoBuffer() {
        long buffer = 0;
        for (MTEHatch hatch : validMTEList(mDynamoHatches)) {
            buffer += hatch.getEUVar();
        }
        return buffer;
    }

    public long getAllMaxDynamoBuffer() {
        long buffer = 0;
        for (MTEHatch hatch : validMTEList(mDynamoHatches)) {
            buffer += hatch.maxEUStore();
        }
        return buffer;
    }

    public long getDynamoAmperage() {
        long dynamoAmperage = 0;
        for (MTEHatch hatch : validMTEList(mDynamoHatches)) {
            IGregTechTileEntity mte = hatch.getBaseMetaTileEntity();

            if (mte == null) {
                return 0;
            }

            dynamoAmperage += mte.getOutputAmperage();
        }
        return dynamoAmperage;
    }

    public boolean checkMixedDynamo() {
        long firstVoltage = -1;
        for (MTEHatchDynamo hatch : validMTEList(mDynamoHatches)) {
            long aVoltage = hatch.maxEUOutput();
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
        for (MTEHatchDynamo hatch : validMTEList(mDynamoHatches)) {
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
    public void onPostTick(IGregTechTileEntity gte, long timer) {
        super.onPostTick(gte, timer);
        if (timer % 100 == 5) {
            validateLinks();
        }
    }

    @Override
    public void onFirstTick(IGregTechTileEntity gte) {
        super.onFirstTick(gte);

        GTN_FirstTick(gte);

        if (!initialized) {
            initialize();
            initialized = true;
        }
    }

    protected void initialize() {}

    protected void GTN_FirstTick(IGregTechTileEntity gte) {}
    // endregion

    // region Block
    @Override
    public void onBlockDestroyed() {
        super.onBlockDestroyed();
        multiBlocks.clear();
        registeredCasingData.clear();
    }
    // endregion

    // region Link
    public CoordMultiBlock getCoord() {
        IGregTechTileEntity gte = getBaseMetaTileEntity();

        if (gte == null) return null;

        return new CoordMultiBlock(
            gte.getWorld().provider.dimensionId,
            gte.getXCoord(),
            gte.getYCoord(),
            gte.getZCoord());
    }

    private void linkTo(CoordMultiBlock coord, IGregTechTileEntity targetTile) {
        IMetaTileEntity meta = targetTile.getMetaTileEntity();

        if (!(meta instanceof GTN_MultiBlockBase<?>target)) {
            return;
        }

        if (linkUseSameType()) {
            removeExistingLinkOfSameType(meta.getClass(), coord);
        }

        if (linkUseP2P()) {

            Iterator<Map.Entry<CoordMultiBlock, IGregTechTileEntity>> it = target.multiBlocks.entrySet()
                .iterator();

            while (it.hasNext()) {

                Map.Entry<CoordMultiBlock, IGregTechTileEntity> entry = it.next();

                IGregTechTileEntity oldTile = entry.getValue();

                if (oldTile == null) {
                    it.remove();
                    continue;
                }

                IMetaTileEntity oldMeta = oldTile.getMetaTileEntity();

                if (oldMeta instanceof GTN_MultiBlockBase<?>oldMachine) {
                    oldMachine.multiBlocks.remove(target.getCoord());
                }

                it.remove();
            }
        }

        multiBlocks.put(coord, targetTile);

        if (linkUseP2P()) {
            target.multiBlocks.put(getCoord(), getBaseMetaTileEntity());
        }
    }

    private void validateLinks() {
        Iterator<Map.Entry<CoordMultiBlock, IGregTechTileEntity>> it = multiBlocks.entrySet()
            .iterator();

        while (it.hasNext()) {

            Map.Entry<CoordMultiBlock, IGregTechTileEntity> entry = it.next();

            CoordMultiBlock coord = entry.getKey();

            IGregTechTileEntity gte = coord.getMTEMultiBlockBase();

            if (gte == null) {

                if (linkUseP2P()) {
                    IGregTechTileEntity oldGte = entry.getValue();

                    if (oldGte != null && oldGte.getMetaTileEntity() instanceof GTN_MultiBlockBase<?>other) {
                        other.multiBlocks.remove(getCoord());
                    }
                }

                it.remove();
                continue;
            }

            IMetaTileEntity mte = gte.getMetaTileEntity();

            if (!(mte instanceof GTN_MultiBlockBase<?>otherMachine)) {

                if (linkUseP2P()) {
                    IGregTechTileEntity oldGte = entry.getValue();

                    if (oldGte != null && oldGte.getMetaTileEntity() instanceof GTN_MultiBlockBase<?>other) {
                        other.multiBlocks.remove(getCoord());
                    }
                }

                it.remove();
                continue;
            }

            if (entry.getValue() == null) {

                entry.setValue(gte);

                if (linkUseP2P()) {
                    otherMachine.multiBlocks.put(getCoord(), getBaseMetaTileEntity());
                }

                continue;
            }

            if (entry.getValue() != gte) {

                if (linkUseP2P()) {
                    IGregTechTileEntity oldGte = entry.getValue();

                    if (oldGte != null && oldGte.getMetaTileEntity() instanceof GTN_MultiBlockBase<?>other) {
                        other.multiBlocks.remove(getCoord());
                    }

                    otherMachine.multiBlocks.put(getCoord(), getBaseMetaTileEntity());
                }

                entry.setValue(gte);
            }
        }
    }

    private void removeExistingLinkOfSameType(Class<?> mteClass, CoordMultiBlock exceptCoord) {
        Iterator<Map.Entry<CoordMultiBlock, IGregTechTileEntity>> iterator = multiBlocks.entrySet()
            .iterator();

        while (iterator.hasNext()) {

            Map.Entry<CoordMultiBlock, IGregTechTileEntity> entry = iterator.next();

            if (entry.getKey()
                .equals(exceptCoord)) {
                continue;
            }

            IGregTechTileEntity gte = entry.getValue();

            if (gte == null) {
                iterator.remove();
                continue;
            }

            IMetaTileEntity mte = gte.getMetaTileEntity();

            if (!mteClass.isInstance(mte)) {
                continue;
            }

            if (linkUseP2P() && mte instanceof GTN_MultiBlockBase<?>otherMachine) {

                otherMachine.multiBlocks.remove(getCoord());
            }

            iterator.remove();
        }
    }

    public boolean tryLink(CoordMultiBlock coord) {
        if (coord == null) return false;

        if (coord.equals(getCoord())) return false;

        IGregTechTileEntity gte = coord.getMTEMultiBlockBase();

        if (gte == null) return false;

        IMetaTileEntity mte = gte.getMetaTileEntity();

        if (mte == null) return false;

        if (!linkClassAllowed(mte.getClass())) return false;

        linkTo(coord, gte);

        return true;
    }

    public boolean linkClassAllowed(Class<?> clazz) {
        return true;
    }

    public boolean linkUseSameType() {
        return false;
    }

    public boolean linkUseP2P() {
        return false;
    }
    // endregion

    // region Scanner Info
    @Override
    public void getExtraInfoData(List<String> info) {
        super.getExtraInfoData(info);

        List<String> list = new ArrayList<>();

        for (CoordMultiBlock coord : multiBlocks.keySet()) {
            IGregTechTileEntity gte = multiBlocks.get(coord);
            list.add(
                EnumChatFormatting.GOLD + "     "
                    + "Module Name: "
                    + EnumChatFormatting.GREEN
                    + gte.getMetaTileEntity()
                        .getLocalName()
                    + EnumChatFormatting.GOLD
                    + " Dim: "
                    + EnumChatFormatting.GREEN
                    + coord.dim
                    + EnumChatFormatting.GOLD
                    + " X: "
                    + EnumChatFormatting.GREEN
                    + coord.x
                    + EnumChatFormatting.GOLD
                    + " Y: "
                    + EnumChatFormatting.GREEN
                    + coord.y
                    + EnumChatFormatting.GOLD
                    + " Z: "
                    + EnumChatFormatting.GREEN
                    + coord.z);
        }

        if (!list.isEmpty()) {
            info.add(EnumChatFormatting.RED + "Active Modules");
            info.addAll(list);
        }
    }
    // endregion

    // region Translate
    protected String tr(String key) {
        return GTN_Utils.tr(this.MULTIBLOCK_NAME_KEY + "." + key);
    }

    protected String tr(String key, Object... formatted) {
        return GTN_Utils.tr(this.MULTIBLOCK_NAME_KEY + "." + key, formatted);
    }
    // endregion

    // region CasingData
    protected CasingData createCasingData(String channelName, boolean isMainCasing) {
        CasingData data = new CasingData();
        data.setChannelName(channelName);
        data.setIsMainCasing(isMainCasing);
        registeredCasingData.add(data);
        return data;
    }

    protected CasingData createCasingData(String channelName) {
        return createCasingData(channelName, false);
    }

    public void setMainCasing(GTN_Casings mainCasing) {
        this.mainCasing = mainCasing;
    }
    // endregion

    // region Other methods
    @SuppressWarnings("unchecked")
    protected final T self() {
        return (T) this;
    }

    public void setMultiBlockTier(int globalMultiBlockTier) {
        this.multiBlockTier = globalMultiBlockTier;
    }
    // endregion
}
