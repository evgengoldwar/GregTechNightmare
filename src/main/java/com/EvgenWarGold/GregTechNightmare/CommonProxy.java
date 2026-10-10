package com.EvgenWarGold.GregTechNightmare;

import net.minecraft.entity.player.EntityPlayer;

import com.EvgenWarGold.GregTechNightmare.DeleteRecipe.DeleteRecipe;
import com.EvgenWarGold.GregTechNightmare.GregTech.Blocks.GTN_BlocksRegister;
import com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag.CommandHandyBagOwner;
import com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag.HandyBagAdmin;
import com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag.HandyBagGuiHandler;
import com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag.HandyBagServerEvents;
import com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag.network.HandyBagNetwork;
import com.EvgenWarGold.GregTechNightmare.GregTech.Hatch.GTN_WildcardPatternBuffer;
import com.EvgenWarGold.GregTechNightmare.GregTech.Items.GTN_ItemsRegister;
import com.EvgenWarGold.GregTechNightmare.GregTech.MachineLoader;
import com.EvgenWarGold.GregTechNightmare.GregTech.Recipe.RecipeLoader;
import com.EvgenWarGold.GregTechNightmare.GregTech.Recipe.RecipeResult.RecipeResultRegisters;

import appeng.api.AEApi;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import gregtech.api.enums.Mods;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        GTN_ItemsRegister.init();
        GTN_BlocksRegister.init();
        HandyBagNetwork.init();
        HandyBagServerEvents.register();
        NetworkRegistry.INSTANCE.registerGuiHandler(GregTechNightmare.instance, new HandyBagGuiHandler());
        AEApi.instance()
            .registries()
            .interfaceTerminal()
            .register(GTN_WildcardPatternBuffer.class);
    }

    public void init(FMLInitializationEvent event) {
        MachineLoader.init();
        RecipeResultRegisters.init();
        HandyBagAdmin.registerPermission();
    }

    public void postInit(FMLPostInitializationEvent event) {
        if (Mods.BetterQuesting.isModLoaded()) {
            Quests.init();
        }

        DeleteRecipe.init();
    }

    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandHandyBagOwner());
    }

    public void complete(FMLLoadCompleteEvent event) {
        RecipeLoader.init();
    }

    public EntityPlayer getPlayerFromMessageContext(MessageContext context) {
        if (context.side == Side.SERVER) return context.getServerHandler().playerEntity;
        return null;
    }

    public Object getHandyBagClientGui(EntityPlayer player, int slot) {
        return null;
    }
}
