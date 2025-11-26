package com.linearpast.sccore.animation.command.exception;

import com.linearpast.sccore.animation.utils.ApiBack;
import com.linearpast.sccore.core.datagen.ModLang;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ApiBackException extends Exception {
    private final ApiBack apiBack;
    private Object[] args;
    public ApiBackException(ApiBack apiBack) {
        this.apiBack = apiBack;
    }

    public ApiBackException(ApiBack apiBack, Object... args) {
        this.apiBack = apiBack;
        this.args = args;
    }

    public static ApiBackException withCooldown(Integer cooldown) {
        return new ApiBackException(ApiBack.COOLDOWN, cooldown);
    }

    public static ApiBackException withOutRange(Integer distance) {
        return new ApiBackException(ApiBack.OUT_RANGE, distance);
    }

    public MutableComponent getCommandFailBack() {
        if(args != null && args.length > 0) {
            return Component.translatable(getLang(), args);
        } else {
            return Component.translatable(getLang());
        }
    }

    public String getLang() {
        return switch (apiBack) {
            case FAIL -> ModLang.TranslatableMessage.COMMAND_RUN_FAIL.getKey();
            case UNSUPPORTED -> ModLang.TranslatableMessage.ANIMATION_OPERATION_UNSUPPORTED.getKey();
            case COOLDOWN -> ModLang.TranslatableMessage.ANIMATION_COOLDOWN.getKey();
            case OUT_RANGE -> ModLang.TranslatableMessage.ANIMATION_OUT_RANGE.getKey();
            case OPERATION_EXPIRE -> ModLang.TranslatableMessage.ANIMATION_EXPIRE.getKey();
            case RESOURCE_NOT_FOUND -> ModLang.TranslatableMessage.ANIMATION_RESOURCE_NOT_FOUND.getKey();
            case SUCCESS -> ModLang.TranslatableMessage.COMMAND_RUN_SUCCESS.getKey();
            case BE_CANCELLED -> ModLang.TranslatableMessage.ANIMATION_OPERATION_CANCELLED.getKey();
        };
    }
}
