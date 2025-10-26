package com.linearpast.sccore.capability.data.entity;

import com.linearpast.sccore.capability.CapabilityUtils;
import com.linearpast.sccore.capability.data.ICapabilitySync;
import com.linearpast.sccore.capability.data.player.PlayerCapabilityRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class EntityCapabilityRemainder {
    /**
     * Player start tracking an entity event<br>
     * When other entities are loaded, the client requires the capabilities of the other party, and this event can be actively sent<br>
     * Will call{@link ICapabilitySync#sendToClient(ServerPlayer)}
     * @param event event
     */
    public static void onEntityBeTracked(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer attacker) {
            PlayerCapabilityRegistry.getCapabilityMap().forEach((key, value) -> {
                ICapabilitySync<?> data = CapabilityUtils.getCapability(event.getTarget(), key);
                if(data == null) return;
                data.sendToClient(attacker);
            });
        }
    }

    /**
     * Entity Tick Event<br>
     * If the capability is dirty, it will call {@link ICapabilitySync#sendToClient()} <br>
     * For performance reasons, synchronization is only triggered once per second
     * @param event event
     */
    public static void capabilitySync(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if(!entity.level().isClientSide){
            if (entity.tickCount % 20 == 0) {
                PlayerCapabilityRegistry.getCapabilityMap().forEach((key, value) -> {
                    ICapabilitySync<?> data = CapabilityUtils.getCapability(entity, key);
                    if(data == null) return;
                    if(data.isDirty()) {
                        data.setDirty(false);
                        data.sendToClient();
                    }
                });
            }
        }
    }

    /**
     * Event of entity joining level, initialization
     * @param event event
     */
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if(entity.level().isClientSide) return;
        EntityCapabilityRegistry.getCapabilityMap().forEach((key, value) -> {
            ICapabilitySync<Entity> data = CapabilityUtils.getEntityCapability(entity, key, null);
            if(data == null) return;
            if(data instanceof SimpleEntityCapabilitySync<?> capabilitySync){
                capabilitySync.setId(entity.getId());
            }
            data.attachInit(entity);
            data.setDirty(false);
            data.sendToClient();
        });
    }
}
