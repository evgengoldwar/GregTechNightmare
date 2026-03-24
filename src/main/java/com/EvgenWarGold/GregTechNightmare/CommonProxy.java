package com.EvgenWarGold.GregTechNightmare;

import com.EvgenWarGold.GregTechNightmare.GregTech.Api.Pipe.NetworkDispatcherPipe;
import com.EvgenWarGold.GregTechNightmare.GregTech.Blocks.GTN_BlocksRegister;
import com.EvgenWarGold.GregTechNightmare.GregTech.Items.GTN_ItemsRegister;
import com.EvgenWarGold.GregTechNightmare.GregTech.MachineLoader;
import com.EvgenWarGold.GregTechNightmare.GregTech.Recipe.RecipeLoader;
import com.EvgenWarGold.GregTechNightmare.GregTech.Recipe.RecipeResult.RecipeResultRegisters;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

public class CommonProxy {

    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());
        GregTechNightmare.LOG.info(GregTechNightmare.MOD_NAME + " at version " + Tags.VERSION);

        GTN_ItemsRegister.init();
        GTN_BlocksRegister.init();
    }

    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {
        MachineLoader.init();
        RecipeResultRegisters.init();
        NetworkDispatcherPipe.registerPackets();
    }

    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {}

    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {}

    public void complete(FMLLoadCompleteEvent event) {
        RecipeLoader.init();
    }
}
