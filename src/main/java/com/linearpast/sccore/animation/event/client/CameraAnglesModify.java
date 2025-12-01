package com.linearpast.sccore.animation.event.client;

import com.linearpast.sccore.animation.capability.AnimationDataCapability;
import com.linearpast.sccore.animation.capability.inter.IAnimationCapability;
import com.linearpast.sccore.animation.data.GenericAnimationData;
import com.linearpast.sccore.animation.service.AnimationService;
import dev.kosmx.playerAnim.core.util.MathHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Comparator;

@OnlyIn(Dist.CLIENT)
public class CameraAnglesModify {
    private static float targetYaw = 0.0F;
    private static float targetPitch = 0.0F;
    private static float targetRoll = 0.0F;
    private static float currentYaw = 0.0F;
    private static float currentPitch = 0.0F;
    private static float currentRoll = 0.0F;

    @SubscribeEvent
    public static void changeCameraView(ViewportEvent.ComputeCameraAngles event){
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.options.getCameraType().isFirstPerson()) {
            LocalPlayer player = minecraft.player;
            if (player != null) {
                IAnimationCapability data = AnimationDataCapability.getCapability(player).orElse(null);
                if(data == null) return;
                GenericAnimationData animation = null;
                try {
                    animation = data.getAnimations().values().stream()
                            .map(AnimationService.INSTANCE::getAnimation)
                            .min(Comparator.comparingDouble(anim -> {
                                if (anim == null) return 1.0f;
                                return anim.getHeightModifier();
                            })).orElse(null);
                }catch (Exception ignored){}


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
}
