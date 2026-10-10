package com.EvgenWarGold.GregTechNightmare;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;

import com.EvgenWarGold.GregTechNightmare.Event.TooltipAdditionDisplayEvent;
import com.EvgenWarGold.GregTechNightmare.Event.WelcomeMessageEvent;
import com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag.ContainerHandyBag;
import com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag.client.GuiHandyBag;
import com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag.client.HandyBagClientEvents;
import com.EvgenWarGold.GregTechNightmare.GregTech.Items.ItemStructuresLinkTool;
import com.EvgenWarGold.GregTechNightmare.Tooltips.TooltipsLoader;
import com.EvgenWarGold.GregTechNightmare.Utils.BlockHighlighter;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ClientProxy extends CommonProxy {

    @SideOnly(Side.CLIENT)
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        MinecraftForge.EVENT_BUS.register(new BlockHighlighter.EventHandler());
        MinecraftForge.EVENT_BUS.register(new ItemStructuresLinkTool());
        TooltipsLoader.init();
        HandyBagClientEvents.register();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);

        FMLCommonHandler.instance()
            .bus()
            .register(new WelcomeMessageEvent());
        FMLCommonHandler.instance()
            .bus()
            .register(new TooltipAdditionDisplayEvent());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public EntityPlayer getPlayerFromMessageContext(MessageContext context) {
        if (context.side == Side.CLIENT) return Minecraft.getMinecraft().thePlayer;
        return super.getPlayerFromMessageContext(context);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Object getHandyBagClientGui(EntityPlayer player, int slot) {
        return new GuiHandyBag(new ContainerHandyBag(player, slot));
    }
}
