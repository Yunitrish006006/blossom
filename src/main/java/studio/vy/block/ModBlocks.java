package studio.vy.block;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.ExperienceDroppingBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import studio.vy.Blossom;

public class ModBlocks {

    public static final Block FISHNET = registerBlock("fishnet",
            new Block(AbstractBlock.Settings.create()
                    .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Blossom.identifier("fishnet")))
                    .strength(4f)
                    .requiresTool()
                    .liquid()
                    .nonOpaque()
                    .sounds(BlockSoundGroup.WOOL)
            ));

    public static final Block TRASH_CAN = registerBlock("trash_can",
            new Block(AbstractBlock.Settings.create()
                    .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Blossom.identifier("trash_can")))
                    .strength(2f)
                    .requiresTool()
                    .sounds(BlockSoundGroup.METAL)
            ));


    public static final Block gravel_iron_ore = registerBlock("gravel_iron_ore",
            new ExperienceDroppingBlock(
                    UniformIntProvider.create(0, 2),
                    AbstractBlock.Settings.create()
                    .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Blossom.identifier("gravel_iron_ore")))
                    .strength(2.4f)
                    .requiresTool()
                    .sounds(BlockSoundGroup.GRAVEL)
            ));

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, Blossom.identifier(name), block);
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(Registries.ITEM, Blossom.identifier(name), new BlockItem(block, new Item.Settings()
                .registryKey(RegistryKey.of(RegistryKeys.ITEM, Blossom.identifier(name)))
                .useBlockPrefixedTranslationKey()));
    }

    public static void registerModBlocks() {
        Blossom.LOGGER.info("Registering blocks");

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries ->  {
            entries.add(ModBlocks.FISHNET);
            entries.add(ModBlocks.TRASH_CAN);
            entries.add(ModBlocks.gravel_iron_ore);
        });
    }
}
