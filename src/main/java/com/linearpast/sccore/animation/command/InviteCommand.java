package com.linearpast.sccore.animation.command;

import com.linearpast.sccore.SnowyCrescentCore;
import com.linearpast.sccore.animation.AnimationApi;
import com.linearpast.sccore.animation.command.argument.AnimationArgument;
import com.linearpast.sccore.animation.command.argument.AnimationLayerArgument;
import com.linearpast.sccore.animation.command.exception.ApiBackException;
import com.linearpast.sccore.animation.utils.ApiBack;
import com.linearpast.sccore.core.configs.ModConfigs;
import com.linearpast.sccore.core.datagen.ModLang;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class InviteCommand {
    public static void register(LiteralArgumentBuilder<CommandSourceStack> animCommand) {
        animCommand.then(literal("invite")
                .then(argument("players", EntityArgument.players())
                        .then(argument("layer", AnimationLayerArgument.layer())
                                .then(argument("anim", AnimationArgument.animation())
                                        .executes(InviteCommand::invite)
                                )
                        )
                )
                .then(literal("accept")
                        .then(argument("player", EntityArgument.player())
                                .executes(InviteCommand::acceptInvite)
                        )
                )
        );
    }

    private static int invite(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        try {
            ServerPlayer player = source.getPlayerOrException();
            Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");

            String layerString = AnimationLayerArgument.getLayer(context, "layer");
            String animString = AnimationArgument.getAnimation(context, "anim");
            ResourceLocation layer = new ResourceLocation(layerString);
            ResourceLocation anim = new ResourceLocation(animString);

            //test info present
            List<UUID> targets = players.stream().map(Entity::getUUID).toList();
            ApiBack back = AnimationApi.getHelper(player).inviteAnimation(layer, anim, targets);
            if(back == ApiBack.COOLDOWN) {
                int cooldown = ModConfigs.Server.inviteCooldown.get();
                throw ApiBackException.withCooldown(cooldown);
            }
            if(back != ApiBack.SUCCESS) throw new ApiBackException(back);

            //click event
            Style pStyle = Style.EMPTY.withBold(true).withColor(ChatFormatting.GREEN).withClickEvent(
                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sccore anim invite accept " + player.getName().getString())
            ).withUnderlined(true);
            //send message
            for (ServerPlayer target : players) {
                target.sendSystemMessage(Component.translatable(
                        ModLang.TranslatableMessage.INVITED_MESSAGE.getKey(),
                        player.getName().copy(),
                        anim.toString()
                ).append(Component.translatable(
                        ModLang.TranslatableMessage.ACCEPT_MESSAGE_CLICK.getKey()
                ).setStyle(pStyle)));
            }
            source.sendSuccess(() -> Component.translatable(
                    ModLang.TranslatableMessage.INVITE_MESSAGE.getKey()
            ).withStyle(ChatFormatting.GREEN), true);
            return 1;
        } catch (ApiBackException e) {
            source.sendFailure(e.getCommandFailBack().withStyle(ChatFormatting.RED));
        } catch (Exception e) {
            source.sendFailure(Component.translatable(
                    ModLang.TranslatableMessage.COMMAND_RUN_FAIL.getKey()
            ).withStyle(ChatFormatting.RED));
            SnowyCrescentCore.log.error(e.getMessage());
        }
        return 0;
    }

    private static int acceptInvite(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        try {
            ServerPlayer player = source.getPlayerOrException();
            ServerPlayer inviter = EntityArgument.getPlayer(context, "player");

            //play animation
            ApiBack back = AnimationApi.getHelper(player).acceptInvite(inviter);
            if(back == ApiBack.OUT_RANGE) throw ApiBackException.withOutRange(ModConfigs.Server.inviteValidDistance.get());
            if(back != ApiBack.SUCCESS) throw new ApiBackException(back);

            //send message
            source.sendSuccess(() -> Component.translatable(
                    ModLang.TranslatableMessage.ACCEPT_INVITE_SUCCESS.getKey()
            ).withStyle(ChatFormatting.GREEN), true);
            inviter.sendSystemMessage(Component.translatable(
                    ModLang.TranslatableMessage.INVITE_SUCCESS.getKey(),
                    player.getName().copy()
            ).withStyle(ChatFormatting.GREEN));
            return 1;
        } catch (ApiBackException e) {
            source.sendFailure(e.getCommandFailBack().withStyle(ChatFormatting.RED));
        } catch (Exception e) {
            source.sendFailure(Component.translatable(
                    ModLang.TranslatableMessage.COMMAND_RUN_FAIL.getKey()
            ).withStyle(ChatFormatting.RED));
            SnowyCrescentCore.log.error(e.getMessage());
        }
        return 0;
    }
}
