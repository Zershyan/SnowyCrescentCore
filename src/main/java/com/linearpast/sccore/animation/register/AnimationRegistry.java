package com.linearpast.sccore.animation.register;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
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
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;
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
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(AnimationDataManager.INSTANCE);
        event.addListener(LayerDataManager.INSTANCE);
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        // Load legacy animations from datapacks
        loadLegacyDataPackAnimations(event.getServer());

        // Load animations from registration events
        loadFromRegistrationEvents();

        SnowyCrescentCore.log.info("Animation loading completed. Total animations: {}, Total layers: {}",
                animations.size(), layers.size());
    }

    /**
     * Load legacy datapack animations from world/datapacks/animation directory
     */
    private static void loadLegacyDataPackAnimations(MinecraftServer server) {
        Path dataPackPath = server.getWorldPath(LevelResource.DATAPACK_DIR);
        Path animationPath = dataPackPath.resolve("animation");

        if (!Files.exists(animationPath)) {
            try {
                Files.createDirectories(animationPath);
            } catch (IOException e) {
                SnowyCrescentCore.log.error("Failed to create legacy animation directory", e);
                return;
            }
        }

        try {
            // Handle zip files
            if (Files.exists(dataPackPath.resolve("animation.zip"))) {
                FileUtils.safeUnzip(dataPackPath.resolve("animation.zip").toString(),
                        animationPath.toAbsolutePath().toString());
            }

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

            // Load animation JSON files
            Set<Path> animPaths = FileUtils.getAllFile(
                    dataPackPath.resolve("animation"),
                    path -> path.toString().endsWith(".anim.json")
            );

            Set<Path> layerPaths = FileUtils.getAllFile(
                    dataPackPath.resolve("animation"),
                    path -> path.getFileName().toString().equals("animation.layer.json")
            );

            for (Path path : animPaths) {
                try {
                    AnimJson.Reader reader = AnimJson.Reader.stream(path);
                    GenericAnimationData anim = reader.parse();
                    animations.put(anim.getKey(), anim);
                    SnowyCrescentCore.log.info("Loaded legacy animation: {} -> {}",
                            anim.getKey(), anim.getName());
                } catch (Exception e) {
                    SnowyCrescentCore.log.error("Failed to parse legacy animation JSON: {}", path, e);
                }
            }

            for (Path path : layerPaths) {
                try {
                    AnimLayerJson.Reader reader = AnimLayerJson.Reader.stream(path);
                    Map<ResourceLocation, Integer> parse = reader.parse();
                    layers.putAll(parse);
                    SnowyCrescentCore.log.info("Loaded {} legacy layer configurations from {}",
                            parse.size(), path);
                } catch (Exception e) {
                    SnowyCrescentCore.log.error("Failed to parse legacy layer JSON: {}", path, e);
                }
            }
        } catch (Exception e) {
            SnowyCrescentCore.log.error("Error loading legacy animations", e);
        }
    }

    /**
     * Load animations from registration events
     */
    private static void loadFromRegistrationEvents() {
        AnimationRegisterEvent.Animation animationRegisterEvent = new AnimationRegisterEvent.Animation();
        MinecraftForge.EVENT_BUS.post(animationRegisterEvent);
        Map<ResourceLocation, GenericAnimationData> animationMap = animationRegisterEvent.getAnimations();
        animations.putAll(animationMap);

        AnimationRegisterEvent.Layer layerRegisterEvent = new AnimationRegisterEvent.Layer();
        MinecraftForge.EVENT_BUS.post(layerRegisterEvent);
        Map<ResourceLocation, Integer> layerMap = layerRegisterEvent.getLayers();
        layers.putAll(layerMap);

        SnowyCrescentCore.log.info("Loaded {} animations and {} layers from registration events",
                animationMap.size(), layerMap.size());
    }

    /**
     * Animation data manager using SimpleJsonResourceReloadListener
     */
    public static class AnimationDataManager extends SimpleJsonResourceReloadListener {
        public static final AnimationDataManager INSTANCE = new AnimationDataManager();

        private AnimationDataManager() {
            super(new GsonBuilder()
                    .setPrettyPrinting()
                    .disableHtmlEscaping()
                    .create(), "scc_animations");
        }

        @Override
        protected void apply(@Nonnull Map<ResourceLocation, JsonElement> resources,
                             @Nonnull ResourceManager resourceManager,
                             @Nonnull ProfilerFiller profiler) {
            SnowyCrescentCore.log.info("Loading animations from data packs...");

            Map<ResourceLocation, JsonElement> sorted = new LinkedHashMap<>();

            // Sort resources by priority
            resourceManager.listPacks().forEach(packResources -> {
                Set<String> namespaces = packResources.getNamespaces(PackType.SERVER_DATA);
                namespaces.forEach(namespace ->
                        packResources.listResources(PackType.SERVER_DATA, namespace, "scc_animations",
                                (resourceLocation, inputStreamIoSupplier) -> {
                                    String path = resourceLocation.getPath();
                                    if (path.endsWith(".anim.json")) {
                                        ResourceLocation rl = new ResourceLocation(namespace,
                                                path.substring("scc_animations/".length(), path.length() - ".json".length()));

                                        JsonElement el = resources.get(rl);
                                        if (el != null) {
                                            rl = new ResourceLocation(namespace,
                                                    path.substring("scc_animations/".length(), path.length() - ".anim.json".length()));
                                            sorted.put(rl, el);
                                        }
                                    }
                                }
                        )
                );
            });

            int loadedCount = 0;
            for (Map.Entry<ResourceLocation, JsonElement> entry : sorted.entrySet()) {
                ResourceLocation animKey = entry.getKey();

                try {
                    JsonObject json = GsonHelper.convertToJsonObject(entry.getValue(), "animation");

                    // Parse animation using existing AnimJson.Reader
                    AnimJson.Reader animReader = AnimJson.Reader.stream(json);
                    GenericAnimationData anim = animReader.parse();

                    // Ensure the key matches
                    if (!anim.getKey().equals(animKey)) {
                        SnowyCrescentCore.log.warn("Animation key mismatch: file={}, expected={}, actual={}",
                                entry.getKey(), animKey, anim.getKey());
                        anim = anim.withName(animKey.getPath()); // Create a copy with correct key if possible
                    }

                    animations.put(animKey, anim);
                    loadedCount++;
                    SnowyCrescentCore.log.debug("Loaded animation: {} -> {}", animKey, anim.getName());

                } catch (IllegalArgumentException | JsonParseException e) {
                    SnowyCrescentCore.log.error("Parsing error loading animation {}", animKey, e);
                }
            }

            SnowyCrescentCore.log.info("Loaded {} animations from data packs", loadedCount);
        }
    }

    /**
     * Layer data manager using SimpleJsonResourceReloadListener
     */
    public static class LayerDataManager extends SimpleJsonResourceReloadListener {
        public static final LayerDataManager INSTANCE = new LayerDataManager();


        private LayerDataManager() {
            super(new GsonBuilder()
                    .setPrettyPrinting()
                    .disableHtmlEscaping()
                    .create(), "scc_animations");
        }

        @Override
        protected void apply(@Nonnull Map<ResourceLocation, JsonElement> resources,
                             @Nonnull ResourceManager resourceManager,
                             @Nonnull ProfilerFiller profiler) {
            SnowyCrescentCore.log.info("Loading layer configurations from data packs...");

            Map<ResourceLocation, JsonElement> sorted = new LinkedHashMap<>();

            // Sort resources by priority
            resourceManager.listPacks().forEach(packResources -> {
                Set<String> namespaces = packResources.getNamespaces(PackType.SERVER_DATA);
                namespaces.forEach(namespace ->
                        packResources.listResources(PackType.SERVER_DATA, namespace, "scc_animations",
                                (resourceLocation, inputStreamIoSupplier) -> {
                                    String path = resourceLocation.getPath();
                                    if (path.endsWith("animation.layer.json")) {
                                        ResourceLocation rl = new ResourceLocation(namespace, "animation.layer");

                                        JsonElement el = resources.get(rl);
                                        if (el != null) {
                                            rl = new ResourceLocation(namespace, "animation_layer");
                                            sorted.put(rl, el);
                                        }
                                    }
                                }
                        )
                );
            });

            int loadedCount = 0;
            for (Map.Entry<ResourceLocation, JsonElement> entry : sorted.entrySet()) {
                try {
                    JsonElement json = entry.getValue();
                    AnimLayerJson.Reader layerReader = AnimLayerJson.Reader.stream(json);
                    Map<ResourceLocation, Integer> parsedLayers = layerReader.parse();

                    // Merge layer configurations
                    parsedLayers.forEach((layerKey, priority) -> {
                        if (layers.containsKey(layerKey)) {
                            SnowyCrescentCore.log.debug("Overriding layer {} with priority {}", layerKey, priority);
                        }
                        layers.put(layerKey, priority);
                    });

                    loadedCount += parsedLayers.size();
                    SnowyCrescentCore.log.debug("Loaded {} layer configurations from {}",
                            parsedLayers.size(), entry.getKey().getNamespace());

                } catch (IllegalArgumentException | JsonParseException e) {
                    SnowyCrescentCore.log.error("Parsing error loading layer configuration", e);
                }
            }

            SnowyCrescentCore.log.info("Loaded {} layer configurations from data packs", loadedCount);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            MinecraftServer server = serverPlayer.getServer();
            if(server == null) return;

            // Send animations to client
            sendAnimationsToClient(serverPlayer);
        }
    }

    /**
     * Send all animations and layers to the client
     */
    private static void sendAnimationsToClient(ServerPlayer player) {
        SnowyCrescentCore.log.debug("Sending animations to player: {}", player.getName().getString());

        // Clear client cache first
        ModChannel.sendToPlayer(new AnimationClientStatusPacket(
                AnimationClientStatusPacket.Status.ANIM_CACHE_CLEAR), player);

        // Send all animations
        for (GenericAnimationData anim : animations.values()) {
            JsonElement json = AnimJson.Writer.stream(anim).toJson();
            String jsonString = json.toString();
            ModChannel.sendToPlayer(new AnimationJsonPacket(jsonString, false), player);
        }

        // Register animations on client
        ModChannel.sendToPlayer(new AnimationClientStatusPacket(
                AnimationClientStatusPacket.Status.ANIM_REGISTER), player);

        // Clear layer cache
        ModChannel.sendToPlayer(new AnimationClientStatusPacket(
                AnimationClientStatusPacket.Status.LAYER_CACHE_CLEAR), player);

        // Send layer configurations
        JsonElement layerJson = convertLayersToJson(layers);
        ModChannel.sendToPlayer(new AnimationJsonPacket(layerJson.toString(), true), player);

        // Register layers on client
        ModChannel.sendToPlayer(new AnimationClientStatusPacket(
                AnimationClientStatusPacket.Status.LAYER_REGISTER), player);

        SnowyCrescentCore.log.debug("Sent {} animations and {} layers to player {}",
                animations.size(), layers.size(), player.getName().getString());
    }

    /**
     * Convert layers map to JSON
     */
    private static JsonElement convertLayersToJson(Map<ResourceLocation, Integer> layers) {
        JsonArray jsonArray = new JsonArray();

        for (Map.Entry<ResourceLocation, Integer> entry : layers.entrySet()) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("key", entry.getKey().toString());
            jsonObject.addProperty("priority", entry.getValue());
            jsonArray.add(jsonObject);
        }

        return jsonArray;
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
                                if (Minecraft.getInstance().player == null) {
                                    return ClientCache.registerPlayerAnimation(player);
                                }
                                Map<ResourceLocation, IAnimation> animationMap =
                                        modifierLayers.getOrDefault(player.getUUID(), new HashMap<>());
                                if (animationMap.containsKey(key)) {
                                    return animationMap.get(key);
                                }
                                IAnimation iAnimation = ClientCache.registerPlayerAnimation(player);
                                animationMap.put(key, iAnimation);
                                modifierLayers.put(player.getUUID(), animationMap);
                                return iAnimation;
                            })
                    );

                    // Update existing players' animation stacks
                    ClientLevel level = Minecraft.getInstance().level;
                    if (level == null) {
                        SnowyCrescentCore.log.error("Level is null, cannot update animation layers");
                        return;
                    }

                    try {
                        for (AbstractClientPlayer player : level.players()) {
                            updatePlayerAnimationStack(player);
                        }
                    } catch (Exception e) {
                        SnowyCrescentCore.log.error("Failed to update player animation layers", e);
                    }
                }
            }
        }

        /**
         * Update a player's animation stack with new layers
         */
        @SuppressWarnings({"unchecked", "JavaReflectionMemberAccess"})
        private static void updatePlayerAnimationStack(AbstractClientPlayer player) {
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

                ArrayList<Pair<Integer, IAnimation>> oldArrayList =
                        new ArrayList<>((ArrayList<Pair<Integer, IAnimation>>) layersField.get(oldAnimationStack));
                ArrayList<Pair<Integer, IAnimation>> newArrayList =
                        new ArrayList<>((ArrayList<Pair<Integer, IAnimation>>) layersField.get(newAnimationStack));

                // Merge layers, keeping unique ones
                ArrayList<Pair<Integer, IAnimation>> result = new ArrayList<>();

                // Add old layers that aren't replaced by new ones
                for (Pair<Integer, IAnimation> oldPair : oldArrayList) {
                    boolean isReplaced = false;
                    for (Pair<Integer, IAnimation> newPair : newArrayList) {
                        if (Objects.equals(oldPair.getLeft(), newPair.getLeft())) {
                            isReplaced = true;
                            break;
                        }
                    }
                    if (!isReplaced) {
                        result.add(oldPair);
                    }
                }

                // Add all new layers
                result.addAll(newArrayList);

                // Set the merged layers
                layersField.set(newAnimationStack, result);
                animationStackField.set(player, newAnimationStack);

                // Update animation applier
                Field animationApplierField = playerClass.getDeclaredField("animationApplier");
                animationApplierField.setAccessible(true);
                //noinspection UnstableApiUsage
                animationApplierField.set(player, new AnimationApplier(newAnimationStack));

                // Restore any playing animations
                restorePlayingAnimations(player);

            } catch (Exception e) {
                SnowyCrescentCore.log.error("Failed to update animation stack for player: {}", player, e);
            }
        }

        /**
         * Restore animations that were playing before the update
         */
        private static void restorePlayingAnimations(AbstractClientPlayer player) {
            try {
                IAnimationCapability data = AnimationDataCapability.getCapability(player).orElse(null);
                if (data == null) return;

                RawAnimationDataCapability rawData = RawAnimationDataCapability.getCapability(player).orElse(null);
                if (rawData == null) return;

                Map<ResourceLocation, ResourceLocation> dataAnimations = new HashMap<>();
                dataAnimations.putAll(data.getAnimations());
                dataAnimations.putAll(rawData.getAnimations());

                ResourceLocation riderAnimLayer = data.getRiderAnimLayer();
                if (riderAnimLayer != null && data.getRiderAnimation() != null) {
                    dataAnimations.put(riderAnimLayer, data.getRiderAnimation());
                }

                for (Map.Entry<ResourceLocation, ResourceLocation> entry : dataAnimations.entrySet()) {
                    ResourceLocation layerKey = entry.getKey();
                    ResourceLocation animKey = entry.getValue();

                    @SuppressWarnings("unchecked")
                    ModifierLayer<IAnimation> modifierLayer = (ModifierLayer<IAnimation>)
                            PlayerAnimationAccess.getPlayerAssociatedData(player).get(layerKey);
                    if (modifierLayer == null) continue;

                    KeyframeAnimation keyframeAnimation;
                    GenericAnimationData anim = animations.get(animKey);

                    if (anim == null) {
                        RawAnimationData rawAnim = RawAnimationService.INSTANCE.getAnimation(animKey);
                        if (rawAnim == null) continue;
                        keyframeAnimation = rawAnim.getAnimation();
                    } else {
                        keyframeAnimation = anim.getAnimation();
                    }

                    if (keyframeAnimation == null) continue;

                    modifierLayer.replaceAnimationWithFade(
                            AbstractFadeModifier.standardFadeIn(3, Ease.INOUTSINE),
                            new KeyframeAnimationPlayer(keyframeAnimation)
                    );
                }
            } catch (Exception e) {
                SnowyCrescentCore.log.error("Failed to restore playing animations for player: {}", player, e);
            }
        }

        private static IAnimation registerPlayerAnimation(AbstractClientPlayer player) {
            return new ModifierLayer<>();
        }
    }
}