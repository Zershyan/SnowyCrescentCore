package com.linearpast.sccore.animation.event.create;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;

import java.util.HashMap;
import java.util.Map;

/**
 * You can listen this event to invite an animation layer <br>
 * It is only useful in server
 */
public class AnimationLayerRegisterEvent extends Event {
    private final Map<ResourceLocation, Integer> layers = new HashMap<>();

    public Map<ResourceLocation, Integer> getLayers() {
        return layers;
    }

    public void registerLayer(ResourceLocation key, Integer value) {
        layers.put(key, value);
    }
}
