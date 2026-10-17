package io.zershyan.sccore.compat.animation.api.service.impl;

import io.zershyan.sccore.compat.animation.api.service.IAnimationService;
import io.zershyan.sccore.compat.animation.core.ClientAnimationRegistry;
import io.zershyan.sccore.compat.animation.data.Animation;
import io.zershyan.sccore.compat.animation.network.data.UpdateAnimationData;
import io.zershyan.sccore.compat.animation.registry.attachment.PlayerAnimations;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * 客户端动画服务实现。
 *
 * <p>{@code setData} 只向服务端提交客户端动画映射，由服务端写入 Attachment 的
 * {@code clientAnimMap} 并同步给所有玩家；服务端动画数据与骑乘动画不由客户端决定。</p>
 *
 * <p>动画定义查询走客户端注册表（本地注册 + 服务端同步，同 ID 时以同步定义为准），
 * 层优先级取合并后的层表。</p>
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
     * @param player 绑定的客户端玩家
     */
    public ClientAnimationService(AbstractClientPlayer player) {
        this.player = player;
    }

    /**
     * 从客户端缓存的玩家动画数据读取（由 Attachment 同步而来）。
     */
    @Override
    public PlayerAnimations getData() {
        return PlayerAnimations.getData(player);
    }

    /**
     * 把本地客户端动画映射提交给服务端，由服务端写入 Attachment 并同步给所有人。
     *
     * <p>只提交客户端动画映射：服务端动画数据与骑乘动画均不由客户端决定；
     * 且只有本地玩家自身的改动会被提交，避免借用他人数据覆盖自己。</p>
     */
    @Override
    public void setData(PlayerAnimations data) {
        if (!(player instanceof LocalPlayer)) return;
        PacketDistributor.sendToServer(new UpdateAnimationData(new HashMap<>(data.clientAnimMap())));
    }

    @Override
    public @Nullable Animation resolveAnimation(ResourceLocation animationId) {
        return ClientAnimationRegistry.getAnimation(animationId);
    }

    @Override
    public @Nullable Integer resolveLayerPriority(ResourceLocation layer) {
        return ClientAnimationRegistry.getAllLayers().get(layer);
    }

    @Override
    public Map<ResourceLocation, ResourceLocation> localAnimMap() {
        return new HashMap<>(getData().clientAnimMap());
    }
}
