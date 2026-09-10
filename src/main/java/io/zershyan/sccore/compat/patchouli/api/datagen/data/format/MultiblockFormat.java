package io.zershyan.sccore.compat.patchouli.api.datagen.data.format;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MultiblockFormat implements IFormat {
    private static final Logger log = LoggerFactory.getLogger(MultiblockFormat.class);
    @Nullable
    private ResourceLocation id;
    private final Map<Character, BlockStateFormat> mapping = new HashMap<>();
    private final List<List<String>> pattern = new ArrayList<>();
    private Boolean symmetrical;
    private Vec3i offset;
    private Vec3i centerIndex;

    public MultiblockFormat(@Nullable ResourceLocation id) {
        this.id = id;
    }

    MultiblockFormat() {}

    public static MultiblockFormat create() {
        return new MultiblockFormat();
    }

    public MultiblockFormat mapping(char key, BlockStateFormat state) {
        mapping.put(key, state);
        return this;
    }

    public MultiblockFormat pattern(String ... patterns) {
        pattern.add(Arrays.asList(patterns));
        return this;
    }

    public MultiblockFormat symmetrical(Boolean symmetrical) {
        this.symmetrical = symmetrical;
        return this;
    }

    public MultiblockFormat offset(Vec3i offset) {
        this.offset = offset;
        return this;
    }

    public MultiblockFormat centerIndex(Vec3i centerIndex) {
        this.centerIndex = centerIndex;
        return this;
    }

    private boolean valid() {
        try {
            if(mapping.isEmpty()) throw new JsonParseException("No mapping!");
            if(pattern.isEmpty()) throw new JsonParseException("No pattern!");
            if(pattern.get(0).isEmpty()) throw new JsonParseException("Pattern cannot be empty!");
            StringBuilder regex = new StringBuilder("[ _0");
            for (Character c : mapping.keySet()) {
                regex.append(c.toString());
            }
            regex.append("]{");
            regex.append(pattern.get(0).get(0).length());
            regex.append("}");
            for (List<String> levelList : pattern) {
                if(levelList.isEmpty()) throw new JsonParseException("Pattern cannot be empty!");
                for (String patternString : levelList) {
                    if(!patternString.matches(regex.toString()))
                        throw new JsonParseException("Invalid pattern: " + patternString + "\nValid regex: " + regex);
                }
            }
            if(centerIndex != null) {
                int x = centerIndex.getX();
                if(pattern.size() <= x) throw new JsonParseException("center index of 'x' out of bounds!");
                List<String> levelList = pattern.get(x);
                int y = centerIndex.getY();
                if(levelList.size() <= y) throw new JsonParseException("center index of 'y' out of bounds!");
                String patternString = levelList.get(y);
                int z = centerIndex.getZ();
                if(patternString.length() <= z) throw new JsonParseException("center index of 'z' out of bounds!");
                char c = patternString.charAt(z);
                if(mapping.containsKey(c)) mapping.put('0', mapping.get(c));
                String prefix = patternString.substring(0, z);
                String suffix = patternString.substring(z + 1);
                levelList.set(y, prefix + "0" + suffix);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
        return true;
    }

    public String parse() {
        if(id == null) return null;
        return id.toString();
    }

    public JsonObject serialize() {
        if(id != null) return null;
        if(!valid()) throw new JsonParseException("Invalid multiblock format");
        JsonObject multiblock = new JsonObject();
        JsonObject mappingObject = new JsonObject();
        mapping.forEach((key, value) ->
                mappingObject.addProperty(key.toString(), value.parse())
        );
        multiblock.add("mapping", mappingObject);
        JsonArray patternArray = new JsonArray();
        for (List<String> patternList : pattern) {
            JsonArray innerArray = new JsonArray();
            for (String patternString : patternList) {
                innerArray.add(patternString);
            }
            patternArray.add(innerArray);
        }
        multiblock.add("pattern", patternArray);
        if(symmetrical != null) {
            multiblock.addProperty("symmetrical", symmetrical);
        }
        if(offset != null) {
            JsonArray offsetArray = new JsonArray();
            offsetArray.add(offset.getX());
            offsetArray.add(offset.getY());
            offsetArray.add(offset.getZ());
            multiblock.add("offset", offsetArray);
        }
        return multiblock;
    }

    public static class MultiblockObject {
        @Nullable
        private final JsonObject multiblock;
        @Nullable
        private final String multiblockId;
        public MultiblockObject(@NotNull JsonObject multiblock) {
            this.multiblock = multiblock;
            this.multiblockId = null;
        }
        public MultiblockObject(@NotNull String multiblockId) {
            this.multiblock = null;
            this.multiblockId = multiblockId;
        }

        public @Nullable JsonObject getMultiblock() {
            return multiblock;
        }

        public @Nullable String getMultiblockId() {
            return multiblockId;
        }

        public boolean hasId() {
            return multiblockId != null;
        }
    }
}
