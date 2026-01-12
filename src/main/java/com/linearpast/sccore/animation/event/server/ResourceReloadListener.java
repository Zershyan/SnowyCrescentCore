package com.linearpast.sccore.animation.event.server;

import com.linearpast.sccore.SnowyCrescentCore;
import com.linearpast.sccore.animation.register.AnimationRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SnowyCrescentCore.MODID, value = Dist.DEDICATED_SERVER, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ResourceReloadListener {
    @SubscribeEvent
    public static void init (AddReloadListenerEvent event) {
        event.addListener(AnimationRegistry.AnimationDataManager.INSTANCE);
        event.addListener(AnimationRegistry.LayerDataManager.INSTANCE);
    }
}
