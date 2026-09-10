package io.zershyan.sccore.compat.patchouli.api.datagen.data.page;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

public class QuestPage extends IPageType {
    private ResourceLocation trigger;
    private String title;
    private String text;

    public QuestPage() {
        super(ResourceLocation.fromNamespaceAndPath("patchouli", "quest"));
    }

    public QuestPage trigger(ResourceLocation trigger) {
        return this;
    }

    public QuestPage title(String title) {
        this.title = title;
        return this;
    }

    public QuestPage text(String text) {
        this.text = text;
        return this;
    }

    @Override
    public JsonObject toJson(JsonObject object) {
        if (trigger != null) {
            object.addProperty("trigger", trigger.toString());
        }
        if (title != null) {
            object.addProperty("title", title);
        }
        if (text != null) {
            object.addProperty("text", text);
        }
        return object;
    }
}
