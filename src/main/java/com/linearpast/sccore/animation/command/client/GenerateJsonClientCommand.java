package com.linearpast.sccore.animation.command.client;

import com.linearpast.sccore.SnowyCrescentCore;
import com.linearpast.sccore.animation.data.Animation;
import com.linearpast.sccore.animation.data.util.AnimJson;
import com.linearpast.sccore.animation.data.util.AnimLayerJson;
import com.linearpast.sccore.animation.register.AnimationRegistry;
import com.linearpast.sccore.core.datagen.ModLang;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class GenerateJsonClientCommand {
    public static void register(LiteralArgumentBuilder<CommandSourceStack> animCommand) {
        animCommand.then(literal("jsonClient")
                .then(argument("path", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            try {
                                File gameDirectory = Minecraft.getInstance().gameDirectory;
                                Path animation = gameDirectory.toPath().resolve(SnowyCrescentCore.MODID).resolve("animation");
                                if(!animation.toFile().exists()) {
                                    Files.createDirectories(animation);
                                }
                                String replace = animation.toString().replace("\\", "\\\\");
                                builder.suggest("\"" + replace + "\"");
                                return builder.buildFuture();
                            } catch (Exception e) { return builder.buildFuture(); }
                        })
                        .then(literal("clearFile").executes(GenerateJsonClientCommand::clearJson))
                        .then(literal("generate")
                                .then(literal("anim")
                                        .then(literal("example").executes(GenerateJsonClientCommand::generateExample))
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
                )
        );
    }

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
            String pathString = StringArgumentType.getString(context, "path");
            Path animationPath = Minecraft.getInstance().gameDirectory.toPath().resolve(pathString).resolve("animation");
            if (!Files.exists(animationPath)) {
                try {Files.createDirectories(animationPath);}
                catch (IOException e) { throw new RuntimeException(e); }
            }
            if(isReset) clearPath(animationPath);
            if(isLayer) {
                Path path = AnimLayerJson.Writer.syntaxImmediately(animationPath);
                MutableComponent component = Component.translatable(
                        ModLang.TranslatableMessage.ANIMATION_TO_JSON.getKey(),
                        "layer", "Client"
                );
                Style style = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, animationPath.toString()))
                        .withColor(ChatFormatting.GREEN).withBold(true).withUnderlined(true);
                component.append(Component.translatable(
                        ModLang.TranslatableMessage.ANIMATION_JSON_PATH.getKey(),
                        path.toString()
                ).setStyle(style));
                source.sendSuccess(() -> component, true);
            } else {
                for (Animation value : AnimationRegistry.getAnimations().values()) {
                    AnimJson.Writer.stream(animationPath, value).syntax();
                }
                MutableComponent component = Component.translatable(
                        ModLang.TranslatableMessage.ANIMATION_TO_JSON.getKey(),
                        "anim", "Client"
                );
                Style style = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, animationPath.toString()))
                        .withColor(ChatFormatting.GREEN).withBold(true).withUnderlined(true);
                component.append(Component.translatable(
                        ModLang.TranslatableMessage.ANIMATION_JSON_PATH.getKey(),
                        animationPath.toString()
                ).withStyle(style));
                source.sendSuccess(() -> component, true);
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
            String pathString = StringArgumentType.getString(context, "path");
            Path animationPath = Minecraft.getInstance().gameDirectory.toPath().resolve(pathString).resolve("animation");
            clearPath(animationPath);
            source.sendSuccess(() -> Component.translatable(
                    ModLang.TranslatableMessage.COMMAND_RUN_SUCCESS.getKey()
            ), true);
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
            String pathString = StringArgumentType.getString(context, "path");
            Path animationPath = Minecraft.getInstance().gameDirectory.toPath().resolve(pathString);
            Path path = AnimJson.Writer.syntaxExample(animationPath);
            MutableComponent component = Component.translatable(
                    ModLang.TranslatableMessage.ANIMATION_TO_JSON.getKey(),
                    "anim example", "Client"
            );
            Style style = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, path.toString()))
                    .withColor(ChatFormatting.GREEN).withBold(true).withUnderlined(true);
            component.append(Component.translatable(
                    ModLang.TranslatableMessage.ANIMATION_JSON_PATH.getKey(),
                    path.toString()
            ).setStyle(style));
            source.sendSuccess(() -> component, true);
        } catch (Exception e) {
            source.sendFailure(Component.translatable(
                    ModLang.TranslatableMessage.COMMAND_RUN_FAIL.getKey()
            ).withStyle(ChatFormatting.RED));
            SnowyCrescentCore.log.error(e.getMessage());
            return 0;
        }
        return 1;
    }
}
