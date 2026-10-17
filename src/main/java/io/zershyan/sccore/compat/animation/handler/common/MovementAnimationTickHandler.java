package io.zershyan.sccore.compat.animation.handler.common;

import io.zershyan.sccore.SCCore;
import io.zershyan.sccore.compat.animation.network.data.MovementAnimationTickData;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = SCCore.MODID)
public class MovementAnimationTickHandler {
    private static final Map<UUID, MovementAnimationTickData> movementAnimTickDataMap = new HashMap<>();

    public static void putData(UUID player, MovementAnimationTickData data) {
        movementAnimTickDataMap.put(player, data);
    }

    @Nullable
    public static MovementAnimationTickData getData(UUID player) {
        return movementAnimTickDataMap.getOrDefault(player, null);
    }

    public static void removeData(UUID player) {
        movementAnimTickDataMap.remove(player);
    }

    /**
     * 玩家离线时丢弃其 tick 数据，避免残留数据被同 UUID 复用。
     */
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        removeData(event.getEntity().getUUID());
    }

    /**
     * 服务端停止时清空缓存。
     */
    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        movementAnimTickDataMap.clear();
    }

    /**
     * 本地客户端断开连接时清空缓存。
     */
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onClientLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        movementAnimTickDataMap.clear();
    }

    /**
     * 有客户端开始追踪某玩家时补发其 tick 数据：定格中的动画不会再发包，
     * 新追踪者需要这份初始状态才能算出一致的碰撞箱。
     */
    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (!(event.getEntity() instanceof ServerPlayer receiver)) return;
        MovementAnimationTickData data = getData(event.getTarget().getUUID());
        if (data == null) return;
        PacketDistributor.sendToPlayer(receiver, data);
    }
}
