package io.zershyan.sccore.compat.patchouli.api.datagen.data.component;

import com.google.gson.JsonObject;
import io.zershyan.sccore.compat.patchouli.api.datagen.data.format.EntityFormat;
import io.zershyan.sccore.compat.patchouli.api.datagen.data.format.Variable;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class EntityComponent extends ITemplateComponent {
    @NotNull
    private final Variable<EntityFormat> entity;
    private Integer renderSize;
    private Boolean rotate;
    private Float defaultRotation;

    public EntityComponent(@NotNull Variable<EntityFormat> entity) {
        super(ResourceLocation.fromNamespaceAndPath("patchouli", "entity"));
        this.entity = entity;
    }

    public EntityComponent renderSize(Integer renderSize) {
        this.renderSize = renderSize;
        return this;
    }

    public EntityComponent rotate(Boolean rotate) {
        this.rotate = rotate;
        return this;
    }

    public EntityComponent defaultRotation(Float defaultRotation) {
        this.defaultRotation = defaultRotation;
        return this;
    }

    @Override
    public JsonObject toJson(JsonObject object) {
        object.addProperty("entity", entity.parseKey());
        if (renderSize != null) {
            object.addProperty("render_size", renderSize);
        }
        if (rotate != null) {
            object.addProperty("rotate", rotate);
            if (!rotate && defaultRotation != null) {
                object.addProperty("default_rotation", defaultRotation);
            }
        }
        return object;
    }
}
