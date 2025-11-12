package com.linearpast.sccore.animation;

import com.linearpast.sccore.animation.capability.AnimationDataCapability;
import com.linearpast.sccore.animation.capability.inter.IAnimationCapability;
import com.linearpast.sccore.animation.data.Animation;
import com.linearpast.sccore.animation.data.Ride;
import com.linearpast.sccore.animation.entity.AnimationRideEntity;
import com.linearpast.sccore.animation.event.PlayerTickEvent;
import com.linearpast.sccore.animation.event.client.CameraAnglesModify;
import com.linearpast.sccore.animation.event.client.ClientPlayerTick;
import com.linearpast.sccore.animation.event.client.EntityRendererRegisterEvent;
import com.linearpast.sccore.animation.network.toserver.RefreshAnimationPacket;
import com.linearpast.sccore.animation.register.AnimationCapabilities;
import com.linearpast.sccore.animation.register.AnimationChannels;
import com.linearpast.sccore.animation.register.AnimationEntities;
import com.linearpast.sccore.animation.register.AnimationRegistry;
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
import net.minecraftforge.common.util.FakePlayer;
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
            forgeBus.addListener(AnimationRegistry::onServerStarted);
            forgeBus.addListener(AnimationRegistry::onPlayerLoggedIn);
            forgeBus.addListener(PlayerTickEvent::onPlayerTickEvent);
        }

        @Override
        public void addClientListener(IEventBus forgeBus, IEventBus modBus) {
            forgeBus.addListener(CameraAnglesModify::changeCameraView);
            modBus.addListener(EntityRendererRegisterEvent::registerEntityRenderer);
            forgeBus.addListener(ClientPlayerTick::onPlayerTick);
            forgeBus.addListener(ClientPlayerTick::delayRuns);
        }
    };

    /**
     * <pre>
     * Play animation.
     * If run in Dist.CLIENT, player can be null, it will play animation only client.
     * If animation be null, it will remove animation on layer.
     * </pre>
     * @param player Target player
     * @param layer Target layer
     * @param animation Animation
     * @return If success
     */
    public static boolean playAnimation(@Nullable Player player, ResourceLocation layer, @Nullable ResourceLocation animation) {
        return ANIMATION_RUNNER.testLoadedAndCall(() -> {
            if(isAnimationLayerPresent(layer) && (animation == null || isAnimationPresent(animation))) {
                if(player instanceof ServerPlayer serverPlayer) {
                    if(serverPlayer instanceof FakePlayer) return false;
                    return AnimationPlayer.serverPlayAnimation(serverPlayer, layer, animation);
                }else if(player == null || player instanceof AbstractClientPlayer) {
                    AnimationPlayer.requestAnimationToServer((AbstractClientPlayer) player, layer, animation);
                    return true;
                }
            }
            return false;
        });
    }

    /**
     * Client send request to server and run play animation. <br>
     * Only play animation with client self.
     * @param layer Target layer
     * @param animation Target animation
     * @return If success
     */
    @OnlyIn(Dist.CLIENT)
    public static boolean requestAnimationClient(@Nullable AbstractClientPlayer player, ResourceLocation layer, @Nullable ResourceLocation animation) {
        return ANIMATION_RUNNER.testLoadedAndCall(() -> {
            if(isAnimationLayerPresent(layer) && (animation == null || isAnimationPresent(animation))) {
                AnimationPlayer.requestAnimationToServer(player, layer, animation);
                return true;
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
                    if(serverPlayer instanceof FakePlayer) return false;
                    if(serverPlayer.getVehicle() != null && force) serverPlayer.unRide();
                    else if(serverPlayer.getVehicle() != null) return false;
                    return AnimationPlayer.playAnimationWithRide(serverPlayer, layer, animation, true);
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
     * @param player Target player
     * @param layer Target layer
     * @return If success
     */
    public static boolean removeAnimation(@Nullable Player player, ResourceLocation layer) {
        return playAnimation(player, layer, null);
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
     * Test if layer exist animation which is not stop.
     * <p>
     * Only in dist client
     * @param player Target player
     * @param layer Target layer
     * @return True when the currentTick not larger than stopTick
     */
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("unchecked")
    public static boolean isClientAnimationNotStop(AbstractClientPlayer player, @Nullable ResourceLocation layer) {
        return ANIMATION_RUNNER.testLoadedAndCall(() -> {
            try {
                Set<ResourceLocation> resourceLocations = new HashSet<>();
                if(layer == null) resourceLocations = AnimationRegistry.getLayers().keySet();
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
     * Test if layer exist animation which is not end.
     * <p>
     * Only in dist client
     * @param player Target player
     * @param layer Target layer
     * @return True when animation is loop, or currentTick not larger than endTick
     */
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("unchecked")
    public static boolean isClientAnimationNotEnd(AbstractClientPlayer player, @Nullable ResourceLocation layer) {
        return ANIMATION_RUNNER.testLoadedAndCall(() -> {
            try {
                Set<ResourceLocation> resourceLocations = new HashSet<>();
                if(layer == null) resourceLocations = AnimationRegistry.getLayers().keySet();
                else resourceLocations.add(layer);
                for (ResourceLocation location : resourceLocations) {
                    ModifierLayer<IAnimation> animationModifierLayer = (ModifierLayer<IAnimation>) PlayerAnimationAccess
                            .getPlayerAssociatedData(player).get(location);
                    if(animationModifierLayer == null) continue;
                    KeyframeAnimationPlayer animation = (KeyframeAnimationPlayer) animationModifierLayer.getAnimation();
                    if(animation == null) return false;
                    int currentTick = animation.getCurrentTick();
                    boolean isLoop = animation.getData().isInfinite;
                    int endTick = animation.getData().endTick;
                    return isLoop || currentTick <= endTick;
                }
            } catch (Exception ignored) {}
            return false;
        });
    }

    /**
     * Sync animation tick to client
     * @param player Player
     * @param target Target player
     */
    public static void syncAnimation(ServerPlayer player, ServerPlayer target) {
        ANIMATION_RUNNER.testLoadedAndRun(() -> AnimationPlayer.syncAnimation(player, target));
    }

    /**
     * Sync animation tick on client
     * @param player Player
     * @param target Target player
     */
    @OnlyIn(Dist.CLIENT)
    public static void syncAnimation(AbstractClientPlayer player, AbstractClientPlayer target) {
        ANIMATION_RUNNER.testLoadedAndRun(() -> AnimationPlayer.syncAnimation(player, target));
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
     * Test if layer exist and has been invite.
     * @param layer Target layer
     * @return If layer exist and has been invite
     */
    public static boolean isAnimationLayerPresent(ResourceLocation layer) {
        return ANIMATION_RUNNER.testLoadedAndCall(() ->  AnimationRegistry.getLayers().containsKey(layer));
    }

    /**
     * Test if animation exist and has been invite.
     * @param location Animation resource location
     * @return If animation exist and has been invited
     */
    public static boolean isAnimationPresent(ResourceLocation location) {
        return ANIMATION_RUNNER.testLoadedAndCall(() -> AnimationRegistry.getAnimations().containsKey(location));
    }

    /**
     * The register handler
     * @param forgeBus Forge event bus
     * @param modBus Mod event bus
     */
    public static void register(IEventBus forgeBus, IEventBus modBus){
        AnimationUtils.ANIMATION_RUNNER.testLoadedAndRun(() -> {
            AnimationCapabilities.registerAnimationCapability();
            AnimationChannels.registerChannel();
        });
        AnimationUtils.ANIMATION_RUNNER.testLoadedAndAddListener(forgeBus, modBus);
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
        return AnimationRegistry.getAnimations().getOrDefault(location, null);
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
        boolean dirty = false;
        for (ResourceLocation layer : Set.copyOf(oldLayers)) {
            if (!isClientAnimationNotStop(clientPlayer, layer)) {
                oldLayers.remove(layer);
                dirty = true;
            }
        }
        if(dirty) ModChannel.sendToServer(new RefreshAnimationPacket(oldLayers));
    }
}
