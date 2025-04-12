package studio.vy.item;

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import studio.vy.util.ModTags;

public class HammerItem extends Item {
    private final ToolMaterial toolMaterial;
    private final int attackDamage;
    private final float attackSpeed;

    /**
     * HammerItem 建構子
     * @param toolMaterial 用於取得工具基礎效率（例如 efficiency 數值）
     * @param attackDamage   攻擊力（可透過屬性修正器附加）
     * @param attackSpeed    攻擊速度（可透過屬性修正器附加）
     * @param settings       物品的基本設定，建議使用 Settings.tool(...) 來註冊基本工具行為
     */
    public HammerItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings) {
        super(settings);
        this.toolMaterial = toolMaterial;
        this.attackDamage = attackDamage;
        this.attackSpeed = attackSpeed;
    }

    @Override
    public float getMiningSpeed(ItemStack stack, BlockState state) {
        float baseSpeed = toolMaterial.speed();
        if (state.isIn(ModTags.Blocks.HAMMER_MINEABLE)) {
            if (state.isIn(ModTags.Blocks.HAMMER_EFFICIENCY)) {
                return baseSpeed * 1.5F;
            }
            return baseSpeed * 0.8F;
        }
        return baseSpeed * 0.5F;
    }

    @Override
    public boolean isCorrectForDrops(ItemStack stack, BlockState state) {
        return state.isIn(ModTags.Blocks.HAMMER_MINEABLE);
    }
}
