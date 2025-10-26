package com.linearpast.sccore.animation.registry;

import com.linearpast.sccore.animation.AnimationUtils;
import com.linearpast.sccore.animation.capability.AnimationDataCapability;
import com.linearpast.sccore.animation.capability.inter.IAnimationCapability;
import com.linearpast.sccore.animation.data.Animation;
import com.linearpast.sccore.animation.network.toclient.AnimationCapabilityPacket;
import com.linearpast.sccore.animation.network.toclient.SyncAnimationPacket;
import com.linearpast.sccore.animation.network.toserver.PlayAnimationRequestPacket;
import com.linearpast.sccore.animation.network.toserver.PlayAnimationRidePacket;
import com.linearpast.sccore.animation.network.toserver.RefreshAnimationPacket;
import com.linearpast.sccore.capability.CapabilityUtils;
import com.linearpast.sccore.capability.data.player.PlayerCapabilityRegistry;
import com.linearpast.sccore.capability.network.CapabilityChannel;
import com.linearpast.sccore.core.ModChannel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.NetworkDirection;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class AnimationRegistry {
    private static final Map<ResourceLocation, Animation> animations = new HashMap<>();

    public static Map<ResourceLocation, Animation> getAnimations() {
        return animations;
    }

    @Nullable
    public static Animation getAnimation(ResourceLocation location) {
        return animations.get(location);
    }

    public static void registerAnimation(ResourceLocation location, Animation animation) {
        animations.put(location, animation);
    }

    public static boolean isAnimationPresent(ResourceLocation location) {
        return animations.containsKey(location);
    }

    public static void addAnimationListener(IEventBus forgeBus, IEventBus modBus) {
        AnimationUtils.ANIMATION_RUNNER.testLoadedAndAddListener(forgeBus, modBus);
    }

    private static void registerAnimationCapability() {
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
    }

    private static void registerChannel() {
        ModChannel.INSTANCE.messageBuilder(SyncAnimationPacket.class, ModChannel.getAndAddCid(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncAnimationPacket::new)
                .encoder(SyncAnimationPacket::encode)
                .consumerMainThread(SyncAnimationPacket::handle)
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

    public static void register(){
        AnimationUtils.ANIMATION_RUNNER.testLoadedAndRun(() -> {
            registerAnimationCapability();
            registerChannel();
        });
    }
}
