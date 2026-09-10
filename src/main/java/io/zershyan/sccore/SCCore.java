package io.zershyan.sccore;


import com.mojang.logging.LogUtils;
import io.zershyan.sccore.compat.SCCCompatFactory;
import io.zershyan.sccore.config.StartupConfig;
import io.zershyan.sccore.example.animation.ExampleAnimations;
import io.zershyan.sccore.example.patchouli.ExamplePatchouli;
import io.zershyan.sccore.registry.SCCCommands;
import io.zershyan.sccore.registry.SCCConfigs;
import io.zershyan.sccore.registry.SCCPackets;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(SCCore.MODID)
public class SCCore {
    public static final Logger log = LogUtils.getLogger();
    public static final String MODID = "sccore";
    public static final String NAME = "Snowy Crescent Core";

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public SCCore(IEventBus modEventBus, Dist dist, ModContainer modContainer) {
        IEventBus neoEventBus = NeoForge.EVENT_BUS;

        SCCPackets.register(modEventBus);
        SCCConfigs.register(modContainer);
        SCCCommands.register(neoEventBus, modEventBus);
        SCCCompatFactory.register(neoEventBus, modEventBus);

        boolean needExample = !FMLEnvironment.production && StartupConfig.enableExample.get();
        if(needExample) {
            ExampleAnimations.register(neoEventBus);
            ExamplePatchouli.register(neoEventBus, modEventBus);
            if(dist.isClient()) {
                ExampleAnimations.registerClient(neoEventBus);
            }
        }
    }
}
