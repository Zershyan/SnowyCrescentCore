package com.linearpast.sccore.capability.network;

import com.linearpast.sccore.SnowyCrescentCore;
import com.linearpast.sccore.capability.CapabilityUtils;
import com.linearpast.sccore.capability.data.ICapabilitySync;
import com.linearpast.sccore.capability.data.entity.SimpleEntityCapabilitySync;
import com.linearpast.sccore.capability.data.player.SimplePlayerCapabilitySync;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

public class SimpleCapabilityPacket<T extends Entity> implements ICapabilityPacket<T> {
    private final ResourceLocation key;
    private final CompoundTag data;

    /**
     * Constructor
     * @param data data tag
     */
    public SimpleCapabilityPacket(ResourceLocation key, CompoundTag data) {
        this.key = key;
        this.data = data;
    }

    public SimpleCapabilityPacket(ICapabilitySync<T> packet) {
        this.key = packet.getKey();
        this.data = packet.serializeNBT();
    }

    /**
     * decoder
     * @param buf buf
     */
    public SimpleCapabilityPacket(FriendlyByteBuf buf) {
        this.key = buf.readResourceLocation();
        this.data = buf.readNbt();
    }

    /**
     * encoder
     * @param buf buf
     */
    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(key);
        buf.writeNbt(data);
    }

    /**
     * Default network packet handle, generally sufficient for use
     * @param context NetworkEvent.Context
     */
    @Override
    public void handler(NetworkEvent.Context context) {
        context.setPacketHandled(true);
        Minecraft instance = Minecraft.getInstance();
        ClientLevel level = instance.level;
        if (level == null) return;
        CompoundTag nbt = getData();
        Entity entity = null;
        if(nbt.contains(SimpleEntityCapabilitySync.Id)){
            entity = level.getEntity(nbt.getInt(SimpleEntityCapabilitySync.Id));
        }
        if(nbt.contains(SimplePlayerCapabilitySync.OwnerUUID)){
            entity = level.getPlayerByUUID(nbt.getUUID(SimplePlayerCapabilitySync.OwnerUUID));
        }
        if(entity == null) return;
        try {
            ICapabilitySync<?> data = CapabilityUtils.getCapability(entity, key);
            if(data == null) {
                SnowyCrescentCore.log.error("key {} not found when sync capability on entity {}", key, entity);
                return;
            }
            syncData(nbt, data);
        }catch (Exception ignored) {}
    }

    @Override
    public CompoundTag getData() {
        return data;
    }
}
