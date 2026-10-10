package com.EvgenWarGold.GregTechNightmare.GregTech.Items;

import static com.EvgenWarGold.GregTechNightmare.Utils.GTN_Utils.tr;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag.HandyBagAdmin;
import com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag.HandyBagData;
import com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag.HandyBagGuiHandler;
import com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag.HandyBagOperations;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Processing.LV.GTN_ItemCrate;
import com.EvgenWarGold.GregTechNightmare.GregTechNightmare;
import com.EvgenWarGold.GregTechNightmare.Utils.Constants;

import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemHandyBag extends Item {

    public static final int REGULAR_SIZE = 27;
    public static final int LARGE_SIZE = 55;
    public static final int PICKUP_OFF = 0;
    public static final int PICKUP_MATCHING = 1;
    public static final int PICKUP_ALL = 2;
    private static final String ROOT = "GTNHandyBag";
    private static final String PICKUP_MODE = "PickupMode";
    private static final String RESTOCK_MODE = "RestockMode";
    private static final String LOCKED_MODE = "DisableOpen";
    private final IIcon[] icons = new IIcon[2];

    public ItemHandyBag() {
        setUnlocalizedName("GTN_HandyBag");
        setHasSubtypes(true);
        setMaxDamage(0);
        setMaxStackSize(1);
        setCreativeTab(CreativeTabs.tabTools);
    }

    public static int getBagSize(ItemStack stack) {
        return stack != null && stack.getItemDamage() == 1 ? LARGE_SIZE : REGULAR_SIZE;
    }

    public static int getPickupMode(ItemStack stack) {
        NBTTagCompound root = getRoot(stack, false);
        if (root == null) return PICKUP_OFF;
        return Math.max(PICKUP_OFF, Math.min(PICKUP_ALL, root.getInteger(PICKUP_MODE)));
    }

    public static void setPickupMode(ItemStack stack, int mode) {
        NBTTagCompound root = getRoot(stack, true);
        if (root != null) root.setInteger(PICKUP_MODE, Math.max(PICKUP_OFF, Math.min(PICKUP_ALL, mode)));
    }

    public static boolean isRestockEnabled(ItemStack stack) {
        NBTTagCompound root = getRoot(stack, false);
        return root != null && root.getBoolean(RESTOCK_MODE);
    }

    public static void setRestockEnabled(ItemStack stack, boolean enabled) {
        NBTTagCompound root = getRoot(stack, true);
        if (root != null) root.setBoolean(RESTOCK_MODE, enabled);
    }

    public static boolean isLocked(ItemStack stack) {
        NBTTagCompound root = getRoot(stack, false);
        return root != null && root.getBoolean(LOCKED_MODE);
    }

    public static void setLocked(ItemStack stack, boolean locked) {
        NBTTagCompound root = getRoot(stack, true);
        if (root != null) root.setBoolean(LOCKED_MODE, locked);
    }

    public static int findOpenableBagSlot(EntityPlayer player) {
        if (player == null || player.inventory == null) return -1;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() instanceof ItemHandyBag && !isLocked(stack)) return i;
        }
        return -1;
    }

    public static ItemStack getBagInSlot(EntityPlayer player, int slot) {
        if (player == null || slot < 0 || slot >= 36) return null;
        ItemStack stack = player.inventory.getStackInSlot(slot);
        return stack != null && stack.getItem() instanceof ItemHandyBag ? stack : null;
    }

    private static NBTTagCompound getRoot(ItemStack stack, boolean create) {
        if (stack == null) return null;
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null && create) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        if (tag == null) return null;
        if (!tag.hasKey(ROOT) && create) tag.setTag(ROOT, new NBTTagCompound());
        return tag.hasKey(ROOT) ? tag.getCompoundTag(ROOT) : null;
    }

    @Override
    public boolean onItemUse(
        ItemStack stack,
        EntityPlayer player,
        World world,
        int x,
        int y,
        int z,
        int side,
        float hitX,
        float hitY,
        float hitZ) {
        if (!player.isSneaking()) return false;
        TileEntity tile = world.getTileEntity(x, y, z);
        return tryTileTransfer(stack, player, world, tile, side);
    }

    public static boolean tryTileTransfer(
        ItemStack stack,
        EntityPlayer player,
        World world,
        TileEntity tile,
        int side) {
        if (tile instanceof IGregTechTileEntity gte && gte.getMetaTileEntity() instanceof GTN_ItemCrate crate) {
            IInventory storage = crate.getHandyBagInventory();
            return storage != null && tryInventoryTransfer(stack, player, world, storage, side);
        }
        return tile instanceof IInventory
            && tryInventoryTransfer(stack, player, world, (IInventory) tile, side);
    }

    public static boolean tryInventoryTransfer(
        ItemStack stack,
        EntityPlayer player,
        World world,
        IInventory inventory,
        int side) {
        if (stack == null || player == null || world == null || inventory == null) return false;
        boolean dump = isRestockEnabled(stack);
        int pickupMode = getPickupMode(stack);
        if (!dump && pickupMode == PICKUP_OFF) return false;
        if (world.isRemote) return true;

        HandyBagData data = new HandyBagData(stack, player);
        if (data.getSelectedCardStack() == null || data.getStackLimit() <= 0) return false;
        boolean moved = dump
            ? HandyBagOperations.moveBagToInventory(data, inventory, side)
            : HandyBagOperations.moveInventoryToBag(data, inventory, side, pickupMode == PICKUP_MATCHING);
        if (moved) world.playSoundAtEntity(player, "mob.endermen.portal", 0.2F, 1.8F);
        return true;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            int slot = player.inventory.currentItem;
            player.openGui(GregTechNightmare.instance, HandyBagGuiHandler.GUI_ID, world, slot, 0, 0);
        }
        return stack;
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return super.getUnlocalizedName(stack) + "." + (stack.getItemDamage() == 1 ? 1 : 0);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        icons[0] = register.registerIcon(GregTechNightmare.RESOURCE_ROOT_ID + ":handybag.0");
        icons[1] = register.registerIcon(GregTechNightmare.RESOURCE_ROOT_ID + ":handybag.1");
        itemIcon = icons[0];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int meta) {
        return icons[meta == 1 ? 1 : 0];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> list) {
        list.add(new ItemStack(item, 1, 0));
        list.add(new ItemStack(item, 1, 1));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean advanced) {
        list.add(tr("gtn.handybag.slots", getBagSize(stack)));
        list.add(tr("gtn.handybag.open_hint"));
        list.add(tr("gtn.handybag.normal_inventory_hint"));
        list.add(tr("gtn.handybag.card_hint"));
        list.add(
            EnumChatFormatting.GRAY
                + tr("gtn.handybag.pickup_status", tr("gtn.handybag.pickup_mode." + getPickupMode(stack))));
        list.add(
            EnumChatFormatting.GRAY
                + tr(
                    "gtn.handybag.restock_status",
                    tr(isRestockEnabled(stack) ? "gtn.handybag.state.on" : "gtn.handybag.state.off")));
        list.add(
            EnumChatFormatting.GRAY
                + tr(
                    "gtn.handybag.lock_status",
                    tr(isLocked(stack) ? "gtn.handybag.state.on" : "gtn.handybag.state.off")));
        list.add(tr("gtn.handybag.block_transfer_hint"));
        list.add(tr("gtn.handybag.block_transfer_priority"));
        list.add(tr("gtn.handybag.middle_click_hint"));
        list.add(tr("gtn.handybag.section.buttons_hint"));
        list.add(tr("gtn.handybag.hotkey.pickup_hint"));
        list.add(tr("gtn.handybag.hotkey.lock_hint"));
        list.add(tr("gtn.handybag.hotkey.restock_hint"));
        list.add(tr("gtn.handybag.hotkey.privacy_hint"));
        list.add(tr("gtn.handybag.hotkey.card_hint"));
        if (HandyBagAdmin.canAdminister(player)) {
            list.add(EnumChatFormatting.AQUA + tr("gtn.handybag.hotkey.admin_hint"));
        }
        list.add(EnumChatFormatting.GRAY + tr("Author_Crazer"));
        list.add(EnumChatFormatting.DARK_GRAY + tr("gtn.handybag.original_credit"));
    }
}
