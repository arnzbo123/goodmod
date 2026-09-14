package com.arn.goodmod.item;

import com.arn.goodmod.client.renderer.ModItemRenderers;
import com.arn.goodmod.init.ModEntities;
import com.arn.goodmod.init.ModSounds;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.function.Consumer;

public class AlliesLampItem extends Item implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public AlliesLampItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            Vec3 look = player.getLookAngle();
            Vec3 spawnPos = player.position().add(look.x * 2.0D, 0.0D, look.z * 2.0D);

            serverLevel.sendParticles(ParticleTypes.ENCHANTED_HIT, spawnPos.x, spawnPos.y + 1.0D, spawnPos.z, 30, 0.5D, 0.5D, 0.5D, 0.1D);
            level.playSound(null, spawnPos.x, spawnPos.y, spawnPos.z, ModSounds.MISC_BELLJINGLE.value(), SoundSource.PLAYERS, 1.0F, 1.0F);

            var allie = ModEntities.ALLIE.get().create(level);
            if (allie != null) {
                allie.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, player.getYRot() + 180.0F, 0.0F);
                level.addFreshEntity(allie);
            }

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.allies_lamp.desc", "Rub the lamp to summon Allie! (Single use)"));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> PlayState.CONTINUE));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            @Override
            public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                return ModItemRenderers.getAlliesLampRenderer();
            }
        });
    }
}
