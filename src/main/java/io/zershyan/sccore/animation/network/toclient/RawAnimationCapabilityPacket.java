package io.zershyan.sccore.animation.network.toclient;

import io.zershyan.sccore.animation.capability.RawAnimationDataCapability;
import io.zershyan.sccore.animation.utils.AnimationUtils;
import io.zershyan.sccore.capability.data.ICapabilitySync;
import io.zershyan.sccore.capability.data.player.SimplePlayerCapabilitySync;
import io.zershyan.sccore.capability.network.SimpleCapabilityPacket;
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

public class RawAnimationCapabilityPacket extends SimpleCapabilityPacket<Player> {
    public RawAnimationCapabilityPacket(ICapabilitySync<Player> packet) {
        super(packet);
    }

    public RawAnimationCapabilityPacket(FriendlyByteBuf buf) {
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
            RawAnimationDataCapability data = RawAnimationDataCapability.getCapability(player).orElse(null);
            testPlayAnimations((AbstractClientPlayer) player, nbt, data);
            syncData(nbt, data);
        }catch (Exception ignored) {}
    }

    private void testPlayAnimations(AbstractClientPlayer player, CompoundTag tag, RawAnimationDataCapability data) {
        if(data == null) return;
        Set<ResourceLocation> oldLayerSet = new HashSet<>(data.getAnimations().keySet());
        CompoundTag animMap = tag.getCompound(RawAnimationDataCapability.AnimMap);
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
