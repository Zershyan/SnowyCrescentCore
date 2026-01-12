package io.zershyan.sccore.animation.event.client;

import io.zershyan.sccore.animation.AnimationApi;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class ClientPlayerEvent {
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side.isClient() && event.phase == TickEvent.Phase.START) {
            Player player = event.player;
            if(player.tickCount % 10 != 0) return;
            if (!(player instanceof AbstractClientPlayer clientPlayer)) return;
            AnimationApi.getHelper(clientPlayer).refreshAnimation();
        }
    }

    public static Map<Runnable, Map.Entry<Integer, Integer>> runs = new HashMap<>();
    @SubscribeEvent
    public static void delayRuns(TickEvent.PlayerTickEvent event) {
        if (event.side.isClient() && event.phase == TickEvent.Phase.END) {
            Map.copyOf(runs).forEach((runnable, countMap) -> {
                if(countMap.getValue() >= countMap.getKey()) {
                    runnable.run();
                    runs.remove(runnable);
                } else {
                    countMap.setValue(countMap.getValue() + 1);
                }
            });
        }
    }
}
