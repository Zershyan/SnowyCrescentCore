package com.linearpast.sccore.animation.command;

import com.linearpast.sccore.animation.AnimationUtils;
import com.linearpast.sccore.animation.command.argument.AnimationArgument;
import com.linearpast.sccore.animation.command.argument.AnimationLayerArgument;
import com.linearpast.sccore.animation.entity.AnimationRideEntity;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class PlayAnimationCommand {
    public static void register(LiteralArgumentBuilder<CommandSourceStack> animCommand){
        animCommand.then(literal("play").then(argument("players", EntityArgument.players()).then(
                argument("layer", AnimationLayerArgument.layer())
                        .then(argument("animation", AnimationArgument.animation())
                                .requires(cs -> cs.hasPermission(2))
                                .executes(context -> playAnimation(context, false))
                                .then(argument("withRide", BoolArgumentType.bool())
                                        .executes(context -> playAnimation(
                                                context, BoolArgumentType.getBool(context, "withRide")
                                        ))
                                        .then(argument("forced", BoolArgumentType.bool())
                                                .executes(context -> playAnimation(
                                                        context, BoolArgumentType.getBool(context, "withRide"))
                                                )
                                        )
                                )
                        )
        )));
        animCommand.then(literal("remove")
                .requires(cs -> cs.hasPermission(2))
                .executes(PlayAnimationCommand::clearAnimation)
                .then(argument("players", EntityArgument.players())
                        .requires(cs -> cs.hasPermission(2))
                        .executes(PlayAnimationCommand::clearAnimation)
                        .then(argument("layer", AnimationLayerArgument.layer())
                                .executes(PlayAnimationCommand::removeAnimation)
                        )
                )
        );
    }

    private static int playAnimation(CommandContext<CommandSourceStack> ctx, boolean withRide) {
        CommandSourceStack source = ctx.getSource();
        try {
            Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "players");
            String animation = AnimationArgument.getAnimation(ctx, "animation").replace("$", ":");
            String layer = AnimationLayerArgument.getLayer(ctx, "layer").replace("$", ":");
            ResourceLocation layerLocation = new ResourceLocation(layer);
            ResourceLocation animLocation = new ResourceLocation(animation);
            boolean animationPresent = AnimationUtils.isAnimationPresent(animLocation);
            boolean layerPresent = AnimationUtils.isAnimationLayerPresent(layerLocation);
            if(!animationPresent) {
                source.sendFailure(Component.literal("Animation is not present.").withStyle(ChatFormatting.RED));
                return 0;
            }
            if(!layerPresent) {
                source.sendFailure(Component.literal("Layer is not present.").withStyle(ChatFormatting.RED));
                return 0;
            }
            Set<ServerPlayer> playerSet = Set.copyOf(players);
            playerSet.forEach(player -> {
                if(withRide) {
                    boolean forced = false;
                    try { forced = BoolArgumentType.getBool(ctx, "forced");}
                    catch (Exception ignored) {}
                    if(AnimationUtils.playAnimationWithRide(player, layerLocation, animLocation, forced)) {
                        players.remove(player);
                    }
                } else {
                    if (AnimationUtils.playAnimation(player, layerLocation, animLocation)) {
                        players.remove(player);
                    }
                }
            });
            MutableComponent fail = Component.literal("Fail to play animation with: ");
            int successNum = playerSet.size() - players.size();
            if(successNum > 0) {
                MutableComponent success = Component.literal("Success to play animation with ")
                        .append(successNum + "")
                        .append(" player");
                if(successNum > 1) success.append("s.");
                else success.append(".");
                source.sendSuccess(() -> success.withStyle(ChatFormatting.GREEN), true);
            }
            List<ServerPlayer> list = players.stream().toList();
            if(!list.isEmpty()) {
                for (int i = 0; i < list.size(); i++) {
                    fail.append(list.get(i).getName());
                    if (i < list.size() - 1) {
                        fail.append(", ");
                    }
                }
                fail.append(".");
                source.sendFailure(fail.withStyle(ChatFormatting.RED));
            }
        } catch (Exception e) {
            source.sendFailure(Component.literal("Run command failure.").withStyle(ChatFormatting.RED));
            return 0;
        }
        return 1;
    }

    private static int removeAnimation(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        try {
            Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "players");
            String layer = AnimationLayerArgument.getLayer(ctx, "layer").replace("$", ":");
            ResourceLocation layerLocation = new ResourceLocation(layer);
            boolean layerPresent = AnimationUtils.isAnimationLayerPresent(layerLocation);
            if(!layerPresent) {
                source.sendFailure(Component.literal("Layer is not present.").withStyle(ChatFormatting.RED));
                return 0;
            }
            Set<ServerPlayer> playerSet = Set.copyOf(players);
            playerSet.forEach(player -> {
                if(player.getVehicle() instanceof AnimationRideEntity rideEntity && rideEntity.getLayer().equals(layerLocation)) {
                    player.unRide();
                    players.remove(player);
                }
                if (AnimationUtils.removeAnimation(player, layerLocation)) {
                    players.remove(player);
                }
            });
            MutableComponent fail = Component.literal("Fail to remove animation with: ");
            int successNum = playerSet.size() - players.size();
            if(successNum > 0) {
                MutableComponent success = Component.literal("Success to remove animation with ")
                        .append(successNum + "")
                        .append(" player");
                if(successNum > 1) success.append("s.");
                else success.append(".");
                source.sendSuccess(() -> success.withStyle(ChatFormatting.GREEN), true);
            }
            List<ServerPlayer> list = players.stream().toList();
            if(!list.isEmpty()) {
                for (int i = 0; i < list.size(); i++) {
                    fail.append(list.get(i).getName());
                    if (i < list.size() - 1) {
                        fail.append(", ");
                    }
                }
                fail.append(".");
                source.sendFailure(fail.withStyle(ChatFormatting.RED));
            }
        } catch (Exception e) {
            source.sendFailure(Component.literal("Run command failure.").withStyle(ChatFormatting.RED));
            return 0;
        }
        return 1;
    }

    private static int clearAnimation(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        try {
            Collection<ServerPlayer> players;
            try {players = EntityArgument.getPlayers(ctx, "players");}
            catch (Exception ignored) { players = Set.of(source.getPlayerOrException()); }
            Set.copyOf(players).forEach(player -> {
                if(player.getVehicle() instanceof AnimationRideEntity) {
                    player.unRide();
                }
                AnimationUtils.clearAnimation(player);
            });
            source.sendSuccess(() -> Component.literal("Animation cleared.").withStyle(ChatFormatting.GREEN), true);
        } catch (Exception e) {
            source.sendFailure(Component.literal("Run command failure.").withStyle(ChatFormatting.RED));
            return 0;
        }
        return 1;
    }
}
