package com.linearpast.sccore.animation.helper;

import net.minecraft.resources.ResourceLocation;

public class HelperGetterFromAnimation implements IHelperGetter {
    private final ResourceLocation location;

    public HelperGetterFromAnimation(ResourceLocation location) {
        this.location = location;
    }

    public static IHelperGetter create(ResourceLocation location) {
        return new HelperGetterFromAnimation(location);
    }

    @Override
    public boolean filter(IAnimationHelper<?, ?> helper) {
        return helper.isAnimationPresent(location);
    }
}
