package com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Processing.LV;

import static gregtech.api.enums.HatchElement.InputBus;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_SCHEST;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_SCHEST_GLOW;
import static gregtech.api.util.GTUtility.validMTEList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.common.util.ForgeDirection;

import org.jetbrains.annotations.NotNull;

import com.EvgenWarGold.GregTechNightmare.GregTech.Api.MultiblockArea;
import com.EvgenWarGold.GregTechNightmare.GregTech.Api.MultiblockOffsets;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Gui.GTN_ItemCrateGui;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses.GTN_Casings;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses.GTN_MultiBlockBase;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses.GTN_MultiBlockTooltipBuilder;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses.StructureVariant;
import com.EvgenWarGold.GregTechNightmare.Utils.Authors;
import com.cleanroommc.modularui.utils.item.ItemStackHandler;
import com.gtnewhorizon.gtnhlib.capability.item.ItemIO;
import com.gtnewhorizon.gtnhlib.item.InventoryItemSink;
import com.gtnewhorizon.gtnhlib.item.InventoryItemSource;
import com.gtnewhorizon.gtnhlib.item.WrappedItemIO;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;

import gregtech.api.interfaces.IIconContainer;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchInputBus;
import gregtech.common.gui.modularui.multiblock.base.MTEMultiBlockBaseGui;

public class GTN_ItemCrate extends GTN_MultiBlockBase<GTN_ItemCrate> {

    public static final int SLOT_COUNT = 576;

    private static final String NBT_INVENTORY = "GTNItemCrateInventory";
    private static final int INPUT_TRANSFER_INTERVAL = 5;

    private static final Comparator<ItemStack> STACK_COMPARATOR = (first, second) -> {
        int result = Integer.compare(Item.getIdFromItem(first.getItem()), Item.getIdFromItem(second.getItem()));

        if (result != 0) {
            return result;
        }

        result = Integer.compare(first.getItemDamage(), second.getItemDamage());
        if (result != 0) {
            return result;
        }

        String firstTag = first.stackTagCompound == null ? "" : first.stackTagCompound.toString();
        String secondTag = second.stackTagCompound == null ? "" : second.stackTagCompound.toString();
        return firstTag.compareTo(secondTag);
    };

    private final ItemStackHandler storageInventory = new ItemStackHandler(SLOT_COUNT) {

        @Override
        protected void onContentsChanged(int slot) {
            markStorageDirty();
        }
    };

    private final IInventory storageInventoryView = new StorageInventoryView();

    private boolean suppressStorageUpdates;

    public GTN_ItemCrate(int id, String name) {
        super(id, name);
    }

    public GTN_ItemCrate(String name) {
        super(name);
    }

    @Override
    public List<StructureVariant<GTN_ItemCrate>> getStructureVariants() {
        return List.of(
            new StructureVariant<>(
                "ItemCrate",
                // spotless:off
                new String[][]{
                    {"AAA", "AAA", "AAA"},
                    {"A~A", "A A", "AAA"},
                    {"AAA", "AAA", "AAA"}
                },
                // spotless:on
                new MultiblockOffsets(1, 1, 0),
                new MultiblockArea(3, 3, 3),
                1,
                GTN_Casings.SolidSteelMachineCasing));
    }

    @Override
    public GTN_ItemCrate createNewMetaEntity() {
        return new GTN_ItemCrate(this.mName);
    }

    @Override
    public void createGtnTooltip(GTN_MultiBlockTooltipBuilder builder) {
        builder.addInputBus();
    }

    @Override
    public Authors getAuthor() {
        return Authors.CRAZER;
    }

    @Override
    public IStructureDefinition<GTN_ItemCrate> getStructureDefinition() {
        return buildStructureDefinition(builder -> builder.addMainCasing('A', b -> b.hatches(InputBus)));
    }

    @Override
    public boolean isEnergyMultiBlock() {
        return false;
    }

    @Override
    public boolean isNoMaintenanceIssue() {
        return true;
    }

    @Override
    protected ItemIO getItemIO(ForgeDirection side) {
        return new WrappedItemIO(
            new InventoryItemSource(storageInventoryView, side),
            new InventoryItemSink(storageInventoryView, side));
    }

    @Override
    public void onPostTick(IGregTechTileEntity gte, long tick) {
        super.onPostTick(gte, tick);
        if (gte.isServerSide() && mMachine && tick % INPUT_TRANSFER_INTERVAL == 0) {
            transferInputBusses();
        }
    }

    @Override
    public boolean onRightclick(IGregTechTileEntity gte, EntityPlayer player) {
        if (gte.isServerSide() && !mMachine) {
            player.addChatMessage(new ChatComponentTranslation("GTN.ItemCrate.structureRequired"));
            return true;
        }
        return super.onRightclick(gte, player);
    }

    public ItemStackHandler getStorageInventory() {
        return storageInventory;
    }

    public IInventory getHandyBagInventory() {
        return mMachine ? storageInventoryView : null;
    }

    public int countOccupiedSlots() {
        int occupied = 0;
        for (int slot = 0; slot < storageInventory.getSlots(); slot++) {
            if (storageInventory.getStackInSlot(slot) != null) {
                occupied++;
            }
        }
        return occupied;
    }

    public void sortStorage() {
        if (!mMachine) {
            return;
        }

        List<ItemStack> stacks = new ArrayList<>();
        for (int slot = 0; slot < storageInventory.getSlots(); slot++) {
            ItemStack stack = storageInventory.getStackInSlot(slot);

            if (stack != null) {
                stacks.add(stack.copy());
            }
        }

        stacks.sort(STACK_COMPARATOR);

        List<ItemStack> compacted = new ArrayList<>();
        for (ItemStack stack : stacks) {
            int remaining = stack.stackSize;
            if (!compacted.isEmpty()) {
                ItemStack previous = compacted.getLast();
                if (canStacksMerge(previous, stack)) {
                    int previousLimit = Math.clamp(previous.getMaxStackSize(), 1, 64);
                    int moved = Math.clamp(previousLimit - previous.stackSize, 0, remaining);
                    previous.stackSize += moved;
                    remaining -= moved;
                }
            }

            while (remaining > 0) {
                ItemStack split = stack.copy();
                int splitLimit = Math.clamp(split.getMaxStackSize(), 1, 64);
                split.stackSize = Math.min(remaining, splitLimit);
                compacted.add(split);
                remaining -= split.stackSize;
            }
        }

        suppressStorageUpdates = true;
        try {
            for (int slot = 0; slot < storageInventory.getSlots(); slot++) {
                storageInventory.setStackInSlot(slot, slot < compacted.size() ? compacted.get(slot) : null);
            }
        } finally {
            suppressStorageUpdates = false;
        }
        markStorageDirty();
    }

    public void depositAll(EntityPlayer player) {
        if (!mMachine || player == null) {
            return;
        }

        boolean changed = false;
        suppressStorageUpdates = true;
        try {
            for (int playerSlot = 0; playerSlot < player.inventory.mainInventory.length; playerSlot++) {
                ItemStack stack = player.inventory.mainInventory[playerSlot];

                if (stack == null) {
                    continue;
                }

                ItemStack remainder = insertIntoStorage(stack.copy());
                if (remainder == null || remainder.stackSize != stack.stackSize) {
                    player.inventory.mainInventory[playerSlot] = remainder;
                    changed = true;
                }
            }
        } finally {
            suppressStorageUpdates = false;
        }

        if (changed) {
            player.inventory.markDirty();
            markStorageDirty();
        }
    }

    private void transferInputBusses() {
        boolean changed = false;
        suppressStorageUpdates = true;
        try {
            for (MTEHatchInputBus inputBus : validMTEList(mInputBusses)) {
                if (transferInputBus(inputBus)) {
                    changed = true;
                }
            }
        } finally {
            suppressStorageUpdates = false;
        }

        if (changed) {
            markStorageDirty();
        }
    }

    private boolean transferInputBus(MTEHatchInputBus inputBus) {
        boolean changed = false;
        for (int slot = 0; slot < inputBus.getSizeInventory(); slot++) {
            if (!inputBus.isValidSlot(slot)) {
                continue;
            }

            ItemStack stack = inputBus.getStackInSlot(slot);
            if (stack == null) {
                continue;
            }

            ItemStack remainder = insertIntoStorage(stack.copy());
            if (remainder != null && remainder.stackSize == stack.stackSize) {
                continue;
            }

            inputBus.setInventorySlotContents(slot, remainder);
            changed = true;
        }

        if (changed) {
            inputBus.updateSlots();
        }
        return changed;
    }

    private ItemStack insertIntoStorage(ItemStack stack) {
        ItemStack remainder = stack;

        for (int slot = 0; slot < storageInventory.getSlots() && remainder != null; slot++) {
            ItemStack existing = storageInventory.getStackInSlot(slot);

            if (existing != null && canStacksMerge(existing, remainder)) {
                remainder = storageInventory.insertItem(slot, remainder, false);
            }
        }

        for (int slot = 0; slot < storageInventory.getSlots() && remainder != null; slot++) {
            if (storageInventory.getStackInSlot(slot) == null) {
                remainder = storageInventory.insertItem(slot, remainder, false);
            }
        }

        return remainder;
    }

    private static boolean canStacksMerge(ItemStack first, ItemStack second) {
        return first.getItem() == second.getItem() && first.getItemDamage() == second.getItemDamage()
            && ItemStack.areItemStackTagsEqual(first, second);
    }

    @Override
    public void saveNBTData(NBTTagCompound nbt) {
        super.saveNBTData(nbt);
        nbt.setTag(NBT_INVENTORY, storageInventory.serializeNBT());
    }

    @Override
    public void loadNBTData(NBTTagCompound nbt) {
        super.loadNBTData(nbt);
        if (!nbt.hasKey(NBT_INVENTORY, 10)) {
            return;
        }

        NBTTagCompound inventoryData = nbt.getCompoundTag(NBT_INVENTORY);
        inventoryData.setInteger("Size", SLOT_COUNT);

        suppressStorageUpdates = true;
        try {
            storageInventory.deserializeNBT(inventoryData);
        } finally {
            suppressStorageUpdates = false;
        }
    }

    @Override
    public void setItemNBT(NBTTagCompound nbt) {
        super.setItemNBT(nbt);
        nbt.setTag(NBT_INVENTORY, storageInventory.serializeNBT());
    }

    private void markStorageDirty() {
        if (suppressStorageUpdates) {
            return;
        }

        IGregTechTileEntity gte = getBaseMetaTileEntity();
        if (gte instanceof TileEntity base) {
            base.markDirty();
        }
    }

    private final class StorageInventoryView implements IInventory {

        @Override
        public int getSizeInventory() {
            return SLOT_COUNT;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return mMachine ? storageInventory.getStackInSlot(slot) : null;
        }

        @Override
        public ItemStack decrStackSize(int slot, int amount) {
            return mMachine ? storageInventory.extractItem(slot, amount, false) : null;
        }

        @Override
        public ItemStack getStackInSlotOnClosing(int slot) {
            return null;
        }

        @Override
        public void setInventorySlotContents(int slot, ItemStack stack) {
            if (mMachine) {
                storageInventory.setStackInSlot(slot, stack);
            }
        }

        @Override
        public String getInventoryName() {
            return mName;
        }

        @Override
        public boolean hasCustomInventoryName() {
            return false;
        }

        @Override
        public int getInventoryStackLimit() {
            return 64;
        }

        @Override
        public void markDirty() {
            markStorageDirty();
        }

        @Override
        public boolean isUseableByPlayer(EntityPlayer player) {
            return mMachine;
        }

        @Override
        public void openInventory() {}

        @Override
        public void closeInventory() {}

        @Override
        public boolean isItemValidForSlot(int slot, ItemStack stack) {
            return mMachine && stack != null;
        }
    }

    @Override
    public IIconContainer getMainOverlay() {
        return OVERLAY_SCHEST;
    }

    @Override
    public IIconContainer getMainOverlayActive() {
        return OVERLAY_SCHEST;
    }

    @Override
    public IIconContainer getMainOverlayGlow() {
        return OVERLAY_SCHEST_GLOW;
    }

    @Override
    public IIconContainer getMainOverlayActiveGlow() {
        return OVERLAY_SCHEST_GLOW;
    }

    @Override
    protected @NotNull MTEMultiBlockBaseGui<?> getGui() {
        return new GTN_ItemCrateGui(this);
    }
}
