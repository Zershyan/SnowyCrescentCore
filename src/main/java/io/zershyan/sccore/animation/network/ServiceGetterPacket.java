package io.zershyan.sccore.animation.network;

import io.zershyan.sccore.animation.helper.IAnimationServiceGetter;
import io.zershyan.sccore.animation.service.AnimationService;
import io.zershyan.sccore.animation.service.IAnimationService;
import io.zershyan.sccore.animation.service.RawAnimationService;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public abstract class ServiceGetterPacket implements IAnimationServiceGetter {
    /**
     * Override it to filter helper in network packet
     * @param helper helper
     * @return Is right helper
     */
    public boolean filter(IAnimationService<?, ?> helper) {
        if(helper instanceof AnimationService service) {
            ResourceLocation animation = getAnimation();
            if(animation != null) return service.isAnimationPresent(animation);
        } else if (helper instanceof RawAnimationService service) {
            CompoundTag tag = getAnimationTag();
            if(tag != null) return service.getAnimation(tag) != null;
        }
        return false;
    }

    /**
     * Selectable to override it
     * @return Animation loacation
     * @see ServiceGetterPacket#filter
     */
    @Nullable
    protected ResourceLocation getAnimation() {
        return null;
    }

    /**
     * Selectable to override it
     * @return Animation data
     * @see ServiceGetterPacket#filter
     */
    @Nullable
    protected CompoundTag getAnimationTag() {
        return null;
    }
}
