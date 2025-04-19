package studio.vy.item.custom;

import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
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

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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

    private List<BlockPos> getPositionsToBreak(BlockPos min, BlockPos max, ItemStack stack, World world) {
        if (!(world instanceof ServerWorld)) return new ArrayList<>();

        // 獲取玩家位置作為參考點
        List<BlockPos> allPositions = new ArrayList<>();
        Map<BlockPos, Double> blockProbabilities = new HashMap<>();
        PlayerEntity player = world.getClosestPlayer(
                (min.getX() + max.getX()) / 2.0,
                (min.getY() + max.getY()) / 2.0,
                (min.getZ() + max.getZ()) / 2.0,
                100.0,
                false
        );

        if (player == null) return new ArrayList<>();
        BlockPos playerPos = player.getBlockPos();

        // 收集所有可挖掘的方塊
        for (int y = min.getY(); y <= max.getY(); y++) {
            for (int x = min.getX(); x <= max.getX(); x++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = world.getBlockState(pos);
                    if (isCorrectForDrops(stack, state) && !state.isAir()) {
                        allPositions.add(pos);

                        // 計算與玩家的距離作為基礎機率
                        double distance = pos.getSquaredDistance(playerPos);
                        double probability = 1.0 / (1.0 + distance * 0.1);

                        // 如果是效率型方塊，提高機率
                        if (state.isIn(ModTags.Blocks.HAMMER_EFFICIENCY)) {
                            probability *= 1.5;
                        }

                        blockProbabilities.put(pos, probability);
                    }
                }
            }
        }

        // 根據工具材質調整機率
        float orderFactor = switch (toolMaterial.toString().toLowerCase()) {
            case "netherite" -> 1.0f;
            case "diamond" -> 0.8f;
            case "gold" -> 0.6f;
            case "iron" -> 0.4f;
            case "copper" -> 0.3f;
            case "stone" -> 0.2f;
            case "wood" -> 0.1f;
            default -> 0.5f;
        };

        // 根據工具完成率計算目標數量
        float completionRate = getCompletionRate(stack);
        int targetSize = (int) (allPositions.size() * completionRate);

        // 根據機率排序方塊
        List<BlockPos> sortedPositions = allPositions.stream()
                .sorted((a, b) -> {
                    double probA = blockProbabilities.get(a) * (1.0 - orderFactor + Math.random() * orderFactor);
                    double probB = blockProbabilities.get(b) * (1.0 - orderFactor + Math.random() * orderFactor);
                    return Double.compare(probB, probA);
                })
                .toList();

        // 返回排序後的指定數量方塊
        return sortedPositions.subList(0, Math.min(targetSize, sortedPositions.size()));
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        if (!(miner instanceof PlayerEntity player)) {
            return false;
        }

        if (!world.isClient && begin != null && end != null) {
            BlockPos min = BlockPos.min(begin, end);
            BlockPos max = BlockPos.max(begin, end);

            // 每次挖掘時重新計算可挖掘的方塊
            List<BlockPos> positions = getPositionsToBreak(min, max, stack, world);
            
            // 如果還有可挖掘的方塊
            if (!positions.isEmpty()) {
                for (BlockPos currentPos : positions) {
                    BlockState currentState = world.getBlockState(currentPos);
                    if (isCorrectForDrops(stack, currentState)) {
                        world.breakBlock(currentPos, true, player);
                        stack.damage(1, player, EquipmentSlot.MAINHAND);

                        if (stack.getDamage() + 1 >= stack.getMaxDamage()) {
                            return true;
                        }
                    }
                }
            }

            // 檢查區域是否已完全挖掘完成
            if (isAreaFullyMined(stack, world, min, max)) {
                begin = null;
                end = null;
            }
        }

        return true;
    }

    private boolean isAreaFullyMined(ItemStack stack, World world, BlockPos min, BlockPos max) {
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    BlockPos currentPos = new BlockPos(x, y, z);
                    BlockState state = world.getBlockState(currentPos);
                    if (isCorrectForDrops(stack, state)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }


    private float getCompletionRate(ItemStack stack) {
        float baseRate = switch (toolMaterial.toString().toLowerCase()) {
            case "netherite" -> 1.0f;
            case "diamond" -> 0.9f;
            case "gold" -> 0.86f;
            case "iron" -> 0.8f;
            case "copper" -> 0.4f;
            case "stone" -> 0.2f;
            case "wood" -> 0.1f;
            default -> 0.5f;
        };

        AtomicReference<Float> finalRate = new AtomicReference<>(baseRate);

        EnchantmentHelper.getEnchantments(stack).getEnchantments().forEach(enchantmentRegistryEntry -> {
            if (enchantmentRegistryEntry.getKey().toString().contains("efficiency")) {
                int level = stack.getEnchantments().getLevel(enchantmentRegistryEntry);
                finalRate.set(Math.min(1.0f, baseRate + level*0.05f));
            }
            if (enchantmentRegistryEntry.getKey().toString().contains("silk_touch")) {
                int level = stack.getEnchantments().getLevel(enchantmentRegistryEntry);
                finalRate.set(Math.min(1.0f, finalRate.get() +level*0.2f));
            }
        });


        return finalRate.get();
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
            if (begin != null) {
                // Check if selected area is within range limits
                int maxRange = getMaxMiningRange(context.getStack());
                BlockPos min = BlockPos.min(begin, blockPos);
                BlockPos max = BlockPos.max(begin, blockPos);

                int xDiff = Math.abs(max.getX() - min.getX()) + 1;
                int yDiff = Math.abs(max.getY() - min.getY()) + 1;
                int zDiff = Math.abs(max.getZ() - min.getZ()) + 1;

                if (xDiff > maxRange || yDiff > maxRange || zDiff > maxRange) {
                    player.sendMessage(
                            Text.translatable("message.blossom.hammer.range_exceeded", maxRange, maxRange, maxRange)
                                    .styled(style -> style.withColor(0xFF0000)),
                            true
                    );
                    return ActionResult.FAIL;
                }
            }

            end = blockPos;
            player.sendMessage(getPosition(), true);
            return ActionResult.SUCCESS;
        } else {
            begin = blockPos;
            player.sendMessage(getPosition(), true);
            return ActionResult.SUCCESS;
        }
    }

    private int getMaxMiningRange(ItemStack stack) {
        // 設定每種材質的最大挖掘範圍（邊長）
        int baseRange = switch (toolMaterial.toString().toLowerCase()) {
            case "netherite" -> 24; // 24x24x24
            case "diamond" -> 20;   // 20x20x20
            case "gold" -> 16;      // 16x16x16
            case "iron" -> 12;      // 12x12x12
            case "copper" -> 8;     // 8x8x8
            case "stone" -> 4;      // 4x4x4
            case "wood" -> 2;       // 2x2x2
            default -> 12;
        };

        AtomicInteger finalRange = new AtomicInteger(baseRange);

        EnchantmentHelper.getEnchantments(stack).getEnchantments().forEach(enchantmentRegistryEntry -> {
            if (enchantmentRegistryEntry.getKey().toString().contains("efficiency")) {
                int level = stack.getEnchantments().getLevel(enchantmentRegistryEntry);
                finalRange.addAndGet(level * 2);
            }
        });

        return finalRange.get();
    }

    private Text getPosition() {
        if (begin == null) {
            return Text.empty()
                    .append(Text.translatable("message.blossom.hammer.select.start"))
                    .append(getBlockPosString(end));
        }
        if (end == null) {
            return Text.empty()
                    .append(Text.translatable("message.blossom.hammer.select.end"))
                    .append(getBlockPosString(begin));
        }
        return Text.empty()
                .append(Text.translatable("message.blossom.hammer.select.range"))
                .append(getBlockPosString(begin))
                .append(Text.translatable("message.blossom.hammer.select.to"))
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
        if (slot == null || !(entity instanceof PlayerEntity) || world.isClient() || (begin == null && end == null)) {
            return;
        }

        spawnOutlineParticles(world);
    }

    private void spawnOutlineParticles(ServerWorld world) {
        if (begin==null) begin=end;
        if (end==null) end=begin;
        BlockPos min = BlockPos.min(begin, end);
        BlockPos max = BlockPos.max(begin, end);
        // X軸方向的4條線
        for (double x = min.getX()-0.5; x <= max.getX()+0.5; x += 0.25) {
            spawnParticle(world, x, min.getY() - 0.5, min.getZ() - 0.5);
            spawnParticle(world, x, min.getY() - 0.5, max.getZ() + 0.5);
            spawnParticle(world, x, max.getY() + 0.5, min.getZ() - 0.5);
            spawnParticle(world, x, max.getY() + 0.5, max.getZ() + 0.5);
        }
        // Y軸方向的4條線
        for (double y = min.getY()-0.5; y <= max.getY()+0.5; y += 0.25) {
            spawnParticle(world, min.getX() - 0.5, y, min.getZ() - 0.5);
            spawnParticle(world, min.getX() - 0.5, y, max.getZ() + 0.5);
            spawnParticle(world, max.getX() + 0.5, y, min.getZ() - 0.5);
            spawnParticle(world, max.getX() + 0.5, y, max.getZ() + 0.5);
        }
        // Z軸方向的4條線
        for (double z = min.getZ()-0.5; z <= max.getZ()+0.5; z += 0.25) {
            spawnParticle(world, min.getX() - 0.5, min.getY() - 0.5, z);
            spawnParticle(world, min.getX() - 0.5, max.getY() + 0.5, z);
            spawnParticle(world, max.getX() + 0.5, min.getY() - 0.5, z);
            spawnParticle(world, max.getX() + 0.5, max.getY() + 0.5, z);
        }
    }

    private void spawnParticle(ServerWorld world, double x, double y, double z) {
        world.spawnParticles(
                ParticleTypes.ELECTRIC_SPARK,
                x + 0.5,
                y + 0.5,
                z + 0.5,
                1,
                0.0, 0.0, 0.0,
                0.0
        );
    }
}
