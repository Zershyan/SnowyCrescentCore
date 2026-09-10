package io.zershyan.sccore.compat.animation.network.data;

import io.netty.buffer.ByteBuf;
import io.zershyan.sccore.SCCore;
import io.zershyan.sccore.compat.animation.core.SyncAnimationFactory;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * 服务端 → 客户端：同步动画层注册表到客户端。
 *
 * @param layers 层资源位置 → 优先级的映射
 */
public record RegisterLayerData(HashMap<ResourceLocation, Integer> layers) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<@NotNull RegisterLayerData> TYPE =
            new CustomPacketPayload.Type<>(SCCore.id("animator_layers"));

    public static final StreamCodec<ByteBuf, RegisterLayerData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(SyncAnimationFactory.LAYER_CODEC),
            RegisterLayerData::layers, RegisterLayerData::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handler(IPayloadContext context) {
        context.enqueueWork(() -> SyncAnimationFactory.reloadLayers(layers()));
    }
}
