package com.EvgenWarGold.GregTechNightmare.GregTech.Hatch;

import static mcp.mobius.waila.api.SpecialChars.ALIGNRIGHT;
import static mcp.mobius.waila.api.SpecialChars.TAB;
import static mcp.mobius.waila.api.SpecialChars.WHITE;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.EvgenWarGold.GregTechNightmare.GregTech.Textures.GTN_BlockIcons;
import com.EvgenWarGold.GregTechNightmare.ModBlocks.ThaumcraftBlocks;
import com.EvgenWarGold.GregTechNightmare.Utils.Constants;
import com.EvgenWarGold.GregTechNightmare.Utils.GTN_Utils;

import gregtech.api.enums.Textures;
import gregtech.api.interfaces.IIconContainer;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatch;
import gregtech.api.render.TextureFactory;
import mcp.mobius.waila.addons.thaumcraft.ThaumcraftModule;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.SpecialChars;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssentiaTransport;

@IMetaTileEntity.SkipGenerateDescription
public class GTN_AspectHatch extends MTEHatch implements IAspectContainer, IEssentiaTransport {

    public static final int CAPACITY = 256;
    protected AspectList mAspects = new AspectList();
    private int texturePage = 0;
    private int textureIndex = 0;

    public int facing = 2;
    public Aspect aspectFilter = null;
    private int tickCounter = 0;

    private static final String TAG_ASPECTS = "aspects";
    private static final String TAG_TEXTURE_PAGE = "texturePage";
    private static final String TAG_TEXTURE_INDEX = "textureIndex";
    private static final String TAG_FACING = "facing";
    private static final String TAG_ASPECT_FILTER = "aspectFilter";

    private static final IIconContainer textureFont = GTN_BlockIcons.ASPECT_HATCH_OVERLAY;
    private static final IIconContainer textureFont_Glow = GTN_BlockIcons.ASPECT_HATCH_OVERLAY;
    private static ITexture cachedArcaneStoneBlockTexture = null;

    public GTN_AspectHatch(int aID, String aName) {
        super(aID, aName, aName, 3, 0, "");
    }

    public GTN_AspectHatch(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
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
        data.setInteger(TAG_TEXTURE_INDEX, getTextureIndex());
        data.setInteger(TAG_TEXTURE_PAGE, getTexturePage());
        data.setByte(TAG_FACING, (byte) facing);
        if (aspectFilter != null) {
            data.setString(TAG_ASPECT_FILTER, aspectFilter.getTag());
        }

        NBTTagList nbtTagList = new NBTTagList();
        Aspect[] aspectArray = this.mAspects.getAspects();
        for (Aspect aspect : aspectArray) {
            if (aspect != null && this.mAspects.getAmount(aspect) > 0) {
                NBTTagCompound f = new NBTTagCompound();
                f.setString("key", aspect.getTag());
                f.setInteger("amount", this.mAspects.getAmount(aspect));
                nbtTagList.appendTag(f);
            }
        }
        data.setTag(TAG_ASPECTS, nbtTagList);

        return data;
    }

    @Override
    public void onDescriptionPacket(NBTTagCompound data) {
        textureIndex = data.getInteger(TAG_TEXTURE_INDEX);
        texturePage = data.getInteger(TAG_TEXTURE_PAGE);
        facing = data.getByte(TAG_FACING);
        aspectFilter = Aspect.getAspect(data.getString(TAG_ASPECT_FILTER));

        this.mAspects.aspects.clear();
        NBTTagList tlist = data.getTagList(TAG_ASPECTS, 10);
        for (int j = 0; j < tlist.tagCount(); ++j) {
            NBTTagCompound rs = tlist.getCompoundTagAt(j);
            if (rs.hasKey("key")) {
                mAspects.add(Aspect.getAspect(rs.getString("key")), rs.getInteger("amount"));
            }
        }
    }

    @Override
    public String[] getDescription() {
        return new String[] { GTN_Utils.tr("GTN.Hatch.AspectHatch.tooltip.00"),
            GTN_Utils.tr("GTN.Hatch.AspectHatch.tooltip.01"), GTN_Utils.tr("GTN.Hatch.AspectHatch.tooltip.02"),
            GTN_Utils.tr("GTN.Hatch.common.addedBy", Constants.MOD_NAME) };
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        texturePage = aNBT.getInteger(TAG_TEXTURE_PAGE);
        textureIndex = aNBT.getInteger(TAG_TEXTURE_INDEX);
        facing = aNBT.getByte(TAG_FACING);
        aspectFilter = Aspect.getAspect(aNBT.getString(TAG_ASPECT_FILTER));

        this.mAspects.aspects.clear();
        NBTTagList tlist = aNBT.getTagList(TAG_ASPECTS, 10);
        for (int j = 0; j < tlist.tagCount(); ++j) {
            NBTTagCompound rs = tlist.getCompoundTagAt(j);
            if (rs.hasKey("key")) {
                mAspects.add(Aspect.getAspect(rs.getString("key")), rs.getInteger("amount"));
            }
        }
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        aNBT.setInteger(TAG_TEXTURE_INDEX, textureIndex);
        aNBT.setInteger(TAG_TEXTURE_PAGE, texturePage);
        aNBT.setByte(TAG_FACING, (byte) facing);
        if (aspectFilter != null) {
            aNBT.setString(TAG_ASPECT_FILTER, aspectFilter.getTag());
        }

        NBTTagList nbtTagList = new NBTTagList();
        Aspect[] aspectArray = this.mAspects.getAspects();
        for (Aspect aspect : aspectArray) {
            if (aspect != null && this.mAspects.getAmount(aspect) > 0) {
                NBTTagCompound f = new NBTTagCompound();
                f.setString("key", aspect.getTag());
                f.setInteger("amount", this.mAspects.getAmount(aspect));
                nbtTagList.appendTag(f);
            }
        }
        aNBT.setTag(TAG_ASPECTS, nbtTagList);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new GTN_AspectHatch(mName, mTier, mDescriptionArray, mTextures);
    }

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        super.onPostTick(aBaseMetaTileEntity, aTick);

        if (aBaseMetaTileEntity.isServerSide()) {
            tickCounter++;

            if (tickCounter % 5 == 0 && this.mAspects.visSize() < CAPACITY) {
                fillHatch();
            }

            if (tickCounter >= 20) {
                tickCounter = 0;
            }
        }
    }

    private void fillHatch() {
        try {
            IGregTechTileEntity baseTE = this.getBaseMetaTileEntity();
            if (baseTE == null) return;

            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {

                TileEntity te = ThaumcraftApiHelper.getConnectableTile(
                    baseTE.getWorld(),
                    baseTE.getXCoord(),
                    baseTE.getYCoord(),
                    baseTE.getZCoord(),
                    dir);

                if (!(te instanceof IEssentiaTransport ic)) continue;

                ForgeDirection opposite = dir.getOpposite();

                if (!ic.canOutputTo(opposite)) continue;

                Aspect ta = null;

                if (this.aspectFilter != null) {
                    ta = this.aspectFilter;
                } else if (this.mAspects.size() > 0) {
                    ta = this.mAspects.getAspects()[0];
                } else
                    if (ic.getEssentiaAmount(opposite) > 0 && ic.getSuctionAmount(opposite) < this.getSuctionAmount(dir)
                        && this.getSuctionAmount(dir) >= ic.getMinimumSuction()) {
                            ta = ic.getEssentiaType(opposite);
                        }

                if (ta != null && ic.getSuctionAmount(opposite) < this.getSuctionAmount(dir)) {
                    int taken = ic.takeEssentia(ta, 1, opposite);
                    if (taken > 0) {
                        this.addToContainer(ta, taken);
                        return;
                    }
                }
            }
        } catch (Exception ignored) {}
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
    public AspectList getAspects() {
        AspectList al = new AspectList();
        for (Aspect aspect : this.mAspects.getAspects()) {
            if (aspect != null && this.mAspects.getAmount(aspect) > 0) {
                al.add(aspect, this.mAspects.getAmount(aspect));
            }
        }
        return al;
    }

    @Override
    public void setAspects(AspectList aspectList) {
        this.mAspects.aspects.clear();
        for (Map.Entry<Aspect, Integer> entry : aspectList.aspects.entrySet()) {
            this.mAspects.add(entry.getKey(), entry.getValue());
        }
        this.markDirty();
    }

    @Override
    public boolean doesContainerAccept(Aspect aspect) {
        if (this.aspectFilter != null) {
            return aspect == this.aspectFilter;
        }

        if (this.mAspects.size() == 0) {
            return true;
        }
        return this.mAspects.getAmount(aspect) > 0;
    }

    @Override
    public int addToContainer(Aspect aspect, int amount) {
        if (amount == 0) {
            return amount;
        }

        if (!doesContainerAccept(aspect)) {
            return amount;
        }

        int totalEssentia = this.mAspects.visSize();
        int freeSpace = CAPACITY - totalEssentia;

        if (freeSpace <= 0) {
            return amount;
        }

        int toAdd = Math.min(amount, freeSpace);
        this.mAspects.add(aspect, toAdd);
        this.markDirty();

        return amount - toAdd;
    }

    @Override
    public boolean takeFromContainer(Aspect aspect, int amount) {
        if (this.mAspects.getAmount(aspect) >= amount) {
            this.mAspects.remove(aspect, amount);
            if (this.mAspects.getAmount(aspect) <= 0) {
                this.mAspects.aspects.remove(aspect);
            }
            this.markDirty();
            return true;
        }
        return false;
    }

    @Override
    public boolean takeFromContainer(AspectList aspects) {
        for (Map.Entry<Aspect, Integer> entry : aspects.aspects.entrySet()) {
            if (this.mAspects.getAmount(entry.getKey()) < entry.getValue()) {
                return false;
            }
        }

        for (Map.Entry<Aspect, Integer> entry : aspects.aspects.entrySet()) {
            this.mAspects.remove(entry.getKey(), entry.getValue());
            if (this.mAspects.getAmount(entry.getKey()) <= 0) {
                this.mAspects.aspects.remove(entry.getKey());
            }
        }

        this.markDirty();
        return true;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect aspect, int amount) {
        return this.mAspects.getAmount(aspect) >= amount;
    }

    @Override
    public boolean doesContainerContain(AspectList aspectList) {
        for (Map.Entry<Aspect, Integer> entry : aspectList.aspects.entrySet()) {
            if (this.mAspects.getAmount(entry.getKey()) < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int containerContains(Aspect aspect) {
        return this.mAspects.getAmount(aspect);
    }

    @Override
    public boolean isConnectable(ForgeDirection face) {
        return true;
    }

    @Override
    public boolean canInputFrom(ForgeDirection face) {
        return true;
    }

    @Override
    public boolean canOutputTo(ForgeDirection face) {
        return false;
    }

    @Override
    public void setSuction(Aspect aspect, int amount) {}

    @Override
    public Aspect getSuctionType(ForgeDirection face) {
        if (this.aspectFilter != null) {
            return this.aspectFilter;
        }
        if (this.mAspects.size() > 0) {
            return this.mAspects.getAspects()[0];
        }
        return null;
    }

    @Override
    public int getSuctionAmount(ForgeDirection face) {
        if (this.mAspects.visSize() < CAPACITY) {
            return this.aspectFilter != null ? 128 : 64;
        }
        return 0;
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, ForgeDirection face) {
        return 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, ForgeDirection face) {
        if (this.canInputFrom(face)) {
            return amount - this.addToContainer(aspect, amount);
        }
        return 0;
    }

    @Override
    public Aspect getEssentiaType(ForgeDirection face) {
        if (this.mAspects.size() > 0) {
            return this.mAspects.getAspects()[0];
        }
        return null;
    }

    @Override
    public int getEssentiaAmount(ForgeDirection face) {
        return this.mAspects.visSize();
    }

    @Override
    public int getMinimumSuction() {
        return this.aspectFilter != null ? 64 : 32;
    }

    @Override
    public boolean renderExtendedTube() {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void getWailaNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, int x, int y,
        int z) {
        if (te instanceof IGregTechTileEntity gte) {
            IMetaTileEntity mte = gte.getMetaTileEntity();

            if (mte instanceof GTN_AspectHatch hatch) {
                ItemStack headSlot = player.inventory.armorInventory[3];
                if (headSlot == null) return;

                boolean hasReveal;
                try {
                    hasReveal = ThaumcraftModule.isGoggles.apply(headSlot);
                } catch (Exception e) {
                    return;
                }

                if (!hasReveal) return;

                NBTTagList aspects = new NBTTagList();

                AspectList aspectList = hatch.getAspects();

                if (aspectList != null && aspectList.size() > 0) {
                    Set<String> knownAspectTags = new HashSet<>();
                    try {
                        Map<String, ?> knownAspects = (Map<String, ?>) ThaumcraftModule.CommonProxy_getKnownAspects
                            .invoke(ThaumcraftModule.Thaumcraft_proxy.get(null));
                        Map<?, Integer> playerAspects = (Map<?, Integer>) ThaumcraftModule.AspectList_aspects
                            .get(knownAspects.get(player.getCommandSenderName()));

                        if (playerAspects != null) {
                            for (Object key : playerAspects.keySet()) {
                                String aspectTag = (String) ThaumcraftModule.Aspect_tag.get(key);
                                if (aspectTag != null) {
                                    knownAspectTags.add(aspectTag);
                                }
                            }
                        }
                    } catch (Exception ignored) {}

                    for (Aspect aspect : aspectList.getAspects()) {
                        if (aspect != null) {
                            int amount = aspectList.getAmount(aspect);
                            if (amount > 0) {
                                NBTTagCompound aspectTag = new NBTTagCompound();

                                if (!knownAspectTags.isEmpty() && !knownAspectTags.contains(aspect.getTag())) {
                                    aspectTag.setString("key", "???");
                                } else {
                                    aspectTag.setString("key", aspect.getTag());
                                }

                                aspectTag.setInteger("amount", amount);
                                aspects.appendTag(aspectTag);
                            }
                        }
                    }
                }

                tag.setTag(TAG_ASPECTS, aspects);
            }
        }
    }

    @Override
    public void getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {

        NBTTagCompound tag = accessor.getNBTData();

        if (tag.hasKey(TAG_ASPECTS)) {
            NBTTagList taglist = tag.getTagList(TAG_ASPECTS, 10);

            if (taglist.tagCount() > 0) {
                List<String> unknownAspects = new ArrayList<>();

                for (int i = 0; i < taglist.tagCount(); i++) {
                    NBTTagCompound subtag = taglist.getCompoundTagAt(i);

                    String aspectKey = subtag.getString("key");
                    int amount = subtag.getInteger("amount");

                    if (aspectKey.equals("???")) {
                        unknownAspects.add(
                            SpecialChars.getRenderString("waila.tcaspect", aspectKey) + TAB
                                + ALIGNRIGHT
                                + WHITE
                                + amount);
                    } else {
                        currenttip.add(
                            SpecialChars.getRenderString("waila.tcaspect", aspectKey) + TAB
                                + ALIGNRIGHT
                                + WHITE
                                + amount);
                    }
                }

                currenttip.addAll(unknownAspects);
            }
        }
    }

    public boolean consumeAspect(Aspect aspect, int amount, boolean simulate) {
        if (aspect == null || amount <= 0) {
            return false;
        }

        int available = this.mAspects.getAmount(aspect);

        if (available < amount) {
            return false;
        }

        if (!simulate) {
            this.mAspects.remove(aspect, amount);

            if (this.mAspects.getAmount(aspect) <= 0) {
                this.mAspects.aspects.remove(aspect);
            }

            this.markDirty();
        }

        return true;
    }
}
