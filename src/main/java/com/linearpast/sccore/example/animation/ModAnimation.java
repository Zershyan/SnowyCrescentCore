package com.linearpast.sccore.example.animation;

import com.linearpast.sccore.SnowyCrescentCore;
import com.linearpast.sccore.animation.AnimationUtils;
import com.linearpast.sccore.animation.data.Animation;
import com.linearpast.sccore.animation.data.Ride;
import com.linearpast.sccore.animation.event.create.AnimationLayerRegisterEvent;
import com.linearpast.sccore.example.animation.event.ExampleCommandEvent;
import com.linearpast.sccore.example.animation.event.ExamplePlayerAttackEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.loading.FMLEnvironment;

/**
 * @see AnimationUtils
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

    public static void register(IEventBus forgeBus, IEventBus modBus) {
        //You must define corresponding Animation to register
        Animation amLTRL = new Animation(AmLyingToRightLying)
                .withLyingType(Animation.LyingType.RIGHT)
                .withRide(Ride.create().addComponentAnimation(AmStandToLying));
        Animation amSTL = new Animation(AmStandToLying)
                .withLyingType(Animation.LyingType.FRONT);

        Animation waltzGentleman = new Animation(WaltzGentleman)
                .withRide(Ride.create().addComponentAnimation(WaltzLady));
        Animation waltzLady = new Animation(WaltzLady)
                .withCamYaw(180);

        //You can use it to register an Animation
        AnimationUtils.registerAnimation(AmLyingToRightLying, amLTRL);
        AnimationUtils.registerAnimation(AmStandToLying, amSTL);
        AnimationUtils.registerAnimation(WaltzGentleman, waltzGentleman);
        AnimationUtils.registerAnimation(WaltzLady, waltzLady);


        //Register by event
        //Or use AnimationUtils.registerAnimationLayer(ResourceLocation layer, int priority);
        modBus.addListener(ModAnimation::onLayerRegister);

        //Try to play animation
        forgeBus.addListener(ExamplePlayerAttackEvent::onPlayerAttack);
        forgeBus.addListener(ExampleCommandEvent::inviteDance);
        if(FMLEnvironment.dist == Dist.CLIENT){
            forgeBus.addListener(ExamplePlayerAttackEvent::onInputEvent);
        }
    }

    public static void onLayerRegister(AnimationLayerRegisterEvent event) {
        event.putLayer(normalLayers, 42);
    }
}
