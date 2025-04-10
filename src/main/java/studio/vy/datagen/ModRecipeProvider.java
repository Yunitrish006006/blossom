package studio.vy.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.block.Blocks;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SmokingRecipe;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import studio.vy.Blossom;
import studio.vy.item.ModItems;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected RecipeGenerator getRecipeGenerator(RegistryWrapper.WrapperLookup wrapperLookup, RecipeExporter recipeExporter) {
        return new RecipeGenerator(wrapperLookup, recipeExporter) {
            @Override
            public void generate() {
                List<ItemConvertible> SMELTABLE = List.of(ModItems.GRAVEL_IRON_ORE);

                offerSmelting(SMELTABLE, RecipeCategory.MISC, Items.IRON_INGOT, 0.25f, 200, "gravel_iron_ore");
                offerBlasting(SMELTABLE, RecipeCategory.MISC, Items.IRON_INGOT, 0.25f, 100, "gravel_iron_ore");

                offerFoodCookingRecipe("smoker",
                        RecipeSerializer.SMOKING,
                        SmokingRecipe::new,
                        100,
                        Items.PUFFERFISH,
                        ModItems.COOKED_PUFFERFISH,
                        0.35f);

                offerFoodCookingRecipe("smoker",
                        RecipeSerializer.SMOKING,
                        SmokingRecipe::new,
                        100,
                        Items.ROTTEN_FLESH,
                        ModItems.SMOKED_ROTTEN_FLESH,
                        0.35f);
                offerArmorRecipe(Items.COPPER_INGOT, "armor_copper", ModItems.COPPER_HELMET, ModItems.COPPER_CHESTPLATE, ModItems.COPPER_LEGGINGS, ModItems.COPPER_BOOTS);
            }
            public void offerArmorRecipe(ItemConvertible input, String group, ItemConvertible helmet, ItemConvertible chestplate, ItemConvertible leggings, ItemConvertible boots) {
                this.createShaped(RecipeCategory.COMBAT,helmet)
                        .input('#', input)
                        .pattern("###")
                        .pattern("# #")
                        .group(group)
                        .criterion(hasItem(input), this.conditionsFromItem(input))
                        .offerTo(this.exporter);
                this.createShaped(RecipeCategory.COMBAT, chestplate)
                        .input('#', input)
                        .pattern("# #")
                        .pattern("###")
                        .pattern("###")
                        .group(group)
                        .criterion(hasItem(input), this.conditionsFromItem(input))
                        .offerTo(this.exporter);
                this.createShaped(RecipeCategory.COMBAT, leggings)
                        .input('#', input)
                        .pattern("###")
                        .pattern("# #")
                        .pattern("# #")
                        .group(group)
                        .criterion(hasItem(input), this.conditionsFromItem(input))
                        .offerTo(this.exporter);
                this.createShaped(RecipeCategory.COMBAT, boots)
                        .input('#', input)
                        .pattern("# #")
                        .pattern("# #")
                        .group(group)
                        .criterion(hasItem(input), this.conditionsFromItem(input))
                        .offerTo(this.exporter);
            }
        };
    }


    @Override
    public String getName() {
        return Blossom.MOD_ID;
    }
}
