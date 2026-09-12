package io.zershyan.sccore.compat.animation.api.service;

import io.zershyan.sccore.compat.animation.data.Animation;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public interface IRegistriesService<T extends Animation> {
    @Nullable
    T getAnimation(ResourceLocation location);

    default @Nullable T getAnimationFromAll(ResourceLocation location) {
        return getAnimation(location);
    }

    default @Nullable T getSyncAnimation(ResourceLocation location) {
        return getAnimation(location);
    }

    Map<ResourceLocation, T> getAnimations();

    default Map<ResourceLocation, T> getAllAnimations() {
        return getAnimations();
    }

    default Map<ResourceLocation, T> getSyncAnimations() {
        return getAnimations();
    }

    Set<ResourceLocation> getLayers();

    default Set<ResourceLocation> getAllLayers() {
        return getLayers();
    }

    default Set<ResourceLocation> getSyncLayers() {
        return getLayers();
    }

    @Nullable
    Integer getLayerPriority(ResourceLocation location);

    default @Nullable Integer getLayerPriorityFromAll(ResourceLocation location) {
        return getLayerPriority(location);
    }

    default @Nullable Integer getSyncLayerPriority(ResourceLocation location) {
        return getLayerPriority(location);
    }
}
