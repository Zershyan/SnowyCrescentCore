package io.zershyan.sccore.compat.patchouli.api.datagen.data;

import com.google.gson.JsonObject;
import io.zershyan.sccore.compat.patchouli.api.datagen.data.format.ConfigFlags;
import io.zershyan.sccore.compat.patchouli.api.datagen.data.format.ItemFormat;
import io.zershyan.sccore.compat.patchouli.api.datagen.data.page.IPageType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public interface IPatchouliEntryData {
    @NotNull ResourceLocation getId();

    PatchouliEntryData addExtraRecipeMapping(ItemFormat.Multi itemFormat, int pageIndex);

    PatchouliEntryData addExtraRecipeMapping(ItemFormat.Multi itemFormat, IPageType pageType);

    IPatchouliEntryData addPages(IPageType... pages);

    IPatchouliEntryData pages(IPageType... pages);

    IPatchouliEntryData advancement(ResourceLocation advancement);

    IPatchouliEntryData turnin(ResourceLocation turnin);

    IPatchouliEntryData flag(ConfigFlags flag);

    IPatchouliEntryData priority(Boolean priority);

    IPatchouliEntryData secret(Boolean secret);

    IPatchouliEntryData readByDefault(Boolean readByDefault);

    IPatchouliEntryData sortnum(Integer sortnum);

    JsonObject serialize();
}
