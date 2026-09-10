package io.zershyan.sccore.api;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class SCCoreApi {
    public static float tryGetPartialTick(Level level) {
        if (level.isClientSide()) {
            return clientGetPartialTick();
        } else return 0;
    }

    @OnlyIn(Dist.CLIENT)
    public static float clientGetPartialTick() {
        return Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
    }
}
