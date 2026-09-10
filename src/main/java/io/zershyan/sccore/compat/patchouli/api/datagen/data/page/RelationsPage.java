package io.zershyan.sccore.compat.patchouli.api.datagen.data.page;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.zershyan.sccore.compat.patchouli.api.datagen.data.IPatchouliEntryData;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RelationsPage extends IPageType {
    private final List<ResourceLocation> entries = new ArrayList<>();
    private String title;
    private String text;

    public RelationsPage() {
        super(ResourceLocation.fromNamespaceAndPath("patchouli", "relations"));
    }

    public RelationsPage addEntries(IPatchouliEntryData... entries) {
        this.entries.addAll(Arrays.stream(entries).map(IPatchouliEntryData::getId).toList());
        return this;
    }

    public RelationsPage addEntries(ResourceLocation... entries) {
        this.entries.addAll(Arrays.asList(entries));
        return this;
    }

    public RelationsPage entries(ResourceLocation... entries) {
        this.entries.clear();
        this.entries.addAll(Arrays.asList(entries));
        return this;
    }

    public RelationsPage title(String title) {
        this.title = title;
        return this;
    }

    public RelationsPage text(String text) {
        this.text = text;
        return this;
    }

    @Override
    public JsonObject toJson(JsonObject object) {
        if (entries.isEmpty()) {
            throw new JsonParseException("No entries found");
        }
        JsonArray array = new JsonArray();
        for (ResourceLocation entry : entries) {
            array.add(entry.toString());
        }
        object.add("entries", array);
        if (title != null) {
            object.addProperty("title", title);
        }
        if (text != null) {
            object.addProperty("text", text);
        }
        return object;
    }
}
