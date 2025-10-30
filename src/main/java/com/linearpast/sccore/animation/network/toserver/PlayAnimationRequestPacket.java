package com.linearpast.sccore.animation.network.toserver;

import com.linearpast.sccore.animation.AnimationPlayer;
import com.linearpast.sccore.animation.register.AnimationRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Supplier;

public class PlayAnimationRequestPacket {
    private final ResourceLocation layer;
    private @Nullable ResourceLocation animation;
    private @Nullable UUID uuid;
    public PlayAnimationRequestPacket(@Nullable UUID uuid, ResourceLocation layer, @Nullable ResourceLocation animation) {
        this.layer = layer;
        this.animation = animation;
        this.uuid = uuid;
    }
    public PlayAnimationRequestPacket(FriendlyByteBuf buf){
        this.layer = buf.readResourceLocation();
        try {
            this.animation = buf.readResourceLocation();
            this.uuid = buf.readUUID();
        } catch (Exception e) {
            this.animation = null;
            this.uuid = null;
        }
    }
    public void encode(FriendlyByteBuf buf){
        buf.writeResourceLocation(layer);
        if(animation != null) buf.writeResourceLocation(animation);
        if(uuid != null) buf.writeUUID(uuid);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            context.setPacketHandled(true);
            if (AnimationRegistry.getLayers().containsKey(layer)) {
                ServerPlayer target;
                ServerPlayer sender = context.getSender();
                if(sender == null || sender.getServer() == null) return;
                if(this.uuid == null) target = sender;
                else target = sender.getServer().getPlayerList().getPlayer(this.uuid);
                AnimationPlayer.serverPlayAnimation(target, layer, animation);
            }
        });
    }
}
