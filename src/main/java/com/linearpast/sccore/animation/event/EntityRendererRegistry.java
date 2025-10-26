package com.linearpast.sccore.animation.event;

import com.linearpast.sccore.animation.entity.renderer.AnimationRideRenderer;
import com.linearpast.sccore.animation.registry.AnimationEntities;
import net.minecraftforge.client.event.EntityRenderersEvent;

public class EntityRendererRegistry {
    public static void registerEntityRenderer(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(AnimationEntities.RIDE.get(), AnimationRideRenderer::new);
    }
}
