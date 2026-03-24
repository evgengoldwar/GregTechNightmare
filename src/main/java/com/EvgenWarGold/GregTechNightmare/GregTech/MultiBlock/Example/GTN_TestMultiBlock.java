package com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Example;

import static com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses.GTN_HatchElement.DataInput;
import static com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses.GTN_HatchElement.DataOutput;
import static com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses.GTN_HatchElement.DynamoMulti;
import static com.EvgenWarGold.GregTechNightmare.Utils.GTN_InventoryUtils.fluidListToArray;
import static com.EvgenWarGold.GregTechNightmare.Utils.GTN_InventoryUtils.itemListToArray;
import static com.EvgenWarGold.GregTechNightmare.Utils.GTN_InventoryUtils.removeFluids;
import static gregtech.api.enums.HatchElement.Dynamo;
import static gregtech.api.enums.HatchElement.Energy;
import static gregtech.api.enums.HatchElement.ExoticEnergy;
import static gregtech.api.enums.HatchElement.InputBus;
import static gregtech.api.enums.HatchElement.InputHatch;
import static gregtech.api.enums.HatchElement.Maintenance;
import static gregtech.api.enums.HatchElement.Muffler;
import static gregtech.api.enums.HatchElement.OutputBus;
import static gregtech.api.enums.HatchElement.OutputHatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.EvgenWarGold.GregTechNightmare.GregTech.Api.MultiblockArea;
import com.EvgenWarGold.GregTechNightmare.GregTech.Api.MultiblockOffsets;
import com.EvgenWarGold.GregTechNightmare.GregTech.Api.NBTDataPacket;
import com.EvgenWarGold.GregTechNightmare.GregTech.Hatch.GTN_DataInput;
import com.EvgenWarGold.GregTechNightmare.GregTech.Hatch.GTN_DataOutput;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses.GTN_Casings;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses.GTN_MultiBlockBase;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses.GTN_MultiBlockTooltipBuilder;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses.GTN_ProcessingLogic;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses.OverclockType;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses.StructureVariant;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses.TierData;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses.TieredElementBuilder;
import com.EvgenWarGold.GregTechNightmare.Utils.Authors;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.util.Vec3Impl;
import gregtech.api.logic.ProcessingLogic;
import gregtech.api.metatileentity.implementations.MTEHatchInputBus;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import org.jetbrains.annotations.NotNull;

public class GTN_TestMultiBlock extends GTN_MultiBlockBase<GTN_TestMultiBlock> {

    public GTN_TestMultiBlock(int id, String name) {
        super(id, name);
    }

    public GTN_TestMultiBlock(String name) {
        super(name);
    }

    @Override
    public List<StructureVariant<GTN_TestMultiBlock>> getStructureVariants() {
        return Arrays.asList(
            new StructureVariant<>(
                "Gas",
                new String[][] { { "AAA", "AAA", "AAA" }, { "A~A", "A A", "AAA" }, { "AAA", "AAA", "AAA" } },
                new MultiblockOffsets(1, 1, 0),
                new MultiblockArea(4, 5, 10),
                1,
                GTN_Casings.FrostProofMachineCasing),
            new StructureVariant<>(
                "Fuel",
                new String[][] { { "BBB", "BBB", "BBB" }, { "B~B", "B B", "BBB" }, { "BBB", "BBB", "BBB" } },
                new MultiblockOffsets(1, 1, 0),
                new MultiblockArea(3, 3, 3),
                2,
                GTN_Casings.TitaniumGearBoxCasing));
    }

    @Override
    public GTN_TestMultiBlock createNewMetaEntity() {
        return new GTN_TestMultiBlock(this.mName);
    }

    @Override
    public void createGtnTooltip(GTN_MultiBlockTooltipBuilder builder) {
        builder.addExtraInfo("CRAZER")
            .addExtraInfoWithSpace("FAOTIK");
    }

    @Override
    public Authors getAuthor() {
        return Authors.EVGEN_WAR_GOLD;
    }

    private final TierData casing = createTierData("casing", true);
    private final TierData casing1 = createTierData("casing1", true);

    @Override
    public IStructureDefinition<GTN_TestMultiBlock> getStructureDefinition() {
        return buildStructureDefinition(
            builder -> builder.addElement(
                'B',
                TieredElementBuilder.create(casing1, GTN_TestMultiBlock.class)
                    .casings(GTN_Casings.TitaniumGearBoxCasing, GTN_Casings.SolidifierCasing)
                    .hatches(
                        InputHatch,
                        OutputHatch,
                        InputBus,
                        OutputBus,
                        Energy,
                        ExoticEnergy,
                        Maintenance,
                        Muffler,
                        Dynamo,
                        DynamoMulti,
                        DataInput,
                        DataOutput)
                    .build())
                .addElement(
                    'A',
                    TieredElementBuilder.create(casing, GTN_TestMultiBlock.class)
                        .casings(GTN_Casings.FrostProofMachineCasing, GTN_Casings.Firebricks)
                        .hatches(
                            InputHatch,
                            OutputHatch,
                            InputBus,
                            OutputBus,
                            Energy,
                            ExoticEnergy,
                            Maintenance,
                            Muffler,
                            Dynamo,
                            DataInput,
                            DataOutput)
                        .build())
                .build());
    }

    @Override
    public OverclockType getOverclockType() {
        return multiBlockTier > 1 ? OverclockType.PerfectOverclock : OverclockType.NormalOverclock;
    }

    @Override
    protected void outputAfterRecipe() {
        super.outputAfterRecipe();
        for (GTN_DataOutput output : mDataOutputHatches) {
            ItemStack itemStack = new ItemStack(Items.diamond);
            NBTTagList tagList = new NBTTagList();
            NBTTagCompound stackTag = new NBTTagCompound();
            NBTTagCompound tag = new NBTTagCompound();

            itemStack.writeToNBT(stackTag);
            tag.setTag("item", stackTag);
            tagList.appendTag(tag);
            output.providePacket(new NBTDataPacket(tagList));
        }
    }

    @Override
    protected ProcessingLogic createProcessingLogic() {
        return new GTN_ProcessingLogic() {

            @Override
            public @NotNull CheckRecipeResult process() {
                if (multiBlockTier == 1) {
                    setDurationInSeconds(5);
                    return CheckRecipeResultRegistry.SUCCESSFUL;
                }

                if (multiBlockTier == 2) {
                    for (GTN_DataInput input : mDataInputHatches) {
                        if (input.data != null) {
                            NBTTagList tagList = input.data.getContent();
                            ItemStack item = null;
                            for (int i = 0; i < tagList.tagCount(); i++) {
                                NBTTagCompound tag = tagList.getCompoundTagAt(i);

                                if (tag.hasKey("item")) {
                                    item = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("item"));
                                }
                            }

                            if (item != null) {
                                outputItems = new ItemStack[]{item};
                                setDurationInSeconds(5);
                                return CheckRecipeResultRegistry.SUCCESSFUL;
                            }
                        }
                    }
                }

                return CheckRecipeResultRegistry.NO_RECIPE;
            }
        }.setMaxParallelSupplier(this::getMaxParallelRecipes);
    }
}
