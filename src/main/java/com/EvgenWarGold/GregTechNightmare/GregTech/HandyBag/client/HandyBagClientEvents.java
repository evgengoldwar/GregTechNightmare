package com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.input.Keyboard;

import com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag.network.HandyBagNetwork;
import com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag.network.MessageBagKeyAction;
import com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag.network.MessageOpenHandyBag;
import com.EvgenWarGold.GregTechNightmare.GregTech.Items.ItemHandyBag;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class HandyBagClientEvents {

    private static final KeyBinding TOGGLE_MODE = new KeyBinding(
        "key.gtn.handybag.toggle_mode",
        Keyboard.KEY_F,
        "key.categories.gregtechnightmare");
    private static boolean registered;

    public static void register() {
        if (!registered) {
            registered = true;
            HandyBagClientEvents handler = new HandyBagClientEvents();
            MinecraftForge.EVENT_BUS.register(handler);
            FMLCommonHandler.instance()
                .bus()
                .register(handler);
            ClientRegistry.registerKeyBinding(TOGGLE_MODE);
        }
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.gui == null || event.gui.getClass() != GuiInventory.class) return;
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player == null || player.isSneaking()) return;
        int slot = ItemHandyBag.findOpenableBagSlot(player);
        if (slot < 0) return;
        event.setCanceled(true);
        HandyBagNetwork.CHANNEL.sendToServer(new MessageOpenHandyBag(slot));
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.thePlayer == null || minecraft.currentScreen != null) return;
        if (!Keyboard.getEventKeyState() || Keyboard.getEventKey() != TOGGLE_MODE.getKeyCode()) return;
        ItemStack held = minecraft.thePlayer.getCurrentEquippedItem();
        if (held == null || !(held.getItem() instanceof ItemHandyBag)) return;

        int modifiers = 0;
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            modifiers |= MessageBagKeyAction.MOD_SHIFT;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
            modifiers |= MessageBagKeyAction.MOD_CONTROL;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU)) {
            modifiers |= MessageBagKeyAction.MOD_ALT;
        }
        HandyBagNetwork.CHANNEL.sendToServer(new MessageBagKeyAction(modifiers));
    }
}
