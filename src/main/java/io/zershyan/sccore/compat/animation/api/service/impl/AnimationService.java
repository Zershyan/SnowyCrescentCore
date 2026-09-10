package io.zershyan.sccore.compat.animation.api.service.impl;

import io.zershyan.sccore.compat.animation.api.service.IAnimationService;
import io.zershyan.sccore.compat.animation.data.Animation;
import io.zershyan.sccore.compat.animation.registry.AnimationAttachments;
import io.zershyan.sccore.compat.animation.registry.attachment.PlayerAnimations;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.attachment.AttachmentType;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 服务端动画服务实现。
 *
 * <p>玩家动画数据通过 NeoForge 的 Attachment 机制存储于服务端玩家身上（见
 * {@link AnimationAttachments#PLAYER_ANIMATIONS}），{@code setData} 会直接写回该 Attachment，
 * 依赖框架的同步逻辑将变更同步到客户端。</p>
 *
 * @see IAnimationService
 * @see ClientAnimationService
 */
public class AnimationService implements IAnimationService {
    private final ServerPlayer player;

    /**
     * 构造服务端动画服务。
     *
     * @param player 绑定的服务端玩家
     */
    public AnimationService(ServerPlayer player) {
        this.player = player;
    }

    private static Supplier<AttachmentType<PlayerAnimations>> type() {
        return AnimationAttachments.PLAYER_ANIMATIONS;
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
        player.setData(type(), data);
    }

    /**
     * 取最高优先级动画：服务端动画映射与骑乘动画（若有层）合并后取最大值。
     *
     * @return 最高优先级动画条目；无则 {@link Optional#empty()}
     */
    @Override
    public Optional<Map.Entry<ResourceLocation, ResourceLocation>> getHighestPriorityAnimation(Predicate<Animation> predicate) {
        return getServerHighestPriorityAnimation(predicate);
    }
}
