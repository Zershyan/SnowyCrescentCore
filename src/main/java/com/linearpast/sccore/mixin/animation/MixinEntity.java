package com.linearpast.sccore.mixin.animation;

import com.linearpast.sccore.animation.data.AnimationData;
import com.linearpast.sccore.animation.data.GenericAnimationData;
import com.linearpast.sccore.animation.service.AnimationService;
import com.linearpast.sccore.animation.utils.AnimationUtils;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @Shadow private AABB bb;

    @Shadow public abstract void setPose(Pose pPose);

    @ModifyReturnValue(
            method = "getEyeHeight()F",
            at = @At("RETURN")
    )
    private float redefinedEyeHeight(float original){
        Entity self = Entity.class.cast(this);
        if(self instanceof Player player){
            AnimationData data = AnimationUtils.getEyeModifierAnimationData(player);
            if(data instanceof GenericAnimationData){
                return player.getEyeHeight(Pose.STANDING) + (float) data.getCamPosOffset().y;
            }
        }
        return original;
    }

    @Inject(
            method = "getBoundingBox",
            at = @At(value = "RETURN"),
            cancellable = true
    )
    private void redefinedBoundingBox(CallbackInfoReturnable<AABB> cir){
        Entity self = Entity.class.cast(this);
        if(self instanceof Player player){
            float heightModifier = AnimationService.INSTANCE.getHeightModifier(player);
            if(heightModifier == 1.0f) return;
            double modifyHeight = 1.8f * heightModifier;
            cir.setReturnValue(this.bb.setMaxY(modifyHeight + this.bb.minY));
        }
    }

    @ModifyReturnValue(
            method = "getBbHeight",
            at = @At(value = "RETURN")
    )
    private float redefinedBbHeight(float original){
        Entity self = Entity.class.cast(this);
        if(self instanceof Player player){
            float heightModifier = AnimationService.INSTANCE.getHeightModifier(player);
            if(heightModifier == 1.0f) return original;
            return original * heightModifier;
        }
        return original;
    }



    @Inject(
            method = "getPose",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void redefinedPose(CallbackInfoReturnable<Pose> cir){
        Entity self = Entity.class.cast(this);
        if(self instanceof Player player){
            float heightModifier = AnimationService.INSTANCE.getHeightModifier(player);
            if(heightModifier == 1.0f) return;
            setPose(Pose.STANDING);
            cir.setReturnValue(Pose.STANDING);
        }
    }
}
