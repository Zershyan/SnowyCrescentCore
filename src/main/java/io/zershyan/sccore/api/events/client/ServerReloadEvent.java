package io.zershyan.sccore.api.events.client;

import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public abstract class ServerReloadEvent extends Event {
    private final MinecraftServer server;

    public ServerReloadEvent(MinecraftServer server) {
        this.server = server;
    }

    public MinecraftServer getServer() {
        return server;
    }

    public static class Pre extends ServerReloadEvent implements ICancellableEvent {
        public Pre(MinecraftServer server) {
            super(server);
        }
    }

    public static class Post extends ServerReloadEvent {
        public Post(MinecraftServer server) {
            super(server);
        }
    }
}
