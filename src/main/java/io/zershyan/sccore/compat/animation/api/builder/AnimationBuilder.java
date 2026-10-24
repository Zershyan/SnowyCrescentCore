package io.zershyan.sccore.compat.animation.api.builder;

import io.zershyan.sccore.compat.animation.data.*;
import io.zershyan.sccore.compat.animation.data.camera.CameraChange;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.UnaryOperator;

/**
 * 动画构建器的抽象基类，用于以链式调用方式构建 {@link Animation} 实例。
 *
 * <p>提供动画的通用属性（名称、优先级、骑乘数据、第三人称默认），
 * 由子类 {@link Server} 与 {@link Client} 分别补充各自独有的属性。</p>
 *
 * <h3>典型用法（在 {@code AnimationRegisterEvent} 中）</h3>
 * <pre>{@code
 * event.createAnimation(animId, keyframeLoc)
 *       .priority(10)
 *       .rideData(rd -> rd.offset(new Vec3(0, 1, 0)).existTick(100))
 *       .defaultThirdPerson(true);
 * }</pre>
 *
 * @see Server
 * @see Client
 * @see RideDataBuilder
 * @see io.zershyan.sccore.compat.animation.api.events.AnimationRegisterEvent
 */
public abstract class AnimationBuilder<T extends AnimationBuilder<?>> {
    protected final ResourceLocation animationLocation;
    @Nullable
    protected String name = null;
    protected int priority = 0;
    @Nullable
    protected RideData rideData = null;
    protected boolean defaultThirdPerson = false;
    private T builder;

    /**
     * @param animationLocation 关联的关键帧动画资源位置
     */
    protected AnimationBuilder(ResourceLocation animationLocation) {
        this.animationLocation = animationLocation;
    }

    void setBuilder(T builder) {
        this.builder = builder;
    }

    /**
     * 设置动画的可选名称。
     */
    public T name(@Nullable String name) {
        this.name = name;
        return builder;
    }

    /**
     * 设置动画优先级，数值越高优先级越高。
     */
    public T priority(int priority) {
        this.priority = priority;
        return builder;
    }

    /**
     * 通过 {@link RideDataBuilder} 配置骑乘数据。
     *
     * @param operator 对新建的 {@link RideDataBuilder} 执行配置并返回
     * @return 当前构建器
     */
    public T rideData(UnaryOperator<RideDataBuilder> operator) {
        this.rideData = operator.apply(new RideDataBuilder()).build();
        return builder;
    }

    /**
     * 设置动画是否默认切换到第三人称视角。
     */
    public T defaultThirdPerson(boolean defaultThirdPerson) {
        this.defaultThirdPerson = defaultThirdPerson;
        return builder;
    }

    /**
     * 骑乘数据构建器，用于配置 {@link RideData} 的各字段。
     *
     * <p>通过 {@link AnimationBuilder#rideData} 获取实例，链式配置后由 {@link #build()} 产出不可变的 {@link RideData}。</p>
     *
     * @see RideData
     */
    public static class RideDataBuilder {
        private final List<ResourceLocation> componentAnimations = new ArrayList<>();
        private Vec3 offset = Vec3.ZERO;
        private int existTick = -1;
        private float xRot = 0;
        private float yRot = 0;

        /**
         * 设置骑乘偏移量，相对于车主玩家位置。
         */
        public RideDataBuilder offset(Vec3 offset) {
            this.offset = offset;
            return this;
        }

        /**
         * 设置骑乘存在时长（tick），-1 表示无限。
         */
        public RideDataBuilder existTick(int existTick) {
            this.existTick = existTick;
            return this;
        }

        /**
         * 设置骑乘实体的 X 轴旋转（俯仰）。
         */
        public RideDataBuilder xRot(float xRot) {
            this.xRot = xRot;
            return this;
        }

        /**
         * 设置骑乘实体的 Y 轴旋转（偏航）。
         */
        public RideDataBuilder yRot(float yRot) {
            this.yRot = yRot;
            return this;
        }

        /**
         * 替换所有组件动画。
         */
        public RideDataBuilder componentAnimations(ResourceLocation... locations) {
            this.componentAnimations.clear();
            this.componentAnimations.addAll(Arrays.stream(locations).toList());
            return this;
        }

        /**
         * 替换所有组件动画。
         */
        public RideDataBuilder componentAnimations(Collection<ResourceLocation> locations) {
            this.componentAnimations.clear();
            this.componentAnimations.addAll(locations);
            return this;
        }

        /**
         * 追加一个组件动画。
         */
        public RideDataBuilder addComponentAnimation(ResourceLocation location) {
            this.componentAnimations.add(location);
            return this;
        }

        /**
         * 构建不可变的骑乘数据。
         */
        public RideData build() {
            return new RideData(
                    componentAnimations,
                    offset,
                    existTick,
                    xRot,
                    yRot
            );
        }
    }

    /**
     * 服务端动画构建器：通用属性 + AABB 移动 + 跳跃修正 + 相机变换。
     *
     * @see ServerAnimation
     */
    public static class Server extends AnimationBuilder<Server> {
        private final AABBMovement aabbMovement = new AABBMovement();
        private float jumpModifier = 1.0f;
        @NotNull
        private CameraChange firstPersonCameraChange = new CameraChange();
        @NotNull
        private CameraChange cameraChange = new CameraChange();

        private Server(ResourceLocation animationLocation) {
            super(animationLocation);
            this.setBuilder(this);
        }

        /**
         * 创建服务端动画构建器。
         */
        public static Server builder(ResourceLocation location) {
            return new Server(location);
        }

        /**
         * 替换整个 AABB 移动时间线。
         */
        public Server aabbMovement(TreeMap<Integer, AABB> aabbMovement) {
            this.aabbMovement.getMovementTree().clear();
            this.aabbMovement.getMovementTree().putAll(aabbMovement);
            return this;
        }

        /**
         * 在指定 tick 处添加一个 AABB 关键帧。
         */
        public Server addAABBMovement(int tick, AABB aabb) {
            this.aabbMovement.add(tick, aabb);
            return this;
        }

        /**
         * 设置移动时间线是否相对玩家朝向。
         */
        public Server addAABBMovementRelative(boolean relative) {
            this.aabbMovement.relative(relative);
            return this;
        }

        /**
         * 设置跳跃力度修正系数，默认 1.0。
         */
        public Server jumpModifier(float jumpModifier) {
            this.jumpModifier = jumpModifier;
            return this;
        }

        /**
         * 设置第一人称相机变换。
         */
        public Server firstPersonCameraChange(CameraChange firstPersonCameraChange) {
            this.firstPersonCameraChange = firstPersonCameraChange;
            return this;
        }

        /**
         * 设置第三人称相机变换。
         */
        public Server cameraChange(CameraChange cameraChange) {
            this.cameraChange = cameraChange;
            return this;
        }

        public ServerAnimation buildServer() {
            return new ServerAnimation(
                    animationLocation,
                    Optional.ofNullable(name),
                    priority,
                    Optional.ofNullable(rideData),
                    defaultThirdPerson,
                    aabbMovement,
                    jumpModifier,
                    firstPersonCameraChange,
                    cameraChange
            );
        }
    }

    /**
     * 客户端动画构建器：通用属性 + 相机变换，没有 AABB。
     *
     * @see ClientAnimation
     */
    public static class Client extends AnimationBuilder<Client> {
        @NotNull
        private CameraChange firstPersonCameraChange = new CameraChange();
        @NotNull
        private CameraChange cameraChange = new CameraChange();

        private Client(ResourceLocation animationLocation) {
            super(animationLocation);
            this.setBuilder(this);
        }

        /**
         * 创建客户端动画构建器。
         */
        public static Client builder(ResourceLocation location) {
            return new Client(location);
        }

        /**
         * 设置第一人称相机变换。
         */
        public Client firstPersonCameraChange(CameraChange firstPersonCameraChange) {
            this.firstPersonCameraChange = firstPersonCameraChange;
            return this;
        }

        /**
         * 设置第三人称相机变换。
         */
        public Client cameraChange(CameraChange cameraChange) {
            this.cameraChange = cameraChange;
            return this;
        }

        public ClientAnimation buildClient() {
            return new ClientAnimation(
                    animationLocation,
                    Optional.ofNullable(name),
                    priority,
                    Optional.ofNullable(rideData),
                    defaultThirdPerson,
                    firstPersonCameraChange,
                    cameraChange
            );
        }
    }
}
