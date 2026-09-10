package io.zershyan.sccore.compat.animation.api.data;

import io.zershyan.sccore.compat.animation.core.ServerAnimationRegistry;
import io.zershyan.sccore.compat.animation.data.Animation;
import io.zershyan.sccore.compat.animation.registry.attachment.PlayerAnimations;
import net.minecraft.resources.ResourceLocation;

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
     * 获取当前优先级最高的动画条目（层 → 动画）。
     *
     * @return 最高优先级动画条目；无动画时为 {@link Optional#empty()}
     */
    default Optional<Map.Entry<ResourceLocation, ResourceLocation>> getServerHighestPriorityAnimation(Predicate<Animation> predicate){
        PlayerAnimations data = getData();
        HashMap<ResourceLocation, ResourceLocation> serverAnimMap = new HashMap<>(data.serverAnimMap());
        data.rideAnim().layer().ifPresent(layer -> serverAnimMap.put(layer, data.rideAnim().animation().orElseThrow()));
        Map.copyOf(serverAnimMap).forEach((key, value) -> {
            if(!predicate.test(ServerAnimationRegistry.commonGetAnimation(value))) serverAnimMap.remove(key);
        });
        return serverAnimMap.entrySet().stream().max(AnimationHelper.COMPARATOR);
    }

    default Optional<Map.Entry<ResourceLocation, ResourceLocation>> getClientHighestPriorityAnimation(Predicate<Animation> predicate) {
        return Optional.empty();
    }

    Optional<Map.Entry<ResourceLocation, ResourceLocation>> getHighestPriorityAnimation(Predicate<Animation> predicate);
}
