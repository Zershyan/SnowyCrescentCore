package io.zershyan.sccore.compat.animation.api.service.impl;

import com.mojang.datafixers.util.Either;
import io.zershyan.sccore.compat.animation.api.helper.AnimationHelper;
import io.zershyan.sccore.compat.animation.api.service.IAnimationService;
import io.zershyan.sccore.compat.animation.core.ClientAnimationRegistry;
import io.zershyan.sccore.compat.animation.core.SyncAnimationFactory;
import io.zershyan.sccore.compat.animation.data.Animation;
import io.zershyan.sccore.compat.animation.data.ClientRideAnimDTO;
import io.zershyan.sccore.compat.animation.network.data.UpdateAnimationData;
import io.zershyan.sccore.compat.animation.network.data.UpdateRideAnimationData;
import io.zershyan.sccore.compat.animation.registry.attachment.PlayerAnimations;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * 客户端动画服务实现。
 *
 * <p>客户端不直接持久化动画数据，而是通过发包将修改同步到服务端：{@code setData} 会分别发送
 * 骑乘动画包（{@link UpdateRideAnimationData}）与客户端/服务端动画映射包
 * （{@link UpdateAnimationData}），由服务端回写并经 Attachment 同步回所有客户端。</p>
 *
 * @see IAnimationService
 * @see AnimationService
 */
@OnlyIn(Dist.CLIENT)
public class ClientAnimationService implements IAnimationService {
    private final AbstractClientPlayer player;

    /**
     * 构造客户端动画服务。
     *
     * @param player 绑定的客户端玩家（会被强转为 {@link AbstractClientPlayer}）
     */
    public ClientAnimationService(Player player) {
        this.player = (AbstractClientPlayer) player;
    }

    /**
     * 从客户端缓存的玩家动画数据读取（由 Attachment 同步而来）。
     */
    @Override
    public PlayerAnimations getData() {
        return PlayerAnimations.getData(player);
    }

    /**
     * 将动画数据通过发包同步到服务端。
     *
     * <p>对骑乘动画会根据来源选择 {@code ResourceLocation} 或 {@link ClientRideAnimDTO}（携带完整
     * 客户端动画定义）；对客户端/服务端动画映射分别发送两个 {@link UpdateAnimationData} 包。</p>
     */
    @Override
    public void setData(PlayerAnimations data) {
        Optional<ResourceLocation> animation = data.rideAnim().animation();
        Optional<Either<ResourceLocation, ClientRideAnimDTO>> either = animation.map(loc -> {
            if (ClientAnimationRegistry.getAnimations().containsKey(loc)) {
                return Either.right(new ClientRideAnimDTO(loc, ClientAnimationRegistry.getAnimations().get(loc)));
            } else if (SyncAnimationFactory.getAnimations().containsKey(loc)) {
                return Either.left(loc);
            }
            return null;
        });
        PacketDistributor.sendToServer(new UpdateRideAnimationData(data.rideAnim().layer(), either));
        PacketDistributor.sendToServer(new UpdateAnimationData(data.clientAnimMap(), false));
        PacketDistributor.sendToServer(new UpdateAnimationData(data.serverAnimMap(), true));
    }

    /**
     * 取最高优先级动画：优先从客户端动画映射取最大值，否则回退到服务端动画映射与骑乘动画合并取最大值。
     *
     * @return 最高优先级动画条目；无则 {@link Optional#empty()}
     */
    @Override
    public Optional<Map.Entry<ResourceLocation, ResourceLocation>> getHighestPriorityAnimation(Predicate<Animation> predicate) {
        Optional<Map.Entry<ResourceLocation, ResourceLocation>> client = getClientHighestPriorityAnimation(predicate);
        return client.isEmpty() ? getServerHighestPriorityAnimation(predicate) : client;
    }

    @Override
    public Optional<Map.Entry<ResourceLocation, ResourceLocation>> getClientHighestPriorityAnimation(Predicate<Animation> predicate) {
        PlayerAnimations data = getData();
        HashMap<ResourceLocation, ResourceLocation> serverAnimMap = new HashMap<>(data.serverAnimMap());
        data.rideAnim().layer().ifPresent(layer -> serverAnimMap.put(layer, data.rideAnim().animation().orElseThrow()));
        Map.copyOf(serverAnimMap).forEach((key, value) -> {
            if (!predicate.test(ClientAnimationRegistry.getAnimation(value))) serverAnimMap.remove(key);
        });
        Optional<Map.Entry<ResourceLocation, ResourceLocation>> max = serverAnimMap.entrySet().stream().max(AnimationHelper.COMPARATOR);
        if (max.isEmpty()) {
            HashMap<ResourceLocation, ResourceLocation> clientAnimMap = new HashMap<>(data.clientAnimMap());
            Map.copyOf(clientAnimMap).forEach((key, value) -> {
                if (!predicate.test(ClientAnimationRegistry.getAnimation(value))) clientAnimMap.remove(key);
            });
            return clientAnimMap.entrySet().stream().max(AnimationHelper.COMPARATOR);
        } else return max;
    }
}
