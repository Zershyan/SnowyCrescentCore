package com.linearpast.sccore.animation.register;

import com.linearpast.sccore.SnowyCrescentCore;
import com.linearpast.sccore.animation.command.argument.AnimationArgument;
import com.linearpast.sccore.animation.data.RawAnimationData;
import com.linearpast.sccore.animation.event.create.AnimationRegisterEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;

import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class RawAnimationRegistry {
    private static final Map<ResourceLocation, RawAnimationData> animations = new HashMap<>();

    public static Map<ResourceLocation, RawAnimationData> getAnimations() {
        return Map.copyOf(animations);
    }

    private static void registerAnimations(Map<ResourceLocation, RawAnimationData> animationMap) {
        animations.clear();
        Map.copyOf(animationMap).keySet().forEach(location -> {
            if(!validateLocation(location)) animations.remove(location);
        });
        animations.putAll(animationMap);
        AnimationArgument.resetAnimationNames();
    }

    public static boolean register(ResourceLocation location, RawAnimationData rawAnimation) {
        if (validateLocation(location)) {
            animations.put(location, rawAnimation);
            return true;
        }
        return false;
    }

    public static void triggerRegistry() {
        resetAnimations();
        AnimationRegisterEvent.RawAnimation event = new AnimationRegisterEvent.RawAnimation();
        MinecraftForge.EVENT_BUS.post(event);
        registerAnimations(event.getAnimations());
    }

    private static void resetAnimations() {
        animations.clear();
    }

    public static boolean validateLocation(ResourceLocation location) {
        try {
            if(!AnimationRegistry.ClientCache.isAnimationRegistered)
                throw new RuntimeException("Server animation is not registered!");
            if(AnimationRegistry.getAnimations().containsKey(location))
                throw new RuntimeException("Duplicated animation on server: " + location);
            return true;
        } catch (RuntimeException e) {
            SnowyCrescentCore.log.error(e.getMessage(), e);
            return false;
        }
    }

}
