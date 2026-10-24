package io.zershyan.sccore.compat.animation.api.service.impl;

import io.zershyan.sccore.compat.animation.api.service.IRegistriesService;
import io.zershyan.sccore.compat.animation.core.ClientAnimationRegistry;
import io.zershyan.sccore.compat.animation.core.SyncAnimationFactory;
import io.zershyan.sccore.compat.animation.data.Animation;
import io.zershyan.sccore.compat.animation.data.ClientAnimation;
import io.zershyan.sccore.compat.animation.data.ServerAnimation;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 客户端注册表服务。
 *
 * <p>本地注册的客户端动画与随登录下发的服务端动画分开保存：{@code getAnimation} 只看本地，
 * {@code getAnimationFromAll} 与 {@code getAllAnimations} 返回两者按优先级合并后的结果。</p>
 */
public class ClientRegistriesService implements IRegistriesService<Animation> {
    @Override
    public @Nullable ClientAnimation getAnimation(ResourceLocation location) {
        return ClientAnimationRegistry.getAnimations().getOrDefault(location, null);
    }

    @Override
    public @Nullable Animation getAnimationFromAll(ResourceLocation location) {
        return ClientAnimationRegistry.getAnimation(location);
    }

    @Override
    public @Nullable ServerAnimation getSyncAnimation(ResourceLocation location) {
        return SyncAnimationFactory.getAnimation(location);
    }

    @Override
    public Map<ResourceLocation, Animation> getAnimations() {
        return Map.copyOf(new HashMap<ResourceLocation, Animation>(ClientAnimationRegistry.getAnimations()));
    }

    @Override
    public Map<ResourceLocation, Animation> getAllAnimations() {
        return ClientAnimationRegistry.getAllAnimations();
    }

    @Override
    public Map<ResourceLocation, Animation> getSyncAnimations() {
        return Map.copyOf(new HashMap<ResourceLocation, Animation>(SyncAnimationFactory.getAnimations()));
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
