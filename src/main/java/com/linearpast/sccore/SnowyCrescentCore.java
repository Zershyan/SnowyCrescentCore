package com.linearpast.sccore;


import com.linearpast.sccore.animation.service.IAnimationService;
import com.linearpast.sccore.capability.CapabilityUtils;
import com.linearpast.sccore.core.ModChannel;
import com.linearpast.sccore.core.ModCommands;
import com.linearpast.sccore.core.configs.ModConfigs;
import com.linearpast.sccore.example.animation.ModAnimation;
import com.linearpast.sccore.example.capability.ModCapability;
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
