package io.zershyan.sccore.compat.animation.data;

import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

/**
 * 动画数据的抽象基类，定义动画的通用属性。
 *
 * <p>由 {@link ClientAnimation}（客户端动画）和 {@link ServerAnimation}（服务端动画）继承，
 * 分别添加各自特有的属性。</p>
 *
 * @see ClientAnimation
 * @see ServerAnimation
 */
public abstract class Animation {
    protected final ResourceLocation animationLocation;
    protected final Optional<String> name;
    protected final int priority;
    protected final Optional<RideData> rideData;
    protected final boolean defaultThirdPerson;
    protected final AABBMovement aabbMovement;

    public Animation(ResourceLocation animationLocation, Optional<String> name, int priority, Optional<RideData> rideData, boolean defaultThirdPerson, AABBMovement aabbMovement) {
        this.animationLocation = animationLocation;
        this.name = name;
        this.priority = priority;
        this.rideData = rideData;
        this.defaultThirdPerson = defaultThirdPerson;
        this.aabbMovement = aabbMovement;
    }

    public Animation(ResourceLocation animationLocation, Optional<String> name, int priority, Optional<RideData> rideData) {
        this(animationLocation, name, priority, rideData, false, new AABBMovement());
    }

    public int priority() {
        return priority;
    }

    public ResourceLocation animationLocation() {
        return animationLocation;
    }

    public Optional<String> name() {
        return name;
    }

    public Optional<RideData> rideData() {
        return rideData;
    }

    public boolean defaultThirdPerson() {
        return defaultThirdPerson;
    }

    public AABBMovement aabbMovement() {
        return aabbMovement;
    }
}
