package com.arn.goodmod.entity.auxiliary;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class EnergyBallEntity extends ThrowableProjectile implements ItemSupplier {
    public EnergyBallEntity(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    public EnergyBallEntity(EntityType<? extends ThrowableProjectile> type, LivingEntity shooter, Level level) {
        super(type, shooter, level);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.FIRE_CHARGE);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide) {
            level().explode(this, getX(), getY(), getZ(), 1.5F, Level.ExplosionInteraction.MOB);
            discard();
        }
    }
}
