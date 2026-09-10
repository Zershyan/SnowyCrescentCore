package io.zershyan.sccore.compat;


import com.mojang.logging.LogUtils;
import io.zershyan.sccore.SCCore;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.concurrent.Callable;

public interface ICompatUtils {
    default boolean testLoadedAndRun(Runnable runnable){
        if(isModLoaded()) runnable.run();
        else return false;
        return true;
    }

    default <T> T testLoadedAndCall(Callable<T> callable, T errorResult) {
        try {
            if(isModLoaded()) return callable.call();
        } catch (Exception ignored) {}
        return errorResult;
    }

    default <T> T testLoadedAndCall(Callable<T> callable, Callable<T> elseCall, T errorResult) {
        try {
            if(isModLoaded()) return callable.call();
            else return elseCall.call();
        }catch(Exception e) {
            return errorResult;
        }
    }

    default void addCommonListener(IEventBus forgeBus, IEventBus modBus){}
    default void addClientListener(IEventBus forgeBus, IEventBus modBus){}
    default void addListener(IEventBus forgeBus, IEventBus modBus) {
        addCommonListener(forgeBus, modBus);
        if(FMLLoader.getDist() == Dist.CLIENT){
            addClientListener(forgeBus, modBus);
        }
    }

    default void init(IEventBus forgeBus, IEventBus modBus){
        addListener(forgeBus, modBus);
    }

    default void initial(IEventBus forgeBus, IEventBus modBus) {
        try { testLoadedAndRun(() -> init(forgeBus, modBus)); }
        catch (Exception e) { LogUtils.getLogger().error(e.getMessage()); }
    }

    default void safelyRun(Runnable runnable){
        try { runnable.run(); }
        catch (Exception ignored) {}
    }

    default void initNetwork(PayloadRegistrar registrar) {
        try { testLoadedAndRun(() -> registerNetwork(registrar)); }
        catch (Exception e) { SCCore.log.error(e.getMessage()); }
    }

    default void registerNetwork(PayloadRegistrar registrar){}

    boolean isModLoaded();
}
