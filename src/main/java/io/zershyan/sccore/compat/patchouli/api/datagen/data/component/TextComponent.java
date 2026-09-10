package io.zershyan.sccore.compat.patchouli.api.datagen.data.component;

import com.google.gson.JsonObject;
import io.zershyan.sccore.compat.patchouli.api.datagen.data.format.StringFormat;
import io.zershyan.sccore.compat.patchouli.api.datagen.data.format.Variable;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class TextComponent extends ITemplateComponent {
    @NotNull
    private final Variable<StringFormat> text;
    private Variable<StringFormat> color;
    private Integer maxWidth;
    private Integer lineHeight;

    public TextComponent(@NotNull Variable<StringFormat> text) {
        super(ResourceLocation.fromNamespaceAndPath("patchouli", "text"));
        this.text = text;
    }

    public TextComponent color(Variable<StringFormat> color) {
        this.color = color;
        return this;
    }

    public TextComponent maxWidth(Integer maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    public TextComponent lineHeight(Integer lineHeight) {
        this.lineHeight = lineHeight;
        return this;
    }

    @Override
    public JsonObject toJson(JsonObject object) {
        object.addProperty("text", text.parseKey());
        if (color != null) {
            object.addProperty("color", color.parseKey());
        }
        if (maxWidth != null) {
            object.addProperty("max_width", maxWidth);
        }
        if (lineHeight != null) {
            object.addProperty("line_height", lineHeight);
        }
        return object;
    }
}
