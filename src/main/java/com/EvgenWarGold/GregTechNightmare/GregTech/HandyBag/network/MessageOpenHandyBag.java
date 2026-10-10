package com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag.network;

import net.minecraft.entity.player.EntityPlayer;

import com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag.HandyBagGuiHandler;
import com.EvgenWarGold.GregTechNightmare.GregTech.Items.ItemHandyBag;
import com.EvgenWarGold.GregTechNightmare.GregTechNightmare;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class MessageOpenHandyBag implements IMessage {

    private int slot;

    public MessageOpenHandyBag() {}

    public MessageOpenHandyBag(int slot) {
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

    public static class Handler implements IMessageHandler<MessageOpenHandyBag, IMessage> {

        @Override
        public IMessage onMessage(MessageOpenHandyBag message, MessageContext context) {
            EntityPlayer player = GregTechNightmare.proxy.getPlayerFromMessageContext(context);
            if (player != null && ItemHandyBag.getBagInSlot(player, message.slot) != null) {
                player.openGui(
                    GregTechNightmare.instance,
                    HandyBagGuiHandler.GUI_ID,
                    player.worldObj,
                    message.slot,
                    0,
                    0);
            }
            return null;
        }
    }
}
