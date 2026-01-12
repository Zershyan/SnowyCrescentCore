package io.zershyan.sccore.animation.helper;

import io.zershyan.sccore.animation.AnimationApi;
import io.zershyan.sccore.animation.data.AnimationData;
import io.zershyan.sccore.animation.register.AnimationRegistry;
import io.zershyan.sccore.animation.register.RawAnimationRegistry;
import io.zershyan.sccore.animation.service.IAnimationService;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class AnimationDataHelper {
    AnimationDataHelper() {}
    public static AnimationDataHelper getHelper() {
        return new AnimationDataHelper();
    }

    public Set<ResourceLocation> getAllAnimations() {
        Set<ResourceLocation> set = new HashSet<>(getAnimationNames());
        Set<ResourceLocation> strings = DistExecutor.unsafeCallWhenOn(Dist.CLIENT,
                () -> this::getAnimationsClient);
        if (strings != null && !strings.isEmpty()) set.addAll(strings);
        return set;
    }

    @OnlyIn(Dist.CLIENT)
    private Set<ResourceLocation> getAnimationsClient() {
        return new HashSet<>(RawAnimationRegistry.getAnimations().keySet());
    }

    private Set<ResourceLocation> getAnimationNames(){
        return new HashSet<>(AnimationRegistry.getAnimations().keySet());
    }

    public Set<ResourceLocation> getLayers() {
        return new HashSet<>(AnimationRegistry.getLayers().keySet());
    }

    public boolean isLayerPresent(ResourceLocation location) {
        return getLayers().contains(location);
    }

    public boolean isAnimationPresent(ResourceLocation location) {
        return getAllAnimations().contains(location);
    }

    public @Nullable AnimationData getAnimationData(ResourceLocation location) {
        IAnimationService<?, ?> helper = AnimationApi.getServiceGetterHelper(location).getService();
        if(helper == null) return null;
        return helper.getAnimation(location);
    }

    public AnimationData getDataByCompoundTag(CompoundTag tag) {
        AnimationData animationData = new AnimationData();
        animationData.deserializeNBT(tag);
        IAnimationService<?, ?> service = AnimationApi.getServiceGetterHelper(animationData.getKey()).getService();
        if(service == null) return animationData;
        return service.getAnimation(tag);
    }
}
