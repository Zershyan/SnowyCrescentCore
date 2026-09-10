package io.zershyan.sccore.mixin.playeranimator.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalDoubleRef;
import io.zershyan.sccore.compat.animation.data.camera.CameraData;
import io.zershyan.sccore.compat.animation.handler.client.CameraTransformStateHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class MixinEntity {

    @Inject(
            method = "turn",
            at = @At("HEAD")
    )
    private void rotateTurnByRoll(
            CallbackInfo ci,
            @Local(name = "yRot", ordinal = 0, argsOnly = true) LocalDoubleRef yRot,
            @Local(name = "xRot", ordinal = 1, argsOnly = true) LocalDoubleRef xRot
    ) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
        boolean firstPerson = mc.options.getCameraType().isFirstPerson();
        CameraData cache = CameraTransformStateHandler.getCache(player, firstPerson);
        if(cache == null) return;
        float roll = cache.eulerAngle().roll();
        if (roll == 0f) return;
        double dx = yRot.get();
        double dy = xRot.get();
        double rad = Math.toRadians(roll);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);
        double ndx = dx * cos - dy * sin;
        double ndy = dx * sin + dy * cos;
        yRot.set(ndx);
        xRot.set(ndy);
    }
}
