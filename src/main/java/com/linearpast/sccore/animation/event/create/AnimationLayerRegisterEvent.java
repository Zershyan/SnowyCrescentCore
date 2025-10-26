package com.linearpast.sccore.animation.event.create;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * You can listen this event to register an animation layer <br>
 * Generally, the static function is better.
 * @see com.linearpast.sccore.animation.AnimationUtils#registerAnimationLayer
 */
public class AnimationLayerRegisterEvent extends Event implements IModBusEvent {
    private final Map<ResourceLocation, Integer> layers = new HashMap<>();

    public AnimationLayerRegisterEvent() {

    }

    public Map<ResourceLocation, Integer> getLayers() {
        return layers;
    }

    public void putLayer(ResourceLocation key, Integer value) {
        layers.put(key, value);
    }
}
