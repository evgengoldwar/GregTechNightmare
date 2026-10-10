package com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;

import com.EvgenWarGold.GregTechNightmare.GregTech.Items.ItemHandyBag;
import com.EvgenWarGold.GregTechNightmare.GregTech.Items.ItemMemoryCard;

public class CommandHandyBagOwner extends CommandBase {

    @Override
    public String getCommandName() {
        return "handybagowner";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/handybagowner <player|self|clear>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (!(sender instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) sender;
        if (!HandyBagAdmin.canAdminister(player)) {
            player.addChatMessage(new ChatComponentTranslation("gtn.handybag.hotkey.admin.denied"));
            return;
        }
        ItemStack bag = player.getCurrentEquippedItem();
        if (bag == null || !(bag.getItem() instanceof ItemHandyBag)) {
            player.addChatMessage(new ChatComponentTranslation("gtn.handybag.admin.command.no_bag"));
            return;
        }
        HandyBagData data = new HandyBagData(bag, player);
        ItemStack card = data.getSelectedCardStack();
        if (card == null) {
            player.addChatMessage(new ChatComponentTranslation("gtn.handybag.hotkey.privacy.no_card"));
            return;
        }
        if (args.length != 1) {
            player.addChatMessage(new ChatComponentTranslation("gtn.handybag.admin.command.usage"));
            return;
        }
        if ("clear".equalsIgnoreCase(args[0])) {
            ItemMemoryCard.clearOwner(card);
            data.flush();
            player.addChatMessage(new ChatComponentTranslation("gtn.handybag.hotkey.admin.owner_clear"));
            return;
        }
        EntityPlayer target = "self".equalsIgnoreCase(args[0]) ? player : findOnlinePlayer(args[0]);
        if (target == null) {
            player.addChatMessage(new ChatComponentTranslation("gtn.handybag.admin.command.player_not_found", args[0]));
            return;
        }
        ItemMemoryCard.setOwner(card, target);
        data.flush();
        player.addChatMessage(
            new ChatComponentTranslation("gtn.handybag.hotkey.admin.owner_self", target.getCommandSenderName()));
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length != 1) return null;
        List<String> values = new ArrayList<String>();
        values.add("self");
        values.add("clear");
        String[] names = MinecraftServer.getServer()
            .getAllUsernames();
        if (names != null) {
            for (String name : names) values.add(name);
        }
        return getListOfStringsFromIterableMatchingLastWord(args, values);
    }

    private static EntityPlayerMP findOnlinePlayer(String name) {
        MinecraftServer server = MinecraftServer.getServer();
        if (server == null || server.getConfigurationManager() == null) return null;
        for (Object object : server.getConfigurationManager().playerEntityList) {
            if (!(object instanceof EntityPlayerMP)) continue;
            EntityPlayerMP player = (EntityPlayerMP) object;
            if (player.getCommandSenderName()
                .equalsIgnoreCase(name)) return player;
        }
        return null;
    }
}
