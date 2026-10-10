package com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import com.EvgenWarGold.GregTechNightmare.GregTech.Items.ItemHandyBag;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;

public class HandyBagServerEvents {

    private static boolean registered;

    public static void register() {
        if (registered) return;
        registered = true;
        HandyBagServerEvents handler = new HandyBagServerEvents();
        MinecraftForge.EVENT_BUS.register(handler);
        FMLCommonHandler.instance()
            .bus()
            .register(handler);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRightClickBlock(PlayerInteractEvent event) {
        if (event == null || event.isCanceled()
            || event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK
            || event.entityPlayer == null
            || event.world == null
            || event.world.isRemote
            || !event.entityPlayer.isSneaking()) return;

        EntityPlayer player = event.entityPlayer;
        ItemStack stack = player.getCurrentEquippedItem();
        if (stack == null || !(stack.getItem() instanceof ItemHandyBag)) return;

        TileEntity tile = event.world.getTileEntity(event.x, event.y, event.z);
        if (!ItemHandyBag.tryTileTransfer(stack, player, event.world, tile, event.face)) return;

        event.setCanceled(true);
        event.useBlock = PlayerInteractEvent.Result.DENY;
        event.useItem = PlayerInteractEvent.Result.DENY;
    }

    @SubscribeEvent
    public void onItemPickup(EntityItemPickupEvent event) {
        if (event.entityPlayer == null || event.entityPlayer.worldObj.isRemote
            || event.item == null
            || event.isCanceled()) return;
        ItemStack pickup = event.item.getEntityItem();
        if (pickup == null || pickup.stackSize <= 0) return;

        EntityPlayer player = event.entityPlayer;
        boolean hasEnabledBag = false;
        for (int slot = 0; slot < 36; slot++) {
            ItemStack bag = ItemHandyBag.getBagInSlot(player, slot);
            if (bag != null && ItemHandyBag.getPickupMode(bag) != ItemHandyBag.PICKUP_OFF) {
                hasEnabledBag = true;
                break;
            }
        }
        if (!hasEnabledBag) return;

        int movedTotal = HandyBagOperations.mergePickupIntoExistingPlayerStacks(player, pickup);
        for (int slot = 0; slot < 36 && pickup.stackSize > 0; slot++) {
            ItemStack bag = ItemHandyBag.getBagInSlot(player, slot);
            if (bag == null) continue;
            int mode = ItemHandyBag.getPickupMode(bag);
            if (mode == ItemHandyBag.PICKUP_OFF) continue;
            HandyBagData data = getData(player, slot, bag);
            int moved = HandyBagOperations.pickupIntoBag(data, pickup, mode == ItemHandyBag.PICKUP_MATCHING);
            if (moved > 0) {
                movedTotal += moved;
                syncOpenBag(player, slot);
            }
        }

        if (movedTotal <= 0) return;
        player.inventory.markDirty();
        FMLCommonHandler.instance()
            .firePlayerItemPickupEvent(player, event.item);
        player.worldObj.playSoundAtEntity(
            player,
            "random.pop",
            0.2F,
            ((player.worldObj.rand.nextFloat() - player.worldObj.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
        player.onItemPickup(event.item, movedTotal);
        if (pickup.stackSize <= 0) {
            event.item.setDead();
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event) {
        if (event.phase != Phase.END || event.player == null || event.player.worldObj.isRemote) return;
        if (event.player.ticksExisted % 5 != 0) return;

        EntityPlayer player = event.player;
        for (int slot = 0; slot < 36; slot++) {
            ItemStack bag = ItemHandyBag.getBagInSlot(player, slot);
            if (bag == null || !ItemHandyBag.isRestockEnabled(bag)) continue;
            HandyBagData data = getData(player, slot, bag);
            if (HandyBagOperations.fillPlayerStacks(player, slot, data)) syncOpenBag(player, slot);
        }
    }

    private static HandyBagData getData(EntityPlayer player, int slot, ItemStack bag) {
        if (player.openContainer instanceof ContainerHandyBag) {
            ContainerHandyBag container = (ContainerHandyBag) player.openContainer;
            if (container.bagSlot == slot) return container.data;
        }
        return new HandyBagData(bag, player);
    }

    private static void syncOpenBag(EntityPlayer player, int slot) {
        if (player.openContainer instanceof ContainerHandyBag) {
            ContainerHandyBag container = (ContainerHandyBag) player.openContainer;
            if (container.bagSlot == slot) container.detectAndSendChanges();
        }
    }
}
