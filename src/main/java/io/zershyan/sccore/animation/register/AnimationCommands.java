package io.zershyan.sccore.animation.register;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.zershyan.sccore.animation.command.*;
import io.zershyan.sccore.animation.command.argument.AnimationArgument;
import io.zershyan.sccore.animation.command.argument.AnimationLayerArgument;
import io.zershyan.sccore.animation.command.client.ListClientCommand;
import io.zershyan.sccore.animation.command.client.RefreshCommand;
import io.zershyan.sccore.animation.service.IAnimationService;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraftforge.registries.DeferredRegister;

import static net.minecraft.commands.Commands.literal;

public class AnimationCommands {
	public static void commonCommandRegister(LiteralArgumentBuilder<CommandSourceStack> builder) {
		if(IAnimationService.ANIMATION_RUNNER.testCondition()){
			LiteralArgumentBuilder<CommandSourceStack> anim = literal("anim");
			ApplyCommand.register(anim);
			InviteCommand.register(anim);
			JsonCommand.register(anim);
			ListServerCommand.register(anim);
			PlayCommand.register(anim);
			RequestCommand.register(anim);
			ReloadCommand.register(anim);
			builder.then(anim);
		}
	}

	public static void clientCommandRegister(LiteralArgumentBuilder<CommandSourceStack> builder) {
		if(IAnimationService.ANIMATION_RUNNER.testCondition()) {
			LiteralArgumentBuilder<CommandSourceStack> anim = literal("anim");
			ListClientCommand.register(anim);
			RefreshCommand.register(anim);
			builder.then(anim);
		}
	}

	public static void registerArguments(DeferredRegister<ArgumentTypeInfo<?, ?>> register) {
		register.register("animations",
				() -> ArgumentTypeInfos.registerByClass(
						AnimationArgument.class,
						SingletonArgumentInfo.contextFree(AnimationArgument::animation)
				)
		);
		register.register("layers",
				() -> ArgumentTypeInfos.registerByClass(
						AnimationLayerArgument.class,
						SingletonArgumentInfo.contextFree(AnimationLayerArgument::layer)
				)
		);
	}
}
