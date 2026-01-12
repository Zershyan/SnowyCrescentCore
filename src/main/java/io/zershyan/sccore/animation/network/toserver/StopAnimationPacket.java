package io.zershyan.sccore.animation.network.toserver;

import io.zershyan.sccore.animation.AnimationApi;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record StopAnimationPacket(ResourceLocation layer) {
    public StopAnimationPacket(FriendlyByteBuf buf) {
        this(buf.readResourceLocation());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(layer);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            context.setPacketHandled(true);
            ServerPlayer sender = context.getSender();
            if(sender == null) return;
            AnimationApi.getHelper(sender).removeAnimation(layer);
        });
    }

}
