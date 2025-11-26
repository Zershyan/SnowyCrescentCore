package com.linearpast.sccore.animation.command;

import com.linearpast.sccore.animation.register.AnimationRegistry;
import com.linearpast.sccore.core.datagen.ModLang;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Random;

import static net.minecraft.commands.Commands.literal;

public class ListServerCommand {
    public static void register(LiteralArgumentBuilder<CommandSourceStack> animCommand) {
        animCommand.then(literal("list")
                .then(literal("layers").executes(ListServerCommand::listLayers))
                .then(literal("serverAnimations")
                        .executes(ListServerCommand::listAnimations)
                )
        );
    }

    private static int listAnimations(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();
            List<ResourceLocation> list = AnimationRegistry.getAnimations().keySet().stream().toList();
            source.sendSuccess(() -> Component.translatable(
                    ModLang.TranslatableMessage.LIST_ANIMATION_RESOURCE.getKey(),
                    "Server", "Animations", getString(list)
            ), false);
            return 1;
        } catch (Exception ignored) {}
        return 0;
    }

    private static int listLayers(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();
            List<ResourceLocation> list = AnimationRegistry.getLayers().keySet().stream().toList();
            source.sendSuccess(() -> Component.translatable(
                    ModLang.TranslatableMessage.LIST_ANIMATION_RESOURCE.getKey(),
                    "Server", "Layers", getString(list)
            ), false);
            return 1;
        } catch (Exception ignored) {}
        return 0;
    }

    public static Component getString(List<ResourceLocation> list) {
        MutableComponent component = Component.empty();
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < list.size(); i++) {
            component.append(Component.literal(list.get(i).toString()).withStyle(randomColor(random)));
            if(i < list.size()-1){
                component.append(", ").withStyle(ChatFormatting.WHITE);
            }
        }
        component.append(".").withStyle(Style.EMPTY.withColor(ChatFormatting.RED));
        return component;
    }

    private static ChatFormatting randomColor(Random random){
        int i = random.nextInt(14) + 1;
        ChatFormatting byId = ChatFormatting.getById(i);
        return byId == null ? ChatFormatting.WHITE : byId;
    }
}
