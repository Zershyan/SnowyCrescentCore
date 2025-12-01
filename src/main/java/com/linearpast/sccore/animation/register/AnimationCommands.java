package com.linearpast.sccore.animation.register;

import com.linearpast.sccore.animation.command.*;
import com.linearpast.sccore.animation.command.argument.AnimationArgument;
import com.linearpast.sccore.animation.command.argument.AnimationLayerArgument;
import com.linearpast.sccore.animation.command.client.ListClientCommand;
import com.linearpast.sccore.animation.command.client.RefreshCommand;
import com.linearpast.sccore.animation.service.IAnimationService;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
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
