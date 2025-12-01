package com.linearpast.sccore.animation.utils;

import com.linearpast.sccore.SnowyCrescentCore;
import com.linearpast.sccore.animation.capability.AnimationDataCapability;
import com.linearpast.sccore.animation.capability.inter.IAnimationCapability;
import com.linearpast.sccore.animation.data.GenericAnimationData;
import com.linearpast.sccore.animation.data.RawAnimationData;
import com.linearpast.sccore.animation.mixin.IMixinKeyframeAnimationPlayer;
import com.linearpast.sccore.animation.register.AnimationRegistry;
import com.linearpast.sccore.animation.service.AnimationService;
import com.linearpast.sccore.animation.service.IAnimationService;
import com.linearpast.sccore.animation.service.RawAnimationService;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class AnimationUtils {
    /**
     * Test if layer exist animation which is not end. <br>
     * Only in dist client
     * @param player Target player
     * @param layer Target layer
     * @return True when animation is loop, or currentTick not larger than endTick
     */
    @SuppressWarnings("unchecked")
    @OnlyIn(Dist.CLIENT)
    public static boolean isClientAnimationNotEnd(AbstractClientPlayer player, @Nullable ResourceLocation layer) {
        return IAnimationService.ANIMATION_RUNNER.testLoadedAndCall(() -> {
            try {
                Set<ResourceLocation> resourceLocations = new HashSet<>();
                if(layer == null) resourceLocations.addAll(AnimationRegistry.getLayers().keySet());
                else resourceLocations.add(layer);
                for (ResourceLocation location : resourceLocations) {
                    ModifierLayer<IAnimation> animationModifierLayer = (ModifierLayer<IAnimation>) PlayerAnimationAccess
                            .getPlayerAssociatedData(player).get(location);
                    if(animationModifierLayer == null) continue;
                    KeyframeAnimationPlayer animation = (KeyframeAnimationPlayer) animationModifierLayer.getAnimation();
                    if(animation == null) return false;
                    int currentTick = animation.getCurrentTick();
                    boolean isLoop = animation.getData().isInfinite;
                    int endTick = animation.getData().endTick;
                    return isLoop || currentTick <= endTick;
                }
            } catch (Exception ignored) {}
            return false;
        });
    }

    /**
     * Test if layer exist animation which is not stop. <br>
     * Only in dist client
     * @param player Target player
     * @param layer Target layer
     * @return True when the currentTick not larger than stopTick
     */
    @SuppressWarnings("unchecked")
    @OnlyIn(Dist.CLIENT)
    public static boolean isClientAnimationStop(AbstractClientPlayer player, @Nullable ResourceLocation layer) {
        return IAnimationService.ANIMATION_RUNNER.testLoadedAndCall(() -> {
            try {
                Set<ResourceLocation> resourceLocations = new HashSet<>();
                if(layer == null) resourceLocations.addAll(AnimationRegistry.getLayers().keySet());
                else resourceLocations.add(layer);
                for (ResourceLocation location : resourceLocations) {
                    ModifierLayer<IAnimation> animationModifierLayer = (ModifierLayer<IAnimation>) PlayerAnimationAccess
                            .getPlayerAssociatedData(player).get(location);
                    if(animationModifierLayer == null) continue;
                    KeyframeAnimationPlayer animation = (KeyframeAnimationPlayer) animationModifierLayer.getAnimation();
                    if(animation == null) return false;
                    int currentTick = animation.getCurrentTick();
                    int stopTick = animation.getStopTick();
                    return currentTick > stopTick;
                }
            } catch (Exception ignored) {}
            return true;
        });
    }

    /**
     * Client sync animation
     * @param clientPlayer player
     * @param target target
     */
    @SuppressWarnings("unchecked")
    @OnlyIn(Dist.CLIENT)
    public static void syncAnimation(AbstractClientPlayer clientPlayer, AbstractClientPlayer target) {
        IAnimationCapability clientPlayerData = AnimationDataCapability.getCapability(clientPlayer).orElse(null);
        IAnimationCapability targetData = AnimationDataCapability.getCapability(target).orElse(null);
        if(clientPlayerData == null) return;
        if(targetData == null) return;
        ResourceLocation clientPlayerLayer = clientPlayerData.getRiderAnimLayer();
        ResourceLocation targetLayer = targetData.getRiderAnimLayer();
        try {
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

    /**
     * client remove animation
     * @param clientPlayer player
     * @param layer layer
     */
    @OnlyIn(Dist.CLIENT)
    public static void removeAnimation(@Nullable AbstractClientPlayer clientPlayer, ResourceLocation layer) {
        playAnimation(clientPlayer, layer, null);
    }

    /**
     * Client play animation
     * @param clientPlayer player
     * @param layer layer
     * @param animation animation
     */
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
            if(modifierLayer == null) return;
            KeyframeAnimation keyframeAnimation;
            GenericAnimationData anim = AnimationService.INSTANCE.getAnimation(animation);
            if(anim == null) {
                RawAnimationData rawAnim = RawAnimationService.INSTANCE.getAnimation(animation);
                if(rawAnim == null) return;
                keyframeAnimation = rawAnim.getAnimation();
            } else keyframeAnimation = anim.getAnimation();
            if(keyframeAnimation == null) {
                if(localPlayer == null) return;
                localPlayer.sendSystemMessage(Component.translatable(
                        ModLang.TranslatableMessage.ANIMATION_RESOURCE_NOT_FOUND.getKey(),
                        animation.toString()
                ).withStyle(ChatFormatting.RED));
                return;
            }
            modifierLayer.replaceAnimationWithFade(
                    AbstractFadeModifier.standardFadeIn(3, Ease.INOUTSINE),
                    new KeyframeAnimationPlayer(keyframeAnimation)
            );
        }catch (Exception e) {
            SnowyCrescentCore.log.error("Failed to play animation : {}", animation, e);
        }
    }
}
