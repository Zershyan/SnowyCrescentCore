package io.zershyan.sccore.registry;

import io.zershyan.sccore.SCCore;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashSet;
import java.util.Set;

public class SCCCommands {
    private static final Set<String> animationCommand = new HashSet<>(Set.of(SCCore.MODID, "sc", "scc"));

    /**
     * 追加动画命令别名，需在命令注册事件之前调用才会生效。
     *
     * @param alias 新的命令别名
     */
    public static void addCommandAlias(String alias) {
        animationCommand.add(alias);
    }

    /**
     * 获取全部动画命令别名。
     *
     * @return 命令别名的快照
     */
    public static Set<String> getAnimationCommand() {
        return Set.copyOf(animationCommand);
    }

    public static void register(IEventBus modBus) {
        Arguments.register(modBus);
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
