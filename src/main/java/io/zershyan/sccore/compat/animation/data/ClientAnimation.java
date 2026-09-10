package io.zershyan.sccore.compat.animation.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.zershyan.sccore.compat.animation.data.camera.CameraChange;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * 客户端动画数据，在 {@link Animation} 基础上增加相机变换（第一人称/第三人称）。
 *
 * <p>包含 {@link CameraChange} 内部记录，定义了按 tick 插值的相机偏移与欧拉角关键帧序列，
 * 用于 {@code CameraTransformStateHandler} 在渲染时驱动相机变换。</p>
 *
 * @see Animation
 * @see ServerAnimation
 * @see CameraChange
 */
public class ClientAnimation extends Animation {
    public static final Codec<ClientAnimation> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.fieldOf("animationLocation").forGetter(ClientAnimation::animationLocation),
            Codec.STRING.optionalFieldOf("name").forGetter(ClientAnimation::name),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(ClientAnimation::priority),
            RideData.CODEC.optionalFieldOf("rideData").forGetter(ClientAnimation::rideData),
            Codec.BOOL.optionalFieldOf("defaultThirdPerson", false).forGetter(ClientAnimation::defaultThirdPerson),
            CameraChange.CODEC.optionalFieldOf("firstPersonCameraChange", new CameraChange()).forGetter(ClientAnimation::firstPersonCameraChange),
            CameraChange.CODEC.optionalFieldOf("cameraChange", new CameraChange()).forGetter(ClientAnimation::cameraChange)
    ).apply(i, ClientAnimation::new));
    public static final Codec<ClientAnimation> SUB_CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.fieldOf("animationLocation").forGetter(ClientAnimation::animationLocation),
            Codec.STRING.optionalFieldOf("name").forGetter(ClientAnimation::name),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(ClientAnimation::priority),
            RideData.CODEC.optionalFieldOf("rideData").forGetter(ClientAnimation::rideData),
            Codec.BOOL.optionalFieldOf("defaultThirdPerson", false).forGetter(ClientAnimation::defaultThirdPerson)
    ).apply(i, ClientAnimation::new));
    private final CameraChange firstPersonCameraChange;
    private final CameraChange cameraChange;

    public ClientAnimation(ResourceLocation animationLocation, Optional<String> name, int priority, Optional<RideData> rideData, boolean defaultThirdPerson, CameraChange firstPersonCameraChange, CameraChange cameraChange) {
        super(animationLocation, name, priority, rideData, defaultThirdPerson, new AABBMovement());
        this.firstPersonCameraChange = firstPersonCameraChange;
        this.cameraChange = cameraChange;
    }

    public ClientAnimation(ResourceLocation animationLocation, @Nullable String name, int priority, @Nullable RideData rideData, boolean defaultThirdPerson, CameraChange firstPersonCameraChange, CameraChange cameraChange) {
        super(animationLocation, Optional.ofNullable(name), priority, Optional.ofNullable(rideData), defaultThirdPerson, new AABBMovement());
        this.firstPersonCameraChange = firstPersonCameraChange;
        this.cameraChange = cameraChange;
    }

    public ClientAnimation(ResourceLocation animationLocation, Optional<String> name, int priority, Optional<RideData> rideData, boolean defaultThirdPerson) {
        this(animationLocation, name, priority, rideData, defaultThirdPerson, new CameraChange(), new CameraChange());
    }

    public ClientAnimation(ResourceLocation animationLocation, @Nullable String name, int priority, @Nullable RideData data, boolean defaultThirdPerson) {
        this(animationLocation, Optional.ofNullable(name), priority, Optional.ofNullable(data), defaultThirdPerson, new CameraChange(), new CameraChange());
    }

    public ClientAnimation(ServerAnimation animation) {
        super(animation.animationLocation(), Optional.ofNullable(animation.getName()), animation.priority(), Optional.ofNullable(animation.getRideData()), animation.defaultThirdPerson(), animation.aabbMovement());
        this.firstPersonCameraChange = new CameraChange();
        this.cameraChange = new CameraChange();
    }

    public boolean hasCameraChange() {
        return !firstPersonCameraChange.isEmpty() || !cameraChange.isEmpty();
    }

    public CameraChange firstPersonCameraChange() {
        return firstPersonCameraChange;
    }

    public CameraChange cameraChange() {
        return cameraChange;
    }

}
