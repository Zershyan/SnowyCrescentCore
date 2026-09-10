package io.zershyan.sccore.compat.animation.api.data;

import io.zershyan.sccore.SCCore;
import io.zershyan.sccore.compat.animation.api.SCCAnimationApi;
import io.zershyan.sccore.compat.animation.core.ClientAnimationRegistry;
import io.zershyan.sccore.compat.animation.core.ServerAnimationRegistry;
import io.zershyan.sccore.compat.animation.core.SyncAnimationFactory;
import io.zershyan.sccore.compat.animation.data.Animation;
import io.zershyan.sccore.compat.animation.data.ClientAnimation;
import io.zershyan.sccore.compat.animation.registry.attachment.PlayerAnimations;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 玩家动画数据操作助手，是动画 API 操作玩家动画数据的主要入口。
 *
 * <p>本类对 {@link IAnimationService} 做了一层封装，提供动画的播放、移除、骑乘动画控制
 * 以及批量数据修改能力。根据玩家类型自动绑定到服务端
 * （{@link AnimationService}）或客户端（{@link ClientAnimationService}）的实现，
 * 因此同一套 API 可同时工作在双端。</p>
 *
 * <h3>批量修改</h3>
 * <p>批量修改动画数据时应使用 {@link #operaData(Function)} 配合 {@link Opera} 进行链式操作，
 * 修改完成后由 {@link Opera#endOpera()} 触发一次性的数据写入与同步，避免中间态被同步到远端。</p>
 *
 * <h3>典型用法</h3>
 * <pre>{@code
 * AnimationHelper helper = SCCAnimationApi.animation(player);
 *
 * // 播放动画
 * helper.playAnimation(layer, animId);
 *
 * // 移除动画
 * helper.removeAnimation(layer, true);  // 移除客户端动画
 * helper.removeAnimation(layer);         // 移除所有动画
 *
 * // 批量操作
 * helper.operaData(opera -> opera
 *     .newClientAnim(layer1, anim1)
 *     .removeServerAnim(layer2)
 *     .endOpera());
 * }</pre>
 *
 * @see IAnimationService
 * @see Opera
 * @see SCCAnimationApi#animation(Player)
 */
@SuppressWarnings("unused")
public class AnimationHelper {
    static final Comparator<Map.Entry<ResourceLocation, ResourceLocation>> COMPARATOR;

    static {
        Comparator<Map.Entry<ResourceLocation, ResourceLocation>> comparingInt = Comparator.comparingInt(entry -> {
            ClientAnimation animation = SyncAnimationFactory.getAnimation(entry.getValue());
            if (animation == null) return Integer.MIN_VALUE;
            if (animation.aabbMovement().isEmpty()) return Integer.MIN_VALUE;
            return animation.priority();
        });
        COMPARATOR = comparingInt.thenComparingInt(entry ->
                ClientAnimationRegistry.getAllLayers().getOrDefault(entry.getKey(), Integer.MIN_VALUE));
    }

    private final IAnimationService service;

    /**
     * 私有构造，通过 {@link #of(Player)} 创建实例。
     *
     * @param service 实际承担数据读写的动画服务
     */
    private AnimationHelper(IAnimationService service) {
        this.service = service;
    }

    /**
     * 根据玩家类型创建动画助手。
     *
     * <p>若为 {@link ServerPlayer} 则使用 {@link AnimationService}（数据存于服务端 Attachment），
     * 否则使用 {@link ClientAnimationService}（修改会发包同步到服务端）。</p>
     *
     * @param player 目标玩家
     * @return 绑定该玩家的动画助手
     */
    public static AnimationHelper of(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            return new AnimationHelper(new AnimationService(serverPlayer));
        } else {
            return new AnimationHelper(new ClientAnimationService(player));
        }
    }

    /**
     * 获取当前优先级最高的动画条目（层 → 动画）。
     *
     * <p>优先级由动画自身的 {@code priority} 与所在层的优先级共同决定。
     * 用于相机变换、AABB 移动等需要取"主"动画的逻辑。</p>
     *
     * @return 最高优先级动画条目；无任何动画时为 {@link Optional#empty()}
     */
    public Optional<Map.Entry<ResourceLocation, ResourceLocation>> getHighestPriorityAnimation(Predicate<Animation> predicate) {
        return service.getHighestPriorityAnimation(predicate);
    }

    /**
     * 在一个 {@link Opera} 上下文中批量修改动画数据。
     *
     * <p>传入的函数接收一个新建的 {@link Opera}，应在其末尾调用 {@link Opera#endOpera()}
     * 以提交修改并触发数据写入/同步。仅在函数返回后修改才会生效，避免中间态被同步。</p>
     *
     * @param opera 操作函数，接收 {@link Opera} 并返回 {@link AnimationHelper}
     */
    public void operaData(Function<Opera, AnimationHelper> opera) {
        opera.apply(new Opera(this));
    }

    /**
     * 在指定层播放动画。
     *
     * <p>自动判断动画属于服务端动画（{@link ServerAnimationRegistry} / {@link SyncAnimationFactory}）
     * 还是客户端动画，并分别写入对应映射。若层不存在则跳过并记录警告日志。</p>
     *
     * @param layer       动画层的资源位置
     * @param animationId 动画的资源位置
     */
    public void playAnimation(ResourceLocation layer, ResourceLocation animationId) {
        try {
            if(!SCCAnimationApi.isLayerExist(layer)) throw new RuntimeException("Unknown layer.");
            operaData(opera -> {
                if (ServerAnimationRegistry.getAnimations().containsKey(animationId)
                        || SyncAnimationFactory.getAnimations().containsKey(animationId)) {
                    opera.newServerAnim(layer, animationId);
                } else opera.newClientAnim(layer, animationId);
                return opera.endOpera();
            });
        } catch (Exception e) {
            SCCore.log.warn("Play animation error, layer : {}, animation : {}", layer, animationId);
        }
    }

    /**
     * 移除指定层的动画。
     *
     * @param layer    动画层的资源位置
     * @param isClient {@code true} 移除客户端动画，{@code false} 移除服务端动画
     */
    public void removeAnimation(ResourceLocation layer, boolean isClient) {
        if(SCCAnimationApi.isLayerExist(layer)) {
            operaData(opera -> {
                if (isClient) opera.removeClientAnim(layer);
                else opera.removeServerAnim(layer);
                return opera.endOpera();
            });
        } else warnUnknowLayer(layer);
    }

    /**
     * 移除指定层的所有动画，包括客户端动画与服务端动画。
     *
     * @param layer 动画层的资源位置
     */
    public void removeAnimation(ResourceLocation layer) {
        if(SCCAnimationApi.isLayerExist(layer)) {
            operaData(opera -> opera
                    .removeClientAnim(layer)
                    .removeServerAnim(layer)
                    .endOpera()
            );
        } else warnUnknowLayer(layer);
    }

    /**
     * 播放骑乘动画。
     *
     * <p>该方法在双端语义不同：</p>
     * <ul>
     *   <li>在服务端调用：直接将骑乘动画写入玩家数据并播放</li>
     *   <li>在客户端调用：将修改发送到服务端，由服务端创建/更新骑乘实体</li>
     * </ul>
     *
     * <p>若层不存在则跳过并记录警告日志。</p>
     *
     * @param rideAnim 骑乘动画数据（层 + 动画）
     * @see PlayerAnimations.RideAnim
     */
    public void playRideAnimation(PlayerAnimations.RideAnim rideAnim) {
        try {
            ResourceLocation layer = rideAnim.layer().orElseThrow();
            if(!SCCAnimationApi.isLayerExist(layer)) throw new RuntimeException("Unknown layer.");
            operaData(opera -> opera.setRideAnim(layer, rideAnim.animation().orElseThrow()).endOpera());
        } catch (Exception e) {
            SCCore.log.warn("Play ride animation error, layer : {}, animation : {}",
                    rideAnim.layer().orElse(null), rideAnim.animation().orElse(null)
            );
        }
    }

    /**
     * 移除当前骑乘动画。
     *
     * <p>在服务端调用会清除骑乘动画数据；在客户端调用会发包到服务端停止骑乘。</p>
     */
    public void removeRideAnimation() {
        operaData(opera -> opera.clearRideAnim().endOpera());
    }

    private void warnUnknowLayer(ResourceLocation layer) {
        SCCore.log.warn("Remove animation error, layer : {}.", layer);
    }

    /**
     * 获取玩家当前的动画数据快照。
     *
     * @return 玩家动画数据
     * @see PlayerAnimations
     */
    public PlayerAnimations getData() {
        return service.getData();
    }

    /**
     * 动画数据批量操作上下文，支持链式调用。
     *
     * <p>通过 {@link AnimationHelper#operaData(Function)} 获取实例。每次修改会在内部副本上累积，
     * 直至调用 {@link #endOpera()} 才将最终数据写回 {@link IAnimationService} 并触发同步。
     * 期间提供对客户端动画映射、服务端动画映射与骑乘动画的细粒度修改方法。</p>
     *
     * <h3>典型用法</h3>
     * <pre>{@code
     * helper.operaData(opera -> opera
     *     .newClientAnim(layer, animId)
     *     .removeServerAnim(otherLayer)
     *     .endOpera());
     * }</pre>
     *
     * @see AnimationHelper#operaData(Function)
     */
    @SuppressWarnings("UnusedReturnValue")
    public static class Opera {
        private final AnimationHelper controller;
        private PlayerAnimations data;

        /**
         * 私有构造，拷贝当前数据作为可修改副本。
         *
         * @param controller 所属动画助手
         */
        private Opera(AnimationHelper controller) {
            this.controller = controller;
            PlayerAnimations originalData = controller.service.getData();
            this.data = new PlayerAnimations(originalData.rideAnim(), originalData.clientAnimMap(), originalData.serverAnimMap());
        }

        /**
         * 对客户端动画映射执行任意修改。
         *
         * <p>在原始映射的副本上执行 {@code operator}，结果替换为新的客户端动画映射。</p>
         *
         * @param operator 映射修改器
         * @return 当前 {@code Opera}，便于链式调用
         */
        public Opera modifyClientAnimMap(Consumer<HashMap<ResourceLocation, ResourceLocation>> operator) {
            HashMap<ResourceLocation, ResourceLocation> originalAnimMap = controller.service.getData().clientAnimMap();
            HashMap<ResourceLocation, ResourceLocation> map = new HashMap<>(originalAnimMap);
            operator.accept(map);
            this.data = new PlayerAnimations(data.rideAnim(), map, data.serverAnimMap());
            return this;
        }

        /**
         * 对服务端动画映射执行任意修改。
         *
         * <p>在原始映射的副本上执行 {@code operator}，结果替换为新的服务端动画映射。</p>
         *
         * @param operator 映射修改器
         * @return 当前 {@code Opera}，便于链式调用
         */
        public Opera modifyServerAnimMap(Consumer<HashMap<ResourceLocation, ResourceLocation>> operator) {
            HashMap<ResourceLocation, ResourceLocation> originalAnimMap = controller.service.getData().serverAnimMap();
            HashMap<ResourceLocation, ResourceLocation> map = new HashMap<>(originalAnimMap);
            operator.accept(map);
            this.data = new PlayerAnimations(data.rideAnim(), data.clientAnimMap(), map);
            return this;
        }

        /**
         * 直接替换整个客户端动画映射。
         *
         * @param newMap 新的客户端动画映射
         * @return 当前 {@code Opera}，便于链式调用
         */
        public Opera newClientAnimMap(HashMap<ResourceLocation, ResourceLocation> newMap) {
            this.data = new PlayerAnimations(data.rideAnim(), newMap, data.serverAnimMap());
            return this;
        }

        /**
         * 直接替换整个服务端动画映射。
         *
         * @param newMap 新的服务端动画映射
         * @return 当前 {@code Opera}，便于链式调用
         */
        public Opera newServerAnimMap(HashMap<ResourceLocation, ResourceLocation> newMap) {
            this.data = new PlayerAnimations(data.rideAnim(), data.clientAnimMap(), newMap);
            return this;
        }

        /**
         * 在指定层添加一个客户端动画，等价于往客户端动画映射里 put 一项。
         *
         * @param layer     动画层
         * @param animation 动画资源位置
         * @return 当前 {@code Opera}，便于链式调用
         */
        public Opera newClientAnim(ResourceLocation layer, ResourceLocation animation) {
            return modifyClientAnimMap(map -> map.put(layer, animation));
        }

        /**
         * 在指定层添加一个服务端动画，等价于往服务端动画映射里 put 一项。
         *
         * @param layer     动画层
         * @param animation 动画资源位置
         * @return 当前 {@code Opera}，便于链式调用
         */
        public Opera newServerAnim(ResourceLocation layer, ResourceLocation animation) {
            return modifyServerAnimMap(map -> map.put(layer, animation));
        }

        /**
         * 移除指定层的客户端动画。
         *
         * @param layer 动画层
         * @return 当前 {@code Opera}，便于链式调用
         */
        public Opera removeClientAnim(ResourceLocation layer) {
            return modifyClientAnimMap(map -> map.remove(layer));
        }

        /**
         * 移除指定层的服务端动画。
         *
         * @param layer 动画层
         * @return 当前 {@code Opera}，便于链式调用
         */
        public Opera removeServerAnim(ResourceLocation layer) {
            return modifyServerAnimMap(map -> map.remove(layer));
        }

        /**
         * 设置骑乘动画（层与动画均不能为空）。
         *
         * @param layer     动画层，非空
         * @param animation 动画资源位置，非空
         * @return 当前 {@code Opera}，便于链式调用
         */
        public Opera setRideAnim(@NotNull ResourceLocation layer, @NotNull ResourceLocation animation) {
            PlayerAnimations.RideAnim rideAnim = new PlayerAnimations.RideAnim(Optional.of(layer), Optional.of(animation));
            this.data = new PlayerAnimations(rideAnim, data.clientAnimMap(), data.serverAnimMap());
            return this;
        }

        /**
         * 清除骑乘动画（层与动画均置空）。
         *
         * @return 当前 {@code Opera}，便于链式调用
         */
        public Opera clearRideAnim() {
            this.data = new PlayerAnimations(new PlayerAnimations.RideAnim(), data.clientAnimMap(), data.serverAnimMap());
            return this;
        }

        /**
         * 提交本次操作，将累积的修改写回 {@link IAnimationService} 并触发同步。
         *
         * <p>应作为链式调用的最后一环；返回所属 {@link AnimationHelper} 以便继续其他操作。</p>
         *
         * @return 所属动画助手
         */
        public AnimationHelper endOpera() {
            controller.service.setData(data);
            return controller;
        }
    }
}
