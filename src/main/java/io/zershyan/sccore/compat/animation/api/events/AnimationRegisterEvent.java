package io.zershyan.sccore.compat.animation.api.events;

import io.zershyan.sccore.compat.animation.api.builder.AnimationBuilder;
import io.zershyan.sccore.compat.animation.data.ClientAnimation;
import io.zershyan.sccore.compat.animation.data.ServerAnimation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;

import java.util.HashMap;
import java.util.Map;

/**
 * 动画注册事件，在动画注册表重载时由 NeoForge 事件总线分发。
 *
 * <p>模组监听 {@link Client} 或 {@link Server} 子类事件，通过
 * {@link Client#createAnimation} / {@link Server#createAnimation} 以链式调用注册自定义动画。
 * 事件在资源包驱动的注册之前触发，因此代码注册的动画可被资源包覆盖。</p>
 *
 * <h3>典型用法</h3>
 * <pre>{@code
 * @SubscribeEvent
 * public static void onRegister(AnimationRegisterEvent.Server event) {
 *     event.createAnimation(SCCore.id("my_anim"), SCCore.id("my_keyframe"))
 *           .priority(10)
 *           .defaultThirdPerson(true);
 * }
 * }</pre>
 *
 * @see LayerRegisterEvent
 * @see AnimationBuilder
 */
public abstract class AnimationRegisterEvent extends Event {
    /**
     * 客户端动画注册事件。
     *
     * <p>在客户端资源重载或玩家登录时触发。注册的动画会被转换为 {@link ClientAnimation}
     * 并存入 {@link io.zershyan.sccore.compat.animation.core.SyncAnimationFactory}。</p>
     */
    public static class Client extends AnimationRegisterEvent {
        private final Map<ResourceLocation, AnimationBuilder.Client> animations = new HashMap<>();
        private final Common common = new Common(false);

        public Client() {
            NeoForge.EVENT_BUS.post(common);
        }
        
        /**
         * 构建并返回所有已注册的客户端动画。
         *
         * <p>每个 {@link AnimationBuilder.Client} 会在调用时执行 {@link AnimationBuilder.Client#buildClient()}。</p>
         *
         * @return 动画 ID 到 {@link ClientAnimation} 的映射
         */
        public Map<ResourceLocation, ClientAnimation> getAnimations() {
            Map<ResourceLocation, ClientAnimation> animationMap = new HashMap<>();
            for (Map.Entry<ResourceLocation, AnimationBuilder.Client> entry : animations.entrySet()) {
                animationMap.put(entry.getKey(), entry.getValue().buildClient());
            }
            animationMap.putAll(common.getAnimationsAsClient());
            return animationMap;
        }

        /**
         * 创建并注册一个客户端动画，返回构建器以继续链式配置。
         *
         * @param location          动画的逻辑 ID（用于引用该动画）
         * @param animationLocation 关联的关键帧动画资源位置
         * @return 客户端动画构建器
         */
        public AnimationBuilder.Client createAnimation(ResourceLocation location, ResourceLocation animationLocation) {
            AnimationBuilder.Client builder = AnimationBuilder.Client.builder(animationLocation);
            animations.put(location, builder);
            return builder;
        }
    }

    /**
     * 服务端动画注册事件。
     *
     * <p>在服务端启动或资源重载时触发。注册的动画会被存入
     * {@link io.zershyan.sccore.compat.animation.core.ServerAnimationRegistry} 并在玩家登录时同步到客户端。</p>
     */
    public static class Server extends AnimationRegisterEvent {
        private final Map<ResourceLocation, AnimationBuilder.Server> animations = new HashMap<>();
        private final Common common = new Common(false);
        
        public Server() {
            NeoForge.EVENT_BUS.post(common);
        }

        /**
         * 构建并返回所有已注册的服务端动画。
         *
         * @return 动画 ID 到 {@link ServerAnimation} 的映射
         */
        public Map<ResourceLocation, ServerAnimation> getAnimations() {
            Map<ResourceLocation, ServerAnimation> animationMap = new HashMap<>();
            for (Map.Entry<ResourceLocation, AnimationBuilder.Server> entry : animations.entrySet()) {
                animationMap.put(entry.getKey(), entry.getValue().buildServer());
            }
            animationMap.putAll(common.getAnimationsAsServer());
            return animationMap;
        }

        /**
         * 创建并注册一个服务端动画，返回构建器以继续链式配置。
         *
         * @param location          动画的逻辑 ID（用于引用该动画）
         * @param animationLocation 关联的关键帧动画资源位置
         * @return 服务端动画构建器
         */
        public AnimationBuilder.Server createAnimation(ResourceLocation location, ResourceLocation animationLocation) {
            AnimationBuilder.Server builder = AnimationBuilder.Server.builder(animationLocation);
            animations.put(location, builder);
            return builder;
        }
    }

    /**
     * 通用动画注册事件。
     *
     * <p>在双端启动或资源重载时触发。注册的动画会被存入</p>
     */
    public static class Common extends AnimationRegisterEvent {
        private final boolean isClient;
        private final Map<ResourceLocation, AnimationBuilder.Common> animations = new HashMap<>();
        
        public Common(boolean isClient) {
            this.isClient = isClient;
        }

        public Map<ResourceLocation, ClientAnimation> getAnimationsAsClient() {
            Map<ResourceLocation, ClientAnimation> animationMap = new HashMap<>();
            if (!isClient) return animationMap;
            for (Map.Entry<ResourceLocation, AnimationBuilder.Common> entry : animations.entrySet()) {
                AnimationBuilder.Common value = entry.getValue();
                animationMap.put(entry.getKey(), value.buildClient());
            }
            return animationMap;
        }

        public Map<ResourceLocation, ServerAnimation> getAnimationsAsServer() {
            Map<ResourceLocation, ServerAnimation> animationMap = new HashMap<>();
            if (isClient) return animationMap;
            for (Map.Entry<ResourceLocation, AnimationBuilder.Common> entry : animations.entrySet()) {
                AnimationBuilder.Common value = entry.getValue();
                animationMap.put(entry.getKey(), value.buildServer());
            }
            return animationMap;
        }

        /**
         * 创建并注册一个通用动画，返回构建器以继续链式配置。
         *
         * @param location          动画的逻辑 ID（用于引用该动画）
         * @param animationLocation 关联的关键帧动画资源位置
         * @return 动画构建器
         */
        public AnimationBuilder.Common createAnimation(ResourceLocation location, ResourceLocation animationLocation) {
            AnimationBuilder.Common builder = AnimationBuilder.Common.builder(animationLocation);
            animations.put(location, builder);
            return builder;
        }
    } 
}
