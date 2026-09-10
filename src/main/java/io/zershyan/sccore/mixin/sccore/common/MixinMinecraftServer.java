package io.zershyan.sccore.mixin.sccore.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.zershyan.sccore.api.events.client.ServerReloadEvent;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {

    @WrapOperation(
            method = "reloadResources",
            at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;thenAcceptAsync(Ljava/util/function/Consumer;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;")
    )
    public CompletableFuture<Void> reloadResourceFinish(CompletableFuture<Void> instance, Consumer<? extends MinecraftServer.ReloadableResources> action, Executor executor, Operation<CompletableFuture<Void>> original) {
        Consumer<? extends MinecraftServer.ReloadableResources> consumer = action.andThen(reloadableResources -> NeoForge.EVENT_BUS.post(new ServerReloadEvent.Post(MinecraftServer.class.cast(this))));
        return original.call(instance, consumer, executor);
    }

    @Inject(
            method = "reloadResources",
            at = @At("HEAD"),
            cancellable = true
    )
    public void reloadResourceStart(CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        ServerReloadEvent.Pre post = NeoForge.EVENT_BUS.post(new ServerReloadEvent.Pre(MinecraftServer.class.cast(this)));
        if (post.isCanceled()) cir.setReturnValue(new CompletableFuture<>());
    }
}
