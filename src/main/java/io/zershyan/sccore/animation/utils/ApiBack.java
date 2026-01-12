package io.zershyan.sccore.animation.utils;

import org.jetbrains.annotations.Nullable;

public enum ApiBack {
    FAIL(0),
    SUCCESS(1),
    COOLDOWN(2),
    RESOURCE_NOT_FOUND(3),
    OUT_RANGE(4),
    OPERATION_EXPIRE(5),
    UNSUPPORTED(6),
    BE_CANCELLED(7),
    ;
    public final int value;
    ApiBack(int value) {
        this.value = value;
    }

    public boolean isValueOf(int value) {
        return this.value == value;
    }

    @Nullable
    public static ApiBack valueOf(int value) {
        ApiBack[] values = ApiBack.values();
        for (ApiBack v : values) {
            if (v.isValueOf(value)) {
                return v;
            }
        }
        return null;
    }
}
