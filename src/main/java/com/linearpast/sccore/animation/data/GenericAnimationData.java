package com.linearpast.sccore.animation.data;

import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public class GenericAnimationData extends AnimationData {
    private @Nullable String name;
    private float heightModifier = 1.0f;
    private float camYaw;
    private float camPitch;
    private float camRoll;
    private float camY;
    private @Nullable GenericAnimationData.LyingType lyingType;

    GenericAnimationData(ResourceLocation key) {
        this.key = key;
    }
    public GenericAnimationData(){}

    public static GenericAnimationData create(ResourceLocation name) {
        return new GenericAnimationData(name);
    }

    public enum LyingType {
        RIGHT("RIGHT"),
        LEFT("LEFT"),
        FRONT("FRONT"),
        BACK("BACK");
        private final String name;
        LyingType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Nullable
        public static LyingType getLyingType(String name) {
            for (LyingType type : LyingType.values()) {
                if (type.name.equals(name)) {
                    return type;
                }
            }
            return null;
        }
    }

    public GenericAnimationData withLyingType(@Nullable GenericAnimationData.LyingType lyingType) {
        this.lyingType = lyingType;
        if(lyingType == null) return this;
        this.camY = -1.3f;
        this.camPitch = -90.0f;
        this.heightModifier = 0.3f;
        switch (lyingType) {
            case RIGHT -> {
                this.camRoll = 90.0f;
                this.camYaw = 90.0f;
            }
            case LEFT -> {
                this.camRoll = -90.0f;
                this.camYaw = -90.0f;
            }
            case BACK -> this.camPitch = 90.0f;
        }
        return this;
    }

    public GenericAnimationData withHeightModifier(float heightModifier) {
        this.heightModifier = heightModifier;
        return this;
    }

    public GenericAnimationData withCamYaw(float camYaw) {
        this.camYaw = camYaw;
        return this;
    }

    public GenericAnimationData withCamPitch(float camPitch) {
        this.camPitch = camPitch;
        return this;
    }

    public GenericAnimationData withCamRoll(float camRoll) {
        this.camRoll = camRoll;
        return this;
    }

    public GenericAnimationData withCamY(float camY) {
        this.camY = camY;
        return this;
    }

    public GenericAnimationData withRide(Ride ride) {
        this.ride = ride;
        return this;
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

    public void setLyingType(@Nullable LyingType lyingType) {
        this.lyingType = lyingType;
    }

    public float getCamRoll() {
        return camRoll;
    }

    public float getCamPitch() {
        return camPitch;
    }

    public float getCamYaw() {
        return camYaw;
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public KeyframeAnimation getAnimation() {
        return PlayerAnimationRegistry.getAnimation(key);
    }

    public @Nullable GenericAnimationData.LyingType getLyingType() {
        return lyingType;
    }

    public float getCamY() {
        return camY;
    }

    public float getHeightModifier() {
        return heightModifier;
    }

    public @Nullable String getName() {
        return name;
    }
}
