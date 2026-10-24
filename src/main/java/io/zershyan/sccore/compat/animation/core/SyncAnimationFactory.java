package io.zershyan.sccore.compat.animation.core;

import com.mojang.serialization.Codec;
import io.zershyan.sccore.compat.animation.data.ServerAnimation;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 同步动画工厂，存储由服务端下发到客户端的动画层与动画定义。
 *
 * <p>服务端在玩家登录或资源重载时将注册表发送到客户端，客户端原样保存为 {@link ServerAnimation}，
 * 与本地注册的客户端动画分开管理：播放时按优先级从两者中取用，同优先级时本地定义优先。</p>
 *
 * @see ClientAnimationRegistry#getAnimation(ResourceLocation)
 */
public class SyncAnimationFactory {
    public static final Codec<HashMap<ResourceLocation, Integer>> LAYER_CODEC = Codec.unboundedMap(
            ResourceLocation.CODEC, Codec.INT
    ).xmap(HashMap::new, Function.identity());
    private static final Map<ResourceLocation, Integer> Layers = new HashMap<>();
    private static final Map<ResourceLocation, ServerAnimation> Animations = new HashMap<>();

    /**
     * 重新加载同步层，同时注册到 Player Animator。
     */
    public static void reloadLayers(Map<ResourceLocation, Integer> layers) {
        ClientAnimationRegistry.registerSyncLayers(layers);
        Layers.clear();
        Layers.putAll(layers);
    }

    /**
     * 重新加载同步动画。
     */
    public static void reloadAnimations(Map<ResourceLocation, ServerAnimation> animations) {
        Animations.clear();
        Animations.putAll(animations);
    }

    /**
     * 清空同步数据，并移除对应的 Player Animator 工厂。
     */
    public static void clear() {
        ClientAnimationRegistry.registerSyncLayers(Map.of());
        Layers.clear();
        Animations.clear();
    }

    @Nullable
    public static ServerAnimation getAnimation(ResourceLocation location) {
        return Animations.getOrDefault(location, null);
    }

    public static Map<ResourceLocation, Integer> getLayers() {
        return Map.copyOf(Layers);
    }

    public static Map<ResourceLocation, ServerAnimation> getAnimations() {
        return Map.copyOf(Animations);
    }
}
