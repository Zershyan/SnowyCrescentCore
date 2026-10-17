package io.zershyan.sccore.example.patchouli.datagen;

import io.zershyan.sccore.example.patchouli.datagen.provider.SCCPatchouliBookProvider;
import io.zershyan.sccore.example.patchouli.datagen.provider.SCCPatchouliContentProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/**
 * Gather data event
 */
public class SCCPatchouliDataGen {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        //1.21.1 中 book.json 生成到 data 目录
        event.createProvider(SCCPatchouliBookProvider::new);
        //1.21.1 中书籍内容生成到 assets 目录
        event.createProvider(SCCPatchouliContentProvider::new);
    }
}
