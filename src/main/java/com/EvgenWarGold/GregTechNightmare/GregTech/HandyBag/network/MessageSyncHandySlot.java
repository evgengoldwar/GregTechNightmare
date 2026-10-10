package com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag.ContainerHandyBag;
import com.EvgenWarGold.GregTechNightmare.GregTechNightmare;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class MessageSyncHandySlot implements IMessage {

    private int windowId;
    private int slot;
    private ItemStack stack;

    public MessageSyncHandySlot() {}

    public MessageSyncHandySlot(int windowId, int slot, ItemStack stack) {
        this.windowId = windowId;
        this.slot = slot;
        this.stack = stack == null ? null : stack.copy();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        windowId = buf.readByte();
        slot = buf.readShort();
        stack = HandyBagByteBuf.readItemStack(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(windowId);
        buf.writeShort(slot);
        HandyBagByteBuf.writeItemStack(buf, stack);
    }

    public static class Handler implements IMessageHandler<MessageSyncHandySlot, IMessage> {

        @Override
        public IMessage onMessage(MessageSyncHandySlot message, MessageContext context) {
            EntityPlayer player = GregTechNightmare.proxy.getPlayerFromMessageContext(context);
            if (player != null && player.openContainer instanceof ContainerHandyBag
                && player.openContainer.windowId == message.windowId) {
                player.openContainer.putStackInSlot(message.slot, message.stack);
            }
            return null;
        }
    }
}
