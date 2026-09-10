package io.zershyan.sccore.api.events.client;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public abstract class ResourceLoadEvent extends Event {
    public static class Pre extends ResourceLoadEvent implements ICancellableEvent {
    }

    public static class Post extends ResourceLoadEvent {
    }
}
