package com.arn.goodmod.init;

import com.arn.goodmod.GoodMod;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, GoodMod.MODID);
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(BuiltInRegistries.POTION, GoodMod.MODID);

    public static final DeferredHolder<MobEffect, MobEffect> HORNY_EFFECT = MOB_EFFECTS.register("horny",
            () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xFF69B4) {});

    public static final DeferredHolder<Potion, Potion> HORNY_POTION = POTIONS.register("horny_potion",
            () -> new Potion(new MobEffectInstance(HORNY_EFFECT, 3600)));
}
