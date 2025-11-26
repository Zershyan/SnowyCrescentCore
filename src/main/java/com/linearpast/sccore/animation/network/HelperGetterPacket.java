package com.linearpast.sccore.animation.network;

import com.linearpast.sccore.animation.helper.AnimationHelper;
import com.linearpast.sccore.animation.helper.IAnimationHelper;
import com.linearpast.sccore.animation.helper.IHelperGetter;
import com.linearpast.sccore.animation.helper.RawAnimationHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public abstract class HelperGetterPacket implements IHelperGetter {
    /**
     * Override it to filter helper in network packet
     * @param helper helper
     * @return Is right helper
     */
    public boolean filter(IAnimationHelper<?, ?> helper) {
        if(helper instanceof AnimationHelper animationHelper) {
            ResourceLocation animation = getAnimation();
            if(animation != null) return animationHelper.isAnimationPresent(animation);
        } else if (helper instanceof RawAnimationHelper rawHelper) {
            CompoundTag tag = getAnimationTag();
            if(tag != null) return rawHelper.getAnimation(tag) != null;
        }
        return false;
    }

    /**
     * Selectable to override it
     * @return Animation loacation
     * @see HelperGetterPacket#filter
     */
    @Nullable
    protected ResourceLocation getAnimation() {
        return null;
    }

    /**
     * Selectable to override it
     * @return Animation data
     * @see HelperGetterPacket#filter
     */
    @Nullable
    protected CompoundTag getAnimationTag() {
        return null;
    }
}
