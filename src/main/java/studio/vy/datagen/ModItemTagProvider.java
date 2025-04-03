package studio.vy.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import studio.vy.item.ModItems;
import studio.vy.util.ModTags;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends FabricTagProvider.ItemTagProvider{
    public ModItemTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        getOrCreateTagBuilder(ModTags.Items.COPPER_REPAIR)
                .add(Items.COPPER_INGOT);
        getOrCreateTagBuilder(ModTags.Items.CRUSHABLE_ITEMS)
                .add(Items.GRAVEL)
                .add(Items.COBBLESTONE)
                .add(Items.GRANITE)
                .add(Items.ANDESITE)
                .add(Items.DIORITE)
                .add(Items.DEEPSLATE);
        getOrCreateTagBuilder(ItemTags.TRIMMABLE_ARMOR)
                .add(ModItems.COPPER_BOOTS)
                .add(ModItems.COPPER_LEGGINGS)
                .add(ModItems.COPPER_CHESTPLATE)
                .add(ModItems.COPPER_HELMET);
    }
}
