package io.zershyan.sccore.example.patchouli.datagen.provider;

import io.zershyan.sccore.SCCore;
import io.zershyan.sccore.compat.patchouli.api.datagen.PatchouliBookProvider;
import io.zershyan.sccore.compat.patchouli.api.datagen.data.IPatchouliBookData;
import io.zershyan.sccore.compat.patchouli.api.datagen.data.format.ItemFormat;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class SCCPatchouliBookProvider extends PatchouliBookProvider {
    public SCCPatchouliBookProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(SCCore.MODID, output, registries);
    }

    /**
     * You can see a link: <a href="https://vazkiimods.github.io/Patchouli/">Patchouli WIKI</a>
     * @param provider provider
     * @param output pack output
     * @see IPatchouliBookData
     */
    @Override
    protected void addBooks(HolderLookup.Provider provider, PackOutput output) {
        IPatchouliBookData bookData = createBook("lexicon",  "This is book name.", "This is landing text.")
                .i18n(true).indexIcon(ItemFormat.of(Items.ENCHANTED_BOOK)).allowExtensions(true).showProgress(false)
                .creativeTab(ResourceLocation.withDefaultNamespace("food_and_drinks"))
                .bookTexture(ResourceLocation.fromNamespaceAndPath("patchouli", "textures/gui/book_purple.png"))
                .dontGenerateBook(false);
    }
}
