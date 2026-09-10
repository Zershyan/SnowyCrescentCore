package io.zershyan.sccore.compat.animation.imixin;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import org.spongepowered.asm.mixin.Unique;

/**
 * Mixin 接口，用于访问 {@code KeyframeAnimationPlayer} 的 {@code currentTick} 字段。
 */
public interface IMixinKeyframeAnimationPlayer {
    /**
     * 安全转型，失败时返回 {@code null}。
     */
    static IMixinKeyframeAnimationPlayer of(IAnimation player) {
        try {
            return (IMixinKeyframeAnimationPlayer) player;
        } catch (Exception ignored) {
            return null;
        }
    }

    @Unique
    void sccore$setCurrentTick(int tick);
}
