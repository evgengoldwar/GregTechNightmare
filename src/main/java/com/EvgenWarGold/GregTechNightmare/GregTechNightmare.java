package com.EvgenWarGold.GregTechNightmare;

import java.io.File;
import java.net.URL;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

@Mod(
    modid = GregTechNightmare.MOD_ID,
    version = Tags.VERSION,
    name = GregTechNightmare.MOD_NAME,
    dependencies = "required-after:gregtech;" + "required-after:bartworks;" + "required-after:gtnhintergalactic;",
    acceptedMinecraftVersions = "[1.7.10]")
public class GregTechNightmare {

    public static final String MOD_ID = "GregTechNightmare";
    public static final String MOD_NAME = "GregTechNightmare";
    public static final String RESOURCE_ROOT_ID = "gregtechnightmare";

    @Mod.Instance(MOD_ID)
    public static GregTechNightmare instance;
    public static File CONFIG_DIR;
    public static URL RESOURCE_URL;

    @SidedProxy(
        clientSide = "com.EvgenWarGold.GregTechNightmare.ClientProxy",
        serverSide = "com.EvgenWarGold.GregTechNightmare.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        RESOURCE_URL = getClass().getResource("/assets/gregtechnightmare/Quests");
        CONFIG_DIR = event.getModConfigurationDirectory();
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting(event);
    }

    @Mod.EventHandler
    public void completeInit(FMLLoadCompleteEvent event) {
        proxy.complete(event);
    }
}
