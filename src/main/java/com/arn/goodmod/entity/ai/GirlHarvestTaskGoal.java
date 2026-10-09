package com.arn.goodmod.entity.ai;

import com.arn.goodmod.entity.BaseGirlEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

public class GirlHarvestTaskGoal extends Goal {
    public static final int DEFAULT_HARVEST_QUOTA = 16;

    private final BaseGirlEntity girl;
    private BlockPos targetPos = null;
    private int miningTicks = 0;
    private int maxMiningTicks = 30;
    private int stuckTicks = 0;
    private int harvestedBlocks = 0;
    private Vec3 lastPos = Vec3.ZERO;

    public GirlHarvestTaskGoal(BaseGirlEntity girl) {
        this.girl = girl;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!girl.isAlive() || girl.isInScene() || !girl.hasActiveHarvestTask()) {
            return false;
        }
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        this.miningTicks = 0;
        this.stuckTicks = 0;
        this.harvestedBlocks = 0;
        this.lastPos = girl.position();
        findNewTarget();
    }

    @Override
    public void stop() {
        this.targetPos = null;
        this.miningTicks = 0;
        this.stuckTicks = 0;
        this.harvestedBlocks = 0;
        girl.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (girl.level().isClientSide) return;
        ServerLevel level = (ServerLevel) girl.level();

        String taskType = girl.getHarvestTaskType();
        String subtype = girl.getHarvestTaskSubtype();

        // 1. Tool check
        ItemStack requiredTool = girl.findToolForTask(taskType, subtype);
        if (requiredTool.isEmpty()) {
            finishTask(level, "I need a tool in my inventory to do this task! Put one in my inventory~", false);
            return;
        }

        if (BaseGirlEntity.isToolAlmostBroken(requiredTool)) {
            finishTask(level, "My tool is almost broken! I'm stopping to keep it safe for you~", true);
            return;
        }

        // Equip tool to main hand visually and mechanically (without overriding slot if already held)
        if (girl.getItemBySlot(EquipmentSlot.MAINHAND) != requiredTool) {
            girl.setItemSlot(EquipmentSlot.MAINHAND, requiredTool);
        }

        // 2. Storage capacity check
        if (girl.isStorageFull()) {
            finishTask(level, "My inventory is completely full! I'm bringing everything back to you~", true);
            return;
        }

        // 3. Batch Quota check
        if (harvestedBlocks >= DEFAULT_HARVEST_QUOTA) {
            finishTask(level, "I've finished gathering a full batch of " + taskType + " for you! All done~", true);
            return;
        }

        // 4. Validate or find target
        if (targetPos == null || !isValidTargetBlock(level.getBlockState(targetPos), taskType, subtype)) {
            findNewTarget();
            if (targetPos == null) {
                // No more target blocks in area! Task successfully finishes
                finishTask(level, "I've harvested all the " + taskType + " nearby! All finished~", true);
                return;
            }
        }

        // 5. Move or Mine
        double distSq = girl.distanceToSqr(Vec3.atCenterOf(targetPos));
        if (distSq > 9.0D) {
            // Moving towards block
            miningTicks = 0;
            girl.getNavigation().moveTo(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D, 1.15D);

            // Anti-stuck detection
            if (girl.position().distanceToSqr(lastPos) < 0.01D) {
                stuckTicks++;
                if (stuckTicks > 80) {
                    targetPos = null;
                    stuckTicks = 0;
                }
            } else {
                stuckTicks = 0;
                lastPos = girl.position();
            }
        } else {
            // In reach: mine block
            girl.getNavigation().stop();
            girl.getLookControl().setLookAt(targetPos.getX() + 0.5D, targetPos.getY() + 0.5D, targetPos.getZ() + 0.5D, 40.0F, 40.0F);

            // Swing hand and particles every 4 ticks
            if (miningTicks % 4 == 0) {
                girl.swing(InteractionHand.MAIN_HAND);
                BlockState state = level.getBlockState(targetPos);
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state),
                        targetPos.getX() + 0.5D, targetPos.getY() + 0.5D, targetPos.getZ() + 0.5D,
                        6, 0.2D, 0.2D, 0.2D, 0.05D);
                level.playSound(null, targetPos, state.getSoundType().getHitSound(), SoundSource.BLOCKS, 0.8F, 1.0F);
            }

            miningTicks++;
            if (miningTicks >= maxMiningTicks) {
                harvestCurrentBlock(level, targetPos, requiredTool);
                harvestedBlocks++;
                miningTicks = 0;
                targetPos = null;

                // Collect dropped items into storage
                collectNearbyItems(level);

                // Post-harvest checks
                if (harvestedBlocks >= DEFAULT_HARVEST_QUOTA) {
                    finishTask(level, "I've finished gathering a full batch of " + taskType + " for you! All done~", true);
                } else if (girl.isStorageFull()) {
                    finishTask(level, "My inventory is completely full! I'm bringing everything back to you~", true);
                }
            }
        }
    }

    private void harvestCurrentBlock(ServerLevel level, BlockPos pos, ItemStack tool) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return;

        // Calculate drops using girl's tool
        List<ItemStack> drops = Block.getDrops(state, level, pos, null, girl, tool);

        // Break block visually with sound and break particles
        level.playSound(null, pos, state.getSoundType().getBreakSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state),
                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                20, 0.3D, 0.3D, 0.3D, 0.1D);
        level.destroyBlock(pos, false, girl);

        // Deposit drops into girl's storage slots strictly
        for (ItemStack drop : drops) {
            ItemStack remaining = girl.addItemToStorage(drop);
            if (!remaining.isEmpty()) {
                Block.popResource(level, pos, remaining);
            }
        }

        // Tool durability damage with anti-break protection
        if (!tool.isEmpty() && tool.isDamageableItem()) {
            if (tool.getMaxDamage() - tool.getDamageValue() <= 2) {
                finishTask(level, "My tool is almost broken! I'm stopping to keep it safe for you~", true);
                return;
            }
            tool.hurtAndBreak(1, level, null, item -> {
                level.playSound(null, girl.getX(), girl.getY(), girl.getZ(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
            });
        }
    }

    private void collectNearbyItems(ServerLevel level) {
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, girl.getBoundingBox().inflate(3.5D));
        for (ItemEntity itemEntity : items) {
            if (itemEntity.isAlive() && !itemEntity.hasPickUpDelay()) {
                ItemStack stack = itemEntity.getItem();
                ItemStack remaining = girl.addItemToStorage(stack);
                if (remaining.isEmpty()) {
                    itemEntity.discard();
                    level.playSound(null, girl.getX(), girl.getY(), girl.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.4F, 1.4F);
                } else {
                    itemEntity.setItem(remaining);
                }
            }
        }
    }

    public void finishTask(ServerLevel level, String message, boolean success) {
        girl.stopHarvestTask();
        this.targetPos = null;
        this.miningTicks = 0;
        this.stuckTicks = 0;
        this.harvestedBlocks = 0;
        girl.getNavigation().stop();

        if (success) {
            level.playSound(null, girl.getX(), girl.getY(), girl.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 0.8F, 1.2F);
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, girl.getX(), girl.getY() + 1.0D, girl.getZ(), 10, 0.3D, 0.3D, 0.3D, 0.02D);
        } else {
            level.playSound(null, girl.getX(), girl.getY(), girl.getZ(), SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 1.0F, 1.0F);
            level.sendParticles(ParticleTypes.SMOKE, girl.getX(), girl.getY() + girl.getEyeHeight(), girl.getZ(), 6, 0.2D, 0.2D, 0.2D, 0.02D);
        }

        // Return to partner player or home
        Player partnerPlayer = null;
        if (girl.getPartnerUUID().isPresent()) {
            partnerPlayer = level.getPlayerByUUID(girl.getPartnerUUID().get());
        }

        if (partnerPlayer != null) {
            girl.getNavigation().moveTo(partnerPlayer, 1.2D);
            if (message != null && !message.isEmpty()) {
                partnerPlayer.sendSystemMessage(Component.literal("§d<" + girl.getGirlName() + "> §a" + message));
            }
        } else if (girl.getHomePos().isPresent()) {
            BlockPos home = girl.getHomePos().get();
            girl.getNavigation().moveTo(home.getX() + 0.5D, home.getY(), home.getZ() + 0.5D, 1.15D);
        }
    }

    private void findNewTarget() {
        ServerLevel level = (ServerLevel) girl.level();
        String taskType = girl.getHarvestTaskType();
        String subtype = girl.getHarvestTaskSubtype();
        BlockPos origin = girl.blockPosition();

        int radius = 16;
        BlockPos bestPos = null;
        double bestDist = Double.MAX_VALUE;

        for (int y = -3; y <= 6; y++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos p = origin.offset(x, y, z);
                    BlockState s = level.getBlockState(p);
                    if (isValidTargetBlock(s, taskType, subtype)) {
                        double d = girl.distanceToSqr(Vec3.atCenterOf(p));
                        if (d < bestDist) {
                            bestDist = d;
                            bestPos = p;
                        }
                    }
                }
            }
        }

        this.targetPos = bestPos;
        if (bestPos != null) {
            BlockState st = level.getBlockState(bestPos);
            float hardness = st.getDestroySpeed(level, bestPos);
            this.maxMiningTicks = Math.max(12, Math.min(60, (int) (hardness * 15)));
        }
    }

    private boolean isValidTargetBlock(BlockState state, String taskType, String subtype) {
        if (state.isAir()) return false;
        String blockId = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath().toLowerCase();

        if ("wood".equalsIgnoreCase(taskType)) {
            if (!state.is(BlockTags.LOGS)) return false;
            if (subtype != null && !subtype.trim().isEmpty()) {
                return blockId.contains(subtype.toLowerCase().trim());
            }
            return true;
        }

        if ("stone".equalsIgnoreCase(taskType)) {
            if (subtype != null && !subtype.trim().isEmpty()) {
                return blockId.contains(subtype.toLowerCase().trim());
            }
            return state.is(BlockTags.BASE_STONE_OVERWORLD) || blockId.contains("stone") || blockId.contains("deepslate") || blockId.contains("cobblestone");
        }

        if ("ore".equalsIgnoreCase(taskType)) {
            if (subtype != null && !subtype.trim().isEmpty()) {
                return blockId.contains(subtype.toLowerCase().trim()) && blockId.contains("ore");
            }
            return state.is(BlockTags.COAL_ORES) || state.is(BlockTags.IRON_ORES) || state.is(BlockTags.COPPER_ORES)
                    || state.is(BlockTags.GOLD_ORES) || state.is(BlockTags.REDSTONE_ORES) || state.is(BlockTags.LAPIS_ORES)
                    || state.is(BlockTags.DIAMOND_ORES) || state.is(BlockTags.EMERALD_ORES);
        }

        return false;
    }
}
