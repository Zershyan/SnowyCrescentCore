package com.linearpast.sccore.capability.network;

import com.linearpast.sccore.capability.data.ICapabilitySync;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public interface ICapabilityPacket<T extends Entity> {
    /**
     * Decoding network packets
     * @param buf FriendlyByteBuf
     */
    void encode(FriendlyByteBuf buf);

    /**
     * Network packet processing events generally do not need to be rewritten, and the default behavior is sufficient for use
     * @param supplier supplier
     */
    default void handle(Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> handler(context));
    }

    /**
     * Network packet processing events should be rewritten here
     * @param context NetworkEvent.Context
     */
    void handler(NetworkEvent.Context context);

    /**
     * Retrieve the corresponding capability from the network packet, usually executed after {@link ICapabilityPacket#syncData}
     * @param entity Target
     * @return capability
     */
    @Nullable ICapabilitySync<?> getCapability(T entity);

    /**
     * Get tag
     * @return tag
     */
    CompoundTag getData();

    /**
     * Convert tags to capability data in network packets and deserialize them directly by default
     * @param dataTag tag
     * @param data The data that should be written into the data
     */
    default void syncData(CompoundTag dataTag, ICapabilitySync<?> data){
        if(data == null) return;
        data.deserializeNBT(dataTag);
    }
}
