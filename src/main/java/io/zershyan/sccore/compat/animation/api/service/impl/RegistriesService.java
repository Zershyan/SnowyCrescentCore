package io.zershyan.sccore.compat.animation.api.service.impl;

import io.zershyan.sccore.compat.animation.api.service.IRegistriesService;
import io.zershyan.sccore.compat.animation.core.ServerAnimationRegistry;
import io.zershyan.sccore.compat.animation.data.ServerAnimation;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class RegistriesService implements IRegistriesService<ServerAnimation> {
    @Override
    public @Nullable ServerAnimation getAnimation(ResourceLocation location) {
        return ServerAnimationRegistry.getAnimations().getOrDefault(location, null);
    }

    @Override
    public Map<ResourceLocation, ServerAnimation> getAnimations() {
        return ServerAnimationRegistry.getAnimations();
    }

    @Override
    public Set<ResourceLocation> getLayers() {
        return ServerAnimationRegistry.getLayers().keySet();
    }

    @Override
    public @Nullable Integer getLayerPriority(ResourceLocation location) {
        return ServerAnimationRegistry.getLayers().getOrDefault(location, null);
    }
}
