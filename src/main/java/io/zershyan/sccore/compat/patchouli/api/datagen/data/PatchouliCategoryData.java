package io.zershyan.sccore.compat.patchouli.api.datagen.data;

import com.google.gson.JsonObject;
import io.zershyan.sccore.compat.patchouli.api.datagen.data.format.ConfigFlags;
import io.zershyan.sccore.compat.patchouli.api.datagen.data.format.ItemFormat;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class PatchouliCategoryData implements IPatchouliCategoryData {
    @NotNull
    private final String name;
    @NotNull
    private final String description;
    @NotNull
    private final ItemFormat icon;
    @NotNull
    private final ResourceLocation id;
    private ResourceLocation parent;
    private ConfigFlags flag;
    private Integer sortNum;
    private Boolean secret;

    public PatchouliCategoryData(
            @NotNull String name,
            @NotNull String description,
            @NotNull ItemFormat icon,
            @NotNull ResourceLocation id
    ) {
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.id = id;
    }

    @Override
    public PatchouliCategoryData parent(IPatchouliCategoryData parent) {
        this.parent = parent.getId();
        return this;
    }

    @Override
    public PatchouliCategoryData parent(ResourceLocation parent) {
        this.parent = parent;
        return this;
    }

    @Override
    public PatchouliCategoryData flag(ConfigFlags flag) {
        this.flag = flag;
        return this;
    }

    @Override
    public PatchouliCategoryData sortNum(Integer sortNum) {
        this.sortNum = sortNum;
        return this;
    }

    @Override
    public PatchouliCategoryData secret(Boolean secret) {
        this.secret = secret;
        return this;
    }

    public ResourceLocation getParent() {
        return parent;
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return id;
    }

    @Override
    public JsonObject serialize() {
        JsonObject object = new JsonObject();
        object.addProperty("name", name);
        object.addProperty("description", description);
        object.addProperty("icon", icon.parse());
        if (parent != null) {
            object.addProperty("parent", parent.toString());
        }
        if (flag != null) {
            object.addProperty("flag", flag.parse());
        }
        if (sortNum != null) {
            object.addProperty("sortnum", sortNum);
        }
        if (secret != null) {
            object.addProperty("secret", secret);
        }
        return object;
    }
}
