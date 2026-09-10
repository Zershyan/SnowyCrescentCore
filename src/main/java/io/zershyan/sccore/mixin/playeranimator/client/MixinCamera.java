package io.zershyan.sccore.mixin.playeranimator.client;

import io.zershyan.sccore.compat.animation.data.camera.CameraData;
import io.zershyan.sccore.compat.animation.data.camera.EulerAngle;
import io.zershyan.sccore.compat.animation.data.camera.Vec2;
import io.zershyan.sccore.compat.animation.handler.client.CameraTransformStateHandler;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class MixinCamera {
    @Shadow
    protected abstract void setRotation(float yaw, float pitch, float roll);

    @Shadow
    protected abstract void setPosition(double x, double y, double z);

    @Inject(
            method = "setup",
            at = @At("TAIL")
    )
    public void afterSetup(BlockGetter level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTick, CallbackInfo ci) {
        Minecraft instance = Minecraft.getInstance();
        LocalPlayer player = instance.player;
        if (player == null) return;

        if (instance.options.getCameraType() == CameraType.THIRD_PERSON_FRONT) return;
        boolean firstPerson = instance.options.getCameraType().isFirstPerson();

        CameraData data = CameraTransformStateHandler.getAndStep(player, partialTick, firstPerson);
        boolean relativeEuler = CameraTransformStateHandler.relativeEuler(player, firstPerson);
        if (data == null) return;

        Vec2 relativeOffset = data.relativeOffset();
        Vec3 offset = data.offset();
        EulerAngle angle = data.eulerAngle();

        float viewXRot = player.getViewXRot(partialTick);
        float viewYRot = player.getViewYRot(partialTick);
        float yaw = angle.yaw() + (relativeEuler ? viewYRot : 0);
        float pitch = angle.pitch() + (relativeEuler ? viewXRot : 0);
        float roll = angle.roll();
        setRotation(yaw, pitch, roll);

        Vec3 pos = player.getEyePosition(partialTick);
        if (!relativeOffset.equals(Vec2.ZERO)) {
            float xRot = -player.getPreciseBodyRotation(partialTick);
            double radians = Math.toRadians(xRot);
            double x = Math.sin(radians) * relativeOffset.y() + Math.cos(radians) * relativeOffset.x();
            double z = Math.cos(radians) * relativeOffset.y() + Math.sin(radians) * relativeOffset.x();
            pos = pos.add(x, 0, z);
        }
        if (!offset.equals(Vec3.ZERO)) {
            pos = pos.add(offset);
        }
        if (pos.equals(player.getEyePosition(partialTick))) return;
        setPosition(pos.x, pos.y, pos.z);
    }
}
