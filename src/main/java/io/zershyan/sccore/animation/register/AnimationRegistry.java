package io.zershyan.sccore.animation.register;

import dev.kosmx.playerAnim.api.layered.AnimationStack;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.core.util.Pair;
import dev.kosmx.playerAnim.impl.animation.AnimationApplier;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import io.zershyan.sccore.SnowyCrescentCore;
import io.zershyan.sccore.animation.AnimationApi;
import io.zershyan.sccore.animation.capability.AnimationDataCapability;
import io.zershyan.sccore.animation.capability.RawAnimationDataCapability;
import io.zershyan.sccore.animation.capability.inter.IAnimationCapability;
import io.zershyan.sccore.animation.data.GenericAnimationData;
import io.zershyan.sccore.animation.data.RawAnimationData;
import io.zershyan.sccore.animation.mixin.IMixinPlayerAnimationFactoryHolder;
import io.zershyan.sccore.animation.network.toclient.AnimationClientStatusPacket;
import io.zershyan.sccore.animation.service.RawAnimationService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class AnimationRegistry {
    private static final Map<ResourceLocation, GenericAnimationData> animations = new HashMap<>();
    private static final Map<ResourceLocation, Integer> layers = new HashMap<>();

    public static Map<ResourceLocation, GenericAnimationData> getAnimations() {
        return Map.copyOf(animations);
    }

    public static void resetAnimations(Map<ResourceLocation, GenericAnimationData> animations) {
        AnimationRegistry.animations.clear();
        AnimationRegistry.animations.putAll(animations);
    }

    public static void resetLayers(Map<ResourceLocation, Integer> layers) {
        AnimationRegistry.layers.clear();
        AnimationRegistry.layers.putAll(layers);
    }

    public static Map<ResourceLocation, Integer> getLayers() {
        return Map.copyOf(layers);
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerAnimations(Map<ResourceLocation, GenericAnimationData> animationMap) {
        animations.clear();
        animations.putAll(animationMap);
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerLayers(Map<ResourceLocation, Integer> layerMap) {
        layers.clear();
        layers.putAll(layerMap);
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        AnimationApi.getRegistryHelper().server(event.getServer()).reloadAnimations();
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            AnimationApi.getRegistryHelper().server(serverPlayer.server)
                    .syncPlayerAnimations(serverPlayer);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class ClientCache {
        public static boolean isAnimationRegistered = false;
        private static final Map<ResourceLocation, GenericAnimationData> animationsCache = new HashMap<>();
        private static final Map<ResourceLocation, Integer> layersCache = new HashMap<>();
        private static final Map<UUID, Map<ResourceLocation, IAnimation>> modifierLayers = new HashMap<>();

        public static void cacheAddAnimation(ResourceLocation location, GenericAnimationData animation) {
            animationsCache.put(location, animation);
        }

        public static void cacheAddAnimationLayer(ResourceLocation location, Integer priority) {
            layersCache.put(location, priority);
        }

        public static void animationStatusUpdate(AnimationClientStatusPacket.Status status) {
            switch (status) {
                case ANIM_CACHE_CLEAR -> animationsCache.clear();
                case LAYER_CACHE_CLEAR -> {
                    ((IMixinPlayerAnimationFactoryHolder)(PlayerAnimationFactory.ANIMATION_DATA_FACTORY))
                            .sccore$clearAnimations();
                    layersCache.clear();
                }
                case ANIM_REGISTER -> {
                    isAnimationRegistered = true;
                    registerAnimations(animationsCache);
                    RawAnimationRegistry.triggerRegistry();
                }
                case LAYER_REGISTER -> {
                    registerLayers(layersCache);
                    layersCache.forEach((key, value) -> PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                            key, value, player -> {
                                if(Minecraft.getInstance().player == null) return ClientCache.registerPlayerAnimation(player);
                                Map<ResourceLocation, IAnimation> animationMap = modifierLayers.getOrDefault(player.getUUID(), new HashMap<>());
                                if(animationMap.containsKey(key)) return animationMap.get(key);
                                IAnimation iAnimation = ClientCache.registerPlayerAnimation(player);
                                animationMap.put(key, iAnimation);
                                modifierLayers.put(player.getUUID(), animationMap);
                                return iAnimation;
                            })
                    );
                    ClientLevel level = Minecraft.getInstance().level;
                    if(level == null) {
                        SnowyCrescentCore.log.error("{} : Level is null", ClientCache.class.getName());
                        return;
                    }
                    try {
                        for (AbstractClientPlayer player : level.players()) {
                            reflectAnimationCore(player);
                        }
                    } catch (Exception ignored) {}
                }
            }
        }

        private static IAnimation registerPlayerAnimation(AbstractClientPlayer player) {
            return new ModifierLayer<>();
        }

        @SuppressWarnings({"JavaReflectionMemberAccess", "UnstableApiUsage", "unchecked"})
        private static void reflectAnimationCore(AbstractClientPlayer player) {
            try {
                Class<?> playerClass = Player.class;
                Field animationStackField = playerClass.getDeclaredField("animationStack");
                animationStackField.setAccessible(true);
                Method createAnimationStack = playerClass.getDeclaredMethod("createAnimationStack");
                createAnimationStack.setAccessible(true);
                AnimationStack newAnimationStack = (AnimationStack) createAnimationStack.invoke(player);
                AnimationStack oldAnimationStack = (AnimationStack) animationStackField.get(player);
                Field layersField = AnimationStack.class.getDeclaredField("layers");
                layersField.setAccessible(true);
                ArrayList<Pair<Integer, IAnimation>> oldArrayList = new ArrayList<>((ArrayList<Pair<Integer, IAnimation>>) layersField.get(oldAnimationStack));
                ArrayList<Pair<Integer, IAnimation>> newArrayList = new ArrayList<>((ArrayList<Pair<Integer, IAnimation>>) layersField.get(newAnimationStack));
                ArrayList<Pair<Integer, IAnimation>> result = new ArrayList<>();
                for (Pair<Integer, IAnimation> oldAnimationPair : List.copyOf(oldArrayList)) {
                    for (Pair<Integer, IAnimation> newAnimationPair : List.copyOf(newArrayList)) {
                        if(Objects.equals(oldAnimationPair.getLeft(), newAnimationPair.getLeft())) {
                            KeyframeAnimation oldData = Optional.ofNullable((KeyframeAnimationPlayer) ((ModifierLayer<?>) oldAnimationPair.getRight()).getAnimation())
                                    .map(KeyframeAnimationPlayer::getData).orElse(null);
                            KeyframeAnimation newData = Optional.ofNullable((KeyframeAnimationPlayer) ((ModifierLayer<?>) newAnimationPair.getRight()).getAnimation())
                                    .map(KeyframeAnimationPlayer::getData).orElse(null);
                            if(Objects.equals(oldData, newData)) oldArrayList.remove(oldAnimationPair);
                        }
                    }
                }
                result.addAll(oldArrayList);
                result.addAll(newArrayList);

                layersField.set(newAnimationStack, result);
                animationStackField.set(player, newAnimationStack);
                Field animationApplierField = playerClass.getDeclaredField("animationApplier");
                animationApplierField.setAccessible(true);
                animationApplierField.set(player, new AnimationApplier(newAnimationStack));
                IAnimationCapability data = AnimationDataCapability.getCapability(player).orElse(null);
                if(data == null) return;
                RawAnimationDataCapability rawData = RawAnimationDataCapability.getCapability(player).orElse(null);
                if(rawData == null) return;
                Map<ResourceLocation, ResourceLocation> dataAnimations = new HashMap<>();
                dataAnimations.putAll(data.getAnimations());
                dataAnimations.putAll(rawData.getAnimations());
                ResourceLocation riderAnimLayer = data.getRiderAnimLayer();
                if(riderAnimLayer != null) {
                    dataAnimations.put(riderAnimLayer, data.getRiderAnimation());
                }
                for (ResourceLocation location : dataAnimations.keySet()) {
                    ModifierLayer<IAnimation> modifierLayer = (ModifierLayer<IAnimation>) PlayerAnimationAccess
                            .getPlayerAssociatedData(player).get(location);
                    if(modifierLayer == null) continue;
                    KeyframeAnimation keyframeAnimation;
                    ResourceLocation animationLocation = dataAnimations.get(location);
                    GenericAnimationData anim = animations.get(animationLocation);
                    if(anim == null) {
                        RawAnimationData rawAnim = RawAnimationService.INSTANCE.getAnimation(animationLocation);
                        if(rawAnim == null) return;
                        keyframeAnimation = rawAnim.getAnimation();
                    } else keyframeAnimation = anim.getAnimation();
                    if(keyframeAnimation == null) continue;
                    modifierLayer.replaceAnimationWithFade(
                            AbstractFadeModifier.standardFadeIn(3, Ease.INOUTSINE),
                            new KeyframeAnimationPlayer(keyframeAnimation)
                    );
                }
            }catch (Exception e){
                SnowyCrescentCore.log.error("Failed to register on {} animation layer: {}", player, e.getMessage(), e);
            }
        }
    }
}
