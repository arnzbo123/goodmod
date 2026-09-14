package com.arn.goodmod.entity.girls;

import com.arn.goodmod.entity.BaseGirlEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;

public class GalathEntity extends BaseGirlEntity {
    public GalathEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    public String getGirlName() {
        return "galath";
    }
}
