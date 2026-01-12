package com.linearpast.sccore.core.datagen;

import com.linearpast.sccore.SnowyCrescentCore;
import com.linearpast.sccore.core.datagen.provider.ModAnimationLayerProvider;
import com.linearpast.sccore.core.datagen.provider.ModAnimationProvider;
import com.linearpast.sccore.core.datagen.provider.ModLangProvider;
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
        generator.addProvider(true, new ModAnimationProvider(generator));
        generator.addProvider(true, new ModAnimationLayerProvider(generator));
    }
}
