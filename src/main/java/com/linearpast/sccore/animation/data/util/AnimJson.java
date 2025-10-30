package com.linearpast.sccore.animation.data.util;

import com.google.gson.*;
import com.linearpast.sccore.SnowyCrescentCore;
import com.linearpast.sccore.animation.data.Animation;
import com.linearpast.sccore.animation.data.Ride;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class AnimJson {
    private static final String Key = "key";
    private static final String Name = "name";
    private static final String LyingType = "lyingType";
    private static final String HeightModifier = "heightModifier";
    private static final String CamY = "camY";
    private static final String CamPitch = "camPitch";
    private static final String CamRoll = "camRoll";
    private static final String CamYaw = "camYaw";
    private static final String WithRide = "withRide";
    private static final String Offset = "offset";
    private static final String XRot = "xRot";
    private static final String YRot = "yRot";
    private static final String ExistTick = "existTick";
    private static final String ComponentsAnimation = "componentsAnimation";

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

        public Animation parse() {
            return fromJson();
        }

        public Animation fromJson() {
            try {
                JsonObject json = originElement.getAsJsonObject();
                Animation animation = Animation.create(new ResourceLocation(json.get(Key).getAsString()));
                if(json.has(Name)) animation.withName(json.get(Name).getAsString());
                if(json.has(LyingType)) animation.withLyingType(Animation.LyingType.valueOf(json.get(LyingType).getAsString()));
                animation.withHeightModifier(json.get(HeightModifier).getAsFloat())
                        .withCamY(json.get(CamY).getAsFloat())
                        .withCamPitch(json.get(CamPitch).getAsFloat())
                        .withCamRoll(json.get(CamRoll).getAsFloat())
                        .withCamYaw(json.get(CamYaw).getAsFloat());
                if(json.has(WithRide)){
                    Ride ride = Ride.create();
                    JsonObject withRide = json.get(WithRide).getAsJsonObject();
                    JsonObject offsetJson = withRide.get(Offset).getAsJsonObject();
                    if(withRide.has(ComponentsAnimation)){
                        JsonArray elements = withRide.get(ComponentsAnimation).getAsJsonArray();
                        for (JsonElement element : elements) {
                            String componentKeyString = element.getAsString();
                            ResourceLocation componentKey = new ResourceLocation(componentKeyString);
                            ride.addComponentAnimation(componentKey);
                        }
                    }
                    Vec3 offset = new Vec3(
                            offsetJson.get("x").getAsDouble(),
                            offsetJson.get("y").getAsDouble(),
                            offsetJson.get("z").getAsDouble()
                    );
                    ride.withOffset(offset).withExistTick(withRide.get(ExistTick).getAsInt())
                            .withXRot(withRide.get(XRot).getAsFloat())
                            .withYRot(withRide.get(YRot).getAsFloat());
                    animation.withRide(ride);
                }
                return animation;
            } catch (Exception e) {
                throw new JsonParseException(e);
            }
        }
    }

    public static class Writer {
        private static final String example = "example";
        private final @Nullable Path file;
        private final Animation animation;
        Writer(@Nullable Path file, Animation animation) {
            this.animation = animation;
            this.file = file;
        }

        public static Writer stream(Path path, Animation animation) {
            return new Writer(path, animation);
        }

        public static Writer stream(Animation animation) {
            return new Writer(null, animation);
        }

        public static Path syntaxExample(Path directory) throws Exception {
            ResourceLocation exampleLocation = new ResourceLocation(SnowyCrescentCore.MODID, Writer.example);
            Animation example = Animation.create(exampleLocation)
                    .withName(Writer.example)
                    .withLyingType(Animation.LyingType.RIGHT)
                    .withHeightModifier(0.3f)
                    .withCamY(-1.3f)
                    .withCamPitch(-90.0f)
                    .withCamRoll(90.0f)
                    .withCamYaw(90.0f)
                    .withRide(Ride.create()
                            .withOffset(new Vec3(0.0f, 1.0f, 0.0f))
                            .withExistTick(200)
                            .withXRot(180)
                            .withYRot(0)
                            .addComponentAnimation(exampleLocation)
                    );
            Writer writer = stream(directory, example);
            return writer.syntax();
        }

        public Path syntax() throws Exception {
            if(file == null) throw new NullPointerException("file is null");
            Path modIdPath = file.resolve(animation.getKey().getNamespace());
            Path resultPath = modIdPath.resolve(animation.getKey().getPath() + ".anim.json");
            if(animation.getName() != null) {
                resultPath = modIdPath.resolve(animation.getName() + ".anim.json");
                if(resultPath.toFile().exists()) {
                    resultPath = modIdPath.resolve(animation.getKey().getPath() + ".anim.json");
                }
            }
            if(resultPath.toFile().exists()) return resultPath;
            if(!Files.exists(modIdPath)) Files.createDirectories(modIdPath);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (FileWriter writer = new FileWriter(resultPath.toFile())) {
                gson.toJson(toJson(), writer);
                return resultPath;
            }
        }


        public JsonElement toJson() {
            JsonObject json = new JsonObject();
            ResourceLocation key = animation.getKey();
            json.addProperty(Key, key.toString());
            if (animation.getName() != null) json.addProperty(Name, animation.getName());
            if (animation.getLyingType() != null) json.addProperty(LyingType, animation.getLyingType().getName());
            json.addProperty(HeightModifier, animation.getHeightModifier());
            json.addProperty(CamY, animation.getCamY());
            json.addProperty(CamPitch, animation.getCamPitch());
            json.addProperty(CamRoll, animation.getCamRoll());
            json.addProperty(CamYaw, animation.getCamYaw());
            Ride ride = animation.getRide();
            if(ride != null) {
                JsonObject jsonRide = new JsonObject();
                JsonObject jsonOffset = new JsonObject();
                Vec3 offset = ride.getOffset();
                jsonOffset.addProperty("x", offset.x);
                jsonOffset.addProperty("y", offset.y);
                jsonOffset.addProperty("z", offset.z);
                jsonRide.add(Offset, jsonOffset);
                jsonRide.addProperty(XRot, ride.getXRot());
                jsonRide.addProperty(YRot, ride.getYRot());
                jsonRide.addProperty(ExistTick, ride.getExistTick());

                if(!ride.getComponentAnimations().isEmpty()) {
                    JsonArray jsonComponents = new JsonArray();
                    ride.getComponentAnimations().forEach(component ->
                            jsonComponents.add(component.toString())
                    );
                    jsonRide.add(ComponentsAnimation, jsonComponents);
                }
                json.add(WithRide, jsonRide);
            }
            return json;
        }

    }
}
