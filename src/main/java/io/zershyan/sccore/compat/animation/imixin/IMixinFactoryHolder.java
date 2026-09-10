package io.zershyan.sccore.compat.animation.imixin;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Mixin 接口，用于访问 {@code PlayerAnimationFactory.FactoryHolder} 的内部数据，
 * 支持按 ID 清除已注册的动画工厂。
 */
public interface IMixinFactoryHolder {
    static IMixinFactoryHolder of(PlayerAnimationFactory.FactoryHolder factoryHolder) {
        return (IMixinFactoryHolder) factoryHolder;
    }

    /**
     * 清除指定 ID 集合对应的已注册动画工厂。
     */
    void sccore$clearAnimations(Set<ResourceLocation> ids);

    record DataHolder(@Nullable ResourceLocation id, int priority, @NotNull IAnimation animation) {
    }
}
