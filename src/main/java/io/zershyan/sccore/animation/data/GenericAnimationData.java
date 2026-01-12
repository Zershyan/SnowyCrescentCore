package io.zershyan.sccore.animation.data;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public class GenericAnimationData extends AnimationData {
    private @Nullable String name;
    private float heightModifier = 1.0f;

    GenericAnimationData(ResourceLocation key) {
        this.key = key;
    }
    public GenericAnimationData(){}

    public static GenericAnimationData create(ResourceLocation name) {
        return new GenericAnimationData(name);
    }

    public GenericAnimationData withHeightModifier(float heightModifier) {
        this.heightModifier = heightModifier;
        return this;
    }

    public GenericAnimationData withRide(Ride ride) {
        this.ride = ride;
        return this;
    }

    @Override
    public GenericAnimationData withLyingType(@Nullable LyingType lyingType) {
        this.heightModifier = 0.3f;
        return (GenericAnimationData) super.withLyingType(lyingType);
    }

    public GenericAnimationData withName(String name) {
        String regex = "^[a-zA-Z0-9_-]+$";
        Pattern pattern = Pattern.compile(regex);
        if (!pattern.matcher(name).matches()) {
            throw new IllegalArgumentException(
                    "Invalid animation name: " + name + ", must match " + regex
            );
        }
        this.name = name;
        return this;
    }

    public float getHeightModifier() {
        return heightModifier;
    }

    public @Nullable String getName() {
        return name;
    }
}
