package io.zershyan.sccore.compat.animation.data.camera;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * 相机变换关键帧序列，按 tick 存储相机偏移与欧拉角，支持线性插值采样。
 */
public record CameraChange(TreeMap<Integer, CameraData> movement, boolean relativeEuler) {
    public static final Codec<CameraChange> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.unboundedMap(Codec.STRING.xmap(Integer::parseInt, Object::toString), CameraData.CODEC)
                    .xmap(TreeMap::new, Function.identity())
                    .optionalFieldOf("movement", new TreeMap<>())
                    .forGetter(CameraChange::movement),
            Codec.BOOL.optionalFieldOf("relativeEuler", true).forGetter(CameraChange::relativeEuler)
    ).apply(i, CameraChange::new));

    public CameraChange() {
        this(new TreeMap<>(), true);
    }

    public CameraChange(TreeMap<Integer, CameraData> movement) {
        this(movement, true);
    }

    /**
     * 在 movement 关键帧之间按 tick 线性插值，得到该时刻的相机变换数据。
     *
     * @param tick 采样时刻（通常为 currentTick + partialTick）
     * @return 插值后的相机数据；movement 为空时返回 {@code null}
     */
    public CameraData sample(float tick) {
        if (movement.isEmpty()) return null;
        int floor = (int) Math.floor(tick);
        Map.Entry<Integer, CameraData> floorEntry = movement.floorEntry(floor);
        Map.Entry<Integer, CameraData> ceilEntry = movement.ceilingEntry(floor);
        if (floorEntry == null) return ceilEntry.getValue();
        if (ceilEntry == null) return floorEntry.getValue();
        if (floorEntry.getKey().equals(ceilEntry.getKey())) return floorEntry.getValue();
        float delta = ceilEntry.getKey() - floorEntry.getKey();
        float alpha = (tick - floorEntry.getKey()) / delta;
        CameraData a = floorEntry.getValue();
        CameraData b = ceilEntry.getValue();
        return a.sample(alpha, b);
    }

    public boolean isEmpty() {
        return movement.isEmpty();
    }
}
