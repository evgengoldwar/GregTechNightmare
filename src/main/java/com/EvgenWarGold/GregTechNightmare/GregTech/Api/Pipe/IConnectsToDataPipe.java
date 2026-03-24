package com.EvgenWarGold.GregTechNightmare.GregTech.Api.Pipe;

import net.minecraftforge.common.util.ForgeDirection;

public interface IConnectsToDataPipe {

    boolean canConnectData(ForgeDirection side);

    IConnectsToDataPipe getNext(IConnectsToDataPipe source);

    boolean isDataInputFacing(ForgeDirection side);

    byte getColorization();
}
