package com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;

import com.EvgenWarGold.GregTechNightmare.GregTech.Items.ItemHandyBag;

public final class HandyBagOperations {

    private HandyBagOperations() {}

    public static boolean containsMatching(HandyBagData data, ItemStack stack) {
        if (data == null || stack == null) return false;
        for (int i = 0; i < data.getSize(); i++) {
            if (data.isSlotQuickActionBlocked(i)) continue;
            if (stacksMatch(data.getItem(i), stack)) return true;
        }
        return false;
    }

    public static int mergePickupIntoExistingPlayerStacks(EntityPlayer player, ItemStack stack) {
        if (player == null || stack == null || stack.stackSize <= 0) return 0;
        InventoryPlayer inventory = player.inventory;
        int before = stack.stackSize;
        for (int i = 0; i < 36 && stack.stackSize > 0; i++) {
            ItemStack target = inventory.getStackInSlot(i);
            if (!stacksMatch(target, stack)) continue;
            int max = Math.min(target.getMaxStackSize(), inventory.getInventoryStackLimit());
            int free = max - target.stackSize;
            if (free <= 0) continue;
            int amount = Math.min(free, stack.stackSize);
            target.stackSize += amount;
            stack.stackSize -= amount;
        }
        if (stack.stackSize != before) inventory.markDirty();
        return before - stack.stackSize;
    }

    public static int pickupIntoBag(HandyBagData data, ItemStack stack, boolean matchingOnly) {
        if (data == null || stack == null || stack.stackSize <= 0) return 0;
        if (matchingOnly && !containsMatching(data, stack)) return 0;
        int before = stack.stackSize;
        data.beginBatch();
        insertIntoBag(data, stack);
        data.endBatch();
        return before - stack.stackSize;
    }

    public static boolean moveBagToInventory(HandyBagData data, IInventory inventory, int side) {
        if (!canUse(data) || inventory == null) return false;
        boolean changed = false;
        data.beginBatch();
        for (int i = 0; i < data.getSize(); i++) {
            if (data.isSlotQuickActionBlocked(i)) continue;
            ItemStack source = data.getItem(i);
            if (source == null || source.stackSize <= 0) continue;
            int before = source.stackSize;
            insertIntoInventory(inventory, side, source);
            if (source.stackSize != before) {
                changed = true;
                data.setItem(i, source.stackSize <= 0 ? null : source);
            }
        }
        data.endBatch();
        if (changed) inventory.markDirty();
        return changed;
    }

    public static boolean moveInventoryToBag(HandyBagData data, IInventory inventory, int side, boolean matchingOnly) {
        if (!canUse(data) || inventory == null) return false;
        boolean changed = false;
        int[] slots = getAccessibleSlots(inventory, side);
        data.beginBatch();
        for (int slot : slots) {
            ItemStack source = inventory.getStackInSlot(slot);
            if (source == null || source.stackSize <= 0 || source.getItem() instanceof ItemHandyBag) continue;
            if (!canExtract(inventory, slot, source, side)) continue;
            if (matchingOnly && !containsMatching(data, source)) continue;
            int before = source.stackSize;
            insertIntoBag(data, source);
            if (source.stackSize != before) {
                changed = true;
                inventory.setInventorySlotContents(slot, source.stackSize <= 0 ? null : source);
            }
        }
        data.endBatch();
        if (changed) inventory.markDirty();
        return changed;
    }

    public static boolean moveAllPlayerToBag(EntityPlayer player, int bagSlot, HandyBagData data) {
        return movePlayerToBag(player, bagSlot, data, false);
    }

    public static boolean quickStackPlayerToBag(EntityPlayer player, int bagSlot, HandyBagData data) {
        return movePlayerToBag(player, bagSlot, data, true);
    }

    public static boolean leaveOneFullStack(EntityPlayer player, int bagSlot, HandyBagData data) {
        if (!canUse(data) || player == null) return false;
        InventoryPlayer inventory = player.inventory;
        boolean changed = false;
        data.beginBatch();
        for (int i = 0; i < 36; i++) {
            if (i == bagSlot) continue;
            ItemStack keeper = inventory.getStackInSlot(i);
            if (keeper == null || keeper.stackSize <= 0 || keeper.getItem() instanceof ItemHandyBag) continue;
            if (hasEarlierMatch(inventory, bagSlot, keeper, i)) continue;

            int total = countMatching(inventory, bagSlot, keeper);
            int keep = Math.min(total, keeper.getMaxStackSize());
            if (total <= keep) continue;

            if (keeper.stackSize < keep) {
                int need = keep - keeper.stackSize;
                for (int j = i + 1; j < 36 && need > 0; j++) {
                    if (j == bagSlot) continue;
                    ItemStack source = inventory.getStackInSlot(j);
                    if (!stacksMatch(keeper, source)) continue;
                    int amount = Math.min(need, source.stackSize);
                    keeper.stackSize += amount;
                    source.stackSize -= amount;
                    need -= amount;
                    changed = true;
                    if (source.stackSize <= 0) inventory.setInventorySlotContents(j, null);
                }
            }

            for (int j = 0; j < 36; j++) {
                if (j == bagSlot || j == i) continue;
                ItemStack source = inventory.getStackInSlot(j);
                if (!stacksMatch(keeper, source)) continue;
                int before = source.stackSize;
                insertIntoBag(data, source);
                if (source.stackSize != before) changed = true;
                if (source.stackSize <= 0) inventory.setInventorySlotContents(j, null);
            }
        }
        data.endBatch();
        if (changed) inventory.markDirty();
        return changed;
    }

    public static boolean fillPlayerStacks(EntityPlayer player, int bagSlot, HandyBagData data) {
        if (!canUse(data) || player == null) return false;
        InventoryPlayer inventory = player.inventory;
        boolean changed = false;
        data.beginBatch();
        for (int i = 0; i < 36; i++) {
            if (i == bagSlot) continue;
            ItemStack target = inventory.getStackInSlot(i);
            if (target == null || target.stackSize <= 0) continue;
            int max = Math.min(target.getMaxStackSize(), inventory.getInventoryStackLimit());
            if (target.stackSize >= max) continue;
            int moved = extractMatchingFromBag(data, target, max - target.stackSize);
            if (moved > 0) {
                target.stackSize += moved;
                changed = true;
            }
        }
        data.endBatch();
        if (changed) inventory.markDirty();
        return changed;
    }

    public static boolean moveMatchingBagToPlayer(EntityPlayer player, int bagSlot, HandyBagData data) {
        return moveBagToPlayer(player, bagSlot, data, true);
    }

    public static boolean moveAllBagToPlayer(EntityPlayer player, int bagSlot, HandyBagData data) {
        return moveBagToPlayer(player, bagSlot, data, false);
    }

    private static boolean movePlayerToBag(EntityPlayer player, int bagSlot, HandyBagData data, boolean matchingOnly) {
        if (!canUse(data) || player == null) return false;
        InventoryPlayer inventory = player.inventory;
        boolean changed = false;
        data.beginBatch();
        for (int i = 0; i < 36; i++) {
            if (i == bagSlot) continue;
            ItemStack source = inventory.getStackInSlot(i);
            if (source == null || source.stackSize <= 0 || source.getItem() instanceof ItemHandyBag) continue;
            if (matchingOnly && !containsMatching(data, source)) continue;
            int before = source.stackSize;
            insertIntoBag(data, source);
            if (source.stackSize != before) changed = true;
            if (source.stackSize <= 0) inventory.setInventorySlotContents(i, null);
        }
        data.endBatch();
        if (changed) inventory.markDirty();
        return changed;
    }

    private static boolean moveBagToPlayer(EntityPlayer player, int bagSlot, HandyBagData data, boolean matchingOnly) {
        if (!canUse(data) || player == null) return false;
        InventoryPlayer inventory = player.inventory;
        boolean changed = false;
        data.beginBatch();
        for (int i = 0; i < data.getSize(); i++) {
            if (data.isSlotQuickActionBlocked(i)) continue;
            ItemStack source = data.getItem(i);
            if (source == null || source.stackSize <= 0) continue;
            if (matchingOnly && !playerContainsMatching(inventory, bagSlot, source)) continue;
            int before = source.stackSize;
            insertIntoPlayer(inventory, bagSlot, source, true);
            insertIntoPlayer(inventory, bagSlot, source, false);
            if (source.stackSize != before) {
                changed = true;
                data.setItem(i, source.stackSize <= 0 ? null : source);
            }
        }
        data.endBatch();
        if (changed) inventory.markDirty();
        return changed;
    }

    private static int insertIntoBag(HandyBagData data, ItemStack source) {
        if (!canUse(data) || source == null || source.stackSize <= 0 || source.getItem() instanceof ItemHandyBag)
            return 0;
        int before = source.stackSize;
        int limit = data.getStackLimit();
        for (int i = 0; i < data.getSize() && source.stackSize > 0; i++) {
            if (data.isSlotQuickActionBlocked(i)) continue;
            ItemStack target = data.getItem(i);
            if (!stacksMatch(target, source)) continue;
            int free = limit - target.stackSize;
            if (free <= 0) continue;
            int amount = Math.min(free, source.stackSize);
            target.stackSize += amount;
            source.stackSize -= amount;
            data.setItem(i, target);
        }
        for (int i = 0; i < data.getSize() && source.stackSize > 0; i++) {
            if (data.isSlotQuickActionBlocked(i) || data.getItem(i) != null) continue;
            int amount = Math.min(limit, source.stackSize);
            ItemStack placed = source.copy();
            placed.stackSize = amount;
            data.setItem(i, placed);
            source.stackSize -= amount;
        }
        return before - source.stackSize;
    }

    private static int extractMatchingFromBag(HandyBagData data, ItemStack target, int amount) {
        int remaining = amount;
        for (int i = 0; i < data.getSize() && remaining > 0; i++) {
            if (data.isSlotQuickActionBlocked(i)) continue;
            ItemStack source = data.getItem(i);
            if (!stacksMatch(source, target)) continue;
            int take = Math.min(remaining, source.stackSize);
            source.stackSize -= take;
            remaining -= take;
            if (source.stackSize <= 0) data.setItem(i, null);
            else data.setItem(i, source);
        }
        return amount - remaining;
    }

    private static void insertIntoPlayer(InventoryPlayer inventory, int bagSlot, ItemStack source,
        boolean existingOnly) {
        for (int i = 0; i < 36 && source.stackSize > 0; i++) {
            if (i == bagSlot) continue;
            ItemStack target = inventory.getStackInSlot(i);
            if (existingOnly) {
                if (!stacksMatch(target, source)) continue;
                int max = Math.min(target.getMaxStackSize(), inventory.getInventoryStackLimit());
                int free = max - target.stackSize;
                if (free <= 0) continue;
                int amount = Math.min(free, source.stackSize);
                target.stackSize += amount;
                source.stackSize -= amount;
            } else {
                if (target != null) continue;
                int max = Math.min(source.getMaxStackSize(), inventory.getInventoryStackLimit());
                int amount = Math.min(max, source.stackSize);
                ItemStack placed = source.copy();
                placed.stackSize = amount;
                inventory.setInventorySlotContents(i, placed);
                source.stackSize -= amount;
            }
        }
    }

    private static void insertIntoInventory(IInventory inventory, int side, ItemStack source) {
        int[] slots = getAccessibleSlots(inventory, side);
        for (int slot : slots) {
            if (source.stackSize <= 0) return;
            ItemStack target = inventory.getStackInSlot(slot);
            if (!stacksMatch(target, source) || !canInsert(inventory, slot, source, side)) continue;
            int max = Math.min(target.getMaxStackSize(), inventory.getInventoryStackLimit());
            int free = max - target.stackSize;
            if (free <= 0) continue;
            int amount = Math.min(free, source.stackSize);
            target.stackSize += amount;
            source.stackSize -= amount;
            inventory.setInventorySlotContents(slot, target);
        }
        for (int slot : slots) {
            if (source.stackSize <= 0) return;
            if (inventory.getStackInSlot(slot) != null || !canInsert(inventory, slot, source, side)) continue;
            int max = Math.min(source.getMaxStackSize(), inventory.getInventoryStackLimit());
            if (max <= 0) continue;
            int amount = Math.min(max, source.stackSize);
            ItemStack placed = source.copy();
            placed.stackSize = amount;
            inventory.setInventorySlotContents(slot, placed);
            source.stackSize -= amount;
        }
    }

    private static int[] getAccessibleSlots(IInventory inventory, int side) {
        if (inventory instanceof ISidedInventory) {
            int[] slots = ((ISidedInventory) inventory).getAccessibleSlotsFromSide(side);
            return slots == null ? new int[0] : slots;
        }
        int[] slots = new int[inventory.getSizeInventory()];
        for (int i = 0; i < slots.length; i++) slots[i] = i;
        return slots;
    }

    private static boolean canInsert(IInventory inventory, int slot, ItemStack stack, int side) {
        if (!inventory.isItemValidForSlot(slot, stack)) return false;
        return !(inventory instanceof ISidedInventory)
            || ((ISidedInventory) inventory).canInsertItem(slot, stack, side);
    }

    private static boolean canExtract(IInventory inventory, int slot, ItemStack stack, int side) {
        return !(inventory instanceof ISidedInventory)
            || ((ISidedInventory) inventory).canExtractItem(slot, stack, side);
    }

    private static boolean hasEarlierMatch(InventoryPlayer inventory, int bagSlot, ItemStack stack, int beforeSlot) {
        for (int i = 0; i < beforeSlot; i++) {
            if (i == bagSlot) continue;
            if (stacksMatch(inventory.getStackInSlot(i), stack)) return true;
        }
        return false;
    }

    private static int countMatching(InventoryPlayer inventory, int bagSlot, ItemStack stack) {
        int total = 0;
        for (int i = 0; i < 36; i++) {
            if (i == bagSlot) continue;
            ItemStack current = inventory.getStackInSlot(i);
            if (stacksMatch(current, stack)) total += current.stackSize;
        }
        return total;
    }

    private static boolean playerContainsMatching(InventoryPlayer inventory, int bagSlot, ItemStack stack) {
        for (int i = 0; i < 36; i++) {
            if (i == bagSlot) continue;
            if (stacksMatch(inventory.getStackInSlot(i), stack)) return true;
        }
        return false;
    }

    private static boolean canUse(HandyBagData data) {
        return data != null && data.getSelectedCardStack() != null && data.getStackLimit() > 0;
    }

    public static boolean stacksMatch(ItemStack a, ItemStack b) {
        return a != null && b != null
            && a.getItem() == b.getItem()
            && a.getItemDamage() == b.getItemDamage()
            && ItemStack.areItemStackTagsEqual(a, b);
    }
}
