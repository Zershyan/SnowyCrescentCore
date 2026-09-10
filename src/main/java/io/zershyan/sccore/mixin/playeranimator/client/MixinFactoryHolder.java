package io.zershyan.sccore.mixin.playeranimator.client;

import dev.kosmx.playerAnim.api.layered.AnimationStack;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import io.zershyan.sccore.compat.animation.imixin.IMixinFactoryHolder;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.function.Function;

@Mixin(PlayerAnimationFactory.FactoryHolder.class)
public class MixinFactoryHolder implements IMixinFactoryHolder {
    @Unique
    @Final
    private static List<Function<AbstractClientPlayer, DataHolder>> sccore$factories = new ArrayList<>();

    @Unique
    @Final
    private static Map<ResourceLocation, Function<AbstractClientPlayer, DataHolder>> sccore$cache = new HashMap<>();

    @Inject(
            method = "prepareAnimations",
            at = @At("HEAD"),
            cancellable = true
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
            cancellable = true
    )
    private void registerFactory(ResourceLocation id, int priority, PlayerAnimationFactory factory, CallbackInfo ci) {
        Function<AbstractClientPlayer, DataHolder> holderFunction = player -> Optional.ofNullable(
                factory.invoke(player)
        ).map(animation -> new DataHolder(
                id, priority, animation
        )).orElse(null);
        sccore$factories.add(holderFunction);
        sccore$cache.put(id, holderFunction);
        ci.cancel();
    }

    @Unique
    public void sccore$clearAnimations(Set<ResourceLocation> ids) {
        for (ResourceLocation id : ids) {
            Function<AbstractClientPlayer, DataHolder> remove = sccore$cache.remove(id);
            if (remove != null) sccore$factories.remove(remove);
        }
    }
}
