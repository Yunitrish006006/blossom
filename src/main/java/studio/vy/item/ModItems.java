package studio.vy.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Block;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import studio.vy.Blossom;
import studio.vy.block.ModBlocks;

import java.util.function.Function;

public class ModItems {

    public static final Item COOKED_PUFFERFISH = registerFood("cooked_pufferfish", 2,0.4f);
    public static final Item SMOKED_ROTTEN_FLESH = registerFood("smoked_rotten_flesh", 2,0.1f);

    public static final Item COPPER_HELMET = register("copper_helmet", (new Item.Settings()).armor(ModArmorMaterials.COPPER, EquipmentType.HELMET));
    public static final Item COPPER_CHESTPLATE = register("copper_chestplate", (new Item.Settings()).armor(ModArmorMaterials.COPPER, EquipmentType.CHESTPLATE));
    public static final Item COPPER_LEGGINGS = register("copper_helmet", (new Item.Settings()).armor(ModArmorMaterials.COPPER, EquipmentType.LEGGINGS));
    public static final Item COPPER_BOOTS = register("copper_boots", (new Item.Settings()).armor(ModArmorMaterials.COPPER, EquipmentType.BOOTS));

    public static final Item GRAVEL_IRON_ORE = registerBlockItem("gravel_iron_ore", ModBlocks.GRAVEL_IRON_ORE);

    private static Item registerBlockItem(String name, Block block) {
        return Registry.register(Registries.ITEM, Blossom.identifier(name), new BlockItem(block, new Item.Settings()
                .registryKey(RegistryKey.of(RegistryKeys.ITEM, Blossom.identifier(name)))
                .useBlockPrefixedTranslationKey()));
    }

    public static Item registerNormalItem(String name) {
        return registerItem(name, new Item(new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Blossom.identifier(name)))));
    }
/**/
    public static Item register(String id, Item.Settings settings) {
        return register(keyOf(id), Item::new, settings);
    }

    private static RegistryKey<Item> keyOf(String id) {
        return RegistryKey.of(RegistryKeys.ITEM, Blossom.identifier(id));
    }

    public static Item register(RegistryKey<Item> key, Function<Item.Settings, Item> factory, Item.Settings settings) {
        Item item = (Item)factory.apply(settings.registryKey(key));
        if (item instanceof BlockItem blockItem) {
            blockItem.appendBlocks(Item.BLOCK_ITEMS, item);
        }

        return (Item)Registry.register(Registries.ITEM, key, item);
    }
/**/

    public static Item registerFood(String name, int hunger, float saturation) {
        return registerItem(name, new Item(new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Blossom.identifier(name))).food(new FoodComponent.Builder().nutrition(hunger).saturationModifier(saturation).build())));
    }

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(Blossom.MOD_ID, name), item);
    }
    public static void registerModItems() {
        Blossom.LOGGER.info("registering mod items");

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(entries -> {
            entries.add(COOKED_PUFFERFISH);
            entries.add(SMOKED_ROTTEN_FLESH);
        });
    }
}
