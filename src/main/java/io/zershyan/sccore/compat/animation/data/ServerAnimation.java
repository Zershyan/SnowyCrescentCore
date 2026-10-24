package io.zershyan.sccore.compat.animation.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.zershyan.sccore.compat.animation.data.camera.CameraChange;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * 服务端动画数据，在 {@link Animation} 基础上增加 AABB 移动时间线、跳跃力度修正，
 * 并拥有与客户端动画相同的相机变换字段。
 *
 * <p>服务端动画会随注册表下发给客户端，因此客户端播放它时既能用 AABB 计算碰撞箱，
 * 也能拿到相机变换，不会再出现"播放服务端动画导致视角效果缺失"的情况。</p>
 *
 * @see Animation
 * @see ClientAnimation
 */
public class ServerAnimation extends Animation {
    public static final Codec<ServerAnimation> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.fieldOf("animationLocation").forGetter(Animation::animationLocation),
            Codec.STRING.optionalFieldOf("name").forGetter(Animation::name),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(Animation::priority),
            RideData.CODEC.optionalFieldOf("rideData").forGetter(Animation::rideData),
            Codec.BOOL.optionalFieldOf("defaultThirdPerson", false).forGetter(Animation::defaultThirdPerson),
            AABBMovement.CODEC.optionalFieldOf("aabbMovement", new AABBMovement()).forGetter(ServerAnimation::aabbMovement),
            Codec.FLOAT.optionalFieldOf("jumpModifier", 1.0f).forGetter(ServerAnimation::jumpModifier),
            CameraChange.CODEC.optionalFieldOf("firstPersonCameraChange", new CameraChange()).forGetter(Animation::firstPersonCameraChange),
            CameraChange.CODEC.optionalFieldOf("cameraChange", new CameraChange()).forGetter(Animation::cameraChange)
    ).apply(i, ServerAnimation::new));
    /**
     * 下发给客户端用的编码：跳跃修正只在服务端计算，无需传输。
     */
    public static final Codec<ServerAnimation> SUB_CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.fieldOf("animationLocation").forGetter(Animation::animationLocation),
            Codec.STRING.optionalFieldOf("name").forGetter(Animation::name),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(Animation::priority),
            RideData.CODEC.optionalFieldOf("rideData").forGetter(Animation::rideData),
            Codec.BOOL.optionalFieldOf("defaultThirdPerson", false).forGetter(Animation::defaultThirdPerson),
            AABBMovement.CODEC.optionalFieldOf("aabbMovement", new AABBMovement()).forGetter(ServerAnimation::aabbMovement),
            CameraChange.CODEC.optionalFieldOf("firstPersonCameraChange", new CameraChange()).forGetter(Animation::firstPersonCameraChange),
            CameraChange.CODEC.optionalFieldOf("cameraChange", new CameraChange()).forGetter(Animation::cameraChange)
    ).apply(i, (location, name, priority, rideData, thirdPerson, aabbMovement, firstPersonCamera, camera) ->
            new ServerAnimation(location, name, priority, rideData, thirdPerson, aabbMovement, 1.0f, firstPersonCamera, camera)));
    private final AABBMovement aabbMovement;
    private final float jumpModifier;

    public ServerAnimation(
            ResourceLocation animationLocation,
            Optional<String> name,
            int priority,
            Optional<RideData> rideData,
            boolean defaultThirdPerson,
            AABBMovement aabbMovement,
            float jumpModifier,
            CameraChange firstPersonCameraChange,
            CameraChange cameraChange
    ) {
        super(animationLocation, name, priority, rideData, defaultThirdPerson, firstPersonCameraChange, cameraChange);
        this.aabbMovement = aabbMovement;
        this.jumpModifier = jumpModifier;
    }

    public ServerAnimation(
            ResourceLocation animationLocation,
            Optional<String> name,
            int priority,
            Optional<RideData> rideData,
            boolean defaultThirdPerson,
            AABBMovement aabbMovement
    ) {
        this(animationLocation, name, priority, rideData, defaultThirdPerson, aabbMovement, 1.0f, new CameraChange(), new CameraChange());
    }

    public AABBMovement aabbMovement() {
        return aabbMovement;
    }

    public float jumpModifier() {
        return jumpModifier;
    }

    public @Nullable String getName() {
        return name.orElse(null);
    }

    public @Nullable RideData getRideData() {
        return rideData.orElse(null);
    }
}
