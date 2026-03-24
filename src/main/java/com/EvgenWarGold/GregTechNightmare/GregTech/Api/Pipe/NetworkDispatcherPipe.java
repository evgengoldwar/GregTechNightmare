package com.EvgenWarGold.GregTechNightmare.GregTech.Api.Pipe;

import com.EvgenWarGold.GregTechNightmare.GregTechNightmare;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class NetworkDispatcherPipe {

    public static final SimpleNetworkWrapper INSTANCE = new SimpleNetworkWrapper(GregTechNightmare.MOD_ID);

    public static void registerPackets() {
        INSTANCE.registerMessage(
            PipeActivityMessage.ServerHandler.class,
            PipeActivityMessage.PipeActivityQuery.class,
            0,
            Side.SERVER);
        INSTANCE.registerMessage(
            PipeActivityMessage.ClientHandler.class,
            PipeActivityMessage.PipeActivityData.class,
            1,
            Side.CLIENT);


        PipeActivity.init();
        INSTANCE.registerMessage(PipeActivity.Handler.class, BatchedPipeActivityMessage.class, 3, Side.CLIENT);
    }
}
