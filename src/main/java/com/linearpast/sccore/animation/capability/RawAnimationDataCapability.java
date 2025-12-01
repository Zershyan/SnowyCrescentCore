package com.linearpast.sccore.animation.capability;

import com.linearpast.sccore.SnowyCrescentCore;
import com.linearpast.sccore.animation.network.toclient.RawAnimationCapabilityPacket;
import com.linearpast.sccore.animation.register.AnimationRegistry;
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

public class RawAnimationDataCapability extends SimplePlayerCapabilitySync {
    public static final ResourceLocation key = new ResourceLocation(SnowyCrescentCore.MODID, "raw_animation_data");

    public static final String AnimMap = "AnimMap";

    private final Map<ResourceLocation, ResourceLocation> animMap = new HashMap<>();

    public boolean mergeAnimation(ResourceLocation layer, ResourceLocation animation) {
        if (AnimationRegistry.getLayers().containsKey(layer)) {
            this.animMap.put(layer, animation);
            setDirty(true);
            return true;
        }
        return false;
    }

    public boolean removeAnimation(ResourceLocation layer) {
        ResourceLocation remove = this.animMap.remove(layer);
        if(remove != null) {
            setDirty(true);
            return true;
        }
        return false;
    }

    @Nullable
    public ResourceLocation getAnimation(ResourceLocation layer) {
        return animMap.getOrDefault(layer, null);
    }

    public Map<ResourceLocation, ResourceLocation> getAnimations() {
        return Map.copyOf(animMap);
    }

    public void clearAnimations() {
        this.animMap.clear();
        setDirty(true);
    }

    public boolean isAnimationPresent(ResourceLocation layer) {
        return animMap.containsKey(layer);
    }

    @Override
    public void copyFrom(ICapabilitySync<?> oldData) {
        RawAnimationDataCapability data = (RawAnimationDataCapability) oldData;
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
    }

    @Override
    public ResourceLocation getKey() {
        return key;
    }

    @Override
    public SimpleCapabilityPacket<Player> getDefaultPacket() {
        return new RawAnimationCapabilityPacket(this);
    }

    @Override
    public void attachInit(Player entity) {
        clearAnimations();
    }

    public static Optional<RawAnimationDataCapability> getCapability(Player player){
        return Optional.ofNullable(CapabilityUtils.getPlayerCapability(
                player, RawAnimationDataCapability.key, RawAnimationDataCapability.class
        ));
    }
}
