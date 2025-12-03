package com.linearpast.sccore.animation.command.argument;

import com.linearpast.sccore.animation.data.GenericAnimationData;
import com.linearpast.sccore.animation.register.AnimationRegistry;
import com.linearpast.sccore.animation.register.RawAnimationRegistry;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class AnimationArgument implements ArgumentType<String> {
    private static final Supplier<Collection<String>> EXAMPLES = AnimationArgument::getAnimationNames;

    private static Supplier<Set<String>> animationNames;
    public AnimationArgument() {
        resetAnimationNames();
    }

    public static void resetAnimationNames() {
        Set<String> set = new HashSet<>(getAnimationNames());
        Set<String> strings = DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () ->
                AnimationArgument::getAnimationNamesClient
        );
        if (strings != null && !strings.isEmpty()) set.addAll(strings);
        animationNames = () -> set;
    }

    private static Set<String> getAnimationNames(){
        Set<String> set = new HashSet<>();
        AnimationRegistry.getAnimations().forEach((key, value) -> {
            String name = value.getName();
            if(name != null && !set.contains(name)) {
                set.add(name);
            } else set.add(key.toString());
        });
        return set;
    }

    @OnlyIn(Dist.CLIENT)
    private static Set<String> getAnimationNamesClient() {
        Set<String> set = new HashSet<>();
        RawAnimationRegistry.getAnimations().keySet().forEach(location ->
                set.add(location.toString())
        );
        return set;
    }

    public static AnimationArgument animation() {
        return new AnimationArgument();
    }

    @Nullable
    private static ResourceLocation getAnimationByName(String name) {
        for (GenericAnimationData animation : AnimationRegistry.getAnimations().values()) {
            if (Objects.equals(animation.getName(), name)) {
                return animation.getKey();
            }
        }
        return null;
    }

    public static String getAnimation(CommandContext<CommandSourceStack> context, String name) {
        String argument = context.getArgument(name, String.class);
        if(argument.contains(":")) return argument;
        ResourceLocation animationByName = getAnimationByName(argument);
        if(animationByName == null) return argument;
        else return animationByName.toString();
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(animationNames.get(), builder);
    }

    public Collection<String> getExamples() {
        return EXAMPLES.get();
    }

    public String parse(StringReader reader) {
        int start = reader.getCursor();
        while (reader.canRead() && canRead(reader.peek())) {
            reader.skip();
        }
        return reader.getString().substring(start, reader.getCursor());
    }

    private static boolean canRead(char peek) {
        boolean origin = StringReader.isAllowedInUnquotedString(peek);
        return origin || peek == ':';
    }
}
