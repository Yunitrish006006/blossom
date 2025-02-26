package studio.vy.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import studio.vy.Blossom;

public class ModItems {

    public static final Item COOKED_PUFFERFISH = registerFood("cooked_pufferfish", 2,0.4f);
    public static final Item SMOKED_ROTTEN_FLESH = registerFood("smoked_rotten_flesh", 2,0.1f);

    public static Item registerNormalItem(String name) {
        return registerItem(name, new Item(new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Blossom.identifier(name)))));
    }

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
