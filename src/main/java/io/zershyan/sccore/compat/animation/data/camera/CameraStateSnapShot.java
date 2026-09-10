package io.zershyan.sccore.compat.animation.data.camera;

import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import io.zershyan.sccore.compat.animation.data.ClientAnimation;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

public class CameraStateSnapShot {
    private @Nullable ClientAnimation curAnimation;
    private @Nullable KeyframeAnimationPlayer curKeyframeAnimation;
    private @Nullable CameraData cacheFirst;
    private @Nullable CameraData cacheThird;

    public CameraStateSnapShot() {
        this.curAnimation = null;
        this.curKeyframeAnimation = null;
        this.cacheFirst = null;
        this.cacheThird = null;
    }

    public @Nullable ClientAnimation getCurAnimation() {
        return curAnimation;
    }

    public void setCurAnimation(@Nullable ClientAnimation curAnimation) {
        this.curAnimation = curAnimation;
    }

    public @Nullable KeyframeAnimationPlayer getCurKeyframeAnimation() {
        return curKeyframeAnimation;
    }

    public void setCurKeyframeAnimation(@Nullable KeyframeAnimationPlayer curKeyframeAnimation) {
        this.curKeyframeAnimation = curKeyframeAnimation;
    }

    public void clearAnimation() {
        this.curAnimation = null;
        this.curKeyframeAnimation = null;
    }

    public @Nullable CameraData getCache(boolean firstPerson) {
        return firstPerson ? cacheFirst : cacheThird;
    }

    public @Nullable CameraData get(float partialTick, boolean firstPerson) {
        CameraData cache = firstPerson ? cacheFirst : cacheThird;
        if (curAnimation == null) curKeyframeAnimation = null;
        if (curKeyframeAnimation == null && CameraData.isOrNearEmpty(cache)) {
            curAnimation = null;
            return firstPerson ? (cacheFirst = null) : (cacheThird = null);
        }
        CameraData absData = calAbsolutionCurrentData(partialTick, firstPerson);
        if (absData == null) absData = CameraData.ZERO;
        if (cache == null) cache = absData;

        float deltaTicks = Minecraft.getInstance().getTimer().getGameTimeDeltaTicks();
        float delta = deltaTicks / 5.0F;
        if (delta == 0.0F) delta = 0.0022857143F;
        CameraData sample = cache.sample(delta, absData);
        return firstPerson ? (cacheFirst = sample) : (cacheThird = sample);
    }

    public @Nullable CameraData calAbsolutionCurrentData(float partialTick, boolean firstPerson) {
        if (curKeyframeAnimation == null) return null;
        CameraChange cameraChange = getCameraChange(firstPerson);
        if (cameraChange == null) return null;
        return cameraChange.sample(curKeyframeAnimation.getCurrentTick() + partialTick);
    }

    public boolean relativeEuler(boolean firstPerson) {
        if (curKeyframeAnimation == null) return true;
        CameraChange cameraChange = getCameraChange(firstPerson);
        if (cameraChange == null) return true;
        return cameraChange.relativeEuler();
    }

    private @Nullable CameraChange getCameraChange(boolean firstPerson) {
        if (curAnimation == null) return null;
        if (curKeyframeAnimation == null) return null;
        if (!curAnimation.hasCameraChange()) return null;
        return firstPerson ? curAnimation.firstPersonCameraChange() : curAnimation.cameraChange();
    }
}
