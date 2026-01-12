package io.zershyan.sccore.animation.event.create;

import io.zershyan.sccore.animation.data.AnimationData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.LogicalSide;
import org.jetbrains.annotations.Nullable;

@Cancelable
@Event.HasResult
public class AnimationEvent extends Event {
    public final LogicalSide side;

    AnimationEvent(LogicalSide side) {
        this.side = side;
    }

    public enum Type {
        APPLY, INVITE, REQUEST
    }

    public static class Send extends AnimationEvent {
        private int validTick;
        private int cooldownTick;
        public final Type type;
        public Send(LogicalSide side, int validTick, int cooldownTick, Type type) {
            super(side);
            this.validTick = validTick;
            this.cooldownTick = cooldownTick;
            this.type = type;
        }

        public int getValidTick() {
            return validTick;
        }
        public int getCooldownTick() {
            return cooldownTick;
        }

        public void setValidTick(int validTick) {
            this.validTick = validTick;
        }
        public void setCooldownTick(int cooldownTick) {
            this.cooldownTick = cooldownTick;
        }
    }

    public static class Accept extends AnimationEvent {
        private int validDistance;
        public final Type type;
        public Accept(Type type) {
            super(LogicalSide.SERVER);
            this.type = type;
        }
        public Accept(Type type, int validDistance) {
            this(type);
            this.validDistance = validDistance;
        }

        public int getValidDistance() {
            return validDistance;
        }
        public void setValidDistance(int validDistance) {
            this.validDistance = validDistance;
        }
    }

    public static class Play extends AnimationEvent {
        private final @Nullable Player player;
        private final ResourceLocation layer;
        private final AnimationData animation;
        public Play(LogicalSide side, @Nullable Player player, ResourceLocation layer, AnimationData animation) {
            super(side);
            this.player = player;
            this.layer = layer;
            this.animation = animation;
        }

        public @Nullable Player getPlayer() {
            return player;
        }
        public ResourceLocation getLayer() {
            return layer;
        }
        public AnimationData getAnimation() {
            return animation;
        }
    }

    public static class Join extends AnimationEvent {
        private final Player player;
        private final Player target;
        private boolean force;
        public Join(Player player, Player target, boolean force) {
            super(LogicalSide.SERVER);
            this.player = player;
            this.target = target;
            this.force = force;
        }

        public Player getPlayer() {
            return player;
        }
        public Player getTarget() {
            return target;
        }
        public boolean isForce() {
            return force;
        }

        public void setForce(boolean force) {
            this.force = force;
        }
    }
}
