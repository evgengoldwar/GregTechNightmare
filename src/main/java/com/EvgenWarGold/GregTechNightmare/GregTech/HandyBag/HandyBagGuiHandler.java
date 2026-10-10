package com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.EvgenWarGold.GregTechNightmare.GregTech.Items.ItemHandyBag;
import com.EvgenWarGold.GregTechNightmare.GregTechNightmare;

import cpw.mods.fml.common.network.IGuiHandler;

public class HandyBagGuiHandler implements IGuiHandler {

    public static final int GUI_ID = 1730;

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id != GUI_ID) return null;
        ItemStack stack = ItemHandyBag.getBagInSlot(player, x);
        if (stack == null) return null;
        return new ContainerHandyBag(player, x);
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id != GUI_ID) return null;
        ItemStack stack = ItemHandyBag.getBagInSlot(player, x);
        if (stack == null) return null;
        return GregTechNightmare.proxy.getHandyBagClientGui(player, x);
    }
}
