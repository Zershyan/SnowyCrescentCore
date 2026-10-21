package io.zershyan.sccore;


import io.zershyan.sccore.compat.SCCCompatFactory;
import io.zershyan.sccore.registry.config.StartupConfig;
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
import org.slf4j.LoggerFactory;

@Mod(SCCore.MODID)
public class SCCore {
    public static final Logger LOGGER = LoggerFactory.getLogger(SCCore.class);
    public static final String MODID = "sccore";
    public static final String NAME = "Snowy Crescent Core";

    public SCCore(IEventBus modEventBus, Dist dist, ModContainer modContainer) {
        IEventBus neoEventBus = NeoForge.EVENT_BUS;

        SCCPackets.doRegister(modEventBus);
        SCCConfigs.doRegister(modContainer);
        SCCCommands.doRegister(modEventBus);
        SCCCompatFactory.register(neoEventBus, modEventBus);

        boolean needExample = !FMLEnvironment.production && StartupConfig.enableExample.get();
        if (needExample) {
            ExampleAnimations.doRegister(neoEventBus);
            ExamplePatchouli.doRegister(neoEventBus, modEventBus);
            if (dist.isClient()) {
                ExampleAnimations.doRegisterClient(neoEventBus);
            }
        }
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
