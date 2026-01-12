package io.zershyan.sccore.animation.register;

import io.zershyan.sccore.SnowyCrescentCore;
import io.zershyan.sccore.animation.entity.AnimationRideEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;



public class AnimationEntities {
    public static final DeferredRegister<EntityType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, SnowyCrescentCore.MODID);

    public static final RegistryObject<EntityType<AnimationRideEntity>> RIDE = register(
            "animation_ride_entity", EntityType.Builder.<AnimationRideEntity>of((type, world) -> new AnimationRideEntity(world), MobCategory.MISC)
                    .sized(0.0F, 0.0F)
                    .setCustomClientFactory((spawnEntity, world) ->
                            new AnimationRideEntity(world)
                    )
    );

    private static <T extends Entity> RegistryObject<EntityType<T>> register(String name, EntityType.Builder<T> builder) {
        return REGISTER.register(name, () -> builder.build(name));
    }

    public static void register(IEventBus modBus){
        REGISTER.register(modBus);
    }
}
