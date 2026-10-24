package io.zershyan.sccore.compat.animation.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.zershyan.sccore.compat.animation.data.camera.CameraChange;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * 客户端动画数据，是 {@link ServerAnimation} 的子集：保留通用属性与相机变换，没有 AABB 移动。
 *
 * <p>客户端注册动画只影响表现，不会定义碰撞箱；播放时只把"客户端正在播放该动画"
 * 告知服务端并由服务端转发给其他玩家。与同 ID 的服务端动画共存时，由 priority 决定用哪一份定义，
 * 同优先级下客户端本地定义优先。</p>
 *
 * @see Animation
 * @see ServerAnimation
 */
public class ClientAnimation extends Animation {
    public static final Codec<ClientAnimation> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.fieldOf("animationLocation").forGetter(Animation::animationLocation),
            Codec.STRING.optionalFieldOf("name").forGetter(Animation::name),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(Animation::priority),
            RideData.CODEC.optionalFieldOf("rideData").forGetter(Animation::rideData),
            Codec.BOOL.optionalFieldOf("defaultThirdPerson", false).forGetter(Animation::defaultThirdPerson),
            CameraChange.CODEC.optionalFieldOf("firstPersonCameraChange", new CameraChange()).forGetter(Animation::firstPersonCameraChange),
            CameraChange.CODEC.optionalFieldOf("cameraChange", new CameraChange()).forGetter(Animation::cameraChange)
    ).apply(i, ClientAnimation::new));

    public ClientAnimation(
            ResourceLocation animationLocation,
            Optional<String> name,
            int priority,
            Optional<RideData> rideData,
            boolean defaultThirdPerson,
            CameraChange firstPersonCameraChange,
            CameraChange cameraChange
    ) {
        super(animationLocation, name, priority, rideData, defaultThirdPerson, firstPersonCameraChange, cameraChange);
    }

    public ClientAnimation(
            ResourceLocation animationLocation,
            @Nullable String name,
            int priority,
            @Nullable RideData rideData,
            boolean defaultThirdPerson,
            CameraChange firstPersonCameraChange,
            CameraChange cameraChange
    ) {
        this(animationLocation, Optional.ofNullable(name), priority, Optional.ofNullable(rideData), defaultThirdPerson, firstPersonCameraChange, cameraChange);
    }

    public ClientAnimation(
            ResourceLocation animationLocation,
            Optional<String> name,
            int priority,
            Optional<RideData> rideData,
            boolean defaultThirdPerson
    ) {
        this(animationLocation, name, priority, rideData, defaultThirdPerson, new CameraChange(), new CameraChange());
    }

    public ClientAnimation(
            ResourceLocation animationLocation,
            @Nullable String name,
            int priority,
            @Nullable RideData rideData,
            boolean defaultThirdPerson
    ) {
        this(animationLocation, Optional.ofNullable(name), priority, Optional.ofNullable(rideData), defaultThirdPerson, new CameraChange(), new CameraChange());
    }
}
