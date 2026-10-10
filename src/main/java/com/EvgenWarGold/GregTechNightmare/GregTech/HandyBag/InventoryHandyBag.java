package com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import com.EvgenWarGold.GregTechNightmare.GregTech.Items.ItemHandyBag;

public class InventoryHandyBag implements IInventory {

    private final HandyBagData data;

    public InventoryHandyBag(HandyBagData data) {
        this.data = data;
    }

    public HandyBagData getData() {
        return data;
    }

    @Override
    public int getSizeInventory() {
        return data.getSize();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return data.getItem(slot);
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        return data.decrItem(slot, amount);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        int limit = getInventoryStackLimit();
        if (stack != null && limit <= 0) stack = null;
        if (stack != null && stack.stackSize > limit) stack.stackSize = limit;
        data.setItem(slot, stack);
    }

    @Override
    public String getInventoryName() {
        return "container.GTN_HandyBag";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return data.getStackLimit();
    }

    @Override
    public void markDirty() {
        data.flush();
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {
        data.flush();
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return stack != null && data.getSelectedCardStack() != null
            && data.canAccessSelectedCard()
            && !(stack.getItem() instanceof ItemHandyBag);
    }
}
