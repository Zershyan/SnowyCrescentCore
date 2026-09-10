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
 */
public class AnimationRegistriesHelper {

    private final IRegistriesService<?> service;

    private AnimationRegistriesHelper(IRegistriesService<?> service) {
        this.service = service;
    }

    public static AnimationRegistriesHelper getInstance() {
        Dist dist = FMLLoader.getDist();
        if (dist.isDedicatedServer() || !testIfClient()) {
            return new AnimationRegistriesHelper(new RegistriesService());
        } else {
            return new AnimationRegistriesHelper(new ClientRegistriesService());
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static boolean testIfClient() {
        Minecraft instance = Minecraft.getInstance();
        return !instance.isLocalServer();
    }

    public IRegistriesService<?> getService() {
        return service;
    }
}
