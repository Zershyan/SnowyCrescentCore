package io.zershyan.sccore.compat.animation.api.server;

import io.zershyan.sccore.compat.animation.api.SCCAnimationApi;
import io.zershyan.sccore.compat.animation.core.ServerAnimationRegistry;
import io.zershyan.sccore.compat.animation.data.ClientRideAnimDTO;
import io.zershyan.sccore.compat.animation.data.ServerAnimation;
import io.zershyan.sccore.compat.animation.network.data.SyncAnimationData;
import io.zershyan.sccore.compat.animation.registry.attachment.PlayerAnimations;
import io.zershyan.sccore.compat.animation.registry.entity.AnimationRideEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Objects;

/**
 * 服务端骑乘动画助手，用于创建与管理玩家协作骑乘动画。
 *
 * <p>通过生成不可见的 {@link AnimationRideEntity} 作为骑乘载体，将一名"车主"玩家与若干"组件"玩家
 * 绑定到同一段骑乘动画上，实现多人骑乘动画的播放与同步。</p>
 *
 * <h3>典型用法</h3>
 * <pre>{@code
 * AnimationRideHelper helper = SCCAnimationApi.ridePlayer(serverPlayer);
 *
 * // 启动骑乘
 * helper.startRide(layer, animLoc);
 * helper.startRide(layer, animLoc, pos);       // 指定位置
 * helper.startRide(layer, animLoc, pos, true); // 强制覆盖
 *
 * // 同步骑乘动画到目标玩家
 * helper.syncRideAnim(targetPlayer);
 *
 * // 停止骑乘
 * helper.stopRide();
 * }</pre>
 *
 * @see AnimationRideEntity
 * @see SCCAnimationApi#ridePlayer(ServerPlayer)
 */
public class AnimationRideHelper {
    private final ServerPlayer player;

    /**
     * 私有构造，通过 {@link #of(ServerPlayer)} 创建实例。
     *
     * @param player 车主玩家
     */
    private AnimationRideHelper(ServerPlayer player) {
        this.player = player;
    }

    /**
     * 为指定服务端玩家创建骑乘动画助手。
     *
     * @param player 车主玩家
     * @return 绑定该玩家的骑乘动画助手
     */
    public static AnimationRideHelper of(ServerPlayer player) {
        return new AnimationRideHelper(player);
    }

    /**
     * 以客户端骑乘动画 DTO 启动骑乘，在玩家当前位置、非强制。
     *
     * @param layer     动画层
     * @param animation 客户端骑乘动画数据
     */
    public void startRide(ResourceLocation layer, ClientRideAnimDTO animation) {
        startRide(layer, new AnimationRideEntity.RideAnimation(animation.id(), animation.animation()), player.position(), false);
    }

    /**
     * 以资源位置启动骑乘，在玩家当前位置、非强制。
     *
     * @param layer   动画层
     * @param animLoc 动画资源位置
     */
    public void startRide(ResourceLocation layer, ResourceLocation animLoc) {
        startRide(layer, animLoc, player.position(), false);
    }

    /**
     * 以资源位置启动骑乘，在指定位置、非强制。
     *
     * @param layer   动画层
     * @param animLoc 动画资源位置
     * @param pos     骑乘位置
     */
    public void startRide(ResourceLocation layer, ResourceLocation animLoc, Vec3 pos) {
        startRide(layer, animLoc, pos, false);
    }

    /**
     * 以资源位置启动骑乘，在指定位置、可强制。
     *
     * @param layer   动画层
     * @param animLoc 动画资源位置
     * @param pos     骑乘位置
     * @param force   是否强制覆盖已有骑乘
     */
    public void startRide(ResourceLocation layer, ResourceLocation animLoc, Vec3 pos, boolean force) {
        ServerAnimation animation = ServerAnimationRegistry.getAnimations().get(animLoc);
        startRide(layer, new AnimationRideEntity.RideAnimation(animLoc, animation), pos, force);
    }

    private void startRide(ResourceLocation layer, AnimationRideEntity.RideAnimation animation, Vec3 pos, boolean force) {
        PlayerAnimations oldData = PlayerAnimations.getData(player);
        ResourceLocation oldLayer = oldData.rideAnim().layer().orElse(null);
        Entity vehicle = player.getVehicle();
        if (Objects.equals(layer, oldLayer) && vehicle instanceof AnimationRideEntity) {
            return;
        }
        if (layer == null) {
            stopRide();
        } else if (animation != null) {
            AnimationRideEntity.create(player, layer, animation, force, pos);
        }
    }

    /**
     * 同步骑乘动画数据给指定目标玩家。
     *
     * <p>向所有客户端广播同步包，使目标玩家的骑乘动画 tick 与本玩家对齐。</p>
     *
     * @param target 目标玩家
     */
    public void syncRideAnim(ServerPlayer target) {
        PacketDistributor.sendToAllPlayers(new SyncAnimationData(player.getUUID(), target.getUUID()));
    }

    /**
     * 停止当前骑乘动画。
     *
     * <p>若玩家当前骑乘的是 {@link AnimationRideEntity}，则令其停止骑乘。</p>
     */
    public void stopRide() {
        if (player.getVehicle() instanceof AnimationRideEntity) {
            player.stopRiding();
        }
    }
}
