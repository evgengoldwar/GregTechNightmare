package com.EvgenWarGold.GregTechNightmare.GregTech.Api;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import com.gtnewhorizon.structurelib.util.Vec3Impl;

public class NBTDataPacket extends DataPacket<NBTTagList> {

    public NBTDataPacket(NBTTagList content) {
        super(content);
    }

    public NBTDataPacket(NBTTagCompound compound) {
        super(compound);
    }

    @Override
    protected NBTTagList contentFromNBT(NBTTagCompound nbt) {
        return nbt.hasKey("data", 9) ? nbt.getTagList("data", 10) : new NBTTagList();
    }

    @Override
    protected NBTTagCompound contentToNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("data", content != null ? content.copy() : new NBTTagList());
        return tag;
    }

    @Override
    public boolean extraCheck() {
        return content != null && content.tagCount() > 0;
    }

    @Override
    protected NBTTagList unifyContentWith(NBTTagList other) {
        NBTTagList combined = new NBTTagList();
        if (content != null) {
            for (int i = 0; i < content.tagCount(); i++) {
                combined.appendTag(
                    content.getCompoundTagAt(i)
                        .copy());
            }
        }
        if (other != null) {
            for (int i = 0; i < other.tagCount(); i++) {
                combined.appendTag(
                    other.getCompoundTagAt(i)
                        .copy());
            }
        }
        return combined;
    }

    public NBTDataPacket unifyTraceWith(Vec3Impl... positions) {
        return (NBTDataPacket) super.unifyTrace(positions);
    }

    public NBTDataPacket unifyTraceWith(NBTDataPacket p) {
        return (NBTDataPacket) super.unifyTrace(p);
    }

    public NBTDataPacket unifyPacketWith(NBTDataPacket p) {
        return (NBTDataPacket) super.unifyWith(p);
    }
}
