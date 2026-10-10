package com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag.network;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public final class HandyBagNetwork {

    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel("GTNHandyBag");

    private HandyBagNetwork() {}

    public static void init() {
        CHANNEL.registerMessage(MessageOpenHandyBag.Handler.class, MessageOpenHandyBag.class, 0, Side.SERVER);
        CHANNEL.registerMessage(MessageSelectCard.Handler.class, MessageSelectCard.class, 1, Side.SERVER);
        CHANNEL.registerMessage(MessageSyncHandySlot.Handler.class, MessageSyncHandySlot.class, 2, Side.CLIENT);
        CHANNEL.registerMessage(MessageBagAction.Handler.class, MessageBagAction.class, 3, Side.SERVER);
        CHANNEL.registerMessage(MessageSetBagMode.Handler.class, MessageSetBagMode.class, 4, Side.SERVER);
        CHANNEL.registerMessage(MessageBagKeyAction.Handler.class, MessageBagKeyAction.class, 5, Side.SERVER);
    }
}
