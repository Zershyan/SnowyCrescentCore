package com.linearpast.sccore.core;

import com.linearpast.sccore.SnowyCrescentCore;
import com.linearpast.sccore.animation.command.AnimationCommands;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.Set;

import static net.minecraft.commands.Commands.literal;

public class ModCommands {
    static final Set<String> animationCommand = new HashSet<>(Set.of(SnowyCrescentCore.MODID, "sc", "scc"));
    public static void addCommandAlias(String alias) {
        animationCommand.add(alias);
    }
    public static Set<String> getAnimationCommand() {
        return animationCommand;
    }

    public static void registerCommands(IEventBus forgeBus, IEventBus modBus) {
        forgeBus.addListener(ModCommands::commonCommandRegister);
        forgeBus.addListener(ModCommands::clientCommandRegister);
        Arguments.register(modBus);
    }


    public static void commonCommandRegister(RegisterCommandsEvent event) {
        animationCommand.forEach(string -> {
            LiteralArgumentBuilder<CommandSourceStack> builder = literal(string);
            CommandBuildContext buildContext = event.getBuildContext();
            AnimationCommands.commonCommandRegister(builder);

            event.getDispatcher().register(builder);
        });
    }

    public static void clientCommandRegister(RegisterClientCommandsEvent event) {
        animationCommand.forEach(string -> {
            LiteralArgumentBuilder<CommandSourceStack> builder = literal(string);
            AnimationCommands.clientCommandRegister(builder);

            event.getDispatcher().register(builder);
        });
    }

    public static class Arguments {
        public static final DeferredRegister<ArgumentTypeInfo<?, ?>> REGISTRY = DeferredRegister.create(
                ForgeRegistries.Keys.COMMAND_ARGUMENT_TYPES, SnowyCrescentCore.MODID
        );
        public static void register(IEventBus eventBus) {
            AnimationCommands.registerArguments(REGISTRY);
            REGISTRY.register(eventBus);
        }
    }
}
