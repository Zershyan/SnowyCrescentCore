package io.zershyan.sccore.compat.animation.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.kosmx.playerAnim.api.IPlayable;
import dev.kosmx.playerAnim.api.layered.AnimationStack;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.core.util.Pair;
import dev.kosmx.playerAnim.impl.animation.AnimationApplier;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import io.zershyan.sccore.SCCore;
import io.zershyan.sccore.api.events.client.ResourceLoadEvent;
import io.zershyan.sccore.compat.animation.api.SCCAnimationApi;
import io.zershyan.sccore.compat.animation.api.events.AnimationRegisterEvent;
import io.zershyan.sccore.compat.animation.api.events.LayerRegisterEvent;
import io.zershyan.sccore.compat.animation.api.helper.AnimationPlayerHelper;
import io.zershyan.sccore.compat.animation.data.Animation;
import io.zershyan.sccore.compat.animation.data.ClientAnimation;
import io.zershyan.sccore.compat.animation.data.ServerAnimation;
import io.zershyan.sccore.util.mixin.playeranimator.IMixinFactoryHolder;
import io.zershyan.sccore.compat.animation.registry.attachment.PlayerAnimations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 客户端动画注册表，管理客户端动画层与动画定义的注册、加载与 Player Animator 层绑定。
 *
 * <p>动画定义来自事件驱动与 {@code assets} 侧的 {@code animation/animation}；服务端定义位于
 * {@code data} 侧的同名路径，由资源管理器的 {@code PackType} 天然隔离，不会在此被读取，
 * 而是随登录由 {@link SyncAnimationFactory} 下发。层同样如此，并与下发的层表合并。</p>
 *
 * <p>本类还负责将注册的层深度绑定到 Player Animator 的 {@code AnimationStack} 中，
 * 并在资源重载时通过反射重建已有玩家的动画栈。</p>
 */
public class ClientAnimationRegistry {
    public static final String LAYER_DIR = "animation/layer";
    public static final String ANIMATION_DIR = "animation/animation";
    private static final Map<ResourceLocation, Integer> Layers = new HashMap<>();
    private static final Map<ResourceLocation, ClientAnimation> Animations = new HashMap<>();
    private static final Map<UUID, Map<ResourceLocation, IAnimation>> CacheAnim = new HashMap<>();
    private static final Set<ResourceLocation> LocalLayerIds = new HashSet<>();
    private static final Set<ResourceLocation> SyncLayerIds = new HashSet<>();

    @SubscribeEvent
    public static void clientReload(ResourceLoadEvent.Post event) {
        reloadData();
    }

    @SubscribeEvent
    public static void clientInit(ClientPlayerNetworkEvent.LoggingIn event) {
        reloadData();
    }

    @SubscribeEvent
    public static void clientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        CacheAnim.clear();
        SyncAnimationFactory.clear();
    }

    private static void reloadData() {
        Layers.clear();
        Animations.clear();
        // 事件驱动的注册Layer
        Layers.putAll(NeoForge.EVENT_BUS.post(new LayerRegisterEvent.Client()).getLayers());
        // 资源包驱动的注册Layer
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        Map<ResourceLocation, Resource> layerResourceMap = resourceManager.listResources(LAYER_DIR,
                location -> location.getPath().endsWith(".json")
        );
        for (Resource value : layerResourceMap.values()) {
            try (BufferedReader reader = value.openAsReader()) {
                JsonElement element = JsonParser.parseReader(reader);
                Layers.putAll(SyncAnimationFactory.LAYER_CODEC.parse(JsonOps.INSTANCE, element).getOrThrow());
            } catch (Exception e) {
                SCCore.LOGGER.error(e.getMessage());
            }
        }

        // 事件驱动的注册Animation
        Animations.putAll(NeoForge.EVENT_BUS.post(new AnimationRegisterEvent.Client()).getAnimations());
        // 资源包驱动的注册Animation
        Map<ResourceLocation, Resource> animationResourceMap = resourceManager.listResources(ANIMATION_DIR,
                location -> location.getPath().endsWith(".json")
        );
        animationResourceMap.forEach((location, resource) -> {
            try (BufferedReader reader = resource.openAsReader()) {
                JsonElement element = JsonParser.parseReader(reader);
                Animations.put(location, ClientAnimation.CODEC.parse(JsonOps.INSTANCE, element).getOrThrow());
            } catch (Exception e) {
                SCCore.LOGGER.error(e.getMessage());
            }
        });

        // 深度注册Layer数据
        registerLayers(Layers);
    }

    /**
     * 深度注册 Layer，将 Layer 映射到 Player Animator 的底层数据中。
     *
     * <p>注册前会移除上一次本地注册的工厂：资源包删除某个层后，其旧工厂不应继续生效。</p>
     *
     * @param layers 需要注册的 Layer 层
     */
    public static void registerLayers(Map<ResourceLocation, Integer> layers) {
        applyLayers(layers, LocalLayerIds);
    }

    /**
     * 深度注册服务端同步来的 Layer，与本地注册各自维护已注册集合。
     *
     * @param layers 服务端同步来的 Layer 层
     */
    public static void registerSyncLayers(Map<ResourceLocation, Integer> layers) {
        applyLayers(layers, SyncLayerIds);
    }

    private static void applyLayers(Map<ResourceLocation, Integer> layers, Set<ResourceLocation> registered) {
        IMixinFactoryHolder.of(PlayerAnimationFactory.ANIMATION_DATA_FACTORY).sccore$clearAnimations(registered);
        registered.clear();
        registered.addAll(layers.keySet());
        Minecraft instance = Minecraft.getInstance();
        layers.forEach((key, value) -> PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                key, value, player -> {
                    if (instance.player == null) return registerPlayerAnimation(player);
                    return CacheAnim.computeIfAbsent(player.getUUID(), uuid -> new HashMap<>())
                            .computeIfAbsent(key, k -> registerPlayerAnimation(player));
                })
        );
        if (layers.isEmpty() || instance.level == null) return;
        try {
            instance.level.players().forEach(ClientAnimationRegistry::reflectAnimationCore);
        } catch (Exception e) {
            SCCore.LOGGER.error("{} reflect error: {}", ClientAnimationRegistry.class.getName(), e.getMessage());
        }
    }

    private static IAnimation registerPlayerAnimation(AbstractClientPlayer player) {
        return new ModifierLayer<>();
    }

    public static Map<ResourceLocation, Integer> getLayers() {
        return Map.copyOf(Layers);
    }

    public static Map<ResourceLocation, ClientAnimation> getAnimations() {
        return Map.copyOf(Animations);
    }

    /**
     * 获取所有客户端可见动画，包括本地注册的与服务端同步的。
     *
     * <p>同 ID 时按 priority 取用，同优先级使用本地注册的客户端定义。</p>
     */
    public static Map<ResourceLocation, Animation> getAllAnimations() {
        Map<ResourceLocation, Animation> result = new HashMap<>();
        SyncAnimationFactory.getAnimations().forEach(result::put);
        Animations.forEach((location, animation) -> {
            Animation synced = result.get(location);
            if (synced == null || animation.priority() >= synced.priority()) result.put(location, animation);
        });
        return Map.copyOf(result);
    }

    /**
     * 获取所有客户端层，包括本地注册的与服务端同步的。
     */
    public static Map<ResourceLocation, Integer> getAllLayers() {
        return Map.copyOf(new HashMap<>(Layers) {{
            putAll(SyncAnimationFactory.getLayers());
        }});
    }

    private static KeyframeAnimation getAnimationOfPair(Pair<Integer, IAnimation> pair) {
        if (!(pair.getRight() instanceof ModifierLayer<?> modifierLayer)) return null;
        return modifierLayer.getAnimation() instanceof KeyframeAnimationPlayer player ? player.getData() : null;
    }

    public static Map<UUID, Map<ResourceLocation, IAnimation>> getCacheAnim() {
        return CacheAnim;
    }

    /**
     * 查询客户端可见的动画定义：服务端同步定义与本地注册定义中优先级更高者，
     * 同优先级时使用本地定义。
     *
     * @param location 动画资源位置
     * @return 动画定义，两端都不存在时返回 {@code null}
     */
    @Nullable
    public static Animation getAnimation(ResourceLocation location) {
        ServerAnimation synced = SyncAnimationFactory.getAnimation(location);
        ClientAnimation local = Animations.get(location);
        if (synced == null) return local;
        if (local == null) return synced;
        return local.priority() >= synced.priority() ? local : synced;
    }

    /**
     * 获取指定动画对应的 Player Animator 关键帧动画。
     *
     * @param location 动画资源位置
     * @return 关键帧动画，不存在或不是关键帧动画时返回 {@code null}
     */
    @Nullable
    public static KeyframeAnimation getKeyframeAnimation(ResourceLocation location) {
        Animation animation = getAnimation(location);
        if (animation == null) return null;
        IPlayable playable = PlayerAnimationRegistry.getAnimation(animation.animationLocation());
        return playable instanceof KeyframeAnimation keyframe ? keyframe : null;
    }

    /**
     * 反射修改玩家 Layer 底层数据。
     *
     * <p>由于 Mixin 无法或过于麻烦实现，此处通过反射重建玩家的 {@link AnimationStack}，
     * 合并旧栈中仍在播放的层与新注册的层，并重新播放当前活跃的动画。</p>
     *
     * @param player 目标玩家
     */
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
                    if (Objects.equals(oldAnimationPair.getLeft(), newAnimationPair.getLeft())) {
                        KeyframeAnimation oldData = getAnimationOfPair(oldAnimationPair);
                        KeyframeAnimation newData = getAnimationOfPair(newAnimationPair);
                        if (Objects.equals(oldData, newData)) oldArrayList.remove(oldAnimationPair);
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
            PlayerAnimations data = PlayerAnimations.getData(player);
            Map<ResourceLocation, ResourceLocation> dataAnimations = new HashMap<>();
            dataAnimations.putAll(data.clientAnimMap());
            dataAnimations.putAll(data.serverAnimMap());
            data.rideAnim().layer().ifPresent(riderAnimLayer ->
                    dataAnimations.put(riderAnimLayer, data.rideAnim().animation().orElseThrow())
            );
            AnimationPlayerHelper animPlayer = SCCAnimationApi.animPlayer(player);
            for (ResourceLocation layer : dataAnimations.keySet()) {
                animPlayer.innerPlayAnimation(3, Ease.INOUTSINE, layer, dataAnimations.get(layer));
            }
        } catch (Exception e) {
            SCCore.LOGGER.error("Failed to register on {} animation layer: {}", player, e.getMessage(), e);
        }
    }
}
