package com.linearpast.sccore.animation.data.util;

import com.google.gson.*;
import com.linearpast.sccore.animation.data.AnimationData;
import com.linearpast.sccore.animation.data.RawAnimationData;
import com.linearpast.sccore.animation.data.Ride;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;

public class RawAnimJson {
    private static final String Key = "key";
    private static final String LyingType = "lyingType";
    private static final String CamPitch = "camPitch";
    private static final String CamRoll = "camRoll";
    private static final String CamYaw = "camYaw";
    private static final String CamPosOffset = "camPosOffset";
    private static final String Relative = "relative";
    private static final String Priority = "priority";
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

        public static RawAnimJson.Reader stream(Path path) throws Exception {
            return new RawAnimJson.Reader(path);
        }

        public static RawAnimJson.Reader stream(JsonElement jsonElement) {
            return new RawAnimJson.Reader(jsonElement);
        }

        public RawAnimationData parse() {
            return fromJson();
        }

        public RawAnimationData fromJson() {
            try {
                JsonObject json = originElement.getAsJsonObject();
                RawAnimationData animation = RawAnimationData.create(new ResourceLocation(json.get(Key).getAsString()));
                if(json.has(LyingType)) animation.withLyingType(AnimationData.LyingType.valueOf(json.get(LyingType).getAsString()));
                JsonObject camOffset = json.get(CamPosOffset).getAsJsonObject();
                animation.withCamComputePriority(json.get(Priority).getAsInt())
                        .setCamPosOffset(new Vec3(
                                camOffset.get("x").getAsDouble(),
                                camOffset.get("y").getAsDouble(),
                                camOffset.get("z").getAsDouble()
                        ))
                        .withCamPosOffsetRelative(camOffset.get(Relative).getAsBoolean())
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
}
