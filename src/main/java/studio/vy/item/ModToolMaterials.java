package studio.vy.item;

import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.tag.BlockTags;
import studio.vy.util.ModTags;

public class ModToolMaterials {
    public static final ToolMaterial COPPER = new ToolMaterial(
            BlockTags.INCORRECT_FOR_IRON_TOOL,
            250,
            6.0F,
            2.0F,
            12,
            ModTags.Items.COPPER_INGOTS
    );
}
