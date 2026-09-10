package io.zershyan.sccore.compat.animation.network.data;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import io.zershyan.sccore.SCCore;
import io.zershyan.sccore.compat.animation.api.SCCAnimationApi;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.function.Function;

/**
 * 客户端 → 服务端：更新玩家的客户端/服务端动画映射。
 *
 * @param animations 层 → 动画的映射
 * @param isServer   是否为服务端动画映射（{@code false} 为客户端映射）
 */
public record UpdateAnimationData(HashMap<ResourceLocation, ResourceLocation> animations, boolean isServer) implements CustomPacketPayload {
    public static final Type<@NotNull UpdateAnimationData> TYPE =
            new Type<>(SCCore.id("animator_animation"));

    public static final StreamCodec<ByteBuf, UpdateAnimationData> STREAM_CODEC = ByteBufCodecs.fromCodec(RecordCodecBuilder.create(i -> i.group(
            Codec.unboundedMap(ResourceLocation.CODEC, ResourceLocation.CODEC).xmap(HashMap::new, Function.identity())
                    .fieldOf("animations").forGetter(UpdateAnimationData::animations),
            Codec.BOOL.fieldOf("isServer").forGetter(UpdateAnimationData::isServer)
    ).apply(i, UpdateAnimationData::new)));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handler(IPayloadContext context) {
        context.enqueueWork(() -> {
            if(context.player() instanceof ServerPlayer sender) {
                SCCAnimationApi.animation(sender).operaData(opera -> {
                    if(isServer()) opera.newServerAnimMap(animations());
                    else opera.newClientAnimMap(animations());
                    return opera.endOpera();
                });
            }
        });
    }
}
