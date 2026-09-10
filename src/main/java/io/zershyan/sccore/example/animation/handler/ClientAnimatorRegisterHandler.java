package io.zershyan.sccore.example.animation.handler;

import io.zershyan.sccore.SCCore;
import io.zershyan.sccore.compat.animation.api.SCCAnimationApi;
import io.zershyan.sccore.compat.animation.api.events.AnimationRegisterEvent;
import io.zershyan.sccore.compat.animation.api.events.LayerRegisterEvent;
import io.zershyan.sccore.compat.animation.data.camera.CameraChange;
import io.zershyan.sccore.compat.animation.data.camera.CameraData;
import io.zershyan.sccore.compat.animation.data.camera.EulerAngle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.TreeMap;

public class ClientAnimatorRegisterHandler {
    private static final ResourceLocation testLayerClient = SCCore.id("test_layer_client");
    private static final ResourceLocation testAnimClient = SCCore.id("test_anim_client");
    @SubscribeEvent
    public static void registerLayerClient(LayerRegisterEvent.Client event) {
        event.registerLayer(testLayerClient, 45);
    }

    @SubscribeEvent
    public static void registerAnimationClient(AnimationRegisterEvent.Client event) {
        TreeMap<Integer, CameraData> movement = new TreeMap<>();
        movement.put(0, CameraData.ZERO);
        movement.put(40, CameraData.of(
                new Vec3(0,-1.5,0),
                new EulerAngle(0f, 90f,90f)
        ));
        TreeMap<Integer, CameraData> movement1 = new TreeMap<>();
        movement1.put(0, CameraData.of(
                Vec3.ZERO,
                new EulerAngle(0, -90, 0)
        ));
        movement1.put(40, CameraData.of(
                new Vec3(0, 10, 0),
                new EulerAngle(90, 0, 0)
        ));
        movement1.put(100, CameraData.of(
                new Vec3(5, -1, 0),
                new EulerAngle(0, 90, 0)
        ));
        event.createAnimation(testAnimClient, SCCore.id("am_lying_to_right_lying"))
                .cameraChange(new CameraChange(movement1))
                .firstPersonCameraChange(new CameraChange(movement))
                .name("华尔兹（男）");
    }

    @SubscribeEvent
    public static void testPlayer(PlayerInteractEvent.LeftClickEmpty event) {
        if(event.getSide() == LogicalSide.CLIENT) {
            Player player = event.getEntity();
            SCCAnimationApi.animation(player).playAnimation(testLayerClient, testAnimClient);
        }
    }

    @SubscribeEvent
    public static void testPlayer1(PlayerInteractEvent.LeftClickBlock event) {
        if(event.getSide() == LogicalSide.CLIENT) {
            Player player = event.getEntity();
            SCCAnimationApi.animation(player).removeAnimation(testLayerClient);
        }
    }
}
