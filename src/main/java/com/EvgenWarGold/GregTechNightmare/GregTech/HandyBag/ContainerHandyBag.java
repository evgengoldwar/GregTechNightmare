package com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S2FPacketSetSlot;

import com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag.network.HandyBagNetwork;
import com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag.network.MessageSyncHandySlot;
import com.EvgenWarGold.GregTechNightmare.GregTech.Items.ItemHandyBag;
import com.EvgenWarGold.GregTechNightmare.GregTech.Items.ItemMemoryCard;

public class ContainerHandyBag extends Container {

    public final EntityPlayer player;
    public final HandyBagData data;
    public final InventoryHandyBag bagInventory;
    public final InventoryHandyBagCards cardInventory;
    public final InventoryCrafting craftMatrix;
    public final IInventory craftResult;
    public final int bagSlot;
    private final ItemStack openedBag;
    private final int bagSize;
    private final int bagEnd;
    private final int cardStart;
    private final int cardEnd;
    private final int playerStart;
    private final int playerEnd;
    private final int armorStart;
    private final int armorEnd;
    private final int craftResultIndex;
    private final int craftStart;
    private final int craftEnd;
    private int lastSelectedCard;
    private int lastPickupMode;
    private boolean lastRestockEnabled;
    private boolean lastLocked;
    private final boolean[] lastQuickBlocked = new boolean[3];
    private int selectedSwapSlot = -1;

    public ContainerHandyBag(EntityPlayer player, int bagSlot) {
        this.player = player;
        this.bagSlot = bagSlot;
        this.openedBag = ItemHandyBag.getBagInSlot(player, bagSlot);
        this.data = new HandyBagData(openedBag, player);
        this.bagInventory = new InventoryHandyBag(data);
        this.cardInventory = new InventoryHandyBagCards(data);
        this.craftMatrix = new InventoryCrafting(this, 2, 2);
        this.craftResult = new InventoryCraftResult();
        this.bagSize = bagInventory.getSizeInventory();
        this.bagEnd = bagSize;
        this.cardStart = bagEnd;
        this.cardEnd = cardStart + 4;
        this.playerStart = cardEnd;
        this.playerEnd = playerStart + 36;
        this.armorStart = playerEnd;
        this.armorEnd = armorStart + 4;
        this.craftResultIndex = armorEnd;
        this.craftStart = craftResultIndex + 1;
        this.craftEnd = craftStart + 4;
        this.lastSelectedCard = data.getSelectedCard();
        this.lastPickupMode = data.getPickupMode();
        this.lastRestockEnabled = data.isRestockEnabled();
        this.lastLocked = data.isLocked();
        for (int i = 0; i < 3; i++) {
            this.lastQuickBlocked[i] = data.isSectionQuickActionBlocked(i);
        }
        addBagSlots();
        addCardSlots();
        addPlayerSlots(player.inventory);
        addArmorAndCraftingSlots(player.inventory);
        onCraftMatrixChanged(craftMatrix);
    }

    public boolean isLarge() {
        return bagSize == ItemHandyBag.LARGE_SIZE;
    }

    public int getSelectedCard() {
        return data.getSelectedCard();
    }

    public int getBagStackLimit() {
        return data.getStackLimit();
    }

    public int getPickupMode() {
        return data.getPickupMode();
    }

    public boolean isRestockEnabled() {
        return data.isRestockEnabled();
    }

    public void setPickupMode(int mode) {
        data.setPickupMode(mode);
        lastPickupMode = data.getPickupMode();
        detectAndSendChanges();
    }

    public void setRestockEnabled(boolean enabled) {
        data.setRestockEnabled(enabled);
        lastRestockEnabled = data.isRestockEnabled();
        detectAndSendChanges();
    }

    public boolean isLocked() {
        return data.isLocked();
    }

    public void setLocked(boolean locked) {
        data.setLocked(locked);
        lastLocked = data.isLocked();
        detectAndSendChanges();
    }

    public int getSelectedSwapSlot() {
        return selectedSwapSlot;
    }

    public int getSectionCount() {
        return data.getSectionCount();
    }

    public boolean isSectionQuickActionBlocked(int section) {
        return data.isSectionQuickActionBlocked(section);
    }

    public void setSectionQuickActionBlocked(int section, boolean blocked) {
        data.setSectionQuickActionBlocked(section, blocked);
        if (section >= 0 && section < lastQuickBlocked.length)
            lastQuickBlocked[section] = data.isSectionQuickActionBlocked(section);
        detectAndSendChanges();
    }

    public void performQuickAction(int action) {
        switch (action) {
            case 0:
                HandyBagOperations.moveAllPlayerToBag(player, bagSlot, data);
                break;
            case 1:
                HandyBagOperations.quickStackPlayerToBag(player, bagSlot, data);
                break;
            case 2:
                HandyBagOperations.leaveOneFullStack(player, bagSlot, data);
                break;
            case 3:
                HandyBagOperations.fillPlayerStacks(player, bagSlot, data);
                break;
            case 4:
                HandyBagOperations.moveMatchingBagToPlayer(player, bagSlot, data);
                break;
            case 5:
                HandyBagOperations.moveAllBagToPlayer(player, bagSlot, data);
                break;
            default:
                return;
        }
        detectAndSendChanges();
    }

    public void selectCard(int slot) {
        if (slot < 0 || slot >= 4 || slot == data.getSelectedCard() || data.getCard(slot) == null) return;
        data.selectCard(slot);
        selectedSwapSlot = -1;
        lastSelectedCard = slot;
        onCraftMatrixChanged(craftMatrix);
        detectAndSendChanges();
    }

    private void addBagSlots() {
        int baseX = isLarge() ? 48 : 8;
        int index = 0;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new SlotHandyBag(bagInventory, index++, baseX + col * 18, 102 + row * 18));
            }
        }
        if (isLarge()) {
            for (int row = 0; row < 7; row++) {
                for (int col = 0; col < 2; col++) {
                    addSlotToContainer(new SlotHandyBag(bagInventory, index++, 8 + col * 18, 102 + row * 18));
                }
            }
            for (int row = 0; row < 7; row++) {
                for (int col = 0; col < 2; col++) {
                    addSlotToContainer(new SlotHandyBag(bagInventory, index++, 214 + col * 18, 102 + row * 18));
                }
            }
        }
    }

    private void addCardSlots() {
        int startX = isLarge() ? 138 : 98;
        for (int i = 0; i < 4; i++) {
            addSlotToContainer(new SlotMemoryCard(cardInventory, i, startX + i * 18, 69));
        }
    }

    private void addPlayerSlots(InventoryPlayer inventory) {
        int baseX = isLarge() ? 48 : 8;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int index = col + row * 9 + 9;
                addSlotToContainer(new SlotLockedPlayer(inventory, index, baseX + col * 18, 174 + row * 18, bagSlot));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new SlotLockedPlayer(inventory, col, baseX + col * 18, 232, bagSlot));
        }
    }

    private void addArmorAndCraftingSlots(final InventoryPlayer inventory) {
        int baseX = isLarge() ? 48 : 8;
        for (int i = 0; i < 4; i++) {
            final int armorType = i;
            addSlotToContainer(new Slot(inventory, 39 - i, baseX, 15 + i * 18) {

                @Override
                public int getSlotStackLimit() {
                    return 1;
                }

                @Override
                public boolean isItemValid(ItemStack stack) {
                    return stack != null && stack.getItem()
                        .isValidArmor(stack, armorType, player);
                }
            });
        }

        int craftX = baseX + 90;
        addSlotToContainer(new SlotCrafting(player, craftMatrix, craftResult, 0, craftX + 54, 25));
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 2; col++) {
                addSlotToContainer(new Slot(craftMatrix, col + row * 2, craftX + col * 18, 15 + row * 18));
            }
        }
    }

    @Override
    public void onCraftMatrixChanged(IInventory inventory) {
        craftResult.setInventorySlotContents(
            0,
            CraftingManager.getInstance()
                .findMatchingRecipe(craftMatrix, player.worldObj));
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        ItemStack current = ItemHandyBag.getBagInSlot(player, bagSlot);
        return current != null && openedBag != null
            && current.getItem() == openedBag.getItem()
            && current.getItemDamage() == openedBag.getItemDamage();
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        if (!player.worldObj.isRemote) {
            data.flush();
            ItemStack currentBag = ItemHandyBag.getBagInSlot(player, bagSlot);
            if (currentBag != null && currentBag != openedBag
                && openedBag != null
                && currentBag.getItem() == openedBag.getItem()
                && currentBag.getItemDamage() == openedBag.getItemDamage()) {
                currentBag.setTagCompound(
                    openedBag.getTagCompound() == null ? null
                        : (NBTTagCompound) openedBag.getTagCompound()
                            .copy());
                player.inventory.markDirty();
            }
            for (int i = 0; i < craftMatrix.getSizeInventory(); i++) {
                ItemStack stack = craftMatrix.getStackInSlotOnClosing(i);
                if (stack != null) player.dropPlayerItemWithRandomChoice(stack, false);
            }
        }
    }

    @Override
    public ItemStack slotClick(int slotId, int button, int mode, EntityPlayer player) {
        if (slotId >= 0 && slotId < inventorySlots.size()) {
            Slot slot = getSlot(slotId);
            if (slot instanceof SlotHandyBag) {
                if (mode == 0) clickLargeSlot(slot, button, player);
                else if (mode == 1) transferStackInSlot(player, slotId);
                else if (mode == 3 && button == 2) middleClickBagSlot(slotId, player);
                return null;
            }
        }
        return super.slotClick(slotId, button, mode, player);
    }

    private void middleClickBagSlot(int slotId, EntityPlayer player) {
        if (selectedSwapSlot < 0) {
            selectedSwapSlot = slotId;
            return;
        }
        if (selectedSwapSlot == slotId) {
            selectedSwapSlot = -1;
            return;
        }
        if (selectedSwapSlot >= inventorySlots.size()) {
            selectedSwapSlot = slotId;
            return;
        }
        Slot first = getSlot(selectedSwapSlot);
        Slot second = getSlot(slotId);
        if (!(first instanceof SlotHandyBag) || !(second instanceof SlotHandyBag)) {
            selectedSwapSlot = -1;
            return;
        }
        ItemStack firstStack = first.getStack();
        ItemStack secondStack = second.getStack();
        data.beginBatch();
        try {
            first.putStack(secondStack);
            second.putStack(firstStack);
            first.onSlotChanged();
            second.onSlotChanged();
        } finally {
            data.endBatch();
        }
        selectedSwapSlot = -1;
        detectAndSendChanges();
    }

    private void clickLargeSlot(Slot slot, int button, EntityPlayer player) {
        ItemStack slotStack = slot.getStack();
        ItemStack cursor = player.inventory.getItemStack();
        int limit = slot.getSlotStackLimit();

        if (cursor == null) {
            if (slotStack == null) return;
            int amount = button == 1 ? 1 : Math.min(slotStack.stackSize, slotStack.getMaxStackSize());
            ItemStack taken = slot.decrStackSize(amount);
            player.inventory.setItemStack(taken);
            if (taken != null) slot.onPickupFromSlot(player, taken);
            return;
        }

        if (!slot.isItemValid(cursor) || limit <= 0) return;

        if (slotStack == null) {
            int amount = button == 1 ? 1 : Math.min(cursor.stackSize, limit);
            ItemStack inserted = cursor.copy();
            inserted.stackSize = amount;
            slot.putStack(inserted);
            cursor.stackSize -= amount;
            if (cursor.stackSize <= 0) player.inventory.setItemStack(null);
            return;
        }

        if (canStacksMerge(slotStack, cursor)) {
            int free = limit - slotStack.stackSize;
            if (free <= 0) return;
            int amount = Math.min(free, button == 1 ? 1 : cursor.stackSize);
            slotStack.stackSize += amount;
            cursor.stackSize -= amount;
            slot.onSlotChanged();
            if (cursor.stackSize <= 0) player.inventory.setItemStack(null);
            return;
        }

        if (button == 0 && slotStack.stackSize <= cursor.getMaxStackSize() && cursor.stackSize <= limit) {
            slot.putStack(cursor);
            player.inventory.setItemStack(slotStack);
        }
    }

    private static boolean canStacksMerge(ItemStack a, ItemStack b) {
        return a != null && b != null
            && a.getItem() == b.getItem()
            && a.getItemDamage() == b.getItemDamage()
            && ItemStack.areItemStackTagsEqual(a, b);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        if (index < 0 || index >= inventorySlots.size()) return null;
        Slot sourceSlot = getSlot(index);
        if (sourceSlot == null || !sourceSlot.getHasStack() || !sourceSlot.canTakeStack(player)) return null;
        ItemStack source = sourceSlot.getStack();
        ItemStack original = source.copy();
        boolean moved;

        if (index < bagEnd) {
            moved = mergeStack(source, playerStart, playerEnd, true);
        } else if (index >= cardStart && index < cardEnd) {
            moved = mergeStack(source, playerStart, playerEnd, true);
        } else if (index >= playerStart && index < playerEnd) {
            if (source.getItem() instanceof ItemMemoryCard) {
                moved = mergeStack(source, cardStart, cardEnd, false);
                if (!moved || source.stackSize > 0) moved |= mergeStack(source, 0, bagEnd, false);
            } else {
                moved = mergeStack(source, 0, bagEnd, false);
            }
        } else if (index == craftResultIndex) {
            moved = mergeStack(source, playerStart, playerEnd, true);
        } else if (index >= armorStart && index < armorEnd || index >= craftStart && index < craftEnd) {
            moved = mergeStack(source, playerStart, playerEnd, false);
        } else {
            moved = false;
        }

        if (!moved) return null;
        sourceSlot.onSlotChange(source, original);
        if (source.stackSize <= 0) {
            sourceSlot.putStack(null);
        } else {
            sourceSlot.onSlotChanged();
        }
        if (source.stackSize == original.stackSize) return null;
        sourceSlot.onPickupFromSlot(player, source);
        return original;
    }

    private boolean mergeStack(ItemStack stack, int start, int end, boolean reverse) {
        if (stack == null || stack.stackSize <= 0) return false;
        boolean changed = false;
        int i = reverse ? end - 1 : start;
        int step = reverse ? -1 : 1;

        while (stack.stackSize > 0 && i >= start && i < end) {
            Slot slot = getSlot(i);
            ItemStack target = slot.getStack();
            if (target != null && slot.isItemValid(stack) && canStacksMerge(target, stack)) {
                int max = getEffectiveLimit(slot, stack);
                int free = max - target.stackSize;
                if (free > 0) {
                    int amount = Math.min(free, stack.stackSize);
                    target.stackSize += amount;
                    stack.stackSize -= amount;
                    slot.onSlotChanged();
                    changed = true;
                }
            }
            i += step;
        }

        i = reverse ? end - 1 : start;
        while (stack.stackSize > 0 && i >= start && i < end) {
            Slot slot = getSlot(i);
            if (!slot.getHasStack() && slot.isItemValid(stack)) {
                int max = getEffectiveLimit(slot, stack);
                if (max > 0) {
                    int amount = Math.min(max, stack.stackSize);
                    ItemStack placed = stack.copy();
                    placed.stackSize = amount;
                    slot.putStack(placed);
                    stack.stackSize -= amount;
                    changed = true;
                }
            }
            i += step;
        }

        return changed;
    }

    private static int getEffectiveLimit(Slot slot, ItemStack stack) {
        if (slot instanceof SlotHandyBag) return slot.getSlotStackLimit();
        return Math.min(slot.getSlotStackLimit(), stack.getMaxStackSize());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addCraftingToCrafters(ICrafting crafting) {
        if (crafters.contains(crafting)) throw new IllegalArgumentException("Listener already listening");
        crafters.add(crafting);
        if (crafting instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) crafting;
            syncAllSlots(player);
            player.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(-1, -1, player.inventory.getItemStack()));
        }
        crafting.sendProgressBarUpdate(this, 0, data.getSelectedCard());
        crafting.sendProgressBarUpdate(this, 1, data.getPickupMode());
        crafting.sendProgressBarUpdate(this, 2, data.isRestockEnabled() ? 1 : 0);
        crafting.sendProgressBarUpdate(this, 3, data.isLocked() ? 1 : 0);
        for (int i = 0; i < 3; i++) {
            crafting.sendProgressBarUpdate(this, 4 + i, data.isSectionQuickActionBlocked(i) ? 1 : 0);
        }
        detectAndSendChanges();
    }

    private int getOpenedBagContainerSlot() {
        return bagSlot >= 9 ? playerStart + bagSlot - 9 : playerStart + 27 + bagSlot;
    }

    private void syncAllSlots(EntityPlayerMP player) {
        int openedBagContainerSlot = getOpenedBagContainerSlot();
        for (int i = 0; i < inventorySlots.size(); i++) {
            if (i == openedBagContainerSlot) continue;
            ItemStack stack = getSlot(i).getStack();
            HandyBagNetwork.CHANNEL.sendTo(new MessageSyncHandySlot(windowId, i, stack), player);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void detectAndSendChanges() {
        int openedBagContainerSlot = getOpenedBagContainerSlot();
        for (int i = 0; i < inventorySlots.size(); i++) {
            ItemStack current = getSlot(i).getStack();
            ItemStack previous = (ItemStack) inventoryItemStacks.get(i);
            if (!ItemStack.areItemStacksEqual(previous, current)) {
                ItemStack copy = current == null ? null : current.copy();
                inventoryItemStacks.set(i, copy);
                if (i == openedBagContainerSlot) continue;
                for (Object listener : (List) crafters) {
                    if (listener instanceof EntityPlayerMP) {
                        HandyBagNetwork.CHANNEL
                            .sendTo(new MessageSyncHandySlot(windowId, i, copy), (EntityPlayerMP) listener);
                    }
                }
            }
        }
        int selected = data.getSelectedCard();
        if (selected != lastSelectedCard) {
            lastSelectedCard = selected;
            for (Object listener : (List) crafters) {
                ((ICrafting) listener).sendProgressBarUpdate(this, 0, selected);
            }
        }
        int pickupMode = data.getPickupMode();
        if (pickupMode != lastPickupMode) {
            lastPickupMode = pickupMode;
            for (Object listener : (List) crafters) {
                ((ICrafting) listener).sendProgressBarUpdate(this, 1, pickupMode);
            }
        }
        boolean restockEnabled = data.isRestockEnabled();
        if (restockEnabled != lastRestockEnabled) {
            lastRestockEnabled = restockEnabled;
            for (Object listener : (List) crafters) {
                ((ICrafting) listener).sendProgressBarUpdate(this, 2, restockEnabled ? 1 : 0);
            }
        }
        boolean locked = data.isLocked();
        if (locked != lastLocked) {
            lastLocked = locked;
            for (Object listener : (List) crafters) {
                ((ICrafting) listener).sendProgressBarUpdate(this, 3, locked ? 1 : 0);
            }
        }
        for (int i = 0; i < 3; i++) {
            boolean quickBlocked = data.isSectionQuickActionBlocked(i);
            if (quickBlocked != lastQuickBlocked[i]) {
                lastQuickBlocked[i] = quickBlocked;
                for (Object listener : (List) crafters) {
                    ((ICrafting) listener).sendProgressBarUpdate(this, 4 + i, quickBlocked ? 1 : 0);
                }
            }
        }
    }

    @Override
    public void updateProgressBar(int id, int value) {
        if (id == 0 && value >= 0 && value < 4 && value != data.getSelectedCard()) data.selectCard(value);
        if (id == 1 && value >= ItemHandyBag.PICKUP_OFF && value <= ItemHandyBag.PICKUP_ALL) data.setPickupMode(value);
        if (id == 2) data.setRestockEnabled(value != 0);
        if (id == 3) data.setLocked(value != 0);
        if (id >= 4 && id <= 6) data.setSectionQuickActionBlocked(id - 4, value != 0);
    }
}
