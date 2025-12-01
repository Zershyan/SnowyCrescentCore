package com.linearpast.sccore.animation.event;

import com.linearpast.sccore.animation.capability.AnimationDataCapability;
import com.linearpast.sccore.animation.entity.AnimationRideEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PlayerTickEvent {
    @SubscribeEvent
    public static void onPlayerTickEvent(TickEvent.PlayerTickEvent event) {
        if (event.side.isServer() && event.phase == TickEvent.Phase.END) {
            if(event.player.tickCount % 20 == 0) {
                Player player = event.player;
                if(!(player.getVehicle() instanceof AnimationRideEntity)){
                    AnimationDataCapability.getCapability(player).ifPresent(capability -> {
                        if(capability.getRiderAnimLayer() != null) {
                            capability.removeRiderAnimation();
                        }
                    });
                }
            }
        }
    }
}
