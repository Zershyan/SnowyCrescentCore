package com.linearpast.sccore.animation.data;

import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class Animation {
    private final ResourceLocation name;
    private KeyframeAnimation animation;
    private float heightModifier = 1.0f;
    private float camYaw;
    private float camPitch;
    private float camRoll;
    private float camY;
    private @Nullable Animation.LyingType lyingType;
    private @Nullable Ride ride;

    public Animation(ResourceLocation name) {
        this.name = name;
    }

    public enum LyingType {
        RIGHT,
        LEFT,
        FRONT,
        BACK
    }

    public Animation withLyingType(@Nullable Animation.LyingType lyingType) {
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

    public Animation withHeightModifier(float heightModifier) {
        this.heightModifier = heightModifier;
        return this;
    }

    public Animation withCamYaw(float camYaw) {
        this.camYaw = camYaw;
        return this;
    }

    public Animation withCamPitch(float camPitch) {
        this.camPitch = camPitch;
        return this;
    }

    public Animation withCamRoll(float camRoll) {
        this.camRoll = camRoll;
        return this;
    }

    public Animation withCamY(float camY) {
        this.camY = camY;
        return this;
    }

    public Animation withRide(Ride ride) {
        this.ride = ride;
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
    public KeyframeAnimation getAnimation() {
        return PlayerAnimationRegistry.getAnimation(name);
    }

    public @Nullable Animation.LyingType getLyingType() {
        return lyingType;
    }

    public float getCamY() {
        return camY;
    }

    public float getHeightModifier() {
        return heightModifier;
    }

    public @Nullable Ride getRide() {
        return ride;
    }

    public ResourceLocation getName() {
        return name;
    }
}
