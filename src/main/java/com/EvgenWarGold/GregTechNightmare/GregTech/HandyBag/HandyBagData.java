package com.EvgenWarGold.GregTechNightmare.GregTech.HandyBag;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import com.EvgenWarGold.GregTechNightmare.GregTech.Items.ItemHandyBag;
import com.EvgenWarGold.GregTechNightmare.GregTech.Items.ItemMemoryCard;

public class HandyBagData {

    private static final String ROOT = "GTNHandyBag";
    private static final String CARDS = "Cards";
    private static final String ITEMS = "GTNHandyBagItems";
    private static final String SETTINGS = "HandyBag";
    private static final String LOCK_MASK = "LockMask";
    private static final long[] SECTION_MASKS = { 0x7FFFFFFL, 0x1FFF8000000L, 0x7FFE0000000000L };
    private final ItemStack bagStack;
    private final EntityPlayer player;
    private final ItemStack[] cards = new ItemStack[4];
    private final ItemStack[] items;
    private int selectedCard;
    private boolean loading;
    private int batchDepth;
    private boolean dirty;

    public HandyBagData(ItemStack bagStack, EntityPlayer player) {
        this.bagStack = bagStack;
        this.player = player;
        this.items = new ItemStack[ItemHandyBag.getBagSize(bagStack)];
        load();
    }

    public ItemStack getBagStack() {
        return bagStack;
    }

    public int getSize() {
        return items.length;
    }

    public ItemStack getItem(int slot) {
        return slot >= 0 && slot < items.length ? items[slot] : null;
    }

    public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= items.length) return;
        items[slot] = stack;
        markItemsDirty();
    }

    public ItemStack decrItem(int slot, int amount) {
        ItemStack stack = getItem(slot);
        if (stack == null || amount <= 0) return null;
        ItemStack result;
        if (stack.stackSize <= amount) {
            result = stack;
            items[slot] = null;
        } else {
            result = stack.splitStack(amount);
            if (stack.stackSize <= 0) items[slot] = null;
        }
        markItemsDirty();
        return result;
    }

    public ItemStack removeItem(int slot) {
        ItemStack stack = getItem(slot);
        if (stack != null) {
            items[slot] = null;
            markItemsDirty();
        }
        return stack;
    }

    public int getSelectedCard() {
        return selectedCard;
    }

    public ItemStack getSelectedCardStack() {
        return cards[selectedCard];
    }

    public int getStackLimit() {
        ItemStack card = getSelectedCardStack();
        return ItemMemoryCard.canAccess(card, player) ? ItemMemoryCard.getCapacity(card) : 0;
    }

    public boolean canAccessSelectedCard() {
        return ItemMemoryCard.canAccess(getSelectedCardStack(), player);
    }

    public int toggleSelectedCardPrivacy() {
        ItemStack card = getSelectedCardStack();
        int result = ItemMemoryCard.togglePrivacy(card, player);
        if (result == ItemMemoryCard.ACCESS_PRIVATE || result == ItemMemoryCard.ACCESS_PUBLIC) {
            writeBagState();
            if (player != null && player.inventory != null) player.inventory.markDirty();
        }
        return result;
    }

    public boolean selectNextCard(boolean reverse) {
        int step = reverse ? -1 : 1;
        for (int offset = 1; offset <= cards.length; offset++) {
            int slot = (selectedCard + step * offset) % cards.length;
            if (slot < 0) slot += cards.length;
            if (cards[slot] != null) {
                if (slot == selectedCard) return false;
                selectCard(slot);
                return true;
            }
        }
        return false;
    }

    public ItemStack getCard(int slot) {
        return slot >= 0 && slot < cards.length ? cards[slot] : null;
    }

    public void setCard(int slot, ItemStack stack) {
        if (slot < 0 || slot >= cards.length) return;
        if (slot == selectedCard) writeItemsToSelectedCard();
        cards[slot] = stack;
        writeBagState();
        if (slot == selectedCard) readItemsFromSelectedCard();
    }

    public ItemStack removeCard(int slot) {
        ItemStack stack = getCard(slot);
        if (stack != null) setCard(slot, null);
        return stack;
    }

    public void selectCard(int slot) {
        if (slot < 0 || slot >= cards.length || slot == selectedCard) return;
        writeItemsToSelectedCard();
        selectedCard = slot;
        writeBagState();
        readItemsFromSelectedCard();
    }

    public int getPickupMode() {
        return ItemHandyBag.getPickupMode(bagStack);
    }

    public void setPickupMode(int mode) {
        ItemHandyBag.setPickupMode(bagStack, mode);
        if (player != null && player.inventory != null) player.inventory.markDirty();
    }

    public boolean isRestockEnabled() {
        return ItemHandyBag.isRestockEnabled(bagStack);
    }

    public void setRestockEnabled(boolean enabled) {
        ItemHandyBag.setRestockEnabled(bagStack, enabled);
        if (player != null && player.inventory != null) player.inventory.markDirty();
    }

    public boolean isLocked() {
        return ItemHandyBag.isLocked(bagStack);
    }

    public void setLocked(boolean locked) {
        ItemHandyBag.setLocked(bagStack, locked);
        if (player != null && player.inventory != null) player.inventory.markDirty();
    }

    public int getSectionCount() {
        return items.length == ItemHandyBag.LARGE_SIZE ? 3 : 1;
    }

    public boolean isSlotQuickActionBlocked(int slot) {
        return slot >= 0 && slot < items.length && (getSelectedCardMask(LOCK_MASK) & (1L << slot)) != 0L;
    }

    public boolean isSectionQuickActionBlocked(int section) {
        return isSectionMaskActive(section, LOCK_MASK);
    }

    public boolean setSectionQuickActionBlocked(int section, boolean blocked) {
        return setSectionMask(section, LOCK_MASK, blocked);
    }

    private boolean isSectionMaskActive(int section, String key) {
        if (section < 0 || section >= getSectionCount()) return false;
        long sectionMask = SECTION_MASKS[section];
        return (getSelectedCardMask(key) & sectionMask) == sectionMask;
    }

    private boolean setSectionMask(int section, String key, boolean enabled) {
        if (section < 0 || section >= getSectionCount()) return false;
        ItemStack card = getSelectedCardStack();
        if (card == null || !ItemMemoryCard.canAccess(card, player)) return false;
        NBTTagCompound settings = getCardSettings(card, true);
        long mask = settings.getLong(key);
        long sectionMask = SECTION_MASKS[section];
        long updated = enabled ? mask | sectionMask : mask & ~sectionMask;
        if (updated == mask) return false;
        settings.setLong(key, updated);
        writeBagState();
        if (player != null && player.inventory != null) player.inventory.markDirty();
        return true;
    }

    private long getSelectedCardMask(String key) {
        ItemStack card = getSelectedCardStack();
        if (card == null || !ItemMemoryCard.canAccess(card, player)) return 0L;
        NBTTagCompound settings = getCardSettings(card, false);
        return settings == null ? 0L : settings.getLong(key);
    }

    private static NBTTagCompound getCardSettings(ItemStack card, boolean create) {
        if (card == null) return null;
        NBTTagCompound tag = card.getTagCompound();
        if (tag == null && create) {
            tag = new NBTTagCompound();
            card.setTagCompound(tag);
        }
        if (tag == null) return null;
        if (!tag.hasKey(SETTINGS) && create) tag.setTag(SETTINGS, new NBTTagCompound());
        return tag.hasKey(SETTINGS) ? tag.getCompoundTag(SETTINGS) : null;
    }

    public void beginBatch() {
        batchDepth++;
    }

    public void endBatch() {
        if (batchDepth > 0) batchDepth--;
        if (batchDepth == 0 && dirty) flush();
    }

    public void flush() {
        if (batchDepth > 0) {
            dirty = true;
            return;
        }
        writeItemsToSelectedCard();
        writeBagState();
        dirty = false;
        if (player != null && player.inventory != null) player.inventory.markDirty();
    }

    private void load() {
        loading = true;
        NBTTagCompound root = getRoot(false);
        if (root != null) {
            selectedCard = Math.max(0, Math.min(3, root.getInteger("SelectedCard")));
            NBTTagList list = root.getTagList(CARDS, 10);
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound entry = list.getCompoundTagAt(i);
                int slot = entry.getByte("Slot") & 0xFF;
                if (slot >= 0 && slot < cards.length && entry.hasKey("Stack")) {
                    cards[slot] = ItemStack.loadItemStackFromNBT(entry.getCompoundTag("Stack"));
                }
            }
        }
        readItemsFromSelectedCard();
        loading = false;
    }

    private NBTTagCompound getRoot(boolean create) {
        NBTTagCompound tag = bagStack.getTagCompound();
        if (tag == null && create) {
            tag = new NBTTagCompound();
            bagStack.setTagCompound(tag);
        }
        if (tag == null) return null;
        if (!tag.hasKey(ROOT) && create) tag.setTag(ROOT, new NBTTagCompound());
        return tag.hasKey(ROOT) ? tag.getCompoundTag(ROOT) : null;
    }

    private void readItemsFromSelectedCard() {
        loading = true;
        for (int i = 0; i < items.length; i++) items[i] = null;
        ItemStack card = getSelectedCardStack();
        if (card != null && ItemMemoryCard.canAccess(card, player) && card.hasTagCompound()) {
            NBTTagList list = card.getTagCompound()
                .getTagList(ITEMS, 10);
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound entry = list.getCompoundTagAt(i);
                int slot = entry.getInteger("Slot");
                if (slot < 0 || slot >= items.length || !entry.hasKey("Stack")) continue;
                NBTTagCompound stackTag = entry.getCompoundTag("Stack");
                ItemStack stack = ItemStack.loadItemStackFromNBT(stackTag);
                if (stack != null && stackTag.hasKey("CountInt")) stack.stackSize = stackTag.getInteger("CountInt");
                items[slot] = stack;
            }
        }
        loading = false;
    }

    private void markItemsDirty() {
        if (loading) return;
        dirty = true;
        if (batchDepth == 0) flush();
    }

    private void writeItemsToSelectedCard() {
        if (loading) return;
        ItemStack card = getSelectedCardStack();
        if (card == null || !ItemMemoryCard.canAccess(card, player)) return;
        NBTTagCompound cardTag = card.getTagCompound();
        if (cardTag == null) {
            cardTag = new NBTTagCompound();
            card.setTagCompound(cardTag);
        }
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < items.length; i++) {
            ItemStack stack = items[i];
            if (stack == null || stack.stackSize <= 0) continue;
            NBTTagCompound entry = new NBTTagCompound();
            entry.setInteger("Slot", i);
            NBTTagCompound stackTag = new NBTTagCompound();
            stack.writeToNBT(stackTag);
            stackTag.setInteger("CountInt", stack.stackSize);
            entry.setTag("Stack", stackTag);
            list.appendTag(entry);
        }
        cardTag.setTag(ITEMS, list);
    }

    private void writeBagState() {
        if (loading) return;
        NBTTagCompound root = getRoot(true);
        root.setInteger("SelectedCard", selectedCard);
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < cards.length; i++) {
            ItemStack card = cards[i];
            if (card == null) continue;
            NBTTagCompound entry = new NBTTagCompound();
            entry.setByte("Slot", (byte) i);
            NBTTagCompound stackTag = new NBTTagCompound();
            card.writeToNBT(stackTag);
            entry.setTag("Stack", stackTag);
            list.appendTag(entry);
        }
        root.setTag(CARDS, list);
    }
}
