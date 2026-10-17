package io.zershyan.sccore.mixin.sccore.client;

import io.zershyan.sccore.api.events.client.ResourceLoadEvent;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Inject(
            method = "reloadResourcePacks(ZLnet/minecraft/client/Minecraft$GameLoadCookie;)Ljava/util/concurrent/CompletableFuture;",
            at = @At("HEAD"),
            cancellable = true
    )
    public void resourceReloadStart(CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        ResourceLoadEvent.Pre post = NeoForge.EVENT_BUS.post(new ResourceLoadEvent.Pre());
        // 取消重载时返回已完成的 future：调用方继续执行，但不会真正重载资源
        if (post.isCanceled()) cir.setReturnValue(CompletableFuture.completedFuture(null));
    }

    @Inject(
            method = "onResourceLoadFinished",
            at = @At("HEAD")
    )
    public void resourceReloadFinish(CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new ResourceLoadEvent.Post());
    }
}
