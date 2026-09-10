package io.zershyan.sccore.compat.animation.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.JsonOps;
import io.zershyan.sccore.SCCore;
import io.zershyan.sccore.compat.animation.core.SyncAnimationFactory;
import io.zershyan.sccore.compat.animation.data.AABBMovement;
import io.zershyan.sccore.compat.animation.data.ClientAnimation;
import io.zershyan.sccore.compat.animation.data.RideData;
import io.zershyan.sccore.compat.animation.data.ServerAnimation;
import io.zershyan.sccore.compat.animation.data.camera.CameraChange;
import io.zershyan.sccore.compat.animation.data.camera.CameraData;
import io.zershyan.sccore.datagen.init.SCCKeyLang;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;

import static net.minecraft.commands.Commands.literal;

public class JsonCommand {
    public static void register(LiteralArgumentBuilder<CommandSourceStack> animCommand) {
        animCommand.then(literal("json").then(literal("example")
                .executes(JsonCommand::generateExample)));
    }

    private static int generateExample(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        try {
            //generate
            String dirString = "sccoreAnimationExample";
            Path dir = Paths.get(dirString);
            if (!Files.exists(dir)) Files.createDirectories(dir);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            Path layerExample = dir.resolve("example_layer.json");
            if (!Files.exists(layerExample)) Files.createFile(layerExample);
            HashMap<ResourceLocation, Integer> exampleLayer = new HashMap<>();
            exampleLayer.put(SCCore.id("example_layer"), 40);
            exampleLayer.put(ResourceLocation.fromNamespaceAndPath("your_namespace", "example_layer"), 41);
            JsonElement exampleLayerJson = SyncAnimationFactory.LAYER_CODEC.encodeStart(JsonOps.INSTANCE, exampleLayer).getOrThrow();
            Files.writeString(layerExample, gson.toJson(exampleLayerJson), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

            Path clientAnimExample = dir.resolve("example_client_animation.json");
            if (!Files.exists(clientAnimExample)) Files.createFile(clientAnimExample);
            TreeMap<Integer, CameraData> movement = new TreeMap<>();
            TreeMap<Integer, CameraData> movement1 = new TreeMap<>();
            movement.put(1, CameraData.ZERO);
            movement1.put(2, CameraData.ZERO);
            ClientAnimation clientAnimation = new ClientAnimation(
                    SCCore.id("animation_key"), Optional.of("测试动画"), 0,
                    Optional.of(new RideData(List.of(SCCore.id("sub_animation")), new Vec3(0, 0, 0), 100, 90, 0)), true,
                    new CameraChange(movement),
                    new CameraChange(movement1)
            );
            JsonElement exampleClientAnimationJson = ClientAnimation.CODEC.encodeStart(JsonOps.INSTANCE, clientAnimation).getOrThrow();
            Files.writeString(clientAnimExample, gson.toJson(exampleClientAnimationJson), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

            Path serverAnimExample = dir.resolve("example_server_animation.json");
            if (!Files.exists(serverAnimExample)) Files.createFile(serverAnimExample);
            ServerAnimation serverAnimation = new ServerAnimation(
                    SCCore.id("animation_key"), Optional.of("测试动画"), 0,
                    Optional.of(new RideData(List.of(SCCore.id("sub_animation")), new Vec3(0, 0, 0), 100, 90, 0)), true,
                    new AABBMovement(true).add(1, new AABB(Vec3.ZERO, Vec3.ZERO.add(2.0, 2.0, 2.0))), 1.0f
            );
            JsonElement exampleServerAnimationJson = ServerAnimation.CODEC.encodeStart(JsonOps.INSTANCE, serverAnimation).getOrThrow();
            Files.writeString(serverAnimExample, gson.toJson(exampleServerAnimationJson), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            source.sendSuccess(() -> SCCKeyLang.AnimationToJson.get(dirString).withStyle(ChatFormatting.GREEN), true);
        } catch (Exception e) {
            source.sendFailure(SCCKeyLang.CommandRunFail.withStyle(ChatFormatting.RED));
            SCCore.log.error(e.getMessage());
            return 0;
        }
        return 1;
    }
}
