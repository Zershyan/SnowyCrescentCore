package io.zershyan.sccore.compat.animation.network.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import io.zershyan.sccore.SCCore;
import io.zershyan.sccore.compat.animation.core.SyncAnimationFactory;
import io.zershyan.sccore.compat.animation.data.ClientAnimation;
import io.zershyan.sccore.compat.animation.data.ServerAnimation;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 服务端 → 客户端：同步服务端动画注册表到客户端。
 *
 * @param animations 动画 ID → 服务端动画定义的映射
 */
public record RegisterAnimationData(HashMap<ResourceLocation, ServerAnimation> animations) implements CustomPacketPayload {
    public static final Type<@NotNull RegisterAnimationData> TYPE =
            new Type<>(SCCore.id("animator_animations"));

    public static final StreamCodec<ByteBuf, RegisterAnimationData> STREAM_CODEC = ByteBufCodecs.fromCodecTrusted(RecordCodecBuilder.create(
            i -> i.group(Codec.unboundedMap(ResourceLocation.CODEC, ServerAnimation.SUB_CODEC)
                    .xmap(HashMap::new, Function.identity())
                    .fieldOf("animations")
                    .forGetter(RegisterAnimationData::animations)
            ).apply(i, RegisterAnimationData::new)));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handler(IPayloadContext context) {
        context.enqueueWork(() -> {
            HashMap<ResourceLocation, ServerAnimation> animations = animations();
            Map<ResourceLocation, ClientAnimation> results = new HashMap<>();
            animations.forEach((location, animation) ->
                    results.put(location, new ClientAnimation(animation)));
            SyncAnimationFactory.reloadAnimations(results);
        });
    }
}
