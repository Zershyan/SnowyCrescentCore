package com.linearpast.sccore.core.datagen.provider;

import com.linearpast.sccore.SnowyCrescentCore;
import com.linearpast.sccore.animation.data.GenericAnimationData;
import com.linearpast.sccore.animation.data.Ride;
import com.linearpast.sccore.animation.data.util.SCCAnimationProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

import static com.linearpast.sccore.example.animation.ModAnimation.WaltzGentleman;
import static com.linearpast.sccore.example.animation.ModAnimation.WaltzLady;

public class ModAnimationProvider extends SCCAnimationProvider {

    public ModAnimationProvider(DataGenerator generator) {
        super(generator, SnowyCrescentCore.MODID);
    }

    @Override
    protected void registerAnimations(Consumer<GenericAnimationData> consumer) {
        {
            GenericAnimationData waltzGentleman = (GenericAnimationData) GenericAnimationData
                    .create(WaltzGentleman)
                    .withName("Waltz-Gentleman")
                    .addCamPosOffset(new Vec3(0.0, 0.0, 1.0))
                    .withCamPosOffsetRelative(true)
                    .withRide(Ride.create().addComponentAnimation(WaltzLady));
            GenericAnimationData waltzLady = (GenericAnimationData) GenericAnimationData
                    .create(WaltzLady)
                    .withName("Waltz-Lady")
                    .withCamYaw(180)
                    .withRide(Ride.create().addComponentAnimation(WaltzGentleman));
            consumer.accept(waltzGentleman);
            consumer.accept(waltzLady);
        }
    }
}
