package io.zershyan.sccore.datagen.util;

import io.zershyan.sccore.SCCore;

public record ConfigLangEntry(String name) {
    public String key() {
        return SCCore.MODID + ".configuration." + name;
    }
}
