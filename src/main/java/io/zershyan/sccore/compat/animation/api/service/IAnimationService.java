package io.zershyan.sccore.compat.animation.api.service;

import io.zershyan.sccore.compat.animation.api.helper.AnimationHelper;
import io.zershyan.sccore.compat.animation.api.service.impl.AnimationService;
import io.zershyan.sccore.compat.animation.api.service.impl.ClientAnimationService;
import io.zershyan.sccore.compat.animation.data.Animation;
import io.zershyan.sccore.compat.animation.registry.attachment.PlayerAnimations;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * 玩家动画服务的抽象接口。
 *
 * <p>定义了读写玩家 {@link PlayerAnimations} 数据所需的最小契约，由服务端
 * （{@link AnimationService}）与客户端（{@link ClientAnimationService}）分别实现。
 * {@link AnimationHelper} 持有一个该接口实例，从而对调用方屏蔽双端差异。</p>
 *
 * <p>动画定义的解析一律交由实现按自身逻辑端完成，公共的优先级仲裁只依赖
 * {@link #resolveAnimation(ResourceLocation)} 与 {@link #resolveLayerPriority(ResourceLocation)}，
 * 因此服务端不会去读取只在客户端存在的注册表。</p>
 *
 * @see AnimationService
 * @see ClientAnimationService
 * @see AnimationHelper
 */
public interface IAnimationService {
    /**
     * 获取玩家当前的动画数据。
     *
     * @return 玩家动画数据
     */
    PlayerAnimations getData();

    /**
     * 设置玩家动画数据。
     *
     * <p>实现负责将数据持久化/同步：服务端写入 Attachment，客户端则发包到服务端。</p>
     *
     * @param data 新的玩家动画数据
     */
    void setData(PlayerAnimations data);

    /**
     * 查询当前逻辑端可见的动画定义。
     *
     * @param animationId 动画资源位置
     * @return 动画定义，未注册时返回 {@code null}
     */
    @Nullable
    Animation resolveAnimation(ResourceLocation animationId);

    /**
     * 查询当前逻辑端可见的层优先级。
     *
     * @param layer 动画层
     * @return 层优先级，未注册时返回 {@code null}
     */
    @Nullable
    Integer resolveLayerPriority(ResourceLocation layer);

    /**
     * 服务端权威动画映射：服务端动画映射与骑乘动画的合并结果。
     *
     * @return 层 → 动画的映射
     */
    default Map<ResourceLocation, ResourceLocation> serverAnimMap() {
        PlayerAnimations data = getData();
        Map<ResourceLocation, ResourceLocation> map = new HashMap<>(data.serverAnimMap());
        data.rideAnim().layer().ifPresent(layer -> data.rideAnim().animation().ifPresent(animation -> map.put(layer, animation)));
        return map;
    }

    /**
     * 客户端本地动画映射，服务端实现恒为空。
     *
     * @return 层 → 动画的映射
     */
    default Map<ResourceLocation, ResourceLocation> localAnimMap() {
        return Map.of();
    }

    /**
     * 获取当前优先级最高的动画条目（层 → 动画）。
     *
     * <p>服务端动画与骑乘动画优先，二者皆无合格候选时才回退到客户端本地动画。</p>
     *
     * @return 最高优先级动画条目；无合格候选时为 {@link Optional#empty()}
     */
    default Optional<Map.Entry<ResourceLocation, ResourceLocation>> getHighestPriorityAnimation(Predicate<Animation> predicate) {
        Optional<Map.Entry<ResourceLocation, ResourceLocation>> server = getServerHighestPriorityAnimation(predicate);
        return server.isPresent() ? server : getClientHighestPriorityAnimation(predicate);
    }

    /**
     * 在服务端权威动画映射中获取最高优先级条目。
     *
     * @return 最高优先级动画条目；无合格候选时为 {@link Optional#empty()}
     */
    default Optional<Map.Entry<ResourceLocation, ResourceLocation>> getServerHighestPriorityAnimation(Predicate<Animation> predicate) {
        return highestPriority(serverAnimMap(), predicate);
    }

    /**
     * 在客户端本地动画映射中获取最高优先级条目。
     *
     * @return 最高优先级动画条目；无合格候选时为 {@link Optional#empty()}
     */
    default Optional<Map.Entry<ResourceLocation, ResourceLocation>> getClientHighestPriorityAnimation(Predicate<Animation> predicate) {
        return highestPriority(localAnimMap(), predicate);
    }

    /**
     * 按"动画自身优先级 → 所在层优先级"取最大条目。
     *
     * <p>动画定义缺失的条目在调用 {@code predicate} 之前就被跳过，谓词无需自行判空。</p>
     *
     * @param candidates 候选映射
     * @param predicate  动画定义过滤条件
     * @return 最大条目；无合格候选时为 {@link Optional#empty()}
     */
    private Optional<Map.Entry<ResourceLocation, ResourceLocation>> highestPriority(
            Map<ResourceLocation, ResourceLocation> candidates,
            Predicate<Animation> predicate
    ) {
        Map.Entry<ResourceLocation, ResourceLocation> best = null;
        int bestPriority = 0;
        int bestLayerPriority = 0;
        for (Map.Entry<ResourceLocation, ResourceLocation> entry : candidates.entrySet()) {
            Animation animation = resolveAnimation(entry.getValue());
            if (animation == null || !predicate.test(animation)) continue;
            Integer layerPriority = resolveLayerPriority(entry.getKey());
            int priority = animation.priority();
            int layer = layerPriority == null ? Integer.MIN_VALUE : layerPriority;
            if (best != null && (priority < bestPriority || (priority == bestPriority && layer <= bestLayerPriority))) continue;
            best = entry;
            bestPriority = priority;
            bestLayerPriority = layer;
        }
        return Optional.ofNullable(best);
    }
}
