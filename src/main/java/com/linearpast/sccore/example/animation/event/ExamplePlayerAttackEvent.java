package com.linearpast.sccore.example.animation.event;

import com.linearpast.sccore.animation.helper.AnimationHelper;
import com.linearpast.sccore.animation.helper.RawAnimationHelper;
import com.linearpast.sccore.example.animation.ModAnimation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
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
     * when attack sheep, will play stand to lying animation
     * @param event event
     */
    public static void onPlayerAttack(AttackEntityEvent event) {
        Entity target = event.getTarget();
        Player entity = event.getEntity();
        if(entity instanceof ServerPlayer player) {
            if(target instanceof Sheep){
                ResourceLocation playing = AnimationHelper.INSTANCE.getAnimationPlaying(player, ModAnimation.normalLayers);
                if(playing == null) {
                    AnimationHelper.INSTANCE.playAnimation(player, ModAnimation.normalLayers, ModAnimation.AmStandToLying);
                } else {
                    AnimationHelper.INSTANCE.removeAnimation(player, ModAnimation.normalLayers);
                }
            }
        }
    }

    public static void rawAnimationAttack(AttackEntityEvent event) {
        Entity target = event.getTarget();
        Player entity = event.getEntity();
        if(entity instanceof AbstractClientPlayer player && target instanceof AbstractClientPlayer targetPlayer) {
            if(player == Minecraft.getInstance().player){
                RawAnimationHelper.INSTANCE.invite(
                        ModAnimation.normalLayers,
                        ModAnimation.WaltzGentleman,
                        targetPlayer
                );
            }
        }
        if(entity instanceof ServerPlayer player && target instanceof ServerPlayer targetPlayer) {
            RawAnimationHelper.INSTANCE.acceptInvite(player, targetPlayer);
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
            ResourceLocation playing = AnimationHelper.INSTANCE.getAnimationPlaying(player, ModAnimation.normalLayers);
            if(playing == null) {
                AnimationHelper.INSTANCE.playAnimationWithRide((AbstractClientPlayer) null, ModAnimation.normalLayers, ModAnimation.AmLyingToRightLying, true);
            }
        }
    }
}
