package io.zershyan.sccore.core;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;

public abstract class ModCompatRun implements IModLazyRun{
    private final String modId;
    public ModCompatRun(String modId) {
        this.modId = modId;
    }

    @Override
    public boolean testCondition() {
        return ModList.get().isLoaded(modId);
    }

    public void addCommonListener(IEventBus forgeBus, IEventBus modBus){}
    public void addClientListener(IEventBus forgeBus, IEventBus modBus){}
    public void testLoadedAndAddListener(IEventBus forgeBus, IEventBus modBus) {
        if(testCondition()){
            addCommonListener(forgeBus, modBus);
            if(FMLLoader.getDist() == Dist.CLIENT){
                addClientListener(forgeBus, modBus);
            }
        }
    }
}
