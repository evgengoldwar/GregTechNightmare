package com.EvgenWarGold.GregTechNightmare.Api;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import com.EvgenWarGold.GregTechNightmare.GregTech.GTN_ItemList;
import com.EvgenWarGold.GregTechNightmare.Utils.GTN_Utils;

import gregtech.api.enums.Mods;
import gregtech.api.util.GTModHandler;
import org.jetbrains.annotations.NotNull;

public class ModItem {

    protected final String modID;
    protected final String unlocalizedName;
    protected final String localizedName;
    protected final int meta;
    private ItemStack cachedStack;

    public ModItem(@NotNull String modID, @NotNull String unlocalizedName, @NotNull String localizedName, int meta) {
        this.modID = modID;
        this.unlocalizedName = unlocalizedName;
        this.localizedName = localizedName;
        this.meta = meta;
    }

    public ModItem(@NotNull Mods mod, @NotNull String unlocalizedName, @NotNull String localizedName, int meta) {
        this(mod.ID, unlocalizedName, localizedName, meta);
    }

    public ModItem(@NotNull Mods mod, @NotNull String unlocalizedName, @NotNull String localizedName) {
        this(mod.ID, unlocalizedName, localizedName, 0);
    }

    public ItemStack get() {
        return get(1);
    }

    public ItemStack get(int count) {
        if (cachedStack == null) {
            cachedStack = createItemStack();
        }
        return GTN_Utils.copyAmount(count, cachedStack);
    }

    private ItemStack createItemStack() {
        ItemStack stack = GTModHandler.getModItem(modID, unlocalizedName, 1, meta);
        if (stack == null) {
            stack = GTN_ItemList.TestItem.get(1);
            String displayName = EnumChatFormatting.WHITE + modID + " : " + localizedName;
            stack.setStackDisplayName(EnumChatFormatting.RESET + displayName);
        }
        return stack;
    }
}
