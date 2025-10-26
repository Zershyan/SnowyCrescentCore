package com.linearpast.sccore.animation.command.argument;

import com.linearpast.sccore.animation.registry.AnimationRegistry;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class AnimationArgument implements ArgumentType<String> {
    private static final Supplier<Collection<String>> EXAMPLES = () -> AnimationRegistry.getAnimations().keySet().stream()
            .map(ResourceLocation::toString).collect(Collectors.toSet());
    private static final DynamicCommandExceptionType UNKNOWN_TYPE = new DynamicCommandExceptionType(
            animation -> Component.literal("Unknow animation : " + animation.toString())
    );

    private final Set<String> animationNames;
    public AnimationArgument() {
        this.animationNames = AnimationRegistry.getAnimations().keySet().stream()
                .map(ResourceLocation::toString).collect(Collectors.toSet());
    }

    public static AnimationArgument animation() {
        return new AnimationArgument();
    }

    public static String getAnimation(CommandContext<CommandSourceStack> context, String name) {
        return context.getArgument(name, String.class);
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(animationNames, builder);
    }

    public Collection<String> getExamples() {
        return EXAMPLES.get();
    }

    public String parse(StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();
        while (reader.canRead() && canRead(reader.peek())) {
            reader.skip();
        }
        String s = reader.getString().substring(start, reader.getCursor());
        if (!animationNames.contains(s)) {
            throw UNKNOWN_TYPE.create(s);
        } else {
            return s;
        }
    }

    private static boolean canRead(char peek) {
        boolean origin = StringReader.isAllowedInUnquotedString(peek);
        return origin || peek == ':';
    }
}
