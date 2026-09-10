package io.zershyan.sccore.compat.patchouli.api.datagen.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.zershyan.sccore.compat.patchouli.api.datagen.data.component.ITemplateComponent;
import io.zershyan.sccore.compat.patchouli.api.datagen.data.format.TemplateInclude;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PatchouliTemplateData implements IPatchouliTemplateData {
    @NotNull
    private final ResourceLocation id;
    @NotNull
    private final List<ITemplateComponent> components = new ArrayList<>();
    private List<TemplateInclude> include;
    private Class<?> processor;

    public PatchouliTemplateData(@NotNull ResourceLocation id) {
        this.id = id;
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return id;
    }

    @Override
    public PatchouliTemplateData addComponents(ITemplateComponent ... components) {
        this.components.addAll(Arrays.stream(components).toList());
        return this;
    }

    @Override
    public PatchouliTemplateData components(ITemplateComponent ... components) {
        this.components.clear();
        this.components.addAll(Arrays.stream(components).toList());
        return this;
    }

    @Override
    public PatchouliTemplateData include(TemplateInclude include) {
        if(this.include == null) {
            this.include = new ArrayList<>();
        }
        this.include.add(include);
        return this;
    }

    @Override
    public PatchouliTemplateData processor(Class<?> processor) {
        this.processor = processor;
        return this;
    }

    @Override
    public JsonObject serialize() {
        JsonObject object = new JsonObject();
        if(components.isEmpty()) {
            throw new JsonParseException("components is empty");
        }
        JsonArray componentsArray = new JsonArray();
        components.stream().map(ITemplateComponent::serialize).forEach(componentsArray::add);
        object.add("components", componentsArray);
        if(include != null) {
            JsonArray includeArray = new JsonArray();
            include.forEach(templateInclude -> includeArray.add(templateInclude.serialize()));
            object.add("include", includeArray);
        }
        if(processor != null) {
            object.addProperty("processor", processor.getName());
        }
        return object;
    }
}
