package io.zershyan.sccore.compat.animation.data.camera;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * 单帧相机数据，包含位置偏移与欧拉角。
 */
public record CameraData(Vec2 relativeOffset, Vec3 offset, EulerAngle eulerAngle) {
    public static final CameraData ZERO = new CameraData(Vec2.ZERO, Vec3.ZERO, EulerAngle.ZERO);
    public static final Codec<CameraData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Vec2.CODEC.optionalFieldOf("relativeOffset", Vec2.ZERO).forGetter(CameraData::relativeOffset),
            Vec3.CODEC.optionalFieldOf("offset", Vec3.ZERO).forGetter(CameraData::offset),
            EulerAngle.CODEC.optionalFieldOf("eulerAngle", EulerAngle.ZERO).forGetter(CameraData::eulerAngle)
    ).apply(i, CameraData::new));

    public static CameraData ofRelative(Vec2 relativeOffset, EulerAngle camEulerAngles) {
        return new CameraData(relativeOffset, Vec3.ZERO, camEulerAngles);
    }

    public static CameraData of(Vec3 offset, EulerAngle camEulerAngles) {
        return new CameraData(Vec2.ZERO, offset, camEulerAngles);
    }

    public static CameraData of(Vec2 relativeOffset, Vec3 offset, EulerAngle camEulerAngles) {
        return new CameraData(relativeOffset, offset, camEulerAngles);
    }

    public static boolean isOrNearEmpty(CameraData cameraData) {
        return cameraData == null || ZERO.equals(cameraData);
    }

    public CameraData sample(float delta, CameraData next) {
        Vec2 relativeOffset = new Vec2(
                lerp(delta, relativeOffset().x(), next.relativeOffset().x()),
                lerp(delta, relativeOffset().y(), next.relativeOffset().y())
        );
        Vec3 offset = new Vec3(
                lerp(delta, offset().x, next.offset().x),
                lerp(delta, offset().y, next.offset().y),
                lerp(delta, offset().z, next.offset().z)
        );
        EulerAngle euler = new EulerAngle(
                lerp(delta, eulerAngle().pitch(), next.eulerAngle().pitch()),
                lerp(delta, eulerAngle().yaw(), next.eulerAngle().yaw()),
                lerp(delta, eulerAngle().roll(), next.eulerAngle().roll())
        );
        return new CameraData(relativeOffset, offset, euler);
    }

    private float lerp(float delta, float a, float b) {
        return (float) lerp((double) delta, a, b);
    }

    private double lerp(double delta, double a, double b) {
        double sub = b - a;
        double abs = Math.abs(sub);
        double v = 0.05 / 120;
        if (abs <= v) return b;
        if (abs <= 0.05) {
            if (sub < 0) {
                a -= v;
            } else if (sub > 0) {
                a += v;
            }
            return a;
        }
        return Mth.lerp(delta, a, b);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof CameraData(Vec2 relativeOffset1, Vec3 offset1, EulerAngle eulerAngle1))) return false;
        return this.relativeOffset.equals(relativeOffset1) && this.offset.equals(offset1) && this.eulerAngle.equals(eulerAngle1);
    }
}
