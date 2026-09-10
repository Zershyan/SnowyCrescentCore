package io.zershyan.sccore.compat;

import io.zershyan.sccore.compat.animation.SCCoreAnimation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.HashSet;
import java.util.Set;

public class SCCCompatFactory {
    private static final Set<ICompatUtils> compatUtils = new HashSet<>();

    public static final SCCoreAnimation PlayerAnimator = addCompat(new SCCoreAnimation());

    private static <T extends ICompatUtils> T addCompat(T utils) {
        if (compatUtils.add(utils)) {
            return utils;
        } else throw new IllegalStateException("CompatUtils already added.");
    }

    public static void register(IEventBus forgeEventBus, IEventBus modEventBus) {
        compatUtils.forEach(compatUtils -> compatUtils.initial(forgeEventBus, modEventBus));
    }

    public static void registerNetwork(PayloadRegistrar registrar) {
        compatUtils.forEach(compatUtils -> compatUtils.initNetwork(registrar));
    }
}
