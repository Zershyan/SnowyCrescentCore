package io.zershyan.sccore.compat.animation.network.data;

import io.netty.buffer.ByteBuf;
import io.zershyan.sccore.SCCore;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * 服务端 → 客户端：通知客户端切换到第三人称视角。
 */
public record TurnThirdPersonData() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<@NotNull TurnThirdPersonData> TYPE =
            new CustomPacketPayload.Type<>(SCCore.id("turn_third_person_data"));

    public static final StreamCodec<ByteBuf, TurnThirdPersonData> STREAM_CODEC = StreamCodec.unit(new TurnThirdPersonData());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handler(IPayloadContext context) {
        context.enqueueWork(() -> Minecraft.getInstance().options.setCameraType(CameraType.THIRD_PERSON_BACK));
    }
}
