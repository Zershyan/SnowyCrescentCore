package com.linearpast.sccore.animation.register;

import com.linearpast.sccore.animation.network.toclient.AnimationClientStatusPacket;
import com.linearpast.sccore.animation.network.toclient.AnimationJsonPacket;
import com.linearpast.sccore.animation.network.toclient.SyncAnimationPacket;
import com.linearpast.sccore.animation.network.toserver.PlayAnimationRequestPacket;
import com.linearpast.sccore.animation.network.toserver.PlayAnimationRidePacket;
import com.linearpast.sccore.animation.network.toserver.RefreshAnimationPacket;
import com.linearpast.sccore.core.ModChannel;
import net.minecraftforge.network.NetworkDirection;

public class AnimationChannels {
    public static void registerChannel() {
        ModChannel.INSTANCE.messageBuilder(SyncAnimationPacket.class, ModChannel.getAndAddCid(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncAnimationPacket::new)
                .encoder(SyncAnimationPacket::encode)
                .consumerMainThread(SyncAnimationPacket::handle)
                .add();
        ModChannel.INSTANCE.messageBuilder(AnimationJsonPacket.class, ModChannel.getAndAddCid(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(AnimationJsonPacket::new)
                .encoder(AnimationJsonPacket::encode)
                .consumerMainThread(AnimationJsonPacket::handle)
                .add();
        ModChannel.INSTANCE.messageBuilder(AnimationClientStatusPacket.class, ModChannel.getAndAddCid(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(AnimationClientStatusPacket::new)
                .encoder(AnimationClientStatusPacket::encode)
                .consumerMainThread(AnimationClientStatusPacket::handle)
                .add();

        ModChannel.INSTANCE.messageBuilder(PlayAnimationRequestPacket.class, ModChannel.getAndAddCid(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(PlayAnimationRequestPacket::new)
                .encoder(PlayAnimationRequestPacket::encode)
                .consumerMainThread(PlayAnimationRequestPacket::handle)
                .add();
        ModChannel.INSTANCE.messageBuilder(PlayAnimationRidePacket.class, ModChannel.getAndAddCid(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(PlayAnimationRidePacket::new)
                .encoder(PlayAnimationRidePacket::encode)
                .consumerMainThread(PlayAnimationRidePacket::handle)
                .add();
        ModChannel.INSTANCE.messageBuilder(RefreshAnimationPacket.class, ModChannel.getAndAddCid(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(RefreshAnimationPacket::new)
                .encoder(RefreshAnimationPacket::encode)
                .consumerMainThread(RefreshAnimationPacket::handle)
                .add();
    }
}
