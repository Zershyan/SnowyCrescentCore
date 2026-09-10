package io.zershyan.sccore.compat.animation.data.camera;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 欧拉角，用于表示相机变换中的旋转。
 *
 * @param pitch 俯仰角
 * @param yaw   偏航角
 * @param roll  翻滚角
 */
public record EulerAngle(float pitch, float yaw, float roll) {
    public static final Codec<EulerAngle> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.FLOAT.optionalFieldOf("pitch", 0f).forGetter(EulerAngle::pitch),
            Codec.FLOAT.optionalFieldOf("yaw", 0f).forGetter(EulerAngle::yaw),
            Codec.FLOAT.optionalFieldOf("roll", 0f).forGetter(EulerAngle::roll)
    ).apply(i, EulerAngle::new));
    public static EulerAngle ZERO = new EulerAngle(0, 0, 0);

    /**
     * 返回各分量相加后的新欧拉角。
     */
    public EulerAngle add(float pitch, float yaw, float roll) {
        return new EulerAngle(this.pitch + pitch, this.yaw + yaw, this.roll + roll);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof EulerAngle(float pitch1, float yaw1, float roll1))) return false;
        return Double.compare(pitch1, this.pitch) == 0 && Double.compare(yaw1, this.yaw) == 0 && Double.compare(roll1, this.roll) == 0;
    }
}
