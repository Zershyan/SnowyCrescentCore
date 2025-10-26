package com.linearpast.sccore.animation.command.client;

import com.linearpast.sccore.animation.AnimationUtils;
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
public class AnimationRefreshCommand {
    public static void register(LiteralArgumentBuilder<CommandSourceStack> animCommand){
        animCommand.then(literal("refresh").executes(AnimationRefreshCommand::refresh));
    }

    private static int refresh(CommandContext<CommandSourceStack> ctx){
        CommandSourceStack source = ctx.getSource();
        try {
            Minecraft instance = Minecraft.getInstance();
            LocalPlayer player = instance.player;
            if(player == null) throw new RuntimeException();
            AnimationUtils.refreshAnimation(player);
            source.sendSuccess(() -> Component.literal("Animation refreshed.").withStyle(ChatFormatting.GREEN), true);
        } catch (Exception e) {
            source.sendFailure(Component.literal("Run command failure.").withStyle(ChatFormatting.RED));
            return 0;
        }
        return 1;
    }
}
