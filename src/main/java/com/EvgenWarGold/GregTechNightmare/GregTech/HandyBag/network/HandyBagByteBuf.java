package com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag.network;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;

public final class HandyBagByteBuf {

    private HandyBagByteBuf() {}

    public static void writeItemStack(ByteBuf buf, ItemStack stack) {
        if (stack == null) {
            buf.writeShort(-1);
            return;
        }
        buf.writeShort(Item.getIdFromItem(stack.getItem()));
        buf.writeShort(stack.getItemDamage());
        buf.writeInt(stack.stackSize);
        ByteBufUtils.writeTag(buf, stack.getTagCompound());
    }

    public static ItemStack readItemStack(ByteBuf buf) {
        int id = buf.readShort();
        if (id < 0) return null;
        int meta = buf.readShort();
        int count = buf.readInt();
        Item item = Item.getItemById(id);
        if (item == null || count <= 0) {
            ByteBufUtils.readTag(buf);
            return null;
        }
        ItemStack stack = new ItemStack(item, count, meta);
        stack.setTagCompound(ByteBufUtils.readTag(buf));
        return stack;
    }
}
