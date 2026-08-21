package com.EvgenWarGold.GregTechNightmare.GregTech;

import com.EvgenWarGold.GregTechNightmare.GregTech.Hatch.GTN_AspectHatch;
import com.EvgenWarGold.GregTechNightmare.GregTech.Hatch.GTN_ManaHatch;
import com.EvgenWarGold.GregTechNightmare.GregTech.Hatch.GTN_MeAspectHatch;
import com.EvgenWarGold.GregTechNightmare.GregTech.Hatch.GTN_SensorHatch;
import com.EvgenWarGold.GregTechNightmare.GregTech.Hatch.GTN_WildcardPatternBuffer;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Example.GTN_TestMultiBlock;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Generators.EV.Magen.GTN_AirModuleMagicGenerator;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Generators.EV.Magen.GTN_EarthModuleMagicGenerator;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Generators.EV.Magen.GTN_EntropyModuleMagicGenerator;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Generators.EV.Magen.GTN_FireModuleMagicGenerator;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Generators.EV.Magen.GTN_MagicGenerator;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Generators.EV.Magen.GTN_OrderModuleMagicGenerator;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Generators.EV.Magen.GTN_WaterModuleMagicGenerator;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Generators.HV.GTN_VacuumNuke;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Generators.LV.GTN_CreosoteEngine;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Processing.EV.GTN_ExtremePowerCircuitAssembler;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Processing.EV.GTN_ImprovedSliceNSplice;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Processing.HV.GTN_HighPowerComponentAssembler;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Processing.HV.GTN_ImprovedAlgaeFarm;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Processing.HV.GTN_NodeEnergizer;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Processing.IV.GTN_BloodEnchanter;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Processing.IV.GTN_LaserMeteorMiner;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Processing.LUV.GTN_LargeArcaneAssembler;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Processing.LV.GTN_GasCollector;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Processing.LV.GTN_ItemCrate;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Processing.LV.GTN_LowPowerVoidMiner;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Processing.LV.GTN_TreeSprouter;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Processing.MV.GTN_MagicEBF;
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
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Processing.UV.GTN_LargeBioLab;
import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.Processing.ZPM.GTN_ZeroPowerWireMill;

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
        GTN_ItemList.ImprovedAlgaeFarm.set(new GTN_ImprovedAlgaeFarm(21_022, "ImprovedAlgaeFarm"));
        GTN_ItemList.ImprovedSliceNSplice.set(new GTN_ImprovedSliceNSplice(21_023, "ImprovedSliceNSplice"));
        GTN_ItemList.HighPowerComponentAssembler.set(new GTN_HighPowerComponentAssembler(21_024, "HighPowerComponentAssembler"));
        GTN_ItemList.ZeroPowerWireMill.set(new GTN_ZeroPowerWireMill(21_025, "ZeroPowerWireMill"));
        GTN_ItemList.ManaHatch.set(new GTN_ManaHatch(21_026, "Mana Hatch"));
        GTN_ItemList.MagicEBF.set(new GTN_MagicEBF(21_027, "MagicEBF"));
        GTN_ItemList.MagicGenerator.set(new GTN_MagicGenerator(21_028, "MagicGenerator"));
        GTN_ItemList.WaterModuleMagicGenerator.set(new GTN_WaterModuleMagicGenerator(21_029, "WaterModuleMagicGenerator"));
        GTN_ItemList.FireModuleMagicGenerator.set(new GTN_FireModuleMagicGenerator(21_030, "FireModuleMagicGenerator"));
        GTN_ItemList.AirModuleMagicGenerator.set(new GTN_AirModuleMagicGenerator(21_031, "AirModuleMagicGenerator"));
        GTN_ItemList.EarthModuleMagicGenerator.set(new GTN_EarthModuleMagicGenerator(21_032, "EarthModuleMagicGenerator"));
        GTN_ItemList.EntropyModuleMagicGenerator.set(new GTN_EntropyModuleMagicGenerator(21_033, "EntropyModuleMagicGenerator"));
        GTN_ItemList.OrderModuleMagicGenerator.set(new GTN_OrderModuleMagicGenerator(21_034, "OrderModuleMagicGenerator"));
        GTN_ItemList.AspectHatch.set(new GTN_AspectHatch(21_035, "Aspect Hatch"));
        GTN_ItemList.MeAspectHatch.set(new GTN_MeAspectHatch(21_036, "ME Aspect Hatch"));
        GTN_ItemList.WildcardPatternBuffer.set(new GTN_WildcardPatternBuffer(21_037, "ME Wildcard Pattern Buffer"));
        GTN_ItemList.ItemCrate.set(new GTN_ItemCrate(21_038, "ItemCrate"));
        GTN_ItemList.LargeBioLab.set(new GTN_LargeBioLab(21_039, "LargeBioLab"));
        // spotless:on
    }
}
