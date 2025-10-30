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

/**
 * Request target player play animation.
 */
public class RequestAnimCommand {
    private static final Map<UUID, Long> lastRequestedMap = new HashMap<>();
    record InviteRecord(long time, ResourceLocation layer, ResourceLocation animation, boolean withRide, boolean isForce) {}
    private static final Map<UUID, Map<UUID, InviteRecord>> invites = new HashMap<>();
    public static void register(LiteralArgumentBuilder<CommandSourceStack> animCommand) {
        animCommand.then(literal("request")
                .then(argument("player", EntityArgument.player()).then(
                        argument("layer", AnimationLayerArgument.layer())
                                .then(argument("animation", AnimationArgument.animation())
                                        .requires(cs -> cs.hasPermission(2))
                                        .executes(context -> invite(context, false))
                                        .then(argument("withRide", BoolArgumentType.bool())
                                                .executes(context -> invite(
                                                        context, BoolArgumentType.getBool(context, "withRide")
                                                ))
                                                .then(argument("forced", BoolArgumentType.bool())
                                                        .executes(context -> invite(
                                                                context, BoolArgumentType.getBool(context, "withRide"))
                                                        )
                                                )
                                        )
                                )
                ))
                .then(literal("accept")
                        .then(argument("player", EntityArgument.player())
                                .executes(RequestAnimCommand::accept)
                        )
                )
        );
    }

    private static int invite(CommandContext<CommandSourceStack> context, boolean withRide) {
        CommandSourceStack source = context.getSource();
        try {
            //get info
            boolean force = false;
            try {
                force = BoolArgumentType.getBool(context, "force");
            } catch (Exception ignored) {}
            ServerPlayer player = source.getPlayerOrException();
            ServerPlayer target = EntityArgument.getPlayer(context, "player");

            //cooldown
            Long lastRequested = lastRequestedMap.getOrDefault(player.getUUID(), null);
            long now = System.currentTimeMillis();
            int requestCooldown = ModConfigs.Server.requestCooldown.get() * 1000;
            if(!(lastRequested == null || now - lastRequested > requestCooldown)) {
                source.sendFailure(Component.translatable(
                        ModLang.TranslatableMessage.COMMAND_COOLDOWN.getKey()
                ).withStyle(ChatFormatting.RED));
                return 0;
            }
            lastRequestedMap.put(player.getUUID(), now);

            String layerString = AnimationLayerArgument.getLayer(context, "layer");
            String animString = AnimationArgument.getAnimation(context, "animation");
            ResourceLocation layer = new ResourceLocation(layerString);
            ResourceLocation anim = new ResourceLocation(animString);

            //test info present
            boolean animationPresent = AnimationUtils.isAnimationPresent(anim);
            boolean animationLayerPresent = AnimationUtils.isAnimationLayerPresent(layer);
            if(!animationLayerPresent || !animationPresent) throw new Exception();

            //update static cache
            Map<UUID, InviteRecord> inviteRecordMap = invites.getOrDefault(player.getUUID(), new HashMap<>());
            inviteRecordMap.put(target.getUUID(), new InviteRecord(System.currentTimeMillis(), layer, anim, force, withRide));
            invites.put(player.getUUID(), inviteRecordMap);

            //click event
            Style pStyle = Style.EMPTY.withBold(true).withColor(ChatFormatting.GREEN).withClickEvent(
                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sccore anim request accept " + player.getName().getString())
            ).withUnderlined(true);

            //send message
            target.sendSystemMessage(Component.translatable(
                    ModLang.TranslatableMessage.REQUESTED_MESSAGE.getKey(),
                    player.getName().copy(),
                    anim.toString()
            ).append(Component.translatable(
                    ModLang.TranslatableMessage.ACCEPT_MESSAGE_CLICK.getKey()
            ).setStyle(pStyle)));
            source.sendSuccess(() -> Component.translatable(
                    ModLang.TranslatableMessage.REQUEST_MESSAGE.getKey()
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

    private static int accept(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        try {
            ServerPlayer target = source.getPlayerOrException();
            ServerPlayer player = EntityArgument.getPlayer(context, "player");

            //get request record and test
            Map<UUID, InviteRecord> inviteRecordMap = invites.getOrDefault(player.getUUID(), null);
            if(inviteRecordMap == null) throw new Exception();
            InviteRecord inviteRecord = inviteRecordMap.getOrDefault(target.getUUID(), null);
            if(inviteRecord == null) throw new Exception();
            long now = System.currentTimeMillis();
            Integer requestDuration = ModConfigs.Server.requestDuration.get();

            //test if expired
            if(now - inviteRecord.time > requestDuration * 1000 || requestDuration == 0) {
                source.sendFailure(Component.translatable(
                        ModLang.TranslatableMessage.ACCEPT_REQUEST_EXPIRED.getKey(),
                        requestDuration
                ).withStyle(ChatFormatting.RED));
                player.sendSystemMessage(Component.translatable(
                        ModLang.TranslatableMessage.REQUEST_EXPIRED.getKey(),
                        target.getName().copy(),
                        requestDuration
                ).withStyle(ChatFormatting.RED));
                return 0;
            }
            inviteRecordMap.remove(target.getUUID());
            invites.put(player.getUUID(), inviteRecordMap);
            boolean withRide = inviteRecord.withRide;

            //play
            if(withRide) {
                AnimationUtils.playAnimationWithRide(target, inviteRecord.layer, inviteRecord.animation, inviteRecord.isForce);
            }else {
                AnimationUtils.playAnimation(target, inviteRecord.layer, inviteRecord.animation);
            }

            //send message
            source.sendSuccess(() -> Component.translatable(
                    ModLang.TranslatableMessage.ACCEPT_REQUEST_SUCCESS.getKey()
            ).withStyle(ChatFormatting.GREEN), true);
            player.sendSystemMessage(Component.translatable(
                    ModLang.TranslatableMessage.REQUEST_SUCCESS.getKey(),
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
