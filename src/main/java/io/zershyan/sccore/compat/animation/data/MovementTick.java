package io.zershyan.sccore.compat.animation.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * AABB 移动控制器，管理动画驱动的碰撞箱移动的 tick 生命周期。
 *
 * <p>定义了四个关键 tick 节点：开始、结束、停止和回退。
 * 超过 {@code endTick} 后自动回退到 {@code returnTick} 循环播放，
 * 达到 {@code stopTick} 后标记为已停止。</p>
 */
public class MovementTick {
    public static final Codec<MovementTick> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("beginTick").forGetter(MovementTick::getBeginTick),
            Codec.INT.fieldOf("endTick").forGetter(MovementTick::getEndTick),
            Codec.INT.fieldOf("stopTick").forGetter(MovementTick::getStopTick),
            Codec.INT.fieldOf("returnTick").forGetter(MovementTick::getReturnTick)
    ).apply(i, MovementTick::new));
    private final int beginTick;
    private final int endTick;
    private final int stopTick;
    private final int returnTick;
    private boolean stopped = false;
    private int currentTick = 0;

    public MovementTick(int beginTick, int endTick, int stopTick, int returnTick) {
        this.beginTick = beginTick;
        this.endTick = endTick;
        this.stopTick = stopTick;
        this.returnTick = returnTick;
    }

    /** 推进一 tick，超过 endTick 后回退到 returnTick，达到 stopTick 后标记停止。 */
    public void tick() {
        this.currentTick++;
        if (this.currentTick > this.endTick) {
            this.currentTick = this.returnTick;
        }
        if (this.currentTick >= this.stopTick) {
            stopped = true;
        }
    }

    public int getBeginTick() {
        return beginTick;
    }

    public int getEndTick() {
        return endTick;
    }

    public int getStopTick() {
        return stopTick;
    }

    public int getReturnTick() {
        return returnTick;
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public boolean isStopped() {
        return stopped;
    }
}
