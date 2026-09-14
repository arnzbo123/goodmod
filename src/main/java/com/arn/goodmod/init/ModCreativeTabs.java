package com.arn.goodmod.init;

import com.arn.goodmod.GoodMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, GoodMod.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> GOODMOD_TAB = CREATIVE_MODE_TABS.register("goodmod_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.goodmod"))
                    .icon(() -> new ItemStack(ModItems.ALLIES_LAMP.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.ALLIES_LAMP.get());
                        output.accept(ModItems.DRAGON_STAFF.get());
                        output.accept(ModItems.GALATH_COIN.get());
                        output.accept(ModItems.KOBOLD_EGG_ITEM.get());
                        output.accept(ModItems.LUNA_ROD.get());
                        output.accept(ModItems.NPC_EDITOR_WAND.get());
                        output.accept(ModItems.TRIBE_EGG.get());
                        output.accept(ModItems.WINCHESTER.get());

                        // Spawn Eggs
                        output.accept(ModItems.JENNY_SPAWN_EGG.get());
                        output.accept(ModItems.ELLIE_SPAWN_EGG.get());
                        output.accept(ModItems.SLIME_SPAWN_EGG.get());
                        output.accept(ModItems.BIA_SPAWN_EGG.get());
                        output.accept(ModItems.BEE_SPAWN_EGG.get());
                        output.accept(ModItems.LUNA_SPAWN_EGG.get());
                        output.accept(ModItems.ALLIE_SPAWN_EGG.get());
                        output.accept(ModItems.KOBOLD_SPAWN_EGG.get());
                        output.accept(ModItems.GOBLIN_SPAWN_EGG.get());
                        output.accept(ModItems.GALATH_SPAWN_EGG.get());
                        output.accept(ModItems.MANGLELIE_SPAWN_EGG.get());
                    })
                    .build());
}
