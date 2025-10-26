package com.linearpast.sccore.animation;

import com.linearpast.sccore.SnowyCrescentCore;
import com.linearpast.sccore.animation.capability.AnimationDataCapability;
import com.linearpast.sccore.animation.capability.inter.IAnimationCapability;
import com.linearpast.sccore.animation.data.Animation;
import com.linearpast.sccore.animation.entity.AnimationRideEntity;
import com.linearpast.sccore.animation.mixin.IMixinKeyframeAnimationPlayer;
import com.linearpast.sccore.animation.network.toclient.SyncAnimationPacket;
import com.linearpast.sccore.animation.network.toserver.PlayAnimationRequestPacket;
import com.linearpast.sccore.animation.network.toserver.PlayAnimationRidePacket;
import com.linearpast.sccore.core.ModChannel;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class AnimationPlayer {
    public static void requestAnimationToServer(ResourceLocation layer, @Nullable ResourceLocation animation) {
        ModChannel.sendToServer(new PlayAnimationRequestPacket(layer, animation));
    }

    public static boolean serverPlayAnimation(ServerPlayer serverPlayer, ResourceLocation layer, @Nullable ResourceLocation animation) {
        IAnimationCapability data = AnimationDataCapability.getCapability(serverPlayer).orElse(null);
        if(data == null) return false;
        if(animation != null) {
            return data.mergeAnimation(layer, animation);
        } else {
            return data.removeAnimation(layer);
        }
    }

    public static boolean playAnimationWithRide(ServerPlayer serverPlayer, ResourceLocation layer, @Nullable ResourceLocation animation, boolean force){
        if(animation != null) {
            IAnimationCapability data = AnimationDataCapability.getCapability(serverPlayer).orElse(null);
            if(data == null) return false;
            data.setRideAnimLayer(layer);
            return AnimationRideEntity.create(serverPlayer, layer, animation, force);
        } else {
            serverPlayer.unRide();
            return true;
        }
    }

    public static void requestAnimationRideToServer(ResourceLocation layer, @Nullable ResourceLocation animation, boolean force) {
        ModChannel.sendToServer(new PlayAnimationRidePacket(layer, animation, force));
    }

    public static void clearAnimation(ServerPlayer serverPlayer) {
        IAnimationCapability data = AnimationDataCapability.getCapability(serverPlayer).orElse(null);
        if(data == null) return;
        data.clearAnimations();
    }

    public static void syncAnimation(ServerPlayer player, ServerPlayer target, ResourceLocation layer) {
        ModChannel.sendToPlayer(new SyncAnimationPacket(player.getUUID(), target.getUUID(), layer), player);
        ModChannel.sendToPlayer(new SyncAnimationPacket(player.getUUID(), target.getUUID(), layer), target);
    }

    @SuppressWarnings("unchecked")
    @OnlyIn(Dist.CLIENT)
    public static void syncAnimation(AbstractClientPlayer clientPlayer, AbstractClientPlayer target, ResourceLocation layer) {
        try {
            ModifierLayer<IAnimation> modifierLayer = (ModifierLayer<IAnimation>) PlayerAnimationAccess
                    .getPlayerAssociatedData(clientPlayer).get(layer);
            ModifierLayer<IAnimation> targetModifierLayer = (ModifierLayer<IAnimation>) PlayerAnimationAccess
                    .getPlayerAssociatedData(target).get(layer);
            if(modifierLayer == null || targetModifierLayer == null) return;
            IMixinKeyframeAnimationPlayer animation = (IMixinKeyframeAnimationPlayer) modifierLayer.getAnimation();
            KeyframeAnimationPlayer targetAnimation = (KeyframeAnimationPlayer) targetModifierLayer.getAnimation();
            if(animation == null || targetAnimation == null) return;
            int currentTick = targetAnimation.getCurrentTick();
            animation.sccore$setCurrentTick(currentTick);
        } catch (Exception ignored) {}
    }

    @SuppressWarnings("unchecked")
    @OnlyIn(Dist.CLIENT)
    public static void playAnimation(AbstractClientPlayer clientPlayer, ResourceLocation layer, @Nullable ResourceLocation animation) {
        try {
            ModifierLayer<IAnimation> modifierLayer = (ModifierLayer<IAnimation>) PlayerAnimationAccess
                    .getPlayerAssociatedData(clientPlayer).get(layer);
            if(animation == null) {
                if(modifierLayer != null) {
                    modifierLayer.replaceAnimationWithFade(
                            AbstractFadeModifier.standardFadeIn(3, Ease.INOUTSINE),
                            null
                    );
                }
                return;
            }
            Animation anim = AnimationUtils.getAnimation(animation);
            if(anim == null) return;
            if(modifierLayer == null) return;
            KeyframeAnimation keyframeAnimation = anim.getAnimation();
            if(keyframeAnimation == null) return;
            Objects.requireNonNull(modifierLayer).replaceAnimationWithFade(
                    AbstractFadeModifier.standardFadeIn(3, Ease.INOUTSINE),
                    new KeyframeAnimationPlayer(keyframeAnimation)
            );
        }catch (Exception e) {
            SnowyCrescentCore.log.error("Failed to play animation : {}", animation, e);
        }
    }
}
