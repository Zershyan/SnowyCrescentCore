package io.zershyan.sccore.compat.patchouli.api.datagen.data.page;

import com.google.gson.JsonObject;
import io.zershyan.sccore.compat.patchouli.api.datagen.data.format.EntityFormat;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class EntityPage extends IPageType {
    @NotNull
    private final EntityFormat entity;
    private Float scale;
    private Float offset;
    private Float defaultRotation;
    private Boolean rotate;
    private String name;
    private String text;

    public EntityPage(@NotNull EntityFormat entity) {
        super(ResourceLocation.fromNamespaceAndPath("patchouli", "entity"));
        this.entity = entity;
    }

    public EntityPage scale(Float scale) {
        this.scale = scale;
        return this;
    }

    public EntityPage offset(Float offset) {
        this.offset = offset;
        return this;
    }

    public EntityPage defaultRotation(Float defaultRotation) {
        this.defaultRotation = defaultRotation;
        return this;
    }

    public EntityPage rotate(Boolean rotate) {
        this.rotate = rotate;
        return this;
    }

    public EntityPage name(String name) {
        this.name = name;
        return this;
    }

    public EntityPage text(String text) {
        this.text = text;
        return this;
    }

    @Override
    public JsonObject toJson(JsonObject object) {
        object.addProperty("entity", entity.parse());
        if (scale != null) {
            object.addProperty("scale", scale);
        }
        if (offset != null) {
            object.addProperty("offset", offset);
        }
        if (rotate != null) {
            object.addProperty("rotate", rotate);
            if (!rotate && defaultRotation != null) {
                object.addProperty("default_rotation", defaultRotation);
            }
        }
        if (name != null) {
            object.addProperty("name", name);
        }
        if (text != null) {
            object.addProperty("text", text);
        }
        return object;
    }
}
