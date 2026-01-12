package io.zershyan.sccore.core.configs;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModConfigs {
    public enum ConfigName {
        inviteValidTime("inviteValidTime"),
        inviteValidDistance("inviteValidDistance"),
        inviteCooldown("inviteCooldown"),

        applyValidTime("applyValidTime"),
        applyValidDistance("applyValidDistance"),
        applyCooldown("applyCooldown"),

        requestValidTime("requestValidTime"),
        requestCooldown("requestCooldown"),
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

        //invite
        public static final ForgeConfigSpec.ConfigValue<Integer> inviteValidTime;
        public static final ForgeConfigSpec.ConfigValue<Integer> inviteValidDistance;
        public static final ForgeConfigSpec.ConfigValue<Integer> inviteCooldown;
        //apply
        public static final ForgeConfigSpec.ConfigValue<Integer> applyValidTime;
        public static final ForgeConfigSpec.ConfigValue<Integer> applyValidDistance;
        public static final ForgeConfigSpec.ConfigValue<Integer> applyCooldown;
        //request
        public static final ForgeConfigSpec.ConfigValue<Integer> requestValidTime;
        public static final ForgeConfigSpec.ConfigValue<Integer> requestCooldown;

        static {
            BUILDER.push("Animation");
            //invite
            inviteValidTime = BUILDER.comment("Animation invite valid time. Ignore when zero. (seconds)")
                    .defineInRange(ConfigName.inviteValidTime.name, 120, 0, Integer.MAX_VALUE);
            inviteValidDistance = BUILDER.comment("Animation invite max distance. Ignore when zero. (blocks)")
                    .defineInRange(ConfigName.inviteValidDistance.name, 6, 0, Integer.MAX_VALUE);
            inviteCooldown = BUILDER.comment("Animation invite cooldown. (seconds)")
                    .defineInRange(ConfigName.inviteCooldown.name, 60, 0, Integer.MAX_VALUE);

            //apply
            applyValidTime = BUILDER.comment("Animation apply valid time. Ignore when zero. (seconds)")
                    .defineInRange(ConfigName.applyValidTime.name, 120, 0, Integer.MAX_VALUE);
            applyValidDistance = BUILDER.comment("Animation apply max distance. Ignore when zero. (blocks)")
                    .defineInRange(ConfigName.applyValidDistance.name, 6, 0, Integer.MAX_VALUE);
            applyCooldown = BUILDER.comment("Animation apply cooldown. (seconds)")
                    .defineInRange(ConfigName.applyCooldown.name, 60, 0, Integer.MAX_VALUE);

            //request
            requestValidTime = BUILDER.comment("Animation request valid time. Ignore when zero (seconds)")
                    .defineInRange(ConfigName.requestValidTime.name, 120, 0, Integer.MAX_VALUE);
            requestCooldown = BUILDER.comment("Animation request cooldown. (seconds)")
                    .defineInRange(ConfigName.requestCooldown.name, 60, 0, Integer.MAX_VALUE);

            BUILDER.pop();
            SPEC = BUILDER.build();
        }
    }
}
