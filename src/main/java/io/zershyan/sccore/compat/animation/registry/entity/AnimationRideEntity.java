package io.zershyan.sccore.compat.animation.registry.entity;

import io.zershyan.sccore.SCCore;
import io.zershyan.sccore.compat.animation.api.SCCAnimationApi;
import io.zershyan.sccore.compat.animation.api.data.AnimationHelper;
import io.zershyan.sccore.compat.animation.core.ServerAnimationRegistry;
import io.zershyan.sccore.compat.animation.core.SyncAnimationFactory;
import io.zershyan.sccore.compat.animation.data.Animation;
import io.zershyan.sccore.compat.animation.data.RideData;
import io.zershyan.sccore.compat.animation.data.ServerAnimation;
import io.zershyan.sccore.compat.animation.registry.AnimationEntities;
import io.zershyan.sccore.compat.animation.registry.AnimationEntityDataSerializers;
import io.zershyan.sccore.compat.animation.registry.attachment.PlayerAnimations;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AnimationRideEntity extends Entity {
    private static final EntityDataAccessor<UUID> OWNER_UUID;
    private static final EntityDataAccessor<ResourceLocation> LAYER;
    private static final EntityDataAccessor<ResourceLocation> ANIMATION_ID;
    private static final EntityDataAccessor<LinkedHashMap<ResourceLocation, UUID>> COMPONENT_PLAYERS_UUID;

    static {
        OWNER_UUID = SynchedEntityData.defineId(AnimationRideEntity.class, AnimationEntityDataSerializers.UUID.get());
        LAYER = SynchedEntityData.defineId(AnimationRideEntity.class, AnimationEntityDataSerializers.RESOURCE_LOCATION.get());
        ANIMATION_ID = SynchedEntityData.defineId(AnimationRideEntity.class, AnimationEntityDataSerializers.RESOURCE_LOCATION.get());
        COMPONENT_PLAYERS_UUID = SynchedEntityData.defineId(AnimationRideEntity.class, AnimationEntityDataSerializers.RL_UUID_LINKED_MAP.get());
    }

    public AnimationRideEntity(EntityType<? extends Entity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.noPhysics = true;
    }

    public AnimationRideEntity(Level pLevel) {
        this(AnimationEntities.RIDE.get(), pLevel);
    }

    public AnimationRideEntity(ServerPlayer pPlayer, ResourceLocation layer, RideAnimation animation) {
        this(pPlayer.level());
        this.owner = pPlayer;
        this.layer = layer;
        this.animation = animation;
        Optional<RideData> rideData = animation.animation().rideData();
        LinkedHashMap<ResourceLocation, UUID> map = new LinkedHashMap<>();
        rideData.ifPresent(ride -> {
            List<ResourceLocation> componentAnimations = ride.componentAnimations();
            for (ResourceLocation componentAnimation : componentAnimations) {
                componentPlayers.put(componentAnimation, null);
                map.put(componentAnimation, null);
            }
        });

        entityData.set(OWNER_UUID, getOwner().getUUID());
        entityData.set(LAYER, layer);
        entityData.set(ANIMATION_ID, animation.animLoc());
        entityData.set(COMPONENT_PLAYERS_UUID, map);
    }
    public record RideAnimation(ResourceLocation animLoc, Animation animation) {}

    private final ComponentPlayer componentPlayers = new ComponentPlayer();
    private ResourceLocation layer;
    private RideAnimation animation;
    private ServerPlayer owner;

    public Map<ResourceLocation, ServerPlayer> getComponentPlayers() {
        return Map.copyOf(componentPlayers);
    }

    public ResourceLocation getLayer() {
        return layer;
    }

    public RideAnimation getAnimation() {
        return animation;
    }

    public ServerPlayer getOwner() {
        return owner;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        builder.define(OWNER_UUID, new UUID(0, 0));
        builder.define(LAYER, SCCore.id(""));
        builder.define(ANIMATION_ID, SCCore.id(""));
        builder.define(COMPONENT_PLAYERS_UUID, new LinkedHashMap<>());
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag compoundTag) {}

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag compoundTag) {}


    @Override
    public void tick() {
        super.tick();
        if(!this.level().isClientSide) {
            RideData ride = animation == null ? null : animation.animation().rideData().orElse(null);
            if(owner == null || !this.getPassengers().contains(owner) || (ride != null && ride.existTick() > 0 && this.tickCount >= ride.existTick())) {
                this.remove(RemovalReason.DISCARDED);
            }
        }
    }

    @Override
    protected boolean canRide(@NotNull Entity vehicle) {
        return true;
    }

    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    @Nullable
    public static AnimationRideEntity create(Player player, ResourceLocation layer, RideAnimation anim) {
        return create(player, layer, anim, false, player.position());
    }

    @Nullable
    public static AnimationRideEntity create(Player player, ResourceLocation layer, RideAnimation anim, boolean force) {
        return create(player, layer, anim, force, player.position());
    }

    @Nullable
    public static AnimationRideEntity create(Player pPlayer, ResourceLocation layer, RideAnimation anim, boolean force, Vec3 pos) {
        if(!(pPlayer instanceof ServerPlayer player)) return null;
        if(anim == null) return null;
        Animation rideAnim = anim.animation();
        ResourceLocation animLoc = anim.animLoc();
        if(rideAnim.rideData().isEmpty()) return null;
        RideData rideData = rideAnim.rideData().get();
        AnimationHelper helper = SCCAnimationApi.animation(player);
        helper.playRideAnimation(PlayerAnimations.RideAnim.of(layer, animLoc));
        AnimationRideEntity seat = new AnimationRideEntity(player, layer, anim);
        float xRot = rideData.xRot();
        float yRot = rideData.yRot();
        if(xRot == 0 && yRot == 0) seat.setRot(player.getXRot(), player.getYRot());
        else seat.setRot(yRot, xRot);
        if(rideAnim instanceof ServerAnimation) pos.add(rideData.offset());
        seat.setPos(pos.x, pos.y + 0.6f, pos.z);
        player.level().addFreshEntity(seat);
        player.startRiding(seat, force);
        return seat;
    }

    @Override
    protected void positionRider(@NotNull Entity passenger, @NotNull MoveFunction callback) {
        super.positionRider(passenger, callback);
        passenger.setYBodyRot(this.getYRot());
    }

    @Override
    public void onPassengerTurned(@NotNull Entity pEntityToUpdate) {
        pEntityToUpdate.setYBodyRot(this.getYRot());
    }

    @Override
    public @NotNull Vec3 getDismountLocationForPassenger(@NotNull LivingEntity entity) {
        if(animation.animation() instanceof ServerAnimation anim && anim.rideData().isPresent()) {
            Vec3 position = entity.position();
            return position.subtract(anim.rideData().get().offset());
        }
        return entity.position();
    }

    @Override
    public boolean canAddPassenger(@NotNull Entity pPassenger) {
        if(pPassenger instanceof ServerPlayer) {
            if(getPassengers().isEmpty()) return true;
            int size = Math.toIntExact(componentPlayers.values().stream().filter(Objects::nonNull).count());
            int maxSize = componentPlayers.size();
            return size < maxSize;
        }
        return false;
    }

    @Override
    protected void addPassenger(@NotNull Entity entity) {
        int passengerNum = getPassengers().size();
        super.addPassenger(entity);
        if(passengerNum == 0) return;
        if(entity instanceof ServerPlayer serverPlayer) {
            Optional<RideData> optionalRideData = animation.animation().rideData();
            if(optionalRideData.isEmpty()) return;
            RideData rideData = optionalRideData.get();
            List<ResourceLocation> componentAnimations = rideData.componentAnimations();
            if(componentAnimations.isEmpty()) return;
            if(passengerNum > componentAnimations.size()) return;
            ResourceLocation animLocation = null;
            for (ResourceLocation location : componentPlayers.keySet()) {
                if(componentPlayers.get(location) != null) {
                    animLocation = location;
                    break;
                }
            }
            if(animLocation == null) return;
            componentPlayers.put(animLocation, serverPlayer);
            SCCAnimationApi.animation(serverPlayer).playRideAnimation(PlayerAnimations.RideAnim.of(layer, animLocation));
            SCCAnimationApi.ridePlayer(serverPlayer).syncRideAnim(owner);
        }
    }

    @Override
    protected void removePassenger(@NotNull Entity entity) {
        super.removePassenger(entity);
        if(entity instanceof ServerPlayer serverPlayer) {
            SCCAnimationApi.animation(serverPlayer).removeRideAnimation();
            Map.copyOf(componentPlayers).forEach((location, player) -> {
                if(player == serverPlayer) componentPlayers.remove(location);
            });
        }
    }

    @Nullable
    public Animation getServerAnimationOn(Player player) {
        if(player.getUUID().equals(entityData.get(OWNER_UUID))) {
            ResourceLocation animationId = entityData.get(ANIMATION_ID);
            Animation anim = ServerAnimationRegistry.getAnimations().getOrDefault(animationId, null);
            return anim == null ? SyncAnimationFactory.getAnimation(animationId) : anim;
        }
        ResourceLocation animLoc = null;
        for (Map.Entry<ResourceLocation, UUID> entry : entityData.get(COMPONENT_PLAYERS_UUID).entrySet()) {
            if(player.getUUID().equals(entry.getValue())) {
                animLoc = entry.getKey();
                break;
            }
        }
        if(animLoc == null) return null;
        Animation anim = ServerAnimationRegistry.getAnimations().getOrDefault(animLoc, null);
        return anim == null ? SyncAnimationFactory.getAnimation(animLoc) : anim;
    }

    public class ComponentPlayer extends TreeMap<ResourceLocation, ServerPlayer> {
        @Override
        public ServerPlayer put(ResourceLocation key, ServerPlayer value) {
            ServerPlayer put = super.put(key, value);
            if(value == null) return put;
            LinkedHashMap<ResourceLocation, UUID> map = entityData.get(COMPONENT_PLAYERS_UUID);
            map.put(key, value.getUUID());
            entityData.set(COMPONENT_PLAYERS_UUID, map);
            return put;
        }

        @Override
        public ServerPlayer remove(Object key) {
            ServerPlayer remove = super.remove(key);
            if(remove != null) {
                LinkedHashMap<ResourceLocation, UUID> map = entityData.get(COMPONENT_PLAYERS_UUID);
                if(map.remove(key) != null) entityData.set(COMPONENT_PLAYERS_UUID, map);
            }
            return remove;
        }
    }
}
