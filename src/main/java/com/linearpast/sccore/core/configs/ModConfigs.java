package com.linearpast.sccore.core.configs;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModConfigs {
    public enum ConfigName {
        inviteDuration("inviteDuration"),
        inviteDistance("inviteDistance"),
        inviteCooldown("inviteCooldown"),
        requestDuration("requestDuration"),
        requestCooldown("requestCooldown"),
        applyDistance("applyDistance"),
        applyDuration("applyDuration"),
        applyCooldown("applyCooldown"),
        ;

        private final String name;
        ConfigName(String name){
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
    public static class Server {
        public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
        public static final ForgeConfigSpec SPEC;

        public static final ForgeConfigSpec.ConfigValue<Integer> inviteDuration;
        public static final ForgeConfigSpec.ConfigValue<Integer> inviteDistance;
        public static final ForgeConfigSpec.ConfigValue<Integer> inviteCooldown;
        public static final ForgeConfigSpec.ConfigValue<Integer> requestDuration;
        public static final ForgeConfigSpec.ConfigValue<Integer> requestCooldown;
        public static final ForgeConfigSpec.ConfigValue<Integer> applyDistance;
        public static final ForgeConfigSpec.ConfigValue<Integer> applyDuration;
        public static final ForgeConfigSpec.ConfigValue<Integer> applyCooldown;

        static {
            BUILDER.push("Animation");
            inviteDuration = BUILDER.comment("Animation invite duration. Ignore when zero. (seconds)")
                    .defineInRange(ConfigName.inviteDuration.name, 120, 0, Integer.MAX_VALUE);
            inviteDistance = BUILDER.comment("Animation invite max distance. Ignore when zero. (blocks)")
                    .defineInRange(ConfigName.inviteDistance.name, 6, 0, Integer.MAX_VALUE);
            inviteCooldown = BUILDER.comment("Animation invite cooldown. (seconds)")
                    .defineInRange(ConfigName.inviteCooldown.name, 60, 0, Integer.MAX_VALUE);
            requestDuration = BUILDER.comment("Animation request duration. Ignore when zero (seconds)")
                    .defineInRange(ConfigName.requestDuration.name, 120, 0, Integer.MAX_VALUE);
            requestCooldown = BUILDER.comment("Animation request cooldown. (seconds)")
                    .defineInRange(ConfigName.requestCooldown.name, 60, 0, Integer.MAX_VALUE);
            applyDuration = BUILDER.comment("Animation apply duration. Ignore when zero. (seconds)")
                    .defineInRange(ConfigName.applyDuration.name, 120, 0, Integer.MAX_VALUE);
            applyDistance = BUILDER.comment("Animation apply max distance. Ignore when zero. (blocks)")
                    .defineInRange(ConfigName.applyDistance.name, 6, 0, Integer.MAX_VALUE);
            applyCooldown = BUILDER.comment("Animation apply cooldown. (seconds)")
                    .defineInRange(ConfigName.applyCooldown.name, 60, 0, Integer.MAX_VALUE);
            BUILDER.pop();
            SPEC = BUILDER.build();
        }
    }
}
