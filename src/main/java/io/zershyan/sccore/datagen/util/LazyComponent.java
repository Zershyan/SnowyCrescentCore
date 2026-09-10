package io.zershyan.sccore.datagen.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public record LazyComponent(String key) {
    private static Object[] formatNumbers(Object[] args, String format) {
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Float || args[i] instanceof Double) {
                args[i] = formatOptimized(format, ((Number) args[i]).doubleValue());
            }
        }
        return args;
    }

    private static String formatOptimized(String format, double value) {
        DecimalFormat df = new DecimalFormat(format, DecimalFormatSymbols.getInstance(Locale.US));
        df.setRoundingMode(RoundingMode.HALF_UP);
        return df.format(value);
    }

    public MutableComponent get(Object... args) {
        return Component.translatable(this.key, args);
    }

    public MutableComponent getNumber2f(Object... args) {
        return get(formatNumbers(args, "#.##"));
    }

    public MutableComponent getNumber1f(Object... args) {
        return get(formatNumbers(args, "#.#"));
    }
}
