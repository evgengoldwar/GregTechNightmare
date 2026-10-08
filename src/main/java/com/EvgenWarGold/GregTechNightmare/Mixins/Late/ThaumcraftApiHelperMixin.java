package com.EvgenWarGold.GregTechNightmare.Mixins.Late;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.IEssentiaTransport;

@Mixin(value = ThaumcraftApiHelper.class, remap = false)
public class ThaumcraftApiHelperMixin {

    /**
     * @author EvgenWarGold
     * @reason Overwrite getConnectableTile
     */
    @Overwrite
    public static TileEntity getConnectableTile(World world, int x, int y, int z, ForgeDirection face) {
        TileEntity te = world.getTileEntity(x + face.offsetX, y + face.offsetY, z + face.offsetZ);
        if (te == null) {
            return null;
        }

        if (te instanceof IEssentiaTransport) {
            IEssentiaTransport essentiaTransport = (IEssentiaTransport) te;
            return essentiaTransport.isConnectable(face.getOpposite()) ? te : null;
        }

        if (te instanceof IGregTechTileEntity) {
            IMetaTileEntity mte = ((IGregTechTileEntity) te).getMetaTileEntity();
            if (mte instanceof IEssentiaTransport) {
                return ((IEssentiaTransport) mte).isConnectable(face.getOpposite()) ? te : null;
            }
        }

        return null;
    }

    /**
     * @author EvgenWarGold
     * @reason Overwrite getConnectableTile
     */
    @Overwrite
    public static TileEntity getConnectableTile(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
        TileEntity te = world.getTileEntity(x + face.offsetX, y + face.offsetY, z + face.offsetZ);
        if (te == null) {
            return null;
        }

        if (te instanceof IEssentiaTransport) {
            IEssentiaTransport essentiaTransport = (IEssentiaTransport) te;
            return essentiaTransport.isConnectable(face.getOpposite()) ? te : null;
        }

        if (te instanceof IGregTechTileEntity) {
            IMetaTileEntity mte = ((IGregTechTileEntity) te).getMetaTileEntity();
            if (mte instanceof IEssentiaTransport) {
                return ((IEssentiaTransport) mte).isConnectable(face.getOpposite()) ? te : null;
            }
        }

        return null;
    }
}
