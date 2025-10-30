package com.linearpast.sccore.animation.register;

import com.linearpast.sccore.animation.AnimationUtils;
import com.linearpast.sccore.animation.command.*;
import com.linearpast.sccore.animation.command.argument.AnimationArgument;
import com.linearpast.sccore.animation.command.argument.AnimationLayerArgument;
import com.linearpast.sccore.animation.command.client.GenerateJsonClientCommand;
import com.linearpast.sccore.animation.command.client.RefreshAnimCommand;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraftforge.registries.DeferredRegister;

import static net.minecraft.commands.Commands.literal;

public class AnimationCommands {
	public static void commonCommandRegister(LiteralArgumentBuilder<CommandSourceStack> builder) {
		if(AnimationUtils.ANIMATION_RUNNER.isModLoaded()){
			LiteralArgumentBuilder<CommandSourceStack> anim = literal("anim");
			PlayAnimCommand.register(anim);
			RequestAnimCommand.register(anim);
			CombineAnimCommand.register(anim);
			GenerateJsonCommand.register(anim);
			ApplyJoinAnimCommand.register(anim);
			builder.then(anim);
		}
	}

	public static void clientCommandRegister(LiteralArgumentBuilder<CommandSourceStack> builder) {
		if(AnimationUtils.ANIMATION_RUNNER.isModLoaded()) {
			LiteralArgumentBuilder<CommandSourceStack> anim = literal("anim");
			RefreshAnimCommand.register(anim);
			GenerateJsonClientCommand.register(anim);
			builder.then(anim);
		}
	}

	public static void registerArguments(DeferredRegister<ArgumentTypeInfo<?, ?>> register) {
		register.register("animation",
				() -> ArgumentTypeInfos.registerByClass(
						AnimationArgument.class,
						SingletonArgumentInfo.contextFree(AnimationArgument::animation)
				)
		);
		register.register("layer",
				() -> ArgumentTypeInfos.registerByClass(
						AnimationLayerArgument.class,
						SingletonArgumentInfo.contextFree(AnimationLayerArgument::layer)
				)
		);
	}
}
