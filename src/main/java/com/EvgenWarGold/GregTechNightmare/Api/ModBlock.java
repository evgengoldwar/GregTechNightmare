package com.EvgenWarGold.GregTechNightmare.Api;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import org.jetbrains.annotations.NotNull;

import com.EvgenWarGold.GregTechNightmare.GregTech.GTN_ItemList;
import com.EvgenWarGold.GregTechNightmare.Utils.GTN_Utils;

import gregtech.api.enums.Mods;
import gregtech.api.util.GTModHandler;

public class ModBlock {

    protected final String modID;
    protected final String unlocalizedName;
    protected final String localizedName;
    protected final int meta;
    private Block cachedBlock;
    private ItemStack cachedItemStack;

    public ModBlock(@NotNull String modID, @NotNull String unlocalizedName, @NotNull String localizedName, int meta) {
        this.modID = modID;
        this.unlocalizedName = unlocalizedName;
        this.localizedName = localizedName;
        this.meta = meta;
    }

    public ModBlock(@NotNull Mods mod, @NotNull String unlocalizedName, @NotNull String localizedName, int meta) {
        this(mod.ID, unlocalizedName, localizedName, meta);
    }

    public ModBlock(@NotNull Mods mod, @NotNull String unlocalizedName, @NotNull String localizedName) {
        this(mod.ID, unlocalizedName, localizedName, 0);
    }

    public Block get() {
        if (cachedBlock == null) {
            cachedBlock = createBlock();
        }
        return cachedBlock;
    }

    public ItemStack getItemStack(int count) {
        if (cachedItemStack == null) {
            cachedItemStack = createItemStack();
        }
        return GTN_Utils.copyAmount(count, cachedItemStack);
    }

    private Block createBlock() {
        ItemStack stack = GTModHandler.getModItem(modID, unlocalizedName, 1, meta);
        if (stack != null && stack.getItem() != null) {
            Block block = Block.getBlockFromItem(stack.getItem());
            if (block != null) {
                cachedItemStack = stack;
                return block;
            }
        }

        Block fallbackBlock = Block.getBlockFromItem(
            GTN_ItemList.TestCasing.get(1)
                .getItem());
        if (fallbackBlock != null) {
            String displayName = EnumChatFormatting.WHITE + modID + " : " + localizedName;
            ItemStack fallbackStack = new ItemStack(fallbackBlock, 1, meta);
            fallbackStack.setStackDisplayName(EnumChatFormatting.RESET + displayName);
            cachedItemStack = fallbackStack;
            return fallbackBlock;
        }
        return null;
    }

    private ItemStack createItemStack() {
        ItemStack stack = GTModHandler.getModItem(modID, unlocalizedName, 1, meta);
        if (stack == null || stack.getItem() == null) {
            stack = GTN_ItemList.TestItem.get(1);
            String displayName = EnumChatFormatting.WHITE + modID + " : " + localizedName;
            stack.setStackDisplayName(EnumChatFormatting.RESET + displayName);
        }
        return stack;
    }
}
