package com.linearpast.sccore.capability.data.entity;

import com.linearpast.sccore.capability.data.ICapabilitySync;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

/**
 * It is recommended to manually add it during implementation: <br>
 * {@code key}  ----  As the unique identifier of capability. <br>
 * {@code getCapability(Entity entity)} ----  Simplified method for obtaining capability<br>
 * 例：
 * <pre>
 * {@code
 *     public static final ResourceLocation key =
 *          new ResourceLocation(MyMod.MODID, "sheep_data");
 *     public static Optional<SheepDataCapability> getCapability(Sheep sheep){
 *         return Optional.ofNullable(CapabilityUtils.getEntityCapability(
 *             player, SheepDataCapability.key, SheepDataCapability.class
 *         ));
 *     }
 * }
 * </pre>
 *
 */
public abstract class SimpleEntityCapabilitySync<T extends Entity> implements ICapabilitySync<T> {
    /**
     * Id
     */
    public static final String Id = "Id";

    private boolean dirty;
    private Integer id;

    @Override
    public boolean isDirty() {
        return dirty;
    }

    /**
     * You should call it to set it to true in the setter of each property to trigger automatic synchronization
     * @param dirty dirty
     */
    @Override
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    /**
     * get id
     * @return Id
     */
    public Integer getId() {
        return id;
    }

    /**
     * set id
     * @param id id
     */
    public void setId(Integer id) {
        this.id = id;
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
        SimpleEntityCapabilitySync<?> data = (SimpleEntityCapabilitySync<?>) oldData;
        this.setId(data.getId());
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
     * You shouldn't rewrite it, you should implement: {@link SimpleEntityCapabilitySync#toTag(CompoundTag)}
     * @return tag
     */
    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        if(id != null) tag.putInt(Id, id);
        tag = toTag(tag);
        return tag;
    }

    /**
     * Deserialize to instance object <br>
     * You don't need to rewrite it, you should implement: {@link SimpleEntityCapabilitySync#fromTag(CompoundTag)}
     * @param nbt nbt
     */
    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.id = null;
        if(nbt.contains(Id)) this.id = nbt.getInt(Id);
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
