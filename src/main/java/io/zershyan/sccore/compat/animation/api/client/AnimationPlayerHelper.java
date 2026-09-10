package io.zershyan.sccore.compat.animation.api.client;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import io.zershyan.sccore.compat.animation.api.SCCAnimationApi;
import io.zershyan.sccore.compat.animation.core.ClientAnimationRegistry;
import io.zershyan.sccore.compat.animation.imixin.IMixinKeyframeAnimationPlayer;
import io.zershyan.sccore.compat.animation.registry.attachment.PlayerAnimations;
import io.zershyan.sccore.datagen.init.SCCKeyLang;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

/**
 * 客户端动画播放器助手，直接驱动 Player Animator 的 {@code ModifierLayer} / {@code KeyframeAnimationPlayer}。
 *
 * <p>提供基于淡入淡出的播放/移除、按新旧数据 diff 的增量更新、跨玩家动画同步以及获取当前播放器等能力。
 * 该类仅在客户端使用，对应服务端的数据操作请使用
 * {@link io.zershyan.sccore.compat.animation.api.data.AnimationHelper}。</p>
 *
 * <h3>典型用法</h3>
 * <pre>{@code
 * AnimationPlayerHelper helper = SCCAnimationApi.animPlayer(clientPlayer);
 *
 * // 播放动画（默认 3 tick 淡入）
 * helper.playAnimation(layer, animId);
 *
 * // 移除动画（触发淡出）
 * helper.removeAnimation(layer);
 *
 * // 增量更新（根据 diff 自动播放/移除）
 * helper.updateAnimation(oldData, newData);
 *
 * // 获取当前播放器
 * KeyframeAnimationPlayer player = helper.getKeyframeAnimationPlayer(layer);
 * }</pre>
 *
 * @see SCCAnimationApi#animPlayer(AbstractClientPlayer)
 */
public class AnimationPlayerHelper {
    private final AbstractClientPlayer player;

    /**
     * 私有构造，通过 {@link #of(AbstractClientPlayer)} 创建实例。
     *
     * @param player 目标客户端玩家
     */
    private AnimationPlayerHelper(AbstractClientPlayer player) {
        this.player = player;
    }

    /**
     * 为指定客户端玩家创建播放器助手。
     *
     * @param player 目标客户端玩家，非空
     * @return 绑定该玩家的播放器助手
     */
    public static AnimationPlayerHelper of(@NotNull AbstractClientPlayer player) {
        return new AnimationPlayerHelper(player);
    }

    /**
     * 在指定层播放动画，使用默认的 3 tick 淡入（{@link Ease#INOUTSINE}）。
     *
     * @param layer       动画层的资源位置
     * @param animationId 动画的资源位置
     */
    public void playAnimation(ResourceLocation layer, ResourceLocation animationId) {
        innerPlayAnimation(layer, animationId);
    }

    /**
     * 将本玩家的当前动画 tick 同步到目标玩家的同名层，使二者保持同步播放。
     *
     * <p>通过 Mixin 接口 {@link IMixinKeyframeAnimationPlayer} 写入目标动画的 {@code currentTick}。
     * 任一方缺少对应层或动画时静默返回。</p>
     *
     * @param target 需要被同步的目标客户端玩家
     */
    @SuppressWarnings("unchecked")
    public void syncAnimation(AbstractClientPlayer target) {
        PlayerAnimations playerData = PlayerAnimations.getData(player);
        PlayerAnimations targetData = PlayerAnimations.getData(target);

        ResourceLocation playerLayer = playerData.rideAnim().layer().orElse(null);
        ResourceLocation targetLayer = targetData.rideAnim().layer().orElse(null);
        try {
            if(playerLayer == null || targetLayer == null) return;
            ModifierLayer<IAnimation> modifierLayer = (ModifierLayer<IAnimation>) PlayerAnimationAccess
                    .getPlayerAssociatedData(player).get(playerLayer);
            ModifierLayer<IAnimation> targetModifierLayer = (ModifierLayer<IAnimation>) PlayerAnimationAccess
                    .getPlayerAssociatedData(target).get(targetLayer);
            if(modifierLayer == null || targetModifierLayer == null) return;
            IMixinKeyframeAnimationPlayer animation = IMixinKeyframeAnimationPlayer.of(modifierLayer.getAnimation());
            KeyframeAnimationPlayer targetAnimation = (KeyframeAnimationPlayer) targetModifierLayer.getAnimation();
            if(animation == null || targetAnimation == null) return;
            int currentTick = targetAnimation.getCurrentTick();
            animation.sccore$setCurrentTick(currentTick);
        } catch (Exception ignored) {}
    }

    /**
     * 移除指定层的动画（等价于在该层播放 {@code null}，触发淡出）。
     *
     * @param layer 动画层的资源位置
     */
    public void removeAnimation(ResourceLocation layer) {
        innerPlayAnimation(layer, null);
    }

    /**
     * 以默认 3 tick 淡入播放/移除层动画的内部实现。
     *
     * @param layer       动画层
     * @param animationId 动画资源位置，{@code null} 表示移除
     */
    private void innerPlayAnimation(ResourceLocation layer, @Nullable ResourceLocation animationId) {
        innerPlayAnimation(3, Ease.INOUTSINE, layer, animationId);
    }

    /**
     * 播放/移除层动画的内部实现。
     *
     * <p>取得该层的 {@code ModifierLayer}，用 {@code KeyframeAnimationPlayer} 包装注册表中的关键帧动画，
     * 先停止旧动画，再以指定淡入长度与缓动替换。若动画在注册表中不存在，会向本地玩家提示并清理对应映射项。</p>
     *
     * @param fadeLength 淡入持续 tick 数
     * @param ease       淡入缓动
     * @param layer      动画层
     * @param animation  动画资源位置，{@code null} 表示移除
     */
    @SuppressWarnings("unchecked")
    public void innerPlayAnimation(int fadeLength, Ease ease, ResourceLocation layer, @Nullable ResourceLocation animation) {
        ModifierLayer<IAnimation> modifierLayer = (ModifierLayer<IAnimation>) PlayerAnimationAccess
                .getPlayerAssociatedData(player).get(layer);
        if(modifierLayer == null) return;
        KeyframeAnimationPlayer iAnimation = null;
        Minecraft instance = Minecraft.getInstance();
        LocalPlayer localPlayer = instance.player;
        if(animation != null) {
            KeyframeAnimation keyframeAnimation = ClientAnimationRegistry.getKeyframeAnimation(animation);
            iAnimation = keyframeAnimation != null ? new KeyframeAnimationPlayer(keyframeAnimation) : null;
            if(keyframeAnimation == null && localPlayer != null) {
                localPlayer.sendSystemMessage(SCCKeyLang.AnimationResourceNotFound
                        .get(animation.toString()).withStyle(ChatFormatting.RED));
                SCCAnimationApi.animation(player).operaData(opera -> opera
                        .removeClientAnim(layer)
                        .removeServerAnim(layer)
                        .endOpera()
                );
            }
        }
        KeyframeAnimationPlayer layerAnimation = (KeyframeAnimationPlayer) modifierLayer.getAnimation();
        if(layerAnimation != null) layerAnimation.stop();
        modifierLayer.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(fadeLength, ease), iAnimation);
    }

    /**
     * 根据新旧动画数据的差异增量更新播放器。
     *
     * <p>对骑乘动画、客户端动画映射、服务端动画映射分别比较：新增或变化的动画会被播放，
     * 消失的动画会被移除。{@code oldAnimations} 为 {@code null} 时视为全部新增。</p>
     *
     * @param oldAnimations 旧动画数据，可为 {@code null}
     * @param newAnimations 新动画数据
     */
    public void updateAnimation(PlayerAnimations oldAnimations, PlayerAnimations newAnimations) {
        if(oldAnimations == null || !newAnimations.rideAnim().equals(oldAnimations.rideAnim())) {
            PlayerAnimations.RideAnim newRideAnim = newAnimations.rideAnim();
            if(oldAnimations != null) oldAnimations.rideAnim().layer().ifPresent(this::removeAnimation);
            Optional<ResourceLocation> layer = newRideAnim.layer();
            Optional<ResourceLocation> animation = newRideAnim.animation();
            if(layer.isPresent() && animation.isPresent()) playAnimation(layer.get(), animation.get());
        }
        if(oldAnimations == null || !newAnimations.clientAnimMapEqual(oldAnimations.clientAnimMap())) {
            compareAndAct(oldAnimations == null ? null : oldAnimations.clientAnimMap(), newAnimations.clientAnimMap());
        }
        if(oldAnimations == null || !newAnimations.serverAnimMapEqual(oldAnimations.serverAnimMap())) {
            compareAndAct(oldAnimations == null ? null : oldAnimations.serverAnimMap(), newAnimations.serverAnimMap());
        }
    }

    private void compareAndAct(@Nullable Map<ResourceLocation, ResourceLocation> oldMap, Map<ResourceLocation, ResourceLocation> newMap) {
        if(oldMap == null) {
            newMap.forEach(this::playAnimation);
            return;
        }

        for (Map.Entry<ResourceLocation, ResourceLocation> entry : newMap.entrySet()) {
            ResourceLocation key = entry.getKey();
            ResourceLocation newVal = entry.getValue();
            ResourceLocation oldVal = oldMap.get(key);
            if (oldVal == null || !oldVal.equals(newVal)) {
                playAnimation(key, newVal);
            }
        }

        for (Map.Entry<ResourceLocation, ResourceLocation> entry : oldMap.entrySet()) {
            ResourceLocation key = entry.getKey();
            if (!newMap.containsKey(key)) {
                removeAnimation(key);
            }
        }
    }

    /**
     * 获取指定层当前正在播放的 {@code KeyframeAnimationPlayer}。
     *
     * @param layer 动画层
     * @return 当前播放器；层不存在或无动画时返回 {@code null}
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public KeyframeAnimationPlayer getKeyframeAnimationPlayer(ResourceLocation layer) {
        ModifierLayer<IAnimation> modifierLayer  = (ModifierLayer<IAnimation>) PlayerAnimationAccess
                .getPlayerAssociatedData(player).get(layer);
        if(modifierLayer == null) return null;
        return (KeyframeAnimationPlayer) modifierLayer.getAnimation();
    }
}