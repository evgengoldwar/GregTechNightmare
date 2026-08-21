package com.EvgenWarGold.GregTechNightmare.GregTech.Items;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;

import com.EvgenWarGold.GregTechNightmare.GregTech.Wildcard.WildcardPrefix;
import com.EvgenWarGold.GregTechNightmare.GregTech.Wildcard.WildcardPrefixItemRenderer;
import com.EvgenWarGold.GregTechNightmare.GregTechNightmare;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public final class ItemWildcardPrefix extends Item {

    private static boolean rendererRegistered;

    private final IIcon[] icons = new IIcon[WildcardPrefix.values().length];

    public ItemWildcardPrefix() {
        setHasSubtypes(true);
        setMaxDamage(0);
        setMaxStackSize(64);
        setUnlocalizedName("GTN_WildcardPrefix");
        setCreativeTab(CreativeTabs.tabMaterials);
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        WildcardPrefix prefix = WildcardPrefix.byMeta(stack.getItemDamage());
        return prefix == null ? super.getUnlocalizedName(stack)
            : super.getUnlocalizedName(stack) + "." + prefix.getSerializedName();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        for (WildcardPrefix prefix : WildcardPrefix.values()) {
            icons[prefix.getMeta()] = register
                .registerIcon(GregTechNightmare.RESOURCE_ROOT_ID + ":wildcard/" + prefix.getSerializedName());
        }
        itemIcon = icons[WildcardPrefix.INGOT.getMeta()];

        if (!rendererRegistered) {
            WildcardPrefixItemRenderer.register(this);
            rendererRegistered = true;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int damage) {
        WildcardPrefix prefix = WildcardPrefix.byMeta(damage);
        return prefix == null ? itemIcon : icons[prefix.getMeta()];
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        for (WildcardPrefix prefix : WildcardPrefix.values()) {
            if (prefix.isFluid() || prefix.getOrePrefix() != null) {
                list.add(new ItemStack(item, 1, prefix.getMeta()));
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
        WildcardPrefix prefix = WildcardPrefix.byMeta(stack.getItemDamage());
        if (prefix == null) {
            return;
        }

        String kind = prefix.isFluid() ? StatCollector.translateToLocal("GTN.Wildcard.tooltip.fluid")
            : prefix.getOrePrefixName();
        tooltip.add(
            EnumChatFormatting.AQUA + StatCollector.translateToLocalFormatted("GTN.Wildcard.tooltip.prefix", kind));

        if (prefix.isFluid()) {
            tooltip.add(EnumChatFormatting.AQUA + StatCollector.translateToLocal("GTN.Wildcard.tooltip.fluidAmount"));
        }
        tooltip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("GTN.Wildcard.tooltip.pattern"));
        tooltip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("GTN.Wildcard.tooltip.expand"));
        tooltip.add(EnumChatFormatting.DARK_GRAY + StatCollector.translateToLocal("GTN.Wildcard.tooltip.phantom"));
        tooltip.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocal("GTN.Wildcard.tooltip.dragFromNei"));
        tooltip.add(
            EnumChatFormatting.GRAY + StatCollector
                .translateToLocalFormatted("GTN.Wildcard.tooltip.author", EnumChatFormatting.GREEN + "Crazer"));
        tooltip.add(
            EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted(
                "GTN.Wildcard.tooltip.addedBy",
                EnumChatFormatting.DARK_RED + "GregTechNightmare"));
    }
}
