package com.linearpast.sccore.animation.network.toserver;

import com.linearpast.sccore.animation.capability.AnimationDataCapability;
import com.linearpast.sccore.animation.capability.inter.IAnimationCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class RefreshAnimationPacket {
    private final Set<ResourceLocation> needRemoves;
    public RefreshAnimationPacket(Set<ResourceLocation> needRemoves) {
        this.needRemoves = needRemoves;
    }
    public RefreshAnimationPacket(FriendlyByteBuf buf) {
        int i = buf.readInt();
        needRemoves = new HashSet<>();
        for (int j = 0; j < i; ++j) {
            needRemoves.add(buf.readResourceLocation());
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(needRemoves.size());
        for (ResourceLocation needRemove : needRemoves) {
            buf.writeResourceLocation(needRemove);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            context.setPacketHandled(true);
            ServerPlayer sender = context.getSender();
            if(sender == null) return;
            IAnimationCapability data = AnimationDataCapability.getCapability(sender).orElse(null);
            if(data == null) return;
            needRemoves.forEach(data::removeAnimation);
        });
    }
}
