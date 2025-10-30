package com.linearpast.sccore.animation.command;

import com.linearpast.sccore.SnowyCrescentCore;
import com.linearpast.sccore.animation.AnimationUtils;
import com.linearpast.sccore.animation.command.argument.AnimationArgument;
import com.linearpast.sccore.animation.command.argument.AnimationLayerArgument;
import com.linearpast.sccore.core.configs.ModConfigs;
import com.linearpast.sccore.core.datagen.ModLang;
import com.mojang.brigadier.arguments.BoolArgumentType;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class CombineAnimCommand {
    private static final Map<UUID, Long> lastInvitedMap = new HashMap<>();
    record InviteRecord(long time, ResourceLocation layer, ResourceLocation animation, boolean isForce){}
    private static final Map<UUID, Map<UUID, InviteRecord>> invites = new HashMap<>();
    public static void register(LiteralArgumentBuilder<CommandSourceStack> animCommand) {
        animCommand.then(literal("invite")
                .then(argument("player", EntityArgument.player())
                        .then(argument("layer", AnimationLayerArgument.layer())
                                .then(argument("anim", AnimationArgument.animation())
                                        .executes(CombineAnimCommand::invite)
                                        .then(argument("force", BoolArgumentType.bool())
                                                .executes(CombineAnimCommand::invite)
                                        )
                                )
                        )
                )
                .then(literal("accept")
                        .then(argument("player", EntityArgument.player())
                                .executes(CombineAnimCommand::acceptInvite)
                        )
                )
        );
    }

    private static int invite(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        try {
            //get info
            boolean force = false;
            try {force = BoolArgumentType.getBool(context, "force");}
            catch (Exception ignored) {}
            ServerPlayer player = source.getPlayerOrException();
            ServerPlayer target = EntityArgument.getPlayer(context, "player");

            //cooldown
            Long lastInvited = lastInvitedMap.getOrDefault(player.getUUID(), null);
            long now = System.currentTimeMillis();
            int inviteCooldown = ModConfigs.Server.inviteCooldown.get() * 1000;
            if(!(lastInvited == null || now - lastInvited > inviteCooldown)) {
                source.sendFailure(Component.translatable(
                        ModLang.TranslatableMessage.COMMAND_COOLDOWN.getKey()
                ).withStyle(ChatFormatting.RED));
                return 0;
            }
            lastInvitedMap.put(player.getUUID(), now);

            String layerString = AnimationLayerArgument.getLayer(context, "layer");
            String animString = AnimationArgument.getAnimation(context, "anim");
            ResourceLocation layer = new ResourceLocation(layerString);
            ResourceLocation anim = new ResourceLocation(animString);

            //test info present
            boolean animationPresent = AnimationUtils.isAnimationPresent(anim);
            boolean animationLayerPresent = AnimationUtils.isAnimationLayerPresent(layer);
            if(!animationLayerPresent || !animationPresent) throw new Exception();

            //update static cache
            Map<UUID, InviteRecord> inviteRecordMap = invites.getOrDefault(player.getUUID(), new HashMap<>());
            inviteRecordMap.put(target.getUUID(), new InviteRecord(System.currentTimeMillis(), layer, anim, force));
            invites.put(player.getUUID(), inviteRecordMap);

            //click event
            Style pStyle = Style.EMPTY.withBold(true).withColor(ChatFormatting.GREEN).withClickEvent(
                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sccore anim invite accept " + player.getName().getString())
            ).withUnderlined(true);
            //send message
            target.sendSystemMessage(Component.translatable(
                    ModLang.TranslatableMessage.INVITED_MESSAGE.getKey(),
                    player.getName().copy(),
                    anim.toString()
            ).append(Component.translatable(
                    ModLang.TranslatableMessage.ACCEPT_MESSAGE_CLICK.getKey()
            ).setStyle(pStyle)));
            source.sendSuccess(() -> Component.translatable(
                    ModLang.TranslatableMessage.INVITE_MESSAGE.getKey()
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

    private static int acceptInvite(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        try {
            ServerPlayer target = source.getPlayerOrException();
            ServerPlayer player = EntityArgument.getPlayer(context, "player");

            Map<UUID, InviteRecord> inviteRecordMap = invites.getOrDefault(player.getUUID(), null);
            if(inviteRecordMap == null) throw new Exception();
            InviteRecord inviteRecord = inviteRecordMap.getOrDefault(target.getUUID(), null);
            if(inviteRecord == null) throw new Exception();

            //test if expired
            long now = System.currentTimeMillis();
            Integer inviteDuration = ModConfigs.Server.inviteDuration.get();
            if(now - inviteRecord.time > inviteDuration * 1000 || inviteDuration == 0) {
                source.sendFailure(Component.translatable(
                        ModLang.TranslatableMessage.ACCEPT_INVITE_EXPIRED.getKey(),
                        inviteDuration
                ).withStyle(ChatFormatting.RED));
                player.sendSystemMessage(Component.translatable(
                        ModLang.TranslatableMessage.INVITE_EXPIRED.getKey(),
                        target.getName().copy(),
                        inviteDuration
                ).withStyle(ChatFormatting.RED));
                return 0;
            }

            //test if in range
            Integer inviteDistance = ModConfigs.Server.inviteDistance.get();
            if(player.position().distanceToSqr(target.position()) > inviteDistance * inviteDistance || inviteDistance == 0) {
                source.sendFailure(Component.translatable(
                        ModLang.TranslatableMessage.ACCEPT_INVITE_TOO_FAR.getKey(),
                        inviteDistance
                ).withStyle(ChatFormatting.RED));
                player.sendSystemMessage(Component.translatable(
                        ModLang.TranslatableMessage.INVITE_TOO_FAR.getKey(),
                        target.getName().copy(),
                        inviteDistance
                ).withStyle(ChatFormatting.RED));
                return 0;
            }
            inviteRecordMap.remove(target.getUUID());
            invites.put(player.getUUID(), inviteRecordMap);

            //play animation
            AnimationUtils.startAnimationTogether(player, inviteRecord.layer, inviteRecord.animation, inviteRecord.isForce, target);

            //send message
            source.sendSuccess(() -> Component.translatable(
                    ModLang.TranslatableMessage.ACCEPT_INVITE_SUCCESS.getKey()
            ).withStyle(ChatFormatting.GREEN), true);
            player.sendSystemMessage(Component.translatable(
                    ModLang.TranslatableMessage.INVITE_SUCCESS.getKey(),
                    target.getName().copy()
            ).withStyle(ChatFormatting.GREEN));
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
