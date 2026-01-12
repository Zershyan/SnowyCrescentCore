package io.zershyan.sccore.animation.helper;

import io.zershyan.sccore.animation.data.AnimationData;
import io.zershyan.sccore.animation.service.IAnimationService;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;

public class AnimationServiceGetterHelper implements IAnimationServiceGetter {
    private final @Nullable ResourceLocation location;
    private final @Nullable CompoundTag tag;

    public AnimationServiceGetterHelper(@NotNull ResourceLocation location) {
        this.location = location;
        this.tag = null;
    }

    public AnimationServiceGetterHelper(@NotNull CompoundTag tag) {
        this.tag = tag;
        this.location = null;
    }

    public AnimationServiceGetterHelper(){
        this.tag = null;
        this.location = null;
    }

    public static IAnimationServiceGetter create(ResourceLocation location) {
        return new AnimationServiceGetterHelper(location);
    }

    @Override
    public boolean filter(IAnimationService<?, ?> helper) {
        if(location != null && helper.isAnimationPresent(location)) return true;
        return Optional.ofNullable(tag).map(helper::getAnimation).map(AnimationData::getKey)
                .map(helper::isAnimationPresent).isPresent();
    }

    @Override
    public @Nullable IAnimationService<?, ?> getService() {
        return IAnimationServiceGetter.super.getService();
    }

    public Set<IAnimationService<?, ?>> getAllServices() {
        return IAnimationServiceGetter.HELPERS;
    }

    @Nullable
    public <T extends IAnimationService<?, ?>> T getService(Class<T> tClass) {
        IAnimationService<?, ?> service = getService();
        if(tClass.isInstance(service)) return tClass.cast(service);
        return null;
    }
}
