package io.zershyan.sccore.animation.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.zershyan.sccore.SnowyCrescentCore;
import io.zershyan.sccore.animation.AnimationApi;
import io.zershyan.sccore.core.datagen.ModLang;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import static net.minecraft.commands.Commands.literal;

public class ReloadCommand {
    public static void register(LiteralArgumentBuilder<CommandSourceStack> animCommand) {
        animCommand.then(literal("reload")
                .requires(source -> source.hasPermission(2))
                .executes(ReloadCommand::reload)
        );
    }

    private static int reload(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        try {
            AnimationApi.getRegistryHelper().server(source.getServer()).reloadAnimationsWithSync();
            source.sendSuccess(() -> Component.translatable(
                    ModLang.TranslatableMessage.COMMAND_RUN_SUCCESS.getKey()
            ).withStyle(ChatFormatting.GREEN), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.translatable(
                    ModLang.TranslatableMessage.COMMAND_RUN_FAIL.getKey()
            ).withStyle(ChatFormatting.RED));
            SnowyCrescentCore.log.error(e.getMessage());
        }
        return 0;
    }
}
