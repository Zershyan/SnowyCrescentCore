package com.linearpast.sccore.animation.register;

import com.linearpast.sccore.SnowyCrescentCore;
import com.linearpast.sccore.animation.command.argument.AnimationArgument;
import com.linearpast.sccore.animation.data.RawAnimationData;
import com.linearpast.sccore.animation.data.util.RawAnimJson;
import com.linearpast.sccore.animation.event.create.AnimationRegisterEvent;
import com.linearpast.sccore.animation.utils.FileUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class RawAnimationRegistry {
    private static final Map<ResourceLocation, RawAnimationData> animations = new HashMap<>();

    public static Map<ResourceLocation, RawAnimationData> getAnimations() {
        return Map.copyOf(animations);
    }

    private static void registerAnimations(Map<ResourceLocation, RawAnimationData> animationMap) {
        animations.clear();
        Map.copyOf(animationMap).keySet().forEach(location -> {
            if(!validateLocation(location)) animations.remove(location);
        });
        animations.putAll(animationMap);
        AnimationArgument.resetAnimationNames();
    }

    public static boolean register(ResourceLocation location, RawAnimationData rawAnimation) {
        if (validateLocation(location)) {
            animations.put(location, rawAnimation);
            return true;
        }
        return false;
    }

    public static void triggerRegistry() {
        resetAnimations();
        AnimationRegisterEvent.RawAnimation event = new AnimationRegisterEvent.RawAnimation();
        MinecraftForge.EVENT_BUS.post(event);
        Map<ResourceLocation, RawAnimationData> animationDataMap = new HashMap<>(event.getAnimations());
        Minecraft instance = Minecraft.getInstance();
        Path dataPackPath = instance.getResourcePackDirectory();
        Path animationPath = dataPackPath.resolve("animation");
        if (!Files.exists(animationPath)) {
            try {
                Files.createDirectories(animationPath);
            } catch (IOException e) { return; }
        }
        FileUtils.safeUnzip(dataPackPath.resolve("animation.zip").toString(), animationPath.toAbsolutePath().toString());
        Set<Path> animZipPaths = FileUtils.getAllFile(
                dataPackPath.resolve("animation"),
                path -> path.toString().endsWith(".anim.zip")
        );
        for (Path zipPath : animZipPaths) {
            FileUtils.safeUnzip(zipPath.toString(), animationPath.toAbsolutePath().toString());
        }
        Set<Path> animPaths = FileUtils.getAllFile(
                dataPackPath.resolve("animation"),
                path -> path.toString().endsWith(".anim.json")
        );

        for (Path path : animPaths) {
            try {
                RawAnimJson.Reader reader = RawAnimJson.Reader.stream(path);
                RawAnimationData anim = reader.parse();
                animationDataMap.put(anim.getKey(), anim);
            } catch (Exception ignored) {
                SnowyCrescentCore.log.error("Failed to parse raw animation JSON: {}", path.toString());
            }
        }
        registerAnimations(animationDataMap);
    }

    private static void resetAnimations() {
        animations.clear();
    }

    public static boolean validateLocation(ResourceLocation location) {
        try {
            if(!AnimationRegistry.ClientCache.isAnimationRegistered)
                throw new RuntimeException("Server animation is not registered!");
            if(AnimationRegistry.getAnimations().containsKey(location))
                throw new RuntimeException("Duplicated animation on server: " + location);
            return true;
        } catch (RuntimeException e) {
            SnowyCrescentCore.log.error(e.getMessage(), e);
            return false;
        }
    }

}
