package com.linearpast.sccore.animation.command;

import com.linearpast.sccore.SnowyCrescentCore;
import com.linearpast.sccore.animation.AnimationUtils;
import com.linearpast.sccore.animation.entity.AnimationRideEntity;
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
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ApplyJoinAnimCommand {
    private static final Map<UUID, Map<UUID, ApplyRecord>> applies = new HashMap<>();
    record ApplyRecord(long time, boolean isForce){}
    private static final Map<UUID, Long> lastAppliedMap = new HashMap<>();
    public static void register(LiteralArgumentBuilder<CommandSourceStack> animCommand) {
        animCommand
                .then(literal("joinApply")
                        .then(argument("target", EntityArgument.player())
                                .executes(ApplyJoinAnimCommand::tryJoinAnimation)
                                .then(argument("force", BoolArgumentType.bool())
                                        .executes(ApplyJoinAnimCommand::tryJoinAnimation)
                                )
                        )
                        .then(literal("accept")
                                .then(argument("player", EntityArgument.player())
                                        .executes(ApplyJoinAnimCommand::acceptJoinAnimation)
                                )
                        )
                );
    }

    private static int tryJoinAnimation(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        try {
            boolean force = false;
            try {force = BoolArgumentType.getBool(context, "force");}
            catch (Exception ignored) {}
            ServerPlayer target = EntityArgument.getPlayer(context, "target");
            ServerPlayer player;
            try {player = EntityArgument.getPlayer(context, "player");}
            catch (Exception e) { player = source.getPlayerOrException(); }

            Entity vehicle = target.getVehicle();
            if(!(vehicle instanceof AnimationRideEntity rideEntity) || !rideEntity.canAddPassenger(player)) {
                source.sendFailure(Component.translatable(
                        ModLang.TranslatableMessage.WITHOUT_ANIMATION_RIDE_ENTITY.getKey()
                ).withStyle(ChatFormatting.RED));
                return 0;
            }

            //cooldown
            Long lastApplied = lastAppliedMap.getOrDefault(player.getUUID(), null);
            long now = System.currentTimeMillis();
            int applyCooldown = ModConfigs.Server.applyCooldown.get() * 1000;
            if(!(lastApplied == null || now - lastApplied > applyCooldown)) {
                source.sendFailure(Component.translatable(
                        ModLang.TranslatableMessage.COMMAND_COOLDOWN.getKey()
                ).withStyle(ChatFormatting.RED));
                return 0;
            }
            lastAppliedMap.put(player.getUUID(), now);

            UUID rideEntityUUID = rideEntity.getUUID();
            Map<UUID, ApplyRecord> applyRecordMap = applies.getOrDefault(rideEntityUUID, new HashMap<>());
            applyRecordMap.put(player.getUUID(), new ApplyRecord(now, force));
            applies.put(rideEntityUUID, applyRecordMap);

            //click event
            Style pStyle = Style.EMPTY.withBold(true).withColor(ChatFormatting.GREEN).withClickEvent(
                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sccore anim joinApply accept " + player.getName().getString())
            ).withUnderlined(true);

            //send message to all participants
            for (Entity passenger : rideEntity.getPassengers()) {
                passenger.sendSystemMessage(Component.translatable(
                        ModLang.TranslatableMessage.APPLIED_JOIN_MESSAGE.getKey(),
                        player.getName().copy()
                ).append(Component.translatable(
                        ModLang.TranslatableMessage.ACCEPT_MESSAGE_CLICK.getKey()
                ).setStyle(pStyle)));
            }
            source.sendSuccess(() -> Component.translatable(
                    ModLang.TranslatableMessage.APPLY_JOIN_MESSAGE.getKey()
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

    private static int acceptJoinAnimation(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        try {
            ServerPlayer target = source.getPlayerOrException();
            ServerPlayer player = EntityArgument.getPlayer(context, "player");
            if(!(target.getVehicle() instanceof AnimationRideEntity rideEntity) || !rideEntity.canAddPassenger(player)) {
                source.sendFailure(Component.translatable(
                        ModLang.TranslatableMessage.WITHOUT_ANIMATION_RIDE_ENTITY.getKey()
                ).withStyle(ChatFormatting.RED));
                return 0;
            }

            Map<UUID, ApplyRecord> applyRecordMap = applies.getOrDefault(rideEntity.getUUID(), null);
            if(applyRecordMap == null) throw new Exception();
            ApplyRecord applyRecord = applyRecordMap.getOrDefault(player.getUUID(), null);

            //test if expired
            long now = System.currentTimeMillis();
            Integer applyDuration = ModConfigs.Server.applyDuration.get();
            if(now - applyRecord.time > applyDuration * 1000 || applyDuration == 0) {
                source.sendFailure(Component.translatable(
                        ModLang.TranslatableMessage.ACCEPT_APPLY_EXPIRED.getKey(),
                        applyDuration
                ).withStyle(ChatFormatting.RED));
                player.sendSystemMessage(Component.translatable(
                        ModLang.TranslatableMessage.APPLY_EXPIRED.getKey(),
                        target.getName().copy(),
                        applyDuration
                ).withStyle(ChatFormatting.RED));
                return 0;
            }

            //test if in range
            Integer applyDistance = ModConfigs.Server.applyDistance.get();
            if(player.position().distanceToSqr(rideEntity.position()) > applyDistance * applyDistance || applyDistance == 0) {
                source.sendFailure(Component.translatable(
                        ModLang.TranslatableMessage.ACCEPT_APPLY_TOO_FAR.getKey(),
                        applyDistance
                ).withStyle(ChatFormatting.RED));
                player.sendSystemMessage(Component.translatable(
                        ModLang.TranslatableMessage.APPLY_TOO_FAR.getKey(),
                        target.getName().copy(),
                        applyDistance
                ).withStyle(ChatFormatting.RED));
                return 0;
            }
            applyRecordMap.remove(player.getUUID());
            applies.put(player.getUUID(), applyRecordMap);

            //define message
            MutableComponent successMessage = Component.translatable(
                    ModLang.TranslatableMessage.ACCEPT_APPLY_SUCCESS.getKey(),
                    target.getName().copy(),
                    player.getName().copy()
            ).withStyle(ChatFormatting.GREEN);

            //play
            AnimationUtils.joinAnimation(player, target, applyRecord.isForce);

            //send message
            source.sendSuccess(() -> successMessage, true);
            for (Entity passenger : rideEntity.getPassengers()) {
                if(!passenger.getUUID().equals(target.getUUID()) && !passenger.getUUID().equals(player.getUUID())) {
                    passenger.sendSystemMessage(successMessage);
                }
            }
            player.sendSystemMessage(Component.translatable(
                    ModLang.TranslatableMessage.APPLY_SUCCESS.getKey(),
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
