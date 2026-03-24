package com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.EvgenWarGold.GregTechNightmare.GregTech.Hatch.GTN_DataInput;
import com.EvgenWarGold.GregTechNightmare.GregTech.Hatch.GTN_DataOutput;
import com.EvgenWarGold.GregTechNightmare.GregTech.Hatch.GTN_SensorHatch;

import gregtech.api.interfaces.IHatchElement;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.api.util.IGTHatchAdder;
import gtPlusPlus.xmod.gregtech.api.metatileentity.implementations.MTEHatchSteamBusInput;
import gtPlusPlus.xmod.gregtech.api.metatileentity.implementations.MTEHatchSteamBusOutput;
import gtPlusPlus.xmod.gregtech.api.metatileentity.implementations.base.MTEHatchCustomFluidBase;
import tectech.thing.metaTileEntity.hatch.MTEHatchDynamoMulti;

@SuppressWarnings("unchecked")
public enum GTN_HatchElement implements IHatchElement<GTN_MultiBlockBase<?>> {

    SteamInputHatch("SteamInputHatch", (base, tile, casing) -> base.addSteamInputHatchToMachineList(tile),
        MTEHatchCustomFluidBase.class) {

        @Override
        public long count(GTN_MultiBlockBase<?> gtnMultiBlockBase) {
            return gtnMultiBlockBase.mSteamInputFluids.size();
        }
    },

    SteamInputBus("SteamInputBus", (base, tile, casing) -> base.addSteamInputBusToMachineList(tile),
        MTEHatchSteamBusInput.class) {

        @Override
        public long count(GTN_MultiBlockBase<?> gtnMultiBlockBase) {
            return gtnMultiBlockBase.mSteamInputBusses.size();
        }
    },

    SteamOutputBus("SteamOutputBus", (base, tile, casing) -> base.addSteamOutputBusToMachineList(tile),
        MTEHatchSteamBusOutput.class) {

        @Override
        public long count(GTN_MultiBlockBase<?> gtnMultiBlockBase) {
            return gtnMultiBlockBase.mSteamOutputBusses.size();
        }
    },

    SensorHatch("SensorHatch", (base, tile, casing) -> base.addSensorHatchToMachineList(tile), GTN_SensorHatch.class) {

        @Override
        public long count(GTN_MultiBlockBase<?> gtnMultiBlockBase) {
            return gtnMultiBlockBase.mSensorHatch.size();
        }
    },

    DataInput("DataInput", (base, tile, casing) -> base.addDataInputHatchToMachineList(tile), GTN_DataInput.class) {
        @Override
        public long count(GTN_MultiBlockBase<?> gtnMultiBlockBase) {
            return gtnMultiBlockBase.mDataInputHatches.size();
        }
    },

    DataOutput("DataOutput", (base, tile, casing) -> base.addDataOutputHatchToMachineList(tile), GTN_DataOutput.class) {
        @Override
        public long count(GTN_MultiBlockBase<?> gtnMultiBlockBase) {
            return gtnMultiBlockBase.mDataOutputHatches.size();
        }
    },

    DynamoMulti("DynamoMulti", (base, tile, casing) -> base.addDynamoMultiHatchToMachineList(tile),
        MTEHatchDynamoMulti.class) {

        @Override
        public long count(GTN_MultiBlockBase<?> gtnMultiBlockBase) {
            return gtnMultiBlockBase.mDynamoHatches.size();
        }
    };

    private final String name;
    private final List<Class<? extends IMetaTileEntity>> mteClasses;
    private final IGTHatchAdder<? super GTN_MultiBlockBase<?>> adder;

    GTN_HatchElement(String name, IGTHatchAdder<? super GTN_MultiBlockBase<?>> adder,
        Class<? extends IMetaTileEntity>... mteClasses) {
        this.name = name;
        this.mteClasses = Collections.unmodifiableList(Arrays.asList(mteClasses));
        this.adder = adder;
    }

    @Override
    public List<? extends Class<? extends IMetaTileEntity>> mteClasses() {
        return mteClasses;
    }

    @Override
    public String getDisplayName() {
        return GTUtility.translate(name);
    }

    @Override
    public IGTHatchAdder<? super GTN_MultiBlockBase<?>> adder() {
        return adder;
    }
}
