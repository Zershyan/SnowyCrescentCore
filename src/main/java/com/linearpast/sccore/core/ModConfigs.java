package com.linearpast.sccore.core;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModConfigs {
    public static class Common {
        public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
        public static final ForgeConfigSpec SPEC;

        public static final ForgeConfigSpec.ConfigValue<Boolean> enableExample;

        static {
            BUILDER.push("Development");
            enableExample = BUILDER.comment("Enable some example for lib.")
                    .define("enableExample", false);
            BUILDER.pop();
            SPEC = BUILDER.build();
        }
    }
}
