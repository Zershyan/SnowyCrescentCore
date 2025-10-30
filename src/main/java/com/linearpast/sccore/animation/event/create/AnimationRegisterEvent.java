package com.linearpast.sccore.animation.event.create;

import com.linearpast.sccore.animation.data.Animation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;

import java.util.HashMap;
import java.util.Map;

/**
 * You can listen this event to invite an animation <br>
 * It is only useful in server
 */
public class AnimationRegisterEvent extends Event {
    private final Map<ResourceLocation, Animation> animations = new HashMap<>();

    public Map<ResourceLocation, Animation> getAnimations() {
        return new HashMap<>(animations);
    }

    public void registerAnimation(ResourceLocation location, Animation animation) {
        animations.put(location, animation);
    }
}
