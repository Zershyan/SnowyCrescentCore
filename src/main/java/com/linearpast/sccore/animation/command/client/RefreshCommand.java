package com.linearpast.sccore.animation.command.client;

import com.linearpast.sccore.SnowyCrescentCore;
import com.linearpast.sccore.animation.AnimationApi;
import com.linearpast.sccore.animation.register.AnimationRegistry;
import com.linearpast.sccore.core.datagen.ModLang;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Set;

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

            for (ResourceLocation layer : Set.copyOf(AnimationRegistry.getLayers().keySet())) {
                PlayerAnimationAccess.PlayerAssociatedAnimationData playerAssociatedData = PlayerAnimationAccess.getPlayerAssociatedData(player);
                IAnimation iAnimation = playerAssociatedData.get(layer);
                if(iAnimation == null) continue;
                ResourceLocation playing = AnimationApi.getHelper(player).getAnimationPlaying(layer);
                if(playing == null) playerAssociatedData.set(layer, null);
            }

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
