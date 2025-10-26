package com.linearpast.sccore.animation.mixin;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface IMixinKeyframeAnimationPlayer {
    void sccore$setCurrentTick(int tick);
}
