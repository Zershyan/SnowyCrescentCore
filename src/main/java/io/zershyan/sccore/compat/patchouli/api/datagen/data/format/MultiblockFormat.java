package io.zershyan.sccore.compat.patchouli.api.datagen.data.format;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MultiblockFormat implements IFormat {
    private final Map<Character, BlockStateFormat> mapping = new HashMap<>();
    private final List<List<String>> pattern = new ArrayList<>();
    @Nullable
    private ResourceLocation id;
    private Boolean symmetrical;
    private Vec3i offset;
    private Vec3i centerIndex;

    public MultiblockFormat(@Nullable ResourceLocation id) {
        this.id = id;
    }

    MultiblockFormat() {
    }

    public static MultiblockFormat create() {
        return new MultiblockFormat();
    }

    public MultiblockFormat mapping(char key, BlockStateFormat state) {
        mapping.put(key, state);
        return this;
    }

    public MultiblockFormat pattern(String... patterns) {
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

    /**
     * 校验当前状态，不做任何改写，因此同一个实例可以反复序列化。
     *
     * @throws JsonParseException 映射、图案或中心点不合法
     */
    private void validate() {
        if (mapping.isEmpty()) throw new JsonParseException("No mapping!");
        if (pattern.isEmpty()) throw new JsonParseException("No pattern!");
        if (pattern.get(0).isEmpty()) throw new JsonParseException("Pattern cannot be empty!");
        StringBuilder regex = new StringBuilder("[ _0");
        for (Character c : mapping.keySet()) {
            regex.append(c.toString());
        }
        regex.append("]{");
        regex.append(pattern.get(0).get(0).length());
        regex.append("}");
        for (List<String> levelList : pattern) {
            if (levelList.isEmpty()) throw new JsonParseException("Pattern cannot be empty!");
            for (String patternString : levelList) {
                if (!patternString.matches(regex.toString()))
                    throw new JsonParseException("Invalid pattern: " + patternString + "\nValid regex: " + regex);
            }
        }
        if (centerIndex == null) return;
        int x = centerIndex.getX();
        if (pattern.size() <= x) throw new JsonParseException("center index of 'x' out of bounds!");
        List<String> levelList = pattern.get(x);
        int y = centerIndex.getY();
        if (levelList.size() <= y) throw new JsonParseException("center index of 'y' out of bounds!");
        String patternString = levelList.get(y);
        int z = centerIndex.getZ();
        if (patternString.length() <= z) throw new JsonParseException("center index of 'z' out of bounds!");
    }

    public String parse() {
        if (id == null) return null;
        return id.toString();
    }

    public JsonObject serialize() {
        if (id != null) return null;
        validate();
        Map<Character, BlockStateFormat> outputMapping = new HashMap<>(mapping);
        List<List<String>> outputPattern = new ArrayList<>(pattern.size());
        for (List<String> levelList : pattern) {
            outputPattern.add(new ArrayList<>(levelList));
        }
        applyCenter(outputMapping, outputPattern);

        JsonObject multiblock = new JsonObject();
        JsonObject mappingObject = new JsonObject();
        outputMapping.forEach((key, value) ->
                mappingObject.addProperty(key.toString(), value.parse())
        );
        multiblock.add("mapping", mappingObject);
        JsonArray patternArray = new JsonArray();
        for (List<String> patternList : outputPattern) {
            JsonArray innerArray = new JsonArray();
            for (String patternString : patternList) {
                innerArray.add(patternString);
            }
            patternArray.add(innerArray);
        }
        multiblock.add("pattern", patternArray);
        if (symmetrical != null) {
            multiblock.addProperty("symmetrical", symmetrical);
        }
        if (offset != null) {
            JsonArray offsetArray = new JsonArray();
            offsetArray.add(offset.getX());
            offsetArray.add(offset.getY());
            offsetArray.add(offset.getZ());
            multiblock.add("offset", offsetArray);
        }
        return multiblock;
    }

    /**
     * 把中心点所在字符改写为 Patchouli 的中心标记 {@code '0'}，只作用于输出副本。
     *
     * @param outputMapping 输出的映射副本
     * @param outputPattern 输出的图案副本
     */
    private void applyCenter(Map<Character, BlockStateFormat> outputMapping, List<List<String>> outputPattern) {
        if (centerIndex == null) return;
        List<String> levelList = outputPattern.get(centerIndex.getX());
        int y = centerIndex.getY();
        int z = centerIndex.getZ();
        String patternString = levelList.get(y);
        char center = patternString.charAt(z);
        BlockStateFormat state = centerState(center);
        if (state != null) outputMapping.put('0', state);
        levelList.set(y, patternString.substring(0, z) + "0" + patternString.substring(z + 1));
    }

    /**
     * 取中心点字符对应的方块状态：显式映射优先，未映射的 {@code '_'} 是任意方块，
     * 空格与 {@code '0'} 由 Patchouli 默认视作空气，无需写出。
     *
     * @param center 中心点原本的字符
     * @return 需要写入映射的状态，无需额外写出时返回 {@code null}
     */
    @Nullable
    private BlockStateFormat centerState(char center) {
        BlockStateFormat state = mapping.get(center);
        if (state != null) return state;
        return center == '_' ? BlockStateFormat.any() : null;
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
