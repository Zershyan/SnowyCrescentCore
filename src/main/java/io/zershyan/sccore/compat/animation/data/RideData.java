package io.zershyan.sccore.compat.animation.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * 骑乘动画数据，定义骑乘实体的组件动画列表、偏移量、存在时长与旋转。
 *
 * @param componentAnimations 组件动画的资源位置列表，按顺序对应各组件玩家
 * @param offset              骑乘位置相对于车主玩家的偏移
 * @param existTick           骑乘存在时长（tick），-1 表示无限
 * @param xRot                骑乘实体的 X 轴旋转（俯仰）
 * @param yRot                骑乘实体的 Y 轴旋转（偏航）
 */
public record RideData(List<ResourceLocation> componentAnimations, Vec3 offset, int existTick, float xRot, float yRot) {
    public static final Codec<RideData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.list(ResourceLocation.CODEC).fieldOf("componentAnimations").forGetter(RideData::componentAnimations),
            Vec3.CODEC.optionalFieldOf("offset", Vec3.ZERO).forGetter(RideData::offset),
            Codec.INT.optionalFieldOf("existTick", -1).forGetter(RideData::existTick),
            Codec.FLOAT.optionalFieldOf("xRot", 0f).forGetter(RideData::xRot),
            Codec.FLOAT.optionalFieldOf("yRot", 0f).forGetter(RideData::yRot)
    ).apply(i, RideData::new));
}
