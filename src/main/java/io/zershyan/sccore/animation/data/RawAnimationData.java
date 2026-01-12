package io.zershyan.sccore.animation.data;

import net.minecraft.resources.ResourceLocation;

public class RawAnimationData extends AnimationData{
    RawAnimationData(ResourceLocation key) {
        this.key = key;
    }
    public RawAnimationData(){}

    public static RawAnimationData create(ResourceLocation name) {
        return new RawAnimationData(name);
    }

    public RawAnimationData withRide(Ride ride) {
        this.ride = ride;
        return this;
    }
}
