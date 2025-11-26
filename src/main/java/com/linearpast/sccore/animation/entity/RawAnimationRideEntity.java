package com.linearpast.sccore.animation.entity;

import com.linearpast.sccore.animation.capability.RawAnimationDataCapability;
import com.linearpast.sccore.animation.data.RawAnimationData;
import com.linearpast.sccore.animation.data.Ride;
import com.linearpast.sccore.animation.helper.RawAnimationHelper;
import com.linearpast.sccore.animation.register.AnimationEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class RawAnimationRideEntity extends Entity {
    public RawAnimationRideEntity(Level pLevel) {
        super(AnimationEntities.RIDE.get(), pLevel);
        this.noPhysics = true;
    }

    private final Set<ServerPlayer> players = new HashSet<>();
    private final Map<ResourceLocation, UUID> animationPair = new HashMap<>();
    private RawAnimationData rawAnimation;
    private ServerPlayer player;
    private ResourceLocation layer;
    public RawAnimationRideEntity(ServerPlayer pPlayer, ResourceLocation layer, RawAnimationData rawAnimation) {
        this(pPlayer.level());
        this.player = pPlayer;
        this.layer = layer;
        this.rawAnimation = rawAnimation;
        Ride ride = rawAnimation.getRide();
        if(ride != null) {
            List<ResourceLocation> componentAnimations = ride.getComponentAnimations();
            for (ResourceLocation componentAnimation : componentAnimations) {
                animationPair.put(componentAnimation, null);
            }
        }
    }

    public ResourceLocation getLayer() {
        return layer;
    }

    public Set<ServerPlayer> getPlayers() {
        return players;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public RawAnimationData getAnimation() {
        return rawAnimation;
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag pCompound) {}

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag pCompound) {}

    @Override
    public void tick() {
        super.tick();
        if(!this.level().isClientSide) {
            Ride ride = rawAnimation == null ? null : rawAnimation.getRide();
            if(!this.getPassengers().contains(player) || (ride != null && ride.getExistTick() > 0 && this.tickCount >= ride.getExistTick())) {
                this.remove(RemovalReason.DISCARDED);
            }
        }
    }

    @Override
    public double getPassengersRidingOffset() {
        return 0.0;
    }

    @Override
    protected boolean canRide(@NotNull Entity entity) {
        return true;
    }

    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket(){
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Nullable
    public static RawAnimationRideEntity create(ServerPlayer pPlayer, ResourceLocation layer, RawAnimationData animation) {
        if(animation.getRide() == null) return null;
        RawAnimationDataCapability data = RawAnimationDataCapability.getCapability(pPlayer).orElse(null);
        if(data == null) return null;
        data.setRiderAnimation(layer, animation.getKey());
        RawAnimationRideEntity seat = new RawAnimationRideEntity(pPlayer, layer, animation);
        float xRot = animation.getRide().getXRot();
        float yRot = animation.getRide().getYRot();
        if(xRot == 0 && yRot == 0) seat.setRot(pPlayer.getXRot(), pPlayer.getYRot());
        else seat.setRot(yRot, xRot);
        Vec3 pos = pPlayer.position();
        pos.add(animation.getRide().getOffset());
        seat.setPos(pos.x, pos.y + 0.35f, pos.z);
        pPlayer.level().addFreshEntity(seat);
        pPlayer.startRiding(seat, false);
        return seat;
    }

    @Override
    protected void positionRider(@NotNull Entity pPassenger, @NotNull MoveFunction pCallback) {
        super.positionRider(pPassenger, pCallback);
        pPassenger.setYBodyRot(this.getYRot());
    }

    @Override
    public void onPassengerTurned(@NotNull Entity pEntityToUpdate) {
        pEntityToUpdate.setYBodyRot(this.getYRot());
    }

    @Override
    public @NotNull Vec3 getDismountLocationForPassenger(@NotNull LivingEntity entity) {
        Ride ride = rawAnimation.getRide();
        if(ride != null) {
            Vec3 position = entity.position();
            return position.subtract(ride.getOffset());
        }
        return entity.position();
    }

    @Override
    protected void addPassenger(@NotNull Entity entity) {
        int passengerNum = getPassengers().size();
        super.addPassenger(entity);
        if(passengerNum == 0) return;
        if(entity instanceof ServerPlayer serverPlayer) {
            Ride ride = rawAnimation.getRide();
            if(ride == null) return;
            List<ResourceLocation> componentAnimations = ride.getComponentAnimations();
            if(componentAnimations.isEmpty()) return;
            if(passengerNum > componentAnimations.size()) return;
            ResourceLocation animLocation = null;
            for (ResourceLocation location : animationPair.keySet()) {
                if(animationPair.get(location) == null)
                    animLocation = location;
            }
            if(animLocation == null) return;
            animationPair.put(animLocation, serverPlayer.getUUID());
            RawAnimationDataCapability data = RawAnimationDataCapability.getCapability(serverPlayer).orElse(null);
            if(data == null) return;
            data.setRiderAnimation(layer, animLocation);
            RawAnimationHelper.INSTANCE.syncAnimation(serverPlayer, player);
            players.add(serverPlayer);
        }
    }

    @Override
    protected void removePassenger(@NotNull Entity entity) {
        super.removePassenger(entity);
        if(entity instanceof ServerPlayer serverPlayer) {
            RawAnimationHelper.INSTANCE.removeAnimation(serverPlayer, layer);
            players.remove(serverPlayer);
            new HashMap<>(animationPair).forEach((key, value) -> {
                if(Objects.equals(value, serverPlayer.getUUID())) {
                    animationPair.put(key, null);
                }
            });
            RawAnimationDataCapability.getCapability(serverPlayer).ifPresent(
                    RawAnimationDataCapability::removeRiderAnimation
            );
        }
    }

    @Override
    public boolean canAddPassenger(@NotNull Entity pPassenger) {
        if(!(pPassenger instanceof ServerPlayer)) return false;
        int size = players.size();
        Ride ride = rawAnimation.getRide();
        if(ride == null) return false;
        int maxSize = ride.getComponentAnimations().size();
        boolean flag1 = size < maxSize;
        boolean flag2 = !(pPassenger.getVehicle() instanceof AnimationRideEntity);
        return flag1 && flag2;
    }
}
