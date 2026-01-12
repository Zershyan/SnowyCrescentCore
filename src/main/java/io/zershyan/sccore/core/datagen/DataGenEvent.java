package io.zershyan.sccore.core.datagen;

import io.zershyan.sccore.SnowyCrescentCore;
import io.zershyan.sccore.core.datagen.provider.ModLangProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = SnowyCrescentCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenEvent {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper helper = event.getExistingFileHelper();

        generator.addProvider(event.includeClient(), new ModLangProvider(packOutput, ModLangProvider.Lang.EN_US));
        generator.addProvider(event.includeClient(), new ModLangProvider(packOutput, ModLangProvider.Lang.ZH_CN));
    }
}
