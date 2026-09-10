package io.zershyan.sccore.compat.animation.api;

import io.zershyan.sccore.compat.SCCCompatFactory;
import io.zershyan.sccore.compat.animation.api.client.AnimationPlayerHelper;
import io.zershyan.sccore.compat.animation.api.data.AnimationHelper;
import io.zershyan.sccore.compat.animation.api.server.AnimationRideHelper;
import io.zershyan.sccore.compat.animation.core.ClientAnimationRegistry;
import io.zershyan.sccore.compat.animation.core.ServerAnimationRegistry;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * SnowyCrescentCore 动画系统的统一 API 入口。
 *
 * <p>外部模组通过本类的静态方法获取各类动画操作助手，从而与动画系统交互。
 * 本类本身不持有任何状态，仅作为工厂方法集合使用。</p>
 *
 * <h3>核心方法</h3>
 * <ul>
 *   <li>{@link #animation(Player)} — 获取玩家动画数据助手，支持双端</li>
 *   <li>{@link #animPlayer(AbstractClientPlayer)} — 获取客户端动画播放器助手，仅客户端</li>
 *   <li>{@link #ridePlayer(ServerPlayer)} — 获取服务端骑乘动画助手，仅服务端</li>
 *   <li>{@link #isLayerExist(ResourceLocation)} — 查询动画层是否已注册</li>
 *   <li>{@link #isAnimationExist(ResourceLocation)} — 查询动画是否已注册</li>
 * </ul>
 *
 * <h3>前置依赖</h3>
 * <p>本类依赖 {@code Player Animator} 模组。若该模组未安装，类初始化时会抛出
 * {@link RuntimeException}，以防止在不兼容环境下继续调用导致更深层错误。</p>
 *
 * <h3>典型用法</h3>
 * <pre>{@code
 * // 服务端：为玩家播放动画
 * SCCAnimationApi.animation(serverPlayer).playAnimation(layer, animId);
 *
 * // 客户端：控制本地玩家动画播放器
 * SCCAnimationApi.animPlayer(clientPlayer).playAnimation(layer, animId);
 *
 * // 服务端：启动骑乘动画
 * SCCAnimationApi.ridePlayer(serverPlayer).startRide(layer, animLoc);
 * }</pre>
 *
 * @see AnimationHelper
 * @see AnimationPlayerHelper
 * @see AnimationRideHelper
 */
public class SCCAnimationApi {

    static {
        if(!SCCCompatFactory.PlayerAnimator.isModLoaded()) {
            throw new RuntimeException("Use Api with Mod Player Animator Uninstalled");
        }
    }

    /**
     * 获取指定玩家的动画数据助手。
     *
     * <p>返回的 {@link AnimationHelper} 提供动画的播放、移除、骑乘动画控制等操作。
     * 内部根据玩家是否为 {@link ServerPlayer} 自动选择服务端实现
     * （{@link io.zershyan.sccore.compat.animation.api.data.AnimationService}）或客户端实现
     * （{@link io.zershyan.sccore.compat.animation.api.data.ClientAnimationService}），
     * 因此同一套 API 可同时工作在双端。</p>
     *
     * @param player 目标玩家，服务端与客户端均可
     * @return 绑定到该玩家的动画数据助手
     * @see AnimationHelper
     */
    public static AnimationHelper animation(Player player) {
        return AnimationHelper.of(player);
    }

    /**
     * 获取客户端玩家的动画播放器助手。
     *
     * <p>返回的 {@link AnimationPlayerHelper} 用于在客户端直接驱动 Player Animator 的
     * {@code KeyframeAnimationPlayer}，包括播放、移除、淡入淡出与动画同步等操作。</p>
     *
     * <p><b>仅客户端有效</b>，调用方应确保处于客户端环境。</p>
     *
     * @param player 目标客户端玩家
     * @return 绑定到该玩家的动画播放器助手
     * @see AnimationPlayerHelper
     */
    @OnlyIn(Dist.CLIENT)
    public static AnimationPlayerHelper animPlayer(AbstractClientPlayer player) {
        return AnimationPlayerHelper.of(player);
    }

    /**
     * 获取服务端玩家的骑乘动画助手。
     *
     * <p>返回的 {@link AnimationRideHelper} 用于在服务端创建与管理动画骑乘实体
     * （{@code AnimationRideEntity}），实现多玩家协作骑乘动画。</p>
     *
     * @param player 目标服务端玩家
     * @return 绑定到该玩家的骑乘动画助手
     * @see AnimationRideHelper
     */
    public static AnimationRideHelper ridePlayer(ServerPlayer player) {
        return AnimationRideHelper.of(player);
    }

    /**
     * 判断指定动画层是否已注册。
     *
     * <p>同时检查服务端注册表（{@link ServerAnimationRegistry}）与客户端注册表
     * （{@link ClientAnimationRegistry}，包含同步动画），任一侧存在即返回 {@code true}。</p>
     *
     * @param layer 动画层的资源位置
     * @return 任一侧存在该层则返回 {@code true}，否则返回 {@code false}
     */
    public static boolean isLayerExist(ResourceLocation layer) {
        return ServerAnimationRegistry.getLayers().containsKey(layer)
                || ClientAnimationRegistry.getAllLayers().containsKey(layer);
    }

    /**
     * 判断指定动画是否已注册。
     *
     * <p>同时检查服务端注册表（{@link ServerAnimationRegistry}）与客户端注册表
     * （{@link ClientAnimationRegistry}，包含同步动画），任一侧存在即返回 {@code true}。</p>
     *
     * @param animationId 动画的资源位置
     * @return 任一侧存在该动画则返回 {@code true}，否则返回 {@code false}
     */
    public static boolean isAnimationExist(ResourceLocation animationId) {
        return ServerAnimationRegistry.getAnimations().containsKey(animationId)
                || ClientAnimationRegistry.getAllAnimations().containsKey(animationId);
    }
}