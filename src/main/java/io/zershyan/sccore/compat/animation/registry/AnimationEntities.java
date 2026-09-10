package io.zershyan.sccore.compat.animation.registry;

import io.zershyan.sccore.SCCore;
import io.zershyan.sccore.compat.animation.registry.entity.AnimationRideEntity;
import io.zershyan.sccore.compat.animation.registry.entity.renderer.AnimationRideEntityRenderer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class AnimationEntities {
    public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(Registries.ENTITY_TYPE, SCCore.MODID);

    public static final Supplier<EntityType<AnimationRideEntity>> RIDE = register(
            "animation_ride_entity", EntityType.Builder.<AnimationRideEntity>of(AnimationRideEntity::new, MobCategory.MISC)
                    .sized(0.0F, 0.0F)
    );

    private static <T extends Entity> Supplier<EntityType<T>> register(String name, EntityType.Builder<T> builder) {
        return REGISTRY.register(name, () -> builder.build(name));
    }

    public static void register(IEventBus modBus){
        REGISTRY.register(modBus);
        modBus.addListener(AnimationEntities::registerRenderer);
    }

    public static void registerRenderer(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(AnimationEntities.RIDE.get(), AnimationRideEntityRenderer::new);
    }
}
