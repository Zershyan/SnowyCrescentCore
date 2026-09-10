package io.zershyan.sccore.compat.patchouli.api.datagen.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.zershyan.sccore.compat.patchouli.api.datagen.data.format.ConfigFlags;
import io.zershyan.sccore.compat.patchouli.api.datagen.data.format.ItemFormat;
import io.zershyan.sccore.compat.patchouli.api.datagen.data.page.IPageType;
import io.zershyan.sccore.compat.patchouli.api.datagen.data.page.TextPage;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PatchouliEntryData implements IPatchouliEntryData {
    @NotNull
    private final String name;
    @NotNull
    private final ResourceLocation category;
    @NotNull
    private final ItemFormat icon;
    private final List<IPageType> pages = new ArrayList<>();
    @NotNull
    private final ResourceLocation id;
    private final Map<ItemFormat, @NotNull Integer> extraRecipeMappings = new HashMap<>();
    private ResourceLocation advancement;
    private ResourceLocation turnin;
    private ConfigFlags flag;
    private Boolean priority;
    private Boolean secret;
    private Boolean readByDefault;
    private Integer sortnum;

    public PatchouliEntryData(
            @NotNull String name,
            @NotNull ResourceLocation category,
            @NotNull ItemFormat icon,
            @NotNull ResourceLocation id
    ) {
        this.name = name;
        this.category = category;
        this.icon = icon;
        this.id = id;
    }

    public @NotNull ResourceLocation getId() {
        return id;
    }

    @Override
    public PatchouliEntryData addExtraRecipeMapping(ItemFormat.Multi itemFormat, int pageIndex) {
        for (ItemFormat format : itemFormat.getItemFormats()) {
            extraRecipeMappings.put(format, pageIndex);
        }
        return this;
    }

    @Override
    public PatchouliEntryData addExtraRecipeMapping(ItemFormat.Multi itemFormat, IPageType pageType) {
        for (int i = 0; i < pages.size(); i++) {
            if(pages.get(i) == pageType) {
                addExtraRecipeMapping(itemFormat, i);
            }
        }
        return this;
    }

    @Override
    public PatchouliEntryData addPages(IPageType... pages) {
        this.pages.addAll(Arrays.stream(pages).toList());
        return this;
    }

    @Override
    public PatchouliEntryData pages(IPageType... pages) {
        this.pages.clear();
        this.pages.addAll(Arrays.stream(pages).toList());
        return this;
    }

    @Override
    public PatchouliEntryData advancement(ResourceLocation advancement) {
        this.advancement = advancement;
        return this;
    }

    @Override
    public PatchouliEntryData turnin(ResourceLocation turnin) {
        this.turnin = turnin;
        return this;
    }

    @Override
    public PatchouliEntryData flag(ConfigFlags flag) {
        this.flag = flag;
        return this;
    }

    @Override
    public PatchouliEntryData priority(Boolean priority) {
        this.priority = priority;
        return this;
    }

    @Override
    public PatchouliEntryData secret(Boolean secret) {
        this.secret = secret;
        return this;
    }

    @Override
    public PatchouliEntryData readByDefault(Boolean readByDefault) {
        this.readByDefault = readByDefault;
        return this;
    }

    @Override
    public PatchouliEntryData sortnum(Integer sortnum) {
        this.sortnum = sortnum;
        return this;
    }

    @Override
    public JsonObject serialize() {
        JsonObject object = new JsonObject();
        object.addProperty("name", name);
        object.addProperty("category", category.toString());
        object.addProperty("icon", icon.parse());
        if(pages.isEmpty()) {
            throw new JsonParseException("pages is empty");
        }
        if(!(pages.get(0) instanceof TextPage)) {
            throw new JsonParseException("The first page must be TextPage");
        }
        JsonArray pageArray = new JsonArray();
        pages.stream().map(IPageType::serialize).forEach(pageArray::add);
        object.add("pages", pageArray);
        if (advancement != null) {
            object.addProperty("advancement", advancement.toString());
        }
        if (turnin != null) {
            object.addProperty("turnin", turnin.toString());
        }
        if (flag != null) {
            object.addProperty("flag", flag.parse());
        }
        if (priority != null) {
            object.addProperty("priority", priority);
        }
        if (secret != null) {
            object.addProperty("secret", secret);
        }
        if (readByDefault != null) {
            object.addProperty("readByDefault", readByDefault);
        }
        if (sortnum != null) {
            object.addProperty("sortnum", sortnum);
        }
        if (!extraRecipeMappings.isEmpty()) {
            JsonObject extraObject = new JsonObject();
            extraRecipeMappings.forEach((itemFormat, pageIndex) ->
                extraObject.addProperty(itemFormat.parse(), pageIndex)
            );
            object.add("extra_recipe_mappings", extraObject);
        }
        return object;
    }
}
