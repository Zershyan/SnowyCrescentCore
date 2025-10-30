package com.linearpast.sccore.animation.command;

import com.linearpast.sccore.SnowyCrescentCore;
import com.linearpast.sccore.animation.AnimationUtils;
import com.linearpast.sccore.animation.command.argument.AnimationArgument;
import com.linearpast.sccore.animation.command.argument.AnimationLayerArgument;
import com.linearpast.sccore.animation.entity.AnimationRideEntity;
import com.linearpast.sccore.core.datagen.ModLang;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
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

public class PlayAnimCommand {
    public static void register(LiteralArgumentBuilder<CommandSourceStack> animCommand){
        RequiredArgumentBuilder<CommandSourceStack, String> animCommandParam = argument("layer", AnimationLayerArgument.layer())
                .then(argument("animation", AnimationArgument.animation())
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
                );
        animCommand
                .then(literal("playSelf")
                        .then(animCommandParam))
                .then(literal("play")
                        .then(argument("players", EntityArgument.players())
                        .requires(cs -> cs.hasPermission(2))
                        .then(animCommandParam)
                ))
                .then(literal("remove")
                        .executes(PlayAnimCommand::clearAnimation)
                        .then(argument("players", EntityArgument.players())
                                .requires(cs -> cs.hasPermission(2))
                                .executes(PlayAnimCommand::clearAnimation)
                                .then(argument("layer", AnimationLayerArgument.layer())
                                        .executes(PlayAnimCommand::removeAnimation)
                                )
                        )
                        .then(argument("layer", AnimationLayerArgument.layer())
                                .executes(PlayAnimCommand::removeAnimation)
                        )
                );
    }

    private static int playAnimation(CommandContext<CommandSourceStack> context, boolean withRide) {
        CommandSourceStack source = context.getSource();
        try {
            Collection<ServerPlayer> players = null;
            ServerPlayer player = null;
            try {players = EntityArgument.getPlayers(context, "players");}
            catch (Exception ignored) { player = source.getPlayerOrException(); }
            String animation = AnimationArgument.getAnimation(context, "animation");
            String layer = AnimationLayerArgument.getLayer(context, "layer");
            ResourceLocation layerLocation = new ResourceLocation(layer);
            ResourceLocation animLocation = new ResourceLocation(animation);
            boolean animationPresent = AnimationUtils.isAnimationPresent(animLocation);
            boolean layerPresent = AnimationUtils.isAnimationLayerPresent(layerLocation);
            if(!animationPresent) {
                source.sendFailure(Component.translatable(
                        ModLang.TranslatableMessage.ANIMATION_NOT_PRESENT.getKey()
                ).withStyle(ChatFormatting.RED));
                return 0;
            }
            if(!layerPresent) {
                source.sendFailure(Component.literal(
                        ModLang.TranslatableMessage.ANIMATION_LAYER_NOT_PRESENT.getKey()
                ).withStyle(ChatFormatting.RED));
                return 0;
            }

            //play with players
            if(players != null) {
                Set<ServerPlayer> playerSet = Set.copyOf(players);
                Collection<ServerPlayer> finalPlayers = players;
                playerSet.forEach(p -> {
                    if(withRide) {
                        boolean forced = false;
                        try { forced = BoolArgumentType.getBool(context, "forced");}
                        catch (Exception ignored) {}
                        if(AnimationUtils.playAnimationWithRide(p, layerLocation, animLocation, forced)) {
                            finalPlayers.remove(p);
                        }
                    } else {
                        if (AnimationUtils.playAnimation(p, layerLocation, animLocation)) {
                            finalPlayers.remove(p);
                        }
                    }
                });

                int successNum = playerSet.size() - players.size();
                if(successNum > 0) {
                    source.sendSuccess(() -> Component.translatable(
                            ModLang.TranslatableMessage.PLAY_ANIMATION_SUCCESS.getKey(),
                            successNum
                    ).withStyle(ChatFormatting.GREEN), true);
                }
                List<ServerPlayer> list = players.stream().toList();
                if(!list.isEmpty()) {
                    MutableComponent failPlayers = Component.literal("");
                    for (int i = 0; i < list.size(); i++) {
                        failPlayers.append(list.get(i).getName().copy());
                        if (i < list.size() - 1) {
                            failPlayers.append(", ");
                        }
                    }
                    failPlayers.append(".");
                    source.sendFailure(Component.translatable(
                            ModLang.TranslatableMessage.PLAY_ANIMATION_FAIL.getKey(),
                            failPlayers
                    ).withStyle(ChatFormatting.RED));
                }
            }

            //play with self
            if(player != null) {
                if(withRide) {
                    boolean forced = false;
                    try { forced = BoolArgumentType.getBool(context, "forced");}
                    catch (Exception ignored) {}
                    AnimationUtils.playAnimationWithRide(player, layerLocation, animLocation, forced);
                } else {
                    AnimationUtils.playAnimation(player, layerLocation, animLocation);
                }
                source.sendSuccess(() -> Component.translatable(
                        ModLang.TranslatableMessage.COMMAND_RUN_SUCCESS.getKey()
                ).withStyle(ChatFormatting.GREEN), true);
            }
        } catch (Exception e) {
            source.sendFailure(Component.translatable(
                    ModLang.TranslatableMessage.COMMAND_RUN_FAIL.getKey()
            ).withStyle(ChatFormatting.RED));
            SnowyCrescentCore.log.error(e.getMessage());
            return 0;
        }
        return 1;
    }

    private static int removeAnimation(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        try {
            Collection<ServerPlayer> players = null;
            ServerPlayer player = null;
            try {players = EntityArgument.getPlayers(context, "players");}
            catch (Exception ignored) { player = source.getPlayerOrException(); }
            String layer = AnimationLayerArgument.getLayer(context, "layer");
            ResourceLocation layerLocation = new ResourceLocation(layer);
            boolean layerPresent = AnimationUtils.isAnimationLayerPresent(layerLocation);
            if(!layerPresent) {
                source.sendFailure(Component.literal(
                        ModLang.TranslatableMessage.ANIMATION_LAYER_NOT_PRESENT.getKey()
                ).withStyle(ChatFormatting.RED));
                return 0;
            }

            //remove with players
            if(players != null) {
                Set<ServerPlayer> playerSet = Set.copyOf(players);
                Collection<ServerPlayer> finalPlayers = players;
                playerSet.forEach(p -> {
                    if(p.getVehicle() instanceof AnimationRideEntity rideEntity && rideEntity.getLayer().equals(layerLocation)) {
                        p.unRide();
                        finalPlayers.remove(p);
                    }
                    if (AnimationUtils.removeAnimation(p, layerLocation)) {
                        finalPlayers.remove(p);
                    }
                });
                int successNum = playerSet.size() - players.size();
                if(successNum > 0) {
                    source.sendSuccess(() -> Component.translatable(
                            ModLang.TranslatableMessage.REMOVE_ANIMATION_SUCCESS.getKey(),
                            successNum
                    ).withStyle(ChatFormatting.GREEN), true);
                }
                List<ServerPlayer> list = players.stream().toList();
                if(!list.isEmpty()) {
                    MutableComponent failPlayers = Component.literal("");
                    for (int i = 0; i < list.size(); i++) {
                        failPlayers.append(list.get(i).getName().copy());
                        if (i < list.size() - 1) {
                            failPlayers.append(", ");
                        }
                    }
                    failPlayers.append(".");
                    source.sendFailure(Component.translatable(
                            ModLang.TranslatableMessage.REMOVE_ANIMATION_FAIL.getKey(),
                            failPlayers
                    ).withStyle(ChatFormatting.RED));
                }
            }

            //remove with self
            if(player != null) {
                if(player.getVehicle() instanceof AnimationRideEntity rideEntity && rideEntity.getLayer().equals(layerLocation)) {
                    player.unRide();
                }
                AnimationUtils.removeAnimation(player, layerLocation);
                source.sendSuccess(() -> Component.translatable(
                        ModLang.TranslatableMessage.COMMAND_RUN_SUCCESS.getKey()
                ).withStyle(ChatFormatting.GREEN), true);
            }
        } catch (Exception e) {
            source.sendFailure(Component.translatable(
                    ModLang.TranslatableMessage.COMMAND_RUN_FAIL.getKey()
            ).withStyle(ChatFormatting.RED));
            SnowyCrescentCore.log.error(e.getMessage());
            return 0;
        }
        return 1;
    }

    private static int clearAnimation(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        try {
            Collection<ServerPlayer> players;
            try {players = EntityArgument.getPlayers(context, "players");}
            catch (Exception ignored) { players = Set.of(source.getPlayerOrException()); }
            Set.copyOf(players).forEach(player -> {
                if(player.getVehicle() instanceof AnimationRideEntity) {
                    player.unRide();
                }
                AnimationUtils.clearAnimation(player);
            });
            source.sendSuccess(() -> Component.translatable(
                    ModLang.TranslatableMessage.CLEAR_ANIMATIONS.getKey()
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
