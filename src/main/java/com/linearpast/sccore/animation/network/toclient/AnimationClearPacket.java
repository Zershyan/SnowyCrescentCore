package com.linearpast.sccore.animation.network.toclient;

import com.linearpast.sccore.animation.register.AnimationRegistry;
import com.linearpast.sccore.animation.utils.AnimationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record AnimationClearPacket(@Nullable ResourceLocation layer) {
    public AnimationClearPacket(FriendlyByteBuf buf) {
        this(buf.readNullable(FriendlyByteBuf::readResourceLocation));
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeNullable(layer, FriendlyByteBuf::writeResourceLocation);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            context.setPacketHandled(true);
            LocalPlayer player = Minecraft.getInstance().player;
            if(player == null) return;
            List<ResourceLocation> layers = new ArrayList<>();
            if(layer != null) layers.add(layer);
            else layers.addAll(AnimationRegistry.getLayers().keySet());
            layers.forEach(layer -> AnimationUtils.playAnimation(player, layer, null));
        });
    }
}
