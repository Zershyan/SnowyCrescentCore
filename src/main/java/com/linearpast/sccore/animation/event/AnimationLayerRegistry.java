package com.linearpast.sccore.animation.event;

import com.linearpast.sccore.animation.event.create.AnimationLayerRegisterEvent;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.HashMap;
import java.util.Map;

public class AnimationLayerRegistry {
    private static final Map<ResourceLocation, Integer> animLayers = new HashMap<>();

    public static void onClientSetup(FMLClientSetupEvent event) {
        onCommonSetUp(null);
        event.enqueueWork(() -> animLayers.forEach((location, integer) ->
                PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(location, integer,
                        AnimationLayerRegistry::registerPlayerAnimation
                )
        ));
    }

    public static void onCommonSetUp(FMLCommonSetupEvent event) {
        AnimationLayerRegisterEvent layerEvent = new AnimationLayerRegisterEvent();
        ModLoader.get().postEvent(layerEvent);
        animLayers.putAll(layerEvent.getLayers());
    }

    public static void registerPlayerAnimation(ResourceLocation location, int priority) {
        animLayers.put(location, priority);
    }

    public static Map<ResourceLocation, Integer> getAnimLayers() {
        return animLayers;
    }

    public static boolean isLayerPresent(ResourceLocation layer) {
        return animLayers.containsKey(layer);
    }

    private static IAnimation registerPlayerAnimation(AbstractClientPlayer player) {
        return new ModifierLayer<>();
    }
}
