package com.linearpast.sccore.animation.command;

import com.linearpast.sccore.SnowyCrescentCore;
import com.linearpast.sccore.animation.helper.JsonHelper;
import com.linearpast.sccore.core.datagen.ModLang;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.nio.file.Path;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class JsonCommand {
    public static void register(LiteralArgumentBuilder<CommandSourceStack> animCommand) {
        animCommand.then(literal("json").requires(cs -> cs.hasPermission(2))
                .then(literal("clearFile").executes(JsonCommand::clearJson))
                .then(literal("generate")
                        .then(literal("anim")
                                .then(literal("example").executes(JsonCommand::generateExample))
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

    private static int generateJson(CommandContext<CommandSourceStack> context, boolean isLayer, boolean isReset) {
        CommandSourceStack source = context.getSource();
        try {
            //generate
            JsonHelper helper = JsonHelper.getHelper(source.getServer());
            Path path = helper.generateJson(isLayer, isReset);

            if(path == null) throw new Exception();
            MutableComponent component;
            String key = ModLang.TranslatableMessage.ANIMATION_TO_JSON.getKey();
            if(isLayer) component = Component.translatable(key, "layer", "Server");
            else component = Component.translatable(key, "anim", "Server");

            component.withStyle(ChatFormatting.GREEN).append(Component.translatable(
                    ModLang.TranslatableMessage.ANIMATION_JSON_PATH.getKey(),
                    path.toString()
            ));

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

    private static int clearJson(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        try {
            //clear path
            JsonHelper.getHelper(source.getServer()).clearPath();
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
            JsonHelper helper = JsonHelper.getHelper(source.getServer());
            Path path = helper.generateExample();
            if(path == null) throw new Exception();

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
}
