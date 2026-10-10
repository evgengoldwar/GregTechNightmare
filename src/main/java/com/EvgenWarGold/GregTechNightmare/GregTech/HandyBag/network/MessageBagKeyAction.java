package com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;

import com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag.HandyBagAdmin;
import com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag.HandyBagData;
import com.EvgenWarGold.GregTechNightmare.GregTech.Items.ItemHandyBag;
import com.EvgenWarGold.GregTechNightmare.GregTech.Items.ItemMemoryCard;
import com.EvgenWarGold.GregTechNightmare.GregTechNightmare;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class MessageBagKeyAction implements IMessage {

    public static final int MOD_SHIFT = 1;
    public static final int MOD_CONTROL = 2;
    public static final int MOD_ALT = 4;
    private int modifiers;

    public MessageBagKeyAction() {}

    public MessageBagKeyAction(int modifiers) {
        this.modifiers = modifiers;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        modifiers = buf.readUnsignedByte();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(modifiers);
    }

    public static class Handler implements IMessageHandler<MessageBagKeyAction, IMessage> {

        @Override
        public IMessage onMessage(MessageBagKeyAction message, MessageContext context) {
            EntityPlayer player = GregTechNightmare.proxy.getPlayerFromMessageContext(context);
            if (player == null || player.inventory == null) return null;
            ItemStack bag = player.getCurrentEquippedItem();
            if (bag == null || !(bag.getItem() instanceof ItemHandyBag)) return null;

            boolean shift = (message.modifiers & MOD_SHIFT) != 0;
            boolean control = (message.modifiers & MOD_CONTROL) != 0;
            boolean alt = (message.modifiers & MOD_ALT) != 0;

            if (control && alt) {
                if (!HandyBagAdmin.canAdminister(player)) {
                    player.addChatMessage(new ChatComponentTranslation("gtn.handybag.hotkey.admin.denied"));
                    return null;
                }
                HandyBagData data = new HandyBagData(bag, player);
                ItemStack card = data.getSelectedCardStack();
                if (card == null) {
                    player.addChatMessage(new ChatComponentTranslation("gtn.handybag.hotkey.privacy.no_card"));
                    return null;
                }
                if (shift) {
                    ItemMemoryCard.clearOwner(card);
                    data.flush();
                    player.addChatMessage(new ChatComponentTranslation("gtn.handybag.hotkey.admin.owner_clear"));
                } else {
                    ItemMemoryCard.setOwner(card, player);
                    data.flush();
                    player.addChatMessage(
                        new ChatComponentTranslation(
                            "gtn.handybag.hotkey.admin.owner_self",
                            player.getCommandSenderName()));
                }
                return null;
            }

            if (control) {
                HandyBagData data = new HandyBagData(bag, player);
                if (data.selectNextCard(shift)) {
                    player.inventory.markDirty();
                    player.addChatMessage(
                        new ChatComponentTranslation("gtn.handybag.hotkey.card", data.getSelectedCard() + 1));
                }
                return null;
            }

            if (alt && shift) {
                boolean enabled = !ItemHandyBag.isRestockEnabled(bag);
                ItemHandyBag.setRestockEnabled(bag, enabled);
                player.inventory.markDirty();
                player.addChatMessage(
                    new ChatComponentTranslation(
                        "gtn.handybag.hotkey.restock",
                        new ChatComponentTranslation(enabled ? "gtn.handybag.state.on" : "gtn.handybag.state.off")));
                return null;
            }

            if (alt) {
                HandyBagData data = new HandyBagData(bag, player);
                ItemStack card = data.getSelectedCardStack();
                int result = data.toggleSelectedCardPrivacy();
                if (result == ItemMemoryCard.ACCESS_PRIVATE) {
                    player.addChatMessage(new ChatComponentTranslation("gtn.handybag.hotkey.privacy.private"));
                } else if (result == ItemMemoryCard.ACCESS_PUBLIC) {
                    player.addChatMessage(new ChatComponentTranslation("gtn.handybag.hotkey.privacy.public"));
                } else if (result == ItemMemoryCard.ACCESS_DENIED) {
                    player.addChatMessage(
                        new ChatComponentTranslation(
                            "gtn.handybag.hotkey.privacy.denied",
                            ItemMemoryCard.getOwnerName(card)));
                } else {
                    player.addChatMessage(new ChatComponentTranslation("gtn.handybag.hotkey.privacy.no_card"));
                }
                return null;
            }

            if (shift) {
                boolean locked = !ItemHandyBag.isLocked(bag);
                ItemHandyBag.setLocked(bag, locked);
                player.inventory.markDirty();
                player.addChatMessage(
                    new ChatComponentTranslation(
                        "gtn.handybag.hotkey.lock",
                        new ChatComponentTranslation(locked ? "gtn.handybag.state.on" : "gtn.handybag.state.off")));
                return null;
            }

            int mode = (ItemHandyBag.getPickupMode(bag) + 1) % 3;
            ItemHandyBag.setPickupMode(bag, mode);
            player.inventory.markDirty();
            player.addChatMessage(
                new ChatComponentTranslation(
                    "gtn.handybag.hotkey.pickup",
                    new ChatComponentTranslation("gtn.handybag.pickup_mode." + mode)));
            return null;
        }
    }
}
