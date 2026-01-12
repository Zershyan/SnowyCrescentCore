package io.zershyan.sccore;


import io.zershyan.sccore.animation.service.IAnimationService;
import io.zershyan.sccore.capability.CapabilityUtils;
import io.zershyan.sccore.core.ModChannel;
import io.zershyan.sccore.core.ModCommands;
import io.zershyan.sccore.core.configs.ModConfigs;
import io.zershyan.sccore.example.animation.ModAnimation;
import io.zershyan.sccore.example.capability.ModCapability;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Mod(SnowyCrescentCore.MODID)
public class SnowyCrescentCore {
    public static final Logger log = LoggerFactory.getLogger(SnowyCrescentCore.class);
    public static final String MODID = "sccore";
    public static final String ENABLE_EXAMPLES_PROPERTY_KEY = "sccore.enable_examples";

    public SnowyCrescentCore() {
        ModLoadingContext modLoadingContext = ModLoadingContext.get();
        modLoadingContext.registerConfig(ModConfig.Type.SERVER, ModConfigs.Server.SPEC);

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;

        CapabilityUtils.registerHandler(forgeBus);
        ModChannel.register();
        IAnimationService.register(forgeBus, modBus);
        ModCommands.registerCommands(forgeBus, modBus);

        if(!FMLEnvironment.production || Boolean.getBoolean(ENABLE_EXAMPLES_PROPERTY_KEY)) {
            ModCapability.register();
            ModCapability.addListenerToEvent(forgeBus);
            ModAnimation.register(forgeBus, modBus);
        }
    }
}
