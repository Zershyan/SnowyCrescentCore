package io.zershyan.sccore.animation.network.toclient;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.zershyan.sccore.animation.data.GenericAnimationData;
import io.zershyan.sccore.animation.data.util.AnimJson;
import io.zershyan.sccore.animation.data.util.AnimLayerJson;
import io.zershyan.sccore.animation.register.AnimationRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.function.Supplier;

public record AnimationJsonPacket(String json, boolean isLayer) {
    public AnimationJsonPacket(FriendlyByteBuf buf) {
        this(buf.readUtf(), buf.readBoolean());
    }
    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(json);
        buf.writeBoolean(isLayer);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            context.setPacketHandled(true);
            JsonElement element = JsonParser.parseString(json);
            if(isLayer) {
                Map<ResourceLocation, Integer> parse = AnimLayerJson.Reader.stream(element).parse();
                parse.forEach(AnimationRegistry.ClientCache::cacheAddAnimationLayer);
            } else {
                GenericAnimationData animation = AnimJson.Reader.stream(element).parse();
                AnimationRegistry.ClientCache.cacheAddAnimation(animation.getKey(), animation);
            }
        });
    }
}
