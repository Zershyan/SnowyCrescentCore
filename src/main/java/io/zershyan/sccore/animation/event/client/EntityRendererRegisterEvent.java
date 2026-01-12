package io.zershyan.sccore.animation.event.client;

import io.zershyan.sccore.animation.entity.renderer.AnimationRideRenderer;
import io.zershyan.sccore.animation.register.AnimationEntities;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EntityRendererRegisterEvent {
    @SubscribeEvent
    public static void registerEntityRenderer(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(AnimationEntities.RIDE.get(), AnimationRideRenderer::new);
    }
}
