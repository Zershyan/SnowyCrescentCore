package io.zershyan.sccore.compat.patchouli.api.datagen.data.component;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class CustomComponent extends ITemplateComponent {
    @NotNull
    private final Class<?> aClass;

    public CustomComponent(@NotNull Class<?> aClass) {
        super(ResourceLocation.fromNamespaceAndPath("patchouli", "custom"));
        this.aClass = aClass;
    }

    @Override
    public JsonObject toJson(JsonObject object) {
        object.addProperty("class", aClass.getName());
        return object;
    }
}
