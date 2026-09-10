package io.zershyan.sccore.compat.patchouli.api.datagen.data.format;

import net.minecraft.resources.ResourceLocation;

public class RLFormat extends StringFormat {
    public RLFormat(ResourceLocation value) {
        super(value.toString());
    }
}
