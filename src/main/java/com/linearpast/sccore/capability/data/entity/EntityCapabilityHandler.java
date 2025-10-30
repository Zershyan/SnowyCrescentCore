package com.linearpast.sccore.capability.data.entity;

import com.linearpast.sccore.SnowyCrescentCore;
import com.linearpast.sccore.capability.data.ICapabilitySync;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod.EventBusSubscriber(modid = SnowyCrescentCore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EntityCapabilityHandler {
    private static final Logger log = LoggerFactory.getLogger(EntityCapabilityHandler.class);
    private static boolean isRegistered = false;

    /**
     * It should be called in the Forge mainline to listen to the capability registration <br>
     * Suggest calling it in the Mod constructor method <br>
     * Normally SCCore will call it, so you should not call it
     * @param forgeBus forge event bus
     */
    public static void register(IEventBus forgeBus) {
        if (isRegistered) return;
        //remainder
        forgeBus.addListener(EventPriority.HIGHEST, EntityCapabilityRemainder::capabilitySync);
        forgeBus.addListener(EventPriority.HIGHEST, EntityCapabilityRemainder::onEntityBeTracked);
        forgeBus.addListener(EventPriority.HIGHEST, EntityCapabilityRemainder::onEntityJoin);
        isRegistered = true;
    }

    /**
     * Register capability
     * @param event event
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void registerCapability(RegisterCapabilitiesEvent event) {
        EntityCapabilityRegistry.getCapabilityMap().values().forEach(record ->
                event.register(record.interfaceClass())
        );
    }

    /**
     * Attach capability to entity
     * @param event event
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void attachCapability(AttachCapabilitiesEvent<?> event) {
        if(event.getObject() instanceof Entity entity) {
            EntityCapabilityRegistry.getCapabilityMap().forEach((key, record) ->
                    record.targets().forEach(target -> {
                        if(target.isInstance(entity)) {
                            try {
                                ICapabilitySync<?> capabilitySync = (ICapabilitySync<?>) record.aClass().getDeclaredConstructor().newInstance();
                                event.addCapability(key, new EntityCapabilityProvider<>(key, capabilitySync));
                            } catch (Exception e) {
                                log.error("Failed to instantiate capability sync class {}. Your capability invite is wrong.", record.aClass(), e);
                            }
                        }
                    }));
        }
    }
}
