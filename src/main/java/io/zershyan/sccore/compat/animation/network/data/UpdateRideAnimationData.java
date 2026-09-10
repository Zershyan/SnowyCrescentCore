package io.zershyan.sccore.compat.animation.network.data;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import io.zershyan.sccore.SCCore;
import io.zershyan.sccore.compat.animation.api.SCCAnimationApi;
import io.zershyan.sccore.compat.animation.api.server.AnimationRideHelper;
import io.zershyan.sccore.compat.animation.data.ClientRideAnimDTO;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * 客户端 → 服务端：更新玩家的骑乘动画。
 *
 * <p>动画来源使用 {@link Either} 表示：左侧为服务端已知的资源位置，
 * 右侧为客户端独有的完整动画定义（{@link ClientRideAnimDTO}）。</p>
 *
 * @param layerLoc  骑乘动画层，为空表示清除骑乘
 * @param animation 骑乘动画来源，为空表示清除骑乘
 */
public record UpdateRideAnimationData(Optional<ResourceLocation> layerLoc, Optional<Either<ResourceLocation, ClientRideAnimDTO>> animation) implements CustomPacketPayload {
    public static final Type<@NotNull UpdateRideAnimationData> TYPE =
            new Type<>(SCCore.id("animator_ride_animation"));

    public static final StreamCodec<ByteBuf, UpdateRideAnimationData> STREAM_CODEC = ByteBufCodecs.fromCodec(RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.optionalFieldOf("layerLoc").forGetter(UpdateRideAnimationData::layerLoc),
            Codec.xor(ResourceLocation.CODEC, ClientRideAnimDTO.CODEC).optionalFieldOf("animation").forGetter(UpdateRideAnimationData::animation)
    ).apply(i, UpdateRideAnimationData::new)));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handler(IPayloadContext context) {
        context.enqueueWork(() -> {
            if(context.player() instanceof ServerPlayer sender) {
                AnimationRideHelper helper = SCCAnimationApi.ridePlayer(sender);
                ResourceLocation layer = layerLoc().orElse(null);
                if(layer != null) {
                    Optional<Either<ResourceLocation, ClientRideAnimDTO>> animation = animation();
                    if(animation.isPresent()) {
                        Either<ResourceLocation, ClientRideAnimDTO> either = animation.get();
                        if(either.left().isPresent()) helper.startRide(layer, either.left().get());
                        else if(either.right().isPresent()) helper.startRide(layer, either.right().get());
                    } else SCCore.log.error("Has layer but animation does not exist");
                } else helper.stopRide();
            }
        });
    }
}
