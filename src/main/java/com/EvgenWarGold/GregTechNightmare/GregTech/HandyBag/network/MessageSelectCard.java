package com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag.network;

import net.minecraft.entity.player.EntityPlayer;

import com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag.ContainerHandyBag;
import com.EvgenWarGold.GregTechNightmare.GregTechNightmare;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class MessageSelectCard implements IMessage {

    private int slot;

    public MessageSelectCard() {}

    public MessageSelectCard(int slot) {
        this.slot = slot;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        slot = buf.readByte();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(slot);
    }

    public static class Handler implements IMessageHandler<MessageSelectCard, IMessage> {

        @Override
        public IMessage onMessage(MessageSelectCard message, MessageContext context) {
            EntityPlayer player = GregTechNightmare.proxy.getPlayerFromMessageContext(context);
            if (player != null && player.openContainer instanceof ContainerHandyBag) {
                ((ContainerHandyBag) player.openContainer).selectCard(message.slot);
            }
            return null;
        }
    }
}
