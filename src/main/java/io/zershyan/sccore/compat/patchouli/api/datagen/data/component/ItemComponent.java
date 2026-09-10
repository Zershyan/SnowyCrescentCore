package io.zershyan.sccore.compat.patchouli.api.datagen.data.component;

import com.google.gson.JsonObject;
import io.zershyan.sccore.compat.patchouli.api.datagen.data.format.ItemFormat;
import io.zershyan.sccore.compat.patchouli.api.datagen.data.format.Variable;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ItemComponent extends ITemplateComponent{
    @NotNull
    private final Variable<ItemFormat.Multi> item;
    private Boolean framed;
    private Boolean linkRecipe;

    public ItemComponent(@NotNull Variable<ItemFormat.Multi> item) {
        super(ResourceLocation.fromNamespaceAndPath("patchouli", "item"));
        this.item = item;
    }

    public ItemComponent framed(Boolean framed) {
        this.framed = framed;
        return this;
    }

    public ItemComponent linkRecipe(Boolean linkRecipe) {
        this.linkRecipe = linkRecipe;
        return this;
    }

    @Override
    public JsonObject toJson(JsonObject object) {
        object.addProperty("item", item.parseKey());
        if(framed != null) {
            object.addProperty("framed", framed);
        }
        if(linkRecipe != null) {
            object.addProperty("linkRecipe", linkRecipe);
        }
        return object;
    }
}
