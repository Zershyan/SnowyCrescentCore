package com.linearpast.sccore.mixin.animation.client;

import com.linearpast.sccore.animation.data.GenericAnimationData;
import com.linearpast.sccore.animation.service.AnimationService;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class MixinHumanoidModel<T extends LivingEntity> extends AgeableListModel<T> implements ArmedModel, HeadedModel {
    @Shadow @Final public ModelPart head;

    @Inject(
            method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;copyFrom(Lnet/minecraft/client/model/geom/ModelPart;)V")
    )
    private void modifyHeadRot(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch, CallbackInfo ci){
        if(pEntity instanceof Player player){
            GenericAnimationData.LyingType lyingType = AnimationService.INSTANCE.getSideView(player);
            if(lyingType != null) {
                float pitch = pHeadPitch - 90.0f;
                float yaw = pNetHeadYaw * -1.0f;
                switch (lyingType) {
                    case LEFT: {
                        pitch *= -1.0f;
                        yaw *= -1.0f;
                    }
                    case RIGHT: {
                        this.head.yRot = pitch * 0.017453292F;
                        this.head.xRot = yaw * 0.017453292F;
                    }
                }
            }
        }
    }
}
