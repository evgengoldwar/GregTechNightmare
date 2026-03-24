package com.EvgenWarGold.GregTechNightmare.GregTech.Hatch;

import com.EvgenWarGold.GregTechNightmare.GregTech.Mte.GTN_DataPipe;
import gregtech.api.metatileentity.BaseMetaPipeEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.ForgeDirection;

import com.EvgenWarGold.GregTechNightmare.GregTech.Api.AbstractGtnHatchDataConnector;
import com.EvgenWarGold.GregTechNightmare.GregTech.Api.Pipe.IConnectsToDataPipe;
import com.EvgenWarGold.GregTechNightmare.GregTech.Api.NBTDataPacket;

import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import tectech.mechanics.pipe.IConnectsToEnergyTunnel;

import java.util.ArrayList;
import java.util.List;

public class GTN_DataOutput extends AbstractGtnHatchDataConnector<NBTDataPacket> {

    public GTN_DataOutput(int aID, String aName) {
        super(aID, aName, aName, 3, new String[]{"Data Output"});
    }

    public GTN_DataOutput(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, aTier, aDescription, aTextures);
    }

    @Override
    protected NBTDataPacket loadPacketFromNBT(NBTTagCompound nbt) {
        return new NBTDataPacket(nbt);
    }

    @Override
    public void moveAround(IGregTechTileEntity aBaseMetaTileEntity) {
        IConnectsToDataPipe current = this, source = this, next;
        int range = 0;
        while ((next = current.getNext(source)) != null && range++ < 1000) {
            if (next instanceof GTN_DataInput input) {
                input.setContents(data);
                break;
            }
            source = current;
            current = next;
        }

        data = null;
    }

    public void providePacket(NBTDataPacket packet) {
        this.data = packet;
    }

    @Override
    public boolean canConnectData(ForgeDirection side) {
        return isInputFacing(side);
    }

    @Override
    public IConnectsToDataPipe getNext(IConnectsToDataPipe source /* ==this */) {
        IGregTechTileEntity base = getBaseMetaTileEntity();
        byte color = base.getColorization();
        if (color < 0) {
            return null;
        }
        IGregTechTileEntity next = base.getIGregTechTileEntityAtSide(base.getFrontFacing());
        if (next == null) {
            return null;
        }
        IMetaTileEntity meta = next.getMetaTileEntity();
        if (meta instanceof GTN_DataPipe pipe) {
            pipe.markUsed();
            return (IConnectsToDataPipe) meta;
        } else if (meta instanceof GTN_DataInput && ((GTN_DataInput) meta).getColorization() == color
            && ((GTN_DataInput) meta).canConnectData(
                base.getFrontFacing()
                    .getOpposite())) {
                        return (IConnectsToDataPipe) meta;
                    }
        return null;
    }

    @Override
    public boolean isDataInputFacing(ForgeDirection side) {
        return isInputFacing(side);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new GTN_DataOutput(this.mName, this.mTier, this.mDescriptionArray, this.mTextures);
    }

    @Override
    public void onColorChangeServer(byte aColor) {
        final IGregTechTileEntity gte = getBaseMetaTileEntity();
        if (gte != null) {
            for (final ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
                IGregTechTileEntity gteSide = gte.getIGregTechTileEntityAtSide(side);
                if (gteSide != null && gteSide.getMetaTileEntity() instanceof GTN_DataPipe pipe) pipe.updateNetwork(true);
            }
        }
    }

    @Override
    public void onBlockDestroyed() {
        final IGregTechTileEntity gte = getBaseMetaTileEntity();
        if (gte != null) {
            for (final ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
                final IGregTechTileEntity gteSide = gte.getIGregTechTileEntityAtSide(side);
                if (gteSide != null && gteSide.getMetaTileEntity() instanceof GTN_DataPipe neighbor
                    && neighbor.isConnectedAtSide(side.getOpposite())) {
                    neighbor.mConnections &= (byte) ~side.getOpposite().flag;
                    neighbor.connectionCount--;
                }
            }
        }
    }

    @Override
    public String[] getInfoData() {
        if (data == null || data.getContent() == null || data.getContent().tagCount() == 0) {
            return new String[]{EnumChatFormatting.RED + "No data in packet"};
        }

        NBTTagList nbt = data.getContent();
        List<String> info = new ArrayList<>();
        info.add(EnumChatFormatting.AQUA + "NBTDataPacket Contents:");
        info.add("Count tag list: " + nbt.tagCount());

        return info.toArray(new String[0]);
    }
}
