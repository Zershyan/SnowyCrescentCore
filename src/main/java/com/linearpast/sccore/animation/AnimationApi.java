package com.linearpast.sccore.animation;

import com.linearpast.sccore.animation.helper.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;

import java.util.Set;

public class AnimationApi {
    public static AnimationApi getInstance() {
        return new AnimationApi();
    }

    public AnimationHelper getAnimationHelper() {
        return AnimationHelper.INSTANCE;
    }

    public RawAnimationHelper getRawAnimationHelper() {
        return RawAnimationHelper.INSTANCE;
    }

    public JsonHelper getJsonHelper(MinecraftServer server) {
        return JsonHelper.getHelper(server);
    }

    public IAnimationHelper<?, ?> getHelperFromAnimKey(ResourceLocation location) {
        return new HelperGetterFromAnimation(location).getHelper();
    }

    public Set<IAnimationHelper<?, ?>> getAllHelpers() {
        return IHelperGetter.HELPERS;
    }
}
