package io.zershyan.sccore.compat.patchouli.api.datagen.data.format;

import com.google.gson.JsonObject;
import io.zershyan.sccore.compat.patchouli.api.datagen.data.IPatchouliTemplateData;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class TemplateInclude implements IFormat {
    @NotNull
    private final ResourceLocation template;
    @NotNull
    private final String as;
    private Map<String, String> using;
    private Integer x;
    private Integer y;

    TemplateInclude(@NotNull IPatchouliTemplateData template, @NotNull String as) {
        this.template = template.getId();
        this.as = as;
    }

    TemplateInclude(@NotNull ResourceLocation template, @NotNull String as) {
        this.template = template;
        this.as = as;
    }

    public static TemplateInclude of(IPatchouliTemplateData template, String as) {
        return new TemplateInclude(template, as);
    }

    public TemplateInclude using(String key, String value) {
        if (using == null) {
            using = new HashMap<>();
        }
        using.put(key, value);
        return this;
    }

    public <T extends IFormat> TemplateInclude usingVar(Variable<T> variable) {
        if (using == null) {
            using = new HashMap<>();
        }
        using.put(variable.getName(), variable.parseKey());
        return this;
    }

    public <T extends IFormat> TemplateInclude usingIns(Variable<T> variable) {
        if (using == null) {
            using = new HashMap<>();
        }
        using.put(variable.getName(), variable.getValue().parse());
        return this;
    }

    public TemplateInclude x(Integer x) {
        this.x = x;
        return this;
    }

    public TemplateInclude y(Integer y) {
        this.y = y;
        return this;
    }

    public JsonObject serialize() {
        JsonObject object = new JsonObject();
        object.addProperty("template", template.toString());
        object.addProperty("as", as);
        if (x != null) {
            object.addProperty("x", x);
        }
        if (y != null) {
            object.addProperty("y", y);
        }
        if (using != null) {
            JsonObject usingObject = new JsonObject();
            using.forEach(usingObject::addProperty);
            object.add("using", usingObject);
        }
        return object;
    }
}
