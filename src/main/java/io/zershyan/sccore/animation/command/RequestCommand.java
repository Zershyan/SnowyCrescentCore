package io.zershyan.sccore.animation.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.zershyan.sccore.SnowyCrescentCore;
import io.zershyan.sccore.animation.AnimationApi;
import io.zershyan.sccore.animation.command.argument.AnimationArgument;
import io.zershyan.sccore.animation.command.argument.AnimationLayerArgument;
import io.zershyan.sccore.animation.command.exception.ApiBackException;
import io.zershyan.sccore.animation.utils.ApiBack;
import io.zershyan.sccore.core.configs.ModConfigs;
import io.zershyan.sccore.core.datagen.ModLang;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

/**
 * Request target player play animation.
 */
public class RequestCommand {
    public static void register(LiteralArgumentBuilder<CommandSourceStack> animCommand) {
        animCommand.then(literal("request")
                .then(argument("player", EntityArgument.player()).then(
                        argument("layer", AnimationLayerArgument.layer())
                                .then(argument("animation", AnimationArgument.animation())
                                        .requires(cs -> cs.hasPermission(2))
                                        .executes(context -> request(context, false))
                                        .then(argument("withRide", BoolArgumentType.bool())
                                                .executes(context -> request(
                                                        context, BoolArgumentType.getBool(context, "withRide")
                                                ))
                                        )
                                )
                ))
                .then(literal("acceptRequest")
                        .then(argument("player", EntityArgument.player())
                                .executes(RequestCommand::acceptRequest)
                        )
                )
        );
    }

    private static int request(CommandContext<CommandSourceStack> context, boolean withRide) {
        CommandSourceStack source = context.getSource();
        try {
            ServerPlayer player = source.getPlayerOrException();
            ServerPlayer target = EntityArgument.getPlayer(context, "player");
            String layerString = AnimationLayerArgument.getLayer(context, "layer");
            String animString = AnimationArgument.getAnimation(context, "animation");
            ResourceLocation layer = new ResourceLocation(layerString);
            ResourceLocation anim = new ResourceLocation(animString);

            ApiBack back = AnimationApi.getHelper(player).requestAnimation(target, layer, anim, withRide);
            if(back == ApiBack.COOLDOWN) {
                int cooldown = ModConfigs.Server.requestCooldown.get();
                throw ApiBackException.withCooldown(cooldown);
            }
            if(back != ApiBack.SUCCESS) throw new ApiBackException(back);

            //click event
            Style pStyle = Style.EMPTY.withBold(true).withColor(ChatFormatting.GREEN).withClickEvent(
                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sccore anim request acceptRequest " + player.getName().getString())
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

    private static int acceptRequest(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        try {
            ServerPlayer player = source.getPlayerOrException();
            ServerPlayer requestor = EntityArgument.getPlayer(context, "requestor");

            //play
            ApiBack back = AnimationApi.getHelper(player).acceptRequest(requestor);
            if(back != ApiBack.SUCCESS) throw new ApiBackException(back);

            //send message
            source.sendSuccess(() -> Component.translatable(
                    ModLang.TranslatableMessage.ACCEPT_REQUEST_SUCCESS.getKey()
            ).withStyle(ChatFormatting.GREEN), true);
            requestor.sendSystemMessage(Component.translatable(
                    ModLang.TranslatableMessage.REQUEST_SUCCESS.getKey(),
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
