package com.linearpast.sccore.animation.event.client;

import com.linearpast.sccore.animation.entity.renderer.AnimationRideRenderer;
import com.linearpast.sccore.animation.register.AnimationEntities;
import net.minecraftforge.client.event.EntityRenderersEvent;

public class EntityRendererRegisterEvent {
    public static void registerEntityRenderer(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(AnimationEntities.RIDE.get(), AnimationRideRenderer::new);
    }
}
