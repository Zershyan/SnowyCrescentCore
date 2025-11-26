package com.linearpast.sccore.mixin.animation.client;

import com.linearpast.sccore.animation.data.GenericAnimationData;
import com.linearpast.sccore.animation.helper.AnimationHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @Shadow public abstract void setXRot(float pXRot);

    @Shadow private float xRot;

    @Shadow public abstract void setYRot(float pYRot);

    @Shadow private float yRot;

    @Shadow public abstract float getXRot();

    @Shadow public float xRotO;

    @Shadow public float yRotO;

    @Inject(
            method = "turn",
            at = {@At(value = "HEAD")},
            cancellable = true
    )
    private void turnPosePlayer(double pYRot, double pXRot, CallbackInfo ci) {
        Entity self = Entity.class.cast(this);
        if(self instanceof Player player){
            GenericAnimationData.LyingType lyingType = AnimationHelper.INSTANCE.getSideView(player);
            if(lyingType != null && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
                float f = (float)pXRot * 0.15F;
                float f1 = (float)pYRot * 0.15F;
                switch (lyingType) {
                    case LEFT -> {
                        this.setXRot(this.xRot + f1 * -1.0f);
                        this.setYRot(this.yRot + f);
                    }
                    case RIGHT -> {
                        this.setXRot(this.xRot + f1);
                        this.setYRot(this.yRot + f * -1.0f);
                    }
                }
                this.setXRot(Mth.clamp(this.getXRot(), 0.0f, 90.0f));
                this.xRotO = this.xRot;
                this.yRotO = this.yRot;
                ci.cancel();
            }
        }
    }
}
