package com.linearpast.sccore.animation.helper;

import com.linearpast.sccore.animation.service.AnimationService;
import com.linearpast.sccore.animation.service.IAnimationService;
import com.linearpast.sccore.animation.service.RawAnimationService;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Set;

public interface IAnimationServiceGetter {
    /**
     * Get helper
     */
    Set<IAnimationService<?, ?>> HELPERS = new LinkedHashSet<>(){{
        add(AnimationService.INSTANCE);
        add(RawAnimationService.INSTANCE);
    }};

    /**
     * @see IAnimationServiceGetter#filter
     */
    @Nullable
    default IAnimationService<?, ?> getService() {
        for (IAnimationService<?, ?> helper : HELPERS) {
            if (filter(helper)) {
                return helper;
            }
        }
        return null;
    }

    boolean filter(IAnimationService<?, ?> helper);
}
