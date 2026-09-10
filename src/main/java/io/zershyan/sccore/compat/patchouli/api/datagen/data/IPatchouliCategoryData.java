package io.zershyan.sccore.compat.patchouli.api.datagen.data;

import com.google.gson.JsonObject;
import io.zershyan.sccore.compat.patchouli.api.datagen.data.format.ConfigFlags;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public interface IPatchouliCategoryData {
    @NotNull ResourceLocation getId();

    IPatchouliCategoryData parent(IPatchouliCategoryData parent);

    IPatchouliCategoryData parent(ResourceLocation parent);

    IPatchouliCategoryData flag(ConfigFlags flag);

    IPatchouliCategoryData sortNum(Integer sortNum);

    IPatchouliCategoryData secret(Boolean secret);

    JsonObject serialize();
}
