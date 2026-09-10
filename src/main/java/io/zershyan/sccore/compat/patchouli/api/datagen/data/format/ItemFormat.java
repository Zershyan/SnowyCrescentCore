package io.zershyan.sccore.compat.patchouli.api.datagen.data.format;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemFormat implements IFormat {
    private final String item;
    private int count;
    private boolean isTag = false;

    ItemFormat(String item) {
        this.item = item;
    }

    public static ItemFormat tagOf(TagKey<Item> tagKey) {
        return tagOf(tagKey.location());
    }

    public static ItemFormat tagOf(ResourceLocation tagKey) {
        ItemFormat itemFormat = new ItemFormat(tagKey.toString());
        itemFormat.isTag = true;
        return itemFormat;
    }

    public static ItemFormat of(ResourceLocation id) {
        return new ItemFormat(id.toString());
    }

    public static ItemFormat of(Item item) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKeyOrNull(item);
        if (key == null) throw new RuntimeException("Item " + item + " has no key");
        return new ItemFormat(key.toString());
    }

    public static ItemFormat of(ItemStack stack) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKeyOrNull(stack.getItem());
        if (key == null) throw new RuntimeException("Item " + stack.getItem() + " has no key");
        ItemFormat itemFormat = of(key);
        itemFormat.count = stack.getCount();
        return itemFormat;
    }

    public ItemFormat count(int count) {
        this.count = count;
        return this;
    }

    public String parse() {
        StringBuilder sb = new StringBuilder();
        if (isTag) {
            sb.append("tag:");
            sb.append(item);
        } else {
            sb.append(item);
            if (count > 1) sb.append("#").append(count);
        }
        return sb.toString();
    }

    public static class Multi implements IFormat {
        private final List<ItemFormat> itemFormats = new ArrayList<>();

        Multi() {
        }

        @SafeVarargs
        public static Multi tagOf(TagKey<Item>... tagKeys) {
            Multi itemFormat = new Multi();
            for (TagKey<Item> tagKey : tagKeys) {
                itemFormat.itemFormats.add(ItemFormat.tagOf(tagKey));
            }
            return itemFormat;
        }

        public static Multi tagOf(ResourceLocation... rls) {
            Multi itemFormat = new Multi();
            for (ResourceLocation rl : rls) {
                itemFormat.itemFormats.add(ItemFormat.tagOf(rl));
            }
            return itemFormat;
        }

        public static Multi of(ItemFormat... formats) {
            Multi itemFormat = new Multi();
            itemFormat.itemFormats.addAll(Arrays.asList(formats));
            return itemFormat;
        }

        public static Multi of(ResourceLocation... rls) {
            Multi itemFormat = new Multi();
            for (ResourceLocation rl : rls) {
                itemFormat.itemFormats.add(ItemFormat.of(rl));
            }
            return itemFormat;
        }

        public static Multi of(Item... items) {
            Multi itemFormat = new Multi();
            for (Item item : items) {
                itemFormat.itemFormats.add(ItemFormat.of(item));
            }
            return itemFormat;
        }

        public static Multi of(ItemStack... itemStacks) {
            Multi itemFormat = new Multi();
            for (ItemStack stack : itemStacks) {
                itemFormat.itemFormats.add(ItemFormat.of(stack));
            }
            return itemFormat;
        }

        public void add(Item item) {
            itemFormats.add(ItemFormat.of(item));
        }

        public void add(ItemStack stack) {
            itemFormats.add(ItemFormat.of(stack));
        }

        public void add(ItemFormat format) {
            itemFormats.add(format);
        }

        public void add(ResourceLocation rl) {
            itemFormats.add(ItemFormat.of(rl));
        }

        public List<ItemFormat> getItemFormats() {
            return itemFormats;
        }

        public String parse() {
            StringBuilder builder = new StringBuilder();
            for (ItemFormat itemFormat : itemFormats) {
                builder.append(itemFormat.parse());
                builder.append(",");
            }
            builder.deleteCharAt(builder.length() - 1);
            return builder.toString();
        }
    }
}
