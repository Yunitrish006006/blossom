package studio.vy.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import studio.vy.block.ModBlocks;
import studio.vy.util.ModTags;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public ModBlockTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        getOrCreateTagBuilder(BlockTags.SHOVEL_MINEABLE)
                .add(ModBlocks.GRAVEL_IRON_ORE);
        getOrCreateTagBuilder(BlockTags.IRON_ORES)
                .add(ModBlocks.GRAVEL_IRON_ORE);
        getOrCreateTagBuilder(BlockTags.NEEDS_STONE_TOOL)
                .add(ModBlocks.GRAVEL_IRON_ORE);

        getOrCreateTagBuilder(ModTags.Blocks.HAMMER_EFFICIENCY)
                .addOptionalTag(BlockTags.PLANKS)
                .addOptionalTag(BlockTags.STONE_BRICKS)
                .addOptionalTag(BlockTags.WALLS);

        getOrCreateTagBuilder(ModTags.Blocks.HAMMER_MINEABLE)
                .addOptionalTag(BlockTags.AXE_MINEABLE)
                .addOptionalTag(BlockTags.PICKAXE_MINEABLE)
                .addOptionalTag(BlockTags.DIRT)
                .addOptionalTag(ModTags.Blocks.HAMMER_EFFICIENCY);

        getOrCreateTagBuilder(ModTags.Blocks.HAMMER_DRAGGABLE)
                .add(Blocks.CHEST)
                .add(Blocks.BARREL)
                .add(Blocks.TRAPPED_CHEST)
                .add(Blocks.ENDER_CHEST)
                .add(Blocks.FURNACE)
                .add(Blocks.BLAST_FURNACE)
                .add(Blocks.SMOKER);
    }
}
