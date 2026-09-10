package io.zershyan.sccore.compat.animation.api.events;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;

import java.util.HashMap;
import java.util.Map;

/**
 * 动画层注册事件，在动画注册表重载时由 NeoForge 事件总线分发。
 *
 * <p>模组监听 {@link Client} 或 {@link Server} 子类事件，通过
 * {@link #registerLayer(ResourceLocation, Integer)} 注册自定义动画层及其优先级。
 * 层的优先级决定了 Player Animator 的 {@code AnimationStack} 中各层的叠加顺序。</p>
 *
 * <h3>典型用法</h3>
 * <pre>{@code
 * @SubscribeEvent
 * public static void onRegister(LayerRegisterEvent.Server event) {
 *     event.registerLayer(SCCore.id("my_layer"), 50);
 * }
 * }</pre>
 *
 * @see AnimationRegisterEvent
 */
public abstract class LayerRegisterEvent extends Event {
    private final Map<ResourceLocation, Integer> layers = new HashMap<>();

    /**
     * 获取所有已注册的动画层。
     *
     * @return 动画层资源位置到优先级的映射
     */
    public Map<ResourceLocation, Integer> getLayers() {
        return layers;
    }

    /**
     * 注册一个动画层。
     *
     * @param key   动画层的资源位置
     * @param value 层优先级（数值越大越靠前）
     */
    public void registerLayer(ResourceLocation key, Integer value) {
        layers.put(key, value);
    }

    /** 客户端动画层注册事件，在客户端资源重载或玩家登录时触发。 */
    public static class Client extends LayerRegisterEvent { }

    /** 服务端动画层注册事件，在服务端启动或资源重载时触发。 */
    public static class Server extends LayerRegisterEvent { }
}
