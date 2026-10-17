package io.zershyan.sccore.compat.animation.api.service.impl;

import io.zershyan.sccore.compat.animation.api.service.IAnimationService;
import io.zershyan.sccore.compat.animation.core.ServerAnimationRegistry;
import io.zershyan.sccore.compat.animation.data.Animation;
import io.zershyan.sccore.compat.animation.registry.AnimationAttachments;
import io.zershyan.sccore.compat.animation.registry.attachment.PlayerAnimations;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/**
 * 服务端动画服务实现。
 *
 * <p>玩家动画数据通过 NeoForge 的 Attachment 机制存储于服务端玩家身上（见
 * {@link AnimationAttachments#PLAYER_ANIMATIONS}），{@code setData} 会直接写回该 Attachment，
 * 依赖框架的同步逻辑将变更同步到客户端。</p>
 *
 * <p>该实现只读取服务端注册表，因此可在逻辑服务端的任意 {@link Player} 上工作，
 * 不限于 {@code ServerPlayer}。</p>
 *
 * @see IAnimationService
 * @see ClientAnimationService
 */
public class AnimationService implements IAnimationService {
    private final Player player;

    /**
     * 构造服务端动画服务。
     *
     * @param player 绑定的逻辑服务端玩家
     */
    public AnimationService(Player player) {
        this.player = player;
    }

    /**
     * 从 Attachment 读取玩家动画数据。
     */
    @Override
    public PlayerAnimations getData() {
        return PlayerAnimations.getData(player);
    }

    /**
     * 将动画数据写回玩家的 Attachment，由框架自动同步。
     */
    @Override
    public void setData(PlayerAnimations data) {
        player.setData(AnimationAttachments.PLAYER_ANIMATIONS, data);
    }

    @Override
    public @Nullable Animation resolveAnimation(ResourceLocation animationId) {
        return ServerAnimationRegistry.commonGetAnimation(animationId);
    }

    @Override
    public @Nullable Integer resolveLayerPriority(ResourceLocation layer) {
        return ServerAnimationRegistry.getLayers().get(layer);
    }
}
