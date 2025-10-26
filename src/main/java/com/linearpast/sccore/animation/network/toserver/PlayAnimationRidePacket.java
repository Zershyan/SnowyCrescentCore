package com.linearpast.sccore.animation.network.toserver;

import com.linearpast.sccore.animation.AnimationPlayer;
import com.linearpast.sccore.animation.event.AnimationLayerRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class PlayAnimationRidePacket {
    private final ResourceLocation layer;
    private @Nullable ResourceLocation animation;
    private final boolean force;
    public PlayAnimationRidePacket(ResourceLocation layer, @Nullable ResourceLocation animation, boolean force) {
        this.layer = layer;
        this.animation = animation;
        this.force = force;
    }

    public PlayAnimationRidePacket(FriendlyByteBuf buf) {
        this.layer = buf.readResourceLocation();
        this.force = buf.readBoolean();
        try {
            this.animation = buf.readResourceLocation();
        } catch (Exception e) {
            this.animation = null;
        }

    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(layer);
        buf.writeBoolean(force);
        if(animation != null) {
            buf.writeResourceLocation(animation);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            context.setPacketHandled(true);
            if (AnimationLayerRegistry.getAnimLayers().containsKey(layer)) {
                ServerPlayer sender = context.getSender();
                if(sender == null) return;
                AnimationPlayer.playAnimationWithRide(sender, layer, animation, force);
            }
        });
    }

}
