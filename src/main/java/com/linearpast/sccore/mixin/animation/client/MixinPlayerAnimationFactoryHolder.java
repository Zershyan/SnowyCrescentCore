package com.linearpast.sccore.mixin.animation.client;

import com.linearpast.sccore.animation.mixin.IMixinPlayerAnimationFactoryHolder;
import dev.kosmx.playerAnim.api.layered.AnimationStack;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Mixin(PlayerAnimationFactory.FactoryHolder.class)
public class MixinPlayerAnimationFactoryHolder implements IMixinPlayerAnimationFactoryHolder {
    @Unique
    @Final
    private static List<Function<AbstractClientPlayer, DataHolder>> sccore$factories = new ArrayList<>();

    @Inject(
            method = "prepareAnimations",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void prepareAnimations(AbstractClientPlayer player, AnimationStack playerStack, Map<ResourceLocation, IAnimation> animationMap, CallbackInfo ci) {
        for (Function<AbstractClientPlayer, DataHolder> factory: sccore$factories) {
            DataHolder dataHolder = factory.apply(player);
            if (dataHolder != null) {
                playerStack.addAnimLayer(dataHolder.priority(), dataHolder.animation());
                if (dataHolder.id() != null) {
                    animationMap.put(dataHolder.id(), dataHolder.animation());
                }
            }
        }
        ci.cancel();
    }

    @Inject(
            method = "registerFactory",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void registerFactory(ResourceLocation id, int priority, PlayerAnimationFactory factory, CallbackInfo ci) {
        sccore$factories.add(player -> Optional.ofNullable(factory.invoke(player)).map(animation -> new DataHolder(id, priority, animation)).orElse(null));
        ci.cancel();
    }

    @Unique
    public void sccore$clearAnimations() {
        sccore$factories.clear();
    }
}
