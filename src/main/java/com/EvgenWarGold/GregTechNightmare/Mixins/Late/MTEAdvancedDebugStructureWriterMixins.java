package com.EvgenWarGold.GregTechNightmare.Mixins.Late;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.iterate;
import static gregtech.GTLoggers.GT_FML_LOGGER;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses.GTN_Casings;
import com.gtnewhorizon.structurelib.alignment.enumerable.ExtendedFacing;

import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.common.blocks.BlockFrameBox;
import gregtech.common.tileentities.debug.MTEDebugStructureWriter;
import gtPlusPlus.core.block.base.BasicBlock;
import gtPlusPlus.core.block.base.BlockBaseModular;
import gtPlusPlus.core.material.Material;
import gtPlusPlus.core.material.MaterialsAlloy;

@Mixin(value = MTEDebugStructureWriter.class, remap = false)
public abstract class MTEAdvancedDebugStructureWriterMixins extends MetaTileEntity {

    private static final String NICE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz=|!@#$%&()[]{};:<>/?_,.*^'`";

    @Shadow(remap = false)
    private short[] numbers;

    @Shadow(remap = false)
    private boolean transpose;

    @Shadow(remap = false)
    private String[] result;

    public MTEAdvancedDebugStructureWriterMixins(int aID, String aBasicName, String aRegionalName, int aInvSlotCount) {
        super(aID, aBasicName, aRegionalName, aInvSlotCount);
    }

    public MTEAdvancedDebugStructureWriterMixins(String aName, int aInvSlotCount) {
        super(aName, aInvSlotCount);
    }

    /**
     * Overrides the structure printing method to add custom logic
     * with support for GTN_Casings and other mods
     *
     * @param aPlayer The player who invoked the method
     * @throws IllegalAccessException If there are errors accessing fields
     * @author EvgenWarGold
     * @reason Original method doesn't handle custom GTN_Casings and other mod blocks properly.
     *         This version adds support for GTN_Casings enumeration, FrameBox blocks from GT,
     *         and modular blocks from GT++, providing more accurate structure definitions
     *         for multiblock validation.
     */
    @Overwrite(remap = false)
    public void printStructure(EntityPlayer aPlayer) throws IllegalAccessException {
        IGregTechTileEntity aBaseMetaTileEntity = getBaseMetaTileEntity();
        String pseudoJavaCode = getPseudoJavaCode(
            aBaseMetaTileEntity.getWorld(),
            ExtendedFacing.of(aBaseMetaTileEntity.getFrontFacing()),
            aBaseMetaTileEntity.getXCoord(),
            aBaseMetaTileEntity.getYCoord(),
            aBaseMetaTileEntity.getZCoord(),
            numbers[0],
            numbers[1],
            numbers[2],
            te -> te.getClass()
                .getCanonicalName(),
            numbers[3],
            numbers[4],
            numbers[5],
            transpose);
        GT_FML_LOGGER.info(pseudoJavaCode);
        result = pseudoJavaCode.split("\\n");
        aPlayer.addChatMessage(new ChatComponentTranslation("Printed structure to console"));
    }

    private static String getPseudoJavaCode(World world, ExtendedFacing extendedFacing, int basePositionX,
        int basePositionY, int basePositionZ, int basePositionA, int basePositionB, int basePositionC,
        Function<? super TileEntity, String> tileEntityClassifier, int sizeA, int sizeB, int sizeC, boolean transpose)
        throws IllegalAccessException {

        Map<Block, Set<Integer>> blocks = new TreeMap<>(Comparator.comparing(Block::getUnlocalizedName));

        Set<Class<? extends TileEntity>> tiles = new HashSet<>();
        Set<String> specialTiles = new HashSet<>();

        boolean[] hasController = new boolean[1];
        int[] controllerPos = new int[3];

        Function<? super TileEntity, String> wrappedClassifier = tileEntity -> {
            if (tileEntity != null) {

                if (tileEntity.getClass()
                    .getName()
                    .contains("BaseMetaTileEntity")) {
                    try {
                        Object metaTileEntity = tileEntity.getClass()
                            .getMethod("getMetaTileEntity")
                            .invoke(tileEntity);

                        if (metaTileEntity != null) {
                            String mteClassName = metaTileEntity.getClass()
                                .getName();

                            if (mteClassName.contains("MTEAdvDebugStructureWriter") || metaTileEntity.getClass()
                                .getSimpleName()
                                .equals("MTEAdvDebugStructureWriter")) {
                                return "CONTROLLER";
                            }
                        }
                    } catch (Exception ignored) {}
                }

                String className = tileEntity.getClass()
                    .getName();

                if (className.contains("MTEAdvDebugStructureWriter") || tileEntity.getClass()
                    .getSimpleName()
                    .equals("MTEAdvDebugStructureWriter")) {
                    return "CONTROLLER";
                }
            }

            return tileEntityClassifier.apply(tileEntity);
        };

        iterate(
            world,
            extendedFacing,
            basePositionX,
            basePositionY,
            basePositionZ,
            basePositionA,
            basePositionB,
            basePositionC,
            sizeA,
            sizeB,
            sizeC,
            (w, x, y, z) -> {

                TileEntity tileEntity = w.getTileEntity(x, y, z);

                if (tileEntity == null) {

                    Block block = w.getBlock(x, y, z);

                    if (block != null && block != Blocks.air) {
                        blocks.compute(block, (b, set) -> {
                            if (set == null) {
                                set = new TreeSet<>();
                            }

                            set.add(block.getDamageValue(world, x, y, z));
                            return set;
                        });
                    }

                } else {

                    String classification = wrappedClassifier.apply(tileEntity);

                    if ("CONTROLLER".equals(classification)) {

                        hasController[0] = true;
                        controllerPos[0] = x;
                        controllerPos[1] = y;
                        controllerPos[2] = z;

                    } else if (classification == null) {

                        tiles.add(tileEntity.getClass());

                    } else {

                        specialTiles.add(classification);
                    }
                }
            });

        Map<String, Character> map = new HashMap<>();
        Map<String, String> casingMap = new HashMap<>();

        for (GTN_Casings casing : GTN_Casings.values()) {
            String key = casing.getBlock()
                .getUnlocalizedName() + '\0'
                + casing.getBlockMeta();

            casingMap.put(key, casing.name());
        }

        StringBuilder builder = new StringBuilder();

        int i = 0;
        char c;

        builder.append("\n")
            .append("Blocks:\n");

        for (Map.Entry<Block, Set<Integer>> entry : blocks.entrySet()) {

            Block block = entry.getKey();
            Set<Integer> metas = entry.getValue();

            for (Integer meta : metas) {

                c = NICE_CHARS.charAt(i++);

                if (i > NICE_CHARS.length()) {
                    return "Too complicated for nice chars";
                }

                String key = block.getUnlocalizedName() + '\0' + meta;
                map.put(key, c);

                String casingName = casingMap.get(key);

                if (casingName != null) {
                    builder.append(c)
                        .append(" -> ")
                        .append(".addElement('")
                        .append(c)
                        .append("', ")
                        .append("GTN_Casings.")
                        .append(casingName)
                        .append(".asElement())\n");

                } else if (block instanceof BlockFrameBox) {
                    String materialName = BlockFrameBox.getMaterial(meta)
                        .toString();

                    builder.append(c)
                        .append(" -> ")
                        .append(".addElement('")
                        .append(c)
                        .append("', ")
                        .append("ofFrame(Materials.")
                        .append(materialName)
                        .append("))\n");
                } else if (block instanceof BlockBaseModular blockBaseModular) {
                    if (BasicBlock.BlockTypes.FRAME == blockBaseModular.blockType) {
                        Field[] fields = MaterialsAlloy.class.getDeclaredFields();

                        for (Field field : fields) {
                            String materialName = blockBaseModular.getMaterialEx()
                                .getLocalizedName();
                            Material fieldMaterial = (Material) field.get(0);
                            String fieldMaterialName = fieldMaterial.getLocalizedName();

                            if (materialName.equals(fieldMaterialName)) {
                                builder.append(c)
                                    .append(" -> ")
                                    .append(".addElement('")
                                    .append(c)
                                    .append("', ")
                                    .append("GTN_StructureUtility.ofFrame(")
                                    .append("MaterialsAlloy.")
                                    .append(field.getName())
                                    .append("))\n");
                            }
                        }
                    }

                } else {
                    builder.append(c)
                        .append(" -> ")
                        .append(".addElement('")
                        .append(c)
                        .append("', ")
                        .append("ofBlock(\"")
                        .append(block.getUnlocalizedName())
                        .append("\", ")
                        .append(meta)
                        .append("))\n");
                }
            }
        }

        if (!tiles.isEmpty()) {
            builder.append("\nTiles:\n");

            for (Class<? extends TileEntity> tile : tiles) {

                c = NICE_CHARS.charAt(i++);

                if (i > NICE_CHARS.length()) {
                    return "Too complicated for nice chars";
                }

                map.put(tile.getCanonicalName(), c);

                builder.append(c)
                    .append(" -> ")
                    .append(".addElement('")
                    .append(c)
                    .append("', ")
                    .append("ofTileAdder(")
                    .append(tile)
                    .append("))\n");
            }
        }

        if (!specialTiles.isEmpty()) {
            builder.append("\nSpecial Tiles:\n");

            for (String tile : specialTiles) {

                c = NICE_CHARS.charAt(i++);

                if (i > NICE_CHARS.length()) {
                    return "Too complicated for nice chars";
                }

                map.put(tile, c);

                builder.append(c)
                    .append(" -> ")
                    .append(".addElement('")
                    .append(c)
                    .append("', ")
                    .append("ofSpecialTileAdder(")
                    .append(tile)
                    .append("))\n");
            }
        }

        builder.append("\nOFFSETS\n")
            .append("Horizontal: ")
            .append(basePositionA)
            .append('\n')
            .append("Vertical: ")
            .append(basePositionB)
            .append('\n')
            .append("Depth: ")
            .append(basePositionC)
            .append('\n');

        builder.append("\nMULTIBLOCK SIZE\n")
            .append("Width: ")
            .append(sizeA)
            .append('\n')
            .append("Height: ")
            .append(sizeB)
            .append('\n')
            .append("Length: ")
            .append(sizeC)
            .append('\n');

        builder.append("\nScan:\n\n")
            .append("return Arrays.asList(\n")
            .append("    new StructureVariant<>(\n")
            .append("        \"\",\n")
            .append("        // spotless:off\n")
            .append("        new String[][]{\n");

        List<String> allRows = new ArrayList<>();
        StringBuilder currentRow = new StringBuilder();

        iterate(
            world,
            extendedFacing,
            basePositionX,
            basePositionY,
            basePositionZ,
            basePositionA,
            basePositionB,
            basePositionC,
            true,
            sizeA,
            sizeB,
            sizeC,
            (w, x, y, z) -> {

                TileEntity tileEntity = w.getTileEntity(x, y, z);

                if (tileEntity != null) {

                    String classification = wrappedClassifier.apply(tileEntity);

                    if ("CONTROLLER".equals(classification)) {
                        currentRow.append('~');
                        return;
                    }
                }

                if (tileEntity == null) {

                    Block block = w.getBlock(x, y, z);

                    if (block != null && block != Blocks.air) {

                        String key = block.getUnlocalizedName() + '\0' + block.getDamageValue(world, x, y, z);

                        Character ch = map.get(key);

                        currentRow.append(ch != null ? ch : '?');

                    } else {
                        currentRow.append(' ');
                    }

                } else {

                    String classification = wrappedClassifier.apply(tileEntity);

                    if (classification == null) {
                        classification = tileEntity.getClass()
                            .getCanonicalName();
                    }

                    Character ch = map.get(classification);
                    currentRow.append(ch != null ? ch : '?');
                }
            },
            () -> {
                allRows.add(currentRow.toString());
                currentRow.setLength(0);
            },
            () -> {
            });

        int rowsPerLayer = sizeC;
        int layersCount = sizeB;

        for (int layer = 0; layer < layersCount; layer++) {
            builder.append("        {");

            for (int row = 0; row < rowsPerLayer; row++) {
                int index = layer * rowsPerLayer + row;
                if (index < allRows.size()) {
                    builder.append("\"").append(allRows.get(index)).append("\"");
                    if (row < rowsPerLayer - 1) {
                        builder.append(",");
                    }
                }
            }

            if (layer < layersCount - 1) {
                builder.append("},\n");
            } else {
                builder.append("}\n");
            }
        }

        builder.append("        },\n")
            .append("        //spotless:on\n")
            .append("        new MultiblockOffsets(")
            .append(basePositionA)
            .append(", ")
            .append(basePositionB)
            .append(", ")
            .append(basePositionC)
            .append("),\n")
            .append("        new MultiblockArea(")
            .append(sizeA)
            .append(", ")
            .append(sizeB)
            .append(", ")
            .append(sizeC)
            .append("),\n")
            .append("        1,\n")
            .append("        GTN_Casings.));\n");

        return builder.toString();
    }

}
