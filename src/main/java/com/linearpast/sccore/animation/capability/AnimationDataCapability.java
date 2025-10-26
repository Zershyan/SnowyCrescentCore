package com.linearpast.sccore.animation.capability;

import com.linearpast.sccore.SnowyCrescentCore;
import com.linearpast.sccore.animation.AnimationUtils;
import com.linearpast.sccore.animation.capability.inter.IAnimationCapability;
import com.linearpast.sccore.animation.event.AnimationLayerRegistry;
import com.linearpast.sccore.animation.network.toclient.AnimationCapabilityPacket;
import com.linearpast.sccore.capability.CapabilityUtils;
import com.linearpast.sccore.capability.data.ICapabilitySync;
import com.linearpast.sccore.capability.data.player.SimplePlayerCapabilitySync;
import com.linearpast.sccore.capability.network.SimpleCapabilityPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AnimationDataCapability extends SimplePlayerCapabilitySync implements IAnimationCapability {
    public static final ResourceLocation key = new ResourceLocation(SnowyCrescentCore.MODID, "animation_data");

    public static final String AnimMap = "AnimMap";
    public static final String RideAnimLayer = "RideAnimLayer";

    private final Map<ResourceLocation, ResourceLocation> animMap = new HashMap<>();
    private ResourceLocation rideAnimLayer;

    @Override
    public void mergeAnimations(Map<ResourceLocation, ResourceLocation> animations) {
        animations.forEach((key, value) -> {
            if (AnimationLayerRegistry.getAnimLayers().containsKey(key)) {
                if (AnimationUtils.isAnimationPresent(value)) {
                    this.animMap.put(key, value);
                    setDirty(true);
                }
            }
        });
    }

    @Override
    public boolean mergeAnimation(ResourceLocation layer, ResourceLocation animation) {
        if (AnimationLayerRegistry.getAnimLayers().containsKey(layer)) {
            if (AnimationUtils.isAnimationPresent(animation)) {
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
        return animMap.get(layer);
    }

    @Override
    public Map<ResourceLocation, ResourceLocation> getAnimations() {
        return animMap;
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
    public void setRideAnimLayer(ResourceLocation rideAnimLayer) {
        this.rideAnimLayer = rideAnimLayer;
        setDirty(true);
    }

    @Override
    public ResourceLocation getRideAnimLayer() {
        return rideAnimLayer;
    }

    @Override
    public void copyFrom(ICapabilitySync<?> oldData) {
        IAnimationCapability data = (IAnimationCapability) oldData;
        this.animMap.clear();
        this.animMap.putAll(data.getAnimations());
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
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        this.animMap.clear();
        if(tag.contains(AnimMap)) {
            CompoundTag animMapTag = tag.getCompound(AnimMap);
            animMapTag.getAllKeys().forEach(key -> this.animMap.put(
                    new ResourceLocation(key),
                    new ResourceLocation(animMapTag.getString(key))
            ));
        }
        if(tag.contains(RideAnimLayer)) this.rideAnimLayer = new ResourceLocation(tag.getString(RideAnimLayer));
    }

    @Override
    public SimpleCapabilityPacket<Player> getDefaultPacket() {
        return new AnimationCapabilityPacket(serializeNBT());
    }

    @Override
    public void attachInit(Player player) {
        Map<ResourceLocation, ResourceLocation> map = new HashMap<>(this.animMap);
        map.forEach((key, value) -> {
            if(!AnimationUtils.isAnimationLayerPresent(key)) this.animMap.remove(key);
            if(!AnimationUtils.isAnimationPresent(value)) this.animMap.remove(key);
            if(key.equals(rideAnimLayer)) this.animMap.remove(key);
        });
        this.rideAnimLayer = null;
    }

    public static Optional<IAnimationCapability> getCapability(Player player){
        return Optional.ofNullable(CapabilityUtils.getPlayerCapability(
                player, AnimationDataCapability.key, IAnimationCapability.class
        ));
    }
}
