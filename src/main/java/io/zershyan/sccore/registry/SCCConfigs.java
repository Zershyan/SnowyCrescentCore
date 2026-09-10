package io.zershyan.sccore.registry;

import io.zershyan.sccore.config.ServerConfig;
import io.zershyan.sccore.config.StartupConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public class SCCConfigs {
    public static void register(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.STARTUP, StartupConfig.SPEC);
        if (FMLLoader.getDist().isClient()) registerClient(modContainer);
    }

    public static void registerClient(ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
