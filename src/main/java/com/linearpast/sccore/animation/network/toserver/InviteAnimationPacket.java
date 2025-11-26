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

import java.util.Collection;
import java.util.UUID;
import java.util.function.Supplier;

public class InviteAnimationPacket extends HelperGetterPacket {
    private final @Nullable CompoundTag animationTag;
    private final ResourceLocation layer;
    private final ResourceLocation animation;
    private final Collection<UUID> targets;

    public InviteAnimationPacket(AnimationData data, ResourceLocation layer, Collection<UUID> targets) {
        this.animationTag = data.serializeNBT();
        this.animation = data.getKey();
        this.layer = layer;
        this.targets = targets;
    }

    public InviteAnimationPacket(FriendlyByteBuf buf) {
        this.animationTag = buf.readNullable(FriendlyByteBuf::readAnySizeNbt);
        this.layer = buf.readResourceLocation();
        this.animation = buf.readResourceLocation();
        this.targets = buf.readList(FriendlyByteBuf::readUUID);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeNullable(animationTag, FriendlyByteBuf::writeNbt);
        buf.writeResourceLocation(layer);
        buf.writeResourceLocation(animation);
        buf.writeCollection(targets, FriendlyByteBuf::writeUUID);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            context.setPacketHandled(true);
            ServerPlayer sender = context.getSender();
            if(sender == null) return;
            IAnimationHelper<?, ?> helper = getHelper();
            if(helper == null) return;
            AnimationData data = helper.getAnimation(animationTag);
            if(data == null) return;
            if(!targets.isEmpty()) helper.invite(sender, layer, data, targets);
            else helper.playAnimationWithRide(sender, layer, data, false);
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
