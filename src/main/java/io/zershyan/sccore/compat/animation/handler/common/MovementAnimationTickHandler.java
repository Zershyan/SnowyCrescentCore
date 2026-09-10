package io.zershyan.sccore.compat.animation.handler.common;

import io.zershyan.sccore.compat.animation.network.data.MovementAnimationTickData;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MovementAnimationTickHandler {
    private static final Map<UUID, MovementAnimationTickData> movementAnimTickDataMap = new HashMap<>();

    public static void putData(UUID player, MovementAnimationTickData data) {
        movementAnimTickDataMap.put(player, data);
    }

    @Nullable
    public static MovementAnimationTickData getData(UUID player) {
        return movementAnimTickDataMap.getOrDefault(player, null);
    }

    public static void removeData(UUID player) {
        movementAnimTickDataMap.remove(player);
    }
}
