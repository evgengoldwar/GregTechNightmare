package com.EvgenWarGold.GregTechNightmare.GregTech.Api.Pipe;

import gregtech.api.interfaces.metatileentity.IMetaTileEntity;

public interface IActivePipe extends IMetaTileEntity {

    void setActive(boolean active);

    boolean getActive();

    void markUsed();
}
