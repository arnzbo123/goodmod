package com.arn.goodmod.entity;

import com.arn.goodmod.init.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.keyframe.event.SoundKeyframeEvent;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.chat.Component;

public abstract class BaseGirlEntity extends PathfinderMob implements GeoEntity {
    public record DialogueOption(Component label, ActionState actionState) {}

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public static final EntityDataAccessor<String> CURRENT_ACTION = SynchedEntityData.defineId(BaseGirlEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<Float> HORNY = SynchedEntityData.defineId(BaseGirlEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Boolean> IS_THRUSTING = SynchedEntityData.defineId(BaseGirlEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Integer> CLOTH_STATE = SynchedEntityData.defineId(BaseGirlEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<String> CUSTOM_SKIN = SynchedEntityData.defineId(BaseGirlEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<Optional<UUID>> PARTNER_UUID = SynchedEntityData.defineId(BaseGirlEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    public static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData.defineId(BaseGirlEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    public static final EntityDataAccessor<Boolean> FOLLOWING = SynchedEntityData.defineId(BaseGirlEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Optional<BlockPos>> HOME_POS = SynchedEntityData.defineId(BaseGirlEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    public static final EntityDataAccessor<Boolean> IS_GOING_HOME = SynchedEntityData.defineId(BaseGirlEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_ON_BED = SynchedEntityData.defineId(BaseGirlEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Optional<BlockPos>> BED_POS = SynchedEntityData.defineId(BaseGirlEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);

    public static java.util.function.Consumer<BaseGirlEntity> dialogueOpener = null;
    public static java.util.function.Consumer<BaseGirlEntity> npcEditorOpener = null;
    public static java.util.function.BiConsumer<BaseGirlEntity, Player> clipboardCopier = null;

    protected int sceneTicks = 0;
    protected int thrustTicks = 0;

    // Client-side tracked bone positions for dynamic camera sync
    public Vec3 clientBoyCamPos = null;
    public Vec3 clientGirlCamPos = null;
    public Vec3 clientHead2Pos = null;

    protected BaseGirlEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new GirlFollowPlayerGoal(this));
        this.goalSelector.addGoal(2, new GirlGoHomeGoal(this));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.15D, Ingredient.of(Items.DIAMOND, Items.EMERALD, Items.GOLD_INGOT), false) {
            @Override
            public boolean canUse() {
                return !isInScene() && !isFollowing() && !isGoingHome() && super.canUse();
            }
        });
        this.goalSelector.addGoal(4, new GirlWanderGoal(this, 0.8D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F) {
            @Override
            public boolean canUse() {
                return !isInScene() && super.canUse();
            }
        });
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this) {
            @Override
            public boolean canUse() {
                return !isInScene() && super.canUse();
            }
        });
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CURRENT_ACTION, ActionState.NULL.name());
        builder.define(HORNY, 0.0F);
        builder.define(IS_THRUSTING, false);
        builder.define(CLOTH_STATE, 0);
        builder.define(CUSTOM_SKIN, "default");
        builder.define(PARTNER_UUID, Optional.empty());
        builder.define(OWNER_UUID, Optional.empty());
        builder.define(FOLLOWING, false);
        builder.define(HOME_POS, Optional.empty());
        builder.define(IS_GOING_HOME, false);
        builder.define(IS_ON_BED, false);
        builder.define(BED_POS, Optional.empty());
    }

    public ActionState getAction() {
        try {
            return ActionState.valueOf(this.entityData.get(CURRENT_ACTION));
        } catch (Exception e) {
            return ActionState.NULL;
        }
    }

    public void setAction(ActionState action) {
        this.entityData.set(CURRENT_ACTION, action.name());
        this.sceneTicks = 0;
        this.clientBoyCamPos = null;
        this.clientGirlCamPos = null;
        this.clientHead2Pos = null;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (CUSTOM_SKIN.equals(key)) {
            resetClientAnimation();
        }
        if (CURRENT_ACTION.equals(key) || PARTNER_UUID.equals(key) || IS_ON_BED.equals(key) || BED_POS.equals(key)) {
            this.sceneTicks = 0;
            this.clientBoyCamPos = null;
            this.clientGirlCamPos = null;
            this.clientHead2Pos = null;

            if (isOnBed()) {
                this.noPhysics = true;
                this.setNoGravity(true);
                getBedPos().ifPresent(bp -> {
                    net.minecraft.world.level.block.state.BlockState bedState = level().getBlockState(bp);
                    if (bedState.getBlock() instanceof net.minecraft.world.level.block.BedBlock) {
                        Direction facing = bedState.getValue(net.minecraft.world.level.block.BedBlock.FACING);
                        BlockPos footPos = bp.relative(facing.getOpposite());
                        float bedYaw = facing.toYRot();
                        double dirX = -Math.sin(Math.toRadians(bedYaw));
                        double dirZ = Math.cos(Math.toRadians(bedYaw));
                        double girlX = footPos.getX() + 0.5D - dirX * 0.75D;
                        double girlZ = footPos.getZ() + 0.5D - dirZ * 0.75D;
                        double targetY = bp.getY();

                        setPos(girlX, targetY, girlZ);
                        this.xo = girlX;
                        this.yo = targetY;
                        this.zo = girlZ;
                        this.xOld = girlX;
                        this.yOld = targetY;
                        this.zOld = girlZ;

                        this.setYRot(bedYaw);
                        this.setYHeadRot(bedYaw);
                        this.setYBodyRot(bedYaw);
                        this.yRotO = bedYaw;
                        this.yHeadRotO = bedYaw;
                        this.yBodyRotO = bedYaw;
                    }
                });
            }

            ActionState action = getAction();
            if (action.isAdultScene() && level().isClientSide) {
                net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                boolean isPartner = (getPartnerUUID().isEmpty() || (mc.player != null && getPartnerUUID().get().equals(mc.player.getUUID())));
                if (isPartner && mc.player != null) {
                    if (!isOnBed()) {
                        boolean faceGirl = ActionState.shouldFaceGirlDirection(action.name());
                        float targetYaw = faceGirl ? mc.player.getYRot() : (mc.player.getYRot() + 180.0F);
                        this.setYRot(targetYaw);
                        this.setYHeadRot(targetYaw);
                        this.setYBodyRot(targetYaw);
                        this.yRotO = targetYaw;
                        this.yHeadRotO = targetYaw;
                        this.yBodyRotO = targetYaw;
                    }

                    com.arn.goodmod.client.ClientGameEvents.bindActiveScene(this, action);
                }
            }
        }
    }

    public void resetClientAnimation() {
        if (this.level().isClientSide()) {
            try {
                AnimatableManager<?> manager = getAnimatableInstanceCache().getManagerForId(getId());
                if (manager != null) {
                    for (AnimationController<?> controller : manager.getAnimationControllers().values()) {
                        controller.forceAnimationReset();
                    }
                }
            } catch (Throwable ignored) {}
        }
    }

    public float getHorny() {
        return this.entityData.get(HORNY);
    }

    public void setHorny(float horny) {
        this.entityData.set(HORNY, Math.max(0.0F, Math.min(100.0F, horny)));
    }

    public boolean isThrusting() {
        return this.entityData.get(IS_THRUSTING);
    }

    public void setThrusting(boolean thrusting) {
        this.entityData.set(IS_THRUSTING, thrusting);
    }

    public int getClothState() {
        return this.entityData.get(CLOTH_STATE);
    }

    public void setClothState(int state) {
        this.entityData.set(CLOTH_STATE, state);
    }

    public String getCustomSkin() {
        return this.entityData.get(CUSTOM_SKIN);
    }

    public void setCustomSkin(String skin) {
        this.entityData.set(CUSTOM_SKIN, (skin == null || skin.trim().isEmpty()) ? "default" : skin.trim());
        resetClientAnimation();
    }

    public void openNpcEditorScreen(Player player) {
        if (npcEditorOpener != null) {
            npcEditorOpener.accept(this);
        }
    }

    public Optional<UUID> getPartnerUUID() {
        return this.entityData.get(PARTNER_UUID);
    }

    public void setPartnerUUID(UUID uuid) {
        this.entityData.set(PARTNER_UUID, Optional.ofNullable(uuid));
    }

    public boolean isFollowing() {
        return this.entityData.get(FOLLOWING);
    }

    public void setFollowing(boolean following) {
        this.entityData.set(FOLLOWING, following);
    }

    public Optional<UUID> getOwnerUUID() {
        return this.entityData.get(OWNER_UUID);
    }

    public void setOwnerUUID(UUID uuid) {
        this.entityData.set(OWNER_UUID, Optional.ofNullable(uuid));
    }

    public Optional<BlockPos> getHomePos() {
        return this.entityData.get(HOME_POS);
    }

    public void setHomePos(BlockPos pos) {
        this.entityData.set(HOME_POS, Optional.ofNullable(pos));
    }

    public boolean hasHome() {
        return this.entityData.get(HOME_POS).isPresent();
    }

    public boolean isGoingHome() {
        return this.entityData.get(IS_GOING_HOME);
    }

    public void setGoingHome(boolean goingHome) {
        this.entityData.set(IS_GOING_HOME, goingHome);
    }

    public boolean isOnBed() {
        return this.entityData.get(IS_ON_BED);
    }

    public void setOnBed(boolean onBed) {
        this.entityData.set(IS_ON_BED, onBed);
    }

    public Optional<BlockPos> getBedPos() {
        return this.entityData.get(BED_POS);
    }

    public void setBedPos(BlockPos pos) {
        this.entityData.set(BED_POS, Optional.ofNullable(pos));
    }

    public static boolean isBedAction(ActionState action) {
        if (action == null) return false;
        String name = action.name();
        return name.contains("DOGGY") || name.contains("COWGIRL") || name.contains("MISSIONARY")
                || name.contains("HUG") || name.contains("ANAL") || name.contains("MATING_PRESS");
    }

    public boolean isInScene() {
        return getAction().isAdultScene();
    }

    public void startScene(ActionState action, Player player) {
        setAction(action);
        setPartnerUUID(player.getUUID());
        setHorny(0.0F);
        setThrusting(false);
        setFollowing(false);
        setGoingHome(false);
        this.setDeltaMovement(Vec3.ZERO);
        this.getNavigation().stop();

        boolean faceGirl = ActionState.shouldFaceGirlDirection(action.name());
        float targetYaw = faceGirl ? player.getYRot() : (player.getYRot() + 180.0F);
        this.setYRot(targetYaw);
        this.setYHeadRot(targetYaw);
        this.setYBodyRot(targetYaw);
        this.yRotO = targetYaw;
        this.yHeadRotO = targetYaw;
        this.yBodyRotO = targetYaw;
    }

    public void stopScene() {
        if (!level().isClientSide && getPartnerUUID().isPresent()) {
            var p = level().getPlayerByUUID(getPartnerUUID().get());
            if (p instanceof net.minecraft.server.level.ServerPlayer sp) {
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(sp,
                        new com.arn.goodmod.network.ModPackets.SyncScenePayload(getId(), ActionState.NULL.name(), Optional.empty()));
            }
        }
        setAction(ActionState.NULL);
        setPartnerUUID(null);
        setThrusting(false);
        setHorny(0.0F);
        setOnBed(false);
        setBedPos(null);
        this.noPhysics = false;
        this.setNoGravity(false);
        this.sceneTicks = 0;
        this.thrustTicks = 0;
        this.clientBoyCamPos = null;
        this.clientGirlCamPos = null;
        this.clientHead2Pos = null;
    }

    public void handleSceneThrust(Player player) {
        if (!isInScene()) return;
        ActionState current = getAction();
        if (current.name().contains("CUM")) return;

        this.thrustTicks = 18;
        setThrusting(true);

        float currentHorny = getHorny();
        float newHorny = Math.min(100.0F, currentHorny + 3.0F);
        setHorny(newHorny);

        // Advance to faster variant if applicable
        ActionState faster = getFasterActionFor(current, newHorny);
        if (faster != null && current != faster) {
            setAction(faster);
            if (!level().isClientSide && getPartnerUUID().isPresent()) {
                var p = level().getPlayerByUUID(getPartnerUUID().get());
                if (p instanceof net.minecraft.server.level.ServerPlayer sp) {
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(sp,
                            new com.arn.goodmod.network.ModPackets.SyncScenePayload(getId(), faster.name(), Optional.of(sp.getUUID())));
                }
            }
        }

        // Play random rhythmic scene sounds & particles
        if (!level().isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level();
            serverLevel.sendParticles(ParticleTypes.HEART, getX(), getY() + 1.2D, getZ(), 2, 0.3D, 0.3D, 0.3D, 0.05D);
            playSceneThrustSound();

            if (newHorny >= 100.0F) {
                handleClimax();
            }
        }
    }

    protected ActionState getFasterActionFor(ActionState current, float horny) {
        boolean fast = horny >= 50.0F;
        return switch (current) {
            case STARTBLOWJOB, SUCKBLOWJOB, SUCKBLOWJOB_BLINK -> fast ? ActionState.THRUSTBLOWJOB : ActionState.SUCKBLOWJOB;
            case STARTDOGGY, WAITDOGGY, DOGGYSTART, DOGGYSLOW -> fast ? ActionState.DOGGYFAST : ActionState.DOGGYSLOW;
            case COWGIRLSTART, COWGIRLSLOW -> fast ? ActionState.COWGIRLFAST : ActionState.COWGIRLSLOW;
            case MISSIONARY_START, MISSIONARY_SLOW -> fast ? ActionState.MISSIONARY_FAST : ActionState.MISSIONARY_SLOW;
            case PAIZURI_START, PAIZURI_IDLE, PAIZURI_SLOW -> fast ? ActionState.PAIZURI_FAST : ActionState.PAIZURI_SLOW;
            case ANAL_START, ANAL_PREPARE, ANAL_WAIT, ANAL_SLOW -> fast ? ActionState.ANAL_FAST : ActionState.ANAL_SLOW;
            case PRONE_DOGGY_INTRO, PRONE_DOGGY_INSERT, PRONE_DOGGY_SOFT -> fast ? ActionState.PRONE_DOGGY_HARD : ActionState.PRONE_DOGGY_SOFT;
            case DEEPTHROAT_START, ALLIE_PREPARE_FIRST_TIME, ALLIE_PREPARE_NORMAL, DEEPTHROAT_SLOW -> fast ? ActionState.DEEPTHROAT_FAST : ActionState.DEEPTHROAT_SLOW;
            case REVERSE_COWGIRL_START, REVERSE_COWGIRL_SLOW -> fast ? ActionState.REVERSE_COWGIRL_FAST_START : ActionState.REVERSE_COWGIRL_SLOW;
            case KOBOLD_ANAL_START, KOBOLD_ANAL_SLOW -> fast ? ActionState.KOBOLD_ANAL_FAST : ActionState.KOBOLD_ANAL_SLOW;
            case MATING_PRESS_START, MATING_PRESS_SOFT -> fast ? ActionState.MATING_PRESS_HARD : ActionState.MATING_PRESS_SOFT;
            case CITIZEN_START, CITIZEN_SLOW -> fast ? ActionState.CITIZEN_FAST : ActionState.CITIZEN_SLOW;
            case COWGIRL_SITTING_INTRO, WAIT_CAT, COWGIRL_SITTING_SLOW -> fast ? ActionState.COWGIRL_SITTING_FAST : ActionState.COWGIRL_SITTING_SLOW;
            case TOUCH_BOOBS_INTRO, TOUCH_BOOBS_SLOW -> fast ? ActionState.TOUCH_BOOBS_FAST : ActionState.TOUCH_BOOBS_SLOW;
            case BREEDING_INTRO_0, BREEDING_INTRO_1, BREEDING_SLOW_0, BREEDING_1 -> fast ? ActionState.BREEDING_FAST_0 : ActionState.BREEDING_SLOW_0;
            case NELSON_INTRO, NELSON_SLOW -> fast ? ActionState.NELSON_FAST : ActionState.NELSON_SLOW;
            case CORRUPT_INTRO, CORRUPT_SLOW -> fast ? ActionState.CORRUPT_FAST : ActionState.CORRUPT_SLOW;
            case RAPE_INTRO, RAPE_PREPARE, RAPE_CHARGE -> ActionState.RAPE_ON_GOING;
            case THREESOME_SLOW -> fast ? ActionState.THREESOME_FAST : ActionState.THREESOME_SLOW;
            default -> current;
        };
    }

    protected void playSceneThrustSound() {
        SoundEvent sound = ModSounds.MISC_POUNDING.value();
        level().playSound(null, getX(), getY(), getZ(), sound, SoundSource.PLAYERS, 0.8F, 0.95F + random.nextFloat() * 0.1F);
    }

    protected void handleClimax() {
        ActionState current = getAction();
        ActionState cumState = getCumStateFor(current);
        if (cumState != null && cumState != ActionState.NULL) {
            setAction(cumState);
            this.sceneTicks = 0;
            setHorny(100.0F);
            if (!level().isClientSide && getPartnerUUID().isPresent()) {
                var p = level().getPlayerByUUID(getPartnerUUID().get());
                if (p instanceof net.minecraft.server.level.ServerPlayer sp) {
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(sp,
                            new com.arn.goodmod.network.ModPackets.SyncScenePayload(getId(), cumState.name(), Optional.of(sp.getUUID())));
                }
            }
        }
    }

    protected ActionState getCumStateFor(ActionState current) {
        String name = current.name();
        if (name.contains("BLOWJOB")) return ActionState.CUMBLOWJOB;
        if (name.contains("PRONE_DOGGY")) return ActionState.PRONE_DOGGY_CUM;
        if (name.contains("DOGGY")) return ActionState.DOGGYCUM;
        if (name.contains("REVERSE_COWGIRL")) return ActionState.REVERSE_COWGIRL_CUM;
        if (name.contains("COWGIRL_SITTING")) return ActionState.COWGIRL_SITTING_CUM;
        if (name.contains("COWGIRL")) return ActionState.COWGIRLCUM;
        if (name.contains("PAIZURI")) return ActionState.PAIZURI_CUM;
        if (name.contains("TOUCH_BOOBS")) return ActionState.TOUCH_BOOBS_CUM;
        if (name.contains("MISSIONARY")) return ActionState.MISSIONARY_CUM;
        if (name.contains("KOBOLD_ANAL")) return ActionState.KOBOLD_ANAL_CUM;
        if (name.contains("ANAL")) return ActionState.ANAL_CUM;
        if (name.contains("DEEPTHROAT")) return ActionState.DEEPTHROAT_CUM;
        if (name.contains("MATING_PRESS")) return ActionState.MATING_PRESS_CUM;
        if (name.contains("CITIZEN")) return ActionState.CITIZEN_CUM;
        if (name.contains("BREEDING")) return ActionState.BREEDING_CUM_0;
        if (name.contains("NELSON")) return ActionState.NELSON_CUM;
        if (name.contains("CORRUPT")) return ActionState.CORRUPT_CUM;
        if (name.contains("RAPE")) return ActionState.RAPE_CUM;
        if (name.contains("THREESOME")) return ActionState.THREESOME_CUM;
        return ActionState.NULL;
    }

    @Override
    public void tick() {
        super.tick();

        if (!hasHome() && !level().isClientSide) {
            setHomePos(blockPosition());
        }

        if (this.thrustTicks > 0) {
            this.thrustTicks--;
            if (this.thrustTicks == 0) {
                setThrusting(false);
            }
        }

        if (getAction() == ActionState.STRIP) {
            this.setDeltaMovement(Vec3.ZERO);
            this.getNavigation().stop();
            this.sceneTicks++;
            if (this.sceneTicks == 60) {
                setClothState(1);
                playLocalSound(ModSounds.MISC_BEDRUSTLE.value(), 0.8F, 1.0F);
            }
            if (this.sceneTicks >= 102) {
                setClothState(1);
                setAction(ActionState.NULL);
                this.sceneTicks = 0;
            }
            return;
        }

        if (isInScene()) {
            this.setDeltaMovement(Vec3.ZERO);
            this.getNavigation().stop();

            if (isOnBed()) {
                this.noPhysics = true;
                this.setNoGravity(true);
                getBedPos().ifPresent(bp -> {
                    net.minecraft.world.level.block.state.BlockState bedState = level().getBlockState(bp);
                    if (bedState.getBlock() instanceof net.minecraft.world.level.block.BedBlock) {
                        Direction facing = bedState.getValue(net.minecraft.world.level.block.BedBlock.FACING);
                        BlockPos footPos = bp.relative(facing.getOpposite());
                        float bedYaw = facing.toYRot();
                        double dirX = -Math.sin(Math.toRadians(bedYaw));
                        double dirZ = Math.cos(Math.toRadians(bedYaw));
                        double girlX = footPos.getX() + 0.5D - dirX * 0.75D;
                        double girlZ = footPos.getZ() + 0.5D - dirZ * 0.75D;
                        double targetY = bp.getY();

                        if (Math.abs(getX() - girlX) > 0.01 || Math.abs(getY() - targetY) > 0.001 || Math.abs(getZ() - girlZ) > 0.01) {
                            setPos(girlX, targetY, girlZ);
                            this.xo = girlX;
                            this.yo = targetY;
                            this.zo = girlZ;
                            this.xOld = girlX;
                            this.yOld = targetY;
                            this.zOld = girlZ;
                        }

                        this.setYRot(bedYaw);
                        this.setYHeadRot(bedYaw);
                        this.setYBodyRot(bedYaw);
                        this.yRotO = bedYaw;
                        this.yHeadRotO = bedYaw;
                        this.yBodyRotO = bedYaw;
                    }
                });
            } else {
                this.setYRot(this.yRotO);
                this.setYHeadRot(this.yRotO);
                this.setYBodyRot(this.yRotO);
            }

            this.sceneTicks++;
            ActionState current = getAction();
            if (current.name().contains("CUM")) {
                if (this.sceneTicks >= 90) {
                    stopScene();
                }
            } else {
                // Natural slow horny decay when idle during scene
                if (!level().isClientSide && this.thrustTicks == 0 && getHorny() > 0.0F) {
                    setHorny(Math.max(0.0F, getHorny() - 0.08F));
                }

                if (current.length > 0 && this.sceneTicks >= current.length) {
                    ActionState followUp = current.getFollowUp();
                    if (followUp != null && followUp != ActionState.NULL) {
                        setAction(followUp);
                        if (!level().isClientSide && getPartnerUUID().isPresent()) {
                            var p = level().getPlayerByUUID(getPartnerUUID().get());
                            if (p instanceof net.minecraft.server.level.ServerPlayer sp) {
                                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(sp,
                                        new com.arn.goodmod.network.ModPackets.SyncScenePayload(getId(), followUp.name(), Optional.of(sp.getUUID())));
                            }
                        }
                    }
                }
            }
        } else {
            if (this.noPhysics) {
                this.noPhysics = false;
                this.setNoGravity(false);
            }
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        ItemStack itemstack = player.getItemInHand(hand);
        ItemStack offstack = player.getItemInHand(InteractionHand.OFF_HAND);
        if (itemstack.getItem() instanceof com.arn.goodmod.item.NpcEditorWandItem || offstack.getItem() instanceof com.arn.goodmod.item.NpcEditorWandItem) {
            if (level().isClientSide) {
                openNpcEditorScreen(player);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }

        if (itemstack.is(Items.DIAMOND) || itemstack.is(Items.EMERALD) || itemstack.is(Items.GOLD_INGOT)) {
            if (!player.getAbilities().instabuild) {
                itemstack.shrink(1);
            }
            setHorny(getHorny() + 25.0F);
            level().playSound(null, getX(), getY(), getZ(), ModSounds.MISC_BELLJINGLE.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
            return InteractionResult.sidedSuccess(level().isClientSide);
        }

        if (!isInScene()) {
            if (level().isClientSide) {
                openDialogueScreen(player);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }

        return super.mobInteract(player, hand);
    }

    public void openDialogueScreen(Player player) {
        if (dialogueOpener != null) {
            dialogueOpener.accept(this);
        }
    }

    public boolean hasOutfitToggle() {
        String girl = getGirlName().toLowerCase();
        return girl.equals("jenny") || girl.equals("ellie") || girl.equals("slime") || girl.equals("bia") || girl.equals("galath");
    }

    public boolean hasStripAnimation() {
        return hasOutfitToggle();
    }

    public void startStripAnimation() {
        setAction(ActionState.STRIP);
        this.sceneTicks = 0;
        this.setDeltaMovement(Vec3.ZERO);
        this.getNavigation().stop();
    }

    public List<DialogueOption> getDialogueOptions() {
        List<DialogueOption> options = new ArrayList<>();
        String girl = getGirlName().toLowerCase();
        switch (girl) {
            case "jenny":
                options.add(new DialogueOption(Component.translatable("action.names.blowjob"), ActionState.STARTBLOWJOB));
                options.add(new DialogueOption(Component.translatable("action.names.boobjob"), ActionState.PAIZURI_START));
                options.add(new DialogueOption(Component.translatable("action.names.doggy"), ActionState.STARTDOGGY));
                break;
            case "ellie":
                options.add(new DialogueOption(Component.translatable("action.names.cowgirl"), ActionState.COWGIRLSTART));
                options.add(new DialogueOption(Component.translatable("action.names.missionary"), ActionState.MISSIONARY_START));
                break;
            case "slime":
                options.add(new DialogueOption(Component.translatable("action.names.blowjob"), ActionState.STARTBLOWJOB));
                options.add(new DialogueOption(Component.translatable("action.names.doggy"), ActionState.STARTDOGGY));
                break;
            case "bia":
                options.add(new DialogueOption(Component.translatable("action.names.anal"), ActionState.ANAL_START));
                options.add(new DialogueOption(Component.translatable("action.names.doggy"), ActionState.PRONE_DOGGY_INTRO));
                options.add(new DialogueOption(Component.translatable("action.names.headpat"), ActionState.HEAD_PAT));
                options.add(new DialogueOption(Component.translatable("action.names.talk"), ActionState.TALK_IDLE));
                break;
            case "bee":
                options.add(new DialogueOption(Component.translatable("action.names.sex"), ActionState.CITIZEN_START));
                break;
            case "cat":
            case "luna":
                options.add(new DialogueOption(Component.translatable("action.names.sex"), ActionState.COWGIRL_SITTING_INTRO));
                options.add(new DialogueOption(Component.translatable("action.names.touchboobs"), ActionState.TOUCH_BOOBS_INTRO));
                options.add(new DialogueOption(Component.translatable("action.names.headpat"), ActionState.HEAD_PAT));
                break;
            case "allie":
                options.add(new DialogueOption(Component.translatable("action.names.deepthroat"), ActionState.DEEPTHROAT_START));
                options.add(new DialogueOption(Component.translatable("action.names.reversecowgirl"), ActionState.REVERSE_COWGIRL_START));
                options.add(new DialogueOption(Component.translatable("action.names.makemerichallie"), ActionState.RICH_NORMAL));
                break;
            case "kobold":
                options.add(new DialogueOption(Component.translatable("action.names.anal"), ActionState.KOBOLD_ANAL_START));
                options.add(new DialogueOption(Component.translatable("action.names.oral"), ActionState.STARTBLOWJOB));
                options.add(new DialogueOption(Component.translatable("action.names.matingpress"), ActionState.MATING_PRESS_START));
                break;
            case "goblin":
                options.add(new DialogueOption(Component.translatable("action.names.breeding"), ActionState.BREEDING_INTRO_0));
                options.add(new DialogueOption(Component.translatable("action.names.boobjob"), ActionState.PAIZURI_START));
                options.add(new DialogueOption(Component.translatable("action.names.nelson"), ActionState.NELSON_INTRO));
                break;
            case "galath":
                options.add(new DialogueOption(Component.translatable("action.names.corrupt"), ActionState.CORRUPT_INTRO));
                options.add(new DialogueOption(Component.translatable("action.names.force"), ActionState.RAPE_INTRO));
                break;
            case "manglelie":
                options.add(new DialogueOption(Component.translatable("action.names.threesome"), ActionState.THREESOME_SLOW));
                options.add(new DialogueOption(Component.translatable("action.names.hug"), ActionState.HUG_MANG));
                break;
            default:
                options.add(new DialogueOption(Component.translatable("action.names.blowjob"), ActionState.STARTBLOWJOB));
                options.add(new DialogueOption(Component.translatable("action.names.doggy"), ActionState.STARTDOGGY));
                break;
        }
        return options;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString("Action", getAction().name());
        compound.putFloat("Horny", getHorny());
        compound.putInt("ClothState", getClothState());
        compound.putString("CustomSkin", getCustomSkin());
        if (getPartnerUUID().isPresent()) {
            compound.putUUID("PartnerUUID", getPartnerUUID().get());
        }
        if (getOwnerUUID().isPresent()) {
            compound.putUUID("OwnerUUID", getOwnerUUID().get());
        }
        compound.putBoolean("Following", isFollowing());
        compound.putBoolean("GoingHome", isGoingHome());
        if (hasHome()) {
            BlockPos home = getHomePos().get();
            compound.putInt("HomeX", home.getX());
            compound.putInt("HomeY", home.getY());
            compound.putInt("HomeZ", home.getZ());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Action")) {
            try {
                setAction(ActionState.valueOf(compound.getString("Action")));
            } catch (Exception ignored) {}
        }
        if (compound.contains("Horny")) {
            setHorny(compound.getFloat("Horny"));
        }
        if (compound.contains("ClothState")) {
            setClothState(compound.getInt("ClothState"));
        }
        if (compound.contains("CustomSkin")) {
            setCustomSkin(compound.getString("CustomSkin"));
        }
        if (compound.hasUUID("PartnerUUID")) {
            setPartnerUUID(compound.getUUID("PartnerUUID"));
        }
        if (compound.hasUUID("OwnerUUID")) {
            setOwnerUUID(compound.getUUID("OwnerUUID"));
        }
        if (compound.contains("Following")) {
            setFollowing(compound.getBoolean("Following"));
        }
        if (compound.contains("GoingHome")) {
            setGoingHome(compound.getBoolean("GoingHome"));
        }
        if (compound.contains("HomeX") && compound.contains("HomeY") && compound.contains("HomeZ")) {
            setHomePos(new BlockPos(compound.getInt("HomeX"), compound.getInt("HomeY"), compound.getInt("HomeZ")));
        }
    }

    public abstract String getGirlName();

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        AnimationController<BaseGirlEntity> controller = new AnimationController<>(this, "controller", 5, this::animPredicate);
        controller.setSoundKeyframeHandler(this::handleSoundKeyframe);
        controllers.add(controller);
    }

    protected void handleSoundKeyframe(SoundKeyframeEvent<BaseGirlEntity> event) {
        String rawSound = event.getKeyframeData().getSound();
        if (rawSound == null || rawSound.isEmpty()) {
            return;
        }

        String sound = rawSound.trim();

        // Control instructions (UI triggers, transitions, state flags)
        if (isControlInstruction(sound)) {
            handleAnimationControlKeyframe(sound);
            return;
        }

        // Voice and mechanical audio dispatch
        dispatchAudioKeyframe(sound);
    }

    protected boolean isControlInstruction(String sound) {
        String lower = sound.toLowerCase();
        return lower.endsWith("done") || lower.contains("msg") || lower.contains("screen")
                || lower.contains("cam") || lower.endsWith("ready") || lower.contains("switch")
                || lower.contains("ui") || lower.contains("render") || lower.contains("item")
                || lower.contains("strip") || lower.contains("reset") || lower.contains("nude")
                || lower.contains("step") || lower.contains("draw") || lower.contains("remove")
                || lower.contains("tp") || lower.contains("particle") || lower.contains("idle")
                || lower.contains("pearl") || lower.contains("cs0") || lower.contains("cs1")
                || lower.contains("cs2") || lower.contains("gonne") || lower.contains("rich_")
                || lower.contains("call_player") || lower.contains("dialog") || lower.contains("talk_")
                || lower.contains("disappear") || lower.contains("goodtiming") || lower.contains("paizuriboth")
                || lower.contains("paizurichoice") || lower.contains("setcoinlook") || lower.contains("throw_away")
                || lower.contains("dress") || lower.contains("undress") || lower.contains("burp");
    }

    protected void handleAnimationControlKeyframe(String sound) {
        String lower = sound.toLowerCase();
        if (lower.contains("becomenude") || lower.contains("setnude") || lower.contains("undress")) {
            setClothState(1);
            playLocalSound(ModSounds.MISC_BEDRUSTLE.value(), 0.8F, 1.0F);
        } else if (lower.contains("stripdone")) {
            setClothState(1);
            if (getAction() == ActionState.STRIP) {
                setAction(ActionState.NULL);
            }
        } else if (lower.contains("dress") && !lower.contains("undress")) {
            setClothState(0);
        }

        if (lower.contains("cumdone") || lower.contains("aftermoan") || lower.contains("cumstart")) {
            if (getHorny() >= 90.0F) {
                setHorny(0.0F);
            }
        }
    }

    protected void dispatchAudioKeyframe(String sound) {
        String girl = getGirlName();
        float volume = 0.9F;
        float pitch = 0.95F + this.random.nextFloat() * 0.1F;

        // 1. Check mechanical action sounds (pounding, slap, touch, bedrustle, etc.)
        SoundEvent miscSound = ModSounds.getMiscActionSound(sound);
        if (miscSound != null) {
            playLocalSound(miscSound, volume, pitch);
            // If it's a cum sound, also layer the girl's orgasm voice
            if (sound.toLowerCase().contains("cum") || sound.toLowerCase().contains("creampie")) {
                SoundEvent voice = ModSounds.getGirlVoice(girl, "orgasm");
                if (voice != null) {
                    playLocalSound(voice, volume, pitch);
                }
            }
            return;
        }

        // 2. Check girl voice sounds (moan, giggle, ahh, mmm, lipsound, breath, etc.)
        SoundEvent voiceSound = ModSounds.getGirlVoice(girl, sound);
        if (voiceSound != null) {
            playLocalSound(voiceSound, volume, pitch);
            return;
        }

        // 3. Fallback generic sound lookup in registry
        SoundEvent fallback = ModSounds.getSound(sound);
        if (fallback != null) {
            playLocalSound(fallback, volume, pitch);
        }
    }

    public void playLocalSound(SoundEvent sound, float volume, float pitch) {
        if (sound == null) return;
        if (this.level().isClientSide()) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), sound, SoundSource.NEUTRAL, volume, pitch, false);
        } else {
            this.level().playSound(null, this.blockPosition(), sound, SoundSource.NEUTRAL, volume, pitch);
        }
    }

    protected PlayState animPredicate(AnimationState<BaseGirlEntity> state) {
        ActionState action = getAction();
        String girlName = getGirlName().toLowerCase();

        if (action == ActionState.STRIP) {
            if (hasStripAnimation()) {
                state.getController().setAnimationSpeed(1.0D);
                return state.setAndContinue(RawAnimation.begin().thenPlay("animation." + girlName + ".strip"));
            }
        }

        if (action == ActionState.NULL || !action.isAdultScene()) {
            if (state.isMoving()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation." + girlName + ".walk"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation." + girlName + ".idle"));
        }

        // Adult scene animation resolution
        String animName = resolveAnimationName(action, girlName);
        if (isThrusting()) {
            state.getController().setAnimationSpeed(getHorny() >= 65.0F ? 1.10D : 0.95D);
        } else {
            state.getController().setAnimationSpeed(0.60D);
        }

        return state.setAndContinue(RawAnimation.begin().thenLoop(animName));
    }

    protected String resolveAnimationName(ActionState action, String girl) {
        if (level().isClientSide()) {
            String customSkin = getCustomSkin();
            if (customSkin != null && !customSkin.isEmpty() && !customSkin.equalsIgnoreCase("default")) {
                String customAnim = com.arn.goodmod.client.skin.CustomSkinManager.resolveCustomAnimation(girl, customSkin, action, isThrusting(), getHorny(), this.sceneTicks);
                if (customAnim != null) {
                    return customAnim;
                }
            }
        }

        String girlSpecificAnim = resolveGirlSpecificAnimation(action, girl);
        if (girlSpecificAnim != null && !girlSpecificAnim.endsWith(".idle")) {
            return girlSpecificAnim;
        }

        if (action.isAdultScene()) {
            return resolveGenericActionAnimation(action, girl);
        }

        return "animation." + girl + ".idle";
    }

    protected String resolveGirlSpecificAnimation(ActionState action, String girl) {
        return switch (girl) {
            case "jenny" -> switch (action) {
                case STRIP -> "animation.jenny.strip";
                case STARTBLOWJOB, SUCKBLOWJOB, SUCKBLOWJOB_BLINK -> "animation.jenny.blowjobsuck";
                case THRUSTBLOWJOB -> "animation.jenny.blowjobthrust";
                case CUMBLOWJOB -> "animation.jenny.blowjobcum";
                case STARTDOGGY, WAITDOGGY, DOGGYSTART -> "animation.jenny.doggystart";
                case DOGGYSLOW -> "animation.jenny.doggyslow";
                case DOGGYFAST -> "animation.jenny.doggyfast_soft";
                case DOGGYCUM -> "animation.jenny.doggycum";
                case PAIZURI_START, PAIZURI_IDLE -> "animation.jenny.paizuri_start";
                case PAIZURI_SLOW -> "animation.jenny.paizuri_slow";
                case PAIZURI_FAST, PAIZURI_FAST_CONTINUES -> "animation.jenny.paizuri_fast";
                case PAIZURI_CUM -> "animation.jenny.paizuri_cum";
                default -> "animation.jenny.idle";
            };
            case "ellie" -> switch (action) {
                case STRIP -> "animation.ellie.strip";
                case COWGIRLSTART -> "animation.ellie.cowgirlstart";
                case COWGIRLSLOW -> "animation.ellie.cowgirlslow2";
                case COWGIRLFAST -> "animation.ellie.cowgirlfast";
                case COWGIRLCUM -> "animation.ellie.cowgirlcum";
                case MISSIONARY_START -> "animation.ellie.missionary_start";
                case MISSIONARY_SLOW -> "animation.ellie.missionary_slow";
                case MISSIONARY_FAST -> "animation.ellie.missionary_fast";
                case MISSIONARY_CUM -> "animation.ellie.missionary_cum";
                case CARRY_INTRO -> "animation.ellie.carry_intro";
                case CARRY_SLOW -> "animation.ellie.carry_slow1";
                case CARRY_FAST -> "animation.ellie.carry_fast";
                case CARRY_CUM -> "animation.ellie.carry_cum";
                default -> "animation.ellie.idle";
            };
            case "slime" -> switch (action) {
                case STRIP -> "animation.slime.strip";
                case STARTBLOWJOB, SUCKBLOWJOB, SUCKBLOWJOB_BLINK -> "animation.slime.blowjobsuck";
                case THRUSTBLOWJOB -> "animation.slime.blowjobthrust";
                case CUMBLOWJOB -> "animation.slime.blowjobcum";
                case STARTDOGGY, WAITDOGGY, DOGGYSTART -> "animation.slime.doggystart";
                case DOGGYSLOW -> "animation.slime.doggyslow";
                case DOGGYFAST -> "animation.slime.doggyfast";
                case DOGGYCUM -> "animation.slime.doggycum";
                default -> "animation.slime.idle";
            };
            case "bia" -> switch (action) {
                case STRIP -> "animation.bia.strip";
                case ANAL_START, ANAL_PREPARE, ANAL_WAIT -> "animation.bia.anal_start";
                case ANAL_SLOW -> "animation.bia.anal_slow";
                case ANAL_FAST -> "animation.bia.anal_fast";
                case ANAL_CUM -> "animation.bia.anal_cum";
                case PRONE_DOGGY_INTRO, PRONE_DOGGY_INSERT -> "animation.bia.prone_doggy_intro";
                case PRONE_DOGGY_SOFT -> "animation.bia.prone_doggy_soft";
                case PRONE_DOGGY_HARD -> "animation.bia.prone_doggy_hard1";
                case PRONE_DOGGY_CUM -> "animation.bia.prone_doggy_cum";
                case HEAD_PAT -> "animation.bia.headpat";
                case TALK_IDLE, TALK_HORNY, TALK_RESPONSE -> "animation.bia.talk_idle";
                default -> "animation.bia.idle";
            };
            case "bee" -> switch (action) {
                case CITIZEN_START -> "animation.bee.sex_start";
                case CITIZEN_SLOW -> "animation.bee.sex_slow";
                case CITIZEN_FAST -> "animation.bee.sex_fast";
                case CITIZEN_CUM -> "animation.bee.sex_cum";
                default -> "animation.bee.idle";
            };
            case "cat", "luna" -> switch (action) {
                case COWGIRL_SITTING_INTRO, WAIT_CAT -> "animation.cat.sitting_intro";
                case COWGIRL_SITTING_SLOW -> "animation.cat.sitting_slow";
                case COWGIRL_SITTING_FAST -> "animation.cat.sitting_fast";
                case COWGIRL_SITTING_CUM -> "animation.cat.sitting_cum";
                case TOUCH_BOOBS_INTRO -> "animation.cat.touch_boobs_intro";
                case TOUCH_BOOBS_SLOW -> "animation.cat.touch_boobs_slow";
                case TOUCH_BOOBS_FAST -> "animation.cat.touch_boobs_fast";
                case TOUCH_BOOBS_CUM -> "animation.cat.touch_boobs_cum";
                case HEAD_PAT -> "animation.cat.head_pat";
                default -> "animation.cat.idle";
            };
            case "allie" -> switch (action) {
                case DEEPTHROAT_START, ALLIE_PREPARE_FIRST_TIME, ALLIE_PREPARE_NORMAL -> "animation.allie.deepthroat_start";
                case DEEPTHROAT_SLOW -> "animation.allie.deepthroat_slow";
                case DEEPTHROAT_FAST -> "animation.allie.deepthroat_fast";
                case DEEPTHROAT_CUM -> "animation.allie.deepthroat_cum";
                case REVERSE_COWGIRL_START -> "animation.allie.reverse_cowgirl_start";
                case REVERSE_COWGIRL_SLOW -> "animation.allie.reverse_cowgirl_slow1";
                case REVERSE_COWGIRL_FAST_START, REVERSE_COWGIRL_FAST_CONTINUES -> "animation.allie.reverse_cowgirl_fasts";
                case REVERSE_COWGIRL_CUM -> "animation.allie.reverse_cowgirl_cum";
                case RICH_FIRST_TIME, RICH_NORMAL -> "animation.allie.rich";
                default -> "animation.allie.idle";
            };
            case "kobold" -> switch (action) {
                case KOBOLD_ANAL_START -> "animation.kobold.analStart";
                case KOBOLD_ANAL_SLOW -> "animation.kobold.analSoft";
                case KOBOLD_ANAL_FAST -> "animation.kobold.analHard";
                case KOBOLD_ANAL_CUM -> "animation.kobold.analCum";
                case STARTBLOWJOB, SUCKBLOWJOB -> "animation.kobold.blowjobStart";
                case THRUSTBLOWJOB -> "animation.kobold.blowjobFast";
                case CUMBLOWJOB -> "animation.kobold.blowjobCum";
                case MATING_PRESS_START -> "animation.kobold.mating_press_start";
                case MATING_PRESS_SOFT -> "animation.kobold.mating_press_soft";
                case MATING_PRESS_HARD -> "animation.kobold.mating_press_hard";
                case MATING_PRESS_CUM -> "animation.kobold.mating_press_cum";
                default -> "animation.kobold.idle";
            };
            case "goblin" -> switch (action) {
                case BREEDING_INTRO_0, BREEDING_INTRO_1, BREEDING_INTRO_2 -> "animation.goblin.breeding_intro_1";
                case BREEDING_SLOW_0, BREEDING_1, BREEDING_SLOW_2 -> "animation.goblin.breeding_slow_1l";
                case BREEDING_FAST_0, BREEDING_FAST_2 -> "animation.goblin.breeding_fast_1s";
                case BREEDING_CUM_0, BREEDING_CUM_1, BREEDING_CUM_2 -> "animation.goblin.breeding_cum_1";
                case PAIZURI_START, PAIZURI_IDLE -> "animation.goblin.paizuri_start";
                case PAIZURI_SLOW -> "animation.goblin.paizuri_slow";
                case PAIZURI_FAST, PAIZURI_FAST_CONTINUES -> "animation.goblin.paizuri_fast";
                case PAIZURI_CUM -> "animation.goblin.paizuri_cum";
                case NELSON_INTRO -> "animation.goblin.nelson_intro";
                case NELSON_SLOW -> "animation.goblin.nelson_slow";
                case NELSON_FAST -> "animation.goblin.nelson_fasts";
                case NELSON_CUM -> "animation.goblin.nelson_cum";
                default -> "animation.goblin.idle";
            };
            case "galath" -> switch (action) {
                case STRIP -> "animation.galath.strip";
                case CORRUPT_INTRO -> "animation.galath.corrupt_intro";
                case CORRUPT_SLOW -> "animation.galath.corrupt_slow";
                case CORRUPT_FAST -> "animation.galath.corrupt_hard";
                case CORRUPT_CUM -> "animation.galath.corrupt_cum";
                case RAPE_INTRO, RAPE_PREPARE, RAPE_CHARGE -> "animation.galath.rape_intro";
                case RAPE_ON_GOING -> "animation.galath.rape1";
                case RAPE_CUM, RAPE_CUM_IDLE -> "animation.galath.rape_cum";
                default -> "animation.galath.idle";
            };
            case "manglelie" -> switch (action) {
                case THREESOME_SLOW -> "animation.shared.double_holding_slow";
                case THREESOME_FAST -> "animation.shared.double_holding_hard";
                case THREESOME_CUM -> "animation.shared.double_holding_cum";
                case HUG_MANG -> "animation.galath.hug_mang";
                default -> "animation.manglelie.idle";
            };
            default -> "animation." + girl + ".idle";
        };
    }

    protected String resolveGenericActionAnimation(ActionState action, String girl) {
        return switch (action) {
            case STARTBLOWJOB, SUCKBLOWJOB, SUCKBLOWJOB_BLINK -> "animation." + girl + ".blowjobsuck";
            case THRUSTBLOWJOB -> "animation." + girl + ".blowjobthrust";
            case CUMBLOWJOB -> "animation." + girl + ".blowjobcum";
            case STARTDOGGY, WAITDOGGY, DOGGYSTART -> "animation." + girl + ".doggystart";
            case DOGGYSLOW -> "animation." + girl + ".doggyslow";
            case DOGGYFAST -> "animation." + girl + ".doggyfast";
            case DOGGYCUM -> "animation." + girl + ".doggycum";
            case PAIZURI_START, PAIZURI_IDLE -> "animation." + girl + ".paizuri_start";
            case PAIZURI_SLOW -> "animation." + girl + ".paizuri_slow";
            case PAIZURI_FAST, PAIZURI_FAST_CONTINUES -> "animation." + girl + ".paizuri_fast";
            case PAIZURI_CUM -> "animation." + girl + ".paizuri_cum";
            case COWGIRLSTART -> "animation." + girl + ".cowgirlstart";
            case COWGIRLSLOW -> "animation." + girl + ".cowgirlslow";
            case COWGIRLFAST -> "animation." + girl + ".cowgirlfast";
            case COWGIRLCUM -> "animation." + girl + ".cowgirlcum";
            case MISSIONARY_START -> "animation." + girl + ".missionary_start";
            case MISSIONARY_SLOW -> "animation." + girl + ".missionary_slow";
            case MISSIONARY_FAST -> "animation." + girl + ".missionary_fast";
            case MISSIONARY_CUM -> "animation." + girl + ".missionary_cum";
            case ANAL_START, ANAL_PREPARE, ANAL_WAIT -> "animation." + girl + ".anal_start";
            case ANAL_SLOW -> "animation." + girl + ".anal_slow";
            case ANAL_FAST -> "animation." + girl + ".anal_fast";
            case ANAL_CUM -> "animation." + girl + ".anal_cum";
            case PRONE_DOGGY_INTRO, PRONE_DOGGY_INSERT -> "animation." + girl + ".prone_doggy_intro";
            case PRONE_DOGGY_SOFT -> "animation." + girl + ".prone_doggy_soft";
            case PRONE_DOGGY_HARD -> "animation." + girl + ".prone_doggy_hard1";
            case PRONE_DOGGY_CUM -> "animation." + girl + ".prone_doggy_cum";
            case HUG, HUG_MANG -> "animation." + girl + ".hug_slow";
            case HEAD_PAT -> "animation." + girl + ".headpat";
            case TALK_IDLE, TALK_HORNY, TALK_RESPONSE -> "animation." + girl + ".talk_idle";
            default -> "animation." + girl + ".idle";
        };
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public static class GirlFollowPlayerGoal extends Goal {
        private final BaseGirlEntity girl;
        private Player owner;
        private int timeToRecalcPath;

        public GirlFollowPlayerGoal(BaseGirlEntity girl) {
            this.girl = girl;
            this.setFlags(java.util.EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.girl.isInScene() || !this.girl.isFollowing() || this.girl.isGoingHome()) {
                return false;
            }
            if (this.girl.getOwnerUUID().isEmpty()) {
                return false;
            }
            this.owner = this.girl.level().getPlayerByUUID(this.girl.getOwnerUUID().get());
            if (this.owner == null || this.owner.isSpectator() || !this.owner.isAlive()) {
                return false;
            }
            return this.girl.distanceToSqr(this.owner) > 6.25D;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.girl.isInScene() || !this.girl.isFollowing() || this.girl.isGoingHome()) {
                return false;
            }
            if (this.owner == null || !this.owner.isAlive() || this.owner.isSpectator()) {
                return false;
            }
            return this.girl.distanceToSqr(this.owner) > 4.0D;
        }

        @Override
        public void start() {
            this.timeToRecalcPath = 0;
        }

        @Override
        public void stop() {
            this.owner = null;
            this.girl.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (this.owner == null) return;
            this.girl.getLookControl().setLookAt(this.owner, 10.0F, (float) this.girl.getMaxHeadXRot());
            double distSqr = this.girl.distanceToSqr(this.owner);

            if (distSqr > 256.0D) {
                tryTeleportNearOwner();
                return;
            }

            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = 10;
                this.girl.getNavigation().moveTo(this.owner, 1.15D);
            }
        }

        private void tryTeleportNearOwner() {
            for (int i = 0; i < 10; i++) {
                int dx = this.girl.getRandom().nextInt(5) - 2;
                int dz = this.girl.getRandom().nextInt(5) - 2;
                int dy = this.girl.getRandom().nextInt(3) - 1;
                BlockPos targetPos = this.owner.blockPosition().offset(dx, dy, dz);
                if (this.girl.level().getBlockState(targetPos).isAir()
                        && !this.girl.level().getBlockState(targetPos.below()).isAir()) {
                    this.girl.moveTo(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D, this.girl.getYRot(), this.girl.getXRot());
                    this.girl.getNavigation().stop();
                    break;
                }
            }
        }
    }

    public static class GirlGoHomeGoal extends Goal {
        private final BaseGirlEntity girl;
        private int timeToRecalcPath;

        public GirlGoHomeGoal(BaseGirlEntity girl) {
            this.girl = girl;
            this.setFlags(java.util.EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.girl.isInScene() || !this.girl.isGoingHome() || !this.girl.hasHome()) {
                return false;
            }
            BlockPos home = this.girl.getHomePos().get();
            return this.girl.blockPosition().distSqr(home) > 4.0D;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.girl.isInScene() || !this.girl.isGoingHome() || !this.girl.hasHome()) {
                return false;
            }
            BlockPos home = this.girl.getHomePos().get();
            return this.girl.blockPosition().distSqr(home) > 4.0D;
        }

        @Override
        public void start() {
            this.timeToRecalcPath = 0;
        }

        @Override
        public void stop() {
            this.girl.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (!this.girl.hasHome()) {
                this.girl.setGoingHome(false);
                return;
            }
            BlockPos home = this.girl.getHomePos().get();
            double distSqr = this.girl.blockPosition().distSqr(home);

            if (distSqr <= 5.0D) {
                this.girl.setGoingHome(false);
                this.girl.getNavigation().stop();
                if (!this.girl.level().isClientSide) {
                    ((ServerLevel) this.girl.level()).sendParticles(
                            ParticleTypes.HEART,
                            this.girl.getX(), this.girl.getY() + 1.2D, this.girl.getZ(),
                            3, 0.3D, 0.3D, 0.3D, 0.05D);
                    this.girl.level().playSound(null, this.girl.blockPosition(),
                            SoundEvents.EXPERIENCE_ORB_PICKUP,
                            SoundSource.NEUTRAL, 0.8F, 1.2F);
                }
                return;
            }

            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = 20;
                this.girl.getNavigation().moveTo(home.getX() + 0.5D, home.getY(), home.getZ() + 0.5D, 1.0D);
            }
        }
    }

    public static class GirlWanderGoal extends WaterAvoidingRandomStrollGoal {
        private final BaseGirlEntity girl;

        public GirlWanderGoal(BaseGirlEntity girl, double speed) {
            super(girl, speed);
            this.girl = girl;
        }

        @Override
        public boolean canUse() {
            return !this.girl.isInScene() && !this.girl.isFollowing() && !this.girl.isGoingHome() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return !this.girl.isInScene() && !this.girl.isFollowing() && !this.girl.isGoingHome() && super.canContinueToUse();
        }

        @Override
        protected Vec3 getPosition() {
            if (this.girl.hasHome()) {
                BlockPos home = this.girl.getHomePos().get();
                if (this.girl.blockPosition().distSqr(home) > 256.0D) {
                    return LandRandomPos.getPosTowards(this.girl, 10, 7, Vec3.atBottomCenterOf(home));
                }
            }
            return super.getPosition();
        }
    }
}
