package io.zershyan.sccore.compat.animation.registry.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import io.zershyan.sccore.compat.animation.data.MovementTick;
import io.zershyan.sccore.compat.animation.registry.AnimationAttachments;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

public record ModifyAABBData(MovementTick movementTick) {
    public static final Codec<ModifyAABBData> CODEC = RecordCodecBuilder.create(i -> i.group(
            MovementTick.CODEC.fieldOf("aabbMovementData").forGetter(ModifyAABBData::movementTick)
    ).apply(i, ModifyAABBData::new));
    public static final StreamCodec<ByteBuf, ModifyAABBData> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public static ModifyAABBData getData(Player player) {
        return player.getData(AnimationAttachments.MODIFY_AABB_DATA);
    }
}
