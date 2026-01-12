package io.zershyan.sccore.animation.command.exception;

import io.zershyan.sccore.core.datagen.ModLang;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class CommandComponentException extends Exception {
	private final Component component;
	private final boolean isCommandFail;
	public CommandComponentException(ModLang.TranslatableMessage message, Object ... args) {
		super("Expected command exception.");
		this.component = Component.translatable(
				message.getKey(),
				args
		).withStyle(ChatFormatting.RED);
		this.isCommandFail = true;
	}
	public CommandComponentException(ModLang.TranslatableMessage message, Style style, Object ... args) {
		super("Expected command exception.");
		this.component = Component.translatable(
				message.getKey(),
				args
		).withStyle(style);
		this.isCommandFail = false;
	}

	public Component getCommandFailBack() {
		return isCommandFail ? Component.translatable(
				ModLang.TranslatableMessage.COMMAND_RUN_FAIL.getKey(),
				component
		).withStyle(ChatFormatting.RED) : component;
	}
}
