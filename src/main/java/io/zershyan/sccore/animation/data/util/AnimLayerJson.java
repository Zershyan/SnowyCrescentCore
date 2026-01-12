package io.zershyan.sccore.animation.data.util;

import com.google.gson.*;
import io.zershyan.sccore.animation.register.AnimationRegistry;
import net.minecraft.resources.ResourceLocation;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class AnimLayerJson {

    private static final String Key = "key";
    private static final String Priority = "priority";

    public static class Reader {
        private final JsonElement originElement;

        Reader(Path jsonFile) throws Exception {
            File file = jsonFile.toFile();
            if (!file.exists()) {
                throw new FileNotFoundException("File does not exist: " + file.getAbsolutePath());
            }
            this.originElement = JsonParser.parseReader(new FileReader(file));
        }

        Reader(JsonElement originElement) {
            this.originElement = originElement;
        }

        public static Reader stream(Path path) throws Exception {
            return new Reader(path);
        }

        public static Reader stream(JsonElement jsonElement) {
            return new Reader(jsonElement);
        }

        public Map<ResourceLocation, Integer> parse() {
            return fromJson();
        }

        private Map<ResourceLocation, Integer> fromJson() {
            try {
                JsonArray jsonArray = originElement.getAsJsonArray();
                Map<ResourceLocation, Integer> map = new HashMap<>();
                for (JsonElement element : jsonArray) {
                    JsonObject jsonObject = element.getAsJsonObject();
                    ResourceLocation location = new ResourceLocation(jsonObject.get(Key).getAsString());
                    int priority = jsonObject.get(Priority).getAsInt();
                    map.put(location, priority);
                }
                return map;
            } catch (Exception e) {
                throw new JsonParseException(e);
            }
        }
    }

    public static class Writer {
        private final Path file;
        private final Map<ResourceLocation, Integer> layers = AnimationRegistry.getLayers();
        private final Map<String, Set<ResourceLocation>> layerNames = new HashMap<>();
        Writer(Path file) {
            this.file = file;
            for (ResourceLocation location : layers.keySet()) {
                String namespace = location.getNamespace();
                Set<ResourceLocation> locationSet = layerNames.getOrDefault(namespace, new HashSet<>());
                locationSet.add(location);
                layerNames.put(namespace, locationSet);
            }
        }

        public static Writer stream(Path path) {
            return new Writer(path);
        }

        public static Path syntaxImmediately(Path directory) throws IOException {
            Writer writer = stream(directory);
            return writer.syntax();
        }

        public Map<String, JsonElement> allToJson() {
            Map<String, JsonElement> map = new HashMap<>();
            for (String namespace : layerNames.keySet()) {
                Set<ResourceLocation> locationSet = layerNames.get(namespace);
                JsonElement json = toJson(locationSet);
                map.put(namespace, json);
            }
            return map;
        }

        public Path syntax(String ... namespaces) throws IOException {
            Set<String> namespaceSet;
            if(namespaces.length == 0) {
                namespaceSet = layerNames.keySet();
            } else {
                namespaceSet = Arrays.stream(namespaces).collect(Collectors.toSet());
            }

            for (String name : namespaceSet) {
                Set<ResourceLocation> locationSet = layerNames.get(name);
                Path modIdPath = file.resolve(name);
                Path resultPath = modIdPath.resolve("animation.layer.json");
                if(!Files.exists(modIdPath)) Files.createDirectories(modIdPath);
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                try (FileWriter writer = new FileWriter(resultPath.toFile())) {
                    gson.toJson(toJson(locationSet), writer);
                }
            }
            return file;
        }

        private JsonElement toJson(Set<ResourceLocation> locationSet) {
            JsonArray jsonArray = new JsonArray();
            for (ResourceLocation location : locationSet) {
                if(layers.containsKey(location)) {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty(Key, location.toString());
                    jsonObject.addProperty(Priority, layers.get(location));
                    jsonArray.add(jsonObject);
                }
            }
            return jsonArray;
        }
    }
}
