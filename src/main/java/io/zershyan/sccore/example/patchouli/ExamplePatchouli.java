package io.zershyan.sccore.example.patchouli;

import io.zershyan.sccore.SCCore;
import io.zershyan.sccore.example.patchouli.datagen.SCCPatchouliDataGen;
import net.neoforged.bus.api.IEventBus;

public class ExamplePatchouli {
    /**
     * @param forgeBus
     * @param modBus
     * @see SCCore#SCCore
     */
    public static void register(IEventBus forgeBus, IEventBus modBus) {
        modBus.addListener(SCCPatchouliDataGen::gatherData);
    }
}
