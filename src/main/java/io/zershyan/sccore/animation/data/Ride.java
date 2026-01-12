package io.zershyan.sccore.animation.data;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class Ride {
    private final List<ResourceLocation> componentAnimations = new ArrayList<>();
    private Vec3 offset = new Vec3(0.0D, 0.0D, 0.0D);
    private int existTick;
    private float xRot;
    private float yRot;


    public static Ride create() {
        return new Ride();
    }

    public Ride withOffset(Vec3 offset) {
        this.offset = offset;
        return this;
    }

    public Ride withExistTick(int existTick) {
        this.existTick = existTick;
        return this;
    }

    public Ride withXRot(float xRot) {
        this.xRot = xRot;
        return this;
    }

    public Ride withYRot(float yRot) {
        this.yRot = yRot;
        return this;
    }

    public Ride setComponentAnimations(List<ResourceLocation> animations) {
        this.componentAnimations.clear();
        this.componentAnimations.addAll(animations);
        return this;
    }

    public Ride addComponentAnimation(ResourceLocation animation) {
        this.componentAnimations.add(animation);
        return this;
    }

    public float getXRot() {
        return xRot;
    }

    public void setXRot(float xRot) {
        this.xRot = xRot;
    }

    public float getYRot() {
        return yRot;
    }

    public void setYRot(float yRot) {
        this.yRot = yRot;
    }

    public Vec3 getOffset() {
        return offset;
    }

    public void setOffset(Vec3 offset) {
        this.offset = offset;
    }

    public int getExistTick() {
        return existTick;
    }

    public void setExistTick(int existTick) {
        this.existTick = existTick;
    }

    public List<ResourceLocation> getComponentAnimations() {
        return componentAnimations;
    }
}
