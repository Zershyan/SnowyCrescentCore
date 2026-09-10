package io.zershyan.sccore.compat.patchouli.api.datagen.data.page;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class TextPage extends IPageType {
    @NotNull
    private final String text;
    private String title;

    public TextPage(@NotNull String text) {
        super(ResourceLocation.fromNamespaceAndPath("patchouli", "text"));
        this.text = text;
    }

    public TextPage(@NotNull ResourceLocation type, @NotNull String text) {
        super(type);
        this.text = text;
    }

    public TextPage title(String title) {
        this.title = title;
        return this;
    }

    @Override
    public JsonObject toJson(JsonObject object) {
        object.addProperty("text", text);
        if(title != null) {
            object.addProperty("title", title);
        }
        return object;
    }
}
