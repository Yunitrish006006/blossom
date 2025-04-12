package studio.vy.item.custom;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ToolMaterial;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import studio.vy.util.ModTags;

public class HammerItem extends Item {
    private final ToolMaterial toolMaterial;
    private BlockPos begin;
    private BlockPos end;
    private final int attackDamage;
    private final float attackSpeed;

    /**
     * HammerItem 建構子
     *
     * @param toolMaterial 用於取得工具基礎效率（例如 efficiency 數值）
     * @param attackDamage 攻擊力（可透過屬性修正器附加）
     * @param attackSpeed  攻擊速度（可透過屬性修正器附加）
     * @param settings     物品的基本設定，建議使用 Settings.tool(...) 來註冊基本工具行為
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

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        if (!(miner instanceof PlayerEntity player)) {
            return false;
        }

        if (!world.isClient && begin != null && end != null) {
            // 確保 begin 的座標小於等於 end
            BlockPos min = new BlockPos(
                    Math.min(begin.getX(), end.getX()),
                    Math.min(begin.getY(), end.getY()),
                    Math.min(begin.getZ(), end.getZ())
            );
            BlockPos max = new BlockPos(
                    Math.max(begin.getX(), end.getX()),
                    Math.max(begin.getY(), end.getY()),
                    Math.max(begin.getZ(), end.getZ())
            );

            // 遍歷區域內所有方塊
            for (int x = min.getX(); x <= max.getX(); x++) {
                for (int y = min.getY(); y <= max.getY(); y++) {
                    for (int z = min.getZ(); z <= max.getZ(); z++) {
                        BlockPos currentPos = new BlockPos(x, y, z);
                        BlockState currentState = world.getBlockState(currentPos);

                        if (isCorrectForDrops(stack, currentState)) {
                            world.breakBlock(currentPos, true, player);
                            stack.damage(1, player, EquipmentSlot.MAINHAND);

                            // 如果工具耐久度耗盡則停止
                            if (stack.getDamage()+1 >= stack.getMaxDamage()) {
                                return true;
                            }
                        }
                    }
                }
            }

            // 清除起始和結束點
            begin = null;
            end = null;
        }

        return true;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        BlockState blockState = world.getBlockState(blockPos);
        PlayerEntity player = context.getPlayer();

        if (player == null || !blockState.isIn(ModTags.Blocks.HAMMER_MINEABLE)) {
            return ActionResult.PASS;
        }

        if (world.isClient) return ActionResult.PASS;

        if (!player.isSneaking()) {
            end = blockPos;
            player.sendMessage(getPosition(), true);
            return ActionResult.PASS;
        }
        else {
            begin = blockPos;
            player.sendMessage(getPosition(), true);
            return ActionResult.SUCCESS;
        }
    }

    private Text getPosition() {
        if (begin == null) {
            return Text.empty()
                    .append("已選擇區域起點: ")
                    .append(getBlockPosString(end));
        }
        if (end == null) {
            return Text.empty()
                    .append("已選擇區域終點: ")
                    .append(getBlockPosString(begin));
        }
        return Text.empty()
                .append("已選擇區域: ")
                .append(getBlockPosString(begin))
                .append(" 到 ")
                .append(getBlockPosString(end));
    }

    private Text getBlockPosString(BlockPos pos) {
        return Text.empty()
                .append(Text.literal(String.valueOf(pos.getX())).styled(style -> style.withColor(0xCC0000)))
                .append(" ")
                .append(Text.literal(String.valueOf(pos.getY())).styled(style -> style.withColor(0x00CC00)))
                .append(" ")
                .append(Text.literal(String.valueOf(pos.getZ())).styled(style -> style.withColor(0x0000CC)));
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, world, entity, slot);
        if (slot == null || !(entity instanceof PlayerEntity) || world.isClient() || begin == null) {
            return;
        }

        // 如果只有起始點，顯示單個方塊的邊框
        if (end == null) {
            spawnBlockOutlineParticles(world, begin);
            return;
        }

        // 計算區域範圍
        BlockPos min = new BlockPos(
                Math.min(begin.getX(), end.getX()),
                Math.min(begin.getY(), end.getY()),
                Math.min(begin.getZ(), end.getZ())
        );
        BlockPos max = new BlockPos(
                Math.max(begin.getX(), end.getX()),
                Math.max(begin.getY(), end.getY()),
                Math.max(begin.getZ(), end.getZ())
        );

        // 生成區域邊框粒子
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    if (x == min.getX() || x == max.getX() ||
                            y == min.getY() || y == max.getY() ||
                            z == min.getZ() || z == max.getZ()) {
                        world.addParticleClient(
                                ParticleTypes.END_ROD,
                                x + 0.5,
                                y + 0.5,
                                z + 0.5,
                                0.0, 0.0, 0.0
                        );
                    }
                }
            }
        }
    }

    private void spawnBlockOutlineParticles(World world, BlockPos pos) {
        // 在方塊的八個頂點生成粒子
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();

        world.addParticleClient(ParticleTypes.END_ROD, x, y, z, 0.0, 0.0, 0.0);
        world.addParticleClient(ParticleTypes.END_ROD, x + 1, y, z, 0.0, 0.0, 0.0);
        world.addParticleClient(ParticleTypes.END_ROD, x, y + 1, z, 0.0, 0.0, 0.0);
        world.addParticleClient(ParticleTypes.END_ROD, x + 1, y + 1, z, 0.0, 0.0, 0.0);
        world.addParticleClient(ParticleTypes.END_ROD, x, y, z + 1, 0.0, 0.0, 0.0);
        world.addParticleClient(ParticleTypes.END_ROD, x + 1, y, z + 1, 0.0, 0.0, 0.0);
        world.addParticleClient(ParticleTypes.END_ROD, x, y + 1, z + 1, 0.0, 0.0, 0.0);
        world.addParticleClient(ParticleTypes.END_ROD, x + 1, y + 1, z + 1, 0.0, 0.0, 0.0);
    }
}
