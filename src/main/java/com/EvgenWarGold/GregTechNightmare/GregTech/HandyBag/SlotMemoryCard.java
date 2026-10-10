package com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag;

import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.EvgenWarGold.GregTechNightmare.GregTech.Items.ItemMemoryCard;

public class SlotMemoryCard extends Slot {

    public SlotMemoryCard(InventoryHandyBagCards inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return stack != null && stack.getItem() instanceof ItemMemoryCard;
    }

    @Override
    public int getSlotStackLimit() {
        return 1;
    }
}
