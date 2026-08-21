package com.EvgenWarGold.GregTechNightmare.GregTech;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;

public enum GTN_ItemList {

    // spotless:off
    // region Things
    // region Items
    TestItem,
    AdvancedClay,
    MeteorMinerSchematic1,
    MeteorMinerSchematic2,
    ManaProspector,
    WildcardIngot,
    WildcardPlate,
    WildcardDust,
    WildcardStick,
    WildcardScrew,
    WildcardBolt,
    WildcardRing,
    WildcardFoil,
    WildcardGear,
    WildcardWire1x,
    WildcardCable1x,
    WildcardFrame,
    WildcardGem,
    WildcardBlock,
    WildcardNugget,
    WildcardDensePlate,
    // endregion
    // region Casings
    TestCasing,
    CokeOvenCasing,
    ArborealCasing,
    SoulCasing,
    // endregion
    // region MultiBlock
    TestMultiBlock,
    AdvancedBBF,
    BronzeVoidMiner,
    LowPowerVoidMiner,
    MediumPowerBender,
    MediumPowerExtruder,
    MediumPowerAssembler,
    MediumPowerCircuitAssembler,
    AdvancedCokeOven,
    NodeEnergizer,
    LargeArcaneAssembler,
    TreeSprouter,
    CreosoteEngine,
    MediumPowerWireMill,
    MediumPowerEngraver,
    ExtremePowerCircuitAssembler,
    UltimatePrecise,
    GasCollector,
    LaserMeteorMiner,
    BloodEnchanter,
    VacuumNuke,
    ImprovedAlgaeFarm,
    ImprovedSliceNSplice,
    HighPowerComponentAssembler,
    ZeroPowerWireMill,
    MagicEBF,
    MagicGenerator,
    WaterModuleMagicGenerator,
    FireModuleMagicGenerator,
    EarthModuleMagicGenerator,
    EntropyModuleMagicGenerator,
    AirModuleMagicGenerator,
    OrderModuleMagicGenerator,
    ItemCrate,
    LargeBioLab,
    // endregion
    // region Hatch
    SensorHatch,
    ManaHatch,
    AspectHatch,
    MeAspectHatch,
    WildcardPatternBuffer,
    // endregion

    ;
    // endregion
    // spotless:on

    // region Main
    private boolean hasNotBeenSet;
    private ItemStack itemStack;

    private static final Item ERROR_ITEM = Item.getItemFromBlock(Blocks.fire);
    private static final ItemStack ERROR_ITEM_STACK = new ItemStack(ERROR_ITEM);

    GTN_ItemList() {
        hasNotBeenSet = true;
    }

    private void check() {
        if (hasNotBeenSet) {
            throw new IllegalAccessError("The Enum '" + name() + "' has not been set to an Item at this time!");
        }
    }

    public boolean equal(@Nullable ItemStack itemStack) {
        if (itemStack == null) return false;
        if (hasNotBeenSet) return false;
        if (this.itemStack == itemStack) return true;
        return this.itemStack.isItemEqual(itemStack);
    }

    // endregion
    // region Getters
    public Item getItem() {
        check();
        if (GTUtility.isStackInvalid(itemStack)) return ERROR_ITEM;
        return itemStack.getItem();
    }

    public Block getBlock() {
        check();
        return Block.getBlockFromItem(getItem());
    }

    public int getMeta() {
        return itemStack.getItemDamage();
    }

    public ItemStack get(int amount) {
        check();
        if (GTUtility.isStackInvalid(itemStack)) {
            GTLog.out.println("Object in the GTNItemList is null at:");
            new NullPointerException().printStackTrace(GTLog.out);
            return ERROR_ITEM_STACK;
        }
        return GTUtility.copyAmountUnsafe(amount, itemStack);
    }

    // endregion
    // region Setters
    public GTN_ItemList set(Item item) {
        hasNotBeenSet = false;
        if (item == null) return this;
        ItemStack stack = new ItemStack(item, 1, 0);
        itemStack = GTUtility.copyAmountUnsafe(1, stack);
        return this;
    }

    public GTN_ItemList set(ItemStack itemStack) {
        if (itemStack != null) {
            hasNotBeenSet = false;
            this.itemStack = GTUtility.copyAmountUnsafe(1, itemStack);

        }
        return this;
    }

    public GTN_ItemList set(IMetaTileEntity metaTileEntity) {
        if (metaTileEntity == null) throw new IllegalArgumentException();
        return set(metaTileEntity.getStackForm(1L));
    }
    // endregion
}
