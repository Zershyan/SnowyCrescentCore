package io.zershyan.sccore.capability.data.entity;

import io.zershyan.sccore.capability.data.ICapabilitySync;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The final serialization, deserialization, and retrieval methods of capability
 * @param <C> extends {@link ICapabilitySync}
 */
@AutoRegisterCapability
public class EntityCapabilityProvider<C extends ICapabilitySync<? extends Entity>> implements ICapabilitySerializable<CompoundTag> {
    private final C instance;
    private final ResourceLocation resourceLocation;

    /**
     * Constructor
     * @param resourceLocation key
     * @param instance instance
     */
    public EntityCapabilityProvider(ResourceLocation resourceLocation, C instance) {
        this.resourceLocation = resourceLocation;
        this.instance = instance;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull <R> LazyOptional<R> getCapability(@NotNull Capability<R> cap, @Nullable Direction side) {
        Capability<C> iCapabilitySyncCapability = (Capability<C>) EntityCapabilityRegistry.getCapabilityRecord(resourceLocation).capability();
        return iCapabilitySyncCapability.orEmpty(cap, LazyOptional.of(() -> instance));
    }

    @Override
    public CompoundTag serializeNBT() {
        return instance.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        instance.deserializeNBT(nbt);
    }
}
