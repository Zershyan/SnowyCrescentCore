package io.zershyan.sccore.compat.patchouli.api.datagen.data.page;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class LinkPage extends TextPage {
    @NotNull
    private final String url;
    @NotNull
    private final String linkText;
    private String text;

    public LinkPage(@NotNull String url, @NotNull String linkText) {
        super(ResourceLocation.fromNamespaceAndPath("patchouli", "link"), "");
        this.url = url;
        this.linkText = linkText;
    }

    public LinkPage text(String text) {
        this.text = text;
        return this;
    }

    @Override
    public JsonObject toJson(JsonObject object) {
        super.toJson(object);
        object.remove("text");
        if (text != null) {
            object.addProperty("text", text);
        }
        object.addProperty("url", url);
        object.addProperty("link_text", linkText);
        return object;
    }
}
