package com.linearpast.sccore.animation.network.toclient;

import com.linearpast.sccore.animation.capability.AnimationDataCapability;
import com.linearpast.sccore.animation.capability.inter.IAnimationCapability;
import com.linearpast.sccore.animation.utils.AnimationUtils;
import com.linearpast.sccore.capability.data.ICapabilitySync;
import com.linearpast.sccore.capability.data.player.SimplePlayerCapabilitySync;
import com.linearpast.sccore.capability.network.SimpleCapabilityPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class AnimationCapabilityPacket extends SimpleCapabilityPacket<Player> {
    public AnimationCapabilityPacket(ICapabilitySync<Player> packet) {
        super(packet);
    }

    public AnimationCapabilityPacket(FriendlyByteBuf buf) {
        super(buf);
    }

    @Override
    public void handler(NetworkEvent.Context context) {
        context.setPacketHandled(true);
        Minecraft instance = Minecraft.getInstance();
        ClientLevel level = instance.level;
        if (level == null) return;
        CompoundTag nbt = getData();
        Player player = level.getPlayerByUUID(nbt.getUUID(SimplePlayerCapabilitySync.OwnerUUID));
        if(player == null) return;
        try {
            IAnimationCapability data = AnimationDataCapability.getCapability(player).orElse(null);
            testPlayAnimations((AbstractClientPlayer) player, nbt, data);
            syncData(nbt, data);
        }catch (Exception ignored) {}
    }

    private void testPlayAnimations(AbstractClientPlayer player, CompoundTag tag, IAnimationCapability data) {
        if(data == null) return;
        ResourceLocation oldRiderAnimLayer = data.getRiderAnimLayer();
        String riderAnimLayerString = tag.getString(AnimationDataCapability.RideAnimLayer);
        ResourceLocation newRiderAnimLayer = riderAnimLayerString.isEmpty() ? null : new ResourceLocation(riderAnimLayerString);
        if(!Objects.equals(oldRiderAnimLayer, newRiderAnimLayer)) {
            String riderAnimationString = tag.getString(AnimationDataCapability.RideAnimation);
            ResourceLocation newRiderAnimation = riderAnimationString.isEmpty() ? null : new ResourceLocation(riderAnimationString);
            if(oldRiderAnimLayer != null) AnimationUtils.playAnimation(player, oldRiderAnimLayer, null);
            if(newRiderAnimLayer != null) AnimationUtils.playAnimation(player, newRiderAnimLayer, newRiderAnimation);
        }

        Set<ResourceLocation> oldLayerSet = new HashSet<>(data.getAnimations().keySet());
        CompoundTag animMap = tag.getCompound(AnimationDataCapability.AnimMap);
        for (String newLayerString : animMap.getAllKeys()) {
            ResourceLocation newLayerLocation = new ResourceLocation(newLayerString);
            String newAnimString = animMap.getString(newLayerString);
            ResourceLocation newAnimLocation = newAnimString.isEmpty() ? null : new ResourceLocation(newAnimString);
            ResourceLocation oldAnimLocation = data.getAnimation(newLayerLocation);
            if (!Objects.equals(newAnimLocation, oldAnimLocation)) {
                AnimationUtils.playAnimation(player, newLayerLocation, newAnimLocation);
            }
            oldLayerSet.remove(newLayerLocation);
        }
        for (ResourceLocation oldLayerLocation : oldLayerSet) {
            AnimationUtils.playAnimation(player, oldLayerLocation, null);
        }
    }
}
