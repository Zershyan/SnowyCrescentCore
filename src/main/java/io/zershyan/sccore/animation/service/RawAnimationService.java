package io.zershyan.sccore.animation.service;

import io.zershyan.sccore.animation.capability.RawAnimationDataCapability;
import io.zershyan.sccore.animation.data.AnimationData;
import io.zershyan.sccore.animation.data.RawAnimationData;
import io.zershyan.sccore.animation.register.RawAnimationRegistry;
import io.zershyan.sccore.animation.utils.AnimationUtils;
import io.zershyan.sccore.animation.utils.ApiBack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class RawAnimationService implements IAnimationService<RawAnimationData, RawAnimationDataCapability> {
    public static final RawAnimationService INSTANCE = new RawAnimationService();

    @Override
    public @Nullable RawAnimationData getAnimation(ResourceLocation location) {
        if(FMLEnvironment.dist == Dist.CLIENT) {
            return RawAnimationRegistry.getAnimations().getOrDefault(location, null);
        } else {
            return RawAnimationData.create(location);
        }
    }

    @Override
    public @Nullable RawAnimationData getAnimation(CompoundTag tag) {
        return new RawAnimationData(){{deserializeNBT(tag);}};
    }

    @Override
    public @Nullable RawAnimationDataCapability getCapability(Player player) {
        return RawAnimationDataCapability.getCapability(player).orElse(null);
    }

    @Override
    public void clearAnimations(ServerPlayer serverPlayer) {
        ANIMATION_RUNNER.testLoadedAndRun(() -> {
            Optional.ofNullable(getCapability(serverPlayer)).ifPresent(RawAnimationDataCapability::clearAnimations);
            detachAnimation(serverPlayer);
        });
    }

    @Override
    public boolean isAnimationPresent(ResourceLocation location) {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void refreshAnimation(AbstractClientPlayer clientPlayer) {
        ANIMATION_RUNNER.testLoadedAndRun(() -> {
            RawAnimationDataCapability data = getCapability(clientPlayer);
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
            RawAnimationDataCapability data = getCapability(player);
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
            if(player instanceof FakePlayer) return ApiBack.UNSUPPORTED;
            Boolean flag = Optional.ofNullable(getCapability(player)).map(data ->
                    data.mergeAnimation(layer, key)).orElse(false);
            return flag ? ApiBack.SUCCESS : ApiBack.FAIL;
        });
    }
}
