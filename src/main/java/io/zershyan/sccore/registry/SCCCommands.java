package io.zershyan.sccore.registry;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.zershyan.sccore.SCCore;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashSet;
import java.util.Set;

import static net.minecraft.commands.Commands.literal;

public class SCCCommands {
    static final Set<String> animationCommand = new HashSet<>(Set.of(SCCore.MODID, "sc", "scc"));

    public static void addCommandAlias(String alias) {
        animationCommand.add(alias);
    }

    public static Set<String> getAnimationCommand() {
        return animationCommand;
    }

    public static void register(IEventBus forgeBus, IEventBus modBus) {
        forgeBus.addListener(SCCCommands::commonCommandRegister);
        forgeBus.addListener(SCCCommands::clientCommandRegister);
        Arguments.register(modBus);
    }


    public static void commonCommandRegister(RegisterCommandsEvent event) {
        animationCommand.forEach(string -> {
            LiteralArgumentBuilder<CommandSourceStack> builder = literal(string);
            CommandBuildContext buildContext = event.getBuildContext();

            event.getDispatcher().register(builder);
        });
    }

    public static void clientCommandRegister(RegisterClientCommandsEvent event) {
        animationCommand.forEach(string -> {
            LiteralArgumentBuilder<CommandSourceStack> builder = literal(string);

            event.getDispatcher().register(builder);
        });
    }

    public static class Arguments {
        public static final DeferredRegister<ArgumentTypeInfo<?, ?>> REGISTRY = DeferredRegister.create(
                Registries.COMMAND_ARGUMENT_TYPE, SCCore.MODID
        );

        public static void register(IEventBus eventBus) {

            REGISTRY.register(eventBus);
        }
    }
}
