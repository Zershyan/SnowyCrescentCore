package com.linearpast.sccore.animation.network.toserver;

import com.linearpast.sccore.animation.data.AnimationData;
import com.linearpast.sccore.animation.network.ServiceGetterPacket;
import com.linearpast.sccore.animation.service.IAnimationService;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Supplier;

public class PlayAnimationRidePacket extends ServiceGetterPacket {
    private final @Nullable CompoundTag animationTag;
    private final ResourceLocation layer;
    private final ResourceLocation animation;
    private final @Nullable UUID uuid;
    private final boolean force;

    public PlayAnimationRidePacket(@Nullable AnimationData data, ResourceLocation layer, ResourceLocation animation, @Nullable UUID uuid, boolean force) {
        this.animationTag = data != null ? data.serializeNBT() : null;
        this.layer = layer;
        this.animation = animation;
        this.uuid = uuid;
        this.force = force;
    }

    public PlayAnimationRidePacket(FriendlyByteBuf buf) {
        this.animationTag = buf.readNullable(FriendlyByteBuf::readAnySizeNbt);
        this.layer = buf.readResourceLocation();
        this.animation = buf.readResourceLocation();
        this.uuid = buf.readNullable(FriendlyByteBuf::readUUID);
        this.force = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeNullable(animationTag, FriendlyByteBuf::writeNbt);
        buf.writeResourceLocation(layer);
        buf.writeResourceLocation(animation);
        buf.writeNullable(uuid, FriendlyByteBuf::writeUUID);
        buf.writeBoolean(force);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            context.setPacketHandled(true);
            ServerPlayer target;
            ServerPlayer sender = context.getSender();
            if(sender == null || sender.getServer() == null) return;
            if(this.uuid == null) target = sender;
            else target = sender.getServer().getPlayerList().getPlayer(this.uuid);
            if(target == null) return;
            IAnimationService<?, ?> service = getService();
            if(service == null) return;
            AnimationData data = service.getAnimation(animationTag);
            if(data == null) return;
            if(target == sender) service.playAnimationWithRide(target, layer, data, force);
            else service.request(sender, target, layer, data, true);
        });
    }

    @Override
    public @Nullable CompoundTag getAnimationTag() {
        return animationTag;
    }

    @Override
    public @Nullable ResourceLocation getAnimation() {
        return animation;
    }
}
