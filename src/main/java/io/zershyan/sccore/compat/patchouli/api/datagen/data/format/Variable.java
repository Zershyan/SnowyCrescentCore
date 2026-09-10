package io.zershyan.sccore.compat.patchouli.api.datagen.data.format;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Variable<T extends IFormat> {
    @Nullable
    private String name;
    @Nullable
    private T value;

    public Variable(@NotNull String name) {
        this.name = name;
    }

    public Variable(@NotNull T value) {
        this.value = value;
    }

    Variable(@Nullable String name, @NotNull T value) {
        this.name = name;
        this.value = value;
    }

    public @NotNull String getName() {
        if(name == null) throw new NullPointerException("Variable name is null");
        return name;
    }

    public String parseKey() {
        if(name == null && value == null) throw new NullPointerException("name or value cannot be null");
        if(name == null) return value.parse();
        return "#" + name;
    }

    public T getValue() {
        if(value == null) throw new NullPointerException("value is null");
        return value;
    }

    public Variable<T> assignment(T value) {
        if(name == null) throw new NullPointerException("Variable name is null");
        return new Variable<>(name, value);
    }

    public Variable<StringFormat> assignment(String value) {
        if(name == null) throw new NullPointerException("Variable name is null");
        return new Variable<>(name, new StringFormat(value));
    }

    public static <T extends IFormat> Variable<T> assignment(@Nullable String name, @NotNull T value) {
        return new Variable<>(name, value);
    }

    public static Variable<StringFormat> assignment(@Nullable String name, String value) {
        return new Variable<>(name, new StringFormat(value));
    }
}
