package io.zershyan.sccore.compat.patchouli.api.datagen.data.component;

import com.google.gson.JsonObject;
import io.zershyan.sccore.compat.patchouli.api.datagen.data.format.RLFormat;
import io.zershyan.sccore.compat.patchouli.api.datagen.data.format.Variable;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ImageComponent extends ITemplateComponent {
    @NotNull
    private final Variable<RLFormat> image;
    @NotNull
    private final Integer width;
    @NotNull
    private final Integer height;
    private Integer u;
    private Integer v;
    private Integer textureWidth;
    private Integer textureHeight;
    private Float scale;

    public ImageComponent(
            @NotNull Variable<RLFormat> image,
            @NotNull Integer width,
            @NotNull Integer height
    ) {
        super(ResourceLocation.fromNamespaceAndPath("patchouli", "image"));
        this.image = image;
        this.width = width;
        this.height = height;
    }

    public ImageComponent u(Integer u) {
        this.u = u;
        return this;
    }

    public ImageComponent v(Integer v) {
        this.v = v;
        return this;
    }

    public ImageComponent textureWidth(Integer textureWidth) {
        this.textureWidth = textureWidth;
        return this;
    }

    public ImageComponent textureHeight(Integer textureHeight) {
        this.textureHeight = textureHeight;
        return this;
    }

    public ImageComponent scale(Float scale) {
        this.scale = scale;
        return this;
    }

    @Override
    public JsonObject toJson(JsonObject object) {
        object.addProperty("image", image.parseKey());
        object.addProperty("width", width);
        object.addProperty("height", height);
        if (u != null) {
            object.addProperty("u", u);
        }
        if (v != null) {
            object.addProperty("v", v);
        }
        if (textureWidth != null) {
            object.addProperty("textureWidth", textureWidth);
        }
        if (textureHeight != null) {
            object.addProperty("textureHeight", textureHeight);
        }
        if (scale != null) {
            object.addProperty("scale", scale);
        }
        return object;
    }
}
