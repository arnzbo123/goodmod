package com.arn.goodmod.item;

import com.arn.goodmod.entity.BaseGirlEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class NpcEditorWandItem extends Item {
    public NpcEditorWandItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        if (interactionTarget instanceof BaseGirlEntity girl) {
            if (player.level().isClientSide) {
                girl.openNpcEditorScreen(player);
            }
            return InteractionResult.sidedSuccess(player.level().isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (entity instanceof BaseGirlEntity girl) {
            if (player.level().isClientSide && BaseGirlEntity.clipboardCopier != null) {
                BaseGirlEntity.clipboardCopier.accept(girl, player);
            }
            player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5F, 1.5F);
            return true; // Cancel attack
        }
        return super.onLeftClickEntity(stack, player, entity);
    }
}
