package com.linearpast.sccore.animation.command.argument;

import com.linearpast.sccore.animation.register.AnimationRegistry;
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

public class AnimationLayerArgument implements ArgumentType<String> {
    private static final Supplier<Collection<String>> EXAMPLES = () -> AnimationRegistry.getLayers().keySet().stream()
            .map(ResourceLocation::toString).collect(Collectors.toSet());
    private static final DynamicCommandExceptionType UNKNOWN_TYPE = new DynamicCommandExceptionType(
            layer -> Component.literal("Unknow layer : " + layer.toString())
    );

    private final Supplier<Set<String>> animationLayers;
    public AnimationLayerArgument() {
        this.animationLayers = () -> AnimationRegistry.getLayers().keySet().stream()
                .map(ResourceLocation::toString).collect(Collectors.toSet());
    }

    public static AnimationLayerArgument layer() {
        return new AnimationLayerArgument();
    }

    public static String getLayer(CommandContext<CommandSourceStack> context, String name) {
        return context.getArgument(name, String.class);
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(animationLayers.get(), builder);
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
        if (!animationLayers.get().contains(s)) {
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
