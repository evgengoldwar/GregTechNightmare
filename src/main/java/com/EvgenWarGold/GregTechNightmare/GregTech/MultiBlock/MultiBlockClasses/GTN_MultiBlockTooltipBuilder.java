package com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses;

import static com.EvgenWarGold.GregTechNightmare.Utils.GTN_Utils.tr;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import com.EvgenWarGold.GregTechNightmare.Mixins.Late.MultiblockTooltipBuilderAccessor;
import com.EvgenWarGold.GregTechNightmare.Utils.Authors;
import com.google.common.collect.SetMultimap;

import gregtech.GTMod;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.api.util.StringUtils;

public class GTN_MultiBlockTooltipBuilder extends MultiblockTooltipBuilder {

    private static final String HOLD = tr("GT5U.MBTT.Hold");
    private static final String DISPLAY = tr("GT5U.MBTT.Display");
    private static final String ADDED_BY = tr("GT5U.MBTT.Mod");
    private static final String MACHINE_TYPE = tr("GT5U.MBTT.MachineType");
    private static final String TAB = "   ";
    private static final String COLON = ": ";
    private static final String SEPARATOR = ", ";
    private static final String STRUCTURE_HINT = tr("GT5U.MBTT.StructureHint");
    private static final String STEAM_INPUT_BUS = tr("GTPP.MBTT.SteamInputBus");
    private static final String STEAM_OUTPUT_BUS = tr("GTPP.MBTT.SteamOutputBus");
    private static final String STEAM_HATCH = tr("GTPP.MBTT.SteamHatch");
    private static final String MAINTENANCE_HATCH = tr("GT5U.MBTT.MaintenanceHatch");
    private static final String ENERGY_HATCH = tr("GT5U.MBTT.EnergyHatch");
    private static final String DYNAMO_HATCH = tr("GT5U.MBTT.DynamoHatch");
    private static final String MUFFLER_HATCH = tr("GT5U.MBTT.MufflerHatch");
    private static final String INPUT_BUS = tr("GT5U.MBTT.InputBus");
    private static final String INPUT_HATCH = tr("GT5U.MBTT.InputHatch");
    private static final String OUTPUT_BUS = tr("GT5U.MBTT.OutputBus");
    private static final String OUTPUT_HATCH = tr("GT5U.MBTT.OutputHatch");
    private static final String EXOTIC_ENERGY_HATCH = tr("GTN.TooltipBuilder.ExoticHatch");
    private static final String EXOTIC_OR_ENERGY_HATCH = tr("GTN.TooltipBuilder.ExoticOrEnergyHatch");
    private static final String DYNAMO_OR_BUFFERED_HATCH = tr("GTN.TooltipBuilder.DynamoOrBufferedHatch");
    private static final String MANA_HATCH = tr("GTN.TooltipBuilder.ManaHatch");
    private static final String ASPECT_HATCH = tr("GTN.TooltipBuilder.AspectHatch");
    private static final String ME_ASPECT_HATCH = tr("GTN.TooltipBuilder.MeAspectHatch");
    private static final String DIMENSIONS = tr("GT5U.MBTT.Dimensions");
    private static final String STRUCTURE = tr("GT5U.MBTT.Structure");
    private static final String[] DOTS = IntStream.range(0, 16)
        .mapToObj(i -> tr("structurelib.blockhint." + i + ".name"))
        .toArray(String[]::new);

    public void addInfoMultiLineTranslated(String key) {
        for (int i = 0; i <= 99; i++) {
            String langKey = key + "." + String.format("%02d", i);
            if (StatCollector.canTranslate(langKey)) {
                addInfo(StatCollector.translateToLocal(langKey));
            } else {
                break;
            }
        }
    }

    public MultiblockTooltipBuilder addMachineType(String machine) {
        MultiblockTooltipBuilderAccessor accessor = (MultiblockTooltipBuilderAccessor) this;
        List<String> iLines = accessor.getILines();
        iLines.add(
            EnumChatFormatting.GRAY + MACHINE_TYPE
                + COLON
                + EnumChatFormatting.YELLOW
                + machine
                + EnumChatFormatting.RESET);
        return this;
    }

    public MultiblockTooltipBuilder addInfo(String info) {
        MultiblockTooltipBuilderAccessor accessor = (MultiblockTooltipBuilderAccessor) this;
        List<String> iLines = accessor.getILines();
        iLines.add(EnumChatFormatting.GRAY + info);
        return this;
    }

    public GTN_MultiBlockTooltipBuilder addAuthor(Authors author) {
        addInfo(getLocalizedAuthor(author));
        return this;
    }

    private static String getLocalizedAuthor(Authors author) {
        return switch (author) {
            case EVGEN_WAR_GOLD -> tr("Author_EvgenWarGold");
            case CRAZER -> tr("Author_Crazer");
            case TOTTO -> tr("Author_Totto");
        };
    }

    public GTN_MultiBlockTooltipBuilder addExtraInfo(String extraInfo) {
        MultiblockTooltipBuilderAccessor accessor = (MultiblockTooltipBuilderAccessor) this;
        accessor.getSLines()
            .add(extraInfo);
        return this;
    }

    public GTN_MultiBlockTooltipBuilder addExtraInfoWithSpace(String info) {
        MultiblockTooltipBuilderAccessor accessor = (MultiblockTooltipBuilderAccessor) this;
        List<String> sLines = accessor.getSLines();
        sLines.add(EnumChatFormatting.GRAY + TAB + info);
        return this;
    }

    private GTN_MultiBlockTooltipBuilder addHatch(String hatchName, int count, int dot) {
        MultiblockTooltipBuilderAccessor accessor = (MultiblockTooltipBuilderAccessor) this;
        List<String> sLines = accessor.getSLines();
        String formattedCount = EnumChatFormatting.RED + String.valueOf(count) + EnumChatFormatting.YELLOW;
        String formattedDot = EnumChatFormatting.RED + String.valueOf(dot) + EnumChatFormatting.YELLOW;
        sLines.add(
            EnumChatFormatting.AQUA + TAB
                + hatchName
                + EnumChatFormatting.YELLOW
                + tr("GTN.TooltipBuilder.HatchRequirement", formattedCount, formattedDot));
        return this;
    }

    public GTN_MultiBlockTooltipBuilder addSteamInputBus(int count, int dot) {
        addHatch(STEAM_INPUT_BUS, count, dot);
        return this;
    }

    public GTN_MultiBlockTooltipBuilder addSteamHatch(int count, int dot) {
        addHatch(STEAM_HATCH, count, dot);
        return this;
    }

    public GTN_MultiBlockTooltipBuilder addSteamOutputBus(int count, int dot) {
        addHatch(STEAM_OUTPUT_BUS, count, dot);
        return this;
    }

    public GTN_MultiBlockTooltipBuilder addEnergyHatch(int count, int dot) {
        addHatch(ENERGY_HATCH, count, dot);
        return this;
    }

    public GTN_MultiBlockTooltipBuilder addDynamoHatch(int count, int dot) {
        addHatch(DYNAMO_HATCH, count, dot);
        return this;
    }

    public GTN_MultiBlockTooltipBuilder addMaintenanceHatch(int count, int dot) {
        addHatch(MAINTENANCE_HATCH, count, dot);
        return this;
    }

    public GTN_MultiBlockTooltipBuilder addMufflerHatch(int count, int dot) {
        addHatch(MUFFLER_HATCH, count, dot);
        return this;
    }

    public GTN_MultiBlockTooltipBuilder addInputBus(int count, int dot) {
        addHatch(INPUT_BUS, count, dot);
        return this;
    }

    public GTN_MultiBlockTooltipBuilder addInputHatch(int count, int dot) {
        addHatch(INPUT_HATCH, count, dot);
        return this;
    }

    public GTN_MultiBlockTooltipBuilder addOutputBus(int count, int dot) {
        addHatch(OUTPUT_BUS, count, dot);
        return this;
    }

    public GTN_MultiBlockTooltipBuilder addOutputHatch(int count, int dot) {
        addHatch(OUTPUT_HATCH, count, dot);
        return this;
    }

    public GTN_MultiBlockTooltipBuilder addExoticEnergyHatch(int count, int dot) {
        addHatch(EXOTIC_ENERGY_HATCH, count, dot);
        return this;
    }

    public GTN_MultiBlockTooltipBuilder addExoticOrEnergyHatch(int count, int dot) {
        addHatch(EXOTIC_OR_ENERGY_HATCH, count, dot);
        return this;
    }

    public GTN_MultiBlockTooltipBuilder addDynamoOrBufferedHatch(int count, int dot) {
        addHatch(DYNAMO_OR_BUFFERED_HATCH, count, dot);
        return this;
    }

    public GTN_MultiBlockTooltipBuilder addManaHatch(int count, int dot) {
        addHatch(MANA_HATCH, count, dot);
        return this;
    }

    public GTN_MultiBlockTooltipBuilder addAspectHatch(int count, int dot) {
        addHatch(ASPECT_HATCH, count, dot);
        return this;
    }

    public GTN_MultiBlockTooltipBuilder addMeAspectHatch(int count, int dot) {
        addHatch(ME_ASPECT_HATCH, count, dot);
        return this;
    }

    public GTN_MultiBlockTooltipBuilder addSteamHatch() {
        addSteamHatch(1, 1);
        return this;
    }

    public GTN_MultiBlockTooltipBuilder addSteamInputBus() {
        addSteamInputBus(1, 1);
        return this;
    }

    public GTN_MultiBlockTooltipBuilder addSteamOutputBus() {
        addSteamOutputBus(1, 1);
        return this;
    }

    public GTN_MultiBlockTooltipBuilder addEnergyHatch() {
        addEnergyHatch(1, 1);
        return this;
    }

    public GTN_MultiBlockTooltipBuilder addDynamoHatch() {
        addDynamoHatch(1, 1);
        return this;
    }

    public GTN_MultiBlockTooltipBuilder addMaintenanceHatch() {
        addMaintenanceHatch(1, 1);
        return this;
    }

    public GTN_MultiBlockTooltipBuilder addMufflerHatch() {
        addMufflerHatch(1, 1);
        return this;
    }

    public GTN_MultiBlockTooltipBuilder addInputBus() {
        addInputBus(1, 1);
        return this;
    }

    public GTN_MultiBlockTooltipBuilder addInputHatch() {
        addInputHatch(1, 1);
        return this;
    }

    public GTN_MultiBlockTooltipBuilder addOutputBus() {
        addOutputBus(1, 1);
        return this;
    }

    public GTN_MultiBlockTooltipBuilder addOutputHatch() {
        addOutputHatch(1, 1);
        return this;
    }

    public GTN_MultiBlockTooltipBuilder addExoticEnergyHatch() {
        addExoticEnergyHatch(1, 1);
        return this;
    }

    public GTN_MultiBlockTooltipBuilder addExoticOrEnergyHatch() {
        addExoticOrEnergyHatch(1, 1);
        return this;
    }

    public GTN_MultiBlockTooltipBuilder addDynamoOrBufferedHatch() {
        addDynamoOrBufferedHatch(1, 1);
        return this;
    }

    public GTN_MultiBlockTooltipBuilder addManaHatch() {
        addManaHatch(1, 1);
        return this;
    }

    public GTN_MultiBlockTooltipBuilder addAspectHatch() {
        addAspectHatch(1, 1);
        return this;
    }

    public GTN_MultiBlockTooltipBuilder addMeAspectHatch() {
        addMeAspectHatch(1, 1);
        return this;
    }

    public MultiblockTooltipBuilder addMultiBlockAreaInfoWithName(String name, int w, int h, int l) {
        MultiblockTooltipBuilderAccessor accessor = (MultiblockTooltipBuilderAccessor) this;

        List<String> sLines = accessor.getSLines();

        sLines.add(
            EnumChatFormatting.GRAY + DIMENSIONS
                + (name.isEmpty() ? "" : EnumChatFormatting.AQUA + " " + name + EnumChatFormatting.GRAY)
                + COLON
                + EnumChatFormatting.GOLD
                + w
                + EnumChatFormatting.GRAY
                + "x"
                + EnumChatFormatting.GOLD
                + h
                + EnumChatFormatting.GRAY
                + "x"
                + EnumChatFormatting.GOLD
                + l
                + EnumChatFormatting.GRAY
                + " ("
                + EnumChatFormatting.GOLD
                + "W"
                + EnumChatFormatting.GRAY
                + "x"
                + EnumChatFormatting.GOLD
                + "H"
                + EnumChatFormatting.GRAY
                + "x"
                + EnumChatFormatting.GOLD
                + "L"
                + EnumChatFormatting.GRAY
                + ")");
        return this;
    }

    public MultiblockTooltipBuilder addMultiBlockAreaInfo(int w, int h, int l) {
        addMultiBlockAreaInfoWithName("", w, h, l);
        return this;
    }

    public MultiblockTooltipBuilder beginStructureBlock() {
        MultiblockTooltipBuilderAccessor accessor = (MultiblockTooltipBuilderAccessor) this;
        List<String> sLines = accessor.getSLines();
        sLines.add(EnumChatFormatting.GRAY + STRUCTURE + COLON);
        return this;
    }

    public MultiblockTooltipBuilder toolTipFinisher(@Nullable String... authors) {
        return toolTipFinisher(EnumChatFormatting.GRAY, 41, authors);
    }

    public MultiblockTooltipBuilder toolTipFinisher(EnumChatFormatting separatorColor, int separatorLength,
        @Nullable String... authors) {
        MultiblockTooltipBuilderAccessor accessor = (MultiblockTooltipBuilderAccessor) this;

        List<String> iLines = accessor.getILines();
        List<String> hLines = accessor.getHLines();
        List<String> sLines = accessor.getSLines();
        String[] iArray = accessor.getIArray();
        String[] sArray = accessor.getSArray();
        String[] hArray = accessor.getHArray();
        SetMultimap<Integer, String> hBlocks = accessor.getHBlocks();

        switch (GTMod.proxy.tooltipFinisherStyle) {
            case 0 -> {}
            case 1 -> iLines.add(" ");
            case 2 -> iLines.add(separatorColor + StringUtils.getRepetitionOf('-', separatorLength));
            default -> iLines.add(
                separatorColor.toString() + EnumChatFormatting.STRIKETHROUGH
                    + StringUtils.getRepetitionOf('-', separatorLength));
        }

        iLines.add(
            HOLD + " "
                + EnumChatFormatting.BOLD
                + "[LSHIFT]"
                + EnumChatFormatting.RESET
                + EnumChatFormatting.GRAY
                + " "
                + DISPLAY);
        if (authors != null && authors.length > 0) {
            final String authorTag = "Author: ";
            final StringBuilder sb = new StringBuilder();
            sb.append(EnumChatFormatting.GRAY);
            sb.append(ADDED_BY);
            sb.append(COLON);
            for (int i = 0; i < authors.length; i++) {
                String author = authors[i];
                if (author.startsWith(authorTag)) {
                    // to support all the values in GTValues
                    // that already have Author at the start
                    sb.append(author.substring(authorTag.length()));
                } else {
                    sb.append(author);
                }
                if (i != authors.length - 1) {
                    sb.append(EnumChatFormatting.RESET);
                    sb.append(EnumChatFormatting.GRAY);
                    sb.append(" & ");
                    sb.append(EnumChatFormatting.GREEN);
                }
            }
            iLines.add(sb.toString());
        }
        hLines.add(STRUCTURE_HINT);
        this.addStructureInfoSeparator(EnumChatFormatting.GRAY, 30, true);
        // create the final arrays
        accessor.setIArray(iLines.toArray(new String[0]));
        accessor.setSArray(sLines.toArray(new String[0]));

        accessor.setHArray(
            Stream.concat(
                hLines.stream(),
                hBlocks.asMap()
                    .entrySet()
                    .stream()
                    .map(e -> DOTS[e.getKey() - 1] + COLON + String.join(SEPARATOR, e.getValue())))
                .toArray(String[]::new));
        // free memory
        iLines = null;
        sLines = null;
        hLines = null;
        hBlocks = null;
        return this;
    }
}
