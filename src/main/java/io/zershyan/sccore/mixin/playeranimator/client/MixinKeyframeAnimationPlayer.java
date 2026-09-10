package io.zershyan.sccore.mixin.playeranimator.client;

import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import io.zershyan.sccore.compat.animation.imixin.IMixinKeyframeAnimationPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(KeyframeAnimationPlayer.class)
public class MixinKeyframeAnimationPlayer implements IMixinKeyframeAnimationPlayer {
    @Shadow(remap = false)
    private int currentTick;

    @Unique
    @Override
    public void sccore$setCurrentTick(int tick) {
        this.currentTick = tick;
    }
}
