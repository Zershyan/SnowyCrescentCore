package com.linearpast.sccore.capability.data.player;

import com.linearpast.sccore.capability.data.ICapabilitySync;
import com.linearpast.sccore.capability.data.entity.SimpleEntityCapabilitySync;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

/**
 * It is recommended to manually add it during implementation: <br>
 * {@code key}  ----  As the unique identifier of capability. <br>
 * {@code getCapability(Player player)} ----  Simplified method for obtaining capability<br>
 * 例：
 * <pre>
 * {@code
 *     public static final ResourceLocation key =
 *          new ResourceLocation(MyMod.MODID, "my_data");
 *     public static Optional<MyDataCapability> getCapability(Player player){
 *         return Optional.ofNullable(CapabilityUtils.getPlayerCapability(
 *             player, MyDataCapability.key, MyDataCapability.class
 *         ));
 *     }
 * }
 * </pre>
 *
 */
public abstract class SimplePlayerCapabilitySync implements ICapabilitySync<Player> {
    public static final String OwnerUUID = "OwnerUUID";

    private boolean dirty;
    private UUID ownerUUID;

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public void setOwnerUUID(UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
        setDirty(true);
    }

    /**
     * Copy data from parameter instance to current instance <br>
     *You shouldn't rewrite it, you should implement: {@link SimpleEntityCapabilitySync#copyFrom(ICapabilitySync)}
     * @param oldData old data
     * @param listenDone Whether to execute the completion method at the end: {@link ICapabilitySync#onCopyDone()}
     */
    @Override
    public void copyFrom(ICapabilitySync<?> oldData, boolean listenDone) {
        SimplePlayerCapabilitySync data = (SimplePlayerCapabilitySync) oldData;
        this.setOwnerUUID(data.getOwnerUUID());
        copyFrom(data);
        ICapabilitySync.super.copyFrom(oldData, listenDone);
    }

    /**
     * The method that will be executed when triggering data replication
     * @param oldData Copy from this data to the current instance
     */
    public abstract void copyFrom(ICapabilitySync<?> oldData);

    /**
     * Serialize to tag <br>
     * You shouldn't rewrite it, you should implement: {@link SimplePlayerCapabilitySync#toTag(CompoundTag)}
     * @return tag
     */
    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        if(ownerUUID != null) tag.putUUID(OwnerUUID, ownerUUID);
        tag = toTag(tag);
        return tag;
    }

    /**
     * Deserialize to instance object <br>
     * You don't need to rewrite it, you should implement: {@link SimplePlayerCapabilitySync#fromTag(CompoundTag)}
     * @param nbt nbt
     */
    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.ownerUUID = null;
        if(nbt.contains(OwnerUUID)) this.ownerUUID = nbt.getUUID(OwnerUUID);
        fromTag(nbt);
    }

    /**
     * In the serializeNBT method of SimpleElementCapability Sync, it will be called <br>
     * Actually equivalent to serializeNBT()
     * @param tag data tag
     * @return tag
     */
    public abstract CompoundTag toTag(CompoundTag tag);

    /**
     * In the deserializeNBT method of SimpleElementCapability Sync, it will be called <br>
     * Actually equivalent to deserializeNBT()
     * @param tag data tag
     */
    public abstract void fromTag(CompoundTag tag);
}
