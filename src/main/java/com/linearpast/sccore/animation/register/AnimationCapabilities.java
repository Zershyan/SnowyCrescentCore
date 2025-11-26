package com.linearpast.sccore.animation.register;

import com.linearpast.sccore.animation.capability.AnimationDataCapability;
import com.linearpast.sccore.animation.capability.RawAnimationDataCapability;
import com.linearpast.sccore.animation.capability.inter.IAnimationCapability;
import com.linearpast.sccore.animation.network.toclient.AnimationCapabilityPacket;
import com.linearpast.sccore.animation.network.toclient.RawAnimationCapabilityPacket;
import com.linearpast.sccore.capability.CapabilityUtils;
import com.linearpast.sccore.capability.data.player.PlayerCapabilityRegistry;
import com.linearpast.sccore.capability.network.CapabilityChannel;
import com.linearpast.sccore.core.ModChannel;
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
