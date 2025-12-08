package com.linearpast.sccore.animation.service;

import com.linearpast.sccore.animation.capability.AnimationDataCapability;
import com.linearpast.sccore.animation.capability.inter.IAnimationCapability;
import com.linearpast.sccore.animation.data.AnimationData;
import com.linearpast.sccore.animation.data.GenericAnimationData;
import com.linearpast.sccore.animation.register.AnimationRegistry;
import com.linearpast.sccore.animation.utils.AnimationUtils;
import com.linearpast.sccore.animation.utils.ApiBack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Animation Util. May be you can call it Api.
 */
public class AnimationService implements IAnimationService<GenericAnimationData, IAnimationCapability> {
    public static final AnimationService INSTANCE = new AnimationService();

    /**
     * Get the HeightModifier when there are animations which playing on player. <br>
     * And It will return the first which be found.
     * @param player Target player
     * @return The first HeightModifier it find.
     */
    public float getHeightModifier(Player player) {
        Float result = ANIMATION_RUNNER.testLoadedAndCall(() -> {
            IAnimationCapability data = AnimationDataCapability.getCapability(player).orElse(null);
            if (data == null) return 1.0f;
            float heightModifier = 1.0f;
            for (ResourceLocation value : data.getAnimations().values()) {
                GenericAnimationData animation = getAnimation(value);
                if (animation == null) continue;
                float animationHeightModifier = animation.getHeightModifier();
                heightModifier = Math.min(heightModifier, animationHeightModifier);
            }
            return heightModifier;
        });
        return result == null ? 1.0f : result;
    }


    @Override
    public @Nullable GenericAnimationData getAnimation(ResourceLocation location) {
        return AnimationRegistry.getAnimations().getOrDefault(location, null);
    }

    @Override
    public @Nullable GenericAnimationData getAnimation(CompoundTag tag) {
        return new GenericAnimationData(){{deserializeNBT(tag);}};
    }

    @Override
    public @Nullable IAnimationCapability getCapability(Player player) {
        return AnimationDataCapability.getCapability(player).orElse(null);
    }

    @Override
    public void clearAnimations(ServerPlayer serverPlayer) {
        ANIMATION_RUNNER.testLoadedAndRun(() -> {
            Optional.ofNullable(getCapability(serverPlayer)).ifPresent(IAnimationCapability::clearAnimations);
            detachAnimation(serverPlayer);
        });
    }

    @Override
    public boolean isAnimationPresent(ResourceLocation location) {
        return ANIMATION_RUNNER.testLoadedAndCall(() -> AnimationRegistry.getAnimations().containsKey(location));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void refreshAnimation(AbstractClientPlayer clientPlayer) {
        ANIMATION_RUNNER.testLoadedAndRun(() -> {
            IAnimationCapability data = getCapability(clientPlayer);
            if(data == null) return;
            Set<ResourceLocation> oldLayers = new HashSet<>(data.getAnimations().keySet());
            for (ResourceLocation layer : Set.copyOf(oldLayers)) {
                if (AnimationUtils.isClientAnimationStop(clientPlayer, layer)) {
                    removeAnimation(clientPlayer, layer);
                }
            }
        });
    }

    @Override
    public @Nullable ResourceLocation getAnimationPlaying(Player player, @Nullable ResourceLocation layer) {
        return ANIMATION_RUNNER.testLoadedAndCall(() -> {
            IAnimationCapability data = getCapability(player);
            if(data == null) return null;
            if(layer == null){
                for (ResourceLocation value : data.getAnimations().values()) {
                    if(value != null) return value;
                }
            } else if (isAnimationLayerPresent(layer)) {
                if(data.isAnimationPresent(layer)){
                    return data.getAnimation(layer);
                }
            }
            return null;
        });
    }

    @Override
    public ApiBack removeAnimation(@NotNull ServerPlayer serverPlayer, ResourceLocation layer) {
        boolean result = ANIMATION_RUNNER.testLoadedAndCall(() -> Optional.ofNullable(getCapability(serverPlayer))
                .map(data -> data.removeAnimation(layer)).orElse(false));
        return result ? ApiBack.SUCCESS : ApiBack.FAIL;
    }

    @Override
    public ApiBack playAnimationServer(@NotNull ServerPlayer player, ResourceLocation layer, AnimationData animation) {
        return ANIMATION_RUNNER.testLoadedAndCall(() -> {
            ResourceLocation key = animation.getKey();
            if(!isAnimationLayerPresent(layer) || !isAnimationPresent(key))
                return ApiBack.RESOURCE_NOT_FOUND;
            if(player instanceof FakePlayer)
                return ApiBack.UNSUPPORTED;
            Boolean flag = Optional.ofNullable(getCapability(player)).map(data ->
                    data.mergeAnimation(layer, key)).orElse(false);
            return flag ? ApiBack.SUCCESS : ApiBack.FAIL;
        });
    }

    public ApiBack playAnimation(@NotNull ServerPlayer player, ResourceLocation layer, ResourceLocation animation) {
        return playAnimation(player, layer, getAnimation(animation));
    }
    public ApiBack playAnimationWithRide(@NotNull ServerPlayer player, ResourceLocation layer, ResourceLocation animation, boolean force) {
        return playAnimationWithRide(player, layer, getAnimation(animation), force);
    }
}
