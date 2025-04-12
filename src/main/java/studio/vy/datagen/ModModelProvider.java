package studio.vy.datagen;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.data.BlockStateModelGenerator;
import net.minecraft.client.data.ItemModelGenerator;
import net.minecraft.client.data.Models;
import studio.vy.block.ModBlocks;
import studio.vy.item.ModEquipmentAssetKeys;
import studio.vy.item.ModItems;

public class ModModelProvider extends FabricModelProvider {
    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        blockStateModelGenerator.registerSimpleCubeAll(ModBlocks.GRAVEL_IRON_ORE);
        blockStateModelGenerator.registerSimpleCubeAll(ModBlocks.FISHNET);
        blockStateModelGenerator.registerSimpleCubeAll(ModBlocks.TRASH_CAN);
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        itemModelGenerator.register(ModItems.COOKED_PUFFERFISH, Models.GENERATED);
        itemModelGenerator.register(ModItems.SMOKED_ROTTEN_FLESH, Models.GENERATED);

        itemModelGenerator.registerArmor(ModItems.COPPER_BOOTS, ModEquipmentAssetKeys.COPPER,ItemModelGenerator.BOOTS_TRIM_ID_PREFIX, false);
        itemModelGenerator.registerArmor(ModItems.COPPER_LEGGINGS, ModEquipmentAssetKeys.COPPER,ItemModelGenerator.LEGGINGS_TRIM_ID_PREFIX, false);
        itemModelGenerator.registerArmor(ModItems.COPPER_CHESTPLATE, ModEquipmentAssetKeys.COPPER,ItemModelGenerator.CHESTPLATE_TRIM_ID_PREFIX, false);
        itemModelGenerator.registerArmor(ModItems.COPPER_HELMET, ModEquipmentAssetKeys.COPPER,ItemModelGenerator.HELMET_TRIM_ID_PREFIX, false);

        itemModelGenerator.register(ModItems.WOODEN_HAMMER, Models.GENERATED);
        itemModelGenerator.register(ModItems.STONE_HAMMER, Models.GENERATED);
        itemModelGenerator.register(ModItems.IRON_HAMMER, Models.GENERATED);
        itemModelGenerator.register(ModItems.COPPER_HAMMER, Models.GENERATED);
        itemModelGenerator.register(ModItems.GOLDEN_HAMMER, Models.GENERATED);
        itemModelGenerator.register(ModItems.DIAMOND_HAMMER, Models.GENERATED);
        itemModelGenerator.register(ModItems.NETHERITE_HAMMER, Models.GENERATED);
    }
}
