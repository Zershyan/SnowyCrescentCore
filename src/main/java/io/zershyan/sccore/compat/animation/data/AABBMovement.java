package io.zershyan.sccore.compat.animation.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.TreeMap;
import java.util.function.Function;

public class AABBMovement {
    public static final Codec<AABBMovement> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.BOOL.optionalFieldOf("relative", true).forGetter(AABBMovement::isRelative),
            Codec.unboundedMap(Codec.STRING.xmap(Integer::parseInt, Object::toString), Vec3.CODEC.listOf(2, 2).xmap(
                    vec3s -> new AABB(vec3s.getFirst(), vec3s.getLast()),
                    ab -> List.of(new Vec3(ab.minX, ab.minY, ab.minZ), new Vec3(ab.maxX, ab.maxY, ab.maxZ))
            )).xmap(TreeMap::new, Function.identity()).optionalFieldOf("aabbMovement", new TreeMap<>()).forGetter(AABBMovement::getMovementTree)
    ).apply(i, AABBMovement::of));
    private final TreeMap<Integer, AABB> movementTree = new TreeMap<>();
    private boolean relative = true;

    public AABBMovement() {}

    public AABBMovement(boolean relative) {
        this.relative = relative;
    }

    public static AABBMovement of(boolean relative, TreeMap<Integer, AABB> aabbMovement) {
        AABBMovement movement = new AABBMovement(relative);
        movement.getMovementTree().putAll(aabbMovement);
        return movement;
    }

    public boolean isRelative() {
        return relative;
    }

    public AABBMovement relative(boolean relative) {
        this.relative = relative;
        return this;
    }

    public TreeMap<Integer, AABB> getMovementTree() {
        return movementTree;
    }

    public AABBMovement add(int tick, AABB aabb) {
        movementTree.put(tick, aabb);
        return this;
    }

    public AABBMovement remove(int tick) {
        movementTree.remove(tick);
        return this;
    }

    public boolean isEmpty() {
        return movementTree.isEmpty();
    }
}
