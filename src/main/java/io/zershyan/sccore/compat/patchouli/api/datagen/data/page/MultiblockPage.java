package io.zershyan.sccore.compat.patchouli.api.datagen.data.page;

import com.google.gson.JsonObject;
import io.zershyan.sccore.compat.patchouli.api.datagen.data.format.MultiblockFormat;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class MultiblockPage extends IPageType {
    @NotNull
    private final String name;
    @NotNull
    private final MultiblockFormat multiblockFormat;
    private Boolean enableVisualize;
    private String text;

    public MultiblockPage(@NotNull String name, @NotNull MultiblockFormat multiblockFormat) {
        super(ResourceLocation.fromNamespaceAndPath("patchouli", "multiblock"));
        this.name = name;
        this.multiblockFormat = multiblockFormat;
    }

    public MultiblockPage enableVisualize(Boolean enableVisualize) {
        this.enableVisualize = enableVisualize;
        return this;
    }

    public MultiblockPage text(String text) {
        this.text = text;
        return this;
    }

    @Override
    public JsonObject toJson(JsonObject object) {
        object.addProperty("name", name);
        String multiblockId = multiblockFormat.parse();
        if(multiblockId != null) {
            object.addProperty("multiblock_id", multiblockId);
        } else {
            object.add("multiblock", multiblockFormat.serialize());
        }
        if(enableVisualize != null) {
            object.addProperty("enable_visualize", enableVisualize);
        }
        if(text != null) {
            object.addProperty("text", text);
        }
        return object;
    }
}
