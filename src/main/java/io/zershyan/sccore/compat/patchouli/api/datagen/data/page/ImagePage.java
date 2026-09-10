package io.zershyan.sccore.compat.patchouli.api.datagen.data.page;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImagePage extends IPageType {
    @NotNull
    private final List<ResourceLocation> images = new ArrayList<>();
    private String title;
    private String text;
    private Boolean border;

    public ImagePage() {
        super(ResourceLocation.fromNamespaceAndPath("patchouli", "image"));
    }

    public ImagePage addImage(ResourceLocation ... images) {
        this.images.addAll(Arrays.asList(images));
        return this;
    }

    public ImagePage addImage(List<ResourceLocation> images) {
        this.images.addAll(images);
        return this;
    }

    public ImagePage images(List<ResourceLocation> images) {
        this.images.clear();
        this.images.addAll(images);
        return this;
    }

    public ImagePage title(String title) {
        this.title = title;
        return this;
    }

    public ImagePage text(String text) {
        this.text = text;
        return this;
    }

    public ImagePage border(Boolean border) {
        this.border = border;
        return this;
    }

    @Override
    public JsonObject toJson(JsonObject object) {
        if(images.isEmpty()) {
            throw new JsonSyntaxException("images cannot be empty");
        }
        JsonArray array = new JsonArray();
        for (ResourceLocation image : images) {
            array.add(image.toString());
        }
        object.add("images", array);
        if(title != null) {
            object.addProperty("title", title);
        }
        if(text != null) {
            object.addProperty("text", text);
        }
        if(border != null) {
            object.addProperty("border", border);
        }
        return object;
    }
}
