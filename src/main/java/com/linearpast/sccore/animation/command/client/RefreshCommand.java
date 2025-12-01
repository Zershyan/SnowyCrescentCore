package com.linearpast.sccore.animation.command.client;

import com.linearpast.sccore.SnowyCrescentCore;
import com.linearpast.sccore.animation.AnimationApi;
import com.linearpast.sccore.core.datagen.ModLang;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static net.minecraft.commands.Commands.literal;

@OnlyIn(Dist.CLIENT)
public class RefreshCommand {
    public static void register(LiteralArgumentBuilder<CommandSourceStack> animCommand){
        animCommand.then(literal("refresh").executes(RefreshCommand::refresh));
    }

    private static int refresh(CommandContext<CommandSourceStack> ctx){
        CommandSourceStack source = ctx.getSource();
        try {
            Minecraft instance = Minecraft.getInstance();
            LocalPlayer player = instance.player;
            if(player == null) throw new RuntimeException();
            AnimationApi.getHelper(player).refreshAnimation();
            source.sendSuccess(() -> Component.translatable(
                    ModLang.TranslatableMessage.REFRESH_ANIMATIONS.getKey()
            ).withStyle(ChatFormatting.GREEN), true);
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
