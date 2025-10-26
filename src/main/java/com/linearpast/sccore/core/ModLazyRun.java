package com.linearpast.sccore.core;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;

import java.util.concurrent.Callable;

public abstract class ModLazyRun {
    private final String modId;
    public ModLazyRun(String modId) {
        this.modId = modId;
    }

    public boolean testLoadedAndRun(Runnable runnable){
        if(isModLoaded()) runnable.run();
        else return false;
        return true;
    }

    public <T> T testLoadedAndCall(Callable<T> callable) {
        try {
            if(isModLoaded()) return callable.call();
        } catch (Exception ignored) {}
        return null;
    }

    public <T> T testLoadedAndCall(Callable<T> callable, Callable<T> elseCall) {
        try {
            if(isModLoaded()) return callable.call();
            else return elseCall.call();
        }catch(Exception e) {
            return null;
        }
    }

    public void addCommonListener(IEventBus forgeBus, IEventBus modBus){}
    public void addClientListener(IEventBus forgeBus, IEventBus modBus){}
    public void testLoadedAndAddListener(IEventBus forgeBus, IEventBus modBus) {
        if(isModLoaded()){
            addCommonListener(forgeBus, modBus);
            if(FMLLoader.getDist() == Dist.CLIENT){
                addClientListener(forgeBus, modBus);
            }
        }
    }

    public boolean isModLoaded() {
        return ModList.get().isLoaded(modId);
    };
}
