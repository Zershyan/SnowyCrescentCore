package io.zershyan.sccore.animation;

import io.zershyan.sccore.animation.helper.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

public class AnimationApi {
    public static AnimationJsonHelper getJsonHelper(MinecraftServer server) {
        return AnimationJsonHelper.getHelper(server);
    }

    public static AnimationDataHelper getDataHelper() {
        return AnimationDataHelper.getHelper();
    }

    public static AnimationServiceGetterHelper getServiceGetterHelper(ResourceLocation location) {
        return new AnimationServiceGetterHelper(location);
    }

    public static AnimationServiceGetterHelper getServiceGetterHelper() {
        return new AnimationServiceGetterHelper();
    }

    public static AnimationHelper getHelper(Player player) {
        return AnimationHelper.getHelper(player);
    }

    public static AnimationRegistryHelper getRegistryHelper() {
        return AnimationRegistryHelper.getHelper();
    }
}
