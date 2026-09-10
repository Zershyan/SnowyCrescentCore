package io.zershyan.sccore.compat.animation.network.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import io.zershyan.sccore.SCCore;
import io.zershyan.sccore.compat.animation.handler.common.MovementAnimationTickHandler;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

/**
 * 双向：同步动画驱动的移动 tick 数据，用于 AABB 移动计算。
 *
 * @param playerUUID  玩家 UUID
 * @param animationId 当前动画 ID，为空表示动画已结束
 * @param currentTick 当前 tick
 */
public record MovementAnimationTickData(UUID playerUUID, Optional<ResourceLocation> animationId, int currentTick) implements CustomPacketPayload {
    public static final Type<@NotNull MovementAnimationTickData> TYPE = new Type<>(SCCore.id("movement_animation_tick"));

    public static final StreamCodec<ByteBuf, MovementAnimationTickData> STREAM_CODEC = ByteBufCodecs.fromCodec(RecordCodecBuilder.create(i -> i.group(
            UUIDUtil.CODEC.fieldOf("playerUUID").forGetter(MovementAnimationTickData::playerUUID),
            ResourceLocation.CODEC.optionalFieldOf("animationId").forGetter(MovementAnimationTickData::animationId),
            Codec.INT.fieldOf("currentTick").forGetter(MovementAnimationTickData::currentTick)
    ).apply(i, MovementAnimationTickData::new)));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handler(IPayloadContext context) {
        context.enqueueWork(() -> {
            if(context.player() instanceof Player sender) {
                if(animationId().isEmpty()) {
                    MovementAnimationTickHandler.removeData(playerUUID());
                } else {
                    MovementAnimationTickHandler.putData(playerUUID(), this);
                }
                if(sender instanceof ServerPlayer) {
                    PacketDistributor.sendToAllPlayers(this);
                }
            }
        });
    }
}
