package io.zershyan.sccore.core;

import io.zershyan.sccore.SnowyCrescentCore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModChannel {
    private static int cid = 0;
    private static final String PROTOCOL_VERSION = ModList.get()
            .getModContainerById(SnowyCrescentCore.MODID)
            .map(c -> c.getModInfo().getVersion().toString())
            .orElse("unknown");
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(SnowyCrescentCore.MODID, SnowyCrescentCore.MODID),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {

    }

    public static int getAndAddCid() {
        return cid++;
    }

    public static <MSG> void sendAllPlayer(MSG message){
        INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player){
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static <MSG> void sendToServer(MSG message){
        INSTANCE.send(PacketDistributor.SERVER.noArg(), message);
    }
}
