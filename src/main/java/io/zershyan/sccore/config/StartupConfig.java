package io.zershyan.sccore.config;

import io.zershyan.sccore.datagen.init.SCCConfigLang;
import net.neoforged.neoforge.common.ModConfigSpec;

public class StartupConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue enableExample;

    static {
        BUILDER.push(SCCConfigLang.DevelopmentConfig.name()).translation(SCCConfigLang.DevelopmentConfig.key());
        enableExample = BUILDER.translation(SCCConfigLang.EnableExample.key())
                .define(SCCConfigLang.EnableExample.name(), true);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
