package com.linearpast.sccore.animation.event.client;

import com.linearpast.sccore.animation.capability.AnimationDataCapability;
import com.linearpast.sccore.animation.capability.inter.IAnimationCapability;
import com.linearpast.sccore.animation.data.AnimationData;
import com.linearpast.sccore.animation.utils.AnimationUtils;
import dev.kosmx.playerAnim.core.util.MathHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class CameraModify {
    private static float targetYaw = 0.0F;
    private static float targetPitch = 0.0F;
    private static float targetRoll = 0.0F;
    private static float currentYaw = 0.0F;
    private static float currentPitch = 0.0F;
    private static float currentRoll = 0.0F;

    @SubscribeEvent
    public static void changeCameraView(ViewportEvent.ComputeCameraAngles event){
        Camera camera = event.getCamera();
        Entity entity = camera.getEntity();
        Minecraft minecraft = Minecraft.getInstance();
        if (entity == minecraft.player && minecraft.options.getCameraType().isFirstPerson()) {
            LocalPlayer player = minecraft.player;
            if (player != null) {
                IAnimationCapability data = AnimationDataCapability.getCapability(player).orElse(null);
                if(data == null) return;
                AnimationData animation = AnimationUtils.getPredicateAnimationData(animationData -> {
                    float camYaw = animationData.getCamYaw();
                    float camPitch = animationData.getCamPitch();
                    float camRoll = animationData.getCamRoll();
                    return !(camYaw == camPitch && camPitch == camRoll && camYaw == 0);
                });

                if(animation != null) {
                    targetPitch = animation.getCamPitch();
                    targetYaw = animation.getCamYaw();
                    targetRoll = animation.getCamRoll();
                } else {
                    targetYaw = 0.0F;
                    targetPitch = 0.0F;
                    targetRoll = 0.0F;
                }
            }
            float var3 = Minecraft.getInstance().getDeltaFrameTime();
            float var4 = var3 / 5.0F;
            if (var4 == 0.0F) {
                var4 = 0.0022857143F;
            }

            currentPitch = MathHelper.lerp(var4, currentPitch, targetPitch);
            currentYaw = MathHelper.lerp(var4, currentYaw, targetYaw);
            currentRoll = MathHelper.lerp(var4, currentRoll, targetRoll);
            event.setPitch(event.getPitch() + currentPitch);
            event.setYaw(event.getYaw() + currentYaw);
            event.setRoll(event.getRoll() + currentRoll);
        }
    }

    @SubscribeEvent
    public static void changeCameraPos(ViewportEvent.ComputeCameraAngles event) {
        Camera camera = event.getCamera();
        Entity entity = camera.getEntity();
        Minecraft minecraft = Minecraft.getInstance();
        if(entity == minecraft.player && minecraft.options.getCameraType().isFirstPerson()) {
            LocalPlayer player = minecraft.player;

            AnimationData animation = AnimationUtils.getPredicateAnimationData(animationData ->
                    !animationData.getCamPosOffset().multiply(1,0,1).equals(Vec3.ZERO)
            );
            if(animation != null) {
                Vec3 camPosOffset = animation.getCamPosOffset();
                if(animation.isCamPosOffsetRelative()) {
                    float yRot = player.yBodyRotO + (player.yBodyRot - player.yBodyRotO) * minecraft.getPartialTick();
                    float bodyAngel = -(yRot + 90) * ((float)Math.PI / 180F);
                    double cos = Math.cos(bodyAngel);
                    double sin = Math.sin(bodyAngel);
                    double x = camPosOffset.x;
                    double z = camPosOffset.z;
                    camera.position = player.getEyePosition(minecraft.getPartialTick()).add(
                            sin * x + cos * z,
                            camPosOffset.y,
                            cos * x - sin * z
                    );
                } else {
                    if(camPosOffset.distanceToSqr(Vec3.ZERO) <= 10.0 * 10.0 * 10.0) {
                        camera.position = player.getEyePosition(minecraft.getPartialTick())
                                .add(camPosOffset);
                    }
                }
            }
        }
    }
}
