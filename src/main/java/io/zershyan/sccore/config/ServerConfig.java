package io.zershyan.sccore.config;

import io.zershyan.sccore.datagen.init.SCCConfigLang;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ServerConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    //invite
    public static final ModConfigSpec.ConfigValue<Integer> inviteValidTime;
    public static final ModConfigSpec.ConfigValue<Integer> inviteValidDistance;
    public static final ModConfigSpec.ConfigValue<Integer> inviteCooldown;
    //apply
    public static final ModConfigSpec.ConfigValue<Integer> applyValidTime;
    public static final ModConfigSpec.ConfigValue<Integer> applyValidDistance;
    public static final ModConfigSpec.ConfigValue<Integer> applyCooldown;
    //request
    public static final ModConfigSpec.ConfigValue<Integer> requestValidTime;
    public static final ModConfigSpec.ConfigValue<Integer> requestCooldown;

    static {
        BUILDER.push("Animation");
        //invite
        inviteValidTime = BUILDER.comment("Animation invite valid time. Ignore when zero. (seconds)")
                .translation(SCCConfigLang.InviteValidTime.key())
                .defineInRange(SCCConfigLang.InviteValidTime.name(), 120, 0, Integer.MAX_VALUE);
        inviteValidDistance = BUILDER.comment("Animation invite max distance. Ignore when zero. (blocks)")
                .translation(SCCConfigLang.InviteValidDistance.key())
                .defineInRange(SCCConfigLang.InviteValidDistance.name(), 6, 0, Integer.MAX_VALUE);
        inviteCooldown = BUILDER.comment("Animation invite cooldown. (seconds)")
                .translation(SCCConfigLang.InviteCooldown.key())
                .defineInRange(SCCConfigLang.InviteCooldown.name(), 60, 0, Integer.MAX_VALUE);

        //apply
        applyValidTime = BUILDER.comment("Animation apply valid time. Ignore when zero. (seconds)")
                .translation(SCCConfigLang.ApplyValidTime.key())
                .defineInRange(SCCConfigLang.ApplyValidTime.name(), 120, 0, Integer.MAX_VALUE);
        applyValidDistance = BUILDER.comment("Animation apply max distance. Ignore when zero. (blocks)")
                .translation(SCCConfigLang.ApplyValidDistance.key())
                .defineInRange(SCCConfigLang.ApplyValidDistance.name(), 6, 0, Integer.MAX_VALUE);
        applyCooldown = BUILDER.comment("Animation apply cooldown. (seconds)")
                .translation(SCCConfigLang.ApplyCooldown.key())
                .defineInRange(SCCConfigLang.ApplyCooldown.name(), 60, 0, Integer.MAX_VALUE);

        //request
        requestValidTime = BUILDER.comment("Animation request valid time. Ignore when zero (seconds)")
                .translation(SCCConfigLang.RequestValidTime.key())
                .defineInRange(SCCConfigLang.RequestValidTime.name(), 120, 0, Integer.MAX_VALUE);
        requestCooldown = BUILDER.comment("Animation request cooldown. (seconds)")
                .translation(SCCConfigLang.RequestCooldown.key())
                .defineInRange(SCCConfigLang.RequestCooldown.name(), 60, 0, Integer.MAX_VALUE);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
