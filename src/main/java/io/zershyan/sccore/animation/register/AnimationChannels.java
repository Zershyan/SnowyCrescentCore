package io.zershyan.sccore.animation.register;

import io.zershyan.sccore.animation.network.toclient.AnimationClearPacket;
import io.zershyan.sccore.animation.network.toclient.AnimationClientStatusPacket;
import io.zershyan.sccore.animation.network.toclient.AnimationJsonPacket;
import io.zershyan.sccore.animation.network.toclient.SyncAnimationPacket;
import io.zershyan.sccore.animation.network.toserver.*;
import io.zershyan.sccore.core.ModChannel;
import net.minecraftforge.network.NetworkDirection;

public class AnimationChannels {
    public static void registerChannel() {
        ModChannel.INSTANCE.messageBuilder(SyncAnimationPacket.class, cid(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncAnimationPacket::new)
                .encoder(SyncAnimationPacket::encode)
                .consumerMainThread(SyncAnimationPacket::handle)
                .add();
        ModChannel.INSTANCE.messageBuilder(AnimationJsonPacket.class, cid(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(AnimationJsonPacket::new)
                .encoder(AnimationJsonPacket::encode)
                .consumerMainThread(AnimationJsonPacket::handle)
                .add();
        ModChannel.INSTANCE.messageBuilder(AnimationClientStatusPacket.class, cid(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(AnimationClientStatusPacket::new)
                .encoder(AnimationClientStatusPacket::encode)
                .consumerMainThread(AnimationClientStatusPacket::handle)
                .add();
        ModChannel.INSTANCE.messageBuilder(AnimationClearPacket.class, cid(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(AnimationClearPacket::new)
                .encoder(AnimationClearPacket::encode)
                .consumerMainThread(AnimationClearPacket::handle)
                .add();

        //To server
        ModChannel.INSTANCE.messageBuilder(PlayAnimationPacket.class, cid(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(PlayAnimationPacket::new)
                .encoder(PlayAnimationPacket::encode)
                .consumerMainThread(PlayAnimationPacket::handle)
                .add();
        ModChannel.INSTANCE.messageBuilder(PlayAnimationRidePacket.class, cid(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(PlayAnimationRidePacket::new)
                .encoder(PlayAnimationRidePacket::encode)
                .consumerMainThread(PlayAnimationRidePacket::handle)
                .add();
        ModChannel.INSTANCE.messageBuilder(ApplyAnimationPacket.class, cid(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(ApplyAnimationPacket::new)
                .encoder(ApplyAnimationPacket::encode)
                .consumerMainThread(ApplyAnimationPacket::handle)
                .add();
        ModChannel.INSTANCE.messageBuilder(InviteAnimationPacket.class, cid(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(InviteAnimationPacket::new)
                .encoder(InviteAnimationPacket::encode)
                .consumerMainThread(InviteAnimationPacket::handle)
                .add();
        ModChannel.INSTANCE.messageBuilder(RequestAnimationPacket.class, cid(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(RequestAnimationPacket::new)
                .encoder(RequestAnimationPacket::encode)
                .consumerMainThread(RequestAnimationPacket::handle)
                .add();
        ModChannel.INSTANCE.messageBuilder(StopAnimationPacket.class, cid(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(StopAnimationPacket::new)
                .encoder(StopAnimationPacket::encode)
                .consumerMainThread(StopAnimationPacket::handle)
                .add();
    }

    private static int cid() {
        return ModChannel.getAndAddCid();
    }
}
