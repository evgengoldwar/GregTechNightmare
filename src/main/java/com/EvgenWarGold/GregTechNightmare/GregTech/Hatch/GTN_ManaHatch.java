package com.EvgenWarGold.GregTechNightmare.GregTech.Hatch;

import java.lang.reflect.Field;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.EvgenWarGold.GregTechNightmare.GregTech.Textures.GTN_BlockIcons;
import com.EvgenWarGold.GregTechNightmare.Utils.Constants;
import com.EvgenWarGold.GregTechNightmare.Utils.GTN_Utils;

import gregtech.api.enums.Textures;
import gregtech.api.interfaces.IIconContainer;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatch;
import gregtech.api.render.TextureFactory;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.api.mana.ManaNetworkEvent;
import vazkii.botania.api.wand.IWandHUD;
import vazkii.botania.client.core.handler.HUDHandler;
import vazkii.botania.common.block.ModBlocks;

@IMetaTileEntity.SkipGenerateDescription
public class GTN_ManaHatch extends MTEHatch implements IManaPool, IWandHUD {

    private final static int MAX_MANA = 1_000_000;
    private int mana = 0;
    private int texturePage = 0;
    private int textureIndex = 0;
    private boolean registered = false;
    private static final String TAG_MANA = "mana";
    private static final String TAG_TEXTURE_PAGE = "texturePage";
    private static final String TAG_TEXTURE_INDEX = "textureIndex";

    private static final IIconContainer textureFont = GTN_BlockIcons.MANA_HATCH_OVERLAY;
    private static final IIconContainer textureFont_Glow = GTN_BlockIcons.MANA_HATCH_OVERLAY;
    private static ITexture cachedLivingRockTexture = null;

    public GTN_ManaHatch(int aID, String aName) {
        super(aID, aName, aName, 3, 0, "");
    }

    public GTN_ManaHatch(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
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
    public NBTTagCompound getDescriptionData() {
        NBTTagCompound data = new NBTTagCompound();
        data.setInteger(TAG_MANA, mana);
        data.setInteger(TAG_TEXTURE_INDEX, getTextureIndex());
        data.setInteger(TAG_TEXTURE_PAGE, getTexturePage());
        return data;
    }

    @Override
    public void onDescriptionPacket(NBTTagCompound data) {
        mana = data.getInteger(TAG_MANA);
        textureIndex = data.getInteger(TAG_TEXTURE_INDEX);
        texturePage = data.getInteger(TAG_TEXTURE_PAGE);
    }

    @Override
    public String[] getDescription() {
        return new String[] { GTN_Utils.tr("GTN.Hatch.ManaHatch.tooltip.00"),
            GTN_Utils.tr("GTN.Hatch.ManaHatch.tooltip.01"), GTN_Utils.tr("GTN.Hatch.ManaHatch.tooltip.02"),
            GTN_Utils.tr("GTN.Hatch.ManaHatch.tooltip.03"),
            GTN_Utils.tr("GTN.Hatch.common.addedBy", Constants.MOD_NAME) };
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        mana = aNBT.getInteger(TAG_MANA);
        texturePage = aNBT.getInteger(TAG_TEXTURE_PAGE);
        textureIndex = aNBT.getInteger(TAG_TEXTURE_INDEX);
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        aNBT.setInteger(TAG_MANA, mana);
        aNBT.setInteger(TAG_TEXTURE_INDEX, textureIndex);
        aNBT.setInteger(TAG_TEXTURE_PAGE, texturePage);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new GTN_ManaHatch(mName, mTier, mDescriptionArray, mTextures);
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

    private int getTexturePage() {
        try {
            Field field = MTEHatch.class.getDeclaredField("texturePage");
            field.setAccessible(true);
            return field.getInt(this);
        } catch (Exception e) {
            return 0;
        }
    }

    private int getTextureIndex() {
        try {
            Field field = MTEHatch.class.getDeclaredField("textureIndex");
            field.setAccessible(true);
            return field.getInt(this);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, ForgeDirection side, ForgeDirection aFacing,
        int colorIndex, boolean aActive, boolean redstoneLevel) {

        try {
            ITexture background;

            if (texturePage == 0) texturePage = getTexturePage();
            if (textureIndex == 0) textureIndex = getTextureIndex();

            if (texturePage > 0 || textureIndex > 0) {
                background = Textures.BlockIcons.casingTexturePages[texturePage][textureIndex];
            } else {
                background = getLivingRockTexture();
            }

            if (side == aFacing) {
                if (aActive) {
                    return new ITexture[] { background, TextureFactory.of(textureFont), TextureFactory.builder()
                        .addIcon(textureFont_Glow)
                        .glow()
                        .build() };
                } else {
                    return new ITexture[] { background, TextureFactory.of(textureFont) };
                }
            }

            return new ITexture[] { background };

        } catch (NullPointerException npe) {
            return new ITexture[] { Textures.BlockIcons.MACHINE_CASINGS[0][0] };
        }
    }

    private ITexture getLivingRockTexture() {
        if (cachedLivingRockTexture != null) {
            return cachedLivingRockTexture;
        }

        try {
            final IIcon livingRockIcon = ModBlocks.livingrock.getIcon(0, 0);

            cachedLivingRockTexture = TextureFactory.of(new IIconContainer() {

                @Override
                public IIcon getIcon() {
                    return livingRockIcon;
                }

                @Override
                public IIcon getOverlayIcon() {
                    return null;
                }

                @Override
                public ResourceLocation getTextureFile() {
                    return null;
                }
            });

            return cachedLivingRockTexture;
        } catch (Exception e) {
            return TextureFactory.of(textureFont);
        }
    }

    @Override
    public void onFirstTick(IGregTechTileEntity aBaseMetaTileEntity) {
        super.onFirstTick(aBaseMetaTileEntity);
        if (aBaseMetaTileEntity.isServerSide() && !registered) {
            ManaNetworkEvent.addPool((TileEntity) aBaseMetaTileEntity);
            registered = true;
        }
    }

    @Override
    public void inValidate() {
        if (getBaseMetaTileEntity() != null && getBaseMetaTileEntity().isServerSide()) {
            ManaNetworkEvent.removePool((BaseMetaTileEntity) getBaseMetaTileEntity());
            registered = false;
        }
        super.inValidate();
    }

    @Override
    public void onBlockDestroyed() {
        inValidate();
        super.onBlockDestroyed();
    }

    @Override
    public boolean isFull() {
        return getCurrentMana() >= MAX_MANA;
    }

    @Override
    public void recieveMana(int mana) {
        this.mana = Math.max(0, Math.min(getCurrentMana() + mana, MAX_MANA));
    }

    @Override
    public boolean canRecieveManaFromBursts() {
        return !isFull();
    }

    @Override
    public int getCurrentMana() {
        return this.mana;
    }

    @Override
    public boolean isOutputtingPower() {
        return false;
    }

    public boolean extractMana(int amount, boolean simulate) {
        if (amount <= 0) {
            return false;
        }

        int extracted = Math.min(amount, mana);
        boolean canExtract = amount <= mana;

        if (!simulate) {
            mana -= extracted;
        }

        return canExtract;
    }

    public boolean extractMana(int amount) {
        return extractMana(amount, false);
    }

    @Override
    public void renderHUD(Minecraft mc, ScaledResolution res, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof BaseMetaTileEntity baseTE) {
            if (baseTE.getMetaTileEntity() instanceof GTN_ManaHatch hatch) {
                int color = 0x00BFFF;
                HUDHandler.drawSimpleManaHUD(
                    color,
                    hatch.mana,
                    MAX_MANA,
                    StatCollector.translateToLocal("GTN.TooltipBuilder.ManaHatch"),
                    res);
            }
        }
    }
}
