package com.linearpast.sccore.capability;

import com.linearpast.sccore.capability.data.ICapabilitySync;
import com.linearpast.sccore.capability.data.entity.EntityCapabilityHandler;
import com.linearpast.sccore.capability.data.entity.EntityCapabilityRegistry;
import com.linearpast.sccore.capability.data.player.PlayerCapabilityHandler;
import com.linearpast.sccore.capability.data.player.PlayerCapabilityRegistry;
import com.linearpast.sccore.capability.network.CapabilityChannel;
import com.linearpast.sccore.capability.network.ICapabilityPacket;
import com.linearpast.sccore.core.ModChannel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class CapabilityUtils {

    /**
     * Simultaneously invite player capability and corresponding network packets
     * @param key The unique name of capability
     * @param capabilityRecord Registration data for capability
     * @param channelRegister You should create an instance in advance to pass in, see: {@link CapabilityUtils#createChannel}
     * @param cid Network Channel Index
     * @param clazz Class of network packets
     * @param decoder Decoding of network packets
     * @param encoder Encoding of network packets
     * @param handler Handle of network packets
     * @param <T> extend {@code ICapabilityPacket<?>}
     */
    public static <T extends ICapabilityPacket<?>> void registerPlayerCapabilityWithNetwork(
            ResourceLocation key, PlayerCapabilityRegistry.CapabilityRecord<? extends ICapabilitySync<Player>> capabilityRecord,
            CapabilityChannel channelRegister,
            int cid,
            Class<T> clazz,
            Function<FriendlyByteBuf, T> decoder,
            BiConsumer<T, FriendlyByteBuf> encoder,
            BiConsumer<T, Supplier<NetworkEvent.Context>> handler
    ) {
        PlayerCapabilityRegistry.registerCapability(key, capabilityRecord);
        channelRegister.register(clazz, cid, decoder, encoder, handler);
    }

    /**
     * Simultaneously invite entity capability and corresponding network packets
     * @param key The unique name of capability
     * @param capabilityRecord Registration data for capability
     * @param channelRegister You should create an instance in advance to pass in, see: {@link CapabilityUtils#createChannel}
     * @param cid Network Channel Index
     * @param clazz Class of network packets
     * @param decoder Decoding of network packets
     * @param encoder Encoding of network packets
     * @param handler Handle of network packets
     * @param <T> {@code ICapabilityPacket<?>}
     */
    public static <T extends ICapabilityPacket<?>> void registerEntityCapabilityWithNetwork(
            ResourceLocation key, EntityCapabilityRegistry.CapabilityRecord<? extends ICapabilitySync<? extends Entity>> capabilityRecord,
            CapabilityChannel channelRegister,
            int cid,
            Class<T> clazz,
            Function<FriendlyByteBuf, T> decoder,
            BiConsumer<T, FriendlyByteBuf> encoder,
            BiConsumer<T, Supplier<NetworkEvent.Context>> handler
    ) {
        EntityCapabilityRegistry.registerCapability(key, capabilityRecord);
        channelRegister.register(clazz, cid, decoder, encoder, handler);
    }

    /**
     * See {@link PlayerCapabilityRegistry#registerCapability(ResourceLocation, PlayerCapabilityRegistry.CapabilityRecord)}
     * @param key The unique name of capability.
     * @param capabilityRecord Record is used to store various data of the capabilities that should be registered, refer to: {@link PlayerCapabilityRegistry.CapabilityRecord}
     * @param <T> extends {@code ICapabilitySync<Player>}
     */
    public static <T extends ICapabilitySync<Player>> void registerPlayerCapability(ResourceLocation key, PlayerCapabilityRegistry.CapabilityRecord<T> capabilityRecord){
        PlayerCapabilityRegistry.registerCapability(key, capabilityRecord);
    }

    /**
     * See {@link EntityCapabilityRegistry#registerCapability(ResourceLocation, EntityCapabilityRegistry.CapabilityRecord)}
     * @param key The unique name of capability.
     * @param capabilityRecord Record is used to store various data of the capabilities that should be registered, refer to: {@link EntityCapabilityRegistry.CapabilityRecord}
     * @param <T> extends {@code ICapabilitySync<Entity>}
     */
    public static <T extends ICapabilitySync<Entity>> void registerEntityCapability(ResourceLocation key, EntityCapabilityRegistry.CapabilityRecord<T> capabilityRecord){
        EntityCapabilityRegistry.registerCapability(key, capabilityRecord);
    }

    /**
     * Return a new PlayerCapabilityChannel instance through this method<br>
     * Generally, only network packets registered on different channels will be used
     * @param channel Your own mod channel
     * @return newInstances
     */
    public static CapabilityChannel createChannel(SimpleChannel channel) {
        return new CapabilityChannel(channel);
    }

    /**
     * Return the PlayerCapabilityChannel instance of the Channel in SCCore through this method
     * @return newInstances
     */
    public static CapabilityChannel createChannel() {
        return new CapabilityChannel(ModChannel.INSTANCE);
    }

    /**
     * By using this method to listen for capability events, all functions will be enabled<br>
     * Repeated calls will not cause anything
     * @param forgeBus Forge event bus
     */
    public static void registerHandler(IEventBus forgeBus){
        PlayerCapabilityHandler.register(forgeBus);
        EntityCapabilityHandler.register(forgeBus);
    }

    /**
     * Please obtain capability through this method
     * @param entity Target entity, type: {@code <E extends Entity>}
     * @param key The unique name of capability
     * @param clazz The capability type that should be returned. If it is null, will return: {@code ICapabilitySync<E>}
     * @param <E> extend {@code Entity}
     * @param <T> extend {@code ICapabilitySync<E>}
     * @return Return the corresponding capability
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public static <E extends Entity, T extends ICapabilitySync<E>> T getEntityCapability(E entity, ResourceLocation key, @Nullable Class<T> clazz) {
        try {
            ICapabilitySync<?> capabilitySync = entity.getCapability(
                    EntityCapabilityRegistry.getCapabilityMap().get(key).capability()
            ).resolve().orElse(null);
            if(clazz == null) return (T) capabilitySync;
            if(clazz.isInstance(capabilitySync))
                return clazz.cast(capabilitySync);
            else return null;
        }catch(Exception e){
            return null;
        }
    }

    /**
     * Please obtain capability through this method
     * @param entity Target entity, type: {@code <E extends Player>}
     * @param key The unique name of capability
     * @param clazz The capability type that should be returned. If it is null, will return: {@code ICapabilitySync<E>}
     * @param <E> extend {@code Player}
     * @param <T> extend {@code ICapabilitySync<E>}
     * @return Return the corresponding capability
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public static <E extends Player, T extends ICapabilitySync<E>> T getPlayerCapability(E entity, ResourceLocation key, @Nullable Class<T> clazz) {
        try {
            ICapabilitySync<?> capabilitySync = entity.getCapability(
                    PlayerCapabilityRegistry.getCapabilityMap().get(key).capability()
            ).resolve().orElse(null);
            if(clazz == null) return (T) capabilitySync;
            if(clazz.isInstance(capabilitySync))
                return clazz.cast(capabilitySync);
            else return null;
        }catch(Exception e){
            return null;
        }
    }

    /**
     * Get a capability of an unconverted type
     * @param entity Target
     * @param key The unique name of capability
     * @return An unconverted capability
     */
    @Nullable
    public static ICapabilitySync<?> getCapability(Entity entity, ResourceLocation key) {
        if(entity == null) return null;
        try {
            if(entity instanceof Player) {
                return entity.getCapability(
                        PlayerCapabilityRegistry.getCapabilityMap().get(key).capability()
                ).resolve().orElse(null);
            }
            return entity.getCapability(
                    EntityCapabilityRegistry.getCapabilityMap().get(key).capability()
            ).resolve().orElse(null);
        }catch(Exception e){
            return null;
        }

    }
}
