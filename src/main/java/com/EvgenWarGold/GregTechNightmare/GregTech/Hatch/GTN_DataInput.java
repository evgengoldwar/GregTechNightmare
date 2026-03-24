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
import tectech.thing.metaTileEntity.pipe.MTEPipeData;
import tectech.thing.metaTileEntity.pipe.MTEPipeLaser;

import java.util.ArrayList;
import java.util.List;

public class GTN_DataInput extends AbstractGtnHatchDataConnector<NBTDataPacket> {

    private boolean delDelay = true;

    public GTN_DataInput(int aID, String aName) {
        super(aID, aName, aName, 3, new String[]{"Data Input"});
    }

    public GTN_DataInput(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, aTier, aDescription, aTextures);
    }

    @Override
    protected NBTDataPacket loadPacketFromNBT(NBTTagCompound nbt) {
        return new NBTDataPacket(nbt);
    }

    @Override
    public void moveAround(IGregTechTileEntity aBaseMetaTileEntity) {
        if (delDelay) {
            delDelay = false;
        } else {
            setContents(null);
        }
    }

    public void setContents(NBTDataPacket packet) {
        if (packet == null || packet.getContent() == null
            || packet.getContent()
                .tagCount() == 0) {
            this.data = null;
        } else {
            this.data = packet;
            delDelay = true;
        }
    }

    @Override
    public boolean canConnectData(ForgeDirection side) {
        return isInputFacing(side);
    }

    @Override
    public IConnectsToDataPipe getNext(IConnectsToDataPipe source) {
        return null;
    }

    @Override
    public boolean isDataInputFacing(ForgeDirection side) {
        return isInputFacing(side);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new GTN_DataInput(this.mName, this.mTier, this.mDescriptionArray, this.mTextures);
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
