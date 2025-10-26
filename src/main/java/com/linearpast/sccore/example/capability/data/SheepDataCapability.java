package com.linearpast.sccore.example.capability.data;

import com.linearpast.sccore.SnowyCrescentCore;
import com.linearpast.sccore.capability.CapabilityUtils;
import com.linearpast.sccore.capability.data.ICapabilitySync;
import com.linearpast.sccore.capability.data.entity.SimpleEntityCapabilitySync;
import com.linearpast.sccore.capability.network.SimpleCapabilityPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Sheep;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * The entity class of cap <br>
 * Inheriting SimpleElementCapability Sync means automatically hosting synchronization of an ID <br>
 * The IsheepData implemented only contains the property 'value' as a getter and setter <br>
 * @see SimpleEntityCapabilitySync
 */
public class SheepDataCapability extends SimpleEntityCapabilitySync<Sheep> implements ISheepData {
    public static final ResourceLocation key = new ResourceLocation(SnowyCrescentCore.MODID, "sheep_data");

    public static final String Value = "Value";

    private Integer value;

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public void setValue(Integer value) {
        this.value = value;
        setDirty(true);
    }

    /**
     * @param tag data tag
     * @return tag
     * @see SimpleEntityCapabilitySync#toTag(CompoundTag)
     */
    @Override
    public CompoundTag toTag(CompoundTag tag) {
        if(value != null) tag.putInt(Value, value);
        return tag;
    }

    /**
     * @see SimpleEntityCapabilitySync#fromTag(CompoundTag)
     * @param tag data tag
     */
    @Override
    public void fromTag(CompoundTag tag) {
        this.value = null;
        if(tag.contains(Value)) this.value = tag.getInt(Value);
    }

    /**
     * @see SimpleEntityCapabilitySync#copyFrom(ICapabilitySync)
     * @param oldData Copy from this data to the current instance
     */
    @Override
    public void copyFrom(ICapabilitySync<?> oldData) {
        SheepDataCapability data = (SheepDataCapability) oldData;
        this.value = data.getValue();
    }

    /**
     * Network packet, you can rewrite any method inside. For the function of methods, please refer to<br>
     * {@link com.linearpast.sccore.capability.network.ICapabilityPacket} <br>
     * It is not necessary to include it in the internal class. I feel that the content is too limited and writing it inside makes it more compact and beautiful
     * @see SimpleCapabilityPacket
     */
    public static class SheepCapabilityPacket extends SimpleCapabilityPacket<Sheep> {
        public SheepCapabilityPacket(CompoundTag data) {
            super(data);
        }

        public SheepCapabilityPacket(FriendlyByteBuf buf) {
            super(buf);
        }

        @Override
        public @Nullable SheepDataCapability getCapability(Sheep entity) {
            return SheepDataCapability.getCapability(entity).orElse(null);
        }
    }

    /**
     * Get the default network packet, which will be called when sendToClient sends it
     * @return network packet
     * @see ICapabilitySync#getDefaultPacket()
     */
    @Override
    public SimpleCapabilityPacket<Sheep> getDefaultPacket() {
        return new SheepCapabilityPacket(serializeNBT());
    }

    /**
     * This method will be called during cap initialization, such as player login <br>
     * In this example, when the sheep joins the level, this method will be called to initialize the capability
     * @param entity Target
     * @see ICapabilitySync#attachInit(Entity)
     */
    @Override
    public void attachInit(Sheep entity) {

    }

    /**
     * It is not necessary. <br>
     * Call this when capability is needed in other places <br>
     * The purpose is to simplify the method of capability get
     * @param sheep Target
     * @return Optional capability
     */
    public static Optional<SheepDataCapability> getCapability(Sheep sheep){
        return Optional.ofNullable(CapabilityUtils.getEntityCapability(
                sheep, SheepDataCapability.key, SheepDataCapability.class
        ));
    }
}
