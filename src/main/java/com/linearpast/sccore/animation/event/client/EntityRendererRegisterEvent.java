package com.linearpast.sccore.animation.event.client;

import com.linearpast.sccore.animation.entity.renderer.AnimationRideRenderer;
import com.linearpast.sccore.animation.register.AnimationEntities;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EntityRendererRegisterEvent {
    @SubscribeEvent
    public static void registerEntityRenderer(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(AnimationEntities.RIDE.get(), AnimationRideRenderer::new);
    }
}
