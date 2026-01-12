package io.zershyan.sccore.animation.mixin;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IMixinPlayerAnimationFactoryHolder {
    record DataHolder(@Nullable ResourceLocation id, int priority, @NotNull IAnimation animation) {}

    void sccore$clearAnimations();
}
