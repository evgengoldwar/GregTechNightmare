package com.EvgenWarGold.GregTechNightmare.Event;

import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import com.EvgenWarGold.GregTechNightmare.Utils.Authors;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;

public class WelcomeMessageEvent {

    private static final String SEPARATOR = "=================================================";

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        String playerName = event.player.getDisplayName();

        sendSeparator(event);
        event.player.addChatMessage(
            translatedLine("      ", EnumChatFormatting.DARK_RED, "GTN.Welcome.header", Authors.EVGEN_WAR_GOLD.name));
        sendSeparator(event);
        event.player.addChatMessage(translatedLine("  ", EnumChatFormatting.YELLOW, "GTN.Welcome.description.0"));
        event.player.addChatMessage(translatedLine("  ", EnumChatFormatting.YELLOW, "GTN.Welcome.description.1"));
        event.player.addChatMessage(translatedLine("  ", EnumChatFormatting.YELLOW, "GTN.Welcome.description.2"));
        sendSeparator(event);
        event.player.addChatMessage(translatedLine("  ", EnumChatFormatting.GREEN, "GTN.Welcome.greeting", playerName));
        sendSeparator(event);

        ChatComponentText githubMessage = translatedLine("  ", EnumChatFormatting.WHITE, "GTN.Welcome.github.label");
        ChatComponentTranslation githubLink = new ChatComponentTranslation("GTN.Welcome.github.open");
        ChatComponentTranslation githubHover = new ChatComponentTranslation("GTN.Welcome.github.hover");

        githubHover.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW));
        githubLink.setChatStyle(
            new ChatStyle().setColor(EnumChatFormatting.RED)
                .setChatClickEvent(
                    new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/evgengoldwar/GregTechNightmare"))
                .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, githubHover)));

        githubMessage.appendText(" ");
        githubMessage.appendSibling(githubLink);
        event.player.addChatMessage(githubMessage);
        sendSeparator(event);
    }

    private static ChatComponentText translatedLine(String prefix, EnumChatFormatting color, String translationKey,
        Object... args) {
        ChatComponentText line = new ChatComponentText(prefix);
        line.setChatStyle(new ChatStyle().setColor(color));

        ChatComponentTranslation translated = new ChatComponentTranslation(translationKey, args);
        translated.setChatStyle(new ChatStyle().setColor(color));
        line.appendSibling(translated);
        return line;
    }

    private static void sendSeparator(PlayerEvent.PlayerLoggedInEvent event) {
        ChatComponentText separator = new ChatComponentText(SEPARATOR);
        separator.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY));
        event.player.addChatMessage(separator);
    }
}
