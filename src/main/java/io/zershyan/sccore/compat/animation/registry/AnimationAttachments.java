package io.zershyan.sccore.compat.animation.registry;

import io.zershyan.sccore.SCCore;
import io.zershyan.sccore.compat.animation.registry.attachment.ModifyAABBData;
import io.zershyan.sccore.compat.animation.registry.attachment.PlayerAnimations;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class AnimationAttachments {
    private static final DeferredRegister<AttachmentType<?>> REGISTRY = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, SCCore.MODID);

    public static final Supplier<AttachmentType<PlayerAnimations>> PLAYER_ANIMATIONS = REGISTRY.register(
            "player_animations", () -> AttachmentType.builder(PlayerAnimations::new)
                    .serialize(PlayerAnimations.CODEC)
                    .sync(PlayerAnimations.SYNC_HANDLER)
                    .copyOnDeath()
                    .build());
    public static final Supplier<AttachmentType<ModifyAABBData>> MODIFY_AABB_DATA = REGISTRY.register(
            "modify_aabb_data", () -> AttachmentType.builder(() -> (ModifyAABBData) null)
                    .serialize(ModifyAABBData.CODEC)
                    .sync(ModifyAABBData.STREAM_CODEC)
                    .copyOnDeath()
                    .build());

    public static void register(IEventBus modEventBus) {
        REGISTRY.register(modEventBus);
    }

}
