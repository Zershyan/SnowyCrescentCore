package io.zershyan.sccore.compat.patchouli.api.datagen.data.format;

public class StringFormat implements IFormat {
    private final String value;

    public StringFormat(String value) {
        this.value = value;
    }

    @Override
    public String parse() {
        return value;
    }
}
