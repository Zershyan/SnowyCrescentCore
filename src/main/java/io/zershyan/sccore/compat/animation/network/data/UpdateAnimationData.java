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
 * 客户端 → 服务端：提交本玩家的客户端动画映射。
 *
 * <p>服务端只把它写入 Attachment 的客户端动画数据（{@code clientAnimMap}）并同步给所有玩家，
 * 使其他拥有同层同动画的客户端也能播放；服务端动画数据与骑乘动画均不受此包影响。</p>
 *
 * <p>目标玩家固定为发送者自身，报文不携带玩家标识，因此无法伪造他人数据。</p>
 *
 * @param animations 层 → 动画的映射
 */
public record UpdateAnimationData(HashMap<ResourceLocation, ResourceLocation> animations) implements CustomPacketPayload {
    public static final Type<@NotNull UpdateAnimationData> TYPE =
            new Type<>(SCCore.id("animator_animation"));

    public static final StreamCodec<ByteBuf, UpdateAnimationData> STREAM_CODEC = ByteBufCodecs.fromCodec(RecordCodecBuilder.create(i -> i.group(
            Codec.unboundedMap(ResourceLocation.CODEC, ResourceLocation.CODEC).xmap(HashMap::new, Function.identity())
                    .fieldOf("animations").forGetter(UpdateAnimationData::animations)
    ).apply(i, UpdateAnimationData::new)));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handler(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer sender)) return;
            SCCAnimationApi.animation(sender).operaData(opera -> opera.newClientAnimMap(animations()).endOpera());
        });
    }
}
