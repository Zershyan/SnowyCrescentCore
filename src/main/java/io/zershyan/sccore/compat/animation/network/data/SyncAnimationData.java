package io.zershyan.sccore.compat.animation.network.data;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import io.zershyan.sccore.SCCore;
import io.zershyan.sccore.compat.animation.api.SCCAnimationApi;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * 服务端 → 客户端：同步两个玩家的骑乘动画 tick，使二者保持同步播放。
 *
 * @param player 源玩家 UUID
 * @param target 目标玩家 UUID
 */
public record SyncAnimationData(UUID player, UUID target) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<@NotNull SyncAnimationData> TYPE =
            new CustomPacketPayload.Type<>(SCCore.id("sync_animation_data"));

    public static final StreamCodec<ByteBuf, SyncAnimationData> STREAM_CODEC = ByteBufCodecs.fromCodec(RecordCodecBuilder.create(i -> i.group(
            UUIDUtil.CODEC.fieldOf("player").forGetter(SyncAnimationData::player),
            UUIDUtil.CODEC.fieldOf("target").forGetter(SyncAnimationData::target)
    ).apply(i, SyncAnimationData::new)));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handler(IPayloadContext context) {
        context.enqueueWork(() -> {
            Level level = context.player().level();
            Player player = level.getPlayerByUUID(player());
            Player target = level.getPlayerByUUID(target());
            if(player instanceof AbstractClientPlayer clientPlayer && target instanceof AbstractClientPlayer targetPlayer) {
                SCCAnimationApi.animPlayer(clientPlayer).syncAnimation(targetPlayer);
            }
        });
    }
}
