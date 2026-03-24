package com.EvgenWarGold.GregTechNightmare.GregTech;

import com.EvgenWarGold.GregTechNightmare.GregTech.Hatch.GTN_DataInput;
import com.EvgenWarGold.GregTechNightmare.GregTech.Hatch.GTN_DataOutput;
import com.EvgenWarGold.GregTechNightmare.GregTech.Hatch.GTN_SensorHatch;
import com.EvgenWarGold.GregTechNightmare.GregTech.Mte.GTN_DataPipe;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Example.GTN_TestMultiBlock;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Generators.HV.GTN_VacuumNuke;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Generators.LV.GTN_CreosoteEngine;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Processing.EV.GTN_ExtremePowerCircuitAssembler;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Processing.HV.GTN_NodeEnergizer;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Processing.IV.GTN_BloodEnchanter;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Processing.IV.GTN_LaserMeteorMiner;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Processing.LUV.GTN_LargeArcaneAssembler;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Processing.LV.GTN_GasCollector;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Processing.LV.GTN_LowPowerVoidMiner;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Processing.LV.GTN_TreeSprouter;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Processing.MV.GTN_MediumPowerAssembler;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Processing.MV.GTN_MediumPowerBender;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Processing.MV.GTN_MediumPowerCircuitAssembler;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Processing.MV.GTN_MediumPowerEngraver;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Processing.MV.GTN_MediumPowerExtruder;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Processing.MV.GTN_MediumPowerWireMill;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Processing.STEAM.GTN_AdvancedBBF;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Processing.STEAM.GTN_AdvancedCokeOven;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Processing.STEAM.GTN_BronzeVoidMiner;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Processing.UHV.GTN_UltimatePrecise;

public final class MachineLoader {

    public static void init() {
        // spotless:off
        GTN_ItemList.TestMultiBlock.set(new GTN_TestMultiBlock(21_000, "Test"));
        GTN_ItemList.AdvancedBBF.set(new GTN_AdvancedBBF(21_001, "AdvancedBBF"));
        GTN_ItemList.BronzeVoidMiner.set(new GTN_BronzeVoidMiner(21_002, "BronzeVoidMiner"));
        GTN_ItemList.LowPowerVoidMiner.set(new GTN_LowPowerVoidMiner(21_003, "LowPowerVoidMiner"));
        GTN_ItemList.MediumPowerBender.set(new GTN_MediumPowerBender(21_004, "MediumPowerBender"));
        GTN_ItemList.MediumPowerExtruder.set(new GTN_MediumPowerExtruder(21_005, "MediumPowerExtruder"));
        GTN_ItemList.MediumPowerAssembler.set(new GTN_MediumPowerAssembler(21_006, "MediumPowerAssembler"));
        GTN_ItemList.MediumPowerCircuitAssembler.set(new GTN_MediumPowerCircuitAssembler(21_007, "MediumPowerCircuitAssembler"));
        GTN_ItemList.AdvancedCokeOven.set(new GTN_AdvancedCokeOven(21_008, "AdvancedCokeOven"));
        GTN_ItemList.NodeEnergizer.set(new GTN_NodeEnergizer(21_009, "NodeEnergizer"));
        GTN_ItemList.LargeArcaneAssembler.set(new GTN_LargeArcaneAssembler(21_010, "LargeArcaneAssembler"));
        GTN_ItemList.TreeSprouter.set(new GTN_TreeSprouter(21_011, "TreeSprouter"));
        GTN_ItemList.CreosoteEngine.set(new GTN_CreosoteEngine(21_012, "CreosoteEngine"));
        GTN_ItemList.MediumPowerWireMill.set(new GTN_MediumPowerWireMill(21_013, "MediumPowerWireMill"));
        GTN_ItemList.MediumPowerEngraver.set(new GTN_MediumPowerEngraver(21_014, "MediumPowerEngraver"));
        GTN_ItemList.ExtremePowerCircuitAssembler.set(new GTN_ExtremePowerCircuitAssembler(21_015, "ExtremePowerCircuitAssembler"));
        GTN_ItemList.UltimatePrecise.set(new GTN_UltimatePrecise(21_016, "UltimatePrecise"));
        GTN_ItemList.GasCollector.set(new GTN_GasCollector(21_017, "GasCollector"));
        GTN_ItemList.LaserMeteorMiner.set(new GTN_LaserMeteorMiner(21_018, "LaserMeteorMiner"));
        GTN_ItemList.BloodEnchanter.set(new GTN_BloodEnchanter(21_019, "BloodEnchanter"));
        GTN_ItemList.VacuumNuke.set(new GTN_VacuumNuke(21_020, "VacuumNuke"));
        GTN_ItemList.SensorHatch.set(new GTN_SensorHatch(21_021, "Sensor Hatch"));
        GTN_ItemList.DataInput.set(new GTN_DataInput(21_022, "Data Input"));
        GTN_ItemList.DataOutput.set(new GTN_DataOutput(21_023, "Data Output"));
        GTN_ItemList.DataPipe.set(new GTN_DataPipe(21_024, "Data Pipe"));
        //spotless:on
    }
}
