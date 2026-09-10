package io.zershyan.sccore.compat.animation.registry;

import com.mojang.serialization.Codec;
import io.zershyan.sccore.SCCore;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

public class AnimationEntityDataSerializers {
    public static final DeferredRegister<EntityDataSerializer<?>> REGISTRY = DeferredRegister.create(NeoForgeRegistries.ENTITY_DATA_SERIALIZERS, SCCore.MODID);

    public static final Supplier<EntityDataSerializer<UUID>> UUID = REGISTRY.register("uuid_serializer",
            () -> EntityDataSerializer.forValueType(ByteBufCodecs.fromCodec(UUIDUtil.CODEC)));
    public static final Supplier<EntityDataSerializer<ResourceLocation>> RESOURCE_LOCATION = REGISTRY.register("resource_location_serilaizer",
            () -> EntityDataSerializer.forValueType(ByteBufCodecs.fromCodec(ResourceLocation.CODEC)));
    public static final Supplier<EntityDataSerializer<LinkedHashMap<ResourceLocation, UUID>>> RL_UUID_LINKED_MAP = REGISTRY.register("rl_uuid_linked_map_serializer",
            () -> EntityDataSerializer.forValueType(ByteBufCodecs.fromCodec(
                    Codec.unboundedMap(ResourceLocation.CODEC, UUIDUtil.CODEC)
                            .xmap(LinkedHashMap::new, Function.identity())
            )));

    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}
