package io.zershyan.sccore.compat.animation.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.zershyan.sccore.SCCore;
import io.zershyan.sccore.api.events.client.ServerReloadEvent;
import io.zershyan.sccore.compat.animation.api.events.AnimationRegisterEvent;
import io.zershyan.sccore.compat.animation.api.events.LayerRegisterEvent;
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
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
    public static final String LAYER_DIR = "animation/layer";
    public static final String ANIMATION_DIR = "animation/animation";
    private static final Map<ResourceLocation, Integer> Layers = new HashMap<>();
    private static final Map<ResourceLocation, ServerAnimation> Animations = new HashMap<>();
    private static final Set<UUID> notifiedPlayers = ConcurrentHashMap.newKeySet();

    @SubscribeEvent
    public static void serverInit(ServerAboutToStartEvent event) {
        // 新世界启动：上一个世界的下发记录必须作废，单人存档退出重进时才能重新下发
        notifiedPlayers.clear();
        reloadData(event.getServer());
    }

    @SubscribeEvent
    public static void initPlayerOnJoin(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!notifiedPlayers.add(player.getUUID())) return;
        sendDataToPlayer(player);
    }

    /**
     * 可能不够早，所以上面写了个EntityJoinLevelEvent，这个是兜底
     * @param event
     */
    @SubscribeEvent
    public static void initPlayer(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!notifiedPlayers.add(player.getUUID())) return;
        sendDataToPlayer(player);
    }

    @SubscribeEvent
    public static void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        notifiedPlayers.remove(event.getEntity().getUUID());
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
        Animations.putAll(NeoForge.EVENT_BUS.post(new AnimationRegisterEvent.Server()).getAnimations());
        // 资源包驱动的注册Animation
        Map<ResourceLocation, Resource> animationResourceMap = resourceManager.listResources(ANIMATION_DIR,
                location -> location.getPath().endsWith(".json")
        );
        animationResourceMap.forEach((location, resource) -> {
            try (BufferedReader reader = resource.openAsReader()) {
                JsonElement element = JsonParser.parseReader(reader);
                Animations.put(location, ServerAnimation.CODEC.parse(JsonOps.INSTANCE, element).getOrThrow());
            } catch (Exception e) {
                SCCore.LOGGER.error(e.getMessage());
            }
        });
    }

    private static void sendDataToPlayer(ServerPlayer player) {
        // 先发动画定义再发层表：客户端注册层时会重建动画栈，此时定义已经就位
        PacketDistributor.sendToPlayer(player, new RegisterAnimationData(new HashMap<>(Animations)));
        PacketDistributor.sendToPlayer(player, new RegisterLayerData(new HashMap<>(Layers)));
    }

    /**
     * 统一查询服务端动画：先查服务端注册表，再查同步动画工厂。
     *
     * <p>服务端只有注册表，客户端两者都有（注册表在客户端进程通常为空），
     * 因此两端用同一方法都能拿到带 AABB 与相机数据的服务端定义。</p>
     *
     * @param animationLocation 动画资源位置
     * @return 服务端动画实例，不存在则返回 {@code null}
     */
    @Nullable
    public static ServerAnimation commonGetAnimation(ResourceLocation animationLocation) {
        ServerAnimation animation = Animations.get(animationLocation);
        return animation != null ? animation : SyncAnimationFactory.getAnimation(animationLocation);
    }

    public static Map<ResourceLocation, Integer> getLayers() {
        return Map.copyOf(Layers);
    }

    public static Map<ResourceLocation, ServerAnimation> getAnimations() {
        return Map.copyOf(Animations);
    }
}
