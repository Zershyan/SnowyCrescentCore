package com.linearpast.sccore.animation.network.toserver;

import com.linearpast.sccore.animation.network.ServiceGetterPacket;
import com.linearpast.sccore.animation.service.IAnimationService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class ApplyAnimationPacket extends ServiceGetterPacket {
    private final UUID targetUUID;

    public ApplyAnimationPacket(UUID targetUUID) {
        this.targetUUID = targetUUID;
    }

    public ApplyAnimationPacket(FriendlyByteBuf buf) {
        this.targetUUID = buf.readUUID();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(targetUUID);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            context.setPacketHandled(true);
            ServerPlayer target;
            ServerPlayer sender = context.getSender();
            if(sender == null || sender.getServer() == null) return;
            if(this.targetUUID == null) target = sender;
            else target = sender.getServer().getPlayerList().getPlayer(this.targetUUID);
            if(target == null) return;
            IAnimationService<?, ?> service = getService();
            if(service == null) return;
            if(target == sender) return;
            service.apply(sender, target);
        });
    }

    @Override
    public boolean filter(IAnimationService<?, ?> helper) {
        return true;
    }
}
