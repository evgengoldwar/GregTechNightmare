package com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag.network;

import net.minecraft.entity.player.EntityPlayer;

import com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag.ContainerHandyBag;
import com.EvgenWarGold.GregTechNightmare.GregTech.Items.ItemHandyBag;
import com.EvgenWarGold.GregTechNightmare.GregTechNightmare;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class MessageSetBagMode implements IMessage {

    public static final int MODE_PICKUP = 0;
    public static final int MODE_RESTOCK = 1;
    public static final int MODE_LOCK = 2;
    public static final int MODE_BLOCK_SECTION = 3;
    private int mode;
    private int value;

    public MessageSetBagMode() {}

    public MessageSetBagMode(int mode, int value) {
        this.mode = mode;
        this.value = value;
    }

    public static int packSection(int section, boolean enabled) {
        return (section & 0x03) | (enabled ? 0x10 : 0);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        mode = buf.readByte();
        value = buf.readByte();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(mode);
        buf.writeByte(value);
    }

    public static class Handler implements IMessageHandler<MessageSetBagMode, IMessage> {

        @Override
        public IMessage onMessage(MessageSetBagMode message, MessageContext context) {
            EntityPlayer player = GregTechNightmare.proxy.getPlayerFromMessageContext(context);
            if (player == null || !(player.openContainer instanceof ContainerHandyBag)) return null;
            ContainerHandyBag container = (ContainerHandyBag) player.openContainer;
            if (message.mode == MODE_PICKUP && message.value >= ItemHandyBag.PICKUP_OFF
                && message.value <= ItemHandyBag.PICKUP_ALL) {
                container.setPickupMode(message.value);
            } else if (message.mode == MODE_RESTOCK && (message.value == 0 || message.value == 1)) {
                container.setRestockEnabled(message.value != 0);
            } else if (message.mode == MODE_LOCK && (message.value == 0 || message.value == 1)) {
                container.setLocked(message.value != 0);
            } else if (message.mode == MODE_BLOCK_SECTION) {
                int section = message.value & 0x03;
                if (section < container.getSectionCount()) {
                    container.setSectionQuickActionBlocked(section, (message.value & 0x10) != 0);
                }
            }
            return null;
        }
    }
}
