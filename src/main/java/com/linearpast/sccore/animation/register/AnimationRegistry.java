package com.linearpast.sccore.animation.register;

import com.google.gson.JsonElement;
import com.linearpast.sccore.SnowyCrescentCore;
import com.linearpast.sccore.animation.capability.AnimationDataCapability;
import com.linearpast.sccore.animation.capability.RawAnimationDataCapability;
import com.linearpast.sccore.animation.capability.inter.IAnimationCapability;
import com.linearpast.sccore.animation.data.GenericAnimationData;
import com.linearpast.sccore.animation.data.RawAnimationData;
import com.linearpast.sccore.animation.data.util.AnimJson;
import com.linearpast.sccore.animation.data.util.AnimLayerJson;
import com.linearpast.sccore.animation.event.create.AnimationRegisterEvent;
import com.linearpast.sccore.animation.mixin.IMixinPlayerAnimationFactoryHolder;
import com.linearpast.sccore.animation.network.toclient.AnimationClientStatusPacket;
import com.linearpast.sccore.animation.network.toclient.AnimationJsonPacket;
import com.linearpast.sccore.animation.service.RawAnimationService;
import com.linearpast.sccore.animation.utils.FileUtils;
import com.linearpast.sccore.core.ModChannel;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class AnimationRegistry {
    private static final Map<ResourceLocation, GenericAnimationData> animations = new HashMap<>();
    private static final Map<ResourceLocation, Integer> layers = new HashMap<>();

    public static Map<ResourceLocation, GenericAnimationData> getAnimations() {
        return Map.copyOf(animations);
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
        Path dataPackPath = event.getServer().getWorldPath(LevelResource.DATAPACK_DIR);
        Path animationPath = dataPackPath.resolve("animation");
        if (!Files.exists(animationPath)) {
            try {
                Files.createDirectories(animationPath);
            } catch (IOException e) { return; }
        }

        FileUtils.safeUnzip(dataPackPath.resolve("animation.zip").toString(), animationPath.toAbsolutePath().toString());
        Set<Path> animZipPaths = FileUtils.getAllFile(
                dataPackPath.resolve("animation"),
                path -> path.toString().endsWith(".anim.zip")
        );
        Set<Path> layerZipPaths = FileUtils.getAllFile(
                dataPackPath.resolve("animation"),
                path -> path.toString().endsWith(".layer.zip")
        );
        for (Path zipPath : animZipPaths) {
            FileUtils.safeUnzip(zipPath.toString(), animationPath.toAbsolutePath().toString());
        }
        for (Path zipPath : layerZipPaths) {
            FileUtils.safeUnzip(zipPath.toString(), animationPath.toAbsolutePath().toString());
        }

        Set<Path> animPaths = FileUtils.getAllFile(
                dataPackPath.resolve("animation"),
                path -> path.toString().endsWith(".anim.json")
        );
        Set<Path> layerPaths = FileUtils.getAllFile(
                dataPackPath.resolve("animation"),
                path -> path.getFileName().toString().equals("animation.layer.json")
        );
        Set<GenericAnimationData> animationsSet = new HashSet<>();
        Map<ResourceLocation, Integer> layersMap = new HashMap<>();
        for (Path path : animPaths) {
            try {
                AnimJson.Reader reader = AnimJson.Reader.stream(path);
                GenericAnimationData anim = reader.parse();
                animationsSet.add(anim);
            } catch (Exception ignored) {
                SnowyCrescentCore.log.error("Failed to parse animation JSON: {}", path.toString());
            }
        }
        for (Path path : layerPaths) {
            try {
                AnimLayerJson.Reader reader = AnimLayerJson.Reader.stream(path);
                Map<ResourceLocation, Integer> parse = reader.parse();
                layersMap.putAll(parse);
            } catch (Exception ignored) {
                SnowyCrescentCore.log.error("Failed to parse layer JSON: {}", path.toString());
            }
        }

        animations.clear();
        AnimationRegisterEvent.Animation animationRegisterEvent = new AnimationRegisterEvent.Animation();
        MinecraftForge.EVENT_BUS.post(animationRegisterEvent);
        Map<ResourceLocation, GenericAnimationData> animationMap = animationRegisterEvent.getAnimations();
        animations.putAll(animationMap);
        animations.putAll(animationsSet.stream().collect(Collectors.toMap(GenericAnimationData::getKey, animation -> animation)));

        layers.clear();
        AnimationRegisterEvent.Layer layerRegisterEvent = new AnimationRegisterEvent.Layer();
        MinecraftForge.EVENT_BUS.post(layerRegisterEvent);
        Map<ResourceLocation, Integer> layerMap = layerRegisterEvent.getLayers();
        layers.putAll(layerMap);
        layers.putAll(layersMap);
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            MinecraftServer server = serverPlayer.getServer();
            if(server == null) return;
            Path dataPackPath = server.getWorldPath(LevelResource.DATAPACK_DIR);
            Path animationPath = dataPackPath.resolve("animation");
            if (!Files.exists(animationPath)) {
                try {Files.createDirectories(animationPath);}
                catch (IOException e) { return; }
            }
            ModChannel.sendToPlayer(new AnimationClientStatusPacket(AnimationClientStatusPacket.Status.ANIM_CACHE_CLEAR), serverPlayer);
            for (GenericAnimationData value : animations.values()) {
                JsonElement json = AnimJson.Writer.stream(value).toJson();
                String string = json.toString();
                ModChannel.sendToPlayer(new AnimationJsonPacket(string, false), serverPlayer);
            }
            ModChannel.sendToPlayer(new AnimationClientStatusPacket(AnimationClientStatusPacket.Status.ANIM_REGISTER), serverPlayer);
            ModChannel.sendToPlayer(new AnimationClientStatusPacket(AnimationClientStatusPacket.Status.LAYER_CACHE_CLEAR), serverPlayer);
            Map<String, JsonElement> jsonElementMap = AnimLayerJson.Writer.stream(animationPath).allToJson();
            jsonElementMap.forEach((key, value) ->
                    ModChannel.sendToPlayer(new AnimationJsonPacket(value.toString(), true), serverPlayer)
            );
            ModChannel.sendToPlayer(new AnimationClientStatusPacket(AnimationClientStatusPacket.Status.LAYER_REGISTER), serverPlayer);
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

        @SuppressWarnings({"JavaReflectionMemberAccess", "UnstableApiUsage", "unchecked"})
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
                                if(data == null) continue;
                                RawAnimationDataCapability rawData = RawAnimationDataCapability.getCapability(player).orElse(null);
                                if(rawData == null) continue;
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
                    } catch (Exception ignored) {}
                }
            }
        }

        private static IAnimation registerPlayerAnimation(AbstractClientPlayer player) {
            return new ModifierLayer<>();
        }
    }
}
