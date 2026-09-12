package io.zershyan.sccore.compat.animation.api.service.impl;

import io.zershyan.sccore.compat.animation.api.service.IRegistriesService;
import io.zershyan.sccore.compat.animation.core.ClientAnimationRegistry;
import io.zershyan.sccore.compat.animation.core.SyncAnimationFactory;
import io.zershyan.sccore.compat.animation.data.ClientAnimation;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class ClientRegistriesService implements IRegistriesService<ClientAnimation> {
    @Override
    public @Nullable ClientAnimation getAnimation(ResourceLocation location) {
        return ClientAnimationRegistry.getAnimations().getOrDefault(location, null);
    }

    @Override
    public @Nullable ClientAnimation getAnimationFromAll(ResourceLocation location) {
        return ClientAnimationRegistry.getAnimation(location);
    }

    @Override
    public @Nullable ClientAnimation getSyncAnimation(ResourceLocation location) {
        return SyncAnimationFactory.getAnimation(location);
    }

    @Override
    public Map<ResourceLocation, ClientAnimation> getAnimations() {
        return ClientAnimationRegistry.getAnimations();
    }

    @Override
    public Map<ResourceLocation, ClientAnimation> getAllAnimations() {
        return ClientAnimationRegistry.getAllAnimations();
    }

    @Override
    public Map<ResourceLocation, ClientAnimation> getSyncAnimations() {
        return SyncAnimationFactory.getAnimations();
    }

    @Override
    public Set<ResourceLocation> getLayers() {
        return ClientAnimationRegistry.getLayers().keySet();
    }

    @Override
    public Set<ResourceLocation> getAllLayers() {
        return ClientAnimationRegistry.getAllLayers().keySet();
    }

    @Override
    public Set<ResourceLocation> getSyncLayers() {
        return SyncAnimationFactory.getLayers().keySet();
    }

    @Override
    public @Nullable Integer getLayerPriority(ResourceLocation location) {
        return ClientAnimationRegistry.getLayers().getOrDefault(location, null);
    }

    @Override
    public @Nullable Integer getLayerPriorityFromAll(ResourceLocation location) {
        return ClientAnimationRegistry.getAllLayers().getOrDefault(location, null);
    }

    @Override
    public @Nullable Integer getSyncLayerPriority(ResourceLocation location) {
        return SyncAnimationFactory.getLayers().getOrDefault(location, null);
    }
}
