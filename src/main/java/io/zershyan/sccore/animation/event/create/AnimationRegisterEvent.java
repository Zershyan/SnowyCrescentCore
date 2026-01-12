package io.zershyan.sccore.animation.event.create;

import io.zershyan.sccore.animation.data.GenericAnimationData;
import io.zershyan.sccore.animation.data.RawAnimationData;
import io.zershyan.sccore.animation.register.RawAnimationRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;

import java.util.HashMap;
import java.util.Map;

public class AnimationRegisterEvent extends Event {
    public static class Layer extends AnimationRegisterEvent {
        private final Map<ResourceLocation, Integer> layers = new HashMap<>();

        public Map<ResourceLocation, Integer> getLayers() {
            return layers;
        }

        public void registerLayer(ResourceLocation key, Integer value) {
            layers.put(key, value);
        }
    }

    public static class Animation extends AnimationRegisterEvent {
        private final Map<ResourceLocation, GenericAnimationData> animations = new HashMap<>();

        public Map<ResourceLocation, GenericAnimationData> getAnimations() {
            return new HashMap<>(animations);
        }

        public void registerAnimation(ResourceLocation location, GenericAnimationData animation) {
            animations.put(location, animation);
        }
    }

    public static class RawAnimation extends AnimationRegisterEvent {
        private final Map<ResourceLocation, RawAnimationData> animations = new HashMap<>();

        public Map<ResourceLocation, RawAnimationData> getAnimations() {
            return new HashMap<>(animations);
        }

        public void registerAnimation(ResourceLocation location, RawAnimationData animation) {
            if (RawAnimationRegistry.validateLocation(location)) {
                animations.put(location, animation);
            }
        }
    }
}
