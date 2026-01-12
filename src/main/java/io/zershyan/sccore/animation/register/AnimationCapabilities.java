package io.zershyan.sccore.animation.register;

import io.zershyan.sccore.animation.capability.AnimationDataCapability;
import io.zershyan.sccore.animation.capability.RawAnimationDataCapability;
import io.zershyan.sccore.animation.capability.inter.IAnimationCapability;
import io.zershyan.sccore.animation.network.toclient.AnimationCapabilityPacket;
import io.zershyan.sccore.animation.network.toclient.RawAnimationCapabilityPacket;
import io.zershyan.sccore.capability.CapabilityUtils;
import io.zershyan.sccore.capability.data.player.PlayerCapabilityRegistry;
import io.zershyan.sccore.capability.network.CapabilityChannel;
import io.zershyan.sccore.core.ModChannel;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class AnimationCapabilities {
    public static void registerAnimationCapability() {
        CapabilityChannel channel = CapabilityUtils.createChannel();
        CapabilityUtils.registerPlayerCapabilityWithNetwork(
                AnimationDataCapability.key,
                new PlayerCapabilityRegistry.CapabilityRecord<>(
                        AnimationDataCapability.class,
                        CapabilityManager.get(new CapabilityToken<>() {}),
                        IAnimationCapability.class
                ),
                channel,
                ModChannel.getAndAddCid(),
                AnimationCapabilityPacket.class,
                AnimationCapabilityPacket::new,
                AnimationCapabilityPacket::encode,
                AnimationCapabilityPacket::handle
        );
        CapabilityUtils.registerPlayerCapabilityWithNetwork(
                RawAnimationDataCapability.key,
                new PlayerCapabilityRegistry.CapabilityRecord<>(
                        RawAnimationDataCapability.class,
                        CapabilityManager.get(new CapabilityToken<>() {}),
                        RawAnimationDataCapability.class
                ),
                channel,
                ModChannel.getAndAddCid(),
                RawAnimationCapabilityPacket.class,
                RawAnimationCapabilityPacket::new,
                RawAnimationCapabilityPacket::encode,
                RawAnimationCapabilityPacket::handle
        );
    }
}
