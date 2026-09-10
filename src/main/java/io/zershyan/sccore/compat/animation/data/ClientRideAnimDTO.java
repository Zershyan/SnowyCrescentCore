package io.zershyan.sccore.compat.animation.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

/**
 * 客户端骑乘动画 DTO，用于客户端向服务端发送骑乘动画时携带完整的客户端动画定义。
 *
 * <p>当骑乘动画来源于客户端注册表（而非服务端同步）时，需要将动画数据一并传输，
 * 因为服务端没有对应的动画定义。</p>
 *
 * @param id        动画的逻辑 ID
 * @param animation 完整的客户端动画数据
 */
public record ClientRideAnimDTO(ResourceLocation id, ClientAnimation animation) {
    public static final Codec<ClientRideAnimDTO> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(ClientRideAnimDTO::id),
            ClientAnimation.SUB_CODEC.fieldOf("animation").forGetter(ClientRideAnimDTO::animation)
    ).apply(i, ClientRideAnimDTO::new));
}
