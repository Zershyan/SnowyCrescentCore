package io.zershyan.sccore.compat.patchouli.api.datagen.data.page;

import com.google.gson.JsonObject;
import io.zershyan.sccore.compat.patchouli.api.datagen.data.format.ItemFormat;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class SpotlightPage extends IPageType {
    @NotNull
    private final ItemFormat.Multi item;
    private String title;
    private String text;
    private Boolean linkRecipe;

    public SpotlightPage(ItemFormat.@NotNull Multi item) {
        super(ResourceLocation.fromNamespaceAndPath("patchouli", "spotlight"));
        this.item = item;
    }

    public SpotlightPage title(String title) {
        this.title = title;
        return this;
    }

    public SpotlightPage text(String text) {
        this.text = text;
        return this;
    }

    public SpotlightPage linkRecipe(Boolean linkRecipe) {
        this.linkRecipe = linkRecipe;
        return this;
    }

    @Override
    public JsonObject toJson(JsonObject object) {
        object.addProperty("item", item.parse());
        if (title != null) {
            object.addProperty("title", title);
        }
        if (text != null) {
            object.addProperty("text", text);
        }
        if (linkRecipe != null) {
            object.addProperty("link_recipe", linkRecipe);
        }
        return object;
    }
}
