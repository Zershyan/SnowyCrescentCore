package com.linearpast.sccore.animation.command;

import com.linearpast.sccore.SnowyCrescentCore;
import com.linearpast.sccore.animation.AnimationApi;
import com.linearpast.sccore.animation.command.argument.AnimationArgument;
import com.linearpast.sccore.animation.command.argument.AnimationLayerArgument;
import com.linearpast.sccore.animation.command.exception.ApiBackException;
import com.linearpast.sccore.animation.data.AnimationData;
import com.linearpast.sccore.animation.helper.AnimationHelper;
import com.linearpast.sccore.animation.helper.AnimationServiceGetterHelper;
import com.linearpast.sccore.animation.network.toclient.AnimationClearPacket;
import com.linearpast.sccore.animation.service.IAnimationService;
import com.linearpast.sccore.animation.service.RawAnimationService;
import com.linearpast.sccore.animation.utils.ApiBack;
import com.linearpast.sccore.core.ModChannel;
import com.linearpast.sccore.core.datagen.ModLang;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class PlayCommand {
    public static void register(LiteralArgumentBuilder<CommandSourceStack> animCommand){
        animCommand
                .then(literal("play")
                        .then(argument("players", EntityArgument.players())
                                .requires(cs -> cs.hasPermission(2))
                                .then(argument("layer", AnimationLayerArgument.layer())
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
                                        )
                                )
                        )
                        .then(literal("self")
                                .then(argument("layer", AnimationLayerArgument.layer())
                                        .then(argument("animation", AnimationArgument.animation())
                                                .executes(context -> playAnimation(context, false))
                                                .then(argument("withRide", BoolArgumentType.bool())
                                                        .executes(context -> playAnimation(
                                                                context, BoolArgumentType.getBool(context, "withRide")
                                                        ))
                                                )
                                        )
                                )
                        )
                )
                .then(literal("clear")
                        .executes(PlayCommand::clearAnimation)
                        .then(argument("players", EntityArgument.players())
                                .requires(cs -> cs.hasPermission(2))
                                .executes(PlayCommand::clearAnimation)
                                .then(argument("layer", AnimationLayerArgument.layer())
                                        .executes(PlayCommand::removeAnimation)
                                )
                        )
                        .then(argument("layer", AnimationLayerArgument.layer())
                                .executes(PlayCommand::removeAnimation)
                        )
                );
    }

    private static int playAnimation(CommandContext<CommandSourceStack> context, boolean withRide) {
        CommandSourceStack source = context.getSource();
        try {
            Collection<ServerPlayer> targets = new ArrayList<>();
            ServerPlayer player = source.getPlayerOrException();
            try { targets.addAll(EntityArgument.getPlayers(context, "players"));}
            catch (Exception ignored) {}
            String layerString = AnimationLayerArgument.getLayer(context, "layer");
            String animString = AnimationArgument.getAnimation(context, "animation");
            ResourceLocation layer = new ResourceLocation(layerString);
            ResourceLocation anim = new ResourceLocation(animString);

            //play with players
            IAnimationService<?, ?> helper = AnimationServiceGetterHelper.create(anim).getService();
            if (helper == null) throw new ApiBackException(ApiBack.RESOURCE_NOT_FOUND);
            AnimationData animationData = helper.getAnimation(anim);
            if(animationData == null) throw new ApiBackException(ApiBack.RESOURCE_NOT_FOUND);
            if(!targets.isEmpty()) {
                Set<ServerPlayer> playerSet = Set.copyOf(targets);
                playerSet.forEach(p -> {
                    if(withRide) {
                        boolean forced = false;
                        try { forced = BoolArgumentType.getBool(context, "forced");}
                        catch (Exception ignored) {}
                        ApiBack back = helper.playAnimationWithRide(p, layer, animationData, forced);
                        if(back == ApiBack.SUCCESS) targets.remove(p);
                    } else {
                        ApiBack back = helper.playAnimation(p, layer, animationData);
                        if (back == ApiBack.SUCCESS) targets.remove(p);
                    }
                });

                int successNum = playerSet.size() - targets.size();
                if(successNum > 0) {
                    source.sendSuccess(() -> Component.translatable(
                            ModLang.TranslatableMessage.PLAY_ANIMATION_SUCCESS.getKey(),
                            successNum
                    ).withStyle(ChatFormatting.GREEN), true);
                }
                List<ServerPlayer> list = targets.stream().toList();
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
            } else {
                //play with self
                ApiBack back;
                RawAnimationService instance = RawAnimationService.INSTANCE;
                if(withRide) back = instance.playAnimationWithRide(player, layer, animationData, false);
                else back = instance.playAnimation(player, layer, animationData);

                if(back != ApiBack.SUCCESS) throw new ApiBackException(back);

                source.sendSuccess(() -> Component.translatable(
                        ModLang.TranslatableMessage.COMMAND_RUN_SUCCESS.getKey()
                ).withStyle(ChatFormatting.GREEN), true);
            }
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.translatable(
                    ModLang.TranslatableMessage.COMMAND_RUN_FAIL.getKey()
            ).withStyle(ChatFormatting.RED));
            SnowyCrescentCore.log.error(e.getMessage());

        }
        return 0;
    }

    private static int removeAnimation(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        try {
            Collection<ServerPlayer> targets = new ArrayList<>();
            ServerPlayer player = source.getPlayerOrException();
            try { targets.addAll(EntityArgument.getPlayers(context, "players"));}
            catch (Exception ignored) {}
            String layer = AnimationLayerArgument.getLayer(context, "layer");
            ResourceLocation layerLocation = new ResourceLocation(layer);

            //remove with players
            if(!targets.isEmpty()) {
                Set<ServerPlayer> playerSet = Set.copyOf(targets);
                playerSet.forEach(p -> {
                    ApiBack back = AnimationApi.getHelper(p).removeAnimation(layerLocation);
                    if (back == ApiBack.SUCCESS) targets.remove(p);
                });
                int successNum = playerSet.size() - targets.size();
                if(successNum > 0) {
                    source.sendSuccess(() -> Component.translatable(
                            ModLang.TranslatableMessage.REMOVE_ANIMATION_SUCCESS.getKey(),
                            successNum
                    ).withStyle(ChatFormatting.GREEN), true);
                }
                List<ServerPlayer> list = targets.stream().toList();
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
            } else {
                ApiBack back = AnimationApi.getHelper(player).removeAnimation(layerLocation);
                if (back != ApiBack.SUCCESS) throw new ApiBackException(back);

                source.sendSuccess(() -> Component.translatable(
                        ModLang.TranslatableMessage.COMMAND_RUN_SUCCESS.getKey()
                ).withStyle(ChatFormatting.GREEN), true);
            }
            return 1;
        } catch (ApiBackException e){
            source.sendFailure(e.getCommandFailBack().withStyle(ChatFormatting.RED));
        } catch (Exception e) {
            source.sendFailure(Component.translatable(
                    ModLang.TranslatableMessage.COMMAND_RUN_FAIL.getKey()
            ).withStyle(ChatFormatting.RED));
            SnowyCrescentCore.log.error(e.getMessage());
        }
        return 0;
    }

    private static int clearAnimation(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        try {
            Collection<ServerPlayer> players;
            try {players = EntityArgument.getPlayers(context, "players");}
            catch (Exception ignored) { players = Set.of(source.getPlayerOrException()); }
            Set.copyOf(players).forEach(player -> {
                AnimationHelper helper = AnimationApi.getHelper(player);
                helper.clearAnimation();
                helper.detachAnimation();
                ModChannel.sendToPlayer(new AnimationClearPacket((ResourceLocation) null), player);
            });
            source.sendSuccess(() -> Component.translatable(
                    ModLang.TranslatableMessage.CLEAR_ANIMATIONS.getKey()
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
