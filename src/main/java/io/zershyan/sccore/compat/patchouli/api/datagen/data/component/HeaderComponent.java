package io.zershyan.sccore.compat.patchouli.api.datagen.data.component;

import com.google.gson.JsonObject;
import io.zershyan.sccore.compat.patchouli.api.datagen.data.format.StringFormat;
import io.zershyan.sccore.compat.patchouli.api.datagen.data.format.Variable;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class HeaderComponent extends ITemplateComponent {
    @NotNull
    private final Variable<StringFormat> text;
    private Variable<StringFormat> color;
    private Boolean centered;
    private Float scale;

    public HeaderComponent(@NotNull Variable<StringFormat> text) {
        super(ResourceLocation.fromNamespaceAndPath("patchouli", "header"));
        this.text = text;
    }

    public HeaderComponent color(Variable<StringFormat> color) {
        this.color = color;
        return this;
    }

    public HeaderComponent centered(Boolean centered) {
        this.centered = centered;
        return this;
    }

    public HeaderComponent scale(Float scale) {
        this.scale = scale;
        return this;
    }

    @Override
    public JsonObject toJson(JsonObject object) {
        object.addProperty("text", text.parseKey());
        if (color != null) {
            object.addProperty("color", color.parseKey());
        }
        if (centered != null) {
            object.addProperty("centered", centered);
        }
        if (scale != null) {
            object.addProperty("scale", scale);
        }
        return object;
    }
}
