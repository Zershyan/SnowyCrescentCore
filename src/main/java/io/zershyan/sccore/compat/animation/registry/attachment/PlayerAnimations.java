package io.zershyan.sccore.compat.animation.registry.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import io.zershyan.sccore.compat.animation.api.SCCAnimationApi;
import io.zershyan.sccore.compat.animation.registry.AnimationAttachments;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public record PlayerAnimations(RideAnim rideAnim, HashMap<ResourceLocation, ResourceLocation> clientAnimMap, HashMap<ResourceLocation, ResourceLocation> serverAnimMap) {
    public record RideAnim(Optional<ResourceLocation> layer, Optional<ResourceLocation> animation) {
        public static final Codec<RideAnim> CODEC = RecordCodecBuilder.create(i -> i.group(
                ResourceLocation.CODEC.optionalFieldOf("layer").forGetter(RideAnim::layer),
                ResourceLocation.CODEC.optionalFieldOf("animation").forGetter(RideAnim::animation)
        ).apply(i, RideAnim::new));
        public RideAnim() {
            this(Optional.empty(), Optional.empty());
        }
        public static RideAnim of(ResourceLocation layer, ResourceLocation animLoc) {
            return new RideAnim(Optional.of(layer), Optional.of(animLoc));
        }
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof RideAnim(Optional<ResourceLocation> otherLayer, Optional<ResourceLocation> otherAnim)) {
                return Objects.equals(otherLayer.orElse(null), this.layer.orElse(null))
                        && Objects.equals(otherAnim.orElse(null), this.animation.orElse(null));
            }
            return false;
        }
    }
    public static final Codec<PlayerAnimations> CODEC = RecordCodecBuilder.create(i -> i.group(
            RideAnim.CODEC.fieldOf("rideAnim").forGetter(PlayerAnimations::rideAnim),
            Codec.unboundedMap(ResourceLocation.CODEC, ResourceLocation.CODEC).xmap(HashMap::new, Function.identity())
                    .fieldOf("clientAnimMap").forGetter(PlayerAnimations::clientAnimMap),
            Codec.unboundedMap(ResourceLocation.CODEC, ResourceLocation.CODEC).xmap(HashMap::new, Function.identity())
                    .fieldOf("serverAnimMap").forGetter(PlayerAnimations::serverAnimMap)
    ).apply(i, PlayerAnimations::new));
    public static final StreamCodec<ByteBuf, PlayerAnimations> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
    public PlayerAnimations() {
        this(new RideAnim(), new HashMap<>(), new HashMap<>());
    }
    public boolean clientAnimMapEqual(HashMap<ResourceLocation, ResourceLocation> otherMap) {
        return Objects.equals(this.clientAnimMap, otherMap);
    }
    public boolean serverAnimMapEqual(HashMap<ResourceLocation, ResourceLocation> otherMap) {
        return Objects.equals(this.serverAnimMap, otherMap);
    }

    public static PlayerAnimations getData(Player player) {
        return player.getData(AnimationAttachments.PLAYER_ANIMATIONS);
    }

    public static final AttachmentSyncHandler<PlayerAnimations> SYNC_HANDLER = new AttachmentSyncHandler<>() {
        @Override
        public void write(@NotNull RegistryFriendlyByteBuf buf, @NotNull PlayerAnimations playerAnimations, boolean initialSync) {
            ByteBufCodecs.fromCodecWithRegistries(CODEC).encode(buf, playerAnimations);
        }

        @Override
        public PlayerAnimations read(@NotNull IAttachmentHolder holder, @NotNull RegistryFriendlyByteBuf buf, @Nullable PlayerAnimations oldAnimations) {
            PlayerAnimations newAnimations = ByteBufCodecs.fromCodecWithRegistries(CODEC).decode(buf);
            if(!(holder instanceof AbstractClientPlayer player)) return newAnimations;
            SCCAnimationApi.animPlayer(player).updateAnimation(oldAnimations, newAnimations);
            return newAnimations;
        }
    };
}
