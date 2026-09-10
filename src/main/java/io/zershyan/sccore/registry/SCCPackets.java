package io.zershyan.sccore.registry;

import io.zershyan.sccore.SCCore;
import io.zershyan.sccore.compat.SCCCompatFactory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforgespi.language.IModInfo;
import org.jetbrains.annotations.NotNull;

public class SCCPackets {
    @NotNull
    private static final String PROTOCOL_VERSION = ModList.get()
            .getModContainerById(SCCore.MODID)
            .map(ModContainer::getModInfo)
            .map(IModInfo::getVersion)
            .map(Object::toString)
            .orElse("unknown");

    public static void payloadRegister(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        SCCCompatFactory.registerNetwork(registrar);
    }

    public static void register(IEventBus eventBus) {
        eventBus.addListener(SCCPackets::payloadRegister);
    }
}
