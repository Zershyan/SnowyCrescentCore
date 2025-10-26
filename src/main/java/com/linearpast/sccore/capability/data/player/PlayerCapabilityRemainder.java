package com.linearpast.sccore.capability.data.player;

import com.linearpast.sccore.capability.CapabilityUtils;
import com.linearpast.sccore.capability.data.ICapabilitySync;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class PlayerCapabilityRemainder {
    /**
     * Players should transfer data to a new body when crossing dimensions or dying
     * @param event event
     */
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player entity = event.getEntity();
        if(entity instanceof ServerPlayer newPlayer) {
            Player original = event.getOriginal();
            original.reviveCaps();
            PlayerCapabilityRegistry.getCapabilityMap().forEach((key, value) -> {
                ICapabilitySync<?> originData = CapabilityUtils.getCapability(original, key);
                ICapabilitySync<?> newData = CapabilityUtils.getCapability(newPlayer, key);
                if(originData != null && newData != null) {
                    newData.copyFrom(originData, true);
                    newData.sendToClient();
                }
            });
            original.invalidateCaps();
        }
    }

    /**
     * Players should update their capabilities when they are reborn
     * @param event event
     */
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if(event.getEntity() instanceof ServerPlayer newPlayer){
            PlayerCapabilityRegistry.getCapabilityMap().forEach((key, value) -> {
                ICapabilitySync<?> data = CapabilityUtils.getCapability(newPlayer, key);
                if(data == null) return;
                data.sendToClient(newPlayer);
            });
        }
    }

    /**
     * Player start tracking an player event<br>
     * When other entities are loaded, the client requires the capabilities of the other party, and this event can be actively sent<br>
     * Will call{@link ICapabilitySync#sendToClient(ServerPlayer)}
     * @param event event
     */
    public static void onEntityBeTracked(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof Player target && event.getEntity() instanceof ServerPlayer attacker) {
            PlayerCapabilityRegistry.getCapabilityMap().forEach((key, value) -> {
                ICapabilitySync<?> data = CapabilityUtils.getCapability(target, key);
                if(data == null) return;
                data.sendToClient(attacker);
            });
        }
    }

    /**
     * Player Tick Event<br>
     * If the capability is dirty, it will call {@link ICapabilitySync#sendToClient()} <br>
     * @param event event
     */
    public static void capabilitySync(TickEvent.PlayerTickEvent event) {
        if(!event.player.level().isClientSide){
            PlayerCapabilityRegistry.getCapabilityMap().forEach((key, value) -> {
                ICapabilitySync<?> data = CapabilityUtils.getCapability(event.player, key);
                if(data == null) return;
                if(data.isDirty()) {
                    data.setDirty(false);
                    data.sendToClient();
                }
            });
        }
    }

    /**
     * Player login event <br>
     * Reinitialize the login player's capability <br>
     * @param event event
     */
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if(!(player instanceof ServerPlayer serverPlayer)) return;
        PlayerCapabilityRegistry.getCapabilityMap().forEach((key, value) -> {
            ICapabilitySync<Player> data = CapabilityUtils.getPlayerCapability(player, key, null);
            if(data == null) return;
            if(data instanceof SimplePlayerCapabilitySync capabilitySync) {
                capabilitySync.setOwnerUUID(serverPlayer.getUUID());
            }
            data.attachInit(serverPlayer);
            data.setDirty(false);
            data.sendToClient();
        });
    }
}
