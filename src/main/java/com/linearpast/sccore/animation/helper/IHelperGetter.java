package com.linearpast.sccore.animation.helper;

import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Set;

public interface IHelperGetter {
    /**
     * Get helper
     */
    Set<IAnimationHelper<?, ?>> HELPERS = new LinkedHashSet<>(){{
        add(AnimationHelper.INSTANCE);
        add(RawAnimationHelper.INSTANCE);
    }};

    /**
     * @see IHelperGetter#filter
     */
    @Nullable
    default IAnimationHelper<?, ?> getHelper() {
        for (IAnimationHelper<?, ?> helper : HELPERS) {
            if (filter(helper)) {
                return helper;
            }
        }
        return null;
    }

    boolean filter(IAnimationHelper<?, ?> helper);
}
