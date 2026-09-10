package io.zershyan.sccore.compat.patchouli.api.datagen.data.component;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

public class SeparatorComponent extends ITemplateComponent {
    public SeparatorComponent() {
        super(ResourceLocation.fromNamespaceAndPath("patchouli", "separator"));
    }

    @Override
    public JsonObject toJson(JsonObject object) {
        return object;
    }
}
