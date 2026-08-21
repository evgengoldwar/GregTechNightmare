package com.EvgenWarGold.GregTechNightmare.Mixins;

import javax.annotation.Nonnull;

import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import com.gtnewhorizon.gtnhmixins.builders.MixinBuilder;

public enum Mixins implements IMixins {

    MINECRAFT_LATE(new MixinBuilder("Minecraft Late")
        .addCommonMixins(
            "MTEHatchCraftingInputMEMixin",
            "MTEHatchCraftingInputMEPatternSlotMixin",
            "MTEHatchPatternProviderMixin")
        .addClientMixins(
            "FixTreeSprouterCrushMixins",
            "MTEMultiblockBaseMixins",
            "MTEAdvancedDebugStructureWriterMixins",
            "FixGalaxySpaceCapeCrushMixins",
            "MultiblockTooltipBuilderAccessor",
            "ItemMachinesMixin",
            "EntityManaBurstMixin",
            "TileSpreaderMixin",
            "HUDHandlerBotaniaMixin",
            "TileTubeMixin",
            "ThaumcraftApiHelperMixin",
            "TileJarFillableMixin",
            "BlockTubeRendererMixin")
        // "FixMultiblockBuilderMixins")
        .setPhase(Phase.LATE)),

    // MINECRAFT_EARLY(new MixinBuilder("Minecraft Early").addClientMixins("Test", "Test")
    // .setPhase(Phase.EARLY))
    ;

    private final MixinBuilder builder;

    Mixins(MixinBuilder builder) {
        this.builder = builder;
    }

    @Nonnull
    @Override
    public MixinBuilder getBuilder() {
        return builder;
    }
}
