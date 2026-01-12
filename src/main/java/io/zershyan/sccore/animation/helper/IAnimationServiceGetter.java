package io.zershyan.sccore.animation.helper;

import io.zershyan.sccore.animation.service.AnimationService;
import io.zershyan.sccore.animation.service.IAnimationService;
import io.zershyan.sccore.animation.service.RawAnimationService;
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
