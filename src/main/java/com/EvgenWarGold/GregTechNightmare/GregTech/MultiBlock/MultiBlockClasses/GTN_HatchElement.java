package com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

    SteamInputHatch("SteamInputHatch", GTN_MultiBlockBase::addSteamInputHatchToMachineList,
        MTEHatchCustomFluidBase.class) {

        @Override
        public long count(GTN_MultiBlockBase<?> gtnMultiBlockBase) {
            return gtnMultiBlockBase.mSteamInputFluids.size();
        }
    },

    SteamInputBus("SteamInputBus", GTN_MultiBlockBase::addSteamInputBusToMachineList, MTEHatchSteamBusInput.class) {

        @Override
        public long count(GTN_MultiBlockBase<?> gtnMultiBlockBase) {
            return gtnMultiBlockBase.mSteamInputBusses.size();
        }
    },

    SteamOutputBus("SteamOutputBus", GTN_MultiBlockBase::addSteamOutputBusToMachineList, MTEHatchSteamBusOutput.class) {

        @Override
        public long count(GTN_MultiBlockBase<?> gtnMultiBlockBase) {
            return gtnMultiBlockBase.mSteamOutputBusses.size();
        }
    },

    SensorHatch("SensorHatch", GTN_MultiBlockBase::addSensorHatchToMachineList, GTN_SensorHatch.class) {

        @Override
        public long count(GTN_MultiBlockBase<?> gtnMultiBlockBase) {
            return gtnMultiBlockBase.mSensorHatches.size();
        }
    },

    DynamoMulti("DynamoMulti", GTN_MultiBlockBase::addDynamoMultiHatchToMachineList, MTEHatchDynamoMulti .class) {

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
