package io.zershyan.sccore.compat.patchouli.api.datagen;

import io.zershyan.sccore.compat.patchouli.api.datagen.data.IPatchouliBookData;
import io.zershyan.sccore.compat.patchouli.api.datagen.data.PatchouliBookData;
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

public abstract class PatchouliBookProvider implements DataProvider {

    private static final String patchouliDirectory = "patchouli_books";
    private final String modId;
    private final CompletableFuture<HolderLookup.Provider> registries;
    private final PackOutput packOutput;
    private final Map<String, IPatchouliBookData> bookBuilders = new HashMap<>();

    public PatchouliBookProvider(String modId, PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        this.modId = modId;
        this.packOutput = output;
        this.registries = registries;
    }

    protected abstract void addBooks(HolderLookup.Provider provider, PackOutput output);

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput pOutput) {
        return this.registries.thenCompose(provider -> {
            List<CompletableFuture<?>> list = new ArrayList<>();
            this.addBooks(provider, packOutput);
            this.bookBuilders.forEach((key, value) -> {
                Path path = this.packOutput.createPathProvider(PackOutput.Target.DATA_PACK, patchouliDirectory)
                        .json(ResourceLocation.fromNamespaceAndPath(this.modId, key + "/book"));
                list.add(DataProvider.saveStable(pOutput, value.serialize(), path));
            });
            return CompletableFuture.allOf(list.toArray(new CompletableFuture<?>[0]));
        });
    }

    public final IPatchouliBookData createBook(String directory, String name, String landingText) {
        return this.bookBuilders.computeIfAbsent(directory, s -> new PatchouliBookData(name, landingText));
    }

    public final IPatchouliBookData createBook(Item item, String landingText) {
        return createBook(item.toString(), item.getDescriptionId(), landingText);
    }

    @Override
    public @NotNull String getName() {
        return "Patchouli book for " + this.modId;
    }
}
