package com.linearpast.sccore.animation.network.toserver;

import com.linearpast.sccore.animation.data.AnimationData;
import com.linearpast.sccore.animation.helper.IAnimationHelper;
import com.linearpast.sccore.animation.network.HelperGetterPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Supplier;

public class PlayAnimationPacket extends HelperGetterPacket {
    private final @Nullable CompoundTag animationTag;
    private final ResourceLocation layer;
    private final ResourceLocation animation;
    private final @Nullable UUID uuid;

    public PlayAnimationPacket(AnimationData data, ResourceLocation layer, ResourceLocation animation, @Nullable UUID uuid) {
        this.animationTag = data.serializeNBT();
        this.layer = layer;
        this.animation = animation;
        this.uuid = uuid;
    }

    public PlayAnimationPacket(FriendlyByteBuf buf){
        this.animationTag = buf.readNullable(FriendlyByteBuf::readAnySizeNbt);
        this.layer = buf.readResourceLocation();
        this.animation = buf.readResourceLocation();
        this.uuid = buf.readNullable(FriendlyByteBuf::readUUID);
    }

    public void encode(FriendlyByteBuf buf){
        buf.writeNullable(animationTag, FriendlyByteBuf::writeNbt);
        buf.writeResourceLocation(layer);
        buf.writeResourceLocation(animation);
        buf.writeNullable(uuid, FriendlyByteBuf::writeUUID);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            context.setPacketHandled(true);
            ServerPlayer target;
            ServerPlayer sender = context.getSender();
            if(sender == null || sender.getServer() == null) return;
            if(this.uuid == null) target = sender;
            else target = sender.getServer().getPlayerList().getPlayer(this.uuid);
            if(target == null) return;
            IAnimationHelper<?, ?> helper = getHelper();
            if(helper == null) return;
            AnimationData data = helper.getAnimation(animationTag);
            if(data == null) return;
            if(target == sender) helper.playAnimation(target, layer, data);
            else helper.request(sender, target, layer, data, false);
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
