package com.linearpast.sccore.example.animation.event;

import com.linearpast.sccore.animation.AnimationUtils;
import com.linearpast.sccore.animation.command.argument.AnimationArgument;
import com.linearpast.sccore.animation.command.argument.AnimationLayerArgument;
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
import net.minecraftforge.event.RegisterCommandsEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ExampleCommandEvent {
    record InviteRecord(long time, ResourceLocation layer, ResourceLocation animation, boolean isForce){}
    private static final Map<UUID, Map<UUID, InviteRecord>> invites = new HashMap<>();
    public static void inviteDance(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> builder = literal("dance").then(literal("invite")
                .then(argument("player", EntityArgument.player())
                        .then(argument("layer", AnimationLayerArgument.layer())
                                .then(argument("anim", AnimationArgument.animation())
                                        .executes(ExampleCommandEvent::inviteDance)
                                        .then(argument("force", BoolArgumentType.bool())
                                                .executes(ExampleCommandEvent::inviteDance)
                                        )
                                )
                        )
                )
                .then(literal("accept")
                        .then(argument("player", EntityArgument.player())
                                .executes(ExampleCommandEvent::acceptInvite)
                        )
                )
        );
        event.getDispatcher().register(builder);
    }

    private static int inviteDance(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        try {
            //get info
            boolean force = false;
            try {
                force = BoolArgumentType.getBool(context, "force");
            } catch (Exception ignored) {}
            ServerPlayer player = source.getPlayerOrException();
            ServerPlayer target = EntityArgument.getPlayer(context, "player");
            String layerString = AnimationLayerArgument.getLayer(context, "layer");
            String animString = AnimationArgument.getAnimation(context, "anim");
            ResourceLocation layer = new ResourceLocation(layerString);
            ResourceLocation anim = new ResourceLocation(animString);
            boolean finalForce = force;

            //test info present
            boolean animationPresent = AnimationUtils.isAnimationPresent(anim);
            boolean animationLayerPresent = AnimationUtils.isAnimationLayerPresent(layer);
            if(!animationLayerPresent || !animationPresent) throw new Exception();

            //update static cache
            Map<UUID, InviteRecord> inviteRecordMap = invites.getOrDefault(player.getUUID(), new HashMap<>());
            inviteRecordMap.put(target.getUUID(), new InviteRecord(System.currentTimeMillis(), layer, anim, finalForce));
            invites.put(player.getUUID(), inviteRecordMap);

            //send message
            Component name = player.getName();
            Style pStyle = Style.EMPTY.withBold(true).withColor(ChatFormatting.GREEN).withClickEvent(
                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/dance invite accept " + player.getName().getString())
            );
            target.sendSystemMessage(name.copy().append("邀请你跳一支舞。").append(Component.literal("单击此处同意.").setStyle(pStyle)));
            source.sendSuccess(() -> Component.literal("命令执行成功. 已发送邀请").withStyle(ChatFormatting.GREEN), true);
        } catch (Exception e) {
            source.sendFailure(Component.literal("Command run fail.").withStyle(ChatFormatting.RED));
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
            long now = System.currentTimeMillis();
            if(now - inviteRecord.time > 120000) {
                source.sendFailure(Component.literal("邀请已超时（2分钟）.").withStyle(ChatFormatting.RED));
                player.sendSystemMessage(target.getName().copy().append("接受了你的舞蹈邀请. 但是邀请超时了（2分钟）.").withStyle(ChatFormatting.RED));
                return 0;
            }
            if(player.position().distanceToSqr(target.position()) > 64) {
                source.sendFailure(Component.literal("你们距离太远了（8格）.").withStyle(ChatFormatting.RED));
                player.sendSystemMessage(target.getName().copy().append("接受了你的舞蹈邀请. 但你们距离太远了（8格）.").withStyle(ChatFormatting.RED));
                return 0;
            }
            inviteRecordMap.remove(target.getUUID());
            invites.put(player.getUUID(), inviteRecordMap);
            AnimationUtils.startAnimationTogether(player, inviteRecord.layer, inviteRecord.animation, inviteRecord.isForce, target);
            source.sendSuccess(() -> Component.literal("已接受邀请.").withStyle(ChatFormatting.GREEN), true);
            player.sendSystemMessage(target.getName().copy().append("已接受你的舞蹈邀请.").withStyle(ChatFormatting.GREEN));
        } catch (Exception e) {
            source.sendFailure(Component.literal("Command run fail.").withStyle(ChatFormatting.RED));
            return 0;
        }
        return 1;
    }
}
