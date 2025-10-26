package com.linearpast.sccore.capability.data;

import com.linearpast.sccore.capability.network.SimpleCapabilityPacket;
import com.linearpast.sccore.core.ModChannel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.util.INBTSerializable;

public interface ICapabilitySync<T extends Entity> extends INBTSerializable<CompoundTag> {
    /**
     * You should call it to set it to true in the setter of each property to trigger automatic synchronization
     * @param dirty dirty
     */
    void setDirty(boolean dirty);

    boolean isDirty();

    /**
     * When this method is overridden, the super method should be called at the end
     * @param oldData old data
     * @param listenDone Whether to execute the completion method at the end: {@link ICapabilitySync#onCopyDone()}
     */
    default void copyFrom(ICapabilitySync<?> oldData, boolean listenDone) {
        this.setDirty(oldData.isDirty());
        if(listenDone) onCopyDone();
    }

    /**
     * After the copy is completed, if certain values need to be redefined, you should override this method <br>
     * Commonly used for resetting data when players cross dimensions or die
     */
    default void onCopyDone(){}

    /**
     * In general, it is recommended to rewrite it, otherwise it will be sent as a Channel instance of SCCore<br>
     * The server sends client synchronized data to all players
     */
    default void sendToClient(){
        ModChannel.sendAllPlayer(getDefaultPacket());
    }

    /**
     * In general, it is recommended to rewrite it, otherwise it will be sent as a Channel instance of SCCore<br>
     * The server sends client synchronized data to a single player
     * @param player Target player
     */
    default void sendToClient(ServerPlayer player){
        ModChannel.sendToPlayer(getDefaultPacket(), player);
    }

    /**
     * Rewrite this method to set a network packet class for your Capability <br>
     * When calling the sendToClient method, network packets will be obtained from here and sent directly <br>
     * In general, you should extend SimpleCapacityPackage and then override the method to return your subclass
     * @return SimpleCapacityPacket, a network packet class
     */
    SimpleCapabilityPacket<T> getDefaultPacket();

    /**
     * When players log in or entity join the world, the capability initialization will be called <br>
     * Must be implemented, but can be an empty method
     * @param entity Target
     */
    void attachInit(T entity);
}
