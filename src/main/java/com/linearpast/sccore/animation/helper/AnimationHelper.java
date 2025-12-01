package com.linearpast.sccore.animation.helper;

import com.linearpast.sccore.animation.AnimationApi;
import com.linearpast.sccore.animation.data.AnimationData;
import com.linearpast.sccore.animation.service.IAnimationService;
import com.linearpast.sccore.animation.utils.ApiBack;
import com.linearpast.sccore.core.IModLazyRun;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class AnimationHelper {
    private final Player player;
    private final AnimationLazyHelper lazyRun;
    AnimationHelper(Player player) {
        this.player = player;
        this.lazyRun = new AnimationLazyHelper(player);
    }

    public static AnimationHelper getHelper(Player player) {
        return new AnimationHelper(player);
    }

    public ApiBack playAnimation(ResourceLocation layer, ResourceLocation location) {
        IAnimationService<?, ?> service = AnimationApi.getServiceGetterHelper(location).getService();
        if(service == null) return ApiBack.FAIL;
        AnimationData data = service.getAnimation(location);
        if(data == null) return ApiBack.FAIL;
        return lazyRun.testLoadedAndCall(
                () -> service.playAnimation(lazyRun.getServerPlayer(), layer, data),
                () -> service.playAnimation(lazyRun.getClientPlayer(), layer, location)
        );
    }

    public ApiBack playAnimationWithRide(ResourceLocation layer, AnimationData animation, boolean isForce) {
        IAnimationService<?, ?> service = AnimationApi.getServiceGetterHelper(animation.getKey()).getService();
        if(service == null) return ApiBack.FAIL;
        return lazyRun.testLoadedAndCall(
                () -> service.playAnimationWithRide(lazyRun.getServerPlayer(), layer, animation, isForce),
                () -> service.playAnimationWithRide(lazyRun.getClientPlayer(), layer, animation.getKey(), isForce)
        );
    }

    public void clearAnimation() {
        for (IAnimationService<?, ?> service : AnimationApi.getServiceGetterHelper().getAllServices()) {
            lazyRun.testLoadedAndRun(
                    () -> service.clearAnimations(lazyRun.getServerPlayer()),
                    () -> service.clearAnimations(lazyRun.getClientPlayer())
            );
        }
    }

    public ApiBack detachAnimation() {
        ApiBack result = ApiBack.FAIL;
        for (IAnimationService<?, ?> service : AnimationApi.getServiceGetterHelper().getAllServices()) {
            ApiBack back = lazyRun.testLoadedAndCall(() -> service.detachAnimation(lazyRun.getServerPlayer()));
            if(back == ApiBack.SUCCESS) return back;
            else result = back;
        }
        return result;
    }

    public ApiBack joinAnimation(Player target, boolean force) {
        ApiBack result = ApiBack.FAIL;
        for (IAnimationService<?, ?> service : AnimationApi.getServiceGetterHelper().getAllServices()) {
            ApiBack apiBack = lazyRun.testLoadedAndCall(() -> {
                if(!(target instanceof ServerPlayer targetPlayer)) return ApiBack.UNSUPPORTED;
                return service.joinAnimation(lazyRun.getServerPlayer(), targetPlayer, force);
            });
            if(apiBack == ApiBack.SUCCESS) return apiBack;
            else result = apiBack;
        }
        return result;
    }

    public ApiBack syncToAnimation(Player target) {
        ApiBack result = ApiBack.FAIL;
        for (IAnimationService<?, ?> service : AnimationApi.getServiceGetterHelper().getAllServices()) {
            ApiBack apiBack = lazyRun.testLoadedAndCall(() -> {
                if(!(target instanceof ServerPlayer targetPlayer)) return ApiBack.UNSUPPORTED;
                service.syncAnimation(lazyRun.getServerPlayer(), targetPlayer);
                return ApiBack.SUCCESS;
            }, () -> {
                if(!(target instanceof AbstractClientPlayer targetPlayer)) return ApiBack.UNSUPPORTED;
                service.syncAnimation(lazyRun.getClientPlayer(), targetPlayer);
                return ApiBack.SUCCESS;
            });
            if(apiBack == ApiBack.SUCCESS) return apiBack;
            else result = apiBack;
        }
        return result;
    }

    @OnlyIn(Dist.CLIENT)
    public void refreshAnimation() {
        for (IAnimationService<?, ?> service : AnimationApi.getServiceGetterHelper().getAllServices()) {
            service.refreshAnimation(lazyRun.getClientPlayer());
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void refreshAnimationUnsafe() {
        for (IAnimationService<?, ?> service : AnimationApi.getServiceGetterHelper().getAllServices()) {
            service.refreshAnimationUnsafe(lazyRun.getClientPlayer());
        }
    }

    @Nullable
    public ResourceLocation getAnimationPlaying(@Nullable ResourceLocation layer) {
        for (IAnimationService<?, ?> service : AnimationApi.getServiceGetterHelper().getAllServices()) {
            ResourceLocation playing = service.getAnimationPlaying(player, layer);
            if(playing != null) return playing;
        }
        return null;
    }

    public ApiBack removeAnimation(ResourceLocation layer) {
        ApiBack result = ApiBack.FAIL;
        for (IAnimationService<?, ?> service : AnimationApi.getServiceGetterHelper().getAllServices()) {
            ApiBack apiBack = lazyRun.testLoadedAndCall(
                    () -> service.removeAnimation(lazyRun.getServerPlayer(), layer),
                    () -> service.removeAnimation(lazyRun.getClientPlayer(), layer)
            );
            if(apiBack == ApiBack.SUCCESS) return apiBack;
            else result = apiBack;
        }
        return result;
    }

    public ApiBack inviteAnimation(ResourceLocation layer, ResourceLocation location, Collection<UUID> targets) {
        IAnimationService<?, ?> service = AnimationApi.getServiceGetterHelper(location).getService();
        if(service == null) return ApiBack.FAIL;
        return lazyRun.testLoadedAndCall(
                () -> {
                    AnimationData data = service.getAnimation(location);
                    if(data == null) return ApiBack.FAIL;
                    return service.invite(lazyRun.getServerPlayer(), layer, data, targets);
                },
                () -> {
                    ClientLevel level = Minecraft.getInstance().level;
                    if(level == null) return ApiBack.FAIL;
                    List<AbstractClientPlayer> list = targets.stream().map(level::getPlayerByUUID)
                            .filter(Objects::nonNull).map(AbstractClientPlayer.class::cast).toList();
                    if(list.isEmpty()) return ApiBack.FAIL;
                    return service.invite(layer, location, list.toArray(new AbstractClientPlayer[]{}));
                }
        );
    }

    public ApiBack applyAnimation(Player target) {
        ApiBack result = ApiBack.FAIL;
        for (IAnimationService<?, ?> service : AnimationApi.getServiceGetterHelper().getAllServices()) {
            ApiBack apiBack = lazyRun.testLoadedAndCall(() -> {
                if(!(target instanceof ServerPlayer targetPlayer)) return ApiBack.UNSUPPORTED;
                return service.apply(lazyRun.getServerPlayer(), targetPlayer);
            }, () -> {
                if(!(target instanceof AbstractClientPlayer targetPlayer)) return ApiBack.UNSUPPORTED;
                return service.apply(targetPlayer);
            });
            if(apiBack == ApiBack.SUCCESS) return apiBack;
            else result = apiBack;
        }
        return result;
    }

    public ApiBack requestAnimation(Player target, ResourceLocation layer, ResourceLocation location, boolean isRide) {
        IAnimationService<?, ?> service = AnimationApi.getServiceGetterHelper(location).getService();
        if(service == null) return ApiBack.FAIL;
        return lazyRun.testLoadedAndCall(
                () -> {
                    AnimationData data = service.getAnimation(location);
                    if(data == null) return ApiBack.FAIL;
                    if(!(target instanceof ServerPlayer targetPlayer)) return ApiBack.UNSUPPORTED;
                    return service.request(lazyRun.getServerPlayer(), targetPlayer, layer, data, isRide);
                },
                () -> {
                    if(!(target instanceof AbstractClientPlayer targetPlayer)) return ApiBack.UNSUPPORTED;
                    return service.request(targetPlayer, layer, location);
                }
        );
    }

    public ApiBack acceptInvite(Player inviter) {
        ApiBack result = ApiBack.FAIL;
        for (IAnimationService<?, ?> service : AnimationApi.getServiceGetterHelper().getAllServices()) {
            ApiBack apiBack = lazyRun.testLoadedAndCall(() -> {
                if(!(inviter instanceof ServerPlayer inviterPlayer)) return ApiBack.UNSUPPORTED;
                return service.acceptInvite(lazyRun.getServerPlayer(), inviterPlayer);
            });
            if(apiBack == ApiBack.SUCCESS) return apiBack;
            else result = apiBack;
        }
        return result;
    }

    public ApiBack acceptApply(Player applier) {
        ApiBack result = ApiBack.FAIL;
        for (IAnimationService<?, ?> service : AnimationApi.getServiceGetterHelper().getAllServices()) {
            ApiBack apiBack = lazyRun.testLoadedAndCall(() -> {
                if(!(applier instanceof ServerPlayer applierPlayer)) return ApiBack.UNSUPPORTED;
                return service.acceptApply(lazyRun.getServerPlayer(), applierPlayer);
            });
            if(apiBack == ApiBack.SUCCESS) return apiBack;
            else result = apiBack;
        }
        return result;
    }

    public ApiBack acceptRequest(Player requestor) {
        ApiBack result = ApiBack.FAIL;
        for (IAnimationService<?, ?> service : AnimationApi.getServiceGetterHelper().getAllServices()) {
            ApiBack apiBack = lazyRun.testLoadedAndCall(() -> {
                if(!(requestor instanceof ServerPlayer requestorPlayer)) return ApiBack.UNSUPPORTED;
                return service.acceptRequest(lazyRun.getServerPlayer(), requestorPlayer);
            });
            if(apiBack == ApiBack.SUCCESS) return apiBack;
            else result = apiBack;
        }
        return result;
    }

    static class AnimationLazyHelper implements IModLazyRun {
        private final Player player;
        AnimationLazyHelper(Player player) {
            this.player = player;
        }

        @Override
        public boolean testCondition() {
            return player instanceof ServerPlayer;
        }

        public ServerPlayer getServerPlayer() {
            return (ServerPlayer) player;
        }

        @OnlyIn(Dist.CLIENT)
        public AbstractClientPlayer getClientPlayer() {
            return (AbstractClientPlayer) player;
        }
    }
}
