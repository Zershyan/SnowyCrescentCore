package com.linearpast.sccore.example.capability.data;

import com.linearpast.sccore.capability.data.ICapabilitySync;
import net.minecraft.world.entity.animal.Sheep;

/**
 * The interface inheritance ICapabilitySync is required, but the interface is not necessary (you can directly use the cap class itself during registration) <br>
 * Server methods for sharing caps that may be used.
 */
public interface ISheepData extends ICapabilitySync<Sheep> {
    Integer getValue();
    void setValue(Integer value);
}
