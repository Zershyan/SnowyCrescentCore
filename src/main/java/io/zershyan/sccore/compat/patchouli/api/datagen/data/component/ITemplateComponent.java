package io.zershyan.sccore.compat.patchouli.api.datagen.data.component;

import com.google.gson.JsonObject;
import io.zershyan.sccore.compat.patchouli.api.datagen.data.format.ConfigFlags;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public abstract class ITemplateComponent {
    @NotNull
    private final ResourceLocation type;
    protected Integer x;
    protected Integer y;
    protected ResourceLocation advancement;
    protected ResourceLocation negateAdvancement;
    protected String guard;
    protected String group;
    protected ConfigFlags flag;

    public ITemplateComponent(@NotNull ResourceLocation type) {
        this.type = type;
    }

    public @NotNull ResourceLocation getType() {
        return type;
    }

    public ITemplateComponent x(Integer x) {
        this.x = x;
        return this;
    }

    public ITemplateComponent y(Integer y) {
        this.y = y;
        return this;
    }

    public ITemplateComponent advancement(ResourceLocation advancement) {
        this.advancement = advancement;
        return this;
    }

    public ITemplateComponent negateAdvancement(ResourceLocation negateAdvancement) {
        this.negateAdvancement = negateAdvancement;
        return this;
    }

    public ITemplateComponent guard(String guard) {
        this.guard = guard;
        return this;
    }

    public ITemplateComponent group(String group) {
        this.group = group;
        return this;
    }

    public ITemplateComponent flag(ConfigFlags flag) {
        this.flag = flag;
        return this;
    }

    public abstract JsonObject toJson(JsonObject object);

    public JsonObject serialize() {
        JsonObject object = new JsonObject();
        object.addProperty("type", type.toString());
        if(x != null) {
            object.addProperty("x", x);
        }
        if(y != null) {
            object.addProperty("y", y);
        }
        if(advancement != null) {
            object.addProperty("advancement", advancement.toString());
        }
        if(negateAdvancement != null) {
            object.addProperty("negate_advancement", negateAdvancement.toString());
        }
        if(guard != null) {
            object.addProperty("guard", guard);
        }
        if(group != null) {
            object.addProperty("group", group);
        }
        if(flag != null) {
            object.addProperty("flag", flag.parse());
        }
        return toJson(object);
    }
}
