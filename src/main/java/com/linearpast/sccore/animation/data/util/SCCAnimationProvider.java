/*
 * *
 *  * Copyright (c) 2026 R3944Realms. All rights reserved.
 *  *
 *  * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 *  * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/4.0/
 *  * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *  *
 *  * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 *
 */

package com.linearpast.sccore.animation.data.util;

import com.google.gson.JsonObject;
import com.linearpast.sccore.animation.data.GenericAnimationData;
import com.linearpast.sccore.animation.data.Ride;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Abstract base class for animation data providers
 * Handles generation of animation JSON files in the scc_animations directory
 */
public abstract class SCCAnimationProvider implements DataProvider {
    private final DataGenerator generator;
    private final String modId;

    /**
     * Constructor for animation provider
     *
     * @param generator Data generator instance
     * @param modId Mod ID for namespace
     */
    protected SCCAnimationProvider(DataGenerator generator, String modId) {
        this.generator = generator;
        this.modId = modId;
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput output) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        Path outputFolder = generator.getPackOutput().getOutputFolder();

        // Register animations and save them as JSON files
        registerAnimations(animation -> {
            ResourceLocation key = animation.getKey();
            Path path = outputFolder
                    .resolve("data")
                    .resolve(key.getNamespace())
                    .resolve("scc_animations")
                    .resolve(key.getPath() + ".anim.json");

            JsonObject json = convertToJson(animation);

            futures.add(DataProvider.saveStable(output, json, path));
        });

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    @Override
    public @NotNull String getName() {
        return "Animations: " + modId;
    }

    /**
     * Register animations to be generated
     * Implement this method to define which animations to create
     *
     * @param consumer Consumer that accepts GenericAnimationData instances
     */
    protected abstract void registerAnimations(Consumer<GenericAnimationData> consumer);

    /**
     * Convert GenericAnimationData to JSON object
     *
     * @param animation Animation data to convert
     * @return JSON object representation
     */
    private JsonObject convertToJson(GenericAnimationData animation) {
        JsonObject json = getJsonObject(animation);

        // Add camera position offset if present
        Vec3 camOffset = animation.getCamPosOffset();
        if (!camOffset.equals(Vec3.ZERO) || animation.isCamPosOffsetRelative()) {
            JsonObject camOffsetJson = new JsonObject();
            camOffsetJson.addProperty("x", camOffset.x);
            camOffsetJson.addProperty("y", camOffset.y);
            camOffsetJson.addProperty("z", camOffset.z);
            camOffsetJson.addProperty("relative", animation.isCamPosOffsetRelative());
            json.add("camPosOffset", camOffsetJson);
        }

        // Add ride configuration if present
        Ride ride = animation.getRide();
        if (ride != null) {
            JsonObject rideJson = new JsonObject();

            JsonObject offsetJson = new JsonObject();
            Vec3 offset = ride.getOffset();
            offsetJson.addProperty("x", offset.x);
            offsetJson.addProperty("y", offset.y);
            offsetJson.addProperty("z", offset.z);
            rideJson.add("offset", offsetJson);

            rideJson.addProperty("xRot", ride.getXRot());
            rideJson.addProperty("yRot", ride.getYRot());
            rideJson.addProperty("existTick", ride.getExistTick());

            if (!ride.getComponentAnimations().isEmpty()) {
                com.google.gson.JsonArray componentsArray = new com.google.gson.JsonArray();
                ride.getComponentAnimations().forEach(component ->
                        componentsArray.add(component.toString())
                );
                rideJson.add("componentsAnimation", componentsArray);
            }

            json.add("withRide", rideJson);
        }

        return json;
    }

    /**
     * Create base JSON object with common animation properties
     *
     * @param animation Animation data
     * @return Base JSON object
     */
    private static @NotNull JsonObject getJsonObject(GenericAnimationData animation) {
        JsonObject json = new JsonObject();
        ResourceLocation key = animation.getKey();
        json.addProperty("key", key.toString());

        if (animation.getName() != null) {
            json.addProperty("name", animation.getName());
        }

        if (animation.getLyingType() != null) {
            json.addProperty("lyingType", animation.getLyingType().getName());
        }

        json.addProperty("heightModifier", animation.getHeightModifier());
        json.addProperty("priority", animation.getCamComputePriority());

        if (animation.getCamPitch() != 0) {
            json.addProperty("camPitch", animation.getCamPitch());
        }

        if (animation.getCamRoll() != 0) {
            json.addProperty("camRoll", animation.getCamRoll());
        }

        if (animation.getCamYaw() != 0) {
            json.addProperty("camYaw", animation.getCamYaw());
        }
        return json;
    }
}