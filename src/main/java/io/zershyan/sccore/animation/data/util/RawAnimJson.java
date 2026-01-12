package io.zershyan.sccore.animation.data.util;

import com.google.gson.*;
import io.zershyan.sccore.animation.data.AnimationData;
import io.zershyan.sccore.animation.data.RawAnimationData;
import io.zershyan.sccore.animation.data.Ride;
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
                if(json.has(Priority)) animation.withCamComputePriority(json.get(Priority).getAsInt());
                if(json.has(CamPitch)) animation.withCamPitch(json.get(CamPitch).getAsFloat());
                if(json.has(CamRoll)) animation.withCamRoll(json.get(CamRoll).getAsFloat());
                if(json.has(CamYaw)) animation.withCamYaw(json.get(CamYaw).getAsFloat());
                if(json.has(CamPosOffset)) {
                    JsonObject camOffset = json.get(CamPosOffset).getAsJsonObject();
                    Vec3 vec3 = Vec3.ZERO;
                    if(camOffset.has("x")) vec3 = vec3.add(camOffset.get("x").getAsDouble(), 0, 0);
                    if(camOffset.has("y")) vec3 = vec3.add(0, camOffset.get("y").getAsDouble(), 0);
                    if(camOffset.has("z")) vec3 = vec3.add(0, 0, camOffset.get("z").getAsDouble());
                    if(!vec3.equals(Vec3.ZERO)) animation.setCamPosOffset(vec3);
                    if(camOffset.has(Relative)) animation.withCamPosOffsetRelative(camOffset.get(Relative).getAsBoolean());
                }
                if(json.has(WithRide)){
                    Ride ride = Ride.create();
                    JsonObject withRide = json.get(WithRide).getAsJsonObject();
                    if(withRide.has(ExistTick)) ride.setExistTick(withRide.get(ExistTick).getAsInt());
                    if(withRide.has(XRot)) ride.setXRot(withRide.get(XRot).getAsFloat());
                    if(withRide.has(YRot)) ride.setYRot(withRide.get(YRot).getAsFloat());
                    if(withRide.has(Offset)) {
                        JsonObject offsetJson = withRide.get(Offset).getAsJsonObject();
                        Vec3 offset = new Vec3(
                                offsetJson.get("x").getAsDouble(),
                                offsetJson.get("y").getAsDouble(),
                                offsetJson.get("z").getAsDouble()
                        );
                        ride.withOffset(offset);
                    }
                    if(withRide.has(ComponentsAnimation)){
                        JsonArray elements = withRide.get(ComponentsAnimation).getAsJsonArray();
                        for (JsonElement element : elements) {
                            String componentKeyString = element.getAsString();
                            ResourceLocation componentKey = new ResourceLocation(componentKeyString);
                            ride.addComponentAnimation(componentKey);
                        }
                    }
                    animation.withRide(ride);
                }
                return animation;
            } catch (Exception e) {
                throw new JsonParseException(e);
            }
        }
    }
}
