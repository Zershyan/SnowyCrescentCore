package com.linearpast.sccore.animation.capability;

import com.linearpast.sccore.SnowyCrescentCore;
import com.linearpast.sccore.animation.capability.inter.IAnimationCapability;
import com.linearpast.sccore.animation.helper.AnimationHelper;
import com.linearpast.sccore.animation.network.toclient.AnimationCapabilityPacket;
import com.linearpast.sccore.animation.register.AnimationRegistry;
import com.linearpast.sccore.capability.CapabilityUtils;
import com.linearpast.sccore.capability.data.ICapabilitySync;
import com.linearpast.sccore.capability.data.player.SimplePlayerCapabilitySync;
import com.linearpast.sccore.capability.network.SimpleCapabilityPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class AnimationDataCapability extends SimplePlayerCapabilitySync implements IAnimationCapability {
    public static final ResourceLocation key = new ResourceLocation(SnowyCrescentCore.MODID, "animation_data");

    public static final String AnimMap = "AnimMap";
    public static final String RideAnimLayer = "RideAnimLayer";
    public static final String RideAnimation = "RideAnimation";

    private final Map<ResourceLocation, ResourceLocation> animMap = new HashMap<>();
    private ResourceLocation rideAnimLayer;
    private ResourceLocation rideAnimation;

    @Override
    public void mergeAnimations(Map<ResourceLocation, ResourceLocation> animations) {
        animations.forEach((key, value) -> {
            if (AnimationRegistry.getLayers().containsKey(key)) {
                if (AnimationHelper.INSTANCE.isAnimationPresent(value)) {
                    if(Objects.equals(rideAnimLayer, key)) {
                        removeRiderAnimation();
                    }
                    this.animMap.put(key, value);
                    setDirty(true);
                }
            }
        });
    }

    @Override
    public boolean mergeAnimation(ResourceLocation layer, ResourceLocation animation) {
        if (AnimationRegistry.getLayers().containsKey(layer)) {
            if (AnimationHelper.INSTANCE.isAnimationPresent(animation)) {
                if(Objects.equals(rideAnimLayer, layer)) {
                    removeRiderAnimation();
                }
                this.animMap.put(layer, animation);
                setDirty(true);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean removeAnimation(ResourceLocation layer) {
        ResourceLocation remove = this.animMap.remove(layer);
        if(remove != null) {
            setDirty(true);
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public ResourceLocation getAnimation(ResourceLocation layer) {
        return animMap.getOrDefault(layer, null);
    }

    @Override
    public Map<ResourceLocation, ResourceLocation> getAnimations() {
        return Map.copyOf(animMap);
    }

    @Override
    public void clearAnimations() {
        this.animMap.clear();
        setDirty(true);
    }

    @Override
    public boolean isAnimationPresent(ResourceLocation layer) {
        return animMap.containsKey(layer);
    }

    @Override
    public ResourceLocation getRiderAnimLayer() {
        return rideAnimLayer;
    }

    @Override
    public ResourceLocation getRiderAnimation() {
        return rideAnimation;
    }

    @Override
    public void setRiderAnimation(@NotNull ResourceLocation layer, @NotNull ResourceLocation animation) {
        if(AnimationHelper.INSTANCE.isAnimationLayerPresent(layer)) {
            if(AnimationHelper.INSTANCE.isAnimationPresent(animation)) {
                this.rideAnimLayer = layer;
                this.rideAnimation = animation;
                if(animMap.get(layer) != null) {
                    animMap.remove(layer);
                }
                setDirty(true);
            }
        }
    }

    @Override
    public void removeRiderAnimation() {
        this.rideAnimLayer = null;
        this.rideAnimation = null;
        setDirty(true);
    }

    @Override
    public void copyFrom(ICapabilitySync<?> oldData) {
        IAnimationCapability data = (IAnimationCapability) oldData;
        this.animMap.clear();
        this.animMap.putAll(data.getAnimations());
        this.rideAnimLayer = data.getRiderAnimLayer();
        this.rideAnimation = data.getRiderAnimation();
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        if(!animMap.isEmpty()) {
            CompoundTag animMapTag = new CompoundTag();
            animMap.forEach((string, animation) ->
                    animMapTag.putString(string.toString(), animation.toString())
            );
            tag.put(AnimMap, animMapTag);
        }
        if(rideAnimLayer != null) tag.putString(RideAnimLayer, rideAnimLayer.toString());
        if(rideAnimation != null) tag.putString(RideAnimation, rideAnimation.toString());
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        this.animMap.clear();
        this.rideAnimLayer = null;
        this.rideAnimation = null;
        if(tag.contains(AnimMap)) {
            CompoundTag animMapTag = tag.getCompound(AnimMap);
            animMapTag.getAllKeys().forEach(key -> this.animMap.put(
                    new ResourceLocation(key),
                    new ResourceLocation(animMapTag.getString(key))
            ));
        }
        if(tag.contains(RideAnimLayer)) this.rideAnimLayer = new ResourceLocation(tag.getString(RideAnimLayer));
        if(tag.contains(RideAnimation)) this.rideAnimation = new ResourceLocation(tag.getString(RideAnimation));
    }

    @Override
    public ResourceLocation getKey() {
        return key;
    }

    @Override
    public SimpleCapabilityPacket<Player> getDefaultPacket() {
        return new AnimationCapabilityPacket(this);
    }

    @Override
    public void attachInit(Player player) {
        Map<ResourceLocation, ResourceLocation> map = new HashMap<>(this.animMap);
        map.forEach((key, value) -> {
            if(!AnimationHelper.INSTANCE.isAnimationLayerPresent(key)) this.animMap.remove(key);
            if(!AnimationHelper.INSTANCE.isAnimationPresent(value)) this.animMap.remove(key);
        });
        if(rideAnimLayer != null && !AnimationHelper.INSTANCE.isAnimationLayerPresent(rideAnimLayer)) {
            removeRiderAnimation();
        }
    }

    public static Optional<IAnimationCapability> getCapability(Player player){
        return Optional.ofNullable(CapabilityUtils.getPlayerCapability(
                player, AnimationDataCapability.key, IAnimationCapability.class
        ));
    }
}
