package com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag.network;

import net.minecraft.entity.player.EntityPlayer;

import com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag.ContainerHandyBag;
import com.EvgenWarGold.GregTechNightmare.GregTechNightmare;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class MessageBagAction implements IMessage {

    private int action;

    public MessageBagAction() {}

    public MessageBagAction(int action) {
        this.action = action;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        action = buf.readByte();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(action);
    }

    public static class Handler implements IMessageHandler<MessageBagAction, IMessage> {

        @Override
        public IMessage onMessage(MessageBagAction message, MessageContext context) {
            EntityPlayer player = GregTechNightmare.proxy.getPlayerFromMessageContext(context);
            if (player != null && player.openContainer instanceof ContainerHandyBag
                && message.action >= 0
                && message.action <= 5) {
                ((ContainerHandyBag) player.openContainer).performQuickAction(message.action);
            }
            return null;
        }
    }
}
