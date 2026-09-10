package io.zershyan.sccore.compat.patchouli.api.datagen.data.format;

import java.util.Arrays;
import java.util.List;

public class ConfigFlags implements IFormat {
    private boolean negation = false;
    private String value;

    ConfigFlags(String value) {
        this.value = value;
    }

    ConfigFlags() {
    }

    public static ConfigFlags debug() {
        return new ConfigFlags("debug");
    }

    public static ConfigFlags advancementsDisabled() {
        return new ConfigFlags("advancements_disabled");
    }

    public static ConfigFlags testingMode() {
        return new ConfigFlags("testing_mode");
    }

    public static ConfigFlags hasMod(String modid) {
        return new ConfigFlags("mod:" + modid);
    }

    public static ConfigFlags custom(String custom) {
        return new ConfigFlags(custom);
    }

    public ConfigFlags negation() {
        negation = true;
        return this;
    }

    public String parse() {
        return (negation ? "!" : "") + value;
    }

    public static class Multi extends ConfigFlags {

        List<ConfigFlags> flags;
        private Boolean junction;
        private String customPrefix;

        Multi(boolean junction, ConfigFlags... flags) {
            this.junction = junction;
            this.flags = Arrays.asList(flags);
        }

        Multi(String customPrefix, ConfigFlags... flags) {
            this.customPrefix = customPrefix;
            this.flags = Arrays.asList(flags);
        }

        public static Multi conjunction(ConfigFlags... flags) {
            return new Multi(true, flags);
        }

        public static Multi disJunction(ConfigFlags... flags) {
            return new Multi(false, flags);
        }

        public static Multi of(String customPrefix, ConfigFlags... flags) {
            return new Multi(customPrefix, flags);
        }

        @Override
        public String parse() {
            String prefix;
            if (junction != null) {
                prefix = junction ? "&" : "|";
            } else if (customPrefix != null) {
                prefix = customPrefix;
            } else throw new NullPointerException("Junction or Custom prefix required");

            StringBuilder sb = new StringBuilder(prefix);
            for (ConfigFlags flag : flags) {
                sb.append(flag.parse());
                sb.append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
            return sb.toString();
        }
    }
}


