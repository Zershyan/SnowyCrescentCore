package io.zershyan.sccore.compat.animation.api.helper;

import io.zershyan.sccore.compat.animation.api.service.IRegistriesService;
import io.zershyan.sccore.compat.animation.api.service.impl.ClientRegistriesService;
import io.zershyan.sccore.compat.animation.api.service.impl.RegistriesService;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLLoader;

/**
 * 获取动画或动画层数据实例
 *
 * <p>实现按调用所处的逻辑端选择注册表：专用服务端，以及物理客户端上的集成服务端线程，
 * 都只能看到服务端注册表；客户端主线程读取客户端注册表（本地注册 + 服务端同步）。
 * 单人存档中服务端线程与客户端线程共享同一进程，因此不能仅凭物理端判定。</p>
 */
public class AnimationRegistriesHelper {

    private final IRegistriesService<?> service;

    private AnimationRegistriesHelper(IRegistriesService<?> service) {
        this.service = service;
    }

    public static AnimationRegistriesHelper getInstance() {
        if (FMLLoader.getDist().isDedicatedServer() || !isClientThread()) {
            return new AnimationRegistriesHelper(new RegistriesService());
        }
        return new AnimationRegistriesHelper(new ClientRegistriesService());
    }

    /**
     * 判断当前线程是否为物理客户端的客户端线程。
     *
     * @return 当前线程持有客户端逻辑时为 {@code true}
     */
    @OnlyIn(Dist.CLIENT)
    private static boolean isClientThread() {
        Minecraft instance = Minecraft.getInstance();
        return instance.isSameThread();
    }

    public IRegistriesService<?> getService() {
        return service;
    }
}
