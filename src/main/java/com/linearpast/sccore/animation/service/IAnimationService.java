package com.linearpast.sccore.animation.service;

import com.linearpast.sccore.animation.data.AnimationData;
import com.linearpast.sccore.animation.entity.AnimationRideEntity;
import com.linearpast.sccore.animation.event.PlayerTickEvent;
import com.linearpast.sccore.animation.event.client.CameraAnglesModify;
import com.linearpast.sccore.animation.event.client.ClientPlayerEvent;
import com.linearpast.sccore.animation.event.client.EntityRendererRegisterEvent;
import com.linearpast.sccore.animation.event.create.AnimationEvent;
import com.linearpast.sccore.animation.network.toclient.SyncAnimationPacket;
import com.linearpast.sccore.animation.network.toserver.*;
import com.linearpast.sccore.animation.register.AnimationCapabilities;
import com.linearpast.sccore.animation.register.AnimationChannels;
import com.linearpast.sccore.animation.register.AnimationEntities;
import com.linearpast.sccore.animation.register.AnimationRegistry;
import com.linearpast.sccore.animation.utils.AnimationUtils;
import com.linearpast.sccore.animation.utils.ApiBack;
import com.linearpast.sccore.capability.data.ICapabilitySync;
import com.linearpast.sccore.core.ModChannel;
import com.linearpast.sccore.core.ModCompatRun;
import com.linearpast.sccore.core.configs.ModConfigs;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.LogicalSide;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Animation helper interface
 * @param <D> Animation data
 * @param <C> Capability
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
public interface IAnimationService<D extends AnimationData, C extends ICapabilitySync<?>>{
    String AnimModId = "playeranimator";
    /**
     * Lazy runner
     */
    ModCompatRun ANIMATION_RUNNER = new ModCompatRun(AnimModId) {
        @Override
        public void addCommonListener(IEventBus forgeBus, IEventBus modBus) {
            AnimationEntities.register(modBus);
            forgeBus.register(AnimationRegistry.class);
            forgeBus.register(PlayerTickEvent.class);
        }

        @Override
        public void addClientListener(IEventBus forgeBus, IEventBus modBus) {
            modBus.register(EntityRendererRegisterEvent.class);
            forgeBus.register(CameraAnglesModify.class);
            forgeBus.register(ClientPlayerEvent.class);
        }
    };

    //Cache info record, not persistent
    record ApplyAnimationRecord(UUID target, int expireTick) {}
    record InviteAnimationRecord(ResourceLocation layer, AnimationData animation, int expireTick, List<UUID> targets) {}
    record RequestAnimationRecord(ResourceLocation layer, AnimationData animation, int expireTick, UUID target, boolean isRide) {}

    //Apply & invite & request history record
    Map<UUID, ApplyAnimationRecord> applyMap = new ConcurrentHashMap<>();
    Map<UUID, InviteAnimationRecord> inviteMap = new ConcurrentHashMap<>();
    Map<UUID, RequestAnimationRecord> requestMap = new ConcurrentHashMap<>();

    //Last apply & invite & request tick map
    Map<UUID, Integer> lastApplyTickMap = new ConcurrentHashMap<>();
    Map<UUID, Integer> lastInviteTickMap = new ConcurrentHashMap<>();
    Map<UUID, Integer> lastRequestTickMap = new ConcurrentHashMap<>();

    /**
     * Equal to {@link AnimationRegistry#getLayers()}
     * @return Resource location set
     */
    default Set<ResourceLocation> getLayers() {
        return Set.copyOf(AnimationRegistry.getLayers().keySet());
    }

    /**
     * Get animation by location
     * @param location location
     * @return Animation data
     */
    @Nullable
    D getAnimation(ResourceLocation location);

    /**
     * Get animation by Tag (deserializeNBT)
     * @param tag tag
     * @return Animation data
     */
    @Nullable
    D getAnimation(CompoundTag tag);

    /**
     * Get capability
     * @param player player
     * @return Capability
     */
    @Nullable
    C getCapability(Player player);

    /**
     * To register handler
     * @param forgeBus Forge event bus
     * @param modBus Mod event bus
     */
    static void register(IEventBus forgeBus, IEventBus modBus){
        ANIMATION_RUNNER.testLoadedAndRun(() -> {
            AnimationCapabilities.registerAnimationCapability();
            AnimationChannels.registerChannel();
        });
        ANIMATION_RUNNER.testLoadedAndAddListener(forgeBus, modBus);
    }

    default void clearAnimations(AbstractClientPlayer player){
        for (ResourceLocation layer : getLayers()) {
            AnimationUtils.removeAnimation(player, layer);
        }
    }

    //clear animations
    void clearAnimations(ServerPlayer serverPlayer);
    //according to location, judge if animation is present
    boolean isAnimationPresent(ResourceLocation location);
    //stop riding
    default ApiBack detachAnimation(ServerPlayer player){
        return ANIMATION_RUNNER.testLoadedAndCall(() -> {
            if(player.getVehicle() instanceof AnimationRideEntity) {
                player.stopRiding();
                return ApiBack.SUCCESS;
            }
            return ApiBack.UNSUPPORTED;
        });
    };
    //start ride
    default ApiBack joinAnimationServer(ServerPlayer player, ServerPlayer target, boolean force){
        return ANIMATION_RUNNER.testLoadedAndCall(() -> {
            Entity vehicle = target.getVehicle();
            if(vehicle instanceof AnimationRideEntity) {
                boolean result = player.startRiding(vehicle, force);
                return result ? ApiBack.SUCCESS : ApiBack.FAIL;
            }
            return ApiBack.UNSUPPORTED;
        });
    };

    /**
     * Trigger event and let implementation class handle
     * @param player player
     * @param target target
     * @param force is force
     * @return Api back
     */
    default ApiBack joinAnimation(ServerPlayer player, ServerPlayer target, boolean force){
        return ANIMATION_RUNNER.testLoadedAndCall(() -> {
            AnimationEvent.Join playEvent = new AnimationEvent.Join(player, target, force);
            boolean post = MinecraftForge.EVENT_BUS.post(playEvent);
            Event.Result eventResult = playEvent.getResult();
            if(post || eventResult == Event.Result.DENY) return ApiBack.BE_CANCELLED;
            return joinAnimationServer(player, target, playEvent.isForce());
        });
    }

    /**
     * Sync animation tick to client
     * @param player Player
     * @param target Target player
     */
    default void syncAnimation(ServerPlayer player, ServerPlayer target) {
        ANIMATION_RUNNER.testLoadedAndRun(() -> {
            ModChannel.sendToPlayer(new SyncAnimationPacket(player.getUUID(), target.getUUID()), player);
            ModChannel.sendToPlayer(new SyncAnimationPacket(player.getUUID(), target.getUUID()), target);
        });
    }

    /**
     * Sync animation tick on client
     * @param player Player
     * @param target Target player
     */
    @OnlyIn(Dist.CLIENT)
    default void syncAnimation(AbstractClientPlayer player, AbstractClientPlayer target) {
        ANIMATION_RUNNER.testLoadedAndRun(() -> AnimationUtils.syncAnimation(player, target));
    }

    /**
     * Refresh animation throw capability
     * @param clientPlayer player
     */
    @OnlyIn(Dist.CLIENT)
    void refreshAnimation(AbstractClientPlayer clientPlayer);

    /**
     * Refresh animation on client, it will not sync to capability
     * @param clientPlayer player
     */
    @OnlyIn(Dist.CLIENT)
    default void refreshAnimationUnsafe(AbstractClientPlayer clientPlayer) {
        ANIMATION_RUNNER.testLoadedAndRun(() -> {
            Set<ResourceLocation> oldLayers = new HashSet<>(AnimationRegistry.getLayers().keySet());
            for (ResourceLocation layer : Set.copyOf(oldLayers)) {
                if (AnimationUtils.isClientAnimationStop(clientPlayer, layer)) {
                    removeAnimation(clientPlayer, layer);
                }
            }
        });
    }

    /**
     * Test if layer exist and has been invite.
     * @param layer Target layer
     * @return If layer exist and has been invited
     */
    default boolean isAnimationLayerPresent(ResourceLocation layer) {
        return ANIMATION_RUNNER.testLoadedAndCall(() ->  getLayers().contains(layer));
    }

    /**
     * Get animation which is playing now on player. <br>
     * If layer is null, it will return the first playing animation which can be found.
     * @param player Target player
     * @param layer Target layer
     * @return Playing animation resource location
     */
    @Nullable
    ResourceLocation getAnimationPlaying(Player player, @Nullable ResourceLocation layer);

    /**
     * Remove animation.
     * @param player Target player
     * @param layer Target layer
     * @return If success
     */
    @OnlyIn(Dist.CLIENT)
    default ApiBack removeAnimation(@Nullable AbstractClientPlayer player, ResourceLocation layer) {
        return ANIMATION_RUNNER.testLoadedAndCall(() -> {
            AnimationUtils.removeAnimation(player, layer);
            return ApiBack.SUCCESS;
        });
    }
    ApiBack removeAnimation(@NotNull ServerPlayer serverPlayer, ResourceLocation layer);

    /**
     * <pre>
     * Play animation with ride. Player will ride an entity, then play animation.
     * When player unride, animation will be remove.
     * If player is riding and the "force" is false, it will return false
     * </pre>
     * @param player Target player
     * @param layer Target layer
     * @param animation Animation
     * @param force If force to ride and play animation
     * @return If success
     */
    @OnlyIn(Dist.CLIENT)
    default ApiBack playAnimationWithRide(@Nullable AbstractClientPlayer player, ResourceLocation layer, ResourceLocation animation, boolean force) {
        return ANIMATION_RUNNER.testLoadedAndCall(() -> {
            if(isAnimationLayerPresent(layer) && isAnimationPresent(animation))
                return ApiBack.RESOURCE_NOT_FOUND;
            UUID uuid = player == null ? null : player.getUUID();
            AnimationData anim = getAnimation(animation);
            if(anim == null || anim.getRide() == null) return ApiBack.RESOURCE_NOT_FOUND;
            ModChannel.sendToServer(new PlayAnimationRidePacket(anim, layer, animation, uuid, force));
            return ApiBack.SUCCESS;
        });
    }
    default ApiBack playAnimationWithRide(@NotNull ServerPlayer player, ResourceLocation layer, AnimationData animation, boolean force) {
        return ANIMATION_RUNNER.testLoadedAndCall(() -> {
            ResourceLocation key = animation.getKey();
            if(!isAnimationLayerPresent(layer) || !isAnimationPresent(key))
                return ApiBack.RESOURCE_NOT_FOUND;
            if(animation.getRide() == null)
                return ApiBack.RESOURCE_NOT_FOUND;
            if(player instanceof FakePlayer)
                return ApiBack.UNSUPPORTED;
            boolean flag = player.getVehicle() != null;
            if(flag && force) player.stopRiding();
            else if(flag) return ApiBack.UNSUPPORTED;
            boolean result = AnimationRideEntity.create(player, layer, animation, force) != null;
            return result ? ApiBack.SUCCESS : ApiBack.FAIL;
        });
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
    @OnlyIn(Dist.CLIENT)
    default ApiBack playAnimation(@Nullable AbstractClientPlayer player, ResourceLocation layer, ResourceLocation animation) {
        return ANIMATION_RUNNER.testLoadedAndCall(() -> {
            if(isAnimationLayerPresent(layer) && isAnimationPresent(animation))
                return ApiBack.RESOURCE_NOT_FOUND;
            UUID uuid = player == null ? null : player.getUUID();
            AnimationData anim = getAnimation(animation);
            if(anim == null) return ApiBack.RESOURCE_NOT_FOUND;

            AnimationEvent.Play playEvent = new AnimationEvent.Play(
                    LogicalSide.CLIENT,
                    player,
                    layer,
                    anim
            );
            boolean post = MinecraftForge.EVENT_BUS.post(playEvent);
            Event.Result eventResult = playEvent.getResult();
            if(post || eventResult == Event.Result.DENY) return ApiBack.BE_CANCELLED;
            ModChannel.sendToServer(new PlayAnimationPacket(anim, layer, animation, uuid));
            return ApiBack.SUCCESS;
        });
    }

    /**
     * Trigger event and let implementation class handle
     * @param player player
     * @param layer target layer
     * @param animation animation
     * @return Api back
     */
    default ApiBack playAnimation(@NotNull ServerPlayer player, ResourceLocation layer, AnimationData animation){
        return ANIMATION_RUNNER.testLoadedAndCall(() -> {
            AnimationEvent.Play playEvent = new AnimationEvent.Play(LogicalSide.SERVER, player, layer, animation);
            boolean post = MinecraftForge.EVENT_BUS.post(playEvent);
            Event.Result eventResult = playEvent.getResult();
            if(post || eventResult == Event.Result.DENY) return ApiBack.BE_CANCELLED;
            return playAnimationServer(player, layer, animation);
        });
    }
    ApiBack playAnimationServer(@NotNull ServerPlayer player, ResourceLocation layer, AnimationData animation);

    /**
     * Request animation
     */
    default ApiBack request(ServerPlayer player, ServerPlayer target, ResourceLocation layer, AnimationData animation, boolean isRide) {
        return ANIMATION_RUNNER.testLoadedAndCall(() -> {
            if(!isAnimationLayerPresent(layer) || !isAnimationPresent(animation.getKey()))
                return ApiBack.RESOURCE_NOT_FOUND;
            int tickCount = player.server.getTickCount();
            UUID playerUUID = player.getUUID();
            UUID targetUUID = target.getUUID();

            int origin = ModConfigs.Server.requestValidTime.get() * 20;
            int cooldown = ModConfigs.Server.requestCooldown.get() * 20;
            AnimationEvent.Send sendEvent = new AnimationEvent.Send(LogicalSide.SERVER, origin, cooldown, AnimationEvent.Type.APPLY);
            boolean post = MinecraftForge.EVENT_BUS.post(sendEvent);
            if(post) return ApiBack.BE_CANCELLED;
            Event.Result eventResult = sendEvent.getResult();
            switch (eventResult) {
                case DENY : return ApiBack.BE_CANCELLED;
                case DEFAULT : {
                    //Test if is not in cooldown
                    int lastTick = lastRequestTickMap.getOrDefault(playerUUID, 0);
                    if(Math.max(tickCount - sendEvent.getCooldownTick(), 0) >= lastTick) {
                        lastRequestTickMap.put(playerUUID, tickCount);
                    } else return ApiBack.COOLDOWN;
                }
                case ALLOW : {
                    //Add to cache, done
                    int expireTick = sendEvent.getValidTick() + tickCount;
                    requestMap.put(playerUUID, new RequestAnimationRecord(layer, animation, expireTick, targetUUID, isRide));
                    return ApiBack.SUCCESS;
                }
                default: return ApiBack.UNSUPPORTED;
            }
        });
    }

    /**
     * Client request
     * @param target target
     * @param layer layer
     * @param animation animation
     * @return Api back
     */
    default ApiBack request(AbstractClientPlayer target, ResourceLocation layer, ResourceLocation animation) {
        return ANIMATION_RUNNER.testLoadedAndCall(() -> {
            if(!isAnimationLayerPresent(layer) || !isAnimationPresent(animation))
                return ApiBack.RESOURCE_NOT_FOUND;
            KeyframeAnimation keyframeAnimation = PlayerAnimationRegistry.getAnimation(animation);
            if(keyframeAnimation == null) return ApiBack.RESOURCE_NOT_FOUND;
            D data = getAnimation(animation);
            if(data == null) return ApiBack.RESOURCE_NOT_FOUND;
            AnimationEvent.Send sendEvent = new AnimationEvent.Send(
                    LogicalSide.CLIENT,
                    0,
                    0,
                    AnimationEvent.Type.REQUEST
            );
            boolean post = MinecraftForge.EVENT_BUS.post(sendEvent);
            Event.Result eventResult = sendEvent.getResult();
            if(post || eventResult == Event.Result.DENY) return ApiBack.BE_CANCELLED;
            ModChannel.sendToServer(new RequestAnimationPacket(data, layer, target.getUUID()));
            return ApiBack.SUCCESS;
        });
    }

    /**
     * Accept Request
     */
    default ApiBack acceptRequest(ServerPlayer player, ServerPlayer requestor) {
        return ANIMATION_RUNNER.testLoadedAndCall(() -> {
            UUID requestorUUID = requestor.getUUID();
            RequestAnimationRecord record = requestMap.getOrDefault(requestorUUID, null);
            if (record == null) return ApiBack.UNSUPPORTED;
            UUID uuid = player.getUUID();
            if (!record.target().equals(uuid)) return ApiBack.UNSUPPORTED;

            AnimationEvent.Accept acceptEvent = new AnimationEvent.Accept(AnimationEvent.Type.REQUEST, 0);
            boolean post = MinecraftForge.EVENT_BUS.post(acceptEvent);
            if(post) return ApiBack.BE_CANCELLED;
            Event.Result eventResult = acceptEvent.getResult();
            switch (eventResult) {
                case DENY : return ApiBack.BE_CANCELLED;
                case DEFAULT : {
                    //Test if is in valid time
                    int tickCount = requestor.server.getTickCount();
                    if (tickCount >= record.expireTick()) {
                        requestMap.remove(requestorUUID);
                        return ApiBack.OPERATION_EXPIRE;
                    }
                }
                case ALLOW : {
                    //done
                    ApiBack back;
                    if(record.isRide()) back = playAnimationWithRide(player, record.layer(), record.animation(), false);
                    else back = playAnimation(player, record.layer(), record.animation());
                    if(back == ApiBack.SUCCESS) requestMap.remove(requestorUUID);
                    return back;
                }
                default: return ApiBack.UNSUPPORTED;
            }
        });
    }

    /**
     * Send apply to join animation on server side
     */
    default ApiBack apply(ServerPlayer player, ServerPlayer target) {
        return ANIMATION_RUNNER.testLoadedAndCall(() -> {
            int tickCount = player.server.getTickCount();

            int origin = ModConfigs.Server.applyValidTime.get() * 20;
            int cooldown = ModConfigs.Server.applyCooldown.get() * 20;
            AnimationEvent.Send sendEvent = new AnimationEvent.Send(
                    LogicalSide.SERVER,
                    origin,
                    cooldown,
                    AnimationEvent.Type.APPLY
            );
            boolean post = MinecraftForge.EVENT_BUS.post(sendEvent);
            if(post) return ApiBack.BE_CANCELLED;
            Event.Result eventResult = sendEvent.getResult();
            switch (eventResult) {
                case DENY : return ApiBack.BE_CANCELLED;
                case DEFAULT : {
                    //Test if is not in cooldown
                    int lastTick = lastApplyTickMap.getOrDefault(player.getUUID(), 0);
                    if(Math.max(tickCount - sendEvent.getCooldownTick(), 0) >= lastTick) {
                        lastApplyTickMap.put(player.getUUID(), tickCount);
                    } else return ApiBack.COOLDOWN;
                }
                case ALLOW : {
                    //Add to cache, done
                    int expireTick = sendEvent.getValidTick() + tickCount;
                    applyMap.put(player.getUUID(), new ApplyAnimationRecord(target.getUUID(), expireTick));
                    return ApiBack.SUCCESS;
                }
                default: return ApiBack.UNSUPPORTED;
            }
        });
    }

    /**
     * Send apply to join animation on client side. <br>
     * It will send network packet and work on server side.
     */
    @OnlyIn(Dist.CLIENT)
    default ApiBack apply(AbstractClientPlayer target) {
        return ANIMATION_RUNNER.testLoadedAndCall(() -> {
            AnimationEvent.Send sendEvent = new AnimationEvent.Send(
                    LogicalSide.CLIENT,
                    0,
                    0,
                    AnimationEvent.Type.APPLY
            );
            boolean post = MinecraftForge.EVENT_BUS.post(sendEvent);
            Event.Result eventResult = sendEvent.getResult();
            if(post || eventResult == Event.Result.DENY) return ApiBack.BE_CANCELLED;
            ModChannel.sendToServer(new ApplyAnimationPacket(target.getUUID()));
            return ApiBack.SUCCESS;
        });
    }

    /**
     * Player accept join apply.
     * @param player Acceptor
     * @param applier Applier
     * @return If accept and riding success
     */
    default ApiBack acceptApply(ServerPlayer player, ServerPlayer applier) {
        return ANIMATION_RUNNER.testLoadedAndCall(() -> {
            ApplyAnimationRecord record = applyMap.getOrDefault(applier.getUUID(), null);
            if (record == null) return ApiBack.UNSUPPORTED;
            UUID uuid = player.getUUID();
            if (!record.target().equals(uuid)) return ApiBack.UNSUPPORTED;

            int maxDistance = ModConfigs.Server.applyValidDistance.get();
            AnimationEvent.Accept acceptEvent = new AnimationEvent.Accept(AnimationEvent.Type.APPLY, maxDistance);
            boolean post = MinecraftForge.EVENT_BUS.post(acceptEvent);
            if(post) return ApiBack.BE_CANCELLED;
            Event.Result eventResult = acceptEvent.getResult();
            switch (eventResult) {
                case DENY : return ApiBack.BE_CANCELLED;
                case DEFAULT : {
                    //Test if is in valid distance
                    int validDistance = acceptEvent.getValidDistance();
                    if(player.distanceToSqr(applier) > validDistance * validDistance) {
                        return ApiBack.OUT_RANGE;
                    }
                    int tickCount = applier.server.getTickCount();
                    if (tickCount > record.expireTick()) {
                        applyMap.remove(applier.getUUID());
                        return ApiBack.OPERATION_EXPIRE;
                    }
                }
                case ALLOW : {
                    //done
                    ApiBack back = joinAnimation(applier, player, false);
                    if(back == ApiBack.SUCCESS) applyMap.remove(applier.getUUID());
                    return back;
                }
                default: return ApiBack.UNSUPPORTED;
            }
        });
    }

    /**
     * Send invite on server side.
     * @param player Sender
     * @param animation Raw animation info
     * @param layer Target layer
     * @param targets Be invited players
     */
    default ApiBack invite(ServerPlayer player, ResourceLocation layer, AnimationData animation, Collection<UUID> targets) {
        return ANIMATION_RUNNER.testLoadedAndCall(() -> {
            if(!isAnimationLayerPresent(layer) || !isAnimationPresent(animation.getKey()))
                return ApiBack.RESOURCE_NOT_FOUND;
            int tickCount = player.server.getTickCount();

            int origin = ModConfigs.Server.inviteValidTime.get() * 20;
            int cooldown = ModConfigs.Server.inviteCooldown.get() * 20;
            AnimationEvent.Send sendEvent = new AnimationEvent.Send(
                    LogicalSide.SERVER,
                    origin,
                    cooldown,
                    AnimationEvent.Type.INVITE
            );
            boolean post = MinecraftForge.EVENT_BUS.post(sendEvent);
            if(post) return ApiBack.BE_CANCELLED;
            Event.Result eventResult = sendEvent.getResult();
            switch (eventResult) {
                case DENY : return ApiBack.BE_CANCELLED;
                case DEFAULT : {
                    //Test if is not in cooldown
                    int lastTick = lastInviteTickMap.getOrDefault(player.getUUID(), 0);
                    if(Math.max(tickCount - sendEvent.getCooldownTick(), 0) >= lastTick) {
                        lastInviteTickMap.put(player.getUUID(), tickCount);
                    } else return ApiBack.COOLDOWN;
                }
                case ALLOW : {
                    //Add to cache, done
                    int expireTick = sendEvent.getValidTick() + tickCount;
                    inviteMap.put(player.getUUID(), new InviteAnimationRecord(layer, animation, expireTick, new ArrayList<>(targets)));
                    return ApiBack.SUCCESS;
                }
                default: return ApiBack.UNSUPPORTED;
            }
        });
    }

    /**
     * Send invite on client side. <br>
     * It will send network packet and work on server side.
     * @param animation Raw animation info
     * @param layer Target layer
     * @param targets Be invited players
     * @return If send to server successfully.
     */
    @OnlyIn(Dist.CLIENT)
    default ApiBack invite(ResourceLocation layer, ResourceLocation animation, AbstractClientPlayer ... targets){
        return ANIMATION_RUNNER.testLoadedAndCall(() -> {
            if(!isAnimationLayerPresent(layer) || !isAnimationPresent(animation))
                return ApiBack.RESOURCE_NOT_FOUND;
            KeyframeAnimation keyframeAnimation = PlayerAnimationRegistry.getAnimation(animation);
            if(keyframeAnimation == null) return ApiBack.RESOURCE_NOT_FOUND;
            Set<UUID> list = Arrays.stream(targets).map(AbstractClientPlayer::getUUID).collect(Collectors.toSet());
            D data = getAnimation(animation);
            if(data == null) return ApiBack.RESOURCE_NOT_FOUND;
            AnimationEvent.Send sendEvent = new AnimationEvent.Send(
                    LogicalSide.CLIENT,
                    0,
                    0,
                    AnimationEvent.Type.INVITE
            );
            boolean post = MinecraftForge.EVENT_BUS.post(sendEvent);
            Event.Result eventResult = sendEvent.getResult();
            if(post || eventResult == Event.Result.DENY) return ApiBack.BE_CANCELLED;
            ModChannel.sendToServer(new InviteAnimationPacket(data, layer, list));
            return ApiBack.SUCCESS;
        });
    }

    /**
     * Player accept invite
     * @param player Acceptor
     * @param inviter Inviter
     * @return If accept and riding success.
     */
    default ApiBack acceptInvite(ServerPlayer player, ServerPlayer inviter) {
        return ANIMATION_RUNNER.testLoadedAndCall(() -> {
            InviteAnimationRecord record = inviteMap.getOrDefault(inviter.getUUID(), null);
            if(record == null) return ApiBack.UNSUPPORTED;
            UUID uuid = player.getUUID();
            if (!record.targets().contains(uuid)) return ApiBack.UNSUPPORTED;

            int maxDistance = ModConfigs.Server.inviteValidDistance.get();
            AnimationEvent.Accept acceptEvent = new AnimationEvent.Accept(AnimationEvent.Type.INVITE, maxDistance);
            boolean post = MinecraftForge.EVENT_BUS.post(acceptEvent);
            if(post) return ApiBack.BE_CANCELLED;
            Event.Result eventResult = acceptEvent.getResult();
            switch (eventResult) {
                case DENY : return ApiBack.BE_CANCELLED;
                case DEFAULT : {
                    //Test if is in valid distance
                    int validDistance = acceptEvent.getValidDistance();
                    if(player.distanceToSqr(inviter) > validDistance * validDistance) {
                        return ApiBack.OUT_RANGE;
                    }
                    int tickCount = inviter.server.getTickCount();
                    if(tickCount >= record.expireTick()) {
                        inviteMap.remove(inviter.getUUID());
                        return ApiBack.OPERATION_EXPIRE;
                    }
                }
                case ALLOW : {
                    //done
                    ApiBack apiBack = playAnimationWithRide(inviter, record.layer(), record.animation(), false);
                    if(apiBack == ApiBack.SUCCESS) {
                        if(record.targets().isEmpty()) inviteMap.remove(inviter.getUUID());
                        ApiBack back = joinAnimation(player, inviter, false);
                        if(back == ApiBack.SUCCESS) record.targets().remove(uuid);
                        return back;
                    } else return apiBack;
                }
                default: return ApiBack.UNSUPPORTED;
            }
        });
    }
}
