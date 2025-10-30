package com.linearpast.sccore.animation.network.toclient;

import com.linearpast.sccore.animation.register.AnimationRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public record AnimationClientStatusPacket(int status) {
    public enum Status {
        ANIM_CACHE_CLEAR(0),
        LAYER_CACHE_CLEAR(1),
        ANIM_REGISTER(2),
        LAYER_REGISTER(3),;
        private final int value;
        Status(final int value) {
            this.value = value;
        }
        @Nullable
        public static Status getStatus(final int value) {
            for (Status status : values()) {
                if (status.value == value) {
                    return status;
                }
            }
            return null;
        }
    }
    public AnimationClientStatusPacket(Status status) {
        this(status.value);
    }
    public AnimationClientStatusPacket(FriendlyByteBuf buf) {
        this(buf.readInt());
    }
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(status);
    }
    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Status state = Status.getStatus(status);
            if(state == null) return;
            AnimationRegistry.ClientCache.animationStatusUpdate(state);
        });
    }
}
