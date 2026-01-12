package com.linearpast.sccore.animation.data.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.linearpast.sccore.SnowyCrescentCore;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public abstract class SCCAnimationLayerProvider implements DataProvider {

    private final DataGenerator generator;
    private final String modId;

    /**
     * Constructor for animation layer data provider
     *
     * @param generator Data generator instance
     * @param modId Mod ID for namespace
     */
    public SCCAnimationLayerProvider(DataGenerator generator, String modId) {
        this.generator = generator;
        this.modId = modId;
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput output) {
        // Create layer data
        Map<ResourceLocation, Integer> layers = createLayerData().build();

        // Convert to JSON array
        JsonArray jsonArray = createJsonArray(layers);

        // Save file
        Path outputPath = getOutputPath();

        try {
            return DataProvider.saveStable(output, jsonArray, outputPath);
        } catch (Exception e) {
            SnowyCrescentCore.log.error("Failed to save animation layer data", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Create layer data
     * Define all animation layers and their priorities here
     *
     * @return Map of layer ResourceLocations to priority values Builder
     */
    protected abstract LayerBuilder createLayerData();

    /**
     * Convert layer data to JSON array format compatible with AnimLayerJson
     *
     * @param layers Layer data map
     * @return JSON array representation
     */
    private JsonArray createJsonArray(Map<ResourceLocation, Integer> layers) {
        return getJsonElements(layers);
    }

    /**
     * Get output path for layer configuration file
     * Path: data/<modId>/scc_animations/animation.layer.json
     *
     * @return Full output path
     */
    private Path getOutputPath() {
        return generator.getPackOutput().getOutputFolder()
                .resolve("data")
                .resolve(modId)
                .resolve("scc_animations")
                .resolve("animation.layer.json");
    }

    @Override
    public @NotNull String getName() {
        return "Animation Layer Data: " + modId;
    }

    /**
     * Helper class for building animation layer data
     * Provides fluent API for creating layer configurations
     */
    public static class LayerBuilder {
        private final Map<ResourceLocation, Integer> layers = new LinkedHashMap<>();

        /**
         * Create layer builder
         *
         */
        public static LayerBuilder create() {
            return new LayerBuilder();
        }
        /**
         * Constructor for layer builder
         *
         */
        private LayerBuilder() {}

        /**
         * Add a base layer with specified priority
         *
         * @param name Layer name
         * @param priority Priority value (lower numbers = higher priority)
         * @return LayerBuilder instance for chaining
         */
        public LayerBuilder addBaseLayer(ResourceLocation name, int priority) {
            layers.put(name, priority);
            return this;
        }

        /**
         * Add a custom layer with specified priority
         *
         * @param name Layer name
         * @param priority Priority value (lower numbers = higher priority)
         * @return LayerBuilder instance for chaining
         */
        public LayerBuilder addCustomLayer(ResourceLocation name, int priority) {
            layers.put(name, priority);
            return this;
        }

        /**
         * Build the layer map
         *
         * @return Unmodifiable map of layers
         */
        public Map<ResourceLocation, Integer> build() {
            return new LinkedHashMap<>(layers);
        }

        /**
         * Build JSON array representation
         *
         * @return JSON array of layers
         */
        public JsonArray buildJsonArray() {
            return getJsonElements(layers);
        }
    }
    /**
     * Convert layer map to JSON array
     * Layers are sorted by priority (ascending)
     *
     * @param layers Layer data map
     * @return JSON array representation
     */
    @NotNull
    static JsonArray getJsonElements(Map<ResourceLocation, Integer> layers) {
        JsonArray jsonArray = new JsonArray();

        layers.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .forEachOrdered(entry -> {
                    JsonObject layerObject = new JsonObject();
                    layerObject.addProperty("key", entry.getKey().toString());
                    layerObject.addProperty("priority", entry.getValue());
                    jsonArray.add(layerObject);
                });

        return jsonArray;
    }
}