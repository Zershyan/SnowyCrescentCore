package com.linearpast.sccore.core.datagen.provider;

import com.linearpast.sccore.SnowyCrescentCore;

import com.linearpast.sccore.animation.data.util.SCCAnimationLayerProvider;
import com.linearpast.sccore.example.animation.ModAnimation;
import net.minecraft.data.DataGenerator;

public class ModAnimationLayerProvider extends SCCAnimationLayerProvider {
    public ModAnimationLayerProvider(DataGenerator generator) {
        super(generator, SnowyCrescentCore.MODID);
    }


    @Override
    protected LayerBuilder createLayerData() {
        return LayerBuilder.create()
                .addCustomLayer(ModAnimation.normalLayers, 42);
    }
}
