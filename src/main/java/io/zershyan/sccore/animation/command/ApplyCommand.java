package io.zershyan.sccore.animation.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.zershyan.sccore.SnowyCrescentCore;
import io.zershyan.sccore.animation.AnimationApi;
import io.zershyan.sccore.animation.command.exception.ApiBackException;
import io.zershyan.sccore.animation.utils.ApiBack;
import io.zershyan.sccore.core.configs.ModConfigs;
import io.zershyan.sccore.core.datagen.ModLang;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ApplyCommand {
    public static void register(LiteralArgumentBuilder<CommandSourceStack> animCommand) {
        animCommand
                .then(literal("apply")
                        .then(argument("target", EntityArgument.player())
                                .executes(ApplyCommand::apply)
                        )
                        .then(literal("accept")
                                .then(argument("player", EntityArgument.player())
                                        .executes(ApplyCommand::acceptApply)
                                )
                        )
                );
    }

    private static int apply(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        try {
            ServerPlayer target = EntityArgument.getPlayer(context, "target");
            ServerPlayer player = source.getPlayerOrException();

            Entity vehicle = target.getVehicle();
            if(vehicle == null) throw new ApiBackException(ApiBack.UNSUPPORTED);

            ApiBack back = AnimationApi.getHelper(player).applyAnimation(target);
            if(back == ApiBack.COOLDOWN) {
                int cooldown = ModConfigs.Server.applyCooldown.get();
                throw ApiBackException.withCooldown(cooldown);
            }
            if(back != ApiBack.SUCCESS) throw new ApiBackException(back);

            //click event
            Style pStyle = Style.EMPTY.withBold(true).withColor(ChatFormatting.GREEN).withClickEvent(
                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sccore anim apply accept " + player.getName().getString())
            ).withUnderlined(true);

            //send message to all participants
            for (Entity passenger : vehicle.getPassengers()) {
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

    private static int acceptApply(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        try {
            ServerPlayer player = source.getPlayerOrException();
            ServerPlayer applier = EntityArgument.getPlayer(context, "applier");

            Entity vehicle = player.getVehicle();
            if(vehicle == null) throw new ApiBackException(ApiBack.UNSUPPORTED);

            ApiBack back = AnimationApi.getHelper(player).acceptApply(applier);
            if(back == ApiBack.OUT_RANGE) throw ApiBackException.withOutRange(ModConfigs.Server.applyValidDistance.get());
            if(back != ApiBack.SUCCESS) throw new ApiBackException(back);

            //define message
            MutableComponent successMessage = Component.translatable(
                    ModLang.TranslatableMessage.ACCEPT_APPLY_SUCCESS.getKey(),
                    player.getName().copy(), applier.getName().copy()
            ).withStyle(ChatFormatting.GREEN);

            //send message
            source.sendSuccess(() -> successMessage, true);
            for (Entity passenger : vehicle.getPassengers()) {
                if(!passenger.getUUID().equals(player.getUUID()) && !passenger.getUUID().equals(applier.getUUID())) {
                    passenger.sendSystemMessage(successMessage);
                }
            }
            applier.sendSystemMessage(Component.translatable(
                    ModLang.TranslatableMessage.APPLY_SUCCESS.getKey(),
                    player.getName().copy()
            ).withStyle(ChatFormatting.GREEN));
        } catch (ApiBackException e) {
            source.sendFailure(e.getCommandFailBack().withStyle(ChatFormatting.RED));
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
