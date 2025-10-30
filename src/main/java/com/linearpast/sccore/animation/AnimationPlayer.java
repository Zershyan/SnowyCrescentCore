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
import com.linearpast.sccore.core.datagen.ModLang;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public class AnimationPlayer {

    public static void requestAnimationToServer(@Nullable AbstractClientPlayer player, ResourceLocation layer, @Nullable ResourceLocation animation) {
        UUID uuid = null;
        if(player != null) uuid = player.getUUID();
        ModChannel.sendToServer(new PlayAnimationRequestPacket(uuid, layer, animation));
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
            return AnimationRideEntity.create(serverPlayer, layer, animation, force);
        } else {
            serverPlayer.unRide();
            AnimationDataCapability.getCapability(serverPlayer).ifPresent(IAnimationCapability::removeRiderAnimation);
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

    public static void syncAnimation(ServerPlayer player, ServerPlayer target) {
        ModChannel.sendToPlayer(new SyncAnimationPacket(player.getUUID(), target.getUUID()), player);
        ModChannel.sendToPlayer(new SyncAnimationPacket(player.getUUID(), target.getUUID()), target);
    }

    @SuppressWarnings("unchecked")
    @OnlyIn(Dist.CLIENT)
    public static void syncAnimation(AbstractClientPlayer clientPlayer, AbstractClientPlayer target) {
        try {
            IAnimationCapability clientPlayerData = AnimationDataCapability.getCapability(clientPlayer).orElse(null);
            if(clientPlayerData == null) return;
            IAnimationCapability targetData = AnimationDataCapability.getCapability(target).orElse(null);
            if(targetData == null) return;
            ResourceLocation clientPlayerLayer = clientPlayerData.getRiderAnimLayer();
            ResourceLocation targetLayer = targetData.getRiderAnimLayer();
            if(clientPlayerLayer == null || targetLayer == null) return;
            ModifierLayer<IAnimation> modifierLayer = (ModifierLayer<IAnimation>) PlayerAnimationAccess
                    .getPlayerAssociatedData(clientPlayer).get(clientPlayerLayer);
            ModifierLayer<IAnimation> targetModifierLayer = (ModifierLayer<IAnimation>) PlayerAnimationAccess
                    .getPlayerAssociatedData(target).get(targetLayer);
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
    public static void playAnimation(@Nullable AbstractClientPlayer clientPlayer, ResourceLocation layer, @Nullable ResourceLocation animation) {
        try {
            LocalPlayer localPlayer = Minecraft.getInstance().player;
            if(clientPlayer == null) clientPlayer = localPlayer;
            if(clientPlayer == null) return;
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
            if(keyframeAnimation == null) {
                if(localPlayer == null) return;
                localPlayer.sendSystemMessage(Component.translatable(
                        ModLang.TranslatableMessage.UNKNOWN_ANIMATION.getKey(),
                        animation.toString()
                ).withStyle(ChatFormatting.RED));
                return;
            };
            Objects.requireNonNull(modifierLayer).replaceAnimationWithFade(
                    AbstractFadeModifier.standardFadeIn(3, Ease.INOUTSINE),
                    new KeyframeAnimationPlayer(keyframeAnimation)
            );
        }catch (Exception e) {
            SnowyCrescentCore.log.error("Failed to play animation : {}", animation, e);
        }
    }
}
