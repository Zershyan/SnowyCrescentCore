package io.zershyan.sccore.compat.patchouli.api.datagen.data.format;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class BlockStateFormat implements IFormat {
    private final String block;
    private final Map<Property<?>, String> property = new HashMap<>();
    private boolean isTag = false;
    BlockStateFormat(String block) {
        this.block = block;
    }

    public static BlockStateFormat tagOf(TagKey<Block> tagKey) {
        return tagOf(tagKey.location());
    }

    public static BlockStateFormat tagOf(ResourceLocation tagKey) {
        BlockStateFormat stateFormat = new BlockStateFormat(tagKey.toString());
        stateFormat.isTag = true;
        return stateFormat;
    }

    public static BlockStateFormat of(ResourceLocation id) {
        return new BlockStateFormat(id.toString());
    }

    public static BlockStateFormat of(Block block) {
        ResourceLocation key = BuiltInRegistries.BLOCK.getKeyOrNull(block);
        if(key == null) throw new RuntimeException("Block  " + block + " has no key");
        return of(key);
    }

    public static BlockStateFormat of(BlockState state) {
        BlockStateFormat stateFormat = of(state.getBlock());
        return stateFormat.property(state.getValues());
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> String getName(Property<T> property, Comparable<?> comparable) {
        return property.getName((T)comparable);
    }

    public BlockStateFormat property(Map<Property<?>, Comparable<?>> property) {
        this.property.clear();
        property.forEach((p, c) -> this.property.put(p, getName(p, c)));
        return this;
    }

    public BlockStateFormat addProperty(Property<?> property, String value) {
        this.property.put(property, value);
        return this;
    }

    public String parse() {
        StringBuilder sb = new StringBuilder(isTag ? "#" : "");
        sb.append(block);
        if(!isTag && !property.isEmpty()) {
            sb.append('[');
            sb.append(property.entrySet().stream().map(entry ->
                    entry.getKey().getName() + "=" + entry.getValue()
            ).collect(Collectors.joining(",")));
            sb.append(']');
        }
        return sb.toString();
    }
}
