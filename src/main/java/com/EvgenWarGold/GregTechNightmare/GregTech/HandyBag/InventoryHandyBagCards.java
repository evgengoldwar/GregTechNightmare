package com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import com.EvgenWarGold.GregTechNightmare.GregTech.Items.ItemMemoryCard;

public class InventoryHandyBagCards implements IInventory {

    private final HandyBagData data;

    public InventoryHandyBagCards(HandyBagData data) {
        this.data = data;
    }

    @Override
    public int getSizeInventory() {
        return 4;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return data.getCard(slot);
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        return data.removeCard(slot);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        if (stack != null && stack.stackSize > 1) stack.stackSize = 1;
        data.setCard(slot, stack);
    }

    @Override
    public String getInventoryName() {
        return "container.GTN_HandyBag.cards";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
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
        return stack != null && stack.getItem() instanceof ItemMemoryCard;
    }
}
