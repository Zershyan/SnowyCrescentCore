package com.linearpast.sccore.animation.data;

import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AnimationData implements INBTSerializable<CompoundTag> {
    protected ResourceLocation key;
    private float camYaw;
    private float camPitch;
    private float camRoll;
    private int camComputePriority;
    private Vec3 camPosOffset = new Vec3(0.0F, 0.0F, 0.0F);
    private boolean camPosOffsetRelative;
    private @Nullable GenericAnimationData.LyingType lyingType;
    protected @Nullable Ride ride;

    public enum LyingType {
        RIGHT("RIGHT", 0),
        LEFT("LEFT", 1),
        FRONT("FRONT", 2),
        BACK("BACK", 3);
        private final String name;
        private final int id;
        LyingType(String name, int id) {
            this.name = name;
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public int getId() {
            return id;
        }

        @Nullable
        public static LyingType getLyingType(int id) {
            for (LyingType type : LyingType.values()) {
                if (type.id == id) {
                    return type;
                }
            }
            return null;
        }
    }

    public ResourceLocation getKey() {
        return key;
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public KeyframeAnimation getAnimation() {
        return PlayerAnimationRegistry.getAnimation(key);
    }

    public @Nullable Ride getRide() {
        return ride;
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

    public int getCamComputePriority() {
        return camComputePriority;
    }

    public boolean isCamPosOffsetRelative() {
        return camPosOffsetRelative;
    }

    public Vec3 getCamPosOffset() {
        return camPosOffset;
    }

    public @Nullable GenericAnimationData.LyingType getLyingType() {
        return lyingType;
    }

    public AnimationData withRide(Ride ride) {
        this.ride = ride;
        return this;
    }

    public AnimationData withCamYaw(float camYaw) {
        this.camYaw = camYaw;
        return this;
    }

    public AnimationData withCamPitch(float camPitch) {
        this.camPitch = camPitch;
        return this;
    }

    public AnimationData withCamRoll(float camRoll) {
        this.camRoll = camRoll;
        return this;
    }

    public AnimationData withCamComputePriority(int camPosPriority) {
        this.camComputePriority = camPosPriority;
        return this;
    }

    public AnimationData addCamPosOffset(Vec3 camPosOffset) {
        this.camPosOffset = this.camPosOffset.add(camPosOffset);
        return this;
    }

    public AnimationData setCamPosOffset(Vec3 camPosOffset) {
        this.camPosOffset = camPosOffset;
        return this;
    }

    public AnimationData withCamPosOffsetRelative(boolean camPosOffsetRelative) {
        this.camPosOffsetRelative = camPosOffsetRelative;
        return this;
    }

    public AnimationData withLyingType(@Nullable AnimationData.LyingType lyingType) {
        this.lyingType = lyingType;
        if(lyingType == null) return this;
        this.camPosOffset.add(0, -1.3f, 0);
        this.camPitch = -90.0f;
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

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("key", key.toString());
        tag.putInt("priority", camComputePriority);
        if(lyingType != null) tag.putInt("lyingType", lyingType.getId());
        tag.putFloat("camYaw", camYaw);
        tag.putFloat("camPitch", camPitch);
        tag.putFloat("camRoll", camRoll);
        CompoundTag camOffset = new CompoundTag();
        camOffset.putDouble("x", camPosOffset.x);
        camOffset.putDouble("y", camPosOffset.y);
        camOffset.putDouble("z", camPosOffset.z);
        camOffset.putBoolean("relative", camPosOffsetRelative);
        tag.put("camOffset", camOffset);
        if (ride != null) {
            CompoundTag rideTag = new CompoundTag();
            Vec3 offset = ride.getOffset();
            rideTag.putDouble("x", offset.x);
            rideTag.putDouble("y", offset.y);
            rideTag.putDouble("z", offset.z);
            rideTag.putInt("existTick", ride.getExistTick());
            rideTag.putFloat("xRot", ride.getXRot());
            rideTag.putFloat("yRot", ride.getYRot());
            List<ResourceLocation> componentAnimations = ride.getComponentAnimations();
            ListTag listTag = new ListTag();
            for (ResourceLocation animation : componentAnimations) {
                listTag.add(StringTag.valueOf(animation.toString()));
            }
            rideTag.put("subAnimations", listTag);
            tag.put("ride", rideTag);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        String string = nbt.getString("key");
        this.key = new ResourceLocation(string);
        this.camComputePriority = nbt.getInt("priority");
        if(nbt.contains("lyingType")) this.lyingType = LyingType.getLyingType(nbt.getInt("lyingType"));
        else this.lyingType = null;
        this.camYaw = nbt.getFloat("camYaw");
        this.camPitch = nbt.getFloat("camPitch");
        this.camRoll = nbt.getFloat("camRoll");
        CompoundTag camOffset = nbt.getCompound("camOffset");
        this.camPosOffset = new Vec3(
                camOffset.getDouble("x"),
                camOffset.getDouble("y"),
                camOffset.getDouble("z")
        );
        this.camPosOffsetRelative = camOffset.getBoolean("relative");
        try {
            if(nbt.contains("ride")) {
                CompoundTag rideTag = nbt.getCompound("ride");
                Vec3 offset = new Vec3(
                        rideTag.getDouble("x"),
                        rideTag.getDouble("y"),
                        rideTag.getDouble("z")
                );
                int existTick = rideTag.getInt("existTick");
                float xRot = rideTag.getFloat("xRot");
                float yRot = rideTag.getFloat("yRot");
                List<ResourceLocation> componentAnimations = new ArrayList<>();
                rideTag.getList("subAnimations", 8).forEach(tag ->
                        componentAnimations.add(new ResourceLocation(tag.getAsString()))
                );
                this.ride = Ride.create()
                        .withOffset(offset)
                        .withExistTick(existTick)
                        .withXRot(xRot)
                        .withYRot(yRot)
                        .setComponentAnimations(componentAnimations);
                return;
            }
        } catch (Exception ignored) {}
        this.ride = null;
    }
}
