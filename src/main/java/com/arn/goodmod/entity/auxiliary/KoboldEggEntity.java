package com.arn.goodmod.entity.auxiliary;

import com.arn.goodmod.init.ModEntities;
import com.arn.goodmod.init.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class KoboldEggEntity extends PathfinderMob implements GeoEntity {
    public static final int TOTAL_HATCH_TIME = 1200;
    private static final EntityDataAccessor<Integer> HATCH_TIME = SynchedEntityData.defineId(KoboldEggEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public KoboldEggEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HATCH_TIME, TOTAL_HATCH_TIME);
    }

    public int getHatchTime() {
        return this.entityData.get(HATCH_TIME);
    }

    public void setHatchTime(int time) {
        this.entityData.set(HATCH_TIME, time);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            int time = getHatchTime() - 1;
            setHatchTime(time);
            if (time <= 0) {
                hatch();
            }
        }
    }

    private void hatch() {
        var kobold = ModEntities.KOBOLD.get().create(level());
        if (kobold != null) {
            kobold.moveTo(getX(), getY(), getZ(), getYRot(), getXRot());
            level().addFreshEntity(kobold);
        }
        discard();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isInvulnerableTo(source)) return false;
        if (!level().isClientSide && !isRemoved()) {
            if (source.getEntity() instanceof Player player && !player.getAbilities().instabuild) {
                spawnAtLocation(ModItems.KOBOLD_EGG_ITEM.get());
            }
            discard();
            return true;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("HatchTime", getHatchTime());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("HatchTime")) {
            setHatchTime(compound.getInt("HatchTime"));
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "egg_controller", 5, state -> {
            int time = getHatchTime();
            int elapsed = TOTAL_HATCH_TIME - time;
            if (time <= 20) {
                return state.setAndContinue(RawAnimation.begin().thenPlay("animation.model.hatch"));
            }
            float progress = (float) elapsed / (float) TOTAL_HATCH_TIME;
            if (progress > 0.98f) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.model.veryfast"));
            } else if (progress > 0.85f) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.model.fast"));
            } else if (progress > 0.75f) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.model.medium"));
            } else if (progress > 0.50f) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.model.slow"));
            } else {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.model.null"));
            }
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
