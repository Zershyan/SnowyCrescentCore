package io.zershyan.sccore.compat.patchouli.api.datagen.data.page;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public abstract class RecipePage extends IPageType {
    @NotNull
    private final ResourceLocation recipe;
    private ResourceLocation recipe2;
    private String title;
    private String text;

    public RecipePage(@NotNull ResourceLocation type, @NotNull ResourceLocation recipe) {
        super(type);
        this.recipe = recipe;
    }

    public RecipePage recipe2(ResourceLocation recipe2) {
        this.recipe2 = recipe2;
        return this;
    }

    public RecipePage title(String title) {
        this.title = title;
        return this;
    }

    public RecipePage text(String text) {
        this.text = text;
        return this;
    }

    @Override
    public JsonObject toJson(JsonObject object) {
        object.addProperty("recipe", recipe.toString());
        if(recipe2 != null) {
            object.addProperty("recipe2", recipe2.toString());
        }
        if(title != null) {
            object.addProperty("title", title);
        }
        if(text != null) {
            object.addProperty("text", text);
        }
        return object;
    }
}
