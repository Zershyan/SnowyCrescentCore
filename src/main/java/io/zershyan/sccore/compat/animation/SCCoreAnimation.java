package io.zershyan.sccore.compat.animation;

import io.zershyan.sccore.compat.ICompatUtils;
import io.zershyan.sccore.compat.animation.core.ClientAnimationRegistry;
import io.zershyan.sccore.compat.animation.core.ServerAnimationRegistry;
import io.zershyan.sccore.compat.animation.network.data.*;
import io.zershyan.sccore.compat.animation.registry.AnimationAttachments;
import io.zershyan.sccore.compat.animation.registry.AnimationCommands;
import io.zershyan.sccore.compat.animation.registry.AnimationEntities;
import io.zershyan.sccore.compat.animation.registry.AnimationEntityDataSerializers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class SCCoreAnimation implements ICompatUtils {
    public static final String MODID = "playeranimator";

    @Override
    public boolean isModLoaded() {
        return ModList.get().isLoaded(MODID);
    }

    @Override
    public void addClientListener(IEventBus forgeBus, IEventBus modBus) {
        forgeBus.register(ClientAnimationRegistry.class);
        forgeBus.addListener(AnimationCommands::clientCommandRegister);
    }

    @Override
    public void addCommonListener(IEventBus forgeBus, IEventBus modBus) {
        forgeBus.register(ServerAnimationRegistry.class);
        forgeBus.addListener(AnimationCommands::commonCommandRegister);

        AnimationEntities.register(modBus);
        AnimationAttachments.register(modBus);
        AnimationEntityDataSerializers.register(modBus);
    }

    @Override
    public void registerNetwork(PayloadRegistrar registrar) {
        //common
        registrar.playBidirectional(MovementAnimationTickData.TYPE, MovementAnimationTickData.STREAM_CODEC, MovementAnimationTickData::handler);

        //server
        registrar.playToServer(UpdateAnimationData.TYPE, UpdateAnimationData.STREAM_CODEC, UpdateAnimationData::handler);
        registrar.playToServer(UpdateRideAnimationData.TYPE, UpdateRideAnimationData.STREAM_CODEC, UpdateRideAnimationData::handler);

        //client
        registrar.playToClient(RegisterLayerData.TYPE, RegisterLayerData.STREAM_CODEC, RegisterLayerData::handler);
        registrar.playToClient(RegisterAnimationData.TYPE, RegisterAnimationData.STREAM_CODEC, RegisterAnimationData::handler);
        registrar.playToClient(SyncAnimationData.TYPE, SyncAnimationData.STREAM_CODEC, SyncAnimationData::handler);
        registrar.playToClient(TurnThirdPersonData.TYPE, TurnThirdPersonData.STREAM_CODEC, TurnThirdPersonData::handler);
    }
}
