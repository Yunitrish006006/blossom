package studio.vy.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import studio.vy.Blossom;
import studio.vy.block.ModBlocks;

public class ModItemGroups {

    public static final ItemGroup BLOSSOM = Registry.register(
            Registries.ITEM_GROUP,
            Blossom.identifier("blossom_item_group"),
            FabricItemGroup
                    .builder()
                    .icon(() -> new ItemStack(ModItems.SMOKED_ROTTEN_FLESH))
                    .displayName(Text.translatable("itemGroup.blossom.title"))
                    .entries(((displayContext, entries) -> {
                        entries.add(ModItems.SMOKED_ROTTEN_FLESH);
                        entries.add(ModItems.COOKED_PUFFERFISH);
                        entries.add(ModBlocks.FISHNET);
                        entries.add(ModBlocks.TRASH_CAN);
                    }))
                    .build());

    public static void registerModItemGroups() {
        Blossom.LOGGER.info("Registering item groups");
    }
}
