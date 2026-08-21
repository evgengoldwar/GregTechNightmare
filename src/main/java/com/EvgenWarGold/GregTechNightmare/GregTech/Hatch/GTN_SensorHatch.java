package com.EvgenWarGold.GregTechNightmare.GregTech.Hatch;

import static net.minecraft.util.StatCollector.translateToLocal;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import com.EvgenWarGold.GregTechNightmare.Utils.Constants;
import com.EvgenWarGold.GregTechNightmare.Utils.GTN_Utils;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.widget.TextWidget;
import com.gtnewhorizons.modularui.common.widget.textfield.NumericWidget;

import gregtech.api.enums.Textures;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.interfaces.IIconContainer;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatch;
import gregtech.api.render.TextureFactory;
import gregtech.common.gui.modularui.widget.CoverCycleButtonWidget;

@IMetaTileEntity.SkipGenerateDescription
public class GTN_SensorHatch extends MTEHatch {

    protected double threshold = 0;
    protected boolean inverted = false;
    private boolean isOn = false;

    private static final IIconContainer textureFont = Textures.BlockIcons.OVERLAY_HATCH_HEAT_SENSOR;
    private static final IIconContainer textureFont_Glow = Textures.BlockIcons.OVERLAY_HATCH_HEAT_SENSOR_GLOW;

    public GTN_SensorHatch(int aID, String aName) {
        super(aID, aName, aName, 3, 0, "");
    }

    public GTN_SensorHatch(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, aTier, 0, aDescription, aTextures);
    }

    @Override
    public boolean isValidSlot(int aIndex) {
        return false;
    }

    @Override
    public boolean isFacingValid(ForgeDirection facing) {
        return true;
    }

    @Override
    public boolean allowGeneralRedstoneOutput() {
        return true;
    }

    @Override
    public boolean allowPullStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection Side,
        ItemStack aStack) {
        return false;
    }

    @Override
    public boolean allowPutStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
        ItemStack aStack) {
        return false;
    }

    @Override
    public void initDefaultModes(NBTTagCompound aNBT) {
        getBaseMetaTileEntity().setActive(true);
    }

    @Override
    public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer, ForgeDirection side,
        float aX, float aY, float aZ) {
        openGui(aPlayer);
        return true;
    }

    @Override
    public String[] getDescription() {
        return new String[] { GTN_Utils.tr("GTN.Hatch.SensorHatch.tooltip.00"),
            GTN_Utils.tr("GTN.Hatch.SensorHatch.tooltip.01"),
            GTN_Utils.tr("GTN.Hatch.common.addedBy", Constants.MOD_NAME) };
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        threshold = aNBT.getDouble("mThreshold");
        inverted = aNBT.getBoolean("mInverted");
        super.loadNBTData(aNBT);
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        aNBT.setDouble("mThreshold", threshold);
        aNBT.setBoolean("mInverted", inverted);
        super.saveNBTData(aNBT);
    }

    public void updateRedstoneOutput(double heat) {
        isOn = (heat > threshold) ^ inverted;
    }

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        if (isOn) {
            for (final ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
                aBaseMetaTileEntity.setStrongOutputRedstoneSignal(side, (byte) 15);
            }
        } else {
            for (final ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
                aBaseMetaTileEntity.setStrongOutputRedstoneSignal(side, (byte) 0);
            }
        }
        super.onPostTick(aBaseMetaTileEntity, aTick);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new GTN_SensorHatch(mName, mTier, mDescriptionArray, mTextures);
    }

    @Override
    public ITexture[] getTexturesActive(ITexture aBaseTexture) {
        return new ITexture[] { aBaseTexture, TextureFactory.of(textureFont), TextureFactory.builder()
            .addIcon(textureFont_Glow)
            .glow()
            .build() };
    }

    @Override
    public ITexture[] getTexturesInactive(ITexture aBaseTexture) {
        return new ITexture[] { aBaseTexture, TextureFactory.of(textureFont) };
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public boolean isInverted() {
        return inverted;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }

    public void addUIWidgets(ModularWindow.Builder builder, UIBuildContext buildContext) {
        builder.widget(
            new CoverCycleButtonWidget().setToggle(() -> inverted, (val) -> inverted = val)
                .setTextureGetter(
                    (state) -> state == 1 ? GTUITextures.OVERLAY_BUTTON_REDSTONE_ON
                        : GTUITextures.OVERLAY_BUTTON_REDSTONE_OFF)
                .addTooltip(0, translateToLocal("gt.interact.desc.normal.tooltip"))
                .addTooltip(1, translateToLocal("gt.interact.desc.inverted.tooltip"))
                .setPos(10, 8))
            .widget(
                new TextWidget()
                    .setStringSupplier(
                        () -> inverted ? translateToLocal("gt.interact.desc.inverted")
                            : translateToLocal("gt.interact.desc.normal"))
                    .setDefaultColor(COLOR_TEXT_GRAY.get())
                    .setTextAlignment(Alignment.CenterLeft)
                    .setPos(28, 12))
            .widget(
                new NumericWidget().setBounds(0, 100)
                    .setIntegerOnly(false)
                    .setGetter(() -> threshold)
                    .setSetter((value) -> threshold = (float) value)
                    .setScrollValues(0.1, 0.01, 1.0)
                    .setMaximumFractionDigits(2)
                    .setTextColor(Color.WHITE.dark(1))
                    .setTextAlignment(Alignment.CenterLeft)
                    .setFocusOnGuiOpen(true)
                    .setBackground(GTUITextures.BACKGROUND_TEXT_FIELD.withOffset(-1, -1, 2, 2))
                    .setPos(10, 28)
                    .setSize(30, 12))
            .widget(
                new TextWidget(GTN_Utils.tr("GTN.gui.text.sensor")).setDefaultColor(COLOR_TEXT_GRAY.get())
                    .setTextAlignment(Alignment.CenterLeft)
                    .setPos(50, 30));
    }
}
