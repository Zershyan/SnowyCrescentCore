package io.zershyan.sccore.mixin.playeranimator.common;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.zershyan.sccore.api.SCCoreApi;
import io.zershyan.sccore.compat.animation.core.ServerAnimationRegistry;
import io.zershyan.sccore.compat.animation.data.Animation;
import io.zershyan.sccore.compat.animation.handler.common.MovementAnimationTickHandler;
import io.zershyan.sccore.compat.animation.network.data.MovementAnimationTickData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

@Mixin(Entity.class)
public class MixinEntity {
    @Unique
    private static AABB sccore$getInterpolatedAABB(int tickCount, float partialTick, TreeMap<Integer, AABB> aabbDataTreeMap) {
        Map.Entry<Integer, AABB> exact = aabbDataTreeMap.floorEntry(tickCount);
        if (exact != null && exact.getKey() == tickCount) {
            return exact.getValue();
        }

        Map.Entry<Integer, AABB> lower = aabbDataTreeMap.floorEntry(tickCount);
        Map.Entry<Integer, AABB> higher = aabbDataTreeMap.ceilingEntry(tickCount);

        if (lower == null && higher == null) {
            return null;
        }
        if (lower == null) {
            return higher.getValue();
        }
        if (higher == null) {
            return lower.getValue();
        }

        int tickLow = lower.getKey();
        int tickHigh = higher.getKey();
        float t = ((tickCount - tickLow) + partialTick) / (tickHigh - tickLow);

        AABB aabbLow = lower.getValue();
        AABB aabbHigh = higher.getValue();

        return new AABB(
                Mth.lerp(t, (float) aabbLow.minX, (float) aabbHigh.minX),
                Mth.lerp(t, (float) aabbLow.minY, (float) aabbHigh.minY),
                Mth.lerp(t, (float) aabbLow.minZ, (float) aabbHigh.minZ),
                Mth.lerp(t, (float) aabbLow.maxX, (float) aabbHigh.maxX),
                Mth.lerp(t, (float) aabbLow.maxY, (float) aabbHigh.maxY),
                Mth.lerp(t, (float) aabbLow.maxZ, (float) aabbHigh.maxZ)
        );
    }

    @ModifyReturnValue(
            method = "getBoundingBox",
            at = @At(value = "RETURN")
    )
    private AABB redefinedBoundingBox(AABB original) {
        Entity self = Entity.class.cast(this);
        if (!(self instanceof Player player)) return original;
        MovementAnimationTickData data = MovementAnimationTickHandler.getData(player.getUUID());
        if (data == null) return original;
        Optional<ResourceLocation> resourceLocation = data.animationId();
        if (resourceLocation.isEmpty()) return original;
        Animation animation = ServerAnimationRegistry.commonGetAnimation(resourceLocation.get());
        if (animation == null) return original;
        float partialTick = SCCoreApi.tryGetPartialTick(player.level());
        AABB aabb = sccore$getInterpolatedAABB(data.currentTick(), partialTick, animation.aabbMovement().getMovementTree());
        if (aabb == null) return original;

        double minXOffset = aabb.minX;
        double minYOffset = aabb.minY;
        double minZOffset = aabb.minZ;
        double maxXOffset = aabb.maxX;
        double maxYOffset = aabb.maxY;
        double maxZOffset = aabb.maxZ;

        if (animation.aabbMovement().isRelative()) {
            float yaw = player.getPreciseBodyRotation(partialTick);
            double rad = Math.toRadians(-yaw);
            double sin = Math.sin(rad);
            double cos = Math.cos(rad);

            double newMinX = minXOffset * cos + minZOffset * sin;
            double newMaxX = maxXOffset * cos + maxZOffset * sin;
            double newMinZ = minZOffset * cos - minXOffset * sin;
            double newMaxZ = maxZOffset * cos - maxXOffset * sin;


            minXOffset = Math.min(newMinX, newMaxX);
            minZOffset = Math.min(newMinZ, newMaxZ);
            maxXOffset = Math.max(newMinX, newMaxX);
            maxZOffset = Math.max(newMinZ, newMaxZ);
        }

        return new AABB(
                original.maxX + maxXOffset,
                original.maxY + maxYOffset,
                original.maxZ + maxZOffset,
                original.minX + minXOffset,
                original.minY + minYOffset,
                original.minZ + minZOffset
        );
    }
}