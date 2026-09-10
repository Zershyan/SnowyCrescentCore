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
        if (post.isCanceled()) cir.setReturnValue(new CompletableFuture<>());
    }

    @Inject(
            method = "onResourceLoadFinished",
            at = @At("HEAD")
    )
    public void resourceReloadFinish(CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new ResourceLoadEvent.Post());
    }
}
