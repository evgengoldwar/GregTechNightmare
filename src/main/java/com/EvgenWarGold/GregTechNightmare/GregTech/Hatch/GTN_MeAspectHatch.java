package com.EvgenWarGold.GregTechNightmare.GregTech.Hatch;

import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.EvgenWarGold.GregTechNightmare.GregTech.Textures.GTN_BlockIcons;
import com.EvgenWarGold.GregTechNightmare.ModBlocks.ThaumcraftBlocks;
import com.EvgenWarGold.GregTechNightmare.Utils.Constants;
import com.EvgenWarGold.GregTechNightmare.Utils.GTN_Utils;

import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.util.IterationCounter;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.IIconContainer;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatch;
import gregtech.api.render.TextureFactory;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumicenergistics.common.storage.AEEssentiaStack;

@IMetaTileEntity.SkipGenerateDescription
public class GTN_MeAspectHatch extends MTEHatch implements IAspectContainer, IActionHost, IGridProxyable {

    private int texturePage = 0;
    private int textureIndex = 0;
    private static final IIconContainer textureFont = GTN_BlockIcons.ASPECT_HATCH_OVERLAY;
    private static final IIconContainer textureFont_Glow = GTN_BlockIcons.ASPECT_HATCH_OVERLAY;
    private static final String TAG_TEXTURE_PAGE = "texturePage";
    private static final String TAG_TEXTURE_INDEX = "textureIndex";
    private static ITexture cachedArcaneStoneBlockTexture = null;

    private final MachineSource machineSource;
    private AENetworkProxy networkProxy = null;
    private IMEMonitor<AEEssentiaStack> monitor = null;
    private boolean isOnline = false;
    private boolean isPowered = false;
    private int tickCounter = 0;

    public GTN_MeAspectHatch(int aID, String aName) {
        super(aID, aName, aName, 3, 0, "");
        this.machineSource = new MachineSource(this);
    }

    public GTN_MeAspectHatch(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, aTier, 0, aDescription, aTextures);
        this.machineSource = new MachineSource(this);
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
        data.setBoolean("isOnline", isOnline);
        data.setBoolean("isPowered", isPowered);
        data.setInteger(TAG_TEXTURE_INDEX, getTextureIndex());
        data.setInteger(TAG_TEXTURE_PAGE, getTexturePage());
        return data;
    }

    @Override
    public void onDescriptionPacket(NBTTagCompound data) {
        isOnline = data.getBoolean("isOnline");
        isPowered = data.getBoolean("isPowered");
        textureIndex = data.getInteger(TAG_TEXTURE_INDEX);
        texturePage = data.getInteger(TAG_TEXTURE_PAGE);
    }

    @Override
    public String[] getDescription() {
        return new String[] { GTN_Utils.tr("GTN.Hatch.MeAspectHatch.tooltip.00"),
            GTN_Utils.tr("GTN.Hatch.MeAspectHatch.tooltip.01"),
            GTN_Utils.tr("GTN.Hatch.common.addedBy", Constants.MOD_NAME) };
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        if (networkProxy != null && aNBT.hasKey("networkProxy")) {
            networkProxy.readFromNBT(aNBT.getCompoundTag("networkProxy"));
        }

        texturePage = aNBT.getInteger(TAG_TEXTURE_PAGE);
        textureIndex = aNBT.getInteger(TAG_TEXTURE_INDEX);
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        if (networkProxy != null) {
            NBTTagCompound proxyTag = new NBTTagCompound();
            networkProxy.writeToNBT(proxyTag);
            aNBT.setTag("networkProxy", proxyTag);
        }

        aNBT.setInteger(TAG_TEXTURE_INDEX, textureIndex);
        aNBT.setInteger(TAG_TEXTURE_PAGE, texturePage);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new GTN_MeAspectHatch(mName, mTier, mDescriptionArray, mTextures);
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
                background = getArcaneStoneBlockTexture();
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

    private ITexture getArcaneStoneBlockTexture() {
        if (cachedArcaneStoneBlockTexture != null) {
            return cachedArcaneStoneBlockTexture;
        }

        try {
            final IIcon livingRockIcon = ThaumcraftBlocks.ArcaneStoneBlock.get()
                .getIcon(0, 6);

            cachedArcaneStoneBlockTexture = TextureFactory.of(new IIconContainer() {

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

            return cachedArcaneStoneBlockTexture;
        } catch (Exception e) {
            return TextureFactory.of(textureFont);
        }
    }

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        super.onPostTick(aBaseMetaTileEntity, aTick);

        if (aBaseMetaTileEntity.isServerSide()) {
            if (networkProxy == null) {
                initNetworkProxy();
            }

            tickCounter++;

            if (tickCounter % 20 == 0) {
                if (isOnline && isPowered) {
                    updateMonitor();
                }
            }

            if (tickCounter >= 20) {
                tickCounter = 0;
            }
        }
    }

    private void initNetworkProxy() {
        IGregTechTileEntity baseTE = this.getBaseMetaTileEntity();
        if (baseTE == null || baseTE.getWorld() == null) return;

        if (networkProxy == null) {
            networkProxy = new AENetworkProxy(this, "proxy", this.getStackForm(1), true);
            networkProxy.setFlags(GridFlags.REQUIRE_CHANNEL);
            networkProxy.setIdlePowerUsage(1.0);
            networkProxy.setValidSides(
                EnumSet.of(
                    ForgeDirection.getOrientation(
                        baseTE.getFrontFacing()
                            .ordinal())));
        }

        if (!networkProxy.isReady() && baseTE.getWorld() != null) {
            networkProxy.onReady();
        }
    }

    @SuppressWarnings("unchecked")
    private void updateMonitor() {
        IGrid grid;
        if (networkProxy == null || !networkProxy.isActive()) {
            this.monitor = null;
            return;
        }

        IGridNode node = networkProxy.getNode();
        if (node != null) {
            grid = node.getGrid();
            if (grid != null) {
                IStorageGrid storage = grid.getCache(IStorageGrid.class);
                this.monitor = (IMEMonitor<AEEssentiaStack>) storage
                    .getMEMonitor(thaumicenergistics.common.storage.AEEssentiaStackType.ESSENTIA_STACK_TYPE);
            }
        }
    }

    public long getAspectAmountInNetwork(Aspect aspect) {
        if (!isOnline || !isPowered || monitor == null || aspect == null) {
            return 0;
        }

        AEEssentiaStack stack = monitor.getAvailableItem(new AEEssentiaStack(aspect), IterationCounter.fetchNewId());

        return stack != null ? stack.getStackSize() : 0;
    }

    public long extractEssentia(Aspect aspect, long amount, boolean simulate) {
        if (!isOnline || !isPowered || monitor == null || aspect == null || amount <= 0) {
            return 0;
        }

        Actionable mode = simulate ? Actionable.SIMULATE : Actionable.MODULATE;

        AEEssentiaStack extracted = monitor.extractItems(new AEEssentiaStack(aspect, amount), mode, this.machineSource);

        return extracted != null ? extracted.getStackSize() : 0;
    }

    public boolean consumeEssentiaList(AspectList aspects, boolean simulate) {
        if (aspects == null || aspects.size() == 0) return true;

        for (Aspect aspect : aspects.getAspects()) {
            if (aspect == null) continue;
            long needed = aspects.getAmount(aspect);
            long available = getAspectAmountInNetwork(aspect);
            if (available < needed) return false;
        }

        if (!simulate) {
            for (Aspect aspect : aspects.getAspects()) {
                if (aspect == null) continue;
                long needed = aspects.getAmount(aspect);
                extractEssentia(aspect, needed, false);
            }
        }

        return true;
    }

    @Override
    public IGridNode getActionableNode() {
        return networkProxy != null ? networkProxy.getNode() : null;
    }

    @Override
    public IGridNode getGridNode(ForgeDirection dir) {
        return networkProxy != null ? networkProxy.getNode() : null;
    }

    @Override
    public AECableType getCableConnectionType(ForgeDirection dir) {
        return AECableType.SMART;
    }

    @Override
    public void securityBreak() {
        IGregTechTileEntity baseTE = this.getBaseMetaTileEntity();
        if (baseTE != null) {
            baseTE.getWorld()
                .createExplosion(
                    null,
                    baseTE.getXCoord() + 0.5,
                    baseTE.getYCoord() + 0.5,
                    baseTE.getZCoord() + 0.5,
                    0.0f,
                    true);
        }
    }

    @Override
    public DimensionalCoord getLocation() {
        IGregTechTileEntity baseTE = this.getBaseMetaTileEntity();
        if (baseTE != null) {
            return new DimensionalCoord(baseTE.getWorld(), baseTE.getXCoord(), baseTE.getYCoord(), baseTE.getZCoord());
        }
        return new DimensionalCoord(null, 0, 0, 0);
    }

    @Override
    public AENetworkProxy getProxy() {
        return networkProxy;
    }

    @Override
    public void gridChanged() {
        if (networkProxy != null) {
            isOnline = networkProxy.isActive();
            isPowered = networkProxy.isPowered();
            updateMonitor();
        }
    }

    @SuppressWarnings("unused")
    @MENetworkEventSubscribe
    public void onChannelEvent(MENetworkChannelsChanged event) {
        if (networkProxy != null) {
            this.isOnline = networkProxy.isActive();
            updateMonitor();
            markDirty();
        }
    }

    @SuppressWarnings("unused")
    @MENetworkEventSubscribe
    public void onPowerEvent(MENetworkPowerStatusChange event) {
        if (networkProxy != null) {
            this.isPowered = networkProxy.isPowered();
            markDirty();
        }
    }

    @Override
    public AspectList getAspects() {
        return new AspectList();
    }

    @Override
    public void setAspects(AspectList aspectList) {}

    @Override
    public boolean doesContainerAccept(Aspect aspect) {
        return true;
    }

    @Override
    public int addToContainer(Aspect aspect, int amount) {
        return amount;
    }

    @Override
    public boolean takeFromContainer(Aspect aspect, int amount) {
        long extracted = extractEssentia(aspect, amount, false);

        return extracted >= amount;
    }

    @Override
    public boolean takeFromContainer(AspectList aspects) {
        return consumeEssentiaList(aspects, false);
    }

    @Override
    public boolean doesContainerContainAmount(Aspect aspect, int amount) {
        return getAspectAmountInNetwork(aspect) >= amount;
    }

    @Override
    public boolean doesContainerContain(AspectList aspectList) {
        for (Aspect aspect : aspectList.getAspects()) {
            if (aspect == null) continue;
            if (getAspectAmountInNetwork(aspect) < aspectList.getAmount(aspect)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int containerContains(Aspect aspect) {
        return (int) getAspectAmountInNetwork(aspect);
    }

    public boolean isOnline() {
        return isOnline && isPowered;
    }

    public void markDirty() {
        IGregTechTileEntity baseTE = this.getBaseMetaTileEntity();
        if (baseTE != null) {
            baseTE.markDirty();
        }
    }

    @Override
    public void onFirstTick(IGregTechTileEntity aBaseMetaTileEntity) {
        super.onFirstTick(aBaseMetaTileEntity);
        initNetworkProxy();
        if (networkProxy != null) {
            networkProxy.onReady();
        }
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        if (networkProxy != null) {
            networkProxy.invalidate();
        }
    }

    @Override
    public void getWailaNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, int x, int y,
        int z) {
        if (te instanceof IGregTechTileEntity gte) {
            IMetaTileEntity mte = gte.getMetaTileEntity();
            if (mte instanceof GTN_MeAspectHatch hatch) {
                tag.setBoolean("isOnline", hatch.isOnline());
            }
        }
    }

    @Override
    public void getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {

        NBTTagCompound tag = accessor.getNBTData();

        if (tag.getBoolean("isOnline")) {
            currenttip.add(GTN_Utils.tr("GTN.Hatch.MeAspectHatch.waila.online"));
        } else {
            currenttip.add(GTN_Utils.tr("GTN.Hatch.MeAspectHatch.waila.offline"));
        }
    }
}
