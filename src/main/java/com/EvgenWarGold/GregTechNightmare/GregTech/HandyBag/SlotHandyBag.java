package com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag;

import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotHandyBag extends Slot {

    private final InventoryHandyBag inventory;

    public SlotHandyBag(InventoryHandyBag inventory, int index, int x, int y) {
        super(inventory, index, x, y);
        this.inventory = inventory;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return inventory.isItemValidForSlot(getSlotIndex(), stack);
    }

    @Override
    public int getSlotStackLimit() {
        return inventory.getInventoryStackLimit();
    }
}
