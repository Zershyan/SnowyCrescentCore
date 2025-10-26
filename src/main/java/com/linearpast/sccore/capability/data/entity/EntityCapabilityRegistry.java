package com.linearpast.sccore.capability.data.entity;

import com.linearpast.sccore.capability.data.ICapabilitySync;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.Capability;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EntityCapabilityRegistry {
    public static final EntityCapabilityRegistry CAPABILITIES = new EntityCapabilityRegistry();
    private final Map<ResourceLocation, CapabilityRecord<?>> capabilityRecordMap = new HashMap<>();

    /**
     * Registering entity capabilities through this method only applies to {@link net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent}<br>
     * @param key The unique name of capability.
     * @param capabilityRecord Record is used to store various data of the capabilities that should be registered, refer to: {@link EntityCapabilityRegistry.CapabilityRecord}
     */
    public static void registerCapability(ResourceLocation key, CapabilityRecord<?> capabilityRecord) {
        CAPABILITIES.capabilityRecordMap.put(key, capabilityRecord);
    }

    /**
     * Obtain corresponding capability data through this method
     * @param key Obtain based on key
     * @return capability
     */
    public static CapabilityRecord<?> getCapabilityRecord(ResourceLocation key){
        return CAPABILITIES.capabilityRecordMap.get(key);
    }

    public static Map<ResourceLocation, CapabilityRecord<?>> getCapabilityMap(){
        return CAPABILITIES.capabilityRecordMap;
    }

    /**
     * Record the registration data of capability
     * @param aClass The instance that will ultimately be attached to the entity. Should be an instance of ICapabilitySync
     * @param capability In general, it is not necessary to initialize it, default: <span>{@code CapabilityManager.get(new CapabilityToken<>(){})}</span>
     * @param interfaceClass The interface class corresponding to the instance, such as: ICapabilitySync.class.
     * @param targets Targets types attached to capability
     */
    public record CapabilityRecord<T extends ICapabilitySync<? extends Entity>>(
            Class<?> aClass,
            Capability<T> capability,
            Class<T> interfaceClass,
            Set<Class<? extends Entity>> targets
    ) {

    }
}
