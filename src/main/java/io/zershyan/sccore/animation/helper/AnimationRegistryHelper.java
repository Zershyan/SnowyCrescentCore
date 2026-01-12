package io.zershyan.sccore.animation.helper;

import com.google.gson.JsonElement;
import io.zershyan.sccore.SnowyCrescentCore;
import io.zershyan.sccore.animation.AnimationApi;
import io.zershyan.sccore.animation.data.GenericAnimationData;
import io.zershyan.sccore.animation.data.RawAnimationData;
import io.zershyan.sccore.animation.data.util.AnimJson;
import io.zershyan.sccore.animation.data.util.AnimLayerJson;
import io.zershyan.sccore.animation.data.util.RawAnimJson;
import io.zershyan.sccore.animation.event.create.AnimationRegisterEvent;
import io.zershyan.sccore.animation.network.toclient.AnimationClientStatusPacket;
import io.zershyan.sccore.animation.network.toclient.AnimationJsonPacket;
import io.zershyan.sccore.animation.register.AnimationRegistry;
import io.zershyan.sccore.animation.register.RawAnimationRegistry;
import io.zershyan.sccore.animation.utils.FileUtils;
import io.zershyan.sccore.core.ModChannel;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.common.MinecraftForge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AnimationRegistryHelper {
    AnimationRegistryHelper() {}
    public static AnimationRegistryHelper getHelper() {
        return new AnimationRegistryHelper();
    }
    public Server server(MinecraftServer server) {
        return new Server(server);
    }

    public Client client() {
        return new Client();
    }

    public static class Server extends AnimationRegistryHelper {
        private final MinecraftServer server;
        Server(MinecraftServer server) {
            this.server = server;
        }

        public void reloadAnimations() {
            Path dataPackPath = server.getWorldPath(LevelResource.DATAPACK_DIR);
            Path animationPath = AnimationApi.getJsonHelper(server).getAnimationPath();
            if (!Files.exists(animationPath)) {
                try {
                    Files.createDirectories(animationPath);
                } catch (IOException e) { return; }
            }

            FileUtils.safeUnzip(dataPackPath.resolve("sccore.zip").toString(), animationPath.toAbsolutePath().toString());
            Set<Path> animZipPaths = FileUtils.getAllFile(
                    animationPath, path -> path.toString().endsWith(".anim.zip")
            );
            Set<Path> layerZipPaths = FileUtils.getAllFile(
                    animationPath, path -> path.toString().endsWith(".layer.zip")
            );
            for (Path zipPath : animZipPaths) {
                FileUtils.safeUnzip(zipPath.toString(), animationPath.toAbsolutePath().toString());
            }
            for (Path zipPath : layerZipPaths) {
                FileUtils.safeUnzip(zipPath.toString(), animationPath.toAbsolutePath().toString());
            }

            Set<Path> animPaths = FileUtils.getAllFile(
                    animationPath, path -> path.toString().endsWith(".anim.json")
            );
            Set<Path> layerPaths = FileUtils.getAllFile(
                    animationPath, path -> path.getFileName().toString().equals("animation.layer.json")
            );
            Set<GenericAnimationData> animationsSet = new HashSet<>();
            Map<ResourceLocation, Integer> layersMap = new HashMap<>();
            for (Path path : animPaths) {
                try {
                    AnimJson.Reader reader = AnimJson.Reader.stream(path);
                    GenericAnimationData anim = reader.parse();
                    animationsSet.add(anim);
                } catch (Exception ignored) {
                    SnowyCrescentCore.log.error("Failed to parse animation JSON: {}", path.toString());
                }
            }
            for (Path path : layerPaths) {
                try {
                    AnimLayerJson.Reader reader = AnimLayerJson.Reader.stream(path);
                    Map<ResourceLocation, Integer> parse = reader.parse();
                    layersMap.putAll(parse);
                } catch (Exception ignored) {
                    SnowyCrescentCore.log.error("Failed to parse layer JSON: {}", path.toString());
                }
            }

            AnimationRegisterEvent.Animation animationRegisterEvent = new AnimationRegisterEvent.Animation();
            MinecraftForge.EVENT_BUS.post(animationRegisterEvent);
            Map<ResourceLocation, GenericAnimationData> animationMap = animationRegisterEvent.getAnimations();
            animationMap.putAll(animationsSet.stream().collect(Collectors.toMap(GenericAnimationData::getKey, animation -> animation)));
            AnimationRegistry.resetAnimations(animationMap);

            AnimationRegisterEvent.Layer layerRegisterEvent = new AnimationRegisterEvent.Layer();
            MinecraftForge.EVENT_BUS.post(layerRegisterEvent);
            Map<ResourceLocation, Integer> layerMap = layerRegisterEvent.getLayers();
            layerMap.putAll(layersMap);
            AnimationRegistry.resetLayers(layerMap);


        }

        public void syncPlayerAnimations(ServerPlayer serverPlayer) {
            Path animationPath = AnimationApi.getJsonHelper(server).getAnimationPath();
            if (!Files.exists(animationPath)) {
                try {Files.createDirectories(animationPath);}
                catch (IOException e) { return; }
            }
            ModChannel.sendToPlayer(new AnimationClientStatusPacket(AnimationClientStatusPacket.Status.ANIM_CACHE_CLEAR), serverPlayer);
            for (GenericAnimationData value : AnimationRegistry.getAnimations().values()) {
                JsonElement json = AnimJson.Writer.stream(value).toJson();
                String string = json.toString();
                ModChannel.sendToPlayer(new AnimationJsonPacket(string, false), serverPlayer);
            }
            ModChannel.sendToPlayer(new AnimationClientStatusPacket(AnimationClientStatusPacket.Status.ANIM_REGISTER), serverPlayer);
            ModChannel.sendToPlayer(new AnimationClientStatusPacket(AnimationClientStatusPacket.Status.LAYER_CACHE_CLEAR), serverPlayer);
            Map<String, JsonElement> jsonElementMap = AnimLayerJson.Writer.stream(animationPath).allToJson();
            jsonElementMap.forEach((key, value) ->
                    ModChannel.sendToPlayer(new AnimationJsonPacket(value.toString(), true), serverPlayer)
            );
            ModChannel.sendToPlayer(new AnimationClientStatusPacket(AnimationClientStatusPacket.Status.LAYER_REGISTER), serverPlayer);
        }

        public void syncAllPlayerAnimations() {
            for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers()) {
                syncPlayerAnimations(serverPlayer);
            }
        }

        public void reloadAnimationsWithSync() {
            reloadAnimations();
            syncAllPlayerAnimations();
        }
    }

    public static class Client extends AnimationRegistryHelper {
        public void reloadAnimations() {
            RawAnimationRegistry.resetAnimations();
            AnimationRegisterEvent.RawAnimation event = new AnimationRegisterEvent.RawAnimation();
            MinecraftForge.EVENT_BUS.post(event);
            Map<ResourceLocation, RawAnimationData> animationDataMap = new HashMap<>(event.getAnimations());
            Minecraft instance = Minecraft.getInstance();
            Path dataPackPath = instance.getResourcePackDirectory();
            Path animationPath = dataPackPath.resolve(SnowyCrescentCore.MODID).resolve("animation");
            if (!Files.exists(animationPath)) {
                try {
                    Files.createDirectories(animationPath);
                } catch (IOException e) { return; }
            }
            FileUtils.safeUnzip(dataPackPath.resolve("sccore.zip").toString(), animationPath.toAbsolutePath().toString());
            Set<Path> animZipPaths = FileUtils.getAllFile(
                    animationPath, path -> path.toString().endsWith(".anim.zip")
            );
            for (Path zipPath : animZipPaths) {
                FileUtils.safeUnzip(zipPath.toString(), animationPath.toAbsolutePath().toString());
            }
            Set<Path> animPaths = FileUtils.getAllFile(
                    animationPath, path -> path.toString().endsWith(".anim.json")
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
            RawAnimationRegistry.registerAnimations(animationDataMap);
        }
    }
}
