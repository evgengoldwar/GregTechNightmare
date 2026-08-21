package com.EvgenWarGold.GregTechNightmare.Mixins.Late;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.EvgenWarGold.GregTechNightmare.GregTech.MultiBlock.MultiBlockClasses.GTN_MultiBlockBase;

import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.common.blocks.ItemMachines;

@Mixin(value = ItemMachines.class, remap = false)
public abstract class ItemMachinesMixin {

    @Inject(method = "isSkipGenerateDescription", at = @At("HEAD"), cancellable = true)
    private static void gtn$skipGeneratedDescriptionForMultiblocks(IMetaTileEntity metaTileEntity,
        CallbackInfoReturnable<Boolean> cir) {
        if (metaTileEntity instanceof GTN_MultiBlockBase<?>) {
            cir.setReturnValue(true);
        }
    }
}
