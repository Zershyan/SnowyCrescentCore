package io.zershyan.sccore.datagen.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public record LazyComponent(String key) {
    private static final ThreadLocal<DecimalFormat> ONE_DECIMAL = ThreadLocal.withInitial(() -> newFormat("#.#"));
    private static final ThreadLocal<DecimalFormat> TWO_DECIMALS = ThreadLocal.withInitial(() -> newFormat("#.##"));

    private static DecimalFormat newFormat(String pattern) {
        DecimalFormat format = new DecimalFormat(pattern, DecimalFormatSymbols.getInstance(Locale.US));
        format.setRoundingMode(RoundingMode.HALF_UP);
        return format;
    }

    private static Object[] formatNumbers(Object[] args, DecimalFormat format) {
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Float || args[i] instanceof Double) {
                args[i] = format.format(((Number) args[i]).doubleValue());
            }
        }
        return args;
    }

    public MutableComponent get(Object... args) {
        return Component.translatable(this.key, args);
    }

    public MutableComponent getNumber2f(Object... args) {
        return get(formatNumbers(args, TWO_DECIMALS.get()));
    }

    public MutableComponent getNumber1f(Object... args) {
        return get(formatNumbers(args, ONE_DECIMAL.get()));
    }
}
