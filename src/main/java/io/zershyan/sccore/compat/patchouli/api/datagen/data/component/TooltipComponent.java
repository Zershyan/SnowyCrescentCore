package io.zershyan.sccore.compat.patchouli.api.datagen.data.component;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TooltipComponent extends ITemplateComponent {
    @NotNull
    private final List<String> tooltips = new ArrayList<>();
    @NotNull
    private final Integer width;
    @NotNull
    private final Integer height;

    public TooltipComponent(@NotNull Integer width, @NotNull Integer height) {
        super(ResourceLocation.fromNamespaceAndPath("patchouli", "tooltip"));
        this.width = width;
        this.height = height;
    }

    public TooltipComponent addTooltips(String... tooltips) {
        this.tooltips.addAll(Arrays.asList(tooltips));
        return this;
    }

    public TooltipComponent tooltips(List<String> tooltips) {
        this.tooltips.clear();
        this.tooltips.addAll(tooltips);
        return this;
    }

    public TooltipComponent tooltips(String... tooltips) {
        return tooltips(Arrays.asList(tooltips));
    }

    @Override
    public JsonObject toJson(JsonObject object) {
        if (tooltips.isEmpty()) {
            throw new JsonSyntaxException("tooltips cannot be empty");
        }
        JsonArray array = new JsonArray();
        for (String tooltip : tooltips) {
            array.add(tooltip);
        }
        object.add("tooltip", array);
        object.addProperty("width", width);
        object.addProperty("height", height);
        return object;
    }
}
