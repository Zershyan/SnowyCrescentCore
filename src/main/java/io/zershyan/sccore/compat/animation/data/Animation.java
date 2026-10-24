package io.zershyan.sccore.compat.animation.data;

import io.zershyan.sccore.compat.animation.data.camera.CameraChange;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * 动画数据的抽象基类，定义两端共有的属性。
 *
 * <p>服务端动画（{@link ServerAnimation}）在此基础上增加 AABB 移动与跳跃修正；
 * 客户端动画（{@link ClientAnimation}）是它的子集，只提供表现数据而没有 AABB，
 * 因此客户端注册动画不会间接定义碰撞箱。</p>
 *
 * <p>相机变换由两端共同拥有：播放服务端动画时客户端也能拿到相机效果，
 * 而同 ID 的客户端动画可以由更高的优先级覆盖它。</p>
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
    protected final CameraChange firstPersonCameraChange;
    protected final CameraChange cameraChange;

    protected Animation(
            ResourceLocation animationLocation,
            Optional<String> name,
            int priority,
            Optional<RideData> rideData,
            boolean defaultThirdPerson,
            CameraChange firstPersonCameraChange,
            CameraChange cameraChange
    ) {
        this.animationLocation = animationLocation;
        this.name = name;
        this.priority = priority;
        this.rideData = rideData;
        this.defaultThirdPerson = defaultThirdPerson;
        this.firstPersonCameraChange = firstPersonCameraChange;
        this.cameraChange = cameraChange;
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

    public CameraChange firstPersonCameraChange() {
        return firstPersonCameraChange;
    }

    public CameraChange cameraChange() {
        return cameraChange;
    }

    /**
     * 是否存在任何相机变换关键帧。
     *
     * @return 第一人称或第三人称任一有数据时返回 {@code true}
     */
    public boolean hasCameraChange() {
        return !firstPersonCameraChange.isEmpty() || !cameraChange.isEmpty();
    }

    public Component getDescription() {
        if(getDescriptionId() == null) return Component.literal(name().orElse("undefined"));
        return Component.translatable(getDescriptionId());
    }

    public @Nullable String getDescriptionId() {
        return animationLocation.toLanguageKey("animation", "name").replace("/", ".");
    }
}
