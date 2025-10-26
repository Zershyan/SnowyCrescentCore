package com.linearpast.sccore.animation;

import com.linearpast.sccore.animation.capability.AnimationDataCapability;
import com.linearpast.sccore.animation.capability.inter.IAnimationCapability;
import com.linearpast.sccore.animation.data.Animation;
import com.linearpast.sccore.animation.data.Ride;
import com.linearpast.sccore.animation.entity.AnimationRideEntity;
import com.linearpast.sccore.animation.event.AnimationLayerRegistry;
import com.linearpast.sccore.animation.event.EntityRendererRegistry;
import com.linearpast.sccore.animation.event.client.CameraAnglesModify;
import com.linearpast.sccore.animation.event.client.ClientPlayerTick;
import com.linearpast.sccore.animation.network.toserver.RefreshAnimationPacket;
import com.linearpast.sccore.animation.registry.AnimationEntities;
import com.linearpast.sccore.animation.registry.AnimationRegistry;
import com.linearpast.sccore.core.ModChannel;
import com.linearpast.sccore.core.ModLazyRun;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.IEventBus;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * Animation Util. May be you can call it Api.
 */
public class AnimationUtils {
    public static final String AnimModId = "playeranimator";
    public static final ModLazyRun ANIMATION_RUNNER = new ModLazyRun(AnimModId) {
        @Override
        public void addCommonListener(IEventBus forgeBus, IEventBus modBus) {
            AnimationEntities.register(modBus);
            modBus.addListener(AnimationLayerRegistry::onCommonSetUp);
        }

        @Override
        public void addClientListener(IEventBus forgeBus, IEventBus modBus) {
            forgeBus.addListener(CameraAnglesModify::changeCameraView);
            modBus.addListener(AnimationLayerRegistry::onClientSetup);
            modBus.addListener(EntityRendererRegistry::registerEntityRenderer);
            forgeBus.addListener(ClientPlayerTick::onPlayerTick);
            forgeBus.addListener(ClientPlayerTick::delayRuns);
        }
    };

    /**
     * <pre>
     * Play animation.
     * If run in Dist.CLIENT, the serverPlayer can be null.
     * If animation be null, it will remove animation on layer.
     * </pre>
     * @param serverPlayer Target player
     * @param layer Target layer
     * @param animation Animation
     * @return If success
     */
    public static boolean playAnimation(@Nullable ServerPlayer serverPlayer, ResourceLocation layer, @Nullable ResourceLocation animation) {
        return ANIMATION_RUNNER.testLoadedAndCall(() -> {
            if(isAnimationLayerPresent(layer) && (animation == null || isAnimationPresent(animation))) {
                if(serverPlayer != null) {
                    return AnimationPlayer.serverPlayAnimation(serverPlayer, layer, animation);
                }else {
                    AnimationPlayer.requestAnimationToServer(layer, animation);
                    return true;
                }
            }
            return false;
        });
    }

    /**
     * <pre>
     * Play animation with ride. Player will ride an entity, then play animation.
     * When player unride, animation will be remove.
     * If run in Dist.CLIENT, the serverPlayer can be null.
     * If animation be null, it will call function: {@link ServerPlayer#unRide()}
     * If player is riding and the "force" is false, it will return false
     * </pre>
     * @param serverPlayer Target player
     * @param layer Target layer
     * @param animation Animation
     * @param force If force to ride and play animation
     * @return If success
     */
    public static boolean playAnimationWithRide(@Nullable ServerPlayer serverPlayer, ResourceLocation layer, @Nullable ResourceLocation animation, boolean force) {
        return ANIMATION_RUNNER.testLoadedAndCall(() -> {
            if(isAnimationLayerPresent(layer) && (animation == null || isAnimationPresent(animation))) {
                Animation anim = AnimationUtils.getAnimation(animation);
                if(anim != null && anim.getRide() == null) return false;
                if(serverPlayer != null) {
                    if(serverPlayer.getVehicle() != null && force) serverPlayer.unRide();
                    else if(serverPlayer.getVehicle() != null) return false;
                    AnimationPlayer.playAnimationWithRide(serverPlayer, layer, animation, true);
                } else {
                    AnimationPlayer.requestAnimationRideToServer(layer, animation, force);
                    return true;
                }
            }
            return false;
        });
    }

    /**
     * Remove animation.
     * @see AnimationUtils#playAnimation
     * @param serverPlayer Target player
     * @param layer Target layer
     * @return If success
     */
    public static boolean removeAnimation(@Nullable ServerPlayer serverPlayer, ResourceLocation layer) {
        return playAnimation(serverPlayer, layer, null);
    }

    /**
     * Get animation which is playing now on player. <br>
     * If layer is null, it will return the first playing animation which can be found.
     * @param player Target player
     * @param layer Target layer
     * @return Playing animation resource location
     */
    @Nullable
    public static ResourceLocation getAnimationPlaying(Player player, @Nullable ResourceLocation layer) {
        return ANIMATION_RUNNER.testLoadedAndCall(() -> {
            IAnimationCapability data = AnimationDataCapability.getCapability(player).orElse(null);
            if(data == null) return null;
            if(layer == null){
                for (ResourceLocation value : data.getAnimations().values()) {
                    if(value != null) return value;
                }
            } else if (isAnimationLayerPresent(layer)) {
                if(data.isAnimationPresent(layer)){
                    return data.getAnimation(layer);
                }
            }
            return null;
        });
    }

    /**
     * Test if layer exist animation which is playing.
     * <p>
     * Only in dist client
     * @param player Target player
     * @param layer Target layer
     * @return If layer exist animation which is playing
     */
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("unchecked")
    public static boolean isClientAnimationPlaying(AbstractClientPlayer player, @Nullable ResourceLocation layer) {
        return ANIMATION_RUNNER.testLoadedAndCall(() -> {
            try {
                Set<ResourceLocation> resourceLocations = new HashSet<>();
                if(layer == null) resourceLocations = AnimationLayerRegistry.getAnimLayers().keySet();
                else resourceLocations.add(layer);
                for (ResourceLocation location : resourceLocations) {
                    ModifierLayer<IAnimation> animationModifierLayer = (ModifierLayer<IAnimation>) PlayerAnimationAccess
                            .getPlayerAssociatedData(player).get(location);
                    if(animationModifierLayer == null) continue;
                    KeyframeAnimationPlayer animation = (KeyframeAnimationPlayer) animationModifierLayer.getAnimation();
                    if(animation == null) return false;
                    int currentTick = animation.getCurrentTick();
                    int stopTick = animation.getStopTick();
                    return currentTick <= stopTick;
                }
            } catch (Exception ignored) {}
            return false;
        });
    }

    /**
     * Sync animation tick to client
     * @param player Player
     * @param target Target player
     * @param layer Target layer
     */
    public static void syncAnimation(ServerPlayer player, ServerPlayer target, ResourceLocation layer) {
        ANIMATION_RUNNER.testLoadedAndRun(() -> AnimationPlayer.syncAnimation(player, target, layer));
    }

    /**
     * Sync animation tick on client
     * @param player Player
     * @param target Target player
     * @param layer Target layer
     */
    @OnlyIn(Dist.CLIENT)
    public static void syncAnimation(AbstractClientPlayer player, AbstractClientPlayer target, ResourceLocation layer) {
        ANIMATION_RUNNER.testLoadedAndRun(() -> AnimationPlayer.syncAnimation(player, target, layer));
    }

    /**
     * Join animation.
     * @param player Will join player
     * @param target Joined player
     * @param force If force
     * @return If success
     */
    public static boolean joinAnimation(ServerPlayer player, ServerPlayer target, boolean force) {
        return ANIMATION_RUNNER.testLoadedAndCall(() -> {
            Entity vehicle = target.getVehicle();
            if(!(vehicle instanceof AnimationRideEntity rideEntity)) return false;
            int playerCount = rideEntity.getPlayers().size();
            Animation animation = rideEntity.getAnimation();
            if(animation == null) return false;
            Ride ride = animation.getRide();
            if(ride == null) return false;
            int maxCount = ride.getComponentAnimations().size();
            if(playerCount >= maxCount) return false;
            return player.startRiding(vehicle, force);
        });
    }

    /**
     * <pre>
     * Start animation together...
     * The max participants' count is depend on your animation.
     * Max count = Size of {@link Ride#getComponentAnimations()}
     * </pre>
     * @param player Leader
     * @param layer Target layer
     * @param animation Animation location
     * @param force If force start leader
     * @param participants Participants
     */
    public static void startAnimationTogether(
            ServerPlayer player,
            ResourceLocation layer,
            ResourceLocation animation,
            boolean force,
            ServerPlayer ... participants
    ) {
        AnimationUtils.playAnimationWithRide(player, layer, animation, force);
        for (ServerPlayer participant : participants) {
            AnimationUtils.joinAnimation(participant, player, force);
        }
    }

    /**
     * Detach animation
     * @param player Player
     */
    public static void detachAnimation(ServerPlayer player) {
        ANIMATION_RUNNER.testLoadedAndRun(() -> {
            if(player.getVehicle() instanceof AnimationRideEntity) {
                player.unRide();
            }
        });
    }

    /**
     * Clear Player animations.
     * @param serverPlayer Target player
     */
    public static void clearAnimation(ServerPlayer serverPlayer) {
        ANIMATION_RUNNER.testLoadedAndRun(() -> AnimationPlayer.clearAnimation(serverPlayer));
    }

    /**
     * Test if layer exist and has been register.
     * @param layer Target layer
     * @return If layer exist and has been register
     */
    public static boolean isAnimationLayerPresent(ResourceLocation layer) {
        return ANIMATION_RUNNER.testLoadedAndCall(() -> AnimationLayerRegistry.isLayerPresent(layer));
    }

    /**
     * Test if animation exist and has been register.
     * @param location Animation resource location
     * @return If animation exist and has been register
     */
    public static boolean isAnimationPresent(ResourceLocation location) {
        return ANIMATION_RUNNER.testLoadedAndCall(() -> AnimationRegistry.isAnimationPresent(location));
    }

    /**
     * Register an animation through static function
     * @param location Animation resource location
     * @param animation Animation data
     */
    public static void registerAnimation(ResourceLocation location, Animation animation) {
        ANIMATION_RUNNER.testLoadedAndRun(() -> AnimationRegistry.registerAnimation(location, animation));
    }

    /**
     * Register an animation layer through static function. <br>
     * The number is bigger and the priority is higher. <br>
     * It must run before these events : <br>
     * {@link net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent} <br>
     * {@link net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent}
     * @param location Layer location key
     * @param priority Layer priority,
     */
    public static void registerAnimationLayer(ResourceLocation location, int priority) {
        ANIMATION_RUNNER.testLoadedAndRun(() -> AnimationLayerRegistry.registerPlayerAnimation(location, priority));
    }

    /**
     * Get animation data from animation resource location. <br>
     * You will get null if you use it too early. <br>
     * Suggest only use it in game has loaded.
     * @param location Animation resource location
     * @return Animation data
     */
    @Nullable
    public static Animation getAnimation(ResourceLocation location) {
        return AnimationRegistry.getAnimation(location);
    }

    /**
     * Get the LyingType when there are animations which playing on player. <br>
     * And It will return the first which be found.
     * @param player Target player
     * @return The first LyingType it find.
     */
    @Nullable
    public static Animation.LyingType getSideView(Player player) {
        return ANIMATION_RUNNER.testLoadedAndCall(() -> {
            IAnimationCapability data = AnimationDataCapability.getCapability(player).orElse(null);
            if(data == null) return null;
            Animation.LyingType lyingType = null;
            for (ResourceLocation value : data.getAnimations().values()) {
                Animation animation = getAnimation(value);
                if(animation == null) return null;
                Animation.LyingType type = animation.getLyingType();
                if(type == null) continue;
                switch (type) {
                    case FRONT,BACK -> {}
                    case LEFT,RIGHT -> lyingType = animation.getLyingType();
                }
            }
            return lyingType;
        });
    }

    /**
     * Get the HeightModifier when there are animations which playing on player. <br>
     * And It will return the first which be found.
     * @param player Target player
     * @return The first HeightModifier it find.
     */
    public static float getHeightModifier(Player player) {
        Float result = ANIMATION_RUNNER.testLoadedAndCall(() -> {
            IAnimationCapability data = AnimationDataCapability.getCapability(player).orElse(null);
            if (data == null) return 1.0f;
            float heightModifier = 1.0f;
            for (ResourceLocation value : data.getAnimations().values()) {
                Animation animation = getAnimation(value);
                if (animation == null) continue;
                float animationHeightModifier = animation.getHeightModifier();
                heightModifier = Math.min(heightModifier, animationHeightModifier);
            }
            return heightModifier;
        });
        return result == null ? 1.0f : result;
    }

    /**
     * Test if animation is playing <br>
     * if not, it will remove the animation resource location on client
     * <p>
     * Only in dist client
     * @param clientPlayer Target player
     */
    @OnlyIn(Dist.CLIENT)
    public static void refreshAnimation(AbstractClientPlayer clientPlayer) {
        IAnimationCapability data = AnimationDataCapability.getCapability(clientPlayer).orElse(null);
        if(data == null) return;
        Set<ResourceLocation> oldLayers = new HashSet<>(data.getAnimations().keySet());
        oldLayers.forEach(layer -> {
            if (isClientAnimationPlaying(clientPlayer, layer)) {
                oldLayers.remove(layer);
            }
        });
        ModChannel.sendToServer(new RefreshAnimationPacket(oldLayers));
    }
}
