package io.zershyan.sccore.compat.animation.handler.client;

import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import io.zershyan.sccore.SCCore;
import io.zershyan.sccore.compat.animation.api.SCCAnimationApi;
import io.zershyan.sccore.compat.animation.api.data.AnimationHelper;
import io.zershyan.sccore.compat.animation.core.ClientAnimationRegistry;
import io.zershyan.sccore.compat.animation.data.ClientAnimation;
import io.zershyan.sccore.compat.animation.data.camera.CameraData;
import io.zershyan.sccore.compat.animation.data.camera.CameraStateSnapShot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@EventBusSubscriber(modid = SCCore.MODID, value = Dist.CLIENT)
public final class CameraTransformStateHandler {
    private static final Map<UUID, CameraStateSnapShot> SnapShots = new HashMap<>();

    /**
     * 每客户端 tick 仅锁定当前激活的相机变换动画与 tick，供渲染端采样 cur1。
     * 不再在此采样 cur——cur 由渲染端用 tick + partialTick 实时插值得到，
     * 相机位置由显示缓冲区 {@code display} 按帧时间 delta 逐帧朝 cur1 追赶，
     * 这样动画切换时 cur1 即刻跳到新动画，display 仅以 delta 比例平滑追赶，避免瞬间重置。
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft instance = Minecraft.getInstance();
        if (instance.level == null) return;
        for (AbstractClientPlayer player : instance.level.players()) {
            samplePlayer(player);
        }
    }

    private static void samplePlayer(AbstractClientPlayer player) {
        AnimationHelper helper = SCCAnimationApi.animation(player);
        Optional<Map.Entry<ResourceLocation, ResourceLocation>> max = helper.getHighestPriorityAnimation(animation -> {
            if (animation instanceof ClientAnimation clientAnimation) {
                return clientAnimation.hasCameraChange();
            } else return false;
        });

        CameraStateSnapShot snapShot = SnapShots.computeIfAbsent(player.getUUID(), uuid -> new CameraStateSnapShot());
        testValid: {
            if (max.isEmpty()) break testValid;
            Map.Entry<ResourceLocation, ResourceLocation> entry = max.get();
            ClientAnimation animation = ClientAnimationRegistry.getAnimation(entry.getValue());
            if (animation == null) break testValid;
            KeyframeAnimationPlayer currentAnimation = SCCAnimationApi.animPlayer(player).getKeyframeAnimationPlayer(entry.getKey());
            if (currentAnimation == null) break testValid;

            snapShot.setCurAnimation(animation);
            snapShot.setCurKeyframeAnimation(currentAnimation);
            return;
        }
        snapShot.clearAnimation();
    }

    public static boolean relativeEuler(Player player, boolean firstPerson) {
        CameraStateSnapShot snapShot = SnapShots.get(player.getUUID());
        if (snapShot == null) return true;
        return snapShot.relativeEuler(firstPerson);
    }

    public static @Nullable CameraData getCache(Player player, boolean firstPerson) {
        CameraStateSnapShot snapShot = SnapShots.get(player.getUUID());
        if (snapShot == null) return null;
        return snapShot.getCache(firstPerson);
    }

    public static @Nullable CameraData getAndStep(Player player, float partialTick, boolean firstPerson) {
        CameraStateSnapShot snapShot = SnapShots.get(player.getUUID());
        if (snapShot == null) return null;
        return snapShot.get(partialTick, firstPerson);
    }
}
