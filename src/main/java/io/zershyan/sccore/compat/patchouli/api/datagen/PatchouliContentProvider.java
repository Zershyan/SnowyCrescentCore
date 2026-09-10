package io.zershyan.sccore.compat.patchouli.api.datagen;

import io.zershyan.sccore.compat.patchouli.api.datagen.data.*;
import io.zershyan.sccore.compat.patchouli.api.datagen.data.format.ItemFormat;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * All of this provider, you can see:
 * <a href="https://vazkiimods.github.io/Patchouli/">Patchouli WIKI</a>
 */
public abstract class PatchouliContentProvider implements DataProvider {

    private static final String patchouliDirectory = "patchouli_books";
    private final String modId;
    private final CompletableFuture<HolderLookup.Provider> registries;
    private final PackOutput packOutput;
    private final Map<ResourceLocation, IPatchouliCategoryData> categoryBuilders = new HashMap<>();
    private final Map<ResourceLocation, IPatchouliTemplateData> templateBuilders = new HashMap<>();
    private final Map<ResourceLocation, IPatchouliEntryData> entryBuilders = new HashMap<>();

    public PatchouliContentProvider(String modId, PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        this.modId = modId;
        this.packOutput = output;
        this.registries = registries;
    }

    protected abstract void addContent(HolderLookup.Provider provider, PackOutput output);

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput pOutput) {
        return this.registries.thenCompose(provider -> {
            List<CompletableFuture<?>> list = new ArrayList<>();
            this.addContent(provider, this.packOutput);
            this.categoryBuilders.forEach((key, value) -> {
                String directory = key.getNamespace() + "/en_us/categories/" + key.getPath();
                Path path = this.packOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, patchouliDirectory)
                        .json(ResourceLocation.fromNamespaceAndPath(this.modId, directory));
                list.add(DataProvider.saveStable(pOutput, value.serialize(), path));
            });
            this.templateBuilders.forEach((key, value) -> {
                String directory = key.getNamespace() + "/en_us/templates/" + key.getPath();
                Path path = this.packOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, patchouliDirectory)
                        .json(ResourceLocation.fromNamespaceAndPath(this.modId, directory));
                list.add(DataProvider.saveStable(pOutput, value.serialize(), path));
            });
            this.entryBuilders.forEach((key, value) -> {
                String directory = key.getNamespace() + "/en_us/entries/" + key.getPath();
                Path path = this.packOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, patchouliDirectory)
                        .json(ResourceLocation.fromNamespaceAndPath(this.modId, directory));
                list.add(DataProvider.saveStable(pOutput, value.serialize(), path));
            });
            return CompletableFuture.allOf(list.toArray(new CompletableFuture<?>[0]));
        });
    }

    /**
     * @param categoryDirectory namespace:book name, path:category directory<pre>
     *                          e.g. "lexicon:mics/cool_stuff" = "modid/patchouli_books/lexicon<br>
     *                               /en_us/categories/mics/cool_stuff.json"<br>
     *                               The modid is already defined with constructor</pre>
     * @param name              name in game
     * @param description       description
     * @param icon              icon
     * @return data chain
     */
    public final IPatchouliCategoryData createCategory(
            ResourceLocation categoryDirectory,
            String name,
            String description,
            ItemFormat icon
    ) {
        return this.categoryBuilders.computeIfAbsent(categoryDirectory, rl -> new PatchouliCategoryData(
                name, description, icon, ResourceLocation.fromNamespaceAndPath(this.modId, categoryDirectory.getPath())
        ));
    }

    /**
     * @param templateDirectory namespace:book name, path:category directory<pre>
     *                          e.g. "lexicon:mics/my_template" = "modid/patchouli_books/lexicon<br>
     *                               /en_us/templates/mics/my_template.json"<br>
     *                               The modid is already defined with constructor</pre>
     * @return data chain
     */
    public final IPatchouliTemplateData createTemplate(ResourceLocation templateDirectory) {
        return this.templateBuilders.computeIfAbsent(templateDirectory, rl ->
                new PatchouliTemplateData(ResourceLocation.fromNamespaceAndPath(this.modId, templateDirectory.getPath()))
        );
    }

    /**
     * @param entryDirectory namespace:book name, path:category directory<pre>
     *                       e.g. "lexicon:mics/my_entry" = "modid/patchouli_books/lexicon<br>
     *                            /en_us/entries/mics/my_entry.json"<br>
     *                            The modid is already defined with constructor</pre>
     * @return data chain
     */
    public final IPatchouliEntryData createEntry(
            ResourceLocation entryDirectory,
            String name,
            ResourceLocation category,
            ItemFormat icon
    ) {
        return this.entryBuilders.computeIfAbsent(entryDirectory, rl -> new PatchouliEntryData(
                name, category, icon, ResourceLocation.fromNamespaceAndPath(this.modId, entryDirectory.getPath())
        ));
    }

    public final IPatchouliCategoryData createCategory(
            Item book,
            String directory,
            String name,
            String description,
            ItemFormat icon
    ) {
        return createCategory(ResourceLocation.fromNamespaceAndPath(book.toString(), directory), name, description, icon);
    }

    public final IPatchouliTemplateData createTemplate(Item book, String directory) {
        return createTemplate(ResourceLocation.fromNamespaceAndPath(book.toString(), directory));
    }

    public final IPatchouliEntryData createEntry(
            Item book,
            String directory,
            String name,
            ResourceLocation category,
            ItemFormat icon
    ) {
        return createEntry(ResourceLocation.fromNamespaceAndPath(book.toString(), directory), name, category, icon);
    }

    public final IPatchouliEntryData createEntry(
            Item book,
            String directory,
            String name,
            IPatchouliCategoryData category,
            ItemFormat icon
    ) {
        return createEntry(book, directory, name, category.getId(), icon);
    }

    @Override
    public @NotNull String getName() {
        return "Patchouli content for " + this.modId;
    }
}
