package com.arn.goodmod.init;

import com.arn.goodmod.GoodMod;
import com.arn.goodmod.entity.BaseGirlEntity;
import com.arn.goodmod.entity.auxiliary.EnergyBallEntity;
import com.arn.goodmod.entity.auxiliary.FriendlySlimeEntity;
import com.arn.goodmod.entity.auxiliary.KoboldEggEntity;
import com.arn.goodmod.entity.girls.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = GoodMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, GoodMod.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<JennyEntity>> JENNY = ENTITIES.register("jenny",
            () -> EntityType.Builder.of(JennyEntity::new, MobCategory.CREATURE).sized(0.49F, 1.95F).build("jenny"));

    public static final DeferredHolder<EntityType<?>, EntityType<EllieEntity>> ELLIE = ENTITIES.register("ellie",
            () -> EntityType.Builder.of(EllieEntity::new, MobCategory.CREATURE).sized(0.49F, 1.95F).build("ellie"));

    public static final DeferredHolder<EntityType<?>, EntityType<SlimeGirlEntity>> SLIME = ENTITIES.register("slime",
            () -> EntityType.Builder.of(SlimeGirlEntity::new, MobCategory.CREATURE).sized(0.49F, 1.8F).build("slime"));

    public static final DeferredHolder<EntityType<?>, EntityType<BiaEntity>> BIA = ENTITIES.register("bia",
            () -> EntityType.Builder.of(BiaEntity::new, MobCategory.CREATURE).sized(0.49F, 1.65F).build("bia"));

    public static final DeferredHolder<EntityType<?>, EntityType<BeeEntity>> BEE = ENTITIES.register("bee",
            () -> EntityType.Builder.of(BeeEntity::new, MobCategory.CREATURE).sized(0.3F, 1.5F).build("bee"));

    public static final DeferredHolder<EntityType<?>, EntityType<LunaEntity>> LUNA = ENTITIES.register("luna",
            () -> EntityType.Builder.of(LunaEntity::new, MobCategory.CREATURE).sized(0.49F, 1.7F).build("luna"));

    public static final DeferredHolder<EntityType<?>, EntityType<AllieEntity>> ALLIE = ENTITIES.register("allie",
            () -> EntityType.Builder.of(AllieEntity::new, MobCategory.CREATURE).sized(0.49F, 1.8F).build("allie"));

    public static final DeferredHolder<EntityType<?>, EntityType<KoboldEntity>> KOBOLD = ENTITIES.register("kobold",
            () -> EntityType.Builder.of(KoboldEntity::new, MobCategory.CREATURE).sized(0.5F, 0.99F).build("kobold"));

    public static final DeferredHolder<EntityType<?>, EntityType<GoblinEntity>> GOBLIN = ENTITIES.register("goblin",
            () -> EntityType.Builder.of(GoblinEntity::new, MobCategory.CREATURE).sized(0.5F, 1.2F).build("goblin"));

    public static final DeferredHolder<EntityType<?>, EntityType<GalathEntity>> GALATH = ENTITIES.register("galath",
            () -> EntityType.Builder.of(GalathEntity::new, MobCategory.CREATURE).sized(0.49F, 1.8F).build("galath"));

    public static final DeferredHolder<EntityType<?>, EntityType<ManglelieEntity>> MANGLELIE = ENTITIES.register("manglelie",
            () -> EntityType.Builder.of(ManglelieEntity::new, MobCategory.CREATURE).sized(0.49F, 1.95F).build("manglelie"));

    public static final DeferredHolder<EntityType<?>, EntityType<KoboldEggEntity>> KOBOLD_EGG = ENTITIES.register("kobold_egg",
            () -> EntityType.Builder.of(KoboldEggEntity::new, MobCategory.CREATURE).sized(0.5F, 0.5F).build("kobold_egg"));

    public static final DeferredHolder<EntityType<?>, EntityType<FriendlySlimeEntity>> FRIENDLY_SLIME = ENTITIES.register("friendly_slime",
            () -> EntityType.Builder.of(FriendlySlimeEntity::new, MobCategory.CREATURE).sized(0.51F, 0.51F).build("friendly_slime"));

    public static final DeferredHolder<EntityType<?>, EntityType<EnergyBallEntity>> ENERGY_BALL = ENTITIES.register("energy_ball",
            () -> EntityType.Builder.<EnergyBallEntity>of(EnergyBallEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).build("energy_ball"));

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(JENNY.get(), BaseGirlEntity.createAttributes().build());
        event.put(ELLIE.get(), BaseGirlEntity.createAttributes().build());
        event.put(SLIME.get(), BaseGirlEntity.createAttributes().build());
        event.put(BIA.get(), BaseGirlEntity.createAttributes().build());
        event.put(BEE.get(), BaseGirlEntity.createAttributes().build());
        event.put(LUNA.get(), BaseGirlEntity.createAttributes().build());
        event.put(ALLIE.get(), BaseGirlEntity.createAttributes().build());
        event.put(KOBOLD.get(), BaseGirlEntity.createAttributes().build());
        event.put(GOBLIN.get(), BaseGirlEntity.createAttributes().build());
        event.put(GALATH.get(), BaseGirlEntity.createAttributes().build());
        event.put(MANGLELIE.get(), BaseGirlEntity.createAttributes().build());
        event.put(KOBOLD_EGG.get(), BaseGirlEntity.createAttributes().build());
        event.put(FRIENDLY_SLIME.get(), BaseGirlEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void registerSpawnPlacements(net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent event) {
        event.register(JENNY.get(), net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND, net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, net.minecraft.world.entity.Mob::checkMobSpawnRules, net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ELLIE.get(), net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND, net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, net.minecraft.world.entity.Mob::checkMobSpawnRules, net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(SLIME.get(), net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND, net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, net.minecraft.world.entity.Mob::checkMobSpawnRules, net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(BIA.get(), net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND, net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, net.minecraft.world.entity.Mob::checkMobSpawnRules, net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(BEE.get(), net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND, net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, net.minecraft.world.entity.Mob::checkMobSpawnRules, net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(LUNA.get(), net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND, net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, net.minecraft.world.entity.Mob::checkMobSpawnRules, net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ALLIE.get(), net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND, net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, net.minecraft.world.entity.Mob::checkMobSpawnRules, net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(KOBOLD.get(), net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND, net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, net.minecraft.world.entity.Mob::checkMobSpawnRules, net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(GOBLIN.get(), net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND, net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, net.minecraft.world.entity.Mob::checkMobSpawnRules, net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(GALATH.get(), net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND, net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, net.minecraft.world.entity.Mob::checkMobSpawnRules, net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(MANGLELIE.get(), net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND, net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, net.minecraft.world.entity.Mob::checkMobSpawnRules, net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }
}
