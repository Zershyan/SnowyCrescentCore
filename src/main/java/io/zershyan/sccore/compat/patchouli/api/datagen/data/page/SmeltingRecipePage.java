package io.zershyan.sccore.compat.patchouli.api.datagen.data.page;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class SmeltingRecipePage extends RecipePage{
    public SmeltingRecipePage(@NotNull ResourceLocation recipe) {
        super(ResourceLocation.fromNamespaceAndPath("patchouli", "smelting"), recipe);
    }
}
