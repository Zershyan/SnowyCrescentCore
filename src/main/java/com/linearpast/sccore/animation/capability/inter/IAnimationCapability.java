package com.linearpast.sccore.animation.capability.inter;

import com.linearpast.sccore.capability.data.ICapabilitySync;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface IAnimationCapability extends ICapabilitySync<Player> {
    void mergeAnimations(Map<ResourceLocation, ResourceLocation> animations);
    boolean mergeAnimation(ResourceLocation layer, ResourceLocation animation);
    boolean removeAnimation(ResourceLocation layer);
    @Nullable ResourceLocation getAnimation(ResourceLocation layer);
    Map<ResourceLocation, ResourceLocation> getAnimations();
    void clearAnimations();
    boolean isAnimationPresent(ResourceLocation layer);

    ResourceLocation getRiderAnimLayer();
    ResourceLocation getRiderAnimation();
    void setRiderAnimation(ResourceLocation layer, ResourceLocation animation);
    void removeRiderAnimation();
}
