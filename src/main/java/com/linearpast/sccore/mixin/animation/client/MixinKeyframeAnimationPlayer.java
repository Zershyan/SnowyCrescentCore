package com.linearpast.sccore.mixin.animation.client;

import com.linearpast.sccore.animation.mixin.IMixinKeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(KeyframeAnimationPlayer.class)
public class MixinKeyframeAnimationPlayer implements IMixinKeyframeAnimationPlayer {

    @Shadow(remap = false)
    private int currentTick;

    @Override
    @Unique
    public void sccore$setCurrentTick(int tick) {
        this.currentTick = tick;
    }
}
