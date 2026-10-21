package io.zershyan.sccore.example.animation;

import io.zershyan.sccore.example.animation.handler.AnimatorRegisterHandler;
import io.zershyan.sccore.example.animation.handler.ClientAnimatorRegisterHandler;
import net.neoforged.bus.api.IEventBus;

public class ExampleAnimations {
    public static void doRegister(IEventBus neoEventBus) {
        neoEventBus.register(AnimatorRegisterHandler.class);
    }

    public static void doRegisterClient(IEventBus neoEventBus) {
        neoEventBus.register(ClientAnimatorRegisterHandler.class);
    }
}
