package io.zershyan.sccore.datagen;

import io.zershyan.sccore.SCCore;
import io.zershyan.sccore.datagen.provider.SCCLangProvider;
import io.zershyan.sccore.datagen.provider.SCCPackMetadataProvider;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = SCCore.MODID)
public class DataGenHandler {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        event.createProvider(SCCPackMetadataProvider::new);
        event.createProvider(SCCLangProvider::runEnUs);
        event.createProvider(SCCLangProvider::runZhCn);
    }
}
