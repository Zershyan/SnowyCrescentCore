package io.zershyan.sccore.compat.patchouli.api.datagen.data.page;

import com.google.gson.JsonObject;
import io.zershyan.sccore.compat.patchouli.api.datagen.data.IPatchouliTemplateData;
import io.zershyan.sccore.compat.patchouli.api.datagen.data.format.Variable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TemplatePage extends IPageType {
    private final List<Variable<?>> customVariables = new ArrayList<>();

    public TemplatePage(@NotNull IPatchouliTemplateData templateData) {
        super(templateData.getId());
    }

    public TemplatePage addVariable(Variable<?> ... variables) {
        for (Variable<?> variable : variables) {
            customVariables.add(Variable.assignment(variable.getName(), variable.getValue()));
        }
        return this;
    }

    @Override
    public JsonObject toJson(JsonObject object) {
        for (Variable<?> variable : customVariables) {
            object.addProperty(variable.getName(), variable.getValue().parse());
        }
        return object;
    }
}
