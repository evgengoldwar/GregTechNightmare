package com.EvgenWarGold.GregTechNightmare.GregTech.Items;

import static com.EvgenWarGold.GregTechNightmare.Utils.GTN_Utils.tr;

import java.util.List;
import java.util.UUID;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;

import com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag.HandyBagAdmin;
import com.EvgenWarGold.GregTechNightmare.GregTechNightmare;
import com.EvgenWarGold.GregTechNightmare.Utils.Constants;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemMemoryCard extends Item {

    public static final int ACCESS_DENIED = -1;
    public static final int ACCESS_NO_CARD = -2;
    public static final int ACCESS_UNCLAIMED = 0;
    public static final int ACCESS_PRIVATE = 1;
    public static final int ACCESS_PUBLIC = 2;
    private static final int[] TIERS = { 6, 8, 10, 12 };
    private static final String PLAYER = "Player";
    private static final String UUID_MOST = "UUIDM";
    private static final String UUID_LEAST = "UUIDL";
    private static final String OWNER_NAME = "Name";
    private static final String PUBLIC = "Public";
    private final IIcon[] icons = new IIcon[TIERS.length];

    public ItemMemoryCard() {
        setUnlocalizedName("GTN_MemoryCard");
        setHasSubtypes(true);
        setMaxDamage(0);
        setMaxStackSize(1);
        setCreativeTab(CreativeTabs.tabTools);
    }

    public static int getTier(ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof ItemMemoryCard)) return -1;
        int meta = stack.getItemDamage();
        if (meta < 0 || meta >= TIERS.length) return -1;
        return TIERS[meta];
    }

    public static int getCapacity(ItemStack stack) {
        int tier = getTier(stack);
        return tier >= 0 ? 1 << tier : 0;
    }

    public static boolean hasOwner(ItemStack stack) {
        NBTTagCompound playerTag = getPlayerTag(stack, false);
        return playerTag != null && playerTag.hasKey(UUID_MOST, 4) && playerTag.hasKey(UUID_LEAST, 4);
    }

    public static boolean isOwner(ItemStack stack, EntityPlayer player) {
        if (player == null) return false;
        NBTTagCompound playerTag = getPlayerTag(stack, false);
        if (playerTag == null || !playerTag.hasKey(UUID_MOST, 4) || !playerTag.hasKey(UUID_LEAST, 4)) return false;
        UUID uuid = player.getUniqueID();
        return playerTag.getLong(UUID_MOST) == uuid.getMostSignificantBits()
            && playerTag.getLong(UUID_LEAST) == uuid.getLeastSignificantBits();
    }

    public static boolean isPublic(ItemStack stack) {
        NBTTagCompound playerTag = getPlayerTag(stack, false);
        return playerTag == null || !hasOwner(stack) || playerTag.getBoolean(PUBLIC);
    }

    public static boolean canAccess(ItemStack stack, EntityPlayer player) {
        return stack != null
            && (!hasOwner(stack) || isPublic(stack) || isOwner(stack, player) || HandyBagAdmin.canAdminister(player));
    }

    public static int getAccessState(ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof ItemMemoryCard)) return ACCESS_NO_CARD;
        if (!hasOwner(stack)) return ACCESS_UNCLAIMED;
        return isPublic(stack) ? ACCESS_PUBLIC : ACCESS_PRIVATE;
    }

    public static String getOwnerName(ItemStack stack) {
        NBTTagCompound playerTag = getPlayerTag(stack, false);
        return playerTag != null ? playerTag.getString(OWNER_NAME) : "";
    }

    public static int togglePrivacy(ItemStack stack, EntityPlayer player) {
        if (stack == null || !(stack.getItem() instanceof ItemMemoryCard) || player == null) return ACCESS_NO_CARD;
        if (!hasOwner(stack)) {
            NBTTagCompound playerTag = getPlayerTag(stack, true);
            UUID uuid = player.getUniqueID();
            playerTag.setLong(UUID_MOST, uuid.getMostSignificantBits());
            playerTag.setLong(UUID_LEAST, uuid.getLeastSignificantBits());
            playerTag.setString(OWNER_NAME, player.getCommandSenderName());
            playerTag.setBoolean(PUBLIC, false);
            return ACCESS_PRIVATE;
        }
        if (!isOwner(stack, player) && !HandyBagAdmin.canAdminister(player)) return ACCESS_DENIED;
        NBTTagCompound playerTag = getPlayerTag(stack, true);
        boolean makePublic = !playerTag.getBoolean(PUBLIC);
        playerTag.setBoolean(PUBLIC, makePublic);
        return makePublic ? ACCESS_PUBLIC : ACCESS_PRIVATE;
    }

    public static boolean setOwner(ItemStack stack, EntityPlayer owner) {
        if (stack == null || !(stack.getItem() instanceof ItemMemoryCard) || owner == null) return false;
        NBTTagCompound playerTag = getPlayerTag(stack, true);
        UUID uuid = owner.getUniqueID();
        playerTag.setLong(UUID_MOST, uuid.getMostSignificantBits());
        playerTag.setLong(UUID_LEAST, uuid.getLeastSignificantBits());
        playerTag.setString(OWNER_NAME, owner.getCommandSenderName());
        playerTag.setBoolean(PUBLIC, false);
        return true;
    }

    public static boolean clearOwner(ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof ItemMemoryCard) || !stack.hasTagCompound()) return false;
        NBTTagCompound tag = stack.getTagCompound();
        if (!tag.hasKey(PLAYER)) return false;
        tag.removeTag(PLAYER);
        return true;
    }

    private static NBTTagCompound getPlayerTag(ItemStack stack, boolean create) {
        if (stack == null) return null;
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null && create) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        if (tag == null) return null;
        if (!tag.hasKey(PLAYER, 10)) {
            if (!create) return null;
            tag.setTag(PLAYER, new NBTTagCompound());
        }
        return tag.getCompoundTag(PLAYER);
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        int meta = Math.max(0, Math.min(TIERS.length - 1, stack.getItemDamage()));
        return super.getUnlocalizedName(stack) + "." + meta;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        for (int i = 0; i < icons.length; i++) {
            icons[i] = register.registerIcon(GregTechNightmare.RESOURCE_ROOT_ID + ":memorycard." + TIERS[i] + "b");
        }
        itemIcon = icons[0];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int meta) {
        return icons[Math.max(0, Math.min(icons.length - 1, meta))];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> list) {
        for (int i = 0; i < TIERS.length; i++) {
            list.add(new ItemStack(item, 1, i));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean advanced) {
        list.add(tr("gtn.handybag.memory.capacity", getCapacity(stack)));
        int state = getAccessState(stack);
        if (state == ACCESS_UNCLAIMED) {
            list.add(EnumChatFormatting.GRAY + tr("gtn.handybag.privacy.unclaimed"));
        } else if (state == ACCESS_PUBLIC) {
            list.add(EnumChatFormatting.GREEN + tr("gtn.handybag.privacy.public"));
            list.add(EnumChatFormatting.GRAY + tr("gtn.handybag.privacy.owner", getOwnerName(stack)));
        } else if (state == ACCESS_PRIVATE) {
            list.add(EnumChatFormatting.GOLD + tr("gtn.handybag.privacy.private"));
            list.add(EnumChatFormatting.GRAY + tr("gtn.handybag.privacy.owner", getOwnerName(stack)));
            if (player != null && !canAccess(stack, player)) {
                list.add(EnumChatFormatting.RED + tr("gtn.handybag.privacy.no_access"));
            } else if (player != null && !isOwner(stack, player) && HandyBagAdmin.canAdminister(player)) {
                list.add(EnumChatFormatting.AQUA + tr("gtn.handybag.privacy.admin_access"));
            }
        }
        list.add(EnumChatFormatting.GRAY + tr("Author_Crazer"));
        list.add(EnumChatFormatting.DARK_GRAY + tr("gtn.handybag.original_credit"));
    }
}
