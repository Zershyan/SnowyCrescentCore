package io.zershyan.sccore.example.capability;

import io.zershyan.sccore.capability.CapabilityUtils;
import io.zershyan.sccore.capability.data.entity.EntityCapabilityRegistry;
import io.zershyan.sccore.capability.network.CapabilityChannel;
import io.zershyan.sccore.core.ModChannel;
import io.zershyan.sccore.example.capability.data.ISheepData;
import io.zershyan.sccore.example.capability.data.SheepDataCapability;
import io.zershyan.sccore.example.capability.event.PlayerAttackEvent;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.Set;

public class ModCapability {
    /**
     * Example of Registered Entity Capability<br>
     * @see CapabilityUtils#registerEntityCapability
     * @see CapabilityUtils#registerPlayerCapability
     * @see CapabilityUtils#registerEntityCapabilityWithNetwork
     * @see CapabilityUtils#registerPlayerCapabilityWithNetwork
     */
    public static void register(){
        //If you want to invite network packets in your own mod, to use : createChannel(INSTANCE)
        //And don't forget to rewrite all the sendToClient methods in the capability class
        CapabilityChannel channel = CapabilityUtils.createChannel();
        //Register the entity capability and its network packet
        //If you want invite about player, please use CapabilityUtils.registerPlayerCapabilityWithNetwork()
        CapabilityUtils.registerEntityCapabilityWithNetwork(
                //A resourceLocation, named arbitrarily without repetition
                SheepDataCapability.key,
                //Data that needs to be registered for capability
                new EntityCapabilityRegistry.CapabilityRecord<>(
                        //Registry will create a new instance of this class
                        //And you can override the parameterless construct in this class to initialize it
                        SheepDataCapability.class,
                        //Fixed writing style, generally you don't need to modify it
                        CapabilityManager.get(new CapabilityToken<>() {}),
                        //The interface of the first parameter class can be an abstract class or not require an interface
                        //You can use it yourself: SheepDataCapability.class
                        ISheepData.class,
                        //What entities should the registered capability be attached to
                        Set.of(Sheep.class)
                ),
                channel,
                //Index+1 after use to prevent subsequent network channel conflicts
                ModChannel.getAndAddCid(),
                //Class of network packet
                SheepDataCapability.SheepCapabilityPacket.class,
                //Decoder method for network packet
                SheepDataCapability.SheepCapabilityPacket::new,
                //Encoder method for network packet
                SheepDataCapability.SheepCapabilityPacket::encode,
                //Handler method for network packet
                SheepDataCapability.SheepCapabilityPacket::handle
        );
    }

    public static void addListenerToEvent(IEventBus forgeBus){
        forgeBus.addListener(PlayerAttackEvent::onPlayerAttack);
    }
}
