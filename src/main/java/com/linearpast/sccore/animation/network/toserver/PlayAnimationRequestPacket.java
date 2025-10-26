package com.linearpast.sccore.animation.network.toserver;

import com.linearpast.sccore.animation.AnimationPlayer;
import com.linearpast.sccore.animation.event.AnimationLayerRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class PlayAnimationRequestPacket {
    private final ResourceLocation layer;
    private @Nullable ResourceLocation animation;
    public PlayAnimationRequestPacket(ResourceLocation layer, @Nullable ResourceLocation animation) {
        this.layer = layer;
        this.animation = animation;
    }
    public PlayAnimationRequestPacket(FriendlyByteBuf buf){
        this.layer = buf.readResourceLocation();
        try {
            this.animation = buf.readResourceLocation();
        } catch (Exception e) {
            this.animation = null;
        }
    }
    public void encode(FriendlyByteBuf buf){
        buf.writeResourceLocation(layer);
        if(animation != null){
            buf.writeResourceLocation(animation);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            context.setPacketHandled(true);
            if (AnimationLayerRegistry.getAnimLayers().containsKey(layer)) {
                ServerPlayer sender = context.getSender();
                if(sender == null) return;
                AnimationPlayer.serverPlayAnimation(sender, layer, animation);
            }
        });
    }
}
