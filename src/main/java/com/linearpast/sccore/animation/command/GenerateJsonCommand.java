package com.linearpast.sccore.animation.command;

import com.linearpast.sccore.SnowyCrescentCore;
import com.linearpast.sccore.animation.data.Animation;
import com.linearpast.sccore.animation.data.util.AnimJson;
import com.linearpast.sccore.animation.data.util.AnimLayerJson;
import com.linearpast.sccore.animation.register.AnimationRegistry;
import com.linearpast.sccore.core.datagen.ModLang;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class GenerateJsonCommand {
    public static void register(LiteralArgumentBuilder<CommandSourceStack> animCommand) {
        animCommand.then(literal("json").requires(cs -> cs.hasPermission(2))
                .then(literal("clearFile").executes(GenerateJsonCommand::clearJson))
                .then(literal("generate")
                        .then(literal("anim")
                                .then(literal("example").executes(GenerateJsonCommand::generateExample))
                                .executes(context -> generateJson(context, false, false))
                                .then(argument("reset", BoolArgumentType.bool())
                                        .executes(context ->
                                                generateJson(context, false, BoolArgumentType.getBool(context, "reset"))
                                        )
                                )
                        )
                        .then(literal("layer")
                                .executes(context -> generateJson(context, true, false))
                                .then(argument("reset", BoolArgumentType.bool())
                                        .executes(context ->
                                                generateJson(context, true, BoolArgumentType.getBool(context, "reset"))
                                        )
                                )
                        )
                )
        );
    }

    //clear path, remove file.
    private static void clearPath(Path dir) throws IOException {
        if (!Files.exists(dir)) return;
        try (var pathStream = Files.walk(dir)) {
            pathStream.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (RuntimeException ignored) {}
    }

    private static int generateJson(CommandContext<CommandSourceStack> context, boolean isLayer, boolean isReset) {
        CommandSourceStack source = context.getSource();
        try {
            //get animation path
            Path animationPath = getAnimationPath(source);
            if (!Files.exists(animationPath)) {
                try {Files.createDirectories(animationPath);}
                catch (IOException e) { throw new RuntimeException(e); }
            }
            if(isReset) clearPath(animationPath);

            //generate json layer or animation
            if(isLayer) {
                //generate
                Path path = AnimLayerJson.Writer.syntaxImmediately(animationPath);
                //send message
                MutableComponent component = Component.translatable(
                        ModLang.TranslatableMessage.ANIMATION_TO_JSON.getKey(),
                        "layer", "Server"
                ).withStyle(ChatFormatting.GREEN);
                component.append(Component.translatable(
                        ModLang.TranslatableMessage.ANIMATION_JSON_PATH.getKey(),
                        path.toString()
                ));
                source.sendSuccess(() -> component, true);
            } else {
                //generate
                for (Animation value : AnimationRegistry.getAnimations().values()) {
                    AnimJson.Writer.stream(animationPath, value).syntax();
                }
                //send message
                MutableComponent component = Component.translatable(
                        ModLang.TranslatableMessage.ANIMATION_TO_JSON.getKey(),
                        "anim", "Server"
                ).withStyle(ChatFormatting.GREEN);
                component.append(Component.translatable(
                        ModLang.TranslatableMessage.ANIMATION_JSON_PATH.getKey(),
                        animationPath.toString()
                ));
            }
        } catch (Exception e) {
            source.sendFailure(Component.translatable(
                    ModLang.TranslatableMessage.COMMAND_RUN_FAIL.getKey()
            ).withStyle(ChatFormatting.RED));
            SnowyCrescentCore.log.error(e.getMessage());
            return 0;
        }
        return 1;
    }

    private static int clearJson(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        try {
            //clear path
            Path animationPath = getAnimationPath(source);
            clearPath(animationPath);
        } catch (Exception e) {
            source.sendFailure(Component.translatable(
                    ModLang.TranslatableMessage.COMMAND_RUN_FAIL.getKey()
            ).withStyle(ChatFormatting.RED));
            SnowyCrescentCore.log.error(e.getMessage());
            return 0;
        }
        return 1;
    }

    private static int generateExample(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        try {
            //generate
            Path animationPath = getAnimationPath(source);
            Path path = AnimJson.Writer.syntaxExample(animationPath);
            //send message
            MutableComponent component = Component.translatable(
                    ModLang.TranslatableMessage.ANIMATION_TO_JSON.getKey(),
                    "anim example", "Server"
            ).withStyle(ChatFormatting.GREEN);
            component.append(Component.translatable(
                    ModLang.TranslatableMessage.ANIMATION_JSON_PATH.getKey(),
                    path.toString()
            ));
        } catch (Exception e) {
            source.sendFailure(Component.translatable(
                    ModLang.TranslatableMessage.COMMAND_RUN_FAIL.getKey()
            ).withStyle(ChatFormatting.RED));
            SnowyCrescentCore.log.error(e.getMessage());
            return 0;
        }
        return 1;
    }

    /**
     * Get animation path
     * @param source command source
     * @return path
     */
    private static Path getAnimationPath(CommandSourceStack source) {
        MinecraftServer server = source.getServer();
        Path dataPackPath = server.getWorldPath(LevelResource.DATAPACK_DIR);
        return dataPackPath.resolve("animation");
    }
}
