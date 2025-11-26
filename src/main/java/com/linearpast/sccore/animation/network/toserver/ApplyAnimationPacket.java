package com.linearpast.sccore.animation.network.toserver;

import com.linearpast.sccore.animation.helper.IAnimationHelper;
import com.linearpast.sccore.animation.network.HelperGetterPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class ApplyAnimationPacket extends HelperGetterPacket {
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
            IAnimationHelper<?, ?> helper = getHelper();
            if(helper == null) return;
            if(target == sender) return;
            helper.apply(sender, target);
        });
    }

    @Override
    public boolean filter(IAnimationHelper<?, ?> helper) {
        return true;
    }
}
