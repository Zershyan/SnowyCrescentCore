package io.zershyan.sccore.capability.network;

import io.zershyan.sccore.capability.CapabilityUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Call createChannel in the Mod main class construction method logic. There are two ways:<br>
 * <pre>
 * 1. {@link CapabilityUtils#createChannel(SimpleChannel)}
 * If you do this, you must override all the sendToPlayer methods in the Capability class, and call your Channel in the override
 * </pre>
 * <pre>
 * 2. {@link CapabilityUtils#createChannel()}
 * If this is done, the network package will be registered with SCCore's Channel
 * </pre>
 * The added network packet must implement the ICapabilityPacket interface
 */
public class CapabilityChannel {
    private final SimpleChannel channel;

    public CapabilityChannel(SimpleChannel channel) {
        this.channel = channel;
    }

    /**
     * Add a network packet through this method and invite
     * @param clazz Network packet class
     * @param cid index
     * @param decoder decoder
     * @param encoder encoder
     * @param handler handler
     * @param <T> extend {@code ICapabilityPacket<?>}
     */
    public <T extends ICapabilityPacket<?>> void register(
            Class<T> clazz,
            int cid,
            Function<FriendlyByteBuf, T> decoder,
            BiConsumer<T, FriendlyByteBuf> encoder,
            BiConsumer<T, Supplier<NetworkEvent.Context>> handler
    ) {
        channel.messageBuilder(clazz, cid, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(decoder)
                .encoder(encoder)
                .consumerMainThread(handler)
                .add();
    }
}
