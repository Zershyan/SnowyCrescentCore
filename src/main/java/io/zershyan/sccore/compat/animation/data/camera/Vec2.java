package io.zershyan.sccore.compat.animation.data.camera;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Vec2(double x, double y) {
    public static final Vec2 ZERO = new Vec2(0.0, 0.0);
    public static final Codec<Vec2> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.DOUBLE.optionalFieldOf("x", 0d).forGetter(Vec2::x),
            Codec.DOUBLE.optionalFieldOf("y", 0d).forGetter(Vec2::y)
    ).apply(i, Vec2::new));

    public Vec2 add(Vec2 vec) {
        return new Vec2(x + vec.x, y + vec.y);
    }

    public Vec2 add(double x, double y) {
        return new Vec2(x + this.x, y + this.y);
    }

    public Vec2 multiply(Vec2 vec) {
        return new Vec2(x * vec.x, y * vec.y);
    }

    public Vec2 multiply(double x, double y) {
        return new Vec2(x * this.x, y * this.y);
    }

    public Vec2 scale(double scale) {
        return new Vec2(x * scale, y * scale);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Vec2(double x1, double y1))) return false;
        return Double.compare(x1, this.x) == 0 && Double.compare(y1, this.y) == 0;
    }
}
