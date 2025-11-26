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
    protected @Nullable Ride ride;

    public ResourceLocation getKey() {
        return key;
    }

    public AnimationData withRide(Ride ride) {
        this.ride = ride;
        return this;
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public KeyframeAnimation getAnimation() {
        return PlayerAnimationRegistry.getAnimation(key);
    }

    public @Nullable Ride getRide() {
        return ride;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("key", key.toString());
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
