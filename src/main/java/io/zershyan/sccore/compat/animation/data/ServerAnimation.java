package io.zershyan.sccore.compat.animation.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * 服务端动画数据，在 {@link Animation} 基础上增加 AABB 移动时间线与跳跃力度修正。
 *
 * @see Animation
 * @see ClientAnimation
 */
public class ServerAnimation extends Animation {
    private final float jumpModifier;
    public static final Codec<ServerAnimation> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.fieldOf("animationLocation").forGetter(Animation::animationLocation),
            Codec.STRING.optionalFieldOf("name").forGetter(Animation::name),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(Animation::priority),
            RideData.CODEC.optionalFieldOf("rideData").forGetter(Animation::rideData),
            Codec.BOOL.optionalFieldOf("defaultThirdPerson", false).forGetter(Animation::defaultThirdPerson),
            AABBMovement.CODEC.optionalFieldOf("aabbMovement", new AABBMovement()).forGetter(Animation::aabbMovement),
            Codec.FLOAT.optionalFieldOf("jumpModifier", 1.0f).forGetter(ServerAnimation::jumpModifier)
    ).apply(i, ServerAnimation::new));
    public static final Codec<ServerAnimation> SUB_CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.fieldOf("animationLocation").forGetter(ServerAnimation::animationLocation),
            Codec.STRING.optionalFieldOf("name").forGetter(ServerAnimation::name),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(ServerAnimation::priority),
            RideData.CODEC.optionalFieldOf("rideData").forGetter(ServerAnimation::rideData),
            Codec.BOOL.optionalFieldOf("defaultThirdPerson", false).forGetter(ServerAnimation::defaultThirdPerson),
            AABBMovement.CODEC.optionalFieldOf("aabbMovement", new AABBMovement()).forGetter(Animation::aabbMovement)
    ).apply(i, ServerAnimation::new));
    public ServerAnimation(ResourceLocation animationLocation, Optional<String> name, int priority, Optional<RideData> data, boolean defaultThirdPerson, AABBMovement aabbMovement, float jumpModifier) {
        super(animationLocation, name, priority, data, defaultThirdPerson, aabbMovement);
        this.jumpModifier = jumpModifier;
    }
    public ServerAnimation(ResourceLocation animationLocation, Optional<String> name, int priority, Optional<RideData> data, boolean defaultThirdPerson, AABBMovement aabbMovement) {
        this(animationLocation, name, priority, data, defaultThirdPerson, aabbMovement, 1.0f);
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
