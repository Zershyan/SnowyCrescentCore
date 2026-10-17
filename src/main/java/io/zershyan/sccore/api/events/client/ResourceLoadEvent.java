package io.zershyan.sccore.api.events.client;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * 客户端资源重载事件。
 *
 * <p>取消 {@link Pre} 会跳过本次重载：重载入口返回一个已完成（但未实际加载资源）的 future，
 * 调用链据此继续执行，因此取消方需要自行保证资源状态一致。</p>
 */
public abstract class ResourceLoadEvent extends Event {
    /**
     * 资源重载开始前发布，可取消。
     */
    public static class Pre extends ResourceLoadEvent implements ICancellableEvent {
    }

    /**
     * 资源重载完成后发布。
     */
    public static class Post extends ResourceLoadEvent {
    }
}
