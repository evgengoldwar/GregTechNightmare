package com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotLockedPlayer extends Slot {

    private final int lockedIndex;

    public SlotLockedPlayer(IInventory inventory, int index, int x, int y, int lockedIndex) {
        super(inventory, index, x, y);
        this.lockedIndex = lockedIndex;
    }

    @Override
    public boolean canTakeStack(EntityPlayer player) {
        return getSlotIndex() != lockedIndex;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return getSlotIndex() != lockedIndex;
    }
}
