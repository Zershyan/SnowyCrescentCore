package io.zershyan.sccore.compat.animation.handler.client;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import io.zershyan.sccore.SCCore;
import io.zershyan.sccore.compat.animation.api.SCCAnimationApi;
import io.zershyan.sccore.compat.animation.api.helper.AnimationPlayerHelper;
import io.zershyan.sccore.compat.animation.core.ClientAnimationRegistry;
import io.zershyan.sccore.compat.animation.network.data.MovementAnimationTickData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Map;
import java.util.Optional;

@EventBusSubscriber(modid = SCCore.MODID, value = Dist.CLIENT)
public class AnimationPlayerHandler {
    private static KeyframeAnimationPlayer currentAnimation;
    private static ResourceLocation currentAnimationId;
    private static int lastSentTick = Integer.MIN_VALUE;

    @SubscribeEvent
    public static void sendAnimationTick(ClientTickEvent.Pre event) {
        try {
            Minecraft instance = Minecraft.getInstance();
            AbstractClientPlayer player = instance.player;
            if (player == null) return;
            Optional<Map.Entry<ResourceLocation, ResourceLocation>> max = SCCAnimationApi.animation(player).getHighestPriorityAnimation(
                    animation -> !animation.aabbMovement().isEmpty()
            );
            if (max.isEmpty()) return;
            Map.Entry<ResourceLocation, ResourceLocation> entry = max.get();
            KeyframeAnimationPlayer animation = SCCAnimationApi.animPlayer(player).getKeyframeAnimationPlayer(entry.getKey());
            if (animation == null) return;
            int currentTick = animation.getCurrentTick();
            // 动画未切换且 tick 未前进时无需重复上报，避免定格动画持续发包
            if (entry.getValue().equals(currentAnimationId) && currentTick == lastSentTick) return;
            currentAnimation = animation;
            currentAnimationId = entry.getValue();
            lastSentTick = currentTick;
            MovementAnimationTickData movementAnimationTickData = new MovementAnimationTickData(player.getUUID(), Optional.of(entry.getValue()), currentTick);
            PacketDistributor.sendToServer(movementAnimationTickData);
        } catch (Exception e) {
            SCCore.LOGGER.error("Failed to report movement animation tick: {}", e.getMessage(), e);
        }
    }

    @SubscribeEvent
    public static void checkValidAnimation(ClientTickEvent.Post event) {
        try {
            Minecraft instance = Minecraft.getInstance();
            AbstractClientPlayer player = instance.player;
            if (player == null) return;
            if (currentAnimation == null) return;
            if (currentAnimation.isActive()) return;
            currentAnimation = null;
            currentAnimationId = null;
            lastSentTick = Integer.MIN_VALUE;
            MovementAnimationTickData movementAnimationTickData = new MovementAnimationTickData(player.getUUID(), Optional.empty(), 0);
            PacketDistributor.sendToServer(movementAnimationTickData);
        } catch (Exception e) {
            SCCore.LOGGER.error("Failed to report movement animation stop: {}", e.getMessage(), e);
        }
    }

    @SubscribeEvent
    public static void clearStopAnimation(ClientTickEvent.Pre event) {
        try {
            Minecraft instance = Minecraft.getInstance();
            if (instance.level == null) return;
            if (instance.player == null) return;
            if (instance.player.tickCount % 20 != 0) return;
            for (AbstractClientPlayer player : instance.level.players()) {
                Map<ResourceLocation, IAnimation> map = ClientAnimationRegistry.getCacheAnim().get(player.getUUID());
                if (map == null) continue;
                AnimationPlayerHelper helper = SCCAnimationApi.animPlayer(player);
                map.forEach((key, iAnimation) -> {
                    if (!(iAnimation instanceof ModifierLayer<?> modifierLayer)) return;
                    IAnimation animation = modifierLayer.getAnimation();
                    if (animation == null || animation.isActive()) return;
                    helper.removeAnimation(key);
                });
            }
        } catch (Exception e) {
            SCCore.LOGGER.error("Failed to clear stopped animations: {}", e.getMessage(), e);
        }
    }
}
