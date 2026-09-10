package io.zershyan.sccore.compat.animation.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.zershyan.sccore.SCCore;
import io.zershyan.sccore.api.events.client.ServerReloadEvent;
import io.zershyan.sccore.compat.animation.api.events.AnimationRegisterEvent;
import io.zershyan.sccore.compat.animation.api.events.LayerRegisterEvent;
import io.zershyan.sccore.compat.animation.data.Animation;
import io.zershyan.sccore.compat.animation.data.ServerAnimation;
import io.zershyan.sccore.compat.animation.network.data.RegisterAnimationData;
import io.zershyan.sccore.compat.animation.network.data.RegisterLayerData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 服务端动画注册表，管理服务端动画层与动画定义的注册、加载与同步。
 *
 * <p>注册来源有两种：</p>
 * <ol>
 *   <li>事件驱动 — 通过 {@link LayerRegisterEvent.Server} 和 {@link AnimationRegisterEvent.Server}</li>
 *   <li>资源包驱动 — 从 {@code animation/layer/} 和 {@code animation/animation/} 目录下的 JSON 文件加载</li>
 * </ol>
 *
 * <p>事件注册先于资源包注册执行，因此资源包可覆盖代码注册的内容。
 * 玩家登录时自动将注册数据同步到客户端。</p>
 */
public class ServerAnimationRegistry {
    public static final String LAYER_DIR = "animation/layer/";
    public static final String ANIMATION_DIR = "animation/animation/";
    private static final Map<ResourceLocation, Integer> Layers = new HashMap<>();
    private static final Map<ResourceLocation, ServerAnimation> Animations = new HashMap<>();

    @SubscribeEvent
    public static void serverInit(ServerAboutToStartEvent event) {
        reloadData(event.getServer());
    }

    @SubscribeEvent
    public static void initPlayer(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        sendDataToPlayer(player);
    }

    @SubscribeEvent
    public static void serverReload(ServerReloadEvent.Post event) {
        MinecraftServer server = event.getServer();
        reloadData(server);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sendDataToPlayer(player);
        }
    }

    private static void reloadData(MinecraftServer server) {
        Layers.clear();
        Animations.clear();

        // 事件驱动的注册Layer
        Layers.putAll(NeoForge.EVENT_BUS.post(new LayerRegisterEvent.Server()).getLayers());
        // 资源包驱动的注册Layer
        ResourceManager resourceManager = server.getResourceManager();
        Map<ResourceLocation, Resource> layerResourceMap = resourceManager.listResources(SCCore.MODID, location ->
                location.getNamespace().equals(LAYER_DIR) && location.getPath().endsWith(".json")
        );
        for (Resource value : layerResourceMap.values()) {
            try (BufferedReader reader = value.openAsReader()) {
                JsonElement element = JsonParser.parseReader(reader);
                Layers.putAll(SyncAnimationFactory.LAYER_CODEC.parse(JsonOps.INSTANCE, element).getOrThrow());
            } catch (Exception e) {
                SCCore.log.error(e.getMessage());
            }
        }

        // 事件驱动的注册Animation
        Animations.putAll(NeoForge.EVENT_BUS.post(new AnimationRegisterEvent.Server()).getAnimations());
        // 资源包驱动的注册Animation
        Map<ResourceLocation, Resource> animationResourceMap = resourceManager.listResources(SCCore.MODID, location ->
                location.getNamespace().equals(ANIMATION_DIR) && location.getPath().endsWith(".json")
        );
        animationResourceMap.forEach((location, resource) -> {
            try (BufferedReader reader = resource.openAsReader()) {
                JsonElement element = JsonParser.parseReader(reader);
                Animations.put(location, ServerAnimation.CODEC.parse(JsonOps.INSTANCE, element).getOrThrow());
            } catch (Exception e) {
                SCCore.log.error(e.getMessage());
            }
        });
    }

    private static void sendDataToPlayer(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new RegisterLayerData(new HashMap<>(Layers)));
        PacketDistributor.sendToPlayer(player, new RegisterAnimationData(new HashMap<>(Animations)));
    }

    /**
     * 统一查询动画：先查服务端注册表，再查同步动画工厂。
     *
     * @param animationLocation 动画资源位置
     * @return 动画实例，不存在则返回 {@code null}
     */
    @Nullable
    public static Animation commonGetAnimation(ResourceLocation animationLocation) {
        if (Animations.containsKey(animationLocation)) return Animations.get(animationLocation);
        else return SyncAnimationFactory.getAnimation(animationLocation);
    }

    public static Map<ResourceLocation, Integer> getLayers() {
        return Map.copyOf(Layers);
    }

    public static Map<ResourceLocation, ServerAnimation> getAnimations() {
        return Map.copyOf(Animations);
    }
}
