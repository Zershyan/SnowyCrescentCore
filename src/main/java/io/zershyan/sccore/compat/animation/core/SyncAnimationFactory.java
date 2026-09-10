package io.zershyan.sccore.compat.animation.core;

import com.mojang.serialization.Codec;
import io.zershyan.sccore.compat.animation.data.ClientAnimation;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 同步动画工厂，存储由服务端同步到客户端的动画层与动画定义。
 *
 * <p>服务端在玩家登录或资源重载时将动画数据发送到客户端，客户端收到后
 * 将其存入本工厂，与本地注册的动画分开管理。</p>
 */
public class SyncAnimationFactory {
    public static final Codec<HashMap<ResourceLocation, Integer>> LAYER_CODEC = Codec.unboundedMap(
            ResourceLocation.CODEC, Codec.INT
    ).xmap(HashMap::new, Function.identity());
    private static final Map<ResourceLocation, Integer> Layers = new HashMap<>();
    private static final Map<ResourceLocation, ClientAnimation> Animations = new HashMap<>();

    /** 重新加载同步层，同时注册到 Player Animator。 */
    public static void reloadLayers(Map<ResourceLocation, Integer> layers) {
        ClientAnimationRegistry.registerLayers(layers);
        Layers.clear();
        Layers.putAll(layers);
    }

    /** 重新加载同步动画。 */
    public static void reloadAnimations(Map<ResourceLocation, ClientAnimation> layers) {
        Animations.clear();
        Animations.putAll(layers);
    }

    @Nullable
    public static ClientAnimation getAnimation(ResourceLocation location) {
        return Animations.getOrDefault(location, null);
    }

    public static Map<ResourceLocation, Integer> getLayers() {
        return Map.copyOf(Layers);
    }

    public static Map<ResourceLocation, ClientAnimation> getAnimations() {
        return Map.copyOf(Animations);
    }
}
