package com.linearpast.sccore.example.animation.event;

import com.linearpast.sccore.animation.AnimationUtils;
import com.linearpast.sccore.example.animation.ModAnimation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

public class ExamplePlayerAttackEvent {
    /**
     * when attack sheep, will play stand to lying animation <br>
     * when attack other player, will play animation together
     * @param event event
     */
    public static void onPlayerAttack(AttackEntityEvent event) {
        Entity target = event.getTarget();
        Player entity = event.getEntity();
        if(entity instanceof ServerPlayer player) {
            if(target instanceof Sheep){
                ResourceLocation playing = AnimationUtils.getAnimationPlaying(player, ModAnimation.normalLayers);
                if(playing == null) {
                    AnimationUtils.playAnimation(player, ModAnimation.normalLayers, ModAnimation.AmStandToLying);
                } else {
                    AnimationUtils.playAnimation(player, ModAnimation.normalLayers, null);
                }
            }
            if(target instanceof ServerPlayer serverPlayer) {
                AnimationUtils.startAnimationTogether(
                        serverPlayer,
                        ModAnimation.normalLayers,
                        ModAnimation.AmLyingToRightLying,
                        true,
                        player
                );

            }
        }

    }

    /**
     * when press "/", this will run
     * @param event event
     */
    @OnlyIn(Dist.CLIENT)
    public static void onInputEvent(InputEvent.Key event) {
        Minecraft instance = Minecraft.getInstance();
        LocalPlayer player = instance.player;
        if (player == null) return;
        if(instance.options.keyCommand.isDown()) {
            ResourceLocation playing = AnimationUtils.getAnimationPlaying(player, ModAnimation.normalLayers);
            if(playing == null) {
                AnimationUtils.playAnimationWithRide(null, ModAnimation.normalLayers, ModAnimation.AmLyingToRightLying, true);
            }
        }
    }
}
