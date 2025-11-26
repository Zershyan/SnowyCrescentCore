package com.linearpast.sccore.animation.helper;

import com.linearpast.sccore.animation.data.GenericAnimationData;
import com.linearpast.sccore.animation.data.util.AnimJson;
import com.linearpast.sccore.animation.data.util.AnimLayerJson;
import com.linearpast.sccore.animation.register.AnimationRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class JsonHelper {
    private final MinecraftServer server;
    JsonHelper(MinecraftServer server) {
        this.server = server;
    }

    public static JsonHelper getHelper(MinecraftServer server) {
        return new JsonHelper(server);
    }

    /**
     * Get animation path
     * @return path
     */
    private Path getAnimationPath() {
        Path dataPackPath = server.getWorldPath(LevelResource.DATAPACK_DIR);
        return dataPackPath.resolve("animation");
    }

    /**
     * Delete directories
     * @throws IOException Exception
     */
    public void clearPath() throws IOException {
        Path animationPath = getAnimationPath();
        if (!Files.exists(animationPath)) return;
        try (var pathStream = Files.walk(animationPath)) {
            pathStream.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (RuntimeException ignored) {}
    }

    /**
     * Generate all json from server animation
     * @param isLayer If layer
     * @param isReset If reset
     * @return Generate path
     */
    @Nullable
    public Path generateJson(boolean isLayer, boolean isReset) {
        try {
            Path animationPath = getAnimationPath();
            if (!Files.exists(animationPath)) {
                try {Files.createDirectories(animationPath);}
                catch (IOException e) { throw new RuntimeException(e); }
            }
            if(isReset) clearPath();

            if(isLayer) {
                return AnimLayerJson.Writer.syntaxImmediately(animationPath);
            } else {
                for (GenericAnimationData value : AnimationRegistry.getAnimations().values()) {
                    AnimJson.Writer.stream(animationPath, value).syntax();
                }
            }
            return animationPath;
        } catch (Exception ignored){}
        return null;
    }

    /**
     * Generate example json
     * @return Example json path
     */
    @Nullable
    public Path generateExample() {
        try {
            Path animationPath = getAnimationPath();
            return AnimJson.Writer.syntaxExample(animationPath);
        } catch (Exception ignored) {}
        return null;
    }
}
