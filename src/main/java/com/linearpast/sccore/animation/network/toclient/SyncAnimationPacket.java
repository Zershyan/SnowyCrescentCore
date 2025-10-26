package com.linearpast.sccore.animation.network.toclient;

import com.linearpast.sccore.animation.AnimationPlayer;
import com.linearpast.sccore.animation.event.client.ClientPlayerTick;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.AbstractMap;
import java.util.UUID;
import java.util.function.Supplier;

public class SyncAnimationPacket {
    private final UUID playerUUID;
    private final UUID targetUUID;
    private final ResourceLocation layer;
    public SyncAnimationPacket(UUID playerUUID, UUID targetUUID, ResourceLocation layer) {
        this.playerUUID = playerUUID;
        this.targetUUID = targetUUID;
        this.layer = layer;
    }

    public SyncAnimationPacket(FriendlyByteBuf buf) {
        this.playerUUID = buf.readUUID();
        this.targetUUID = buf.readUUID();
        this.layer = buf.readResourceLocation();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(this.playerUUID);
        buf.writeUUID(this.targetUUID);
        buf.writeResourceLocation(this.layer);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            context.setPacketHandled(true);
            Minecraft instance = Minecraft.getInstance();
            ClientLevel level = instance.level;
            if(level == null) return;
            AbstractClientPlayer player = (AbstractClientPlayer) level.getPlayerByUUID(playerUUID);
            AbstractClientPlayer target = (AbstractClientPlayer) level.getPlayerByUUID(targetUUID);
            ClientPlayerTick.runs.put(
                    () -> AnimationPlayer.syncAnimation(player, target, layer),
                    new AbstractMap.SimpleEntry<>(5, 0)
            );

        });
    }
}
