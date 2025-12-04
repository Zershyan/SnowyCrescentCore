package com.linearpast.sccore.example.animation;

import com.linearpast.sccore.SnowyCrescentCore;
import com.linearpast.sccore.animation.data.GenericAnimationData;
import com.linearpast.sccore.animation.data.Ride;
import com.linearpast.sccore.animation.event.create.AnimationRegisterEvent;
import com.linearpast.sccore.animation.service.AnimationService;
import com.linearpast.sccore.example.animation.event.ExamplePlayerAttackEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.loading.FMLEnvironment;

/**
 * @see AnimationService
 */
public class ModAnimation {
    /**
     * This is an animation layer
     */
    public static final ResourceLocation normalLayers = new ResourceLocation(SnowyCrescentCore.MODID, "normal_layers");

    /**
     * <pre>
     * They are animations
     * {@code new ResourceLocation(modid, name)}
     * Resource from "assets/{modid}/player_animation/{name}.json"</pre>
     */
    public static final ResourceLocation AmLyingToRightLying = new ResourceLocation(SnowyCrescentCore.MODID, "am_lying_to_right_lying");
    public static final ResourceLocation AmStandToLying = new ResourceLocation(SnowyCrescentCore.MODID, "am_stand_to_lying");
    public static final ResourceLocation WaltzGentleman = new ResourceLocation(SnowyCrescentCore.MODID, "waltz_gentleman");
    public static final ResourceLocation WaltzLady = new ResourceLocation(SnowyCrescentCore.MODID, "waltz_lady");

    /**
     * You can register animation layer by event or json <br>
     * See wiki (If I'm done.)
     * @param event event
     */
    public static void onLayerRegister(AnimationRegisterEvent.Layer event) {
        event.registerLayer(normalLayers, 42);
    }

    /**
     * You can register animation by event or json <br>
     * See wiki (If I'm done.)
     * @param event event
     */
    public static void onAnimationRegister(AnimationRegisterEvent.Animation event) {
        //You must define corresponding Animation to invite
        GenericAnimationData amLTRL = GenericAnimationData.create(AmLyingToRightLying)
                .withLyingType(GenericAnimationData.LyingType.RIGHT)
                .withName("Lying-to-Right-Lying");
        GenericAnimationData amSTL = GenericAnimationData.create(AmStandToLying)
                .withName("Stand-to-Lying")
                .withLyingType(GenericAnimationData.LyingType.FRONT);
        GenericAnimationData waltzGentleman = GenericAnimationData.create(WaltzGentleman)
                .withName("Waltz-Gentleman")
                .withRide(Ride.create().addComponentAnimation(WaltzLady));
        GenericAnimationData waltzLady = GenericAnimationData.create(WaltzLady)
                .withName("Waltz-Lady")
                .withCamYaw(180)
                .withRide(Ride.create().addComponentAnimation(WaltzGentleman));

        //You can use it to invite an Animation
        event.registerAnimation(AmLyingToRightLying, amLTRL);
        event.registerAnimation(AmStandToLying, amSTL);
        event.registerAnimation(WaltzGentleman, waltzGentleman);
        event.registerAnimation(WaltzLady, waltzLady);
    }

    public static void onRawAnimationRegister(AnimationRegisterEvent.RawAnimation event) {
//        RawAnimationData amSTL = RawAnimationData.create(AmStandToLying).withRide(Ride.create().withExistTick(100));
//        RawAnimationData amLTRL = RawAnimationData.create(AmLyingToRightLying).withRide(Ride.create().withExistTick(100));
//        RawAnimationData waltzGentleman = RawAnimationData.create(WaltzGentleman).withRide(Ride.create().withExistTick(100).addComponentAnimation(WaltzLady));
//        RawAnimationData waltzLady = RawAnimationData.create(WaltzLady).withRide(Ride.create().withExistTick(100).addComponentAnimation(WaltzGentleman));
//        event.registerAnimation(AmLyingToRightLying, amLTRL);
//        event.registerAnimation(AmStandToLying, amSTL);
//        event.registerAnimation(WaltzGentleman, waltzGentleman);
//        event.registerAnimation(WaltzLady, waltzLady);
    }

    public static void register(IEventBus forgeBus, IEventBus modBus) {
        //Register by event
        //Or use AnimationUtils.registerAnimationLayer(ResourceLocation layer, int priority);
        forgeBus.addListener(ModAnimation::onLayerRegister);
        forgeBus.addListener(ModAnimation::onAnimationRegister);

        //Try to play animation
//        forgeBus.addListener(ExamplePlayerAttackEvent::onPlayerAttack);
        forgeBus.addListener(ExamplePlayerAttackEvent::rawAnimationAttack);
        if(FMLEnvironment.dist == Dist.CLIENT){
//            forgeBus.addListener(ExamplePlayerAttackEvent::onInputEvent);
            forgeBus.addListener(ModAnimation::onRawAnimationRegister);
        }
    }
}
