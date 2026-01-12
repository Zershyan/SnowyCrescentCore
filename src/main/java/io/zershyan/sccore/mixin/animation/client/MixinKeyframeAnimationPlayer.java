package io.zershyan.sccore.mixin.animation.client;

import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import io.zershyan.sccore.animation.mixin.IMixinKeyframeAnimationPlayer;
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
