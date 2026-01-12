package io.zershyan.sccore.animation.command.client;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.zershyan.sccore.animation.command.ListServerCommand;
import io.zershyan.sccore.animation.register.RawAnimationRegistry;
import io.zershyan.sccore.core.datagen.ModLang;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

import static net.minecraft.commands.Commands.literal;

@OnlyIn(Dist.CLIENT)
public class ListClientCommand {
    public static void register(LiteralArgumentBuilder<CommandSourceStack> animCommand) {
        animCommand.then(literal("list").then(literal("clientAnimations")
                .executes(ListClientCommand::listAnimations))
        );
    }

    private static int listAnimations(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();
            List<ResourceLocation> list = RawAnimationRegistry.getAnimations().keySet().stream().toList();
            source.sendSuccess(() -> Component.translatable(
                    ModLang.TranslatableMessage.LIST_ANIMATION_RESOURCE.getKey(),
                    "Client", "Animations", ListServerCommand.getString(list)
            ), false);
            return 1;
        } catch (Exception ignored) {}
        return 0;
    }
}
