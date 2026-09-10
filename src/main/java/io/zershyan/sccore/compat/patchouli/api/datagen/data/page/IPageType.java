package io.zershyan.sccore.compat.patchouli.api.datagen.data.page;

import com.google.gson.JsonObject;
import io.zershyan.sccore.compat.patchouli.api.datagen.data.format.ConfigFlags;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public abstract class IPageType {
    @NotNull
    private final ResourceLocation type;
    private ResourceLocation advancement;
    private ConfigFlags flag;
    private String anchor;

    public IPageType(@NotNull ResourceLocation type) {
        this.type = type;
    }

    public @NotNull ResourceLocation getType() {
        return type;
    }

    public IPageType advancement(ResourceLocation advancement) {
        this.advancement = advancement;
        return this;
    }

    public IPageType flag(ConfigFlags flag) {
        this.flag = flag;
        return this;
    }

    public IPageType anchor(String anchor) {
        this.anchor = anchor;
        return this;
    }

    public abstract JsonObject toJson(JsonObject object);

    public JsonObject serialize() {
        JsonObject object = new JsonObject();
        object.addProperty("type", type.toString());
        if (advancement != null) {
            object.addProperty("advancement", advancement.toString());
        }
        if (flag != null) {
            object.addProperty("flag", flag.parse());
        }
        if (anchor != null) {
            object.addProperty("anchor", anchor);
        }
        return toJson(object);
    }
}
