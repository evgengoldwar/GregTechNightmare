package com.EvgenWarGold.GregTechNightmare.GregTech.Hatch;

import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_ME_CRAFTING_INPUT_BUFFER;
import static gregtech.api.metatileentity.BaseTileEntity.TOOLTIP_DELAY;

import java.util.Collections;
import java.util.List;

import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;

import org.jetbrains.annotations.NotNull;

import com.EvgenWarGold.GregTechNightmare.GregTech.Gui.GTN_WildcardPatternBufferGui;
import com.EvgenWarGold.GregTechNightmare.GregTech.Wildcard.WildcardBlacklistMode;
import com.EvgenWarGold.GregTechNightmare.GregTech.Wildcard.WildcardPatternBlacklist;
import com.EvgenWarGold.GregTechNightmare.GregTech.Wildcard.WildcardPatternExpander;
import com.EvgenWarGold.GregTechNightmare.GregTech.Wildcard.WildcardPatternExpansionCache;
import com.EvgenWarGold.GregTechNightmare.Utils.Constants;
import com.EvgenWarGold.GregTechNightmare.Utils.GTN_Utils;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.item.ItemStackHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.widget.ButtonWidget;
import com.gtnewhorizons.modularui.common.widget.CycleButtonWidget;
import com.gtnewhorizons.modularui.common.widget.FakeSyncWidget;
import com.gtnewhorizons.modularui.common.widget.SlotGroup;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.items.misc.ItemEncodedPattern;
import gregtech.api.enums.SoundResource;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.render.TextureFactory;
import gregtech.common.tileentities.machines.MTEHatchCraftingInputME;

@IMetaTileEntity.SkipGenerateDescription
public class GTN_WildcardPatternBuffer extends MTEHatchCraftingInputME {

    public static final int PHYSICAL_PATTERN_SLOTS = 36;
    public static final int PRIMARY_PATTERN_SLOT = 0;
    public static final int CIRCUIT_SLOT = PHYSICAL_PATTERN_SLOTS;
    public static final int SHARED_INPUT_START = CIRCUIT_SLOT + 1;
    public static final int SHARED_INPUT_END = SHARED_INPUT_START + 8;
    public static final int BLACKLIST_COLUMNS = 9;
    public static final int BLACKLIST_ROWS = 6;
    public static final int BLACKLIST_PAGE_SIZE = BLACKLIST_COLUMNS * BLACKLIST_ROWS;
    public static final int BLACKLIST_PAGE_COUNT = 4;
    public static final int BLACKLIST_SLOTS = BLACKLIST_PAGE_SIZE * BLACKLIST_PAGE_COUNT;

    private static final String NBT_BLACKLIST = "gtnWildcardBlacklist";
    private static final String NBT_BLACKLIST_MODE = "gtnWildcardBlacklistMode";

    private final ItemStackHandler blacklistInventory = new ItemStackHandler(BLACKLIST_SLOTS) {

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            if (!suppressBlacklistUpdates) {
                onBlacklistChanged();
            }
        }
    };
    private final WildcardPatternExpansionCache expansionCache = new WildcardPatternExpansionCache();
    private WildcardBlacklistMode blacklistMode = WildcardBlacklistMode.OUTPUT;
    private boolean suppressBlacklistUpdates;
    private String primaryPatternFingerprint;

    public GTN_WildcardPatternBuffer(int aID, String aName) {

        super(aID, aName, aName, true);
    }

    public GTN_WildcardPatternBuffer(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, aTier, aDescription, aTextures, true);
    }

    @Override
    public GTN_WildcardPatternBuffer newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new GTN_WildcardPatternBuffer(mName, mTier, mDescriptionArray, mTextures);
    }

    @Override
    public int rows() {
        return 1;
    }

    @Override
    public int rowSize() {
        return 1;
    }

    @Override
    public int numSlots() {
        return 1;
    }

    @Override
    public void setInventorySlotContents(int aIndex, ItemStack aStack) {
        if (aIndex > PRIMARY_PATTERN_SLOT && aIndex < PHYSICAL_PATTERN_SLOTS) {
            return;
        }

        super.setInventorySlotContents(aIndex, aStack);
        if (aIndex == PRIMARY_PATTERN_SLOT) invalidatePatternIfChanged(aStack);
    }

    @Override
    public void onPatternChange(int index, ItemStack stack) {
        super.onPatternChange(index, stack);
        if (index == PRIMARY_PATTERN_SLOT) invalidatePatternIfChanged(stack);
    }

    public boolean isPrimaryPattern(ICraftingPatternDetails source) {
        if (source == null) {
            return false;
        }

        ItemStack installed = getStackInSlot(PRIMARY_PATTERN_SLOT);
        ItemStack advertised = source.getPattern();
        return installed != null && advertised != null
            && installed.isItemEqual(advertised)
            && ItemStack.areItemStackTagsEqual(installed, advertised);
    }

    public List<ICraftingPatternDetails> getExpandedPatterns(ICraftingPatternDetails source) {
        return expansionCache.getExpandedPatterns(source, blacklistMode, blacklistInventory);
    }

    public ItemStackHandler getBlacklistInventory() {
        return blacklistInventory;
    }

    public WildcardBlacklistMode getBlacklistMode() {
        return blacklistMode;
    }

    public void setBlacklistMode(WildcardBlacklistMode mode) {
        WildcardBlacklistMode newMode = mode == null ? WildcardBlacklistMode.OUTPUT : mode;
        if (blacklistMode == newMode) {
            return;
        }

        blacklistMode = newMode;
        onBlacklistChanged();
    }

    public void clearBlacklist() {
        boolean changed = false;
        suppressBlacklistUpdates = true;
        try {
            for (int slot = 0; slot < blacklistInventory.getSlots(); slot++) {
                if (blacklistInventory.getStackInSlot(slot) == null) {
                    continue;
                }

                blacklistInventory.setStackInSlot(slot, null);
                changed = true;
            }
        } finally {
            suppressBlacklistUpdates = false;
        }
        if (changed) onBlacklistChanged();
    }

    public WildcardPatternBlacklist createBlacklistSnapshot() {
        return expansionCache.getBlacklistSnapshot(blacklistMode, blacklistInventory);
    }

    @Override
    public void saveNBTData(NBTTagCompound nbt) {
        super.saveNBTData(nbt);
        nbt.setTag(NBT_BLACKLIST, blacklistInventory.serializeNBT());
        nbt.setString(NBT_BLACKLIST_MODE, blacklistMode.name());
    }

    @Override
    public void loadNBTData(NBTTagCompound nbt) {
        super.loadNBTData(nbt);
        blacklistMode = WildcardBlacklistMode.fromName(nbt.getString(NBT_BLACKLIST_MODE));

        if (nbt.hasKey(NBT_BLACKLIST, 10)) {
            suppressBlacklistUpdates = true;
            try {
                NBTTagCompound blacklistData = nbt.getCompoundTag(NBT_BLACKLIST);
                blacklistData.setInteger("Size", BLACKLIST_SLOTS);
                blacklistInventory.deserializeNBT(blacklistData);
                for (int slot = 0; slot < blacklistInventory.getSlots(); slot++) {
                    ItemStack stack = blacklistInventory.getStackInSlot(slot);
                    if (stack != null) stack.stackSize = 1;
                }
            } finally {
                suppressBlacklistUpdates = false;
            }
        }

        primaryPatternFingerprint = WildcardPatternExpander
            .fingerprintPatternStack(getStackInSlot(PRIMARY_PATTERN_SLOT));
        expansionCache.invalidateBlacklist();
    }

    private void invalidatePatternIfChanged(ItemStack pattern) {
        String fingerprint = WildcardPatternExpander.fingerprintPatternStack(pattern);
        if (fingerprint.equals(primaryPatternFingerprint)) {
            return;
        }

        primaryPatternFingerprint = fingerprint;
        if (expansionCache != null) expansionCache.invalidatePattern();
    }

    private void onBlacklistChanged() {
        expansionCache.invalidateBlacklist();

        IGregTechTileEntity base = getBaseMetaTileEntity();
        if (base == null || base.getWorld() == null || base.getWorld().isRemote) {
            return;
        }

        gridChanged();
        base.enableTicking();
        if (base instanceof TileEntity tileEntity) {
            tileEntity.markDirty();
        }
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings uiSettings) {
        return new GTN_WildcardPatternBufferGui(this).build(data, syncManager, uiSettings);
    }

    @Override
    public int getGUIWidth() {
        return 212;
    }

    @Override
    public int getGUIHeight() {
        return 184;
    }

    @Override
    public void addUIWidgets(ModularWindow.@NotNull Builder builder, UIBuildContext buildContext) {
        addDedicatedUI(builder, buildContext);
    }

    public void addDedicatedUI(ModularWindow.Builder builder, UIBuildContext buildContext) {
        builder

            .widget(
                SlotGroup.ofItemHandler(inventoryHandler, 1)
                    .startFromSlot(PRIMARY_PATTERN_SLOT)
                    .endAtSlot(PRIMARY_PATTERN_SLOT)
                    .phantom(false)
                    .background(getGUITextureSet().getItemSlot(), GTUITextures.OVERLAY_SLOT_PATTERN_ME)
                    .widgetCreator(slot -> new SlotWidget(slot) {

                        @Override
                        protected ItemStack getItemStackForRendering(Slot slotIn) {
                            ItemStack stack = slot.getStack();
                            if (stack == null || !(stack.getItem() instanceof ItemEncodedPattern itemEncodedPattern)) {
                                return stack;
                            }

                            ItemStack output = itemEncodedPattern.getOutput(stack);
                            return output != null ? output : stack;
                        }
                    }.setFilter(itemStack -> itemStack.getItem() instanceof ICraftingPatternItem)
                        .setChangeListener(() -> onPatternChange(slot.getSlotIndex(), slot.getStack())))
                    .build()
                    .setPos(8, 9))

            .widget(
                SlotGroup.ofItemHandler(inventoryHandler, 1)
                    .startFromSlot(CIRCUIT_SLOT)
                    .endAtSlot(CIRCUIT_SLOT)
                    .phantom(false)
                    .background(getGUITextureSet().getItemSlot())
                    .build()
                    .setPos(32, 9))

            .widget(
                SlotGroup.ofItemHandler(inventoryHandler, 3)
                    .startFromSlot(SHARED_INPUT_START)
                    .endAtSlot(SHARED_INPUT_END)
                    .phantom(false)
                    .background(getGUITextureSet().getItemSlot())
                    .build()
                    .setPos(8, 36))
            .widget(
                new ButtonWidget().setOnClick((clickData, _) -> { if (clickData.mouseButton == 0) refundAll(false); })
                    .setPlayClickSound(true)
                    .setBackground(GTUITextures.BUTTON_STANDARD, GTUITextures.OVERLAY_BUTTON_EXPORT)
                    .addTooltip(StatCollector.translateToLocal("GT5U.gui.tooltip.hatch.crafting_input_me.export"))
                    .setSize(16, 16)
                    .setPos(80, 9))
            .widget(
                new CycleButtonWidget()
                    .setToggle(() -> disablePatternOptimization, value -> disablePatternOptimization = value)
                    .setStaticTexture(GTUITextures.OVERLAY_BUTTON_PATTERN_OPTIMIZE)
                    .setVariableBackground(GTUITextures.BUTTON_STANDARD_TOGGLE)
                    .addTooltip(0, StatCollector.translateToLocal("GTN.Wildcard.patternOptimization.allowed"))
                    .addTooltip(1, StatCollector.translateToLocal("GTN.Wildcard.patternOptimization.disabled"))
                    .setPos(98, 9)
                    .setSize(16, 16))
            .widget(new ButtonWidget().setOnClick((clickData, widget) -> {
                int value = clickData.shift ? 1 : 0;
                if (clickData.mouseButton == 1) value |= 0b10;
                doublePatterns(value);
            })
                .setPlayClickSound(true)
                .setBackground(GTUITextures.BUTTON_STANDARD, GTUITextures.OVERLAY_BUTTON_X2)
                .addTooltip(StatCollector.translateToLocal("gui.tooltips.appliedenergistics2.DoublePatterns"))
                .setSize(16, 16)
                .setPos(116, 9))
            .widget(
                new ButtonWidget().setOnClick((_, _) -> showPattern = !showPattern)
                    .setPlayClickSoundResource(
                        () -> showPattern ? SoundResource.GUI_BUTTON_UP.resourceLocation
                            : SoundResource.GUI_BUTTON_DOWN.resourceLocation)
                    .setBackground(() -> {
                        if (showPattern) {
                            return new IDrawable[] { GTUITextures.BUTTON_STANDARD_PRESSED,
                                GTUITextures.OVERLAY_BUTTON_WHITELIST };
                        }
                        return new IDrawable[] { GTUITextures.BUTTON_STANDARD, GTUITextures.OVERLAY_BUTTON_BLACKLIST };
                    })
                    .attachSyncer(
                        new FakeSyncWidget.BooleanSyncer(() -> showPattern, value -> showPattern = value),
                        builder)
                    .dynamicTooltip(
                        () -> Collections.singletonList(
                            StatCollector.translateToLocal(
                                "GT5U.infodata.hatch.crafting_input_me.show_pattern."
                                    + (showPattern ? "enable" : "disabled"))))
                    .setTooltipShowUpDelay(TOOLTIP_DELAY)
                    .setUpdateTooltipEveryTick(true)
                    .setPos(134, 9)
                    .setSize(16, 16));
    }

    @Override
    public ITexture[] getTexturesActive(ITexture aBaseTexture) {
        return getTexturesInactive(aBaseTexture);
    }

    @Override
    public ITexture[] getTexturesInactive(ITexture aBaseTexture) {
        return new ITexture[] { aBaseTexture, TextureFactory.of(OVERLAY_ME_CRAFTING_INPUT_BUFFER) };
    }

    @Override
    public String[] getDescription() {
        return new String[] { GTN_Utils.tr("GTN.Hatch.WildcardPatternBuffer.tooltip.00"),
            GTN_Utils.tr("GTN.Hatch.WildcardPatternBuffer.tooltip.01"),
            GTN_Utils.tr("GTN.Hatch.WildcardPatternBuffer.tooltip.02"),
            GTN_Utils.tr("GTN.Hatch.WildcardPatternBuffer.tooltip.03"),
            GTN_Utils.tr("GTN.Hatch.WildcardPatternBuffer.tooltip.04"),
            GTN_Utils.tr("GTN.Hatch.WildcardPatternBuffer.tooltip.05"),
            GTN_Utils.tr("GTN.Wildcard.tooltip.author", "§aCrazer"),
            GTN_Utils.tr("GTN.Hatch.common.addedBy", Constants.MOD_NAME) };
    }
}
