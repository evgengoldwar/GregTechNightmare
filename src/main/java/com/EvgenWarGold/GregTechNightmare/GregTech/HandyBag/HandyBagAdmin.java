package com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag;

import net.minecraft.entity.player.EntityPlayer;

import cpw.mods.fml.common.Loader;
import serverutils.lib.util.permission.DefaultPermissionLevel;
import serverutils.lib.util.permission.PermissionAPI;

public final class HandyBagAdmin {

    public static final String PERMISSION = "gregtechnightmare.handybag.admin";

    private HandyBagAdmin() {}

    public static void registerPermission() {
        if (!Loader.isModLoaded("serverutilities")) return;
        PermissionAPI.registerNode(
            PERMISSION,
            DefaultPermissionLevel.NONE,
            "Allows administrative access to private Handy Bag Memory Cards");
    }

    public static boolean canAdminister(EntityPlayer player) {
        if (player == null) return false;
        if (player.canCommandSenderUseCommand(4, "")) return true;
        return Loader.isModLoaded("serverutilities") && PermissionAPI.hasPermission(player, PERMISSION);
    }
}
