package studio.vy.util;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import studio.vy.Blossom;

public class ModTags {
    public static class Blocks {
        private static TagKey<Block> createTag(String name) {
            return TagKey.of(RegistryKeys.BLOCK, Blossom.identifier(name));
        }

        public static final TagKey<Block> HAMMER_EFFICIENCY = createTag("hammer_efficiency");

        public static final TagKey<Block> HAMMER_MINEABLE = createTag("hammer_mineable");
    }
    public static class Items {

        public static final TagKey<Item> CRUSHABLE_ITEMS = createTag("crushable_items");

        public static final TagKey<Item> COPPER_REPAIR = createTag("copper_repair");

        public static final TagKey<Item> COPPER_INGOTS = createTag("copper_ingots");

        private static TagKey<Item> createTag(String name) {
            return TagKey.of(RegistryKeys.ITEM, Blossom.identifier(name));
        }
    }
}
